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

import com.foreach.across.core.support.AttributeOverridingSupport;
import com.foreach.across.core.support.AttributeSupport;
import com.foreach.across.core.support.ReadableAttributes;
import com.foreach.across.modules.web.context.WebAppLinkBuilder;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.resource.WebResourceUtils;
import com.foreach.across.modules.web.support.LocalizedTextResolver;
import com.foreach.across.modules.web.support.MessageCodeSupportingLocalizedTextResolver;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;

import java.util.Locale;
import java.util.Map;

/**
 * <p>Standard implementation of a {@link ViewElementBuilderContext} that optionally allows a parent set of attributes
 * to be provided.  Attributes from the parent will be visible in the context, but attributes set on the builder
 * context directly will hide the same attributes from the parent.  Hiding with a non-null value is possible, but
 * removing attributes entirely from the parent is not.</p>
 * <p>For example when creating a new context based on a {@link Model}, all attributes from the model will be
 * available on the context.  Additional attributes can be added to the context only and will not be available in the
 * original Model.  Changes to the model should propagate to the model (depending on the model implementation).</p>
 * <p>Custom implementations can override {@link #setParent(ReadableAttributes)} to allow the parent to be set
 * after construction.</p>
 * <p>This implementation also registers some default attributes, like the {@link WebResourceRegistry},
 * see {@link #registerMissingDefaultAttributes(ViewElementBuilderContext)}</p>.  See also
 * {@link #DefaultViewElementBuilderContext(boolean)} for sub class implementations that do not want the default
 * attributes set.
 *
 * @author Arne Vandamme
 * @see IteratorViewElementBuilderContext
 */
public class DefaultViewElementBuilderContext extends AttributeOverridingSupport implements ViewElementBuilderContext
{
	public DefaultViewElementBuilderContext() {
		this( true );
	}

	protected DefaultViewElementBuilderContext( boolean registerDefaultAttributes ) {
		if ( registerDefaultAttributes ) {
			registerMissingDefaultAttributes( this );
		}
	}

	public DefaultViewElementBuilderContext( Model parent ) {
		this( parent.asMap() );
	}

	public DefaultViewElementBuilderContext( Map<String, Object> parent ) {
		this( new AttributesMapWrapper( parent ) );
	}

	public DefaultViewElementBuilderContext( ReadableAttributes parent ) {
		setParent( parent );
		registerMissingDefaultAttributes( this );
	}

	/**
	 * <p>Set the {@link WebResourceRegistry} for this builder context.  Builders can register additional resources if
	 * required for the view elements they generate.  For example a datepicker control could register additional
	 * javascript.</p>
	 * <p>This method is an alias to calling {@link #setAttribute(Class, Object)} with {@code WebResourceRegistry.class}
	 * as attribute name, and the instance as value.  The explicit method indicates that this attribute is not
	 * required but is expected.</p>
	 *
	 * @param webResourceRegistry instance
	 */
	public void setWebResourceRegistry( WebResourceRegistry webResourceRegistry ) {
		setAttribute( WebResourceRegistry.class, webResourceRegistry );
	}

	public WebResourceRegistry getWebResourceRegistry() {
		return getAttribute( WebResourceRegistry.class );
	}

	/**
	 * Set the {@link WebAppLinkBuilder} that should be used for calls to {@link #buildLink(String)}.
	 * By default the link builder attached to the current request would have been registered.
	 *
	 * @param linkBuilder instance
	 */
	public void setWebAppLinkBuilder( WebAppLinkBuilder linkBuilder ) {
		setAttribute( WebAppLinkBuilder.class, linkBuilder );
	}

	public WebAppLinkBuilder getWebAppLinkBuilder() {
		return getAttribute( WebAppLinkBuilder.class );
	}

	/**
	 * Set the {@link MessageSource} that should be used in this builder context.
	 *
	 * @param messageSource to use
	 */
	public void setMessageSource( MessageSource messageSource ) {
		setAttribute( MessageSource.class, messageSource );
	}

	public MessageSource getMessageSource() {
		return getAttribute( MessageSource.class );
	}

