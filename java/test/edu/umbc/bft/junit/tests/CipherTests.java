package edu.umbc.bft.junit.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import edu.umbc.bft.beans.crypto.Cipher;
import edu.umbc.bft.beans.crypto.ECDSA;
import edu.umbc.bft.beans.crypto.HMacWithSHA256;
import edu.umbc.bft.beans.crypto.MultiKeygenerator;
import edu.umbc.bft.beans.crypto.extended.HMacForNeighbor;
import edu.umbc.bft.beans.crypto.extended.HMacNonNeighbors;
import edu.umbc.bft.junit.categories.IntegratedTests;
import edu.umbc.bft.util.Logger;

@Category(IntegratedTests.class)
public class CipherTests {

	@Test
	public void testEcdsa()	{
		String str = "this is a random text";
		Cipher ds = new ECDSA();
		String ciphertext = ds.encryprt(str);
		Assert.assertEquals(ds.verify(str, ciphertext), true);
	}
	
	@Test
	public void testPureHMac()	{
		String str = "this is a random text";
		Cipher c = new HMacWithSHA256("1235461");
		String ciphertext = c.encryprt(str);
		Logger.info( this.getClass(), " HMAC | CipherText: "+ ciphertext );
		Assert.assertEquals(c.verify(str, ciphertext), true);
	}
	
	@Test
	public void testHMacExtensionForNeighbor()	{
		String str = "this is a random text";
		Cipher c = new HMacForNeighbor("1235461");
		String ciphertext = c.encryprt(str);
		Logger.info( this.getClass(), " HMACNeighbor | CipherText: "+ ciphertext );
		Assert.assertEquals(c.verify(str, ciphertext), true);
	}
	
	@Test
	public void testHMacExtensionForNonNeighbor()	{
		String str = "this is a random text";
		Cipher c = new HMacNonNeighbors("1235461");
		String ciphertext = c.encryprt(str);
		Logger.info( this.getClass(), " HMACNonNeighbor | CipherText: "+ ciphertext );
		Assert.assertEquals(c.verify(str, ciphertext), true);
	}
	
	@Test
	public void testKeyGenerationForECDSA() throws Exception {
		MultiKeygenerator mkg = new MultiKeygenerator();
		String[] key = mkg.generateECDSA();
		
		Cipher c = new ECDSA( key[1], key[0] );
		String ctext = c.encryprt("siddhant goenka");
		Assert.assertEquals(c.verify("siddhant goenka", ctext), true);
	}
	
	@Test
	public void testKeyGenerationForHMAC() throws Exception {
		MultiKeygenerator mkg = new MultiKeygenerator();
		String key = mkg.generateHMAC();
		
		Cipher c = new HMacForNeighbor(key);
		String ctext = c.encryprt("siddhant goenka");
		Assert.assertEquals(c.verify("siddhant goenka", ctext), true);
	}
	
	@Test
	public void testDigSig() throws Exception {
		
		String plain = "3132372e302e302e323132372e302e302e31000000000000002e013132372e302e302e323132372e302e302e333132372e302e302e3100000000000000000000000000000000001e00"; 
		String cipher = "3046022100e789d8564e2b24bf601cb473afc5637a91d62ef7ad914ecdb518b04506611935022100f381e033afca00cd786ab2434cc1b02cf5d8f844e41f3b64453ebf29813f18c1";
		
		String key = "3059301306072a8648ce3d020106082a8648ce3d030107034200041e248abec305f0b1126a151b833f9d9ae95423525c832d9888d41748be7bcc74d6b37d0b44ef627644bf1c11022cd5973f127c1e56f2ed8ac3e814c2bc920ca6";
		
		System.out.println(ECDSA.verify(key, plain, cipher));
		
	}
	
}
