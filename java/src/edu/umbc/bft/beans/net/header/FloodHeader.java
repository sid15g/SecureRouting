package edu.umbc.bft.beans.net.header;

import edu.umbc.bft.beans.net.route.Route;

public class FloodHeader implements Header {

	private Header header;
	
	public FloodHeader(Header header) {
		this.header = header;
	}//end of constructor

	@Override
	public Route getRoute() {
		return this.header.getRoute();
	}
	@Override
	public String getSource() {
		return this.header.getSource();
	}
	@Override
	public String getDestination() {
		return "255.255.255.255";
	}
	@Override
	public long getSequenceNumber() {
		return this.header.getSequenceNumber();
	}
	@Override
	public void setSecureMode(boolean mode) {
		this.header.setSecureMode(mode);
	}
	@Override
	public boolean isSecureMode() {
		return this.header.isSecureMode();
	}
	@Override
	public void setRoute(Route route) {
		this.header.setRoute(route);
	}
}