	/**
	 * Set the {@link LocalizedTextResolver} to use in this builder context.
	 *
	 * @param textResolver to use
	 */
	public void setLocalizedTextResolver( LocalizedTextResolver textResolver ) {
		setAttribute( LocalizedTextResolver.class, textResolver );
	}

	public LocalizedTextResolver getLocalizedTextResolver() {
		return getAttribute( LocalizedTextResolver.class );
	}

	@Override
	public String buildLink( String baseLink ) {
		WebAppLinkBuilder linkBuilder = getWebAppLinkBuilder();
		return linkBuilder != null ? linkBuilder.buildLink( baseLink ) : baseLink;
	}

	@Override
	public String resolveText( String text ) {
		return getLocalizedTextResolver().resolveText( text );
	}

	@Override
	public String resolveText( String text, Locale locale ) {
		return getLocalizedTextResolver().resolveText( text, locale );
	}

	@Override
	public String resolveText( String text, String defaultValue ) {
		return getLocalizedTextResolver().resolveText( text, defaultValue );
	}

	@Override
	public String resolveText( String text, String defaultValue, Locale locale ) {
		return getLocalizedTextResolver().resolveText( text, defaultValue, locale );
	}

	@Override
	public String getMessage( String code ) {
		return getMessage( code, code );
	}

	@Override
	public String getMessage( String code, Locale locale ) {
		return getMessage( code, code, locale );
	}

	@Override
	public String getMessage( String code, String defaultMessage ) {
		return getMessage( code, code, LocaleContextHolder.getLocale() );
	}

	@Override
	public String getMessage( String code, String defaultMessage, Locale locale ) {
		return getMessage( code, new Object[0], defaultMessage, locale );
	}

	@Override
	public String getMessage( String code, Object[] args ) {
		return getMessage( code, args, code );
	}

	@Override
	public String getMessage( String code, Object[] args, Locale locale ) {
		return getMessage( code, args, code, locale );
	}

	@Override
	public String getMessage( String code, Object[] args, String defaultMessage ) {
		return getMessage( code, args, defaultMessage, LocaleContextHolder.getLocale() );
	}

	@Override
	public String getMessage( String code, Object[] args, String defaultMessage, Locale locale ) {
		return getMessageSource().getMessage( code, args, defaultMessage, locale );
	}

	private static class AttributesMapWrapper extends AttributeSupport
	{
		AttributesMapWrapper( Map<String, Object> backingMap ) {
			super( backingMap );
		}
	}

	/**
	 * Registers default attributes in the builder context if they are not yet present.  If they are present
	 * - either directly or in the parent - they will be skipped.
	 * The following request-bound attributes are registered by default if present:
	 * <ul>
	 * <li>{@link WebResourceRegistry}</li>
	 * <li>{@link WebAppLinkBuilder}</li>
	 * <li>{@link MessageSource}</li>
	 * <li>{@link LocalizedTextResolver} (created if a {@link MessageSource} is present)</li>
	 * </ul>
	 *
	 * @param builderContext to add the attributes to
	 */
	public static void registerMissingDefaultAttributes( ViewElementBuilderContext builderContext ) {
		if ( !builderContext.hasAttribute( WebResourceRegistry.class ) ) {
			WebResourceUtils.currentRegistry().ifPresent(
					r -> builderContext.setAttribute( WebResourceRegistry.class, r )
			);
		}
		if ( !builderContext.hasAttribute( WebAppLinkBuilder.class ) ) {
			WebResourceUtils.currentLinkBuilder().ifPresent(
					lb -> builderContext.setAttribute( WebAppLinkBuilder.class, lb )
			);
		}
		if ( !builderContext.hasAttribute( MessageSource.class ) ) {
			WebResourceUtils.currentMessageSource().ifPresent(
					lb -> builderContext.setAttribute( MessageSource.class, lb )
			);
		}
		if ( !builderContext.hasAttribute( LocalizedTextResolver.class ) ) {
			MessageSource messageSource = builderContext.getAttribute( MessageSource.class );
			if ( messageSource != null ) {
				builderContext.setAttribute( LocalizedTextResolver.class, new MessageCodeSupportingLocalizedTextResolver( messageSource ) );
			}
		}
	}
}
