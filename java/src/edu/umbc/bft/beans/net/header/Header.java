package edu.umbc.bft.beans.net.header;

import java.nio.ByteBuffer;

import edu.umbc.bft.beans.gson.Convertable;
import edu.umbc.bft.beans.net.route.Route;

public interface Header extends Convertable	{

	public Route getRoute();
	public String getSource();
	public String getDestination();	
	public long getSequenceNumber();
	public void setSecureMode(boolean mode);
	public void setRoute(Route r);
	
	public default byte[] toByteArray()	{
		byte[] r = getRoute().toByteArray();
		ByteBuffer bf = ByteBuffer.allocate(32 + r.length);
		bf.put(getSource().getBytes());
		bf.put(getDestination().getBytes());
		bf.putLong(getSequenceNumber());
		bf.put(isSecureMode()?(byte)1:(byte)0);
		bf.put(r);
		return bf.array();
	}//end of method
	
	public default boolean isSecureMode()	{
		return false;
	}
	
	@Override
	public default String getClassName() {
		return this.getClass().getName();
	}
	
}