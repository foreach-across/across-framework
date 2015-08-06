package com.foreach.across.modules.web.ui;

/**
 * Represents a {@link ViewElement} in its most simple form.  In a web context this is almost certainly
 * a HTML node or collection thereof.
 */
public interface ViewElement
{
	/**
	 * A ViewElement can have an internal name that should preferably be unique within a
	 * {@link com.foreach.across.modules.web.ui.ViewElements} collection.  A name is optional however.
	 *
	 * @return Internal name of this element, can be null.
	 */
	String getName();

	/**
	 * @return Type id of this view element.
	 */
	String getElementType();

	/**
	 * @return Custom template to use when rendering this view element.
	 */
	String getCustomTemplate();
}
