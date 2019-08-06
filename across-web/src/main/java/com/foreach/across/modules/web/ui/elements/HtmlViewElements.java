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
 * Contains factory methods for HTML5 nodes as {@link ViewElement}.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
public interface HtmlViewElements
{
	// -- begin generated section

	default VoidNodeViewElement area() {
		return new VoidNodeViewElement( "area" );
	}

	default VoidNodeViewElement base() {
		return new VoidNodeViewElement( "base" );
	}

	default VoidNodeViewElement br() {
		return new VoidNodeViewElement( "br" );
	}

	default VoidNodeViewElement col() {
		return new VoidNodeViewElement( "col" );
	}

	default VoidNodeViewElement embed() {
		return new VoidNodeViewElement( "embed" );
	}

	default VoidNodeViewElement hr() {
		return new VoidNodeViewElement( "hr" );
	}

	default VoidNodeViewElement img() {
		return new VoidNodeViewElement( "img" );
	}

	default VoidNodeViewElement input() {
		return new VoidNodeViewElement( "input" );
	}

	default VoidNodeViewElement keygen() {
		return new VoidNodeViewElement( "keygen" );
	}

	default VoidNodeViewElement link() {
		return new VoidNodeViewElement( "link" );
	}

	default VoidNodeViewElement meta() {
		return new VoidNodeViewElement( "meta" );
	}

	default VoidNodeViewElement param() {
		return new VoidNodeViewElement( "param" );
	}

	default VoidNodeViewElement source() {
		return new VoidNodeViewElement( "source" );
	}

	default VoidNodeViewElement track() {
		return new VoidNodeViewElement( "track" );
	}

	default VoidNodeViewElement wbr() {
		return new VoidNodeViewElement( "wbr" );
	}

	default NodeViewElement a( ViewElement... children ) {
		return createNode( "a", children );
	}

	default NodeViewElement abbr( ViewElement... children ) {
		return createNode( "abbr", children );
	}

	default NodeViewElement address( ViewElement... children ) {
		return createNode( "address", children );
	}

	default NodeViewElement article( ViewElement... children ) {
		return createNode( "article", children );
	}

	default NodeViewElement aside( ViewElement... children ) {
		return createNode( "aside", children );
	}

	default NodeViewElement audio( ViewElement... children ) {
		return createNode( "audio", children );
	}

	default NodeViewElement b( ViewElement... children ) {
		return createNode( "b", children );
	}

	default NodeViewElement bdi( ViewElement... children ) {
		return createNode( "bdi", children );
	}

	default NodeViewElement bdo( ViewElement... children ) {
		return createNode( "bdo", children );
	}

	default NodeViewElement blockquote( ViewElement... children ) {
		return createNode( "blockquote", children );
	}

	default NodeViewElement body( ViewElement... children ) {
		return createNode( "body", children );
	}

	default NodeViewElement button( ViewElement... children ) {
		return createNode( "button", children );
	}

	default NodeViewElement canvas( ViewElement... children ) {
		return createNode( "canvas", children );
	}

	default NodeViewElement caption( ViewElement... children ) {
		return createNode( "caption", children );
	}

	default NodeViewElement cite( ViewElement... children ) {
		return createNode( "cite", children );
	}

	default NodeViewElement code( ViewElement... children ) {
		return createNode( "code", children );
	}

	default NodeViewElement colgroup( ViewElement... children ) {
		return createNode( "colgroup", children );
	}

	default NodeViewElement datalist( ViewElement... children ) {
		return createNode( "datalist", children );
	}

	default NodeViewElement dd( ViewElement... children ) {
		return createNode( "dd", children );
	}

	default NodeViewElement del( ViewElement... children ) {
		return createNode( "del", children );
	}

	default NodeViewElement details( ViewElement... children ) {
		return createNode( "details", children );
	}

	default NodeViewElement dfn( ViewElement... children ) {
		return createNode( "dfn", children );
	}

	default NodeViewElement dialog( ViewElement... children ) {
		return createNode( "dialog", children );
	}

	default NodeViewElement div( ViewElement... children ) {
		return createNode( "div", children );
	}

	default NodeViewElement dl( ViewElement... children ) {
		return createNode( "dl", children );
	}

	default NodeViewElement dt( ViewElement... children ) {
		return createNode( "dt", children );
	}

	default NodeViewElement em( ViewElement... children ) {
		return createNode( "em", children );
	}

	default NodeViewElement fieldset( ViewElement... children ) {
		return createNode( "fieldset", children );
	}

	default NodeViewElement figcaption( ViewElement... children ) {
		return createNode( "figcaption", children );
	}

	default NodeViewElement figure( ViewElement... children ) {
		return createNode( "figure", children );
	}

	default NodeViewElement footer( ViewElement... children ) {
		return createNode( "footer", children );
	}

	default NodeViewElement form( ViewElement... children ) {
		return createNode( "form", children );
	}

	default NodeViewElement h1( ViewElement... children ) {
		return createNode( "h1", children );
	}

	default NodeViewElement h2( ViewElement... children ) {
		return createNode( "h2", children );
	}

	default NodeViewElement h3( ViewElement... children ) {
		return createNode( "h3", children );
	}

	default NodeViewElement h4( ViewElement... children ) {
		return createNode( "h4", children );
	}

	default NodeViewElement h5( ViewElement... children ) {
		return createNode( "h5", children );
	}

	default NodeViewElement h6( ViewElement... children ) {
		return createNode( "h6", children );
	}

	default NodeViewElement head( ViewElement... children ) {
		return createNode( "head", children );
	}

	default NodeViewElement header( ViewElement... children ) {
		return createNode( "header", children );
	}

