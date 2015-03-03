package world;

import migration.Household;
import migration.Migration;
import migration.parameters.*;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import environment.Cell;
import environment.Map;

public class WorldAgent implements Steppable
{


	@Override
    public void step(SimState sim)
    {	
      Map map = ((Migration)sim).getMap();
      Parameters params = ((Migration)sim).getParams();
      int populationIncrease;
      double populationGrowthRate = 0.01;
      Cell c;
      for(Object o : map.getCities()) {
    	  c= (Cell)o;
    	  populationIncrease = (int)(populationGrowthRate * c.population+0.5);
    	  c.addHouseholds(populationIncrease);
    	  for(int i=0;i<populationIncrease;i++) {
    		  ((Migration)sim).households.add(new Household(c, params));
    	  }
      }   
    }
}


