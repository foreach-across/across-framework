package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContainerViewElementBuilder extends ContainerViewElementBuilderSupport<ContainerViewElement, ContainerViewElementBuilder>
{
	private final List<ElementOrBuilder> children = new ArrayList<>();
	private String[] sortElements;

	@Override
	public ContainerViewElementBuilder add( ViewElement... viewElements ) {
		children.addAll( ElementOrBuilder.wrap( Arrays.asList( viewElements ) ) );
		return this;
	}

	@Override
	public ContainerViewElementBuilder add( ViewElementBuilder... viewElements ) {
		children.addAll( ElementOrBuilder.wrap( viewElements ) );
		return this;
	}

	@Override
	public ContainerViewElementBuilder addAll( Iterable<?> viewElements ) {
		children.addAll( ElementOrBuilder.wrap( viewElements ) );
		return this;
	}

	@Override
	public ContainerViewElementBuilder sort( String... elementNames ) {
		this.sortElements = elementNames;
		return this;
	}

	@Override
	protected ContainerViewElement createElement( ViewElementBuilderContext builderContext ) {
		ContainerViewElement container = new ContainerViewElement();

		for ( ElementOrBuilder child : children ) {
			container.addChild( child.get( builderContext ) );
		}

		if ( sortElements != null ) {
			ContainerViewElementUtils.sortRecursively( container, sortElements );
		}

		return apply( container, builderContext );
	}
}
