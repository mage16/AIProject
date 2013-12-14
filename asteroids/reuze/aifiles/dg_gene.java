package reuze.aifiles;

import java.util.Random;


public class dg_gene {
	//data
			public static byte m_thrust;
			public static byte m_sector;
		//methods    
		public dg_gene()
		{
			m_thrust = randint(0,2);
			m_sector = randint(0,dg_Genome.NUM_SECTORS - 1);
		}
		private byte randint(int i, int j) {
			Random generator = new Random(); 
			 i = generator.nextInt(j);
			return (byte) i;
		}
		public dg_gene(int a, int d)
		{
			this.m_thrust = 'a';
			this.m_sector = 'd';
		}
		public static boolean OpEquality(dg_gene ImpliedObject, dg_gene rhs)
		{
			return (m_thrust == rhs.m_thrust) && (m_sector == rhs.m_sector);
		}
		public static boolean operator(dg_gene ImpliedObject, dg_gene rhs)
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

		
	}


