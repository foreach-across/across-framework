package com.foreach.across.it;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.database.DatabaseInfo;
import com.foreach.across.test.AcrossTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = ITDatabaseInfo.Config.class)
public class ITDatabaseInfo
{
	private static final Logger LOG = LoggerFactory.getLogger( ITDatabaseInfo.class );

	@Autowired
	private DataSource dataSource;

	@Test
	public void dataSourceShouldBeOneOfKnown() {
		DatabaseInfo databaseInfo = DatabaseInfo.retrieve( dataSource );

		assertNotNull( databaseInfo );
		assertNotNull( databaseInfo.getProductName() );
		assertNotNull( databaseInfo.getProductVersion() );

		LOG.info( "DatabaseInfo detected database {} - version: {}", databaseInfo.getProductName(),
		          databaseInfo.getProductVersion() );

		assertTrue(
				databaseInfo.isHsql()
						|| databaseInfo.isOracle()
						|| databaseInfo.isSqlServer()
						|| databaseInfo.isMySQL()
		);
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
