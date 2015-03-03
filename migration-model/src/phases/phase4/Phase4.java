package phases.phase4;




import java.util.ArrayList;

import codecLib.mpa.MPADException;
import sim.engine.MakesSimState;
import sim.engine.SimState;
import sim.field.grid.DoubleGrid2D;
import environment.Cell;
import environment.Map;
import phases.BasePhase;
import phases.Utility;
import migration.parameters.*;

/**
 * @author Ermo Wei
 *
 */

public class Phase4 extends BasePhase
{
	

	protected final static double COEFF_LOWER_BOUND = -10.0;
	protected final static double COEFF_UPPER_BOUND = 10.0;

	
	public Inspector inspector;
	public WorldAgent worldAgent;
	public int[] selectionBuffer = new int[2];
	public double[] selectionProb = new double[]{0.75,0.25};
	public boolean writeFile = false;
	public boolean recordData = false;
	
	//public World worldAgent; // for doing clean up stuff when all the agents have been scheduled
	
	// This grid is only for the visualization of the population in each cell
	public DoubleGrid2D ruralResidenceGrid = new DoubleGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT,Double.NEGATIVE_INFINITY);
	public DoubleGrid2D urbanResidenceGrid = new DoubleGrid2D(Map.GRID_WIDTH,Map.GRID_HEIGHT,Double.NEGATIVE_INFINITY);
	public DoubleGrid2D ruralDesirabilityGrid = new DoubleGrid2D(Map.GRID_WIDTH,Map.GRID_HEIGHT,Double.NEGATIVE_INFINITY);
	public DoubleGrid2D urbanDesirabilityGrid = new DoubleGrid2D(Map.GRID_WIDTH,Map.GRID_HEIGHT,Double.NEGATIVE_INFINITY);
	
	public DoubleGrid2D infrastructureGrid = new DoubleGrid2D(Map.GRID_WIDTH,Map.GRID_HEIGHT, Double.NEGATIVE_INFINITY);
	public DoubleGrid2D opportunityGrid = new DoubleGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT, 0);
	
	
	public DoubleGrid2D urbanSocialWeightGrid = new DoubleGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT);
	public DoubleGrid2D ruralSocialWeightGrid = new DoubleGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT);
	public DoubleGrid2D urbanAdjacentSocialWeightGrid = new DoubleGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT);
	public DoubleGrid2D ruralAdjacentSocialWeightGrid = new DoubleGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT);
	
	public DoubleGrid2D urbanSocialTermGrid = new DoubleGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT, Double.NEGATIVE_INFINITY);
	public DoubleGrid2D ruralSocialTermGrid = new DoubleGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT, Double.NEGATIVE_INFINITY);
	
	
	public DoubleGrid2D urbanRouletteWheelGrid = new DoubleGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT, 0);
	public DoubleGrid2D ruralRouletteWheelGrid = new DoubleGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT, 0);
	
	
	
	public double[] infrastructureDataBounds = new double[]{Double.MAX_VALUE,Double.MIN_VALUE};
	public double[] residenceDataBounds = new double[]{Double.MAX_VALUE,Double.MIN_VALUE};
	public double[] desDataBounds = new double[]{Double.MAX_VALUE,Double.MIN_VALUE};
	public double[] socialBounds = new double[]{Double.MAX_VALUE, Double.MIN_VALUE};
	public double[] rouletteBounds = new double[]{Double.MAX_VALUE, Double.MIN_VALUE};
	public double[] opportunityDataBounds = new double[]{Double.MAX_VALUE, Double.MIN_VALUE};
	
	
	// this three properites should not read from parameter file 
	public int urbanResidence = 0;
	public int ruralResidence = 0;
	public int urbanDensity = 100;
	
	public double getMeanTempAdjust() {
		return parameters.meanTempAdjust;
	}


	public void setMeanTempAdjust(double meanTempAdjust) {
		parameters.meanTempAdjust = meanTempAdjust;
	}

	public double getStdevTempAdjust() {
		return parameters.stdevTempAdjust;
	}


	public void setStdevTempAdjust(double stdevTempAdjust) {
		parameters.stdevTempAdjust = stdevTempAdjust;
	}
	
	public int getTempRunnningAvgWindow() {
		return parameters.tempRunnningAvgWindow;
	}


	public void setTempRunnningAvgWindow(int tempRunnningAvgWindow) {
		parameters.tempRunnningAvgWindow = tempRunnningAvgWindow;
	}
	
	public int getUrbanDensity() {
		return urbanDensity;
	}


	public void setUrbanDensity(int UrbanDensity) {
		this.urbanDensity = UrbanDensity;
	}

	
	public int getDensityIncrement() {
		return parameters.densityIncrement;
	}

	public void setDensityIncrement(int densityIncrement) {
		parameters.densityIncrement = densityIncrement;
	}
	
	
	public int getDensityIncrementInterval() {
		return parameters.densityIncrementInterval;
	}

	public void setDensityIncrementInterval(int densityIncrementInterval) {
		parameters.densityIncrementInterval = densityIncrementInterval;
	}

	// Household is directly connect with cell, for a given coordination (x,y),
	// there is only one cell, please see the cellGrid in map
	public ArrayList<Household> households;
	public void addNewHousehold(Household h)
	{
		this.households.add(h);
	}
	

	public int getRuralResidence() {
		return ruralResidence;
	}

	
	public int getUrbanResidence() {
		return urbanResidence;
	}

	
	public int getTotalPopulation()
	{
		return this.urbanResidence+this.ruralResidence;
	}

	public int getRecalculationSkip() {
		return parameters.recalSkip;
	}
	
	public void setRecalculationSkip(int recalculationSkip) {
		parameters.recalSkip = recalculationSkip;
	}
	
	public double getInitSocialWeight() {
		return parameters.initSocialWeight;
	}

	public void setInitSocialWeight(double initSocialWeight) {
		parameters.initSocialWeight = initSocialWeight;
	}

	public double getInitSocialWeightSpread() {
		return parameters.initSocialWeightSpread;
	}

	public void setInitSocialWeightSpread(double initSocialWeightSpread) {
		parameters.initSocialWeightSpread = initSocialWeightSpread;
	}

	public double getInitDesirabilityExp() {
		return parameters.initDesirabilityExp;
	}

	public void setInitDesirabilityExp(double initDesirabilityExp) {
		parameters.initDesirabilityExp = initDesirabilityExp;
	}

	public int getInitRecalculationSkip() {
		return parameters.initRecalculationSkip;
	}

	public void setInitRecalculationSkip(int initRecalculationSkip) {
		parameters.initRecalculationSkip = initRecalculationSkip;
	}

	
	public int getHouseholdSize()
	{
		return parameters.householdSize;
	}
	public void setHouseholdSize(int val)
	{
		parameters.householdSize = val;
	}

	
	public double getInfrastructureDecreaseRate() {
		return parameters.infrastructureDecreaseRate;
	}
	public void setInfrastructureDecreaseRate(double infrastructureDecreaseRate) {
		parameters.infrastructureDecreaseRate = infrastructureDecreaseRate;
	}

	
	public double getInfrastructureIncreaseRate() {
		return parameters.infrastructureIncreaseRate;
	}
	public void setInfrastructureIncreaseRate(double infrastructureIncreaseRate) {
		parameters.infrastructureIncreaseRate = infrastructureIncreaseRate;
	}
	

	public boolean isRecordData() {
		return parameters.recordData;
	}


	public void setRecordData(boolean recordData) {
		parameters.recordData = recordData;
	}


	public double getInfrastructureDeviationRate()
	{
		return parameters.infrastructureDeviationRate;
	}
	public void setInfrastructureDeviationRate(double val)
	{
		parameters.infrastructureDeviationRate = val;
	}
	
	
	/**
	 * Constructs a new simulation.  Side-effect: sets the random seed of
	 * Parameters to equal seed.
	 *
	 * @param seed Random seed
	 * @param args Command-line arguments
	 */
	public Phase4(long seed)
	{
		this(seed,null);		
	}
	
	public Phase4(long seed, String[] args)
	{
		super(seed,args);
	}


	
	@Override
	public void start()
	{
		super.start();
		
		writeFile = false;
		
		if(parameters.recordData)
			writeFile = true;
			
		inspector = new Inspector(parameters.filePath);
		
		map.initializeTemperature();
		
		this.urbanDensity = parameters.initUrbanDensity;
		initializeTotalDesirability(parameters);
		initializeResident();
		initializeSocialWeight();
		initializeInfrastructure();
		initializeOpportunity();
		

		worldAgent = new WorldAgent(this);
		//addEvent();   // This was for demo, and isn't needed now.  Delete?
		
		
		scheduleAgents();
		
	}

	private void addEvent() {
//		ArrayList<Point> polygon = new ArrayList<Point>();
//		polygon.add(new Point(733,806));
//		polygon.add(new Point(733,818));
//		polygon.add(new Point(743,806));
//		polygon.add(new Point(743,818));
//		Event e1 = new Event(5, 35, polygon, Type.ADD, 1000, this.urbanDesirabilityGrid);
//		Event e2 = new Event(5, 35, polygon, Type.ADD, 1000, this.ruralDesirabilityGrid);
//		worldAgent.registerEvent(e1);
//		worldAgent.registerEvent(e2);
	}


	public void updateBounds(double[] bounds, double value)
	{
		if(value<bounds[0])
		{
			bounds[0] = value;
		}
		else if(value>bounds[1])
		{
			bounds[1] = value;
		}
	}


	private void initializeOpportunity() {
		updateOpportunityGrid();
	}
	
	
	
	public void updateInfrastructureGrid() {
		for(Cell cell:map.canadaCells)
		{
			this.infrastructureGrid.field[cell.x][cell.y] = cell.infrastructure;
			updateBounds(infrastructureDataBounds, cell.infrastructure);
		}
	}

	
	
	public void updateOpportunityGrid() {
		for(Cell cell:map.canadaCells)
		{
			double value = cell.infrastructure - cell.population;
			this.opportunityGrid.field[cell.x][cell.y] = value;
		}
		
		Utility.updateGridWithZscores(this.opportunityGrid, map.canadaCells);
		
		for(Cell cell:map.canadaCells)
		{
			double value = this.opportunityGrid.field[cell.x][cell.y];
			updateBounds(opportunityDataBounds, value);
		}
	}
	
	private void initializeInfrastructure() {
		for(Cell cell:map.canadaCells)
		{	
			cell.infrastructure = 0;
			if(cell.population!=0)
			{
				double sigma = cell.population *  parameters.infrastructureDeviationRate;
				cell.infrastructure = this.parameters.distributions.gaussianSample(cell.population, sigma);
			}
			infrastructureGrid.field[cell.x][cell.y] = cell.infrastructure;
			updateBounds(infrastructureDataBounds, cell.infrastructure);
		}	
	}

	private void initializeSocialWeight()
	{
		this.updateSocialWeight();
	}
	
	// this initialize the urban and rural socialWeightGrid and adjacentSocialWeightGrid
	public void updateSocialWeight()
	{
		DoubleGrid2D normalizedPopGrid = new DoubleGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT,0);
		// first, we reset the grid
		for (Cell cell : map.canadaCells) {
			urbanSocialWeightGrid.field[cell.x][cell.y] = 0;
			urbanAdjacentSocialWeightGrid.field[cell.x][cell.y] = 0;
			ruralSocialWeightGrid.field[cell.x][cell.y] = 0;
			ruralAdjacentSocialWeightGrid.field[cell.x][cell.y] = 0;
			normalizedPopGrid.field[cell.x][cell.y] = urbanResidenceGrid.field[cell.x][cell.y]
					+ ruralResidenceGrid.field[cell.x][cell.y];
		}

		Utility.updateGridWithZscores(normalizedPopGrid, map.canadaCells);
		
		
		for(Cell cell:map.canadaCells)
		{
			urbanSocialWeightGrid.field[cell.x][cell.y] = parameters.urbanSocialWeight * normalizedPopGrid.field[cell.x][cell.y];
			ruralSocialWeightGrid.field[cell.x][cell.y] = parameters.ruralSocialWeight * normalizedPopGrid.field[cell.x][cell.y];
			
			// then we start to deal with adjacent social weight
			if (parameters.urbanAdjacentSocialDiscount!=0) {
				double adSocialWeight = urbanSocialWeightGrid.field[cell.x][cell.y]
						* parameters.urbanAdjacentSocialDiscount;
				this.increaseAdjacentSocialWeight(cell, urbanAdjacentSocialWeightGrid, adSocialWeight);
			}
			
			if (parameters.ruralAdjacentSocialDiscount!=0) {
				double adSocialWeight = ruralSocialWeightGrid.field[cell.x][cell.y]
						* parameters.ruralAdjacentSocialDiscount;
				this.increaseAdjacentSocialWeight(cell, ruralAdjacentSocialWeightGrid, adSocialWeight);
			}
		}
		
	}
	

	private void increaseAdjacentSocialWeight(Cell cell, DoubleGrid2D grid, double adSocialWeight) {
		for (int y = cell.y - 1; y <= cell.y + 1; y++)
			for (int x = cell.x - 1; x <= cell.x + 1; x++)
				if (((y != cell.y) || (x != cell.x)) &&
					(x >= 0) && (x < Map.GRID_WIDTH) &&
					(y >= 0) && (y < Map.GRID_HEIGHT)) {
					Cell c = (Cell)map.cellGrid.field[x][y];
					if ((c != null))
					{
						grid.field[c.x][c.y] += adSocialWeight;
					}
				}
		
	}

	/**
	 * initialize the total desirability grid with different kind of agents, the implementation may change
	 * @param parameters
	 */
	public void initializeTotalDesirability(Parameters parameters)
	{
		map.updateTotalDesirability(parameters);
		updateDesirabilityMap(urbanDesirabilityGrid, parameters.urbanTempCoeff,
				parameters.urbanRiverCoeff, parameters.urbanPortCoeff,
				parameters.urbanElevCoeff);
		updateDesirabilityMap(ruralDesirabilityGrid, parameters.ruralTempCoeff,
				parameters.ruralRiverCoeff, parameters.ruralPortCoeff,
				parameters.ruralElevCoeff);		
		
	}
	

	
	public void updateDesirabilityMap(DoubleGrid2D grid,double tempCoeff, double riverCoeff, double portCoeff, double elevCoeff) {
		double totDes;
	    for (Cell c : map.canadaCells) {
	    	totDes = map.calculateTotalDesirability(c.x, c.y, tempCoeff, portCoeff, riverCoeff, elevCoeff);
	    	grid.field[c.x][c.y] = totDes;
	    	updateBounds(desDataBounds, totDes);
	    }
	}

	public void scheduleAgents()
	{
		int householdOrdering = 2;
		for(Household h:households)
		{
			schedule.scheduleRepeating(h,householdOrdering,1.0);

		}
		schedule.scheduleRepeating(0,6,worldAgent);
	}
	
	
	public void initializeResident()
	{
		households = new ArrayList<Household>();				
		int numberOfPeople = 10;
		
		new PeopleSprinkler().initializeResidence(numberOfPeople, map,
				parameters.initSocialWeight, parameters.initSocialWeightSpread,
				parameters.initDesirabilityExp, parameters.householdSize, random);
	
//		map.initializeResidence(householdSize, map.totalDes,
//				initSocialWeight, initSocialWeightSpread, initDesirabilityExp, initRecalculationSkip);
		this.initializeHousehold();
		

	}
	
	/**
	 * 
	 */
	public void initializeHousehold()
	{
		int ruralSum = 0,urbanSum = 0;
		for(Cell cell:map.canadaCells)
		{
			// reset the grid
			urbanResidenceGrid.field[cell.x][cell.y] = 0;
			ruralResidenceGrid.field[cell.x][cell.y] = 0;
			
			// this is the urban agent
			if(cell.population >= this.urbanDensity)
			{
				for(int i = 0;i<cell.population;++i)
				{
					this.households.add(new Household(cell,parameters,0, this.random,this.parameters.distributions.lognormalSample(this.parameters.wealthMu,this.parameters.wealthSigma)));
				}
				this.urbanResidenceGrid.field[cell.x][cell.y] = cell.population;
				urbanSum += cell.population;
				cell.cellType = 0;
			}
			else if(cell.population < this.urbanDensity)
			{
				for(int i = 0;i<cell.population;++i)
				{
					this.households.add(new Household(cell,parameters,1, this.random, this.parameters.distributions.lognormalSample(this.parameters.wealthMu,this.parameters.wealthSigma)));
				}
				this.ruralResidenceGrid.field[cell.x][cell.y] = cell.population;
				ruralSum += cell.population;
				cell.cellType = 1;
			}
			
			updateBounds(residenceDataBounds, cell.population);
			
		}
		
		this.ruralResidence = ruralSum;
		this.urbanResidence = urbanSum;
	}

	public void updateResidenceGrid()
	{
		int ruralSum = 0,urbanSum = 0;
		for(Cell cell:map.canadaCells)
		{
			// reset the grid
			urbanResidenceGrid.field[cell.x][cell.y] = 0;
			ruralResidenceGrid.field[cell.x][cell.y] = 0;
			// this is the urban agent
			if(cell.population >= this.urbanDensity)
			{
				this.urbanResidenceGrid.field[cell.x][cell.y] = cell.population;
				urbanSum += cell.population;
				// record Data
				if(cell.cellType==1)
				{
					inspector.incrementCellToUrban();
				}
				cell.cellType = 0;
			}
			else if(cell.population < this.urbanDensity)
			{
				this.ruralResidenceGrid.field[cell.x][cell.y] = cell.population;
				ruralSum += cell.population;
				// record Data
				if(cell.cellType==0)
				{
					inspector.incrementCellToRural();
				}
				cell.cellType = 1;
			}
			updateBounds(residenceDataBounds, cell.population);
			
		}
		
		this.ruralResidence = ruralSum;
		this.urbanResidence = urbanSum;
	}
	


	public static void main(String[] args)
	{
		doLoop(new MakesSimState()
		{
			@Override
			public SimState newInstance(long seed, String[] args)
			{
				return new Phase4(seed);
			}

			@Override
			public Class simulationClass()
			{
				return Phase4.class;
			}
		}, args);

	}

	
}
