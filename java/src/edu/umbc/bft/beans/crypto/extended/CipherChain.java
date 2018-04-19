package edu.umbc.bft.beans.crypto.extended;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.umbc.bft.router.main.DatagramFactory;

public class CipherChain implements Serializable, Cloneable, Iterator<String> {

	private static final long serialVersionUID = 4893402629287403766L;
	
	protected List<String> ciphers;
	protected int current;

	CipherChain(List<String> ciphers) {
		this.ciphers = ciphers;
		this.current = 0;
	}//end of constructor
	
	@Override
	@Deprecated
	public void remove() {}
	

	@Override
	public boolean hasNext() {
		return this.current < this.ciphers.size();
	}

	@Override
	public String next() {
		if( this.hasNext() )
			return this.ciphers.get(this.current++);
		else	{
			return null;
		}
	}

	public int ttl() {
		return this.ciphers.size() - this.current;
	}
	
	public int length() {
		return this.ciphers.size();
	}
	

	public boolean contains(String o) 	{
		if( o!=null && this.hasNext() )	{
			for(int i=this.current; i<this.ciphers.size(); i++ )	{
				if( this.ciphers.get(i).equals(o) )
					return true;
			}
		}
		return false;
	}//end of method
	
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		List<String> list = new ArrayList<String>();
		
		for( String s: this.ciphers )
			list.add(s);
		
		CipherChain cc = new CipherChain(list);
		cc.current = this.current;
		return cc;
	}//end of method
	
	
	@Override
	public String toString() {
		return DatagramFactory.serialize(this.ciphers);
	}

}