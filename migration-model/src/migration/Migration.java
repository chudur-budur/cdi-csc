package migration;

import java.util.ArrayList;

import migration.parameters.Parameters;
import sim.engine.MakesSimState;
import sim.engine.ParallelSequence;
import sim.engine.Sequence;
import sim.engine.SimState;
import sim.engine.Steppable;
import world.WorldAgent;
import environment.Map;
import optimization.desirability.DesirabilityMaps ;

public class Migration extends SimState
{
	private Parameters parameters;

	Map map ;
	WorldAgent worldAgent;
	public MySequence households;

	// desirability maps
	DesirabilityMaps dmaps ;
	
	public class MySequence extends ArrayList<Steppable> implements Steppable{

		public MySequence() {
			super();
		}
		public MySequence(int x) {
			super(x);
		}
		@Override
		public void step(SimState arg0) {
			for(Steppable s:this)
				s.step(arg0);	
		}
	}

	/**
	 * Constructs a new simulation.  Side-effect: sets the random seed of
	 * Parameters to equal seed.
	 *
	 * @param seed Random seed
	 * @param args Command-line arguments
	 * @param params Model Parameters.
	 */
	public Migration(long seed)
	{
		super(seed);
		//params = new Parameters(seed);
		
	}

	public Parameters getParams()
	{
		return parameters;
	}

	/**
	 * Initialization involves constructing the Terrain, Government, Provinces,
	 * placing Cities and Resources, and constructing the Household and Army
	 * agents.
	 */
	
	@Override
	public void start()
	{
		super.start();
		System.err.println("Migration.start() : executing Map.Map() ...");
		households = new MySequence(1000000);
		this.parameters = new Parameters(this.seed(),null);
		map = new Map(parameters,random);
		map.initializeNativePopulation(households);

		// the DesirabilityMap stuffs
		System.err.println("Migration.start() : now executing" 
				+ " DesirabilityMaps.DesirabilityMaps() ... ");
		dmaps = new DesirabilityMaps(map);
		dmaps.prepareMapsForDesirabilityCalculations(false);	
		dmaps.prepareMapsForRendering();	

		worldAgent = new WorldAgent();
		System.out.println(households.size());
		scheduleAgents();
	}

	/**
	 * Agents must be scheduled in a particular partial order.  This method
	 * keeps all those dependencies in one place.  In the following graph,
	 * arrows represent a "must be run before" relation between the scheduled
	 * objects.
	 *
	 * <center><img src="doc-files/agentExecutionOrder.png" width="300" /></center>
	 */
	private void scheduleAgents()
	{

		int householdOrdering = 2;
		int lastOrdering = 100;

		/*Steppable householdsSequence = (params.system.getNumThreads() == 1) ? 
			 : new ParallelSequence(households, params.system.getNumThreads());*/
		
		schedule.scheduleRepeating(households,householdOrdering,1.0);
		schedule.scheduleRepeating(0,6,worldAgent);
	}

	//<editor-fold defaultstate="collapsed" desc="Accessors">
	public Parameters getParameters()
	{
		return this.parameters;
	}


	public Map getMap()
	{
		return map;
	}

	public DesirabilityMaps getDesirabilityMaps()
	{
		return dmaps;
	}

	public double getModelFitness()
	{
		return dmaps.getCoefficientFitness(map.parameters);
	}

	//</editor-fold>

	public static void main(String[] args)
	{
		doLoop(new MakesSimState()
		{
			@Override
			public SimState newInstance(long seed, String[] args)
			{
				return new Migration(seed);
			}

			@Override
			public Class simulationClass()
			{
				return Migration.class;
			}
		}, args);

	}
}
