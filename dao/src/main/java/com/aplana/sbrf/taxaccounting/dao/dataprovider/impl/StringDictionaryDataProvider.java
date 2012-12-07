package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Источник данных для строковых справочников
 */
public class StringDictionaryDataProvider extends JdbcDictionaryDataProvider<String> {
	/**
	 * Получает значение справочника из {@link ResultSet}
	 *
	 * @param rs result set
	 * @return значение справочника
	 * @throws SQLException например, если в ответе не окажется нужного поля
	 */
	@Override
	protected String getValue(ResultSet rs) throws SQLException {
		return rs.getString("value");
	}

	/**
	 * Возвращает отфйильтрованные значения из справочника. В качестве фильтра выступает паттерн поиска. Ищутся его
	 * вхождения как в значении из справочника, так и в описании (name) этого значения.
	 *
	 * @param pattern паттерн поиска
	 * @return отфильтрованный список значений справочника
	 */
	@Override
	public List<DictionaryItem<String>> getValues(String pattern) {
		String preparedPattern = preparePattern(pattern);
		return getJdbcTemplate().query(
				"select * from (" + getSqlQuery() + ") where lower(value) like ? escape '\\' or lower(name) like ? escape '\\'",
				new Object[]{preparedPattern, preparedPattern},
				new int[]{Types.VARCHAR, Types.VARCHAR},
				new ItemRowMapper()
		);
	}
}