	default NodeViewElement html( ViewElement... children ) {
		return createNode( "html", children );
	}

	default NodeViewElement i( ViewElement... children ) {
		return createNode( "i", children );
	}

	default NodeViewElement iframe( ViewElement... children ) {
		return createNode( "iframe", children );
	}

	default NodeViewElement ins( ViewElement... children ) {
		return createNode( "ins", children );
	}

	default NodeViewElement kbd( ViewElement... children ) {
		return createNode( "kbd", children );
	}

	default NodeViewElement label( ViewElement... children ) {
		return createNode( "label", children );
	}

	default NodeViewElement legend( ViewElement... children ) {
		return createNode( "legend", children );
	}

	default NodeViewElement li( ViewElement... children ) {
		return createNode( "li", children );
	}

	default NodeViewElement main( ViewElement... children ) {
		return createNode( "main", children );
	}

	default NodeViewElement map( ViewElement... children ) {
		return createNode( "map", children );
	}

	default NodeViewElement mark( ViewElement... children ) {
		return createNode( "mark", children );
	}

	default NodeViewElement menu( ViewElement... children ) {
		return createNode( "menu", children );
	}

	default NodeViewElement menuitem( ViewElement... children ) {
		return createNode( "menuitem", children );
	}

	default NodeViewElement meter( ViewElement... children ) {
		return createNode( "meter", children );
	}

	default NodeViewElement nav( ViewElement... children ) {
		return createNode( "nav", children );
	}

	default NodeViewElement noscript( ViewElement... children ) {
		return createNode( "noscript", children );
	}

	default NodeViewElement object( ViewElement... children ) {
		return createNode( "object", children );
	}

	default NodeViewElement ol( ViewElement... children ) {
		return createNode( "ol", children );
	}

	default NodeViewElement optgroup( ViewElement... children ) {
		return createNode( "optgroup", children );
	}

	default NodeViewElement option( ViewElement... children ) {
		return createNode( "option", children );
	}

	default NodeViewElement output( ViewElement... children ) {
		return createNode( "output", children );
	}

	default NodeViewElement p( ViewElement... children ) {
		return createNode( "p", children );
	}

	default NodeViewElement pre( ViewElement... children ) {
		return createNode( "pre", children );
	}

	default NodeViewElement progress( ViewElement... children ) {
		return createNode( "progress", children );
	}

	default NodeViewElement q( ViewElement... children ) {
		return createNode( "q", children );
	}

	default NodeViewElement rp( ViewElement... children ) {
		return createNode( "rp", children );
	}

	default NodeViewElement rt( ViewElement... children ) {
		return createNode( "rt", children );
	}

	default NodeViewElement ruby( ViewElement... children ) {
		return createNode( "ruby", children );
	}

	default NodeViewElement s( ViewElement... children ) {
		return createNode( "s", children );
	}

	default NodeViewElement samp( ViewElement... children ) {
		return createNode( "samp", children );
	}

	default NodeViewElement script( ViewElement... children ) {
		return createNode( "script", children );
	}

	default NodeViewElement section( ViewElement... children ) {
		return createNode( "section", children );
	}

	default NodeViewElement select( ViewElement... children ) {
		return createNode( "select", children );
	}

	default NodeViewElement small( ViewElement... children ) {
		return createNode( "small", children );
	}

	default NodeViewElement span( ViewElement... children ) {
		return createNode( "span", children );
	}

	default NodeViewElement strong( ViewElement... children ) {
		return createNode( "strong", children );
	}

	default NodeViewElement style( ViewElement... children ) {
		return createNode( "style", children );
	}

	default NodeViewElement sub( ViewElement... children ) {
		return createNode( "sub", children );
	}

	default NodeViewElement summary( ViewElement... children ) {
		return createNode( "summary", children );
	}

	default NodeViewElement sup( ViewElement... children ) {
		return createNode( "sup", children );
	}

	default NodeViewElement table( ViewElement... children ) {
		return createNode( "table", children );
	}

	default NodeViewElement tbody( ViewElement... children ) {
		return createNode( "tbody", children );
	}

	default NodeViewElement td( ViewElement... children ) {
		return createNode( "td", children );
	}

	default NodeViewElement textarea( ViewElement... children ) {
		return createNode( "textarea", children );
	}

	default NodeViewElement tfoot( ViewElement... children ) {
		return createNode( "tfoot", children );
	}

	default NodeViewElement th( ViewElement... children ) {
		return createNode( "th", children );
	}

	default NodeViewElement thead( ViewElement... children ) {
		return createNode( "thead", children );
	}

	default NodeViewElement time( ViewElement... children ) {
		return createNode( "time", children );
	}

	default NodeViewElement title( ViewElement... children ) {
		return createNode( "title", children );
	}

	default NodeViewElement tr( ViewElement... children ) {
		return createNode( "tr", children );
	}

	default NodeViewElement u( ViewElement... children ) {
		return createNode( "u", children );
	}

	default NodeViewElement ul( ViewElement... children ) {
		return createNode( "ul", children );
	}

	default NodeViewElement var( ViewElement... children ) {
		return createNode( "var", children );
	}

	default NodeViewElement video( ViewElement... children ) {
		return createNode( "video", children );
	}

	// -- end generated section

	default TextViewElement text( String text ) {
		return new TextViewElement( text );
	}

	default NodeViewElement createNode( String tagName, ViewElement... children ) {
		NodeViewElement node = new NodeViewElement( tagName );
		if ( children.length > 0 ) {
			node.addChildren( Arrays.asList( children ) );
		}

		return node;
	}
}
