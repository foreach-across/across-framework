package com.foreach.across.test.modules.hibernate1;

public interface ProductRepository {

    Product getProductWithId( int id );

    void save( Product product );
}
