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
package com.foreach.across.modules.web.ui;

import lombok.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

/**
 * Fallback implementation in case there is no message source.
 * Will usually return the code that was to be resolved or the default message in case of {@link MessageSourceResolvable}.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
class FallbackMessageSource implements MessageSource
{
	@Override
	public String getMessage( String code, Object[] args, String defaultMessage, Locale locale ) {
		return code;
	}

	@Override
	public String getMessage( String code, Object[] args, Locale locale ) throws NoSuchMessageException {
		return code;
	}

	@Override
	public String getMessage( @NonNull MessageSourceResolvable resolvable, Locale locale ) throws NoSuchMessageException {
		return resolvable.getDefaultMessage();
	}
}
