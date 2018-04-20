package edu.umbc.bft.beans.net.payload;

import edu.umbc.bft.beans.gson.Convertable;
import edu.umbc.bft.router.engine.Engine.MessageAdapter;

public interface Payload extends MessageAdapter, Convertable	{
	
	public boolean hasSignature();
	public byte[] toByteArray();
	
	@Override
	public default String getClassName() {
		return this.getClass().getName();
	}
	
}
