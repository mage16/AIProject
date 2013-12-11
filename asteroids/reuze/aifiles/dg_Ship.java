package reuze.aifiles;

import com.software.reuze.gb_Vector3;
import com.software.reuze.m_MathUtils;

public class dg_Ship extends dg_GameObject {
	public final static int MAX_SHIP_SPEED   =120;
	public final static int MAX_AG_SHIP_SPEED=120;
	public final static int MAX_TRACTOR_DIST =180;
	public final static int MAX_TRACTOR_POWER=300;
		//constructor/functions
		public dg_Ship() {this(6);}
		public dg_Ship(float size) {
			super(size);
			m_shotPowerLevel   = 0;
			m_type             = OBJ_SHIP;
			m_collisionFlags   = OBJ_POWERUP;
			Init();
		}
		@Override
		public void Draw() {
			z_app.app.pushStyle();
			z_app.app.pushMatrix();
			z_app.app.stroke(255);
			z_app.app.translate(m_position.x, m_position.y);
			z_app.app.rotate((float) (2*Math.PI-Math.toRadians(m_angle)));
			z_app.app.translate(-m_size, -m_size);
			z_app.app.line(0, 0, m_size*2, m_size);
			z_app.app.line(m_size*2, m_size, 0, m_size*2);
			z_app.app.line(m_size * 0.2f, m_size * 0.2f, m_size*0.2f, m_size*1.8f);
			z_app.app.ellipseMode(z_app.app.CENTER);
			z_app.app.ellipse(m_size*2, m_size, 4,4);
			if (m_thrust) {
				z_app.app.stroke(m_MathUtils.random(127,255),m_MathUtils.random(0,127),0);
				z_app.app.strokeWeight(m_size*3/2);
				z_app.app.line(-m_size * 0.2f, m_size,-m_size * 0.2f-5, m_size);
			}
			z_app.app.popMatrix();
			z_app.app.popStyle();
			//TODO not complete
		}
		public void Init() {
			m_invincibilityTimer= 3.0f;
			m_thrust           = false;
			m_revThrust        = false;
			m_position		   = new gb_Vector3(z_app.app.width/2,z_app.app.height/2,0);
			m_velocity		   = new gb_Vector3(0,0,0);
			m_angle			   = 0;
			m_angVelocity	   = 0;
			m_activeBulletCount= 0;
		    m_agThrust         = false;
		    m_tractor          = false;
		    m_agNorm           = new gb_Vector3(0,0,0);
		    m_tractorNorm      = new gb_Vector3(0,0,0);
		    m_shotPowerLevel   = 0;		    
		}
		@Override
		public gb_Vector3 Update(float dt) {
		    if (m_thrust || m_revThrust)
		    {
		    	m_acceleration=UnitVectorFacing().mul(m_thrust?MAX_SHIP_SPEED:-MAX_SHIP_SPEED);
		    } else m_acceleration=gb_Vector3.ZERO;

		    if(m_agThrust)
		        AGMove(dt);
		    m_agNorm.set(0,0,0);

		    if(m_tractor)
		        ApplyTractorBeam(dt);
		    
		    if(m_invincibilityTimer > 0)
		        m_invincibilityTimer -= dt;
		    return super.Update(dt);
		}
		@Override
		public boolean IsColliding(dg_GameObject obj) {
			if (obj instanceof dg_Powerup)
			//only collide if you're not invincible
		    if(m_invincibilityTimer > 0) return false;
		    return super.IsColliding(obj);
		}
		@Override
		public void DoCollision(dg_GameObject obj) {
			if (obj.m_type==OBJ_POWERUP) {
				GetPowerup(((dg_Powerup)obj).m_powerType);
				super.DoCollision(obj);
				return;
			}
			//just take myself out
			z_app.RespawnTimer();
		    z_app.Kill(this);
			super.DoCollision(obj);
		}
		
		//controls
		public void ThrustOn()     {m_thrust=true;m_revThrust=false;}
		public void ThrustReverse(){m_revThrust=true;m_thrust=false;}
		public void ThrustOff()    {m_thrust=false;m_revThrust=false;}
		public void TurnLeft() {
			m_angVelocity =  Math.max(120.0f,320.0f/z_app.GetTimeScale());
		}	
		public void TurnRight() {
			m_angVelocity = Math.min(-120.0f,-320.0f/z_app.GetTimeScale());
		}	
		public void StopTurn()		{m_angVelocity =    0.0f;}
		public void Stop() {
			m_velocity.set(0,0,0);
			m_angVelocity =    0.0f;
		}
	    public void Hyperspace() {
	    	m_position.x = (float) (Math.random()*z_app.app.width);
	        m_position.y = (float) (Math.random()*z_app.app.height);
	    }

