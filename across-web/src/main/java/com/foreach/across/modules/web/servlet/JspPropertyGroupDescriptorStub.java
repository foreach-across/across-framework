package com.foreach.across.modules.web.servlet;

import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import java.util.Collection;
import java.util.Collections;

/**
 * This is a stub implementation for the {@link javax.servlet.descriptor.JspPropertyGroupDescriptor} from the servlet 3.0 spec.
 * This simplifies configuring your own jsp-property-group in a {@link com.foreach.across.modules.web.servlet.AcrossWebDynamicServletConfigurer} by providing sane defaults.
 *
 * @author niels
 * @since 23/10/2014
 */
public abstract class JspPropertyGroupDescriptorStub implements JspPropertyGroupDescriptor
{
	@Override
	public Collection<String> getUrlPatterns() {
		return Collections.emptyList();
	}

	@Override
	public String getElIgnored() {
		return null;
	}

	@Override
	public String getPageEncoding() {
		return null;
	}

	@Override
	public String getScriptingInvalid() {
		return null;
	}

	@Override
	public String getIsXml() {
		return null;
	}

	@Override
	public Collection<String> getIncludePreludes() {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getIncludeCodas() {
		return Collections.emptyList();
	}

	@Override
	public String getDeferredSyntaxAllowedAsLiteral() {
		return null;
	}

	@Override
	public String getTrimDirectiveWhitespaces() {
		return null;
	}

	@Override
	public String getDefaultContentType() {
		return null;
	}

	@Override
	public String getBuffer() {
		return null;
	}

	@Override
	public String getErrorOnUndeclaredNamespace() {
		return null;
	}
}
