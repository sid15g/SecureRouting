package edu.umbc.bft.beans.crypto;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import edu.umbc.bft.util.Logger;

public class HMacWithSHA256 implements Cipher {

	private final String name;
	private Mac hasher;
	
	public HMacWithSHA256(String key) {
		
		this.name  = "HmacSHA256";
		
		try	{
			this.hasher = Mac.getInstance(this.name);
			SecretKey keySpec = new SecretKeySpec(key.trim().getBytes(charset), this.name);
			hasher.init(keySpec);
		}catch(NoSuchAlgorithmException e) {
			Logger.error(this.getClass(), e);
		}catch(Exception e){
			Logger.error(this.getClass(), e);
		}

	}//end of constructor
	
	
	@Override
	public String encryprt(String plainText) {
		byte[] sigArr = this.hasher.doFinal(plainText.getBytes(charset));		
		return new String(hexcoder.encode(sigArr));
	}//end of method

	@Override
	public boolean verify(String plainText, String cipherText) {
		return this.encryprt(plainText).equals(cipherText);
	}	
	
}
