/*
 * Created on Nov 24, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.utk.cs.loci.exnode;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author millar
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class IntegerArgument extends Argument
{
	Integer value = null;
	
    public IntegerArgument( String name, Integer value )
    {
        this.name = name;
        this.value = value;
    }

    public Integer getInteger()
    {
        return value;
    }

    public String toXML()
    {
        StringBuffer sb = new StringBuffer();

        sb.append( "<exnode:argument name=\"" + name + "\" type=\"integer\">" );
        sb.append( (Integer) value );
        sb.append( "</exnode:argument>\n" );

        return (sb.toString());
    }

    public static Argument fromXML( Element e )
    {
        String name = e.getAttribute( "name" );
        Node child = e.getFirstChild();
        Integer value = new Integer( child.getNodeValue() );

        return (new IntegerArgument( name, value ));
    }
}