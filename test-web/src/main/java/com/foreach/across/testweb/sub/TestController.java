package com.foreach.across.testweb.sub;

import com.foreach.across.core.annotations.Refreshable;
import com.foreach.across.testweb.other.CacheableServiceOne;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Refreshable
public class TestController
{
	@Autowired(required = false)
	private CacheableServiceOne serviceOne;

	@Autowired(required = false)
	private CacheableServiceTwo serviceTwo;

	@RequestMapping("/")
	public String hello( Model model ) {
		model.addAttribute( "oneCached", serviceOne.getNumber() );
		model.addAttribute( "oneNotCached", serviceOne.getNumberNotCached() );
		model.addAttribute( "oneWithId", serviceOne.getNumberWithId( 1 ) );
		model.addAttribute( "twoCached", serviceTwo.getOtherNumber() );
		model.addAttribute( "twoNotCached", serviceTwo.getOtherNumberNotCached() );
		model.addAttribute( "twoWithId", serviceOne.getNumberWithId( 2 ) );

		return "sub/test";
	}
}
