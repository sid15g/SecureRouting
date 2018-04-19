package edu.umbc.bft.beans.net.payload;

import java.nio.ByteBuffer;

import edu.umbc.bft.router.engine.DataHandler;
import edu.umbc.bft.router.engine.Engine.MessageHandler;

public class DataPayload implements Payload {

	private String data;
	private long ackSeqNo;
	private boolean secureReply;
	
	public DataPayload(String data, long ackSeqNum) {
		this.data = data;
		this.secureReply = false;
		this.ackSeqNo = ackSeqNum;
	}//end of constructor
	
	public String getData() {
		return this.data;
	}
	public long getAckSequenceNo() {
		return this.ackSeqNo;
	}
	public boolean isSecureReply() {
		return this.secureReply;
	}
	public void setSecureReply(boolean secureReply) {
		this.secureReply = secureReply;
	}
	
	@Override
	public boolean isCreateOnlySignature() {
		return false;
	}
	@Override
	public byte[] toByteArray() {
		ByteBuffer bf = ByteBuffer.allocate(Long.BYTES + this.data.length() + 1);
		bf.put(this.data.getBytes());
		bf.putLong(this.ackSeqNo);
		return bf.array();
	}

	@Override
	public MessageHandler getHandler() {
		return new DataHandler();
	}

}