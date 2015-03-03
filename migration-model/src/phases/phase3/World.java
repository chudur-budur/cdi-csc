package phases.phase3;

import java.util.ArrayList;
import java.util.Collections;

import phases.Utility;
import phases.phase2.PeopleSprinkler;
import environment.Cell;
import environment.Map;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.util.Double2D;


/**
 * 
 * @author Ermo Wei
 *
 * This agent is responsible for update the  and also add the new born people
 *
 */
public class World implements Steppable{

	public double[] scores;
	private Phase3 model;
	private double desMin, desMax, desRange;
	private int counter = 0;
	private double totalProb = 0;
	private double[] cumulProb = null;
	
	public World(Phase3 model)
	{
		this.model = model;
		counter = 0;
		scores = new double[model.map.canadaCells.size()];
		Double2D minMax = PeopleSprinkler.calcDesirabilityBounds(model.map);
    	desMin = minMax.x;
    	desMax = minMax.y;
    	desRange = desMax - desMin;
    	int index = 0;
		for (Cell cell : model.map.canadaCells) 
			scores[index++] = Math.pow(calcNaturalScore(cell.x, cell.y), model.getDesExp());
		distributeSocialWeight();
		this.updateCumulativeProbs();
	}

	@Override
	public void step(SimState state) {
		
		// increase the counter
		counter++;
		
		if((counter>0)&&(counter%model.recalculationSkip==0))
		{
			//System.out.println("update cumulative probs "+counter);
			this.updateCumulativeProbs();
		}
		
		
		
		// update the residence grid
		model.updateResidenceGrid();
		
		// update the infrastructure
		this.updateInfrastructure();
		this.updateDesirability();
	
		
		model.updateInfrastructureGrid();
		model.updateOpportunity();
	}
	
	
	private void updateDesirability() {
		DoubleGrid2D diffGrid = new DoubleGrid2D(Map.GRID_WIDTH,Map.GRID_HEIGHT,0);
		for(Cell cell:model.map.canadaCells)
		{
			double diff = cell.infrastructure - cell.population;
			diffGrid.field[cell.x][cell.y] = diff;	
		}
		
		Utility.updateGridWithZscores(diffGrid, model.map.canadaCells);
		for(Cell cell:model.map.canadaCells)
		{
			double rate = model.infrastructureCoeff * diffGrid.get(cell.x, cell.y);
			// update at two place to make it consistent
			cell.totalDes += rate;
			model.map.totalDes.field[cell.x][cell.y] = cell.totalDes;			
		}
		
	}

	private void updateInfrastructure() {
		for(Cell cell:model.map.canadaCells)
		{
			double diff = cell.population - cell.infrastructure;
			diff = diff>0?diff*model.infrastructureIncreaseRate:diff*model.infrastructureDecreaseRate;
			cell.infrastructure += diff;
		}
		
	}

	public int chooseLocation()
	{
		return PeopleSprinkler.chooseStochasticallyFromCumulativeProbs(this.cumulProb, this.totalProb, model.random);
	}

	public void updateCumulativeProbs()
	{
		double [] cumulProbs = new double[scores.length];
		this.totalProb = PeopleSprinkler.calcCumulativeProbs(scores, cumulProbs);
		this.cumulProb = cumulProbs;
	}
	
	private double calcNaturalScore(int x, int y) {
		Double des = model.map.totalDes.field[x][y];

		if (Double.isNaN(des))
			System.out.format("Error: totalDes[%d][%d] is NaN.\n", x, y);

		// normalize it between zero and one
		return (des - desMin) / desRange;
	}

	/**
	 * this method could be very expensive
	 */
	private void distributeSocialWeight()
	{
		for(int i = 0;i<model.households.size();++i)
		{
			Household h = model.households.get(i);
			
			int index = -1;
			if(model.map.indexMap.containsKey(h.currentCell))
				index = model.map.indexMap.get(h.currentCell);
			
			scores[index] += model.getSocialWeight();
			// add discounted socialWeight to adjacent cells
			if (model.getIncreaseAdjacentCells()) 
			{
				double adjacentWeight = model.getSocialWeight() * model.getAdjacentSocialDiscount();
				PeopleSprinkler.increaseAdjacentScores(scores, adjacentWeight, h.currentCell, model.map);
			}
		}
	}

}
