package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import com.aplana.sbrf.taxaccounting.model.PaginatedSearchParams;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
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
	 * @param pageParams страница
	 * @return отфильтрованный список значений справочника
	 */
	@Override
	public PaginatedSearchResult<DictionaryItem<String>> getValues(String pattern, PaginatedSearchParams pageParams) {
		String preparedPattern = preparePattern(pattern);
		PaginatedSearchResult<DictionaryItem<String>> result = new PaginatedSearchResult<DictionaryItem<String>>();
		result.setRecords(
				getJdbcTemplate().query(
						"select * from (" + getPagedSqlQuery() + ") ",
						new Object[]{
								preparedPattern,
								preparedPattern,
								pageParams.getStartIndex() + 1,
								pageParams.getStartIndex() + pageParams.getCount()
						},
						new int[]{Types.VARCHAR, Types.VARCHAR, Types.NUMERIC, Types.NUMERIC},
						new ItemRowMapper()
				)
		);

		result.setTotalRecordCount(getRowCount(pattern));

		return result;
	}
}


