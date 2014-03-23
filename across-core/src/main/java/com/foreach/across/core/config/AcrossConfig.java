package com.foreach.across.core.config;

import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.MBassadorEventPublisher;
import com.foreach.across.core.events.SpringContextRefreshedEventListener;
import com.foreach.across.core.installers.AcrossCoreSchemaInstaller;
import com.foreach.across.core.installers.AcrossInstallerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

/**
 * Installs the common beans that are always available.
 */
@Configuration
public class AcrossConfig
{
	@Bean
	public AcrossEventPublisher eventPublisher() {
		return new MBassadorEventPublisher();
	}

	@Bean
	public SpringContextRefreshedEventListener refreshedEventListener() {
		return new SpringContextRefreshedEventListener();
	}
}
