package com.foreach.across.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Date;

@Repository
public class AcrossInstallerDao
{
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public AcrossInstallerDao( DataSource installDatasource ) {
		jdbcTemplate = new JdbcTemplate( installDatasource );
	}

	public boolean isInstalled( String installer ) {
		String SQL = "select * from ACROSSMODULES where installer = ?";

		return !jdbcTemplate.queryForList( SQL, installer ).isEmpty();
	}

	public void setInstalled( String installer ) {
		String SQL = "insert into ACROSSMODULES (installer, created) VALUES (?, ?)";

		jdbcTemplate.update( SQL, installer, new Date() );
	}

}
