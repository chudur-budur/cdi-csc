package phases.phase2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.util.Double2D;
import ec.util.MersenneTwisterFast;
import environment.Cell;
import environment.Map;

/**
 * This class contains the logic and related functions for stochastically sprinkling 
 * people into a grid based on natural desirability and social attraction.
 * 
 * @author Joey Harrison
 */
public class PeopleSprinkler
{

	/**
	 * Modified version of Utility.chooseStochastically that takes an array of
	 * cumulative probabilities rather than calculating them itself.
	 */
	public static int chooseStochasticallyFromCumulativeProbs(double [] cumulProbs, double total, MersenneTwisterFast rand) {
		int val = Arrays.binarySearch(cumulProbs, rand.nextDouble() * total);
		if (val < 0)
		{
			val = -(val + 1);		// Arrays.binarySearch(...) returns (-(insertion point) - 1) if the key isn't found
		}
		
		if (val == cumulProbs.length)
			System.out.format("Error: val:%d, total:%f\n", val, total);
		
		return val;
	}
	
	/**
	 * Calculate the cumulative probabilities for the given array probabilities.
	 * @return the sum of the probabilities
	 */
	public static double calcCumulativeProbs(double[] probs, double[] cumulProbs) {
		double sum = 0;
		for (int j = 0; j < probs.length; j++) {
			sum += probs[j];
			cumulProbs[j] = sum;
		}
		
		return sum;
	}
	
	/**
	 * Calculate the bounds of the total desirability grid in the given map.
	 * @return A Double2D where x holds the min and y holds the max.
	 */
	public static Double2D calcDesirabilityBounds(Map map) {
    	// calculate bounds of desirability map
    	double min = Double.MAX_VALUE;
    	double max = Double.MIN_VALUE;
    	for (Cell cell : map.canadaCells){
				double des = cell.totalDes;
				if ((des == 0) || Double.isInfinite(des))
					continue;
				if (des < min)
					min = des;
				if (des > max)
					max = des;
			}
		
    	return new Double2D(min, max);
    }
	
	private static Double2D calcDesirabilityBounds(
			ArrayList<Cell> cellOfInterest, DoubleGrid2D desGrid) {
		// calculate bounds of desirability map
    	double min = Double.MAX_VALUE;
    	double max = Double.MIN_VALUE;
    	for (Cell cell : cellOfInterest){
				double des = desGrid.field[cell.x][cell.y];
				if ((des == 0) || Double.isInfinite(des))
					continue;
				if (des < min)
					min = des;
				if (des > max)
					max = des;
			}
		
    	return new Double2D(min, max);
	}
	
	
	/**
	 * Increase the scores of the cells adjacent (in the 8-set or Moore neighborhood) 
	 * to the given cell. The function checks to make sure the scores it increases 
	 * belong to valid cells.
	 * 
	 * @param scores Array of scores, some of which will be increased.
	 * @param amount Amount by which to increase the adjacent cells.
	 * @param cell The cell at the center.
	 * @param map The map containing all the grids in the model.
	 */
	public static void increaseAdjacentScores(double[] scores, double amount, Cell cell, Map map, HashMap<Cell, Integer> indexMap) {
		for (int y = cell.y - 1; y <= cell.y + 1; y++)
			for (int x = cell.x - 1; x <= cell.x + 1; x++)
				if (((y != cell.y) || (x != cell.x)) &&
					(x >= 0) && (x < Map.GRID_WIDTH) &&
					(y >= 0) && (y < Map.GRID_HEIGHT)) {
					Cell c = (Cell)map.cellGrid.field[x][y];
					if ((c != null))// && (c.totalDes > 0))
					{
						int index = -1;
						if(indexMap.containsKey(c))
						{
							index = indexMap.get(c);
							scores[index] += amount;
						}	
					}
				}
	}
	
	/**
	 * Increase the scores of the cells adjacent (in the 8-set or Moore neighborhood) 
	 * to the given cell. The function checks to make sure the scores it increases 
	 * belong to valid cells.
	 * 
	 * @param scores Array of scores, some of which will be increased.
	 * @param amount Amount by which to increase the adjacent cells.
	 * @param cell The cell at the center.
	 * @param map The map containing all the grids in the model.
	 */
	public static void increaseAdjacentScores(double[] scores, double amount, Cell cell, Map map) {
		PeopleSprinkler.increaseAdjacentScores(scores,amount,cell,map,map.indexMap);
	}


