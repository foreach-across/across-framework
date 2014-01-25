package com.foreach.across.testweb;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurationSelector;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.annotation.ProxyCachingConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

public class AcrossCachingSelector extends AdviceModeImportSelector<EnableAcrossCaching> implements BeanFactoryAware
{
	private BeanFactory beanFactory;

	/**
	 * Callback that supplies the owning factory to a bean instance.
	 * <p>Invoked after the population of normal bean properties
	 * but before an initialization callback such as
	 * {@link InitializingBean#afterPropertiesSet()} or a custom init-method.
	 *
	 * @param beanFactory owning BeanFactory (never {@code null}).
	 *                    The bean can immediately call methods on the factory.
	 * @throws org.springframework.beans.BeansException in case of initialization errors
	 * @see BeanInitializationException
	 */
	public void setBeanFactory( BeanFactory beanFactory ) throws BeansException {
		this.beanFactory = beanFactory;
	}


	/**
	 * {@inheritDoc}
	 *
	 * @return {@link org.springframework.cache.annotation.ProxyCachingConfiguration} or {@code AspectJCacheConfiguration} for
	 * {@code PROXY} and {@code ASPECTJ} values of {@link EnableCaching#mode()}, respectively
	 */
	@Override
	public String[] selectImports( AdviceMode adviceMode ) {
		Map<String, CacheManager> cacheManagers = BeanFactoryUtils.beansOfTypeIncludingAncestors( (ListableBeanFactory) beanFactory, CacheManager.class );

		if ( cacheManagers.isEmpty() ) {
			return null;
		}

		switch ( adviceMode ) {
			case PROXY:
				return new String[] { AutoProxyRegistrar.class.getName(), ProxyCachingConfiguration.class.getName() };
			case ASPECTJ:
				return new String[] { AnnotationConfigUtils.CACHE_ASPECT_CONFIGURATION_CLASS_NAME };
			default:
				return null;
		}
	}

}
