package edu.utk.loci.lodn.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class LoDNClientFuture<A> implements Future<A> 
{
	/*** Instance variables ***/
	A result = null;
	Throwable exc = null;
	CountDownLatch countDownLatch = new CountDownLatch(1);
	

	void setResult(A result, Throwable exc)
	{
		/* Set results */
		this.result = result;
		this.exc = exc;
		
		/* Signal done */
		this.countDownLatch.countDown();
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) 
	{
		return false;
	}

	@Override
	public boolean isCancelled() 
	{
		return false;
	}

	@Override
	public boolean isDone() 
	{
		return this.countDownLatch.getCount() == 0;
	}

	@Override
	public A get() throws InterruptedException, ExecutionException 
	{
		/* Waits as long as necessary for the task to finish */
		this.countDownLatch.await();
		
		if(this.exc != null)
		{
			throw new ExecutionException(exc);
		}
		
		return this.result;
	}

	@Override
	public A get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException 
	{
		/* Waits if necessary on the result for up to the timeout period*/
		boolean isDone = this.countDownLatch.await(timeout, unit);
		
		/* Job is finished */
		if(isDone)
		{
			/* Error so throw an exception */
			if(this.exc != null)
			{
				throw new ExecutionException(exc);
			}

			/* Return the result */
			return this.result;
			
		/* No result in the specified time period so throw timeout exception */
		}else
		{
			throw new TimeoutException();
		}
	}
}