	/**
	 * Sprinkle "people" into the given grid. The people will be placed stochastically
	 * based on the natural desirability and social attractiveness of each cell.
	 * 
	 * @param n Number of people to sprinkle
	 * @param grid Grid into which people should be sprinkled. Note that this is an IntGrid2D 
	 * 		  so each sprinkling just results in the increment of the value in the cell.
	 * @param map Map containing all the grids in the model
	 * @param socialWeight How much more attractive a cell becomes for each person in it
	 * @param socialWeightSpread What proportion of the socialWeight that gets spread to adjacent cells (8-set)
	 * @param desirabilityExp Desirability exponent. The natural desirability is raised to this power
	 * 		  when calculating the overall desirability of each cell. A value greater than 1 makes 
	 * 		  low-desirability cells even less desirable. 
	 * @param recalculationSkip How often the cumulative probabilities get recalculated.
	 * 		  A value of 0 means they will never get recalculated, 1 means they'll be 
	 *        recalculated each step, 10 means they'll be recalculated every ten steps.
	 *        These recalculations are expensive, so this is for optimization.
	 * @param reportProgress If true, the function prints percentages to the console
	 * 		  to indicate how close it is to being finished.
	 * @param random Random number generator used for stochastically choosing cells.
	 */
    public static void sprinklePeople(int n, IntGrid2D grid, Map map, ArrayList<Cell> cellOfInterest, DoubleGrid2D desGrid, double socialWeight, double socialWeightSpread, 
    		double desirabilityExp, int recalculationSkip, boolean reportProgress, MersenneTwisterFast random) {

    	if (reportProgress)
    		System.out.println("Sprinkling people...");
    	
    	for (int[] row : grid.field)
    		Arrays.fill(row, 0);

    	//Double2D minMax = calcDesirabilityBounds(map);
    	Double2D minMax = calcDesirabilityBounds(cellOfInterest, desGrid);
    	double desMin = minMax.x;
    	double desMax = minMax.y;
    	double desRange = desMax - desMin;

		// calculate the natural desirabilty scores
    	double[] scores = new double[cellOfInterest.size()];
    	//double[] scores = new double[map.canadaCells.size()];

    	HashMap<Cell,Integer> indexInCellOfInterest = new HashMap<Cell,Integer>();
    	
    	int index = 0;
		//for (Cell cell : map.canadaCells) {
    	for(Cell cell:cellOfInterest) {
			//double des = map.totalDes.field[cell.x][cell.y];
			double des = desGrid.field[cell.x][cell.y];
			indexInCellOfInterest.put(cell, index);
			scores[index++] = Math.pow((des - desMin) / desRange, desirabilityExp);
		}
		
		double [] cumulProbs = new double[scores.length];
		double total = calcCumulativeProbs(scores, cumulProbs);
		
    	int ticks = Math.max(1, n / 100);
    	for (int i = 0; i < n; i++) {
			int loc = chooseStochasticallyFromCumulativeProbs(cumulProbs, total, random);
		
			//Cell cell = map.canadaCells.get(loc);
			Cell cell = cellOfInterest.get(loc);
			cell.addHousehold();
			grid.field[cell.x][cell.y]++;
			scores[loc] += socialWeight;
			
			// add discounted socialWeight to adjacent cells
			if (socialWeightSpread != 0.0)
				increaseAdjacentScores(scores, (socialWeight * socialWeightSpread), cell, map, indexInCellOfInterest);
			
			if ((recalculationSkip > 0) && (i % recalculationSkip == 0))
				total = calcCumulativeProbs(scores, cumulProbs);
			
			if (reportProgress && (i % ticks == 0))
				System.out.format("%.0f%%\n", 100.0 * i / (double)n);
    	}
    }

	public static void sprinklePeople(int n, IntGrid2D grid, Map map,
			double socialWeight, double socialWeightSpread,
			double desirabilityExp, int recalculationSkip,
			boolean reportProgress, MersenneTwisterFast random) {

		PeopleSprinkler.sprinklePeople(n, grid, map, map.canadaCells,
				map.totalDes, socialWeight, socialWeightSpread,
				desirabilityExp, recalculationSkip, reportProgress, random);

	}
    
	
}
