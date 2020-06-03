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
package com.foreach.across.modules.web.ui.elements;

import com.foreach.across.modules.web.ui.ViewElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static com.foreach.across.modules.web.ui.MutableViewElement.Functions.customTemplate;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElement.Functions.attribute;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElement.Functions.children;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.html;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@DisplayName("Test HtmlViewElements creation methods")
class TestHtmlViewElements
{
	@Test
	void text() {
		TextViewElement text = html.text( "xyz" );
		assertThat( text ).isNotNull();
		assertThat( text.isEscapeXml() ).isTrue();
		assertThat( text.getText() ).isEqualTo( "xyz" );
	}

	@Test
	void unescapedText() {
		TextViewElement text = html.unescapedText( "xyz" );
		assertThat( text ).isNotNull();
		assertThat( text.isEscapeXml() ).isFalse();
		assertThat( text.getText() ).isEqualTo( "xyz" );
	}

	@Test
	void container() {
		ContainerViewElement container = html.container( html.div() );
		assertThat( container ).isNotNull();
		assertThat( container.getChildren() ).hasSize( 1 );
		assertThat( container.getChildren().get( 0 ) ).isInstanceOf( NodeViewElement.class );

		container = html.container( customTemplate( "test" ), children( html.div() ) );
		assertThat( container ).isNotNull();
		assertThat( container.getCustomTemplate() ).isEqualTo( "test" );
		assertThat( container.getChildren() ).hasSize( 1 );
		assertThat( container.getChildren().get( 0 ) ).isInstanceOf( NodeViewElement.class );
	}

	private void assertNode( Function<ViewElement.WitherSetter[], NodeViewElement> factory, String expectedTagName ) {
		NodeViewElement node = factory.apply( new ViewElement.WitherSetter[] { attribute( "x", 1 ) } );
		assertThat( node ).isNotNull();
		assertThat( node.getTagName() ).isEqualTo( expectedTagName );
		assertThat( node.getAttributes() ).containsEntry( "x", 1 ).hasSize( 1 );
	}

	private void assertVoidNode( Function<ViewElement.WitherSetter[], VoidNodeViewElement> factory, String expectedTagName ) {
		VoidNodeViewElement node = factory.apply( new ViewElement.WitherSetter[] { attribute( "x", 1 ) } );
		assertThat( node ).isNotNull();
		assertThat( node.getTagName() ).isEqualTo( expectedTagName );
		assertThat( node.getAttributes() ).containsEntry( "x", 1 ).hasSize( 1 );
	}

	@Nested
	@DisplayName("Generated methods test")
	class Generated
	{
		@Test
		void nodes() {
			assertNode( html::a, "a" );
			assertNode( html::abbr, "abbr" );
			assertNode( html::address, "address" );
			assertNode( html::article, "article" );
			assertNode( html::aside, "aside" );
			assertNode( html::audio, "audio" );
			assertNode( html::b, "b" );
			assertNode( html::bdi, "bdi" );
			assertNode( html::bdo, "bdo" );
			assertNode( html::blockquote, "blockquote" );
			assertNode( html::body, "body" );
			assertNode( html::button, "button" );
			assertNode( html::canvas, "canvas" );
			assertNode( html::caption, "caption" );
			assertNode( html::cite, "cite" );
			assertNode( html::code, "code" );
			assertNode( html::colgroup, "colgroup" );
			assertNode( html::datalist, "datalist" );
			assertNode( html::dd, "dd" );
			assertNode( html::del, "del" );
			assertNode( html::details, "details" );
			assertNode( html::dfn, "dfn" );
			assertNode( html::dialog, "dialog" );
			assertNode( html::div, "div" );
			assertNode( html::dl, "dl" );
			assertNode( html::dt, "dt" );
			assertNode( html::em, "em" );
			assertNode( html::fieldset, "fieldset" );
			assertNode( html::figcaption, "figcaption" );
			assertNode( html::figure, "figure" );
			assertNode( html::footer, "footer" );
			assertNode( html::form, "form" );
			assertNode( html::h1, "h1" );
			assertNode( html::h2, "h2" );
			assertNode( html::h3, "h3" );
			assertNode( html::h4, "h4" );
			assertNode( html::h5, "h5" );
			assertNode( html::h6, "h6" );
			assertNode( html::head, "head" );
			assertNode( html::header, "header" );
			assertNode( html::html, "html" );
			assertNode( html::i, "i" );
			assertNode( html::iframe, "iframe" );
			assertNode( html::ins, "ins" );
			assertNode( html::kbd, "kbd" );
			assertNode( html::label, "label" );
			assertNode( html::legend, "legend" );
			assertNode( html::li, "li" );
			assertNode( html::main, "main" );
			assertNode( html::map, "map" );
			assertNode( html::mark, "mark" );
			assertNode( html::menu, "menu" );
			assertNode( html::menuitem, "menuitem" );
			assertNode( html::meter, "meter" );
			assertNode( html::nav, "nav" );
			assertNode( html::noscript, "noscript" );
			assertNode( html::object, "object" );
			assertNode( html::ol, "ol" );
			assertNode( html::optgroup, "optgroup" );
			assertNode( html::option, "option" );
			assertNode( html::output, "output" );
			assertNode( html::p, "p" );
			assertNode( html::pre, "pre" );
			assertNode( html::progress, "progress" );
			assertNode( html::q, "q" );
			assertNode( html::rp, "rp" );
			assertNode( html::rt, "rt" );
			assertNode( html::ruby, "ruby" );
			assertNode( html::s, "s" );
			assertNode( html::samp, "samp" );
			assertNode( html::script, "script" );
			assertNode( html::section, "section" );
			assertNode( html::select, "select" );
			assertNode( html::small, "small" );
			assertNode( html::span, "span" );
			assertNode( html::strong, "strong" );
			assertNode( html::style, "style" );
			assertNode( html::sub, "sub" );
			assertNode( html::summary, "summary" );
			assertNode( html::sup, "sup" );
			assertNode( html::table, "table" );
			assertNode( html::tbody, "tbody" );
			assertNode( html::td, "td" );
			assertNode( html::textarea, "textarea" );
			assertNode( html::tfoot, "tfoot" );
			assertNode( html::th, "th" );
			assertNode( html::thead, "thead" );
			assertNode( html::time, "time" );
			assertNode( html::title, "title" );
			assertNode( html::tr, "tr" );
			assertNode( html::u, "u" );
			assertNode( html::ul, "ul" );
			assertNode( html::var, "var" );
			assertNode( html::video, "video" );
		}

		@Test
		void voidNodes() {
			assertVoidNode( html::area, "area" );
			assertVoidNode( html::base, "base" );
			assertVoidNode( html::br, "br" );
			assertVoidNode( html::col, "col" );
			assertVoidNode( html::embed, "embed" );
			assertVoidNode( html::hr, "hr" );
			assertVoidNode( html::img, "img" );
			assertVoidNode( html::input, "input" );
			assertVoidNode( html::keygen, "keygen" );
			assertVoidNode( html::link, "link" );
			assertVoidNode( html::meta, "meta" );
			assertVoidNode( html::param, "param" );
			assertVoidNode( html::source, "source" );
			assertVoidNode( html::track, "track" );
			assertVoidNode( html::wbr, "wbr" );
		}
	}
}
