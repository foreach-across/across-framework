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
package test.modules.module2;

import com.foreach.across.core.events.AcrossEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * @author arne
 * @since 2.0.0
 */
public class SingleGenericEvent<T> implements AcrossEvent, ResolvableTypeProvider
{
	private final Class<T> type;

	public SingleGenericEvent( Class<T> type ) {
		this.type = type;
	}

	@Override
	public ResolvableType getResolvableType() {
		ResolvableType classResolvableType = ResolvableType.forClass( getClass() );
		return classResolvableType.hasGenerics()
				? ResolvableType.forClassWithGenerics( getClass(), type )
				: classResolvableType;
	}
}
