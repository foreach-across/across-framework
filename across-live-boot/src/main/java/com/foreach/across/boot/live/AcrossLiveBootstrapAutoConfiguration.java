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
package com.foreach.across.boot.live;

import com.foreach.across.core.events.AcrossContextEvent;
import com.foreach.across.core.events.AcrossContextFailedEvent;
import com.foreach.across.core.events.AcrossContextReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Collections;

/**
 * Activates the separate servlet container for live bootstrap tracking.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties(ServerProperties.class)
public class AcrossLiveBootstrapAutoConfiguration implements EmbeddedServletContainerCustomizer, ApplicationListener<AcrossContextEvent>
{
	private final ServerProperties serverProperties;

	private EmbeddedServletContainer liveServletContainer;

	@Override
	public void customize( ConfigurableEmbeddedServletContainer container ) {
		if ( liveServletContainer == null ) {
			if ( StringUtils.containsIgnoreCase( container.getClass().toString(), "Tomcat" ) ) {
				TomcatEmbeddedServletContainerFactory t = new TomcatEmbeddedServletContainerFactory();
				serverProperties.customize( t );
				t.setInitializers( Collections.emptyList() );
				t.addContextValves( new TomcatProgressValve() );

				LOG.info( "Starting Across Live Bootstrap servlet container" );
				liveServletContainer = t.getEmbeddedServletContainer();
				liveServletContainer.start();
			}
			else {
				LOG.warn( "Across Live Bootstrap tracking only supports Tomcat embedded servlet containers." );
			}
		}
	}

	@Override
	public void onApplicationEvent( AcrossContextEvent event ) {
		if ( event instanceof AcrossContextReadyEvent || event instanceof AcrossContextFailedEvent ) {
			LOG.info( "Shutting down Across Live Bootstrap servler container: {} received", event.getClass().getSimpleName() );
			liveServletContainer.stop();
		}
	}
}
