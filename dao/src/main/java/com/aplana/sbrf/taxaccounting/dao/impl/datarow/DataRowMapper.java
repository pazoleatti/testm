package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sgoryachkin
 *
 */
class DataRowMapper implements RowMapper<DataRow<Cell>> {

	private static final Log LOG = LogFactory.getLog(DataRowMapper.class);

	private static final char STYLE_SEPARATOR = ';';
	static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private static final char DECIMAL_SEPARATOR = '.';
	private static final char WRONG_DECIMAL_SEPARATOR = ',';
	private static final DecimalFormat DECIMAL_FORMAT;
	static{
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(DECIMAL_SEPARATOR);
		DECIMAL_FORMAT = new DecimalFormat("#0.#", symbols);
		DECIMAL_FORMAT.setParseBigDecimal(true);
		DECIMAL_FORMAT.setDecimalSeparatorAlwaysShown(false);
		DECIMAL_FORMAT.setMaximumIntegerDigits(NumericColumn.MAX_LENGTH-NumericColumn.MAX_PRECISION);
		DECIMAL_FORMAT.setMaximumFractionDigits(NumericColumn.MAX_PRECISION);
	}

	/**
	 * Признак участия фиксированной строки в автонумерации
	 */
	public final static String ALIASED_WITH_AUTO_NUMERATION_AFFIX = "{wan}";

	private FormData formData;

	public DataRowMapper(FormData formData) {
		this.formData = formData;
	}

	/**
	 * Формирует sql-запрос для извлечения данных НФ
	 *
	 * @param range параметры пейджинга, может быть null
	 * @return пара "sql-запрос"-"параметры" для извлечения данных НФ
	 */
	public Pair<String, Map<String, Object>> createSql(DataRowRange range, DataRowType dataRowType) {
		DataRowType isManual = formData.isManual() ? DataRowType.MANUAL : DataRowType.AUTO;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", formData.getId());
		params.put("temporary", dataRowType.getCode());
		params.put("manual", isManual.getCode());

		StringBuilder sql = new StringBuilder("SELECT id, ord, alias,\n");
		// автонумерация (считаются все строки где нет алиасов, либо алиас = ALIASED_WITH_AUTO_NUMERATION_AFFIX)
		sql.append("CASE WHEN (alias IS NULL OR alias LIKE '%").append(ALIASED_WITH_AUTO_NUMERATION_AFFIX).append("%') THEN\n")
			.append("ROW_NUMBER() OVER (PARTITION BY CASE WHEN (alias IS NULL OR alias LIKE '%")
			.append(ALIASED_WITH_AUTO_NUMERATION_AFFIX).append("%') THEN 1 ELSE 0 END ORDER BY ord)\n")
			.append("ELSE NULL END ");
        if (range != null) {
            sql.append("+ (SELECT count(*) from form_data_row")
                .append(" WHERE (alias IS NULL OR alias LIKE '%").append(ALIASED_WITH_AUTO_NUMERATION_AFFIX).append("%') AND")
                .append(" form_data_id = :formDataId AND temporary = :temporary AND manual = :manual")
                .append(" AND ord < :from)");
        }
        sql.append(" numeration");
		getColumnNamesString(formData, sql);
		sql.append("\nFROM form_data_row");
		sql.append("\nWHERE form_data_id = :formDataId AND temporary = :temporary AND manual = :manual");
		// пейджинг
		if (range != null) {
			sql.append(" AND ord BETWEEN :from AND :to");
			params.put("from", range.getOffset());
			params.put("to", range.getOffset() + range.getCount() - 1);
		}
		sql.append("\nORDER BY ord");

		return new Pair<String, Map<String, Object>>(sql.toString(), params);
	}

	/**
	 * Добавляет в sql запрос список столбцов по всем графам по шаблону <b>", cXXX, cXXX_style_id, ..., cXXX_rowspan"</b>
	 * @param formData НФ
	 * @param sql запрос, куда следует добавить перечень столбцов.
	 */
	static void getColumnNamesString(FormData formData, StringBuilder sql) {
		Map<Integer, String[]> columnNames = getColumnNames(formData);
		for (Column column : formData.getFormColumns()){
			sql.append('\n');
			for(String name : columnNames.get(column.getId())) {
				sql.append(", ").append(name);
			}
		}
	}

	/**
	 * Формирует список названий столбцов таблицы как они хранятся в бд
	 * @param formData
	 * @return Map<columnId, [cNN, cNN_style]>
	 */
	static Map<Integer, String[]> getColumnNames(FormData formData) {
		Map<Integer, String[]> columnNames = new HashMap<Integer, String[]>();
		for (Column column : formData.getFormColumns()){
			StringBuilder sb = new StringBuilder("c");
			columnNames.put(column.getId(), new String[]{
					(sb.append(column.getDataOrder()).toString()).intern(),
					(sb.append("_style").toString()).intern()});
		}
		return columnNames;
	}

