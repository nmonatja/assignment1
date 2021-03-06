/******************************************************************************************************************
* File:Plumber.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*   2.0 Feb 2016 - Updated for System A, added 
*
* Description:
*
* This class serves as a Plumbler connecter to create a main thread that instantiates and connects a set
* of filters. This example consists of four filters: a source, a temperature filter, an altitude filter,
* and a sink filter which formats and prints out the data.
*
* Parameters: 		None
*
* Internal Methods:	None
*
******************************************************************************************************************/
public class Plumber
{
   public static void main( String argv[])
   {
		/****************************************************************************
		* Here we instantiate three filters.
		****************************************************************************/

		SourceFilter Filter1 = new SourceFilter();
		TemperatureFilter Filter2 = new TemperatureFilter();
		AltitudeFilter Filter3 = new AltitudeFilter();
		SinkFilter Filter4 = new SinkFilter();

		/****************************************************************************
		* Here we connect the filters starting with the sink filter (Filter 1) which
		* we connect to Filter2 the middle filter. Then we connect Filter2 to the
		* source filter (Filter3).
		****************************************************************************/

		Filter4.Connect(Filter3);
		Filter3.Connect(Filter2); // This esstially says, "connect Filter3 input port to Filter2 output port
		Filter2.Connect(Filter1); // This esstially says, "connect Filter2 intput port to Filter1 output port

		/****************************************************************************
		* Here we start the filters up. All-in-all,... its really kind of boring.
		****************************************************************************/

		Filter1.start();
		Filter2.start();
		Filter3.start();
		Filter4.start();

   } // main

} // Plumber