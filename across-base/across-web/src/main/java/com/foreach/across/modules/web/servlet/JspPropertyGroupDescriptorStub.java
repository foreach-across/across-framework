/*
 * Copyright 2019 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.across.modules.web.servlet;

import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import java.util.Collection;
import java.util.Collections;

/**
 * This is a stub implementation for the {@link javax.servlet.descriptor.JspPropertyGroupDescriptor} from the servlet 3.0 spec.
 * This simplifies configuring your own jsp-property-group in servlet configuration by providing sane defaults.
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
