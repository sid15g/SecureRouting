package edu.umbc.bft.beans.timer;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.beans.net.payload.DataPayload;
import edu.umbc.bft.exceptions.InvalidClassInstanceException;
import edu.umbc.bft.router.main.NetworkInterface;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

public class TimerManager {
	
	/** IP -> timer manager */
	private Map<String, Timeout> map;
	private int networkTimeout;	
	private Timer timerManager;
	
	
	public TimerManager()	{
		this( Router.getPropertyAsInteger("network.timeout.millis") );
	}//end of constructor
	
	public TimerManager(int timeoutInMillis)	{
		this.timerManager = new Timer();
		this.networkTimeout = timeoutInMillis;
		this.map = new ConcurrentHashMap<String, Timeout>();		
	}//end of constructor
	
	
	public Timeout startAckTimer(NetworkInterface inf, Datagram d)	throws InvalidClassInstanceException	{
		
		if( d!=null && d.getPayload() instanceof DataPayload )		{
			
			final int hops = (int)(d.getHeader().getRoute().ttl() + 1.5);
			AckTimeout t = new AckTimeout(this.networkTimeout*hops);
			t.setNetworkInf(inf);
			t.setPacket(d);
			
			try	{
				
				Logger.info(this.getClass(), " Starting ACK timer of "+ t.getTimeoutInMillis() +" milli seconds for "+ d.print() );
				this.timerManager.schedule(t, t.getTimeoutInMillis());
				this.map.put(d.getSequenceKey(), t);
				
			}catch(IllegalArgumentException iae)	{
				Logger.error(this.getClass(), iae);
			}catch(IllegalStateException ise)	{
				Logger.error(this.getClass(), ise);
			}
			
			return t;
			
		}else	{
			throw new InvalidClassInstanceException( DataPayload.class, d.getPayload().getClass() );
		}
		
	}//end of method
	
	
	public boolean cancelAckTimer(Datagram d)	{
		
		Timeout t = this.map.get(d.getSequenceKey());

		if( t!=null && t instanceof AckTimeout )	{
			if( t.isDone()==false )
				return t.cancel();
		}else if( t!=null )	{
			Logger.error(this.getClass(), " Trying to stop invalid Timer object of type "+ t.getClass().getName() );
		}
		
		return false;
		
	}//end of method
	
	
	public boolean hasAckTimer(Datagram d) 	{

		Timeout t = this.map.get(d.getSequenceKey());

		if( t!=null && t instanceof AckTimeout )	{
			if( t.isDone()==false )
				return true;
		}
		return false;

	}//end of method
	
}