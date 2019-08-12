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

import java.util.Arrays;

/**
 * Contains factory methods and {@link ViewElement.Wither} functions for HTML5 nodes.
 *
 * @author Arne Vandamme
 * @see HtmlViewElement.Functions
 * @since 5.0.0
 */
public interface HtmlViewElements
{
	// -- begin generated section

	static VoidNodeViewElement area( ViewElement.WitherSetter... setters ) {
		return area().set( setters );
	}

	static VoidNodeViewElement area() {
		return new VoidNodeViewElement( "area" );
	}

	static VoidNodeViewElement base( ViewElement.WitherSetter... setters ) {
		return base().set( setters );
	}

	static VoidNodeViewElement base() {
		return new VoidNodeViewElement( "base" );
	}

	static VoidNodeViewElement br( ViewElement.WitherSetter... setters ) {
		return br().set( setters );
	}

	static VoidNodeViewElement br() {
		return new VoidNodeViewElement( "br" );
	}

	static VoidNodeViewElement col( ViewElement.WitherSetter... setters ) {
		return col().set( setters );
	}

	static VoidNodeViewElement col() {
		return new VoidNodeViewElement( "col" );
	}

	static VoidNodeViewElement embed( ViewElement.WitherSetter... setters ) {
		return embed().set( setters );
	}

	static VoidNodeViewElement embed() {
		return new VoidNodeViewElement( "embed" );
	}

	static VoidNodeViewElement hr( ViewElement.WitherSetter... setters ) {
		return hr().set( setters );
	}

	static VoidNodeViewElement hr() {
		return new VoidNodeViewElement( "hr" );
	}

	static VoidNodeViewElement img( ViewElement.WitherSetter... setters ) {
		return img().set( setters );
	}

	static VoidNodeViewElement img() {
		return new VoidNodeViewElement( "img" );
	}

	static VoidNodeViewElement input( ViewElement.WitherSetter... setters ) {
		return input().set( setters );
	}

	static VoidNodeViewElement input() {
		return new VoidNodeViewElement( "input" );
	}

	static VoidNodeViewElement keygen( ViewElement.WitherSetter... setters ) {
		return keygen().set( setters );
	}

	static VoidNodeViewElement keygen() {
		return new VoidNodeViewElement( "keygen" );
	}

	static VoidNodeViewElement link( ViewElement.WitherSetter... setters ) {
		return link().set( setters );
	}

	static VoidNodeViewElement link() {
		return new VoidNodeViewElement( "link" );
	}

	static VoidNodeViewElement meta( ViewElement.WitherSetter... setters ) {
		return meta().set( setters );
	}

	static VoidNodeViewElement meta() {
		return new VoidNodeViewElement( "meta" );
	}

	static VoidNodeViewElement param( ViewElement.WitherSetter... setters ) {
		return param().set( setters );
	}

	static VoidNodeViewElement param() {
		return new VoidNodeViewElement( "param" );
	}

	static VoidNodeViewElement source( ViewElement.WitherSetter... setters ) {
		return source().set( setters );
	}

	static VoidNodeViewElement source() {
		return new VoidNodeViewElement( "source" );
	}

	static VoidNodeViewElement track( ViewElement.WitherSetter... setters ) {
		return track().set( setters );
	}

	static VoidNodeViewElement track() {
		return new VoidNodeViewElement( "track" );
	}

	static VoidNodeViewElement wbr( ViewElement.WitherSetter... setters ) {
		return wbr().set( setters );
	}

	static VoidNodeViewElement wbr() {
		return new VoidNodeViewElement( "wbr" );
	}

	static NodeViewElement a( ViewElement.WitherSetter... setters ) {
		return a().set( setters );
	}

	static NodeViewElement a() {
		return new NodeViewElement( "a" );
	}

	static NodeViewElement abbr( ViewElement.WitherSetter... setters ) {
		return abbr().set( setters );
	}

	static NodeViewElement abbr() {
		return new NodeViewElement( "abbr" );
	}

	static NodeViewElement address( ViewElement.WitherSetter... setters ) {
		return address().set( setters );
	}

	static NodeViewElement address() {
		return new NodeViewElement( "address" );
	}

	static NodeViewElement article( ViewElement.WitherSetter... setters ) {
		return article().set( setters );
	}

	static NodeViewElement article() {
		return new NodeViewElement( "article" );
	}

	static NodeViewElement aside( ViewElement.WitherSetter... setters ) {
		return aside().set( setters );
	}

	static NodeViewElement aside() {
		return new NodeViewElement( "aside" );
	}

