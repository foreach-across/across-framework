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
import com.foreach.across.modules.web.ui.ViewElementBuilder;

/**
 * Contains factory methods for node element builders, matching the default HTML tags.
 * This class is deliberately non-static to allow for easier composition and extension.
 * Can be statically imported through {@link com.foreach.across.modules.web.ui.elements.HtmlViewElements} as {@code html.builders.*}.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.web.ui.elements.HtmlViewElements
 * @since 5.0.0
 */
@SuppressWarnings("WeakerAccess")
public class HtmlViewElementBuilders
{
	// -- Begin generated section

	public VoidNodeViewElementBuilder area( ViewElement.WitherSetter... setters ) {
		return area().with( setters );
	}

	public VoidNodeViewElementBuilder area() {
		return new VoidNodeViewElementBuilder( "area" );
	}

	public VoidNodeViewElementBuilder base( ViewElement.WitherSetter... setters ) {
		return base().with( setters );
	}

	public VoidNodeViewElementBuilder base() {
		return new VoidNodeViewElementBuilder( "base" );
	}

	public VoidNodeViewElementBuilder br( ViewElement.WitherSetter... setters ) {
		return br().with( setters );
	}

	public VoidNodeViewElementBuilder br() {
		return new VoidNodeViewElementBuilder( "br" );
	}

	public VoidNodeViewElementBuilder col( ViewElement.WitherSetter... setters ) {
		return col().with( setters );
	}

	public VoidNodeViewElementBuilder col() {
		return new VoidNodeViewElementBuilder( "col" );
	}

	public VoidNodeViewElementBuilder embed( ViewElement.WitherSetter... setters ) {
		return embed().with( setters );
	}

	public VoidNodeViewElementBuilder embed() {
		return new VoidNodeViewElementBuilder( "embed" );
	}

	public VoidNodeViewElementBuilder hr( ViewElement.WitherSetter... setters ) {
		return hr().with( setters );
	}

	public VoidNodeViewElementBuilder hr() {
		return new VoidNodeViewElementBuilder( "hr" );
	}

	public VoidNodeViewElementBuilder img( ViewElement.WitherSetter... setters ) {
		return img().with( setters );
	}

	public VoidNodeViewElementBuilder img() {
		return new VoidNodeViewElementBuilder( "img" );
	}

	public VoidNodeViewElementBuilder input( ViewElement.WitherSetter... setters ) {
		return input().with( setters );
	}

	public VoidNodeViewElementBuilder input() {
		return new VoidNodeViewElementBuilder( "input" );
	}

	public VoidNodeViewElementBuilder keygen( ViewElement.WitherSetter... setters ) {
		return keygen().with( setters );
	}

	public VoidNodeViewElementBuilder keygen() {
		return new VoidNodeViewElementBuilder( "keygen" );
	}

	public VoidNodeViewElementBuilder link( ViewElement.WitherSetter... setters ) {
		return link().with( setters );
	}

	public VoidNodeViewElementBuilder link() {
		return new VoidNodeViewElementBuilder( "link" );
	}

	public VoidNodeViewElementBuilder meta( ViewElement.WitherSetter... setters ) {
		return meta().with( setters );
	}

	public VoidNodeViewElementBuilder meta() {
		return new VoidNodeViewElementBuilder( "meta" );
	}

	public VoidNodeViewElementBuilder param( ViewElement.WitherSetter... setters ) {
		return param().with( setters );
	}

	public VoidNodeViewElementBuilder param() {
		return new VoidNodeViewElementBuilder( "param" );
	}

	public VoidNodeViewElementBuilder source( ViewElement.WitherSetter... setters ) {
		return source().with( setters );
	}

	public VoidNodeViewElementBuilder source() {
		return new VoidNodeViewElementBuilder( "source" );
	}

	public VoidNodeViewElementBuilder track( ViewElement.WitherSetter... setters ) {
		return track().with( setters );
	}

	public VoidNodeViewElementBuilder track() {
		return new VoidNodeViewElementBuilder( "track" );
	}

	public VoidNodeViewElementBuilder wbr( ViewElement.WitherSetter... setters ) {
		return wbr().with( setters );
	}

	public VoidNodeViewElementBuilder wbr() {
		return new VoidNodeViewElementBuilder( "wbr" );
	}

	public NodeViewElementBuilder a( ViewElement.WitherSetter... setters ) {
		return a().with( setters );
	}

