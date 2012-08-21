package com.aplana.sbrf.taxaccounting.dao.impl;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Абстрактный класс, позволяющий делать автовайринг с DataSource'ом
 */
public abstract class AbstractDao extends JdbcDaoSupport {
	@Autowired
	protected void setAutowiredDataSource(DataSource ds) {
		setDataSource(ds);
	}
}
