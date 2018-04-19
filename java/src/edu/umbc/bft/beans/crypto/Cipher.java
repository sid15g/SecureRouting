package edu.umbc.bft.beans.crypto;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public interface Cipher {
	
	final Charset charset = StandardCharsets.ISO_8859_1;
	final Hex hexcoder = new Hex(charset);
	
	public String encryprt(String plainText);
	public boolean verify(String plainText, String cipherText);
	
	public default byte[] hexEncode(String str)	{
		byte[] arr = str.getBytes(charset);
		return hexcoder.encode(arr);
	}//end of method
	
	public default byte[] hexDecode(String str) throws DecoderException	{
		byte[] sigByte = str.getBytes(charset);
		return hexcoder.decode(sigByte);
	}//end of method
	
}
