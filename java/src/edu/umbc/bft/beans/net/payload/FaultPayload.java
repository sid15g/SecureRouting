package edu.umbc.bft.beans.net.payload;

import java.nio.ByteBuffer;

import edu.umbc.bft.router.engine.Engine.MessageHandler;
import edu.umbc.bft.router.engine.FaultHandler;

public class FaultPayload implements Payload {

	private String accuserNodeIp, suspectNodeIp;
	private long sequenceNum;
	
	public FaultPayload(String accuserNodeIp, String suspectNodeIp, long sequenceNum) {
		this.accuserNodeIp = accuserNodeIp;
		this.suspectNodeIp = suspectNodeIp;
		this.sequenceNum = sequenceNum;
	}//end of constructor
	
	
	public String getAccuserNodeIp() {
		return this.accuserNodeIp;
	}
	public String getSuspectNodeIp() {
		return this.suspectNodeIp;
	}
	public long getSequenceNum() {
		return this.sequenceNum;
	}

	@Override
	public boolean isCreateOnlySignature() {
		return true;
	}
	@Override
	public byte[] toByteArray() {
		ByteBuffer bf = ByteBuffer.allocate(Long.BYTES + this.accuserNodeIp.length() + this.suspectNodeIp.length() + 1);
		bf.put(this.accuserNodeIp.getBytes());
		bf.put(this.suspectNodeIp.getBytes());
		bf.putLong(this.sequenceNum);
		return bf.array();
	}

	@Override
	public MessageHandler getHandler() {
		return new FaultHandler();
	}

}
