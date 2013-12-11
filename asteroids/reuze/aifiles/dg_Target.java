package reuze.aifiles;

public class dg_Target extends dg_GameObject {
	public dg_Target() {this(1);}
	public dg_Target(int size) {
		super(size);
		m_type = OBJ_TARGET;
		m_collisionFlags = OBJ_NONE;
	}
	@Override
	public void Draw() {
		z_app.app.text('X',m_position.x, m_position.y);
	}

}
