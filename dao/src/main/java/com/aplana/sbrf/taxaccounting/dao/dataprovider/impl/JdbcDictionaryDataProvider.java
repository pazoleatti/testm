package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.DictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

/**
 * Простая JDBC-реализация источника данных для справочников
 *
 * @param <ValueType> тип значения, может быть BigDecimal или String
 *                    Сам этот класс является абстрактным
 *                    Использоваться должны отнаследованные от него классы: {@link StringDictionaryDataProvider} и
 *                    {@link NumericDictionaryDataProvider}
 */
public abstract class JdbcDictionaryDataProvider<ValueType extends Serializable> extends AbstractDao implements DictionaryDataProvider<ValueType> {

	private static final String EMPTY_PATTERN = "%";

	protected class ItemRowMapper implements RowMapper<DictionaryItem<ValueType>> {
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

	/**
	 * Возвращает все значения из справочника.
	 *
	 * @return список значений справочника
	 */
	@Override
	public List<DictionaryItem<ValueType>> getValues() {
		return getJdbcTemplate().query(getSqlQuery(), new ItemRowMapper());
	}

	/**
	 * Возвращает запись св справочнике по значению. Запись может содержать так же и название значения.
	 * Т.е. кроме кода ОКАТО, например, ещё и его название.
	 *
	 * @param value значение из справочника
	 * @return запись в справочнике
	 */
	@Override
	public DictionaryItem<ValueType> getItem(ValueType value) {
		try {
			return getJdbcTemplate().queryForObject(
					"select * from (" + sqlQuery + ") where value = ?",
					new Object[]{value},
					new ItemRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
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

	/**
	 * Получает значение справочника из {@link ResultSet}
	 *
	 * @param rs result set
	 * @return значение справочника
	 * @throws SQLException например, если в ответе не окажется нужного поля
	 */
	protected abstract ValueType getValue(ResultSet rs) throws SQLException;

	/**
	 * Возвращает количество записей соответствующих фильтру
	 * @param pattern фильтр
	 * @return количество записей
	 */
	protected abstract int getRowCount(String pattern);

	/**
	 * Подготавливает паттерн поиска. Обрезает со всех сторон. Эскейпит всякие символы. Обрамляет в %.
	 *
	 * @param pattern исходный паттерн поиска
	 * @return паттерн поиска, готовый к использованию в БД
	 */
	protected String preparePattern(String pattern) {
		if (pattern == null) {
			return EMPTY_PATTERN;
		}

		String prepared = pattern.trim();

		if (prepared.isEmpty()) {
			return EMPTY_PATTERN;
		}

		prepared = prepared.toLowerCase();

		prepared = prepared.replaceAll("\\\\", "\\\\");
		prepared = prepared.replaceAll("%", "\\%");
		prepared = prepared.replaceAll("_", "\\_");
		prepared = '%' + prepared + '%';

		return prepared;
	}


}
