/*
 * Created on Nov 24, 2003
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
public class FunctionArgument extends Argument
{
	Function value = null;
	
    public FunctionArgument( String name, Function f )
    {
        this.name = name;
        this.value = f;
    }

    public Function getFunction()
    {
        return value;
    }

    public String toXML()
    {
        return value.toXML();
    }
}