/*
 * Copyright 2019 the original author or authors
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
package com.foreach.across.modules.web.config.resources;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import java.lang.reflect.Field;

public class AcrossResourceUrlProvider extends ResourceUrlProvider
{

	@Override
	public void onApplicationEvent( ContextRefreshedEvent event ) {
		if ( this.isAutodetect() ) {
			getHandlerMap().clear();
			this.detectResourceHandlers( event.getApplicationContext() );
			if ( !getHandlerMap().isEmpty() ) {
				setAutodetect( false );
			}
		}
	}

	private void setAutodetect( boolean autodetect ) {
		try {
			Field field = getClass().getSuperclass().getDeclaredField( "autodetect" );
			field.setAccessible( true );
			field.set( this, autodetect );
		}
		catch ( NoSuchFieldException | IllegalAccessException e ) {
			throw new RuntimeException( e );
		}
	}

}
