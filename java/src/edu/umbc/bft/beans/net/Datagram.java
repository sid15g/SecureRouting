package edu.umbc.bft.beans.net;

import edu.umbc.bft.beans.crypto.Cipher;
import edu.umbc.bft.beans.crypto.ECDSA;
import edu.umbc.bft.beans.crypto.extended.CipherChain;
import edu.umbc.bft.beans.crypto.extended.CipherChainBuilder;
import edu.umbc.bft.beans.crypto.extended.HMacForNeighbor;
import edu.umbc.bft.beans.crypto.extended.HMacNonNeighbors;
import edu.umbc.bft.beans.crypto.extended.SignatureChain;
import edu.umbc.bft.beans.net.header.Header;
import edu.umbc.bft.beans.net.payload.Payload;
import edu.umbc.bft.beans.net.route.Route;
import edu.umbc.bft.router.main.DatagramFactory;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

public class Datagram	{
	
	private Header header;
	private Payload payload;
	private CipherChain cchain;
	private String digitalSignature;
	private String cipherTextForNeighbor;
	
	public Datagram(Header header, Payload payload) {
		
		this.header = header;
		this.payload = payload;
		this.cchain = CipherChainBuilder.build(new Packet(this.header, this.payload));
		
		/** DSig validates sender, cipher chain validates the notes in the route */
		
		if( payload.isCreateOnlySignature() )	{
			String hex = DatagramFactory.hexString(new Packet(header, payload));
			this.digitalSignature = Router.getMyAsymmetricKey().encryprt(hex);
			this.cipherTextForNeighbor = null;
		}else	{
			this.digitalSignature = null;
		}
		
	}//end of constructor
	
	public Datagram(Datagram d) throws CloneNotSupportedException	{
		this.header = d.header;
		this.payload = d.payload;
		this.cchain = (CipherChain)d.cchain.clone();
		this.digitalSignature = this.cipherTextForNeighbor = null;
	}//end of constructor
	
	public Header getHeader() {
		return this.header;
	}
	public Payload getPayload() {
		return this.payload;
	}
	
	public final String getSource()	{
		return this.getHeader().getSource();
	}
	public Route getRoute() {
		return this.header.getRoute();
	}
	public int timeToLive() {
		return this.getRoute().ttl();
	}
	
	public void setCipherTextForNeighbor(String cipherTextForNeighbor) {
		this.cipherTextForNeighbor = cipherTextForNeighbor;
	}
	
	public boolean validateSender() {
		
		if( this.header!=null && this.payload!=null )	{

			Packet p = new Packet(this.header, this.payload);
			String hex = DatagramFactory.hexString(p);
			String source = this.getRoute().prev();
			Logger.info(this.getClass(), " Sender IP: "+ source );
			
			String nodeIp = this.header.getRoute().current();
			String key = Router.getSymmetricKeyFor(source);
			
			if( nodeIp==null || nodeIp.trim().equals(Router.serverIP)==false )	{
				Logger.error(this.getClass(), " Invalid route | Expected next node to be "+ nodeIp );
				return false;
			}else if( key!=null && key.length()>0 )	{
				Cipher c = new HMacForNeighbor(key);
				return c.verify(hex, this.cipherTextForNeighbor);
			}else	{
				Logger.error(this.getClass(), " Unable to validate sender "+ p.getSource() +" | Key not found ");
				return false;
			}
		}else	{
			Logger.error(this.getClass(), " Sender Validation Failed | Invalid Datagram  | Null Header/Payload ");
			return false;
		}
		
	}//end of method
	
