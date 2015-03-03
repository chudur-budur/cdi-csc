package migration;


import migration.parameters.Parameters;
import sim.engine.SimState;
import sim.engine.Steppable;
import environment.Cell;

public class Household implements Steppable
{
	Cell currentCell;
	double tempFactor, portFactor, elevFactor, waterFactor,socialFactor;
	public Household(Cell firstCell, Parameters params ) {
		currentCell = firstCell; 
		tempFactor = params.distributions.gaussianSample(0.5, 0.5);
		portFactor = params.distributions.gaussianSample(0.5, 0.5);
		waterFactor = params.distributions.gaussianSample(0.5, 0.5);
		elevFactor = params.distributions.gaussianSample(0.5, 0.5);
		socialFactor = params.distributions.gaussianSample(0.5, 0.5);
	}
	
	@Override
	public void step(SimState arg) {
		if(((Migration)arg).random.nextDouble()<0.1) {
			
			for(int i=0;i<currentCell.nearestCities.size();i++) {
				if(((Cell)currentCell.nearestCities.get(i)).totalDes > currentCell.totalDes) {
					move(currentCell,(Cell)currentCell.nearestCities.get(i));
				}
			}
		}
		
	}
	
	public void move (Cell from, Cell to) {
		from.removeHousehold(this);
		to.addHousehold(this);
		this.currentCell=to;
	}
	
    
}
