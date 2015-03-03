package phases.phase2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import masoncsc.datawatcher.TimeSeriesDataStore;
import phases.BasePhaseWithUI;
import sim.display.Console;
import sim.display.Controller;
import sim.field.grid.IntGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.SimpleInspector;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.inspector.TabbedInspector;
import sim.util.Double2D;
import sim.util.Interval;
import sim.util.media.chart.TimeSeriesChartGenerator;
import environment.Cell;
import environment.SmartColorMap;

public class Phase2WithUI extends BasePhaseWithUI
{


	Phase2 model;
	double sprinklePortrayalExp = 0.2;

	FastValueGridPortrayal2D sprinkledPopPortrayal;

	public Phase2WithUI(String[] args)
	{
		super(new Phase2(System.currentTimeMillis(), args));
		model = (Phase2)state;
		super.model = model;
	}
	
	public static String getName() {
		return "Phase 2";
	}

	public Object getSimulationInspectedObject() {
		return state;
	}
	
	@Override
	public Inspector getInspector() {
        TabbedInspector i = new TabbedInspector();

        i.setVolatile(true);
        i.addInspector(new SimpleInspector(model, this), "Main");
        i.addInspector(new SimpleInspector(new DisplayProperties(this), this), "Display");

        i.addInspector(new SimpleInspector(new MetricsProperties(this), this), "Metrics");

        return i;
	}
	
	public double[] getSprinkledPopData() {

		double[] data = new double[model.map.canadaCells.size()];
		int index = 0;
		for (Cell c : model.map.canadaCells)
			data[index++] = model.sprinkleGrid.field[c.x][c.y];
		
		return data;
	}

	@Override
	public void start()
	{
		super.start();

		double[] data = getSprinkledPopData();
		if (data != null)
			((SmartColorMap)sprinkledPopPortrayal.getMap()).setBoundsMinMax(data, new Color(0,0,0,0), Color.blue);

	}

	/** Attach field portrayals to their respective grids. */
	protected void setupPortrayals()
	{
		super.setupPortrayals();
		
		sprinkledPopPortrayal = getSprinkledPopulationPortrayal();
		display.attach(sprinkledPopPortrayal, "Sprinkled Pop.", true);

	}

	@Override
	public void init(Controller c)
	{
		super.init(c);

	}
	
	public FastValueGridPortrayal2D getSprinkledPopulationPortrayal()
	{
		FastValueGridPortrayal2D portrayal = new FastValueGridPortrayal2D();
		portrayal.setField(model.sprinkleGrid);

        SmartColorMap colorMap = new SmartColorMap(0, 10, new Color(0, 0, 0, 0), Color.blue) {
			@Override
			public double filterLevel(double level) {
				return Math.pow(level, sprinklePortrayalExp);
			}
        };        
		
		portrayal.setMap(colorMap);
		return portrayal;
	}

	/** Create charts from combinations of the time series and histograms
	 * collected by DataCollector, and attach them to the specified controller. */
	private void setupCharts(Controller c)
	{
	}


	/**
	 * Create a chart from a single time series and register it with the
	 * controller.  The title and Y axis labels for the chart will be taken from
	 * TimeSeriesDataStore.getDescription().
	 *
	 * @param series The time series data.
	 * @param xLabel Label for the X axis.
	 * @param c Controller to register the chart with (so it can be opened by
	 * the user).
	 * @param updateInterval How frequently the chart needs refreshed.
	 */
	private void attachTimeSeriesToChart(TimeSeriesDataStore series,
	                                     String xLabel, Controller c, int updateInterval)
	{
		attachTimeSeriesToChart(new TimeSeriesDataStore[] {series},
		                        series.getDescription(),
		                        xLabel,
		                        series.getDescription(),
		                        c,
		                        updateInterval);
	}