	static NodeViewElement audio( ViewElement.WitherSetter... setters ) {
		return audio().set( setters );
	}

	static NodeViewElement audio() {
		return new NodeViewElement( "audio" );
	}

	static NodeViewElement b( ViewElement.WitherSetter... setters ) {
		return b().set( setters );
	}

	static NodeViewElement b() {
		return new NodeViewElement( "b" );
	}

	static NodeViewElement bdi( ViewElement.WitherSetter... setters ) {
		return bdi().set( setters );
	}

	static NodeViewElement bdi() {
		return new NodeViewElement( "bdi" );
	}

	static NodeViewElement bdo( ViewElement.WitherSetter... setters ) {
		return bdo().set( setters );
	}

	static NodeViewElement bdo() {
		return new NodeViewElement( "bdo" );
	}

	static NodeViewElement blockquote( ViewElement.WitherSetter... setters ) {
		return blockquote().set( setters );
	}

	static NodeViewElement blockquote() {
		return new NodeViewElement( "blockquote" );
	}

	static NodeViewElement body( ViewElement.WitherSetter... setters ) {
		return body().set( setters );
	}

	static NodeViewElement body() {
		return new NodeViewElement( "body" );
	}

	static NodeViewElement button( ViewElement.WitherSetter... setters ) {
		return button().set( setters );
	}

	static NodeViewElement button() {
		return new NodeViewElement( "button" );
	}

	static NodeViewElement canvas( ViewElement.WitherSetter... setters ) {
		return canvas().set( setters );
	}

	static NodeViewElement canvas() {
		return new NodeViewElement( "canvas" );
	}

	static NodeViewElement caption( ViewElement.WitherSetter... setters ) {
		return caption().set( setters );
	}

	static NodeViewElement caption() {
		return new NodeViewElement( "caption" );
	}

	static NodeViewElement cite( ViewElement.WitherSetter... setters ) {
		return cite().set( setters );
	}

	static NodeViewElement cite() {
		return new NodeViewElement( "cite" );
	}

	static NodeViewElement code( ViewElement.WitherSetter... setters ) {
		return code().set( setters );
	}

	static NodeViewElement code() {
		return new NodeViewElement( "code" );
	}

	static NodeViewElement colgroup( ViewElement.WitherSetter... setters ) {
		return colgroup().set( setters );
	}

	static NodeViewElement colgroup() {
		return new NodeViewElement( "colgroup" );
	}

	static NodeViewElement datalist( ViewElement.WitherSetter... setters ) {
		return datalist().set( setters );
	}

	static NodeViewElement datalist() {
		return new NodeViewElement( "datalist" );
	}

	static NodeViewElement dd( ViewElement.WitherSetter... setters ) {
		return dd().set( setters );
	}

	static NodeViewElement dd() {
		return new NodeViewElement( "dd" );
	}

	static NodeViewElement del( ViewElement.WitherSetter... setters ) {
		return del().set( setters );
	}

	static NodeViewElement del() {
		return new NodeViewElement( "del" );
	}

	static NodeViewElement details( ViewElement.WitherSetter... setters ) {
		return details().set( setters );
	}

	static NodeViewElement details() {
		return new NodeViewElement( "details" );
	}

	static NodeViewElement dfn( ViewElement.WitherSetter... setters ) {
		return dfn().set( setters );
	}

	static NodeViewElement dfn() {
		return new NodeViewElement( "dfn" );
	}

	static NodeViewElement dialog( ViewElement.WitherSetter... setters ) {
		return dialog().set( setters );
	}

	static NodeViewElement dialog() {
		return new NodeViewElement( "dialog" );
	}

	static NodeViewElement div( ViewElement.WitherSetter... setters ) {
		return div().set( setters );
	}

	static NodeViewElement div() {
		return new NodeViewElement( "div" );
	}

	static NodeViewElement dl( ViewElement.WitherSetter... setters ) {
		return dl().set( setters );
	}

	static NodeViewElement dl() {
		return new NodeViewElement( "dl" );
	}

	static NodeViewElement dt( ViewElement.WitherSetter... setters ) {
		return dt().set( setters );
	}

	static NodeViewElement dt() {
		return new NodeViewElement( "dt" );
	}

	static NodeViewElement em( ViewElement.WitherSetter... setters ) {
		return em().set( setters );
	}

	static NodeViewElement em() {
		return new NodeViewElement( "em" );
	}

	static NodeViewElement fieldset( ViewElement.WitherSetter... setters ) {
		return fieldset().set( setters );
	}

