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
package test;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.jsp.ThymeleafTag;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.jsp.JspException;
import java.io.UnsupportedEncodingException;

/**
 * @author Marc Vanbrabant
 * @since 1.1.3
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@WebAppConfiguration
@ContextConfiguration(classes = TestThymeleafTag.Config.class)
public class TestThymeleafTag
{
	@Autowired
	private MockServletContext servletContext;
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void outputIsThymeleaf() throws JspException, UnsupportedEncodingException {
		ThymeleafTag tag = new ThymeleafTag();
		tag.setTemplate( "th/testResources/jspThymeleaf" );
		MockPageContext pageContext = new MockPageContext( servletContext );
		MockHttpServletRequest request = (MockHttpServletRequest) pageContext.getRequest();
		request.setAttribute( "name", "Joe Doe" );
		request.setAttribute( DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext );
		tag.setPageContext( pageContext );
		tag.doEndTag();
		MockHttpServletResponse response = (MockHttpServletResponse) pageContext.getResponse();
		String content = response.getContentAsString();
		Assertions.assertTrue( StringUtils.contains( StringUtils.deleteWhitespace( content ),
		                                             StringUtils.deleteWhitespace( "<p>Hello, Joe Doe!</p>" ) ) );
		Assertions.assertTrue( StringUtils.contains( content, "mvcConversionService found" ) );
		Assertions.assertTrue( StringUtils.contains( content, "environment found" ) );
	}

	@EnableAcrossContext
	@Configuration
	protected static class Config implements WebMvcConfigurer, AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new AcrossWebModule() );
		}
	}
}
