package phases.phase2;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

import nl.peterbloem.powerlaws.Continuous;
import phases.BasePhase;
import phases.ECInterface;
import phases.Utility;
import sim.engine.MakesSimState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import util.MersenneTwisterFastApache;
import ec.util.MersenneTwisterFast;
import environment.Cell;
import environment.Map;

public class Phase2 extends BasePhase implements ECInterface
{
	private static final long serialVersionUID = 1L;

	IntGrid2D sprinkleGrid;

	public boolean clipAtXMin = false;

	public double empiricalXMin = 1928.0;	// already calculated
	public double sprinkledXMin = 0;
	
	

//	private double socialAttractionMu = 1.0;
//	public double getSocialAttractionMu() { return socialAttractionMu; }
//	public void setSocialAttractionMu(double val) { socialAttractionMu = val; }
//
//	private double socialAttractionSigma = 1.0;
//	public double getSocialAttractionSigma() { return socialAttractionSigma; }
//	public void setSocialAttractionSigma(double val) { socialAttractionSigma = val; }

	public int getPopSize() { return parameters.popSize; }
	public void setPopSize(int val) { parameters.popSize = val; }
	
	public double getSocialWeight() { return parameters.socialWeight; }
	public void setSocialWeight(double val) { parameters.socialWeight = val; }
	
	public double getSocialWeightSpread() { return parameters.socialWeightSpread; }
	public void setSocialWeightSpread(double val) { parameters.socialWeightSpread = val; }
	
	public double getDesExp() { return parameters.desExp; }
	public void setDesExp(double val) { parameters.desExp = val; }
	
	public int getRecalculationSkip() { return parameters.recalculationSkip; }
	public void setRecalculationSkip(int val) { parameters.recalculationSkip = val; }
	
	public boolean calculatePowerLaw = false;
	public boolean getCalculatePowerLaw() { return calculatePowerLaw; }
	public void setCalculatePowerLaw(boolean val) { calculatePowerLaw = val; }
	
//	public boolean useZScores = false;
//	public boolean getUseZScores() { return useZScores; }
//	public void setUseZScores(boolean val) { useZScores = val; }

	public boolean reportProgress = false;
	public boolean getReportProgress() { return reportProgress; }
	public void setReportProgress(boolean val) { reportProgress = val; }
	
	
	
	private double fitness = 0;
	public double getFitness()
	{
		return fitness;
	}
	

	
	
	/**
     * Constructs a new simulation.  Side-effect: sets the random seed of
     * Parameters to equal seed.
     * 
     * @param seed Random seed
     * @param args Command-line arguments
     * @param params Model Parameters.
     */
    public Phase2(long seed)
    {
        this(seed,null);        
    }

    public Phase2(long seed, String[] args)
    {
    	super(seed, args);

//      calcPowerLaw(map.getPopGrid());
        System.out.println("PowerLaw exp: 1.846053, xMin: 1928.000000, significance: 0.312800");
        sprinkleGrid = new IntGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT);
    }
    
    private void calcPowerLaw(IntGrid2D pGrid) {
		ArrayList<Double> cellSizes = new ArrayList<Double>();
		for (Cell cell : map.canadaCells)
			if (pGrid.field[cell.x][cell.y] > 0)
				cellSizes.add((double)pGrid.field[cell.x][cell.y]);

		Continuous distribution = Continuous.fit(cellSizes).fit();
		
		if (distribution == null) {
			System.out.println("Power Law fit failed, probably because of too few data points.");
			return;
		}

		// * Retrieve the distribution's parameters
		double exponent = distribution.exponent();
		double xMin = distribution.xMin();
		System.out.format("PowerLaw exp: %f, xMin: %f", exponent, xMin);
		
		sprinkledXMin = xMin;
		
//		double significance = distribution.significance(cellSizes, 100);
//		System.out.format(", significance: %f", exponent, xMin, significance);
		
		System.out.println();
    }
    

	private int[] getSprinkledPopData() {
		return getSprinkledPopData(Integer.MIN_VALUE);
	}
    

	private int[] getSprinkledPopData(int minVal) {
		if (sprinkleGrid == null)
			return null;
		
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		
		for (Cell cell : map.canadaCells) {
			if (sprinkleGrid.field[cell.x][cell.y] > minVal)
				sizes.add(sprinkleGrid.field[cell.x][cell.y]);
		}
		
		int[] a = new int[sizes.size()];
		for (int i = 0; i < a.length; i++)
			a[i] = sizes.get(i);
		
		return a;
	}
	
	public double[] randomSample(int n) {
		double[] a = new double[n];
		for (int i = 0; i < n; i++) {
			a[i] = random.nextGaussian();
		}
		return a;
	}

