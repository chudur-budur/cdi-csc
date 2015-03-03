package phases.phase4;

import java.awt.Color;

import environment.Cell;
import environment.SmartColorMap;
import environment.SmartDoubleColorMap;
import environment.SmartFastValueGridPortrayal2D;
import phases.BasePhaseWithUI;
import sim.display.Console;
import sim.display.Controller;
import sim.portrayal.Inspector;
import sim.portrayal.SimpleInspector;
import sim.portrayal.inspector.TabbedInspector;
import sim.util.Interval;




public class Phase4WithUI extends BasePhaseWithUI
{
	// phaseModel is the same object of super.model
	public Phase4 phaseModel;
	private SmartFastValueGridPortrayal2D urbanResidencePortrayal;
	private SmartFastValueGridPortrayal2D ruralResidencePortrayal;
	private SmartFastValueGridPortrayal2D urbanDesirabilityPortrayal;
	private SmartFastValueGridPortrayal2D ruralDesirabilityPortrayal;
	private SmartFastValueGridPortrayal2D urbanSocialTermPortrayal;
	private SmartFastValueGridPortrayal2D ruralSocialTermPortrayal;
	private SmartFastValueGridPortrayal2D urbanRouletteWheelPortrayal;
	private SmartFastValueGridPortrayal2D ruralRouletteWheelPortrayal;
	
	private SmartFastValueGridPortrayal2D infrastructurePortrayal;
	private SmartFastValueGridPortrayal2D opportunityPortrayal;

	
	
	private SmartColorMap infrastructurecolorMap = new SmartColorMap(0.25, new Color(0, 0, 0, 0), Color.yellow);;
	private SmartColorMap desColorMap = new SmartColorMap(2.5, new Color(0,0,0,0), Color.green);
	private SmartColorMap residenceColorMap = new SmartColorMap(0.144, new Color(0, 0, 0, 0), Color.blue);
	private SmartColorMap socialTermColorMap = new SmartColorMap(0.35, new Color(0,0,0,0), new Color(255,0,255));
	private SmartColorMap rouletteWheelColorMap = new SmartColorMap(0.15, new Color(0,0,0,0), new Color(200,255,0));
	
	// FIXME the scale factor is not working here, need to fix it
	private SmartDoubleColorMap opporColorMap = new SmartDoubleColorMap(0.10, Color.cyan, Color.orange);

	
	public Phase4WithUI()
	{
		super(new Phase4(System.currentTimeMillis()));
		phaseModel = (Phase4)state;
		super.model = phaseModel;
	}
	
	
	public Phase4WithUI(Phase4 model)
	{
		super(model);
	}
	

    public static Object getInfo() 
    {
    	return Phase4WithUI.class.getResource("Phase4.html");
    } 

    
    public static String getName() {
		return "Phase4";
	}

