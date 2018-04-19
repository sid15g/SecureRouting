package edu.umbc.bft.router.extras;

public class NodeDetail {

	private int id;
	private String ip;
	private int port;
	
	public NodeDetail(int id, String ip, int port) {
		this.id = id;
		this.ip = ip;
		this.port = port;
	}//end of constructor
	
	public int getId() {
		return this.id;
	}
	public String getIp() {
		return this.ip;
	}
	public int getPort() {
		return this.port;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if( obj instanceof NodeDetail )	{
			NodeDetail nd = (NodeDetail)obj;
			boolean res = this.ip.equals(nd.ip);
			res &= this.port == nd.port;
			return res;
		}else
			return false;
	}//end of method
	
	@Override
	public int hashCode() {
		return this.id;
	}
	
}