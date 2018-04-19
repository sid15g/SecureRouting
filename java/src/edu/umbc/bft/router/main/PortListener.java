package edu.umbc.bft.router.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Iterator;

import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.beans.net.header.FloodHeader;
import edu.umbc.bft.router.engine.Engine;
import edu.umbc.bft.util.Logger;

public class PortListener implements Runnable, NetworkInterface	{

	private Object lock;
	private Engine engine;
	private Thread thread;
	private DatagramSocket socket;
	private volatile boolean running;
	
	PortListener(int port) throws SocketException	{
		this.socket = new DatagramSocket(port);
		this.socket.setReceiveBufferSize(1024);
		this.socket.setSoTimeout(5000);				// 5 seconds
		this.lock = new Object();
		this.running = false;
	}//end of constructor

	public void setEngine(Engine engine) {
		this.engine = engine;
	}
	
	@Override
	public void run() {
		
		this.running = true;
		
		while( this.running )	{
			String data = this.receive();
			
			if( data != null )
				this.engine.process(data);
			
		}//end of loop
		
	}//end of method
	

	void start() {
		this.thread = new Thread(this);
		this.thread.start();
	}//end of method
	
	void stop() {
		this.running = false;
		this.socket.close();
		Logger.info(this.getClass(), " Port closed " );
	}//end of method
	
	
	private void send(DatagramPacket packet) {
		try {
			synchronized (this.lock) {
				this.socket.send(packet);
			}
		}catch(SocketException se)	{
			Logger.warn(this.getClass(), se.getMessage() );
		}catch(IOException e) {
			Logger.error(this.getClass(), e);
		}catch(Exception e) {
			Logger.error(this.getClass(), e);
		}//end of try catch
	}//end of method

	@Override
	public boolean sendTo(String serverIP, int port, String message)	{
		if( port!=-1 && port>0 )	{
			DatagramPacket pkt = DatagramFactory.createDatagramToSend(serverIP, port, message);
			synchronized (this.lock) {
				this.send(pkt);
			}
			return true;
		}else	{
			Logger.error(this.getClass(), "Unable to send message to "+ serverIP +":"+ port );
			return false;
		}
	}//end of method
	
	@Override
	public boolean send(Datagram message)	{
		String serverIP = message.getRoute().current();
		int port = Router.getNodePort(serverIP);
		if( port!=-1 && port>0 )	{
			DatagramPacket pkt = DatagramFactory.createDatagramToSend(serverIP, port, message);
			Logger.info(this.getClass(), " Sending to "+ serverIP +" @ "+ port );
			synchronized (this.lock) {
				this.send(pkt);
			}
			return true;
		}else {
			Logger.error(this.getClass(), "Unable to send datagram to "+ serverIP +":"+ port );
			return false;
		}
	}//end of method
	
	@Override
	public void flood(Datagram message)	{
		Iterator<String> iter = Router.getNodes().iterator();
		while( iter!=null && iter.hasNext() )	{
			String serverIP = iter.next();
			int port = Router.getNodePort(serverIP);
			if( port!=-1 && port>0 )	{
				Datagram newMsg = new Datagram(new FloodHeader(message.getHeader()), message.getPayload());
				DatagramPacket pkt = DatagramFactory.createDatagramToSend(serverIP, port, newMsg);
				synchronized (this.lock) {
					this.send(pkt);
				}
			}else	{
				Logger.error(this.getClass(), "Unable to flood packet to "+ serverIP +":"+ port );
			}
		}//end of loop
	}//end of method
	
	private String receive()	{
			try {
				
				DatagramPacket rdp = DatagramFactory.createEmptyDatagram();
				this.socket.receive(rdp);
				return new String(rdp.getData());
				
			}catch(SocketException se)	{
				Logger.warn(this.getClass(), se.getMessage() );
			}catch(SocketTimeoutException se)	{
				Logger.info(this.getClass(), " Nothing to Receive " );
			}catch(IOException e) {
				Logger.error(this.getClass(), e);
			}catch(Exception e) {
				Logger.error(this.getClass(), e);
			}//end of try catch
			
			return null;
	}//end of method
	
}