	public NodeViewElementBuilder a() {
		return new NodeViewElementBuilder( "a" );
	}

	public NodeViewElementBuilder abbr( ViewElement.WitherSetter... setters ) {
		return abbr().with( setters );
	}

	public NodeViewElementBuilder abbr() {
		return new NodeViewElementBuilder( "abbr" );
	}

	public NodeViewElementBuilder address( ViewElement.WitherSetter... setters ) {
		return address().with( setters );
	}

	public NodeViewElementBuilder address() {
		return new NodeViewElementBuilder( "address" );
	}

	public NodeViewElementBuilder article( ViewElement.WitherSetter... setters ) {
		return article().with( setters );
	}

	public NodeViewElementBuilder article() {
		return new NodeViewElementBuilder( "article" );
	}

	public NodeViewElementBuilder aside( ViewElement.WitherSetter... setters ) {
		return aside().with( setters );
	}

	public NodeViewElementBuilder aside() {
		return new NodeViewElementBuilder( "aside" );
	}

	public NodeViewElementBuilder audio( ViewElement.WitherSetter... setters ) {
		return audio().with( setters );
	}

	public NodeViewElementBuilder audio() {
		return new NodeViewElementBuilder( "audio" );
	}

	public NodeViewElementBuilder b( ViewElement.WitherSetter... setters ) {
		return b().with( setters );
	}

	public NodeViewElementBuilder b() {
		return new NodeViewElementBuilder( "b" );
	}

	public NodeViewElementBuilder bdi( ViewElement.WitherSetter... setters ) {
		return bdi().with( setters );
	}

	public NodeViewElementBuilder bdi() {
		return new NodeViewElementBuilder( "bdi" );
	}

	public NodeViewElementBuilder bdo( ViewElement.WitherSetter... setters ) {
		return bdo().with( setters );
	}

	public NodeViewElementBuilder bdo() {
		return new NodeViewElementBuilder( "bdo" );
	}

	public NodeViewElementBuilder blockquote( ViewElement.WitherSetter... setters ) {
		return blockquote().with( setters );
	}

	public NodeViewElementBuilder blockquote() {
		return new NodeViewElementBuilder( "blockquote" );
	}

	public NodeViewElementBuilder body( ViewElement.WitherSetter... setters ) {
		return body().with( setters );
	}

	public NodeViewElementBuilder body() {
		return new NodeViewElementBuilder( "body" );
	}

	public NodeViewElementBuilder button( ViewElement.WitherSetter... setters ) {
		return button().with( setters );
	}

	public NodeViewElementBuilder button() {
		return new NodeViewElementBuilder( "button" );
	}

	public NodeViewElementBuilder canvas( ViewElement.WitherSetter... setters ) {
		return canvas().with( setters );
	}

	public NodeViewElementBuilder canvas() {
		return new NodeViewElementBuilder( "canvas" );
	}

	public NodeViewElementBuilder caption( ViewElement.WitherSetter... setters ) {
		return caption().with( setters );
	}

	public NodeViewElementBuilder caption() {
		return new NodeViewElementBuilder( "caption" );
	}

	public NodeViewElementBuilder cite( ViewElement.WitherSetter... setters ) {
		return cite().with( setters );
	}

	public NodeViewElementBuilder cite() {
		return new NodeViewElementBuilder( "cite" );
	}

	public NodeViewElementBuilder code( ViewElement.WitherSetter... setters ) {
		return code().with( setters );
	}

	public NodeViewElementBuilder code() {
		return new NodeViewElementBuilder( "code" );
	}

	public NodeViewElementBuilder colgroup( ViewElement.WitherSetter... setters ) {
		return colgroup().with( setters );
	}

	public NodeViewElementBuilder colgroup() {
		return new NodeViewElementBuilder( "colgroup" );
	}

	public NodeViewElementBuilder datalist( ViewElement.WitherSetter... setters ) {
		return datalist().with( setters );
	}

	public NodeViewElementBuilder datalist() {
		return new NodeViewElementBuilder( "datalist" );
	}

	public NodeViewElementBuilder dd( ViewElement.WitherSetter... setters ) {
		return dd().with( setters );
	}

	public NodeViewElementBuilder dd() {
		return new NodeViewElementBuilder( "dd" );
	}

	public NodeViewElementBuilder del( ViewElement.WitherSetter... setters ) {
		return del().with( setters );
	}

