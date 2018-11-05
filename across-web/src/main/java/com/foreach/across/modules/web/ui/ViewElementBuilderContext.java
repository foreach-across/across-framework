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

import com.foreach.across.core.support.InheritedAttributeValue;
import com.foreach.across.core.support.WritableAttributes;
import com.foreach.across.modules.web.resource.WebResourceUtils;
import com.foreach.across.modules.web.support.LocalizedTextResolver;

import java.util.Locale;
import java.util.Optional;

public interface ViewElementBuilderContext extends WritableAttributes, LocalizedTextResolver
{
	/**
	 * Try to resolve the message. Return the message code no message was found.
	 *
	 * @param code the code to lookup up, such as 'calculator.noRateSet'
	 * @return the resolved message if the lookup was successful, otherwise the code itself
	 * @see java.text.MessageFormat
	 */
	String getMessage( String code );

	/**
	 * Try to resolve the message. Return default message if no message was found.
	 *
	 * @param code   the code to lookup up, such as 'calculator.noRateSet'
	 * @param locale the Locale in which to do the lookup
	 * @return the resolved message if the lookup was successful;  otherwise the code itself
	 * @see java.text.MessageFormat
	 */
	String getMessage( String code, Locale locale );

	/**
	 * Try to resolve the message. Return default message if no message was found.
	 *
	 * @param code           the code to lookup up, such as 'calculator.noRateSet'
	 * @param defaultMessage String to return if the lookup fails
	 * @return the resolved message if the lookup was successful;
	 * otherwise the default message passed as a parameter
	 * @see java.text.MessageFormat
	 */
	String getMessage( String code, String defaultMessage );

	/**
	 * Try to resolve the message. Return default message if no message was found.
	 *
	 * @param code           the code to lookup up, such as 'calculator.noRateSet'
	 * @param defaultMessage String to return if the lookup fails
	 * @param locale         the Locale in which to do the lookup
	 * @return the resolved message if the lookup was successful;
	 * otherwise the default message passed as a parameter
	 * @see java.text.MessageFormat
	 */
	String getMessage( String code, String defaultMessage, Locale locale );

	/**
	 * Try to resolve the message. Return message code if no message was found.
	 *
	 * @param code the code to lookup up, such as 'calculator.noRateSet'
	 * @param args array of arguments that will be filled in for params within
	 *             the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 *             or {@code null} if none.
	 * @return the resolved message if the lookup was successful; otherwise the message code
	 * @see java.text.MessageFormat
	 */
	String getMessage( String code, Object[] args );

	/**
	 * Try to resolve the message. Return message code if no message was found.
	 *
	 * @param code   the code to lookup up, such as 'calculator.noRateSet'
	 * @param args   array of arguments that will be filled in for params within
	 *               the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 *               or {@code null} if none.
	 * @param locale the Locale in which to do the lookup
	 * @return the resolved message if the lookup was successful; otherwise the message code
	 * @see java.text.MessageFormat
	 */
	String getMessage( String code, Object[] args, Locale locale );

	/**
	 * Try to resolve the message. Return default message if no message was found.
	 *
	 * @param code           the code to lookup up, such as 'calculator.noRateSet'
	 * @param args           array of arguments that will be filled in for params within
	 *                       the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 *                       or {@code null} if none.
	 * @param defaultMessage String to return if the lookup fails
	 * @return the resolved message if the lookup was successful;
	 * otherwise the default message passed as a parameter
	 * @see java.text.MessageFormat
	 */
	String getMessage( String code, Object[] args, String defaultMessage );

	/**
	 * Try to resolve the message. Return default message if no message was found.
	 *
	 * @param code           the code to lookup up, such as 'calculator.noRateSet'
	 * @param args           array of arguments that will be filled in for params within
	 *                       the message (params look like "{0}", "{1,date}", "{2,time}" within a message),
	 *                       or {@code null} if none.
	 * @param defaultMessage String to return if the lookup fails
	 * @param locale         the Locale in which to do the lookup
	 * @return the resolved message if the lookup was successful;
	 * otherwise the default message passed as a parameter
	 * @see java.text.MessageFormat
	 */
	String getMessage( String code, Object[] args, String defaultMessage, Locale locale );

