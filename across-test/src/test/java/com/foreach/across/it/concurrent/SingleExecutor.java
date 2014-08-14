package com.foreach.across.it.concurrent;

import com.foreach.across.core.concurrent.DistributedLock;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Arne Vandamme
 */
public class SingleExecutor implements Callable<String>
{
	private final int sleepTime;
	private final DistributedLock lock;
	private final StopWatch stopWatch = new StopWatch();
	private final Map<DistributedLock, Integer> resultsByLock;

	private boolean failed;
	private boolean finished;

	public SingleExecutor( Map<DistributedLock, Integer> resultsByLock, DistributedLock lock, int sleepTime ) {
		this.resultsByLock = resultsByLock;

		resultsByLock.put( lock, 0 );

		this.lock = lock;
		this.sleepTime = sleepTime;
	}

	public boolean isFinished() {
		return finished && !isFailed();
	}

	public boolean isFailed() {
		return failed;
	}

	public long getExecutionTime() {
		return stopWatch.getTime();
	}

	@Override
	public String call() throws Exception {
		try {
			stopWatch.start();
			lock.lock();

			if ( sleepTime > 0 ) {
				Thread.sleep( sleepTime );
			}

			synchronized ( resultsByLock ) {
				Integer currentCount = resultsByLock.get( lock.getLockId() );
				resultsByLock.put( lock, currentCount + 1 );
			}

			finished = true;

			stopWatch.stop();
		}
		catch ( Exception ie ) {
			failed = true;
			ie.printStackTrace();
		}
		finally {
			lock.unlock();
		}

		return "success";
	}
}
