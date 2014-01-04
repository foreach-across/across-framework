package com.foreach.across.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Date;

@Repository
public class AcrossInstallerRepository
{
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public AcrossInstallerRepository( DataSource installDatasource ) {
		jdbcTemplate = new JdbcTemplate( installDatasource );
	}

	public boolean isInstalled( AcrossInstaller installer ) {
		String SQL = "select * from ACROSSMODULES where module = ? and installer = ?";

		return !jdbcTemplate.queryForList( SQL, installer.getModule().getName(),
		                                   installer.getClass().getName() ).isEmpty();
	}

	public void setInstalled( AcrossInstaller installer ) {
		String SQL = "insert into ACROSSMODULES (module, installer, created, description) VALUES (?, ?, ?, ?)";

		jdbcTemplate.update( SQL, installer.getModule().getName(), installer.getClass().getName(), new Date(),
		                     StringUtils.abbreviate( installer.getDescription(), 500 ) );
	}

}
