package edu.umbc.bft.beans.net.payload;

import java.nio.ByteBuffer;

import edu.umbc.bft.router.engine.AckHandler;
import edu.umbc.bft.router.engine.Engine.MessageHandler;

public class AckPayload implements Payload {
	
	/** sequence number of original packet */
	private long sequenceNo;
	
	public AckPayload(long seqNo) {
		this.sequenceNo = seqNo;
	}//end of method
	
	public long getSequenceNo() {
		return this.sequenceNo;
	}

	@Override
	public boolean isCreateOnlySignature() {
		return false;
	}
	@Override
	public byte[] toByteArray() {
		ByteBuffer bf = ByteBuffer.allocate(Long.BYTES);
		bf.putLong(this.sequenceNo);
		return bf.array();
	}

	@Override
	public MessageHandler getHandler() {
		return new AckHandler();
	}

}
