package com.aplana.sbrf.taxaccounting.dao.dataprovider.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.aplana.sbrf.taxaccounting.model.PaginatedSearchParams;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

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
	protected BigDecimal getValue(ResultSet rs) throws SQLException {
		return rs.getBigDecimal("value");
	}

	/**
	 * Возвращает отфйильтрованные значения из справочника. В качестве фильтра выступает паттерн поиска. Ищутся его
	 * вхождения как в значении из справочника, так и в описании (name) этого значения.
	 *
	 * @param pattern паттерн поиска
	 * @param pageParams страница
	 *
	 *
	 * @return отфильтрованный список значений справочника
	 */
	@Override
	public PaginatedSearchResult<DictionaryItem<BigDecimal>> getValues(String pattern, PaginatedSearchParams pageParams) {
		String preparedPattern = preparePattern(pattern);
		PaginatedSearchResult<DictionaryItem<BigDecimal>> result = new PaginatedSearchResult<DictionaryItem<BigDecimal>>();
		result.setRecords(
			getJdbcTemplate().query(
					"select value, name from (select rownum as r, value, name from ( " +
							getSqlQuery() +
							" ) where value like ? escape '\\' or lower(name) like ? escape '\\') where r between ? and ?",
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

	@Override
	public long getRowCount(String pattern) {
		String preparedPattern = preparePattern(pattern);
		return getJdbcTemplate().queryForLong(
				"select count(*) from (" + getSqlQuery() + ") " +
						"where value like ? escape '\\' or lower(name) like ? escape '\\'",
				new Object[]{
						preparedPattern,
						preparedPattern
				},
				new int[]{Types.VARCHAR, Types.VARCHAR}
		);
	}

}
