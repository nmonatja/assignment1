/******************************************************************************************************************
* File:PressureFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
*
* This class serves as an example for using the SinkFilterTemplate for creating a sink filter. This particular
* filter reads some input from the filter's input port and does the following:
*
*	1) It parses the input stream and "decommutates" the measurement ID
*	2) It parses the input steam for measurments and "decommutates" measurements, storing the bits in a long word.
*
* This filter illustrates how to convert the byte stream data from the upstream filterinto useable data found in
* the stream: namely time (long type) and measurements (double type).
*
*
* Parameters: 	None
*
* Internal Methods: None
*
******************************************************************************************************************/
import java.util.*;						// This class is used to interpret time words
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;		// This class is used to format and write time in a string format.

public class PressureFilter extends FilterFramework
{
		ArrayList<DataFrame> frameList = new ArrayList<DataFrame>();
		SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy:dd:hh:mm:ss");
		DecimalFormat TempFormat = new DecimalFormat("#000.00000");
		DecimalFormat AltitudeFormat = new DecimalFormat("#000000.00000");
		DecimalFormat PressureFormat = new DecimalFormat("#00.00000");
		
		//used for testing only
		public void printFrames()
		{
			String output;
			for (DataFrame frame : frameList)
			{
			output = TempFormat.format(frame.temperature) + "    " + AltitudeFormat.format(frame.altitude) 
					+ "    " + PressureFormat.format(frame.pressure) + "    " 
					+ frame.wildPoint + "    ";
			
			System.out.print(output);
			System.out.print( "\n" );
			
			}
		}
		
