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
import org.thymeleaf.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.ProcessingContext;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.exceptions.ConfigurationException;
import org.thymeleaf.fragment.IFragmentSpec;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.dialect.SpringStandardDialect;
import org.thymeleaf.spring4.expression.ThymeleafEvaluationContext;
import org.thymeleaf.spring4.naming.SpringContextVariableNames;
import org.thymeleaf.standard.expression.FragmentSelectionUtils;
import org.thymeleaf.standard.fragment.StandardFragment;
import org.thymeleaf.standard.fragment.StandardFragmentProcessor;
import org.thymeleaf.standard.processor.attr.StandardFragmentAttrProcessor;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

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
		final ApplicationContext applicationContext = RequestContextUtils.getWebApplicationContext( request );
		final TemplateEngine viewTemplateEngine = AcrossContextUtils
				.getBeanRegistry( applicationContext.getBean( AcrossContextInfo.class ) )
				.getBeanOfTypeFromModule( AcrossWebModule.NAME, TemplateEngine.class );

		if ( !viewTemplateEngine.isInitialized() ) {
			viewTemplateEngine.initialize();
		}

		if ( viewTemplateName == null ) {
			throw new IllegalArgumentException( "Property 'templateName' is required" );
		}

		SpringWebContext context = buildWebContext( model, request, response );

		final String templateName;
		final IFragmentSpec nameFragmentSpec;
		if ( !viewTemplateName.contains( "::" ) ) {
			// No fragment specified at the template name

			templateName = viewTemplateName;
			nameFragmentSpec = null;

		}
		else {
			// Template name contains a fragment name, so we should parse it as such
			final Configuration configuration = viewTemplateEngine.getConfiguration();
			final ProcessingContext processingContext = new ProcessingContext( context );

			final String dialectPrefix = getStandardDialectPrefix( configuration );

			final StandardFragment fragment =
					StandardFragmentProcessor.computeStandardFragmentSpec(
							configuration, processingContext, viewTemplateName, dialectPrefix,
							StandardFragmentAttrProcessor.ATTR_NAME );

			if ( fragment == null ) {
				throw new IllegalArgumentException( "Invalid template name specification: '" + viewTemplateName + "'" );
			}

			templateName = fragment.getTemplateName();
			nameFragmentSpec = fragment.getFragmentSpec();
			final Map<String, Object> nameFragmentParameters = fragment.getParameters();

			if ( nameFragmentParameters != null ) {

				if ( FragmentSelectionUtils.parameterNamesAreSynthetic( nameFragmentParameters.keySet() ) ) {
					// We cannot allow synthetic parameters because there is no way to specify them at the template
					// engine execution!
					throw new IllegalArgumentException(
							"Parameters in a view specification must be named (non-synthetic): '" + viewTemplateName + "'" );
				}

				context.setVariables( nameFragmentParameters );
			}
		}

		IFragmentSpec templateFragmentSpec = null;

		if ( nameFragmentSpec != null ) {
			templateFragmentSpec = nameFragmentSpec;
		}

		viewTemplateEngine.process( templateName, context, templateFragmentSpec, writer );
	}

	protected SpringWebContext buildWebContext( Map<String, ?> model,
	                                            HttpServletRequest request,
	                                            HttpServletResponse response )
			throws Exception {
		SpringWebContext context = (SpringWebContext) request.getAttribute( SpringWebContext.class.getName() );

		if ( context == null ) {
			// Build a new SpringWebContext
			Locale locale = LocaleContextHolder.getLocale();

			ServletContext servletContext = pageContext.getServletContext();
			ApplicationContext applicationContext = RequestContextUtils.getWebApplicationContext( request );

			final RequestContext requestContext =
					new RequestContext( request, response, servletContext, null );

			// For compatibility with ThymeleafView
			request.setAttribute( SpringContextVariableNames.SPRING_REQUEST_CONTEXT, requestContext );
			request.setAttribute( AbstractTemplateView.SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE, requestContext );

			// Expose Thymeleaf's own evaluation context as a model variable
			final ConversionService conversionService =
					(ConversionService) request.getAttribute( ConversionService.class.getName() ); // might be null!
			final ThymeleafEvaluationContext evaluationContext =
					new ThymeleafEvaluationContext( applicationContext, conversionService );
			request.setAttribute( ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME,
			                      evaluationContext );

			context = new SpringWebContext( request, response, servletContext, locale, null,
			                                applicationContext );

			request.setAttribute( SpringWebContext.class.getName(), context );
		}

		return context;
	}

	protected static String getStandardDialectPrefix( final Configuration configuration ) {

		for ( final Map.Entry<String, IDialect> dialectByPrefix : configuration.getDialects().entrySet() ) {
			final IDialect dialect = dialectByPrefix.getValue();
			if ( SpringStandardDialect.class.isAssignableFrom( dialect.getClass() ) ) {
				return dialectByPrefix.getKey();
			}
		}

		throw new ConfigurationException(
				"StandardDialect dialect has not been found. In order to use AjaxThymeleafView, you should configure " +
						"the " + SpringStandardDialect.class.getName() + " dialect at your Template Engine" );

	}
}
