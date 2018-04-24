package edu.umbc.bft.junit.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.gson.Gson;

import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.beans.net.header.DefaultHeader;
import edu.umbc.bft.beans.net.payload.DataPayload;
import edu.umbc.bft.beans.net.payload.Payload;
import edu.umbc.bft.beans.net.route.Route;
import edu.umbc.bft.junit.categories.IntegratedTests;
import edu.umbc.bft.router.extras.RouteDiscovery;
import edu.umbc.bft.router.extras.TopologyManager;
import edu.umbc.bft.router.main.Router;

@Category(IntegratedTests.class)
public class FunctionalityTests {
	

	@Test
	public void testTopologyManager()	{
		
		Router.load();
		TopologyManager tm = Router.getTopologyManager();
		Assert.assertEquals("0.0", String.valueOf(tm.getLinkWeight("127.0.0.1", "127.0.0.3")));
		Assert.assertEquals("0.0", String.valueOf(tm.getLinkWeight("127.0.0.1", "127.0.0.4")));
		Assert.assertEquals("0.0", String.valueOf(tm.getLinkWeight("127.0.0.1", "127.0.0.1")));
		tm.deleteLink("127.0.0.1", "127.0.0.3");
		Assert.assertEquals("0.0", String.valueOf(tm.getLinkWeight("127.0.0.1", "127.0.0.3")));
		tm.addLink("127.0.0.1", "127.0.0.1", 1.0F);
		Assert.assertEquals("1.0", String.valueOf(tm.getLinkWeight("127.0.0.1", "127.0.0.1")));
		
	}//end of test
	
	@Test
	public void testCipherChain()	{
		
		Router.load();
		DefaultHeader h = new DefaultHeader("127.0.0.1", "127.0.0.2", 12L);
		Router.findRoute(h);
		Payload pl = new DataPayload("hello", 143);
		Datagram d = new Datagram(h, pl);
		
		Assert.assertNotNull(d);
		
	}//end of test	
	
	@Test
	public void routeDiscovery()	{
		Gson g = new Gson();
		Router.load();
		TopologyManager tm = new TopologyManager();
		RouteDiscovery rd = new RouteDiscovery(tm);
/*		tm.addLink(2, 5, 1.0F);
		tm.addLink(3, 6, 1.0F);
		tm.addLink(4, 6, 1.0F);
		rd.find(1, 4);
		String res = g.toJson(rd.getLastStack());
		Assert.assertTrue(res.equals("[2,3,6,4]") || res.equals("[3,6,4]") || res.equals("[3,2,4]"));
		System.out.println();
		tm.addLink(2, 5, 2.3F);
		tm.addLink(3, 6, 1.0F);
		tm.addLink(4, 6, 1.55F);
		rd.find(1, 4);
		res = g.toJson(rd.getLastStack());
		Assert.assertTrue(res.equals("[2,3,6,4]") || res.equals("[3,6,4]") || res.equals("[3,2,4]"));
		System.out.println();
		tm.addLink(5, 4, 2.78F);
		tm.addLink(1, 3, 2.0F);
		rd.find(1, 4);
		res = g.toJson(rd.getLastStack());
		Assert.assertTrue(res.equals("[3,2,5,4]") || res.equals("[3,6,4]") || res.equals("[3,2,4]"));
		System.out.println();
		rd.find(1, 3);
		res = g.toJson(rd.getLastStack());
		Assert.assertEquals("[3]", res);
		tm.addLink(5, 4, 1.0F);
		rd.find(2, 4);
		res = g.toJson(rd.getLastStack());
		Assert.assertTrue(res.equals("[5,4]") || res.equals("[3,6,4]") || res.equals("[4]"));
		System.out.println();
		rd.find(1, 2);
		res = g.toJson(rd.getLastStack());
		Assert.assertTrue(res.equals("[3,2]"));*/
		
		Router.serverIP = "127.0.0.11";
		
		Route r = rd.find(1, 14);
		String res = g.toJson(rd.getLastStack());
		System.out.println(res);
		System.out.println(g.toJson(r.reverseRoute()));
		
		rd.find(8, 19);
		res = g.toJson(rd.getLastStack());
		System.out.println(res);

		rd.find(5, 13);
		res = g.toJson(rd.getLastStack());
		System.out.println(res);
		
		rd.find(14, 1);
		res = g.toJson(rd.getLastStack());
		System.out.println(res);

	}//end of test

	
}