package reuze.aifiles;

import java.util.Arrays;

import com.software.reuze.gb_Vector3;

public class dg_InfluenceMapBits extends dg_InfluenceMap {
	//constructor/functions
	public dg_InfluenceMapBits() {
		super(IM_BITWISE);
	}
	@Override
	public void Update(float dt)
	{
		//bail out if nobody to update
		if(m_registeredObjects.size() == 0) return;

		//clear out map
		Arrays.fill(m_map,0);

		//stamp new data
		for(RegObject listObj:m_registeredObjects)
		{
			//have to update the bits, since you can change direction continuously
			listObj.m_objType = (char)listObj.m_objType | GetVelocityDirectionMask(listObj.m_pObject);
			StampInfluenceShape(m_map,listObj.m_pObject.m_position,listObj.m_objSizeX,listObj.m_objSizeY,listObj.m_objType,false);
			listObj.m_stamped = true;
			listObj.m_lastPosition = listObj.m_pObject.m_position;
		}
	}
	void RegisterGameObj(dg_GameObject object) {
		int sizeX,sizeY;
		if(object.m_size <4)
		{
			sizeX = m_dataSizeX/16;// 2;
			sizeY = m_dataSizeY/16;// 2;
		}
		else if(object.m_size<11)
		{
			sizeX = m_dataSizeX/10;//3;
			sizeY = m_dataSizeY/10;//3;
		}
		else if(object.m_size<33)
		{
			sizeX = m_dataSizeX/8;//4;
			sizeY = m_dataSizeY/8;//4;
		}
		else if(object.m_size <49)
		{
			sizeX = m_dataSizeX/5;//6;
			sizeY = m_dataSizeX/5;//6;
		}
		else if(object.m_size <65)
		{
			sizeX = m_dataSizeX/4;//8;
			sizeY = m_dataSizeX/4;//8;
		}
		else
		{
			sizeX = m_dataSizeX/3;//10;
			sizeY = m_dataSizeX/3;//10;
		}

		//set minimum size of 1 in each direction
		sizeX = Math.max(1,sizeX);
		sizeY = Math.max(1,sizeY);


		RegObject temp;
		temp = new RegObject();
		temp.m_objType = object.m_type;
		temp.m_objType |= GetVelocityDirectionMask(object);

		temp.m_pObject      = object;
		temp.m_objSizeX     = sizeX;
		temp.m_objSizeY     = sizeY;
		temp.m_lastPosition = object.m_position;
		temp.m_stamped      = false;
		m_registeredObjects.add(temp);
	}
	public static final gb_Vector3 colorArray[]=new gb_Vector3[16];

