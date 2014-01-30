package com.foreach.across.core.context;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * Condition that checks that a given module is present in the AcrossContext.
 * To be used on @Configuration and @Bean instances to load components only if other modules
 * are being loaded.
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

		AcrossContext acrossContext = context.getBeanFactory().getParentBeanFactory().getBean( AcrossContext.class );

		for ( String requiredModuleId : required ) {
			if ( !hasModule( acrossContext, requiredModuleId ) ) {
				LOG.trace( "AcrossDependsCondition does not match because required module {} is not present",
				           requiredModuleId );
				return false;
			}
		}

		// If all required modules are present, the condition is matched if there is no optional preference
		boolean shouldLoad = optional.length == 0;

		for ( String optionalModuleId : optional ) {
			if ( hasModule( acrossContext, optionalModuleId ) ) {
				LOG.trace( "AcrossDependsCondition matches because optional module {} is present", optionalModuleId );
				shouldLoad = true;
			}
		}

		if ( !shouldLoad ) {
			LOG.trace( "AcrossDependsCondition does not match because none of the optional modules {} were present",
			           optional );
		}
		else if ( required.length > 0 ) {
			LOG.trace( "AcrossDependsCondition matches because all required modules {} were present", required );
		}

		return shouldLoad;
	}

	private boolean hasModule( AcrossContext context, String moduleId ) {
		for ( AcrossModule module : context.getModules() ) {
			if ( StringUtils.equals( moduleId, module.getName() ) ) {
				return true;
			}
			if ( StringUtils.equals( module.getClass().getName(), moduleId ) ) {
				return true;
			}
		}

		return false;
	}
}
