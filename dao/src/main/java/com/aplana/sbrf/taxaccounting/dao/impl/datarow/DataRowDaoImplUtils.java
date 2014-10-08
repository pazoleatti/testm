package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import com.aplana.sbrf.taxaccounting.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DataRowDaoImplUtils {

	public static final long DEFAULT_ORDER_STEP = 100000;

    private DataRowDaoImplUtils() {}

	/**
	 * Массив содержит функции извлечения значения для разных таблиц со
	 * значениями ячеек
	 */
	static final CellValueExtractor[] CELL_VALUE_TABLE_EXTRACTORS = {
			new CellValueExtractor() {
				@Override
				public Object getValue(ResultSet rs, String columnLabel)
						throws SQLException {
					return rs.getBigDecimal(columnLabel);
				}
			}, new CellValueExtractor() {
				@Override
				public Object getValue(ResultSet rs, String columnLabel)
						throws SQLException {
					return rs.getString(columnLabel);
				}
			}, new CellValueExtractor() {
				@Override
				public Object getValue(ResultSet rs, String columnLabel)
						throws SQLException {
					return rs.getDate(columnLabel);
				}
			} };

	static <T> T getCellValueComponent(Column column, T[] objects) {
		switch (column.getColumnType()) {
			case STRING:
				return objects[1];
			case DATE:
				return objects[2];
			default:
				return objects[0];
		}
	}

	static CellValueExtractor getCellValueExtractor(Column c) {
		return getCellValueComponent(c, CELL_VALUE_TABLE_EXTRACTORS);
	}

	/**
	 * Рассчитывает шаг с которым должны вставляться новые строки между сохраненными строками с номерами ordBegin и ordEnd.
	 * @param ordBegin начальный индекс
	 * @param ordEnd конечный индекс
	 * @param count число строк для вставки
	 * @return шаг, равномерно задает распределение на отрезке [ordBegin; ordEnd]
	 */
	static long calcOrdStep(Long ordBegin, Long ordEnd, int count) {
		return (ordEnd - ordBegin) / (count + 1);
	}

	/**
	 * Ищет одинаковые элементы в списке
	 * @return true - есть совпадания; false - иначе
 	 */
	static boolean hasDuplicates(List<?> list){
		Set<Object> set = new HashSet<Object>(list);
		return list.size() > set.size();
	}

	interface CellValueExtractor {
		Object getValue(ResultSet rs, String columnLabel)
				throws SQLException;
	}

}