/*
	public double calcInvertedKolmogorovSmirnovStatistic(double[] a, double[] b, MersenneTwisterFast random) {
    	KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest(new MersenneTwisterFastApache(random));
    	double ksStatistic = Double.NaN;
    	
    	try {
    		ksStatistic = ksTest.kolmogorovSmirnovStatistic(a, b);
    	}
    	catch(Exception e) {}
    	
    	return (1 - ksStatistic);
	}
*/
    
    private void runKolmogorovSmirnovTest() {
    	double[] empirical = Utility.convertToDoubles(map.getPopGridData(1));
    	double[] sprinkled = Utility.convertToDoubles(getSprinkledPopData(1));
    	
    	double invKSStatistic = calcInvertedKolmogorovSmirnovStatistic(empirical, sprinkled, random);
    	System.out.format("Inv. KS statistic: %.2f\n", invKSStatistic);
    }
    


//    private double runChiSquareTest() {
//
//    	int[] empirical = getEmpiricalPopInts();
//    	int[] sprinkled = getSprinkledPopInts();
////
////    	Frequency empiricalFreq = new Frequency();
////    	Frequency sprinkledFreq = new Frequency();
////    	
////    	for (int i = 0; i < empirical.length; i++)
////    		empiricalFreq.addValue(empirical[i]);
//    	
//    	EmpiricalDistribution empiricalFreq = new EmpiricalDistribution(25);
//    	
////    	empiricalFreq.load(getEmpiricalPopDoubles());
//    	
//    	
//    	ChiSquareTest csTest = new ChiSquareTest();
//    	double pValue = csTest.chiSquareTest(empirical, sprinkled);
//    }
    
    /**
     * Returns the KL divergence, K(p1 || p2).
     *
     * The log is w.r.t. base 2. <p>
     *
     * *Note*: If any value in <tt>p2</tt> is <tt>0.0</tt> then the KL-divergence
     * is <tt>infinite</tt>. Limin changes it to zero instead of infinite. 
     * 
     */
/*
    public static double klDivergence(double[] p1, double[] p2) {
      double klDiv = 0.0;

		for (int i = 0; i < p1.length; ++i) {
			if ((p1[i] == 0.0) || (p2[i] == 0.0)) 
				continue;

			klDiv += p1[i] * Math.log(p1[i] / p2[i]);
      }

      return klDiv / Math.log(2); 
    }
    
    public double calcKLDivergenceMetric(double[] a, double[] b) {
    	Arrays.sort(a);
    	Arrays.sort(b);

    	Utility.normalizeValues(a);
    	Utility.normalizeValues(b);

    	double klDivAB = klDivergence(a, b);
    	double klDivBA = klDivergence(b, a);
    	
    	double klDiv = Math.abs(klDivAB) + Math.abs(klDivBA);	// range between 0 and ~7, with 0 being better

    	return klDiv;
    }
    
    private void runKLDivergenceTest() {
    	double[] empirical = Utility.convertToDoubles(map.getPopGridData());
    	double[] sprinkled = Utility.convertToDoubles(getSprinkledPopData());
    	
    	double klDiv = calcKLDivergenceMetric(empirical, sprinkled);  // TODO: This is probably wrong
    	System.out.format("Mod. KL Div:       %.2f\n", klDiv);
    }
*/

    /**
     * Initialization involves constructing the Terrain, Government, Provinces,
     * placing Cities and Resources, and constructing the Household and Army
     * agents.
     */
    @Override
    public void start()
    {
        super.start();
        double startTime, duration;

        map.updateTotalDesirability(parameters);
        
        startTime = System.currentTimeMillis();
        PeopleSprinkler.sprinklePeople(parameters.popSize, sprinkleGrid, map, parameters.socialWeight, parameters.socialWeightSpread, parameters.desExp, parameters.recalculationSkip, reportProgress, random);
        duration = System.currentTimeMillis() - startTime;
        System.out.format("Sprinkle duration (s): %.1f\n", duration*0.001);
        
        if (calculatePowerLaw) {
	        startTime = System.currentTimeMillis();
	        calcPowerLaw(sprinkleGrid);
	        duration = System.currentTimeMillis() - startTime;
	        System.out.format("PowerLaw calculation duration (s): %.1f\n", duration*0.001);
        }

        runKolmogorovSmirnovTest();        
        //runKLDivergenceTest();
        
        fitness = calcFitness();
        
        schedule.scheduleRepeating(new Steppable() {
			public void step(SimState state) {
				// do nothing
			}
		});
    }
        
	/**
	 * Modified version of Utility.chooseStochastically that takes an array of
	 * cumulative probabilities rather than calculating them itself.
	 * 
	 */
	public int chooseStochastically(double [] cumulProbs, double total, MersenneTwisterFast rand) {
		int val = Arrays.binarySearch(cumulProbs, rand.nextDouble() * total);
		if (val < 0)
			val = -(val + 1);		// Arrays.binarySearch(...) returns (-(insertion point) - 1) if the key isn't found

		if (val == cumulProbs.length)
			System.out.format("Error: val:%d, total:%f\n", val, total);
		
		return val;
	}

	
    /**
     * Returns an array containing the population distribution derived by the model,
     * but for only the grid cells we care about (i.e. Canada).
     * All the values in the array should sum to 1.
     * Override this in each Phase class.
     */
    public double[] gimmeModelPop()
    {
        double[] modelPop = Utility.convertToDoubles(getSprinkledPopData(1));
        Utility.normalizeValues(modelPop);
        return modelPop;
    }


    /**
     * Returns an array containing the empirical population distribution after
     * being Gaussian smoothed, but for only the grid cells we care about (i.e. Canada).
     * All the values in the array should sum to 1.
     * Override this in each Phase class.
     */
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
    public double[] gimmeSmoothedModelPop()
    {
        // Smooth the sprinkle grid and calculate z-scores
        DoubleGrid2D smoothedSprinkleGrid = Utility.smoothGrid(map, sprinkleGrid, 9, 9, 3, 2);

        return extractSmoothedDataFromGrid(smoothedSprinkleGrid);
    }


	/**
	 * Calculates the similarity between smoothed versions of the sprinkled and
	 * target (i.e. LandScan) populations.
	 * 
	 * @return fitness
	 */
