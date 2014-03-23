package com.foreach.across.core.context;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.*;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * Condition that checks that a given module is present in the AcrossContext.
 * To be used on @Configuration and @Bean instances to load components only if other modules
 * are being loaded.
 *
 * @see com.foreach.across.core.annotations.AcrossDepends
 */
public class AcrossDependsCondition implements Condition
{
	private static final Logger LOG = LoggerFactory.getLogger( AcrossDependsCondition.class );

	/**
	 * Determine if the condition matches.
	 *
	 * @param context  the condition context
	 * @param metadata metadata of the {@link org.springframework.core.type.AnnotationMetadata class}
	 *                 or {@link org.springframework.core.type.MethodMetadata method} being checked.
	 * @return {@code true} if the condition matches and the component can be registered
	 * or {@code false} to veto registration.
	 */
	public boolean matches( ConditionContext context, AnnotatedTypeMetadata metadata ) {
		Map<String, Object> attributes = metadata.getAnnotationAttributes( AcrossDepends.class.getName() );
		String[] required = (String[]) attributes.get( "required" );
		String[] optional = (String[]) attributes.get( "optional" );

		String expression = (String) attributes.get( "expression" );

		if ( !StringUtils.isBlank( expression ) && !evaluate( expression, context ) ) {
			return false;
		}

		boolean shouldLoad = true;

		if ( required.length > 0 || optional.length > 0 ) {
			AcrossContext acrossContext =
					context.getBeanFactory().getParentBeanFactory().getBean( AcrossContext.class );

			for ( String requiredModuleId : required ) {
				if ( !hasModule( acrossContext, requiredModuleId ) ) {
					LOG.trace( "AcrossDependsCondition does not match because required module {} is not present",
					           requiredModuleId );
					return false;
				}
			}

			// If all required modules are present, the condition is matched if there is no optional preference
			shouldLoad = optional.length == 0;

			for ( String optionalModuleId : optional ) {
				if ( hasModule( acrossContext, optionalModuleId ) ) {
					LOG.trace( "AcrossDependsCondition matches because optional module {} is present",
					           optionalModuleId );
					shouldLoad = true;
				}
			}

			if ( LOG.isTraceEnabled() ) {
				if ( !shouldLoad ) {
					LOG.trace(
							"AcrossDependsCondition does not match because none of the optional modules {} were present",
							optional );
				}
				else if ( required.length > 0 ) {
					LOG.trace( "AcrossDependsCondition matches because all required modules {} were present",
					           required );
				}
			}
		}

		return shouldLoad;
	}

	/**
	 * Based on implementation of OnExpressionCondition in spring-boot package.
	 * https://github.com/spring-projects/spring-boot/blob/master/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/condition/OnExpressionCondition.java
	 */
	private boolean evaluate( String expression, ConditionContext context ) {
		if ( !expression.startsWith( "#{" ) ) {
			// For convenience allow user to provide bare expression with no #{} wrapper
			expression = "#{" + expression + "}";
		}

		// Explicitly allow environment placeholders inside the expression
		expression = context.getEnvironment().resolvePlaceholders( expression );
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		BeanExpressionResolver resolver = beanFactory.getBeanExpressionResolver();
		BeanExpressionContext expressionContext =
				( beanFactory != null ) ? new CurrentModuleBeanExpressionContext( beanFactory, null ) : null;
		if ( resolver == null ) {
			resolver = new StandardBeanExpressionResolver();
		}

		boolean result = (Boolean) resolver.evaluate( expression, expressionContext );

		if ( LOG.isTraceEnabled() ) {
			LOG.trace( "AcrossDepends expression {} evaluated to {}", expression, result );
		}

		return result;
	}

	private boolean hasModule( AcrossContext context, String moduleId ) {
		for ( AcrossModule module : context.getModules() ) {
			if ( module.isEnabled() ) {
				if ( StringUtils.equals( moduleId, module.getName() ) ) {
					return true;
				}
				if ( StringUtils.equals( module.getClass().getName(), moduleId ) ) {
					return true;
				}
			}
		}

		return false;
	}

	private static class CurrentModuleBeanExpressionContext extends BeanExpressionContext
	{
		private CurrentModuleBeanExpressionContext( ConfigurableBeanFactory beanFactory, Scope scope ) {
			super( beanFactory, scope );
		}

		public AcrossModule getCurrentModule() {
			Map<String, AcrossModule> modules =
					BeanFactoryUtils.beansOfTypeIncludingAncestors( (ListableBeanFactory) getBeanFactory(),
					                                                AcrossModule.class, false, false );

			for ( AcrossModule module : modules.values() ) {
				if ( AcrossContextUtils.getBeanFactory( module ) == getBeanFactory() ) {
					return module;
				}
			}

			return null;
		}
	}
}