	public NodeViewElementBuilder del() {
		return new NodeViewElementBuilder( "del" );
	}

	public NodeViewElementBuilder details( ViewElement.WitherSetter... setters ) {
		return details().with( setters );
	}

	public NodeViewElementBuilder details() {
		return new NodeViewElementBuilder( "details" );
	}

	public NodeViewElementBuilder dfn( ViewElement.WitherSetter... setters ) {
		return dfn().with( setters );
	}

	public NodeViewElementBuilder dfn() {
		return new NodeViewElementBuilder( "dfn" );
	}

	public NodeViewElementBuilder dialog( ViewElement.WitherSetter... setters ) {
		return dialog().with( setters );
	}

	public NodeViewElementBuilder dialog() {
		return new NodeViewElementBuilder( "dialog" );
	}

	public NodeViewElementBuilder div( ViewElement.WitherSetter... setters ) {
		return div().with( setters );
	}

	public NodeViewElementBuilder div() {
		return new NodeViewElementBuilder( "div" );
	}

	public NodeViewElementBuilder dl( ViewElement.WitherSetter... setters ) {
		return dl().with( setters );
	}

	public NodeViewElementBuilder dl() {
		return new NodeViewElementBuilder( "dl" );
	}

	public NodeViewElementBuilder dt( ViewElement.WitherSetter... setters ) {
		return dt().with( setters );
	}

	public NodeViewElementBuilder dt() {
		return new NodeViewElementBuilder( "dt" );
	}

	public NodeViewElementBuilder em( ViewElement.WitherSetter... setters ) {
		return em().with( setters );
	}

	public NodeViewElementBuilder em() {
		return new NodeViewElementBuilder( "em" );
	}

	public NodeViewElementBuilder fieldset( ViewElement.WitherSetter... setters ) {
		return fieldset().with( setters );
	}

	public NodeViewElementBuilder fieldset() {
		return new NodeViewElementBuilder( "fieldset" );
	}

	public NodeViewElementBuilder figcaption( ViewElement.WitherSetter... setters ) {
		return figcaption().with( setters );
	}

	public NodeViewElementBuilder figcaption() {
		return new NodeViewElementBuilder( "figcaption" );
	}

	public NodeViewElementBuilder figure( ViewElement.WitherSetter... setters ) {
		return figure().with( setters );
	}

	public NodeViewElementBuilder figure() {
		return new NodeViewElementBuilder( "figure" );
	}

	public NodeViewElementBuilder footer( ViewElement.WitherSetter... setters ) {
		return footer().with( setters );
	}

	public NodeViewElementBuilder footer() {
		return new NodeViewElementBuilder( "footer" );
	}

	public NodeViewElementBuilder form( ViewElement.WitherSetter... setters ) {
		return form().with( setters );
	}

	public NodeViewElementBuilder form() {
		return new NodeViewElementBuilder( "form" );
	}

	public NodeViewElementBuilder h1( ViewElement.WitherSetter... setters ) {
		return h1().with( setters );
	}

	public NodeViewElementBuilder h1() {
		return new NodeViewElementBuilder( "h1" );
	}

	public NodeViewElementBuilder h2( ViewElement.WitherSetter... setters ) {
		return h2().with( setters );
	}

	public NodeViewElementBuilder h2() {
		return new NodeViewElementBuilder( "h2" );
	}

	public NodeViewElementBuilder h3( ViewElement.WitherSetter... setters ) {
		return h3().with( setters );
	}

	public NodeViewElementBuilder h3() {
		return new NodeViewElementBuilder( "h3" );
	}

	public NodeViewElementBuilder h4( ViewElement.WitherSetter... setters ) {
		return h4().with( setters );
	}

	public NodeViewElementBuilder h4() {
		return new NodeViewElementBuilder( "h4" );
	}

	public NodeViewElementBuilder h5( ViewElement.WitherSetter... setters ) {
		return h5().with( setters );
	}

	public NodeViewElementBuilder h5() {
		return new NodeViewElementBuilder( "h5" );
	}

	public NodeViewElementBuilder h6( ViewElement.WitherSetter... setters ) {
		return h6().with( setters );
	}

	public NodeViewElementBuilder h6() {
		return new NodeViewElementBuilder( "h6" );
	}

	public NodeViewElementBuilder head( ViewElement.WitherSetter... setters ) {
		return head().with( setters );
	}

	public NodeViewElementBuilder head() {
		return new NodeViewElementBuilder( "head" );
	}