	/**
	 * Will build a link using the {@link com.foreach.across.modules.web.context.WebAppLinkBuilder} attribute
	 * that is present on this context.  If none is present, the baseLink will remain unmodified.
	 *
	 * @param baseLink to process
	 * @return processed link
	 */
	String buildLink( String baseLink );

	/**
	 * Find an attribute value in this builder context or any of its parents, in case of a hierarchy.
	 *
	 * @param attributeName name of the attribute
	 * @return attribute value information - never {@code null}
	 */
	InheritedAttributeValue<Object> findAttribute( String attributeName );

	/**
	 * Find an attribute value in this builder context or any of its parents, in case of a hierarchy.
	 *
	 * @param attributeType type of the attribute
	 * @return attribute value information - never {@code null}
	 */
	<U> InheritedAttributeValue<U> findAttribute( Class<U> attributeType );

	/**
	 * Find an attribute value in this builder context or any of its parents, in case of a hierarchy.
	 *
	 * @param attributeName name of the attribute
	 * @param attributeType expected type of the attribute
	 * @return attribute value information - never {@code null}
	 */
	<U> InheritedAttributeValue<U> findAttribute( String attributeName, Class<U> attributeType );

	/**
	 * Create a temporary {@link java.io.Closeable} scope for the current {@link ViewElementBuilderContext} in which
	 * the given attribute value is overridden with the specified value. Setting {@code null} as attribute value
	 * will "remove" the attribute.
	 * <p/>
	 * Once {@link ScopedAttributesViewElementBuilderContext#close()} is called, the original attribute values will be reset.
	 * <p/>
	 * <strong>WARNING:</strong> this does not create a new separate builder context, it effectively modified the current
	 * builder context as long as {@link ScopedAttributesViewElementBuilderContext#close()} is not called.
	 *
	 * @param attributeName name of the attribute to override
	 * @param attributeValue value to override the attribute with
	 * @return builder context scope
	 */
	default ScopedAttributesViewElementBuilderContext withAttributeOverride( String attributeName, Object attributeValue ) {
		return new ScopedAttributesViewElementBuilderContext( this ).withAttributeOverride( attributeName, attributeValue );
	}

	/**
	 * Create a temporary {@link java.io.Closeable} scope for the current {@link ViewElementBuilderContext} in which
	 * the given attribute value is overridden with the specified value. Setting {@code null} as attribute value
	 * will "remove" the attribute.
	 * <p/>
	 * Once {@link ScopedAttributesViewElementBuilderContext#close()} is called, the original attribute values will be reset.
	 * <p/>
	 * <strong>WARNING:</strong> this does not create a new separate builder context, it effectively modified the current
	 * builder context as long as {@link ScopedAttributesViewElementBuilderContext#close()} is not called.
	 *
	 * @param attributeType type of the attribute to override
	 * @param attributeValue value to override the attribute with
	 * @return builder context scope
	 */
	default <Y> ScopedAttributesViewElementBuilderContext withAttributeOverride( Class<Y> attributeType, Y attributeValue ) {
		return new ScopedAttributesViewElementBuilderContext( this ).withAttributeOverride( attributeType, attributeValue );
	}

	/**
	 * Fetches the global {@link ViewElementBuilderContext}.  Can be bound to the local thread using
	 * {@link ViewElementBuilderContextHolder} or - lacking that - to the request attributes.
	 *
	 * @return optional buildercontext
	 * @see ViewElementBuilderContextHolder
	 */
	static Optional<ViewElementBuilderContext> retrieveGlobalBuilderContext() {
		return Optional.ofNullable(
				ViewElementBuilderContextHolder
						.getViewElementBuilderContext()
						.orElseGet( () -> WebResourceUtils.currentViewElementBuilderContext().orElse( null ) )
		);
	}
}
