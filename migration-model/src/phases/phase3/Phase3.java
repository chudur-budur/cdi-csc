package phases.phase3;


import java.util.ArrayList;

import sim.engine.MakesSimState;
import sim.engine.SimState;
import sim.field.grid.DoubleGrid2D;
import environment.Cell;
import environment.Map;
import phases.BasePhase;

public class Phase3 extends BasePhase
{
	public World worldAgent; // for doing clean up stuff when all the agents have been scheduled
	
	// This grid is only for the visualization of the population in each cell
	public DoubleGrid2D residentGrid = new DoubleGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT);
	public DoubleGrid2D InfrastructureGrid = new DoubleGrid2D(Map.GRID_WIDTH,Map.GRID_HEIGHT);
	public DoubleGrid2D opportunityGrid = new DoubleGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT);
	
	
	// Household is directly connect with cell, for a given coordination (x,y),
	// there is only one cell, please see the cellGrid in map
	public ArrayList<Household> households;
	public void addNewHousehold(Household h)
	{
		this.households.add(h);
	}
	
	public int totolPopoluation = 0;
	public int getTotalPopulation()
	{
		return this.totolPopoluation;
	}
	
	public double infrastructureCoeff = 0.01;
	//public double infrastructureCoeff = 0.00;
	
	
	public double getInfrastructureCoeff() {
		return infrastructureCoeff;
	}
	public void setInfrastructureCoeff(double infrastructureCoeff) {
		this.infrastructureCoeff = infrastructureCoeff;
	}

	public double infrastructureDecreaseRate = 0.005;
	//public double infrastructureDecreaseRate = 0.000;
	public double getInfrastructureDecreaseRate() {
		return infrastructureDecreaseRate;
	}
	public void setInfrastructureDecreaseRate(double infrastructureDecreaseRate) {
		this.infrastructureDecreaseRate = infrastructureDecreaseRate;
	}

	public double infrastructureIncreaseRate = 0.01;
	//public double infrastructureIncreaseRate = 0.00;
	public double getInfrastructureIncreaseRate() {
		return infrastructureIncreaseRate;
	}
	public void setInfrastructureIncreaseRate(double infrastructureIncreaseRate) {
		this.infrastructureIncreaseRate = infrastructureIncreaseRate;
	}

	public int recalculationSkip = 1;
	public int initRecalculationSkip = 10;
	public double initSocialWeight = 10;
	public double initSocialWeightSpread = 0.5;
	public double initDesirabilityExp = 3.0;
	
	public int getInitRecalculationSkip() {
		return initRecalculationSkip;
	}
	public void setInitRecalculationSkip(int initRecalculationSkip) {
		this.initRecalculationSkip = initRecalculationSkip;
	}
	public double getInitSocialWeight() {
		return initSocialWeight;
	}
	public void setInitSocialWeight(double initSocialWeight) {
		this.initSocialWeight = initSocialWeight;
	}
	public double getInitSocialWeightSpread() {
		return initSocialWeightSpread;
	}
	public void setInitSocialWeightSpread(double initSocialWeightSpread) {
		this.initSocialWeightSpread = initSocialWeightSpread;
	}
	public double getInitDesirabilityExp() {
		return initDesirabilityExp;
	}
	public void setInitDesirabilityExp(double desirabilityExp) {
		this.initDesirabilityExp = desirabilityExp;
	}
	public int getRecalculationSkip() {
		return recalculationSkip;
	}
	public void setRecalculationSkip(int recalculationSkip) {
		this.recalculationSkip = recalculationSkip;
	}

	public double infrastructureDeviationRate = 0.1;
	public double getInfrastructureDeviationRate()
	{
		return this.infrastructureDeviationRate;
	}
	public void setInfrastructureDeviationRate(double val)
	{
		this.infrastructureDeviationRate = val;
	}
	
	
	public boolean increaseAdjacentCells = false;

	public boolean getIncreaseAdjacentCells() {
		return increaseAdjacentCells;
	}

	public void setIncreaseAdjacentCells(boolean val) {
		increaseAdjacentCells = val;
	}

	public double adjacentSocialDiscount = 0.5;

	public double getAdjacentSocialDiscount() {
		return adjacentSocialDiscount;
	}

	public void setAdjacentSocialDiscount(double val) {
		adjacentSocialDiscount = val;
	}
	
	private double growthRate = 0.016;
	public double getGrowthRate()
	{
		return this.growthRate;
	}
	public void setGrowthRate(double val)
	{
		this.growthRate = val;
	}
	
	private int householdSize = 4;
	public int getHouseholdSize()
	{
		return this.householdSize;
	}
	public void setHouseholdSize(int val)
	{
		this.householdSize = val;
	}

	private double movementWill = 0.02;

	public void setMovementWill(double val)
	{
		this.movementWill = val;
	}
	public double getMovementWill()
	{
		return this.movementWill;
	}
	
	public double getSocialWeight() 
	{
		return parameters.socialWeight;
	}
	public void setSocialWeight(double val) 
	{
		parameters.socialWeight = val;
	}
	
	public double getDesExp() {
		return parameters.desExp;
	}

	public void setDesExp(double val) {
		parameters.desExp = val;
	}
	
	/**
	 * Constructs a new simulation.  Side-effect: sets the random seed of
	 * Parameters to equal seed.
	 *
	 * @param seed Random seed
	 * @param args Command-line arguments
	 */
	public Phase3(long seed)
	{
		this(seed,null);		
	}
	
	public Phase3(long seed, String[] args)
	{
		super(seed,args);
	}


	
	@Override
	public void start()
	{
		super.start();
		//System.out.println("start method called");
		map.updateTotalDesirability(parameters);
		initializeResident();
		
		worldAgent = new World(this);
	
		scheduleAgents();
		
	}

	public void scheduleAgents()
	{
		int householdOrdering = 2;
		int lastOrdering = 100;
		for(Household h:households)
		{
			schedule.scheduleRepeating(h,householdOrdering,1.0);
		}
		
		schedule.scheduleRepeating(0,6,worldAgent);
	}
	
	

	
	public void initializeResident()
	{
		households = new ArrayList<Household>();
		map.initializeResidence(householdSize, map.totalDes,
				initSocialWeight, initSocialWeightSpread, initDesirabilityExp, initRecalculationSkip);
		this.initializeHousehold();
		this.initializeInfrastructure();
		this.initializeOpportunity();
		this.updateResidenceGrid();
		this.updateInfrastructureGrid();
		this.updateOpportunity();
	}
	
	
	public void updateOpportunity() {
		
		for (int i = 0; i < map.canadaCells.size(); ++i) {
			Cell cell = map.canadaCells.get(i);
			double value = cell.infrastructure - cell.population;
			if(value > 0)
				this.opportunityGrid.field[cell.x][cell.y] = value;
			else {
				this.opportunityGrid.field[cell.x][cell.y] = 0;
			}
		}
		
	}
	private void initializeOpportunity() {
		// do nothing this method, just for consistency
		
	}
	
	
	public void initializeHousehold()
	{
		for(Cell cell:map.canadaCells)
		{
			int population = cell.population;
			for(int i = 0;i<cell.population;++i)
			{
				this.households.add(new StochasticHousehold(cell,parameters));
			}
		}
	}
	
	
	/**
	 * This initialize the infrastructure of the grid using the Gaussian distribution
	 * The mean is the population of the cell
	 */
	public void initializeInfrastructure()
	{
		for(int i = 0;i<map.canadaCells.size();++i)
		{
			Cell cell = map.canadaCells.get(i);
			cell.infrastructure = 0;
			if(cell.population!=0)
			{
				double sigma = cell.population *  this.infrastructureDeviationRate;
				cell.infrastructure = this.parameters.distributions.gaussianSample(cell.population, sigma);
			}
		}
	}
	
	public void updateInfrastructureGrid()
	{
		for(int i = 0;i<map.canadaCells.size();++i)
		{
			Cell cell = map.canadaCells.get(i);
			this.InfrastructureGrid.field[cell.x][cell.y] = cell.infrastructure;
		}
	}
	
	public void updateResidenceGrid()
	{
		//System.out.println("update residence");
		int sum = 0;
		for(int i = 0;i<map.canadaCells.size();++i)
		{
			Cell cell = map.canadaCells.get(i);
			this.residentGrid.field[cell.x][cell.y] = cell.population;
			sum+=cell.population;
		}
		this.totolPopoluation = sum;
		//System.out.println("the new population is " + sum);
	}

	public static void main(String[] args)
	{
		doLoop(new MakesSimState()
		{
			@Override
			public SimState newInstance(long seed, String[] args)
			{
				return new Phase3(seed);
			}

			@Override
			public Class simulationClass()
			{
				return Phase3.class;
			}
		}, args);

	}
}