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

	static <T> T getCellValueComponent(Column c, T[] objects) {
		if (c instanceof StringColumn) {
			return objects[1];
		} else if (c instanceof NumericColumn || c instanceof RefBookColumn || c instanceof ReferenceColumn || c instanceof AutoNumerationColumn) {
			return objects[0];
		} else if (c instanceof DateColumn) {
			return objects[2];
		} else {
			throw new IllegalArgumentException();
		}
	}

	static CellValueExtractor getCellValueExtractor(Column c) {
		return getCellValueComponent(c, CELL_VALUE_TABLE_EXTRACTORS);
	}

	static long calcOrdStep(Long ordBegin, Long ordEnd, int number) {
		return (ordEnd - ordBegin) / (number + 1);
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