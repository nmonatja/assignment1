/******************************************************************************************************************
* File:SinkFilter.java    ivan was here
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*   2.0 Feb 2016 - SinkFilter formats and outputs data for System A
*
* Description:
*
* This class is a sink filter for System A. This particular
* filter reads some input from the filter's input port and does the following:
*
*	1) It parses the input stream and "decommutates" the measurement ID
*	2) It parses the input steam for measurments and "decommutates" measurements, storing the bits in a long word.
*   3) Write the data into a output file OutputA.dat
*
*
* Parameters: 	None
*
* Internal Methods: None
*
******************************************************************************************************************/
import java.util.*;						// This class is used to interpret time words
import java.text.SimpleDateFormat;		// This class is used to format and write time in a string format.
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class SinkFilter extends FilterFramework
{
	public void run()
    {
		/************************************************************************************
		*	TimeStamp is used to compute time using java.util's Calendar class.
		* 	TimeStampFormat is used to format the time value 
		*************************************************************************************/

		Calendar TimeStamp = Calendar.getInstance();
		SimpleDateFormat TimeStampFormat = new SimpleDateFormat("YYYY:MM:dd:HH:mm:ss"); //this is not what's in the instructions, but I think it's what is really desired.
		NumberFormat formatter = new DecimalFormat("#0.00000");  // we only want the number to 5 decimal points

		int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
		int IdLength = 4;				// This is the length of IDs in the byte stream

		byte databyte = 0;				// This is the data byte read from the stream
		int bytesread = 0;				// This is the number of bytes read from the stream

		long measurement;				// This is the word used to store all measurements - conversions are illustrated.
		long alt = 0;
		int id;							// This is the measurement id
		int i;							// This is a loop counter

		try 
		{	

			BufferedWriter writer = new BufferedWriter( new FileWriter( "../DataSets/OutputA.dat"));

			/*************************************************************
			*	First we announce to the world that we are alive...
			**************************************************************/

			System.out.print( "\n" + this.getName() + "::Sink Reading ");
		
			writer.write("Time:\t\t\t\t\tTemperature(C):\t\tAltitude (m):");
			writer.newLine();
			writer.write("----------------------------------------------------------------------------");
			writer.newLine();

			while (true)
			{
				try
				{
					/***************************************************************************
					// We know that the first data coming to this filter is going to be an ID and
					// that it is IdLength long. So we first decommutate the ID bytes.
					****************************************************************************/

					id = 0;

					for (i=0; i<IdLength; i++ )
					{
						databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...

						id = id | (databyte & 0xFF);		// We append the byte on to ID...

						if (i != IdLength-1)				// If this is not the last byte, then slide the
						{									// previously appended byte to the left by one byte
							id = id << 8;					// to make room for the next byte we append to the ID

						} // if

						bytesread++;						// Increment the byte count

					} // for

					/****************************************************************************
					// Here we read measurements. All measurement data is read as a stream of bytes
					// and stored as a long value. This permits us to do bitwise manipulation that
					// is neccesary to convert the byte stream into data words. Note that bitwise
					// manipulation is not permitted on any kind of floating point types in Java.
					// If the id = 0 then this is a time value and is therefore a long value - no
					// problem. However, if the id is something other than 0, then the bits in the
					// long value is really of type double and we need to convert the value using
					// Double.longBitsToDouble(long val) to do the conversion which is illustrated.
					// below.
					*****************************************************************************/

					measurement = 0;

					for (i=0; i<MeasurementLength; i++ )
					{
						databyte = ReadFilterInputPort();
						measurement = measurement | (databyte & 0xFF);	// We append the byte on to measurement...

						if (i != MeasurementLength-1)					// If this is not the last byte, then slide the
						{												// previously appended byte to the left by one byte
							measurement = measurement << 8;				// to make room for the next byte we append to the
																		// measurement
						} // if

						bytesread++;									// Increment the byte count

					} // if

					/****************************************************************************
					// Here we look for an ID of 0 which indicates this is a time measurement.
					// Every frame begins with an ID of 0, followed by a time stamp which correlates
					// to the time that each proceeding measurement was recorded. Time is stored
					// in milliseconds since Epoch. This allows us to use Java's calendar class to
					// retrieve time and also use text format classes to format the output into
					// a form humans can read. So this provides great flexibility in terms of
					// dealing with time arithmetically or for string display purposes. This is
					// illustrated below.
					****************************************************************************/

					if ( id == 0 )
					{
						TimeStamp.setTimeInMillis(measurement);

					} // if

					/****************************************************************************
					// Here we pick up a measurement, but you can pick up
					// any measurement you want to. All measurements in the stream are
					// decommutated by this class. Note that all data measurements are double types
					// This illustrates how to convert the bits read from the stream into a double
					// type. Its pretty simple using Double.longBitsToDouble(long value). So here
					// we print the time stamp and the data associated with the ID we are interested
					// in.
					****************************************************************************/
					
					// Unfortunately, Altitude is encountered before temp, but we need to print out temp first. 
					// So, we need to save alt for later use - it will get reset for the next row, just like time does.
				
					if ( id == 2 ) 
					{
						alt = measurement;
					} 

					if ( id == 4 )
					{
						//System.out.print( TimeStampFormat.format(TimeStamp.getTime()) + " ID = " + id + " " + Double.longBitsToDouble(measurement));
						// print out the file 
						
						writer.write(TimeStampFormat.format(TimeStamp.getTime())+"\t\t"
							+ formatter.format(Double.longBitsToDouble(measurement))+"\t\t\t"
							+ formatter.format(Double.longBitsToDouble(alt)));
						writer.newLine();

					} // if

					//System.out.print( "\n" );
				

				} // try

				/*******************************************************************************
				*	The EndOfStreamExeception below is thrown when you reach end of the input
				*	stream (duh). At this point, the filter ports are closed and a message is
				*	written letting the user know what is going on.
				********************************************************************************/

				catch (EndOfStreamException e)
				{
					ClosePorts();
					System.out.print( "\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread );
					break;

				} // catch

			} // while
		
			writer.close();
		}
		catch (IOException e)
		{
			System.out.println("Cannot write out to OutputA.dat!");
		}

   } // run

} // SingFilter
