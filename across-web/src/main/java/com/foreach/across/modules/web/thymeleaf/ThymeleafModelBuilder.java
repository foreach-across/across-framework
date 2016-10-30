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
package com.foreach.across.modules.web.thymeleaf;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementAttributeConverter;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementNodeBuilderRegistry;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementThymeleafBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.thymeleaf.context.IEngineContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.AttributeValueQuotes;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.unbescape.html.HtmlEscape;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Specialized builder class wrapping around a Thymeleaf {@link org.thymeleaf.model.IModelFactory}.
 * Meant for writing HTML style elements.
 * <p/>
 * When an element is opened, its attributes can be modified until it is flushed to the Thymeleaf model.
 * Flushing happens when either a new element is opened, or the current element is closed.
 * <p/>
 * An attribute can have multiple values, these will be joined together with a DOUBLE space character.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class ThymeleafModelBuilder
{
	private final ITemplateContext templateContext;
	private final ViewElementNodeBuilderRegistry nodeBuilderRegistry;
	private final HtmlIdStore htmlIdStore;
	private final ViewElementAttributeConverter attributeConverter;

	private final IModelFactory modelFactory;
	private final IModel model;

	private final Deque<String> openTags = new ArrayDeque<>();
	private final Map<String, Collection<String>> pendingTagAttributes = new HashMap<>();
	private String pendingTag;

	public ThymeleafModelBuilder( ITemplateContext templateContext,
	                              ViewElementNodeBuilderRegistry nodeBuilderRegistry,
	                              HtmlIdStore htmlIdStore,
	                              ViewElementAttributeConverter attributeConverter ) {
		Assert.notNull( templateContext );
		Assert.notNull( nodeBuilderRegistry );
		Assert.notNull( htmlIdStore );
		Assert.notNull( attributeConverter );

		this.templateContext = templateContext;
		this.nodeBuilderRegistry = nodeBuilderRegistry;
		this.htmlIdStore = htmlIdStore;
		this.attributeConverter = attributeConverter;

		this.modelFactory = templateContext.getModelFactory();
		this.model = modelFactory.createModel();
	}

	/**
	 * @return Thymeleaf template context
	 */
	public ITemplateContext getTemplateContext() {
		return templateContext;
	}

	/**
	 * Get a unique id for the specific element.  Takes into account the id property set if it
	 * is a {@link com.foreach.across.modules.web.ui.elements.HtmlViewElement} but will ensure duplicates
	 * return a unique value.
	 *
	 * @param viewElement to get a unique id for
	 * @return unique id
	 */
	public String retrieveHtmlId( ViewElement viewElement ) {
		return htmlIdStore.retrieveHtmlId( templateContext, viewElement );
	}

	/**
	 * Add the element to the model.  Will lookup the {@link ViewElementThymeleafBuilder} for the element typs
	 * in the {@link ViewElementNodeBuilderRegistry} attached to this model builder.
	 *
	 * @param viewElement to add
	 */
	public void addViewElement( ViewElement viewElement ) {
		if ( hasCustomTemplate( viewElement ) ) {
			renderCustomTemplate( viewElement, templateContext );
		}
		else {
			ViewElementThymeleafBuilder<ViewElement> processor = nodeBuilderRegistry.getNodeBuilder( viewElement );

			if ( processor != null ) {
				processor.writeModel( viewElement, this );
			}
		}
	}

	private void renderCustomTemplate( ViewElement viewElement, ITemplateContext context ) {
		writePendingTag();
		if ( context instanceof IEngineContext ) {
			( (IEngineContext) context ).setVariable( "component", viewElement );
			String templateWithFragment = appendFragmentIfRequired( viewElement.getCustomTemplate() );

			model.add( modelFactory.createOpenElementTag( "div", "th:replace", templateWithFragment, false ) );
			model.add( modelFactory.createCloseElementTag( "div" ) );
		}
		else {
			throw new IllegalStateException(
					"Custom template rendering is only available when the template context is of type EngineContext." );
		}
	}

	/**
	 * Append the fragment to the custom template name if there is no fragment.
	 */
	private String appendFragmentIfRequired( String customTemplate ) {
		if ( !StringUtils.contains( customTemplate, "::" ) ) {
			return customTemplate + " :: render(${component})";
		}

		return customTemplate;
	}

	private boolean hasCustomTemplate( ViewElement viewElement ) {
		return viewElement.getCustomTemplate() != null;
	}

	/**
	 * Return the finished model, will balance any pending elements first.
	 *
	 * @return model
	 */
	public IModel createModel() {
		balanceOpenTags();
		return model;
	}

	/**
	 * Add some text to the model.  Text will be escaped.
	 *
	 * @param text to add
	 */
	public void addText( String text ) {
		addText( text, true );
	}

	/**
	 * Add some HTML to the model.
	 *
	 * @param html to add
	 */
	public void addHtml( String html ) {
		addText( html, false );
	}

	/**
	 * Add some text to the model.
	 *
	 * @param text      to add
	 * @param escapeXml true if text should be escaped
	 */
	public void addText( String text, boolean escapeXml ) {
		writePendingTag();
		String html = escapeXml ? HtmlEscape.escapeHtml4Xml( text ) : text;
		model.add( modelFactory.createText( html ) );
	}

	/**
	 * Adds all attributes to the current open element.
	 * Will throw an exception if no element is currently opened and still available for modification.
	 * Any other attributes will remain, but attributes with the same name will be replaced.
	 *
	 * @param attributes to set
	 */
	public void addAttributes( Map<String, Collection<Object>> attributes ) {
		verifyPendingTag();
		attributes.forEach( this::addAttribute );
	}

	/**
	 * Set the attribute with the given name.
	 * Will replace any existing attribute values.
	 * Duplicate values will be ignored.
	 *
	 * @param attributeName name of the attribute to set
	 * @param values        to set for the attribute
	 */
	public void addAttribute( String attributeName, Object... values ) {
		verifyPendingTag();
		addAttribute( attributeName,
		              values.length == 0 ? Collections.singleton( attributeName ) : Arrays.asList( values ) );
	}

	private void addAttribute( String attributeName, Collection<Object> values ) {
		pendingTagAttributes.compute( attributeName, ( key, v ) -> {
			List<String> newValues = convertToValidAttributeValues( values );
			return newValues.isEmpty() ? null : newValues;
		} );
	}

	/**
	 * Only keep non-null and no duplicates.  Escape markup characters from attribute value.
	 */
	private List<String> convertToValidAttributeValues( Collection<Object> candidates ) {
		return candidates.stream()
		                 .map( attributeConverter )
		                 .filter( Objects::nonNull )
		                 .distinct()
		                 .map( HtmlEscape::escapeHtml4Xml )
		                 .collect( Collectors.toList() );
	}

	/**
	 * Add values for a specific attribute.  Any other values will remain.
	 * Duplicate values will be ignored.
	 *
	 * @param attributeName name of the attribute to modify
	 * @param values        to add to the attribute
	 */
	public void addAttributeValue( String attributeName, Object... values ) {
		verifyPendingTag();
		pendingTagAttributes.computeIfAbsent( attributeName, k -> new LinkedHashSet<>() )
		                    .addAll( convertToValidAttributeValues( Arrays.asList( values ) ) );
	}

	/**
	 * Remove a DOUBLE attribute for the open element.
	 *
	 * @param attributeName name of the attribute to remove
	 */
	public void removeAttribute( String attributeName ) {
		verifyPendingTag();
		pendingTagAttributes.remove( attributeName );
	}

	/**
	 * Removes all currently declared attributes for the open element.
	 */
	public void removeAttributes() {
		pendingTagAttributes.clear();
	}

	/**
	 * Remove one or more values for a particular attribute.
	 * If no values remain, the entire attribute will be removed.
	 *
	 * @param attributeName name of the attribute from which you want to remove some values
	 * @param values        to remove
	 */
	public void removeAttributeValue( String attributeName, Object... values ) {
		verifyPendingTag();
		pendingTagAttributes.computeIfPresent( attributeName, ( k, v ) -> {
			v.removeAll( convertToValidAttributeValues( Arrays.asList( values ) ) );
			return v.isEmpty() ? null : v;
		} );
	}

	/**
	 * Start a new element with the specific tag name.
	 * After the element is opened, attributes can be modified until it is flushed.
	 *
	 * @param tagName of the element
	 */
	public void addOpenElement( String tagName ) {
		writePendingTag();
		pendingTag = tagName;
		openTags.push( tagName );
	}

	/**
	 * Closes the current element, flushes it to the model.
	 */
	public void addCloseElement() {
		writePendingTag();
		String tagName = openTags.pop();
		model.add( modelFactory.createCloseElementTag( tagName ) );
	}

	private void writePendingTag() {
		if ( pendingTag != null ) {
			model.add(
					modelFactory.createOpenElementTag(
							pendingTag, buildAttributeValues(), AttributeValueQuotes.DOUBLE, false
					)
			);
			pendingTag = null;
		}
	}

	private Map<String, String> buildAttributeValues() {
		if ( !pendingTagAttributes.isEmpty() ) {
			return pendingTagAttributes
					.entrySet()
					.stream()
					.collect( Collectors.toMap( Map.Entry::getKey, e -> StringUtils.join( e.getValue(), ' ' ) ) );
		}
		return Collections.emptyMap();
	}

	private void balanceOpenTags() {
		while ( !openTags.isEmpty() ) {
			addCloseElement();
		}
	}

	private void verifyPendingTag() {
		if ( pendingTag == null ) {
			throw new IllegalStateException( "No currently open element." );
		}
	}
}
