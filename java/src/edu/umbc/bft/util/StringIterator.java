package edu.umbc.bft.util;

import java.util.Iterator;

public class StringIterator implements Iterator<String>	{

	private final int size;
	private int current;
	private String str;
	
	public StringIterator(String str) {
		this(str, 127);
	}	
	public StringIterator(String str, int size) {
		this.current = 0;
		this.size = size;
		this.str = str;
	}
	
	@Override
	public boolean hasNext() {
		return this.current<str.length();
	}

	@Override
	public String next()	{
		if( this.current+size < str.length()  )	{
			String temp = this.str.substring(this.current, this.current+this.size);
			this.current = this.current+this.size;
			return temp;
		}else	{
			String temp = this.str.substring(this.current, this.str.length());
			this.current = this.current+this.size;
			return temp;
		}
	}

}
