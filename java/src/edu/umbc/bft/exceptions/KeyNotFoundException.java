package edu.umbc.bft.exceptions;

public class KeyNotFoundException extends Exception {

	private static final long serialVersionUID = -1295515754052496018L;
	
	public KeyNotFoundException() {
		super(" Key Not found ");
	}
	public KeyNotFoundException(String ip) {
		super(" Key Not found for node "+ ip);
	}
	
	@Override
	public void printStackTrace() {
		super.printStackTrace();
	}

}
