package edu.umbc.bft.router.engine;

import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.router.main.NetworkInterface;

public interface Engine {
	
	public interface MessageAdapter	{
		public MessageHandler getHandler();
	}
	
	public interface MessageHandler {
		public void handle(NetworkInterface inf, Datagram dg);
	}
	
	public void process(String data);
	public void start();
	public void stop();

}
