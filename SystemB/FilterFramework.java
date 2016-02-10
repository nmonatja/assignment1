/******************************************************************************************************************
* File:FilterFramework.java
* Course: 17655
* Project: Assignment 1
* Copyright: Copyright (c) 2003 Carnegie Mellon University
* Versions:
*	1.0 November 2008 - Initial rewrite of original assignment 1 (ajl).
*       1.1 Feb 07,2016 - N.Hoskeri : Updated class to have two input ports and 2 output ports
*                                     Any input port can be connected to any output port 
* Description:
*
* This superclass defines a skeletal filter framework that defines a filter in terms of the input and output
* ports. All filters must be defined in terms of this framework - that is, filters must extend this class
* in order to be considered valid system filters. Filters as standalone threads until the inputport no longer
* has any data - at which point the filter finishes up any work it has to do and then terminates.
*
* Parameters:
*
* InputReadPort:	This is the filter's input port. Essentially this port is connected to another filter's piped
*					output steam. All filters connect to other filters by connecting their input ports to other
*					filter's output ports. This is handled by the Connect() method.
*
* OutputWritePort:	This the filter's output port. Essentially the filter's job is to read data from the input port,
*					perform some operation on the data, then write the transformed data on the output port.
*
* FilterFramework:  This is a reference to the filter that is connected to the instance filter's input port. This
*					reference is to determine when the upstream filter has stopped sending data along the pipe.
*
* Internal Methods:
*
*	public void Connect( FilterFramework Filter )
*	public byte ReadFilterInputPort()
*	public void WriteFilterOutputPort(byte datum)
*	public boolean EndOfInputStream()
*
******************************************************************************************************************/

import java.io.*;

public class FilterFramework extends Thread
{
	// Define filter input and output ports

	private PipedInputStream InputReadPort_1 = new PipedInputStream();
	private PipedOutputStream OutputWritePort_1 = new PipedOutputStream();
        
    private PipedInputStream InputReadPort_2 = new PipedInputStream();
	private PipedOutputStream OutputWritePort_2 = new PipedOutputStream();

	// The following reference to a filter is used because java pipes are able to reliably
	// detect broken pipes on the input port of the filter. This variable will point to
	// the previous filter in the network and when it dies, we know that it has closed its
	// output pipe and will send no more data.

	private FilterFramework InputFilter_1 = null;
    private FilterFramework InputFilter_2 = null;
        

	/***************************************************************************
	* InnerClass:: EndOfStreamExeception
	* Purpose: This
	*
	*
	*
	* Arguments: none
	*
	* Returns: none
	*
	* Exceptions: none
	*
	****************************************************************************/

	class EndOfStreamException extends Exception {
		
		static final long serialVersionUID = 0; // the version for serializing

		EndOfStreamException () { super(); }

		EndOfStreamException(String s) { super(s); }

	} // class


	/***************************************************************************
	* CONCRETE METHOD:: Connect
	* Purpose: This method connects filters to each other. All connections are
	* through the inputport of each filter. That is each filter's inputport is
	* connected to another filter's output port through this method.
	*
	* Arguments:
	* 	FilterFramework - this is the filter that this filter will connect to.
        *       inPort : Input port of this filter
        *       outPort : Output port of the specified filter
        *       Filter : Filter whose output port is to be connected to this input port
	*
	* Returns: void
	*
	* Exceptions: IOException
	*
	****************************************************************************/

