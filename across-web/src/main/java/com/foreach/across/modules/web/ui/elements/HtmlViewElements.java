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
import com.foreach.across.modules.web.ui.elements.builder.HtmlViewElementBuilders;

import static com.foreach.across.modules.web.ui.elements.HtmlViewElement.Functions.children;

/**
 * Contains factory methods and {@link ViewElement.Wither} functions for HTML5 nodes.
 *
 * @author Arne Vandamme
 * @see HtmlViewElement.Functions
 * @since 5.0.0
 */
@SuppressWarnings("WeakerAccess")
public class HtmlViewElements
{
	/**
	 * Static import handle for the {@link HtmlViewElement} implementations.
	 */
	public static final HtmlViewElements html = new HtmlViewElements();

	/**
	 * Import handle for the equivalent element builders.
	 */
	public final HtmlViewElementBuilders builders = new HtmlViewElementBuilders();

	// -- begin generated section

	public VoidNodeViewElement area( ViewElement.WitherSetter... setters ) {
		return area().set( setters );
	}

	public VoidNodeViewElement area() {
		return new VoidNodeViewElement( "area" );
	}

	public VoidNodeViewElement base( ViewElement.WitherSetter... setters ) {
		return base().set( setters );
	}

	public VoidNodeViewElement base() {
		return new VoidNodeViewElement( "base" );
	}

	public VoidNodeViewElement br( ViewElement.WitherSetter... setters ) {
		return br().set( setters );
	}

	public VoidNodeViewElement br() {
		return new VoidNodeViewElement( "br" );
	}

	public VoidNodeViewElement col( ViewElement.WitherSetter... setters ) {
		return col().set( setters );
	}

	public VoidNodeViewElement col() {
		return new VoidNodeViewElement( "col" );
	}

	public VoidNodeViewElement embed( ViewElement.WitherSetter... setters ) {
		return embed().set( setters );
	}

	public VoidNodeViewElement embed() {
		return new VoidNodeViewElement( "embed" );
	}

	public VoidNodeViewElement hr( ViewElement.WitherSetter... setters ) {
		return hr().set( setters );
	}

	public VoidNodeViewElement hr() {
		return new VoidNodeViewElement( "hr" );
	}

	public VoidNodeViewElement img( ViewElement.WitherSetter... setters ) {
		return img().set( setters );
	}

	public VoidNodeViewElement img() {
		return new VoidNodeViewElement( "img" );
	}

	public VoidNodeViewElement input( ViewElement.WitherSetter... setters ) {
		return input().set( setters );
	}

	public VoidNodeViewElement input() {
		return new VoidNodeViewElement( "input" );
	}

	public VoidNodeViewElement keygen( ViewElement.WitherSetter... setters ) {
		return keygen().set( setters );
	}

	public VoidNodeViewElement keygen() {
		return new VoidNodeViewElement( "keygen" );
	}

	public VoidNodeViewElement link( ViewElement.WitherSetter... setters ) {
		return link().set( setters );
	}

	public VoidNodeViewElement link() {
		return new VoidNodeViewElement( "link" );
	}

	public VoidNodeViewElement meta( ViewElement.WitherSetter... setters ) {
		return meta().set( setters );
	}

	public VoidNodeViewElement meta() {
		return new VoidNodeViewElement( "meta" );
	}

	public VoidNodeViewElement param( ViewElement.WitherSetter... setters ) {
		return param().set( setters );
	}

	public VoidNodeViewElement param() {
		return new VoidNodeViewElement( "param" );
	}

	public VoidNodeViewElement source( ViewElement.WitherSetter... setters ) {
		return source().set( setters );
	}

	public VoidNodeViewElement source() {
		return new VoidNodeViewElement( "source" );
	}

	public VoidNodeViewElement track( ViewElement.WitherSetter... setters ) {
		return track().set( setters );
	}

