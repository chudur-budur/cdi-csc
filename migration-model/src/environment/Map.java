package environment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.spiderland.Psh.intStack;

import phases.ECInterface;
import phases.phase2.PeopleSprinkler;
import phases.phase4.Phase4;
import migration.Household;
import migration.parameters.*;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import ec.util.MersenneTwisterFast;


public class Map
{
    public static final int GRID_CELL_SIZE = 10;  // i.e. 10km or 100km square
    public static final int REGION_WIDTH = 10000;
    public static final int REGION_HEIGHT = REGION_WIDTH;
    public static final int GRID_WIDTH = 772;
    public static final int GRID_HEIGHT = 981;
    public static final int CANADA_CODE = 1;

    // TODO: I should get this from the file instead of hard-coding it.
    //       Same for GRID_WIDTH and GRID_HEIGHT.
    public static final int MISSING = -9999;
    private static final int PEOPLE_PER_BUILDING = 100; // 4;
    
    public static final double ARTIC_CIRCLE_LATITUDE = 66.5622;
	public static final double ARCTIC_CIRCLE_DIAMETER = 259.138959 * 2; // in grid space
	public static final Double2D NORTH_POLE = new Double2D(427, 481);	// determined by finding the cell with latitude closest to 90
	
	public static final double idealTemp = 273.15 + 20.0;  // XXX Should make this a parameter
    
    BufferedReader reader;
    StringTokenizer tokenizer;
    
    IntGrid2D nationGrid;

   

	public IntGrid2D popGrid;
	public ObjectGrid2D cellGrid;
	public ArrayList<Cell> canadaCells;
    IntGrid2D cultureGrid;
    IntGrid2D landCoverGrid;
    public DoubleGrid2D nppGrid;   // Net primary productivity, a bit like NDVI
    public DoubleGrid2D latGrid;   // Maybe these should move to a
    public DoubleGrid2D lonGrid;   // reprojection class
    public DoubleGrid2D coastalGrid;
    public IntGrid2D initPopRegionGrid;  // map the canada cell to region for population initialization
    //Parameters params;
    public Parameters parameters;
    MersenneTwisterFast random;  // This is just a pointer to the same random
                                 // number generator in the SimState class.
    //Ucar weatherIO;
    UcarSeasons weatherIO;
    public HashMap<Cell, Integer> indexMap;
    
    //migration portrayals
    public DoubleGrid2D tempDes,portDes,riverDes,elevDes;
    DoubleGrid2D tempRawToAdd, tempRawToSubtract, tempRawMovingAverage, tempTemp;
    DoubleGrid2D portRaw, riverRaw, elevRaw;
    public DoubleGrid2D totalDes;
    SparseGrid2D cities;
    
    private double[] tempParams;
    
    public MapPortrayals portrayals;
    

