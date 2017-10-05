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
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestPrefixingPathContext
{
	@Test
	public void hashSignOnlyIsNeverPrefixed() {
		assertEquals( "#", new PrefixingPathContext( StringUtils.EMPTY ).path( "#" ) );
		assertEquals( "#hello", new PrefixingPathContext( "boe" ).path( "#hello" ) );
	}

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
		assertEquals( "/boe/test/path?test=key#check", ctx.path( "/test/path?test=key#check" ) );
		assertEquals( "http://www.google.be", ctx.path( "http://www.google.be" ) );
		assertEquals( "~/test/path", ctx.path( "~/test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.path( "redirect:test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.path( "redirect:/test/path" ) );
		assertEquals( "redirect:http://www.google.be", ctx.path( "redirect:http://www.google.be" ) );
		assertEquals( "//www.google.be", ctx.path( "//www.google.be" ) );
		assertEquals( "redirect:~/test/path", ctx.path( "redirect:~/test/path" ) );
		assertEquals( "forward:/boe/test/path", ctx.path( "forward:test/path" ) );
		assertEquals( "forward:/boe/test/path", ctx.path( "forward:/test/path" ) );
		assertEquals( "forward:http://www.google.be", ctx.path( "forward:http://www.google.be" ) );
		assertEquals( "forward:~/test/path", ctx.path( "forward:~/test/path" ) );
	}

	@Test
	public void suppressPrefixing() {
		PrefixingPathContext ctx = new PrefixingPathContext( "/boe" );

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

	@Test
	public void absolutePrefixers() {
		PrefixingPathContext ctx = new PrefixingPathContext( "/boe" );
		PrefixingPathContext other = new PrefixingPathContext( "/other/prefix" );

		Map<String, PrefixingPathContext> prefixers = new HashMap<>();
		prefixers.put( "current", ctx );
		prefixers.put( "other", other );

		ctx.setNamedPrefixMap( prefixers );

		assertEquals( "/boe/test/path", ctx.path( "@current:test/path" ) );
		assertEquals( "/boe/test/path", ctx.path( "@current:/test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.path( "redirect:@current:test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.path( "redirect:@current:/test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.redirect( "@current:test/path" ) );
		assertEquals( "redirect:/boe/test/path", ctx.redirect( "@current:/test/path" ) );

		assertEquals( "/other/prefix/test/path", ctx.path( "@other:test/path" ) );
		assertEquals( "/other/prefix/test/path", ctx.path( "@other:/test/path" ) );
		assertEquals( "redirect:/other/prefix/test/path", ctx.path( "redirect:@other:test/path" ) );
		assertEquals( "redirect:/other/prefix/test/path", ctx.path( "redirect:@other:/test/path" ) );
		assertEquals( "redirect:/other/prefix/test/path", ctx.redirect( "@other:test/path" ) );
		assertEquals( "redirect:/other/prefix/test/path", ctx.redirect( "@other:/test/path" ) );
	}

	@Test
	public void emptyPrefix() {
		PrefixingPathContext ctx = new PrefixingPathContext( StringUtils.EMPTY );

		assertEquals( "/test/path", ctx.path( "test/path" ) );
		assertEquals( "/test/path", ctx.path( "/test/path" ) );
		assertEquals( "~/test/path", ctx.path( "~/test/path" ) );
		assertEquals( "redirect:/test/path", ctx.path( "redirect:test/path" ) );
		assertEquals( "redirect:/test/path", ctx.path( "redirect:/test/path" ) );
		assertEquals( "redirect:http://www.google.be", ctx.path( "redirect:http://www.google.be" ) );
		assertEquals( "redirect:~/test/path", ctx.path( "redirect:~/test/path" ) );
		assertEquals( "forward:/test/path", ctx.path( "forward:test/path" ) );
		assertEquals( "forward:/test/path", ctx.path( "forward:/test/path" ) );
		assertEquals( "forward:http://www.google.be", ctx.path( "forward:http://www.google.be" ) );
		assertEquals( "forward:~/test/path", ctx.path( "forward:~/test/path" ) );
		assertEquals( "redirect:/test/path", ctx.redirect( "/test/path" ) );
	}
}
