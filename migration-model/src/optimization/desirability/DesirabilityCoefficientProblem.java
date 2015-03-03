package optimization.desirability ;

import ec.* ;
import ec.util.*; 
import ec.vector.*;
import ec.simple.*;
import sim.engine.* ;
import migration.Migration ;
import environment.Map ;
import migration.parameters.*;

/**
 * This class is used for EC to find the desirability coefficients,
 * this is exented from generic ECJ's Problem.java.
 */

public class DesirabilityCoefficientProblem extends Problem 
	implements SimpleProblemForm
{
	Migration migration ;
	Map map ;
	DesirabilityMaps dmaps ;
	
	public void setup(final EvolutionState state, final Parameter base)
	{
		super.setup(state, base);
		System.err.println("DesirabilityCoefficientProblem.setup()");
        Thread.dumpStack();
        System.exit(0);
        //throw new UnsupportedOperationException("Deprecated");
		
		/** with MASON model instatiation **/
		/*migration = new Migration(System.currentTimeMillis());
		migration.start();*/
		
		/** with no MASON instantiation, directly instantiating a Map **/
		Parameters parameters = new Parameters(System.currentTimeMillis(), null);
				
		MersenneTwisterFast mtf = new MersenneTwisterFast();
		map = new Map(parameters, mtf);
		dmaps = new DesirabilityMaps(map);
		dmaps.prepareMapsForDesirabilityCalculations(false);
	}

	public void evaluate(final EvolutionState state,
			final Individual ind,
			final int subpopulation,
			final int threadnum)
	{
        Thread.dumpStack();
	    System.exit(0);
		if(!ind.evaluated)
		{
			if( !(ind instanceof DoubleVectorIndividual))
				state.output.fatal("DesirabilityCoefficientProblem.evaluate() :" 
						+ " DoubleVectorIndividual expected");

			double fitness = 0.0 ;
			double[] coeff = ((DoubleVectorIndividual)ind).genome ;
			
			/** with MASON model instantiation **/
			/*
			// get params
			Parameters param = migration.getParams();
			// set params
			param.desCoeff.setAllDesCoeff(coeff);
			
			// re-initialize the model and 
			// do some N steps here ..

			// then get fitness of the MASON model
			fitness = migration.getModelFitness();*/
			
			/** without MASON model instantiation **/
			// get the fitness from the map
			fitness = dmaps.getCoefficientFitness(coeff);
			
			// set fitness
			((SimpleFitness)ind.fitness).setFitness(state, fitness, false);
			ind.evaluated = true ;
		}
	}
}
