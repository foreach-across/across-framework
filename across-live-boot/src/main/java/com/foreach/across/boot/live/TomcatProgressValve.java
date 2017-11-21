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
package com.foreach.across.boot.live;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Custom valve for live bootstrap feedback.
 *
 * @author Arne Vandamme
 * @since 3.0.0
 */
@Slf4j
public class TomcatProgressValve extends ValveBase
{
	public TomcatProgressValve() {
		super( true );
/*		ProgressBeanPostProcessor.observe().subscribe(
				beanName -> log.trace("Bean found: {}", beanName),
				t -> log.error("Failed", t),
				this::removeMyself);*/
	}

	public void removeMyself() {
		LOG.debug( "Application started, de-registering" );
		getContainer().getPipeline().removeValve( this );
	}

	@Override
	public void invoke( Request request, Response response ) throws IOException, ServletException {
		switch ( request.getRequestURI() ) {
			case "/init.stream":
				//final AsyncContext asyncContext = request.startAsync();
				//streamProgress(asyncContext);
				break;
			case "/health":
			case "/info":
				response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
				break;
			default:
				sendHtml( response, "/loading.html" );
		}
	}
/*
	private void streamProgress(AsyncContext asyncContext) throws IOException {
		final ServletResponse resp = asyncContext.getResponse();
		resp.setContentType("text/event-stream");
		resp.setCharacterEncoding("UTF-8");
		resp.flushBuffer();
		final Subscription subscription = ProgressBeanPostProcessor.observe()
		                                                           .map(beanName -> "data: " + beanName)
		                                                           .subscribeOn(Schedulers.io())
		                                                           .subscribe(
				                                                           event -> stream(event, asyncContext.getResponse()),
				                                                           e -> log.error("Error in observe()", e),
				                                                           () -> complete(asyncContext)
		                                                           );
		unsubscribeOnDisconnect(asyncContext, subscription);
	}

	private void complete(AsyncContext asyncContext) {
		stream("event: complete\ndata:", asyncContext.getResponse());
		asyncContext.complete();
	}

	private void unsubscribeOnDisconnect(AsyncContext asyncContext, final Subscription subscription) {
		asyncContext.addListener(new AsyncListener() {
			@Override
			public void onComplete(AsyncEvent event) throws IOException {
				log.info("onComplete()");
				subscription.unsubscribe();
			}

			@Override
			public void onTimeout(AsyncEvent event) throws IOException {
				log.info("onTimeout()");
				subscription.unsubscribe();
			}

			@Override
			public void onError(AsyncEvent event) throws IOException {
				log.info("onError()");
				subscription.unsubscribe();
			}

			@Override
			public void onStartAsync(AsyncEvent event) throws IOException {
			}
		});
	}

	private void stream(String event, ServletResponse response) {
		try {
			final PrintWriter writer = response.getWriter();
			writer.println(event);
			writer.println();
			writer.flush();
		} catch (IOException e) {
			log.warn("Failed to stream", e);
		}
	}
	*/

	private void sendHtml( Response response, String name ) throws IOException {
		try (InputStream loadingHtml = getClass().getResourceAsStream( name )) {
			IOUtils.copy( loadingHtml, response.getOutputStream() );
		}
	}
}

