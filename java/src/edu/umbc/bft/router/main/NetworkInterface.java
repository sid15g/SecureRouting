package edu.umbc.bft.router.main;

import edu.umbc.bft.beans.net.Datagram;

public interface NetworkInterface {
	
	public boolean send(Datagram message);
	public void flood(Datagram message);
	public boolean sendTo(String serverIP, int port, String message);
	
}
