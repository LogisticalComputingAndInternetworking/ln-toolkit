/*
 * Created on Feb 10, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.utk.cs.loci.transfer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.utk.cs.loci.ibp.Log;

/**
 * @author lfgs
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TransferStatListDisplay extends JFrame
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static Log DEBUG = new Log( true );

    protected Map<String, TransferStat> statsMap = null;
    protected Map<TransferStat, TransferStatPanel> statPanels = 
    		new HashMap<TransferStat, TransferStatPanel>();
    protected JScrollPane scrollPane;
    protected JPanel statListPanel;
    protected TransferStatPanel header;

    public TransferStatListDisplay( Map<String, TransferStat> depotStats )
    {
        DEBUG.println( "TransferStatListDisplay(): " + depotStats );

        this.statsMap = depotStats;

        Container c = this.getContentPane();

        c.setLayout( new BorderLayout() );
        this.header = new TransferStatPanel( null );
        //        c.add( this.header, BorderLayout.NORTH );

        this.statListPanel = new JPanel();
        this.statListPanel.setSize( 800, 600 );
        this.statListPanel.setLayout( new BoxLayout(
            this.statListPanel, BoxLayout.Y_AXIS ) );
        this.statListPanel.add( this.header );

        this.scrollPane = new JScrollPane(
            statListPanel );
        //, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        //    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

        c.add( this.scrollPane, BorderLayout.CENTER );

        DEBUG.println( "TransferStatListDisplay(): finished creating scroll pane.  Calling updateStats() ... " );

        if ( this.statsMap != null )
        {
            this.updateStats();
        }
        this.setSize( 800, 600 );
        this.validate();

    }

    public void updateStats()
    {
        if ( ( this.statsMap != null ) && ( this.isVisible() ) )
        {
            synchronized ( statsMap )
            {
                Collection<TransferStat> c = this.statsMap.values();
                boolean hasNew = false;

                if ( c != null )
                {

                    for(Iterator<TransferStat> i = c.iterator(); i.hasNext();)
                    {
                        TransferStat ts = (TransferStat) i.next();

                        //DEBUG.println( "Checking TransferStat " + ts );

                        TransferStatPanel tsPanel = (TransferStatPanel) this.statPanels.get( ts );
                        if ( tsPanel == null )
                        {
                            tsPanel = new TransferStatPanel( ts );
                            this.statPanels.put( ts, tsPanel );
                            this.statListPanel.add( tsPanel );
                            tsPanel.updateStats();
                            hasNew = true;
                        }
                        else
                        {
                            tsPanel.updateStats();
                        }
                    }

                }
                if ( hasNew )
                {
                    this.scrollPane.validate();
                }
            }
        }
    }

}