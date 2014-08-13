package com.foreach.across.core.installers;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.Installer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.DigestUtils;

import javax.sql.DataSource;
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
			return DigestUtils.md5DigestAsHex( name.getBytes() );
		}

		return name;
	}

}
