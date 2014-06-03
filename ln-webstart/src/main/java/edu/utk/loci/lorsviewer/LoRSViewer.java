package edu.utk.loci.lorsviewer;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;

import tcl.lang.Interp;
import tcl.lang.Notifier;
import tcl.lang.TCL;
import tcl.lang.TclBoolean;
import tcl.lang.TclException;
import tcl.lang.TclInteger;
import tcl.lang.TclList;
import tcl.lang.TclObject;
import tcl.lang.TclString;

public class LoRSViewer
{
    /* Class constants */
    final private static String SCRIPT_FILE = "/edu/utk/loci/lorsviewer/lat_display.tcl";
    final private static String MAP_FILE    = "/edu/utk/loci/lorsviewer/newusa_55_-130_25_-60_.gif";
    final private static String CONFIG_FILE    = "/edu/utk/loci/lorsviewer/newusa.cfg";
    
    /* Class variables */
    static LoRSViewer globalLoRSViewer = null;
	
    /* Instance variables */
    final private Thread viewerThread;
    final private CountDownLatch viewerReady = new CountDownLatch(1);
    final private CountDownLatch socketChannelReady = new CountDownLatch(1);
    final private int port;
    SocketChannel viewerChannel = null;
    
    
    public LoRSViewer(final int port)
    {
        this.port = port;
    
        this.viewerThread = new Thread(new Runnable()
    	{
			@Override
			public void run() 
			{
				try
				{
					/* Creates the temporary directory to be used by the lors viewer */
			        Path tempDir = Files.createTempDirectory(null);
			        
			        /* Paths to the temporary resource files */
			        final Path tempScriptFile = tempDir.resolve("lat_display.tcl");
			        final Path tempMapFile    = tempDir.resolve("newusa_55_-130_25_-60_.gif");
			        final Path tempConfigFile = tempDir.resolve("newusa.cfg");
			        
			        System.out.printf("tempDir = %s\n", tempDir);
			        try(InputStream scriptFileInput = getClass().getResourceAsStream(SCRIPT_FILE);
			            InputStream mapFileInput = getClass().getResourceAsStream(MAP_FILE);
			            InputStream configFileInput = getClass().getResourceAsStream(CONFIG_FILE);
			            SeekableByteChannel tempScriptChannel =
			                Files.newByteChannel(tempScriptFile, EnumSet.of(CREATE_NEW,READ,WRITE));
			            SeekableByteChannel tempMapChannel =
			                Files.newByteChannel(tempMapFile, EnumSet.of(CREATE_NEW,READ,WRITE));
			            SeekableByteChannel tempConfigChannel =
			                Files.newByteChannel(tempConfigFile, EnumSet.of(CREATE_NEW,READ,WRITE)))
			        {
			            byte[] transferArray = new byte[8192];
			            
			            for(int amtRead = scriptFileInput.read(transferArray);
			                amtRead > -1;
			                amtRead = scriptFileInput.read(transferArray))
			            {
			                tempScriptChannel.write(ByteBuffer.wrap(transferArray, 0, amtRead));
			            }
			            
			            for(int amtRead = mapFileInput.read(transferArray);
			                amtRead > -1;
			                amtRead = mapFileInput.read(transferArray))
			            {
			                tempMapChannel.write(ByteBuffer.wrap(transferArray, 0, amtRead));
			            }
			            
			            StringBuffer configStringBuilder = new StringBuffer();
			            
			            for(int amtRead = configFileInput.read(transferArray);
			                amtRead > -1;
			                amtRead = configFileInput.read(transferArray))
			            {
			                configStringBuilder.append(new String(transferArray, 0, amtRead));
			                
			            }
			            
			            String configString = configStringBuilder.toString();
			            
			            configString = configString.replaceAll("<MAP_FILE>", tempMapFile.toString());
			            configString = configString.replaceAll("<PORT>", Integer.toString(port));
			            
			            tempConfigChannel.write(ByteBuffer.wrap(configString.getBytes()));
			        }catch(Exception e) 
			        {	
			        	throw e;
					}
			        
			        System.out.println("Ready to start viewer");
					
			        launchViewer(tempScriptFile.toString(), tempConfigFile.toString());
			        
			        
				}catch(Throwable e)
				{
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
    	});
    }
    
    public void start()
    {
        this.viewerThread.setDaemon(false);
        this.viewerThread.start();
       
       
        try 
        {
        	viewerReady.await();
        	Thread.sleep(1*1000);
			
        	this.viewerChannel = SocketChannel.open(new InetSocketAddress("localhost", port));
			this.viewerChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
			
			System.out.printf("connected = %b\n", viewerChannel.isConnected());
	
			ByteBuffer recvBuffer = ByteBuffer.wrap(new byte[1000]);
			viewerChannel.read(recvBuffer);
			
			System.out.printf("message = %s\n", new String(recvBuffer.array(), 0, recvBuffer.position()));

			this.socketChannelReady.countDown();
		}catch(IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void launchViewer(String scriptfile, String configfile) 
    		throws TclException 
    {
    	System.out.printf("scriptfile = %s configfile = %s\n", scriptfile, configfile);
    	
        /* Create the interpreter. */
        Interp interp = new Interp();

        try 
        {
            interp.evalResource("/com/onemoonscientific/swank/library/init.tcl");
        }catch (TclException e) 
        {
            System.out.println(
                "could not open /com/onemoonscientific/swank/library/init.tcl: "+e.toString());
            //e.printStackTrace();  
            //throw e;
        }

        

        TclObject argv = TclList.newInstance();
        argv.preserve();

         
        interp.setVar("argv0", TclString.newInstance(scriptfile),
        		TCL.GLOBAL_ONLY);
        interp.setVar("tcl_interactive", TclBoolean.newInstance(false),
        		TCL.GLOBAL_ONLY);


        //TclList.append(interp, argv, TclString.newInstance(scriptfile));
        TclList.append(interp, argv, TclString.newInstance("-config"));
        TclList.append(interp, argv, TclString.newInstance(configfile));


        interp.setVar("argv", argv, TCL.GLOBAL_ONLY);
        interp.setVar("argc", TclInteger.newInstance(3), TCL.GLOBAL_ONLY);

        argv.release();

        
        try 
        {
        	/* Run the script */
        	interp.evalFile(scriptfile);
        	
        	this.viewerReady.countDown();

        	Notifier notifier = interp.getNotifier();

        	/* Process all events until exit */
        	while(true) 
        	{
        		System.out.printf("Processing event\n");
        		//notifier.doOneEvent(TCL.ALL_EVENTS|TCL.DONT_WAIT);
        		Notifier.processTclEvents(notifier);
        	}

        }catch(TclException e) 
        {

        	switch(e.getCompletionCode())
        	{
	        	case TCL.RETURN: 
	
	        		if(interp.updateReturnInfo() != TCL.OK)
	        		{
	        			System.err.println("command returned bad code");
	        		}
	
	        		break;
	
	        	case TCL.ERROR:
	        		System.err.println(interp.getResult().toString());
	        		//e.printStackTrace();
	        		break;
	
	        	default:
	        		System.err.println("command returned bad code");
	        		e.printStackTrace();
	        		break;
        	}
        }finally
        {

        	// Note that if the above interp.evalFile() returns the main
        	// thread will exit.  This may bring down the VM and stop
        	// the execution of Tcl.
        	//
        	// If the script needs to handle events, it must call
        	// vwait or do something similar.
        	//
        	// Note that the script can create AWT widgets. This will
        	// start an AWT event handling thread and keep the VM up. However,
        	// the interpreter thread (the same as the main thread) would
        	// have exited and no Tcl scripts can be executed.
        	interp.dispose();
        	System.exit(0);
        }
    }

    
    public static LoRSViewer getLoRSViewer()
    {
    	if(globalLoRSViewer == null)
    	{
    		synchronized(LoRSViewer.class) 
    		{
    			if(globalLoRSViewer == null)
    	    	{
    				globalLoRSViewer = new LoRSViewer(8000);
    				globalLoRSViewer.start();
    	    	}
			}
    	}
    	
    	return globalLoRSViewer;
    }
    
    public void waitTilReady() throws InterruptedException
    {
    	this.socketChannelReady.await();
    }
    
    public void setMessage(String message, int position) throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("MESSAGE %d %s\n", position, message).getBytes()));
    }
    
    public void setTitle(String title) throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("TITLE %s\n",title).getBytes()));
    }
    
    public void setSize(long size) throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("SIZE %d\n", size).getBytes()));
    }
    
    public void drawMappingAllocate(String mappingID, long offset, long length, 
    		String depot) throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("DRAW MappingAllocate %s %d %d %s\n", 
					mappingID, offset, length, depot).getBytes()));
    }
    
    public void drawMappingBegin(String srcDepot, String mappingID, long offset, 
    		long length, String dstDepot) throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("DRAW MappingBegin %s %s %d %d %s\n", 
					srcDepot, mappingID, offset, length, dstDepot).getBytes()));
    }
    
    public void drawMappingFrom(String srcDepot, long srcOffset, long srcLength,
    		String mappingID, long dstOffset, long dstLength, String dstDepot) 
    				throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
    		String.format("DRAW MappingFrom %s %d %d %s %d %d %d\n", 
    				srcDepot, srcOffset, srcLength, mappingID, dstOffset, 
    				dstLength, dstDepot).getBytes()));
    }
    
    public void drawMappingFinish(long sourceOffset, long sourceLength, 
    		String mappingID, long dstOffset, long dstLength, String dstDepot) 
    				throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("DRAW MappingFinish %d %d %s %d %d %s\n", 
					sourceOffset, sourceLength, mappingID, dstOffset, 
					dstLength, dstDepot).getBytes()));
    }
    
    public void drawMappingEnd(String mappingID, long offset, long length, 
    		String depot, String text) throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("DRAW MappingEnd %s %d %d %s %s\n", mappingID, 
					offset, length, depot, text).getBytes()));
    }
    
    public void drawMapping(String mappingID, long offset, long length, 
    		String depot, String text) throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("DRAW Mapping %s %d %d %s %s\n", 
					mappingID, offset, length, depot, text).getBytes()));
    }
    
    public void drawMappingStoreArrow(String mappingID, long offset, long length, 
    		String depot) throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("DRAW Arrow1 dummy0 %s %d %d %s\n", 
					mappingID, offset, length, depot).getBytes()));
    }
    
    public void drawMappingLoadArrow(String depot, long offset, long length) 
    		throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("DRAW Arrow2 from %s %d %d\n", 
					depot, offset, length).getBytes()));
    }
    
    public void drawMappingCopyArrow(String depot, String dstMappingId) 
    		throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("DRAW Arrow3 from %s %s to 10\n", 
					depot, dstMappingId).getBytes()));
    }
    
    public void drawMappingDLBuffer(long offset, long length) throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("DRAW DLBuffer %d %d\n", offset, length).getBytes()));
    }
    
    public void drawMappingDLSlice(String depotID, long offset, long length) 
    		throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("DRAW DLSlice %s %d %d\n", depotID, offset, length)
				.getBytes()));
    }
    
    public void drawMappingOutput(long offset, long length) throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("DRAW Output %d %d\n", offset, length).getBytes()));
    }
    
    public void deleteMapping(String mappingID, long offset, long length, 
    		String depot) throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("DELETE MAPPING %s %d %d %s\n", 
					mappingID, offset, length, depot).getBytes()));
    }
    
    public void deleteStoreArrow(String mappingID, long offset, long length, 
    		String depot) throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
			String.format("DELETE Arrow1 from %s %d %d %s\n", 
					mappingID, offset, length, depot).getBytes()));
    }
    
    public void deleteLoadArrow(String depotID, long offset, long length) 
    		throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
    		String.format("DELETE Arrow2 from %s %d %d\n", 
    				depotID, offset, length).getBytes()));
    }
    
    public void deleteCopyArrow(String srcDepotID, String mappingID, String dstDepotID) throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
    		String.format("DELETE Arrow3 from %s %s to %s\n", 
    				srcDepotID, mappingID, dstDepotID).getBytes()));
    }
    
    public void clear() throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
    		String.format("CLEAR\n").getBytes()));
    }
    
    public void quit() throws IOException
    {
    	this.viewerChannel.write(ByteBuffer.wrap(
    		String.format("QUIT\n").getBytes()));
    }
    
    public static void main(String[] args) throws IOException, TclException
    {
        LoRSViewer lorsViewer = new LoRSViewer(8000);
        lorsViewer.start();
        
        lorsViewer.setTitle("Hello There Title");
    }
}