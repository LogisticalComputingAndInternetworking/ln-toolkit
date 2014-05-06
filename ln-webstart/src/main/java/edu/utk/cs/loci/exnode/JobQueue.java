/* $Id: JobQueue.java,v 1.4 2008/05/24 22:25:52 linuxguy79 Exp $ */

package edu.utk.cs.loci.exnode;

import java.util.ArrayDeque;
import java.util.Deque;

public class JobQueue
{
    private Deque<Job> queue;

    public JobQueue()
    {
        queue = new ArrayDeque<Job>();
    }

    public synchronized void clear()
    {
        queue.clear();
    }

    public synchronized void add(Job o )
    {
        queue.add( o );
    }

    public synchronized void remove(Job o )
    {
        queue.remove( o );
    }

    public synchronized Job remove()
    {
        return (queue.removeFirst());
    }

    public synchronized int size()
    {
        return (queue.size());
    }
}