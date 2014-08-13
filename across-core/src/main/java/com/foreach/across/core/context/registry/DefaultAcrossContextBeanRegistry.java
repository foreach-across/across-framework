package com.foreach.across.core.context.registry;

import com.foreach.across.core.context.ModuleBeanOrderComparator;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.context.info.ConfigurableAcrossContextInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DefaultAcrossContextBeanRegistry implements AcrossContextBeanRegistry
{
	private final ConfigurableAcrossContextInfo contextInfo;

	public DefaultAcrossContextBeanRegistry( ConfigurableAcrossContextInfo contextInfo ) {
		this.contextInfo = contextInfo;
	}

	@Override
	public String getContextId() {
		return contextInfo.getId();
	}

	@Override
	public String getFactoryName() {
		return contextInfo.getId() + "@" + AcrossContextBeanRegistry.BEAN;
	}

	@Override
	public Object getBean( String beanName ) {
		return contextInfo.getApplicationContext().getBean( beanName );
	}

	@Override
	public Class<?> getBeanType( String beanName ) {
		return contextInfo.getApplicationContext().getType( beanName );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T getBeanFromModule( String moduleName, String beanName ) {
		if ( StringUtils.isEmpty( moduleName ) ) {
			return (T) getBean( beanName );
		}

		return (T) contextInfo.getConfigurableModuleInfo( moduleName ).getApplicationContext().getBean( beanName );
	}

	@Override
	public Class<?> getBeanTypeFromModule( String moduleName, String beanName ) {
		if ( StringUtils.isEmpty( moduleName ) ) {
			return getBeanType( beanName );
		}

		return contextInfo.getConfigurableModuleInfo( moduleName ).getApplicationContext().getType( beanName );
	}

	@Override
	public <T> T getBeanOfType( Class<T> requiredType ) {
		return contextInfo.getApplicationContext().getBean( requiredType );
	}

	@Override
	public <T> T getBeanOfTypeFromModule( String moduleName, Class<T> requiredType ) {
		if ( StringUtils.isEmpty( moduleName ) ) {
			return null;
		}

		return contextInfo.getConfigurableModuleInfo( moduleName ).getApplicationContext().getBean( requiredType );
	}

	@Override
	public <T> List<T> getBeansOfType( Class<T> beanClass ) {
		return getBeansOfType( beanClass, false );
	}

	@Override
	public <T> List<T> getBeansOfType( Class<T> beanClass, boolean includeModuleInternals ) {
		Set<T> beans = new LinkedHashSet<>();
		ModuleBeanOrderComparator comparator = new ModuleBeanOrderComparator();

		for ( T bean : BeanFactoryUtils.beansOfTypeIncludingAncestors( contextInfo.getApplicationContext(), beanClass )
		                               .values() ) {
			comparator.register( bean, Ordered.HIGHEST_PRECEDENCE );
			beans.add( bean );
		}

		if ( includeModuleInternals ) {
			for ( AcrossModuleInfo module : contextInfo.getModules() ) {
				ListableBeanFactory beanFactory = module.getApplicationContext();

				if ( beanFactory != null ) {
					for ( T bean : beanFactory.getBeansOfType( beanClass ).values() ) {
						comparator.register( bean, module.getIndex() );
						beans.add( bean );
					}
				}
			}
		}

		List<T> beanList = new ArrayList<>( beans );
		comparator.sort( beanList );

		return beanList;
	}
}
