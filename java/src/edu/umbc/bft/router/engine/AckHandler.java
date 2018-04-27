package edu.umbc.bft.router.engine;

import java.util.Random;

import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.beans.net.header.DefaultHeader;
import edu.umbc.bft.beans.net.header.Header;
import edu.umbc.bft.beans.net.payload.AckPayload;
import edu.umbc.bft.beans.net.payload.FaultPayload;
import edu.umbc.bft.beans.net.payload.Payload;
import edu.umbc.bft.beans.net.route.Route;
import edu.umbc.bft.router.engine.Engine.MessageHandler;
import edu.umbc.bft.router.main.NetworkInterface;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

public class AckHandler implements MessageHandler {

	private static MessageHandler instance = null;

	private float dropProbability, framingProbability;
	
	public static synchronized MessageHandler getInstance()	{
		if( instance == null )
			instance = new AckHandler();
		return instance;
	}//end of method
	
	
	private AckHandler() {
		String dprob = Router.getProperty("ack.drop.prob");
		String fprob = Router.getProperty("frame.packet.prob");
		this.dropProbability = Float.parseFloat(dprob);
		this.framingProbability = Float.parseFloat(fprob);
	}//end of constructor
	
	
	@Override
	public void handle(NetworkInterface inf, Datagram dg) {

		boolean senderValidation = true; //dg.validateSender();
		boolean packetValidation = dg.validate();
		
		if( senderValidation && packetValidation )	{
			
			if( Router.cancelAckTimer(dg) )			{
				
				if( dg.timeToLive() > 0 )	{
					
					boolean faulty = false;
					Random r = new Random();
					final String prevNode = dg.getRoute().prev();
					
//					final float succProb = 100 - this.dropProbability - this.framingProbability;
					final float faultProb = this.dropProbability + this.framingProbability;
					
					if( (int)this.dropProbability!=0 && r.nextFloat() < this.dropProbability )	{
						
						Logger.imp(this.getClass(), "Assume ACK was not delivered");			
						faulty = true;
								
					}else if( (int)this.framingProbability!=0 && r.nextFloat()<faultProb )	{
						
						Route newRoute = dg.getRoute();
//						Route newRoute = rt.reverseRoute();	// wrong
						AckPayload ack = (AckPayload)dg.getPayload();
													
						if( newRoute!=null && newRoute.length() > 0 )		{
							
							Logger.imp(this.getClass(), " Generating fake FA | Accused Node: "+ prevNode );
							Header h = new DefaultHeader( Router.serverIP, dg.getHeader().getDestination(), dg.getHeader().getSequenceNumber() );
							h.setRoute(newRoute);
							Payload p = new FaultPayload(Router.serverIP, prevNode, ack.getSequenceNum());
							Datagram d = new Datagram(h, p);
							
							if( d.updateDatagram() )	{
								Logger.info(this.getClass(), " ACK dropped deliberately ");
								faulty = true;
								inf.send(d);
							}else {
								Logger.info(this.getClass(), "Fake F.A. not sent | ACK dropped ");
							}
							
						}else	{
							Logger.debug(this.getClass(), "Unable to generate fake FA... Following default protocol");
						}
						
					}//end of faulty check
					
					if( faulty==false && dg.updateDatagram() )	{
						// Forward the ACK; without any faulty behavior
						inf.send(dg);
					}else if( faulty == false )	{
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
