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
package com.foreach.across.modules.web.support;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * Attempts to localize a text snippet.  An actual piece of text will simply be returned as is.
 * If the text is a message code token, the message code will be resolved instead, and the optional
 * default text will be used as fallback if the code can't be resolved.
 * <p/>
 * Example supported text formats:
 * <ul>
 * <li><strong>Some text</strong> : will always return <em>Some text</em></li>
 * <li><strong>#{message.code}</strong> : will return the resolved message code value or the message code itself if unresolvable</li>
 * <li><strong>#{message.code=Some text}</strong> : will return the resolved message code value or <em>Some text</em> if unresolvable</li>
 * </ul>
 *
 * @author Arne Vandamme
 * @since 2.1.0
 */
public final class MessageCodeSupportingLocalizedTextResolver implements LocalizedTextResolver
{
	private final MessageSource messageSource;

	public MessageCodeSupportingLocalizedTextResolver( MessageSource messageSource ) {
		this.messageSource = messageSource;
	}

	public String resolveText( String text ) {
		return resolveText( text, LocaleContextHolder.getLocale() );
	}

	public String resolveText( String text, Locale locale ) {
		return resolveText( text, null, locale );
	}

	public String resolveText( String text, String defaultValue ) {
		return resolveText( text, defaultValue, LocaleContextHolder.getLocale() );
	}

	public String resolveText( String text, String defaultValue, Locale locale ) {
		if ( text != null ) {
			String[] codeElements = parse( text );

			if ( codeElements != null ) {
				String actualDefault = StringUtils.defaultString( defaultValue != null ? defaultValue : codeElements[1], codeElements[0] );
				return messageSource.getMessage( codeElements[0], new Object[0], actualDefault, locale );
			}

			return text;
		}

		return defaultValue;
	}

	private String[] parse( String text ) {
		if ( text.startsWith( "#{" ) && text.endsWith( "}" ) ) {
			int sep = text.indexOf( '=' );
			return sep > 2 ?
					new String[] { text.substring( 2, sep ), text.substring( sep + 1, text.length() - 1 ) } :
					new String[] { text.substring( 2, text.length() - 1 ), null };
		}
		return null;
	}
}
