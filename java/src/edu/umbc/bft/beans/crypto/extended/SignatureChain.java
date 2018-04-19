package edu.umbc.bft.beans.crypto.extended;

import java.util.List;

public class SignatureChain extends CipherChain {

	private static final long serialVersionUID = -8775733811930845721L;

	SignatureChain(List<String> ciphers) {
		super(ciphers);
	}//end of constructor
	
	public SignatureChain(CipherChain cc)	{
		super(cc.ciphers);
	}//end of constructor
	
	public boolean addToChain(String cipherText)		{
		if( super.current == 0 )	{
			this.ciphers.add(cipherText);
			return true;
		}
		return false;
	}//end of method
	
}
