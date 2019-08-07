/*
 * Copyright 2014 the original author or authors
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
package com.foreach.across.test;

import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import javax.servlet.descriptor.TaglibDescriptor;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * @author Marc Vanbrabant
 * @since 1.1.2
 */
public class MockJspConfigDescriptor implements JspConfigDescriptor
{
	private Collection<JspPropertyGroupDescriptor> jspPropertyGroups =
			new LinkedHashSet<>();

	private Collection<TaglibDescriptor> taglibs =
			new HashSet<>();

	@Override
	public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
		return jspPropertyGroups;
	}

	@Override
	public Collection<TaglibDescriptor> getTaglibs() {
		return taglibs;
	}
}
