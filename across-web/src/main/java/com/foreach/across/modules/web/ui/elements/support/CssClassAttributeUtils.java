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
package com.foreach.across.modules.web.ui.elements.support;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author Arne Vandamme
 */
public class CssClassAttributeUtils
{
	public static void addCssClass( Map<String, Object> attributes, String... cssClass ) {
		attributes.put(
				"class",
				StringUtils.join(
						ArrayUtils.addAll( ArrayUtils.removeElements( cssClasses( attributes ), cssClass ), cssClass ),
						" "
				)
		);
	}

	public static boolean hasCssClass( Map<String, Object> attributes, String cssClass ) {
		return ArrayUtils.contains( cssClasses( attributes ), cssClass );
	}

	public static void removeCssClass( Map<String, Object> attributes, String... cssClass ) {
		attributes.put( "class", StringUtils.join( ArrayUtils.removeElements( cssClasses( attributes ), cssClass ) ) );
	}

	private static String[] cssClasses( Map<String, Object> attributes ) {
		String css = StringUtils.defaultString( (String) attributes.get( "class" ) );
		return StringUtils.split( css, " " );
	}
}
