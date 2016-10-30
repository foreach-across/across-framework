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

import com.foreach.across.modules.web.thymeleaf.ProcessableModel;
import com.foreach.across.modules.web.thymeleaf.ThymeleafModelBuilder;
import com.foreach.across.modules.web.thymeleaf.ViewElementNodeFactory;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementThymeleafBuilder;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;

public class ContainerViewElementThymeleafBuilder implements ViewElementThymeleafBuilder<ContainerViewElement>
{
	@Override
	public ProcessableModel buildModel( ContainerViewElement container,
	                                    ITemplateContext context,
	                                    ViewElementNodeFactory componentElementProcessor ) {
		IModel model = context.getModelFactory().createModel();
		container.getChildren().forEach(
				c -> model.addModel( componentElementProcessor.buildModel( c, context ).getModel() ) );
		return new ProcessableModel( model, true );
	}

	@Override
	public void writeModel( ContainerViewElement container, ThymeleafModelBuilder writer ) {
		container.getChildren().forEach( writer::addViewElement );
	}
}
