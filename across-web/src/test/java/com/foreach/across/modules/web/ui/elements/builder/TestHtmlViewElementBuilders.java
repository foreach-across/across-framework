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
package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.web.ui.elements.VoidNodeViewElement;
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
 * @since 4.0.0
 */
@DisplayName("Tests html builder methods")
class TestHtmlViewElementBuilders
{
	@Test
	void text() {
		TextViewElement text = html.builders.text( "xyz" ).build();
		assertThat( text ).isNotNull();
		assertThat( text.isEscapeXml() ).isTrue();
		assertThat( text.getText() ).isEqualTo( "xyz" );
	}

	@Test
	void unescapedText() {
		TextViewElement text = html.builders.unescapedText( "xyz" ).build();
		assertThat( text ).isNotNull();
		assertThat( text.isEscapeXml() ).isFalse();
		assertThat( text.getText() ).isEqualTo( "xyz" );
	}

	@Test
	void container() {
		ContainerViewElement container = html.builders.container( html.builders.div() ).build();
		assertThat( container ).isNotNull();
		assertThat( container.getChildren() ).hasSize( 1 );
		assertThat( container.getChildren().get( 0 ) ).isInstanceOf( NodeViewElement.class );

		container = html.builders.container( customTemplate( "test" ), children( html.div() ) ).build();
		assertThat( container ).isNotNull();
		assertThat( container.getCustomTemplate() ).isEqualTo( "test" );
		assertThat( container.getChildren() ).hasSize( 1 );
		assertThat( container.getChildren().get( 0 ) ).isInstanceOf( NodeViewElement.class );
	}

	private void assertNode( Function<ViewElement.WitherSetter[], NodeViewElementBuilder> factory, String expectedTagName ) {
		NodeViewElement node = factory.apply( new ViewElement.WitherSetter[] { attribute( "x", 1 ) } ).build();
		assertThat( node ).isNotNull();
		assertThat( node.getTagName() ).isEqualTo( expectedTagName );
		assertThat( node.getAttributes() ).containsEntry( "x", 1 ).hasSize( 1 );
	}

