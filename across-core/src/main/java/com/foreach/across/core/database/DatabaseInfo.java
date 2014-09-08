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
