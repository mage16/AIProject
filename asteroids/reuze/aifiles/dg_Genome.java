package reuze.aifiles;

//#define NUM_SECTORS
//#define NUM_THRUST_STATES


///#pragma warning(disable: 4786)


public class dg_Genome
{
	public static final int NUM_SECTORS = 18;
	public static final int NUM_THRUST_STATES = 2;
	public java.util.ArrayList<dg_gene> m_genes = new java.util.ArrayList<dg_gene>();
	public static float m_fitness;
	//methods
	public dg_Genome()
	{
		this.m_fitness = 0F;
	}
	public dg_Genome(int num_genes)
	{
		this.m_fitness = 0F;
		for (int i = 0;i < num_genes;++i)
		{
			m_genes.add(new dg_gene());
		}
	}

	public static boolean OpLessThan(dg_Genome ImpliedObject, dg_Genome rhs)
	{
		return (m_fitness < rhs.m_fitness);
	}

	
	
}

