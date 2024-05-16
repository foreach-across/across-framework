package com.foreach.across.modules.web.servlet;

import jakarta.annotation.PreDestroy;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;

public class AcrossMultipartFilter extends MultipartFilter
{
	private MultipartResolver multipartResolver;

	public AcrossMultipartFilter( MultipartResolver multipartResolver ) {
		this.multipartResolver = multipartResolver;
	}

	public void setMultipartResolver( MultipartResolver multipartResolver ) {
		this.multipartResolver = multipartResolver;
	}

	@Override
	protected MultipartResolver lookupMultipartResolver() {
		return multipartResolver;
	}

	@PreDestroy
	public void close() {
		multipartResolver = null;
	}
}
