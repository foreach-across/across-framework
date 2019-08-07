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

import com.foreach.across.modules.web.template.WebTemplateInterceptor;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AbstractTemplateHandler;
import org.thymeleaf.engine.ITemplateHandler;
import org.thymeleaf.model.IProcessingInstruction;
import org.thymeleaf.postprocessor.IPostProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Optional;

/**
 * If only a specific {@link com.foreach.across.modules.web.ui.ViewElement} should be rendered, this
 * processor will submit all output unless it is between specific processing instructions.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.web.template.WebTemplateInterceptor
 * @see ThymeleafModelBuilder
 * @since 2.0.0
 */
final class PartialViewElementTemplateProcessor implements IPostProcessor
{
	@Override
	public TemplateMode getTemplateMode() {
		return TemplateMode.HTML;
	}

	@Override
	public int getPrecedence() {
		return Integer.MAX_VALUE;
	}

	@Override
	public Class<? extends ITemplateHandler> getHandlerClass() {
		return TemplateHandler.class;
	}

	public static class TemplateHandler extends AbstractTemplateHandler
	{
		private ITemplateHandler outputHandler;
		private boolean partial = false;

		@Override
		public void setNext( ITemplateHandler next ) {
			this.outputHandler = next;
			super.setNext( partial ? null : next );
		}

		@Override
		public void setContext( ITemplateContext context ) {
			super.setContext( context );

			Optional.ofNullable( context.getVariable( WebTemplateInterceptor.RENDER_VIEW_ELEMENT ) )
			        .ifPresent( elementName -> {
				                    partial = true;
				                    super.setNext( null );
			                    }
			        );
		}

		@Override
		public void handleProcessingInstruction( IProcessingInstruction processingInstruction ) {
			if ( WebTemplateInterceptor.RENDER_VIEW_ELEMENT.equals( processingInstruction.getTarget() ) ) {
				super.setNext( "start".equals( processingInstruction.getContent() ) ? outputHandler : null );
			}
		}
	}
}