	public void start()
	{
		super.start();
		urbanResidencePortrayal.updateBounds(phaseModel.residenceDataBounds);
		ruralResidencePortrayal.updateBounds(phaseModel.residenceDataBounds);
		ruralDesirabilityPortrayal.updateBounds(phaseModel.desDataBounds);
		urbanDesirabilityPortrayal.updateBounds(phaseModel.desDataBounds);
		urbanSocialTermPortrayal.updateBounds(phaseModel.socialBounds);
		ruralSocialTermPortrayal.updateBounds(phaseModel.socialBounds);
		urbanRouletteWheelPortrayal.updateBounds(phaseModel.rouletteBounds);
		ruralRouletteWheelPortrayal.updateBounds(phaseModel.rouletteBounds);
		infrastructurePortrayal.updateBounds(phaseModel.infrastructureDataBounds);
		opportunityPortrayal.updateBounds(phaseModel.opportunityDataBounds);
		
		
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
        i.addInspector(new SimpleInspector(new UrbanProperties(this), this), "Urban");
        i.addInspector(new SimpleInspector(new RuralProperties(this), this), "Rural");
        
        return i;
	}
	
	
	@Override
	protected void setupPortrayals()
	{
		super.setupPortrayals();
		urbanResidencePortrayal = this.getUrbanResidencePortrayal();
		ruralResidencePortrayal = this.getRuralResidencePortrayal();
		urbanDesirabilityPortrayal = this.getUrbanDesirabilityPortrayl();
		ruralDesirabilityPortrayal = this.getRuralDesirabilityPortrayl();
		urbanSocialTermPortrayal = this.getUrbanSocialTermPortrayal();
		ruralSocialTermPortrayal = this.getRuralSocialTermPortrayal();
		urbanRouletteWheelPortrayal = this.getUrbanRouletteWheelPortrayal();
		ruralRouletteWheelPortrayal = this.getRuralRouletteWheelPortrayal();
		infrastructurePortrayal = this.getInfrastructurePortrayal();
		opportunityPortrayal = this.getOpportunityPortrayal();
		
		
		display.attach(urbanDesirabilityPortrayal, "Urban Total Des", false);
		display.attach(ruralDesirabilityPortrayal, "Rural Total Des", false);
		display.attach(urbanSocialTermPortrayal, "Urban Social Term", false);
		display.attach(ruralSocialTermPortrayal, "Rural Social Term", false);
		display.attach(urbanRouletteWheelPortrayal, "Urban Wheel", false);
		display.attach(ruralRouletteWheelPortrayal, "Rural Wheel", false);
		
		display.attach(urbanResidencePortrayal, "Urban People",true);
		display.attach(ruralResidencePortrayal, "Rural People",true);
		
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
		Phase4WithUI ui = new Phase4WithUI();

		Console c = new Console(ui);
		c.setVisible(true);
	}
	
	public SmartFastValueGridPortrayal2D getOpportunityPortrayal()
	{
		SmartFastValueGridPortrayal2D portrayal = new SmartFastValueGridPortrayal2D();
		portrayal.setField(phaseModel.opportunityGrid);
        portrayal.setMap(opporColorMap);
        return portrayal;
	}
	
	
	
	
	public SmartFastValueGridPortrayal2D getInfrastructurePortrayal()
	{
		SmartFastValueGridPortrayal2D portrayal = new SmartFastValueGridPortrayal2D();
		portrayal.setField(phaseModel.infrastructureGrid);
        portrayal.setMap(infrastructurecolorMap);
        return portrayal;
	}
	
	
	private SmartFastValueGridPortrayal2D getRuralRouletteWheelPortrayal() {
		SmartFastValueGridPortrayal2D portrayal = new SmartFastValueGridPortrayal2D();
		portrayal.setField(phaseModel.ruralRouletteWheelGrid);
        portrayal.setMap(rouletteWheelColorMap);
        return portrayal;
	}


	private SmartFastValueGridPortrayal2D getUrbanRouletteWheelPortrayal() {
		SmartFastValueGridPortrayal2D portrayal = new SmartFastValueGridPortrayal2D();
		portrayal.setField(phaseModel.urbanRouletteWheelGrid);
        portrayal.setMap(rouletteWheelColorMap);
        return portrayal;
	}


	private SmartFastValueGridPortrayal2D getRuralSocialTermPortrayal() {
		SmartFastValueGridPortrayal2D portrayal = new SmartFastValueGridPortrayal2D();
		portrayal.setField(phaseModel.ruralSocialTermGrid);
        portrayal.setMap(socialTermColorMap);
        return portrayal;
	}


	private SmartFastValueGridPortrayal2D getUrbanSocialTermPortrayal() {
		SmartFastValueGridPortrayal2D portrayal = new SmartFastValueGridPortrayal2D();
		portrayal.setField(phaseModel.urbanSocialTermGrid);
        portrayal.setMap(socialTermColorMap);
        return portrayal;
	}

	
	private SmartFastValueGridPortrayal2D getRuralDesirabilityPortrayl() {
		SmartFastValueGridPortrayal2D portrayal = new SmartFastValueGridPortrayal2D();
		portrayal.setField(phaseModel.ruralDesirabilityGrid);
        portrayal.setMap(desColorMap);
        return portrayal;
	}


