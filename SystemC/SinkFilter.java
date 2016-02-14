/******************************************************************************************************************
* File:SinkFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*       1.1 Feb,07,2016 - Modified to read from each input filter and write to corresponding sink (NLH)
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
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


public class SinkFilter extends FilterFramework
{
    private String SinkFileName_1           = null;
    private DataOutputStream out_1          = null;			// File stream reference.
    private File file_1                     = null;
    Boolean WriteFormattedOuput             = true;
    
    Integer[]     paramFilterList           = {0,1,2,3,4,5};        //By default print all

    public SinkFilter() 
    {
        WriteFormattedOuput = true;
    }
    public void configParams(Integer[] paramList)
    {
        paramFilterList = paramList;
        WriteFormattedOuput = true;
    }
    
    public void configParams(Integer[] paramList, Boolean formattedOut)
    {
        paramFilterList = paramList;
        WriteFormattedOuput = formattedOut;
    }
    
    @Override
    public void run()
    {
        /************************************************************************************
        *	TimeStamp is used to compute time using java.util's Calendar class.
        * 	TimeStampFormat is used to format the time value so that it can be easily printed
        *	to the terminal.
        *************************************************************************************/

        int  bytesread      = 0;

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
                     System.out.println("\n" + this.getName() + "::Sink Creating file 1..." +  SinkFileName_1);
                }

                out_1 = new DataOutputStream(new FileOutputStream(file_1));
                System.out.println("\n" + this.getName() + "::Sink writing file 1..." + SinkFileName_1);
            }
        }
        catch (IOException e)
        {
          System.out.println("IOException : " + e);

          /*Clean up*/
          out_1 = null;
          ClosePorts();
        }
      
        try 
        {
            if(out_1 !=null)
            {
                if(WriteFormattedOuput == true)
                {
                    String headerStr = PrintHeader();
                    WriteByteBufToFile(Serialize(headerStr));
                }
                 
                ProcessInputChannel(1);
            }
            out_1.close();
        } 
        catch (IOException iox) 
        {
            System.out.println("\n" + this.getName() + "::Problem writing output data file:" + SinkFileName_1 + iox );
            ClosePorts();
            System.out.print( "\n" + this.getName() + "::Sink Exiting; bytes read: " + bytesread );
        }

    } // run
    public void SetSink(String fileName)
    {
        SinkFileName_1 = fileName;
    } //SetSink
    
    public void ProcessInputChannel(int portNum) throws IOException
    {
        byte databyte;				// The byte of data read from input port 1
        int bytesread = 0;                             // Number of bytes read from input port 1
        int byteswritten = 0;
        
        int idLength = 4;
        int id;
        long measurement;
        int measurementLength = 8;
        
        Boolean isFirstFrame = true;
        
        Frame f = new Frame(portNum);
        
        
        while (true)
        {
            /*************************************************************
            *	Here we parse the stream
            *************************************************************/
            try
            {
                id = 0;
                for (int i=0; i<idLength; i++ )
                {
                    databyte = ReadFilterInputPort(portNum);
                    bytesread++;

                    id = id | (databyte & 0xFF);                        // We append the byte on to ID...
                    if (i != idLength-1)                                // If this is not the last byte, then slide the
                    {                                                   // previously appended byte to the left by one byte
                        id = id << 8;                                    // to make room for the next byte we append to the ID
                    } // if
                } // for
                    
                /*Read the measurement*/
                measurement = 0;

                for (int i=0; i<measurementLength; i++ )
                {
                    databyte = ReadFilterInputPort(portNum);
                    bytesread++;

                    measurement = measurement | (databyte & 0xFF);	// We append the byte on to measurement...
                    if (i != measurementLength-1)			// If this is not the last byte, then slide the
                    {							// previously appended byte to the left by one byte
                        measurement = measurement << 8;			// to make room for the next byte we append to the
                    } // if
                } // if
                
                
                if(id == 0)
                {
                    /*This is a new frame, insert the previous frame in the list. Dont do it if it is the very first frame*/
                    if(!isFirstFrame)
                    {
                        /*Write the frame to the sink file*/
                        WriteFrameToFile(f);
                        
                        f = new Frame(portNum);
                    }
                    isFirstFrame = false;
                }
                Measurement m = new Measurement(id, measurement, true);
                
                /*Add the measurement to the frame*/
                f.add(m);
            }
                
            catch (EndOfStreamException e)
            {
                /*Insert Final frame to the sink file*/
                CloseInputPorts(portNum);
                
                WriteFrameToFile(f);

                DbgTrace( "\n" + this.getName() + "::Sink Filter Exiting; bytes read from channel 1: " + bytesread + " bytes Written:" + byteswritten);
                break;
            } // catch

        } // while

    }
    
    void  WriteFrameToFile(Frame frame) throws IOException
    {
        if(WriteFormattedOuput == true)
        {
            WriteFormattedFrameToFile(frame);
        }
        else
        {
            WriteRawFrameToFile(frame);
        }
    }
    void  WriteRawFrameToFile(Frame frame) throws IOException
    {
        ByteBuffer buffer = frame.Serialize();
        WriteByteBufToFile(buffer);
    }
    
    void WriteFormattedFrameToFile(Frame frame) throws IOException
    {
        String str = PrintFrame(frame);
        
        ByteBuffer buffer = ByteBuffer.allocate(str.length());
        buffer.put(str.getBytes());
        
        WriteByteBufToFile(buffer);
    }
    
    void  WriteByteBufToFile(ByteBuffer buffer) throws IOException
    {
        byte[] bufferArray = buffer.array();
        
        for (int i=0; i<bufferArray.length; i++)
        {
            out_1.write(bufferArray[i]);
        }
    }
    ByteBuffer Serialize(String str)
    {
        ByteBuffer buffer = ByteBuffer.allocate(str.length());
        buffer.put(str.getBytes());
        return (buffer);
    }
    
    String PrintHeader()
    {
        String headerStr = "";
        String underLine = "";
        /*
        =  "Time:"                     + "\t\t\t" +
                            "Velocity (knots/hr)"       + "\t" +
                            "Altitude(m)"               + "\t" +
                            "Pressure (psi)"            + "\t" +
                            "Temperature(C)"            + "\t" +
                            "Attitude"               + "\n";
        
        String underLine = ("====================================================================================================================\n");
        */
        for (Integer paramID : paramFilterList) 
        {
            switch(paramID)
            {
                case 0:
                {
                    headerStr += "Time:" + "\t\t\t";
                    underLine += "===================";
                    break;
                }
                case 1:
                {
                    headerStr += "Velocity (knots/hr)" + "\t";
                    underLine += "===================";
                    break;
                }
                case 2:
                {
                    headerStr += "Altitude(m)" + "\t";
                    underLine += "===================";
                    break;
                }
                case 3:
                {
                    headerStr += "Pressure (psi)" + "\t";
                    underLine += "===================";
                    break;
                }
                case 4:
                {
                    headerStr += "Temperature(C)" + "\t";
                    underLine += "===================";
                    break;
                }
                case 5:
                {
                    headerStr += "Attitude";
                    underLine += "===================";
                    break;
                }
                default:
                {
                    break;
                }
            }
        }
        return headerStr + "\n" + underLine + "\n";
    }
    String PrintFrame(Frame frame)
    {
        SimpleDateFormat    timestampFormat = new SimpleDateFormat("yyyy:dd:hh:mm:ss");
        DecimalFormat       tempFormat      = new DecimalFormat("#000.00000");
        DecimalFormat       altitudeFormat  = new DecimalFormat("#000000.00000");
        DecimalFormat       pressureFormat  = new DecimalFormat("#000.00000");
        DecimalFormat       velocityFormat  = new DecimalFormat("#000.00000");
        DecimalFormat       attitudeFormat  = new DecimalFormat("#000.00000");

        String outputStr    = "";
        String tsStr        = "***";
        String tempStr      = "***";
        String altStr       = "***";
        String pressStr     = "***";
        String velStr       = "***";
        String attStr       = "***";
        
        
        for(int i=0; i< frame.size();i++)
        {
            Measurement measurement = frame.get(i);
            
            switch(measurement.paramID)
            {
                case 0 : 
                {
                    tsStr = timestampFormat.format(measurement.paramVal);
                    break;
                }
                case 1 : 
                {
                    velStr = velocityFormat.format(new Double(Double.longBitsToDouble(measurement.paramVal)));
                    break;
                }
                case 2 : 
                {
                    altStr = altitudeFormat.format(new Double(Double.longBitsToDouble(measurement.paramVal)));
                    break;
                }
                case 3 : 
                {
                    pressStr = pressureFormat.format(new Double(Double.longBitsToDouble(measurement.paramVal)));
                    break;
                }
                case 4 : 
                {
                    tempStr = tempFormat.format(new Double(Double.longBitsToDouble(measurement.paramVal)));
                    break;
                }
                case 5 : 
                {
                    attStr = attitudeFormat.format(new Double(Double.longBitsToDouble(measurement.paramVal)));
                    break;
                }
                case 6:
                {
                    pressStr = pressureFormat.format(new Double(Double.longBitsToDouble(measurement.paramVal)));
                    pressStr +=  "*";
                    break;
                }
            }
        }
        for (Integer paramID : paramFilterList) 
        {
            switch(paramID)
            {
                case 0:
                {
                    outputStr += tsStr + "\t";
                    break;
                }
                case 1:
                {
                    outputStr += velStr + "\t\t";
                    break;
                }
                case 2:
                {
                    outputStr += altStr + "\t";                    
                    break;
                }
                case 3:
                {
                    outputStr += pressStr+ "\t";
                    break;
                }
                case 4:
                {
                    outputStr += tempStr+ "\t";
                    break;
                }
                case 5:
                {
                    outputStr += attStr+ "\t";
                    break;
                }
                default:
                {
                    break;
                }
            }
        }
        outputStr += "\n";
        
        DbgTrace(outputStr);
        
        return(outputStr);
    }
    
    void DbgTrace(String output)
    {
        if(DbgTraceOn)
        {
            System.out.print(output);
        }
    }

} // SingFilter