	static NodeViewElement fieldset() {
		return new NodeViewElement( "fieldset" );
	}

	static NodeViewElement figcaption( ViewElement.WitherSetter... setters ) {
		return figcaption().set( setters );
	}

	static NodeViewElement figcaption() {
		return new NodeViewElement( "figcaption" );
	}

	static NodeViewElement figure( ViewElement.WitherSetter... setters ) {
		return figure().set( setters );
	}

	static NodeViewElement figure() {
		return new NodeViewElement( "figure" );
	}

	static NodeViewElement footer( ViewElement.WitherSetter... setters ) {
		return footer().set( setters );
	}

	static NodeViewElement footer() {
		return new NodeViewElement( "footer" );
	}

	static NodeViewElement form( ViewElement.WitherSetter... setters ) {
		return form().set( setters );
	}

	static NodeViewElement form() {
		return new NodeViewElement( "form" );
	}

	static NodeViewElement h1( ViewElement.WitherSetter... setters ) {
		return h1().set( setters );
	}

	static NodeViewElement h1() {
		return new NodeViewElement( "h1" );
	}

	static NodeViewElement h2( ViewElement.WitherSetter... setters ) {
		return h2().set( setters );
	}

	static NodeViewElement h2() {
		return new NodeViewElement( "h2" );
	}

	static NodeViewElement h3( ViewElement.WitherSetter... setters ) {
		return h3().set( setters );
	}

	static NodeViewElement h3() {
		return new NodeViewElement( "h3" );
	}

	static NodeViewElement h4( ViewElement.WitherSetter... setters ) {
		return h4().set( setters );
	}

	static NodeViewElement h4() {
		return new NodeViewElement( "h4" );
	}

	static NodeViewElement h5( ViewElement.WitherSetter... setters ) {
		return h5().set( setters );
	}

	static NodeViewElement h5() {
		return new NodeViewElement( "h5" );
	}

	static NodeViewElement h6( ViewElement.WitherSetter... setters ) {
		return h6().set( setters );
	}

	static NodeViewElement h6() {
		return new NodeViewElement( "h6" );
	}

	static NodeViewElement head( ViewElement.WitherSetter... setters ) {
		return head().set( setters );
	}

	static NodeViewElement head() {
		return new NodeViewElement( "head" );
	}

	static NodeViewElement header( ViewElement.WitherSetter... setters ) {
		return header().set( setters );
	}

	static NodeViewElement header() {
		return new NodeViewElement( "header" );
	}

	static NodeViewElement html( ViewElement.WitherSetter... setters ) {
		return html().set( setters );
	}

	static NodeViewElement html() {
		return new NodeViewElement( "html" );
	}

	static NodeViewElement i( ViewElement.WitherSetter... setters ) {
		return i().set( setters );
	}

	static NodeViewElement i() {
		return new NodeViewElement( "i" );
	}

	static NodeViewElement iframe( ViewElement.WitherSetter... setters ) {
		return iframe().set( setters );
	}

	static NodeViewElement iframe() {
		return new NodeViewElement( "iframe" );
	}

	static NodeViewElement ins( ViewElement.WitherSetter... setters ) {
		return ins().set( setters );
	}

	static NodeViewElement ins() {
		return new NodeViewElement( "ins" );
	}

	static NodeViewElement kbd( ViewElement.WitherSetter... setters ) {
		return kbd().set( setters );
	}

	static NodeViewElement kbd() {
		return new NodeViewElement( "kbd" );
	}

	static NodeViewElement label( ViewElement.WitherSetter... setters ) {
		return label().set( setters );
	}

	static NodeViewElement label() {
		return new NodeViewElement( "label" );
	}

	static NodeViewElement legend( ViewElement.WitherSetter... setters ) {
		return legend().set( setters );
	}

	static NodeViewElement legend() {
		return new NodeViewElement( "legend" );
	}

	static NodeViewElement li( ViewElement.WitherSetter... setters ) {
		return li().set( setters );
	}

	static NodeViewElement li() {
		return new NodeViewElement( "li" );
	}

	static NodeViewElement main( ViewElement.WitherSetter... setters ) {
		return main().set( setters );
	}

	static NodeViewElement main() {
		return new NodeViewElement( "main" );
	}

	static NodeViewElement map( ViewElement.WitherSetter... setters ) {
		return map().set( setters );
	}

	static NodeViewElement map() {
		return new NodeViewElement( "map" );
	}

	static NodeViewElement mark( ViewElement.WitherSetter... setters ) {
		return mark().set( setters );
	}

