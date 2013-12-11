package reuze.aifiles;



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
		this.dg_ControlAI = ship;
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
		Ship ship = (Ship)Game.m_ships[index];
		if (ship == null)
		{
			return;
		}

		//determine current game evasion state
		int collisionState = -1;
		int directionState = -1;
		int distanceState = -1;

		//store closest asteroid
		m_nearestAsteroid = Game.GetClosestGameObj(ship,GameObj.OBJ_ASTEROID);

		//reset distance to a large bogus number
		m_nearestAsteroidDist = 100000.0f;

		if (m_nearestAsteroid)
		{
			Point3f normDelta = m_nearestAsteroid.m_position - ship.m_position;
			normDelta.Normalize();

			//asteroid collision determination
			float speed = ship.m_velocity.Length();
			m_nearestAsteroidDist = m_nearestAsteroid.m_position.Distance(ship.m_position);
			float astSpeed = m_nearestAsteroid.m_velocity.Length();
			float shpSpeedAdj = DOT(ship.UnitVectorVelocity(),normDelta) * speed;
			float astSpeedAdj = DOT(m_nearestAsteroid.UnitVectorVelocity(),-normDelta) * astSpeed;
			speed = shpSpeedAdj + astSpeedAdj;
			speed = MIN(speed,m_maxSpeed);
			collisionState = (int)LERP(speed / m_maxSpeed,0.0f,9.0f);

			//direction determination
			directionState = GETSECTOR(normDelta);

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
	public final void Reset()
	{
		Init();
		m_GAMachine.Reset();
	}
}