	public VoidNodeViewElement track() {
		return new VoidNodeViewElement( "track" );
	}

	public VoidNodeViewElement wbr( ViewElement.WitherSetter... setters ) {
		return wbr().set( setters );
	}

	public VoidNodeViewElement wbr() {
		return new VoidNodeViewElement( "wbr" );
	}

	public NodeViewElement a( ViewElement.WitherSetter... setters ) {
		return a().set( setters );
	}

	public NodeViewElement a() {
		return new NodeViewElement( "a" );
	}

	public NodeViewElement abbr( ViewElement.WitherSetter... setters ) {
		return abbr().set( setters );
	}

	public NodeViewElement abbr() {
		return new NodeViewElement( "abbr" );
	}

	public NodeViewElement address( ViewElement.WitherSetter... setters ) {
		return address().set( setters );
	}

	public NodeViewElement address() {
		return new NodeViewElement( "address" );
	}

	public NodeViewElement article( ViewElement.WitherSetter... setters ) {
		return article().set( setters );
	}

	public NodeViewElement article() {
		return new NodeViewElement( "article" );
	}

	public NodeViewElement aside( ViewElement.WitherSetter... setters ) {
		return aside().set( setters );
	}

	public NodeViewElement aside() {
		return new NodeViewElement( "aside" );
	}

	public NodeViewElement audio( ViewElement.WitherSetter... setters ) {
		return audio().set( setters );
	}

	public NodeViewElement audio() {
		return new NodeViewElement( "audio" );
	}

	public NodeViewElement b( ViewElement.WitherSetter... setters ) {
		return b().set( setters );
	}

	public NodeViewElement b() {
		return new NodeViewElement( "b" );
	}

	public NodeViewElement bdi( ViewElement.WitherSetter... setters ) {
		return bdi().set( setters );
	}

	public NodeViewElement bdi() {
		return new NodeViewElement( "bdi" );
	}

	public NodeViewElement bdo( ViewElement.WitherSetter... setters ) {
		return bdo().set( setters );
	}

	public NodeViewElement bdo() {
		return new NodeViewElement( "bdo" );
	}

	public NodeViewElement blockquote( ViewElement.WitherSetter... setters ) {
		return blockquote().set( setters );
	}

	public NodeViewElement blockquote() {
		return new NodeViewElement( "blockquote" );
	}

	public NodeViewElement body( ViewElement.WitherSetter... setters ) {
		return body().set( setters );
	}

	public NodeViewElement body() {
		return new NodeViewElement( "body" );
	}

	public NodeViewElement button( ViewElement.WitherSetter... setters ) {
		return button().set( setters );
	}

	public NodeViewElement button() {
		return new NodeViewElement( "button" );
	}

	public NodeViewElement canvas( ViewElement.WitherSetter... setters ) {
		return canvas().set( setters );
	}

	public NodeViewElement canvas() {
		return new NodeViewElement( "canvas" );
	}

	public NodeViewElement caption( ViewElement.WitherSetter... setters ) {
		return caption().set( setters );
	}

	public NodeViewElement caption() {
		return new NodeViewElement( "caption" );
	}

	public NodeViewElement cite( ViewElement.WitherSetter... setters ) {
		return cite().set( setters );
	}

	public NodeViewElement cite() {
		return new NodeViewElement( "cite" );
	}

	public NodeViewElement code( ViewElement.WitherSetter... setters ) {
		return code().set( setters );
	}

	public NodeViewElement code() {
		return new NodeViewElement( "code" );
	}

	public NodeViewElement colgroup( ViewElement.WitherSetter... setters ) {
		return colgroup().set( setters );
	}

	public NodeViewElement colgroup() {
		return new NodeViewElement( "colgroup" );
	}

	public NodeViewElement datalist( ViewElement.WitherSetter... setters ) {
		return datalist().set( setters );
	}

	public NodeViewElement datalist() {
		return new NodeViewElement( "datalist" );
	}

