package edu.umbc.bft.beans;

import java.io.Serializable;

public class MessageToSend implements Serializable {

	private static final long serialVersionUID = 2439435952375090989L;
	
	private int id;
	private String data;
	private int nodeId;
	private long timeline;
	private int copies;


	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return this.id;
	}
	public String getData() {
		return this.data;
	}
	public int getNodeId() {
		return this.nodeId;
	}
	public long getTimeline() {
		return this.timeline;
	}
	public int getCopies() {
		return copies;
	}
	
	
	@Override
	public boolean equals(Object obj)	{
		if( obj instanceof MessageToSend )	{
			MessageToSend m = (MessageToSend)obj;
			return m.id == this.id; 
		}else
			return false;
	}//end of method
	

}