    public Map(Parameters params, MersenneTwisterFast random_){
        nationGrid = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT);
        popGrid = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT);
        cultureGrid = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT);
        landCoverGrid = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT);
        nppGrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        tempRawToAdd = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);  // The most recent temperatures
        tempRawToSubtract = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);  // Remove from moving average
        tempRawMovingAverage = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);  // Averaged temps over some period
        tempTemp = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);  // Used to temporarily store temperature data
        tempDes = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);  // derived from tempRawMovingAverage
        latGrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        lonGrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        portRaw = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        portDes = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        riverRaw = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        riverDes = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        elevRaw = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        elevDes = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        totalDes = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        cellGrid = new ObjectGrid2D(GRID_WIDTH, GRID_HEIGHT);
        coastalGrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        initPopRegionGrid = new IntGrid2D(GRID_WIDTH, GRID_HEIGHT);
        canadaCells = new ArrayList<Cell>(91777);	// how many Canadian cells there are
        
        cities = new SparseGrid2D(GRID_WIDTH, GRID_HEIGHT);
        
        totalDes = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        
        indexMap = new HashMap<Cell, Integer>();
        
        random = random_;
        this.parameters = params;
        
        portrayals = new MapPortrayals(this);

        try {
            populateGrid(parameters.nationsFilename, nationGrid);
            populateGrid(parameters.populationFilename, popGrid);
            populateGrid(parameters.latFilename, latGrid);
            populateGrid(parameters.lonFilename, lonGrid);
            
            //weatherIO = new Ucar(parameters.temperatureFilename, latGrid, lonGrid);
            weatherIO = new UcarSeasons(parameters.temperatureFilename, latGrid, lonGrid);
            calcInitialTempMovingAverage(parameters);

            populateGrid(parameters.portRawFile, portRaw);
            populateGrid(parameters.portDesFile, portDes);
            populateGrid(parameters.riverRawFile, riverRaw);
            populateGrid(parameters.riverDesFile, riverDes);
            populateGrid(parameters.elevRawFile, elevRaw);
            populateGrid(parameters.elevDesFile, elevDes);
            populateGrid(parameters.cultureGroupFile, cultureGrid);
            populateGrid(parameters.landCoverFile, landCoverGrid);
            populateGrid(parameters.nppFile, nppGrid);
            populateGrid(parameters.coastalFile, coastalGrid);
            populateGrid(parameters.popReigonFile, initPopRegionGrid);

        } catch (Exception e) {
            e.printStackTrace();
        }
        

        nationGrid.replaceAll(MISSING, 0);
        nationGrid.replaceAll(124, CANADA_CODE);  // Canada
        
        coastalGrid.replaceAll(MISSING, 0);
        
        remapCountryCodes(nationGrid);
        
        //compute the zscore for raw data
        zscoreValue(riverDes, riverRaw);
        zscoreValue(elevDes, elevRaw);
        zscoreValue(portDes, portRaw);
        calcDistanceFromIdeal(tempRawMovingAverage, tempTemp, idealTemp);
        tempParams = zscoreValue(tempDes, tempTemp);  // XXX Mean will be lost, so need to record the mean and sigma here
        
        tempDes.replaceAll(MISSING, Double.NEGATIVE_INFINITY);    // TODO: We should find a better way
        portDes.replaceAll(MISSING, Double.NEGATIVE_INFINITY);
        riverDes.replaceAll(MISSING, Double.NEGATIVE_INFINITY);
        elevDes.replaceAll(MISSING, Double.NEGATIVE_INFINITY);
        
        
        double tWeight = params.tempCoeff;
        double pWeight = params.portCoeff;
        double rWeight = params.riverCoeff;
        double eWeight = params.elevCoeff;
        
        int waterCount = 0;
        
        for(int y = 0; y < GRID_HEIGHT; y++) {
        	for(int x = 0; x < GRID_WIDTH; x++) {
            	if (nationGrid.get(x, y)==CANADA_CODE) {
            		// exclude cells with land cover of 0 (water), unless people really live there
                    if ((nppGrid.field[x][y] < 0) && (popGrid.field[x][y] <= 0)) {
                    	waterCount++;
                    	continue;
                    }
            		double totDes = calculateTotalDesirability(x, y, tWeight, pWeight, rWeight, eWeight);
                    if (Double.isInfinite(totDes) || Double.isNaN(totDes))
                    	continue;
                    totalDes.set(x, y, totDes);
                    Cell c = new Cell(x, y, nationGrid.get(x, y), tempDes.get(x, y), portDes.get(x, y), elevDes.get(x, y), riverDes.get(x, y), totDes, popGrid.get(x, y));
            		cellGrid.set(x, y, c);
            		canadaCells.add(c);
            		this.indexMap.put(c,canadaCells.size()-1);
        		}
            	else {
                    totalDes.set(x, y, Double.NEGATIVE_INFINITY);
            	}
        	}
        }

        System.out.format("CanadaCells: %d, water cells: %d\n", canadaCells.size(), waterCount);
