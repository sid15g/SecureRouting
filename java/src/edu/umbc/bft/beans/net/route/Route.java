package edu.umbc.bft.beans.net.route;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.umbc.bft.router.main.Router;

public class Route implements Serializable, Cloneable, Iterator<String> {

	private static final long serialVersionUID = -7094043154007827259L;

	private List<String> list;
	private String source;
	private int current;
	
	public Route(List<String> list) {
		this.source = "";
		this.list = list;
		this.current = 0;
	}//end of constructor

	public void setSource(String source) {
		this.source = source;
	}
	
	@Override
	@Deprecated
	public void remove() {}

	@Override
	public boolean hasNext() {
		if( this.list != null )
			return this.current < this.list.size();
		else 
			return false;
	}

	public String prev()	{
		if( this.current >= 2 )
			return this.list.get(this.current-2);
		else
			return this.source;
	}
	
	public String current()	{
		if( this.current >= 1 )
			return this.list.get(this.current-1);
		else
			return this.list.get(0);
	}
	
	@Override
	public String next()	{
		if( this.hasNext() )
			return this.list.get(this.current++);
		else	{
			return null;
		}
	}

	public int ttl() {
		return this.list.size() - this.current;
	}
	
	public int length() {
		return this.list.size();
	}

	public boolean contains(String o) {
		if( o!=null && this.hasNext() )	{
			for(int i=this.current; i<this.list.size(); i++ )	{
				if( this.list.get(i).equals(o) )
					return true;
			}
		}
		return false;
	}//end of method
	
	
	public int getHopsFromSource() {
		
		if( Router.serverIP.equals( this.source ) )	{
			return 0;
		}else	{
			
			for(int i=0; i<this.list.size(); i++ )	{
				if( this.list.get(i).equals(Router.serverIP) )
					return i+1;
			}//end of loop
			
		}
		
		return -1;
		
	}//end of method
	
	public byte[] toByteArray()		{
		
		final int size = 4 *(Router.maxHops+1);			//128; Per IPAddress = 4 bytes
		ByteBuffer bf = ByteBuffer.allocate(size);
		
		for(int i=0; i<this.list.size(); i++)	{
			bf.put(this.list.get(i).getBytes());
		}
		
		bf.put(this.source.getBytes());
		return bf.array();
		
	}//end of method
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		List<String> list = new ArrayList<String>(this.list.size());
		
		for( String s: this.list )
			list.add(s);
		
		Route r = new Route(list);
		r.setSource(this.source);
		return r;
	}//end of route

}
