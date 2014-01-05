package com.foreach.across.testweb;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.debugweb.DebugHandlerMapping;
import com.foreach.across.modules.debugweb.DebugWebModule;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.sql.DataSource;

@ComponentScan({ "com.foreach.across.testweb" })
public class TestWebConfig extends WebMvcConfigurationSupport
{
	@Bean
	public AcrossContext acrossContext() {
		AcrossContext context = new AcrossContext();
		context.setAllowInstallers( false );

		context.addModule( debugWebModule() );

		return context;
	}

	@Bean
	public DebugWebModule debugWebModule() {
		DebugWebModule debugWebModule = new DebugWebModule();
		debugWebModule.setRootPath( "/test" );

		return debugWebModule;
	}

	@Bean
	@Override
	public RequestMappingHandlerMapping requestMappingHandlerMapping() {
		DebugHandlerMapping mapping = new DebugHandlerMapping();
		mapping.setOrder( 0 );
		mapping.setInterceptors( getInterceptors() );

		return mapping;
	}

	@Bean
	public DataSource installDataSource() throws Exception {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName( "oracle.jdbc.driver.OracleDriver" );
		ds.setUrl( "jdbc:oracle:thin:@192.168.2.215:1522:fe" );
		ds.setUsername( "vkstub" );
		ds.setPassword( "vkstub" );
		ds.setDefaultAutoCommit( true );

		return ds;
	}
}
