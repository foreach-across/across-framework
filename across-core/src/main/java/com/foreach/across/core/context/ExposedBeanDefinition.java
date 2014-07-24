package com.foreach.across.core.context;

import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Represents an exposed BeanDefinition.  An exposed BeanDefinition always uses the original
 * ApplicationContext as a
 *
 * @author Arne Vandamme
 */
public class ExposedBeanDefinition extends RootBeanDefinition
{
	private final String moduleName;

	public ExposedBeanDefinition( AcrossModuleInfo moduleInfo, String originalBeanName ) {
		this.moduleName = moduleInfo.getName();

		setSynthetic( true );

		setFactoryBeanName( moduleName );
		setFactoryMethodName( "getBean" );
		setScope( "prototype" );

		setAutowireCandidate( true );

		ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
		constructorArgumentValues.addGenericArgumentValue( originalBeanName );

		setConstructorArgumentValues( constructorArgumentValues );
	}

	public ExposedBeanDefinition( AcrossModuleInfo moduleInfo, String originalBeanName, BeanDefinition original ) {
		this( moduleInfo, originalBeanName );

		setPrimary( original.isPrimary() );
		setLazyInit( original.isLazyInit() );
		setDescription( original.getDescription() );
		setRole( original.getRole() );
		setBeanClassName( original.getBeanClassName() );

		// Add detailed information
		if ( original instanceof AbstractBeanDefinition ) {
			AbstractBeanDefinition originalAbstract = (AbstractBeanDefinition) original;

			for ( AutowireCandidateQualifier qualifier : originalAbstract.getQualifiers() ) {
				addQualifier( qualifier );
			}

			if ( !originalAbstract.hasBeanClass() ) {
				try {
					originalAbstract.resolveBeanClass( Thread.currentThread().getContextClassLoader() );
				}
				catch ( Exception e ) {
					throw new RuntimeException( e );
				}
			}

			if ( originalAbstract.hasBeanClass() ) {
				setBeanClass( originalAbstract.getBeanClass() );
				setTargetType( originalAbstract.getBeanClass() );
			}
		}

		addQualifier( new AutowireCandidateQualifier( Module.class.getName(), moduleInfo.getName() ) );

	}

	public String getModuleName() {
		return moduleName;
	}
}
