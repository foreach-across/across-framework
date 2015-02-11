/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.web.template;

import com.foreach.across.core.events.AcrossEventPublisher;
import com.foreach.across.modules.web.events.BuildTemplateWebResourcesEvent;
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
public class LayoutTemplateProcessorAdapterBean implements NamedWebTemplateProcessor
{
	@Autowired
	private MenuFactory menuFactory;

	@Autowired
	private AcrossEventPublisher eventPublisher;

	private final String name, layout;

	public LayoutTemplateProcessorAdapterBean( String name, String layout ) {
		this.name = name;
		this.layout = layout;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void prepareForTemplate( HttpServletRequest request, HttpServletResponse response, Object handler ) {
		WebResourceRegistry webResourceRegistry = WebResourceUtils.getRegistry( request );

		if ( webResourceRegistry != null ) {
			registerWebResources( webResourceRegistry );
		}

		buildMenus( menuFactory );

		eventPublisher.publish( new BuildTemplateWebResourcesEvent( getName(), webResourceRegistry ) );
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