//      findNearestPointToLatitude(90.0); // north pole
//      findNearestPointToLatitude(ARTIC_CIRCLE_LATITUDE); 
    }

    
    public void calcDistanceFromIdeal(DoubleGrid2D srcGrid, DoubleGrid2D destGrid, double ideal)
    {
        //System.out.println("calcDistanceFromIdeal");
        if(destGrid != srcGrid)
            destGrid.setTo(srcGrid);
        
        int width = srcGrid.getWidth();
        int height = srcGrid.getHeight();
        double val = 0.0;
        
        for(int x = 0; x < width; x++)
            for(int y = 0; y < height; y++)
            {
                val = srcGrid.get(x, y);
                //System.out.println(val);
                val = -Math.abs(ideal - val);
                destGrid.set(x, y, val);
            }
    }
    
    
    public void initializeTemperature()
    {
    	try {
    		calcInitialTempMovingAverage(parameters);
    		calcDistanceFromIdeal(tempRawMovingAverage, tempTemp, idealTemp);
			tempParams = zscoreValue(tempDes, tempTemp);  // XXX Mean will be lost, so need to record the mean and sigma here
			tempDes.replaceAll(MISSING, Double.NEGATIVE_INFINITY);    // TODO: We should find a better way
    	} catch (Exception e) {
			e.printStackTrace();
		}  
    	
    }
    
    
    private double[] zscoreValue(DoubleGrid2D desGrid, DoubleGrid2D srcGrid) {
    	
    	double sum = 0.0;
    	double sumOfSquares = 0.0;
    	int counter = 0;
    	for(int y = 0; y < GRID_HEIGHT; y++) {
        	for(int x = 0; x < GRID_WIDTH; x++) {
            	if (nationGrid.get(x, y)==CANADA_CODE) {
            		double val = srcGrid.field[x][y];
                    sum += val;
                    sumOfSquares += val * val;
                    counter++;
            	}
            }
        }
    	
    	double mean = sum / counter;
        double sd = Math.sqrt(sumOfSquares/counter - mean * mean);
    
        double[] ret = new double[2];
        ret[0] = mean;
        ret[1] = sd;
        
        for(int y = 0; y < GRID_HEIGHT; y++) {
        	for(int x = 0; x < GRID_WIDTH; x++) {
            	if (nationGrid.get(x, y)==CANADA_CODE) {
            		desGrid.field[x][y] = (srcGrid.field[x][y] - mean) / sd;
            	}
            	else {
					desGrid.field[x][y] = Double.NEGATIVE_INFINITY;
				}
            }
        }
        return ret;
	}

	private Int2D findNearestPointToLatitude(double latitude) {
    	double minDistance = Double.MAX_VALUE;
    	int nearestX = -1, nearestY = -1;

        for(int y = 0; y < GRID_HEIGHT; y++) {
            for(int x = 0; x < GRID_WIDTH; x++) {
            	double distance = Math.abs(latGrid.field[x][y] - latitude);
            	if (distance < minDistance) {
            		minDistance = distance;
            		nearestX = x;
            		nearestY = y;
            	}
            }
        }

        double centerX = 427;
        double centerY = 481;
        Int2D point = new Int2D(nearestX, nearestY);
        double gridDistance = point.distance(centerX, centerY);
        
        System.out.format("Nearest point to latitude %f is %f at (%d,%d), at a grid distance of %f from the center (%.0f,%.0f)\n", 
        		latitude, latGrid.field[nearestX][nearestY], nearestX, nearestY,
        		gridDistance, centerX, centerY);
    	
    	
    	return new Int2D(nearestX, nearestY);
    }
    
    public int countMatchingCells(IntGrid2D grid, int value) {
    	int count = 0;
    	int h = grid.getHeight();
    	int w = grid.getWidth();
    	for (int y = 0; y < h; y++)
    		for (int x = 0; x < w; x++)
    			if (grid.field[x][y] == value)
    				count++;
    	return count;
    }
    
    public void updateTotalDesirability(Parameters params) {
        double totDes;
        double maxTotal = Double.MIN_VALUE;
        double minTotal = Double.MAX_VALUE;
    	for (Cell c : canadaCells) {
    		totDes = calculateTotalDesirability(c.x, c.y, params.tempCoeff, params.portCoeff, params.riverCoeff, params.elevCoeff);
    		totalDes.set(c.x, c.y, totDes);
    		c.totalDes = totDes;
    		
    		if(totDes > maxTotal)
    			maxTotal = totDes;
    		if(totDes < minTotal)
    			minTotal = totDes;
    	}
    	
    	System.out.println("the max total is "+maxTotal);
    	System.out.println("the min total is "+minTotal);
    }
    
    private double[] getCanadianSubset(DoubleGrid2D grid) {
		double[] data = new double[canadaCells.size()];
		int index = 0;
		for (Cell c : canadaCells)
			data[index++] = grid.field[c.x][c.y];
		
		return data;
    }
    
	public double[] getTotalDesData() {
		return getCanadianSubset(totalDes);
	}
	
	public double[] getTempDesData() {
		return getCanadianSubset(tempDes);
	}
	
	public double[] getRiverDesData() {
		return getCanadianSubset(riverDes);
	}
	
	public double[] getPortDesData() {
		return getCanadianSubset(portDes);
	}
	
	public double[] getElevDesData() {
		return getCanadianSubset(elevDes);
	}
	
	public int[] getPopGridData() {
		int[] data = new int[canadaCells.size()];
		int index = 0;
		for (Cell c : canadaCells)
			data[index++] = popGrid.field[c.x][c.y];
		
		return data;
	}
	
	public int[] getPopGridData(int minVal) {
		ArrayList<Integer> sizes = new ArrayList<Integer>();
		
		for (Cell cell : canadaCells) {
			if (cell.empPop > 0)
				if (cell.empPop >= minVal)
					sizes.add(popGrid.field[cell.x][cell.y]);
		}
		
		int[] a = new int[sizes.size()];
		for (int i = 0; i < a.length; i++)
			a[i] = sizes.get(i);
		
		return a;
	}
    
    public void initializeNativePopulation(ArrayList<Steppable> households) {
    	// preprocessing to create initial population
        
        HashMap<Integer, ArrayList<Cell>> cultureMap = new HashMap<Integer, ArrayList<Cell>>();

        ArrayList<Cell> list;
        for (Cell c : canadaCells) {
    		// group the cells of each cultural group
    		int code = cultureGrid.get(c.x, c.y);
    		if(code!=-9999) {
    			if(cultureMap.containsKey(code)) {
    				cultureMap.get(code).add(c);
    			}
    			else {
    				list = new ArrayList<Cell>();
    				list.add(c);
    				cultureMap.put(code, list);
    			}
    		}
        }
    		
        //sort cells in each cultural group based on totalDes
        for(ArrayList<Cell> cells:cultureMap.values()) {
        	Collections.sort(cells);
        }
        
        // populate map with initial population
        Cell cell;
        try {
			reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("files/data-files/can_culture_table_Index.txt")));
			reader.readLine();
			
			int regionCode, regionPop,numGroups;
			while(reader.ready()) {
				tokenizer= new StringTokenizer(reader.readLine(), ",");
				tokenizer.nextToken();
				regionCode = (int) Double.parseDouble(tokenizer.nextToken());
				regionPop = (int) Double.parseDouble(tokenizer.nextToken());
				numGroups = (int) Double.parseDouble(tokenizer.nextToken());
				if(regionPop ==0 || numGroups == 0)
					continue;
				regionPop/=4; // to get Households
				
				//distribute region Population among groups
				
				// first find the number in each group by dividing the sum randomly among the groups
				double [] partitions = new double[numGroups];
				partitions[numGroups-1] = 1.0;
				for(int i=0;i<numGroups-1;i++) {
					partitions[i] = random.nextDouble();
				}
				Arrays.sort(partitions);
				for(int  i= numGroups-1;i>0;i--) {
					partitions[i]-=partitions[i-1];
				}
				
				//get the cells in the region
				list = cultureMap.get(regionCode);
				boolean randomize = false;
				if(numGroups>list.size())
					randomize = true;
				
				for(int i = 0;i<numGroups;i++) {
					
					// place each group on a cell in the region biased by best totalDes
					if(randomize) {
						cell = list.get(random.nextInt(list.size()));
					}
					else {
						if(random.nextDouble()<0.8) {
							cell = list.remove(list.size()-1);
						}
						else {
							cell = list.remove(random.nextInt(list.size()));
						}
					}
					
					int numHousholds = (int)(0.5+partitions[i]*regionPop);
					cell.addHouseholds(numHousholds);
					for(int j=0;j<numHousholds;j++) {
						households.add(new Household(cell, parameters));
					}
					cities.setObjectLocation(cell, cell.x, cell.y);
				}
			}
			for(Object o:cities.getAllObjects()) {
				((Cell)o).findNearestCities(cities);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public IntGrid2D getPopulationGrid() { return popGrid; }
    public DoubleGrid2D getTempDes() { return tempDes ; }
    public DoubleGrid2D getElevDes() { return elevDes ; }
    public DoubleGrid2D getRiverDes() { return riverDes ; }
    public DoubleGrid2D getPortDes() { return portDes ; }
    
    /**
     * Function to calculate the total desirability of a cell based on the individual desirability 
     * scores for temperature, port-distance, river-distance, and elevation. This is currently 
     * calculated using a linear combination but could be calculated in a different functional form.
     * 
     * @param x x-coordinate
     * @param y y-coordinate
     * @param tWeight temperature weight
     * @param pWeight port-distance weight
     * @param rWeight river-distance weight
     * @param eWeight elevation weight
     * @return The total desirability at the given coordinates
     */
    public double calculateTotalDesirability(int x, int y, double tWeight, double pWeight, double rWeight, double eWeight) {
    	return  tWeight * tempDes.field[x][y] +
    			pWeight * portDes.field[x][y] +
    			rWeight * riverDes.field[x][y] +
    			eWeight * elevDes.field[x][y];
    }




    public void calcInitialTempMovingAverage(Parameters parameters)
    {
        final int movingAverageSize = parameters.tempRunnningAvgWindow;
        try
        {
            weatherIO.loadLayer(0);
            weatherIO.populateDoubleGrid2D(tempRawMovingAverage);
        
            for (int i = 1; i < movingAverageSize; i++)
            {
                weatherIO.loadLayer(i);
                weatherIO.populateDoubleGrid2D(tempRawToAdd);
                tempRawMovingAverage = tempRawMovingAverage.add(tempRawToAdd);
            }
            tempRawMovingAverage = tempRawMovingAverage.multiply(1.0/movingAverageSize);            
        }
        catch (Exception e)
        {
            Thread.dumpStack();
            System.exit(1);
        }
    }


    // Note, in the file the current step is really stepNum + movingAverageSize
    public void updateTemperatures(Phase4 model, int stepNum)
    {
    	System.out.println(stepNum);
        final int movingAverageSize = model.getTempRunnningAvgWindow();
        final int stepsPerYear = weatherIO.getStepsPerYear();
        try
        {
            // Load and prepare data from the current timestep
            weatherIO.loadLayer(stepNum + movingAverageSize);
            weatherIO.populateDoubleGrid2D(tempRawToAdd);
            tempRawToAdd = adjustVariance(tempRawToAdd, model.getStdevTempAdjust() * stepNum / stepsPerYear);
            tempRawToAdd = tempRawToAdd.add(model.getMeanTempAdjust() * stepNum / stepsPerYear);
            tempRawToAdd = tempRawToAdd.multiply(1.0/movingAverageSize);
            
            // Load and prepare data from a previous timestep (current - movingAverageSize)
            weatherIO.loadLayer(stepNum);
            weatherIO.populateDoubleGrid2D(tempRawToSubtract);
            tempRawToSubtract = adjustVariance(tempRawToSubtract, model.getStdevTempAdjust() * Math.max(stepNum - movingAverageSize, 0.0) / stepsPerYear);
            tempRawToSubtract = tempRawToSubtract.add(model.getMeanTempAdjust() * Math.max(stepNum - movingAverageSize, 0.0) / stepsPerYear);
            tempRawToSubtract = tempRawToSubtract.multiply(-1.0/movingAverageSize);

            // Add current data and subtract previous data (i.e. moving average)
            tempRawMovingAverage = tempRawMovingAverage.add(tempRawToAdd);
            tempRawMovingAverage = tempRawMovingAverage.add(tempRawToSubtract);

            // Standardize the data so that it can be combined with other desirability parameters
            calcDistanceFromIdeal(tempRawMovingAverage, tempTemp, idealTemp);
            updateWithMiuAndSigma(tempDes, tempTemp, tempParams);
            
        }
        catch (Exception e)
        {
            Thread.dumpStack();
            System.exit(1);
        }
    }


    private DoubleGrid2D adjustVariance(DoubleGrid2D srcGrid,
			double stdevTempAdjust) {
    	double sum = 0.0;
    	int counter = 0;
    	for(int y = 0; y < GRID_HEIGHT; y++) {
        	for(int x = 0; x < GRID_WIDTH; x++) {
            	if (nationGrid.get(x, y)==CANADA_CODE) {
            		double val = srcGrid.field[x][y];
                    sum += val;
                    counter++;
            	}
            }
        }
    	
    	double mean = sum / counter;
    
  
        for(int y = 0; y < GRID_HEIGHT; y++) {
        	for(int x = 0; x < GRID_WIDTH; x++) {
            	if (nationGrid.get(x, y)==CANADA_CODE) {
            		srcGrid.field[x][y] = (srcGrid.field[x][y] - mean) * stdevTempAdjust + mean;
            	}
            }
        }
        return srcGrid;
	}

	private void updateWithMiuAndSigma(DoubleGrid2D desGrid,
			DoubleGrid2D srcGrid, double[] params) {
    	//System.out.println("update temperature grid");
    	//System.out.println(params[0]);
    	//System.out.println(params[1]);
    	
    	double mean = params[0];
        double sd = params[1];
        
        for(int y = 0; y < GRID_HEIGHT; y++) {
        	for(int x = 0; x < GRID_WIDTH; x++) {
            	if (nationGrid.get(x, y)==CANADA_CODE) {
            		desGrid.field[x][y] = (srcGrid.field[x][y] - mean) / sd;
            	}
            	else {
					desGrid.field[x][y] = Double.NEGATIVE_INFINITY;
				}
            }
        }		
	}

	public DoubleGrid2D loadDoubleGrid2D(String filename)
    {
        DoubleGrid2D grid = null;

        try
        {
            FileInputStream fileIn = new FileInputStream(filename);
            // InputStream fileIn = getClass().getClassLoader().getResourceAsStream(filename);
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            grid = (DoubleGrid2D) objIn.readObject();
            objIn.close();
            fileIn.close();
        }
        catch(IOException i)
        {
            System.out.println("Error reading file: " + filename);
            //i.printStackTrace();
        }
        catch(ClassNotFoundException c)
        {
            System.out.println("Grid2D class not found");
            c.printStackTrace();
        }
      
        return grid;
    }

    /**
     * Reads and ArcGIS grid file full of integers
     */
	public void populateGrid(String filename, IntGrid2D grid) throws Exception {
		InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        InputStreamReader isr = new InputStreamReader(is);
        reader = new BufferedReader(isr);

        for(int i=0;i<6;i++)
            reader.readLine();

        for(int i=0;i<grid.getHeight();i++) {
            tokenizer = new StringTokenizer(reader.readLine());
            for(int j=0;j<grid.getWidth();j++) {
                grid.set(j, i, (int)Double.parseDouble(tokenizer.nextToken()));
            }
        }
    }

    /**
     * Reads and ArcGIS grid file full of doubles
     */
    public void populateGrid(String filename, DoubleGrid2D grid) throws Exception {
		InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
		
        InputStreamReader isr = new InputStreamReader(is);
        reader = new BufferedReader(isr);

        for(int i=0;i<6;i++)
            reader.readLine();

        for(int i=0;i<grid.getHeight();i++) {
            tokenizer = new StringTokenizer(reader.readLine());
            for(int j=0;j<grid.getWidth();j++) {
                grid.set(j, i, Double.parseDouble(tokenizer.nextToken()));
            }
        }
    }
    
    public void remapCountryCodes(IntGrid2D grid) {
        boolean [] bools = new boolean [900];   // Country codes go into the 800s
        for(int i=0;i<grid.getHeight();i++) {
            for(int j=0;j<grid.getWidth();j++) {
//                bools[grid.get(j, i)]= true;
                int val = grid.get(j, i);
                if (val < 0)
                {
                    System.out.println("j = " + j);
                    System.out.println("i = " + i);
                    System.out.println("val = " + val);
                }
                bools[val] = true;
            }
        }
        int count = 0;
        int [] numbers = new int[bools.length];
        for(int i=0;i<bools.length;i++) 
            if(bools[i]) {
                numbers[i] = count;
                count++;
            }
        
        for(int i=0;i<grid.getHeight();i++) {
            for(int j=0;j<grid.getWidth();j++) {
                grid.set(j,i,numbers[grid.get(j, i)]);
            }
        }
    }
      
    public IntGrid2D getNationGrid() {
		return nationGrid;
	}

    public Double2D getLonLat(int x, int y)
    {
        return new Double2D(lonGrid.get(x,y), latGrid.get(x,y));
    }
    
    public double getTemperature(int x, int y)
    {
        return tempRawMovingAverage.get(x,y);
    }
    
    public Bag getCities() {
    	return cities.getAllObjects();
    }

    
	
	public void initializeResidence(int householdSize, DoubleGrid2D desGrid,
			double socialWeight, double socialWeightSpread,
			double desirabilityExp, int recalculationSkip) {
		// preprocessing to create initial population

		HashMap<Integer, ArrayList<Cell>> popMap = new HashMap<Integer, ArrayList<Cell>>();

		// divide canadaCell into different region for people sprinkle
		ArrayList<Cell> list;
		for (Cell c : canadaCells) {
			// group the cells of each cultural group
			c.setHouseholds(0); // clear the population for initialization
			int code = this.initPopRegionGrid.get(c.x, c.y);
			if (code != -9999) {
				if (popMap.containsKey(code)) {
					popMap.get(code).add(c);
				} else {
					list = new ArrayList<Cell>();
					list.add(c);
					popMap.put(code, list);
				}
			}
		}

		IntGrid2D sprinkleGrid = new IntGrid2D(Map.GRID_WIDTH, Map.GRID_HEIGHT, 0);

		// populate map with initial population
		Cell cell;
		try {
			reader = new BufferedReader(new InputStreamReader(getClass()
					.getClassLoader().getResourceAsStream(
							parameters.initialzationPopulationFile)));
			reader.readLine();

			int regionCode, pop1911, regionHousehold;
			while (reader.ready()) {				
				tokenizer = new StringTokenizer(reader.readLine(), ",");
				
				// skip some field for population
				for(int i = 0;i<5;++i)
					tokenizer.nextToken();
				pop1911 = (int) Double.parseDouble(tokenizer.nextToken());
				
				// skip some field for region code
				String rc = null;
				while(tokenizer.hasMoreTokens())
				{
					rc = tokenizer.nextToken();
				}
				regionCode = (int) Double.parseDouble(rc);

				//System.out.println("initialization:" + regionCode + "," + pop_precontact + ","
				//		+ pop1800 + "," + pop1900);

				regionHousehold = pop1911 / householdSize; // to get Households

				// distribute Population among regions use sprinkle people
				ArrayList<Cell> cellOfInterest = popMap.get(regionCode);
				if(cellOfInterest!=null)
					PeopleSprinkler.sprinklePeople(regionHousehold, sprinkleGrid,
							this, cellOfInterest, desGrid, socialWeight,
							socialWeightSpread, desirabilityExp, recalculationSkip,
							false, random);
				if(cellOfInterest==null)
					System.out.println(regionCode + " is null");
				
				
				//System.out.println("next line");

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	
    
	
}


