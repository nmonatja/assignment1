/******************************************************************************************************************
* File:SinkFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*       1.1 Feb,07,2016 - Modified to read from each input filter and write to corresponding sink
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
import java.io.*; // note we must add this here since we use BufferedReader class to read from the keyboard


public class SinkFilter extends FilterFramework
{
    private String SinkFileName_1   = null;
    private DataOutputStream out_1  = null;			// File stream reference.
    private File file_1             = null;

    
	public void run()
    {
		/************************************************************************************
		*	TimeStamp is used to compute time using java.util's Calendar class.
		* 	TimeStampFormat is used to format the time value so that it can be easily printed
		*	to the terminal.
		*************************************************************************************/

		byte databyte       = 0;				// This is the data byte read from the stream
                int  bytesread      = 0;
                int  byteswritten   = 0;
		
		/*************************************************************
		*	First we announce to the world that we are alive...
		**************************************************************/

		System.out.print( "\n" + this.getName() + "::Sink Reading ");
                try
		{
                    /***********************************************************************************
                    *	Here we open the file to write.
                    ***********************************************************************************/
                    if(SinkFileName_1 != null)
                    {
                        file_1 = new File(SinkFileName_1);
                        // if file doesnt exists, then create it
                        if (!file_1.exists()) 
                        {
                             file_1.createNewFile();
                             System.out.println("\n" + this.getName() + "::Sink Creating file 1..." );
                        }

                        out_1 = new DataOutputStream(new FileOutputStream(file_1));
                        System.out.println("\n" + this.getName() + "::Sink writing file 1..." );
                    }
                }
                catch (IOException e)
                {
                  System.out.println("IOException : " + e);
                  
                  /*Clean up*/
                  out_1 = null;
                  ClosePorts();
                }

                if(out_1 !=null)
                {
                    while (true)
                    {
                        try
                        {
                            if(out_1 !=null)
                            {
                                databyte = ReadFilterInputPort(1);	// This is where we read the byte from the stream...
                                bytesread++;
                                out_1.write(databyte);                    //Write byte to sink file
                                byteswritten++;
                            }
                        } // try

                        /*******************************************************************************
                        *	The EndOfStreamExeception below is thrown when you reach end of the input
                        *	stream (duh). At this point, the filter ports are closed and a message is
                        *	written letting the user know what is going on.
                        ********************************************************************************/

                        catch (EndOfStreamException e)
                        {
                                try
                                {
                                    if(out_1 != null)
                                    {
                                        out_1.close();
                                    }
                                }
                                catch ( IOException iox )
                                {
                                        System.out.println("\n" + this.getName() + "::Problem reading input data file::" + iox );
                                } // catch

                                ClosePorts();
                                System.out.print( "\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread );
                                break;
                        } // catch
                        catch ( IOException iox )
                        {
                                System.out.println("\n" + this.getName() + "::Problem writing output data file::" + iox );

                        } // catch
                    } // while
                }

   } // run
    public void SetSink(String fileName)
    {
        SinkFileName_1 = fileName;
    } //SetSink

} // SingFilter