	private void assertVoidNode( Function<ViewElement.WitherSetter[], VoidNodeViewElementBuilder> factory, String expectedTagName ) {
		VoidNodeViewElement node = factory.apply( new ViewElement.WitherSetter[] { attribute( "x", 1 ) } ).build();
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
			assertNode( html.builders::a, "a" );
			assertNode( html.builders::abbr, "abbr" );
			assertNode( html.builders::address, "address" );
			assertNode( html.builders::article, "article" );
			assertNode( html.builders::aside, "aside" );
			assertNode( html.builders::audio, "audio" );
			assertNode( html.builders::b, "b" );
			assertNode( html.builders::bdi, "bdi" );
			assertNode( html.builders::bdo, "bdo" );
			assertNode( html.builders::blockquote, "blockquote" );
			assertNode( html.builders::body, "body" );
			assertNode( html.builders::button, "button" );
			assertNode( html.builders::canvas, "canvas" );
			assertNode( html.builders::caption, "caption" );
			assertNode( html.builders::cite, "cite" );
			assertNode( html.builders::code, "code" );
			assertNode( html.builders::colgroup, "colgroup" );
			assertNode( html.builders::datalist, "datalist" );
			assertNode( html.builders::dd, "dd" );
			assertNode( html.builders::del, "del" );
			assertNode( html.builders::details, "details" );
			assertNode( html.builders::dfn, "dfn" );
			assertNode( html.builders::dialog, "dialog" );
			assertNode( html.builders::div, "div" );
			assertNode( html.builders::dl, "dl" );
			assertNode( html.builders::dt, "dt" );
			assertNode( html.builders::em, "em" );
			assertNode( html.builders::fieldset, "fieldset" );
			assertNode( html.builders::figcaption, "figcaption" );
			assertNode( html.builders::figure, "figure" );
			assertNode( html.builders::footer, "footer" );
			assertNode( html.builders::form, "form" );
			assertNode( html.builders::h1, "h1" );
			assertNode( html.builders::h2, "h2" );
			assertNode( html.builders::h3, "h3" );
			assertNode( html.builders::h4, "h4" );
			assertNode( html.builders::h5, "h5" );
			assertNode( html.builders::h6, "h6" );
			assertNode( html.builders::head, "head" );
			assertNode( html.builders::header, "header" );
			assertNode( html.builders::html, "html" );
			assertNode( html.builders::i, "i" );
			assertNode( html.builders::iframe, "iframe" );
			assertNode( html.builders::ins, "ins" );
			assertNode( html.builders::kbd, "kbd" );
			assertNode( html.builders::label, "label" );
			assertNode( html.builders::legend, "legend" );
			assertNode( html.builders::li, "li" );
			assertNode( html.builders::main, "main" );
			assertNode( html.builders::map, "map" );
			assertNode( html.builders::mark, "mark" );
			assertNode( html.builders::menu, "menu" );
			assertNode( html.builders::menuitem, "menuitem" );
			assertNode( html.builders::meter, "meter" );
			assertNode( html.builders::nav, "nav" );
			assertNode( html.builders::noscript, "noscript" );
			assertNode( html.builders::object, "object" );
			assertNode( html.builders::ol, "ol" );
			assertNode( html.builders::optgroup, "optgroup" );
			assertNode( html.builders::option, "option" );
			assertNode( html.builders::output, "output" );
			assertNode( html.builders::p, "p" );
			assertNode( html.builders::pre, "pre" );
			assertNode( html.builders::progress, "progress" );
			assertNode( html.builders::q, "q" );
			assertNode( html.builders::rp, "rp" );
			assertNode( html.builders::rt, "rt" );
			assertNode( html.builders::ruby, "ruby" );
			assertNode( html.builders::s, "s" );
			assertNode( html.builders::samp, "samp" );
			assertNode( html.builders::script, "script" );
			assertNode( html.builders::section, "section" );
			assertNode( html.builders::select, "select" );
			assertNode( html.builders::small, "small" );
			assertNode( html.builders::span, "span" );
			assertNode( html.builders::strong, "strong" );
			assertNode( html.builders::style, "style" );
			assertNode( html.builders::sub, "sub" );
			assertNode( html.builders::summary, "summary" );
			assertNode( html.builders::sup, "sup" );
			assertNode( html.builders::table, "table" );
			assertNode( html.builders::tbody, "tbody" );
			assertNode( html.builders::td, "td" );
			assertNode( html.builders::textarea, "textarea" );
			assertNode( html.builders::tfoot, "tfoot" );
			assertNode( html.builders::th, "th" );
			assertNode( html.builders::thead, "thead" );
			assertNode( html.builders::time, "time" );
			assertNode( html.builders::title, "title" );
			assertNode( html.builders::tr, "tr" );
			assertNode( html.builders::u, "u" );
			assertNode( html.builders::ul, "ul" );
			assertNode( html.builders::var, "var" );
			assertNode( html.builders::video, "video" );
		}

		@Test
		void voidNodes() {
			assertVoidNode( html.builders::area, "area" );
			assertVoidNode( html.builders::base, "base" );
			assertVoidNode( html.builders::br, "br" );
			assertVoidNode( html.builders::col, "col" );
			assertVoidNode( html.builders::embed, "embed" );
			assertVoidNode( html.builders::hr, "hr" );
			assertVoidNode( html.builders::img, "img" );
			assertVoidNode( html.builders::input, "input" );
			assertVoidNode( html.builders::keygen, "keygen" );
			assertVoidNode( html.builders::link, "link" );
			assertVoidNode( html.builders::meta, "meta" );
			assertVoidNode( html.builders::param, "param" );
			assertVoidNode( html.builders::source, "source" );
			assertVoidNode( html.builders::track, "track" );
			assertVoidNode( html.builders::wbr, "wbr" );
		}
	}
}
