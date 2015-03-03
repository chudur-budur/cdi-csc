package environment;



import migration.Household;
import sim.field.grid.Grid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.IntBag;

public class Cell implements Comparable<Object>{
	

	final public int x, y, nation;
	public int population, empPop;
	public int cellType; // -1 for unknown, 0 for urban, 1 for rural
	final public double tempDes, portDes, elevDes, riverDes;
	public double infrastructure;
	public double socialDes, totalDes;
	public Bag nearestCities;
	
	
	public Cell(int x, int y, int nation, double tempDes, double portDes, double elevDes, double riverDes, double totalDes, int empPop){
		
        this.x=x;
        this.y=y;
        this.nation = nation;
        this.tempDes =tempDes;
        this.portDes = portDes;
        this.elevDes = elevDes;
        this.riverDes = riverDes;
        this.totalDes = totalDes;
        this.empPop = empPop; // this is from the population grid
        socialDes = 0;
        population = 0;
        infrastructure = 0;
        nearestCities= new Bag();
	}
	
	public void findNearestCities(SparseGrid2D cities) {
		cities.getRadialNeighbors(x, y, 50, Grid2D.BOUNDED, false, nearestCities, new IntBag(), new IntBag());
	}
	
	public void addHouseholds(int number) {
		population+=number;
	}
	
	public void addHousehold (Household h) {
		population++;
	}
	
	public void removeHousehold (Household h) {
		population--;
	}
	
	public void addHousehold () {
		population++;
	}
	
	public void removeHousehold () {
		population--;
	}
	
	public void setHouseholds(int number)
	{
		population = number;
	}

	@Override
	public int compareTo(Object o) {
		Cell c = (Cell)o;
		if(totalDes<c.totalDes)
			return -1;
		else if(totalDes>c.totalDes)
			return 1;
		else
		return 0;
	}  
	
	
	// to allow Cell as a key in hashmap
	@Override
	public int hashCode(){    
        return this.x * 100 + this.y;
	}
	
	@Override
	public boolean equals(Object o){    
		Cell rhs = (Cell)o;
		if((this.x==rhs.x)&&(this.y==rhs.y))
			return true;
		return false;
	}   
}
