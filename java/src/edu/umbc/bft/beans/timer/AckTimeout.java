package edu.umbc.bft.beans.timer;

import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.beans.net.header.DefaultHeader;
import edu.umbc.bft.beans.net.header.Header;
import edu.umbc.bft.beans.net.payload.DataPayload;
import edu.umbc.bft.beans.net.payload.FaultPayload;
import edu.umbc.bft.beans.net.payload.Payload;
import edu.umbc.bft.beans.net.route.Route;
import edu.umbc.bft.router.main.NetworkInterface;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

public class AckTimeout extends Timeout {

	private Datagram packet;
	
	public AckTimeout(long timeoutInMillis, NetworkInterface inf, Datagram packet)		{
		
		super(timeoutInMillis);
		super.setNetworkInf(inf);
		
		try {
			if( packet!=null && packet.getRoute().length() > 0 )	{
				this.packet = new Datagram(packet);
			}else	{
				throw new InstantiationException("No Datagram/Route to start timeout");
			}
		}catch(CloneNotSupportedException e) {
			Logger.error(this.getClass(), " Unable to store datagram object : "+ e.getMessage() );
		}catch(InstantiationException e)	{
			Logger.error(this.getClass(), e.getMessage() );
		}//end of try catch
		
	}//end of constructor	


	@Override
	public void onTimeout() {
		
		Logger.info(this.getClass(), " ACK timed out ");
		
		long originalSeqNum = this.packet.getHeader().getSequenceNumber();
		Route r = this.packet.getRoute();
		
		if( this.packet.getSource().equals(Router.serverIP) == false )			{
			
			DataPayload dp = (DataPayload)this.packet.getPayload();
			Header h = new DefaultHeader(Router.serverIP, this.packet.getSource(), dp.getAckSequenceNo());
			Route newRoute = Router.findRoute(h);
			
			if( newRoute!=null && newRoute.length()>0 )			{
				
				Logger.info(this.getClass(), " Generating fault announcement | Accused Node: "+ r.current() +" | Original Seq No. "+ originalSeqNum );
				Payload p = new FaultPayload(Router.serverIP, r.current(), originalSeqNum);
				Datagram d = new Datagram(h, p);
				
				if( d.updateDatagram() )
					super.getNetworkInf().send(d);
				else {
					Logger.info(this.getClass(), "F.A. not sent");
				}
			}else	{
				Logger.warn(this.getClass(), "No Route found... Unable to send FA ");
			}
			
		}else	{
			
			boolean secureMode = ((DataPayload)this.packet.getPayload()).isSecureReply();
			boolean testingBR3 = false;

			
			if( testingBR3 && secureMode == false )	{
				
				Logger.info(this.getClass(), " Resending datagram with secure mode enable " );
				Header old = this.packet.getHeader();
				
				try {
					
					Route copy = (Route)r.clone();
					int seqNum = Router.getNextSequenceNumber();
					Header h = new DefaultHeader(old.getSource(), old.getDestination(), seqNum);
					h.setRoute(copy);
					
					int nodeid = Router.getNodeID(old.getDestination());
					final int ackOffset = nodeid * Router.maxHops;
					
					DataPayload oldPayload = (DataPayload)this.packet.getPayload();
					DataPayload p = new DataPayload(oldPayload.getData(), seqNum+ackOffset);
					p.setSecureReply(true);
					
					Datagram d = new Datagram(h, p);
					
					if( d.updateDatagram() )	{
						Router.startAckTimer(super.getNetworkInf(), d);
						super.getNetworkInf().send(d);
					}else	{
						Logger.info(this.getClass(), "Retransmission failed !");
					}
					
				}catch (CloneNotSupportedException e) {
					Logger.error(this.getClass(), " Unable to copy route "+ e.getMessage() );
					Logger.info(this.getClass(), " Falling back... ");
					secureMode = true;
				}
				
			}//end of check
			
			if( testingBR3==false || secureMode )	{
				
				Datagram fa = Router.getFaultAnnouncement(this.packet);
				
				if( fa != null ) {
					
					FaultPayload p = (FaultPayload)fa.getPayload();
					String accused = p.getSuspectNodeIp();
					String source = p.getAccuserNodeIp();
					
					Logger.info(this.getClass(), " Marking the link FAULTY | Accuser: "+ source +" | Accused Node: "+ accused +" | Seq No. "+ originalSeqNum );
					Router.getTopologyManager().markLinkFaulty(source, accused);
					Router.removeFaultAnnouncement(fa.getSequenceKey());
					
				}else	{
					String source = this.packet.getSource();
					Logger.info(this.getClass(), " NO FAs | Marking the neighbor link FAULTY | Accuser: "+ source +" | Accused Node: "+ r.current() +" | Seq No. "+ originalSeqNum );
					Router.getTopologyManager().markLinkFaulty(source, r.current());
				}
				
			}//end of marking link faulty
			
		}//end of source check
		
	}//end of timer

}