	public NodeViewElement dd( ViewElement.WitherSetter... setters ) {
		return dd().set( setters );
	}

	public NodeViewElement dd() {
		return new NodeViewElement( "dd" );
	}

	public NodeViewElement del( ViewElement.WitherSetter... setters ) {
		return del().set( setters );
	}

	public NodeViewElement del() {
		return new NodeViewElement( "del" );
	}

	public NodeViewElement details( ViewElement.WitherSetter... setters ) {
		return details().set( setters );
	}

	public NodeViewElement details() {
		return new NodeViewElement( "details" );
	}

	public NodeViewElement dfn( ViewElement.WitherSetter... setters ) {
		return dfn().set( setters );
	}

	public NodeViewElement dfn() {
		return new NodeViewElement( "dfn" );
	}

	public NodeViewElement dialog( ViewElement.WitherSetter... setters ) {
		return dialog().set( setters );
	}

	public NodeViewElement dialog() {
		return new NodeViewElement( "dialog" );
	}

	public NodeViewElement div( ViewElement.WitherSetter... setters ) {
		return div().set( setters );
	}

	public NodeViewElement div() {
		return new NodeViewElement( "div" );
	}

	public NodeViewElement dl( ViewElement.WitherSetter... setters ) {
		return dl().set( setters );
	}

	public NodeViewElement dl() {
		return new NodeViewElement( "dl" );
	}

	public NodeViewElement dt( ViewElement.WitherSetter... setters ) {
		return dt().set( setters );
	}

	public NodeViewElement dt() {
		return new NodeViewElement( "dt" );
	}

	public NodeViewElement em( ViewElement.WitherSetter... setters ) {
		return em().set( setters );
	}

	public NodeViewElement em() {
		return new NodeViewElement( "em" );
	}

	public NodeViewElement fieldset( ViewElement.WitherSetter... setters ) {
		return fieldset().set( setters );
	}

	public NodeViewElement fieldset() {
		return new NodeViewElement( "fieldset" );
	}

	public NodeViewElement figcaption( ViewElement.WitherSetter... setters ) {
		return figcaption().set( setters );
	}

	public NodeViewElement figcaption() {
		return new NodeViewElement( "figcaption" );
	}

	public NodeViewElement figure( ViewElement.WitherSetter... setters ) {
		return figure().set( setters );
	}

	public NodeViewElement figure() {
		return new NodeViewElement( "figure" );
	}

	public NodeViewElement footer( ViewElement.WitherSetter... setters ) {
		return footer().set( setters );
	}

	public NodeViewElement footer() {
		return new NodeViewElement( "footer" );
	}

	public NodeViewElement form( ViewElement.WitherSetter... setters ) {
		return form().set( setters );
	}

	public NodeViewElement form() {
		return new NodeViewElement( "form" );
	}

	public NodeViewElement h1( ViewElement.WitherSetter... setters ) {
		return h1().set( setters );
	}

	public NodeViewElement h1() {
		return new NodeViewElement( "h1" );
	}

	public NodeViewElement h2( ViewElement.WitherSetter... setters ) {
		return h2().set( setters );
	}

	public NodeViewElement h2() {
		return new NodeViewElement( "h2" );
	}

	public NodeViewElement h3( ViewElement.WitherSetter... setters ) {
		return h3().set( setters );
	}

	public NodeViewElement h3() {
		return new NodeViewElement( "h3" );
	}

	public NodeViewElement h4( ViewElement.WitherSetter... setters ) {
		return h4().set( setters );
	}

	public NodeViewElement h4() {
		return new NodeViewElement( "h4" );
	}

	public NodeViewElement h5( ViewElement.WitherSetter... setters ) {
		return h5().set( setters );
	}

	public NodeViewElement h5() {
		return new NodeViewElement( "h5" );
	}

	public NodeViewElement h6( ViewElement.WitherSetter... setters ) {
		return h6().set( setters );
	}

