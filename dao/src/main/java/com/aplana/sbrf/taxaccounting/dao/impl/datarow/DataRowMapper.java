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

	private static final char STYLE_CODE = 's';
	private static final char COLSPAN_CODE = 'c';
	private static final char ROWSPAN_CODE = 'r';
	private static final char EDITABLE_CODE = 'e';

	private static final String COLUMN_PREFIX = "c";
	private static final char COLOR_SEPARATOR = '-';
	private static final char STYLE_BOLD = 'b';
	private static final char STYLE_ITALIC = 'i';
	private static final String STYLE_PARSING_ERROR_MESSAGE = "Строка с описанием стиля \"%s\" не может быть обработана";
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
	/**
	 * Статический кэш стилей, чтобы не создавать много одинаковых объектов-стилей
	 */
	private final static Map<String, FormStyle> styleCache = new HashMap<String, FormStyle>();

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
			StringBuilder sb = new StringBuilder(COLUMN_PREFIX);
			columnNames.put(column.getId(), new String[]{
					(sb.append(column.getDataOrder()).toString()).intern(),
					(sb.append("_style").toString()).intern()});
		}
		return columnNames;
	}

	@Override
	public DataRow<Cell> mapRow(ResultSet rs, int rowNum) throws SQLException {
		List<Cell> cells = FormDataUtils.createCells(formData.getFormTemplate());
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
				cell.setValue(parseCellValue(column.getColumnType(), rs.getString(COLUMN_PREFIX + column.getDataOrder())), rowNum);
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
			for (String style : styles) {
				char ch = style.charAt(0);
				if (ch == EDITABLE_CODE) {
					cell.setEditable(true);
					if (style.length() != 1) { // никаких цифр быть не должно после буквы "e"
						throw new IllegalArgumentException(String.format("Ошибка чтения стилей ячейки \"%s\"", value));
					}
				} else if (ch == STYLE_CODE) {
					String styleString = style.substring(1);
					if (styleString.isEmpty()) { // не может быт пустым
						throw new IllegalArgumentException(String.format("Ошибка чтения стилей ячейки \"%s\"", value));
					}
					cell.setStyle(getStyle(styleString));
				} else {
					String numStr = style.substring(1);
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
		StringBuilder sb = new StringBuilder();
		FormStyle style = cell.getStyle();
		if (style != null && !FormStyle.DEFAULT_STYLE.equals(style)) {
			sb.append(STYLE_CODE)
				.append(style.getFontColor().getId())
				.append(COLOR_SEPARATOR)
				.append(style.getBackColor().getId());
			if (style.isItalic()) {
				sb.append(STYLE_ITALIC);
			}
			if (style.isBold()) {
				sb.append(STYLE_BOLD);
			}
			sb.append(STYLE_SEPARATOR);
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
			} catch (Exception e) {
				throw new IllegalArgumentException(String.format("Значение \"%s\" не является типом %s", value, columnType.name()), e);
			}
		}
	}

	/**
	 * Осуществляет разбор строки стиля, работает с кэшем стилей
	 * @param styleString
	 * @return
	 */
	static final FormStyle getStyle(String styleString) {
		if (styleCache.containsKey(styleString)) {
			return styleCache.get(styleString);
		}
		FormStyle style = new FormStyle();
		StringBuilder fontColor = new StringBuilder();
		StringBuilder backColor = new StringBuilder();
		boolean fontScan = true; // флаг. true - поиск цвета шрифта, false - поиск цвета фона
		for (int i = 0; i < styleString.length(); i++) {
			char ch = styleString.charAt(i);
			switch (ch) {
				case STYLE_BOLD:
					style.setBold(true);
					break;
				case STYLE_ITALIC:
					style.setItalic(true);
					break;
				case COLOR_SEPARATOR:
					if (fontColor.length() == 0) {
						throw new IllegalArgumentException(String.format(STYLE_PARSING_ERROR_MESSAGE, styleString));
					}
					style.setFontColor(Color.getById(Integer.valueOf(fontColor.toString())));
					fontScan = false;
					break;
				default:
					if (fontScan) {
						fontColor.append(ch);
					} else {
						backColor.append(ch);
					}
			}
		}
		if (backColor.length() == 0) {
			throw new IllegalArgumentException(String.format(STYLE_PARSING_ERROR_MESSAGE, styleString));
		}
		style.setBackColor(Color.getById(Integer.valueOf(backColor.toString())));
		// добавление в кэш
		styleCache.put(styleString, style);
		return style;
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