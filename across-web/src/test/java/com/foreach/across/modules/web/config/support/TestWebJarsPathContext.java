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

package com.foreach.across.modules.web.config.support;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestWebJarsPathContext
{
	@Test
	public void segmentsIgnoredIfRelative() {
		WebJarsPathContext WebJarsPathContext = new WebJarsPathContext( "/webjars" );
		assertEquals( "/webjars/hello.js", WebJarsPathContext.path( "/hello.js" ) );
		assertEquals( "/webjars/hello.js", WebJarsPathContext.path( "org.webjars/hello.js" ) );
		assertEquals( "/webjars/hello.js", WebJarsPathContext.path( "org.webjars.npm/hello.js" ) );
		assertEquals( "/webjars/hello.js", WebJarsPathContext.path( "bower/hello.js" ) );
	}

	@Test
	public void segmentAddedIfAbsolute() {
		WebJarsPathContext WebJarsPathContext = new WebJarsPathContext( "https://cdn.jsdelivr.net/webjars" );
		assertEquals( "https://cdn.jsdelivr.net/webjars/org.webjars/hello.js", WebJarsPathContext.path( "/hello.js" ) );
		assertEquals( "https://cdn.jsdelivr.net/webjars/org.webjars/hello.js", WebJarsPathContext.path( "org.webjars/hello.js" ) );
		assertEquals( "https://cdn.jsdelivr.net/webjars/org.webjars.npm/hello.js", WebJarsPathContext.path( "org.webjars.npm/hello.js" ) );
		assertEquals( "https://cdn.jsdelivr.net/webjars/org.webjars.bower/hello.js", WebJarsPathContext.path( "bower/hello.js" ) );
	}

	@Test
	public void hashSignOnlyIsNeverPrefixed() {
		assertEquals( "#", new WebJarsPathContext( StringUtils.EMPTY ).path( "#" ) );
		assertEquals( "#hello", new WebJarsPathContext( "boe" ).path( "#hello" ) );
	}

	@Test
	public void cleanedPathPrefix() {
		assertEquals( "/boe", new WebJarsPathContext( "/boe" ).getPathPrefix() );
		assertEquals( "/boe", new WebJarsPathContext( "/boe/" ).getPathPrefix() );
		assertEquals( "/sub/section/page", new WebJarsPathContext( "/sub/section/page" ).getPathPrefix() );
		assertEquals( "/sub/section/page", new WebJarsPathContext( "/sub/section/page/" ).getPathPrefix() );
	}

	@Test
	public void rootProperty() {
		assertEquals( "/boe/", new WebJarsPathContext( "/boe" ).getRoot() );
		assertEquals( "/sub/section/page/", new WebJarsPathContext( "/sub/section/page" ).getRoot() );
	}

	@Test
	public void pathMethod() {
		WebJarsPathContext ctx = new WebJarsPathContext( "/boe" );

		assertEquals( "/boe/path", ctx.path( "test/path" ) );
		assertEquals( "/boe/test/path?test=key#check", ctx.path( "/test/path?test=key#check" ) );
		assertEquals( "http://www.google.be", ctx.path( "http://www.google.be" ) );
		assertEquals( "~/test/path", ctx.path( "~/test/path" ) );
		assertEquals( "redirect:/boe/path", ctx.path( "redirect:test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.path( "redirect:/test/path" ) );
		assertEquals( "redirect:http://www.google.be", ctx.path( "redirect:http://www.google.be" ) );
		assertEquals( "//www.google.be", ctx.path( "//www.google.be" ) );
		assertEquals( "redirect:~/test/path", ctx.path( "redirect:~/test/path" ) );
		assertEquals( "forward:/boe/path", ctx.path( "forward:test/path" ) );
		assertEquals( "forward:/boe/test/path", ctx.path( "forward:/test/path" ) );
		assertEquals( "forward:http://www.google.be", ctx.path( "forward:http://www.google.be" ) );
		assertEquals( "forward:~/test/path", ctx.path( "forward:~/test/path" ) );
	}

	@Test
	public void suppressPrefixing() {
		WebJarsPathContext ctx = new WebJarsPathContext( "/boe" );

		assertEquals( "test/path", ctx.path( "!test/path" ) );
		assertEquals( "/test/path", ctx.path( "!/test/path" ) );
		assertEquals( "~/test/path", ctx.path( "!~/test/path" ) );
		assertEquals( "redirect:test/path", ctx.path( "redirect:!test/path" ) );
		assertEquals( "redirect:/test/path", ctx.path( "redirect:!/test/path" ) );
		assertEquals( "redirect:http://www.google.be", ctx.path( "redirect:!http://www.google.be" ) );
		assertEquals( "redirect:~/test/path", ctx.path( "redirect:!~/test/path" ) );
		assertEquals( "forward:test/path", ctx.path( "forward:!test/path" ) );
		assertEquals( "forward:/test/path", ctx.path( "forward:!/test/path" ) );
		assertEquals( "forward:http://www.google.be", ctx.path( "forward:!http://www.google.be" ) );
		assertEquals( "forward:~/test/path", ctx.path( "forward:!~/test/path" ) );
		assertEquals( "redirect:/test/path", ctx.redirect( "!/test/path" ) );
	}

	@Test
	public void redirectMethod() {
		WebJarsPathContext ctx = new WebJarsPathContext( "/boe" );
		assertEquals( "redirect:/boe/path", ctx.redirect( "test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.redirect( "/test/path" ) );
		assertEquals( "redirect:http://www.google.be", ctx.redirect( "http://www.google.be" ) );
		assertEquals( "redirect:~/test/path", ctx.redirect( "~/test/path" ) );
		assertEquals( "redirect:/boe/path", ctx.redirect( "redirect:test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.redirect( "redirect:/test/path" ) );
		assertEquals( "redirect:http://www.google.be", ctx.redirect( "redirect:http://www.google.be" ) );
		assertEquals( "redirect:~/test/path", ctx.redirect( "redirect:~/test/path" ) );

		// Instead of modifying the forward, assume it's what the user want
		assertEquals( "redirect:forward:/boe/path", ctx.redirect( "forward:test/path" ) );
		assertEquals( "redirect:forward:/boe/test/path", ctx.redirect( "forward:/test/path" ) );
		assertEquals( "redirect:forward:http://www.google.be", ctx.redirect( "forward:http://www.google.be" ) );
		assertEquals( "redirect:forward:~/test/path", ctx.redirect( "forward:~/test/path" ) );
	}

	@Test
	public void emptyPrefix() {
		WebJarsPathContext ctx = new WebJarsPathContext( StringUtils.EMPTY );

		assertEquals( "/path", ctx.path( "test/path" ) );
		assertEquals( "/test/path", ctx.path( "/test/path" ) );
		assertEquals( "~/test/path", ctx.path( "~/test/path" ) );
		assertEquals( "redirect:/path", ctx.path( "redirect:test/path" ) );
		assertEquals( "redirect:/test/path", ctx.path( "redirect:/test/path" ) );
		assertEquals( "redirect:http://www.google.be", ctx.path( "redirect:http://www.google.be" ) );
		assertEquals( "redirect:~/test/path", ctx.path( "redirect:~/test/path" ) );
		assertEquals( "forward:/path", ctx.path( "forward:test/path" ) );
		assertEquals( "forward:/test/path", ctx.path( "forward:/test/path" ) );
		assertEquals( "forward:http://www.google.be", ctx.path( "forward:http://www.google.be" ) );
		assertEquals( "forward:~/test/path", ctx.path( "forward:~/test/path" ) );
		assertEquals( "redirect:/test/path", ctx.redirect( "/test/path" ) );
	}
}
