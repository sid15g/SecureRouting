package edu.umbc.bft.router.extras;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import edu.umbc.bft.router.main.Router;

public class TopologyManager {

	private Map<Integer, Map<Integer, Float>> graph;
	
	public TopologyManager() {
		
		final int total = Router.getPropertyAsInteger("total.nodes");
		this.graph = new HashMap<Integer, Map<Integer, Float>>(total);
		Gson gson = new Gson();
		
		for( int i=1; i<=total; i++ )	{
			
			Map<Integer, Float> links = new HashMap<Integer, Float>();
			this.graph.put(i, links);
			
			String key = "link.from."+ i;
			String value = Router.getProperty(key);
			float[] arr = gson.fromJson("["+ value +"]", float[].class);
			
			for(int j=0; j<arr.length; j++)	{
				if( arr[j] > 0.00F )	{
					links.put(j+1, arr[j]);
				}
			}//end of loop
			
			links.put(i, 0.00F);
			
		}//end of loop
		
	}//end of constructor
	
	
	public int size() {
		return this.graph.size();
	}
	
	public boolean addLink(String srcIP, String destIP, float weight)	{
		int sid = Router.getNodeID(srcIP);
		int did = Router.getNodeID(destIP);
		return this.addLink(sid, did, weight);
	}
	public boolean addLink(int sid, int did, float weight)	{
		if( this.graph.containsKey(sid) && this.graph.containsKey(did) )	{
			this.graph.get(sid).put(did, weight);
			this.graph.get(did).put(sid, weight);
			return true;
		}else
			return false;
	}//end of method
	
	public boolean deleteLink(String srcIP, String destIP)	{
		int sid = Router.getNodeID(srcIP);
		int did = Router.getNodeID(destIP);	
		if( this.graph.containsKey(sid) && this.graph.containsKey(did) )	{
			this.graph.get(sid).put(did, 0.00F);
			this.graph.get(did).put(sid, 0.00F);
			return true;
		}else
			return false;
	}//end of method
	
	public boolean markLinkFaulty(String srcIP, String destIP)	{
		int sid = Router.getNodeID(srcIP);
		int did = Router.getNodeID(destIP);	
		if( this.graph.containsKey(sid) && this.graph.containsKey(did) )	{
			float sw = this.graph.get(sid).get(did);
			float dw = this.graph.get(did).get(sid);
			float w = (sw>dw?dw:sw)/2;
			this.graph.get(sid).put(did, w);
			this.graph.get(did).put(sid, w);
			return true;
		}else
			return false;
	}//end of method
	
	public float getLinkWeight(String srcIP, String destIP)	{
		int sid = Router.getNodeID(srcIP);
		int did = Router.getNodeID(destIP);
		if( this.graph.containsKey(sid) && this.graph.containsKey(did) )	{
			return this.graph.get(sid).get(did);
		}else
			return 0.0F;
	}//end of method
	
	public boolean hasLink(String srcIP, String destIP)	{
		return this.getLinkWeight(srcIP, destIP)>0.00F;
	}//end of method
	
	public Map<Integer, Float> getLinksOf(String nodeip)	{
		int nid = Router.getNodeID(nodeip);
		return this.getLinksOf(nid);
	}
	
	public Map<Integer, Float> getLinksOf(int nodeid)	{	
		
		if( this.graph.containsKey(nodeid) )	{
			return this.graph.get(nodeid);
		}else
			return new HashMap<Integer, Float>(0);
		
	}//end of method
	
	/** Only for test purpose */
	public void addNode(int id) {
		Map<Integer, Float> links = new HashMap<Integer, Float>();
		this.graph.put(id, links);
	}
	
}
