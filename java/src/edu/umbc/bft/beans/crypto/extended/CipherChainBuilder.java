package edu.umbc.bft.beans.crypto.extended;

import java.util.ArrayList;
import java.util.List;

import edu.umbc.bft.beans.crypto.Cipher;
import edu.umbc.bft.beans.net.Packet;
import edu.umbc.bft.beans.net.route.Route;
import edu.umbc.bft.router.main.DatagramFactory;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

public class CipherChainBuilder		{
	
	public static CipherChain build(Packet p)	{
		
		if( p.getHeader().isSecureMode() )	{
			return CipherChainBuilder.buildSignatureChain(p);
		}else	{
			return CipherChainBuilder.buildHmacChain(p);
		}
		
	}//end of method

	
	public static CipherChain buildHmacChain(Packet p)	{
		
		List<String> cipherTexts = new ArrayList<String>();
		String packet = DatagramFactory.hexString(p);
		Logger.debug(CipherChainBuilder.class, DatagramFactory.serialize(p) );
		
		try {
		
			Route r = (Route)p.getHeader().getRoute().clone();
			StringBuilder buffer = new StringBuilder(packet);
			
			while( r.hasNext() )	{
				
				String ip = r.next();
				String skey = Router.getSymmetricKeyFor(ip);
				Logger.debug(CipherChainBuilder.class, " Encrypting using key "+ skey +" for Node: "+ ip);
				Cipher c = new HMacNonNeighbors(skey.trim());
//				cipherTexts.add(c.encryprt(packet));
				String cipher = c.encryprt(buffer.toString());
				cipherTexts.add(cipher);
				buffer.append(cipher);
				
			}//end of loop
			
		}catch (CloneNotSupportedException e) {
			Logger.error(CipherChainBuilder.class, " Unable to create Cipher chain : "+ e.getMessage() );
		}
	
		return new CipherChain(cipherTexts);
		
	}//end of method
	
	
	public static CipherChain buildSignatureChain(Packet p)	{
		
		List<String> cipherTexts = new ArrayList<String>();
		/** Signature added during updateDatagram() */
		return new SignatureChain(cipherTexts);
		
	}//end of method
	
	
	public static CipherChain create(List<String> list)	{
		return new CipherChain(list);
	}
	
}