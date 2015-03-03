package phases.phase4;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;



public class Inspector {
	public int iteration;
	public int urbanPop;
	public int ruralPop;
	public int urbanToRural;
	public int urbanToUrban;
	public int ruralToUrban;
	public int ruralToRural;
	public double totalUrbanDistance;
	public double totalRuralDistance;
	public int cellToUrban;
	public int cellToRural;
	private String path;
	
	private boolean appendToEnd;
	
	public Inspector(String _path)
	{
		path = _path;
		appendToEnd = false; // every time start, we overwrite the file for the first time
		resetAll();
	}
	
	
	public void writeToFile()
	{
		try
		{
			File file = new File(path);

			
		    FileWriter writer = new FileWriter(file, appendToEnd);
		    writer.append(Integer.toString(iteration));
		    writer.append(',');
		    
		    writer.append(Integer.toString(urbanPop));
		    writer.append(',');
		    
		    writer.append(Integer.toString(ruralPop));
		    writer.append(',');
		    
		    writer.append(Integer.toString(urbanToRural));
		    writer.append(',');
		    
		    writer.append(Integer.toString(urbanToUrban));
		    writer.append(',');
		    
		    writer.append(Integer.toString(ruralToUrban));
		    writer.append(',');
		    
		    writer.append(Integer.toString(ruralToRural));
		    writer.append(',');
		    
		    writer.append(Double.toString(totalUrbanDistance));
		    writer.append(',');
		    
		    writer.append(Double.toString(totalRuralDistance));
		    writer.append(',');
		    
		    writer.append(Integer.toString(cellToUrban));
		    writer.append(',');
		    
		    writer.append(Integer.toString(cellToRural));
		    writer.append('\n');
	 
		    writer.flush();
		    writer.close();
		    
		    // after the first write, we append the info at the end
		    if(!appendToEnd)
		    	appendToEnd = true;
		    
		    resetAll();
		}
		catch(IOException e)
		{
		     e.printStackTrace();
		} 
		
		
	}


	public void setIteration(int counter) {
		iteration = counter;
		
	}


	public void setRuralPop(int ruralResidence) {
		ruralPop = ruralResidence;
	}


	public void setUrbanPop(int urbanResidence) {
		urbanPop = urbanResidence;
	}


	public void incrementUrbanToUrban() {
		urbanToUrban++;
	}


	public void incrementRuralToUrban() {
		ruralToUrban++;
	}


	public void incrementRuralToRural() {
		ruralToRural++;
	}


	public void incrementUrbanToRural() {
		urbanToRural++;
	}
	
	
	public void resetAll()
	{
		iteration = -1;
		urbanPop = 0;
		ruralPop = 0;
		urbanToRural = 0;
		urbanToUrban = 0;
		ruralToUrban = 0;
		ruralToRural = 0;
		totalUrbanDistance = 0;
		totalRuralDistance = 0;
		cellToUrban = 0;
		cellToRural = 0;
	}


	public void incrementCellToRural() {
		cellToRural++;
	}


	public void incrementCellToUrban() {	
		cellToUrban++;
	}
	
	
	
}