	public NodeViewElement h6() {
		return new NodeViewElement( "h6" );
	}

	public NodeViewElement head( ViewElement.WitherSetter... setters ) {
		return head().set( setters );
	}

	public NodeViewElement head() {
		return new NodeViewElement( "head" );
	}

	public NodeViewElement header( ViewElement.WitherSetter... setters ) {
		return header().set( setters );
	}

	public NodeViewElement header() {
		return new NodeViewElement( "header" );
	}

	public NodeViewElement html( ViewElement.WitherSetter... setters ) {
		return html().set( setters );
	}

	public NodeViewElement html() {
		return new NodeViewElement( "html" );
	}

	public NodeViewElement i( ViewElement.WitherSetter... setters ) {
		return i().set( setters );
	}

	public NodeViewElement i() {
		return new NodeViewElement( "i" );
	}

	public NodeViewElement iframe( ViewElement.WitherSetter... setters ) {
		return iframe().set( setters );
	}

	public NodeViewElement iframe() {
		return new NodeViewElement( "iframe" );
	}

	public NodeViewElement ins( ViewElement.WitherSetter... setters ) {
		return ins().set( setters );
	}

	public NodeViewElement ins() {
		return new NodeViewElement( "ins" );
	}

	public NodeViewElement kbd( ViewElement.WitherSetter... setters ) {
		return kbd().set( setters );
	}

	public NodeViewElement kbd() {
		return new NodeViewElement( "kbd" );
	}

	public NodeViewElement label( ViewElement.WitherSetter... setters ) {
		return label().set( setters );
	}

	public NodeViewElement label() {
		return new NodeViewElement( "label" );
	}

	public NodeViewElement legend( ViewElement.WitherSetter... setters ) {
		return legend().set( setters );
	}

	public NodeViewElement legend() {
		return new NodeViewElement( "legend" );
	}

	public NodeViewElement li( ViewElement.WitherSetter... setters ) {
		return li().set( setters );
	}

	public NodeViewElement li() {
		return new NodeViewElement( "li" );
	}

	public NodeViewElement main( ViewElement.WitherSetter... setters ) {
		return main().set( setters );
	}

	public NodeViewElement main() {
		return new NodeViewElement( "main" );
	}

	public NodeViewElement map( ViewElement.WitherSetter... setters ) {
		return map().set( setters );
	}

	public NodeViewElement map() {
		return new NodeViewElement( "map" );
	}

	public NodeViewElement mark( ViewElement.WitherSetter... setters ) {
		return mark().set( setters );
	}

	public NodeViewElement mark() {
		return new NodeViewElement( "mark" );
	}

	public NodeViewElement menu( ViewElement.WitherSetter... setters ) {
		return menu().set( setters );
	}

	public NodeViewElement menu() {
		return new NodeViewElement( "menu" );
	}

	public NodeViewElement menuitem( ViewElement.WitherSetter... setters ) {
		return menuitem().set( setters );
	}

	public NodeViewElement menuitem() {
		return new NodeViewElement( "menuitem" );
	}

	public NodeViewElement meter( ViewElement.WitherSetter... setters ) {
		return meter().set( setters );
	}

	public NodeViewElement meter() {
		return new NodeViewElement( "meter" );
	}

	public NodeViewElement nav( ViewElement.WitherSetter... setters ) {
		return nav().set( setters );
	}

	public NodeViewElement nav() {
		return new NodeViewElement( "nav" );
	}

	public NodeViewElement noscript( ViewElement.WitherSetter... setters ) {
		return noscript().set( setters );
	}

	public NodeViewElement noscript() {
		return new NodeViewElement( "noscript" );
	}

	public NodeViewElement object( ViewElement.WitherSetter... setters ) {
		return object().set( setters );
	}

	public NodeViewElement object() {
		return new NodeViewElement( "object" );
	}

