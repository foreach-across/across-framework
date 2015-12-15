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

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Installer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.DigestUtils;

import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.util.Date;

public class AcrossInstallerRepository
{
	private static final String SQL_SELECT_VERSION =
			"select version from ACROSSMODULES where module_id = ? and installer_id = ?";
	private static final String SQL_UPDATE_VERSION =
			"update ACROSSMODULES set version = ?, description = ?, created = ? " +
					"where module_id = ? and installer_id = ?";
	private static final String SQL_INSERT_VERSION =
			"insert into ACROSSMODULES (module, module_id, installer, installer_id, version, created, description) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?)";

	private final JdbcTemplate jdbcTemplate;

	public AcrossInstallerRepository( DataSource installDatasource ) {
		jdbcTemplate = new JdbcTemplate( installDatasource );
	}

	public int getInstalledVersion( AcrossModule module, Class<?> installerClass ) {
		try {
			return jdbcTemplate.queryForObject( SQL_SELECT_VERSION, Integer.class, determineId( module.getName() ),
			                                    determineInstallerId( installerClass ) );
		}
		catch ( EmptyResultDataAccessException erdae ) {
			return -1;
		}
	}

	public void setInstalled( AcrossModule module, Installer config, Class<?> installerClass ) {
		if ( getInstalledVersion( module, installerClass ) != -1 ) {
			jdbcTemplate.update( SQL_UPDATE_VERSION, config.version(), StringUtils.abbreviate( config.description(),
			                                                                                   500 ), new Date(),
			                     determineId( module.getName() ), determineInstallerId( installerClass ) );
		}
		else {
			jdbcTemplate.update( SQL_INSERT_VERSION, determineModuleName( module.getName() ), determineId(
					                     module.getName() ),
			                     determineInstallerName( installerClass ), determineInstallerId( installerClass ),
			                     config.version(), new Date(), StringUtils.abbreviate( config.description(), 500 ) );
		}
	}

	private String determineModuleName( String name ) {
		return StringUtils.substring( name, 0, 250 );
	}

	private String determineInstallerName( Class<?> installerClass ) {
		String className = installerClass.getName();

		if ( StringUtils.length( className ) > 250 ) {
			return StringUtils.substring( installerClass.getSimpleName(), 0, 250 );
		}

		return className;
	}

	private String determineInstallerId( Class<?> installerClass ) {
		return determineId( installerClass.getName() );
	}

	private String determineId( String name ) {
		if ( StringUtils.length( name ) > 120 ) {
			return DigestUtils.md5DigestAsHex( name.getBytes( Charset.forName( "UTF-8" ) ) );
		}

		return name;
	}

}
