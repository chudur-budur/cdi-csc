package phases.phase2;

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

public class Phase2Problem extends Problem implements SimpleProblemForm
{
	Phase2 phase2;  // Need to clone if doing multi-threading

	/**
	 *  Initialize the problem
	 */
	public void setup(final EvolutionState state, final Parameter ecParams)
	{
		super.setup(state, ecParams);
		System.err.println("Phase2Problem.setup()");
		
		/** with MASON model instatiation **/
		String[] args = null;
		phase2 = new Phase2(System.currentTimeMillis());
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
			if(!(ind instanceof DoubleVectorIndividual))
				state.output.fatal("Phase2Problem.evaluate() :" + " DoubleVectorIndividual expected");

			double fitness = 0.0;
			double[] genome = ((DoubleVectorIndividual)ind).genome;

			// decode the genome and initialize model parameters
			
			Parameters params = phase2.parameters;
            params.tempCoeff = genome[0];
            params.portCoeff = genome[1];
            params.riverCoeff = genome[2];
            params.elevCoeff = genome[3];
			phase2.setSocialWeight(genome[4]);
			phase2.setSocialWeightSpread(genome[5]);
            phase2.setDesExp(genome[6]);
			
            // Iterate the model
            phase2.start();
            phase2.schedule.step(phase2);  // One step
            phase2.finish();

			// Calculate fitness
			fitness = phase2.getFitness();
			
			// Set the individual's fitness
			((SimpleFitness)ind.fitness).setFitness(state, fitness, false);
			ind.evaluated = true ;
		}
	}
}
