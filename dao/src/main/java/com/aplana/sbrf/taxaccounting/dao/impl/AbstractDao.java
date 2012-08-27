package com.aplana.sbrf.taxaccounting.dao.impl;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Абстрактный класс, позволяющий делать автовайринг с DataSource'ом
 * и содержащий вспомогательные методы, востребованные насле
 */
public abstract class AbstractDao extends JdbcDaoSupport {
	@Autowired
	protected void setAutowiredDataSource(DataSource ds) {
		setDataSource(ds);
	}
	
	protected <T extends Number> T generateId(String sequenceName, Class<T> resultType) {
		return getJdbcTemplate().queryForObject("values (nextval for " + sequenceName + ")", resultType);
	}
}
