/*
 * Created on Jan 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.utk.cs.loci.lodnclient;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

/**
 * @author millar
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class OutputBrowseAction extends AbstractAction
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -1271767697627200278L;
	private LoDNClient client;

    public OutputBrowseAction( LoDNClient client )
    {
        super( "Browse" );
        this.client = client;
    }

    public void actionPerformed( ActionEvent e )
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile( new File( client.outputFileField.getText() ) );

        int result = chooser.showSaveDialog( null );

        if ( result == JFileChooser.APPROVE_OPTION )
        {
            client.outputFileField.setText( chooser.getSelectedFile().getPath() );
        }
    }

}