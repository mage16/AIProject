package reuze.aifiles;

import com.software.reuze.gb_Vector3;

public class dg_Bullet extends dg_GameObject {
	public static final float BULLET_MAX_LIFE=1.0f;
		//constructors/functions
	public dg_Bullet(dg_GameObject _parent, gb_Vector3 _p, float _angle) {
		super(_p.cpy(),_angle, 2.0f);
		m_parent = _parent;
		gb_Vector3 vel;
		vel=UnitVectorFacing();
		vel.mul(z_app.game.BULLET_SPEED).add(_parent!=null?_parent.m_velocity:gb_Vector3.ZERO);
		m_velocity = vel;
		Init();
	}
	public void Init()
	{
		m_type = OBJ_BULLET;
		m_collisionFlags = OBJ_ASTEROID;
		m_lifeTimer = BULLET_MAX_LIFE;
	}
	@Override
	public void Draw() {
		z_app.app.pushStyle();
      	float x=m_position.x, y=m_position.y;
		z_app.app.fill(0,0,255);
		z_app.app.ellipse(m_position.x, m_position.y,4,4);
		z_app.app.popStyle();
		//TODO not complete
	}
	@Override
	public gb_Vector3 Update(float dt)
	{
		gb_Vector3 temp=super.Update(dt);
		if(m_lifeTimer<0.0f) 
		{
			if (m_parent!=null)
				m_parent.Action(this, "kill_Bullet");
		}
		return temp;
	}
	@Override
	public void DoCollision(dg_GameObject obj) {
		//take both me and the other object out
	 	if (obj!=null && obj.m_active)
		{
			obj.Explode();
			obj.DoCollision(this);
		}

		if (m_parent!=null)
		{
			z_app.IncrementScore(z_app.game.ASTEROID_SCORE_VAL);
			m_parent.Action(this, "kill_Bullet");
		}
		super.DoCollision(obj);
	}

		//data
		dg_GameObject	m_parent;
}
