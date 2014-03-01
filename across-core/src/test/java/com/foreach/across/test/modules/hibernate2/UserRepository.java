package com.foreach.across.test.modules.hibernate2;

import com.foreach.across.test.modules.hibernate1.Product;

public interface UserRepository
{

	User getUserWithId( int id );

	void save( User user );

	void save( User user, Product product );
}
