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


// This class is a wrapper to the Ucar class, and aggregates some time steps
// the process
public class UcarSeasons
{
    // XXX Should make this a parameter
    private final int fileStepsPerModelStep = 3;  // i.e. months per season

    private Ucar ucar;
    
    private DoubleGrid2D currentSeason;
    private DoubleGrid2D currentMonth;  // To reduce re-allocations
    
    /**
     * Open a UCAR data file, and keep it open so that we can read different
     * layers out of it while the simulation proceeds.
     *
     * @param filename
     */
    UcarSeasons(String filename, DoubleGrid2D latGrid, DoubleGrid2D lonGrid)
    {
        ucar = new Ucar(filename, latGrid, lonGrid);
        currentSeason = new DoubleGrid2D(latGrid);  // Just to get the dimensions right
        currentMonth = new DoubleGrid2D(currentSeason);
    }


    /**
     * Close the file.  This isn't really necessary.  Destroying the class should
     * be enough.
     * @throws IOException
     */
    void close() throws IOException
    {
        ucar.close();
    }

    
    int getStepsPerYear()
    {
        return ucar.getStepsPerYear() / fileStepsPerModelStep;
    }
    
    
    /**
     * Reads in the layer of weather data in the next time-step.
     * @throws IOException
     * @throws InvalidRangeException
     */
    void loadLayer(int timeIndex) throws IOException
    {
        currentSeason.setTo(0.0);

        for(int i = 0; i < fileStepsPerModelStep; i++)
        {
            ucar.loadLayer(timeIndex * fileStepsPerModelStep + i);
            ucar.populateDoubleGrid2D(currentMonth);
            currentSeason.add(currentMonth);
            //currentSeason.setTo(currentMonth);
        }
        currentSeason.multiply(1.0/fileStepsPerModelStep);
    }


    /**
     * Returns a UCAR index object for given (x,y) coordinates.
     */
//    Index getIndex(int x, int y)
//    {
//        return xy2indexMap[x][y];
//    }


    /**
     * Returns weather data at a specific UCAR Index.
     * Of course, this assumes that the index is properly formed.
     */
//    double getDataInCell(Index index)
//    {
//        return currentLayer.getFloat(index);
//    }


    /**
     * Returns the current layer as a DoubleGrid2D, re-projected to our
     * current projection.
     */
    void populateDoubleGrid2D(DoubleGrid2D grid)
    {
        grid.setTo(currentSeason);
    }


}

