/*
 * Created on Nov 21, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.utk.cs.loci.exnode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author millar
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ListMetadata extends Metadata
{
    public ListMetadata( String name )
    {
        this.name = name;
        this.value = new HashMap<String, Metadata>();
    }

    public void add( Metadata md )
    {
        @SuppressWarnings("unchecked")
		Map<String, Metadata> map = (Map<String, Metadata>) value;
        map.put( md.getName(), md );
    }

    public void remove( String name )
    {
        @SuppressWarnings("unchecked")
		Map<String, Metadata> map = (Map<String, Metadata>) value;
        map.remove( name );
    }

    public Metadata getChild( String name )
    {
        @SuppressWarnings("unchecked")
		Map<String, Metadata> map = (Map<String, Metadata>) value;
        return ((Metadata) map.get( name ));
    }

    public Iterator<Metadata> getChildren()
    {
        @SuppressWarnings("unchecked")
		Map<String, Metadata> map = (Map<String, Metadata>) value;
        List<Metadata> list = new ArrayList<Metadata>();
        Set<String> keys = map.keySet();
        Iterator<String> i = keys.iterator();
        String name;
        while ( i.hasNext() )
        {
            name = i.next();
            list.add( getChild( name ) );
        }
        return (list.iterator());
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();

        buf.append( "Metadata(" + name + ",LIST," + "(" );

        Iterator<Metadata> i = getChildren();
        Metadata md;
        while ( i.hasNext() )
        {
            md = i.next();
            buf.append( md.toString() + "," );
        }

        buf.append( "))" );

        return (buf.toString());
    }

    public String toXML()
    {
        StringBuffer sb = new StringBuffer();

        sb.append( "<exnode:metadata name=\"" + name + "\" type=\"meta\">\n" );

        Iterator<Metadata> i = getChildren();
        Metadata child;
        while ( i.hasNext() )
        {
            child = i.next();
            sb.append( child.toXML() );
        }

        sb.append( "</exnode:metadata>\n" );

        return (sb.toString());
    }

    public static Metadata fromXML( Element e ) throws DeserializeException
    {
        String name = e.getAttribute( "name" );

        ListMetadata md = new ListMetadata( name );

        Metadata child;
        NodeList children = e.getElementsByTagNameNS(
            Exnode.namespace, "metadata" );
        for ( int i = 0; i < children.getLength(); i++ )
        {
            child = Metadata.fromXML( (Element) children.item( i ) );
            md.add( child );
        }

        return (md);
    }
}