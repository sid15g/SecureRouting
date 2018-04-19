package edu.umbc.bft.beans.crypto;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.DecoderException;

import edu.umbc.bft.util.Logger;

public class ECDSA implements Cipher	{
	
	private PrivateKey privKey;
	private PublicKey pubKey;
	private Signature dsa;
	
	public ECDSA(String publickey, String secretkey) throws InvalidKeySpecException, DecoderException	{
		try	{
			this.privKey = this.parsePrivateKey(secretkey);
			this.pubKey = this.parsePublicKey(publickey);
			this.dsa = Signature.getInstance("SHA256withECDSA");
		}catch(NoSuchAlgorithmException nsae) {
			Logger.error(this.getClass(), nsae.getMessage() );
		}
	}//end of constructor
	
	public ECDSA()		{
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			keyGen.initialize(256, random);
			
			KeyPair pair = keyGen.generateKeyPair();
			this.privKey = pair.getPrivate();
			this.pubKey = pair.getPublic();
			this.dsa = Signature.getInstance("SHA256withECDSA");
			
		}catch(NoSuchAlgorithmException nsae) {
			Logger.error(this.getClass(), nsae.getMessage() );
		}
	}//end of constructor
	
	
	@Override
	public String encryprt(String plainText)	{

		try {
			this.dsa.initSign(this.privKey);
			
			byte[] arr = plainText.getBytes(charset);
			this.dsa.update(arr);

			byte[] realSig = this.dsa.sign();
			return new String(hexcoder.encode(realSig));
			
		}catch(InvalidKeyException ike) {
			Logger.error( this.getClass(), ike.getMessage() );
		}catch(SignatureException se) {
			Logger.error( this.getClass(), se.getMessage() );
		}catch(Exception e)	{
			Logger.error( this.getClass(), e.getMessage() );
		}
		
		return null;

	}//end of method
	
	
	@Override
	public boolean verify(String plainText, String cipherText)	{
		
		try {

			this.dsa.initVerify(this.pubKey);

			byte[] sigByte = this.hexDecode(cipherText);
			this.dsa.update(plainText.getBytes(charset));
			
			return this.dsa.verify(sigByte);
			
		}catch(InvalidKeyException ike) {
			Logger.error( this.getClass(), "InvalidKeyException: "+ ike.getMessage() );
		}catch(SignatureException se) {
			Logger.warn( this.getClass(), se.getMessage() );
		}catch(Exception e)	{
			Logger.error( this.getClass(), "Exception: "+e.getMessage() );
		}
		
		return false;
		
	}//end of method
	
	
	public static boolean verify(String publicKey, String plainText, String cipherText) throws Exception	{
		
		ECDSA e = new ECDSA();
		Signature dsa = Signature.getInstance("SHA256withECDSA");		
		PublicKey pkey = e.parsePublicKey(publicKey);
		dsa.initVerify(pkey);

		byte[] sigByte = cipherText.getBytes(charset);
		sigByte = Cipher.hexcoder.decode(sigByte);
		
		dsa.update(plainText.getBytes(charset));
		return dsa.verify(sigByte);
		
	}//end of method
	
	
	private PublicKey parsePublicKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException, DecoderException	{
		byte[] sigByte = this.hexDecode(key.trim());
	    X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(sigByte);
	    KeyFactory keyFactory = KeyFactory.getInstance("EC");
	    return keyFactory.generatePublic(publicSpec);
	}//end of method
	
	private PrivateKey parsePrivateKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException, DecoderException	{
		byte[] sigByte = this.hexDecode(key.trim());
	    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(sigByte);
	    KeyFactory keyFactory = KeyFactory.getInstance("EC");
	    return keyFactory.generatePrivate(keySpec);     
	}//end of method
	
}