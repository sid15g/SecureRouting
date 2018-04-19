package edu.umbc.bft.beans.timer;

import java.util.TimerTask;

import edu.umbc.bft.router.main.NetworkInterface;
import edu.umbc.bft.util.Logger;

public abstract class Timeout extends TimerTask		{
	
	private volatile boolean started, done;
	private NetworkInterface inf;
	private long timeoutInMilli;
		
	public Timeout(long timeoutInMillis) {
		super();
		this.started = this.done = false;
		this.timeoutInMilli = timeoutInMillis;
	}//end of constructor
	
	
	public void setNetworkInf(NetworkInterface inf) {
		this.inf = inf;
	}
	protected NetworkInterface getNetworkInf() {
		return this.inf;
	}
	public long getTimeoutInMillis() {
		return this.timeoutInMilli;
	}
	public boolean hasStarted()		{
		return this.started;
	}
	public boolean isDone()		{
		return this.done;
	}
	
	/** 
	 * ~~~Executes this method when Timer Expires~~~
	 * In case the processing is heavy, do not use this implementation [Check `ScheduledThreadPoolExecutor`]
	 * Timer class is single threaded - if once thread is delayed, all other waiting threads will be delayed
	 **/
	public final void run()	{
		this.started = true;
		try	{
			this.onTimeout();
		}catch(Exception e)	{
			Logger.error( this.getClass(), " Unable to start timer  \n"+ Logger.getStack(e) );
		}finally{
			this.done = true;
		}
	}//end of thread
	
	@Override
	public final boolean cancel() {
		Logger.info( this.getClass(), this.getClass().getSimpleName() +" Timer cancelled " );
		this.done = true;
		return super.cancel();
	}//End Of Method

	public abstract void onTimeout();	
	
}
