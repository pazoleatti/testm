package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;

public class DataRowDaoImplUtils {
	
	static final int DEFAULT_ORDER_STEP = 100000;

	/**
	 * Массив содержит названия таблиц со значениями ячеек
	 */
	public static final String[] CELL_VALUE_TABLE_NAMES = { "NUMERIC_VALUE",
			"STRING_VALUE", "DATE_VALUE" };

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
		} else if (c instanceof NumericColumn) {
			return objects[0];
		} else if (c instanceof DateColumn) {
			return objects[2];
		} else {
			throw new IllegalArgumentException();
		}
	}

	public static String getCellValueTableName(Column c) {
		return getCellValueComponent(c, CELL_VALUE_TABLE_NAMES);
	}

	static CellValueExtractor getCellValueExtractor(Column c) {
		return getCellValueComponent(c, CELL_VALUE_TABLE_EXTRACTORS);
	}
	
	static long calcOrdStep(Long ordBegin, Long ordEnd, int number){
		return (ordEnd - ordBegin) / (number + 1);
	}
	
	static interface CellValueExtractor {
		public Object getValue(ResultSet rs, String columnLabel)
				throws SQLException;
	}
	
}
