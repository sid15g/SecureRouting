package edu.umbc.bft.junit.tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import edu.umbc.bft.beans.crypto.extended.CipherChain;
import edu.umbc.bft.beans.crypto.extended.CipherChainBuilder;
import edu.umbc.bft.beans.net.route.Route;
import edu.umbc.bft.junit.categories.IntegratedTests;

@Category(IntegratedTests.class)
public class IteratorsTests {

	private List<String> list;
	
	@Before
	public void loadList()	{
		this.list = new ArrayList<String>();
		this.list.add("1");	this.list.add("2");
		this.list.add("3");	this.list.add("4");
		this.list.add("5");	this.list.add("6");
		this.list.add("7");	this.list.add("8");
		this.list.add("9");	this.list.add("10");
	}//end of method
	
	
	@Test
	public void testRoute()	{
		Route r = new Route(this.list);
		Assert.assertEquals("1", r.next());
		Assert.assertEquals("2", r.next());
		Assert.assertEquals(8, r.ttl());
		Assert.assertEquals("3", r.next());
		Assert.assertEquals(7, r.ttl());
		Assert.assertEquals(false, r.contains("3"));
		Assert.assertEquals(true, r.contains("4"));
		Assert.assertEquals("4", r.next());
		Assert.assertEquals(false, r.contains("4"));
	}//end of method
	
	
	@Test
	public void testCipherChain()	{
		CipherChain cc = CipherChainBuilder.create(this.list);
		Assert.assertEquals("1", cc.next());
		Assert.assertEquals("2", cc.next());
		Assert.assertEquals(8, cc.ttl());
		Assert.assertEquals("3", cc.next());
		Assert.assertEquals(7, cc.ttl());
		Assert.assertEquals(false, cc.contains("3"));
		Assert.assertEquals(true, cc.contains("4"));
		Assert.assertEquals("4", cc.next());
		Assert.assertEquals(false, cc.contains("4"));
	}//end of method
	
}
