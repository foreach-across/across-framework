package com.foreach.across.core.annotations.conditions;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfig;
import com.foreach.across.core.context.info.AcrossContextInfo;
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

		AcrossContextInfo acrossContext = context.getBeanFactory().getBean( AcrossContextInfo.class );

		return applies( acrossContext.getBootstrapConfiguration(), required, optional );
	}

	/**
	 * Checks if the class has an AcrossDepends annotation, and if so if the dependencies are met.
	 *
	 * @param config       Bootstrap configuration to check against.
	 * @param classToCheck Class to check for AcrossDepends annotation.
	 * @return True if dependencies are met or no annotation was found.
	 * @see com.foreach.across.core.annotations.AcrossDepends
	 */
	public static boolean applies( AcrossBootstrapConfig config, Class<?> classToCheck ) {
		AcrossDepends dependsAnnotation = classToCheck.getAnnotation( AcrossDepends.class );

		return dependsAnnotation == null
				|| applies( config, dependsAnnotation.required(), dependsAnnotation.optional() );
	}

	/**
	 * Checks if the required and optional dependencies apply against a given bootstrap configuration.
	 *
	 * @param config   Bootstrap configuration to check against.
	 * @param required Required modules.
	 * @param optional Optional modules.
	 * @return True if all required modules are present and at least one of the optionals (if any defined).
	 * @see com.foreach.across.core.annotations.AcrossDepends
	 */
	public static boolean applies( AcrossBootstrapConfig config, String[] required, String[] optional ) {
		boolean shouldLoad = true;

		if ( required.length > 0 || optional.length > 0 ) {
			for ( String requiredModuleId : required ) {
				if ( !config.hasModule( requiredModuleId ) ) {
					LOG.trace(
							"AcrossDependsCondition does not match because required module {} is not in the boostrap configuration",
							requiredModuleId );
					return false;
				}
			}

			// If all required modules are present, the condition is matched if there is no optional preference
			shouldLoad = optional.length == 0;

			for ( String optionalModuleId : optional ) {
				if ( config.hasModule( optionalModuleId ) ) {
					LOG.trace( "AcrossDependsCondition matches because optional module {} is present",
					           optionalModuleId );
					shouldLoad = true;
					break;
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
}
