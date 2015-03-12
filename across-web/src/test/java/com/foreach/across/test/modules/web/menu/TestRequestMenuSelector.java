package com.foreach.across.test.modules.web.menu;

import com.foreach.across.modules.web.menu.Menu;
import com.foreach.across.modules.web.menu.RequestMenuSelector;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestRequestMenuSelector
{
	@Test
	public void selectLongestPrefix() {
		Menu entities = new Menu( "/entities", "Entities" );
		entities.addItem( "/entities/sector", "Sector" )
		        .addItem( "/entities/sector/create", "Create a new sector" );
		entities.addItem( "/entities/sectorRepresentative", "Sector representative" )
		        .addItem( "/entities/sectorRepresentative/create", "Create a sector representative" );

		Menu menu = new Menu();
		menu.addItem( entities );

		RequestMenuSelector selector = new RequestMenuSelector(
				"http://localhost:8103/sector/entities/sectorRepresentative/-100/update",
				"/entities/sectorRepresentative/-100/update",
				"/entities/sectorRepresentative/-100/update?from=goback"
		);

		Menu selected = selector.find( menu );
		assertEquals( "/entities/sectorRepresentative", selected.getPath() );
	}
}
