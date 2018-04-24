package edu.umbc.bft.router.extras;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import com.google.gson.Gson;

import edu.umbc.bft.beans.net.route.Route;
import edu.umbc.bft.router.main.Router;
import edu.umbc.bft.util.Logger;

/**
 * we can set bandwidth in ns2; implies route selection all links = 1; use distance vector
 * route hard coded 
 **/
public class RouteDiscovery		{

	private TopologyManager graph;
	private Stack<Integer> stack;
	private boolean[] visited;
	
	public RouteDiscovery(TopologyManager manager) 	{
		this.graph = manager;
		this.visited = new boolean[this.graph.size()+1];
	}//end of constructor

	
	private List<Pair> getSortedLinksOf(int srcid) {
		
		Map<Integer, Float> links = this.graph.getLinksOf(srcid);
		Iterator<Integer> iter = links.keySet().iterator();
		List<Pair> list = new LinkedList<Pair>();
		
		while( iter!=null && iter.hasNext() )	{
			int key = iter.next();
			float value = links.get(key);
			if( value > 0.00F )
				list.add(new Pair(key, value));
		}//end of loop
		
		Collections.sort(list);
		return list;
		
	}//end of method
	
	
	public Route find(String srcIP, String destIP)		{
		
		int sid = Router.getNodeID(srcIP);
		int did = Router.getNodeID(destIP);
		
		return this.find(sid, did);
		
	}//end of method
	
	public Route find(int sid, int did)		{
		
		Logger.info(this.getClass(), " Finding route ["+ sid +"] -> ["+ did +"]");
		
		for( int i=0; i<this.visited.length; i++ )
			this.visited[i] = false;
		
		this.visited[sid] = true;
		
		List<Pair> links = this.getSortedLinksOf(sid);
		Stack<Integer> route = new Stack<Integer>();
		Gson gson = new Gson();
		boolean res = false;
		
		if( links.contains(new Pair(did)) )	{
			route.add(did);
		}else	{
			
			while( links.size() > 0 )	{
				
				Pair p = links.get(0);
				int id = p.getKey();
				route.push(id);
				
				if( id == did )	{
					break;
				}else if( this.visited[id] == false )	{
					res = this.find(id, did, route);
				}
				
				if( res )	{
					break;
				}else	{
					route.pop();
					links.remove(p);
				}
				
			}//end of loop
			
		}//end of if-else
		
		Logger.info(this.getClass(), " Route found: "+ gson.toJson(route));
		this.stack = route;
		List<String> list = new ArrayList<String>();
		
		for( Integer id: route ) {
			String ip = Router.getNodeIP(id);
			list.add(ip);
		}
		
		Route r = new Route(list);
		r.setSource(Router.getNodeIP(sid));
		return r;
		
	}//end of method
	
	private boolean find( int srcid, int destid, Stack<Integer> route )	{
		
		this.visited[srcid] = true;
		List<Pair> links = this.getSortedLinksOf(srcid);
		boolean res = false;
		
		if( links.contains(new Pair(destid)) )	{
			route.add(destid);
			return true;
		}
		
		while( links.size() > 0 )	{
			
			Pair p = links.get(0);
			int id = p.getKey();
			route.push(id);
			
			if( id == destid )	{
				return true;
			}else if( this.visited[id] == false )	{
				res = this.find(id, destid, route);
			}
			
			if( res )
				return res;
			else	{
				route.pop();
				links.remove(p);
			}
			
		}//end of loop
		
		return false;
		
	}//end of method


	public Stack<Integer> getLastStack() {
		return this.stack;
	}
	
}//end of class


class Pair implements Comparable<Pair> {

	private int key;
	private float value;
	private Random rand;
	
	Pair(Integer key)	{
		this(key, 0.00F);
	}
	Pair(Integer key, Float value)	{
		this.key = key;
		this.value = value;
		this.rand = new Random();
	}
	
	
	public int getKey() {
		return key;
	}
	public float getValue() {
		return value;
	}
	
	@Override
	public int compareTo(Pair o) {
		if( this.value == o.value )	{
			/** Randomly choose a link, if of same weight */
			return this.rand.nextBoolean()?1:-1;
		}else if( this.value > o.value )	{
			return -1;
		}else {
			return 1;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj instanceof Pair )	{
			Pair p = (Pair)obj;
			return this.key==p.key;
		}else
			return false;
	}
	
}