package reuze.aifiles;

import java.util.ArrayList;
import java.util.Stack;
import com.software.reuze.gb_Vector3;
import com.software.reuze.m_MathUtils;

public class dg_GameSession {
	public final static int BONUS_LIFE_SCORE=10000;
	public final static float AI_MAX_SPEED_TRY =80.0f;
	public final static int MAX_SHOT_LEVEL=3;
	public static final int ASTEROID_SCORE_VAL=100;
	public static final float BULLET_SPEED=150.0f;
	public static final float APPROACH_DIST    =180.0f; //for AI FSM
	//constructor/main functions

	//controls
	public static final int
	CONTROL_THRUST_ON=1,
	CONTROL_THRUST_REVERSE=2,
	CONTROL_THRUST_OFF=3,
	CONTROL_RIGHT_ON=4,
	CONTROL_LEFT_ON=5,
	CONTROL_STOP_TURN=6,
	CONTROL_STOP=7,
	CONTROL_SHOOT=8,
	CONTROL_HYPERSPACE=9,
	CONTROL_PAUSE=10,
	CONTROL_STEP=11,
	CONTROL_AI_ON=12,
	CONTROL_AI_OFF=13,
	CONTROL_COUNT=14;

	//score functions
	public void IncrementScore(int inc)	{m_score += inc;}
	public void ResetScore()				{m_score = 0;m_bonusScore = BONUS_LIFE_SCORE;}

	//game related fucntions

	//data
	dg_Ship         m_mainShip;
	dg_ControlHuman m_humanControl;
	dg_ControlAI    m_AIControl;

	int  m_bonusScore;
	float m_respawnTimer;
	float m_powerupTimer;
	States  m_state;
	int  m_score;
	int  m_numLives;
	int  m_waveNumber;
	boolean m_AIOn;
	int  m_timeScale;

	public static enum States
	{
		STATE_PLAY,
		STATE_PAUSE,
		STATE_NEXTWAVE,
		STATE_GAMEOVER,
		STATE_STEP,
		STATE_STEPWAIT
	};

