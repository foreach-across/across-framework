package com.foreach.across.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.Map;

//@Repository
public class AcrossInstallerRepository
{
	private final JdbcTemplate jdbcTemplate;

	public AcrossInstallerRepository( DataSource installDatasource ) {
		jdbcTemplate = new JdbcTemplate( installDatasource );
	}

	public int getInstalledVersion( AcrossModule module, Object installer ) {
		String SQL = "select version from ACROSSMODULES where module = ? and installer = ?";

		List<Map<String, Object>> rows =
				jdbcTemplate.queryForList( SQL, module.getName(), installer.getClass().getName() );

		if ( rows.isEmpty() ) {
			return -1;
		}

		return (Integer) rows.get( 0 ).get( "version" );
	}

	public void setInstalled( AcrossModule module, Installer config, Object installer ) {
		String SQL =
				"insert into ACROSSMODULES (module, installer, version, created, description) VALUES (?, ?, ?, ?, ?)";

		jdbcTemplate.update( SQL, module.getName(), installer.getClass().getName(), config.version(), new Date(),
		                     StringUtils.abbreviate( config.description(), 500 ) );
	}

}
