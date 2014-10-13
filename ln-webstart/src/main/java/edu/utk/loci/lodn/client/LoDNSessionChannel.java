package edu.utk.loci.lodn.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.DirectoryStream;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import com.google.protobuf.GeneratedMessage;

import edu.utk.cs.loci.exnode.Exnode;
import edu.utk.cs.loci.exnode.IdentityFunction;
import edu.utk.cs.loci.exnode.IntegerMetadata;
import edu.utk.cs.loci.exnode.Mapping;
import edu.utk.cs.loci.ibp.Allocation;
import edu.utk.cs.loci.ibp.Capability;
import edu.utk.cs.loci.ibp.HostMismatchException;
import edu.utk.cs.loci.ibp.PortMismatchException;
import edu.utk.loci.lodn.client.LoDNMessageProtos.GetMappingsReply;
import edu.utk.loci.lodn.client.LoDNMessageProtos.GetMappingsRequest;
import edu.utk.loci.lodn.client.LoDNMessageProtos.ResponseMsg;

public class LoDNSessionChannel 
{
	/* Instance variables */
	AsynchronousSocketChannel channel = null;
	int sessionID;
	int currMsgID; 
	
	LoDNSessionChannel(AsynchronousSocketChannel channel, int sessionID, int currMsgID)
	{
		this.channel = channel;
		this.sessionID = sessionID;
		this.currMsgID = currMsgID;
	}
	
	public <A> void mkdir(String path, CompletionHandler<Void, A> completionHandler)
	{
		throw new UnsupportedOperationException();
	}
	
	public Future<?> mkdir(String path)
	{
		throw new UnsupportedOperationException();
	}
	
	public <A> void rmdir(String path, CompletionHandler<Void, A> completionHandler)
	{
		throw new UnsupportedOperationException();
	}
	
	public Future<?> rmdir(String path)
	{
		throw new UnsupportedOperationException();
	}
	
	public <A> void createFile(String path, CompletionHandler<Void, A> completionHandler)
    {
		throw new UnsupportedOperationException();
    }
	
	public Future<?> createFile(String path)
    {
		throw new UnsupportedOperationException();
    }
	
	public <A> void chmod(String path, PosixFileAttributes mode, CompletionHandler<Void, A> completionHandler)
	{
		throw new UnsupportedOperationException();
	}
	
	public Future<?> chmod(String path, PosixFileAttributes mode)
	{
		throw new UnsupportedOperationException();
	}

	public <A> void unlink(String path, CompletionHandler<Void, A> completionHandler)
	{
		throw new UnsupportedOperationException();
	}
	
	public Future<?> unlink(String path)
	{
		throw new UnsupportedOperationException();
	}
	
	//public <A> void stat(String path, A attachment, CompletionHandler<struct stat *statbuf, A> completionHandler)
	//{
	//	throw new UnsupportedOperationException();
	//}
	
	//public Future<struct stat *statbuf> stat(String path)
	//{
	//	throw new UnsupportedOperationException();
	//}
	
	public <A> void getdir(String path, CompletionHandler<DirectoryStream<LoDNDirent>, A> completionHandler)
	{
		throw new UnsupportedOperationException();
	}
	
	public Future<DirectoryStream<LoDNDirent>> getdir(String path)
	{
		throw new UnsupportedOperationException();
	}
	
	public <A> void rename(String origpath, String newpath, CompletionHandler<Void, A> completionHandler)
	{
		throw new UnsupportedOperationException();
	}
	
	public Future<?> rename(String origpath, String newpath)
	{
		throw new UnsupportedOperationException();
	}
	
	//public <A> void getxattr(String path, String name,  CompletionHandler<void *value, size_t *size,
    //        u_int32_t position, A> completionHandler)
	//{
	//	throw new UnsupportedOperationException();
	//}
	
	//public Future<void *value, size_t *size, u_int32_t position> getxattr(String path, String name)
	//{
	//	throw new UnsupportedOperationException();
	//}
	
	public <A> void removexattr(String path, String name, CompletionHandler<Void, A> completionHandler)
	{
		throw new UnsupportedOperationException();
	}
	
	public Future<?> removexattr(String path, String name)
	{
		throw new UnsupportedOperationException();
	}
	
	public <A> void listxattr(String path, CompletionHandler<List<String>, A> completionHandler)
	{
		throw new UnsupportedOperationException();
	}
	
	public Future<List<String>> listxattr(String path)
	{ 
		throw new UnsupportedOperationException();
	}
	
	//public <A> void setxattr(String path, String name, void *value, size_t size,
    //                u_int32_t position, CompletionHandler<Void, A> completionHandler)
	//{
	//	throw new UnsupportedOperationException();
	//}
	
	//public Future<?> setxattr(String path, String name, void *value, size_t size,
    //        u_int32_t position)
	//{
	//	throw new UnsupportedOperationException();
	//}
	
	//public <A> void insertMappings(String path, LorsSet *lorsSet, CompeletionHandler<Void, A> completionHandler)
	//{
	//	throw new UnsupportedOperationException();
	//}
	