	static NodeViewElement mark() {
		return new NodeViewElement( "mark" );
	}

	static NodeViewElement menu( ViewElement.WitherSetter... setters ) {
		return menu().set( setters );
	}

	static NodeViewElement menu() {
		return new NodeViewElement( "menu" );
	}

	static NodeViewElement menuitem( ViewElement.WitherSetter... setters ) {
		return menuitem().set( setters );
	}

	static NodeViewElement menuitem() {
		return new NodeViewElement( "menuitem" );
	}

	static NodeViewElement meter( ViewElement.WitherSetter... setters ) {
		return meter().set( setters );
	}

	static NodeViewElement meter() {
		return new NodeViewElement( "meter" );
	}

	static NodeViewElement nav( ViewElement.WitherSetter... setters ) {
		return nav().set( setters );
	}

	static NodeViewElement nav() {
		return new NodeViewElement( "nav" );
	}

	static NodeViewElement noscript( ViewElement.WitherSetter... setters ) {
		return noscript().set( setters );
	}

	static NodeViewElement noscript() {
		return new NodeViewElement( "noscript" );
	}

	static NodeViewElement object( ViewElement.WitherSetter... setters ) {
		return object().set( setters );
	}

	static NodeViewElement object() {
		return new NodeViewElement( "object" );
	}

	static NodeViewElement ol( ViewElement.WitherSetter... setters ) {
		return ol().set( setters );
	}

	static NodeViewElement ol() {
		return new NodeViewElement( "ol" );
	}

	static NodeViewElement optgroup( ViewElement.WitherSetter... setters ) {
		return optgroup().set( setters );
	}

	static NodeViewElement optgroup() {
		return new NodeViewElement( "optgroup" );
	}

	static NodeViewElement option( ViewElement.WitherSetter... setters ) {
		return option().set( setters );
	}

	static NodeViewElement option() {
		return new NodeViewElement( "option" );
	}

	static NodeViewElement output( ViewElement.WitherSetter... setters ) {
		return output().set( setters );
	}

	static NodeViewElement output() {
		return new NodeViewElement( "output" );
	}

	static NodeViewElement p( ViewElement.WitherSetter... setters ) {
		return p().set( setters );
	}

	static NodeViewElement p() {
		return new NodeViewElement( "p" );
	}

	static NodeViewElement pre( ViewElement.WitherSetter... setters ) {
		return pre().set( setters );
	}

	static NodeViewElement pre() {
		return new NodeViewElement( "pre" );
	}

	static NodeViewElement progress( ViewElement.WitherSetter... setters ) {
		return progress().set( setters );
	}

	static NodeViewElement progress() {
		return new NodeViewElement( "progress" );
	}

	static NodeViewElement q( ViewElement.WitherSetter... setters ) {
		return q().set( setters );
	}

	static NodeViewElement q() {
		return new NodeViewElement( "q" );
	}

	static NodeViewElement rp( ViewElement.WitherSetter... setters ) {
		return rp().set( setters );
	}

	static NodeViewElement rp() {
		return new NodeViewElement( "rp" );
	}

	static NodeViewElement rt( ViewElement.WitherSetter... setters ) {
		return rt().set( setters );
	}

	static NodeViewElement rt() {
		return new NodeViewElement( "rt" );
	}

	static NodeViewElement ruby( ViewElement.WitherSetter... setters ) {
		return ruby().set( setters );
	}

	static NodeViewElement ruby() {
		return new NodeViewElement( "ruby" );
	}

	static NodeViewElement s( ViewElement.WitherSetter... setters ) {
		return s().set( setters );
	}

	static NodeViewElement s() {
		return new NodeViewElement( "s" );
	}

	static NodeViewElement samp( ViewElement.WitherSetter... setters ) {
		return samp().set( setters );
	}

	static NodeViewElement samp() {
		return new NodeViewElement( "samp" );
	}

	static NodeViewElement script( ViewElement.WitherSetter... setters ) {
		return script().set( setters );
	}

	static NodeViewElement script() {
		return new NodeViewElement( "script" );
	}

	static NodeViewElement section( ViewElement.WitherSetter... setters ) {
		return section().set( setters );
	}

	static NodeViewElement section() {
		return new NodeViewElement( "section" );
	}

	static NodeViewElement select( ViewElement.WitherSetter... setters ) {
		return select().set( setters );
	}

	static NodeViewElement select() {
		return new NodeViewElement( "select" );
	}

	static NodeViewElement small( ViewElement.WitherSetter... setters ) {
		return small().set( setters );
	}