	ArrayList<dg_GameObject> m_activeObj;
	static Stack<dg_GameObject> m_pending;
	static Stack<dg_GameObject> m_deleted;
	public dg_GameSession()
	{
		m_AIOn	       = true;
		m_timeScale    = 1;
		m_activeObj=new ArrayList<dg_GameObject>();
		if (m_pending==null) m_pending=new Stack<dg_GameObject>();
		else throw new RuntimeException("pending not empty");
		if (m_deleted==null) m_deleted=new Stack<dg_GameObject>();
		else throw new RuntimeException("deleted not empty");
		m_humanControl = new dg_ControlHuman(null);
		m_AIControl    = new dg_ControlAIFSM();
	}
	public void Clip(gb_Vector3 p)
	{
		if(p.x > z_app.app.width) p.x -=z_app.app.width; 
		if(p.y > z_app.app.height) p.y -=z_app.app.height; 
		if(p.x < 0)         p.x +=z_app.app.width; 
		if(p.y < 0)         p.y +=z_app.app.height; 
	}
	public void StartGame()
	{
		m_mainShip		= null;
		m_powerupTimer	= 0.2f;
		m_waveNumber	= 1;
		m_numLives		= 3;
		dg_Asteroid.ZeroNumber();
		m_respawnTimer  = -1;
		m_activeObj.clear();
		if (!m_pending.empty()) throw new RuntimeException("pending != 0 in start");
		m_pending.clear();
		if (!m_deleted.empty()) throw new RuntimeException("deleted != 0 in start");
		m_deleted.clear();
		m_AIControl.Init();
		ResetScore();
		StartNextWave();
	}
	public void StartNextWave()
	{
		m_state			= States.STATE_PLAY;
		LaunchAsteroidWave();	
	}
	public void LaunchAsteroidWave()
	{
		dg_Asteroid a;
		for(int i=0;i<2*m_waveNumber;i++)
		{
			a=new dg_Asteroid(8*(1+m_MathUtils.random(10)));
			do
			{
				a.m_position.x= (float) (Math.random()*z_app.app.width);	
				a.m_position.y= (float) (Math.random()*z_app.app.height);
			}
			while( m_mainShip!=null && a.IsColliding(m_mainShip));
			a.m_position.z= 0;
			a.m_velocity.x= (float) (Math.random()*70 - 30);	
			a.m_velocity.y= (float) (Math.random()*70 - 30);
			a.m_velocity.z= 0;
			AddGameObj(a);
		}
	}
	public static void AddGameObj(dg_GameObject obj)
	{
		m_pending.add(obj);
	}
	public void Draw() {
		z_app.app.pushStyle();
		switch(m_state)
		{
		case STATE_STEPWAIT:
		case STATE_STEP:
		case STATE_PLAY:
			for(dg_GameObject li:m_activeObj) {
				if (li.m_active) li.Draw();
			}
			z_app.app.fill(0,255,0);
			z_app.app.text("Score: "+m_score+" Wave: "+m_waveNumber,5,z_app.app.height-20);
			DrawLives();
			if(m_AIOn)//debug text drawing
			{
				z_app.app.text("AI ON "+m_AIControl.m_debugTxt,z_app.app.width/4,z_app.app.height-20);
			}
			break;
		case STATE_PAUSE:
			z_app.app.text("Game Paused, Press p",5,z_app.app.height-20);
			break;
		case STATE_GAMEOVER:
			z_app.app.text("FINAL SCORE: "+m_score+" Start Again(y/n)",5,z_app.app.height-20);
			break;
		case STATE_NEXTWAVE:
			z_app.app.text("Wave Completed, Press Space Bar",5,z_app.app.height-20);
			if(m_AIOn) StartNextWave();
			break;
		default:
			break;
		}
		z_app.app.popStyle();
	}
	public void DrawLives()
	{
		z_app.app.fill(0,255,0);
		z_app.app.text("Lives: "+m_numLives,z_app.app.width-60,z_app.app.height-20);

	}
	public void Update(float dt)
	{
		m_activeObj.addAll(m_pending);
		m_pending.clear();
		for (dg_GameObject list1:m_activeObj)
		{
			//update logic and positions
			if (!list1.m_active) continue;
			list1.Update(dt);
			Clip(list1.m_position);	    		
			//check for collisions
			if (list1.m_collisionFlags != dg_GameObject.OBJ_NONE)
			{
				for(dg_GameObject list2:m_activeObj)
				{
					//the first obj may have already collided with something, making it inactive
					if (!(list1.m_active&list2.m_active)) continue;

					//don't collide with yourself
					if (list1 == list2) continue;

					if ((list1.m_collisionFlags & list2.m_type)==list2.m_type && 
							list1.IsColliding(list2)) 
					{
						list1.DoCollision(list2);
					}
				} //for
			}
		}
		m_activeObj.removeAll(m_deleted);
		m_deleted.clear();
		//check for no main ship, respawn
		if (m_mainShip == null || m_respawnTimer>=0.0f)
		{
			m_respawnTimer-=dt;
			if(m_respawnTimer<0.0f)
			{
				m_mainShip = new dg_Ship();	
				AddGameObj(m_mainShip);
				m_humanControl.SetShip(m_mainShip);
				m_AIControl.SetShip(m_mainShip);
			}
		}

		//occasionally spawn a powerup
		m_powerupTimer-=dt;
		if (m_powerupTimer <0.0f)
		{
			m_powerupTimer = (float) (Math.random()*6.0f + 4.0f);
			dg_Powerup pow = new dg_Powerup();
			pow.m_position.x= (float) (Math.random()*z_app.app.width);	
			pow.m_position.y= (float) (Math.random()*z_app.app.height); 
			pow.m_position.z= 0;
			pow.m_velocity.x= (float) (Math.random()*40 - 20);	
			pow.m_velocity.y= (float) (Math.random()*40 - 20);
			pow.m_velocity.z= 0;
			AddGameObj(pow);
		}

		//check for additional life bonus each 10K points
		if(m_score >= m_bonusScore)
		{
			m_numLives++;
			m_bonusScore += BONUS_LIFE_SCORE;
		}

		//check for finished wave
		if(dg_Asteroid.GetNumber()<=0 && m_state==States.STATE_PLAY)
		{
			m_waveNumber++;
			WaveOver();
		}

		//check for finished game, and reset
		if (m_numLives<=0)
			GameOver();

		m_humanControl.Update(dt);

		//update AI control, if turned on
		if(m_AIOn) m_AIControl.Update(dt);
	}
	public void UseControl(int control)
	{
		if(m_mainShip==null)// && !Game.m_AIOn)
			return;
		switch(control)
		{
		case CONTROL_THRUST_ON:
			m_mainShip.ThrustOn();
			break;
		case CONTROL_THRUST_REVERSE:
			m_mainShip.ThrustReverse();
			break;
		case CONTROL_THRUST_OFF:
			m_mainShip.ThrustOff();
			break;
		case CONTROL_RIGHT_ON:
			m_mainShip.TurnRight();
			break;
		case CONTROL_LEFT_ON:
			m_mainShip.TurnLeft();
			break;
		case CONTROL_STOP_TURN:
			m_mainShip.StopTurn();
			break;
		case CONTROL_STOP:
			m_mainShip.Stop();
			break;
		case CONTROL_SHOOT:
			m_mainShip.Shoot();
			break;
		case CONTROL_HYPERSPACE:
			m_mainShip.Hyperspace();
			break;
		case CONTROL_PAUSE:
			if(m_state == States.STATE_PLAY)
				m_state = States.STATE_PAUSE;
			else
				m_state = States.STATE_PLAY;
			break;
		case CONTROL_STEP:
			if(m_state == States.STATE_STEPWAIT || m_state == States.STATE_STEP)
				m_state = States.STATE_PLAY;
			else
				m_state = States.STATE_STEP;
			break;
		case CONTROL_AI_ON:
			m_AIOn = true;
			break;
		case CONTROL_AI_OFF:
			m_AIOn = false;
			break;
		default:
			break;
		}
	}

