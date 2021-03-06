package com.iv.jpa.util.hibernate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.c3p0.internal.C3P0ConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.Stoppable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMultiTenantConnectionProvider
		implements MultiTenantConnectionProvider, Stoppable, Configurable, ServiceRegistryAwareService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1357388684561200949L;
	//private final DriverManagerConnectionProviderImpl connectionProvider = new DriverManagerConnectionProviderImpl();
	private static Logger logger = LoggerFactory.getLogger(AbstractMultiTenantConnectionProvider.class);
	protected abstract C3P0ConnectionProvider getConnectionProvider();
	
	@Override
	public boolean isUnwrappableAs(Class unwrapType) {
		return getConnectionProvider().isUnwrappableAs(unwrapType);
	}

	@Override
	public <T> T unwrap(Class<T> unwrapType) {
		return getConnectionProvider().unwrap(unwrapType);
	}

	@Override
	public Connection getAnyConnection() throws SQLException {
		return getConnectionProvider().getConnection();
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		getConnectionProvider().closeConnection(connection);
	}

	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		final Connection connection = getAnyConnection();
		useDatabase(tenantIdentifier, connection);
		return connection;
	}

	/*@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		try {
			connection.createStatement().execute("use iv_ops_shared");
		} catch (SQLException e) {
			throw new HibernateException(
					"Could not alter JDBC connection to specified schema [" + tenantIdentifier + "]", e);
		}
		connectionProvider.closeConnection(connection);
	}*/

	private void useDatabase(String dbName, Connection connection) {
		try {
			PreparedStatement ps = connection.prepareStatement(
					"SELECT count(sc.SCHEMA_NAME) FROM information_schema.SCHEMATA sc where sc.SCHEMA_NAME=?");
			ps.setString(1, dbName);
			ResultSet rs = ps.executeQuery();
			boolean exist = true;
			if (rs.next()) {
				if (rs.getInt(1) == 0) {
					// 创建db
					connection.createStatement().execute(
							"create database " + dbName + " DEFAULT character set utf8 COLLATE utf8_general_ci");
					exist = false;
				}
			}
			connection.createStatement().execute("use " + dbName);

			// 初始化数据库表
			if (!exist) {
				// 该方式无法加载jar包中的文件
				// ClassPathResource cpr = new ClassPathResource("init_table.sql");
				try {
					InputStream stream = null;
					stream  = getClass().getClassLoader().getResourceAsStream(getExcuteScriptName(dbName));
					
					/*if (ConstantContainer.KNOWLEDGE_DB_ID.equals(dbName)) {
						// 初始化知识库表
						stream = getClass().getClassLoader().getResourceAsStream("iv_ops_knowledge.sql");
					} else {
						stream = getClass().getClassLoader().getResourceAsStream("init_table.sql");
					}*/
					// File file = cpr.getFile();
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
					String line;
					StringBuilder stringBuilder = new StringBuilder();
					while ((line = reader.readLine()) != null) {

						/*if (line.startsWith("--") || line.startsWith("#")) {
							continue;// 注解行
						}*/
						line = line.trim();
						if (!"".equals(line) && !line.startsWith("--")) {
							stringBuilder.append(line);
						} else {
							logger.info("执行sql:{}", stringBuilder.toString());
							connection.createStatement().execute(stringBuilder.toString());
							stringBuilder.setLength(0);
						}
					}
					reader.close();
				} catch (Exception e) {
					logger.error("初始化数据库异常", e);
				}
			}

		} catch (SQLException e) {
			logger.error("sql异常", e);
			throw new HibernateException("Could not alter JDBC connection to specified schema [" + dbName + "]", e);
		}
	}
	
	/**
	 * 获取需要执行的sql脚本文件名
	 * @return
	 */
	public abstract String getExcuteScriptName(String dbName);

	@Override
	public boolean supportsAggressiveRelease() {
		return getConnectionProvider().supportsAggressiveRelease();
	}

	@Override
	public void injectServices(ServiceRegistryImplementor serviceRegistry) {
		getConnectionProvider().injectServices(serviceRegistry);
	}

	@Override
	public void configure(Map configurationValues) {
		getConnectionProvider().configure(configurationValues);
	}

	@Override
	public void stop() {
		getConnectionProvider().stop();
	}
	
}