	private SmartFastValueGridPortrayal2D getUrbanDesirabilityPortrayl() {
		SmartFastValueGridPortrayal2D portrayal = new SmartFastValueGridPortrayal2D();
		portrayal.setField(phaseModel.urbanDesirabilityGrid);
		portrayal.setMap(desColorMap);
        return portrayal;
	}
	
	
	
	public SmartFastValueGridPortrayal2D getUrbanResidencePortrayal()
	{
		SmartFastValueGridPortrayal2D portrayal = new SmartFastValueGridPortrayal2D();
		portrayal.setField(phaseModel.urbanResidenceGrid);
        portrayal.setMap(residenceColorMap);
        return portrayal;
	}
	
	public SmartFastValueGridPortrayal2D getRuralResidencePortrayal()
	{
		SmartFastValueGridPortrayal2D portrayal = new SmartFastValueGridPortrayal2D();
		portrayal.setField(phaseModel.ruralResidenceGrid);
        portrayal.setMap(residenceColorMap);
        return portrayal;
	}
	
	
	
	
	
	public int[] getResidence() {
		if ((model.map == null) || model.map.canadaCells.isEmpty())
			return null;
		
		int[] residence  = new int[model.map.canadaCells.size()];
		
		int index = 0;
		for (Cell cell : model.map.canadaCells) {
			int sum = (int) (phaseModel.ruralResidenceGrid.field[cell.x][cell.y]+phaseModel.urbanResidenceGrid.field[cell.x][cell.y]);
			residence[index++] = sum;
		}
		
		return residence;
	}
	
	
	
	public class DisplayProperties extends BasePhaseWithUI.DisplayProperties {

		Phase4 model;
		Phase4WithUI modelUI;

		public DisplayProperties(BasePhaseWithUI modelUI) {
			super(modelUI);

			this.modelUI = (Phase4WithUI)modelUI;
			this.model = (Phase4)((Phase4WithUI)modelUI).phaseModel;
		}

		public double getResidencePortrayalExp() { return modelUI.residenceColorMap.scaleFactor; }
		public void setResidencePortrayalExp(double val) { modelUI.residenceColorMap.scaleFactor = val; }
		public Object domResidencePortrayalExp() { return new Interval(0.0, 1.0); }
		
		
		public double getDesPortrayalExp() { return modelUI.desColorMap.scaleFactor; }
		public void setDesPortrayalExp(double val) { modelUI.desColorMap.scaleFactor = val; }
		public Object domDesPortrayalExp() { return new Interval(0.0, 10.0); }
		
		public double getInfrastructurePortrayalExp() { return modelUI.infrastructurecolorMap.scaleFactor; }
		public void setInfrastructurePortrayalExp(double val) { modelUI.infrastructurecolorMap.scaleFactor = val; }
		public Object domInfrastructurePortrayalExp() { return new Interval(0.0, 1.0); }
		
		public double getOpportunityPortrayalExp() { return modelUI.opporColorMap.scaleFactor; }
		public void setOpportunityPortrayalExp(double val) { modelUI.opporColorMap.scaleFactor = val; }
		public Object domOpportunityPortrayalExp() { return new Interval(0.0, 1.0); }
		
		public double getSocialTermExp() { return modelUI.socialTermColorMap.scaleFactor; }
		public void setSocialTermExp(double val) { modelUI.socialTermColorMap.scaleFactor = val; }
		public Object domSocialTermExp() { return new Interval(0.0, 1.0); }
		
		public double getRouletteWheelExp() { return modelUI.rouletteWheelColorMap.scaleFactor; }
		public void setRouletteWheelExp(double val) { modelUI.rouletteWheelColorMap.scaleFactor = val; }
		public Object domRouletteWheelExp() { return new Interval(0.0, 1.0); }
		
		
	}
	
	
	public class MetricsProperties {