	/**
	 * Create a chart from several time series and register it with the
	 * controller.
	 *
	 * @param seriesArray The time series data.
	 * @param title Label to display at the top of the chart.
	 * @param xLabel Label for the X axis.
	 * @param yLabel Label for the Y axis.
	 * @param c Controller to register the chart with (so it can be opened by
	 * the user).
	 * @param updateInterval How frequently the chart needs refreshed.
	 */
	private void attachTimeSeriesToChart(TimeSeriesDataStore[] seriesArray, String title,
	                                     String xLabel, String yLabel, Controller c,int updateInterval)
	{
		TimeSeriesChartGenerator chartGen = new TimeSeriesChartGenerator();
		chartGen.setTitle(title);
		chartGen.setXAxisLabel(xLabel);
		chartGen.setYAxisLabel(yLabel);
		for (TimeSeriesDataStore dw : seriesArray)
			chartGen.addSeries(dw.getData(), null);
		this.chartGenerators.add(chartGen);
		this.chartUpdateIntervals.add(updateInterval);

		JFrame frame = chartGen.createFrame();
		frame.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		frame.setTitle(title);
		frame.pack();
		c.registerFrame(frame);
	}

	/** Create a frame from the given chart and register it using the given parameters. */
	public void registerChartFrame(TimeSeriesChartGenerator chart, String title,
	                               String xLabel, String yLabel, Controller c, boolean visible)
	{
		chart.setTitle(title);
		chart.setXAxisLabel(xLabel);
		chart.setYAxisLabel(yLabel);
		JFrame frame = chart.createFrame();
		frame.pack();
		c.registerFrame(frame);
		frame.setVisible(visible);
	}


	@Override
	public void quit()
	{
		super.quit();

		if (displayFrame != null)
		{
			displayFrame.dispose();
		}
		displayFrame = null;
		display = null;
	}

	@Override
	public boolean step()
	{
		return super.step();
	}

	public static void main(String[] args)
	{
		Phase2WithUI phase2 = new Phase2WithUI(args);

		Console c = new Console(phase2);
		c.setVisible(true);
	}
	
	public class DisplayProperties extends BasePhaseWithUI.DisplayProperties {

		Phase2 model;
		Phase2WithUI modelUI;

		public DisplayProperties(BasePhaseWithUI modelUI) {
			super(modelUI);

			this.modelUI = (Phase2WithUI)modelUI;
			this.model = (Phase2)modelUI.model;
		}

		public double getSprinklePortrayalExp() { return modelUI.sprinklePortrayalExp; }
		public void setSprinklePortrayalExp(double val) { modelUI.sprinklePortrayalExp = val; }
		public Object domSprinklePortrayalExp() { return new Interval(0.0, 1.0); }
		
	}
	
	
	public class MetricsProperties {

		Phase2 model;
		Phase2WithUI modelUI;
		
		
		public MetricsProperties(Phase2WithUI modelUI) {
			this.modelUI = modelUI;
			this.model = modelUI.model;
		}
		
		public double[] getEmpiricalPop() {
			if (model.map == null)
				return null;
			
			IntGrid2D pGrid = model.map.getPopulationGrid();
			
			ArrayList<Integer> sizes = new ArrayList<Integer>();
			
			for (Cell cell : model.map.canadaCells) {
				if (cell.empPop > 0)
					if (!model.clipAtXMin || (cell.empPop > model.empiricalXMin))		// xMin
						sizes.add(pGrid.field[cell.x][cell.y]);
			}
			
			double[] a = new double[sizes.size()];
			for (int i = 0; i < a.length; i++)
				a[i] = sizes.get(i).doubleValue();
			
			return a;
		}
		
		public Double2D[] getEmpiricalPopRank() {
			double[] sizes = getEmpiricalPop();
			if (sizes == null)
				return null;
			
			Arrays.sort(sizes);	// sorts in ascending order
			Double2D[] rankSizes = new Double2D[sizes.length];
			for (int i = 0; i < sizes.length; i++) {
				int reversedRank = sizes.length-i-1;
				rankSizes[i] = new Double2D(reversedRank, sizes[i]);
			}
			
			return rankSizes;
		}

