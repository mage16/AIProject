package reuze.aifiles;

public abstract class dg_ControlAI extends dg_Control {
	public dg_ControlAI() {this(null);}
	public dg_ControlAI(dg_Ship ship) {
		super(ship);
			if (m_ship!=null )
				m_ship.m_control = this;
			m_type = Controller.CONTROLTYPE_AI;

			m_debugTxt = "";
		    m_target   = null;
		}
		@Override
		public abstract void Update(float dt);
	//debug data
	dg_GameObject m_target;
	String	 m_debugTxt;
	float	 m_targetDir;
}
