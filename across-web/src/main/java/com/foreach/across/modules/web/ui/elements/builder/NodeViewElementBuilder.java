package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import org.springframework.util.Assert;

public class NodeViewElementBuilder extends AbstractNodeViewElementBuilder<NodeViewElement, NodeViewElementBuilder>
{
	private String tagName;

	public NodeViewElementBuilder( String tagName ) {
		tagName( tagName );
	}

	public NodeViewElementBuilder tagName( String tagName ) {
		Assert.notNull( tagName );
		this.tagName = tagName;
		return this;
	}

	@Override
	protected NodeViewElement createElement( ViewElementBuilderContext builderContext ) {
		NodeViewElement element = new NodeViewElement( tagName );

		return apply( element, builderContext );
	}
}
