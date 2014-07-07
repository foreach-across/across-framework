package com.foreach.across.modules.web.resource;

import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.core.events.BuildRegistryEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Configures a WebResourceRegistry for the request.  Will initialize using the default
 * registry provided and will fire the registry build events.
 */
public class WebResourceRegistryInterceptor extends HandlerInterceptorAdapter
{
	private final WebResourcePackageManager webResourcePackageManager;

	@Autowired
	private AcrossEventPublisher eventBus;

	private WebResourceRegistry defaultRegistry;

	private Collection<WebResourceTranslator> webResourceTranslators = new LinkedList<WebResourceTranslator>();

	public WebResourceRegistryInterceptor( WebResourcePackageManager webResourcePackageManager ) {
		this.webResourcePackageManager = webResourcePackageManager;
	}

	public WebResourceRegistry getDefaultRegistry() {
		return defaultRegistry;
	}

	public void setDefaultRegistry( WebResourceRegistry defaultRegistry ) {
		this.defaultRegistry = defaultRegistry;
	}

	public Collection<WebResourceTranslator> getWebResourceTranslators() {
		return webResourceTranslators;
	}

	public void setWebResourceTranslators( Collection<WebResourceTranslator> webResourceTranslators ) {
		this.webResourceTranslators = webResourceTranslators;
	}

	public void addWebResourceTranslator( WebResourceTranslator translator ) {
		webResourceTranslators.add( translator );
	}

	/**
	 * Adds a new default WebResourceRegistry to the request.
	 */
	@Override
	public boolean preHandle( HttpServletRequest request,
	                          HttpServletResponse response,
	                          Object handler ) {
		WebResourceRegistry registry = new WebResourceRegistry( webResourcePackageManager );
		registry.merge( defaultRegistry );

		eventBus.publish( new BuildRegistryEvent<>( registry ) );

		WebResourceUtils.storeRegistry( registry, request );

		return true;
	}

	/**
	 * Prepares the WebResourceRegistry for rendering by translating locations.
	 */
	@Override
	public void postHandle( HttpServletRequest request,
	                        HttpServletResponse response,
	                        Object handler,
	                        ModelAndView modelAndView ) {
		WebResourceRegistry registry = WebResourceUtils.getRegistry( request );

		if ( registry != null && !webResourceTranslators.isEmpty() ) {
			for ( WebResource resource : registry.getResources() ) {
				for ( WebResourceTranslator translator : webResourceTranslators ) {
					if ( translator.shouldTranslate( resource ) ) {
						translator.translate( resource );
					}
				}
			}
		}
	}
}
