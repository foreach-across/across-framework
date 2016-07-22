package com.foreach.across.modules.web.ui.elements.builder;

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ContainerViewElementBuilderSupport<T extends ContainerViewElement, SELF extends ContainerViewElementBuilderSupport<T, SELF>>
		extends ViewElementBuilderSupport<T, SELF>
{
	private final List<Object> children = new ArrayList<>();
	private String[] sortElements;

	@SuppressWarnings("unchecked")
	public SELF add( ViewElement... viewElements ) {
		Collections.addAll( children, viewElements );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF add( ViewElementBuilder... viewElements ) {
		Collections.addAll( children, viewElements );
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF addAll( Iterable<?> viewElements ) {
		for ( Object viewElement : viewElements ) {
			Assert.isTrue( viewElement instanceof ViewElement || viewElement instanceof ViewElementBuilder );
			children.add( viewElement );
		}
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	public SELF sort( String... elementNames ) {
		this.sortElements = elementNames;
		return (SELF) this;
	}

	@Override
	protected T apply( T viewElement, ViewElementBuilderContext builderContext ) {
		T container = super.apply( viewElement, builderContext );

		for ( Object child : children ) {
			if ( child != null ) {
				if ( child instanceof ViewElement ) {
					container.addChild( (ViewElement) child );
				}
				else {
					container.addChild( ( (ViewElementBuilder) child ).build( builderContext ) );
				}
			}
		}

		if ( sortElements != null ) {
			ContainerViewElementUtils.sortRecursively( container, sortElements );
		}

		return container;
	}
}
