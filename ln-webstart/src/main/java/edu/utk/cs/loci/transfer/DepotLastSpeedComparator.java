/*
 * Created on Jan 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.utk.cs.loci.transfer;

import java.util.Comparator;

import edu.utk.cs.loci.ibp.Log;

/**
 * @author lfgs
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DepotLastSpeedComparator implements Comparator<DepotScore>
{
    public static Log DEBUG = new Log( true );

    public static final DepotLastSpeedComparator singleton = new DepotLastSpeedComparator();

    /* 
     * Returns a positive if o1 has a LOWER threadCount than o2.
     * (The rationale is that a depot with fewer outstanding connections
     * is better.)
     */
    public int compare(DepotScore o1, DepotScore o2 )
    {
        TransferStat ts1 = ((DepotScore) o1).getDepotStat();
        TransferStat ts2 = ((DepotScore) o2).getDepotStat();

        String d1Name = ts1.getHostname() + ":" + ts1.getPort();
        String d2Name = ts2.getHostname() + ":" + ts2.getPort();

        DEBUG.print( "Compare: " );

        if ( ts1.isLastTimeFailed() && !ts2.isLastTimeFailed() )
        {
            DEBUG.println( d1Name + " failed last time, while " + d2Name
                + " did not." );
            return -1;
        }
        else if ( !ts1.isLastTimeFailed() && ts2.isLastTimeFailed() )
        {
            DEBUG.println( d2Name + " failed last time, while " + d1Name
                + " did not." );
            return 1;
        }
        else if ( ts1.isLastTimeFailed() && ts2.isLastTimeFailed() )
        {
            double fr1 = ts1.getFailureRate();
            double fr2 = ts2.getFailureRate();
            DEBUG.println( d1Name + " fail rate=" + fr1 + ", " + d2Name
                + " fail rate=" + fr2 );

            // LOWER FAILURE rate should return positive
            int comp = Double.compare( fr2, fr1 );
            if ( comp == 0 )
            {
                // If ts2 failed before ts2 then it's better.
                // (Pick the least recently failed one as better.)
                comp = (ts1.getLastFailTime() < ts2.getLastFailTime()) ? 1 : -1;
            }
            return comp;
        }

        // else neither of their last times are failed

        double lSpeed1 = ts1.getLastBlockTransferSpeed().getSpeed();
        double lSpeed2 = ts2.getLastBlockTransferSpeed().getSpeed();

        DEBUG.println( d1Name + " ls=" + lSpeed1 + ", " + d2Name + " ls="
            + lSpeed2 );

        if ( (lSpeed1 == lSpeed2)
            || (Double.isNaN( lSpeed1 ) && Double.isNaN( lSpeed2 )) )
        {
            // if speeds are the same or speeds haven't been measured yet,
            // then ts1 is better if ts2 has more threads

            int tc1 = ts1.getThreadCount();
            int tc2 = ts2.getThreadCount();

            DEBUG.println( d1Name + " tc=" + tc1 + ", " + d2Name + " tc=" + tc2 );

            return (tc2 - tc1);
        }
        else
        {
            return (lSpeed1 > lSpeed2) ? 1 : -1;
        }
    }

}