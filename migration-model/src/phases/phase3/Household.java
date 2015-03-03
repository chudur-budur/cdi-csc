package phases.phase3;

import phases.phase2.PeopleSprinkler;
import environment.Cell;
import sim.engine.SimState;
import sim.engine.Steppable;
import migration.parameters.*;
public abstract class Household implements Cloneable, Steppable{

	protected Cell currentCell;
	protected Parameters parameters;
	protected Phase3 model;
	
	
	public Household(Cell cell, Parameters parameters)
	{
		this.currentCell = cell;
		this.parameters = parameters;
	}
	
	@Override
	public void step(SimState state) {
		
		this.model = (Phase3)state;
		
		if(wantToMove())
		{
			// move using the roulette wheel
			Cell previousCell = this.currentCell;
			Cell newCell = move();
			impact(previousCell,newCell);
		}
		
		newBirth();
		
	}
	
	/**
	 * this method is responsible for this agent to move the another cell
	 * @return Cell the cell this agent is going to move into
	 */
	public abstract Cell move();
	
	/**
	 * what impact will this movement cause
	 */
	public abstract void impact(Cell previousCell, Cell newCell);
	
	
	private boolean wantToMove()
	{
		double prob = model.getMovementWill();
		return model.random.nextDouble()<prob;
	}

	protected void moveToCell(Cell from, Cell to)
	{
		from.removeHousehold();
		to.addHousehold();
		this.currentCell=to;
	}
	
	private void newBirth()
	{
		if(model.random.nextDouble()<model.getGrowthRate())
		{
			Household newHousehold = new StochasticHousehold(currentCell,parameters);
			model.addNewHousehold(newHousehold);
			currentCell.addHousehold();
			
			int index = -1;
			if(model.map.indexMap.containsKey(currentCell))
				index = model.map.indexMap.get(currentCell);
			
			model.worldAgent.scores[index] += model.getSocialWeight();
			

			if (model.getIncreaseAdjacentCells()) 
			{
				double adjacentWeight = model.getSocialWeight() * model.getAdjacentSocialDiscount();
				PeopleSprinkler.increaseAdjacentScores(model.worldAgent.scores, adjacentWeight, currentCell, model.map);
			}
			
			
			
			model.schedule.scheduleRepeating(newHousehold,2,1.0);
		}
	}
	
}
