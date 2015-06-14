package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementPostProcessor;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import org.springframework.util.Assert;

import java.util.Map;

public class NodeViewElementBuilder extends NodeViewElementSupportBuilder<NodeViewElement, NodeViewElementBuilder>
{
	private String tagName;

	public NodeViewElementBuilder tagName( String tagName ) {
		Assert.notNull( tagName );
		this.tagName = tagName;
		return this;
	}

	@Override
	public NodeViewElementBuilder htmlId( String htmlId ) {
		return super.htmlId( htmlId );
	}

	@Override
	public NodeViewElementBuilder attribute( String name, Object value ) {
		return super.attribute( name, value );
	}

	@Override
	public NodeViewElementBuilder attributes( Map<String, Object> attributes ) {
		return super.attributes( attributes );
	}

	@Override
	public NodeViewElementBuilder removeAttribute( String name ) {
		return super.removeAttribute( name );
	}

	@Override
	public NodeViewElementBuilder clearAttributes() {
		return super.clearAttributes();
	}

	@Override
	public NodeViewElementBuilder add( ViewElement... viewElements ) {
		return super.add( viewElements );
	}

	@Override
	public NodeViewElementBuilder add( ViewElementBuilder... viewElements ) {
		return super.add( viewElements );
	}

	@Override
	public NodeViewElementBuilder addAll( Iterable<?> viewElements ) {
		return super.addAll( viewElements );
	}

	@Override
	public NodeViewElementBuilder sort( String... elementNames ) {
		return super.sort( elementNames );
	}

	@Override
	public NodeViewElementBuilder name( String name ) {
		return super.name( name );
	}

	@Override
	public NodeViewElementBuilder customTemplate( String template ) {
		return super.customTemplate( template );
	}

	@Override
	public NodeViewElementBuilder postProcessor( ViewElementPostProcessor<NodeViewElement> postProcessor ) {
		return super.postProcessor( postProcessor );
	}

	@Override
	protected NodeViewElement createElement( ViewElementBuilderContext builderContext ) {
		NodeViewElement element = new NodeViewElement();
		element.setTagName( tagName );

		return apply( element, builderContext );
	}
}
