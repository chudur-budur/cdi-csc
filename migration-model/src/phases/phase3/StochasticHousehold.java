package phases.phase3;

import phases.Utility;
import phases.phase2.PeopleSprinkler;
import migration.parameters.Parameters;
import environment.Cell;
import environment.Map;

public class StochasticHousehold extends Household{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	
	public StochasticHousehold(Cell cell, Parameters parameters) {
		super(cell, parameters);
	}

	/**
	 * This method determine which new cell the household moving to
	 */
	@Override
	public Cell move() 
	{
		int indexOfCell = model.worldAgent.chooseLocation();
		Cell cell = model.map.canadaCells.get(indexOfCell);
		moveToCell(this.currentCell,cell);
		//System.out.println("Move from ("+ currentCell.x+","+currentCell.y + ") to ("+ cell.x + "," + cell.y + ")");
		return cell;
	}

	
	/**
	 * This change some of the environment of the cell.
	 */
	@Override
	public void impact(Cell previousCell, Cell newCell)
	{
		int indexNew = -1, indexOld = -1;
		
		if(model.map.indexMap.containsKey(previousCell))
			indexOld = model.map.indexMap.get(previousCell);
		
		if(model.map.indexMap.containsKey(currentCell))
			indexNew = model.map.indexMap.get(newCell);
		
		
		model.worldAgent.scores[indexOld] -= parameters.socialWeight;
		model.worldAgent.scores[indexNew] += parameters.socialWeight;
		//test code
//		if(model.worldAgent.scores[previousCell.indexInList] < 0)
//		{
//			System.err.println("score of previous cell is less than 0");
//		}
//		if(model.worldAgent.scores[newCell.indexInList] < 0)
//		{
//			System.err.println("score is new cell is less than 0");
//		}
		//test code end
		if (model.getIncreaseAdjacentCells()) 
		{
			double adjacentWeight = model.getSocialWeight() * model.getAdjacentSocialDiscount();
			PeopleSprinkler.increaseAdjacentScores(model.worldAgent.scores, -adjacentWeight, previousCell, model.map);
			PeopleSprinkler.increaseAdjacentScores(model.worldAgent.scores, adjacentWeight, newCell, model.map);
		}
	}
	

	
}
