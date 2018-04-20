package edu.umbc.bft.router.engine;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.router.main.DatagramFactory;
import edu.umbc.bft.router.main.NetworkInterface;
import edu.umbc.bft.util.Logger;

public class MessageEngine implements Runnable, Engine	{
	
	private static Gson gson;
	
	private Thread thread;
	private boolean running;
	private Queue<Datagram> queue;
	private NetworkInterface listener;
	private Map<String, Datagram> buffer;
	
	static	{
		MessageEngine.gson = DatagramFactory.getGsonWithAdapter();
	}
	
	public MessageEngine() {
		this.buffer = new ConcurrentHashMap<String, Datagram>();
		this.queue = new LinkedBlockingQueue<Datagram>();
		this.thread = new Thread(this);
		this.running = false;
	}//end of constructor
	
	public void setListener(NetworkInterface listener) {
		this.listener = listener;
	}
	
	@Override
	public void process(String data)	{
		if( this.running == false )	{
			Logger.imp(this.getClass() , " Illegal state of engine to process | Check running state " );
		}else if( data!=null && data.length()>0 )	{
			try	{
				Datagram d = MessageEngine.gson.fromJson(data.trim(), Datagram.class);
				this.queue.add(d);
			}catch(JsonSyntaxException jse)	{
				Logger.debug(this.getClass(), " Unable to parse data | processing ignored ");
				Logger.info(this.getClass() , " UnParsed Message : "+ data );
			}
		}else	{
			Logger.debug(this.getClass(), " Null Message Ignored!");
		}
	}//end of method
	
	@Override
	public final void start()	{
		this.thread.start();
	}
	@Override
	public void stop()	{
		this.running = false;
	}
	
	@Override
	public void run() {
		
		this.running = true;
		
		while( this.running )	{
			
			Datagram dg = this.queue.poll();
			
			if(dg!=null && this.isRecent(dg) )	{
				
				Logger.info(this.getClass(), " Received: "+ dg.print() );
				Logger.debug(this.getClass(), " Datagram: "+ DatagramFactory.serialize(dg) );
				MessageHandler handler = dg.getPayload().getHandler();
				handler.handle(this.listener, dg);
				
			}else if(dg!=null)	{
				Logger.imp(this.getClass(), " UnExpected Sequence number | Datagram dropped from "+ dg.getSource() );
			}
			
		}//end of loop
		
		Logger.imp(this.getClass(), " Message Engine stopped..");
		
	}//end of thread
	
	
	private boolean isRecent(Datagram d)	{
		
		Datagram last = this.buffer.get(d.getSource());
		
		if( last!=null )	{
			boolean recent = last.getHeader().getSequenceNumber() < d.getHeader().getSequenceNumber();
			if( recent )
				this.buffer.put(d.getSource(), d);
			return recent;
		}else
			return true;
		
	}//end of method
	
}