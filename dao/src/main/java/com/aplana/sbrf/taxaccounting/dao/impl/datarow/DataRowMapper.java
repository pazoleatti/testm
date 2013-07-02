package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import static com.aplana.sbrf.taxaccounting.dao.impl.datarow.DataRowDaoImplUtils.*;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowFilter;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

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

	public Pair<String, Map<String, Object>> createSql() {

		StringBuilder select = new StringBuilder(
				"select rownum as IDX, R.ID as ID, R.ALIAS as A");
		StringBuilder from = new StringBuilder(" from DATA_ROW R");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formDataId", fd.getId());
		params.put("types", TypeFlag.rtsToKeys(types));

		for (Column c : fd.getFormColumns()) {
			params.put(String.format("column%sId", c.getId()), c.getId());
			String valueTableName = getCellValueTableName(c);

			// Values
			select.append(String.format(", C%s.VALUE as V%s", c.getId(),
					c.getId()));
			from.append(String
					.format(" left join (select COLUMN_ID, ROW_ID, VALUE from %s N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId) C%s on C%s.ROW_ID = R.ID and C%s.COLUMN_ID = :column%sId",
							valueTableName, c.getId(), c.getId(), c.getId(),
							c.getId()));
			// Styles
			select.append(String.format(", S%s.STYLE_ID as S%s", c.getId(),
					c.getId()));
			from.append(String
					.format(" left join (select COLUMN_ID, ROW_ID, STYLE_ID from CELL_STYLE N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId) S%s on S%s.ROW_ID = R.ID and S%s.COLUMN_ID = :column%sId",
							c.getId(), c.getId(), c.getId(), c.getId()));
			// Editables
			select.append(String.format(", E%s.EDIT as E%s", c.getId(),
					c.getId()));
			from.append(String
					.format(" left join (select COLUMN_ID, ROW_ID, 1 as EDIT from CELL_EDITABLE N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId) E%s on E%s.ROW_ID = R.ID and E%s.COLUMN_ID = :column%sId",
							c.getId(), c.getId(), c.getId(), c.getId()));
			// Span Info
			select.append(String.format(
					", SI%s.COLSPAN as CSI%s, SI%s.ROWSPAN as RSI%s",
					c.getId(), c.getId(), c.getId(), c.getId()));
			from.append(String
					.format(" left join (select COLUMN_ID, ROW_ID, COLSPAN, ROWSPAN from CELL_SPAN_INFO N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId) SI%s on SI%s.ROW_ID = R.ID and SI%s.COLUMN_ID = :column%sId",
							c.getId(), c.getId(), c.getId(), c.getId()));

		}

		StringBuilder sql = new StringBuilder();
		sql.append(select)
				.append(from)
				.append(" where R.FORM_DATA_ID = :formDataId and R.TYPE in (:types) order by R.ORD");

		if (range != null) {
			sql.insert(0, "select * from(");
			sql.append(") where IDX between :from and :to");
			params.put("from", range.getOffset());
			params.put("to", range.getOffset() + range.getLimit());
		}

		return new Pair<String, Map<String, Object>>(sql.toString(), params);

	}

	@Override
	public DataRow<Cell> mapRow(ResultSet rs, int rowNum) throws SQLException {
		List<Cell> cells = FormDataUtils.createCells(fd.getFormColumns(),
				fd.getFormStyles());
		for (Cell cell : cells) {
			// Values
			CellValueExtractor extr = getCellValueExtractor(cell.getColumn());
			cell.setValue(extr.getValue(rs,
					String.format("V%s", cell.getColumn().getId())));
			// Styles
			BigDecimal styleId = rs.getBigDecimal(String.format("S%s", cell
					.getColumn().getId()));
			cell.setStyleId(styleId != null ? styleId.intValueExact() : null);
			// Editable
			cell.setEditable(rs.getBoolean(String.format("E%s", cell
					.getColumn().getId())));
			// Span Info
			int rowSpan = rs.getInt(String.format("RSI%s", cell.getColumn()
					.getId()));
			cell.setRowSpan(rowSpan == 0 ? 1 : rowSpan);
			int colSpan = rs.getInt(String.format("CSI%s", cell.getColumn()
					.getId()));
			cell.setColSpan(colSpan == 0 ? 1 : colSpan);
		}
		DataRow<Cell> dataRow = new DataRow<Cell>(rs.getString("A"), cells);
		dataRow.setId(rs.getLong("ID"));
		dataRow.setIndex(rs.getInt("IDX"));
		return dataRow;
	}
}
