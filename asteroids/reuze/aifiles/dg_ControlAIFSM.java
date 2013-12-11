package reuze.aifiles;

import com.software.reuze.gb_Vector3;
import com.software.reuze.m_InterpolateLerp;

public class dg_ControlAIFSM extends dg_ControlAI {
	public static final float POWERUP_SCAN_DIST=180.0f;
		//constructor/functions
		public dg_ControlAIFSM() {this(null);}
		
		//perception data 
		dg_GameObject	m_nearestAsteroid;
		dg_GameObject	m_nearestPowerup;
		float       m_nearestAsteroidDist;
		float       m_nearestPowerupDist;
		boolean        m_willCollide;
		boolean        m_powerupNear;
		float       m_safetyRadius;
	    float       m_maxSpeed;
		//data
		dg_FSMMachine m_machine;
		public dg_ControlAIFSM(dg_Ship ship) {
			super(ship);
			    //construct the state machine and add the necessary states
			    m_machine = new dg_FSMMachine(dg_FSMState.Types.FSM_MACH_MAINSHIP,this);
			    dg_StateApproach approach = new dg_StateApproach(this);
			    m_machine.AddState(approach);
			    m_machine.AddState(new dg_StateAttack(this));
			    m_machine.AddState(new dg_StateEvade(this));
			    m_machine.AddState(new dg_StateGetPowerup(this));
			    dg_StateIdle idle = new dg_StateIdle(this);
			    m_machine.AddState(idle);
			    //m_machine.SetDefaultState(idle);
			    m_machine.SetDefaultState(approach);
			    m_machine.Reset();
			}

			//---------------------------------------------------------
			public void Init()
			{
			    m_willCollide  = false;
			    m_powerupNear  = false;
			    m_nearestAsteroid = null;
			    m_nearestPowerup  = null;
			    m_safetyRadius    = 15.0f;
			    m_maxSpeed        = z_app.game.AI_MAX_SPEED_TRY;///Game.m_timeScale;
			    
			    if(m_target==null)
			    {
			        m_target = new dg_Target(1);
			        z_app.game.AddGameObj(m_target);
			    }
			}

			//---------------------------------------------------------
			public void Update(float dt)
			{
			    if(m_ship==null)
			    {
			        m_machine.Reset();
			        return;
			    }
			    
			    UpdatePerceptions(dt);
			    m_machine.Update(dt);
			}

			//---------------------------------------------------------
			public void UpdatePerceptions(float dt)
			{
			    if(m_willCollide)
			        m_safetyRadius = 30.0f;
			    else
			        m_safetyRadius = 15.0f;
			    
			    //store closest asteroid and powerup
			    m_nearestAsteroid = z_app.game.GetClosestGameObj(m_ship,m_ship.OBJ_ASTEROID);
			    m_nearestPowerup  = z_app.game.GetClosestGameObj(m_ship,m_ship.OBJ_POWERUP);
			    
			    //asteroid collision determination
			    m_willCollide = false;
			    if(m_nearestAsteroid!=null)
			    {
			        float speed = m_ship.m_velocity.len();
			        m_nearestAsteroidDist = m_nearestAsteroid.m_position.dst(m_ship.m_position);
			        gb_Vector3 normDelta = m_nearestAsteroid.m_position.tmp().sub(m_ship.m_position);
			        normDelta.nor();
			        float astSpeed = m_nearestAsteroid.m_velocity.len();
			        float shpSpeedAdj = m_ship.UnitVectorVelocity().dot(normDelta)*speed;
			        normDelta.inv();
			        float astSpeedAdj = m_nearestAsteroid.UnitVectorVelocity().dot(normDelta)*astSpeed;
			        speed = shpSpeedAdj+astSpeedAdj;

//			        if(speed > astSpeed)
//			            dotVel  = DOT(m_ship.UnitVectorVelocity(),normDelta);
//			        else 
//			        {
//			            speed = astSpeed;
//			            dotVel = DOT(m_nearestAsteroid.UnitVectorVelocity(),-normDelta);
//			        }
			        float spdAdj = m_InterpolateLerp.lerp(speed/m_maxSpeed,0.0f,90.0f);
			        float adjSafetyRadius = m_safetyRadius+spdAdj+m_nearestAsteroid.m_size;
			        
			        //if you're too close, and I'm heading somewhat towards you,
			        //flag a collision
			        if(m_nearestAsteroidDist <= adjSafetyRadius && speed > 0)
			            m_willCollide = true;
			    }

			    //powerup near determination
			    m_powerupNear = false;
			    if(m_nearestPowerup!=null)
			    {
			        m_nearestPowerupDist = m_nearestPowerup.m_position.dst(m_ship.m_position);
			        if(m_nearestPowerupDist <= POWERUP_SCAN_DIST)
			        {
			            m_powerupNear     = true;
			        }
			    }
			    
			}

}
