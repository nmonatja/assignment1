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


		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		byte databyte = 0;					// The byte of data read from the file

		// Next we write a message to the terminal to let the world know we are alive...

		System.out.print( "\n" + this.getName() + "::Temperature Reading ");

		while (true)
		{
			/*************************************************************
			*	Here we read a byte and write a byte
			*************************************************************/

			int MeasurementLength = 8;		// This is the length of all measurements (including time) in bytes
			int IdLength = 4;				// This is the length of IDs in the byte stream

			long measurement;				// This is the word used to store all measurements - conversions are illustrated.
			int id;							// This is the measurement id
			int i;							// This is a loop counter


			try
			{
				//databyte = ReadFilterInputPort();
				//bytesread++;
				//WriteFilterOutputPort(databyte);
				//byteswritten++;
				
				/*  First, grab the id and be sure to write it out to the stream */
				
				id = 0;

				for (i=0; i<IdLength; i++ )
				{
					databyte = ReadFilterInputPort();	// This is where we read the byte from the stream...

					id = id | (databyte1 & 0xFF);		// We append the byte on to ID...

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
				System.out.print( "\n" + this.getName() + "::Middle Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
				break;

			} // catch

		} // while

   } // run

} // MiddleFilter