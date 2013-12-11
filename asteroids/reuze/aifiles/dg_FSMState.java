package reuze.aifiles;

public abstract class dg_FSMState {
	//FSM state types
	public static enum States
	{
		FSM_STATE_NONE,
		FSM_STATE_APPROACH,
		FSM_STATE_ATTACK,
		FSM_STATE_EVADE,
		FSM_STATE_GETPOWERUP,
		FSM_STATE_IDLE,
	    FSM_STATE_COUNT
	};

	//FSM machine types
	public static enum Types
	{
		FSM_MACH_NONE,
		FSM_MACH_MAINSHIP,
	    FSM_MACH_COUNT
	};
	//constructor/functions
	public dg_FSMState() {
		this(States.FSM_STATE_NONE, null);
	}
	public dg_FSMState(	States type, dg_Control parent) {
		m_type = type;m_parent = parent;
	}
    abstract void Enter();
    abstract void Exit();
    abstract void Update(float dt);
    abstract void Init();
    States  CheckTransitions() {return States.FSM_STATE_NONE;}

	//data
	dg_Control   m_parent;
	States        m_type;
}
