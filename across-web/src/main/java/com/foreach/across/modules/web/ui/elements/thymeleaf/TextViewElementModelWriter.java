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
package com.foreach.across.modules.web.ui.elements.thymeleaf;

import com.foreach.across.modules.web.thymeleaf.ThymeleafModelBuilder;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementModelWriter;
import org.apache.commons.lang3.StringUtils;

/**
 * Builds model for {@link TextViewElement}.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class TextViewElementModelWriter implements ViewElementModelWriter<TextViewElement>
{
	@Override
	public void writeModel( TextViewElement viewElement, ThymeleafModelBuilder model ) {
		model.addText( StringUtils.defaultString( viewElement.getText() ), viewElement.isEscapeXml() );
	}
}
