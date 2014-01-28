package com.foreach.across.modules.web.resource;

/**
 * <p>Represents a single entry in the WebResourceRegistry.</p>
 * <p>All constants are deliberately Strings so they can easily be used in different view
 * layers and custom values can be added in other modules.</p>
 */
public class WebResource
{
	/**
	 * Default type of web resources.
	 */
	public static final String CSS = "css";
	public static final String JAVASCRIPT = "javascript";

	/**
	 * Used for data that should be serialized and passed to the client (usually as json).
	 */
	public static final String DATA = "data";

	/**
	 * Inline resource - entire content
	 */
	public static final String INLINE = "inline";

	/**
	 * External resource - usually an absolute link
	 */
	public static final String EXTERNAL = "external";

	/**
	 * Relative to the context/controller being rendered - this is usually the default.
	 */
	public static final String RELATIVE = "relative";

	/**
	 * Embedded resource in the views directory - these usually get translated into a path using a
	 * {@link com.foreach.across.modules.web.resource.WebResourceTranslator}.
	 */
	public static final String VIEWS = "views";

	private String key, type, location;
	private Object data;

	public WebResource( String key, String type, String location, Object data ) {
		this.key = key;
		this.type = type;
		this.location = location;
		this.data = data;
	}

	/**
	 * @return Key this resource is registered under.  Can be null or empty.
	 */
	public String getKey() {
		return key;
	}

	public void setKey( String key ) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType( String type ) {
		this.type = type;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation( String location ) {
		this.location = location;
	}

	public Object getData() {
		return data;
	}

	public void setData( Object data ) {
		this.data = data;
	}

	public boolean hasKey() {
		return key != null;
	}
}
