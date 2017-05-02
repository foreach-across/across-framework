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

package com.foreach.across.core.events;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.annotation.PostConstruct;

/**
 * Takes care of actions that need to happen whenever the parent Spring ApplicationContext
 * of the AcrossContext is refreshed.
 * <p>
 * Actions include:
 * <ul>
 * <li>updating all @Refreshable components</li>
 * </ul>
 * </p>
 */
public class SpringContextRefreshedEventListener implements ApplicationListener<ContextRefreshedEvent>
{
	@Autowired
	private AcrossContext context;

	@Autowired
	private AcrossEventPublisher publisher;

	@PostConstruct
	protected void registerInParentContext() {
		ConfigurableApplicationContext parentApplicationContext =
				(ConfigurableApplicationContext) AcrossContextUtils.getParentApplicationContext( context );

		if ( parentApplicationContext != null ) {
			parentApplicationContext.addApplicationListener( this );
		}
	}

	public void onApplicationEvent( ContextRefreshedEvent event ) {
		if ( event.getApplicationContext() == AcrossContextUtils.getParentApplicationContext( context ) ) {
			// Refresh the different beans
			AcrossContextUtils.refreshBeans( context );
		}
	}
}
