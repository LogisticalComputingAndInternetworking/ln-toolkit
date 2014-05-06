package edu.utk.cs.loci.lodnpublisher;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class CnxTypeAction extends AbstractAction
{

    /**
	 * 
	 */
	private static final long serialVersionUID = -7117133833742755500L;
	private LoDNPublisher pub;

    public CnxTypeAction( LoDNPublisher pub )
    {
        this.pub = pub;
    }

    public void actionPerformed( ActionEvent e )
    {
        String ac = e.getActionCommand();

        if ( ac.equals( LoDNPublisher.dialupString ) )
        {
            pub.connectionsCombo.setSelectedIndex( 0 );
            pub.sizeField.setText( "512" );
            pub.kiloButton.setSelected( true );

        }
        else if ( ac.equals( LoDNPublisher.dslcableString ) )
        {
            pub.connectionsCombo.setSelectedIndex( 1 );
            pub.sizeField.setText( "1024" );
            pub.kiloButton.setSelected( true );

        }
        else if ( ac.equals( LoDNPublisher.t3String ) )
        {
            pub.connectionsCombo.setSelectedIndex( 2 );
            pub.sizeField.setText( "2" );
            pub.megaButton.setSelected( true );

        }
        else if ( ac.equals( LoDNPublisher.highspeedString ) )
        {
            pub.connectionsCombo.setSelectedIndex( 3 );
            pub.sizeField.setText( "10" );
            pub.megaButton.setSelected( true );

        }
    }

}