	static NodeViewElement small() {
		return new NodeViewElement( "small" );
	}

	static NodeViewElement span( ViewElement.WitherSetter... setters ) {
		return span().set( setters );
	}

	static NodeViewElement span() {
		return new NodeViewElement( "span" );
	}

	static NodeViewElement strong( ViewElement.WitherSetter... setters ) {
		return strong().set( setters );
	}

	static NodeViewElement strong() {
		return new NodeViewElement( "strong" );
	}

	static NodeViewElement style( ViewElement.WitherSetter... setters ) {
		return style().set( setters );
	}

	static NodeViewElement style() {
		return new NodeViewElement( "style" );
	}

	static NodeViewElement sub( ViewElement.WitherSetter... setters ) {
		return sub().set( setters );
	}

	static NodeViewElement sub() {
		return new NodeViewElement( "sub" );
	}

	static NodeViewElement summary( ViewElement.WitherSetter... setters ) {
		return summary().set( setters );
	}

	static NodeViewElement summary() {
		return new NodeViewElement( "summary" );
	}

	static NodeViewElement sup( ViewElement.WitherSetter... setters ) {
		return sup().set( setters );
	}

	static NodeViewElement sup() {
		return new NodeViewElement( "sup" );
	}

	static NodeViewElement table( ViewElement.WitherSetter... setters ) {
		return table().set( setters );
	}

	static NodeViewElement table() {
		return new NodeViewElement( "table" );
	}

	static NodeViewElement tbody( ViewElement.WitherSetter... setters ) {
		return tbody().set( setters );
	}

	static NodeViewElement tbody() {
		return new NodeViewElement( "tbody" );
	}

	static NodeViewElement td( ViewElement.WitherSetter... setters ) {
		return td().set( setters );
	}

	static NodeViewElement td() {
		return new NodeViewElement( "td" );
	}

	static NodeViewElement textarea( ViewElement.WitherSetter... setters ) {
		return textarea().set( setters );
	}

	static NodeViewElement textarea() {
		return new NodeViewElement( "textarea" );
	}

	static NodeViewElement tfoot( ViewElement.WitherSetter... setters ) {
		return tfoot().set( setters );
	}

	static NodeViewElement tfoot() {
		return new NodeViewElement( "tfoot" );
	}

	static NodeViewElement th( ViewElement.WitherSetter... setters ) {
		return th().set( setters );
	}

	static NodeViewElement th() {
		return new NodeViewElement( "th" );
	}

	static NodeViewElement thead( ViewElement.WitherSetter... setters ) {
		return thead().set( setters );
	}

	static NodeViewElement thead() {
		return new NodeViewElement( "thead" );
	}

	static NodeViewElement time( ViewElement.WitherSetter... setters ) {
		return time().set( setters );
	}

	static NodeViewElement time() {
		return new NodeViewElement( "time" );
	}

	static NodeViewElement title( ViewElement.WitherSetter... setters ) {
		return title().set( setters );
	}

	static NodeViewElement title() {
		return new NodeViewElement( "title" );
	}

	static NodeViewElement tr( ViewElement.WitherSetter... setters ) {
		return tr().set( setters );
	}

	static NodeViewElement tr() {
		return new NodeViewElement( "tr" );
	}

	static NodeViewElement u( ViewElement.WitherSetter... setters ) {
		return u().set( setters );
	}

	static NodeViewElement u() {
		return new NodeViewElement( "u" );
	}

	static NodeViewElement ul( ViewElement.WitherSetter... setters ) {
		return ul().set( setters );
	}

	static NodeViewElement ul() {
		return new NodeViewElement( "ul" );
	}

	static NodeViewElement var( ViewElement.WitherSetter... setters ) {
		return var().set( setters );
	}

	static NodeViewElement var() {
		return new NodeViewElement( "var" );
	}

	static NodeViewElement video( ViewElement.WitherSetter... setters ) {
		return video().set( setters );
	}

	static NodeViewElement video() {
		return new NodeViewElement( "video" );
	}

	// -- end generated section

	/**
	 * Add escaped text.
	 */
	static TextViewElement text( String text ) {
		return new TextViewElement( text );
	}

	/**
	 * Add unescaped text (usually html).
	 */
	static TextViewElement unescapedText( String text ) {
		return new TextViewElement( text, false );
	}

	static NodeViewElement createNode( String tagName, ViewElement... children ) {
		NodeViewElement node = new NodeViewElement( tagName );
		if ( children.length > 0 ) {
			node.addChildren( Arrays.asList( children ) );
		}

		return node;
	}
}