	public boolean validate()	{
		
		if( this.header!=null && this.payload!=null )	{
			
			Packet p = new Packet(this.header, this.payload);
			String hex = DatagramFactory.hexString(p);
			String sourceip = this.getSource();
			
			/** Check Signature chain by source only - secure mode - only validated by the source */
			if( this.header.isSecureMode() ) {
				
				try {
					
					Logger.info(this.getClass(), " Validation Signature chain... ");
					Route r = (Route)this.header.getRoute().clone();
					String nodeip = this.getSource();
					
					do	{
						
						String pkey = Router.getPublicKeyOf(nodeip);
						String cipherText = this.cchain.next();
						
						if( ECDSA.verify(pkey, hex, cipherText) == false )	{
							Logger.info(this.getClass(), " Signature chain verification failed for "+ nodeip );
							return false;
						}
						
						nodeip = r.next();
						
					}while( this.cchain.hasNext() );
					
					return true;
					
				}catch (CloneNotSupportedException e) {
					Logger.error(this.getClass(), " Route verification failed : "+ e.getMessage() );
					return false;
				}catch(Exception e) {
					Logger.error(this.getClass(), " Signature chain verification error: "+ e.getMessage() );
					return false;
				}//end of try catch
				
			}else if( this.payload.isCreateOnlySignature() ) {
				/** Check Signature - valid for Fault announcements */
				String key = Router.getPublicKeyOf(sourceip);
				
				if( key!=null && key.length()>0 )	{
					try {
						return ECDSA.verify(key, hex, this.digitalSignature);
					}catch(Exception e) {
						Logger.info(this.getClass(), " Signature verification failed ");
						Logger.error(this.getClass(), e);
					}
				}else	{
					Logger.error(this.getClass(), " Unable to validate datagram | Source Key not found -> "+ sourceip );
				}
				
			}else	{
				/** Check Signature - valid for Fault announcements */
				String key = Router.getSymmetricKeyFor(sourceip);
				
				if( key!=null && key.length()>0 )	{
					Cipher c = new HMacNonNeighbors(key);
					return c!=null?c.verify(hex, cchain.next()):false;
				}else	{
					Logger.error(this.getClass(), " Unable to validate datagram | Source Key not found -> "+ sourceip );
				}
			}
			
		}else	{
			Logger.error(this.getClass(), " Datagram validation failed | Invalid Datagram  | Null Header/Payload ");
		}
		
		return false;
		
	}//end of method
	
	
	public boolean updateDatagram()		{
		
		if( this.header.isSecureMode() )	{
			
			SignatureChain sc = new SignatureChain(this.cchain);
			String hex = DatagramFactory.hexString(this);
			String cipherText = Router.getMyAsymmetricKey().encryprt(hex);
			
			if( sc.addToChain(cipherText) == false )	{
				Logger.error(this.getClass(), " Unable to append digital signature");
			}else	{
				Logger.info(this.getClass(), " Digital signature appended to cipher chain ");
				this.cchain = sc;
			}			
			
		}
	
		return this.setHashForNeighbor();
		
	}//end of method
	
	
	public boolean setHashForNeighbor()		{
		/** DONOT change the order */
		String nextNode = this.getRoute().next();
		String npKey = Router.getSymmetricKeyFor(nextNode);
		
		if( npKey!=null && npKey.length()>0 )	{
			
			Logger.info(this.getClass(), " Creating HMAC for "+ nextNode );
			Packet p = new Packet(this.header, this.payload);
			String hex = DatagramFactory.hexString(p);
			Cipher c = new HMacForNeighbor(npKey);
			String cipherText = c.encryprt(hex);
			this.cipherTextForNeighbor = cipherText;
			return true;
			
		}else	{
			Logger.error(this.getClass(), " Key NOT found for "+ nextNode );
			return false;
		}
		
	}//end of method
	
	
	@Override
	public boolean equals(Object obj) 		{
		
		if( obj instanceof Datagram )	{
			
			Datagram d = (Datagram)obj;
			
			/** If class instances matches */
			boolean res = this.getHeader().getClass().equals(d.getHeader().getClass());
			res &= this.getPayload().getClass().equals(d.getPayload().getClass());
			
			/** and hex string matches */
			if( res )	{
				String h1 = DatagramFactory.hexString(d);
				String h2 = DatagramFactory.hexString(this);
				return h1.equals(h2);
			}
			
		}
		return false;
		
	}//end of method
	
}