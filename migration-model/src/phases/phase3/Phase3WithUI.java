package phases.phase3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import org.spiderland.Psh.intStack;

import environment.Cell;
import environment.SmartColorMap;
import phases.BasePhaseWithUI;
import phases.phase2.Phase2;
import phases.phase2.Phase2WithUI;
import phases.phase2.Phase2WithUI.MetricsProperties;
import sim.display.Console;
import sim.display.Controller;
import sim.field.grid.IntGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.SimpleInspector;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.inspector.TabbedInspector;
import sim.util.Double2D;
import sim.util.Interval;




public class Phase3WithUI extends BasePhaseWithUI
{
	// phaseModel is the same object of super.model
	public Phase3 phaseModel;
	private FastValueGridPortrayal2D residencePortrayal;
	private FastValueGridPortrayal2D infrastructurePortrayal;
	private FastValueGridPortrayal2D opportunityPortrayal;
	private double residencePortrayalExp = 0.63;
	private double infrastructurePortrayalExp = 0.63;
	private double opportunityPortrayalExp = 0.10;
	public Phase3WithUI()
	{
		super(new Phase3(System.currentTimeMillis()));
		phaseModel = (Phase3)state;
		super.model = phaseModel;
	}
	
	
	public Phase3WithUI(Phase3 model)
	{
		super(model);
	}
	

    public static Object getInfo() 
    {
    	return Phase3WithUI.class.getResource("Phase3.html");
    } 

    
    public static String getName() {
		return "Phase3";
	}

	public void start()
	{
		super.start();
		int[] data = this.getResidence();
		if (data != null)
			((SmartColorMap)residencePortrayal.getMap()).setBoundsMinMax(data, new Color(0,0,0,0), Color.blue);

		double[] data2 = this.getInfrastructure();
		if (data2 != null)
			((SmartColorMap)infrastructurePortrayal.getMap()).setBoundsMinMax(data2, new Color(0,0,0,0), Color.yellow);

		
		double[] data3 = this.getOpportunity();
		if (data3 != null)
			((SmartColorMap)opportunityPortrayal.getMap()).setBoundsMinMax(data3, new Color(0,0,0,0), Color.orange);

		
		display.reset();
		display.repaint();
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
	
	
	@Override
	protected void setupPortrayals()
	{
		super.setupPortrayals();
		residencePortrayal = this.getResidencePortrayal();
		infrastructurePortrayal = this.getInfrastructurePortrayal();
		opportunityPortrayal = this.getOpportunityPortrayal();
		display.attach(residencePortrayal, "Residence",true);
		display.attach(infrastructurePortrayal, "Infrastructure",false);
		display.attach(opportunityPortrayal, "Opportunity", false);

	}
	
	@Override
	public void init(Controller c)
	{
		super.init(c);
	}


	public static void main(String[] args)
	{
		Phase3WithUI ui = new Phase3WithUI();

		Console c = new Console(ui);
		c.setVisible(true);
	}
	
	public FastValueGridPortrayal2D getOpportunityPortrayal()
	{
		FastValueGridPortrayal2D portrayal = new FastValueGridPortrayal2D();
		portrayal.setField(phaseModel.opportunityGrid);
        
        SmartColorMap colorMap = new SmartColorMap(0, 200, new Color(0, 0, 0, 0), Color.orange) {
			@Override
			public double filterLevel(double level) {
				return Math.pow(level, opportunityPortrayalExp);
			}
        };    
        
        
        portrayal.setMap(colorMap);
        return portrayal;
	}
	
	
	public FastValueGridPortrayal2D getResidencePortrayal()
	{
		FastValueGridPortrayal2D portrayal = new FastValueGridPortrayal2D();
		portrayal.setField(phaseModel.residentGrid);
        
        SmartColorMap colorMap = new SmartColorMap(0, 200, new Color(0, 0, 0, 0), Color.blue) {
			@Override
			public double filterLevel(double level) {
				return Math.pow(level, residencePortrayalExp);
			}
        };    
        
        
        portrayal.setMap(colorMap);
        return portrayal;
	}
	
	
	public FastValueGridPortrayal2D getInfrastructurePortrayal()
	{
		FastValueGridPortrayal2D portrayal = new FastValueGridPortrayal2D();
		portrayal.setField(phaseModel.InfrastructureGrid);
        
        SmartColorMap colorMap = new SmartColorMap(0, 200, new Color(0, 0, 0, 0), Color.yellow) {
			@Override
			public double filterLevel(double level) {
				return Math.pow(level, infrastructurePortrayalExp);
			}
        };    
        
        
        portrayal.setMap(colorMap);
        return portrayal;
	}
	
	
	public int[] getResidence() {
		if ((model.map == null) || model.map.canadaCells.isEmpty())
			return null;
		
		int[] residence  = new int[model.map.canadaCells.size()];
		
		int index = 0;
		for (Cell cell : model.map.canadaCells) {
			residence[index++] = cell.population;	
		}
		
		return residence;
	}
	
	public double[] getInfrastructure() {
		if ((model.map == null) || model.map.canadaCells.isEmpty())
			return null;
		
		double[] infrastructure  = new double[model.map.canadaCells.size()];
		
		int index = 0;
		for (Cell cell : model.map.canadaCells) {
			infrastructure[index++] = cell.infrastructure;	
		}
		
		return infrastructure;
	}
	
	private double[] getOpportunity() {
		if ((model.map == null) || model.map.canadaCells.isEmpty())
			return null;
		
		double[] opportunity  = new double[model.map.canadaCells.size()];
		
		int index = 0;
		// may need to fix the bug in here, the value may go to minus
		for (Cell cell : model.map.canadaCells) {
			double value = cell.infrastructure - cell.population;
			if(value>0)
				opportunity[index] = value;
			else {
				opportunity[index] = 0;
			}
			index++;
		}
		
		return opportunity;
	}
	
	public class DisplayProperties extends BasePhaseWithUI.DisplayProperties {

		Phase3 model;
		Phase3WithUI modelUI;

		public DisplayProperties(BasePhaseWithUI modelUI) {
			super(modelUI);

			this.modelUI = (Phase3WithUI)modelUI;
			this.model = (Phase3)((Phase3WithUI)modelUI).phaseModel;
		}

		public double getResidencePortrayalExp() { return modelUI.residencePortrayalExp; }
		public void setResidencePortrayalExp(double val) { modelUI.residencePortrayalExp = val; }
		public Object domResidencePortrayalExp() { return new Interval(0.0, 1.0); }
		
		public double getInfrastructurePortrayalExp() { return modelUI.infrastructurePortrayalExp; }
		public void setInfrastructurePortrayalExp(double val) { modelUI.infrastructurePortrayalExp = val; }
		public Object domInfrastructurePortrayalExp() { return new Interval(0.0, 1.0); }
		
		public double getOpportunityPortrayalExp() { return modelUI.opportunityPortrayalExp; }
		public void setOpportunityPortrayalExp(double val) { modelUI.opportunityPortrayalExp = val; }
		public Object domOpportunityPortrayalExp() { return new Interval(0.0, 1.0); }
		
	}
	
	
	public class MetricsProperties {

		Phase3 model;
		Phase3WithUI modelUI;
		
		
		public MetricsProperties(Phase3WithUI modelUI) {
			this.modelUI = modelUI;
			this.model = modelUI.phaseModel;
		}
		
		public int[] getResidence() {
			
			return Phase3WithUI.this.getResidence();
		}


	}
	
}

