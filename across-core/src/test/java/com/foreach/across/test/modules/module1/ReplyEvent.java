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
package com.foreach.across.test.modules.module1;

import com.foreach.across.test.modules.EventPubSub;
import lombok.Getter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class ReplyEvent
{
	@Getter
	private EventPubSub.ByName byName;

	@Component
	public static class Listener
	{
		@EventListener
		EventPubSub.ByName handle( ReplyEvent original ) {
			original.byName = new EventPubSub.ByName( "Replied" );
			return original.byName;
		}
	}
}
