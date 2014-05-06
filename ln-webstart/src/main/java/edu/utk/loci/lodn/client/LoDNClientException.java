package edu.utk.loci.lodn.client;

public class LoDNClientException extends Exception 
{
	/*Serialization stuff */ 
	private static final long serialVersionUID = 3863738863868294409L;

	public LoDNClientException(String msg)
	{
		super(msg);
	}

	public LoDNClientException(String msg, Throwable exc) 
	{
		super(msg, exc);
	}
}
