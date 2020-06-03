/*
 * Copyright 2019 the original author or authors
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

import com.foreach.across.modules.web.template.WebTemplateInterceptor;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementAttributeConverter;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementModelWriter;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementModelWriterRegistry;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
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
public final class ThymeleafModelBuilder
{
	private final ITemplateContext templateContext;
	private final ViewElementModelWriterRegistry nodeBuilderRegistry;
	private final HtmlIdStore htmlIdStore;
	private final ViewElementAttributeConverter attributeConverter;
	private final AttributeNameGenerator attributeNameGenerator;

	private final IModelFactory modelFactory;
	private final IModel model;

	private final Deque<String> openTags = new ArrayDeque<>();
	private final Map<String, Collection<String>> pendingTagAttributes = new HashMap<>();
	private final boolean developmentMode;

	private String pendingTag;
	private String viewElementName;

	public ThymeleafModelBuilder( @NonNull ITemplateContext templateContext,
	                              @NonNull ViewElementModelWriterRegistry nodeBuilderRegistry,
	                              @NonNull HtmlIdStore htmlIdStore,
	                              @NonNull ViewElementAttributeConverter attributeConverter,
	                              @NonNull AttributeNameGenerator attributeNameGenerator,
	                              boolean developmentMode ) {
		this.templateContext = templateContext;
		this.nodeBuilderRegistry = nodeBuilderRegistry;
		this.htmlIdStore = htmlIdStore;
		this.attributeConverter = attributeConverter;
		this.attributeNameGenerator = attributeNameGenerator;

		this.modelFactory = templateContext.getModelFactory();
		this.model = modelFactory.createModel();
		this.developmentMode = developmentMode;
	}

	private ThymeleafModelBuilder( ThymeleafModelBuilder parent ) {
		templateContext = parent.templateContext;
		nodeBuilderRegistry = parent.nodeBuilderRegistry;
		htmlIdStore = parent.htmlIdStore;
		attributeConverter = parent.attributeConverter;
		attributeNameGenerator = parent.attributeNameGenerator;
		developmentMode = parent.developmentMode;

		modelFactory = templateContext.getModelFactory();
		model = modelFactory.createModel();
	}

	/**
	 * @return Thymeleaf template context
	 */
	public ITemplateContext getTemplateContext() {
		return templateContext;
	}

	/**
	 * @return Thymeleaf model factory
	 */
	public IModelFactory getModelFactory() {
		return modelFactory;
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
	 * Add the element to the model.  Will lookup the {@link ViewElementModelWriter} for the element type
	 * in the {@link ViewElementModelWriterRegistry} attached to this model builder.
	 * <p/>
	 * If the request contains a {@link WebTemplateInterceptor#RENDER_VIEW_ELEMENT} attribute, it is considered to be the name of the ViewElement
	 * that should have its actual output rendered.  All other ViewElements will still get built, but their markup suppressed.
	 * In case of a partial rendering, special processing instructions are added to tell the {@link PartialViewElementTemplateProcessor} to allow the markup.
	 *
	 * @param viewElement to add
	 */
	public void addViewElement( ViewElement viewElement ) {
		if ( viewElement != null ) {
			String partialName = (String) templateContext.getVariable( WebTemplateInterceptor.RENDER_VIEW_ELEMENT );

			boolean partialRenderingEnabled = false;

			if ( partialName != null && partialName.equals( viewElement.getName() ) ) {
				partialRenderingEnabled = true;
				writePendingTag();
				model.add( modelFactory.createProcessingInstruction( WebTemplateInterceptor.RENDER_VIEW_ELEMENT, "start" ) );
			}

			boolean writeViewElementName = developmentMode && viewElement.getName() != null;

			if ( writeViewElementName ) {
				writePendingTag();
				writeViewElementNameComment( viewElement.getName(), false );
			}

			if ( hasCustomTemplate( viewElement ) ) {
				renderCustomTemplate( viewElement, templateContext );
			}
			else {
				ViewElementModelWriter<ViewElement> processor = nodeBuilderRegistry.getModelWriter( viewElement );

				if ( processor != null ) {
					viewElementName = viewElement.getName();
					processor.writeModel( viewElement, this );
				}
			}

			if ( writeViewElementName ) {
				writeViewElementNameComment( viewElement.getName(), true );
			}

			if ( partialRenderingEnabled ) {
				writePendingTag();
				model.add( modelFactory.createProcessingInstruction( WebTemplateInterceptor.RENDER_VIEW_ELEMENT, "end" ) );
			}
		}
	}

	/**
	 * Create a separate {@link IModel} for a {@link ViewElement}.  The model will use the configuration of this
	 * {@link ThymeleafModelBuilder} but will not yet been added.  This method is useful if you want to manually
	 * post-process a model before adding it.  Adding the child model can be done through {@link #addModel(IModel)}.
	 *
	 * @param viewElement to create the model for
	 * @return model
	 */
	public IModel createViewElementModel( ViewElement viewElement ) {
		ThymeleafModelBuilder nestedBuilder = new ThymeleafModelBuilder( this );
		nestedBuilder.addViewElement( viewElement );
		return nestedBuilder.retrieveModel();
	}

	/**
	 * @return a separate model builder using the same configuration as the current one
	 */
	public ThymeleafModelBuilder createChildModelBuilder() {
		return new ThymeleafModelBuilder( this );
	}

	/**
	 * Directly add a child model to the current {@link IModel} this builder represents.
	 *
	 * @param childModel to add
	 */
	public void addModel( IModel childModel ) {
		retrieveModel().addModel( childModel );
	}

	private void renderCustomTemplate( ViewElement viewElement, ITemplateContext context ) {
		writePendingTag();
		if ( context instanceof IEngineContext ) {
			String attributeName = attributeNameGenerator.generateAttributeName();
			( (IEngineContext) context ).setVariable( attributeName, viewElement );
			String templateWithFragment = StringUtils.replace(
					appendFragmentIfRequired( viewElement.getCustomTemplate() ), "${component", "${" + attributeName
			);

			Map<String, String> attributes = new HashMap<>( 2 );
			attributes.put( "th:insert", templateWithFragment );
			attributes.put( "th:inline", context.getTemplateMode().name() );

			model.add( modelFactory.createOpenElementTag( "th:block", attributes, AttributeValueQuotes.DOUBLE, false ) );
			model.add( modelFactory.createCloseElementTag( "th:block" ) );
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
			return customTemplate + " :: render(component=${component})";
		}
		else if ( !StringUtils.endsWith( customTemplate, ")" ) ) {
			return customTemplate + "(component=${component})";
		}

		return customTemplate;
	}

	private boolean hasCustomTemplate( ViewElement viewElement ) {
		return viewElement.getCustomTemplate() != null;
	}

	/**
	 * Return the current model, ensures pending tags have been written.
	 *
	 * @return model
	 */
	public IModel retrieveModel() {
		writePendingTag();
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
		if ( text != null ) {
			writePendingTag();
			String html = escapeXml ? HtmlEscape.escapeHtml4Xml( text ) : text;
			model.add( modelFactory.createText( html ) );
		}
	}

	private void writeViewElementNameComment( String elementName, boolean closing ) {
		model.add( modelFactory.createComment( ( closing ? "[/ax:" : "[ax:" ) + elementName + "]" ) );
	}

	/**
	 * Adds all attributes to the current open element.
	 * Will throw an exception if no element is currently opened and still available for modification.
	 * Any other attributes will remain, but attributes with the same name will be replaced if there is
	 * at least one valid value for that attribute.
	 *
	 * @param attributes to set
	 * @see #addAttribute(String, Object...) for more information on possible values
	 */
	public void addAttributes( Map<String, Collection<Object>> attributes ) {
		verifyPendingTag();
		attributes.forEach( this::addAttribute );
	}

	/**
	 * Add a boolean attribute.  A boolean attribute is an attribute that is either present or not,
	 * its value is determined by its presence.  In regular html for example this
	 * would be written as {@code required="required"}.
	 * <p/>
	 * This method will add the attribute if {@param value} is {@code true}.
	 * Any existing attribute with that name will be replaced, or removed if {@param value} is {@code false}.
	 * <p/>
	 * Requires an open element.
	 *
	 * @param attributeName name of the attribute
	 * @param value         true if it should be added
	 */
	public void addBooleanAttribute( String attributeName, boolean value ) {
		if ( value ) {
			addAttribute( attributeName );
		}
		else {
			removeAttribute( attributeName );
		}
	}

	/**
	 * Set the attribute with the given name. Will replace any existing attribute values.
	 * <p/>
	 * If {@param values} is empty, a single value identical to {@param attributeName} will be added.
	 * If {@param values} is not empty but contains only {@code null}, the attribute will be ignored
	 * and any previously registered value will be kept.
	 * All values will be XML escaped and duplicate values will be ignored.
	 * <p/>
	 * Requires an open element.
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
			return newValues.isEmpty() ? v : newValues;
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
	 * Values will be XML escaped and duplicate values will be ignored.
	 * <p/>
	 * Requires an open element.
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
	 * Remove a single attribute for the open element.
	 * <p/>
	 * Requires an open element.
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
	 * <p/>
	 * Requires an open element.
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
		if ( developmentMode && viewElementName != null ) {
			addAttribute( "data-ax-dev-view-element", viewElementName );
			viewElementName = null;
		}
		openTags.push( tagName );
	}

	/**
	 * Change the open element to the specific tag.
	 * Will throw {@link IllegalStateException} if there is no currently open element that has not yet
	 * been flushed.
	 *
	 * @param tagName of the element
	 */
	public void changeOpenElement( String tagName ) {
		verifyPendingTag();
		pendingTag = tagName;
		openTags.pop();
		openTags.push( tagName );
	}

	/**
	 * Closes the current element, flushes it to the model.
	 */
	public void addCloseElement() {
		if ( openTags.isEmpty() ) {
			throw new IllegalStateException( "No more open elements to close." );
		}
		writePendingTag();
		String tagName = openTags.pop();
		model.add( modelFactory.createCloseElementTag( tagName ) );
	}

	private boolean writePendingTag() {
		if ( pendingTag != null ) {
			model.add(
					modelFactory.createOpenElementTag(
							pendingTag, buildAttributeValues(), AttributeValueQuotes.DOUBLE, false
					)
			);
			pendingTag = null;
			removeAttributes();

			return true;
		}

		return false;
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

	private void verifyPendingTag() {
		if ( pendingTag == null ) {
			throw new IllegalStateException( "No currently open element." );
		}
	}
}
