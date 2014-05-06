package edu.utk.cs.loci.ibp;


public class PortMismatchException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -1383507232351727764L;

	public PortMismatchException()
    {
        super( "Capability ports don't match" );
    }
}