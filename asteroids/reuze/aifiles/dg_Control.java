package reuze.aifiles;

public class dg_Control {
		//constructor/functions
		public dg_Control() {this(null);}
		public dg_Control(dg_Ship ship) {m_ship = ship; m_type = Controller.CONTROLTYPE_NONE;}
		public void Update(float dt) {}
		public void Init() {}
		public void SetShip(dg_Ship ship)  {m_ship = ship; m_ship.m_control = this;}
		public void Key(int key, int x, int y) {}
		public enum Controller
		{
			CONTROLTYPE_NONE,
			CONTROLTYPE_HUMAN,
			CONTROLTYPE_AI
		};
		public static final int
			KEY_ESC=27,KEY_SPACE=' ',KEY_DOT='.',KEY_COMMA=',',
			KEY_UP=-1,KEY_DOWN=-2,KEY_RIGHT=-3,KEY_LEFT=-4,
			KEYUP_UP=-5,KEYUP_DOWN=-6,KEYUP_RIGHT=-7,KEYUP_LEFT=-8,
			KEY_A='a',KEY_H='h',KEY_N='n',KEY_P='p',KEY_S='s',KEY_Y='y'
				;
			
		//data
		dg_Ship m_ship;
		Controller   m_type;
}
