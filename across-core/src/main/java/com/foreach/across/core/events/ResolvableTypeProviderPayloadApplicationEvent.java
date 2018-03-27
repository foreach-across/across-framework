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

import org.springframework.context.PayloadApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * Extension to {@link PayloadApplicationEvent} where the payload itself defines the {@link ResolvableType} directly.
 *
 * @param <T> the payload type of the event
 * @author Arne Vandamme
 * @since 3.0.0
 */
public final class ResolvableTypeProviderPayloadApplicationEvent<T extends ResolvableTypeProvider> extends PayloadApplicationEvent<T>
{
	public ResolvableTypeProviderPayloadApplicationEvent( Object source, T payload ) {
		super( source, payload );
	}

	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics( getClass(), getPayload().getResolvableType() );
	}
}
