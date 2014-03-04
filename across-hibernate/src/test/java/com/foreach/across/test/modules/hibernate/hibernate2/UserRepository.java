package com.foreach.across.test.modules.hibernate.hibernate2;

import com.foreach.across.test.modules.hibernate.hibernate1.Product;

public interface UserRepository
{

	User getUserWithId( int id );

	void save( User user );

	void save( User user, Product product );
}
