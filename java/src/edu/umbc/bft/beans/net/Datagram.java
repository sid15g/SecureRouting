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
import edu.umbc.bft.beans.net.payload.Request;
import edu.umbc.bft.beans.net.payload.Response;
import edu.umbc.bft.beans.net.route.Route;
import edu.umbc.bft.router.main.DatagramFactory;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

public class Datagram	{
	
	private Header header;
	private Payload payload;
	private String signature;
	private CipherChain cchain;
	
	public Datagram(Header header, Payload payload) {
		
		this.cchain = null;
		this.header = header;
		this.payload = payload;
		
		if( payload.hasSignature() )	{
			String hex = DatagramFactory.hexString(new Packet(header, payload));
			this.signature = Router.getMyAsymmetricKey().encryprt(hex);
			//Not changed in updateDatagram()
		}else	{
			//Initialized every time on updateDatagram()
			this.signature = null;
			this.cchain = CipherChainBuilder.build(new Packet(this.header, this.payload));
		}
		
	}//end of constructor
	
	public Datagram(Datagram d) throws CloneNotSupportedException	{
		this.signature = null;
		this.header = d.header;
		this.payload = d.payload;
		this.cchain = (CipherChain)d.cchain.clone();
	}//end of constructor
	
	public Header getHeader() {
		return this.header;
	}
	public Payload getPayload() {
		return this.payload;
	}
	
	public String getSequenceKey()	{
		if( this.payload instanceof Request )	{
			long seqNo = this.header.getSequenceNumber();
			long resNo = ((Request)this.payload).getAckSequenceNo();
			return seqNo +"_"+ resNo;
		}else if( this.payload instanceof Response )	{
			long resNo = this.header.getSequenceNumber();
			long seqNo = ((Response)this.payload).getSequenceNum();
			return seqNo +"_"+ resNo;			
		}else	{
			Logger.warn(this.getClass(), "No sequence key defined");
			return String.valueOf(this.header.getSequenceNumber());
		}
	}//end of method
	
	
	public final String getSource()	{
		return this.getHeader().getSource();
	}
	public Route getRoute() {
		return this.header.getRoute();
	}
	public int timeToLive() {
		return this.getRoute().ttl();
	}	
	
	public boolean validateSender() {
		
		if( this.header!=null && this.payload!=null )	{

			if( this.payload.hasSignature() )	{
				//No need to verify the sender (Only source needs to be verified)
				return true;
			}else	{
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
					return c.verify(hex, this.signature);
				}else	{
					Logger.error(this.getClass(), " Unable to validate sender "+ p.getSource() +" | Key not found ");
					return false;
				}				
			}//end of signature check
			
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
				
			}else if( this.payload.hasSignature() ) {
				/** Check Signature - valid for Fault announcements */
				String key = Router.getPublicKeyOf(sourceip);
				
				if( key!=null && key.length()>0 )	{
					try {
						return ECDSA.verify(key, hex, this.signature);						
					}catch(Exception e) {
						Logger.info(this.getClass(), " Signature verification failed ");
						Logger.error(this.getClass(), e);
					}
				}else	{
					Logger.error(this.getClass(), " Unable to validate datagram | Source Key not found -> "+ sourceip );
				}
				
			}else	{
				/** Check HMAC chain */
				String key = Router.getSymmetricKeyFor(sourceip);
				
				if( key!=null && key.length()>0 )	{
					Cipher c = new HMacNonNeighbors(key);
					String plainText = hex + cchain.getCurrentChain();
//					return c!=null?c.verify(hex, cchain.next()):false;
					return c!=null?c.verify(plainText, cchain.next()):false;
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
		
		if( this.payload.hasSignature() )	{
			//Nothing to do, just increase the route count
			this.getRoute().next();
			return true;
		}else
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
			this.signature = c.encryprt(hex);
			return true;
			
		}else	{
			Logger.error(this.getClass(), " Key NOT found for "+ nextNode );
			return false;
		}
		
	}//end of method
	
	
	public String print() {
		return "["+ this.payload.getClass().getSimpleName() +" | "+ this.getSource() +" | "+ this.header.getSequenceNumber() +"] ";
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