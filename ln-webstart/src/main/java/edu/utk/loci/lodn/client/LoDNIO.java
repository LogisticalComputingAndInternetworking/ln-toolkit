package edu.utk.loci.lodn.client;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.GeneratedMessage;

import edu.utk.loci.lodn.client.LoDNMessageProtos.AddMappingsRequest;
import edu.utk.loci.lodn.client.LoDNMessageProtos.CreateFileRequest;
import edu.utk.loci.lodn.client.LoDNMessageProtos.GetDirReply;
import edu.utk.loci.lodn.client.LoDNMessageProtos.GetDirRequest;
import edu.utk.loci.lodn.client.LoDNMessageProtos.GetMappingsReply;
import edu.utk.loci.lodn.client.LoDNMessageProtos.GetMappingsRequest;
import edu.utk.loci.lodn.client.LoDNMessageProtos.GetStatReply;
import edu.utk.loci.lodn.client.LoDNMessageProtos.GetStatRequest;
import edu.utk.loci.lodn.client.LoDNMessageProtos.GetXAttrRequest;
import edu.utk.loci.lodn.client.LoDNMessageProtos.GetXattrReply;
import edu.utk.loci.lodn.client.LoDNMessageProtos.LoDNInfoMsg;
import edu.utk.loci.lodn.client.LoDNMessageProtos.LoDNInfoRequestMsg;
import edu.utk.loci.lodn.client.LoDNMessageProtos.MkdirMsg;
import edu.utk.loci.lodn.client.LoDNMessageProtos.ResponseMsg;
import edu.utk.loci.lodn.client.LoDNMessageProtos.RmdirRequest;
import edu.utk.loci.lodn.client.LoDNMessageProtos.SessionClosedMsg;
import edu.utk.loci.lodn.client.LoDNMessageProtos.SessionOpenMsg;
import edu.utk.loci.lodn.client.LoDNMessageProtos.SetXAttrRequest;
import edu.utk.loci.lodn.client.LoDNMessageProtos.UnlinkRequest;

class LoDNIO 
{
	/*** Class variables ***/
	private static int HEADER_SIZE_IN_BYTES = 2*(Integer.SIZE/Byte.SIZE);
	
	static <A> void sendMessage(AsynchronousSocketChannel channel, 
			MessageType messageType, GeneratedMessage message,
			final CompletionHandler<Void, Void> completionHandler)
	{
		/* Creates the header buffer and payload (message) buffer */
		ByteBuffer[] buffers = new ByteBuffer[] 
			{ 
				ByteBuffer.allocate(HEADER_SIZE_IN_BYTES), 
				message.toByteString().asReadOnlyByteBuffer() 
			};
		
		/* Fills in the header */
		buffers[0].order(ByteOrder.LITTLE_ENDIAN);
		buffers[0].putInt(messageType.ordinal());
		buffers[0].putInt(buffers[1].limit());
		buffers[0].flip();
		
		System.out.printf("Pos %d limit %d\n", buffers[0].position(), buffers[0].limit());
		System.out.printf("Pos %d limit %d\n", buffers[1].position(), buffers[1].limit());
		
		/* Sends the header and the message */
		channel.write(buffers, 0, 2, 300, TimeUnit.SECONDS, channel, 
			new CompletionHandler<Long, AsynchronousSocketChannel>() 
			{
				@Override
				public void completed(Long result,
						AsynchronousSocketChannel channel) 
				{
					System.out.printf("Wrote bytes: %d\n", result);
					
					/* TODO handle incomplete writes */
					
					/* Success */
					completionHandler.completed(null, null);
				}

				@Override
				public void failed(Throwable exc,
						AsynchronousSocketChannel channel) 
				{
					System.out.printf("Error sending bytes");
					
					/* Set the completion handler to failed */
					completionHandler.failed(exc, null);
				}
			});
	}
	
