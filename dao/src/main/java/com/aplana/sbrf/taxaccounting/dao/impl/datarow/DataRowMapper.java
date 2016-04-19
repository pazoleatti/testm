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

	private static final char COLSPAN_CODE = 'c';
	private static final char ROWSPAN_CODE = 'r';
	private static final char EDITABLE_CODE = 'e';

	private static final String COLUMN_PREFIX = "c";
	private static final char STYLE_SEPARATOR = ';';
	private static final char DECIMAL_SEPARATOR = '.';
	private static final char WRONG_DECIMAL_SEPARATOR = ',';
	private DecimalFormat decimalFormat;

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
    };
	/**
	 * Признак участия фиксированной строки в автонумерации
	 */
	public final static String ALIASED_WITH_AUTO_NUMERATION_AFFIX = "{wan}";
	/**
	 * Статический кэш стилей, чтобы не создавать много одинаковых объектов-стилей
	 */
	private final static Map<String, FormStyle> styleCache = new HashMap<String, FormStyle>();

	private FormData formData;

	public DataRowMapper(FormData formData) {
		this.formData = formData;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(DECIMAL_SEPARATOR);
        decimalFormat = new DecimalFormat("#0.#", symbols);
        decimalFormat.setParseBigDecimal(true);
        decimalFormat.setDecimalSeparatorAlwaysShown(false);
        decimalFormat.setMaximumIntegerDigits(NumericColumn.MAX_LENGTH-NumericColumn.MAX_PRECISION);
        decimalFormat.setMaximumFractionDigits(NumericColumn.MAX_PRECISION);
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
			StringBuilder sb = new StringBuilder(COLUMN_PREFIX);
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
				cell.setValue(parseCellValue(column.getColumnType(), rs.getString(COLUMN_PREFIX + column.getDataOrder())), rowNum, true);
			}
			// чтение стилей
			parseCellStyle(cell, rs.getString(COLUMN_PREFIX + column.getDataOrder() + "_style"));
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
		cell.setStyle(FormStyle.DEFAULT_STYLE);
		cell.setEditable(false);
		cell.setColSpan(1);
		cell.setRowSpan(1);
		// разбираем стили
	 	if (value != null && !value.isEmpty()) {
			String[] styles = StringUtils.split(value, STYLE_SEPARATOR);
			for (String styleString : styles) {
				char ch = styleString.charAt(0);
				if (ch == EDITABLE_CODE) {
					cell.setEditable(true);
					if (styleString.length() != 1) { // никаких цифр быть не должно после буквы "e"
						throw new IllegalArgumentException(String.format("Ошибка чтения стилей ячейки \"%s\"", value));
					}
				} else if (ch == FormStyle.STYLE_CODE) {
					if (!styleCache.containsKey(styleString)) {
						styleCache.put(styleString, FormStyle.valueOf(styleString));
					}
					cell.setStyle(styleCache.get(styleString));
				} else {
					String numStr = styleString.substring(1);
					if (numStr.isEmpty()) { // хотя бы один символ должен быть
						throw new IllegalArgumentException(String.format("Ошибка чтения стилей ячейки \"%s\"", value));
					}
					Integer num = Integer.valueOf(numStr);
					switch (ch) {
						case COLSPAN_CODE:
							cell.setColSpan(num);
							break;
						case ROWSPAN_CODE:
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
		FormStyle formStyle = cell.getStyle();
		StringBuilder sb = new StringBuilder();
		if (!FormStyle.DEFAULT_STYLE.equals(formStyle)) {
			sb.append(formStyle.toString()).append(STYLE_SEPARATOR);
		}
		if (cell.isEditable()) {
			sb.append(EDITABLE_CODE).append(STYLE_SEPARATOR);
		}
		if (cell.getColSpan() > 1) {
			sb.append(COLSPAN_CODE).append(cell.getColSpan()).append(STYLE_SEPARATOR);
		}
		if (cell.getRowSpan() > 1) {
			sb.append(ROWSPAN_CODE).append(cell.getRowSpan()).append(STYLE_SEPARATOR);
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
	Object parseCellValue(ColumnType columnType, String value) {
		if (value == null) {
			return null;
		} else {
			try {
				switch (columnType) {
					case STRING:
						return value;
					case DATE:
						return sdf.get().parse(value);
					case NUMBER:
						String cleanValue = value.replace(WRONG_DECIMAL_SEPARATOR, DECIMAL_SEPARATOR);
						return decimalFormat.parse(cleanValue);
					case REFBOOK:
						// только целые числа
						if (value.indexOf(WRONG_DECIMAL_SEPARATOR) + value.indexOf(DECIMAL_SEPARATOR) != -2) {
							throw new IllegalArgumentException(String.format("Значение \"%s\" не является типом %s", value, columnType.name()));
						}
						return decimalFormat.parse(value).longValue();
					default:
						return null;
				}
			} catch (Exception e) {
				throw new IllegalArgumentException(String.format("Значение \"%s\" не является типом %s", value, columnType.name()), e);
			}
		}
	}

	/**
	 * Преобразование значения ячейки в строку
	 * @param cell
	 * @return
	 */
	String formatCellValue(Cell cell) {
		Object value = cell.getValue();
		if (value == null) {
			return null;
		} else {
			switch (cell.getColumn().getColumnType()) {
				case STRING:
					return value.toString();
				case DATE:
					return sdf.get().format(value);
				case NUMBER:
				case REFBOOK:
					return decimalFormat.format(value);
				default:
					return null;
			}
		}
	}
}