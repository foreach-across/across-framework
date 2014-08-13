package com.foreach.across.it;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.concurrent.DistributedLock;
import com.foreach.across.core.concurrent.DistributedLockRepository;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossTestContextConfiguration;
import org.apache.commons.lang3.time.StopWatch;
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
	@Autowired
	private Environment environment;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Test
	public void executeBatch() throws Exception {
		int batchSize = 5;
		int totalResults = batchSize * 200;
		int totalLocks = batchSize * 25;

		beanRegistry.getBeanOfType( DistributedLockRepository.class );

		ExecutorService batchExecutorService = Executors.newFixedThreadPool( batchSize );

		Set<Future<Integer>> results = new HashSet<>();

		for ( int i = 0; i < batchSize; i++ ) {
			ExecutorBatch batch = new ExecutorBatch( i * 4000 );
			results.add( batchExecutorService.submit( batch ) );
		}

		batchExecutorService.shutdown();
		batchExecutorService.awaitTermination( 60, TimeUnit.SECONDS );

		int actualResults = 0;

		for ( Future<Integer> result : results ) {
			actualResults += result.get();
		}

		assertEquals( totalResults, actualResults );

		JdbcTemplate jdbcTemplate = new JdbcTemplate( dataSource );
		assertEquals( Integer.valueOf( totalLocks ),
		              jdbcTemplate.queryForObject( "SELECT count(*) FROM across_locks", Integer.class ) );
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

			List<Executor> executors = new ArrayList<>( 200 );

			for ( int i = 0; i < 25; i++ ) {
				DistributedLock lock = distributedLockRepository.createLock( UUID.randomUUID().toString() );

				for ( int j = 0; j < 8; j++ ) {
					executors.add( new Executor( lock, 10 ) );
				}
			}

			for ( Executor executor : executors ) {
				fixedThreadPool.submit( executor );
			}

			fixedThreadPool.shutdown();
			fixedThreadPool.awaitTermination( 30, TimeUnit.SECONDS );

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

	protected static class Executor implements Runnable
	{
		private int sleepTime;
		private DistributedLock lock;
		private StopWatch stopWatch = new StopWatch();
		private boolean failed;
		private boolean finished;

		public Executor( DistributedLock lock, int sleepTime ) {
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

				finished = true;

				stopWatch.stop();
			}
			catch ( Exception ie ) {
				failed = true;
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
