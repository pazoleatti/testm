package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowFilter;
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

	private FormData fd;
	private DataRowRange range;
	private TypeFlag[] types;

	public DataRowMapper(FormData fd, TypeFlag[] types, DataRowFilter filter,
						 DataRowRange range) {
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
		StringBuilder select = new StringBuilder("SELECT ROW_NUMBER() OVER (ORDER BY sub.ord) as idx, sub.id, sub.a \n");
		// генерация max(X) X
		for (Column c : fd.getFormColumns()){
			for (String prefix : prefixes){
				select
						.append(" , MAX(")
						.append(prefix)
						.append(c.getId())
						.append(") ")
						.append(prefix)
						.append(c.getId());
			}
			select.append('\n');
		}
		select.append(" FROM (SELECT d.id, d.alias AS a, d.ord");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", fd.getId());
		params.put("manual", fd.isManual() ? 1 : 0);
		params.put("types", TypeFlag.rtsToKeys(types));

		// генерация "case when C.COLUMN_ID=18740 then C.STYLE_ID else null end S18740,"
		for (Column column : fd.getFormColumns()){
			int columnId = column.getId();
			String columnSId =  String.format("column%sId", columnId);
			params.put(columnSId, columnId);

			char valuePrefix = 's';
			if (column instanceof StringColumn) {
				valuePrefix = 's';
			} else if (column instanceof NumericColumn || column instanceof RefBookColumn || column instanceof ReferenceColumn || column instanceof AutoNumerationColumn) {
				valuePrefix = 'n';
			} else if (column instanceof DateColumn) {
				valuePrefix = 'd';
			} else {
				throw new IllegalArgumentException();
			}

			select.append(",\n CASE WHEN c.column_id = :").append(columnSId).append(" THEN c.").append(valuePrefix).append("value ELSE NULL END v").append(columnId).append(",\n")
					.append(" CASE WHEN c.column_id = :").append(columnSId).append(" THEN c.style_id ELSE NULL END s").append(columnId).append(",\n")
					.append(" CASE WHEN c.column_id = :").append(columnSId).append(" THEN c.edit ELSE 0 END e").append(columnId).append(",\n")
					.append(" CASE WHEN c.column_id = :").append(columnSId).append(" THEN c.colspan ELSE NULL END csi").append(columnId).append(",\n")
					.append(" CASE WHEN c.column_id = :").append(columnSId).append(" THEN c.rowspan ELSE NULL END rsi").append(columnId);
		}

		select.append("\n FROM data_row d LEFT JOIN \n");
		select.append(" (SELECT column_id, row_id, style_id, editable edit, colspan, rowspan, nvalue, dvalue, svalue FROM data_cell) c ON d.id = c.row_id \n");
		select.append(" WHERE d.form_data_id = :formDataId AND manual = :manual AND d.type IN (:types)) sub \n");
		select.append(" GROUP BY sub.id, sub.ord, sub.a ORDER BY sub.ord");

		StringBuilder sql = new StringBuilder();
		sql.append(select);
		// Генерация нумерации строк
		sql.insert(0, "SELECT * FROM (\n");
		sql.append(") table1\nLEFT JOIN \n(");
		sql.append("SELECT ROW_NUMBER() OVER (ORDER BY sub.ord) AS idx2, sub.id AS id2\n");
		sql.append(" FROM (SELECT d.id, d.alias AS a, d.ord FROM data_row d\n");
		sql.append(" WHERE d.form_data_id = :formDataId AND manual = :manual AND d.type IN (:types) AND d.alias IS NULL) sub\n");
		sql.append(" GROUP BY sub.id, sub.ord, sub.a ORDER BY sub.ord");
		sql.append(") table2\nON table1.id = table2.id2");

		if (range != null) {
			sql.insert(0, "select * from( ");
			sql.append(") where IDX between :from and :to");
			params.put("from", range.getOffset());
			params.put("to", range.getOffset() + range.getLimit() - 1);
		}

		return new Pair<String, Map<String, Object>>(sql.toString(), params);
	}

	@Override
	public DataRow<Cell> mapRow(ResultSet rs, int rowNum) throws SQLException {
		List<Cell> cells = FormDataUtils.createCells(fd.getFormColumns(),
				fd.getFormStyles());
		Integer previousRowNumber = fd.getPreviousRowNumber() != null ? fd.getPreviousRowNumber() : 0;
		for (Cell cell : cells) {
			// Values
			if (cell.getColumn() instanceof AutoNumerationColumn && rs.getString("A") == null) {
				if (((AutoNumerationColumn) cell.getColumn()).getType() == 1) {
					cell.setValue(SqlUtils.getInteger(rs, "IDX2") + previousRowNumber, rowNum);
				} else {
					cell.setValue(SqlUtils.getInteger(rs, "IDX2"), rowNum);
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
		DataRow<Cell> dataRow = new DataRow<Cell>(rs.getString("A"), cells);
		dataRow.setId(SqlUtils.getLong(rs,"ID"));
		dataRow.setIndex(SqlUtils.getInteger(rs,"IDX"));
		return dataRow;
	}
}