		Phase4 model;
		Phase4WithUI modelUI;
		
		
		public MetricsProperties(Phase4WithUI modelUI) {
			this.modelUI = modelUI;
			this.model = modelUI.phaseModel;
		}
		
		public int[] getResidence() {
			
			return Phase4WithUI.this.getResidence();
		}


	}
	
	
	public class UrbanProperties {
		Phase4 model;
		Phase4WithUI modelUI;
		
		public UrbanProperties(Phase4WithUI modelUI) {
			this.modelUI = (Phase4WithUI)modelUI;
			this.model = (Phase4)((Phase4WithUI)modelUI).phaseModel;
		}
		
		public double getTempDesCoefficient() {
			return model.parameters.urbanTempCoeff;
		}

		public void setTempDesCoefficient(double val) {
			if(Phase4.COEFF_LOWER_BOUND<=val&&val<=Phase4.COEFF_UPPER_BOUND)
				model.parameters.urbanTempCoeff = val;	
		}

		public Object domTempDesCoefficient() {
			return new Interval(Phase4.COEFF_LOWER_BOUND, Phase4.COEFF_UPPER_BOUND);
		}


		public double getPortDesCoefficient() {
			return model.parameters.urbanPortCoeff;
		}

		public void setPortDesCoefficient(double val) {
			if(Phase4.COEFF_LOWER_BOUND<=val&&val<=Phase4.COEFF_UPPER_BOUND)
				model.parameters.urbanPortCoeff = val;
		}

		public Object domPortDesCoefficient() {
			return new Interval(Phase4.COEFF_LOWER_BOUND, Phase4.COEFF_UPPER_BOUND);
		}


		public double getRiverDesCoefficient() {
			return model.parameters.urbanRiverCoeff;
		}

		public void setRiverDesCoefficient(double val) {
			if(Phase4.COEFF_LOWER_BOUND<=val&&val<=Phase4.COEFF_UPPER_BOUND)
				model.parameters.urbanRiverCoeff = val;
		}

		public Object domRiverDesCoefficient() {
			return new Interval(Phase4.COEFF_LOWER_BOUND, Phase4.COEFF_UPPER_BOUND);
		}

		public double getElevDesCoefficient() {
			return model.parameters.urbanElevCoeff;
		}

		public void setElevDesCoefficient(double val) {
			if(Phase4.COEFF_LOWER_BOUND<=val&&val<=Phase4.COEFF_UPPER_BOUND)
				model.parameters.urbanElevCoeff = val;
		}

		public Object domElevDesCoefficient() {
			return new Interval(Phase4.COEFF_LOWER_BOUND, Phase4.COEFF_UPPER_BOUND);
		}
		 
		public double getOpportunityCoeff() {
			return model.parameters.urbanOpportunityCoeff;
		}
		public void setOpportunityCoeff(double opportunityCoeff) {
			model.parameters.urbanOpportunityCoeff = opportunityCoeff;
		}
		
		public double getAdjacentSocialDiscount() {
			return model.parameters.urbanAdjacentSocialDiscount;
		}

		public void setAdjacentSocialDiscount(double val) {
			model.parameters.urbanAdjacentSocialDiscount = val;
		}
		
		public double getGrowthRate()
		{
			return model.parameters.urbanGrowthRate;
		}
		public void setGrowthRate(double val)
		{
			model.parameters.urbanGrowthRate = val;
		}
		
		public void setMovementWill(double val)
		{
			model.parameters.urbanMovementWill = val;
		}
		public double getMovementWill()
		{
			return model.parameters.urbanMovementWill;
		}
		
		public double getSocialWeight() 
		{
			return model.parameters.urbanSocialWeight;
		}
		public void setSocialWeight(double val) 
		{
			model.parameters.urbanSocialWeight = val;
		}
		
		public double getDesExp() {
			return model.parameters.urbanDesExp;
		}

		public void setDesExp(double val) {
			model.parameters.urbanDesExp = val;
		}
		
