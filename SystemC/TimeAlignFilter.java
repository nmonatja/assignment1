/******************************************************************************************************************
* File:TimeAlignFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Sample Pipe and Filter code (ajl).
*       1.1 Feb 07,2016 - N.Hoskeri. Created
* Description:
*
* This filter merges inputs from two input ports, merges sorts the data based on timestamp and pipes the data out of the output port
*
* Parameters: 		None
*
* Internal Methods: None
*
******************************************************************************************************************/
import java.io.*;
import java.util.*;                                 		// This class is used to interpret time words
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;                              // This class is used to format and write time in a string format.
import java.text.DecimalFormat;

public class TimeAlignFilter extends FilterFramework
{
        private ArrayList<DataFrame>    DataframeList   = new ArrayList<DataFrame>();
        private Boolean                 DbgTraceOn      = true;
        
	public void run()
    {
		// Next we write a message to the terminal to let the world know we are alive...
		System.out.print( "\n" + this.getName() + "::Time Align Reading ");
                
                DbgTrace( "\n" + this.getName() + "::Processing Source 1");
                ProcessInputChannel(1);
                
                DbgTrace( "\n" + this.getName() + "::Processing Source 2");
                ProcessInputChannel(2);
                
                /*Sort Data Frame List*/
                DbgTrace( "\n" + this.getName() + "::Sorting merged frame list");
                Collections.sort(DataframeList, new DataFrameCompare());
                
                DbgTrace( "\n" + this.getName() + "::Piping out merge and sortedframe data" + "\n");
                
                
                /*Print console header*/
                DbgTrace(   "Source"        + "\t" +
                            "TimeStamp"     + "\t\t" +
                            "Velocity"      + "\t" +
                            "Altitude"      + "\t" +
                            "Pressure"      + "\t" +
                            "Temperature"   + "\t" +
                            "Attitude"
                        );
                        
                DbgTrace( "\n" );
                /*Pipe Out DataFrame array list*/
                
                for (DataFrame frame : DataframeList)
                {
                    /*if(frame.SourceChannel == 1)*/
                    {
                        PipeOutFrame( frame, 1);
                        
                        /*Print to console - works only if DbgTraceOn == true*/
                        PrintFrame(frame);
                    }
                }

                /*Close the output ports*/
                CloseOutputPorts(1);
                CloseOutputPorts(2);
		
   } // run
        
    public void ProcessInputChannel(int portNum)
    {
        byte databyte = 0;				// The byte of data read from input port 1
        int bytesread = 0;                             // Number of bytes read from input port 1
        int byteswritten = 0;
        
        int idLength = 4;
        int id = 0;
        long measurement = 0;
        int measurementLength = 8;
        
        Calendar TimeStamp = Calendar.getInstance();
        long TS = 0; 
        Double temp = null;
        Double pressure = null;
        Double alt = null;
        Double vel = null;
        Double att = null;
        
        Boolean isFirstFrame = true;
        
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

                switch(id)
                {
                    case 0:
                    {
                        /*This is a new frame, insert the previous frame in the list. Dont do it if it is the very first frame*/
                        if(!isFirstFrame)
                        {
                            DataFrame Frame  = new DataFrame(portNum, TS, vel, alt, pressure, temp, att, false, null );
                            DataframeList.add(Frame);
                        }
                        /*Reinitialize new frame variables*/
                        isFirstFrame = false;
                        temp        = null;
                        pressure    = null;
                        alt         = null;
                        vel         = null;
                        att         = null;
                        
                        TimeStamp.setTimeInMillis(measurement);
                        TS = measurement;
                        break;
                    }
                    case 1:
                    {
                        vel = Double.longBitsToDouble(measurement);
                        break;
                    }
                    case 2:
                    {
                        alt = Double.longBitsToDouble(measurement);
                        break;
                    }
                    case 3:
                    {
                        pressure = Double.longBitsToDouble(measurement);
                        break;
                    }
                    case 4:
                    {
                        temp = Double.longBitsToDouble(measurement);
                        break;
                    }
                    case 5:
                    {
                        att = Double.longBitsToDouble(measurement);

                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
                    
            }
            catch (EndOfStreamException e)
            {
                /*Insert Final frame*/
                DataFrame Frame  = new DataFrame(portNum, TS, vel, alt, pressure, temp, att, false, null );
                DataframeList.add(Frame);
                
                CloseInputPorts(portNum);

                DbgTrace( "\n" + this.getName() + "::Time Align Filter Exiting; bytes read from channel 1: " + bytesread + " bytes Written:" + byteswritten);
                break;
            } // catch

        } // while

    }
     
    ByteBuffer Serialize(Long data) 
    {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(0, data);
        return (buffer);
    }
    
    ByteBuffer Serialize(Integer data) 
    {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(0, data);
        return (buffer);
    }
    
