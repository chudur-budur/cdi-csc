package phases.phase1;


import java.util.Arrays;

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

import ec.util.MersenneTwisterFast;
import environment.Cell;
import phases.BasePhase;
import phases.Utility;
import sim.engine.MakesSimState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import util.MersenneTwisterFastApache;


public class Phase1 extends BasePhase
{
	private static final long serialVersionUID = 1L;


    public Phase1(long seed)
    {
    	this(seed, null);
    }
    
    public Phase1(long seed, String[] args)
    {
    	super(seed, args);
    }
    
    @Override
    public void start()
    {
        super.start();
        
        map.updateTotalDesirability(parameters);
        
        fitness = calcFitness();
        
        schedule.scheduleRepeating(new Steppable() {
			public void step(SimState state) {
				// do nothing
			}
		});
    }


    /**
     * Returns an array containing the population distribution derived by the model,
     * but for only the grid cells we care about (i.e. Canada).
     * All the values in the array should sum to 1.
     * Override this in each Phase class.
     */
    @Override
    public double[] gimmeModelPop()
    {
        return extractSpikinessDataFromGrid(map.totalDes);
    }


    /**
     * Returns an array containing the empirical population distribution after
     * being Gaussian smoothed, but for only the grid cells we care about (i.e. Canada).
     * All the values in the array should sum to 1.
     * Override this in each Phase class.
     */
    @Override
    public double[] gimmeSmoothedEmpiricalPop()
    {
        return extractSmoothedDataFromGrid(smoothedTargetPopGrid);
    }


    /**
     * Returns an array containing the Gaussian smoothed population distribution
     * derived by running the model, but for only the grid cells we care about (Canada).
     * All the values in the array should sum to 1.
     * Override this in each Phase class.
     */
    @Override
    public double[] gimmeSmoothedModelPop()
    {
        return extractSmoothedDataFromGrid(map.totalDes);
    }



	@Override
	public IntGrid2D gimmePopulationGrid() {
		return map.getPopulationGrid();
	}

	@Override
	public int gimmeRunDuration() {
		return 0;
	}
	
    public static void main(String[] args)
    {
    	// we have a new Migration instance instead of phase2?
      doLoop(new MakesSimState()
        {
            @Override
            public SimState newInstance(long seed, String[] args)
            {
                return new Phase1(seed, args);
            }

            @Override
            public Class simulationClass()
            {
                return Phase1.class;
            }
        }, args);

    }
    
}