	@Override
	public DataRow<Cell> mapRow(ResultSet rs, int rowNum) throws SQLException {
		List<Cell> cells = FormDataUtils.createCells(formData.getFormColumns(), formData.getFormStyles());
		Integer previousRowNumber = formData.getPreviousRowNumber() != null ? formData.getPreviousRowNumber() : 0;
		String alias = rs.getString("alias");
		for (Cell cell : cells) {
			Column column = cell.getColumn();
			// Values
			if (ColumnType.AUTO.equals(column.getColumnType()) &&
					(alias == null || alias.contains(ALIASED_WITH_AUTO_NUMERATION_AFFIX))) {
				Long numeration = SqlUtils.getLong(rs, "numeration");
				if (NumerationType.CROSS.equals(((AutoNumerationColumn) column).getNumerationType())) {
					cell.setValue(numeration + previousRowNumber, rowNum);
				} else {
					cell.setValue(numeration, rowNum);
				}
			} else {
				cell.setValue(parseCellValue(column.getColumnType(), rs.getString("c" + column.getDataOrder())), rowNum);
			}
			// чтение стилей
			parseCellStyle(cell, rs.getString("c" + column.getDataOrder() + "_style"));
		}
		DataRow<Cell> dataRow = new DataRow<Cell>(alias, cells);
		dataRow.setId(SqlUtils.getLong(rs, "id"));
		dataRow.setIndex(SqlUtils.getInteger(rs,"ord"));
		return dataRow;
	}

	/**
	 * Чтение стилей ячейки из строки
	 * @param cell
	 * @param value
	 */
	static final void parseCellStyle(Cell cell, String value) {
		// значения по умолчанию
		cell.setStyleId(null);
		cell.setEditable(false);
		cell.setColSpan(1);
		cell.setRowSpan(1);
		// разбираем стили
	 	if (value != null && !value.isEmpty()) {
			String[] styles = StringUtils.split(value, STYLE_SEPARATOR);
			for (String style : styles) {
				char ch = style.charAt(0);
				if (ch == 'e') {
					cell.setEditable(true);
					if (style.length() != 1) { // никаких цифр быть не должно после буквы "e"
						throw new IllegalArgumentException(String.format("Ошибка чтения стилей ячейки \"%s\"", value));
					}
				} else {
					String numStr = style.substring(1);
					if (numStr.isEmpty()) { // хоть одна циферка должна быть
						throw new IllegalArgumentException(String.format("Ошибка чтения стилей ячейки \"%s\"", value));
					}
					Integer num = Integer.valueOf(numStr);
					switch (ch) {
						case 's':
							cell.setStyleId(num);
							break;
						case 'c':
							cell.setColSpan(num);
							break;
						case 'r':
							cell.setRowSpan(num);
							break;
					}
				}
			}
		}
	}

	/**
	 * Запись стиля ячейки в строку
	 * @param cell
	 * @return
	 */
	static final String formatCellStyle(Cell cell) {
		StringBuilder sb = new StringBuilder();
		if (cell.getStyle() != null) {
			sb.append('s').append(cell.getStyle().getId()).append(STYLE_SEPARATOR);
		}
		if (cell.isEditable()) {
			sb.append('e').append(STYLE_SEPARATOR);
		}
		if (cell.getColSpan() > 1) {
			sb.append('c').append(cell.getColSpan()).append(STYLE_SEPARATOR);
		}
		if (cell.getRowSpan() > 1) {
			sb.append('r').append(cell.getRowSpan()).append(STYLE_SEPARATOR);
		}
		if (sb.length() > 0) {
			return sb.substring(0, sb.length() - 1);
		}
		return null;
	}

	/**
	 * Чтение данных ячейки. Преобразование строки в конкретный тип
	 * @param columnType
	 * @param value
	 * @return
	 * @throws SQLException
	 */
	static final Object parseCellValue(ColumnType columnType, String value) {
		if (value == null) {
			return null;
		} else {
			try {
				switch (columnType) {
					case STRING:
						return value;
					case DATE:
						return SDF.parse(value);
					case NUMBER:
						String cleanValue = value.replace(WRONG_DECIMAL_SEPARATOR, DECIMAL_SEPARATOR);
						return DECIMAL_FORMAT.parse(cleanValue);
					case REFBOOK:
						// только целые числа
						if (value.indexOf(WRONG_DECIMAL_SEPARATOR) + value.indexOf(DECIMAL_SEPARATOR) != -2) {
							throw new IllegalArgumentException(String.format("Значение \"%s\" не является типом %s", value, columnType.name()));
						}
						return DECIMAL_FORMAT.parse(value).longValue();
					default:
						return null;
				}
			} catch (ParseException e) {
				throw new IllegalArgumentException(String.format("Значение \"%s\" не является типом %s", value, columnType.name()), e);
			}
		}
	}

	public static String formatCellValue(Cell cell) {
		return formatCellValue(cell.getColumn().getColumnType(), cell.getValue());
	}

	/**
	 * Запись значения ячейки. Преобразование значения конкретного типа в строку
	 * @param columnType
	 * @param value
	 * @return
	 */
	 static final String formatCellValue(ColumnType columnType, Object value) {
		if (value == null) {
			return null;
		} else {
			switch (columnType) {
				case STRING:
					return value.toString();
				case DATE:
					return SDF.format(value);
				case NUMBER:
				case REFBOOK:
					return DECIMAL_FORMAT.format(value);
				default:
					return null;
			}
		}
	}
}