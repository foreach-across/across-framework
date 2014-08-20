package com.foreach.across.core.database;

import com.foreach.across.core.AcrossException;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Utility to quickly get some properties of a data source.
 * Uses the {@link java.sql.DatabaseMetaData} of the connection.
 * <p/>
 * Meant primarily to determine the type of database the data source
 * represents, required to be able to handle cross-database issues differently.
 *
 * @author Arne Vandamme
 */
public class DatabaseInfo
{
	private String productName, productVersion;

	public String getProductName() {
		return productName;
	}

	public String getProductVersion() {
		return productVersion;
	}

	public boolean isHsql() {
		return StringUtils.containsIgnoreCase( productName, "HSQL" );
	}

	public boolean isMySQL() {
		return StringUtils.containsIgnoreCase( productName, "MySQL" );
	}

	public boolean isSqlServer() {
		return StringUtils.containsIgnoreCase( productName, "Microsoft SQL Server" );
	}

	public boolean isOracle() {
		return StringUtils.containsIgnoreCase( productName, "Oracle" );
	}

	public static DatabaseInfo retrieve( DataSource dataSource ) {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();

			DatabaseInfo databaseInfo = new DatabaseInfo();
			databaseInfo.productName = metaData.getDatabaseProductName();
			databaseInfo.productVersion = metaData.getDatabaseProductVersion();

			return databaseInfo;
		}
		catch ( SQLException sqle ) {
			throw new AcrossException( "Could not retrieve DatabaseInfo for DataSource", sqle );
		}
	}
}
