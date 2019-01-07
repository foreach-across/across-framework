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
package com.foreach.across.modules.web.jsp;

import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.modules.web.AcrossWebModule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.servlet.support.RequestContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.AbstractTemplateView;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebExpressionContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring4.expression.ThymeleafEvaluationContext;
import org.thymeleaf.spring4.naming.SpringContextVariableNames;
import org.thymeleaf.standard.expression.FragmentExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressionExecutionContext;
import org.thymeleaf.standard.expression.StandardExpressions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>Simple tag that allows rendering a Thymeleaf template from a JSP.
 * Requires Thymeleaf support to be enabled and servlet-api to be present.</p>
 * <p>Based on source code from {@link org.thymeleaf.spring4.view.ThymeleafView}</p>.
 *
 * @author Arne Vandamme
 * @see org.thymeleaf.spring4.view.ThymeleafView
 */
@SuppressWarnings("all")
public class ThymeleafTag extends BodyTagSupport
{
	private String template;

	public void setTemplate( String template ) {
		this.template = template;
	}

	@Override
	public int doAfterBody() throws JspException {
		return TagSupport.SKIP_BODY;
	}

	@Override
	public int doEndTag() throws JspException {
		try {
			renderFragment( null,
			                (HttpServletRequest) pageContext.getRequest(),
			                (HttpServletResponse) pageContext.getResponse(),
			                pageContext.getOut() );
		}
		catch ( Exception ioe ) {
			throw new JspException( ioe );
		}

		return TagSupport.EVAL_PAGE;
	}

	protected void renderFragment( final Map<String, ?> model,
	                               final HttpServletRequest request,
	                               final HttpServletResponse response,
	                               final Writer writer )
			throws Exception {
		final String viewTemplateName = template;
		final ApplicationContext applicationContext = RequestContextUtils.findWebApplicationContext( request );
		final TemplateEngine viewTemplateEngine = AcrossContextUtils
				.getBeanRegistry( applicationContext.getBean( AcrossContextInfo.class ) )
				.getBeanOfTypeFromModule( AcrossWebModule.NAME, TemplateEngine.class );

		if ( viewTemplateName == null ) {
			throw new IllegalArgumentException( "Property 'templateName' is required" );
		}

		//WebContext context = buildWebContext( model, request, response );
		final IEngineConfiguration configuration = viewTemplateEngine.getConfiguration();
		final WebExpressionContext context = getWebExpressionContext( request, response, configuration );

		final String templateName;
		final Set<String> markupSelectors;
		if ( !viewTemplateName.contains( "::" ) ) {
			// No fragment specified at the template name

			templateName = viewTemplateName;
			markupSelectors = null;
		}
		else {
			// Template name contains a fragment name, so we should parse it as such

			final IStandardExpressionParser parser = StandardExpressions.getExpressionParser( configuration );

			final FragmentExpression fragmentExpression;
			try {
				// By parsing it as a standard expression, we might profit from the expression cache
				fragmentExpression = (FragmentExpression) parser.parseExpression( context,
				                                                                  "~{" + viewTemplateName + "}" );
			}
			catch ( final TemplateProcessingException e ) {
				throw new IllegalArgumentException( "Invalid template name specification: '" + viewTemplateName + "'" );
			}

			final FragmentExpression.ExecutedFragmentExpression fragment =
					FragmentExpression.createExecutedFragmentExpression( context, fragmentExpression,
					                                                     StandardExpressionExecutionContext.NORMAL );

			templateName = FragmentExpression.resolveTemplateName( fragment );
			markupSelectors = FragmentExpression.resolveFragments( fragment );
			final Map<String, Object> nameFragmentParameters = fragment.getFragmentParameters();

			if ( nameFragmentParameters != null ) {

				if ( fragment.hasSyntheticParameters() ) {
					// We cannot allow synthetic parameters because there is no way to specify them at the template
					// engine execution!
					throw new IllegalArgumentException(
							"Parameters in a view specification must be named (non-synthetic): '" + viewTemplateName + "'" );
				}

				context.setVariables( nameFragmentParameters );

			}
		}

		viewTemplateEngine.process( templateName, markupSelectors, context, writer );
	}

	protected WebExpressionContext getWebExpressionContext( HttpServletRequest request,
	                                                        HttpServletResponse response,
	                                                        IEngineConfiguration configuration ) {
		WebExpressionContext context = (WebExpressionContext) request.getAttribute(
				WebExpressionContext.class.getName() );
		if ( context == null ) {
			ApplicationContext applicationContext = RequestContextUtils.findWebApplicationContext( request );

			final RequestContext requestContext =
					new RequestContext( request, response, pageContext.getServletContext(), null );

			// For compatibility with ThymeleafView
			request.setAttribute( SpringContextVariableNames.SPRING_REQUEST_CONTEXT, requestContext );
			// For compatibility with AbstractTemplateView
			request.setAttribute( AbstractTemplateView.SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE, requestContext );

			// Expose Thymeleaf's own evaluation context as a model variable
			//
			// Note Spring's EvaluationContexts are NOT THREAD-SAFE (in exchange for SpelExpressions being thread-safe).
			// That's why we need to create a new EvaluationContext for each request / template execution, even if it is
			// quite expensive to create because of requiring the initialization of several ConcurrentHashMaps.
			final ConversionService conversionService =
					(ConversionService) request.getAttribute( ConversionService.class.getName() ); // might be null!
			final ThymeleafEvaluationContext evaluationContext =
					new ThymeleafEvaluationContext( applicationContext, conversionService );
			request.setAttribute( ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
			                      evaluationContext );

			context = new WebExpressionContext( configuration, request, response, pageContext.getServletContext(),
			                                    LocaleContextHolder
					                                    .getLocale(), new HashMap<String, Object>( 30 ) );
			request.setAttribute( WebExpressionContext.class.getName(), context );
		}
		return context;
	}
}
