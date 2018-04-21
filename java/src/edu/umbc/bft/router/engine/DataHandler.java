package edu.umbc.bft.router.engine;

import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.beans.net.header.DefaultHeader;
import edu.umbc.bft.beans.net.header.Header;
import edu.umbc.bft.beans.net.payload.AckPayload;
import edu.umbc.bft.beans.net.payload.DataPayload;
import edu.umbc.bft.beans.net.payload.Payload;
import edu.umbc.bft.beans.net.route.Route;
import edu.umbc.bft.router.engine.Engine.MessageHandler;
import edu.umbc.bft.router.main.NetworkInterface;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

public class DataHandler implements MessageHandler {
	
	private static MessageHandler instance = null;
	
	public static synchronized MessageHandler getInstance()	{
		if( instance == null )
			instance = new DataHandler();
		return instance;
	}//end of method
	
	private DataHandler() {}

	
	@Override
	public void handle(NetworkInterface inf, Datagram dg) {
		
		boolean senderValidation = dg.validateSender();
		boolean packetValidation = dg.validate();
		
		if( senderValidation && packetValidation )	{
			
			if( dg.timeToLive() > 0 )	{
				
				if( dg.updateDatagram() )	{
					Router.startAckTimer(inf, dg);
					inf.send(dg);
				}else	{
					Logger.info(this.getClass(), "Datagram not forwarded");
				}
				
			}else if( dg.getRoute().current().equals(Router.serverIP) )	{
								
				DataPayload payload = (DataPayload)dg.getPayload();
				long resAck = payload.getAckSequenceNo();
				
				Header h = new DefaultHeader(Router.serverIP, dg.getSource(), resAck);
				h.setSecureMode(payload.isSecureReply());
				Route newRoute = Router.findRoute(h);
				
				if( newRoute!=null && newRoute.length()>0 )			{
					Logger.info(this.getClass(), " Replying by ACK with secure mode "+ payload.isSecureReply());
					
					Payload ack = new AckPayload(dg.getHeader().getSequenceNumber());
					Datagram replyAck = new Datagram(h,ack); 
					
					if( replyAck.updateDatagram() )	{
						inf.send(replyAck);
					}else	{
						Logger.info(this.getClass(), "ACK not sent");	
					}
				}else	{
					Logger.warn(this.getClass(), "No Route found... Unable to send ACK ");
				}
				
			}else	{
				Logger.imp(this.getClass(), " Destination IP mismatch | Datagram dropped ");
			}
			
		}else	{
			Logger.info(this.getClass(), " Sender Validation: "+ senderValidation +" | Packet Validation: "+ packetValidation );
			Logger.warn(this.getClass(), " Datagram validation failed | Packet dropped... ");
		}
		
	}//end of method

}
