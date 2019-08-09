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

	static VoidNodeViewElement area() {
		return new VoidNodeViewElement( "area" );
	}

	static VoidNodeViewElement base() {
		return new VoidNodeViewElement( "base" );
	}

	static VoidNodeViewElement br() {
		return new VoidNodeViewElement( "br" );
	}

	static VoidNodeViewElement col() {
		return new VoidNodeViewElement( "col" );
	}

	static VoidNodeViewElement embed() {
		return new VoidNodeViewElement( "embed" );
	}

	static VoidNodeViewElement hr() {
		return new VoidNodeViewElement( "hr" );
	}

	static VoidNodeViewElement img() {
		return new VoidNodeViewElement( "img" );
	}

	static VoidNodeViewElement input() {
		return new VoidNodeViewElement( "input" );
	}

	static VoidNodeViewElement keygen() {
		return new VoidNodeViewElement( "keygen" );
	}

	static VoidNodeViewElement link() {
		return new VoidNodeViewElement( "link" );
	}

	static VoidNodeViewElement meta() {
		return new VoidNodeViewElement( "meta" );
	}

	static VoidNodeViewElement param() {
		return new VoidNodeViewElement( "param" );
	}

	static VoidNodeViewElement source() {
		return new VoidNodeViewElement( "source" );
	}

	static VoidNodeViewElement track() {
		return new VoidNodeViewElement( "track" );
	}

	static VoidNodeViewElement wbr() {
		return new VoidNodeViewElement( "wbr" );
	}

	static NodeViewElement a( ViewElement... children ) {
		return createNode( "a", children );
	}

	static NodeViewElement abbr( ViewElement... children ) {
		return createNode( "abbr", children );
	}

	static NodeViewElement address( ViewElement... children ) {
		return createNode( "address", children );
	}

	static NodeViewElement article( ViewElement... children ) {
		return createNode( "article", children );
	}

	static NodeViewElement aside( ViewElement... children ) {
		return createNode( "aside", children );
	}

	static NodeViewElement audio( ViewElement... children ) {
		return createNode( "audio", children );
	}

	static NodeViewElement b( ViewElement... children ) {
		return createNode( "b", children );
	}

	static NodeViewElement bdi( ViewElement... children ) {
		return createNode( "bdi", children );
	}

	static NodeViewElement bdo( ViewElement... children ) {
		return createNode( "bdo", children );
	}

	static NodeViewElement blockquote( ViewElement... children ) {
		return createNode( "blockquote", children );
	}

	static NodeViewElement body( ViewElement... children ) {
		return createNode( "body", children );
	}

	static NodeViewElement button( ViewElement... children ) {
		return createNode( "button", children );
	}

	static NodeViewElement canvas( ViewElement... children ) {
		return createNode( "canvas", children );
	}

	static NodeViewElement caption( ViewElement... children ) {
		return createNode( "caption", children );
	}

	static NodeViewElement cite( ViewElement... children ) {
		return createNode( "cite", children );
	}

	static NodeViewElement code( ViewElement... children ) {
		return createNode( "code", children );
	}

	static NodeViewElement colgroup( ViewElement... children ) {
		return createNode( "colgroup", children );
	}

	static NodeViewElement datalist( ViewElement... children ) {
		return createNode( "datalist", children );
	}

	static NodeViewElement dd( ViewElement... children ) {
		return createNode( "dd", children );
	}

	static NodeViewElement del( ViewElement... children ) {
		return createNode( "del", children );
	}

	static NodeViewElement details( ViewElement... children ) {
		return createNode( "details", children );
	}

	static NodeViewElement dfn( ViewElement... children ) {
		return createNode( "dfn", children );
	}

	static NodeViewElement dialog( ViewElement... children ) {
		return createNode( "dialog", children );
	}

	static NodeViewElement div( ViewElement... children ) {
		return createNode( "div", children );
	}

	static NodeViewElement dl( ViewElement... children ) {
		return createNode( "dl", children );
	}

	static NodeViewElement dt( ViewElement... children ) {
		return createNode( "dt", children );
	}

	static NodeViewElement em( ViewElement... children ) {
		return createNode( "em", children );
	}

	static NodeViewElement fieldset( ViewElement... children ) {
		return createNode( "fieldset", children );
	}

	static NodeViewElement figcaption( ViewElement... children ) {
		return createNode( "figcaption", children );
	}

	static NodeViewElement figure( ViewElement... children ) {
		return createNode( "figure", children );
	}

	static NodeViewElement footer( ViewElement... children ) {
		return createNode( "footer", children );
	}

	static NodeViewElement form( ViewElement... children ) {
		return createNode( "form", children );
	}

	static NodeViewElement h1( ViewElement... children ) {
		return createNode( "h1", children );
	}

	static NodeViewElement h2( ViewElement... children ) {
		return createNode( "h2", children );
	}

	static NodeViewElement h3( ViewElement... children ) {
		return createNode( "h3", children );
	}

	static NodeViewElement h4( ViewElement... children ) {
		return createNode( "h4", children );
	}

	static NodeViewElement h5( ViewElement... children ) {
		return createNode( "h5", children );
	}

	static NodeViewElement h6( ViewElement... children ) {
		return createNode( "h6", children );
	}

	static NodeViewElement head( ViewElement... children ) {
		return createNode( "head", children );
	}

	static NodeViewElement header( ViewElement... children ) {
		return createNode( "header", children );
	}

	static NodeViewElement html( ViewElement... children ) {
		return createNode( "html", children );
	}

	static NodeViewElement i( ViewElement... children ) {
		return createNode( "i", children );
	}

	static NodeViewElement iframe( ViewElement... children ) {
		return createNode( "iframe", children );
	}

	static NodeViewElement ins( ViewElement... children ) {
		return createNode( "ins", children );
	}

	static NodeViewElement kbd( ViewElement... children ) {
		return createNode( "kbd", children );
	}

	static NodeViewElement label( ViewElement... children ) {
		return createNode( "label", children );
	}

	static NodeViewElement legend( ViewElement... children ) {
		return createNode( "legend", children );
	}

	static NodeViewElement li( ViewElement... children ) {
		return createNode( "li", children );
	}

	static NodeViewElement main( ViewElement... children ) {
		return createNode( "main", children );
	}

	static NodeViewElement map( ViewElement... children ) {
		return createNode( "map", children );
	}

	static NodeViewElement mark( ViewElement... children ) {
		return createNode( "mark", children );
	}

	static NodeViewElement menu( ViewElement... children ) {
		return createNode( "menu", children );
	}

	static NodeViewElement menuitem( ViewElement... children ) {
		return createNode( "menuitem", children );
	}

	static NodeViewElement meter( ViewElement... children ) {
		return createNode( "meter", children );
	}

	static NodeViewElement nav( ViewElement... children ) {
		return createNode( "nav", children );
	}

	static NodeViewElement noscript( ViewElement... children ) {
		return createNode( "noscript", children );
	}

	static NodeViewElement object( ViewElement... children ) {
		return createNode( "object", children );
	}

	static NodeViewElement ol( ViewElement... children ) {
		return createNode( "ol", children );
	}

	static NodeViewElement optgroup( ViewElement... children ) {
		return createNode( "optgroup", children );
	}

	static NodeViewElement option( ViewElement... children ) {
		return createNode( "option", children );
	}

	static NodeViewElement output( ViewElement... children ) {
		return createNode( "output", children );
	}

	static NodeViewElement p( ViewElement... children ) {
		return createNode( "p", children );
	}

	static NodeViewElement pre( ViewElement... children ) {
		return createNode( "pre", children );
	}

	static NodeViewElement progress( ViewElement... children ) {
		return createNode( "progress", children );
	}

	static NodeViewElement q( ViewElement... children ) {
		return createNode( "q", children );
	}

	static NodeViewElement rp( ViewElement... children ) {
		return createNode( "rp", children );
	}

	static NodeViewElement rt( ViewElement... children ) {
		return createNode( "rt", children );
	}

	static NodeViewElement ruby( ViewElement... children ) {
		return createNode( "ruby", children );
	}

	static NodeViewElement s( ViewElement... children ) {
		return createNode( "s", children );
	}

	static NodeViewElement samp( ViewElement... children ) {
		return createNode( "samp", children );
	}

	static NodeViewElement script( ViewElement... children ) {
		return createNode( "script", children );
	}

	static NodeViewElement section( ViewElement... children ) {
		return createNode( "section", children );
	}

	static NodeViewElement select( ViewElement... children ) {
		return createNode( "select", children );
	}

	static NodeViewElement small( ViewElement... children ) {
		return createNode( "small", children );
	}

	static NodeViewElement span( ViewElement... children ) {
		return createNode( "span", children );
	}

	static NodeViewElement strong( ViewElement... children ) {
		return createNode( "strong", children );
	}

	static NodeViewElement style( ViewElement... children ) {
		return createNode( "style", children );
	}

	static NodeViewElement sub( ViewElement... children ) {
		return createNode( "sub", children );
	}

	static NodeViewElement summary( ViewElement... children ) {
		return createNode( "summary", children );
	}

	static NodeViewElement sup( ViewElement... children ) {
		return createNode( "sup", children );
	}

	static NodeViewElement table( ViewElement... children ) {
		return createNode( "table", children );
	}

	static NodeViewElement tbody( ViewElement... children ) {
		return createNode( "tbody", children );
	}

	static NodeViewElement td( ViewElement... children ) {
		return createNode( "td", children );
	}

	static NodeViewElement textarea( ViewElement... children ) {
		return createNode( "textarea", children );
	}

	static NodeViewElement tfoot( ViewElement... children ) {
		return createNode( "tfoot", children );
	}

	static NodeViewElement th( ViewElement... children ) {
		return createNode( "th", children );
	}

	static NodeViewElement thead( ViewElement... children ) {
		return createNode( "thead", children );
	}

	static NodeViewElement time( ViewElement... children ) {
		return createNode( "time", children );
	}

	static NodeViewElement title( ViewElement... children ) {
		return createNode( "title", children );
	}

	static NodeViewElement tr( ViewElement... children ) {
		return createNode( "tr", children );
	}

	static NodeViewElement u( ViewElement... children ) {
		return createNode( "u", children );
	}

	static NodeViewElement ul( ViewElement... children ) {
		return createNode( "ul", children );
	}

	static NodeViewElement var( ViewElement... children ) {
		return createNode( "var", children );
	}

	static NodeViewElement video( ViewElement... children ) {
		return createNode( "video", children );
	}

	// -- end generated section

	static TextViewElement text( String text ) {
		return new TextViewElement( text );
	}

	static NodeViewElement createNode( String tagName, ViewElement... children ) {
		NodeViewElement node = new NodeViewElement( tagName );
		if ( children.length > 0 ) {
			node.addChildren( Arrays.asList( children ) );
		}

		return node;
	}
}
