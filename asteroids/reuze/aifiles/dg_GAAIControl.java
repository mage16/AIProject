package reuze.aifiles;

import java.util.Random;

import com.software.reuze.gb_Vector3;
import com.software.reuze.m_MathUtils;



public class dg_GAAIControl extends dg_ControlAI
{


	public dg_GAMachine m_GAMachine;
	//perception data 
	public dg_GameObject m_nearestAsteroid;
	public float m_nearestAsteroidDist;
	public int m_currentEvasionSituation;
	public float m_maxSpeed;

	public dg_GAAIControl(dg_Ship ship)
	{
		this.m_ship = ship;
		m_GAMachine = new dg_GAMachine(this);
		Init();
		m_GAMachine.CreateStartPopulation();
	}
	public final void dispose()
	{
		//take down GA machine
	}
	public final void Init()
	{
		m_nearestAsteroid = null;
		m_maxSpeed = 80.0f;
		m_currentEvasionSituation = -1;
	}
	public final void Update(float dt)
	{
		m_GAMachine.Update(dt);
	}
	public final void UpdatePerceptions(float dt, int index)
	{
		dg_Ship ship = (dg_Ship) z_app.game.m_mainShip;
		if (ship == null)
		{
			return;
		}

		//determine current game evasion state
		int collisionState = -1;
		int directionState = -1;
		int distanceState = -1;

		//store closest asteroid
		m_nearestAsteroid =  z_app.game.GetClosestGameObj(ship,dg_GameObject.OBJ_ASTEROID);

		//reset distance to a large bogus number
		m_nearestAsteroidDist = 100000.0f;

		if (m_nearestAsteroid != null)
		{
			gb_Vector3 normDelta = m_nearestAsteroid.m_position.tmp().sub(m_ship.m_position);
			normDelta.nor();

			//asteroid collision determination
			 float speed = m_ship.m_velocity.len();
			 m_nearestAsteroidDist = m_nearestAsteroid.m_position.dst(m_ship.m_position);
			 float astSpeed = m_nearestAsteroid.m_velocity.len();
			 float shpSpeedAdj = m_ship.UnitVectorVelocity().dot(normDelta)*speed;
		        normDelta.inv();
		        float astSpeedAdj = m_nearestAsteroid.UnitVectorVelocity().dot(normDelta)*astSpeed;
		        speed = shpSpeedAdj+astSpeedAdj;
			speed = shpSpeedAdj + astSpeedAdj;
			speed = MIN(speed,m_maxSpeed);
			collisionState = (int)LERP(speed / m_maxSpeed,0.0f,9.0f);

			//direction determination
			directionState =(int) m_MathUtils.directionXangle(normDelta);

			//distance determination
			distanceState = MIN((int)(m_nearestAsteroidDist / m_nearestAsteroid.m_size),4);
		}
		if (collisionState == -1)
		{
			m_currentEvasionSituation = -1;
		}
		else
		{
			m_currentEvasionSituation = (collisionState * 10) + (directionState * 18) + distanceState;
		}
	}
	private int LERP(float f, float g, float h) {
		return (int) (f + h * (g - f));
	}
	private float MIN(float speed, float m_maxSpeed2) {
		float min = Math.min(speed, m_maxSpeed2);
		return min;
		
	}
	private int MIN(int i, int j) {
		int min = Math.min(i, j);
		return min;
	}
	private byte randint(int i, int j) {
		Random generator = new Random(); 
		 i = generator.nextInt(j);
		return (byte) i;
	}
	public final void Reset()
	{
		Init();
		m_GAMachine.Reset();
	}
}