	public NodeViewElement ol( ViewElement.WitherSetter... setters ) {
		return ol().set( setters );
	}

	public NodeViewElement ol() {
		return new NodeViewElement( "ol" );
	}

	public NodeViewElement optgroup( ViewElement.WitherSetter... setters ) {
		return optgroup().set( setters );
	}

	public NodeViewElement optgroup() {
		return new NodeViewElement( "optgroup" );
	}

	public NodeViewElement option( ViewElement.WitherSetter... setters ) {
		return option().set( setters );
	}

	public NodeViewElement option() {
		return new NodeViewElement( "option" );
	}

	public NodeViewElement output( ViewElement.WitherSetter... setters ) {
		return output().set( setters );
	}

	public NodeViewElement output() {
		return new NodeViewElement( "output" );
	}

	public NodeViewElement p( ViewElement.WitherSetter... setters ) {
		return p().set( setters );
	}

	public NodeViewElement p() {
		return new NodeViewElement( "p" );
	}

	public NodeViewElement pre( ViewElement.WitherSetter... setters ) {
		return pre().set( setters );
	}

	public NodeViewElement pre() {
		return new NodeViewElement( "pre" );
	}

	public NodeViewElement progress( ViewElement.WitherSetter... setters ) {
		return progress().set( setters );
	}

	public NodeViewElement progress() {
		return new NodeViewElement( "progress" );
	}

	public NodeViewElement q( ViewElement.WitherSetter... setters ) {
		return q().set( setters );
	}

	public NodeViewElement q() {
		return new NodeViewElement( "q" );
	}

	public NodeViewElement rp( ViewElement.WitherSetter... setters ) {
		return rp().set( setters );
	}

	public NodeViewElement rp() {
		return new NodeViewElement( "rp" );
	}

	public NodeViewElement rt( ViewElement.WitherSetter... setters ) {
		return rt().set( setters );
	}

	public NodeViewElement rt() {
		return new NodeViewElement( "rt" );
	}

	public NodeViewElement ruby( ViewElement.WitherSetter... setters ) {
		return ruby().set( setters );
	}

	public NodeViewElement ruby() {
		return new NodeViewElement( "ruby" );
	}

	public NodeViewElement s( ViewElement.WitherSetter... setters ) {
		return s().set( setters );
	}

	public NodeViewElement s() {
		return new NodeViewElement( "s" );
	}

	public NodeViewElement samp( ViewElement.WitherSetter... setters ) {
		return samp().set( setters );
	}

	public NodeViewElement samp() {
		return new NodeViewElement( "samp" );
	}

	public NodeViewElement script( ViewElement.WitherSetter... setters ) {
		return script().set( setters );
	}

	public NodeViewElement script() {
		return new NodeViewElement( "script" );
	}

	public NodeViewElement section( ViewElement.WitherSetter... setters ) {
		return section().set( setters );
	}

	public NodeViewElement section() {
		return new NodeViewElement( "section" );
	}

	public NodeViewElement select( ViewElement.WitherSetter... setters ) {
		return select().set( setters );
	}

	public NodeViewElement select() {
		return new NodeViewElement( "select" );
	}

	public NodeViewElement small( ViewElement.WitherSetter... setters ) {
		return small().set( setters );
	}

	public NodeViewElement small() {
		return new NodeViewElement( "small" );
	}

	public NodeViewElement span( ViewElement.WitherSetter... setters ) {
		return span().set( setters );
	}

	public NodeViewElement span() {
		return new NodeViewElement( "span" );
	}

	public NodeViewElement strong( ViewElement.WitherSetter... setters ) {
		return strong().set( setters );
	}

	public NodeViewElement strong() {
		return new NodeViewElement( "strong" );
	}

	public NodeViewElement style( ViewElement.WitherSetter... setters ) {
		return style().set( setters );
	}

	public NodeViewElement style() {
		return new NodeViewElement( "style" );
	}