	public NodeViewElementBuilder header( ViewElement.WitherSetter... setters ) {
		return header().with( setters );
	}

	public NodeViewElementBuilder header() {
		return new NodeViewElementBuilder( "header" );
	}

	public NodeViewElementBuilder html( ViewElement.WitherSetter... setters ) {
		return html().with( setters );
	}

	public NodeViewElementBuilder html() {
		return new NodeViewElementBuilder( "html" );
	}

	public NodeViewElementBuilder i( ViewElement.WitherSetter... setters ) {
		return i().with( setters );
	}

	public NodeViewElementBuilder i() {
		return new NodeViewElementBuilder( "i" );
	}

	public NodeViewElementBuilder iframe( ViewElement.WitherSetter... setters ) {
		return iframe().with( setters );
	}

	public NodeViewElementBuilder iframe() {
		return new NodeViewElementBuilder( "iframe" );
	}

	public NodeViewElementBuilder ins( ViewElement.WitherSetter... setters ) {
		return ins().with( setters );
	}

	public NodeViewElementBuilder ins() {
		return new NodeViewElementBuilder( "ins" );
	}

	public NodeViewElementBuilder kbd( ViewElement.WitherSetter... setters ) {
		return kbd().with( setters );
	}

	public NodeViewElementBuilder kbd() {
		return new NodeViewElementBuilder( "kbd" );
	}

	public NodeViewElementBuilder label( ViewElement.WitherSetter... setters ) {
		return label().with( setters );
	}

	public NodeViewElementBuilder label() {
		return new NodeViewElementBuilder( "label" );
	}

	public NodeViewElementBuilder legend( ViewElement.WitherSetter... setters ) {
		return legend().with( setters );
	}

	public NodeViewElementBuilder legend() {
		return new NodeViewElementBuilder( "legend" );
	}

	public NodeViewElementBuilder li( ViewElement.WitherSetter... setters ) {
		return li().with( setters );
	}

	public NodeViewElementBuilder li() {
		return new NodeViewElementBuilder( "li" );
	}

	public NodeViewElementBuilder main( ViewElement.WitherSetter... setters ) {
		return main().with( setters );
	}

	public NodeViewElementBuilder main() {
		return new NodeViewElementBuilder( "main" );
	}

	public NodeViewElementBuilder map( ViewElement.WitherSetter... setters ) {
		return map().with( setters );
	}

	public NodeViewElementBuilder map() {
		return new NodeViewElementBuilder( "map" );
	}

	public NodeViewElementBuilder mark( ViewElement.WitherSetter... setters ) {
		return mark().with( setters );
	}

	public NodeViewElementBuilder mark() {
		return new NodeViewElementBuilder( "mark" );
	}

	public NodeViewElementBuilder menu( ViewElement.WitherSetter... setters ) {
		return menu().with( setters );
	}

	public NodeViewElementBuilder menu() {
		return new NodeViewElementBuilder( "menu" );
	}

	public NodeViewElementBuilder menuitem( ViewElement.WitherSetter... setters ) {
		return menuitem().with( setters );
	}

	public NodeViewElementBuilder menuitem() {
		return new NodeViewElementBuilder( "menuitem" );
	}

	public NodeViewElementBuilder meter( ViewElement.WitherSetter... setters ) {
		return meter().with( setters );
	}

	public NodeViewElementBuilder meter() {
		return new NodeViewElementBuilder( "meter" );
	}

	public NodeViewElementBuilder nav( ViewElement.WitherSetter... setters ) {
		return nav().with( setters );
	}

	public NodeViewElementBuilder nav() {
		return new NodeViewElementBuilder( "nav" );
	}

	public NodeViewElementBuilder noscript( ViewElement.WitherSetter... setters ) {
		return noscript().with( setters );
	}

	public NodeViewElementBuilder noscript() {
		return new NodeViewElementBuilder( "noscript" );
	}

	public NodeViewElementBuilder object( ViewElement.WitherSetter... setters ) {
		return object().with( setters );
	}

	public NodeViewElementBuilder object() {
		return new NodeViewElementBuilder( "object" );
	}

	public NodeViewElementBuilder ol( ViewElement.WitherSetter... setters ) {
		return ol().with( setters );
	}

	public NodeViewElementBuilder ol() {
		return new NodeViewElementBuilder( "ol" );
	}

	public NodeViewElementBuilder optgroup( ViewElement.WitherSetter... setters ) {
		return optgroup().with( setters );
	}

