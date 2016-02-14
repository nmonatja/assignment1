/******************************************************************************************************************
* File:TimeAlignFilter.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Created Model for Measurement, Frames and Frame List (NLH)
* Description:
*
* Class for creating and operating on measurements, frames and frame lists
*
* Parameters: 		None
*
* Internal Methods: None
*
******************************************************************************************************************/


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author FZ4432
 */
    import java.util.ArrayList;
    import java.nio.ByteBuffer;
    import java.util.Collections;
    import java.util.Comparator;
    import java.util.Objects;
    
    class Measurement
    {
        Integer paramID;
        Long    paramVal;
        Boolean validity;
        
        Measurement(Integer id, Long value, Boolean isValid)
        {
            paramID     = id;
            paramVal    = value;
            validity    = isValid;
        }
        
        Measurement(Integer id, Double value, Boolean isValid)
        {
            paramID     = id;
            paramVal = Double.doubleToRawLongBits(value);
            validity    = isValid;
        
        }
        Measurement()
        {
           paramID  = -1;
           paramVal = new Long(0);
           validity = false;
        }
        
        public void clear()
        {
            paramID  = -1;
            paramVal = new Long(0);
            validity = false;
        }
        
        public Boolean IsValid()
        {
            return validity;
        }
        
        public void SetValidity(Boolean isValid)
        {
            validity = isValid;
        }
        
        public ByteBuffer Serialize()
        {
             ByteBuffer buffer      = ByteBuffer.allocate(Integer.BYTES+Long.BYTES);
             ByteBuffer paramIDBuf  = ByteBuffer.allocate(Integer.BYTES);
             ByteBuffer paramValBuf = ByteBuffer.allocate(Long.BYTES);
             
             
             paramIDBuf.putInt(0, paramID);     /*Serialize ID*/
             paramValBuf.putLong(0, paramVal);  /*Serialize value*/
             
             /*Concatenate the two buffers, ID first followed by the parameter value*/
             buffer.put(paramIDBuf);
             buffer.put(paramValBuf);
             
             return (buffer);
        }
        static int size()
        {
            return (Integer.BYTES+Long.BYTES); /*Return the size of ID and parameter value*/
            
        }
                
    }
    
    class Frame
    {
        ArrayList<Measurement>  MeasurementList   = new ArrayList<Measurement>();
        int                     SourceChannel     = 1;    
        
        public Frame(int srcChan)
        {
            SourceChannel = srcChan;
        }
        public Frame(Measurement measurement, int srcChan)
        {
            MeasurementList.add(measurement);
            SourceChannel = srcChan;
        }
        
        public void add(Measurement measurement)
        {
            MeasurementList.add(measurement);
        }
        
        public Measurement get(int index)
        {
            Measurement measurement = MeasurementList.get(index);

            return (measurement);
        }
        
        public int getSourceChannel()
        {
            return (SourceChannel);
        }
        
        public void set(int index, Measurement measurement)
        {
            MeasurementList.set(index, measurement);
        }
        
        public void clear()
        {
            MeasurementList.clear();
        }
        
        public int size()
        {
            return (MeasurementList.size());
        }
        
        ByteBuffer Serialize()
        {
            ByteBuffer buf = ByteBuffer.allocate(MeasurementList.size() * Measurement.size());
            for (Measurement measurement : MeasurementList)
            {
                ByteBuffer measBuf = measurement.Serialize();
                buf.put(measBuf.array());
            }
            return (buf);
        }
        public Measurement find(Integer paramID)
        {
            /*Finds the first measurement with the requested paramID*/
            Measurement measurement = null;
            
            for (Measurement m : MeasurementList)
            {
                if(Objects.equals(m.paramID, paramID))
                {
                    measurement =  m;
                    break;
                }
            }
            return (measurement);
        }
       
    }
    
    class FrameList
    {
        ArrayList<Frame> FrameList = new ArrayList<Frame>();
        Integer          SortParamID = 0;
        
        public FrameList()
        {
        
        }
        public FrameList(Frame frame)
        {
            FrameList.add(frame);
        }
        
        public void add(Frame frame)
        {
            FrameList.add(frame);
        }
        
        public Frame get(int index)
        {
            Frame frame = FrameList.get(index);

            return (frame);
        }
        
        public void set(int index, Frame frame)
        {
            FrameList.set(index, frame);
        }
        
        public void clear()
        {
            FrameList.clear();
        }
        
        public int size()
        {
            return(FrameList.size());
        }
        
        ByteBuffer SerializeFrame(int index)
        {
            Frame frame = FrameList.get(index);
            
            return frame.Serialize();
        }
        
        void Sort(Integer paramID)
        {
            SortParamID = paramID;
            Collections.sort(FrameList, new FrameCompare());
        }
        
        Measurement FindNextValidMeasurement(Integer paramID, int index)
        {
            /*Find the next valid measurement for a given parameter ID in the frames in the framelist after the given index*/
            Measurement measurement = null;
            
            for(int i=index+1;i< this.size();i++)
            {
                Frame frame = this.get(i);
                Measurement m = frame.find(paramID);
                if(m.validity)
                {
                    measurement = m;
                    break;
                }
            }
            return(measurement);
        }
        
        Measurement FindPrevValidMeasurement(Integer paramID, int index)
        {
            /*Find the next valid measurement for a given parameter ID in the frames in the framelist after the given index*/
            Measurement measurement = null;
            
            for(int i=index-1;i>=0;i--)
            {
                Frame frame = this.get(i);
                Measurement m = frame.find(paramID);
                if(m.validity)
                {
                    measurement = m;
                    break;
                }
            }
            return(measurement);
        }
        
        class FrameCompare implements Comparator<Frame>
        {
            @Override
            public int compare(Frame f1, Frame f2) 
            {
                /*Locate measurements in the frame with the desired measurement id*/
                Measurement m1 = f1.find(SortParamID);
                Measurement m2 = f2.find(SortParamID);
                
                if(Double.longBitsToDouble(m1.paramVal) < Double.longBitsToDouble(m2.paramVal))
                {
                    return -1;
                } 
                else 
                {
                    return 1;
                }
            }
        } 
        
    }
