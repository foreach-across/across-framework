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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementAttributeConverter;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementModelWriterRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.WebEngineContext;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Enables generic {@link com.foreach.across.modules.web.ui.ViewElement} rendering support.
 */
class ViewElementElementProcessor extends AbstractElementTagProcessor
{
	public static final String ELEMENT_NAME = "view";

	private static final String ATTRIBUTE_ITEM = "element";

	private final ObjectMapper objectMapper = new ObjectMapper();

	public ViewElementElementProcessor() {
		super(
				TemplateMode.HTML, // This processor will apply only to HTML mode
				AcrossWebDialect.PREFIX,     // Prefix to be applied to name for matching
				ELEMENT_NAME,          // Tag name: match specifically this tag
				true,              // Apply dialect prefix to tag name
				null,              // No attribute name: will match by tag name
				false,             // No prefix to be applied to attribute name
				10000 );       // Precedence (inside dialect's own precedence)
	}

	@Override
	protected void doProcess( ITemplateContext context,
	                          IProcessableElementTag tag,
	                          IElementTagStructureHandler structureHandler ) {
		ViewElement viewElement = retrieveViewElementFromAttribute( context, tag );
		ApplicationContext appCtx = RequestContextUtils.findWebApplicationContext(
				( (WebEngineContext) context ).getRequest() );
		ViewElementModelWriterRegistry registry = appCtx.getBean( ViewElementModelWriterRegistry.class );
		ViewElementAttributeConverter attributeConverter = appCtx.getBean( ViewElementAttributeConverter.class );
		HtmlIdStore idStore = (HtmlIdStore) context.getExpressionObjects().getObject( AcrossWebDialect.HTML_ID_STORE );

		ThymeleafModelBuilder builder = new ThymeleafModelBuilder( context, registry, idStore, attributeConverter );
		builder.addViewElement( viewElement );
		structureHandler.replaceWith( builder.retrieveModel(), true );
	}

	private ViewElement retrieveViewElementFromAttribute( ITemplateContext context, IProcessableElementTag element ) {
		String expr = element.getAttributeValue( ATTRIBUTE_ITEM );
		IStandardExpressionParser parser = StandardExpressions.getExpressionParser( context.getConfiguration() );
		IStandardExpression expression = parser.parseExpression( context, expr );

		Object viewElement = expression.execute( context );

		if ( viewElement instanceof ViewElement ) {
			return (ViewElement) viewElement;
		}

		throw new IllegalArgumentException(
				ELEMENT_NAME + " element requires a " + ATTRIBUTE_ITEM + " attribute of type ViewElement"
		);
	}
}
