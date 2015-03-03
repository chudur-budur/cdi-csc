package phases.phase3;

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

public class Phase3Problem extends Problem implements SimpleProblemForm
{
    Phase3 phase3;  // Need to clone if doing multi-threading

    /**
     *  Initialize the problem
     */
    public void setup(final EvolutionState state, final Parameter ecParams)
    {
        super.setup(state, ecParams);
        System.err.println("Phase3Problem.setup()");
        
        /** with MASON model instatiation **/
        String[] args = null;
        phase3 = new Phase3(System.currentTimeMillis());
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
            long seed = 0;  // XXX Can we get this from the evolution state?
            Phase3 model = new Phase3(seed);

            if(!(ind instanceof DoubleVectorIndividual))
                state.output.fatal("Phase3Problem.evaluate() :" + " DoubleVectorIndividual expected");

            double fitness = 0.0;
            double[] genome = ((DoubleVectorIndividual)ind).genome;

            // decode the genome and initialize model parameters
            
            Parameters params = phase3.parameters;
            params.tempCoeff = genome[0];
            params.portCoeff = genome[1];
            params.riverCoeff = genome[2];
            params.elevCoeff = genome[3];
            phase3.setSocialWeight(genome[4]);
            //phase3.setSocialWeightSpread(genome[5]);
            phase3.setDesExp(genome[6]);
            
            // Iterate the model
            int numYears = 100;  // XXX Put this in the parameters
            phase3.start();
            for (int year = 0; year < numYears; year++)
            {
                phase3.schedule.step(phase3);  // One step
            }
            phase3.finish();

            // Calculate fitness
            fitness = phase3.getFitness();
            
            // Set the individual's fitness
            ((SimpleFitness)ind.fitness).setFitness(state, fitness, false);
            ind.evaluated = true ;
        }
    }
}