/*
    public double calcSmoothedFitness()
    {
        // Smooth the sprinkle grid and calculate z-scores
        DoubleGrid2D smoothedSprinkleGrid = Utility.smoothGrid(map, sprinkleGrid, 9, 9, 3, 2);

        DoubleGrid2D modelGrid = smoothedSprinkleGrid;
        DoubleGrid2D targetGrid = smoothedTargetPopGrid;

        //DoubleGrid2D zscoreSmoothedSprinkleGrid = smoothedSprinkleGrid;
        //Utility.updateGridWithZscores(zscoreSmoothedSprinkleGrid, map.canadaCells);
        //DoubleGrid2D modelGrid = zscoreSmoothedSprinkleGrid;
        //DoubleGrid2D targetGrid = zscoreSmoothedTargetPopGrid;

        double sumSpr = 0, sumTar = 0;
        double[] modelPop = new double[map.canadaCells.size()];
        double[] targetPop = new double[map.canadaCells.size()];
        int i = 0;
        double KL_adjust = 1.0; // 1.0;  Kullback-Leibler doesn't handle 0's very well.
        for (Cell cell : map.canadaCells)
        {
            modelPop[i] = modelGrid.field[cell.x][cell.y] + KL_adjust;
            targetPop[i] = targetGrid.field[cell.x][cell.y] + KL_adjust;
            sumSpr += modelPop[i];
            sumTar += targetPop[i];
            i++;
        }
        System.out.format("sumSpr: %.5f\n", sumSpr);
        System.out.format("sumTar: %.5f\n", sumTar);

        //double smoothFitness = calcInvertedKolmogorovSmirnovStatistic(targetPop, modelPop, random);
        double smoothFitness = 1.0/(1.0 + calcKLDivergenceMetric(targetPop, modelPop));  // range 0 to 1
        //double smoothFitness = 1.0/(1.0 + Utility.sumOfSquaredDifferences(targetPop, modelPop)/targetPop.length);
        //double smoothFitness = 1.0/(1.0 + Math.sqrt(Utility.sumOfSquaredDifferences(targetPop, modelPop)/targetPop.length));
        
        return smoothFitness;
    }
*/
    
    /**
     * Measures how similar two population distributions are in terms of "spikiness".
     * In other words, how similar are two population distributions, independent of
     * any spatial component.
     * 
     * @return Fitness as a similarity metric between 0 and 1
     */
/*
    public double calcSpikinessFitness()
    {
        double[] empirical = Utility.convertToDoubles(map.getPopGridData(1));
        double[] model = Utility.convertToDoubles(getSprinkledPopData(1));
        
        double invKSStatistic = calcInvertedKolmogorovSmirnovStatistic(empirical, model, random);
        return invKSStatistic;
    }
*/
    
    /**
     * Calculates the desirability maps coefficients fitness 
     * with respect to the population distribution, used as a
     * EC evaluation function, it actually calculates an RMS
     * pixel-by-pixel difference.
     */
