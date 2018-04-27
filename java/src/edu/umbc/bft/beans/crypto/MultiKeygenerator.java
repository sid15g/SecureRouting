package edu.umbc.bft.beans.crypto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

public class MultiKeygenerator {
	
	public static enum cipher	{
		
		HMAC("HmacSHA256"),
		ECDSA("EC"),
		BOTH("");
		
		private String name;
		
		private cipher(String name)	{
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
	}//end of enum		
	
	public String generateHMAC() throws NoSuchAlgorithmException, UnsupportedEncodingException {

		KeyGenerator skeyGen = KeyGenerator.getInstance(cipher.HMAC.getName());
		SecretKey skey = skeyGen.generateKey();
		byte[] key = skey.getEncoded();
		byte[] newarr = Cipher.hexcoder.encode(key);
		return new String(newarr, Cipher.charset);
		
	}//end of method
	
	
	public String[] generateECDSA() throws NoSuchAlgorithmException, UnsupportedEncodingException {

		KeyPairGenerator askeyGen = KeyPairGenerator.getInstance(cipher.ECDSA.getName());
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		askeyGen.initialize(256, random);
		KeyPair pair = askeyGen.generateKeyPair();
		
		String[] res = new String[2];
		
		res[0] = new String(Cipher.hexcoder.encode(pair.getPrivate().getEncoded()) );
		res[1] = new String(Cipher.hexcoder.encode(pair.getPublic().getEncoded()) );

		return res;
		
	}//end of method

	

	public void generateAndSave(cipher c, String filepath, List<String> list)	{
		
		File f = new File(filepath);
		
		try(
				FileWriter writer = new FileWriter(f);
				BufferedWriter buff = new BufferedWriter(writer);
		) {			
			
			for(int i=0; i<list.size(); i++ )		{
				
				try {
					
					switch(c)	{
						case HMAC:
							String key = this.generateHMAC();
							buff.write(key);
							break;
						case ECDSA:
							String[] arr = this.generateECDSA();
							buff.write(arr[0]);
							buff.write('\n');
							buff.write(arr[1]);
							break;
						case BOTH:
							String ip = list.get(i);
							String[] sigs = this.generateECDSA();
							buff.write(ip+".private.key=");
							buff.write(sigs[0]);
							buff.write('\n');
							buff.write(ip+".public.key=");
							buff.write(sigs[1]);
							for( int j=i+1; j<list.size(); j++ )	{
								String ip2 = list.get(j);
								String skey = this.generateHMAC();
								buff.write('\n');
								buff.write(ip+".and."+ ip2 +"=");
								buff.write(skey);
							}
							break;							
						default:
							Logger.info(this.getClass(), " Key Generation not defined yet ");
					}
					
					buff.write('\n');
					
				}catch (NoSuchAlgorithmException e){
					Logger.debug(this.getClass(), "Generating failed ... retrying ! ");
					i--;
				}
				
			}//end of loop
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}//end of method
	
	public void configure(Set<String> ips, String path) throws Exception {
		
		List<String> list = new ArrayList<String>(ips);
		
		for(int i=0; i<list.size(); i++ )		{
			int x = list.indexOf(String.valueOf(i+1));
			list.remove(x);
		}
		
		MultiKeygenerator mkg = new MultiKeygenerator();
		mkg.generateAndSave(cipher.BOTH, path.trim(), list);
		
	}//end of method
	
	
	public static void main(String[] args) {
		
		Router.load();
		
		/** 
		 * Generate Keys for all nodes and save it in a file,
		 * assuming this way to be secure and no node is accessing other nodes private key
		 **/
		File keyMap = new File("resource", "keys.map");
		
		if( keyMap.exists() == false )	{
			try {
				MultiKeygenerator mkg = new MultiKeygenerator();
				mkg.configure(Router.getNodes(), keyMap.getPath());
			} catch (Exception e1) {}
		}//end of key generation
		
		System.out.println("Done");
		
	}//end of main

}
