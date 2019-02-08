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
package com.foreach.across.utils;

import org.jsoup.Jsoup;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.util.AssertionErrors.assertEquals;

public class JsoupResultMatcher
{

	public JsoupElementByIdMatcher elementById( String elementId ) {
		return new JsoupElementByIdMatcher( elementId );
	}

	public class JsoupElementByIdMatcher
	{
		private final String elementId;

		public JsoupElementByIdMatcher( String elementId ) {
			this.elementId = elementId;
		}

		public <T> ResultMatcher valueIgnoringLineEndings( String expected ) {
			return new ResultMatcher()
			{
				@Override
				public void match( MvcResult result ) throws Exception {
					assertEquals(
							"Response content",
							removeLineEndings( expected ),
							removeLineEndings(
									Jsoup.parse( result.getResponse().getContentAsString() ).getElementById( elementId ).html()
							)
					);
				}
			};
		}

		private String removeLineEndings( String text ) {
			return text.replace( "\n", "" ).replace( "\r", "" );
		}
	}
}
