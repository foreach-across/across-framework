package com.foreach.across.test.concurrent;

import com.foreach.across.core.concurrent.DistributedLock;
import com.foreach.across.core.concurrent.DistributedLockRepository;
import com.foreach.across.core.concurrent.DistributedLockRepositoryImpl;
import com.foreach.across.core.concurrent.SqlBasedDistributedLockManager;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.After;
import org.junit.Before;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = ITDistributedLocking.Config.class)
public class ITDistributedLocking
{
	private static final int BATCHES = 5;
	private static final int LOCKS_PER_BATCH = 20;
	private static final int EXECUTORS_PER_LOCK = 30;

	private static final AtomicInteger REPOSITORY_COUNTER = new AtomicInteger();

	private ExecutorService singleThread = Executors.newSingleThreadExecutor();

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;
	private Set<SqlBasedDistributedLockManager> lockManagers;

	private final Map<String, Integer> resultsByLock = Collections.synchronizedMap(
			new HashMap<String, Integer>() );

	@Before
	public void setup() {
		resultsByLock.clear();

		jdbcTemplate = new JdbcTemplate( dataSource );
		lockManagers = new HashSet<>();
	}

	@After
	public void shutdown() {
		for ( SqlBasedDistributedLockManager lockManager : lockManagers ) {
			lockManager.close();
		}
	}

	@Test
	public void testSynchronization() throws Exception {
		int batchSize = BATCHES;
		int totalResults = BATCHES * LOCKS_PER_BATCH * EXECUTORS_PER_LOCK;
		int totalLocks = LOCKS_PER_BATCH;
		int resultsPerLock = EXECUTORS_PER_LOCK * BATCHES;

		int locksBeforeStart = lockCount();

		ExecutorService batchExecutorService = Executors.newFixedThreadPool( batchSize );

		Set<Future<Integer>> results = new HashSet<>();

		for ( int i = 0; i < batchSize; i++ ) {
			ExecutorBatch batch = new ExecutorBatch( createRepository() );
			results.add( batchExecutorService.submit( batch ) );
		}

		batchExecutorService.shutdown();
		batchExecutorService.awaitTermination( 3, TimeUnit.MINUTES );

		int actualResults = 0;

		for ( Future<Integer> result : results ) {
			actualResults += result.get();
		}

		assertEquals( totalResults, actualResults );
		assertEquals( totalLocks + locksBeforeStart, lockCount() );

		// Check synchronization was correct
		assertEquals( totalLocks, resultsByLock.size() );
		for ( Map.Entry<String, Integer> entry : resultsByLock.entrySet() ) {
			assertEquals( Integer.valueOf( resultsPerLock ), entry.getValue() );
		}
	}

	@Test
	public void tryLockShouldReturnImmediately() throws Exception {
		String localRepositoryName = "local-" + REPOSITORY_COUNTER.incrementAndGet();

		DistributedLockRepository lockRepository = createRepository( localRepositoryName );
		DistributedLockRepository otherRepositoryInSameJvm = createRepository( localRepositoryName );
		DistributedLockRepository externalRepository = createRepository(
				"external-" + REPOSITORY_COUNTER.incrementAndGet() );

		final DistributedLock lock = lockRepository.createLock( UUID.randomUUID().toString() );
		final DistributedLock otherLock = otherRepositoryInSameJvm.createLock( lock.getLockId() );
		final DistributedLock externalLock = externalRepository.createLock( lock.getLockId() );

		boolean locked = lock.tryLock();
		assertTrue( locked );

		final AtomicLong duration = new AtomicLong( 0 );

		// Same lock but from a different thread should fail
		Future<Boolean> sameLockByOtherThreadLocked = singleThread.submit( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception {
				StopWatch stopWatch = new StopWatch();
				stopWatch.start();

				boolean success = lock.tryLock();

				duration.set( stopWatch.getTime() );

				return success;
			}
		} );

		assertFalse( sameLockByOtherThreadLocked.get() );
		assertTrue( duration.get() < 100 );

		// Other lock instances but same thread should work
		assertTrue( otherLock.tryLock() );

