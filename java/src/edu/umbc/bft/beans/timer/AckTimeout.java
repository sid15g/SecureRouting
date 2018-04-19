package edu.umbc.bft.beans.timer;

import java.util.Iterator;
import java.util.Set;

import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.beans.net.header.DefaultHeader;
import edu.umbc.bft.beans.net.header.Header;
import edu.umbc.bft.beans.net.payload.DataPayload;
import edu.umbc.bft.beans.net.payload.FaultPayload;
import edu.umbc.bft.beans.net.payload.Payload;
import edu.umbc.bft.beans.net.route.Route;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

public class AckTimeout extends Timeout {

	private Datagram packet;
	
	public AckTimeout(long timeoutInMillis) {
		super(timeoutInMillis);
	}//end of constructor

	public void setPacket(Datagram packet) {
		try {
			this.packet = new Datagram(packet);
		}catch(CloneNotSupportedException e) {
			Logger.error(this.getClass(), "Unable to store datagram object : "+ e.getMessage() );
		}//end of try catch
	}//end of method
	

		
	@Override
	public void onTimeout() {
		
		Logger.info(this.getClass(), " ACK timed out ");
		
		long originalSeqNum = this.packet.getHeader().getSequenceNumber();
		Route r = this.packet.getRoute();
		
		if( this.packet.getSource().equals(Router.serverIP) == false )			{
			
			DataPayload dp = (DataPayload)this.packet.getPayload();
			Payload p = new FaultPayload(Router.serverIP, r.current(), originalSeqNum);
			Header h = new DefaultHeader(Router.serverIP, this.packet.getSource(), dp.getAckSequenceNo());
			Router.findRoute(h);
			
			Logger.info(this.getClass(), " Sending fault announcement | Accused Node: "+ r.current() +" | Seq No. "+ originalSeqNum );
			
			Datagram d = new Datagram(h, p);
			
			if( d.updateDatagram() )
				super.getNetworkInf().send(d);
			else {
				Logger.info(this.getClass(), "F.A. not sent");
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
				
				Set<Datagram> fas = Router.getFaultAnnouncements(this.packet);
				
				if( fas!=null && fas.size()>0 )		{
					
					Iterator<Datagram> iter = fas.iterator();
							
					Datagram bnode = iter.next();
					int hops = bnode.getRoute().getHopsFromSource();
					
					while( iter.hasNext() )		{
						
						Datagram d = iter.next();
						int temp = d.getRoute().getHopsFromSource();
						
						if( hops > temp  )	{
							bnode = d;
							hops = temp;
						}
						
					}//end of loop
					
					String source = bnode.getSource();
					String accused = bnode.getRoute().current();
					
					Logger.info(this.getClass(), " Marking the link FAULTY | Accuser: "+ source +" | Accused Node: "+ accused +" | Seq No. "+ originalSeqNum );
					Router.getTopologyManager().markLinkFaulty(source, accused);
					
				}else	{
					String source = this.packet.getSource();
					Logger.info(this.getClass(), " NO FAs | Marking the neighbor link FAULTY | Accuser: "+ source +" | Accused Node: "+ r.current() +" | Seq No. "+ originalSeqNum );
					Router.getTopologyManager().markLinkFaulty(source, r.current());
				}
				
			}//end of marking link faulty
			
		}//end of source check
		
	}//end of timer

}
