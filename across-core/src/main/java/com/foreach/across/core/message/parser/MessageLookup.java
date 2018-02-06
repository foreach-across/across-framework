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
package com.foreach.across.core.message.parser;

import com.foreach.across.core.message.ResolvableMessage;
import com.foreach.across.core.message.ResolvableMessageFormatContext;
import lombok.Data;

/**
 * Token that represents another message that should be looked up.
 * Can in turn contain expressions or arguments that represent the message codes,
 * or message arguments.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Data
class MessageLookup implements MessageToken, MessageTokenOutput
{
	private final String[] messageCodes;

	@Override
	public MessageTokenOutput createFormat( ResolvableMessageFormatContext context ) {
		return this;
	}

	@Override
	public boolean isLocalized() {
		return false;
	}

	@Override
	public boolean requiresSynchronization() {
		return false;
	}

	@Override
	public void write( StringBuilder output, ResolvableMessageFormatContext context ) {
		output.append( context.resolveMessage( ResolvableMessage.messageCode( messageCodes ), true ) );
	}
}
