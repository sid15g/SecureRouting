package edu.umbc.bft.router.extras;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;

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
	private RouteDiscovery dicovery;
	private NetworkInterface ninterface;
	private List<MessageToSend> messages;
	private int lastSN, secondLastSN;
		
	public MessageInitiator(NetworkInterface ninterface)	{
		
		final int total = Router.getPropertyAsInteger("send.total");
		this.messages = new ArrayList<MessageToSend>(total);
		this.thread = new Thread(this);
		this.ninterface = ninterface;
		Gson gson = new Gson();
		
		Random r = new Random();
		final int offset = 2 * Router.maxHops;
		this.secondLastSN = r.nextInt(offset);
		this.lastSN = this.secondLastSN + r.nextInt(offset);
		
		for( int i=1; i<=total; i++ )	{
			
			String json = Router.getProperty("send.p"+ i);
			MessageToSend m = gson.fromJson(json.trim(), MessageToSend.class);
			m.setId(i);
			Logger.debug(this.getClass(), " MessageToSend parsed: "+ gson.toJson(m));
			this.messages.add(m);
			
		}//end of loop
		
	}//end of constructor
	
	
	public final void start()	{
		this.thread.start();
	}
	public int getTotalMessages() {
		return this.messages.size();
	}
	public void setDicovery(RouteDiscovery dicovery) {
		this.dicovery = dicovery;
	}
	
	@Override
	public void run() {
		
		long lastMessageTime = 0L; 
		
		for( int i=0; i<this.messages.size(); i++ )	{
			
			MessageToSend m = this.messages.get(i);
			Datagram d = this.toDatagram(m);
			
			if( d != null )			{
				
				long diff = m.getTimeline() - lastMessageTime;
				lastMessageTime = m.getTimeline();
				
				try {
					Logger.debug(this.getClass(), "Waiting for "+ diff +" millis to send ");
					Thread.sleep(diff);
				}catch(InterruptedException e) {
					Logger.warn(this.getClass(), " Sleep interrupted; sending message before scheduled time..");
				}
				
//				NodeDetail nd = Router.getNodeDetails(m.getNodeId());
//				this.ninterface.sendTo(nd.getIp(), nd.getPort(), m.getData());
				Router.startAckTimer(this.ninterface, d);
				this.ninterface.send(d);
				
			}else	{
				Logger.info(this.getClass(), " Unable to send datagram... ");
			}
			
		}//end of loop
		
	}//end of thread

	
	private Datagram toDatagram(MessageToSend m) {
		
		DefaultHeader h = new DefaultHeader(Router.serverIP, Router.getNodeIP(m.getNodeId()));
		Route r = this.dicovery.find(Router.nodeID, m.getNodeId());
		
		if( r.length() > 0 )		{
			int seqNo = this.getNextSequenceNumber();
			h.setSequenceNo(seqNo);
			h.setRoute(r);
			
			final int ackOffset = m.getNodeId() * Router.maxHops;
			Payload p = new DataPayload(m.getData(), seqNo+ackOffset);
			Datagram d = new Datagram(h, p);
			
			if( d.updateDatagram() ) {
				return d;
			}else	{
				Logger.error(this.getClass(), "Datagram creation failed");
				return null;
			}
		}else	{
			Logger.warn(this.getClass(), "No Route found... ");
			return null;
		}
		
	}//end of method
	
	
	public int getNextSequenceNumber()		{
		int seqNo = this.secondLastSN + this.lastSN - 1;
		this.secondLastSN = this.lastSN;
		this.lastSN = seqNo;
		return seqNo;
	}//end of method
	
}
