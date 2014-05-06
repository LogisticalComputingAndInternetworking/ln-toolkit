/*
 * Created on Mar 9, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.utk.cs.loci.lodnpublisher;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * @author millar
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ContentsAction extends AbstractAction
{

    /**
	 * 
	 */
	private static final long serialVersionUID = -8889973626463377970L;

	public ContentsAction()
    {
        super( "Contents" );
    }

    public void actionPerformed( ActionEvent e )
    {
        try
        {
            //ClassLoader loader=LoDNClient.class.getClassLoader();

            //URL helpsetURL=new URL("jar:http://localhost/~jp/LoDNHelp.jar!/lodnhelp/helpupload.hs");
            URL helpsetURL = new URL(
                "jar:http://promise.sinrg.cs.utk.edu/lodn/LoDNHelp.jar!/lodnhelp/helpupload.hs" );

            //URL helpsetURL=HelpSet.findHelpSet(null,"LoDNHelp/help.hs");
            //System.out.println(helpsetURL);
            HelpSet hs = new HelpSet( null, helpsetURL );
            //System.out.println(hs.getHelpSetURL().toString());
            HelpBroker broker = hs.createHelpBroker();
            broker.setDisplayed( true );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
            JOptionPane.showMessageDialog( null, "Error loading help: "
                + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE );
        }
    }

}