package com.foreach.across.modules.web.config.support;

import com.foreach.across.modules.web.mvc.InterceptorRegistry;

/**
 * Interface used for configuring separate
 * {@link com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping} instances.
 */
public interface PrefixingHandlerMappingConfigurer
{
	/**
	 * @param mapperName Unique name of the mapper (usually module name).
	 * @return true if this configurer should be applied for that mapper
	 */
	boolean supports( String mapperName );

	/**
	 * Add one or more interceptors for the mapper.
	 *
	 * @param interceptorRegistry registry
	 */
	void addInterceptors( InterceptorRegistry interceptorRegistry );
}
