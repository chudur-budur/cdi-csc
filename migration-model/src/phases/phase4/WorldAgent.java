package phases.phase4;

import java.util.ArrayList;

import org.spiderland.Psh.intStack;
import org.springframework.ui.Model;
import org.springframework.ui.context.Theme;

import phases.Utility;
import phases.phase2.PeopleSprinkler;
import phases.phase3.Household;
import phases.phase3.Phase3;
import environment.Cell;
import environment.Map;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.util.Double2D;

public class WorldAgent implements Steppable {

	public Phase4 model;
	public double[] urbanScores, ruralScores, urbanSatisfaction, ruralSatisfaction;
	public double[] urbanNaturalScores, ruralNaturalScores, urbanSocialScores, ruralSocialScores;
	private int counter = 0;
	private double urbanTotalProb = 0;
	private double ruralTotalProb = 0;
	private double[] urbanCumulProb = null;
	private double[] ruralCumulProb = null;

	public ArrayList<Event> events = new ArrayList<Event>();
	
	
	public WorldAgent(Phase4 model)
	{
		this.model = model;
		counter = 0;
		urbanScores = new double[model.map.canadaCells.size()];
		ruralScores = new double[model.map.canadaCells.size()];
		
		urbanSatisfaction = new double[model.map.canadaCells.size()];
		ruralSatisfaction = new double[model.map.canadaCells.size()];
		
		urbanNaturalScores = new double[model.map.canadaCells.size()];
		ruralNaturalScores = new double[model.map.canadaCells.size()];
		
		urbanSocialScores = new double[model.map.canadaCells.size()];
		ruralSocialScores = new double[model.map.canadaCells.size()];
		
		
		this.updateNaturalTerms();
		this.updateSocialTerms();
		this.updateSatisfaction();
    	this.updateRouletteWheel();
		this.updateCumulativeProbs();
	}

	
	private void updateNaturalTerms()
	{
		Double2D minMax = this
				.calcDesirabilityBounds(model.urbanDesirabilityGrid);
		double urbanDesMin = minMax.x;
		double urbanDesMax = minMax.y;
		double urbanDesRange = urbanDesMax - urbanDesMin;
		//System.out.printf("Urban min: %f, max: %f, range: %f%n", urbanDesMin, urbanDesMax, urbanDesRange);

		minMax = this.calcDesirabilityBounds(model.ruralDesirabilityGrid);
		double ruralDesMin = minMax.x;
		double ruralDesMax = minMax.y;
        double ruralDesRange = ruralDesMax - ruralDesMin;
        //System.out.printf("Rural min: %f, max: %f, range: %f%n", ruralDesMin, ruralDesMax, ruralDesRange);

		int index = 0;
		for (Cell cell : model.map.canadaCells) {
			urbanNaturalScores[index] = calcNaturalScore(cell,
					model.urbanDesirabilityGrid, urbanDesMax, urbanDesMin,
					urbanDesRange);

			ruralNaturalScores[index] = calcNaturalScore(cell,
					model.ruralDesirabilityGrid, ruralDesMax, ruralDesMin,
					ruralDesRange);

			index++;
		}
	}

