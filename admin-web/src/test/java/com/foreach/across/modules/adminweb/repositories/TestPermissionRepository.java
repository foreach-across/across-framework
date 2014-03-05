package com.foreach.across.modules.adminweb.repositories;

import com.foreach.across.modules.adminweb.TestDatabaseConfig;
import com.foreach.across.modules.adminweb.business.Permission;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestPermissionRepository.Config.class)
@DirtiesContext
public class TestPermissionRepository
{
	@Autowired
	private PermissionRepository permissionRepository;

	@Autowired
	private SessionFactory sessionFactory;

	@Before
	public void openSession() {
		Session session = sessionFactory.openSession();
		TransactionSynchronizationManager.bindResource( sessionFactory, new SessionHolder( session ) );
	}

	@After
	public void closeSession() {
		sessionFactory.getCurrentSession().flush();
		SessionFactoryUtils.closeSession( sessionFactory.getCurrentSession() );
		TransactionSynchronizationManager.unbindResource( sessionFactory );
	}

	@Test
	public void notExistingPermission() {
		Permission existing = permissionRepository.getPermission( "djsklfjdskds" );

		assertNull( existing );
	}

	@Test
	public void saveAndGetPermission() {
		Permission manageUsers = new Permission( "manage users" );
		permissionRepository.save( manageUsers );

		assertTrue( manageUsers.getId() > 0 );

		Permission existing = permissionRepository.getPermission( "manage users" );
		assertEquals( manageUsers, existing );
		assertEquals( manageUsers.getId(), existing.getId() );

		permissionRepository.delete( existing );

		existing = permissionRepository.getPermission( "manage users" );
		assertNull( existing );
	}

	@Configuration
	@Import(TestDatabaseConfig.class)
	static class Config
	{
		@Bean
		public PermissionRepository permissionRepository() {
			return new PermissionRepositoryImpl();
		}
	}
}