	//---------------------------------------------------------
	/*void glInitColorArray()
	{
		//white
		colorArray[0] = Point3f(1,1,1);
		//red
		colorArray[1] = Point3f(1,0,0);
		//green
		colorArray[2] = Point3f(0,1,0);
		//blue
		colorArray[3] = Point3f(0,0,1);
		//black
		colorArray[4] = Point3f(0,0,0);
		//gray
		colorArray[5] = Point3f(0.5f,0.5f,0.5f);
		//cyan
		colorArray[6] = Point3f(0,1,1);
		//yellow
		colorArray[7] = Point3f(1,1,0);
		//magenta
		colorArray[8] = Point3f(1,0,1);
	    //silver
	    colorArray[9] = Point3f(0.75f,0.75f,0.75f);
	    //maroon
	    colorArray[10]= Point3f(0.5f,0,0);
	    //dk green
	    colorArray[11]= Point3f(0,0.5f,0);
	    //dk blue
	    colorArray[12]= Point3f(0,0,0.5f);
	    //purple
	    colorArray[13]= Point3f(0.5f,0,0.5f);
	    //olive
	    colorArray[14]= Point3f(0.5f,0.5f,0);
	    //teal
	    colorArray[15]= Point3f(0,0.5f,0.5f);*/
	void DrawTheInfluence() {
		for(int i=0;i<m_numCells;i++) {
			if(m_map[i]==0) continue;
			int y = i / m_dataSizeY;
			int x = i - y*m_dataSizeY;
			//determine color for type
			gb_Vector3 color=new gb_Vector3(0,0,0);
			for(int index = 0;index<8;index++)
			{
				int bitset = (m_map[i] & (1<<index));
				if(bitset!=0)
					color.add(colorArray[index]);
			}
			/*glColor3f(color.x,color.y,color.z);
			glBegin(GL_POLYGON);
			glVertex3f(x*m_celResX,          y*m_celResY,               0);
			glVertex3f(x*m_celResX,          y*m_celResY+m_celResY*0.5f,0);
			glVertex3f(x*m_celResX+m_celResX,y*m_celResY+m_celResY*0.5f,0);
			glVertex3f(x*m_celResX+m_celResX,y*m_celResY,               0);
			glEnd();*/

			color.set(0,0,0);
			//get colors for direction
			int direction = m_map[i]>>8;
				if((direction & DIR_LEFT) != 0)
					color = colorArray[9];//left silver
				if((direction & DIR_RIGHT) != 0)
					color = colorArray[13];//right purple
				/*glColor3f(color.x,color.y,color.z);
				glBegin(GL_POLYGON);
				glVertex3f(x*m_celResX,               y*m_celResY+m_celResY*0.5f,0);
				glVertex3f(x*m_celResX,               y*m_celResY+m_celResY,     0);
				glVertex3f(x*m_celResX+m_celResX*0.5f,y*m_celResY+m_celResY,     0);
				glVertex3f(x*m_celResX+m_celResX*0.5f,y*m_celResY+m_celResY*0.5f,0);
				glEnd();*/

				color.set(0,0,0);
				if((direction & DIR_UP) != 0)
					color = colorArray[14];//up olive
				if((direction & DIR_DOWN) != 0)
					color = colorArray[15];//down teal

				/*glColor3f(color.x,color.y,color.z);
				glBegin(GL_POLYGON);
				glVertex3f(x*m_celResX+m_celResX*0.5f,y*m_celResY+m_celResY*0.5f,0);
				glVertex3f(x*m_celResX+m_celResX*0.5f,y*m_celResY+m_celResY,     0);
				glVertex3f(x*m_celResX+m_celResX,     y*m_celResY+m_celResY,     0);
				glVertex3f(x*m_celResX+m_celResX,     y*m_celResY+m_celResY*0.5f,0);
				glEnd();*/
		}
	}
	void StampInfluenceShape(int map[],gb_Vector3 location,int sizeX,int sizeY, int value, boolean undo) {
		int gridX = (int) (location.x/ m_celResX);
		int gridY = (int) (location.y/ m_celResY);

		int startX = gridX - sizeX/2;
		if(startX < 0) startX += m_dataSizeX;
		int startY = gridY - sizeY/2;
		if(startY < 0) startY += m_dataSizeY;

		for(int y = startY;y<startY + sizeY;y++)
		{
			for(int x = startX;x<startX + sizeX;x++)
			{
				if(undo)
					map[(y%m_dataSizeY)*m_dataSizeY + (x%m_dataSizeX)] &= ~value;
				else
					map[(y%m_dataSizeY)*m_dataSizeY + (x%m_dataSizeX)] |= value;
			}
		}
	}
	int GetVelocityDirectionMask(dg_GameObject object) {
		//set up the information bits
		//    bottom byte is the object type
		//    top byte is the velocity direction
		int velDir = 0;
		if(object.m_velocity.x > 0)
			velDir |= DIR_RIGHT;
		else if (object.m_velocity.x < 0)
			velDir |= DIR_LEFT;
		if(object.m_velocity.y > 0)
			velDir |= DIR_UP;
		else if (object.m_velocity.y < 0)
			velDir |= DIR_DOWN;
		return velDir<<8;
	}
	int  GetInfluenceType(int map[],gb_Vector3 location) {
		int gridX = (int) (location.x/ m_celResX);
		int gridY = (int) (location.y/ m_celResY);
		return map[(gridY%m_dataSizeY)*m_dataSizeY + (gridX%m_dataSizeX)] & 0x0f;
	}
	int  GetInfluenceDirection(int map[],gb_Vector3 location) {
		int gridX = (int) (location.x/ m_celResX);
		int gridY = (int) (location.y/ m_celResY);
		return map[(gridY%m_dataSizeY)*m_dataSizeY + (gridX%m_dataSizeX)] >> 8;
	}
}
