package reuze.aifiles;

import reuze.aifiles.Gene.AnonymousEnum;

public class dg_gene {
	
		//methods    
		public Gene()
		{
			m_thrust = randint(0,2);
			m_sector = randint(0,Defines.NUM_SECTORS - 1);
		}
		public Gene(int a, int d)
		{
			this.m_thrust = a;
			this.m_sector = d;
		}
		public static boolean OpEquality(Gene ImpliedObject, Gene rhs)
		{
			return (m_thrust == rhs.m_thrust) && (m_sector == rhs.m_sector);
		}
		public static boolean operator !=  (Gene ImpliedObject, Gene rhs)
		{
			return (m_thrust != rhs.m_thrust) || (m_sector != rhs.m_sector);
		}

		public enum AnonymousEnum
		{
			THRUST_OFF,
			THRUST_FORWARD,
			THRUST_REVERSE;

			public int getValue()
			{
				return this.ordinal();
			}

			public static AnonymousEnum getEnum(int intValue)
			{
				return values()[intValue];
			}
		}

		//data
		public byte m_thrust;
		public byte m_sector;
	}

}
