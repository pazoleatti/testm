package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Источник данных для числовых справочников. Значениями в которых, являются числа
 */
public class NumericDictionaryDataProvider extends JdbcDictionaryDataProvider<BigDecimal> {
	/**
	 * Получает значение справочника из {@link ResultSet}
	 *
	 * @param rs result set
	 * @return значение справочника
	 * @throws SQLException например, если в ответе не окажется нужного поля
	 */
	@Override
	public BigDecimal getValue(ResultSet rs) throws SQLException {
		return rs.getBigDecimal("value");
	}

	/**
	 * Возвращает отфйильтрованные значения из справочника. В качестве фильтра выступает паттерн поиска. Ищутся его
	 * вхождения как в значении из справочника, так и в описании (name) этого значения.
	 *
	 * @param pattern паттерн поиска
	 * @return отфильтрованный список значений справочника
	 */
	@Override
	public List<DictionaryItem<BigDecimal>> getValues(String pattern) {
		String preparedPattern = preparePattern(pattern);
		return getJdbcTemplate().query(
				"select * from (" + getSqlQuery() + ") where to_nchar(value) like ? escape '\\' or lower(name) like ? escape '\\'",
				new Object[]{preparedPattern, preparedPattern},
				new int[]{Types.VARCHAR, Types.VARCHAR},
				new ItemRowMapper()
		);
	}
}