	public NodeViewElementBuilder optgroup() {
		return new NodeViewElementBuilder( "optgroup" );
	}

	public NodeViewElementBuilder option( ViewElement.WitherSetter... setters ) {
		return option().with( setters );
	}

	public NodeViewElementBuilder option() {
		return new NodeViewElementBuilder( "option" );
	}

	public NodeViewElementBuilder output( ViewElement.WitherSetter... setters ) {
		return output().with( setters );
	}

	public NodeViewElementBuilder output() {
		return new NodeViewElementBuilder( "output" );
	}

	public NodeViewElementBuilder p( ViewElement.WitherSetter... setters ) {
		return p().with( setters );
	}

	public NodeViewElementBuilder p() {
		return new NodeViewElementBuilder( "p" );
	}

	public NodeViewElementBuilder pre( ViewElement.WitherSetter... setters ) {
		return pre().with( setters );
	}

	public NodeViewElementBuilder pre() {
		return new NodeViewElementBuilder( "pre" );
	}

	public NodeViewElementBuilder progress( ViewElement.WitherSetter... setters ) {
		return progress().with( setters );
	}

	public NodeViewElementBuilder progress() {
		return new NodeViewElementBuilder( "progress" );
	}

	public NodeViewElementBuilder q( ViewElement.WitherSetter... setters ) {
		return q().with( setters );
	}

	public NodeViewElementBuilder q() {
		return new NodeViewElementBuilder( "q" );
	}

	public NodeViewElementBuilder rp( ViewElement.WitherSetter... setters ) {
		return rp().with( setters );
	}

	public NodeViewElementBuilder rp() {
		return new NodeViewElementBuilder( "rp" );
	}

	public NodeViewElementBuilder rt( ViewElement.WitherSetter... setters ) {
		return rt().with( setters );
	}

	public NodeViewElementBuilder rt() {
		return new NodeViewElementBuilder( "rt" );
	}

	public NodeViewElementBuilder ruby( ViewElement.WitherSetter... setters ) {
		return ruby().with( setters );
	}

	public NodeViewElementBuilder ruby() {
		return new NodeViewElementBuilder( "ruby" );
	}

	public NodeViewElementBuilder s( ViewElement.WitherSetter... setters ) {
		return s().with( setters );
	}

	public NodeViewElementBuilder s() {
		return new NodeViewElementBuilder( "s" );
	}

	public NodeViewElementBuilder samp( ViewElement.WitherSetter... setters ) {
		return samp().with( setters );
	}

	public NodeViewElementBuilder samp() {
		return new NodeViewElementBuilder( "samp" );
	}

	public NodeViewElementBuilder script( ViewElement.WitherSetter... setters ) {
		return script().with( setters );
	}

	public NodeViewElementBuilder script() {
		return new NodeViewElementBuilder( "script" );
	}

	public NodeViewElementBuilder section( ViewElement.WitherSetter... setters ) {
		return section().with( setters );
	}

	public NodeViewElementBuilder section() {
		return new NodeViewElementBuilder( "section" );
	}

	public NodeViewElementBuilder select( ViewElement.WitherSetter... setters ) {
		return select().with( setters );
	}

	public NodeViewElementBuilder select() {
		return new NodeViewElementBuilder( "select" );
	}

	public NodeViewElementBuilder small( ViewElement.WitherSetter... setters ) {
		return small().with( setters );
	}

	public NodeViewElementBuilder small() {
		return new NodeViewElementBuilder( "small" );
	}

	public NodeViewElementBuilder span( ViewElement.WitherSetter... setters ) {
		return span().with( setters );
	}

	public NodeViewElementBuilder span() {
		return new NodeViewElementBuilder( "span" );
	}

	public NodeViewElementBuilder strong( ViewElement.WitherSetter... setters ) {
		return strong().with( setters );
	}

	public NodeViewElementBuilder strong() {
		return new NodeViewElementBuilder( "strong" );
	}

	public NodeViewElementBuilder style( ViewElement.WitherSetter... setters ) {
		return style().with( setters );
	}

	public NodeViewElementBuilder style() {
		return new NodeViewElementBuilder( "style" );
	}

	public NodeViewElementBuilder sub( ViewElement.WitherSetter... setters ) {
		return sub().with( setters );
	}

	public NodeViewElementBuilder sub() {
		return new NodeViewElementBuilder( "sub" );
	}

