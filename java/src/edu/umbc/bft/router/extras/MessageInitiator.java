package edu.umbc.bft.router.extras;

import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import edu.umbc.bft.beans.MessageToSend;
import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.beans.net.header.DefaultHeader;
import edu.umbc.bft.beans.net.payload.DataPayload;
import edu.umbc.bft.beans.net.payload.Payload;
import edu.umbc.bft.beans.net.route.Route;
import edu.umbc.bft.router.main.NetworkInterface;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

public class MessageInitiator implements Runnable {

	private Thread thread;
	private final int totalMessages;
	private int lastSN, secondLastSN;
	private NetworkInterface ninterface;
		
	public MessageInitiator(NetworkInterface ninterface)	{
		
		this.totalMessages = Router.getPropertyAsInteger("send.total");
		this.thread = new Thread(this);
		this.ninterface = ninterface;
		
		Random r = new Random();
		final int offset = 2 * Router.maxHops;
		this.secondLastSN = r.nextInt(offset);
		this.lastSN = this.secondLastSN + r.nextInt(offset);
		
	}//end of constructor
	
	public final void start()	{
		this.thread.start();
	}	
	
	@Override
	public void run()		{
		
		Gson gson = new Gson();
		
		for( int i=1; i<=this.totalMessages; i++ )		{
			
			String json = Router.getProperty("send.p"+ i);
			json = json!=null?json.trim():null;
			
			try {
			
				MessageToSend m = gson.fromJson(json, MessageToSend.class);
				m.setId(i);
				Logger.debug(this.getClass(), " MessageToSend parsed: "+ gson.toJson(m));				
				
				try {
					Logger.debug(this.getClass(), " Waiting for "+ m.getTimeline() +" millis to send ");
					Thread.sleep(m.getTimeline());
				}catch(InterruptedException e) {
					Logger.warn(this.getClass(), " Sleep interrupted; sending message before scheduled time..");
				}
				
				for( int j=0; j<m.getCopies(); j++ )	{
										
					Logger.info(this.getClass(), " Creating "+ (j+1) +" copy of the message "+ i );
					Datagram d = this.toDatagram(m);
					
					if( d != null )			{
						
						Router.startAckTimer(this.ninterface, d);
						this.ninterface.send(d);
						
						try {
							synchronized (Router.lock) {
								Logger.info(this.getClass(), " Waiting for the response... ");
								Router.lock.wait();
							}
						}catch(InterruptedException e) {
							Logger.warn(this.getClass(), e.getMessage() );
						}
						
					}else	{
						Logger.info(this.getClass(), " Unable to send datagram... ");
					}
					
					
				}//end of copy loop
				
			}catch(JsonSyntaxException e) {
				Logger.warn(this.getClass(), " Unable to parse MessageToSend : "+ json );
				Logger.info(this.getClass(), " Message "+ i +"not sent. ");
			}//end of try catch
			
		}//end of loop
		
	}//end of thread

	
	private Datagram toDatagram(MessageToSend m)		{
		
		DefaultHeader h = new DefaultHeader(Router.serverIP, Router.getNodeIP(m.getNodeId()));
		Route r = Router.findRoute(h);
		
		if( r!=null && r.length() > 0 )		{
			
			int seqNo = this.getNextSequenceNumber();
			h.setSequenceNo(seqNo);
			
			final int ackOffset = m.getNodeId() * Router.maxHops;
			Payload p = new DataPayload(m.getData(), seqNo+ackOffset);
			Datagram d = new Datagram(h, p);
			
			if( d.updateDatagram() ) {
				return d;
			}else	{
				Logger.error(this.getClass(), " Datagram creation failed");
				return null;
			}
			
		}else	{
			Logger.warn(this.getClass(), " No Route found... ");
			return null;
		}
		
	}//end of method
	
	
	/** Fibonacci Pattern to determine next sequence number */
	public int getNextSequenceNumber()		{
		int seqNo = this.secondLastSN + this.lastSN - 1;
		this.secondLastSN = this.lastSN;
		this.lastSN = seqNo;
		return seqNo;
	}//end of method
	
}
