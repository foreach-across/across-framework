package com.foreach.across.test.concurrent;

import com.foreach.across.core.concurrent.DistributedLockRepository;
import com.foreach.across.core.concurrent.DistributedLockRepositoryImpl;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;

import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestDistributedLock.Config.class)
public class TestDistributedLock
{
	@Autowired
	private DataSource dataSource;

	@Test
	public void batch() throws Exception {
		int batchSize = 5;
		int totalResults = batchSize * 100;
		int totalLocks = batchSize * 20;

		ExecutorService batchExecutorService = Executors.newFixedThreadPool( batchSize );

		Set<Future<Integer>> results = new HashSet<>();

		for ( int i = 0; i < batchSize; i++ ) {
			ExecutorBatch batch = new ExecutorBatch( createRepository() );
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

	private DistributedLockRepository createRepository() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
		dataSource.setUrl( "jdbc:hsqldb:mem:/hsql-mem/distributed-lock" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );

		return new DistributedLockRepositoryImpl( dataSource );
	}

	protected static class ExecutorBatch implements Callable<Integer>
	{
		private final DistributedLockRepository distributedLockRepository;

		public ExecutorBatch( DistributedLockRepository distributedLockRepository ) {
			this.distributedLockRepository = distributedLockRepository;
		}

		@Override
		public Integer call() throws Exception {
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool( 50 );

			List<Executor> executors = new ArrayList<>( 100 );

			for ( int i = 0; i < 20; i++ ) {
				Lock lock = distributedLockRepository.getLock( UUID.randomUUID().toString() );

				for ( int j = 0; j < 5; j++ ) {
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

			return totalSucceeded;
		}
	}

	protected static class Executor implements Runnable
	{
		private int sleepTime;
		private Lock lock;
		private StopWatch stopWatch = new StopWatch();
		private boolean failed;
		private boolean finished;

		public Executor( Lock lock, int sleepTime ) {
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
	protected static class Config
	{
		@Bean
		public DataSource dataSource() {
			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
			dataSource.setUrl( "jdbc:hsqldb:mem:/hsql-mem/distributed-lock" );
			dataSource.setUsername( "sa" );
			dataSource.setPassword( "" );

			return dataSource;
		}

		@Bean
		public SpringLiquibase createSchema() {
			SpringLiquibase springLiquibase = new SpringLiquibase();
			springLiquibase.setDataSource( dataSource() );
			springLiquibase.setChangeLog(
					"classpath:com/foreach/across/core/installers/AcrossCoreSchemaInstaller.xml" );

			return springLiquibase;
		}
	}
}
