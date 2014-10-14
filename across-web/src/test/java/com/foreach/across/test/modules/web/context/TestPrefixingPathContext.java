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

package com.foreach.across.test.modules.web.context;

import com.foreach.across.modules.web.context.PrefixingPathContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestPrefixingPathContext
{
	@Test
	public void cleanedPathPrefix() {
		assertEquals( "/boe", new PrefixingPathContext( "/boe" ).getPathPrefix() );
		assertEquals( "/boe", new PrefixingPathContext( "/boe/" ).getPathPrefix() );
		assertEquals( "/sub/section/page", new PrefixingPathContext( "/sub/section/page" ).getPathPrefix() );
		assertEquals( "/sub/section/page", new PrefixingPathContext( "/sub/section/page/" ).getPathPrefix() );
	}

	@Test
	public void rootProperty() {
		assertEquals( "/boe/", new PrefixingPathContext( "/boe" ).getRoot() );
		assertEquals( "/sub/section/page/", new PrefixingPathContext( "/sub/section/page" ).getRoot() );
	}

	@Test
	public void pathMethod() {
		PrefixingPathContext ctx = new PrefixingPathContext( "/boe" );

		assertEquals( "/boe/test/path", ctx.path( "test/path" ) );
		assertEquals( "/boe/test/path", ctx.path( "/test/path" ) );
		assertEquals( "http://www.google.be", ctx.path( "http://www.google.be" ) );
		assertEquals( "~/test/path", ctx.path( "~/test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.path( "redirect:test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.path( "redirect:/test/path" ) );
		assertEquals( "redirect:http://www.google.be", ctx.path( "redirect:http://www.google.be" ) );
		assertEquals( "redirect:~/test/path", ctx.path( "redirect:~/test/path" ) );
		assertEquals( "forward:/boe/test/path", ctx.path( "forward:test/path" ) );
		assertEquals( "forward:/boe/test/path", ctx.path( "forward:/test/path" ) );
		assertEquals( "forward:http://www.google.be", ctx.path( "forward:http://www.google.be" ) );
		assertEquals( "forward:~/test/path", ctx.path( "forward:~/test/path" ) );
	}

	@Test
	public void redirectMethod() {
		PrefixingPathContext ctx = new PrefixingPathContext( "/boe" );

		assertEquals( "redirect:/boe/test/path", ctx.redirect( "test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.redirect( "/test/path" ) );
		assertEquals( "redirect:http://www.google.be", ctx.redirect( "http://www.google.be" ) );
		assertEquals( "redirect:~/test/path", ctx.redirect( "~/test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.redirect( "redirect:test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.redirect( "redirect:/test/path" ) );
		assertEquals( "redirect:http://www.google.be", ctx.redirect( "redirect:http://www.google.be" ) );
		assertEquals( "redirect:~/test/path", ctx.redirect( "redirect:~/test/path" ) );

		// Instead of modifying the forward, assume it's what the user want
		assertEquals( "redirect:forward:/boe/test/path", ctx.redirect( "forward:test/path" ) );
		assertEquals( "redirect:forward:/boe/test/path", ctx.redirect( "forward:/test/path" ) );
		assertEquals( "redirect:forward:http://www.google.be", ctx.redirect( "forward:http://www.google.be" ) );
		assertEquals( "redirect:forward:~/test/path", ctx.redirect( "forward:~/test/path" ) );
	}
}
