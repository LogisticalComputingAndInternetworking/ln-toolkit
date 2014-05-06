package edu.utk.cs.loci.ibp;


public class HostMismatchException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6570941402395787154L;

	public HostMismatchException()
    {
        super( "Capability hosts don't match" );
    }
}