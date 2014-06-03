package edu.utk.cs.loci.ibp;

import java.net.URI;
import java.net.URISyntaxException;

public class Capability
{

    // Instance variables
    URI uri;

    // Constructors
    public Capability( String uri ) throws URISyntaxException
    {
        this.uri = new URI( uri );
    }

    // Instance methods
    public String getProtocol()
    {
        return (uri.getScheme());
    }

    public String getHost()
    {
        return (uri.getHost());
    }

    public String getPort()
    {
        return (new Integer( uri.getPort() ).toString());
    }

    public String getKey()
    {
        String str = uri.toString();
        String[] fields = str.split( "/" );
        return (fields[3]);
    }

    public String getWRMKey()
    {
        String str = uri.toString();
        String[] fields = str.split( "/" );
        return fields[4];
    }

    public String getType()
    {
        String str = uri.toString();
        String[] fields = str.split( "/" );
        return (fields[5]);
    }

    public String toString()
    {
        return (uri.toString());
    }
}