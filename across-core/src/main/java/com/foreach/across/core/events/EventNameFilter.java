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

import com.foreach.across.core.annotations.EventName;
import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;

/**
 * Filters events based on the @EventName annotation and the eventName property of the message.
 * If the message is not a NamedAcrossEvent instance and the handler defines an EventName annotation,
 * it will not be handled.  If the handler does not define an EventName, the message can be handled.
 */
public class EventNameFilter implements IMessageFilter<AcrossEvent>
{
	public boolean accepts( AcrossEvent message, MessageHandler metadata ) {
		EventName methodAnnotation = findEventNameAnnotation( metadata );

		if ( methodAnnotation == null ) {
			return true;
		}

		if ( message instanceof NamedAcrossEvent ) {
			String eventName = ( (NamedAcrossEvent) message ).getEventName();
			String[] acceptable = methodAnnotation.value();

			return ArrayUtils.contains( acceptable, eventName );
		}

		// Event name is declared but event is not named, refuse it
		return false;
	}

	private EventName findEventNameAnnotation( MessageHandler metadata ) {
		for ( Annotation annotation : metadata.getHandler().getParameterAnnotations()[0] ) {
			if ( annotation instanceof EventName ) {
				return (EventName) annotation;
			}
		}

		return null;
	}
}
