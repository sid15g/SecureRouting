package edu.umbc.bft.beans.net.payload;

import java.nio.ByteBuffer;

import edu.umbc.bft.router.engine.AckHandler;
import edu.umbc.bft.router.engine.Engine.MessageHandler;

public class AckPayload implements Response {
	
	/** sequence number of original packet */
	private long sequenceNum;
	
	public AckPayload(long seqNo) {
		this.sequenceNum = seqNo;
	}//end of method
	
	
	@Override
	public long getSequenceNum() {
		return this.sequenceNum;
	}
	@Override
	public boolean hasSignature() {
		return false;
	}
	@Override
	public byte[] toByteArray() {
		ByteBuffer bf = ByteBuffer.allocate(Long.BYTES);
		bf.putLong(this.sequenceNum);
		return bf.array();
	}

	@Override
	public MessageHandler getHandler() {
		return AckHandler.getInstance();
	}

}
