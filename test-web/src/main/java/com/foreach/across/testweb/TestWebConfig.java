package com.foreach.across.testweb;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.web.AcrossWebContext;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.ControllerOnlyRequestMappingHandlerMapping;
import com.foreach.across.testweb.other.TestWebOtherModule;
import com.foreach.across.testweb.sub.TestWebModule;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;

//@EnableWebMvc
@Configuration
public class TestWebConfig
{
	@Bean
	@Autowired
	public AcrossContext acrossContext( ConfigurableApplicationContext applicationContext ) throws Exception {
		AcrossWebContext context = new AcrossWebContext( applicationContext );
		context.setDataSource( acrossDataSource() );
		context.setAllowInstallers( false );

		context.addModule( acrossWebModule() );
		//context.addModule( debugWebModule() );
		context.addModule( otherModule() );
		context.addModule( testWebModule() );


		return context;
	}

	@Bean
	public TestWebOtherModule otherModule() {
		return new TestWebOtherModule();
	}

	@Bean
	public TestWebModule testWebModule() {
		return new TestWebModule();
	}

	@Bean
	public AcrossWebModule acrossWebModule() {
		return new AcrossWebModule();
	}

	@Bean
	public DebugWebModule debugWebModule() {
		DebugWebModule debugWebModule = new DebugWebModule();
		debugWebModule.setRootPath( "/test" );

		return debugWebModule;
	}

	@Bean
	public DataSource acrossDataSource() throws Exception {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
		dataSource.setUrl( "jdbc:hsqldb:mem:acrossTestWeb" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );

		return dataSource;
	}
}