		// Other lock instance in another thread should also fail
		Future<Boolean> otherLockByOtherThreadLocked = singleThread.submit( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception {
				StopWatch stopWatch = new StopWatch();
				stopWatch.start();

				boolean success = otherLock.tryLock();

				duration.set( stopWatch.getTime() );

				return success;
			}
		} );

		assertFalse( otherLockByOtherThreadLocked.get() );
		assertTrue( duration.get() < 100 );

		// Same thread but an "external" repository should fail
		assertFalse( externalLock.tryLock() );
	}

	@Test
	public void tryLockWithTimeout() {
		DistributedLockRepository lockRepository = createRepository( "local-" + REPOSITORY_COUNTER.incrementAndGet() );
		DistributedLockRepository externalRepository = createRepository(
				"external-" + REPOSITORY_COUNTER.incrementAndGet() );

		DistributedLock lock = lockRepository.createLock( UUID.randomUUID().toString() );
		DistributedLock externalLock = externalRepository.createLock( lock.getLockId() );

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		assertTrue( lock.tryLock( 3, TimeUnit.SECONDS ) );
		assertTrue( stopWatch.getTime() < 100 );

		stopWatch.reset();
		stopWatch.start();

		assertFalse( externalLock.tryLock( 3, TimeUnit.SECONDS ) );
		assertTrue( stopWatch.getTime() >= 3000 );
	}

	@Test
	public void lockIsStolenIfIdleForTooLong() {
		DistributedLockRepository lockRepository = createRepository( "local-" + REPOSITORY_COUNTER.incrementAndGet() );
		DistributedLockRepository externalRepository = createRepository(
				"external-" + REPOSITORY_COUNTER.incrementAndGet() );

		DistributedLock lock = lockRepository.createLock( UUID.randomUUID().toString() );
		DistributedLock externalLock = externalRepository.createLock( lock.getLockId() );

		assertTrue( lock.tryLock() );
		assertTrue( lock.isLocked() );
		assertTrue( lock.isLockedByMe() );

		assertFalse( externalLock.tryLock() );
		assertTrue( externalLock.isLocked() );
		assertFalse( externalLock.isLockedByMe() );

		updateIdleTime( lock, System.currentTimeMillis() - 30000 );

		assertTrue( externalLock.tryLock() );
		assertTrue( externalLock.isLocked() );
		assertTrue( externalLock.isLockedByMe() );

		assertFalse( lock.tryLock() );
		assertTrue( lock.isLocked() );
		assertFalse( lock.isLockedByMe() );
	}

	@Test
	public void stolenLockCallback() {
		DistributedLockRepository lockRepository = createRepository( "local-" + REPOSITORY_COUNTER.incrementAndGet() );

		final DistributedLock lock = lockRepository.createSharedLock( "owner-one", UUID.randomUUID().toString() );
		final DistributedLock otherLock = lockRepository.createSharedLock( "owner-two", lock.getLockId() );

		final AtomicBoolean callbackExecuted = new AtomicBoolean( false );

		DistributedLock.LockStolenCallback callback = new DistributedLock.LockStolenCallback()
		{
			@Override
			public void stolen( String lockId, String ownerId, DistributedLock stolenLock ) {
				assertEquals( lock.getLockId(), lockId );
				assertEquals( "owner-one", ownerId );
				assertSame( lock, stolenLock );

				callbackExecuted.set( true );
			}
		};

		lock.setStolenCallback( callback );

		assertTrue( lock.tryLock() );
		assertFalse( otherLock.tryLock() );

		updateIdleTime( lock, System.currentTimeMillis() - 30000 );

		assertTrue( otherLock.tryLock() );
		assertFalse( lock.isLockedByMe() );
		assertTrue( callbackExecuted.get() );
	}

	@Test
	public void lockManagerShouldNotifyLocksInUse() throws InterruptedException {
		DistributedLockRepository lockRepository = createRepository( "local-" + REPOSITORY_COUNTER.incrementAndGet() );
		DistributedLock lock = lockRepository.createLock( UUID.randomUUID().toString() );

		lock.lock();

		long creation = lastUpdated( lock );
		Thread.sleep( SqlBasedDistributedLockManager.DEFAULT_VERIFY_INTERVAL + 500 );

		long updated = lastUpdated( lock );
		assertTrue( updated > creation );

		creation = updated;
		Thread.sleep( SqlBasedDistributedLockManager.DEFAULT_VERIFY_INTERVAL + 500 );

		assertTrue( lastUpdated( lock ) > creation );
	}

	private void updateIdleTime( DistributedLock lock, long updated ) {
		jdbcTemplate.update( "UPDATE across_locks SET updated = ? WHERE lock_id = ?", updated, lock.getLockId() );
	}

	private int lockCount() {
		return jdbcTemplate.queryForObject( "SELECT count(*) FROM across_locks", Integer.class );
	}

	private long lastUpdated( DistributedLock lock ) {
		return jdbcTemplate.queryForObject( "SELECT updated FROM across_locks WHERE lock_id = ?", Long.class,
		                                    lock.getLockId() );
	}

	private DistributedLockRepository createRepository() {
		return createRepository( "local" );
	}

	private DistributedLockRepository createRepository( String defaultOwnerName ) {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
		dataSource.setUrl( "jdbc:hsqldb:mem:/hsql-mem/distributed-lock" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );

		SqlBasedDistributedLockManager lockManager = new SqlBasedDistributedLockManager( dataSource );
		lockManagers.add( lockManager );

		return new DistributedLockRepositoryImpl( lockManager,
		                                          defaultOwnerName );
	}

	protected class ExecutorBatch implements Callable<Integer>
	{
		private final DistributedLockRepository distributedLockRepository;

		public ExecutorBatch( DistributedLockRepository distributedLockRepository ) {
			this.distributedLockRepository = distributedLockRepository;
		}

		@Override
		public Integer call() throws Exception {
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool( 50 );

			List<Executor> executors = new ArrayList<>( LOCKS_PER_BATCH * EXECUTORS_PER_LOCK );

			for ( int i = 0; i < LOCKS_PER_BATCH; i++ ) {
				DistributedLock lock = distributedLockRepository.createLock( "batch-lock-" + i );

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

			return totalSucceeded;
		}
	}

	protected class Executor implements Runnable
	{
		private int sleepTime;
		private DistributedLock lock;
		private StopWatch stopWatch = new StopWatch();
		private boolean failed;
		private boolean finished;

		public Executor( DistributedLock lock, int sleepTime ) {
			if ( !resultsByLock.containsKey( lock.getLockId() ) ) {
				resultsByLock.put( lock.getLockId(), 0 );
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

				Integer currentCount = resultsByLock.get( lock.getLockId() );
				resultsByLock.put( lock.getLockId(), currentCount + 1 );

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