	private void updateSocialTerms()
	{
		double[] opportunity = new double[urbanSocialScores.length];
		int index = 0;
		for(Cell cell:model.map.canadaCells)
		{
			opportunity[index] = model.opportunityGrid.field[cell.x][cell.y];
			index++;
		}
		
		index = 0;
		for(Cell cell:model.map.canadaCells)
		{
			// update the array
			// first compute the value regarding opportunity
			urbanSocialScores[index] = model.parameters.urbanOpportunityCoeff * opportunity[index];
			ruralSocialScores[index] = model.parameters.ruralOpportunityCoeff * opportunity[index];
		
			// then add the social weight
			urbanSocialScores[index] += model.urbanSocialWeightGrid.field[cell.x][cell.y];
			urbanSocialScores[index] += model.urbanAdjacentSocialWeightGrid.field[cell.x][cell.y];
			
			ruralSocialScores[index] += model.ruralSocialWeightGrid.field[cell.x][cell.y];
			ruralSocialScores[index] += model.ruralAdjacentSocialWeightGrid.field[cell.x][cell.y];
			
			// update the grid
			model.urbanSocialTermGrid.field[cell.x][cell.y] = urbanSocialScores[index];
			model.ruralSocialTermGrid.field[cell.x][cell.y] = ruralSocialScores[index];
			
			model.updateBounds(model.socialBounds, urbanSocialScores[index]);
			model.updateBounds(model.socialBounds, ruralSocialScores[index]);
			
			index++;
		}
		
		// convert the value between 0 and 1
		Double2D minMax = this.calcDesirabilityBounds(urbanSocialScores);
		double urbanSocialMin = minMax.x;
		double urbanSocialMax = minMax.y;
		double urbanSocialRange = urbanSocialMax - urbanSocialMin;


		minMax = this.calcDesirabilityBounds(ruralSocialScores);
		double ruralSocialMin = minMax.x;
		double ruralSocialMax = minMax.y;
        double ruralSocialRange = ruralSocialMax - ruralSocialMin;
        
        System.out.println("rurualMax:"+ruralSocialMax+",ruralMin:"+ruralSocialMin);
        System.out.println("rural social range: "+ruralSocialRange);
        
        index = 0;
		for (Cell cell : model.map.canadaCells) {
			
			if(urbanSocialRange!=0)
				urbanSocialScores[index] = (urbanSocialScores[index]-urbanSocialMin)/urbanSocialRange;
			if(ruralSocialRange!=0)
				ruralSocialScores[index] = (ruralSocialScores[index]-ruralSocialMin)/ruralSocialRange;
			
			index++;
		}
		
		
	}
	
	
	public void updateSatisfaction()
	{
		// convert the value between 0 and 1
        int index = 0;
		for (Cell cell : model.map.canadaCells) {
			urbanSatisfaction[index] = urbanNaturalScores[index]+urbanSocialScores[index];
			ruralSatisfaction[index] = ruralNaturalScores[index]+ruralSocialScores[index];
			index++;
		}
		
		Double2D minMax = this.calcDesirabilityBounds(urbanSatisfaction);
		double urbanSatisfactionMin = minMax.x;
		double urbanSatisfactionMax = minMax.y;
		double urbanSatisfactionRange = urbanSatisfactionMax - urbanSatisfactionMin;


		minMax = this.calcDesirabilityBounds(ruralSatisfaction);
		double ruralSatisfactionMin = minMax.x;
		double ruralSatisfactionMax = minMax.y;
        double ruralSatisfactionRange = ruralSatisfactionMax - ruralSatisfactionMin;
        
        index = 0;
		for (Cell cell : model.map.canadaCells) {
			urbanSatisfaction[index] = (urbanSatisfaction[index]-urbanSatisfactionMin)/urbanSatisfactionRange;
			ruralSatisfaction[index] = (ruralSatisfaction[index]-ruralSatisfactionMin)/ruralSatisfactionRange;			
			index++;
		}
        
	}
	
	
	public void updateRouletteWheel() {
		
		int index = 0;
		for (Cell cell : model.map.canadaCells) {
			urbanScores[index] = Math.pow(urbanNaturalScores[index],model.parameters.urbanDesExp);
			ruralScores[index] = Math.pow(ruralNaturalScores[index],model.parameters.ruralDesExp);
			urbanScores[index] += Math.pow(urbanSocialScores[index],model.parameters.urbanSocialExp);
			ruralScores[index] += Math.pow(ruralSocialScores[index],model.parameters.ruralSocialExp);
			
			model.urbanRouletteWheelGrid.field[cell.x][cell.y] = urbanScores[index];
			model.ruralRouletteWheelGrid.field[cell.x][cell.y] = ruralScores[index];
			
			model.updateBounds(model.rouletteBounds, urbanScores[index]);
			model.updateBounds(model.rouletteBounds, ruralScores[index]);
			
			index++;
		}
		
	}

	

	private double calcNaturalScore(Cell cell, DoubleGrid2D grid, double desMax, double desMin, double desRange) {
		
		
		Double des = grid.field[cell.x][cell.y];
		
		// prevent arithmetic error here
		if(desRange==0)
			return des;
		
		if (Double.isNaN(des))
			System.out.format("Error: totalDes[%d][%d] is NaN.\n", cell.x, cell.y);

		// normalize it between zero and one
		return (des - desMin) / desRange;
	}
	
	
	public Double2D calcDesirabilityBounds(double[] scores)
	{
		// calculate bounds of desirability map
    	double min = Double.MAX_VALUE;
    	double max = Double.MIN_VALUE;
    	for(int i = 0;i<scores.length;++i)
    	{
    		double des = scores[i];
//    		if ((des == 0) || Double.isInfinite(des))
//				continue;
			if (des < min)
				min = des;
			if (des > max)
				max = des;
    	}
    	
    	return new Double2D(min, max);
	}
	
