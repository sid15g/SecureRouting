package edu.umbc.bft.router.extras;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.DecoderException;

import edu.umbc.bft.beans.crypto.Cipher;
import edu.umbc.bft.beans.crypto.ECDSA;
import edu.umbc.bft.exceptions.KeyNotFoundException;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

public class KeyManager {
	
	/** HMAC: IP to Key mapping */
	private Map<String, String> symmetricKeys;
	
	/** HMAC: IP to Key mapping */
	private Map<String, String> publicKeys;
	private Cipher asymmetricKey;
	
	public KeyManager() {
		
		this.symmetricKeys = new ConcurrentHashMap<String, String>();
		this.publicKeys = new ConcurrentHashMap<String, String>();
		final String serverip = Router.serverIP;
		Properties props = new Properties();
		
		try(FileInputStream fis = new FileInputStream(new File("resource", "key.map"));)	{
			
			props.load(fis);
			Iterator<?> iter = props.keySet().iterator();
			String pbKey = null, scKey = null;
			
			while( iter!=null && iter.hasNext() )	{
				
				String key = iter.next().toString().trim();
				
				if( key.contains(serverip) )	{
					
					String temp = key.replace(serverip, "");
					
					if( temp.endsWith("private.key") )	{
						scKey = props.getProperty(key);
						Logger.info(this.getClass(), " Private Key created " );
					}else if( temp.endsWith("public.key") )	{
						pbKey = props.getProperty(key);
						Logger.info(this.getClass(), " Public Key created " );
						this.publicKeys.put(serverip, pbKey.trim());
					}else if( temp.contains(".and.") )		{
						/** Do not create Cipher here, as the type of object is scenario dependent (neighbor and non-neighbor)  */
						String destIP = temp.replace(".and.", "").trim();
						String value = props.getProperty(key);
						this.symmetricKeys.put(destIP, value.trim());
						Logger.info(this.getClass(), " Symmetric Key added for "+ serverip +" and "+ destIP );
					}else {
						Logger.error(this.getClass(), " Key NOT identified => "+ key );
					}
					
				}else if( key.contains("public.key") )	{
					pbKey = props.getProperty(key);
					String nodeIP = key.replace(".public.key", "").trim();
					this.publicKeys.put(nodeIP, pbKey.trim());
					Logger.info(this.getClass(), " Public Key added for "+ nodeIP );
				}//end of if
				
			}//end of loop
			
			this.asymmetricKey = new ECDSA(pbKey, scKey);
			
		}catch(IOException e)	{
			Logger.error(this.getClass(), e);
		}catch(InvalidKeySpecException e) {
			Logger.error(this.getClass(), e);
		}catch (DecoderException e) {
			Logger.error(this.getClass(), e);
		}catch(Exception e)	{
			Logger.error(this.getClass(), e);
		}
		
	}//end of constructor
	
	
	public Cipher getMyAsymmetricCipher() {
		return this.asymmetricKey;
	}
	
	public String getSymmetricKeyFor(String serverip) throws KeyNotFoundException {
		if( serverip!=null && serverip.length()>0 && this.symmetricKeys.containsKey(serverip) )
			return this.symmetricKeys.get(serverip);
		else	{
			throw new KeyNotFoundException(serverip);
		}
	}//end of method
	
	
	public String getPublicKeyOf(String serverip) throws KeyNotFoundException {
		if( serverip!=null && serverip.length()>0 && this.publicKeys.containsKey(serverip) )
			return this.publicKeys.get(serverip);
		else	{
			throw new KeyNotFoundException(serverip);
		}
	}//end of method
	
}