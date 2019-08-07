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

import liquibase.util.StringUtils;
import org.springframework.util.Assert;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Supports rendering the collection of web resources present in a {@link com.foreach.across.modules.web.resource.WebResourceRegistry} bucket.
 *
 * @author Arne Vandamme
 * @since 3.2.0
 */
class WebResourcesElementProcessor extends AbstractElementTagProcessor
{
	public static final String ELEMENT_NAME = "web-resources";

	private static final String ATTRIBUTE_BUCKET_NAME = "bucket";

	public WebResourcesElementProcessor() {
		super(
				TemplateMode.HTML, // This processor will apply only to HTML mode
				AcrossWebDialect.PREFIX,     // Prefix to be applied to name for matching
				ELEMENT_NAME,          // Tag name: match specifically this tag
				true,              // Apply dialect prefix to tag name
				null,              // No attribute name: will match by tag name
				false,             // No prefix to be applied to attribute name
				9999 );       // Precedence (inside dialect's own precedence)
	}

	@Override
	protected void doProcess( ITemplateContext context, IProcessableElementTag tag, IElementTagStructureHandler structureHandler ) {
		String bucketName = null;

		String expr = tag.getAttributeValue( ATTRIBUTE_BUCKET_NAME );
		if ( expr != null ) {
			IStandardExpressionParser parser = StandardExpressions.getExpressionParser( context.getConfiguration() );
			IStandardExpression expression = parser.parseExpression( context, expr );

			bucketName = (String) expression.execute( context );
		}

		Assert.isTrue( StringUtils.isNotEmpty( bucketName ), "across:web-resources requires a valid bucket name" );

		IModelFactory modelFactory = context.getModelFactory();
		IModel model = modelFactory.createModel();
		model.add( modelFactory.createStandaloneElementTag(
				"across:view", "element", "${webResourceRegistry.getResourcesForBucket('" + bucketName + "')}"
		) );

		structureHandler.replaceWith( model, true );
	}
}
