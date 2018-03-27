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
package test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foreach.across.config.AcrossApplication;
import com.foreach.across.core.context.bootstrap.AcrossBootstrapConfigurer;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import com.foreach.across.modules.web.resource.WebResourceTranslator;
import com.foreach.across.modules.web.ui.ViewElementAttributeConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.UriComponentsContributor;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.BeanNameViewResolver;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestAcrossWebModuleBootstrap.Config.class)
public class TestAcrossWebModuleBootstrap extends AbstractWebIntegrationTest
{
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Autowired
	private ServletContext servletContext;

	@Test
	public void exposedAutoConfigurationBeans() {
		assertExposed( RestTemplateBuilder.class );
		assertExposed( HttpMessageConverters.class );
		assertExposed( ObjectMapper.class );
		assertExposed( Jackson2ObjectMapperBuilder.class );

		assertExposed( MultipartResolver.class );
		assertExposed( MultipartConfigElement.class );

		// Exposed from the post-processor
		assertExposed( UriComponentsContributor.class );

		assertExposed( HandlerMappingIntrospector.class );
	}

	@Test
	public void jacksonMessageConverterUsingTheObjectMapperShouldBeRegistered() {
		HttpMessageConverters converters = beanRegistry.getBeanOfTypeFromModule( AcrossWebModule.NAME, HttpMessageConverters.class );
		ObjectMapper objectMapper = beanRegistry.getBeanOfTypeFromModule( AcrossWebModule.NAME, ObjectMapper.class );

		Optional<MappingJackson2HttpMessageConverter> found = converters
				.getConverters()
				.stream()
				.filter( MappingJackson2HttpMessageConverter.class::isInstance )
				.map( MappingJackson2HttpMessageConverter.class::cast )
				.filter( c -> c.getObjectMapper() == objectMapper )
				.findFirst();

		assertTrue( found.isPresent() );
	}

	@Test
	public void mvcConversionServiceCreatedInAcrossWebModule() {
		FormattingConversionService acrossWebConversionService
				= applicationContext.getBean( AcrossWebModule.CONVERSION_SERVICE_BEAN, FormattingConversionService.class );
		assertSame( acrossWebConversionService, beanRegistry.getBeanFromModule( AcrossWebModule.NAME, AcrossWebModule.CONVERSION_SERVICE_BEAN ) );
		FormattingConversionService autoConfiguredConversionService
				= beanRegistry.getBeanFromModule( AcrossBootstrapConfigurer.CONTEXT_POSTPROCESSOR_MODULE, AcrossWebModule.CONVERSION_SERVICE_BEAN );
		assertSame( acrossWebConversionService, autoConfiguredConversionService );
	}

	@Test
	public void validatorShouldBeInitialized() {
		Validator validator = applicationContext.getBean( Validator.class );
		assertTrue( validator instanceof SmartValidator );

		Validator mvcValidator = applicationContext.getBean( "mvcValidator", Validator.class );
		assertSame( validator, mvcValidator );
	}

	@Test
	public void exposedDomainBeans() {
		assertExposed( ViewElementAttributeConverter.class );
		assertNotNull( beanRegistry.getBean( "viewsWebResourceTranslator", WebResourceTranslator.class ) );
	}

	@Test
	public void registeredFilters() {
		assertNotNull( servletContext.getFilterRegistration( "characterEncodingFilter" ) );
		assertNotNull( servletContext.getFilterRegistration( "multipartFilter" ) );
	}

	@Test
	public void defaultRequestMappingIsExpectedToBePrefixedVariant() {
		RequestMappingHandlerMapping handlerMapping = applicationContext.getBean( RequestMappingHandlerMapping.class );
		assertTrue( handlerMapping instanceof PrefixingRequestMappingHandlerMapping );
		assertEquals( 0, handlerMapping.getOrder() );
	}

	@Test
	public void errorPageAttributes() {
		assertExposed( ErrorAttributes.class );
		assertExposed( BeanNameViewResolver.class );
		assertExposed( ErrorViewResolver.class );
		assertExposed( ErrorController.class );

		// error page customizer should be suppressed as it is useless from within a module
		assertFalse( beanRegistry.moduleContainsLocalBean( AcrossBootstrapConfigurer.CONTEXT_POSTPROCESSOR_MODULE, "errorPageCustomizer" ) );
	}

	private void assertExposed( Class<?> type ) {
		assertNotNull( applicationContext.getBean( type ) );
	}

	@AcrossApplication(modules = AcrossWebModule.NAME, autoConfiguration = false)
	@ImportAutoConfiguration(ErrorMvcAutoConfiguration.class)
	@Configuration
	static class Config
	{
	}
}
