package migration;

import java.util.ArrayList;

import javax.swing.JFrame;

import masoncsc.datawatcher.TimeSeriesDataStore;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.util.media.chart.ChartGenerator;
import sim.util.media.chart.TimeSeriesChartGenerator;

//import com.sun.corba.se.spi.orb.DataCollector;

/**
 *
 * @see Migration
 * @see DataCollector
 */
public class MigrationWithUI extends GUIState
{
	// <editor-fold defaultstate="collapsed" desc="Fields">
	public Display2D display;
	public JFrame displayFrame;
	public Controller controller;
	private final static double INITIAL_WIDTH = 500;
	private final static double INITIAL_HEIGHT = 500;

	private ArrayList<ChartGenerator> chartGenerators;
	private ArrayList<Integer>chartUpdateIntervals;

	static int stepNum = 0;  // Kludge!  Use the scheduler.
	static int monthNum = 0;

	/**private FieldPortrayal2D terrainPortrayal;
	private FieldPortrayal2D provincePortrayal;
	private FieldPortrayal2D resourcePortrayal;*/
	//</editor-fold>

	public MigrationWithUI()
	{
		super(new Migration(System.currentTimeMillis()));
		this.chartGenerators = new ArrayList<ChartGenerator>();
		this.chartUpdateIntervals = new ArrayList<Integer>();
	}


	@Override
	public void start()
	{
		super.start();
		setupPortrayals();
		System.out.println("Initialization finished!");
	}

	/** Attach field portrayals to their respective grids. */
	private void setupPortrayals()
	{
		display.attach(((Migration) super.state).map.portrayals.getNationsPortrayal(),"Nations");
		display.attach(((Migration) super.state).map.portrayals.getPopulationPortrayal(),
				"Population",false);
		display.attach(((Migration) super.state).map.portrayals.getCitiesPortrayal(),"Cities");
		
		// individual desirability maps
		display.attach(((Migration) super.state).map.portrayals.getTempDesPortrayal(),
				"Temperature-Desirability",false);
		display.attach(((Migration) super.state).map.portrayals.getPortDesPortrayal(),
				"PortDist-Desirability",false);
		display.attach(((Migration) super.state).map.portrayals.getRiverDesPortrayal(),
				"RiverDist-Desirability",false);
		display.attach(((Migration) super.state).map.portrayals.getElevDesPortrayal(),
				"Elevation-Desirability",false);
		display.attach(((Migration) super.state).map.portrayals.getTotalDesPortrayal(),
				"Total-Desirability",false);
		
		// these are for EC stuffs
		display.attach(((Migration) super.state).dmaps.portrayals.getPopulationPortrayal(),
				"Raw-Population", true);
		display.attach(((Migration) super.state).dmaps.portrayals.getSmoothedPopulationPortrayal(),
				"Smoothed-Population", false);
		display.attach(((Migration) super.state).dmaps.portrayals.getNormalizedPopulationPortrayal(),
				"Normalized-Population", true);
		display.attach(((Migration) super.state).dmaps.portrayals.getRiverDesPortrayal(true),
				"River-Desirability-Clamped", false);
		display.attach(((Migration) super.state).dmaps.portrayals.getPortDesPortrayal(true),
				"Port-Desirability-Clamped", false);
		display.attach(((Migration) super.state).dmaps.portrayals.getTotalDesMeanPortrayal(true),
				"Total-Desirability-Mean-Clamped", false);
		display.attach(((Migration) super.state).dmaps.portrayals.getTotalDesMedianPortrayal(true),
				"Total-Desirability-Median-Clamped", false);
		// and the bounding box
		display.attach(((Migration) super.state).dmaps.portrayals.getBoundingBoxPortrayal(),
				"Bounding-box", false);

		display.reset();
		display.repaint();
	}

	@Override
	public void init(Controller c)
	{
		super.init(c);
		controller = c;

		display = new Display2D(INITIAL_WIDTH, INITIAL_HEIGHT, this);
		displayFrame = display.createFrame();
		c.registerFrame(displayFrame);
		displayFrame.setVisible(true);
		setupCharts(c);

		stepNum = 0;  // kludge! Use the scheduler
		monthNum = 0;

	}

	/** Create charts from combinations of the time series and histograms
	 * collected by DataCollector, and attach them to the specified controller. */
	private void setupCharts(Controller c)
	{
	}

	public void updateCharts(long currentStep)
	{
		int i=0;
		for (ChartGenerator cg : this.chartGenerators)
		{
			if(currentStep % chartUpdateIntervals.get(i)==0)
				cg.updateChartLater(currentStep);
			i++;
		}
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

		if (stepNum % 30 == 0)
		{
			//((Migration) super.state).map.updateTemperatures(monthNum);
			monthNum++;
		}
		stepNum++;
	
		updateCharts(((Migration) super.state).schedule.getSteps());
		return super.step();
	}

	public static void main(String[] args)
	{
		MigrationWithUI migration = new MigrationWithUI();

		Console c = new Console(migration);
		c.setVisible(true);
		// Map map = new Map();
	}
}

