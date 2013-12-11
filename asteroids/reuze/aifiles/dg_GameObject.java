package reuze.aifiles;

import com.software.reuze.gb_Sphere;
import com.software.reuze.gb_Vector3;
import com.software.reuze.m_MathUtils;

public abstract class dg_GameObject {
	public final static int NO_LIFE_TIMER=99999;
		//constructors/functions
	    public dg_GameObject(float _size)
	    {
	    	m_position		= new gb_Vector3(0,0,0);
	        m_velocity		= new gb_Vector3(0,0,0);
	        Init(_size);
	    }

		public dg_GameObject() {this(1);}
	    public dg_GameObject(final gb_Vector3 _p, float _angle, float size) {
	    	m_position		= _p;
	        m_velocity		= new gb_Vector3(0,0,0);
	        m_angle			= _angle;
	        Init(size);
	    }
	    public dg_GameObject(final gb_Vector3 _p, float _angle, final gb_Vector3 _v) {
	    	m_position		= _p;
	        m_velocity		= _v;
	    	m_angle			= _angle;
	        Init(1);
	    }
		public abstract void Draw();
		public void Init(float size) {
			m_id = ++id;
			m_acceleration = new gb_Vector3(0,0,0);
		    m_angVelocity	= 0;
		    m_axis			= new gb_Vector3(0,0,1);
		    m_active		= true;
		    m_size			= size;
		    m_boundSphere	= new gb_Sphere(m_position, size);
		    m_collisionFlags= OBJ_NONE;
		    m_type			= OBJ_NONE;
		    m_lifeTimer		= NO_LIFE_TIMER;
		}
		public gb_Vector3 Update(float dt) {
			m_velocity.add(m_acceleration.tmp().mul(dt));
			//don't clamp bullets
			if((m_type & OBJ_BULLET)==0)
				gb_Vector3.clamp(m_velocity,0.0f,z_app.game.AI_MAX_SPEED_TRY);

		    m_position.add(m_velocity.tmp().mul(dt));

		    m_angle     += dt*m_angVelocity;
		    m_angle      = m_MathUtils.clamp180(m_angle);

		    if(m_position.z != 0.0f) m_position.z = 0.0f;
		    
		    if(m_lifeTimer != NO_LIFE_TIMER)
		    {
		        m_lifeTimer -= dt;
		        if (m_lifeTimer<0.0f) { 
		            z_app.Kill(this);
		        }
		    }
		    return m_position;
		}
		public void Action(dg_GameObject obj, String action) { }
		public boolean IsColliding(dg_GameObject obj) {
			m_boundSphere.center		 = m_position;
			obj.m_boundSphere.center = obj.m_position;
			boolean b=m_boundSphere.intersects(obj.m_boundSphere);
			if (m_type==OBJ_BULLET && obj.m_type==OBJ_ASTEROID) System.out.println(b+" "+m_boundSphere+" "+obj.m_boundSphere+" "+obj.m_id);
			return b;
		}
		public void DoCollision(dg_GameObject obj) {
			obj.Explode();
			z_app.Kill(obj);
		}
		public void Explode() {
			dg_Effect e = null;
			switch(m_type)
			{
				case OBJ_ASTEROID:
		        case OBJ_SHIP:
		        case OBJ_SAUCER:
					e = new dg_Effect(this);
					dg_GameSession.AddGameObj(e);
					break;		

				case OBJ_NONE:
				case OBJ_BULLET:
				case OBJ_EFFECT:
				case OBJ_POWERUP:
				default:
					break;
			}
		}
		public gb_Vector3 UnitVectorFacing() {//unit vector in facing direction
			return new gb_Vector3(Math.cos(-Math.PI*m_angle/180.0),Math.sin(-Math.PI*m_angle/180.0),0);
		}
		public gb_Vector3 UnitVectorVelocity() {//unit vector in velocity direction
			return m_velocity.cpy().nor();
		}
	    

		 //collision flags/object types
		public final static int
			OBJ_NONE		= 0x00000000,
			OBJ_ASTEROID	= 0x00000002,
	        OBJ_SHIP		= 0x00000004,
			OBJ_BULLET		= 0x00000008,
			OBJ_EFFECT		= 0x00000010,
			OBJ_POWERUP		= 0x00000020,
			OBJ_TARGET      = 0x00000040,
	        OBJ_SAUCER		= 0x00000080
	    ;
		
		//data
		gb_Vector3	m_position;	  
		gb_Vector3	m_axis;	  
		float		m_angle;  
		gb_Vector3  m_velocity;  
		gb_Vector3  m_acceleration;
	    float		m_angVelocity;
	    boolean		m_active; 
		boolean		m_explodes; 
		float		m_size;
		gb_Sphere	m_boundSphere;
		int			m_type;
		int 		m_collisionFlags;
		float		m_lifeTimer;
		int         m_id;
		static int id;
		public boolean IsIntersecting(gb_Vector3 p1, gb_Vector3 p2) {
			return m_boundSphere.intersects(p1,p2);
		}
		@Override
		public String toString() {
			return m_active+" "+m_type+" "+m_id+" "+m_position.x+" "+m_position.y+" "+m_position.z;
		}
}
