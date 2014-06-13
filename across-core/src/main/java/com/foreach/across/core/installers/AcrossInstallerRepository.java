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
	private final JdbcTemplate jdbcTemplate;

	public AcrossInstallerRepository( DataSource installDatasource ) {
		jdbcTemplate = new JdbcTemplate( installDatasource );
	}

	public int getInstalledVersion( AcrossModule module, Object installer ) {
		String SQL = "select version from ACROSSMODULES where module = ? and installer_id = ?";

		try {
			return jdbcTemplate.queryForObject( SQL, Integer.class, module.getName(),
			                                    determineInstallerId( installer ) );
		}
		catch ( EmptyResultDataAccessException erdae ) {
			return -1;
		}
	}

	public void setInstalled( AcrossModule module, Installer config, Object installer ) {
		if ( getInstalledVersion( module, installer ) != -1 ) {
			String SQL =
					"update ACROSSMODULES set version = ?, description = ?, created = ? where module = ? and installer_id = ?";

			jdbcTemplate.update( SQL, config.version(), StringUtils.abbreviate( config.description(), 500 ), new Date(),
			                     module.getName(), determineInstallerId( installer ) );
		}
		else {
			String SQL =
					"insert into ACROSSMODULES (module, installer, installer_id, version, created, description) VALUES (?, ?, ?, ?, ?, ?)";

			jdbcTemplate.update( SQL, module.getName(), determineInstallerName( installer ),
			                     determineInstallerId( installer ), config.version(), new Date(),
			                     StringUtils.abbreviate( config.description(), 500 ) );
		}
	}

	private String determineInstallerName( Object installer ) {
		String className = installer.getClass().getName();

		if ( StringUtils.length( className ) > 200 ) {
			return installer.getClass().getSimpleName();
		}

		return className;
	}

	private String determineInstallerId( Object installer ) {
		String className = installer.getClass().getName();

		if ( StringUtils.length( className ) > 200 ) {
			return DigestUtils.md5DigestAsHex( className.getBytes() );
		}

		return className;
	}

}