	//public Future<?> insertMappings(String path, LorsSet *lorsSet)
	//{
	//	throw new UnsupportedOperationException();
	//}
	
	public <A> void getMappings(String path, int offset, int num_mappings, 
			final A userAttachment, 
			final CompletionHandler<Collection<Mapping>, A> userCompletionHandler)
	{
		/* Increments the message id */
		this.currMsgID++;
		
		System.out.printf("Message ID = %d\n", this.currMsgID);
		
		/* Generates a message to fetching the mappings */
		GetMappingsRequest message = GetMappingsRequest.newBuilder()
				.setMsgID(this.currMsgID)
				.setPath(path)
				.setMappingOffset(offset)
				.setNumMappings(num_mappings)
				.build();
		
		System.out.printf("Message = %s\n", message);
		
		/* Sends the message to LoDN with a handler for the return message */
		LoDNIO.sendMessage(this.channel, MessageType.GETMAPPINGS_REQUEST, message, 
			new CompletionHandler<Void, Void>() 
			{
				@Override
				public void completed(Void result, Void attachment) 
				{
					System.out.println("Sent message for getting mappings");
					
					/* Handle the response from LoDN */
					LoDNIO.readMessage(channel, 
						new CompletionHandler<MessageType, GeneratedMessage>()
						{
							@Override
							public void completed(MessageType messageType, 
									GeneratedMessage message) 
							{
								System.out.printf("Recieved message for mappings = %s\n", message);
								
							
								switch(messageType)
								{
									/* Success, build mapping response */
									case GETMAPPINGS_REPLY:
										GetMappingsReply mappingsReply = 
											(GetMappingsReply)message;
										
										/* Creates the list of mappings to return */
										List<Mapping> mappings = new ArrayList<>(mappingsReply.getMappingsCount());
										
										try 
										{
											/* Processes each mapping recieved */
											for(edu.utk.loci.lodn.client.LoDNDatatypesProtos.Mapping mapping: mappingsReply.getMappingsList())
											{
												/* Checks that the necessary fields are present */
												if(mapping.hasReadkey() == false ||
												   mapping.hasManagekey() == false ||
												   mapping.hasWritekey() == false ||
												   mapping.hasAllocLength() == false ||
												   mapping.hasAllocOffset() == false ||
												   mapping.hasE2EBlocksize() == false ||
												   mapping.hasExnodeOffset() == false ||
												   mapping.hasLogicalLength() == false)
												{
													throw new LoDNClientException(
															"Invalid mapping from LoDN");
													
												}
												
												/* Creates a new mapping */
												Mapping exnodeMapping = new Mapping();
												
												/* Adds the capabilites to the mapping */
												exnodeMapping.setAllocation(
													new Allocation(
														new Capability(
															String.format("ibp://%s:%d/%s#%s/READ", 
																mapping.getHost(), mapping.getPort(), mapping.getRid(), mapping.getReadkey())),
														new Capability(
															String.format("ibp://%s:%d/%s#%s/WRITE", 
																mapping.getHost(), mapping.getPort(), mapping.getRid(), mapping.getWritekey())),
														new Capability(
															String.format("ibp://%s:%d/%s#%s/MANAGE", 
																	mapping.getHost(), mapping.getPort(), mapping.getRid(), mapping.getManagekey()))));
													
												
												
												/* Adds the required metadata to the mapping */
												exnodeMapping.addMetadata(new IntegerMetadata("alloc_length", mapping.getAllocLength()));
												exnodeMapping.addMetadata(new IntegerMetadata("alloc_offset", mapping.getAllocOffset()));
												exnodeMapping.addMetadata(new IntegerMetadata("e2e_blocksize", mapping.getE2EBlocksize()));
												exnodeMapping.addMetadata(new IntegerMetadata("exnode_offset", mapping.getExnodeOffset()));
												exnodeMapping.addMetadata(new IntegerMetadata("logical_length", mapping.getLogicalLength()));
												
												exnodeMapping.setFunction(new IdentityFunction());
												
												mappings.add(exnodeMapping);
											}	
											
											/* Success, pass the mappings to the completion handler */
											userCompletionHandler.completed(mappings, userAttachment);
											
										}catch(URISyntaxException | HostMismatchException | PortMismatchException e) 
										{
											userCompletionHandler.failed(
													new LoDNClientException(
														"Invalid mapping from LoDN", e),
													userAttachment);
										}catch(LoDNClientException e)
										{
											userCompletionHandler.failed(e,
													userAttachment);
										}
											
										break;
										
									/* Error response  */
									case SESSION_RESPONSE_MSG_TYPE:
										ResponseMsg responseMsg = 
											(ResponseMsg)message;
										
										if(responseMsg.hasErrstr())
										{
											userCompletionHandler.failed(
												new LoDNClientException(
													responseMsg.getErrnum()+":"+responseMsg.getErrstr()),
												userAttachment);
										}else
										{
											userCompletionHandler.failed(
												new LoDNClientException(
													"response"+responseMsg.getErrnum()), 
												userAttachment);
										}
										
										break;
										
									/* Invalid  response */
									default:
										userCompletionHandler.failed(
											new LoDNClientException(
												"Invalid response from LoDN"), 
											userAttachment);
										break;
								}
							}
	
							@Override
							public void failed(Throwable exc, 
									GeneratedMessage attachment) 
							{
								userCompletionHandler.failed(exc, userAttachment);
							}
						});
				}

				@Override
				public void failed(Throwable exc, Void attachment) 
				{
					userCompletionHandler.failed(
						new LoDNClientException(
								"Error sending message to LoDN", 
								exc), 
						userAttachment); 
				}
			});
	}
	
