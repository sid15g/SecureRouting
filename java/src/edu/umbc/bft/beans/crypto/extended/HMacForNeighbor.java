package edu.umbc.bft.beans.crypto.extended;

import edu.umbc.bft.beans.crypto.HMacWithSHA256;

public class HMacForNeighbor extends HMacWithSHA256 {

	private final String nuance = "0";
	
	public HMacForNeighbor(String key) {
		super(key);
	}//end of constructor
	
	@Override
	public String encryprt(String plainText) {
		return super.encryprt(this.nuance + plainText);
	}
	
}