	void Connect( FilterFramework Filter, int outPort, int inPort )
	{
		try
		{
			// Connect this filter's input to the upstream pipe's output stream
                        
                        switch(inPort)
                        {    
                            case 1:
                            {
                                switch(outPort)
                                {
                                    case 1:
                                    {
                                        InputReadPort_1.connect( Filter.OutputWritePort_1 );
                                        InputFilter_1 = Filter;
                                        break;
                                    }
                                    case 2:
                                    {
                                        InputReadPort_1.connect( Filter.OutputWritePort_2 );
                                        InputFilter_1 = Filter;
                                        break;
                                    }
                                    default:
                                    {
                                        InputReadPort_1.connect( Filter.OutputWritePort_1 );
                                        InputFilter_1 = Filter;
                                        break;
                                    }
                                }
                                break;
                            }
                            case 2:
                            {
                                switch(outPort)
                                {
                                    case 1:
                                    {
                                        InputReadPort_2.connect( Filter.OutputWritePort_1 );
                                        InputFilter_2 = Filter;
                                        break;
                                    }
                                    case 2:
                                    {
                                        InputReadPort_2.connect( Filter.OutputWritePort_2 );
                                        InputFilter_2 = Filter;
                                        break;
                                    }
                                    default:
                                    {
                                        InputReadPort_2.connect( Filter.OutputWritePort_1 );
                                        InputFilter_2 = Filter;
                                        break;
                                    }
                                }
                                break;
                            }
                            default:
                            {
                                switch(outPort)
                                {
                                    case 1:
                                    {
                                        InputReadPort_1.connect( Filter.OutputWritePort_1 );
                                        InputFilter_1 = Filter;
                                        break;
                                    }
                                    case 2:
                                    {
                                        InputReadPort_1.connect( Filter.OutputWritePort_2 );
                                        InputFilter_2 = Filter;
                                        break;
                                    }
                                    default:
                                    {
                                        InputReadPort_1.connect( Filter.OutputWritePort_1 );
                                        InputFilter_1 = Filter;
                                        break;
                                    }
                                }
                            }
                        }
		} // try

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " FilterFramework error connecting::"+ Error );

		} // catch

	} // Connect
	
	/* another connect method to support the old stuff */
	void Connect( FilterFramework Filter) {
		Connect(Filter, 1, 1);
		/* port 1 is the default */
	}

	/***************************************************************************
	* CONCRETE METHOD:: ReadFilterInputPort
	* Purpose: This method reads data from the input port one byte at a time.
	*
	* Arguments: void
	*
	* Returns: byte of data read from the input port of the filter.
	*
	* Exceptions: IOExecption, EndOfStreamException (rethrown)
	*
	****************************************************************************/

	byte ReadFilterInputPort(int port_num) throws EndOfStreamException
	{
		byte datum = 0;
                PipedInputStream InputReadPort = InputReadPort_1;
                
                if(port_num == 1)
                {
                    InputReadPort = InputReadPort_1;
                }
                else if(port_num == 2)
                {
                    InputReadPort = InputReadPort_2;
                }

		/***********************************************************************
		* Since delays are possible on upstream filters, we first wait until
		* there is data available on the input port. We check,... if no data is
		* available on the input port we wait for a quarter of a second and check
		* again. Note there is no timeout enforced here at all and if upstream
		* filters are deadlocked, then this can result in infinite waits in this
		* loop. It is necessary to check to see if we are at the end of stream
		* in the wait loop because it is possible that the upstream filter completes
		* while we are waiting. If this happens and we do not check for the end of
		* stream, then we could wait forever on an upstream pipe that is long gone.
		* Unfortunately Java pipes do not throw exceptions when the input pipe is
		* broken.
		***********************************************************************/
		try
		{
			while (InputReadPort.available()==0 )
			{
				if (EndOfInputStream(port_num))
				{
					throw new EndOfStreamException("End of input stream reached");

				} //if

				sleep(250);

			} // while

		} // try

		catch( EndOfStreamException Error )
		{
			throw Error;

		} // catch

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " Error in read port wait loop::" + Error );

		} // catch

		/***********************************************************************
		* If at least one byte of data is available on the input
		* pipe we can read it. We read and write one byte to and from ports.
		***********************************************************************/

		try
		{
			datum = (byte)InputReadPort.read();
			return datum;

		} // try

		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " Pipe read error::" + Error );
			return datum;

		} // catch

	} // ReadFilterPort
	
	byte ReadFilterInputPort() throws EndOfStreamException {
		return ReadFilterInputPort(1);
	}

	/***************************************************************************
	* CONCRETE METHOD:: WriteFilterOutputPort
	* Purpose: This method writes data to the output port one byte at a time.
	*
	* Arguments:
	* 	byte datum - This is the byte that will be written on the output port.of
	*	the filter.
	*
	* Returns: void
	*
	* Exceptions: IOException
	*
	****************************************************************************/

	void WriteFilterOutputPort(int port_num, byte datum)
	{
                PipedOutputStream OutputWritePort = OutputWritePort_1;
                
                if(port_num == 1)
                {
                    OutputWritePort = OutputWritePort_1;
                }
                else if(port_num == 2)
                {
                    OutputWritePort = OutputWritePort_2;
                }
                
		try
		{
                        OutputWritePort.write((int) datum );
		   	OutputWritePort.flush();

		} // try

		catch( Exception Error )
		{
			System.out.println("\n" + this.getName() + " Pipe write error::" + Error );

		} // catch

		return;

	} // WriteFilterPort
	
	void WriteFilterOutputPort(byte datum) {
		WriteFilterOutputPort(1, datum);
	}

	/***************************************************************************
	* CONCRETE METHOD:: EndOfInputStream
	* Purpose: This method is used within this framework which is why it is private
	* It returns a true when there is no more data to read on the input port of
	* the instance filter. What it really does is to check if the upstream filter
	* is still alive. This is done because Java does not reliably handle broken
	* input pipes and will often continue to read (junk) from a broken input pipe.
	*
	* Arguments: void
	*
	* Returns: A value of true if the previous filter has stopped sending data,
	*		   false if it is still alive and sending data.
	*
	* Exceptions: none
	*
	****************************************************************************/

	private boolean EndOfInputStream(int filterNum)
	{
                FilterFramework InputFilter = InputFilter_1;
                if(filterNum == 1)
                {
                    InputFilter = InputFilter_1;
                }
                else if(filterNum == 2)
                {
                    InputFilter = InputFilter_2;
                }
		if (InputFilter.isAlive())
		{
			return false;

		} else {

			return true;

		} // if

	} // EndOfInputStream

	/***************************************************************************
	* CONCRETE METHOD:: ClosePorts
	* Purpose: This method is used to close the input and output ports of the
	* filter. It is important that filters close their ports before the filter
	* thread exits.
	*
	* Arguments: void
	*
	* Returns: void
	*
	* Exceptions: IOExecption
	*
	****************************************************************************/

	void ClosePorts()
	{
		try
		{
			InputReadPort_1.close();
            InputReadPort_2.close();
			OutputWritePort_1.close();
            OutputWritePort_2.close();

		}
		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " ClosePorts error::" + Error );

		} // catch

	} // ClosePorts
        
    void CloseInputPorts(int portNum)
	{
		try
		{
                    switch(portNum)
                    {
                        case 1:
                        {
                            InputReadPort_1.close();
                            break;
                        }
                        case 2:
                        {
                            InputReadPort_2.close();
                            break;
                        }
                        default:
                        {
                            break;
                        }
                            
                    }
		}
		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " CloseInputPorts error::" + Error );

		} // catch

	} // CloseInputPorts
        
    void CloseOutputPorts(int portNum)
	{
		try
		{
                    switch(portNum)
                    {
                        case 1:
                        {
                            OutputWritePort_1.close();
                            break;
                        }
                        case 2:
                        {
                            OutputWritePort_2.close();
                            break;
                        }
                        default:
                        {
                            break;
                        }
                            
                    }
		}
		catch( Exception Error )
		{
			System.out.println( "\n" + this.getName() + " CloseOutputPorts error::" + Error );

		} // catch

	} // CloseOutputPorts

	/***************************************************************************
	* CONCRETE METHOD:: run
	* Purpose: This is actually an abstract method defined by Thread. It is called
	* when the thread is started by calling the Thread.start() method. In this
	* case, the run() method should be overridden by the filter programmer using
	* this framework superclass
	*
	* Arguments: void
	*
	* Returns: void
	*
	* Exceptions: IOExecption
	*
	****************************************************************************/

	public void run()
    {
		// The run method should be overridden by the subordinate class. Please
		// see the example applications provided for more details.

	} // run

} // FilterFramework class
