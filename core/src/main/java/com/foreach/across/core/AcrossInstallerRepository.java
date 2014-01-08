package com.foreach.across.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Date;

//@Repository
public class AcrossInstallerRepository
{
	private final JdbcTemplate jdbcTemplate;

	public AcrossInstallerRepository( DataSource installDatasource ) {
		jdbcTemplate = new JdbcTemplate( installDatasource );
	}

	public int getInstalledVersion( AcrossModule module, Object installer ) {
		String SQL = "select version from ACROSSMODULES where module = ? and installer = ?";

		try {
			return jdbcTemplate.queryForObject( SQL, Integer.class, module.getName(), installer.getClass().getName() );
		}
		catch ( EmptyResultDataAccessException erdae ) {
			return -1;
		}
	}

	public void setInstalled( AcrossModule module, Installer config, Object installer ) {
		if ( getInstalledVersion( module, installer ) != -1 ) {
			String SQL =
					"update ACROSSMODULES set version = ?, description = ?, created = ? where module = ? and installer = ?";

			jdbcTemplate.update( SQL, config.version(), StringUtils.abbreviate( config.description(), 500 ), new Date(),
			                     module.getName(), installer.getClass().getName() );
		}
		else {
			String SQL =
					"insert into ACROSSMODULES (module, installer, version, created, description) VALUES (?, ?, ?, ?, ?)";

			jdbcTemplate.update( SQL, module.getName(), installer.getClass().getName(), config.version(), new Date(),
			                     StringUtils.abbreviate( config.description(), 500 ) );
		}
	}

}
