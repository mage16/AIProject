package reuze.aifiles;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

//#define POPULATION_SIZE
//#define TIME_PER_GENERATION
//#define GENOME_SIZE
//#define NUM_MAX_GENERATIONS


///#pragma warning(disable: 4786)

//class GAAIControl;



public class dg_GAMachine
{
	public static final int POPULATION_SIZE = 50;
	public static final float TIME_PER_GENERATION = 60.0f;
	public static final int GENOME_SIZE = 520;
	public static final int NUM_MAX_GENERATIONS = 5000;
	public dg_GAMachine(dg_GAAIControl parent)
	{
		this.m_parent = parent;
	}
    protected dg_GAAIControl m_parent;
	//genetic data
	protected java.util.ArrayList<dg_Genome> m_genomes = new java.util.ArrayList<dg_Genome>();
	protected int m_rankIndexLast;
	protected dg_Genome m_bestGenome = new dg_Genome();
	protected int m_generation;
	protected float m_crossoverRate;
	protected float m_mutationRate;
	protected float m_offsetSize;
	protected float m_bestFitness;
	protected float m_totalFitness;
	protected int m_liveCount;
	
	private String Update_buffer = new String(new char[30]);

	
	public final class Defines
	{
		public static final int NUM_ELITE = 4;
		public static final int NUM_COPIES_ELITE = 2;
		public static final int NUM_TO_TOURNEY = 8;
	}

	
		public final void Init()
		{
			m_mutationRate = 0.001f;
			m_crossoverRate = 0.70f;
			m_offsetSize = 1.0f;
			m_generation = 0;
			m_bestFitness = 0.0f;
			m_totalFitness = 0.0f;

		}
		public final void ApplyBehaviorRule(int index)
		{
			if (index < 0 || index > POPULATION_SIZE)
			{
				return;
			}

			dg_Ship ship = (dg_Ship) z_app.game.m_mainShip;

			//not going to collide, just idle...
			if (m_parent.m_currentEvasionSituation < 0)
			{
				ship.ThrustOff();
				ship.StopTurn();
				return;
			}

			//thrust
			int thrustTp = m_genomes.get(index).m_genes.get(m_parent.m_currentEvasionSituation).m_thrust;
			ship.StopTurn();
			if (thrustTp == Integer.parseInt(dg_gene.AnonymousEnum.THRUST_FORWARD.toString()))
			{
				ship.ThrustOn();
			}
			else if (thrustTp == Integer.parseInt(dg_gene.AnonymousEnum.THRUST_REVERSE.toString()))
			{
				ship.ThrustReverse();
			}
			else
			{
				ship.ThrustOff();
			}

			//turn
			//-10 puts you in the middle of the sector
			float newDir = m_genomes.get(index).m_genes.get(m_parent.m_currentEvasionSituation).m_sector * 20 - 10;
			float angDelta = CLAMPDIR180(ship.m_angle - newDir);
			if (Math.abs(angDelta) <= 90)
			{
				if (angDelta > 0)
				{
					ship.TurnRight();
				}
				else
				{
					ship.TurnLeft();
				}
			}
			else
			{
				if (angDelta < 0)
				{
					ship.TurnRight();
				}
				else
				{
					ship.TurnLeft();
				}
			}
		}
		private float CLAMPDIR180(float dir)
		{
			float outDirAbs = Math.abs(dir);
			float outDir;

			if (outDirAbs > 360.0f)
			{
				if (outDirAbs > 720.0f)
				{
					outDir = (dir % 360.0f);
				}
				else
				{
					outDir = (dir > 0.0f) ? dir - 360.0f : dir + 360.0f;
				}
				outDirAbs = Math.abs(outDir);
			}
			else
			{
				outDir = dir;
			}

			if (outDirAbs > 180.0f)
			{
				if (outDir > 0.0f)
				{
					outDir -= 360.0f;
				}
				else
				{
					outDir += 360.0f;
				}
			}
			return outDir;
		}
		private byte randint(int i, int j) {
			Random generator = new Random(); 
			 i = generator.nextInt(j);
			return (byte) i;
		}
		public final void UpdateFitness(int index)
		{
			dg_Ship ship = (dg_Ship)z_app.game.m_mainShip;
			if (ship != null && ship.m_active)
			{
				//if I'm currently surviving a collision situation, incr fitness
				if (m_parent.m_currentEvasionSituation != -1)
				{
					m_genomes.get(index).m_fitness++;
				}
			}
		}
		public final void Update(float dt)
		{
			//find best out of 5000 tries, then start over
			if (m_generation > NUM_MAX_GENERATIONS)
			{
				WriteSolution();
				//reset
				CreateStartPopulation();
				Reset();
			}

			m_liveCount = 0;
			for (int shpNum = 0; shpNum < POPULATION_SIZE; ++shpNum)
			{
				if (!z_app.game.m_mainShip.m_active)
				{
					continue;
				}
				m_liveCount++;
				m_parent.UpdatePerceptions(dt,shpNum);
				ApplyBehaviorRule(shpNum);
				UpdateFitness(shpNum);
			}

			//if the generation is over...
			if (m_liveCount ==0)
			{
				SetupNextGeneration();
			}

		//	static sbyte buffer[30];
		
			Update_buffer = String.format("G: %d    Best Fitness %3.2f", m_generation * m_bestFitness);
			m_parent.m_debugTxt = Update_buffer;
		}
		public void bubbleSort(ArrayList<dg_Genome> array) {
		    boolean swapped = true;
		    int j = 0;
		    dg_Genome tmp;
		    while (swapped) {
		        swapped = false;
		        j++;
		        for (int i = 0; i < array.size() - j; i++) {
		            if (Integer.parseInt(array.get(i).toString()) > Integer.parseInt((array.get(i + 1).toString()))) {
		                tmp = array.get(i);
		                array.set(i,(array.get(i + 1)));
		              array.set(i + 1,tmp) ;
		                swapped = true;
		            }
		        }
		    }
		}
		public final void SetupNextGeneration()
		{
			//next Generation 
			java.util.ArrayList<dg_Genome> offspring = new java.util.ArrayList<dg_Genome>();

			//sort the population (for scaling and elitism)
			bubbleSort(m_genomes);
			m_rankIndexLast = POPULATION_SIZE-1;

			//statistics
			m_totalFitness = 0.0f;
			for (int i = 0; i < POPULATION_SIZE; ++i)
			{
				m_totalFitness += m_genomes.get(i).m_fitness;
			}
			m_bestFitness = m_genomes.get(POPULATION_SIZE - 1).m_fitness;

			CopyEliteInto(offspring);

			while (offspring.size() < POPULATION_SIZE)
			{
				//selection operator
				dg_Genome parent1 = SelectRouletteWheel();
				dg_Genome parent2 = SelectRouletteWheel();

				//crossover operator
				dg_Genome offspring1 = new dg_Genome();
				dg_Genome offspring2 = new dg_Genome();
				CrossSinglePoint(parent1.m_genes, parent2.m_genes, offspring1.m_genes, offspring2.m_genes);

				//mutation operator
				MutateOffset(offspring1.m_genes);
				MutateOffset(offspring2.m_genes);

				//add to new population
				offspring.add(offspring1);
				offspring.add(offspring2);
			}

			//replace old generation with new
			m_genomes = offspring;

			for (int i = 0;i < POPULATION_SIZE;i++)
			{
				m_genomes.get(i).m_fitness = 0.0f;
			}

			++m_generation;

			//reactivate the ships
			for (int shpNum = 0; shpNum < POPULATION_SIZE; ++shpNum)
			{
				//reset test ships to startup state
				dg_Ship ship = (dg_Ship)z_app.game.m_mainShip;
				ship.m_active = true;
				ship.m_velocity.x = 0;
				ship.m_velocity.y = 0;
				ship.m_velocity.z = 0;
				ship.MakeInvincible(3.0f);
			}
		}
		public final void CreateStartPopulation()
		{
			m_genomes.clear();

			//create random initial generation
			for (int i = 0; i < POPULATION_SIZE; i++)
			{
				m_genomes.add(m_genomes.get(GENOME_SIZE));
			}

			//reset all variables
			Init();
		}
		public final void CopyEliteInto(java.util.ArrayList<dg_Genome> destination)
		{
			int numberOfElite = Defines.NUM_ELITE;
			//copy the elite over to the supplied destination
			for (int i = numberOfElite; i > 0; --i)
			{
				for (int j = 0;j < Defines.NUM_COPIES_ELITE;++j)
				{
					destination.add(m_genomes.get((POPULATION_SIZE - 1) - numberOfElite));
				}
			}
		}
		public final dg_Genome SelectRouletteWheel()
		{
			float wedge = randflt() * m_totalFitness;
			float total = 0.0f;

			for (int i = 0; i < POPULATION_SIZE; ++i)
			{
				total += m_genomes.get(i).m_fitness;
				if (total > wedge)
				{
					return m_genomes.get(i);
				}
			}
			return m_genomes.get(0);
		}
		public final dg_Genome SelectTournament()
		{
			int bestGenome = 0;
			float bestFitness = 0.0f;

			for (int i = 0; i < Defines.NUM_TO_TOURNEY; ++i)
			{
				int tourneyMember = randint(0,POPULATION_SIZE-1);
				if (m_genomes.get(i).m_fitness > bestFitness)
				{
					bestFitness = m_genomes.get(i).m_fitness;
					bestGenome = i;
				}
			}
			return m_genomes.get(bestGenome);
		}
		public final dg_Genome SelectRank()
		{
			//list is already sorted, and m_rankIndexLast is set to the 
			//best guy after the sort, note that this function doesn't bounds check
			return m_genomes.get(m_rankIndexLast--);
		}
		public final void CrossUniform(java.util.ArrayList<dg_gene> parent1, java.util.ArrayList<dg_gene> parent2, java.util.ArrayList<dg_gene> offspring1, java.util.ArrayList<dg_gene> offspring2)
		{
			if ((randflt() > m_crossoverRate) || (parent1 == parent2))
			{
				offspring1 = parent1;
				offspring2 = parent2;
				return;
			}

			for (int gene = 0; gene < GENOME_SIZE; ++gene)
			{
				if (randflt() < m_crossoverRate)
				{
					//switch the genes at this point
					offspring1.add(parent2.get(gene));
					offspring2.add(parent1.get(gene));

				}
				else
				{
					//just copy into offspring 
					offspring1.add(parent1.get(gene));
					offspring2.add(parent2.get(gene));
				}
			}
		}
		public final void CrossSinglePoint(java.util.ArrayList<dg_gene> parent1, java.util.ArrayList<dg_gene> parent2, java.util.ArrayList<dg_gene> offspring1, java.util.ArrayList<dg_gene> offspring2)
		{
			if ((randflt() > m_crossoverRate) || (parent1 == parent2))
			{
				offspring1 = parent1;
				offspring2 = parent2;
				return;
			}

			//determine a crossover point
			int crossPoint = (int)(randflt() * GENOME_SIZE-1);

			for (int gene = 0; gene < GENOME_SIZE; ++gene)
			{
				if (gene > crossPoint)
				{
					//just copy into offspring 
					offspring1.add(parent1.get(gene));
					offspring2.add(parent2.get(gene));

				}
				else
				{
					//switch the genes at this point
					offspring1.add(parent2.get(gene));
					offspring2.add(parent1.get(gene));
				}
			}
		}
		public final void CrossMultiPoint(java.util.ArrayList<dg_gene> parent1, java.util.ArrayList<dg_gene> parent2, java.util.ArrayList<dg_gene> offspring1, java.util.ArrayList<dg_gene> offspring2)
		{
			if ((randflt() > m_crossoverRate) || (parent1 == parent2))
			{
				offspring1 = parent1;
				offspring2 = parent2;
				return;
			}

			//determine crossover interval points
			int crossPoint1 = randint(0,GENOME_SIZE-2);
			int crossPoint2 = randint(crossPoint1,GENOME_SIZE-1);

			for (int gene = 0; gene < GENOME_SIZE; ++gene)
			{
				if (gene < crossPoint1 || gene>crossPoint2)
				{
					//just copy into offspring 
					offspring1.add(parent1.get(gene));
					offspring2.add(parent2.get(gene));

				}
				else
				{
					//switch the genes at this point
					offspring1.add(parent2.get(gene));
					offspring2.add(parent1.get(gene));
				}
			}
		}
		public final void CrossPMX(java.util.ArrayList<dg_gene> parent1, java.util.ArrayList<dg_gene> parent2, java.util.ArrayList<dg_gene> offspring1, java.util.ArrayList<dg_gene> offspring2)
		{
			if ((randflt() > m_crossoverRate) || (parent1 == parent2))
			{
				offspring1 = parent1;
				offspring2 = parent2;
				return;
			}

			//determine crossover interval points
			int crossPoint1 = randint(0,GENOME_SIZE-2);
			int crossPoint2 = randint(crossPoint1,GENOME_SIZE-1);

			//do a straight copy
			offspring1 = parent1;
			offspring2 = parent2;
			//then swap over the mapped interval
			for (int gene = crossPoint1; gene < crossPoint2 + 1; ++gene)
			{
				dg_gene swapGene1 = parent1.get(gene);
				dg_gene swapGene2 = parent2.get(gene);

				if (swapGene1 != swapGene2)
				{
					//swap positions in both offspring
					//dg_gene index1 = find(offspring1.iterator(),offspring1.get(offspring1.size()-1),swapGene1);
					//dg_gene index2 = find(offspring1.iterator(),offspring1.get(offspring1.size()-1),swapGene2);
					//swap(index1,index2);

					//index1 = *find(offspring2.iterator(),offspring1.get(offspring1.size()-1),swapGene1);
				//	index2 = *find(offspring2.iterator(),offspring1.get(offspring1.size()-1),swapGene2);
				//	swap(index1,index2);
				}
			}
		}
		private void swap(dg_gene index1, dg_gene index2) {
			// TODO Auto-generated method stub
			
		}
		public final void CrossOrderBased(java.util.ArrayList<dg_gene> parent1, java.util.ArrayList<dg_gene> parent2, java.util.ArrayList<dg_gene> offspring1, java.util.ArrayList<dg_gene> offspring2)
		{
			if ((randflt() > m_crossoverRate) || (parent1 == parent2))
			{
				offspring1 = parent1;
				offspring2 = parent2;
				return;
			}

			//vector to save the genes already used
			java.util.ArrayList<dg_gene> usedGenes = new java.util.ArrayList<dg_gene>();
			java.util.ArrayList<Integer> usedGenesPositions = new java.util.ArrayList<Integer>();

			//do a straight copy
			offspring1 = parent1;
			offspring2 = parent2;

			//find a number of random genes within parent 1, and record
			//both the position, and the gene itself
			int position = randint(0,GENOME_SIZE-2);
			while (position < GENOME_SIZE)
			{
				usedGenesPositions.add(position);
				usedGenes.add(parent1.get(position));
				position += randint(1,GENOME_SIZE - position);
			}

			int genePosition = 0;
			for (int i = 0;i < GENOME_SIZE;++i)
			{
				for (int j = 0;j < usedGenes.size();++j)
				{
					if (offspring2.get(i) == usedGenes.get(j))
					{
						offspring2.set(i, usedGenes.get(genePosition++));
						break;
					}
				}
			}

			//and now do the same for the other offspring
			usedGenes.clear();
			genePosition = 0;
			for (int i = 0;i < usedGenesPositions.size();++i)
			{
				usedGenes.add(parent2.get(i));
			}

			for (int i = 0;i < GENOME_SIZE;++i)
			{
				for (int j = 0;j < usedGenes.size();++j)
				{
					if (offspring1.get(i) == usedGenes.get(j))
					{
						offspring1.set(i, usedGenes.get(genePosition++));
						break;
					}
				}
			}
		}
		public final void CrossPositionBased(java.util.ArrayList<dg_gene> parent1, java.util.ArrayList<dg_gene> parent2, java.util.ArrayList<dg_gene> offspring1, java.util.ArrayList<dg_gene> offspring2)
		{
			if ((randflt() > m_crossoverRate) || (parent1 == parent2))
			{
				offspring1 = parent1;
				offspring2 = parent2;
				return;
			}

			int off1PosFilled = 0;
			int off2PosFilled = 0;
			java.util.ArrayList<Integer> genePositions = new java.util.ArrayList<Integer>();
			//find a number of random genes positions, these will
			//be the slots that will be position static
			int randGenePos = randint(0,GENOME_SIZE-2);
			while (randGenePos < GENOME_SIZE)
			{
				genePositions.add(randGenePos);
				randGenePos += randint(1,GENOME_SIZE - randGenePos);
			}
			//copy over the genes in the position static slots
			for (int i = 0; i < genePositions.size(); ++i)
			{
				offspring1.set(genePositions.get(i), parent1.get(genePositions.get(i)));
				offspring2.set(genePositions.get(i), parent2.get(genePositions.get(i)));
			}
			//now, fill in the non-static slots
			int openSlot = 0;
			for (int i = 0;i < GENOME_SIZE;++i)
			{
				//find next open slot
				boolean found = false;
			}
			
		}
		public final void MutateOffset(java.util.ArrayList<dg_gene> genes)
		{

			for (int gene = 0; gene < genes.size(); ++gene)
			{
				//check for thrust mutation
				if (randflt() < m_mutationRate)
				{
					genes.get(gene).m_thrust += (randint(0,1) - m_offsetSize);
					//bounds check
					if (genes.get(gene).m_thrust >dg_Genome.NUM_THRUST_STATES)
					{
						genes.get(gene).m_thrust = 0;
					}
					if (genes.get(gene).m_thrust < 0)
					{
						genes.get(gene).m_thrust = dg_Genome.NUM_THRUST_STATES;
					}
				}

				//check for angle mutation
				if (randflt() < m_mutationRate)
				{
					genes.get(gene).m_sector += (randint(0,1) - m_offsetSize);
					//bounds check
					if (genes.get(gene).m_sector > dg_Genome.NUM_SECTORS)
					{
						genes.get(gene).m_sector = 0;
					}
					if (genes.get(gene).m_sector < 0)
					{
						genes.get(gene).m_sector = dg_Genome.NUM_SECTORS;
					}
				}

			}
		}
		
