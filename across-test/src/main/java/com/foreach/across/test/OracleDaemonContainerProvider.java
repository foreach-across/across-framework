package com.foreach.across.test;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.JdbcDatabaseContainerProvider;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.MountableFile;

/***
 * A special implementation of OracleContainer which executes in its own schema to allow flash table resets to works.
 * @author Marc Vanbrabant
 * @since 5.0.0
 */
public class OracleDaemonContainerProvider extends JdbcDatabaseContainerProvider
{
	@Override
	public boolean supports( String databaseType ) {
		return databaseType.equals( "axoracle" );
	}

	@Override
	public JdbcDatabaseContainer newInstance() {
		return new OracleContainer()
				.withUsername( "axt" )
				.withPassword( "across_test" )
				.withCopyFileToContainer( MountableFile.forClasspathResource( "ax-init.sql" ), "/docker-entrypoint-initdb.d/init.sql" );
	}

	@Override
	public JdbcDatabaseContainer newInstance( String tag ) {
		if ( tag != null ) {
			throw new UnsupportedOperationException( "Oracle database tag should be set in the configured image name" );
		}

		return newInstance();
	}
}