	public void GameOver()
	{
		//kill everything
		m_activeObj.clear();
		m_state = States.STATE_GAMEOVER;
	}
	boolean RemoveBulletOrExplosion(dg_GameObject obj)
	{
		if(obj.m_type == obj.OBJ_BULLET || obj.m_type == obj.OBJ_EFFECT)
		{
			m_deleted.add(obj);
			obj.m_active=false;
			return true;
		}
		return false;
	}
	public void WaveOver()
	{
		//kill all the bullets and explosions
		for (dg_GameObject list1:m_activeObj) RemoveBulletOrExplosion(list1);

		//reset the main ship stuff
		if (m_mainShip!=null) m_mainShip.Init();
		m_state = States.STATE_NEXTWAVE;
	}
	//---------------------------------------------------------
	public void Kill(dg_GameObject ship)
	{
		if(ship==null) return;
		ship.m_active=false;
		if (ship == m_mainShip)
		{
			m_mainShip = null;
			m_humanControl.m_ship = null;
			m_AIControl.m_ship = null;
			m_numLives--;
		}
		m_deleted.add(ship);
		//go through the obj list and make sure nobody is still pointing to
		//this ship
		if (ship.m_type==ship.OBJ_SHIP)
			for(dg_GameObject list1:m_activeObj)
			{
				if(list1.m_type == list1.OBJ_BULLET && ((dg_Bullet)list1).m_parent == ship)
					((dg_Bullet)list1).m_parent = null;
			}

	}

	//---------------------------------------------------------
	dg_GameObject GetClosestGameObj(dg_GameObject obj, int type)
	{
		//go through the list, find the closest object of param "type"
		//to the param "obj"
		float closeDist = 100000000.0f;
		dg_GameObject closeObj = null;

		for (dg_GameObject list1:m_activeObj)
		{
			//watch out for yourself
			if(list1 == obj || !list1.m_active)
				continue;
			//TODO Distance should be an object method        
	        //our "distance apart" should take into account our size
			float combinedSize = list1.m_size + obj.m_size;
			float dist = list1.m_position.dst2(obj.m_position)-combinedSize*combinedSize;
			if(list1.m_type == type && dist< closeDist)
			{
				closeDist = dist;
				closeObj = list1;
			}
		}
		return closeObj;

	}

	//---------------------------------------------------------
	dg_GameObject GetClosestGameObj(gb_Vector3 point, int type)
	{
		//go through the list, find the closest object of param "type"
		//to the param "point"
		float closeDist = 100000000.0f;
		dg_GameObject closeObj = null;

		for(dg_GameObject list1:m_activeObj)
		{
			if (!list1.m_active) continue;
			float dist = list1.m_position.dst2(point)-list1.m_size*list1.m_size;
			if (list1.m_type == type && dist< closeDist)
			{
				closeDist = dist;
				closeObj = list1;
			}
		}
		return closeObj;

	}

	//---------------------------------------------------------
	int GetNumGameObj(int type)
	{
		//go through the list, count up all the objects of param "type"
		//to the param "point"
		int count = 0;
		for(dg_GameObject list1:m_activeObj)
		{
			if (list1.m_type == type)
				count++;
		}
		return count;

	}

	//---------------------------------------------------------
	void ApplyForce(int type, gb_Vector3 force, float dt)
	{
		for(dg_GameObject list1:m_activeObj)
		{
			if(list1.m_type != type)
				continue;

			list1.m_velocity.add(force.tmp().mul(dt));
		}
	}

	//---------------------------------------------------------
	void ApplyForce(int type,gb_Vector3 p1, gb_Vector3 p2, gb_Vector3 force, float dt)
	{
		for(dg_GameObject list1:m_activeObj)
		{
			if(list1.m_type != type)
				continue;

			//check if the object is colliding with 
			//the force line segment
			if(list1.IsIntersecting(p1,p2))
				list1.m_velocity.add(force.tmp().mul(dt));
		}
	}
}