	public NodeViewElement sub( ViewElement.WitherSetter... setters ) {
		return sub().set( setters );
	}

	public NodeViewElement sub() {
		return new NodeViewElement( "sub" );
	}

	public NodeViewElement summary( ViewElement.WitherSetter... setters ) {
		return summary().set( setters );
	}

	public NodeViewElement summary() {
		return new NodeViewElement( "summary" );
	}

	public NodeViewElement sup( ViewElement.WitherSetter... setters ) {
		return sup().set( setters );
	}

	public NodeViewElement sup() {
		return new NodeViewElement( "sup" );
	}

	public NodeViewElement table( ViewElement.WitherSetter... setters ) {
		return table().set( setters );
	}

	public NodeViewElement table() {
		return new NodeViewElement( "table" );
	}

	public NodeViewElement tbody( ViewElement.WitherSetter... setters ) {
		return tbody().set( setters );
	}

	public NodeViewElement tbody() {
		return new NodeViewElement( "tbody" );
	}

	public NodeViewElement td( ViewElement.WitherSetter... setters ) {
		return td().set( setters );
	}

	public NodeViewElement td() {
		return new NodeViewElement( "td" );
	}

	public NodeViewElement textarea( ViewElement.WitherSetter... setters ) {
		return textarea().set( setters );
	}

	public NodeViewElement textarea() {
		return new NodeViewElement( "textarea" );
	}

	public NodeViewElement tfoot( ViewElement.WitherSetter... setters ) {
		return tfoot().set( setters );
	}

	public NodeViewElement tfoot() {
		return new NodeViewElement( "tfoot" );
	}

	public NodeViewElement th( ViewElement.WitherSetter... setters ) {
		return th().set( setters );
	}

	public NodeViewElement th() {
		return new NodeViewElement( "th" );
	}

	public NodeViewElement thead( ViewElement.WitherSetter... setters ) {
		return thead().set( setters );
	}

	public NodeViewElement thead() {
		return new NodeViewElement( "thead" );
	}

	public NodeViewElement time( ViewElement.WitherSetter... setters ) {
		return time().set( setters );
	}

	public NodeViewElement time() {
		return new NodeViewElement( "time" );
	}

	public NodeViewElement title( ViewElement.WitherSetter... setters ) {
		return title().set( setters );
	}

	public NodeViewElement title() {
		return new NodeViewElement( "title" );
	}

	public NodeViewElement tr( ViewElement.WitherSetter... setters ) {
		return tr().set( setters );
	}

	public NodeViewElement tr() {
		return new NodeViewElement( "tr" );
	}

	public NodeViewElement u( ViewElement.WitherSetter... setters ) {
		return u().set( setters );
	}

	public NodeViewElement u() {
		return new NodeViewElement( "u" );
	}

	public NodeViewElement ul( ViewElement.WitherSetter... setters ) {
		return ul().set( setters );
	}

	public NodeViewElement ul() {
		return new NodeViewElement( "ul" );
	}

	public NodeViewElement var( ViewElement.WitherSetter... setters ) {
		return var().set( setters );
	}

	public NodeViewElement var() {
		return new NodeViewElement( "var" );
	}

	public NodeViewElement video( ViewElement.WitherSetter... setters ) {
		return video().set( setters );
	}

	public NodeViewElement video() {
		return new NodeViewElement( "video" );
	}

	// -- end generated section

	/**
	 * Add escaped text.
	 */
	public TextViewElement text( String text ) {
		return new TextViewElement( text );
	}

	/**
	 * Add unescaped text (usually html).
	 */
	public TextViewElement unescapedText( String text ) {
		return new TextViewElement( text, false );
	}

	public ContainerViewElement container( ViewElement... childElements ) {
		return container( children( childElements ) );
	}

	public ContainerViewElement container( ViewElement.WitherSetter... setters ) {
		return new ContainerViewElement().set( setters );
	}
}
