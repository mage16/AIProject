package reuze.aifiles;

import com.software.reuze.gb_Vector3;

import processing.core.PApplet;

public class aitest extends PApplet {
	dg_InfluenceMap m;
	dg_GameObject o;
	public void setup() {
		size(512,512);
		z_app.app=this;
		m=new dg_InfluenceMap(dg_InfluenceMap.IM_BITWISE);
		m.DrawGrid(true);
		m.DrawInfluence(true);
		o=new dg_Bullet(null,new gb_Vector3(200,200,0),8f);
		m.RegisterGameObj(o);
		m.StampInfluenceShape(o.m_position, 2, 1, 3);
	}
	public void draw() {
		background(-1);
		m.Draw();
		o.Draw();
	}
}
