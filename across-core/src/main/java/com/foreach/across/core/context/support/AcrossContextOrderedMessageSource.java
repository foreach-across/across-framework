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
package com.foreach.across.core.context.support;

import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DelegatingMessageSource;

import java.util.Locale;

/**
 * Custom {@link org.springframework.context.support.DelegatingMessageSource} that allows
 * subscribing additional MessageSources.  These will be added in reverse order for lookups:
 * the last added MessageSource will be used first and will have the previous message source
 * as parent.
 *
 * The message sources are not added to this message source, but to the endpoint, which is assumed
 * to be in the parents of the current message source.
 *
 * For internal use only.
 *
 * @author Arne Vandamme
 */
public class AcrossContextOrderedMessageSource extends DelegatingMessageSource
{
	private final HierarchicalMessageSource endpoint;
	private HierarchicalMessageSource lastPushed;

	public AcrossContextOrderedMessageSource( HierarchicalMessageSource endpoint ) {
		this.endpoint = endpoint;
	}

	public void push( HierarchicalMessageSource messageSource ) {
		if ( messageSource.getParentMessageSource() != null ) {
			throw new RuntimeException( "Unable to add a messageSource as it already has a parent message source set" );
		}

		messageSource.setParentMessageSource( lastPushed );

		if ( endpoint != null ) {
			endpoint.setParentMessageSource( messageSource );
		}

		lastPushed = messageSource;
	}
}
