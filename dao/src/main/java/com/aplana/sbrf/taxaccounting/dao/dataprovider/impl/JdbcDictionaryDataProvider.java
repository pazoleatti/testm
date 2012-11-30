package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

/**
 * Простая JDBC-реализация источника данных для справочников
 * @param <ValueType> тип значения, может быть BigDecimal или String
 * Сам этот класс является абстрактным
 * Использоваться должны отнаследованные от него классы: {@link StringDictionaryDataProvider} и 
 * {@link NumericDictionaryDataProvider}
 */
public abstract class JdbcDictionaryDataProvider<ValueType> extends AbstractDao implements DictionaryDataProvider<ValueType> {
	private class ItemRowMapper implements RowMapper<DictionaryItem<ValueType>> {
		@Override
		public DictionaryItem<ValueType> mapRow(ResultSet rs, int rowNum) throws SQLException {
			DictionaryItem<ValueType> item = new DictionaryItem<ValueType>();
			item.setName(rs.getString("name"));
			item.setValue(getValue(rs));
			return item;
		}
		
	}
	
	private String dictionaryName;
	private String sqlQuery;
	@Override
	public List<DictionaryItem<ValueType>> getValues(String valuePattern) {
		return getJdbcTemplate().query(
			"select * from (" + sqlQuery + ") where value like ?",
			new Object[] { valuePattern },
			new int[] { Types.VARCHAR },
			new ItemRowMapper()
		);
	}
	
	public DictionaryItem<ValueType> getItem(ValueType value) {
		try {
			return getJdbcTemplate().queryForObject(
				"select * from (" + sqlQuery + ") where value = ?",
				new Object[] { value },
				new ItemRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	public String getDictionaryName() {
		return dictionaryName;
	}
	public void setDictionaryName(String dictionaryName) {
		this.dictionaryName = dictionaryName;
	}
	public String getSqlQuery() {
		return sqlQuery;
	}
	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}
	public abstract ValueType getValue(ResultSet rs) throws SQLException;
}
