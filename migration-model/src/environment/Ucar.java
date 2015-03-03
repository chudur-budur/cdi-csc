/**
 * This class is designed to allow a relatively simple interface to the UCAR
 * tools jar library for reading weather data.  Where possible, this class will
 * return data in a model-friendly form (i.e. DoubleGrid2D, or whatever).
 */
package environment;

//import java.awt.Color;
//import java.io.FileInputStream;
//import java.io.ObjectInputStream;
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.StringTokenizer;
//import ec.util.MersenneTwisterFast;

import java.io.*;

import ucar.ma2.*;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import sim.field.grid.DoubleGrid2D;


// This class interfaces the the Ucar APIs for reading climate data.
public class Ucar
{

	private NetcdfFile ncFile = null;
	private double latBounds[];
	private double lonBounds[];
	private Variable dataVar;
	private Array currentLayer;
	private int[] layerOrigin;
	private int[] layerSize;
	private DoubleGrid2D latGrid;
	private DoubleGrid2D lonGrid;


	// It might make sense to put the re-projection stuff in a different class.
	Index[][] xy2indexMap;  // our (x,y) coordinates to NetCDF indices


	/**
	 * Open a UCAR data file, and keep it open so that we can read different
	 * layers out of it while the simulation proceeds.
	 *
	 * XXX I really have to rethink exceptions in this method.
	 *
	 * @param filename
	 */
	Ucar(String filename, DoubleGrid2D latGrid, DoubleGrid2D lonGrid)
	{
		//String filename="../../tas_Amon_CCSM4_piControl_r1i1p1_080001-130012.nc";

		this.latGrid = latGrid;
		this.lonGrid = lonGrid;

		// Open the NetCDF file
		try
		{
			ncFile = NetcdfFile.open(filename);
		}
		catch (IOException e)
		{
			// Is this really the right place to be catching exceptions?
			// I think it might make more sense to pass them up.
			System.err.println("Error opening netCDF File");
			System.err.printf("filename = %s%n", filename);
			e.printStackTrace();
			System.exit(1);
		}

		try
		{
			loadMetadata();
		}
		catch (IOException e)
		{
			System.err.println("Error loading NetCDF metadata");
			System.exit(1);
		}

		// In order to create indices for the reprojection map,
		// I need to load a layer.
		try
		{
			loadLayer(0);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.xy2indexMap = generateReprojectionMap();
	}


	/**
	 * Close the file.  This isn't really necessary.  Destroying the class should
	 * be enough.
	 * @throws IOException
	 */
	void close() throws IOException
	{
		ncFile.close();
	}


	/**
	 * Makes sure the dimensions lat, lon and time are there.  Also records
	 * the ranges for each lat and lon cell in the class variables latBounds
	 * and lonBounds.  Also checks on the variable that stores the real data.
	 * @throws IOException
	 */
	private void loadMetadata() throws IOException
	{
		Dimension latDim = ncFile.findDimension("lat");
		Dimension lonDim = ncFile.findDimension("lon");
		Dimension timeDim = ncFile.findDimension("time");
		assert(latDim != null);
		assert(lonDim != null);
		assert(timeDim != null);

		Variable latVar = ncFile.findVariable(latDim.getShortName());
		Variable lonVar = ncFile.findVariable(lonDim.getShortName());
		Variable timeVar = ncFile.findVariable(timeDim.getShortName());
		assert(latVar != null);
		assert(lonVar != null);
		assert(timeVar != null);

		Variable latBoundsVar = ncFile.findVariable("lat_bnds");
		Variable lonBoundsVar = ncFile.findVariable("lon_bnds");
		assert(latBoundsVar != null);
		assert(lonBoundsVar != null);

		Index  ncIndex;

		// Get the latitude bounds
		Array latBoundsArray = latBoundsVar.read();
		int latBoundsShape[] = latBoundsArray.getShape();
		assert(latBoundsShape[1] == 2);
		latBounds = new double[latBoundsShape[0]+1];

		ncIndex = latBoundsArray.getIndex();
		latBounds[0] = latBoundsArray.getDouble(ncIndex.set(0,0));
		for (int i=0; i < latBoundsShape[0]; i++)
		{
			double lower = latBoundsArray.getDouble(ncIndex.set(i,0));
			double upper = latBoundsArray.getDouble(ncIndex.set(i,1));
			assert(lower == latBounds[i]);
			latBounds[i+1] = upper;
		}

		// Get the longitude bounds
		Array lonBoundsArray = lonBoundsVar.read();
		int lonBoundsShape[] = lonBoundsArray.getShape();
		assert(lonBoundsShape[1] == 2);
		lonBounds = new double[lonBoundsShape[0]+1];

		ncIndex = lonBoundsArray.getIndex();
		lonBounds[0] = lonBoundsArray.getDouble(ncIndex.set(0,0));
		for (int i=0; i < lonBoundsShape[0]; i++)
		{
			double lower = lonBoundsArray.getDouble(ncIndex.set(i,0));
			double upper = lonBoundsArray.getDouble(ncIndex.set(i,1));
			assert(lower == lonBounds[i]);
			lonBounds[i+1] = upper;
		}

		// For now we'll assume the data is temperature data.  Actually,
		// a lot of things are assumed here (and above). We should probably
		// generalize this at some point.
		dataVar = ncFile.findVariable("tas");
		assert(dataVar != null);

		int[] shape = dataVar.getShape();
		layerOrigin = new int[] {0, 0, 0};
		layerSize = new int[] {1, shape[1], shape[2]};
	}


	int getStepsPerYear()
	{
	    return 12;  // Should get from metadata.
	}
	
	
	/**
	 * Reads in the layer of weather data in the next time-step.
	 * @throws IOException
	 * @throws InvalidRangeException
	 */
	void loadLayer(int timeIndex) throws IOException
	{
		layerOrigin[0] = timeIndex;

		// Paul's original comment:
		// The data read in from the file is 3-D [time][lat][lon]
		//  we could get cute and apply .reduce() to it directly,
		//  but for clarity, create a new array, read it in then
		//  reduce it to the 2D temperature Array.

		Array t3D = null;
		try
		{
			t3D = dataVar.read(layerOrigin, layerSize);
		}
		catch (InvalidRangeException e)
		{
			System.err.println("InvalidRangeException");
			Thread.dumpStack();
		}
		currentLayer = t3D.reduce();
	}


	/**
	 * Returns a UCAR index object for given (x,y) coordinates.
	 */
	Index getIndex(int x, int y)
	{
		return xy2indexMap[x][y];
	}


	/**
	 * Returns weather data at a specific UCAR Index.
	 * Of course, this assumes that the index is properly formed.
	 */
	double getDataInCell(Index index)
	{
		return currentLayer.getFloat(index);
	}


	/**
	 * Returns the current layer as a DoubleGrid2D, re-projected to our
	 * current projection.
	 */
	void populateDoubleGrid2D(DoubleGrid2D grid)
	{
		int width = xy2indexMap.length;
		int height = xy2indexMap[0].length;
		assert(width == grid.getWidth());
		assert(height == grid.getHeight());

		for (int xi = 0; xi < width; xi++)
			for (int yi = 0; yi < height; yi++)
			{
				grid.set(xi, yi, getDataInCell(xy2indexMap[xi][yi]));
			}
	}


	/**
	 * Make sure all angle fall between 0 and 360.
	 */
	public static double adjustAngle(double angle)
	{
		double newAngle = angle;

		while (newAngle < 0.0)
			newAngle += 360.0;

		while (newAngle > 360.0)
			newAngle -= 360.0;

		return newAngle;
	}



	/**
	 * Check to see if a number is within certain bounds.  If upperBound is
	 * less than lowerBound, it is assumed that the area is toroidal, and the
	 * space wraps.
	 */
	public static boolean withinBounds(double val, double lowerBound,
	                                   double upperBound)
	{
		boolean isWithin = false;
		if (upperBound < lowerBound)
			isWithin = val < upperBound || val > lowerBound;
		else
			isWithin = val < upperBound && val > lowerBound;

		return isWithin;
	}


	/**
	 * Check to see if an angle is within certain bounds.  Bounds are
	 * assumed to wrap (i.e. -180 degrees = 180 degrees).
	 */
	public static boolean withinAngles(double angle, double lowerBound,
	                                   double upperBound)
	{
		return withinBounds(adjustAngle(angle), adjustAngle(lowerBound),
		                    adjustAngle(upperBound));
	}


	/**
	 * Generate the map from (x,y) coordinates in our polar projection to indices
	 * into the UCAR data.
	 */
	public Index[][] generateReprojectionMap()
	{
		int gridWidth = latGrid.getWidth();
		int gridHeight = latGrid.getHeight();
		Index[][] indexGrid = new Index[gridWidth][gridHeight];

		int latInd, lonInd; // Components of the weather data indices
		for (int yi = 0; yi < gridHeight; yi++)
			for (int xi = 0; xi < gridWidth; xi++)
			{
				double lat = latGrid.get(xi,  yi);
				double lon = lonGrid.get(xi,  yi);

				// Get the associated weather grid cell
				// TODO: This can be optimized a lot, and should be.
				for (latInd = 0; latInd < latBounds.length - 2; latInd++)
					if (withinAngles(lat, latBounds[latInd],
					                 latBounds[latInd + 1]))
						break;
				assert (latInd < latBounds.length - 1);

				for (lonInd = 0; lonInd < lonBounds.length - 2; lonInd++)
					if (withinAngles(lon, lonBounds[lonInd],
					                 lonBounds[lonInd + 1]))
						break;
				assert (lonInd < lonBounds.length - 1);

				// This assumes a layer has already been loaded!
				Index index = currentLayer.getIndex();
				index.set(latInd, lonInd);
				indexGrid[xi][yi] = index;
			}

		return indexGrid;
	}
}

