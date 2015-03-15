package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContainerViewElementBuilder extends ContainerViewElementBuilderSupport<ContainerViewElement, ContainerViewElementBuilder>
{
	private final List<Object> children = new ArrayList<>();
	private String[] sortElements;

	@Override
	public ContainerViewElementBuilder name( String name ) {
		return super.name( name );
	}

	@Override
	public ContainerViewElementBuilder customTemplate( String template ) {
		return super.customTemplate( template );
	}

	public ContainerViewElementBuilder add( ViewElement... viewElements ) {
		Collections.addAll( children, viewElements );
		return this;
	}

	public ContainerViewElementBuilder add( ViewElementBuilder... viewElements ) {
		Collections.addAll( children, viewElements );
		return this;
	}

	public ContainerViewElementBuilder sort( String... elementNames ) {
		this.sortElements = elementNames;
		return this;
	}

	@Override
	public ContainerViewElement build( ViewElementBuilderContext builderContext ) {
		ContainerViewElement container = new ContainerViewElement();

		for ( Object child : children ) {
			if ( child instanceof ViewElement ) {
				container.add( (ViewElement) child );
			}
			else {
				container.add( ( (ViewElementBuilder) child ).build( builderContext ) );
			}
		}

		if ( sortElements != null ) {
			container.sort( sortElements );
		}

		return apply( container );
	}
}
