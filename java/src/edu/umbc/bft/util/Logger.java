package edu.umbc.bft.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class Logger 		{

	public static void fatal(Class<? extends Object> c, String message) {
		getLogger(c.getName()).fatal(message);
	}
	public static void fatal(Class<? extends Object> c, Throwable t) {
		getLogger(c.getName()).fatal(Logger.getStack(t));
	}
	
	public static void hist(Class<? extends Object> c, String message) {
		getLogger(c.getName()).log(MyLevel.HIST,message);
	}
	public static void hist(Class<? extends Object> c, Throwable t) {
		getLogger(c.getName()).log(MyLevel.HIST, Logger.getStack(t));
	}
	
	public static void error(Class<? extends Object> c, String message) {
		getLogger(c.getName()).error(message);
	}
	public static void error(Class<? extends Object> c, Throwable t) {
		getLogger(c.getName()).error(Logger.getStack(t));
	}
	
	public static void warn(Class<? extends Object> c, String message) {
		getLogger(c.getName()).warn(message);
	}
	public static void warn(Class<? extends Object> c, Throwable t) {
		getLogger(c.getName()).warn(Logger.getStack(t));
	}
	
	public static void imp(Class<? extends Object> c, String message) {
		getLogger(c.getName()).log(MyLevel.IMP, message);
	}
	public static void imp(Class<? extends Object> c, Throwable t) {
		getLogger(c.getName()).log(MyLevel.IMP, Logger.getStack(t));
	}
	
	public static void info(Class<? extends Object> c, String message) {
		getLogger(c.getName()).info(message);
	}
	public static void info(Class<? extends Object> c, Throwable t) {
		getLogger(c.getName()).info(Logger.getStack(t));
	}
	
	public static void debug(Class<? extends Object> c, String message) {
		getLogger(c.getName()).debug(message);
	}
	public static void debug(Class<? extends Object> c, Throwable t) {
		getLogger(c.getName()).debug(Logger.getStack(t));
	}
	
	public static void trace(Class<? extends Object> c, String message) {
		getLogger(c.getName()).trace(message);
	}
	public static void trace(Class<? extends Object> c, Throwable t) {
		getLogger(c.getName()).trace(Logger.getStack(t));
	}
	
	
	private static org.apache.log4j.Logger getLogger(String applicationName)	{
		org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(applicationName);
		return log;
	}

	public static String getStack( Throwable E )		{
		String result = "There was some error creating the Stack Frame";

		try        {

			ByteArrayOutputStream Stack = new ByteArrayOutputStream();
			PrintStream PrintStack = null;

			if( Stack != null )		{
				PrintStack = new PrintStream( Stack );

				if( PrintStack != null )				{
					E.printStackTrace( PrintStack );
					result = Stack.toString();
				}

			}
		}catch( Exception e )        {
			result += ( "\r\n\t"+ e.toString() );
		}
		return "\r\n\t"+result.trim();
	}//End Of Method

	public static void setLogLevel(int level)		{
		
		try		{
			switch(level)		{
				case LogValues.trace:
					setLogLevel(Level.TRACE);
					break;					
				case LogValues.debug:
					setLogLevel(Level.DEBUG);
					break;
				case LogValues.info:
					setLogLevel(Level.INFO);
					break;
				case LogValues.imp:
					setLogLevel(MyLevel.IMP);
					break;					
				case LogValues.warn:
					setLogLevel(Level.WARN);
					break;
				case LogValues.error:
					setLogLevel(Level.ERROR);
					break;
				case LogValues.hist:
					setLogLevel(MyLevel.HIST);
					break;						
				case LogValues.fatal:
					setLogLevel(Level.FATAL);
					break;	
			}			
		}catch (Exception e) 	{
			System.err.println("Error while change log level: "+level);
		}//End Of Try Catch		 
	}//End Of Method

	
	private static void setLogLevel(Level level)		{
		
		LogManager.getRootLogger().setLevel(level);
		
		@SuppressWarnings("unchecked")
		Enumeration<Category> allLoggers = LogManager.getRootLogger().getLoggerRepository().getCurrentCategories();
		
		while (allLoggers.hasMoreElements())		{
			Category category = (Category) allLoggers.nextElement();
			category.setLevel(level);			
		}//End Of Loop
		
	}//End Of Method

	
}//End Of Class


final class MyLevel extends Level 	{
    
	private static final long serialVersionUID = 2751778514843177729L;
	private static final int IMP_INT = (INFO_INT+WARN_INT)/2;				/** In between INFO and WARN */
	private static final int HIST_INT = (FATAL_INT+ERROR_INT)/2;			/** In between FATAL and ERROR */
	
	public static final Level IMP = new MyLevel(IMP_INT, "IMP", 5);
	public static final Level HIST = new MyLevel(HIST_INT, "HIST", 1);
	

	private MyLevel(int level, String levelStr, int syslogEquivalent) {
		super(level, levelStr, syslogEquivalent);
	}//End Of Constructor
	
}//End Of Class
