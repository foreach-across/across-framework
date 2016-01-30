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

package com.foreach.across.core.installers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.DigestUtils;

import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.util.Date;

public class AcrossInstallerRepositoryImpl implements AcrossInstallerRepository
{
	private static final String SQL_SELECT_VERSION =
			"select version from ACROSSMODULES where module_id = ? and installer_id = ?";
	private static final String SQL_UPDATE_VERSION =
			"update ACROSSMODULES set version = ?, description = ?, created = ? " +
					"where module_id = ? and installer_id = ?";
	private static final String SQL_INSERT_VERSION =
			"insert into ACROSSMODULES (module, module_id, installer, installer_id, version, created, description) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String SQL_RENAME_INSTALLER_FOR_MODULE =
			"update ACROSSMODULES set installer = ?, installer_id = ? where module_id = ? and installer_id = ?";
	private static final String SQL_RENAME_INSTALLER =
			"update ACROSSMODULES set installer = ?, installer_id = ? where installer_id = ?";

	private final JdbcTemplate jdbcTemplate;

	public AcrossInstallerRepositoryImpl( DataSource installDatasource ) {
		jdbcTemplate = new JdbcTemplate( installDatasource );
	}

	@Override
	public int renameInstaller( String oldInstallerName, String newInstallerName ) {
		return jdbcTemplate.update(
				SQL_RENAME_INSTALLER,
				newInstallerName,
				determineInstallerId( newInstallerName ),
				determineInstallerId( oldInstallerName )
		);
	}

	@Override
	public boolean renameInstallerForModule( String oldInstallerName, String newInstallerName, String moduleName ) {
		return jdbcTemplate.update(
				SQL_RENAME_INSTALLER_FOR_MODULE,
				newInstallerName,
				determineInstallerId( newInstallerName ),
				determineId( moduleName ),
				determineInstallerId( oldInstallerName )
		) > 0;
	}

	@Override
	public int getInstalledVersion( String moduleName, String installerName ) {
		try {
			return jdbcTemplate.queryForObject(
					SQL_SELECT_VERSION,
					Integer.class,
					determineId( moduleName ),
					determineInstallerId( installerName )
			);
		}
		catch ( EmptyResultDataAccessException erdae ) {
			return -1;
		}
	}

	@Override
	public void setInstalled( String moduleName, InstallerMetaData installerMetaData ) {
		if ( getInstalledVersion( moduleName, installerMetaData.getName() ) != -1 ) {
			jdbcTemplate.update(
					SQL_UPDATE_VERSION,
					installerMetaData.getVersion(),
					StringUtils.abbreviate( installerMetaData.getDescription(), 500 ),
					new Date(),
					determineId( moduleName ),
					determineInstallerId( installerMetaData.getName() )
			);
		}
		else {
			jdbcTemplate.update(
					SQL_INSERT_VERSION,
					determineModuleName( moduleName ),
					determineId( moduleName ),
					determineInstallerName( installerMetaData ),
					determineInstallerId( installerMetaData.getName() ),
					installerMetaData.getVersion(),
					new Date(),
					StringUtils.abbreviate( installerMetaData.getDescription(), 500 )
			);
		}
	}

	private String determineModuleName( String name ) {
		return StringUtils.substring( name, 0, 250 );
	}

	private String determineInstallerName( InstallerMetaData installerMetaData ) {
		String className = installerMetaData.getName();

		if ( StringUtils.length( className ) > 250 ) {
			return StringUtils.substring( installerMetaData.getName(), 0, 250 );
		}

		return className;
	}

	private String determineInstallerId( String installerName ) {
		return determineId( installerName );
	}

	private String determineId( String name ) {
		if ( StringUtils.length( name ) > 120 ) {
			return DigestUtils.md5DigestAsHex( name.getBytes( Charset.forName( "UTF-8" ) ) );
		}

		return name;
	}
}
