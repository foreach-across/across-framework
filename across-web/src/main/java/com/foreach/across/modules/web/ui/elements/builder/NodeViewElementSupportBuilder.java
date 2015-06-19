package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.NodeViewElementSupport;

import java.util.HashMap;
import java.util.Map;

public abstract class NodeViewElementSupportBuilder<T extends NodeViewElementSupport, SELF extends NodeViewElementSupportBuilder<T, SELF>>
		extends ContainerViewElementBuilderSupport<T, SELF>
{
	private String htmlId;
	private Map<String, Object> attributes = new HashMap<>();

	@SuppressWarnings("unchecked")
	public SELF htmlId( String htmlId ) {
		this.htmlId = htmlId;
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF attribute( String name, Object value ) {
		attributes.put( name, value );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF attributes( Map<String, Object> attributes ) {
		this.attributes.putAll( attributes );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF removeAttribute( String name ) {
		attributes.put( name, null );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF clearAttributes() {
		attributes.clear();
		return (SELF) this;
	}

	@Override
	protected T apply( T viewElement, ViewElementBuilderContext builderContext ) {
		T element = super.apply( viewElement, builderContext );

		if ( htmlId != null ) {
			element.setHtmlId( htmlId );
		}

		for ( Map.Entry<String, Object> attribute : attributes.entrySet() ) {
			element.setAttribute( attribute.getKey(), attribute.getValue() );
		}

		return element;
	}
}
