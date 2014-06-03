/*
 * Created on Jan 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.utk.cs.loci.exnode;

/**
 * @author millar
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ProgressEvent
{
	/* Instance variables */
	private final String statusMsg;
	private final long   total;
	private final double percentComplete;
	private final double thoughput;
	private final double eta;
	
    public ProgressEvent(String statusMsg, long total, double percentComplete, 
    		double throughput, double eta)
    {
    	this.statusMsg = statusMsg;
    	this.total     = total;
    	this.percentComplete = percentComplete;
    	this.thoughput  = throughput;
    	this.eta 	    = eta;
    }

	public String getStatusMsg() 
	{
		return statusMsg;
	}

	public long getTotal() 
	{
		return total;
	}

	public double getPercentComplete() 
	{
		return percentComplete;
	}

	public double getThoughput() 
	{
		return thoughput;
	}

	public double getETA() 
	{
		return eta;
	}
}