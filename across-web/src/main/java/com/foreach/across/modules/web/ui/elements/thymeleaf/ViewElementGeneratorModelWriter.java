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

import com.foreach.across.modules.web.thymeleaf.HtmlIdStore;
import com.foreach.across.modules.web.thymeleaf.ThymeleafModelBuilder;
import com.foreach.across.modules.web.ui.elements.ViewElementGenerator;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementModelWriter;

/**
 * Builds model for a {@link ViewElementGenerator}, which in turns builds a collection of elements.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class ViewElementGeneratorModelWriter implements ViewElementModelWriter<ViewElementGenerator<?, ?>>
{
	@Override
	public void writeModel( ViewElementGenerator<?, ?> generator, ThymeleafModelBuilder model ) {
		HtmlIdStore idStore = HtmlIdStore.fetch( model.getTemplateContext() );

		generator.forEach( child -> {
			if ( child != null ) {
				if ( !generator.isBuilderItemTemplate() ) {
					idStore.increaseLevel();

					try {
						model.addViewElement( child );
					}
					finally {
						idStore.decreaseLevel();
					}
				}
				else {
					model.addViewElement( child );
				}
			}
		} );
	}
}
