/* $Id: Progress.java,v 1.4 2008/05/24 22:25:52 linuxguy79 Exp $ */

package edu.utk.cs.loci.exnode;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

import edu.utk.cs.loci.ibp.Log;

public class Progress
{
    public static Log DEBUG = new Log( true );

    private long length;
    private long total;
    private double percentComplete;
    private double throughput;
    private double eta;
    private Date start;
    private List<ProgressListener> listeners;

    private String statusMsg;

    
    public Progress( long length )
    {
        this.length = length;
        this.total = 0;
        this.percentComplete = 0.0;
        this.throughput = 0.0;
        this.eta = 0.0;
        this.start = new Date();
        this.listeners = new ArrayList<ProgressListener>();
        this.statusMsg = "";
    }

    public void addProgressListener( ProgressListener listener )
    {

        DEBUG.println( "addProgressListener!" );
        listeners.add( listener );
    }

    public void update( long progress )
    {
        this.update( progress, "Continuing ..." );
    }

    public void update( long progress, String statusMsg )
    {
    	Long remaining = null;

    	synchronized(this) 
    	{
    		this.statusMsg = statusMsg;

    		total += progress;

    		percentComplete = ( (double) total / (double) length ) * 100.0f;

    		Date now = new Date();
    		double elapsed = (now.getTime() - start.getTime()) / 1000.0;

    		throughput = (total * 8.0) / 1024.0 / 1024.0 / elapsed;

    		remaining = length - total;
    		eta = (double) remaining * elapsed / (double) total;


    		DEBUG.println( "Exnode Progress: " + elapsed + "s, " + total
    				+ " bytes = " + (throughput / 8.0) + "MB/s, eta=" + eta + "s" );
    	}

    	if(remaining != 0)
    	{
    		fireProgressChangedEvent();
    		DEBUG.println( "Progress (update):01 remaining = " + remaining );
    	}
    	else
    	{
    		fireProgressChangedEvent();
    		fireProgressDoneEvent();
    		DEBUG.println( "Progress (update):FINAL remaining = " + remaining );
    	}

    }

    public synchronized void fireProgressChangedEvent()
    {
        final ProgressEvent e = new ProgressEvent(statusMsg, total, 
    			percentComplete, throughput, eta);
        
        for(final ProgressListener progressListener : listeners)
        {
        	SwingUtilities.invokeLater(new Runnable() 
        	{
				@Override
				public void run() 
				{
					progressListener.progressUpdated(e);		
				}
			});
        	
        }
    }

    public synchronized void fireProgressDoneEvent()
    {
    	
        try
        {
            final ProgressEvent e = new ProgressEvent(statusMsg, total, 
        			percentComplete, throughput, eta);
            
            for(final ProgressListener progressListener : listeners)
            {
            	SwingUtilities.invokeLater(new Runnable() 
            	{
					@Override
					public void run() 
					{
						progressListener.progressDone( e );
					}
				});
            }

        }catch ( Exception excpt )
        {
            DEBUG.error( "fireProgressDoneEvent: " + excpt );
        }
    }

    public synchronized void fireProgressErrorEvent()
    {
    	final ProgressEvent e = new ProgressEvent(statusMsg, total, 
    			percentComplete, throughput, eta);
        
        for(final ProgressListener progressListener : listeners)
        {
        	SwingUtilities.invokeLater(new Runnable() 
        	{
				@Override
				public void run() 
				{
					progressListener.progressError( e );
				}
        	});
        }
    }

    public long getProgress()
    {
        return (total);
    }

    public double getPercentComplete()
    {
        return (percentComplete);
    }

    public double getThroughput()
    {
        return (throughput);
    }

    public double getETA()
    {
        return (eta);
    }

    public String getStatusMsg()
    {
        return this.statusMsg;
    }

    public synchronized void setDone()
    {
        this.statusMsg = "Done.";
        percentComplete = 100.0;
        eta = 0.0;
        // lfgs: don't set to 0 so you can still see the final throughput
        // throughput = 0.0;
    }

}