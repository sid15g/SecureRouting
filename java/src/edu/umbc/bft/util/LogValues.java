package edu.umbc.bft.util;

public class LogValues 		{
	
	public static final int trace = 7;
	public static final int debug = 6;
	public static final int info  = 5;
	public static final int imp	  = 4;
	public static final int warn  = 3;
	public static final int error = 2;
	public static final int hist = 1;
	public static final int fatal = 0;
	
	
	public static int parse(String level)		{
		
		if( level == null )
			return LogValues.info;
		
		level = level.toLowerCase().trim();
		
		if( level.equals("trace") )		{
			return LogValues.trace;
		}else if( level.equals("debug") )	{
			return LogValues.debug;
		}else if( level.equals("info") )	{
			return LogValues.info;
		}else if( level.equals("imp") )		{
			return LogValues.imp;
		}else if( level.equals("warn") )	{
			return LogValues.warn;
		}else if( level.equals("error") )	{
			return LogValues.error;
		}else if( level.equals("hist") )	{
			return LogValues.hist;
		}else if( level.equals("fatal") )	{
			return LogValues.fatal;
		}else	{
			/** Default is INFO */
			return LogValues.info;
		}
		
	}//End Of Method

}//End Of Class
