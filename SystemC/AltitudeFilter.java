/******************************************************************************************************************
* File:AltitudeFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*       1.1 N.Hoskeri, Feb,07,2016 - Modified to read from one channel and write to two channels
*
* Description:
*
*
* Parameters: 		None
*
* Internal Methods: None
*
******************************************************************************************************************/

public class AltitudeFilter extends FilterFramework
{
	public void run()
    {


		int bytesread = 0;					// Number of bytes read from the input file.
		int byteswritten = 0;				// Number of bytes written to the stream.
		byte databyte = 0;					// The byte of data read from the file

		// Next we write a message to the terminal to let the world know we are alive...

		System.out.print( "\n" + this.getName() + "::Altitude Reading ");

		while (true)
		{
			/*************************************************************
			*	Here we read a byte and write a byte
			*************************************************************/

			try
			{
				databyte = ReadFilterInputPort(1);
				bytesread++;
				WriteFilterOutputPort(1, databyte);
                                WriteFilterOutputPort(2, databyte);
				byteswritten++;

			} // try

			catch (EndOfStreamException e)
			{
				ClosePorts();
				System.out.print( "\n" + this.getName() + "::Middle Exiting; bytes read: " + bytesread + " bytes written: " + byteswritten );
				break;

			} // catch

		} // while

   } // run

} // AltitudeFilter