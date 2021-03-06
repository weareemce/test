package com.iv.aggregation.util;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.c3p0.internal.C3P0ConnectionProvider;

import com.iv.jpa.util.hibernate.AbstractMultiTenantConnectionProvider;


public class MultiTenantConnectionProviderImpl extends AbstractMultiTenantConnectionProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3565754320432761251L;

	private static final C3P0ConnectionProvider connectionProvider = new C3P0ConnectionProvider();

	@Override
	protected C3P0ConnectionProvider getConnectionProvider() {
		return connectionProvider;
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		
		connectionProvider.closeConnection(connection);

	}

	@Override
	public String getExcuteScriptName(String dbName) {
		return "init_table.sql";
	}

}
