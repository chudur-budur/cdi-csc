package migration.util;

import java.util.HashMap;

import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import sim.util.Double2D;

/**
 * Simulation from various distributions with the Apache commons math library.
 * Creates and maintains a distribution object with the given seed for each
 * set of parameters requested.
 * 
 * @author Eric 'Siggy' Scott
 * @author Ahmed Elmolla
 */
public class Distributions
{
    private long seed;
    HashMap<Double2D, LogNormalDistribution> logNormalInstances;
    HashMap<Double2D, WeibullDistribution> weibullInstances;
    HashMap<Double, ExponentialDistribution> exponentialInstances;
    HashMap<Double2D, NormalDistribution> normalInstances;
    
    
    public Distributions(long seed)
    {
        this.seed = seed;
        logNormalInstances = new HashMap();
        weibullInstances = new HashMap();
        exponentialInstances = new HashMap();
        normalInstances = new HashMap();
    }
    
    public double lognormalSample(double mu, double sigma)
    {
        Double2D parameters = new Double2D(mu, sigma);
        LogNormalDistribution dist;
        if (logNormalInstances.containsKey(parameters))
            dist = logNormalInstances.get(parameters);
        else
        {
            dist = new LogNormalDistribution(mu, sigma);
            dist.reseedRandomGenerator(seed);
            logNormalInstances.put(parameters, dist);
        }
        
        return dist.sample();
    }
    
    public double weibullSample(double shape, double scale)
    {
        Double2D parameters = new Double2D(shape, scale);
        WeibullDistribution dist;
        if (weibullInstances.containsKey(parameters))
            dist = weibullInstances.get(parameters);
        else
        {
            dist = new WeibullDistribution(shape, scale);
            dist.reseedRandomGenerator(seed);
            weibullInstances.put(parameters, dist);
        }
        
        return dist.sample();
    }
    
    public double poissonIntervalSample(double interval)
    {
        ExponentialDistribution dist;
        if (exponentialInstances.containsKey(interval))
            dist = exponentialInstances.get(interval);
        else
        {
            dist = new ExponentialDistribution(interval);
            dist.reseedRandomGenerator(seed);
            exponentialInstances.put(interval, dist);
        }
        
        return dist.sample();
    }
    
    public double gaussianSample(double mean, double sd)
    {
        Double2D parameters = new Double2D(mean, sd);
        NormalDistribution dist;
        if (normalInstances.containsKey(parameters))
            dist = normalInstances.get(parameters);
        else
        {
            dist = new NormalDistribution(mean, sd);
            dist.reseedRandomGenerator(seed);
            normalInstances.put(parameters, dist);
        }
        
        return dist.sample();
    }
}
