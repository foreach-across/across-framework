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
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

import java.util.HashSet;
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
public class AcrossWebDialect extends AbstractProcessorDialect implements IExpressionObjectDialect
{
	public static final String PREFIX = "across";

	public static final String UTILITY_WEBAPP = "webapp";
	public static final String HTML_ID_STORE = "htmlIdStore";

	public AcrossWebDialect() {
		super( "AcrossWeb", PREFIX, StandardDialect.PROCESSOR_PRECEDENCE );
	}

	@Override
	public Set<IProcessor> getProcessors( final String dialectPrefix ) {
		Set<IProcessor> processors = new HashSet<>();
		processors.add( new ViewElementElementProcessor() );
		return processors;
	}

	@Override
	public IExpressionObjectFactory getExpressionObjectFactory() {
		return new AcrossExpressionObjectFactory();
	}

	public static class AcrossExpressionObjectFactory implements IExpressionObjectFactory
	{

		public HtmlIdStore htmlIdStore = new HtmlIdStore( this );
		private IExpressionObjectFactory delegate = null;

		@Override
		public Set<String> getAllExpressionObjectNames() {
			HashSet names = new HashSet();
			names.add( UTILITY_WEBAPP );
			names.add( HTML_ID_STORE );
			return names;
		}

		@Override
		public Object buildObject( IExpressionContext context, String expressionObjectName ) {
			if ( delegate != null ) {
				return delegate.buildObject( context, expressionObjectName );
			}
			else {
				if ( HTML_ID_STORE.equals( expressionObjectName ) ) {
					return htmlIdStore;
				}
				else if ( UTILITY_WEBAPP.equals( expressionObjectName ) ) {
					return context.getVariable( WebResourceUtils.PATH_RESOLVER_ATTRIBUTE_KEY );
				}
				return null;
			}
		}

		public void setTemporaryDelegate( IExpressionObjectFactory delegate ) {
			this.delegate = delegate;
		}

		@Override
		public boolean isCacheable( String expressionObjectName ) {
			return false;
		}
	}
}