		public double getSocialExp() {
			return model.parameters.urbanSocialExp;
		}

		public void setSocialExp(double val) {
			model.parameters.urbanSocialExp = val;
		}
	}
	
	public class RuralProperties {
		Phase4 model;
		Phase4WithUI modelUI;
		
		public RuralProperties(Phase4WithUI modelUI) {
			this.modelUI = (Phase4WithUI)modelUI;
			this.model = (Phase4)((Phase4WithUI)modelUI).phaseModel;
		}
		
		public double getTempDesCoefficient() {
			return model.parameters.ruralTempCoeff;
		}

		public void setTempDesCoefficient(double val) {
			if(Phase4.COEFF_LOWER_BOUND<=val&&val<=Phase4.COEFF_UPPER_BOUND)
				model.parameters.ruralTempCoeff = val;	
		}

		public Object domTempDesCoefficient() {
			return new Interval(Phase4.COEFF_LOWER_BOUND, Phase4.COEFF_UPPER_BOUND);
		}


		public double getPortDesCoefficient() {
			return model.parameters.ruralPortCoeff;
		}

		public void setPortDesCoefficient(double val) {
			if(Phase4.COEFF_LOWER_BOUND<=val&&val<=Phase4.COEFF_UPPER_BOUND)
				model.parameters.ruralPortCoeff = val;
		}

		public Object domPortDesCoefficient() {
			return new Interval(Phase4.COEFF_LOWER_BOUND, Phase4.COEFF_UPPER_BOUND);
		}


		public double getRiverDesCoefficient() {
			return model.parameters.ruralRiverCoeff;
		}

		public void setRiverDesCoefficient(double val) {
			if(Phase4.COEFF_LOWER_BOUND<=val&&val<=Phase4.COEFF_UPPER_BOUND)
				model.parameters.ruralRiverCoeff = val;
		}

		public Object domRiverDesCoefficient() {
			return new Interval(Phase4.COEFF_LOWER_BOUND, Phase4.COEFF_UPPER_BOUND);
		}

		public double getElevDesCoefficient() {
			return model.parameters.ruralElevCoeff;
		}

		public void setElevDesCoefficient(double val) {
			if(Phase4.COEFF_LOWER_BOUND<=val&&val<=Phase4.COEFF_UPPER_BOUND)
				model.parameters.ruralElevCoeff = val;
		}

		public Object domElevDesCoefficient() {
			return new Interval(Phase4.COEFF_LOWER_BOUND, Phase4.COEFF_UPPER_BOUND);
		}
		
		
		public double getOpportunityCoeff() {
			return model.parameters.ruralOpportunityCoeff;
		}
		public void setOpportunityCoeff(double opportunityCoeff) {
			model.parameters.ruralOpportunityCoeff = opportunityCoeff;
		}

		public double getAdjacentSocialDiscount() {
			return model.parameters.ruralAdjacentSocialDiscount;
		}

		public void setAdjacentSocialDiscount(double val) {
			model.parameters.ruralAdjacentSocialDiscount = val;
		}
		
		public double getGrowthRate()
		{
			return model.parameters.ruralGrowthRate;
		}
		public void setGrowthRate(double val)
		{
			model.parameters.ruralGrowthRate = val;
		}
		
		public void setMovementWill(double val)
		{
			model.parameters.ruralMovementWill = val;
		}
		public double getMovementWill()
		{
			return model.parameters.ruralMovementWill;
		}
		
		public double getSocialWeight() 
		{
			return model.parameters.ruralSocialWeight;
		}
		public void setSocialWeight(double val) 
		{
			model.parameters.ruralSocialWeight = val;
		}
		
		public double getDesExp() {
			return model.parameters.ruralDesExp;
		}

		public void setDesExp(double val) {
			model.parameters.ruralDesExp = val;
		}
		
		public double getSocialExp() {
			return model.parameters.ruralSocialExp;
		}

		public void setSocialExp(double val) {
			model.parameters.ruralSocialExp = val;
		}
		
	}
	
}

