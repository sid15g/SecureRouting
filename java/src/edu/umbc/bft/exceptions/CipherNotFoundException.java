package edu.umbc.bft.exceptions;

import java.security.NoSuchAlgorithmException;

public class CipherNotFoundException extends NoSuchAlgorithmException	{

	private static final long serialVersionUID = -8679378620250158632L;
	
	public CipherNotFoundException(String cipherName) {
		super(" Cipher NOT found with name "+ cipherName);
	}//end of constructor

}