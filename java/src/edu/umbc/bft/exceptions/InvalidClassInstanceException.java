package edu.umbc.bft.exceptions;

public class InvalidClassInstanceException extends ClassCastException {

	private static final long serialVersionUID = 8768371541137664073L;
	
	public InvalidClassInstanceException(Class<?> expected, Class<?> found) {
		super(" Expected class of type "+ expected.getName() +" and found "+ found.getName() );
	}//end of constructor

}
