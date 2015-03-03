package phases.phase4;




import java.util.List;

import ec.util.MersenneTwisterFast;
import environment.Cell;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.DoubleBag;
import migration.parameters.*;

public class Household implements Cloneable, Steppable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	MersenneTwisterFast rand;
	
	protected Cell currentCell;
	protected Parameters parameters;
	protected Phase4 model;
	protected Cell attachedCell;
	private double satisfaction;
	
	//peter
		//need to initialize the cosntants somehow
	protected double[] thresholds;
	protected int[] priorities; //1-elevDes, 2-infrastructure, 3-portDes, 4-riverDes, 5-socialDes, 6-tempDes
	protected List<Double> thresholdsUp;
	protected List<Double> thresholdsLow;
	private static final int NUM_LOCATIONS = 5; //???
	
	public int typeFlag = -1; // type of the agent: -1 for unknown
							  //                     0 for urban
	         				  //                     1 for rural

	public double wealth;     // Socioeconomic status
	public double SocCon;  // Social connectedness (a measure of one's support structure)

	public Household(Cell cell, Parameters parameters, int type, MersenneTwisterFast rand, double wealth)
	{
	    init(cell, null, parameters, type, rand, wealth);
	}
	
	//peter 
	public Household(Cell cell, Cell attachedCell, Parameters parameters, int type, MersenneTwisterFast rand, double wealth) //attached cell can be same as cell
	{
        init(cell, attachedCell, parameters, type, rand, wealth);
	}
	

    public void init(Cell cell, Cell attachedCell, Parameters parameters, int type, MersenneTwisterFast rand, double wealth)
    {
        this.currentCell = cell;
        this.parameters = parameters;
        this.typeFlag = type;
        this.attachedCell = attachedCell;
        this.rand = rand;
        this.wealth=wealth;
        this.SocCon = rand.nextDouble();
    }
    
	@Override
	public void step(SimState state) {
		
		this.model = (Phase4)state;
		
		// based on the current threshold of the cell, determine the type of the agents 
		this.updateType();
		this.updateSatisfaction();
		this.updateWealth();
		
		//decide(); --replaces the following if statement;
		
		// then we can move
		if(wantToMove())
		{
			// move using the roulette wheel
			Cell previousCell = this.currentCell;
			Cell newCell = move();
			impact(previousCell,newCell);
		}
		
		newBirth();
		
	}
	
	// for now, the satisfaction are compute only using the factors associate with cell
	// so we just grab it back
	private void updateSatisfaction() {
		double[] satisfactions = this.typeFlag==1?model.worldAgent.ruralSatisfaction:model.worldAgent.urbanSatisfaction;
		this.satisfaction = satisfactions[model.map.indexMap.get(currentCell)];
		
	}

	private void updateType() {
		int previousType = this.typeFlag;
		
		this.typeFlag = model.worldAgent.isUrban(this.currentCell)?0:1;
		
		// record data
		if(previousType==0&&this.typeFlag==0)
		{
			model.inspector.incrementUrbanToUrban();
		}
		else if(previousType==1&&this.typeFlag==0)
		{
			model.inspector.incrementRuralToUrban();
		}
		else if(previousType==0&&this.typeFlag==1)
		{
			model.inspector.incrementUrbanToRural();
		}
		else if(previousType==1&&this.typeFlag==1)
		{
			model.inspector.incrementRuralToRural();
		}
		
	}
	
	public void updateWealth() {
		
		this.wealth=this.wealth+this.wealth*(parameters.wealthAdjMu+parameters.wealthAdjSigma*rand.nextGaussian());
		
		
	}

	//peter
	public boolean isSatisfactory(Cell cell) {
		double[] attributes = {cell.elevDes, cell.infrastructure, cell.portDes, cell.riverDes, cell.socialDes, cell.tempDes};
		DoubleBag cellAttributes = new DoubleBag(attributes);
		for (int i=0; i<this.priorities.length; i++) {
			if (this.thresholds[this.priorities[i]]<cellAttributes.get(this.priorities[i])) {
				return false;
			}
		}
		
		return true;
	}
	
	public void decide() {
		
		boolean currentOK = isSatisfactory(currentCell);
		boolean attachedOK = isSatisfactory(attachedCell);
		
		if (currentCell!=attachedCell && attachedOK) {
			moveToCell(currentCell,attachedCell);
			//status =?
		}
		
		else if  (currentOK)  {
			return;
			//status = loyalty;
		}
		else {
			for (int i=0; i<NUM_LOCATIONS; i++) {
				int indexOfCell = model.worldAgent.chooseLocation(this.typeFlag); //is this using the roulette wheel?
				Cell cell = model.map.canadaCells.get(indexOfCell);
				if (isSatisfactory(cell)) {
					moveToCell(currentCell, cell);
					//status=exit;
					return;
				}
			}
			//status=voice;
		}
	}
	
	
	/**
	 * this method is responsible for this agent to move the another cell
	 * @return Cell the cell this agent is going to move into
	 */
	public Cell move()
	{
		for(int i = 0;i<model.selectionBuffer.length;++i)
		{
			model.selectionBuffer[i] = model.worldAgent.chooseLocation(this.typeFlag);
		}
		
		
		int indexOfCell = selectFromCandidates(model.selectionBuffer);
				
		Cell cell = model.map.canadaCells.get(indexOfCell);
		if (moveCost(cellDist(this.currentCell, cell)) < this.wealth ) {
		moveToCell(this.currentCell,cell);
		this.wealth-=moveCost(cellDist(this.currentCell, cell));
		return cell;
		}
		//System.out.println("Move from ("+ currentCell.x+","+currentCell.y + ") to ("+ cell.x + "," + cell.y + ")");
		else {
			return this.currentCell;
		}
	}
	
	private int selectFromCandidates(int[] selectionBuffer) {
		double value = Math.random();
		double sum = 0;
		for(int i = 0;i<model.selectionProb.length;++i)
		{
			sum += model.selectionProb[i];
			if(value <= sum)
			{
				return selectionBuffer[i];
			}
		}
		// this should never happen
		System.err.println("This should never happen");
		return -1;
	}
	
	private double cellDist(Cell cell1, Cell cell2) {
		
		int x1=cell1.x;
		int x2=cell2.x;
		int y1=cell1.y;
		int y2=cell2.y;
		
		double dist = Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
		return dist;

	}
	
	private double moveCost(double dist) {
		
		return parameters.moveCost*Math.log(dist);
	
	}

	/**
	 * what impact will this movement cause
	 */
	public void impact(Cell previousCell, Cell newCell)
	{
		int indexNew = -1, indexOld = -1;
		
		if(model.map.indexMap.containsKey(previousCell))
			indexOld = model.map.indexMap.get(previousCell);
		
		if(model.map.indexMap.containsKey(currentCell))
			indexNew = model.map.indexMap.get(newCell);
		
		
		//model.worldAgent.scores[indexOld] -= parameters.socialWeight;
		//model.worldAgent.scores[indexNew] += parameters.socialWeight;
		
		/*
		if (model.getIncreaseAdjacentCells()) 
		{
			double adjacentWeight = model.getSocialWeight() * model.getAdjacentSocialDiscount();
			PeopleSprinkler.increaseAdjacentScores(model.worldAgent.scores, -adjacentWeight, previousCell, model.map);
			PeopleSprinkler.increaseAdjacentScores(model.worldAgent.scores, adjacentWeight, newCell, model.map);
		}
		*/
	}
	
	protected double getSatisfaction(int index)
	{
		return AgentUtil.getSatisfaction(model, index, this.typeFlag);
		//return this.satisfaction;
	}
	
	protected double getMovementWill()
	{
		return AgentUtil.getMovementWill(model, this.typeFlag);
	}
	protected double getGrowthRate()
	{
		return AgentUtil.getGrowthRate(model, this.typeFlag);
	}
	protected double getSocialWeight()
	{
		return AgentUtil.getSocialWeight(model, this.typeFlag);
	}
	protected double getAdjacentSocialDiscount()
	{
		return AgentUtil.getAdjacentSocialDiscount(model, this.typeFlag);
	}
	
	private boolean wantToMove()
	{
		double prob = getMovementWill();
		
		// get satisfaction
		int index = model.map.indexMap.get(currentCell);
		Double sat = new Double(this.getSatisfaction(index));
        assert(sat >= 0.0 && sat <= 1.0);
        assert(!sat.isNaN());
        assert(!sat.isInfinite(sat));
//        assert(!Float.isNaN(1.0/0.0));
//        assert(!Float.isInfinite(1.0/0.0));
//        System.out.println(sat);
		prob = prob * (1 - sat);
		
		boolean wantMove = model.random.nextDouble()<prob;
//		System.out.println(wantMove);
		return wantMove;
	}

	protected void moveToCell(Cell from, Cell to)
	{
		from.removeHousehold();
		to.addHousehold();
		this.currentCell=to;
	}
	
	
	
	private void newBirth()
	{
		if(model.random.nextDouble()<getGrowthRate())
		{
			this.wealth=this.wealth*(this.parameters.wealthLossToBirthMu+this.rand.nextDouble()*this.parameters.wealthLossToBirthSigma);
			Household newHousehold = newHousehold();
			model.addNewHousehold(newHousehold);
			currentCell.addHousehold();

			model.schedule.scheduleRepeating(newHousehold,2,1.0);
		}
	}

	private Household newHousehold() {


		Household newHousehold = new Household(this.currentCell, parameters, this.typeFlag, this.rand, this.wealth*(this.parameters.wealthAtBirthMu+this.rand.nextDouble()*this.parameters.wealthAtBirthSigma));
		
		return newHousehold;
		
		
	}
	
}
