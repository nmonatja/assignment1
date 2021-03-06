/******************************************************************************************************************
* File:TemperatureFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*       1.1 Feb 07,2016 - Created Framework for Temperature filter (NLH)
* Description:
*
* This filter merges operates on the altitude measurements
*
* Parameters: 		None
*
* Internal Methods: None
*
******************************************************************************************************************/
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;                              // This class is used to format and write time in a string format.
import java.text.DecimalFormat;

public class TemperatureFilter extends FilterFramework
{
    @Override
    public void run()
    {
        // Next we write a message to the terminal to let the world know we are alive...
        System.out.print( "\n" + this.getName() + "::Temperature Filter Reading ");

        DbgTrace( "\n" + this.getName() + "::Processing Input Port 1");
        ProcessInputChannel(1);


        /*Close the output ports*/
        CloseOutputPorts(1);
        CloseOutputPorts(2);
		
    } // run
        
    void  FilterFrame(Frame frame)
    {
        final Integer temperatureParamID = 4;
       
        Measurement m = frame.find(temperatureParamID);
        
        Double tempF   = Double.longBitsToDouble(m.paramVal);
        Double tempC   = (tempF -32)*5/9;
        
        /*Convert to meters*/
        m.paramVal = Double.doubleToRawLongBits(tempC);
        
        /*Output through output port 1*/
        PipeOutFrame(frame, 1);
        
        /*Print frame for debugging*/
        DbgTrace(PrintFrame(frame));

    }            
        
    public void ProcessInputChannel(int portNum)
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

                    id = id | (databyte & 0xFF);                   // We append the byte on to ID...
                    if (i != idLength-1)                            // If this is not the last byte, then slide the
                    {                                               // previously appended byte to the left by one byte
                        id = id << 8;                           // to make room for the next byte we append to the ID
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
                        /*Filter the new frame*/
                        FilterFrame(f);
                        
                        f = new Frame(portNum);
                        
                    }
                    isFirstFrame = false;
                }
                Measurement m = new Measurement(id, measurement, true);
                
                /*Add the measurement to the frame*/
                f.add(m);
            }
            catch (FilterFramework.EndOfStreamException e)
            {
                /*Filter the final frame*/
                FilterFrame(f);

                
                CloseInputPorts(portNum);

                DbgTrace( "\n" + this.getName() + "::Temperature Filter Exiting; bytes read from channel 1: " + bytesread + " bytes Written:" + byteswritten);
                break;
            } // catch

        } // while

    }
    
    void  PipeOutFrame(Frame frame, int outPort)
    {
        ByteBuffer buffer = frame.Serialize();
        PipeOutByteBuffer( buffer, outPort);
    }
        
    void PipeOutByteBuffer(ByteBuffer buffer, int outPort)
    {
        byte[] bufferArray = buffer.array();
        for (int i=0; i<bufferArray.length; i++)
        {
            WriteFilterOutputPort(outPort, bufferArray[i]);
        }
    }
    
    ByteBuffer Serialize(String str)
    {
        ByteBuffer buffer = ByteBuffer.allocate(str.length());
        buffer.put(str.getBytes());
        return (buffer);
    }
    
    String PrintFrame(Frame frame)
    {
        SimpleDateFormat    timestampFormat = new SimpleDateFormat("yyyy:dd:hh:mm:ss");
        DecimalFormat       tempFormat      = new DecimalFormat("#000.00000");
        DecimalFormat       altitudeFormat  = new DecimalFormat("#000000.00000");
        DecimalFormat       pressureFormat  = new DecimalFormat("#000.00000");
        DecimalFormat       velocityFormat  = new DecimalFormat("#000.00000");
        DecimalFormat       attitudeFormat  = new DecimalFormat("#000.00000");

        String outputStr;
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
            }
        }
        
        outputStr = tsStr                   + "\t" + 
                    velStr                  + "\t\t" + 
                    altStr                  + "\t" + 
                    pressStr                + "\t" + 
                    tempStr                 + "\t" + 
                    attStr                  + "\n";
        
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

} // TimeAlignFilter