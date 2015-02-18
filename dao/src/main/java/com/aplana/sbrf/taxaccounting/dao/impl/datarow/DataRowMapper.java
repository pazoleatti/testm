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

	private FormData fd;
	private DataRowRange range;
	private TypeFlag[] types;

	public DataRowMapper(FormData fd, TypeFlag[] types, DataRowRange range) {
		this.fd = fd;
		this.types = types;
		this.range = range;
	}

	/**
	 * improved createSql function
	 * @return
	 */
	public Pair<String, Map<String, Object>> createSql() {
		String[] prefixes = new String[]{"v", "s", "e", "csi", "rsi"};
		StringBuilder sql = new StringBuilder("SELECT MAX(idx1) AS idx, sub.id, max(sub.alias) as alias \n");
		// генерация max(X) X
		for (Column c : fd.getFormColumns()){
			for (String prefix : prefixes){
                sql
						.append(" , MAX(")
						.append(prefix)
						.append(c.getId())
						.append(") ")
						.append(prefix)
						.append(c.getId());
			}
            sql.append("\n ");
		}
        sql.append(", sub.id as id2, max(sub.idx2) as idx2 \n");
        sql.append("FROM (SELECT d.id, d.alias, d.ord, d.idx1, d.idx2");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", fd.getId());
		params.put("manual", fd.isManual() ? 1 : 0);
		params.put("types", TypeFlag.rtsToKeys(types));

		// генерация "case when C.COLUMN_ID=18740 then C.STYLE_ID else null end S18740,"
		for (Column column : fd.getFormColumns()){
			int columnId = column.getId();
			char valuePrefix;
			switch (column.getColumnType()) {
				case STRING:
					valuePrefix = 's';
					break;
				case DATE:
					valuePrefix = 'd';
					break;
				default:
					valuePrefix = 'n';
			}
            sql.append(",\n CASE WHEN c.column_id = ").append(columnId).append(" THEN c.").append(valuePrefix).append("value ELSE NULL END v").append(columnId).append(", ")
					.append(" CASE WHEN c.column_id = ").append(columnId).append(" THEN c.style_id ELSE NULL END s").append(columnId).append(", ")
					.append(" CASE WHEN c.column_id = ").append(columnId).append(" THEN c.editable ELSE 0 END e").append(columnId).append(", ")
					.append(" CASE WHEN c.column_id = ").append(columnId).append(" THEN c.colspan ELSE NULL END csi").append(columnId).append(", ")
					.append(" CASE WHEN c.column_id = ").append(columnId).append(" THEN c.rowspan ELSE NULL END rsi").append(columnId);
		}

        sql.append("\n FROM (\n");
        sql.append("  select dr.id, dr.form_data_id, dr.alias, dr.ord, dr.type, dr.manual, dr.idx1, case when idx2_critery = 1 then idx2 else null end idx2 from ( \n");
        sql.append("   select d.*, row_number() over (order by ord) as idx1, case when (d.alias IS NULL OR d.alias LIKE '%{wan}') then 1 else 0 end as idx2_critery, \n");
        sql.append("    row_number() over (partition by case when (d.alias IS NULL OR d.alias LIKE '%" + ALIASED_WITH_AUTO_NUMERATION_AFFIX + "') then 1 else 0 end order by ord) idx2 \n");
        sql.append("    from data_row d  where d.form_data_id = :formDataId and d.manual = :manual and d.type in (:types)) dr \n");
        if (range != null) {
            sql.append("  WHERE idx1 BETWEEN :from AND :to \n");
            params.put("from", range.getOffset());
            params.put("to", range.getOffset() + range.getLimit() - 1);
        }
        sql.append("  order by ord \n");
        sql.append("  ) d \n");
        sql.append(" LEFT JOIN data_cell c ON d.id = c.row_id ) sub \n");
        sql.append("GROUP BY sub.id \n");
        return new Pair<String, Map<String, Object>>(sql.toString(), params);
	}

	@Override
	public DataRow<Cell> mapRow(ResultSet rs, int rowNum) throws SQLException {
		List<Cell> cells = FormDataUtils.createCells(fd.getFormColumns(),
				fd.getFormStyles());
		Integer previousRowNumber = fd.getPreviousRowNumber() != null ? fd.getPreviousRowNumber() : 0;
        String alias = rs.getString("alias");
        for (Cell cell : cells) {
			// Values
			if (ColumnType.AUTO.equals(cell.getColumn().getColumnType()) && (alias == null
                    || alias.contains(ALIASED_WITH_AUTO_NUMERATION_AFFIX))) {
				if (NumerationType.CROSS.equals(((AutoNumerationColumn) cell.getColumn()).getNumerationType())) {
						cell.setValue(SqlUtils.getLong(rs, "IDX2") + previousRowNumber, rowNum);
				} else {
					cell.setValue(SqlUtils.getLong(rs, "IDX2"), rowNum);
				}
			} else {
				DataRowDaoImplUtils.CellValueExtractor extr = getCellValueExtractor(cell.getColumn());
				cell.setValue(extr.getValue(rs,
						String.format("V%s", cell.getColumn().getId())), rowNum);
			}
			// Styles
			BigDecimal styleId = rs.getBigDecimal(String.format("S%s", cell
					.getColumn().getId()));
			cell.setStyleId(styleId != null ? styleId.intValueExact() : null);
			// Editable
			cell.setEditable(rs.getBoolean(String.format("E%s", cell
					.getColumn().getId())));
			// Span Info
			Integer rowSpan = SqlUtils.getInteger(rs, String.format("RSI%s", cell.getColumn()
					.getId()));
			cell.setRowSpan(((rowSpan == null) || (rowSpan == 0)) ? 1 : rowSpan);
			Integer colSpan = SqlUtils.getInteger(rs, String.format("CSI%s", cell.getColumn()
					.getId()));
			cell.setColSpan(((colSpan == null) || (colSpan == 0)) ? 1 : colSpan);
		}
		DataRow<Cell> dataRow = new DataRow<Cell>(alias, cells);
		dataRow.setId(SqlUtils.getLong(rs,"ID"));
		dataRow.setIndex(SqlUtils.getInteger(rs,"IDX"));
		return dataRow;
	}
}
