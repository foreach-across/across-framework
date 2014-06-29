package com.foreach.across.modules.ehcache.handlers;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.events.AcrossModuleBeforeBootstrapEvent;
import com.foreach.across.modules.ehcache.config.EhcacheClientModuleConfig;
import net.engio.mbassy.listener.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * Ensures that every module that is bootstrapped after the current module has a
 * configuration class with the @EnableCaching annotation.
 */
@AcrossEventHandler
@Component
public class RegisterClientModuleConfigHandler
{
	@Handler
	public void registerEhCacheClientModule( AcrossModuleBeforeBootstrapEvent event ) {
		event.addApplicationContextConfigurers( new AnnotatedClassConfigurer( EhcacheClientModuleConfig.class ) );
	}
}
