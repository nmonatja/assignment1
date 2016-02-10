import java.util.Calendar;


public class DataFrame {

	//define data frame variables. These will hold the information from each frame
        Long TimeStamp;
	Double velocity;
	Double altitude;
	Double pressure;
	Double temperature;
	Double attitude; 
	Boolean wildPoint;//stores whether a frame has PSI wild point or not
	Double wildPSI;
	
	//constructor
	public DataFrame(int srcChan, Long TS, Double vel, Double alt, 
			Double press, Double temp, Double att, Boolean WP, Double wPSI) 
	{
                TimeStamp       = TS;
		velocity        = vel;
		altitude        = alt;
		pressure        = press;
		temperature     = temp;
		attitude        = att;
		wildPoint       = WP;
		wildPSI         = wPSI;
	}
	
	//default constructor
	public DataFrame ()
	{
                TimeStamp       = null;
		velocity        = null;
		altitude        = null;
		pressure        = null;
		temperature     = null;
		attitude        = null;
		wildPoint       = null;
		wildPSI         = null;
	}
	
}