	static <A> void readMessage(
			AsynchronousSocketChannel channel, 
			final CompletionHandler<MessageType, GeneratedMessage> completionHandler)
	{
		/* Create the Bytebuffer for holding the header */
		final ByteBuffer msghdr = ByteBuffer.allocate(HEADER_SIZE_IN_BYTES);
		//msghdr.order(ByteOrder.LITTLE_ENDIAN);

		System.out.printf("Issuing read\n");
		
		/* Sets up the read for doing the message header */
		channel.read(msghdr, channel, 
			new CompletionHandler<Integer, AsynchronousSocketChannel>() 
			{
				@Override
				public void completed(Integer result,
						AsynchronousSocketChannel channel) 
				{
					System.out.printf("Amount read %d\n", result);
	
					/* Not enough data read, so issuing another header read */
					if(msghdr.remaining() != 0)
					{
						channel.read(msghdr, channel, this);
					}
					
					/* Switch the byte order to big endian for java */
					//msghdr.order(ByteOrder.BIG_ENDIAN);
					//msghdr.order(ByteOrder.LITTLE_ENDIAN);
					
					msghdr.order(ByteOrder.LITTLE_ENDIAN);
					final int msgTypeAsInt = msghdr.getInt(0);
					
					//msghdr.order(ByteOrder.BIG_ENDIAN);
					final int size         = msghdr.getInt(4);
					
					System.out.printf("Message type %d Message length %d\n",
							msgTypeAsInt, size);
					
					//msghdr.order(ByteOrder.LITTLE_ENDIAN);
					System.out.printf("other size %d\n", msghdr.getInt(4));
					
					
					/* Creates the buffer for message payload */
					final ByteBuffer payloadBuffer = 
							ByteBuffer.allocate(size);
	
					channel.read(payloadBuffer, channel, 
						new CompletionHandler<Integer, AsynchronousSocketChannel>() 
						{
							@Override
							public void completed(Integer result,
									AsynchronousSocketChannel channel) 
							{
								System.out.printf("Read %d\n", result);
		
								System.out.printf("position %d, limit %d\n", payloadBuffer.position(), payloadBuffer.limit());
								
								/* Not enough data read, so issuing another read */
								if(payloadBuffer.position() != size)
								{
									while(true);
									//channel.read(msghdr, channel, this);
								}
								
								payloadBuffer.flip();
		
								
								try
								{
									
									MessageType messageType = 
											MessageType.fromInteger(msgTypeAsInt);
									GeneratedMessage message = null;
									
									/* Generate the individual message types */
									switch(messageType)
									{
										case ADDMAPPINGS_REQUEST:
											message = AddMappingsRequest.parseFrom(
													payloadBuffer.array());
											break;
											
										case CREATEFILE_REQUEST:
											message = CreateFileRequest.parseFrom(
													payloadBuffer.array());
											break;
											
										case GETDIR_REPLY:
											message = GetDirReply.parseFrom(
													payloadBuffer.array());
											break;
											
										case GETDIR_REQUEST:
											message = GetDirRequest.parseFrom(
													payloadBuffer.array());
											break;
											
										case GETMAPPINGS_REPLY:
											message = GetMappingsReply.parseFrom(
													payloadBuffer.array());
											break;
											
										case GETMAPPINGS_REQUEST:
											message = GetMappingsRequest.parseFrom(
													payloadBuffer.array());
											break;
											
										case GETSTAT_REPLY:
											message = GetStatReply.parseFrom(
													payloadBuffer.array());
											break;
											
										case GETSTAT_REQUEST:
											message = GetStatRequest.parseFrom(
													payloadBuffer.array());
											break;
											
										case GETXATTR_REPLY:
											message = GetXattrReply.parseFrom(
													payloadBuffer.array());
											break;
											
										case GETXATTR_REQUEST:
											message = GetXAttrRequest.parseFrom(
													payloadBuffer.array());
											break;
											
										//case LISTXATTR_REPLY:
										//	message = .parseFrom(payloadBuffer.array());
										//	break;
											
										//case LISTXATTR_REQUEST:
										//	message = .parseFrom(payloadBuffer.array());
										//	break;
											
										case MKDIR_MSG:
											message = MkdirMsg.parseFrom(
													payloadBuffer.array());
											break;
											
										//case REMOVEXATTR_REQUEST:
										//	message = .parseFrom(payloadBuffer.array());
										//	break;
											
										case RMDIR_REQUEST:
											message = RmdirRequest.parseFrom(
													payloadBuffer.array());
											break;
											
										case SESSION_CLOSED_MSG_TYPE:
											message = SessionClosedMsg.parseFrom(
													payloadBuffer.array());
											break;
											
										case SESSION_LODNINFO_MSG_TYPE:
											message = LoDNInfoMsg.parseFrom(
													payloadBuffer.array());
											break;
											
										case SESSION_LODNINFO_REQUEST_MSG_TYPE:
											message = LoDNInfoRequestMsg.parseFrom(
													payloadBuffer.array());
											break;
											
										case SESSION_OPEN_MSG_TYPE:
											message = SessionOpenMsg.parseFrom(
													Arrays.copyOf(payloadBuffer.array(), size));
											break;
											
										case SESSION_RESPONSE_MSG_TYPE:
										
											message = ResponseMsg.parseFrom(
													Arrays.copyOf(payloadBuffer.array(), size));
											break;
											
										case SETXATTR_REQUEST:
											message = SetXAttrRequest.parseFrom(
													payloadBuffer.array());
											break;
											
										case UNLINK_REQUEST:
											message = UnlinkRequest.parseFrom(
													payloadBuffer.array());
											break;
											
										default:
											completionHandler.failed(
												new LoDNClientException("Unkown message type"), 
												null);
											break;
									}
									
									/* Calls the completion handler with the 
									 * message and it's type */
									completionHandler.completed(messageType, message);
									
								}catch(InvalidProtocolBufferException e)
								{
									/* Pass the failure back to the completion handler */
									completionHandler.failed(e, null);	
								}
							}
		
							@Override
							public void failed(Throwable exc,
									AsynchronousSocketChannel channel) 
							{
								/* Pass the failure back to the completion handler */
								completionHandler.failed(exc, null);		
							}
						});
	
				}
	
				@Override
				public void failed(Throwable exc,
						AsynchronousSocketChannel channel) 
				{
					/* Pass the failure back to the completion handler */
					completionHandler.failed(exc, null);	
				}
			});

	}
}
