package edu.umbc.bft.beans.net.header;

import edu.umbc.bft.beans.net.route.Route;

public class DefaultHeader implements Header	{

	private String source, destination;
	private boolean secureMode;
	private long sequenceNo;
	private Route route;

	public DefaultHeader(String srcIP, String destIP, long seqNumber) {
		this(srcIP, destIP);
		this.sequenceNo = seqNumber;
	}//End of constructor
	
	public DefaultHeader(String srcIP, String destIP) {
		this();
		this.source = srcIP;
		this.destination = destIP;
	}//End of constructor
	
	private DefaultHeader() {
		this.source = this.destination = null;
		this.secureMode = false;
		this.sequenceNo = 0L;
		this.route = null;
	}//End of constructor
	
	@Override
	public Route getRoute() {
		return this.route;
	}
	@Override
	public String getSource() {
		return this.source;
	}
	@Override
	public String getDestination() {
		return this.destination;
	}
	@Override
	public long getSequenceNumber() {
		return this.sequenceNo;
	}
	@Override
	public boolean isSecureMode() {
		return this.secureMode;
	}
	
	@Override
	public void setRoute(Route route) {
		this.route = route;
	}
	@Override
	public void setSecureMode(boolean secureMode) {
		this.secureMode = secureMode;
	}
	public void setSequenceNo(long sequenceNo) {
		this.sequenceNo = sequenceNo;
	}
	
}
