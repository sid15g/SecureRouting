package edu.umbc.bft.junit.tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.beans.net.Packet;
import edu.umbc.bft.beans.net.header.DefaultHeader;
import edu.umbc.bft.beans.net.payload.DataPayload;
import edu.umbc.bft.beans.net.payload.Payload;
import edu.umbc.bft.beans.net.route.Route;
import edu.umbc.bft.junit.categories.IntegratedTests;
import edu.umbc.bft.router.main.DatagramFactory;
import edu.umbc.bft.router.main.Router;

@Category(IntegratedTests.class)
public class SerializationTests {
	
	@Test
	public void jsonSerializingDatagram()	{
		Router.load();
		DefaultHeader h = new DefaultHeader("127.0.0.3", "127.0.0.1", 12L);
		List<String> l = new ArrayList<String>();
		h.setSequenceNo(12);
		h.setSecureMode(true);
		h.setRoute(new Route(l));
		Payload pl = new DataPayload("hello", 143);
		l.add("127.0.0.3");
		l.add("127.0.0.2");
//		l.add("127.0.0.1");
		Datagram d = new Datagram(h, pl);
		String json = DatagramFactory.serialize(d);
		Assert.assertNotNull(json);
	}
	
	@Test
	public void jsonSerializingPacket()	{
		DefaultHeader h = new DefaultHeader("127.0.0.3", "127.0.0.1");
		List<String> l = new ArrayList<String>();
		h.setSequenceNo(12);
		h.setSecureMode(true);
		h.setRoute(new Route(l));
		Payload pl = new DataPayload("hello", 143);
		Packet p = new Packet(h, pl);
		l.add("127.0.0.3");
		l.add("127.0.0.2");
		l.add("127.0.0.1");
		String json = DatagramFactory.serialize(p);
		Assert.assertNotNull(json);
		/** Check if json and hex does not give a same hash */
		String phex = DatagramFactory.hexString(p);
		Assert.assertNotEquals(json, phex);
	}
	
	@Test
	public void hexSerializingPacket()	{
		DefaultHeader h = new DefaultHeader("127.0.0.3", "127.0.0.1");
		List<String> l = new ArrayList<String>();
		h.setSequenceNo(12);
		h.setSecureMode(true);
		h.setRoute(new Route(l));
		Payload pl = new DataPayload("hello", 143);
		Packet p = new Packet(h, pl);
		l.add("127.0.0.3");
		l.add("127.0.0.2");
		l.add("127.0.0.1");
		String p1 = DatagramFactory.hexString(p);
		
		pl = new DataPayload("hello", 144);
		p = new Packet(h, pl);
		String p2 = DatagramFactory.hexString(p);
		Assert.assertNotEquals(p1, p2);
		
		/**Changing route sequence => different packet  */
		l.clear();
		l.add("127.0.0.2");
		l.add("127.0.0.3");
		l.add("127.0.0.1");
		p2 = DatagramFactory.hexString(p);
		Assert.assertNotEquals(p1, p2);
	}//end of method

	
	@Test
	public void testDatagramDeSerialization()	{
		//TODO
	}//end of method
	
}
