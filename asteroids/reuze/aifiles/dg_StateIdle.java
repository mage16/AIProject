package reuze.aifiles;

import reuze.aifiles.dg_FSMState.States;

public class dg_StateIdle extends dg_FSMState {
	public dg_StateIdle(dg_Control parent) {
		super(States.FSM_STATE_IDLE, parent);
	}
	public void Update(float dt)
	{
	    //Do nothing
	    dg_ControlAIFSM parent = (dg_ControlAIFSM)m_parent;
	    parent.m_debugTxt = "Idle";
	}

	//---------------------------------------------------------
	public States CheckTransitions()
	{
		dg_ControlAIFSM parent = (dg_ControlAIFSM)m_parent;

	    if(parent.m_willCollide)
	        return States.FSM_STATE_EVADE;

	    if(parent.m_nearestAsteroid!=null)
	    {
	        if(parent.m_nearestAsteroidDist > z_app.game.APPROACH_DIST)
	            return States.FSM_STATE_APPROACH;
	        else
	            return States.FSM_STATE_ATTACK;
	    }

	    if(parent.m_nearestPowerup!=null && 
		   parent.m_ship.GetShotLevel() < z_app.game.MAX_SHOT_LEVEL)
	        return States.FSM_STATE_GETPOWERUP;

	    return States.FSM_STATE_IDLE;
	}
	@Override
	void Enter() {
		// TODO Auto-generated method stub
		
	}
	@Override
	void Exit() {
		// TODO Auto-generated method stub
		
	}
	@Override
	void Init() {
		// TODO Auto-generated method stub
		
	}
}
