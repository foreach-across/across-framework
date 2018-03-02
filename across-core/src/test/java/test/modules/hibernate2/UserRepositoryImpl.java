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

package test.modules.hibernate2;

import test.modules.hibernate1.Product;
import test.modules.hibernate1.ProductRepository;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRepositoryImpl implements UserRepository
{

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private ProductRepository productRepository;

	@Transactional(readOnly = true)
	public User getUserWithId( int id ) {
		return (User) sessionFactory.getCurrentSession().byId( User.class ).load( id );
	}

	@Transactional
	public void save( User user ) {
		sessionFactory.getCurrentSession().saveOrUpdate( user );
	}

	@Transactional
	public void save( User user, Product product ) {
		productRepository.save( product );

		if ( user != null ) {
			save( user );
		}
		else {
			throw new RuntimeException( "rollback transaction" );
		}
	}
}
