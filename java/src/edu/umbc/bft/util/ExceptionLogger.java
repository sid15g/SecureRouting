package edu.umbc.bft.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExceptionLogger	{

	private static ExecutorService threadPool;
	private static final String basePath = "./logs/exceptions/";
	
	static	{
		ExceptionLogger.threadPool = Executors.newCachedThreadPool();
		File base = new File(ExceptionLogger.basePath);
		base.mkdirs();
	}//End Of Block
	
	
	public static void write(String filePrefix, Throwable stackTrace, String details)	{
		
		if( filePrefix==null || filePrefix.trim().isEmpty() || details==null || details.trim().isEmpty() || stackTrace==null )
			return;
		
		final long timestamp = System.currentTimeMillis();
		filePrefix = filePrefix.trim();
		
		
		String errorFile = filePrefix +"_"+ timestamp +".err";
		String detailFile = filePrefix +"_"+ timestamp +".log";
		
		Writer writer = new Writer(detailFile, details.trim());
		ExceptionLogger.threadPool.execute(writer);
		
		writer = new Writer(errorFile, Logger.getStack(stackTrace).trim());
		ExceptionLogger.threadPool.execute(writer);
		
	}//End Of Method
	
	
	public static void close()	{
		if( ExceptionLogger.threadPool!=null )	{
			ExceptionLogger.threadPool.shutdown();
			try {
				ExceptionLogger.threadPool.awaitTermination(5000, TimeUnit.MILLISECONDS);
			}catch(InterruptedException e) {}
		}
	}//End Of Method
	
	
	private static class Writer implements Runnable	{
		
		private File file;
		private StringReader reader;
		
		public Writer(String filename, String details) 	{
			this.file = new File(ExceptionLogger.basePath, filename);
			this.reader = new StringReader(details);
		}//End Of Constructor
		
		@Override
		public void run() {
			
			try 	{
				FileOutputStream fos = new FileOutputStream(this.file);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				
				int ch = this.reader.read();
				
				while( ch != -1 )		{
					
					bos.write(ch);
					ch = this.reader.read();
					
				}//End Of Loop
				
				bos.close();
				fos.close();
				
				this.reader.close();
				
			}catch(IOException ioe) {
				Logger.error( ExceptionLogger.class, " Unable to write exception in file "+ this.file.getName() +" :: "+ ioe.getMessage() );
			}catch(Exception e) {
				Logger.error( ExceptionLogger.class, " Error Writing Exception in file "+ this.file.getName() +" \n"+ Logger.getStack(e) );
			}//End Of Try Catch
			
		}//End Of Thread
		
	}//End Of Writer Class
	
}//End Of Looger Class
