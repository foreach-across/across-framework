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
package utils;

import lombok.RequiredArgsConstructor;

import java.io.PrintStream;
import java.util.stream.Stream;

/**
 * Helper for generating HTML elements.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@RequiredArgsConstructor
class GenerateHtmlViewElements
{
	private static final String[] VOID_NODES = new String[] {
			"area",
			"base",
			"br",
			"col",
			"embed",
			"hr",
			"img",
			"input",
			"keygen",
			"link",
			"meta",
			"param",
			"source",
			"track",
			"wbr"
	};

	private static final String[] NODES = new String[] {
			"a",
			"abbr",
			"address",
			"article",
			"aside",
			"audio",
			"b",
			"bdi",
			"bdo",
			"blockquote",
			"body",
			"button",
			"canvas",
			"caption",
			"cite",
			"code",
			"colgroup",
			"datalist",
			"dd",
			"del",
			"details",
			"dfn",
			"dialog",
			"div",
			"dl",
			"dt",
			"em",
			"fieldset",
			"figcaption",
			"figure",
			"footer",
			"form",
			"h1",
			"h2",
			"h3",
			"h4",
			"h5",
			"h6",
			"head",
			"header",
			"html",
			"i",
			"iframe",
			"ins",
			"kbd",
			"label",
			"legend",
			"li",
			"main",
			"map",
			"mark",
			"menu",
			"menuitem",
			"meter",
			"nav",
			"noscript",
			"object",
			"ol",
			"optgroup",
			"option",
			"output",
			"p",
			"pre",
			"progress",
			"q",
			"rp",
			"rt",
			"ruby",
			"s",
			"samp",
			"script",
			"section",
			"select",
			"small",
			"span",
			"strong",
			"style",
			"sub",
			"summary",
			"sup",
			"table",
			"tbody",
			"td",
			"textarea",
			"tfoot",
			"th",
			"thead",
			"time",
			"title",
			"tr",
			"u",
			"ul",
			"var",
			"video"
	};

	private final PrintStream out;

	public GenerateHtmlViewElements writeViewElements() {
		Stream.of( VOID_NODES ).forEach( this::writeVoidNode );
		Stream.of( NODES ).forEach( this::writeNode );
		return this;
	}

	private void writeVoidNode( String tag ) {
		out.print( "static VoidNodeViewElement " + tag + "( ViewElement.WitherSetter... setters ) {\n" +
				           "\t\treturn " + tag + "().set( setters );\n" +
				           "\t}\n\n" +
				           "static VoidNodeViewElement " + tag + "() {\n" +
				           "\t\treturn new VoidNodeViewElement( \"" + tag + "\" );\n" +
				           "\t}\n" );
	}

	private void writeNode( String tag ) {
		out.print( "static NodeViewElement " + tag + "( ViewElement.WitherSetter... setters ) {\n" +
				           "\t\treturn " + tag + "().set( setters );\n" +
				           "\t}\n\n" +
				           "static NodeViewElement " + tag + "() {\n" +
				           "\t\treturn new NodeViewElement( \"" + tag + "\" );\n" +
				           "\t}\n" );
	}

	public static void main( String[] args ) {
		new GenerateHtmlViewElements( System.out )
				.writeViewElements();

	}

}
