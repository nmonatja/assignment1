/******************************************************************************************************************
* File:Plumber.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*       1.1 N.Hoskeri, Feb,07,2016 - Modified to set up pipe and filter configure for System C
*
* Description:
*
* This class serves as an the pipe and filter setup for System C
*
* Parameters: 		None
*
* Internal Methods:	None
*
******************************************************************************************************************/
import java.util.Properties;
import java.io.*;
import java.nio.ByteBuffer;

public class Plumber
{
   public static void main( String argv[])
   {
        String cfgPropFile      = System.getProperty("user.dir") + "/config.properties";
        //String cfgPropFile      = "W:/usr/nhoskeri/personal/TEP/course/17-655/Assignment/A1/SystemD/src/config.properties";
        Properties configProp   = LoadPropfile(cfgPropFile);
       

      
        /****************************************************************************
        * Here we instantiate the filters.
        ****************************************************************************/
        /*
        String Source_1_fileName = "W:/usr/nhoskeri/personal/TEP/course/17-655/Assignment/A1/temp/DataSets/SubSetA.dat";             // Input data file.
        String Source_2_fileName = "W:/usr/nhoskeri/personal/TEP/course/17-655/Assignment/A1/temp/DataSets/SubSetB.dat";             // Input data file.
        String Sink_1_fileName   = "W:/usr/nhoskeri/personal/TEP/course/17-655/Assignment/A1/temp/DataSets/LessThan10K.dat";         // Sink 1 Data file
        String Sink_2_fileName   = "W:/usr/nhoskeri/personal/TEP/course/17-655/Assignment/A1/temp/DataSets/PressureWildPoints.dat";  // Sink 2 Data file
        String Sink_3_fileName   = "W:/usr/nhoskeri/personal/TEP/course/17-655/Assignment/A1/temp/DataSets/OutputC.dat";             // Sink 3 Data file
        */
        String DataSetsSourceFolder    = configProp.getProperty("DataSetsSourceFolder");                         // Source data folder.
        String Source_1_fileName = DataSetsSourceFolder + configProp.getProperty("Source_1_fileName");           // Input data file.
        String Source_2_fileName = DataSetsSourceFolder + configProp.getProperty("Source_2_fileName");           // Input data file.
        
        String DataSetsSinkFolder    = configProp.getProperty("DataSetsSinkFolder");                            // Sink data folder.
        String Sink_1_fileName   = DataSetsSinkFolder + configProp.getProperty("Sink_1_fileName");              // Sink 1 Data file
        String Sink_2_fileName   = DataSetsSinkFolder + configProp.getProperty("Sink_2_fileName");              // Sink 2 Data file
        String Sink_3_fileName   = DataSetsSinkFolder + configProp.getProperty("Sink_3_fileName");              // Sink 3 Data file

        SourceFilter SrcFilter1     = new SourceFilter();
        if(!SrcFilter1.SetSource(Source_1_fileName)) return;

        SourceFilter SrcFilter2     = new SourceFilter();
        if(!SrcFilter2.SetSource(Source_2_fileName)) return;

        TimeAlignFilter TimeAlgnFlt = new TimeAlignFilter();
        
        AltitudeFilter AltFlt       = new AltitudeFilter();
        
        PressureFilter PressFlt     = new PressureFilter();

        SinkFilter SnkFilter1       = new SinkFilter();
        SnkFilter1.SetSink(Sink_1_fileName);

        SinkFilter SnkFilter2       = new SinkFilter();
        SnkFilter2.SetSink(Sink_2_fileName);

        SinkFilter SnkFilter3       = new SinkFilter();
        SnkFilter3.SetSink(Sink_3_fileName);

        
        //TimeAlgnFlt.DbgTraceOn      = true;
        AltFlt.DbgTraceOn           = true;

        /****************************************************************************
        * Here we connect the filters starting with the sink filter (Filter 1) which
        * we connect to Filter2 the middle filter. Then we connect Filter2 to the
        * source filter (Filter3).
        ****************************************************************************/
        /*Connect the filter chain in reverse order*/
        SnkFilter3.Connect(PressFlt,    1, 1);          /*Connect SnkFilter3 input port 1 to Pressure Filter's output port 1*/
        
        SnkFilter2.Connect(PressFlt,    2, 1);          /*Connect SnkFilter2 input port 1 to Pressure Filter's output port 2*/

        PressFlt.Connect(AltFlt,        1, 1);          /*Connect Pressure filter input port 1 to Altitude Fitler output port 1*/

        SnkFilter1.Connect(AltFlt,      2, 1);          /*Connect Snkfilter11 input port 1 to Altitude Filter ouput port 2*/

        AltFlt.Connect(TimeAlgnFlt,     1, 1);          /*Connect Altitude filter 1 input port to Time Align Filter output port 1*/

        TimeAlgnFlt.Connect(SrcFilter1, 1, 1);          /*Connect Time Aligh filter input port 1 to Source Filter 1 ouput port 1*/

        TimeAlgnFlt.Connect(SrcFilter2, 1, 2);          /*Connect Time Aligh filter input port 2 to Source Filter 2 ouput port 1*/


        /****************************************************************************
        * Here we start the filters up. All-in-all,... its really kind of boring.
        ****************************************************************************/

        SrcFilter1.start();
        SrcFilter2.start();
        TimeAlgnFlt.start();
        AltFlt.start();
        PressFlt.start();
        SnkFilter1.start();
        SnkFilter2.start();
        SnkFilter3.start();
        
        
   } // main
   
   static Properties LoadPropfile(String propfile)
   {
        Properties prop = new Properties();
        InputStream input = null;
        
        
        try 
        {
            input = new FileInputStream(propfile);

            // load a properties file
            prop.load(input);
                
        } 
        catch (IOException iox) 
        {
            System.out.println("\n" + "Plumber "+ "::Problem loading properties file::" + iox );
        } 
        finally 
        {
            if (input != null) 
            {
                try 
                {
                    input.close();
                } 
                catch (IOException iox) 
                {
                    System.out.println("\n" + "Plumber "+ "::Problem closing properties file::" + iox );
		}
            }
	}
        return (prop);
   }

} // Plumber