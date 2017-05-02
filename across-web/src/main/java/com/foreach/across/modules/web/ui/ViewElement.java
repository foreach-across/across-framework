package com.foreach.across.modules.web.ui;

/**
 * Represents a {@link ViewElement} in its most simple form.  In a web context this is almost certainly
 * a HTML node or collection thereof.
 */
public interface ViewElement
{
	/**
	 * A ViewElement can have an internal name that identifies it within a
	 * {@link com.foreach.across.modules.web.ui.elements.ContainerViewElement}.  A name is optional but when given,
	 * is preferably unique within its container as most operations work on the first element with a specific name.
	 *
	 * @return Internal name of this element, can be null.
	 * @see com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils
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
