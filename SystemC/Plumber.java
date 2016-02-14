/******************************************************************************************************************
* File:Plumber.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*       1.1 Feb,07,2016 - Modified to set up pipe and filter configure for System C (NLH)
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

public class Plumber
{
   public static void main( String argv[])
   {
       System.out.println("Plumber::Process Start\n"); 
       
       String cfgPropFile      = System.getProperty("user.dir") + "/config.properties";
        //String cfgPropFile      = "W:/usr/nhoskeri/personal/TEP/course/17-655/Assignment/A1/SystemF/src/config.properties";
        Properties configProp   = LoadPropfile(cfgPropFile);
       
       
        System.out.println("Plumber::Loading config file " + cfgPropFile + "\n"); 
        
        /****************************************************************************
        * Get the source and sink names from the property file
        ****************************************************************************/
        String DataSetsSourceFolder    = configProp.getProperty("DataSetsSourceFolder");                         // Source data folder.
        String Source_1_fileName = DataSetsSourceFolder + configProp.getProperty("Source_1_fileName");           // Input data file.
        String Source_2_fileName = DataSetsSourceFolder + configProp.getProperty("Source_2_fileName");           // Input data file.
        
        String DataSetsSinkFolder    = configProp.getProperty("DataSetsSinkFolder");                            // Sink data folder.
        String Sink_1_fileName   = DataSetsSinkFolder + configProp.getProperty("Sink_1_fileName");              // Sink 1 Data file
        String Sink_2_fileName   = DataSetsSinkFolder + configProp.getProperty("Sink_2_fileName");              // Sink 2 Data file
        String Sink_3_fileName   = DataSetsSinkFolder + configProp.getProperty("Sink_3_fileName");              // Sink 3 Data file
        
        /****************************************************************************
        * Here we instantiate the filters.
        ****************************************************************************/


        SourceFilter SrcFilter1     = new SourceFilter();
        if(!SrcFilter1.SetSource(Source_1_fileName)) return;

        SourceFilter SrcFilter2     = new SourceFilter();
        if(!SrcFilter2.SetSource(Source_2_fileName)) return;

        TimeAlignFilter TimeAlgnFlt = new TimeAlignFilter();
        
        TemperatureFilter TempFlt   = new TemperatureFilter();        
        
        AltitudeFilter AltFlt       = new AltitudeFilter();
        
        PressureFilter PressFlt     = new PressureFilter();

        
        SinkFilter SnkFilter1       = new SinkFilter();
        SnkFilter1.SetSink(Sink_1_fileName);
        Integer[] paramIdOut1 = {0,4,1,2,3,5};  /*Select which measurement IDs to be output*/
        SnkFilter1.configParams(paramIdOut1);

        SinkFilter SnkFilter2       = new SinkFilter();
        SnkFilter2.SetSink(Sink_2_fileName);
        Integer[] paramIdOut2 = {0,3}; /*Select which measurement IDs to be output*/
        SnkFilter2.configParams(paramIdOut2);

        SinkFilter SnkFilter3       = new SinkFilter();
        SnkFilter3.SetSink(Sink_3_fileName);
        Integer[] paramIdOut3 = {0,4,2,3}; /*Select which measurement IDs to be output*/
        SnkFilter3.configParams(paramIdOut3);

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

        AltFlt.Connect(TempFlt,         1, 1);          /*Connect Altitude filter 1 input port to Temperature Filter output port 1*/
        
        TempFlt.Connect(TimeAlgnFlt,    1, 1);          /*Connect Temperature filter 1 input port to Time Align Filter output port 1*/

        TimeAlgnFlt.Connect(SrcFilter1, 1, 1);          /*Connect Time Aligh filter input port 1 to Source Filter 1 ouput port 1*/

        TimeAlgnFlt.Connect(SrcFilter2, 1, 2);          /*Connect Time Aligh filter input port 2 to Source Filter 2 ouput port 1*/


        /****************************************************************************
        * Here we start the filters up. All-in-all,... its really kind of boring.
        ****************************************************************************/

        SrcFilter1.start();
        SrcFilter2.start();
        TimeAlgnFlt.start();
        TempFlt.start();
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