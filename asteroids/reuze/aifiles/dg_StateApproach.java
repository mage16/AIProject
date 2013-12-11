package reuze.aifiles;

import com.software.reuze.gb_Vector3;
import com.software.reuze.m_MathUtils;

public class dg_StateApproach extends dg_FSMState {
	//constructor/functions
	public dg_StateApproach(dg_Control parent) {
		super(States.FSM_STATE_APPROACH,parent);
	}
	@Override
	public void Update(float dt)
	{
		//turn and then thrust towards closest asteroid
		dg_ControlAIFSM parent = (dg_ControlAIFSM)m_parent;
		dg_GameObject asteroid = parent.m_nearestAsteroid;
		dg_Ship    ship     = parent.m_ship;

		gb_Vector3 deltaPos = asteroid.m_position.tmp().sub(ship.m_position);
		//use braking vector if you're going too fast
		boolean needToBrake = false;
		float speed		 = ship.m_velocity.len();
		if(speed > parent.m_maxSpeed)
		{
			needToBrake = true;
			deltaPos	= ship.m_velocity.tmp().inv();
		}
		else
		{
			float dotVelocity = ship.UnitVectorVelocity().dot(asteroid.UnitVectorVelocity());

			//if the other guy is "to my front" and we're moving towards each other...
			if ((deltaPos.dot(ship.UnitVectorVelocity()) < 0) || (dotVelocity > -0.93))//magic number == about 21 degrees
			{
				gb_Vector3 shipVel = ship.m_velocity.tmp2();
				shipVel.nor().mul(parent.m_maxSpeed);
				float combinedSpeed	  = shipVel.add(asteroid.m_velocity).len();
				if (Math.abs(combinedSpeed)>.01) {
					float predictionTime  = deltaPos.len() / combinedSpeed;
					gb_Vector3 targetPos = asteroid.m_position.tmp().add(asteroid.m_velocity.tmp2().mul(predictionTime));
					deltaPos  = targetPos.sub(ship.m_position);
				}
			}
		}
		//sub off our current velocity, to get direction of wanted velocity
		deltaPos.sub(ship.m_velocity);

		//find new direction, and head to it
		float newDir	 = -m_MathUtils.directionXangle(deltaPos);
		//ship nose angle down screen 0 to 180 clkwise, else 0 to 180 counter-clkwise
		float angDelta	 = m_MathUtils.clamp180(ship.m_angle - newDir);
		boolean canApproachInReverse = needToBrake || ship.GetShotLevel()!=0;
		String turn="";
		if(Math.abs(angDelta) <3 || (Math.abs(angDelta)> 177 && canApproachInReverse) )
		{
			//thrust
			ship.StopTurn(); turn="STOP";
			if(parent.m_nearestAsteroidDist > parent.m_nearestAsteroid.m_size + 19) {
	                if (Math.abs(angDelta)<3) ship.ThrustOn(); else ship.ThrustReverse();
	            } else ship.ThrustOff();
		}
		else if(newDir<0 && ship.m_angle<0) {
			if (newDir<ship.m_angle) {ship.TurnRight(); turn="right";} else {ship.TurnLeft(); turn="left";}
		} else if(newDir>=0 && ship.m_angle>=0) {
				if (newDir<ship.m_angle) {ship.TurnRight(); turn="right";} else {ship.TurnLeft(); turn="left";}
		} else if(newDir<0 && ship.m_angle>=0) {
				if ((360+newDir-ship.m_angle)>180) {ship.TurnRight(); turn="right";} else {ship.TurnLeft(); turn="left";}
		} else if(newDir>=0 && ship.m_angle<0) {
			if ((360-newDir+ship.m_angle)<180) {ship.TurnRight(); turn="right";} else {ship.TurnLeft(); turn="left";}
		}
		parent.m_target.m_position = asteroid.m_position;
		parent.m_targetDir = newDir;
		parent.m_debugTxt = "Approach "+ship.m_angle+" "+turn+" "+asteroid.m_id;
	}

	@Override
	public States CheckTransitions()
	{
		dg_ControlAIFSM parent = (dg_ControlAIFSM)m_parent;
		if(parent.m_willCollide)
			return States.FSM_STATE_EVADE;

		if(parent.m_powerupNear && parent.m_nearestAsteroidDist > 
		parent.m_nearestPowerupDist && parent.m_ship.GetShotLevel() < z_app.game.MAX_SHOT_LEVEL)
			return States.FSM_STATE_GETPOWERUP;

		if(parent.m_nearestAsteroid==null || parent.m_nearestAsteroidDist < z_app.game.APPROACH_DIST)
			return States.FSM_STATE_IDLE;

		return States.FSM_STATE_APPROACH;  
	}

	@Override
	public void Exit()
	{
		if(((dg_ControlAIFSM)m_parent).m_ship!=null)
		{
			((dg_ControlAIFSM)m_parent).m_ship.ThrustOff();
			((dg_ControlAIFSM)m_parent).m_ship.StopTurn();
		}
	}
	@Override
	void Enter() {
		// TODO Auto-generated method stub			
	}
	@Override
	void Init() {
		// TODO Auto-generated method stub			
	}
	/*public static void main(String args[]) {
			System.out.println(m_MathUtils.clamp180(37-34));
			System.out.println(m_MathUtils.clamp180(34-37));
			System.out.println(m_MathUtils.clamp180(1-(-2)));
			System.out.println(m_MathUtils.clamp180(179-(-179)));
		}*/
}