	    public void TractorBeamOn(gb_Vector3 offset) {
	    	m_tractor = true;
	        m_tractorNorm = offset.cpy().nor();
	    }
	    public void StopTractorBeam(){m_tractor = false;}
	    public void ApplyTractorBeam(float dt) {
	    	//is anybody intersecting the tractor line?
	        //have to pass back to Game object, which is keeper of 
	        //all the objects in the game world
	    	gb_Vector3 temp = m_tractorNorm.tmp().mul(MAX_TRACTOR_POWER);
	    	gb_Vector3 endOfTractor = m_position.cpy().add(temp);
	        z_app.ApplyForce(OBJ_POWERUP,m_position,endOfTractor,temp, dt);
	    }
	    public void AGThrustOn(gb_Vector3 offset) {
	    	m_agThrust = true;
	        m_agNorm = offset.cpy().nor();
	    }
	    public void AGThrustAccumulate(gb_Vector3 offset) {
	    	m_agThrust = true;
	        m_agNorm.add(offset);
	    }
	    public void StopAGThrust()   {m_agThrust = false;}
	    public void AGMove(float dt) {
	    	//anti-gravity move, no acceleration or velocity
	        //directly affects position
	        m_position.add(m_agNorm.tmp().nor().mul(dt*MAX_AG_SHIP_SPEED));
	    }
	    public gb_Vector3 GetAGvector() {return m_agNorm;}
	    public boolean IsThrustOn() {return m_thrust;}
	    public boolean IsTurningRight() {
	    	return m_angVelocity < 0.0f;
	    }
	    public boolean IsTurningLeft() {
	    	return m_angVelocity > 0.0f;
	    }

		//Powerup management
		public void GetPowerup(int powerupType) {
			switch(powerupType) {
		    case dg_Powerup.POWERUP_SHOT:
		        if(m_shotPowerLevel < z_app.game.MAX_SHOT_LEVEL) 
		            m_shotPowerLevel++;
		        break;
		    default:
		        break;
		    }
		}
		public int GetShotLevel() {return m_shotPowerLevel;}
		public int GetNumBullets() {return m_activeBulletCount;}
		public void IncNumBullets(int num) {m_activeBulletCount+=num;}
		public void IncNumBullets() {m_activeBulletCount++;}
		public void MakeInvincible(float time) {m_invincibilityTimer = time;}

		//bullet management
		public int MaxBullet() {
			int num = 0;
			switch(m_shotPowerLevel)
			{
				case 3:
					num = 25;
					break;
				case 2:
					num = 20;
					break;
				case 1:
					num = 15;
					break;
				case 0:
				default:
					num = 10;
					break;
			}
			return num;
		}
		@Override
		public void Action(dg_GameObject obj, String action) {
			if (m_activeBulletCount > 0) m_activeBulletCount--;
		}
		public void Shoot(float angle) {
			if(angle == -1)
		        angle = m_angle;
		    
			if(m_activeBulletCount > MaxBullet())
		  		return;
			
			dg_Bullet bb;
			switch(m_shotPowerLevel)
			{
				case 3:
					m_activeBulletCount+=4;
					bb =new dg_Bullet(this,m_position,angle-180.0f);
					dg_GameSession.AddGameObj(bb);
					bb =new dg_Bullet(this,m_position,angle);
					dg_GameSession.AddGameObj(bb);
					bb =new dg_Bullet(this,m_position,angle-90.0f);
					dg_GameSession.AddGameObj(bb);
					bb =new dg_Bullet(this,m_position,angle+90.0f);
					dg_GameSession.AddGameObj(bb);
					break;
				case 2:
					m_activeBulletCount+=3;
					bb =new dg_Bullet(this,m_position,angle-180.0f);
					dg_GameSession.AddGameObj(bb);
					bb =new dg_Bullet(this,m_position,angle);
					dg_GameSession.AddGameObj(bb);
					bb =new dg_Bullet(this,m_position,angle-90.0f);
					dg_GameSession.AddGameObj(bb);
					break;
				case 1:
					m_activeBulletCount+=2;
					bb =new dg_Bullet(this,m_position,angle-180.0f);
					dg_GameSession.AddGameObj(bb);
					bb =new dg_Bullet(this,m_position,angle);
					dg_GameSession.AddGameObj(bb);
					break;
				default:
					m_activeBulletCount++;
					bb =new dg_Bullet(this,m_position,angle);
					dg_GameSession.AddGameObj(bb);
					break;
			} //switch
		}
		public void Shoot() {Shoot(-1);}
		public float GetClosestGunAngle(float angle) {
			if(Math.abs(angle)< 45 || m_shotPowerLevel == 0)
		        return m_angle;
		    if(Math.abs(angle)> 135 && m_shotPowerLevel > 0)
		        return m_angle+180;
		    if(angle < 0 && m_shotPowerLevel >1)
		        return m_angle-90;
		    if(angle > 0 && m_shotPowerLevel >2)
		        return m_angle+90;		    
		    return m_angle;
		}
	    public float GetClosestGunApproachAngle(float angle) {
	    	if(Math.abs(angle) > 90 && m_shotPowerLevel != 0)
	            return m_angle+180;
	        return m_angle;
	    }
	        
		//data
		dg_Control m_control;
		int		m_activeBulletCount;
		boolean	m_thrust;
	    boolean	m_revThrust;
	    boolean	m_agThrust;
	    boolean	m_tractor;
		int		m_shotPowerLevel;
		float   m_invincibilityTimer;
	    gb_Vector3 m_agNorm;
	    gb_Vector3 m_tractorNorm;
}
