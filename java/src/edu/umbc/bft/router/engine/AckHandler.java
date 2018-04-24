package edu.umbc.bft.router.engine;

import java.util.Random;

import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.router.engine.Engine.MessageHandler;
import edu.umbc.bft.router.main.NetworkInterface;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

public class AckHandler implements MessageHandler {

	private static MessageHandler instance = null;

	private float dropProbability;
	
	public static synchronized MessageHandler getInstance()	{
		if( instance == null )
			instance = new AckHandler();
		return instance;
	}//end of method
	
	
	private AckHandler() {
		String prob = Router.getProperty("ack.drop.prob");
		this.dropProbability = Float.parseFloat(prob);
	}//end of constructor
	
	
	@Override
	public void handle(NetworkInterface inf, Datagram dg) {

		boolean senderValidation = true; //dg.validateSender();
		boolean packetValidation = dg.validate();
		
		if( senderValidation && packetValidation )	{
			
			if( Router.cancelAckTimer(dg) )			{
				
				if( dg.timeToLive() > 0 )	{
					
					if( dg.updateDatagram() )	{
						Random r = new Random();
						if( (int)this.dropProbability==0 || r.nextFloat() > this.dropProbability )
							inf.send(dg);
						else	{
							Logger.imp(this.getClass(), "Assume ACK was not delivered");
						}
					}else	{
						Logger.info(this.getClass(), "ACK not forwarded");
					}
					
				}else if( dg.getRoute().current().equals(Router.serverIP) )	{
					
					Logger.info(this.getClass(), "Yeahhhh ACK found....");
					
					synchronized(Router.lock)	{
						Router.lock.notify();
					}
					
				}else	{
					Logger.imp(this.getClass(), " Destination IP mismatch | ACK dropped ");
				}
				
			}else	{
				Logger.info(this.getClass(), " Delayed ACK | Timer not found | SeqNum: "+ dg.getHeader().getSequenceNumber() +" | Dropped... " );
			}
			
		}else	{
			Logger.info(this.getClass(), " Sender Validation: "+ senderValidation +" | Packet Validation: "+ packetValidation );
			Logger.warn(this.getClass(), " ACK validation failed | Packet dropped... ");
		}

	}//end of handler

}
