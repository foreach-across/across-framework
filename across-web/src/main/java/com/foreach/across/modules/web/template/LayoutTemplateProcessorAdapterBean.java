package com.foreach.across.modules.web.template;

import com.foreach.across.modules.web.menu.MenuFactory;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.resource.WebResourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Applies a layout to a view, load resource packages and generate menu instances.
 * Will put the original view under the childPage attribute.
 */
public class LayoutTemplateProcessorAdapterBean implements WebTemplateProcessor
{
	@Autowired
	private MenuFactory menuFactory;

	private String layout;

	public LayoutTemplateProcessorAdapterBean( String layout ) {
		this.layout = layout;
	}

	@Override
	public void prepareForTemplate( HttpServletRequest request, HttpServletResponse response, Object handler ) {
		WebResourceRegistry webResourceRegistry = WebResourceUtils.getRegistry( request );

		if ( webResourceRegistry != null ) {
			registerWebResources( webResourceRegistry );
		}

		buildMenus( menuFactory );
	}

	protected void registerWebResources( WebResourceRegistry registry ) {

	}

	protected void buildMenus( MenuFactory menuFactory ) {

	}

	@Override
	public void applyTemplate( HttpServletRequest request,
	                           HttpServletResponse response,
	                           Object handler,
	                           ModelAndView modelAndView ) {
		if ( modelAndView.hasView() && modelAndView.isReference() ) {
			String viewName = modelAndView.getViewName();

			// Redirect views should not be modified
			if ( !StringUtils.startsWithAny( viewName, UrlBasedViewResolver.REDIRECT_URL_PREFIX,
			                                 UrlBasedViewResolver.FORWARD_URL_PREFIX ) ) {
				modelAndView.addObject( "childPage", modelAndView.getViewName() );
				modelAndView.setViewName( layout );
			}
		}
	}
}