	public Double2D calcDesirabilityBounds(DoubleGrid2D grid) {
    	// calculate bounds of desirability map
    	double min = Double.MAX_VALUE;
    	double max = Double.MIN_VALUE;
    	for (Cell cell : model.map.canadaCells){
    			int x = cell.x, y = cell.y;
				double des = grid.field[x][y];
//				if ((des == 0) || Double.isInfinite(des))
//					continue;
				if (des < min)
					min = des;
				if (des > max)
					max = des;
			}
		
    	return new Double2D(min, max);
    }
	

	@Override
	public void step(SimState state) {
		
		// increase the counter
		counter++;
		
		// update the urbanDensityThreshold
		if((counter>0)&&(counter%model.parameters.densityIncrementInterval==0))
		{
			model.urbanDensity += model.parameters.densityIncrement;
		}
		
		eventProcess(counter);
		
		// update the residence grid
		model.updateResidenceGrid();
		model.updateSocialWeight();

		// update the infrastructure
		this.updateInfrastructure();
		
		
		model.map.updateTemperatures(model, counter);
		
	    model.updateDesirabilityMap(model.urbanDesirabilityGrid, model.parameters.urbanTempCoeff,
	                model.parameters.urbanRiverCoeff, model.parameters.urbanPortCoeff,
	                model.parameters.urbanElevCoeff);
	    model.updateDesirabilityMap(model.ruralDesirabilityGrid, model.parameters.ruralTempCoeff,
	                model.parameters.ruralRiverCoeff, model.parameters.ruralPortCoeff,
	                model.parameters.ruralElevCoeff);
	    
				
		model.updateInfrastructureGrid();
		model.updateOpportunityGrid();
		
		
		// update the roulette wheel
		if ((counter > 0) && (counter % model.parameters.recalSkip == 0)) {
			// System.out.println("update cumulative probs "+counter);
			this.updateNaturalTerms();
			this.updateSocialTerms();
			this.updateSatisfaction();
			this.updateRouletteWheel();
			this.updateCumulativeProbs();
		}
		
		
		
		if(model.writeFile)
		{
			model.inspector.setIteration(counter);
			model.inspector.setRuralPop(model.ruralResidence);
			model.inspector.setUrbanPop(model.urbanResidence);
			model.inspector.writeToFile();
		}
		
	
		
	}
	

	


	private void eventProcess(int time) {
		for(Event event:this.events)
		{
			event.doEvent(time, this);
		}
	}

	private void updateInfrastructure() {
		for(Cell cell:model.map.canadaCells)
		{
			double diff = cell.population - cell.infrastructure;
			diff = diff>0?diff*model.parameters.infrastructureIncreaseRate:diff*model.parameters.infrastructureDecreaseRate;
			cell.infrastructure += diff;
			// infrastructure can not be less than zero
			if(cell.infrastructure<0)
				cell.infrastructure = 0;
			
		}
		
	}
	
	


	public int chooseLocation(int agentType)
	{
		switch(agentType)
		{
		case 0:
			return PeopleSprinkler.chooseStochasticallyFromCumulativeProbs(this.urbanCumulProb, this.urbanTotalProb, model.random);
		case 1:
			return PeopleSprinkler.chooseStochasticallyFromCumulativeProbs(this.ruralCumulProb, this.ruralTotalProb, model.random);
		case 2:
			System.err.println("This should never happen");
			return -1;
		}
		return -1;
	}

	public void updateCumulativeProbs()
	{
		double[] cumulProbs = new double[urbanScores.length];
		this.urbanTotalProb = PeopleSprinkler.calcCumulativeProbs(urbanScores, cumulProbs);
		this.urbanCumulProb = cumulProbs;
		
		cumulProbs = new double[ruralScores.length];
		this.ruralTotalProb = PeopleSprinkler.calcCumulativeProbs(ruralScores, cumulProbs);
		this.ruralCumulProb = cumulProbs;
	}
	

	
	

	public boolean isUrban(Cell cell) {
		return cell.population >= model.urbanDensity;
	}

	public void registerEvent(Event event) {
		this.events.add(event);
		
	}
}
