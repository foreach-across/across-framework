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

import com.foreach.across.modules.web.resource.WebResourceUtils;
import org.thymeleaf.Arguments;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionEnhancingDialect;
import org.thymeleaf.processor.IProcessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Custom Thymeleaf dialect supporting AcrossWebModule setups.
 * <p>
 * Adds the following utility objects:
 * <ul>
 * <li><b>#webapp</b>: {@link com.foreach.across.modules.web.context.WebAppPathResolver} for the current context</li>
 * </ul>
 * </p>
 *
 * @author Arne Vandamme
 */
public class AcrossWebDialect extends AbstractDialect implements IExpressionEnhancingDialect
{
	public static final String PREFIX = "across";

	public static final String UTILITY_WEBAPP = "webapp";
	public static final String HTML_ID_STORE = "htmlIdStore";

	@Override
	public Map<String, Object> getAdditionalExpressionObjects( IProcessingContext processingContext ) {
		Map<String, Object> objects = new HashMap<>();

		Object pathResolver = processingContext.getContext().getVariables().get(
				WebResourceUtils.PATH_RESOLVER_ATTRIBUTE_KEY );

		if ( pathResolver != null ) {
			objects.put( UTILITY_WEBAPP, pathResolver );
		}

		if ( processingContext instanceof Arguments ) {
			objects.put( HTML_ID_STORE, new HtmlIdStore( (Arguments) processingContext ) );
		}

		return objects;
	}

	@Override
	public Set<IProcessor> getProcessors() {
		Set<IProcessor> processors = new HashSet<>();
		processors.add( new ProcessableAttrProcessor() );
		processors.add( new ViewElementElementProcessor() );

		return processors;
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
}