/*
    public double calcFitness()
    {
        double smoothedFitness = calcSmoothedFitness();
        double spikinessFitness = calcSpikinessFitness();
        double totalFitness = smoothedFitness * spikinessFitness;
        System.out.format("SmoothedFitness: %f\n", smoothedFitness);
        System.out.format("SpikinessFitness: %f\n", spikinessFitness);
        System.out.format("TotalFitness: %f\n", totalFitness);
        
        return totalFitness;
    }
*/

    /**
     * Calculates the desirability maps coefficients fitness 
     * with respect to the population distribution, used as a
     * EC evaluation function, it actually calculates an RMS
     * pixel-by-pixel difference.
     */
    public double testFitness()
    {        
        // Smooth the target population grid and calculate z-scores
//      IntGrid2D targetPopGrid = map.getPopulationGrid();
//        DoubleGrid2D smoothedTargetPopGrid = Utility.smoothGridWithMapConstraint(map, targetPopGrid);
//        DoubleGrid2D zscoreSmoothedTargetPopGrid = new DoubleGrid2D(smoothedTargetPopGrid);
//        Utility.updateGridWithZscores(zscoreSmoothedTargetPopGrid, map.canadaCells);
        
        ///// Original code
//        // Smooth the sprinkle grid and calculate z-scores
//        DoubleGrid2D smoothedSprinkleGrid = Utility.smoothGridWithMapConstraint(map, sprinkleGrid);
//        DoubleGrid2D zscoreSmoothedSprinkleGrid = new DoubleGrid2D(smoothedSprinkleGrid);
//        Utility.updateGridWithZscores(zscoreSmoothedSprinkleGrid, map.canadaCells);
//        
//        double sumSq = Utility.sumOfSquaredDifferences(zscoreSmoothedSprinkleGrid, zscoreSmoothedTargetPopGrid, map.canadaCells);

        int totalEmpPop = 0;
        for (Cell c : map.canadaCells)
            totalEmpPop += c.empPop;
        
        System.out.format("Empirical data: %d people in %d cells.\n", totalEmpPop, map.canadaCells.size());

        IntGrid2D targetPopGridInts = map.getPopulationGrid();
        DoubleGrid2D targetPopGrid = new DoubleGrid2D(Utility.castToDoubleField(targetPopGridInts));
        DoubleGrid2D zscoreTargetPopGrid = new DoubleGrid2D(targetPopGrid);
        Utility.updateGridWithZscores(zscoreTargetPopGrid, map.canadaCells);

        // Smooth the sprinkle grid and calculate z-scores
        DoubleGrid2D smoothedSprinkleGrid = Utility.smoothGridWithMapConstraint(map, sprinkleGrid);
        DoubleGrid2D zscoreSmoothedSprinkleGrid = new DoubleGrid2D(smoothedSprinkleGrid);
        Utility.updateGridWithZscores(zscoreSmoothedSprinkleGrid, map.canadaCells);
        
        DoubleGrid2D sprinkleGridDoubles = new DoubleGrid2D(Utility.castToDoubleField(sprinkleGrid));
        DoubleGrid2D zscoreSprinkleGrid = new DoubleGrid2D(Utility.castToDoubleField(sprinkleGrid));
        
        double sumSq;

        sumSq = Utility.sumOfSquaredDifferences(sprinkleGridDoubles, targetPopGrid, map.canadaCells);
        System.out.format("RMS Error:                     %f\n", Math.sqrt(sumSq / map.canadaCells.size()));
        
        Utility.updateGridWithZscores(zscoreSprinkleGrid, map.canadaCells);
        
        sumSq = Utility.sumOfSquaredDifferences(zscoreSprinkleGrid, zscoreTargetPopGrid, map.canadaCells);
        System.out.format("RMS Error (z-scores):          %f\n", Math.sqrt(sumSq / map.canadaCells.size()));
        
        sumSq = Utility.sumOfSquaredDifferences(smoothedSprinkleGrid, smoothedTargetPopGrid, map.canadaCells);
        System.out.format("RMS Error (smoothed):          %f\n", Math.sqrt(sumSq / map.canadaCells.size()));

        sumSq = Utility.sumOfSquaredDifferences(zscoreSmoothedSprinkleGrid, zscoreSmoothedTargetPopGrid, map.canadaCells);
        System.out.format("RMS Error (smoothed z-scores): %f\n", Math.sqrt(sumSq / map.canadaCells.size()));

        double err = Math.sqrt(sumSq / map.canadaCells.size()); 

        // bigger rms value means less resemblance
        // and ecj by default maximizes, so --

        return (1.0 / err);
    }


	@Override
	public IntGrid2D gimmePopulationGrid() {
		return sprinkleGrid;
	}

	@Override
	public int gimmeRunDuration() {
		return 0;
	}
    
    public static void main(String[] args)
    {
      doLoop(new MakesSimState()
        {
            @Override
            public SimState newInstance(long seed, String[] args)
            {
                return new Phase2(seed, args);
            }

            @Override
            public Class simulationClass()
            {
                return Phase2.class;
            }
        }, args);

    }
    
}
