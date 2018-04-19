package edu.umbc.bft.router.engine;

import java.util.Random;

import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.router.engine.Engine.MessageHandler;
import edu.umbc.bft.router.main.NetworkInterface;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

public class AckHandler implements MessageHandler {

	@Override
	public void handle(NetworkInterface inf, Datagram dg) {

		boolean senderValidation = dg.validateSender();
		boolean packetValidation = dg.validate();
		
		if( senderValidation && packetValidation )	{
			
			Router.cancelAckTimer(dg);
			//TODO take action of cancellation fails
			
			if( dg.timeToLive() > 0 )	{
				
				if( dg.updateDatagram() )	{
					Random r = new Random();
					if( r.nextFloat() > 0.50 )
						inf.send(dg);
					else	{
						Logger.imp(this.getClass(), "Assume ACK was not delivered");
					}
				}else	{
					Logger.info(this.getClass(), "ACK not forwarded");
				}
				
			}else if( dg.getRoute().current().equals(Router.serverIP) )	{
				
				Logger.info(this.getClass(), "Yeahhhh ACK found....");
				
			}else	{
				Logger.imp(this.getClass(), " Destination IP mismatch | ACK dropped ");
			}
			
		}else	{
			Logger.info(this.getClass(), " Sender Validation: "+ senderValidation +" | Packet Validation: "+ packetValidation );
			Logger.warn(this.getClass(), " ACK validation failed | Packet dropped... ");
		}

	}//end of handler

}
