/*
 * Created on Nov 24, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.utk.cs.loci.exnode;

import java.util.Vector;

/**
 * @author millar
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class VectorArgument extends Argument
{
	Vector<Argument> value = null;
	
    public VectorArgument( String name )
    {
        this.name = name;
        this.value = new Vector<Argument>();
    }

    public void insert( int i, Argument arg )
    {
		Vector<Argument> v = value;

        if ( i > v.size() )
        {
            v.setSize( i );
        }

        v.add( i, arg );
    }

    public Argument getElement( int i ) throws ArrayIndexOutOfBoundsException
    {
        return value.elementAt(i);
    }
}