package com.aplana.sbrf.taxaccounting.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Абстрактный класс, позволяющий делать автовайринг с DataSource'ом
 * и содержащий вспомогательные методы, востребованные наследниками
 */
public abstract class AbstractDao {
	protected Log logger = LogFactory.getLog(getClass());

    @Autowired
    DBInfo dbInfo;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	protected JdbcTemplate getJdbcTemplate() {
		return (JdbcTemplate)namedParameterJdbcTemplate.getJdbcOperations();
	}
	
	protected NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return namedParameterJdbcTemplate;
	}
	
	protected <T extends Number> T generateId(String sequenceName, Class<T> resultType) {
		return getJdbcTemplate().queryForObject("select " + sequenceName + ".nextval from dual", resultType);
	}

    protected boolean isSupportOver(){
        return dbInfo.isSupportOver();
    }
}
