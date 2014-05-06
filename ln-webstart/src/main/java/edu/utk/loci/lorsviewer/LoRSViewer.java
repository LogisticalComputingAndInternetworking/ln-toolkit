package edu.utk.loci.lorsviewer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.channels.SeekableByteChannel;
import java.io.InputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.nio.ByteBuffer;
import java.lang.StringBuffer;

import tcl.lang.*;

import static java.nio.file.StandardOpenOption.*;

public class LoRSViewer
{
    /* Class constants */
    final private String SCRIPT_FILE = "/edu/utk/loci/lorsviewer/lat_display.tcl";
    final private String MAP_FILE    = "/edu/utk/loci/lorsviewer/newusa_55_-130_25_-60_.gif";
    final private String CONFIG_FILE    = "/edu/utk/loci/lorsviewer/newusa.cfg";
    
    /* Instance variables */
    final private int port;
    
    
    public LoRSViewer(int port)
    {
        this.port = port;
    }
    
    public void start() throws IOException
    {
        /* Creates the temporary directory to be used by the lors viewer */
        Path tempDir = Files.createTempDirectory(null);
        
        /* Paths to the temporary resource files */
        Path tempScriptFile = tempDir.resolve("lat_display.tcl");
        Path tempMapFile    = tempDir.resolve("newusa_55_-130_25_-60_.gif");
        Path tempConfigFile = tempDir.resolve("newusa.cfg");
        
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
            configString = configString.replaceAll("<PORT>", Integer.toString(this.port));
            
            tempConfigChannel.write(ByteBuffer.wrap(configString.getBytes()));
        }
        
        
        SwkShell.main(new String[]{ tempScriptFile.toString(), "-config",  tempConfigFile.toString()});
        
        System.out.printf("Done.");
        
    }
    
    public static void main(String[] args) throws IOException
    {
        new LoRSViewer(8000).start();
    }
}