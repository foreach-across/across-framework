package com.foreach.across.modules.ehcache.handlers;

import com.foreach.across.core.context.AcrossContextUtil;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.events.AcrossModuleBeforeBootstrapEvent;
import net.engio.mbassy.listener.Handler;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Registers the pointcut advisor in the other module context to activate caching.
 */
@AcrossEventHandler
@Component
public class CacheInterceptRegistrar
{
	@Autowired
	private DefaultListableBeanFactory beanFactory;

	@Handler
	public void registerPointcutAdvisor( AcrossModuleBeforeBootstrapEvent event ) {
		AcrossModule module = event.getModule();

		AcrossContextUtil.getApplicationContext(module);

		Map<String, PointcutAdvisor> beans = beanFactory.getBeansOfType( PointcutAdvisor.class );

		//ConfigurableListableBeanFactory moduleBeanFactory =
		//		( (ConfigurableListableBeanFactory) module.getApplicationContext().getAutowireCapableBeanFactory() );

//		beanFactory.getBeanDefinition( "org.springframework.aop.config.internalAutoProxyCreator" );

		//BeanDefinitionRegistry registry = ((BeanDefinitionRegistry) module.getApplicationContext()).getAutowireCapableBeanFactory();

//		registry.registerBeanDefinition( "org.springframework.aop.config.internalAutoProxyCreator",
//					                                 beanFactory.getBeanDefinition(
//							                                 "org.springframework.aop.config.internalAutoProxyCreator" ) );
//
//		for ( Map.Entry<String, PointcutAdvisor> bean : beans.entrySet() ) {
//			(( ConfigurableBeanFactory) registry).registerSingleton( bean.getKey(), bean.getValue() );
//		}
	}
}