		public final void MutateExchange(java.util.ArrayList<dg_gene> genes)
		{
			if (randflt() > m_mutationRate)
			{
				return;
			}

			//choose two non-identical genes
			int pos1 = randint(0, GENOME_SIZE-1);
			int pos2;
			do
			{
				pos2 = randint(0, GENOME_SIZE-1);
			}while (pos2 == pos1);
			//swap their positions
			swap(genes.get(pos1), genes.get(pos2));
		}
		public final void MutateDisplacement(java.util.ArrayList<dg_gene> genes)
		{
			if (randflt() > m_mutationRate)
			{
				return;
			}

			//determine crossover interval points
			int index = randint(0,GENOME_SIZE-2);
			int crossPoint1 =Integer.parseInt( genes.iterator().toString()) + index;
			int crossPoint2 =crossPoint1 + randint(0,GENOME_SIZE-1 - index);

			//save the interval
			java.util.ArrayList<dg_gene> temp = new java.util.ArrayList<dg_gene>();
		//	temp.assign(crossPoint1,crossPoint2);
			//remove interval from genes, and reinsert at random point
			//genes.erase(crossPoint1,crossPoint2);
			int insertPoint = Integer.parseInt(genes.iterator().toString()) + randint(0,GENOME_SIZE-1);
			//genes.insert(insertPoint,temp.iterator(),temp.end());
		}
		public final void MutateInsertion(java.util.ArrayList<dg_gene> genes)
		{
			if (randflt() > m_mutationRate)
			{
				return;
			}

			//determine a gene to mutate
			int index = randint(0,GENOME_SIZE-1);
			int mutateGene = Integer.parseInt(genes.iterator().toString()) + index;
			//remove gene from list, and reinsert at random point
		//	 genes.erase(mutateGene);
			int insertPoint = Integer.parseInt(genes.iterator().toString()) + randint(0,GENOME_SIZE-1);
		//	genes.set(index, mutateGene);
		}
		private float randflt() {
			Random generator = new Random(); 
			float i = generator.nextFloat();
			return i;
		}
		public final boolean WriteSolution()
		{
			try{
			String fileName = "solution.txt";
			 FileOutputStream inputStream = 
		                new FileOutputStream(fileName);
			//sort the genomes (to single out the best guy)
			bubbleSort(m_genomes);

			for (int i = 0;i < GENOME_SIZE;++i)
			{
			
				inputStream.write(m_genomes.get(GENOME_SIZE-1).m_genes.get(i).m_thrust);
				inputStream.write(m_genomes.get(GENOME_SIZE-1).m_genes.get(i).m_sector);
			}
			inputStream.close();
			return true;
			}
			catch(IOException ex) {
	            System.out.println(
	                "Error writing file '");
	            return false;
	        }
		}
		public final boolean ReadSolution()
		{
			try{
				String fileName = "solution.txt";
				 FileInputStream inputStream = 
			                new FileInputStream(fileName);
				//sort the genomes (to single out the best guy)
				bubbleSort(m_genomes);

				for (int i = 0;i < GENOME_SIZE;++i)
				{
				
					inputStream.read();
					
				}
				inputStream.close();
				return true;
				}
				catch(IOException ex) {
		            System.out.println(
		                "Error writing file '");
		            return false;
		        }
		}
		private float MIN(float speed, float m_maxSpeed2) {
			float min = Math.min(speed, m_maxSpeed2);
			return min;
			
		}
		private int LERP(float f, float g, float h) {
			return (int) (f + h * (g - f));
		}
		private int MIN(int i, int j) {
			int min = Math.min(i, j);
			return min;
		}
		public final void Reset()
		{
			Init();
			for (int shpNum = 0; shpNum < POPULATION_SIZE; ++shpNum)
			{
				//reset test ships to startup state
				dg_Ship ship = (dg_Ship) z_app.game.m_mainShip;
				ship.m_active = true;
				ship.m_velocity.x = 0;
				ship.m_velocity.y = 0;
				ship.m_velocity.z = 0;
				ship.MakeInvincible(3.0f);
			}
			CreateStartPopulation();
		}
	}