	public NodeViewElementBuilder summary( ViewElement.WitherSetter... setters ) {
		return summary().with( setters );
	}

	public NodeViewElementBuilder summary() {
		return new NodeViewElementBuilder( "summary" );
	}

	public NodeViewElementBuilder sup( ViewElement.WitherSetter... setters ) {
		return sup().with( setters );
	}

	public NodeViewElementBuilder sup() {
		return new NodeViewElementBuilder( "sup" );
	}

	public NodeViewElementBuilder table( ViewElement.WitherSetter... setters ) {
		return table().with( setters );
	}

	public NodeViewElementBuilder table() {
		return new NodeViewElementBuilder( "table" );
	}

	public NodeViewElementBuilder tbody( ViewElement.WitherSetter... setters ) {
		return tbody().with( setters );
	}

	public NodeViewElementBuilder tbody() {
		return new NodeViewElementBuilder( "tbody" );
	}

	public NodeViewElementBuilder td( ViewElement.WitherSetter... setters ) {
		return td().with( setters );
	}

	public NodeViewElementBuilder td() {
		return new NodeViewElementBuilder( "td" );
	}

	public NodeViewElementBuilder textarea( ViewElement.WitherSetter... setters ) {
		return textarea().with( setters );
	}

	public NodeViewElementBuilder textarea() {
		return new NodeViewElementBuilder( "textarea" );
	}

	public NodeViewElementBuilder tfoot( ViewElement.WitherSetter... setters ) {
		return tfoot().with( setters );
	}

	public NodeViewElementBuilder tfoot() {
		return new NodeViewElementBuilder( "tfoot" );
	}

	public NodeViewElementBuilder th( ViewElement.WitherSetter... setters ) {
		return th().with( setters );
	}

	public NodeViewElementBuilder th() {
		return new NodeViewElementBuilder( "th" );
	}

	public NodeViewElementBuilder thead( ViewElement.WitherSetter... setters ) {
		return thead().with( setters );
	}

	public NodeViewElementBuilder thead() {
		return new NodeViewElementBuilder( "thead" );
	}

	public NodeViewElementBuilder time( ViewElement.WitherSetter... setters ) {
		return time().with( setters );
	}

	public NodeViewElementBuilder time() {
		return new NodeViewElementBuilder( "time" );
	}

	public NodeViewElementBuilder title( ViewElement.WitherSetter... setters ) {
		return title().with( setters );
	}

	public NodeViewElementBuilder title() {
		return new NodeViewElementBuilder( "title" );
	}

	public NodeViewElementBuilder tr( ViewElement.WitherSetter... setters ) {
		return tr().with( setters );
	}

	public NodeViewElementBuilder tr() {
		return new NodeViewElementBuilder( "tr" );
	}

	public NodeViewElementBuilder u( ViewElement.WitherSetter... setters ) {
		return u().with( setters );
	}

	public NodeViewElementBuilder u() {
		return new NodeViewElementBuilder( "u" );
	}

	public NodeViewElementBuilder ul( ViewElement.WitherSetter... setters ) {
		return ul().with( setters );
	}

	public NodeViewElementBuilder ul() {
		return new NodeViewElementBuilder( "ul" );
	}

	public NodeViewElementBuilder var( ViewElement.WitherSetter... setters ) {
		return var().with( setters );
	}

	public NodeViewElementBuilder var() {
		return new NodeViewElementBuilder( "var" );
	}

	public NodeViewElementBuilder video( ViewElement.WitherSetter... setters ) {
		return video().with( setters );
	}

	public NodeViewElementBuilder video() {
		return new NodeViewElementBuilder( "video" );
	}

	// -- End generated section

	/**
	 * Add escaped text.
	 */
	public TextViewElementBuilder text( String text ) {
		return new TextViewElementBuilder().text( text );
	}

	/**
	 * Add unescaped text (usually html).
	 */
	public TextViewElementBuilder unescapedText( String text ) {
		return new TextViewElementBuilder().text( text ).escapeXml( false );
	}

	public ContainerViewElementBuilder container() {
		return new ContainerViewElementBuilder();
	}

	public ContainerViewElementBuilder container( ViewElementBuilder... childElements ) {
		return new ContainerViewElementBuilder().add( childElements );
	}

	public ContainerViewElementBuilder container( ViewElement.WitherSetter... setters ) {
		return new ContainerViewElementBuilder().with( setters );
	}
}
