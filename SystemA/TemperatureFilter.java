/******************************************************************************************************************
* File:TempuratureFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*
* Description:
* Based on MiddleFilter from the Sample
*
* Parameters: 		None
*
* Internal Methods: None
*
******************************************************************************************************************/

public class TemperatureFilter extends FilterFramework
{
	public void run()
    {


		int bytesread = 0;				// Number of bytes read from the input file.
		int byteswritten = 0;			// Number of bytes written to the stream.
		byte databyte = 0;				// The byte of data read from the file
		
		int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
		int IdLength = 4;				// This is the length of IDs in the byte stream
		long measurement;				// This is the word used to store all measurements - conversions are illustrated.
		int id;							// This is the measurement id
		int i;							// This is a loop counter

		// Next we write a message to the terminal to let the world know we are alive...

		System.out.print( "\n" + this.getName() + "::Temperature Reading ");

		while (true)
		{

			try
			{
				//databyte = ReadFilterInputPort();
				//bytesread++;
				//WriteFilterOutputPort(databyte);
				//byteswritten++;
				
				/*  First, grab the id and be sure to write it out to the stream */
				/* We know id is always first */
				
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
					
					WriteFilterOutputPort(databyte);	// write it back to the output port
					byteswritten++;

				} // for
				
				/* Then grab the measurement - we can't write this out to the file untill we know what id it is
				/* because we might need to transform it. For this filter we care about temperature, id = 4 */
				
				measurement = 0;
				
				if ( id == 4 )
				{
					// first, read the measurement
					
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

					} 
					
					// now let's transform the measurement
					
					// the sample code indicates that all measurements are of Double type so we need to convert 
					// the Long into a Double to make sure we have the right number
					Double tempF = Double.longBitsToDouble(measurement);
					Double tempC = ((tempF - 32)*5)/9;
					
					// I checked the values for tempF against what Sample1 printed - they are the same
					// I spot-checked the values for tempC by picking a few tempF and converting them via Google - the calculations are correct
					// TODO: Comment this out when we're actually done with SystemA and it's all working
					//System.out.print("\nOriginal (F): "+ tempF+" Converted (C): "+tempC);
					
					// now we write out the transformed measurement to the datastream
					// Converting tempC back into a long will cause us to loose some precision, 
					// but I think it needs to be done to write it out properly to the stream
					// fun fact: a long is ALWAYS 8 bytes in Java so that's why its okay to hard-code it
					
					long output = Double.doubleToRawLongBits(tempC); 
					System.out.print("\nOriginal (F): "+ tempF+" Converted (C): "+tempC+" Long: "+output);
					
					for (i = 7; i >= 0; i--) {
				        WriteFilterOutputPort((byte)(output & 0xFF));
						byteswritten++;
				        output >>= 8;
					}
					
				}
				else
				{
					// this is not temperature, we don't care about it, simply read it in and write it out.
					for (i=0; i<MeasurementLength; i++ )
					{
						databyte = ReadFilterInputPort();
						bytesread++;
						
						WriteFilterOutputPort(databyte);
						byteswritten++;
					}
				}			
				

			} // try

			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.print( "\n" + this.getName() + "::Temperature Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
				break;

			} // catch

		} // while

   } // run

} // TemperatureFilter