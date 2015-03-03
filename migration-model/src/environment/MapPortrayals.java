package environment;

import java.awt.Color;
import java.awt.Graphics2D;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.gui.ColorMap;
import sim.util.gui.SimpleColorMap;

public class MapPortrayals {
	Map map;
	public MapPortrayals(Map map) {
		this.map=map;
	}
	
	public FastValueGridPortrayal2D getNationsPortrayal() {
        FastValueGridPortrayal2D nationsPortrayal = new FastValueGridPortrayal2D();
        nationsPortrayal.setField(map.nationGrid);
        Color[] colorTable = {new Color(0f,0f,0f,0f),
                              new Color(0.9f, 0.9f, 0.9f, 1f), Color.BLACK,
                              Color.yellow, Color.magenta, Color.DARK_GRAY,
                              Color.green, Color.lightGray, Color.blue,
                              Color.pink, Color.gray, Color.cyan,
                              Color.orange, new Color(0.8f, 0.8f, 0.8f, 1f),
                              Color.red, Color.blue};
        nationsPortrayal.setMap(new SimpleColorMap(colorTable));
        return nationsPortrayal;
    }
	
	public FastValueGridPortrayal2D getCoastalPortrayal() {
		FastValueGridPortrayal2D coastalPortrayal = new FastValueGridPortrayal2D();
		coastalPortrayal.setField(map.coastalGrid);
		Color[] colorTable = {new Color(0f,0f,0f,0f),new Color(0f,0f,0f,0f), Color.decode("0xA6DBED")};
		coastalPortrayal.setMap(new SimpleColorMap(colorTable));
		return coastalPortrayal;
	}
	
    
    public FastValueGridPortrayal2D getPopulationPortrayal() {
        FastValueGridPortrayal2D populationPortrayal = new FastValueGridPortrayal2D();
        populationPortrayal.setField(map.popGrid);
        SimpleColorMap colorMap = new SimpleColorMap(
        		map.parameters.popColorMapLowerBound,
        		map.parameters.popColorMapUpperBound,
        		new Color(1, 0, 0, 0), Color.red);
        colorMap.setColorTable(new Color[]{new Color(0, 0, 0, 0)});
        populationPortrayal.setMap(colorMap);
        return populationPortrayal;
    }
    

    
    
    
    public FastValueGridPortrayal2D getTemperaturePortrayal() {
        FastValueGridPortrayal2D temperaturePortrayal = new FastValueGridPortrayal2D();
        temperaturePortrayal.setField(map.tempRawMovingAverage);
        //SimpleColorMap colorMap = new SimpleColorMap(240, 310, Color.white, Color.blue.darker());
        SimpleColorMap colorMap = new SimpleColorMap(
        		map.parameters.tempColorMapLowerBound,
        		map.parameters.tempColorMapUpperBound,
        		Color.white, Color.blue.darker());
        colorMap.setColorTable(new Color[]{new Color(0, 0, 0, 0)});
        temperaturePortrayal.setMap(colorMap);
        return temperaturePortrayal;
    }
    

    public FastValueGridPortrayal2D getTempDesPortrayal() {
        FastValueGridPortrayal2D tempDesPortrayal = new FastValueGridPortrayal2D();
        tempDesPortrayal.setField(map.tempDes);
        SmartColorMap colorMap = new SmartColorMap(map.getTempDesData(), new Color(0,0,0,0), Color.red);
        tempDesPortrayal.setMap(colorMap);
        return tempDesPortrayal;
    }
    
    public FastValueGridPortrayal2D getPortDesPortrayal() {
        FastValueGridPortrayal2D portDesPortrayal = new FastValueGridPortrayal2D();
        portDesPortrayal.setField(map.portDes);
        SmartColorMap colorMap = new SmartColorMap(map.getPortDesData(), new Color(0,0,0,0), Color.blue);
        portDesPortrayal.setMap(colorMap);
        return portDesPortrayal;
    }
    
    public FastValueGridPortrayal2D getRiverDesPortrayal() {
        FastValueGridPortrayal2D riverDesPortrayal = new FastValueGridPortrayal2D();
        riverDesPortrayal.setField(map.riverDes);
        SmartColorMap colorMap = new SmartColorMap(map.getRiverDesData(), new Color(0,0,0,0), Color.cyan);
        riverDesPortrayal.setMap(colorMap);
        return riverDesPortrayal;
    }
    
    public FastValueGridPortrayal2D getElevDesPortrayal() {
        FastValueGridPortrayal2D riverDesPortrayal = new FastValueGridPortrayal2D();
        riverDesPortrayal.setField(map.elevDes);
        SmartColorMap colorMap = new SmartColorMap(map.getTempDesData(), new Color(0,0,0,0), new Color(153, 76, 0));
        riverDesPortrayal.setMap(colorMap);
        return riverDesPortrayal;
    }
    
    public FastValueGridPortrayal2D getTotalDesPortrayal() {
        FastValueGridPortrayal2D riverDesPortrayal = new FastValueGridPortrayal2D();
        riverDesPortrayal.setField(map.totalDes);
        SmartColorMap colorMap = new SmartColorMap(map.getTotalDesData(), new Color(0,0,0,0), Color.green);
        riverDesPortrayal.setMap(colorMap);
        return riverDesPortrayal;
    }
    
    public FieldPortrayal2D getCitiesPortrayal()
    {
        SparseGridPortrayal2D portrayal = new SparseGridPortrayal2D();
        portrayal.setField(map.cities);

        final int largestPopulation = 20000;
       // final ColorMap colorMap = new SimpleColorMap(0,1.0, new Color(200,200,200), new Color(255,0, 0));
        OvalPortrayal2D cityPortrayal = new OvalPortrayal2D(
        		map.parameters.cityColor,map.parameters.maxCityPortrayalScale)
        {
            private double newScale(Cell city)
            {
                double cityScale = (double)city.population/largestPopulation * 
                		Math.pow(map.parameters.maxCityPortrayalScale,4);
                double radius = Math.sqrt(cityScale/Math.PI);
                return Math.max((int) (map.parameters.minCityPortrayalScale),(int) 2*radius);
            }
            
            private Color newColor(Cell city)
            {
                return Color.red;
            }
            
            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D drawInfo)
            {
                scale = newScale((Cell) object);
                paint = newColor((Cell) object);
                super.draw(object, graphics, drawInfo);
                /*int width = Math.max((int) (drawInfo.draw.width * params.getMinCityPortrayalScale()), (int) (drawInfo.draw.width * scale * cityScale));
                int height = Math.max((int) (drawInfo.draw.height * params.getMinCityPortrayalScale()), (int) (drawInfo.draw.height * scale * cityScale));
                graphics.fillOval((int) drawInfo.draw.x, (int) drawInfo.draw.y, width, height);*/
            }
            
            @Override
            public boolean hitObject(Object object, DrawInfo2D range)
            {
                scale = newScale((Cell) object);
                return super.hitObject(object, range);
            }
            
        };
        
        portrayal.setPortrayalForClass(Cell.class, cityPortrayal);
        
        return portrayal;
    }
}
