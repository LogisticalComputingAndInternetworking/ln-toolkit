package edu.utk.loci.lodn.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.omg.CORBA.RepositoryIdHelper;

import com.google.protobuf.GeneratedMessage;

import edu.utk.cs.loci.exnode.Mapping;
import edu.utk.loci.lodn.client.LoDNMessageProtos.ResponseMsg;
import edu.utk.loci.lodn.client.LoDNMessageProtos.SessionOpenMsg;

public class LoDNClient 
{
	private static URI _parseURI(String lodnURL) throws LoDNClientException
	{
		URI uri = null;
		
		try 
		{
			uri = new URI(lodnURL);
			
		}catch(URISyntaxException e) 
		{
			throw new LoDNClientException("Invalid LoDN URL");
		}
		
		if(uri.getScheme().equals("lodn") == false || uri.getHost() == null || 
		   uri.getPath() == null || uri.getFragment() != null || 
		   uri.getQuery() != null || uri.getUserInfo() != null)
		{
			throw new LoDNClientException("Invalid LoDN URL");
		}
		
		
		
		System.out.printf("Authority %s\nFragment %s\nHost %s\nPort %d\nPath %s\nQuery %s\nUserInfo %s\nScheme %s\n", 
				uri.getAuthority(), uri.getFragment(), uri.getHost(), uri.getPort(), 
				uri.getPath(), uri.getQuery(), uri.getUserInfo(), uri.getScheme());
		
		
		/* Returns the URL */
		return uri;
	}
	
	private static <A> void _handle_connection(
			final AsynchronousSocketChannel channel,
			final A userAttachment, 
			final CompletionHandler<LoDNSessionChannel, A> userCompletionHandler)
	{
		/* Builds the message */
		SessionOpenMsg msg = SessionOpenMsg.newBuilder().setMsgID(0).build();
		
		/* Send the Session Open msg to LoDN */
		LoDNIO.sendMessage(channel, MessageType.SESSION_OPEN_MSG_TYPE, msg,
			new CompletionHandler<Void, Void>() 
			{
				@Override
				public void completed(Void result, Void attachment) 
				{
					/* Read the response message from LoDN */
					LoDNIO.readMessage(channel, 
						new CompletionHandler<MessageType, GeneratedMessage>() 
						{
							@Override
							public void completed(MessageType messageType, 
									GeneratedMessage message) 
							{
								System.out.printf("Received message of type %s and mesasge = %s\n", messageType, message);
								
								/* Handles the responses from LoDN */
								switch(messageType)
								{
									/* Success */
									case SESSION_OPEN_MSG_TYPE:
										SessionOpenMsg sessionOpenMsg = 
											(SessionOpenMsg) message;
										
										if(sessionOpenMsg.hasMsgID() == false ||
											sessionOpenMsg.hasSessionID() == false)
										{
											userCompletionHandler.failed(
												new LoDNClientException(
													"Invalid response from LoDN"), 
												userAttachment);
										}
										
										System.out.printf("Encryption methods %d\n", 
												sessionOpenMsg.getEncryptionMethodCount());
										
										/* Pass the session channel back to the completion handler */
										userCompletionHandler.completed(
											new LoDNSessionChannel(channel, 
													sessionOpenMsg.getSessionID(), 
													sessionOpenMsg.getMsgID()), 
													userAttachment);
										
										break;
										
									/* Failure */
									case SESSION_RESPONSE_MSG_TYPE:
										ResponseMsg responseMsg = 
											(ResponseMsg)message;
										
										/* Pass failure result back to completion handler */
										userCompletionHandler.failed(
											new LoDNClientException(responseMsg.getErrstr()), 
											userAttachment);
										
									/* Unknown message type (Also failure) */
									default:
										userCompletionHandler.failed(
											new LoDNClientException("Unrecognized response from LoDN"), 
											userAttachment);
								}
								
							}
							
							@Override
							public void failed(Throwable exc, GeneratedMessage message) 
							{
								/* On failure set the user's completion handler to failed */
								userCompletionHandler.failed(
									new LoDNClientException("Error connecting to LoDN", exc), 
									userAttachment);	
							}
					});
						
				}

				@Override
				public void failed(Throwable exc, Void attachment) 
				{
					/* On failure set the user's completion handler to failed */
					userCompletionHandler.failed(
						new LoDNClientException("Error connecting to LoDN", exc), 
						userAttachment);
				}
			});
	}
	
	public static <A> void openLoDNSessionChannel(String lodnURL, 
			EnumSet<LoDNSessionOptions> options, 
			final A attachment, 
			final CompletionHandler<LoDNSessionChannel, A> completionHandler) 
					throws LoDNClientException
	{
		/* Parses the LoDN URL */
		URI uri = _parseURI(lodnURL);
		
		/* Gets a socket addresss from the URL */
		InetSocketAddress address = new InetSocketAddress(uri.getHost(), 
				(uri.getPort() < 1) ? 5000 : uri.getPort());
		
		try
		{
			/* Opens a channel and attempts to connect the lodn server */
			AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
			channel.connect(address, channel,
				new CompletionHandler<Void, AsynchronousSocketChannel>() 
				{
					@Override
					public void completed(Void result,
							AsynchronousSocketChannel channel) 
					{
						_handle_connection(channel, attachment, completionHandler);
					}

					@Override
					public void failed(Throwable exc,
							AsynchronousSocketChannel channel) 
					{
						completionHandler.failed(
							new LoDNClientException("Unable to connection", exc), 
							attachment);
					}
				});

		}catch(IOException e)
		{
			throw new LoDNClientException(
					String.format("Error connecting to %s:%d",
							address.getHostString(), address.getPort()));
		}
	}
	
	public static Future<LoDNSessionChannel> openLoDNSessionChannel(String lodnURL, 
			EnumSet<LoDNSessionOptions> options)
	{
		/* Creates a future object for the result */
		LoDNClientFuture<LoDNSessionChannel> future = new LoDNClientFuture<>();
		
		try 
		{
			/* Calls openSession with the future as the attachment and 
			 * a completion handler that sets the future's result based on 
			 * success or failure */
			openLoDNSessionChannel(lodnURL, options, future,
				new CompletionHandler<LoDNSessionChannel, LoDNClientFuture<LoDNSessionChannel>>() 
				{
					@Override
					public void completed(LoDNSessionChannel session, 
							LoDNClientFuture<LoDNSessionChannel> future) 
					{
						future.setResult(session, null);
					}

					@Override
					public void failed(Throwable exc,
							LoDNClientFuture<LoDNSessionChannel> future) 
					{
						future.setResult(null, exc);
					}
				});
			
		}catch(LoDNClientException e) 
		{
			future.setResult(null, e);
		}
		
		/* Returns the OpenSessionFuture */
		return future;
	}
	
	
	
	public static void main(String[] args) 
		throws LoDNClientException, InterruptedException, ExecutionException
	{
		Future<LoDNSessionChannel> f = LoDNClient.openLoDNSessionChannel(
				"lodn://dresci.incntre.iu.edu:5000", null);

		LoDNSessionChannel loDNSessionChannel = f.get();
		
		Future<Collection<Mapping>> mappingsFuture = loDNSessionChannel.getMappings("/cbrumgard/output", 0, 64);

		Collection<Mapping> mappings = mappingsFuture.get();
		
		System.out.println("I have the mappings in main: "+ mappings);
	}
}