		//Takes the data from DataFrame and sends it through the pipe
		public void pipeOutput()
		{			
			Boolean wildPoint;//stores whether a frame has PSI wild point or not
			Double wildPSI;
			
			for (DataFrame frame : frameList)
			{
				pipeInteger(0);
				//to do - send timestamp
				pipeLong(frame.TimeStamp);
				pipeInteger(1);
				pipeDouble(frame.velocity);
				pipeInteger(2);
				pipeDouble(frame.altitude);
				pipeInteger(3);
				pipeDouble(frame.pressure);
				pipeInteger(4);
				pipeDouble(frame.temperature);
				pipeInteger(5);
				pipeDouble(frame.attitude);		
			}
			
			
		}
		//pipe Double type 
		public void pipeDouble(Double input)
		{
			ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);				// used to write out transformed measurement
			long output = Double.doubleToRawLongBits(input); 
			buffer.putLong(0, output);
			byte[] bufferArray = buffer.array();
			for (int i=0; i<bufferArray.length; i++)
			{
				WriteFilterOutputPort(bufferArray[i]);
			}
		}
		
		//pipe Integer type 
		public void pipeInteger(Integer input)
		{			
			ByteBuffer b = ByteBuffer.allocate(4);
			//b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
			b.putInt(input);

			byte[] bufferArray = b.array();
			
			for (int i=0; i<bufferArray.length; i++)
			{
				WriteFilterOutputPort(bufferArray[i]);
			}
		}
		
		//pipe Long type 
				public void pipeLong(Long input)
				{
					ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);				// used to write out transformed measurement
					long output = input; 
					buffer.putLong(0, output);
					byte[] bufferArray = buffer.array();
					for (int i=0; i<bufferArray.length; i++)
					{
						WriteFilterOutputPort(bufferArray[i]);
					}
				}
		
		//method used to check whether a pressure point is wild or not
		public Boolean isWildPoint (Double PSI, Double previousPSI)
		{
			if (previousPSI == null)//first frame or no valid prevValidPSI yet
			{
				previousPSI = PSI;
			}
			if (PSI < 0 || Math.abs(PSI - previousPSI) > 10)
			{
				return true;//not a valid PSI 
			}
			else
			{
				return false;//valid PSI
			}
			
		}//end isWildPoint
	
		
		//method to determine what value to extrapolate
	
		public void extrapolatePoints ()
		{
			//go through each frame
			for (DataFrame frame : frameList) 
			{
				if (frame.wildPoint)//frame has a pressure wild point
				{
					frame.wildPSI = frame.pressure; //store the wild point value in wildPSI
					if (frameList.indexOf(frame) == 0)//first frame - If a wild point occurs at the beginning of the stream,
						//replace it with the first valid value			
					{
						frame.pressure = findNextValidPSI(frameList, frameList.indexOf(frame));
					}
					else if (frameList.indexOf(frame) == frameList.size())//last frame - if a wild point 
						//occurs at the end of the stream, replace it with
						//the last valid value
					{
						//frame.pressure = findPrevValidPSI(frameList, frameList.indexOf(frame));
					}
					else //any other frame - Extrapolate the replacement value by computing the average of the last valid measurement and
						//the next valid measurement in the stream.
					{
						frame.pressure = (findPrevValidPSI(frameList, frameList.indexOf(frame))
								+ findNextValidPSI(frameList, frameList.indexOf(frame)))/2;
					}
				}			
				
			}//end for
		}//end extrapolatePoints
	
	
		//method used to find the next valid pressure point 
		//this is done by searching through all the frames after the index starting point of
		//the wild point
		public Double findNextValidPSI (ArrayList<DataFrame> frameList, int indexStartingPoint)
		{
			Double nextValidPSI = null;
			
			for (int i = indexStartingPoint+1; i < frameList.size(); i++) 
			{
				DataFrame frame = frameList.get(i);
				if (!frame.wildPoint)//not a wild point - good! Return that frame's PSI
				{
					nextValidPSI = frame.pressure;
					break; //exit for loop
				}
			}
			
			return (nextValidPSI);
					
		}//end findNextValidPSI
		
		
		//method used to find the previous valid pressure point 
		//this is done by searching through all the frames before the index starting point of
		//the wild point
		public Double findPrevValidPSI (ArrayList<DataFrame> frameList, int indexStartingPoint)
		{
			Double prevValidPSI = null;
					
			for (int i = indexStartingPoint-1; i >= 0; i--) 
			{
				DataFrame frame = frameList.get(i);
				if (!frame.wildPoint)//not a wild point - good! Return that frame's PSI
				{
					prevValidPSI = frame.pressure;
					break; //exit for loop
				}
			}
					
			return (prevValidPSI);
							
		}//end findPrevValidPSI
		
		
	public void run()
    {
		/************************************************************************************
		*	TimeStamp is used to compute time using java.util's Calendar class.
		* 	TimeStampFormat is used to format the time value so that it can be easily printed
		*	to the terminal.
		*************************************************************************************/

		Calendar TimeStamp = Calendar.getInstance();
		
		Double temp = null;
		Double prevValidPSI = null;
		Boolean isWild = null; //wild point
		Double pressure = null;
		Double alt = null;
		Double vel = null;
		Double att = null;
		String output = null; 
		
		//store all the data frames in array list
		//used for extrapolating values for wild points
		
		
		
		int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
		int IdLength = 4;				// This is the length of IDs in the byte stream

		byte databyte = 0;				// This is the data byte read from the stream
		int bytesread = 0;				// This is the number of bytes read from the stream

		long measurement;				// This is the word used to store all measurements - conversions are illustrated.
		long TS = 0; 						// timestamp stored as long
		int id;							// This is the measurement id
		int i;							// This is a loop counter

		/*************************************************************
		*	First we announce to the world that we are alive...
		**************************************************************/

		System.out.print( "\n" + this.getName() + "::PressureFilter Reading ");

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
					TS = measurement;

				} // if

				/****************************************************************************
				// Here we pick up measurements. All measurements in the stream are
				// decommutated by this class. Note that all data measurements are double types
				// This illustrates how to convert the bits read from the stream into a double
				// type. Its pretty simple using Double.longBitsToDouble(long value). So here
				// we print the time stamp and the data associated with the ID we are interested
				// in.
				****************************************************************************/
				//velocity
				if ( id == 1 )
				{
					vel = Double.longBitsToDouble(measurement);

				} // if
				
				//altitude
				if ( id == 2 )
				{
					alt = Double.longBitsToDouble(measurement);

				} // if
				
				//pressure
				if ( id == 3 )
				{
					pressure = ((Double.longBitsToDouble(measurement)));
					
					isWild = isWildPoint (pressure, prevValidPSI);
					//if the point is not wild, set prevValidPSI to current pressure
					if (!isWild)
					{
						prevValidPSI = pressure;
					}
					

				} // if
				
				
				//temperature
				if ( id == 4 )
				{
					temp = Double.longBitsToDouble(measurement);
				} // if

				//attitude
				if ( id == 5 )
				{
					att = Double.longBitsToDouble(measurement);
					//create a new data frame 
					DataFrame Frame = new DataFrame(TS, vel, alt, pressure, temp, att, isWild, null );
					//add a Frame to the array list of frames. We are essentially building a table
					//where each row is a frame and columns are the data measurements
					frameList.add(Frame);
					
					//System.out.print(output);
					//System.out.print( "\n" );
					/*
					output = TimeStampFormat.format(Frame.TimeStamp.getTime()) + "    " 
							+ TempFormat.format(Frame.temperature) + "    " + AltitudeFormat.format(Frame.altitude) 
							+ "    " + Frame.pressure + "    " + Frame.wildPoint;
					
					System.out.print(output);
					
					writer.println(output);
					 */

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
				extrapolatePoints();//perform pressure point extrapolation for wild points
				//printFrames();//used for testing only -> SinkFilter must be used in a live system
				pipeOutput();//pipe the output from Data Frames
				ClosePorts();


				System.out.print( "\n" + this.getName() + "::PressureFilter Exiting; bytes read: " + bytesread );
				break;

			} // catch

		} // while

   } // run

} // SingFilter