	public Future<Collection<Mapping>> getMappings(String path, int offset, int num_mappings)
	{
		/* Creates a Future for the mappings call result */
		LoDNClientFuture<Collection<Mapping>> lodnClientFuture = new LoDNClientFuture<>();
		
		/* Calls the getMappings with a completion handler that sets the
		 * created future according to the success or failure of the 
		 * operation */
		this.getMappings(path, offset, num_mappings, lodnClientFuture, 
			new CompletionHandler<Collection<Mapping>, LoDNClientFuture<Collection<Mapping>>>()
			{
				@Override
				public void completed(Collection<Mapping> mappings,
						LoDNClientFuture<Collection<Mapping>> lodnClientFuture) 
				{
					/* Set the result of the future */
					lodnClientFuture.setResult(mappings, null);
				}

				@Override
				public void failed(Throwable exc,
						LoDNClientFuture<Collection<Mapping>> lodnClientFuture) 
				{
					/* Set failure with the exception */
					lodnClientFuture.setResult(null, exc);
				}		
			});
		
		/* Returns the future */
		return lodnClientFuture;
	}

	public <A> void importExnodeFile(String path, ByteBuffer exnodeBuf, 
			A attachment, CompletionHandler<?, A> completionHandler)
	{
		throw new UnsupportedOperationException();
	}
	
	public Future<Void> importExnodeFile(String path, ByteBuffer exnodeBuf)
	{
		throw new UnsupportedOperationException();
	}
	
	public <A> void exportExnodeFile(String path, A attachment, final CompletionHandler<Exnode, A> completionHandler)
	{
        String[] elements = path.split("/");

        final String filename = elements[elements.length-1];


        this.getMappings(path, 0, Integer.MAX_VALUE, attachment, new CompletionHandler<Collection<Mapping>, A>()
        {
            @Override
            public void completed(Collection<Mapping> mappings, A attachment)
            {
                //ByteBuffer exnodeBuffer = null;

                Exnode exnode = new Exnode();

                try(ByteArrayOutputStream exnodeStream = new ByteArrayOutputStream())
                {
                    /* Write the exnode header */
//                    exnodeStream.write("<?xml version=\"1.0\"?>\n".getBytes());
//                    exnodeStream.write("<exnode xmlns:exnode=\"http://loci.cs.utk.edu/exnode\">\n".getBytes());
//                    exnodeStream.write("<exnode:metadata name=\"Version\" type=\"string\">3.0</exnode:metadata>\n".getBytes());
//                    exnodeStream.write(("<exnode:metadata name=\"filename\" type=\"string\">"+ filename +"</exnode:metadata>\n").getBytes());
//                    exnodeStream.write("<exnode:metadata name=\"lorsversion\" type=\"double\">0.828000</exnode:metadata>\n".getBytes());

                    /* Write the mappings */
                    for(Mapping mapping : mappings)
                    {
                       // exnodeStream.write(mapping.toXML().getBytes());

                        exnode.addMapping(mapping);
                    }

                    /* Close it up */
                   // exnodeStream.write("</exnode>".getBytes());

                    /* Convert to a byte buffer */
                   // exnodeBuffer = ByteBuffer.wrap(exnodeStream.toByteArray());


                }catch(IOException e)
                {
                    completionHandler.failed(e, attachment);
                    return;
                }

                /* Call the completion handler */
                completionHandler.completed(exnode, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment)
            {
                completionHandler.failed(exc, attachment);
            }
        });
	}

	public Future<Exnode> exportExnodeFile(String path)
	{
         /* Creates a Future for the mappings call result */
        final LoDNClientFuture<Exnode> lodnClientFuture = new LoDNClientFuture<>();


        /* Call the completeion handler version to actually do all of the work */
        this.exportExnodeFile(path, lodnClientFuture,
                new CompletionHandler<Exnode, LoDNClientFuture<Exnode>>()
                {
                    @Override
                    public void completed(Exnode exnode, LoDNClientFuture<Exnode> loDNClientFuture)
                    {
                        lodnClientFuture.setResult(exnode, null);
                    }

                    @Override
                    public void failed(Throwable exc, LoDNClientFuture<Exnode> loDNClientFuture)
                    {
                        /* Set failure with the exception */
                        loDNClientFuture.setResult(null, exc);
                    }
                });


        /* Return the lodn client future */
        return lodnClientFuture;
	}
}
