package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.dao.impl.datarow.DataRowDaoImplUtils.getCellValueExtractor;

/**
 * @author sgoryachkin
 *
 *         <a>http://conf.aplana.com/pages/viewpage.action?pageId=9588773&
 *         focusedCommentId=9591393#comment-9591393</a>
 */
class DataRowMapper implements RowMapper<DataRow<Cell>> {

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
	 * @param isTemporary временный срез?
	 * @return пара "sql-запрос"-"параметры" для извлечения данных НФ
	 */
	public Pair<String, Map<String, Object>> createSql(DataRowRange range, DataRowType isTemporary) {
		DataRowType isManual = formData.isManual() ? DataRowType.MANUAL : DataRowType.AUTO;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", formData.getId());
		params.put("temporary", isTemporary.getCode());
		params.put("manual", isManual.getCode());

		StringBuilder sql = new StringBuilder("SELECT ord, alias,\n");
		// автонумерация (считаются все строки где нет алиасов, либо алиас = ALIASED_WITH_AUTO_NUMERATION_AFFIX)
		sql.append("CASE WHEN (alias IS NULL OR alias LIKE '%").append(ALIASED_WITH_AUTO_NUMERATION_AFFIX).append("') THEN\n")
			.append("ROW_NUMBER() OVER (PARTITION BY CASE WHEN (alias IS NULL OR alias LIKE '%")
			.append(ALIASED_WITH_AUTO_NUMERATION_AFFIX).append("') THEN 1 ELSE 0 END ORDER BY ord)\n")
			.append("ELSE NULL END numeration");
		// значения и стили ячеек
		for (Column column : formData.getFormColumns()){
			Integer columnId = column.getId();
			sql.append(",\n");
			sql.append('c').append(columnId).append(", ");
			sql.append('c').append(columnId).append("_style_id, ");
			sql.append('c').append(columnId).append("_editable, ");
			sql.append('c').append(columnId).append("_colspan, ");
			sql.append('c').append(columnId).append("_rowspan");
		}
		sql.append("\nFROM form_data_").append(formData.getFormTemplateId());
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

	@Override
	public DataRow<Cell> mapRow(ResultSet rs, int rowNum) throws SQLException {
		List<Cell> cells = FormDataUtils.createCells(formData.getFormColumns(), formData.getFormStyles());
		Integer previousRowNumber = formData.getPreviousRowNumber() != null ? formData.getPreviousRowNumber() : 0;
		String alias = rs.getString("alias");
		for (Cell cell : cells) {
			Integer columnId = cell.getColumn().getId();
			// Values
			if (ColumnType.AUTO.equals(cell.getColumn().getColumnType()) &&
					(alias == null || alias.contains(ALIASED_WITH_AUTO_NUMERATION_AFFIX))) {
				Long numeration = SqlUtils.getLong(rs, "numeration");
				if (NumerationType.CROSS.equals(((AutoNumerationColumn) cell.getColumn()).getNumerationType())) {
					cell.setValue(numeration + previousRowNumber, rowNum);
				} else {
					cell.setValue(numeration, rowNum);
				}
			} else {
				DataRowDaoImplUtils.CellValueExtractor extr = getCellValueExtractor(cell.getColumn());
				cell.setValue(extr.getValue(rs, String.format("c%s", columnId)), rowNum);
			}
			// Styles
			BigDecimal styleId = rs.getBigDecimal(String.format("c%s_style_id", columnId));
			cell.setStyleId(styleId != null ? styleId.intValueExact() : null);
			// Editable
			cell.setEditable(rs.getBoolean(String.format("c%s_editable", columnId)));
			// Span Info
			Integer colSpan = SqlUtils.getInteger(rs, String.format("c%s_colspan", columnId));
			cell.setColSpan(((colSpan == null) || (colSpan == 0)) ? 1 : colSpan);
			Integer rowSpan = SqlUtils.getInteger(rs, String.format("c%s_rowspan", columnId));
			cell.setRowSpan(((rowSpan == null) || (rowSpan == 0)) ? 1 : rowSpan);
		}
		DataRow<Cell> dataRow = new DataRow<Cell>(alias, cells);
		dataRow.setId(SqlUtils.getLong(rs, "ord")); //TODO удалить
		dataRow.setIndex(SqlUtils.getInteger(rs,"ord"));
		return dataRow;
	}
}
