package edu.umbc.bft.router.engine;

import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.router.engine.Engine.MessageHandler;
import edu.umbc.bft.router.main.NetworkInterface;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

/**
 * 1) it has seq in its buffer, 
 * 2) the accuser is the predecessor of the accused, and 
 * 3) the signature is correct. If all conditions are satisfied, it forwards S USPECT to its predecessor. 
 * 
 * If the source does not receive a ACK or a SUSPECT message, the source marks p2 as faulty, 
 * and marks all the links connecting p2 as faulty.
**/
public class FaultHandler implements MessageHandler {
	
	private static MessageHandler instance = null;
	
	public static synchronized MessageHandler getInstance()	{
		if( instance == null )
			instance = new FaultHandler();
		return instance;
	}//end of method
	
	private FaultHandler() {}
	
	
	@Override
	public void handle(NetworkInterface inf, Datagram dg) {

		boolean senderValidation = dg.validateSender();
		boolean packetValidation = dg.validate();
		
		if( senderValidation && packetValidation )	{
			
			if( dg.getHeader().getDestination().equals(Router.serverIP) )	{
				//Wait until ACK timeout, and then process all the FAs. 
				
				if( Router.hasAckTimer(dg) )	{
					Logger.info(this.getClass(), " Waiting for all F.A.");
					Router.addFaultAnnouncement(dg);
				}else {
					Logger.info(this.getClass(), " Delayed FA | Timer not found | SeqNum: "+ dg.getHeader().getSequenceNumber() +" | Dropped... " );
				}
				
			}else if( dg.timeToLive() > 0 )	{
				
				if( Router.cancelAckTimer(dg) )		{
					if( dg.updateDatagram() )	{
						inf.send(dg);
					}else	{
						Logger.info(this.getClass(), "F.A. not forwarded");
					}
				}else	{
					Logger.info(this.getClass(), " Delayed FA | Timer not found | SeqNum: "+ dg.getHeader().getSequenceNumber() +" | Dropped... " );
				}
				
			}else	{
				Logger.imp(this.getClass(), " Destination IP mismatch | FA dropped ");
			}
			
		}else	{
			Logger.info(this.getClass(), " Sender Validation: "+ senderValidation +" | Packet Validation: "+ packetValidation );
			Logger.warn(this.getClass(), " F.A. validation failed | Packet dropped... ");
		}
		
	}//end of handler

}
