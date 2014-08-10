package com.foreach.across.core.context;

import com.foreach.across.core.annotations.Module;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an exposed BeanDefinition.  An exposed BeanDefinition always uses the original
 * ApplicationContext as a
 *
 * @author Arne Vandamme
 */
public class ExposedBeanDefinition extends RootBeanDefinition
{
	private final String contextId;
	private final String moduleName;

	private final String fullyQualifiedBeanName;
	private String preferredBeanName;

	private Set<String> aliases = new HashSet<>();

	public ExposedBeanDefinition( ExposedBeanDefinition original ) {
		super( original );

		contextId = original.contextId;
		moduleName = original.moduleName;
		fullyQualifiedBeanName = original.fullyQualifiedBeanName;
		preferredBeanName = original.fullyQualifiedBeanName;
		aliases.addAll( original.aliases );
	}

	public ExposedBeanDefinition( AcrossContextBeanRegistry contextBeanRegistry,
	                              String moduleName,
	                              String originalBeanName,
	                              Class beanClass ) {
		this.contextId = contextBeanRegistry.getContextId();
		this.moduleName = moduleName;

		setSynthetic( true );

		setFactoryBeanName( contextBeanRegistry.getFactoryName() );
		setFactoryMethodName( "getBeanFromModule" );

		setScope( "prototype" );

		if ( beanClass != null ) {
			setBeanClassName( beanClass.getName() );
			setBeanClass( beanClass );
			setTargetType( beanClass );
		}

		setAutowireMode( AUTOWIRE_NO );
		setAutowireCandidate( true );
		setDependencyCheck( DEPENDENCY_CHECK_NONE );

		ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
		constructorArgumentValues.addGenericArgumentValue( moduleName );
		constructorArgumentValues.addGenericArgumentValue( originalBeanName );

		setConstructorArgumentValues( constructorArgumentValues );

		addQualifier( new AutowireCandidateQualifier( Module.class.getName(), moduleName ) );
		//addQualifier( new AutowireCandidateQualifier( Context.class.getName(), contextId ) );

		fullyQualifiedBeanName = contextId + "." + moduleName + "@" + originalBeanName;
		setPreferredBeanName( originalBeanName );
	}

	public ExposedBeanDefinition( AcrossContextBeanRegistry contextBeanRegistry,
	                              String moduleName,
	                              String originalBeanName,
	                              BeanDefinition original,
	                              Class<?> beanClass ) {
		this( contextBeanRegistry, moduleName, originalBeanName, beanClass );

		// todo: required?
		setPrimary( original.isPrimary() );
		//setLazyInit( original.isLazyInit() );
		setDescription( original.getDescription() );
		setRole( original.getRole() );

		// Add detailed information
		if ( original instanceof AbstractBeanDefinition ) {
			AbstractBeanDefinition originalAbstract = (AbstractBeanDefinition) original;

			for ( AutowireCandidateQualifier qualifier : originalAbstract.getQualifiers() ) {
				addQualifier( qualifier );
			}
		}
	}

	public String getFullyQualifiedBeanName() {
		return fullyQualifiedBeanName;
	}

	public String getPreferredBeanName() {
		return preferredBeanName;
	}

	public void setPreferredBeanName( String preferredBeanName ) {
		this.preferredBeanName = preferredBeanName;
		addQualifier( new AutowireCandidateQualifier( Qualifier.class.getName(), preferredBeanName ) );
	}

	public String getContextId() {
		return contextId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public Set<String> getAliases() {
		return Collections.unmodifiableSet( aliases );
	}

	public void addAlias( String alias ) {
		aliases.add( alias );
	}

	public void removeAlias( String alias ) {
		aliases.remove( alias );
	}
}
