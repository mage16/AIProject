package reuze.aifiles;

import com.software.reuze.gb_Vector3;
import com.software.reuze.m_MathUtils;

import reuze.aifiles.dg_FSMState.States;

public class dg_StateAttack extends dg_FSMState {
	//constructor/functions
    public dg_StateAttack(dg_Control parent) {
    	super(States.FSM_STATE_ATTACK,parent);
    }
    @Override
	public void Update(float dt)
	{
	    //turn towards closest asteroid's future position, and then fire
    	dg_ControlAIFSM parent = (dg_ControlAIFSM)m_parent;
	    dg_GameObject asteroid = parent.m_nearestAsteroid;
	    dg_Ship    ship     = parent.m_ship;
	    
	    if(asteroid==null)
	        return;
	    
	    gb_Vector3 futureAstPosition;
	    gb_Vector3 deltaPos = asteroid.m_position.tmp().sub(ship.m_position);
	    float dist  = deltaPos.len();
	    float time = dist/z_app.game.BULLET_SPEED;
	    futureAstPosition = asteroid.m_velocity.tmp().sub(ship.m_velocity).mul(time).add(asteroid.m_position);
	    gb_Vector3 deltaFPos = futureAstPosition.tmp2().sub(ship.m_position);
	    deltaFPos.nor();

	    float newDir     = -m_MathUtils.directionXangle(deltaFPos);
	    float gun = ship.GetClosestGunAngle(newDir);
	    float angDelta   = m_MathUtils.clamp180(gun - newDir);
	    //float dangerAdjAngle = ((1.0f - parent.m_nearestAsteroidDist/ z_app.game.APPROACH_DIST)*5.0f) + 1.0f;
	    ship.Shoot();
	    if(angDelta >=1) {//dangerAdjAngle)
	        ship.ThrustOn(); ship.TurnRight();
	    } else if(angDelta <= -1) {//-dangerAdjAngle)
	        ship.ThrustOn(); ship.TurnLeft();
	    } else {
	    	ship.ThrustOff(); ship.StopTurn();
	    }
	    
	    parent.m_target.m_position = futureAstPosition;
	    parent.m_targetDir = newDir;
	    parent.m_debugTxt = "Attack "+deltaFPos+" "+asteroid.m_id;
	}

	//---------------------------------------------------------
	States CheckTransitions()
	{
		dg_ControlAIFSM parent = (dg_ControlAIFSM)m_parent;
	    if(parent.m_willCollide)
	        return States.FSM_STATE_EVADE;

	    if(parent.m_powerupNear && parent.m_nearestAsteroidDist > 
	       parent.m_nearestPowerupDist && parent.m_ship.GetShotLevel() < z_app.game.MAX_SHOT_LEVEL)
	        return States.FSM_STATE_GETPOWERUP;
	    
	    if(parent.m_nearestAsteroid==null || parent.m_nearestAsteroidDist > z_app.game.APPROACH_DIST)
	        return States.FSM_STATE_IDLE;

	    return States.FSM_STATE_ATTACK;    
	}

	@Override
	public void Exit()
	{
	    if(((dg_ControlAIFSM)m_parent).m_ship!=null)
	        ((dg_ControlAIFSM)m_parent).m_ship.StopTurn();
	}
	@Override
	void Enter() {
		// TODO Auto-generated method stub

	}

	@Override
	void Init() {
		// TODO Auto-generated method stub

	}

}
