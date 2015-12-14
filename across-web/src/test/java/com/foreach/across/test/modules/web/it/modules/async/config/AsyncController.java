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
package com.foreach.across.test.modules.web.it.modules.async.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.Callable;

/**
 * @author Arne Vandamme
 */
@Controller
public class AsyncController
{
	@ResponseBody
	@RequestMapping("/callable")
	public Callable<String> callable( final @RequestParam("msg") String message ) {
		return () -> "callable:" + message;
	}

	@ResponseBody
	@RequestMapping("/deferredResult")
	public DeferredResult<String> deferredResult( final @RequestParam("msg") String message ) {
		final DeferredResult<String> result = new DeferredResult<>();

		new Thread( () -> {
			try {
				Thread.sleep( 500 );
				result.setResult( "deferred:" + message );
			}
			catch ( InterruptedException e ) {
				e.printStackTrace();
			}

		} ).start();

		return result;
	}
}