    ByteBuffer Serialize(Double data) 
    {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(0, Double.doubleToRawLongBits(data));
        return (buffer);
    }
    
    void  PipeOutFrame(DataFrame frame, int outPort)
    {
        PipeOutMeasurement(frame, 0,  outPort);
        PipeOutMeasurement(frame, 1,  outPort);
        PipeOutMeasurement(frame, 2,  outPort);
        PipeOutMeasurement(frame, 3,  outPort);
        PipeOutMeasurement(frame, 4,  outPort);
        PipeOutMeasurement(frame, 5,  outPort);
        
    }

    void PipeOutMeasurement(DataFrame frame, Integer measurementID, int outPort)
    {
        ByteBuffer idBuffer             = null;
        ByteBuffer measurementBuffer    = null;
        Boolean pipeData                = false;
        
        idBuffer = Serialize(measurementID); 
        
        switch(measurementID)
        {
            case 0:
            {
                measurementBuffer = Serialize(frame.TimeStamp);
                pipeData = true;
                break;
            }
            case 1:
            {
                if(frame.velocity !=null)
                {
                    measurementBuffer = Serialize(frame.velocity);
                    pipeData = true;
                }
                
                break;
            }
            case 2:
            {
                if(frame.altitude !=null)
                {
                    measurementBuffer = Serialize(frame.altitude);
                    pipeData = true;
                }
                break;
            }
            case 3:
            {
                if(frame.pressure !=null)
                {
                    measurementBuffer = Serialize(frame.pressure);
                    pipeData = true;
                }
                break;
            }
            case 4:
            {
                if(frame.temperature !=null)
                {
                    measurementBuffer = Serialize(frame.temperature);
                    pipeData = true;
                }
                break;
            }
            case 5:
            {
                if(frame.attitude !=null)
                {
                    measurementBuffer = Serialize(frame.attitude);
                    pipeData = true;
                }
                break;
            }
            default:
            {
                measurementBuffer = Serialize(Double.longBitsToDouble(0));
                pipeData = false;
                break;
            }
        }
        
        if(pipeData)
        {
            PipeOutByteBuffer( idBuffer, outPort);
            PipeOutByteBuffer( measurementBuffer, outPort);           
        }
    }
    
    void PipeOutByteBuffer(ByteBuffer buffer, int outPort)
    {
        byte[] bufferArray = buffer.array();
        for (int i=0; i<bufferArray.length; i++)
        {
            WriteFilterOutputPort(outPort, bufferArray[i]);
        }
    }
    
    void PrintFrame(DataFrame frame)
    {
        SimpleDateFormat    timestampFormat = new SimpleDateFormat("yyyy:dd:hh:mm:ss");
        DecimalFormat       tempFormat      = new DecimalFormat("#000.00000");
        DecimalFormat       altitudeFormat  = new DecimalFormat("#000000.00000");
        DecimalFormat       pressureFormat  = new DecimalFormat("#000.00000");
        DecimalFormat       velocityFormat  = new DecimalFormat("#000.00000");
        DecimalFormat       attitudeFormat  = new DecimalFormat("#000.00000");

        String outputStr    = "***";
        String tsStr        = "***";
        String tempStr      = "***";
        String altStr       = "***";
        String pressStr     = "***";
        String velStr       = "***";
        String attStr       = "***";
        
        if(frame.TimeStamp !=null)
        {
            tsStr = timestampFormat.format(frame.TimeStamp);
        }
        if(frame.velocity !=null)
        {
            velStr = velocityFormat.format(frame.velocity);
        }
        if(frame.altitude !=null)
        {
            altStr = altitudeFormat.format(frame.altitude);
        }
        if(frame.pressure !=null)
        {
            pressStr = pressureFormat.format(frame.pressure);
        }
        
        if(frame.temperature !=null)
        {
            tempStr = tempFormat.format(frame.temperature);
        }
        if(frame.attitude !=null)
        {
            attStr = attitudeFormat.format(frame.attitude);
        }
        
        
        
        outputStr = frame.SourceChannel + "\t" + 
                    tsStr               + "\t" + 
                    velStr              + "\t" + 
                    altStr              + "\t" + 
                    pressStr            + "\t" + 
                    tempStr             + "\t" + 
                    attStr;
        
        DbgTrace(outputStr);
	DbgTrace( "\n" );
    }
    
    void DbgTrace(String output)
    {
        if(DbgTraceOn)
        {
            System.out.print(output);
        }
    }
            
            
    class DataFrameCompare implements Comparator<DataFrame>
    {

            @Override
            public int compare(DataFrame f1, DataFrame f2) 
            {
                    if(f1.TimeStamp < f2.TimeStamp)
                    {
                        return -1;
                    } 
                    else 
                    {
                        return 1;
                    }
            }
    }            
        
} // TimeAlignFilter