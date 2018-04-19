package edu.umbc.bft.beans.net;

import edu.umbc.bft.beans.net.header.Header;
import edu.umbc.bft.beans.net.payload.Payload;

public final class Packet {
	
	final private Header header;
	final private Payload payload;
	
	public Packet(Header header, Payload payload) {
		this.header = header;
		this.payload = payload;
	}//end of constructor

	public Header getHeader() {
		return this.header;
	}
	public Payload getPayload() {
		return this.payload;
	}
	
	public final String getSource()	{
		return this.getHeader().getSource();
	}
	
}