		public Double2D[] getEmpiricalPopVsDes() {
			if (model.map == null)
				return null;
			
			IntGrid2D pGrid = model.map.getPopulationGrid();
			
			ArrayList<Cell> populatedCells = new ArrayList<Cell>();
			for (Cell cell : model.map.canadaCells)
				if (pGrid.field[cell.x][cell.y] > 0)
					populatedCells.add(cell);
			
			Double2D[] results = new Double2D[populatedCells.size()];
			for (int i = 0; i < results.length; i++) {
				Cell cell = populatedCells.get(i);
				results[i] = new Double2D(cell.totalDes, pGrid.field[cell.x][cell.y]);
			}
			
			return results;
		}

		public Double2D[] getEmpiricalPopVsRiverDes() {
			if (model.map == null)
				return null;
			
			IntGrid2D pGrid = model.map.getPopulationGrid();
			
			ArrayList<Cell> populatedCells = new ArrayList<Cell>();
			for (Cell cell : model.map.canadaCells)
				if (pGrid.field[cell.x][cell.y] > 0)
					populatedCells.add(cell);
			
			Double2D[] results = new Double2D[populatedCells.size()];
			for (int i = 0; i < results.length; i++) {
				Cell cell = populatedCells.get(i);
				results[i] = new Double2D(cell.riverDes, pGrid.field[cell.x][cell.y]);
			}
			
			return results;
		}
		
		public double[] getSprinkledPop() {
			if (model.sprinkleGrid == null)
				return null;
			
			ArrayList<Integer> sizes = new ArrayList<Integer>();
			
			for (Cell cell : model.map.canadaCells) {
				if (model.sprinkleGrid.field[cell.x][cell.y] > 0)
					if (!model.clipAtXMin || (model.sprinkleGrid.field[cell.x][cell.y] > model.sprinkledXMin))
						sizes.add(model.sprinkleGrid.field[cell.x][cell.y]);
			}
			
			double[] a = new double[sizes.size()];
			for (int i = 0; i < a.length; i++)
				a[i] = sizes.get(i).doubleValue();
			
			return a;
		}
		
		
		public Double2D[] getSprinkledPopRank() {
			double[] sizes = getSprinkledPop();
			if (sizes == null)
				return null;
			
			Arrays.sort(sizes);	// sorts in ascending order
			Double2D[] rankSizes = new Double2D[sizes.length];
			for (int i = 0; i < sizes.length; i++) {
				int reversedRank = sizes.length-i-1;
				rankSizes[i] = new Double2D(reversedRank, sizes[i]);
			}
			
			return rankSizes;
		}
			
		public Double2D[] getSprinkledPopVsDes() {
			if (model.sprinkleGrid == null)
				return null;
			
			ArrayList<Cell> populatedCells = new ArrayList<Cell>();
			for (Cell cell : model.map.canadaCells)
				if (model.sprinkleGrid.field[cell.x][cell.y] > 0)
					populatedCells.add(cell);
			
			Double2D[] results = new Double2D[populatedCells.size()];
			for (int i = 0; i < results.length; i++) {
				Cell cell = populatedCells.get(i);
				results[i] = new Double2D(cell.totalDes, model.sprinkleGrid.field[cell.x][cell.y]);
			}
			
			return results;
		}
		
		public double[] getDesirabilities() {
			if ((model.map == null) || model.map.canadaCells.isEmpty())
				return null;
			
			double[] des  = new double[model.map.canadaCells.size()];
			
			int index = 0;
			for (Cell cell : model.map.canadaCells) {
//				des[index++] = cell.totalDes;	// the cells don't have the right values
				des[index++] = model.map.totalDes.field[cell.x][cell.y];
			}
			
			return des;
		}

		public boolean getClipAtXMin() { return model.clipAtXMin; }
		public void setClipAtXMin(boolean val) { model.clipAtXMin = val; }
		

	}
}


