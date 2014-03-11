package com.foreach.across.modules.web.mvc;

import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * Extension to the default RequestMappingHandlerAdapter that allows for re-initialization.
 */
public class ReloadableRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter
{
	/**
	 * Reloads the entire configuration.  Only call this method if the properties where not set
	 * using {@link #setArgumentResolvers(java.util.List)}, {@link #setInitBinderArgumentResolvers(java.util.List)}
	 * or {@link #setReturnValueHandlers(java.util.List)}.  If the methods {@link #setCustomArgumentResolvers(java.util.List)},
	 * {@link #setCustomReturnValueHandlers(java.util.List)} were used, calling reload() should not be a problem.
	 */
	public void reload() {
		setArgumentResolvers( null );
		setInitBinderArgumentResolvers( null );
		setReturnValueHandlers( null );

		afterPropertiesSet();
	}
}
