package edu.umbc.bft.junit.tests;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.gson.Gson;

import edu.umbc.bft.junit.categories.IntegratedTests;
import edu.umbc.bft.router.extras.RouteDiscovery;
import edu.umbc.bft.router.extras.TopologyManager;
import edu.umbc.bft.router.main.Router;

@Category(IntegratedTests.class)
public class FunctionalityTests {
	

	@Test
	public void testTopologyManager()	{
		
		Router.load();
		TopologyManager tm = new TopologyManager();
		Assert.assertEquals("1.0", String.valueOf(tm.getLinkWeight("127.0.0.1", "127.0.0.3")));
		Assert.assertEquals("0.0", String.valueOf(tm.getLinkWeight("127.0.0.1", "127.0.0.4")));
		Assert.assertEquals("0.0", String.valueOf(tm.getLinkWeight("127.0.0.1", "127.0.0.1")));
		tm.deleteLink("127.0.0.1", "127.0.0.3");
		Assert.assertEquals("0.0", String.valueOf(tm.getLinkWeight("127.0.0.1", "127.0.0.3")));
		tm.addLink("127.0.0.1", "127.0.0.1", 1.0F);
		Assert.assertEquals("1.0", String.valueOf(tm.getLinkWeight("127.0.0.1", "127.0.0.1")));
		
	}//end of method
	
	@Test
	public void testCipherChain()	{
		//TODO
	}	
	
	@Test
	public void routeDiscovery()	{
		Gson g = new Gson();
		Router.load();
		TopologyManager tm = new TopologyManager();
		tm.addNode(4);			tm.addNode(5);			tm.addNode(6);
		RouteDiscovery rd = new RouteDiscovery(tm);
		tm.addLink(2, 5, 1.0F);
		tm.addLink(3, 6, 1.0F);
		tm.addLink(4, 6, 1.0F);
		rd.find(1, 4);
		String res = g.toJson(rd.getLastStack());
		Assert.assertTrue(res.equals("[2,3,6,4]") || res.equals("[3,6,4]"));
		tm.addLink(2, 5, 2.3F);
		tm.addLink(3, 6, 1.0F);
		tm.addLink(4, 6, 1.55F);
		rd.find(1, 4);
		res = g.toJson(rd.getLastStack());
		Assert.assertTrue(res.equals("[2,3,6,4]") || res.equals("[3,6,4]"));
		tm.addLink(5, 4, 2.78F);
		tm.addLink(1, 3, 2.0F);
		rd.find(1, 4);
		res = g.toJson(rd.getLastStack());
		Assert.assertTrue(res.equals("[3,2,5,4]") || res.equals("[3,6,4]"));
		rd.find(1, 3);
		res = g.toJson(rd.getLastStack());
		Assert.assertEquals("[3]", res);
		tm.addLink(5, 4, 1.0F);
		rd.find(2, 4);
		res = g.toJson(rd.getLastStack());
		Assert.assertTrue(res.equals("[5,4]") || res.equals("[3,6,4]"));
		rd.find(1, 2);
		res = g.toJson(rd.getLastStack());
		Assert.assertTrue(res.equals("[2]"));
		
	}//end of method

	
}