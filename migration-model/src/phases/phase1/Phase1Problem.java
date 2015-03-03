package phases.phase1;

import ec.*;
import ec.util.*; 
import ec.vector.*;
import ec.simple.*;

import sim.engine.* ;

import migration.Migration ;
import environment.Map ;
import migration.parameters.*;

/**
 * This class is used for EC to find the desirability coefficients,
 * this is extended from generic ECJ's Problem.java.
 */

public class Phase1Problem extends Problem 
	implements SimpleProblemForm
{
	Phase1 phase1;  // Need to clone if doing multi-threading

	/**
	 *  Initialize the problem
	 */
	public void setup(final EvolutionState state, final Parameter ecParams)
	{
		super.setup(state, ecParams);
		System.err.println("Phase1Problem.setup()");
		
		/** with MASON model instatiation **/
		String[] args = null;
		phase1 = new Phase1(System.currentTimeMillis(), args);
		//phase1.start();
	}


    /**
     *  Calculate and return the fitness.
     */
	public void evaluate(final EvolutionState state,
			final Individual ind,
			final int subpopulation,
			final int threadnum)
	{
		if(!ind.evaluated)
		{
			if( !(ind instanceof DoubleVectorIndividual))
				state.output.fatal("Phase1Problem.evaluate() :" 
						+ " DoubleVectorIndividual expected");

			double fitness = 0.0;
			double[] genome = ((DoubleVectorIndividual)ind).genome;

			// decode the genome and initialize model parameters
			phase1.setTempDesCoefficient(genome[0]);
            phase1.setPortDesCoefficient(genome[1]);
            phase1.setRiverDesCoefficient(genome[2]);
            phase1.setElevDesCoefficient(genome[3]);

            // Iterate the model
            phase1.start();
            phase1.schedule.step(phase1);  // One step
            phase1.finish();

			// Calculate fitness
			fitness = phase1.getFitness();
			
			// Set the individual's fitness
			((SimpleFitness)ind.fitness).setFitness(state, fitness, false);
			ind.evaluated = true ;
		}
	}
}
