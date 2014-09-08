/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.it.concurrent;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossTestContextConfiguration;
import com.foreach.common.concurrent.locks.distributed.DistributedLock;
import com.foreach.common.concurrent.locks.distributed.DistributedLockRepository;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = ITDistributedLockRepository.Config.class)
public class ITDistributedLockRepository
{
	private static final int BATCHES = 3;
	private static final int LOCKS_PER_BATCH = 50;
	private static final int EXECUTORS_PER_LOCK = 10;

	@Autowired
	private Environment environment;

	@Autowired
	private DataSource dataSource;

	private final Map<String, Integer> resultsByLock = Collections.synchronizedMap(
			new HashMap<String, Integer>() );

	@Before
	public void setup() {
		resultsByLock.clear();
	}

	@Test
	public void executeBatch() throws Exception {
		int batchSize = BATCHES;
		int totalResults = BATCHES * LOCKS_PER_BATCH * EXECUTORS_PER_LOCK;
		int totalLocks = LOCKS_PER_BATCH;
		int resultsPerLock = EXECUTORS_PER_LOCK * BATCHES;

		ExecutorService batchExecutorService = Executors.newFixedThreadPool( batchSize );

		Set<Future<Integer>> results = new HashSet<>();

		for ( int i = 0; i < batchSize; i++ ) {
			ExecutorBatch batch = new ExecutorBatch( i * 4000 );
			results.add( batchExecutorService.submit( batch ) );
		}

		batchExecutorService.shutdown();
		batchExecutorService.awaitTermination( 3, TimeUnit.MINUTES );

		int actualResults = 0;

		for ( Future<Integer> result : results ) {
			actualResults += result.get();
		}

		assertEquals( totalResults, actualResults );

		JdbcTemplate jdbcTemplate = new JdbcTemplate( dataSource );
		assertEquals( Integer.valueOf( totalLocks ),
		              jdbcTemplate.queryForObject( "SELECT count(*) FROM across_locks", Integer.class ) );

		// Check synchronization was correct
		assertEquals( totalLocks, resultsByLock.size() );
		for ( Map.Entry<String, Integer> entry : resultsByLock.entrySet() ) {
			assertEquals( Integer.valueOf( resultsPerLock ), entry.getValue() );
		}
	}

	private AcrossContext createContext() {
		AcrossContext context = new AcrossContext();
		context.setDataSource( uniqueDataSource() );

		context.bootstrap();

		return context;
	}

	private DataSource uniqueDataSource() {
		AcrossTestContextConfiguration factory = new AcrossTestContextConfiguration();
		factory.setEnvironment( environment );

		return factory.dataSource();
	}

	protected class ExecutorBatch implements Callable<Integer>
	{
		private final int startDelay;

		public ExecutorBatch( int startDelay ) {
			this.startDelay = startDelay;
		}

		@Override
		public Integer call() throws Exception {
			if ( startDelay > 0 ) {
				Thread.sleep( startDelay );
			}

			AcrossContext context = createContext();
			DistributedLockRepository distributedLockRepository = AcrossContextUtils.getBeanRegistry( context )
			                                                                        .getBeanOfType(
					                                                                        DistributedLockRepository.class );

			ExecutorService fixedThreadPool = Executors.newFixedThreadPool( 50 );

			List<Executor> executors = new ArrayList<>( LOCKS_PER_BATCH * EXECUTORS_PER_LOCK );

			for ( int i = 0; i < LOCKS_PER_BATCH; i++ ) {
				DistributedLock lock = distributedLockRepository.getLock( "batch-lock-" + i );

				for ( int j = 0; j < EXECUTORS_PER_LOCK; j++ ) {
					executors.add( new Executor( lock, 10 ) );
				}
			}

			for ( Executor executor : executors ) {
				fixedThreadPool.submit( executor );
			}

			fixedThreadPool.shutdown();
			fixedThreadPool.awaitTermination( 3, TimeUnit.MINUTES );

			int totalSucceeded = 0;

			for ( Executor executor : executors ) {
				if ( executor.isFinished() ) {
					totalSucceeded++;
				}
			}

			context.destroy();

			return totalSucceeded;
		}
	}

	protected class Executor implements Runnable
	{
		private final int sleepTime;
		private final DistributedLock lock;
		private final StopWatch stopWatch = new StopWatch();
		private boolean failed;
		private boolean finished;

		public Executor( DistributedLock lock, int sleepTime ) {
			if ( !resultsByLock.containsKey( lock.getKey() ) ) {
				resultsByLock.put( lock.getKey(), 0 );
			}

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
		public void run() {
			try {
				stopWatch.start();
				lock.lock();

				if ( sleepTime > 0 ) {
					Thread.sleep( sleepTime );
				}

				Integer currentCount = resultsByLock.get( lock.getKey() );
				resultsByLock.put( lock.getKey(), currentCount + 1 );

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
		}
	}

	@Configuration
	@AcrossTestConfiguration
	static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
		}
	}
}
