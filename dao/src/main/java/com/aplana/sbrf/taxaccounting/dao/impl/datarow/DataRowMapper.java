package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

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

import static com.aplana.sbrf.taxaccounting.dao.impl.datarow.DataRowDaoImplUtils.*;

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

        String[] prefixs = new String[]{"V", "S", "E", "CSI", "RSI"};

        StringBuilder select = new StringBuilder("select row_number() over (order by sub.ORD) as IDX, sub.id, sub.a");

        // генерация max(X) X
        for (Column c : fd.getFormColumns()){
            for (String prefix : prefixs){
                select
                    .append(", max(")
                    .append(prefix)
                    .append(c.getId())
                    .append(") ")
                    .append(prefix)
                    .append(c.getId())
                    .append("\n");
            }
        }

        select.append(" from (with C as");
        select.append("     (select COLUMN_ID, ROW_ID, max(STYLE_ID) STYLE_ID, max(EDIT) EDIT, max(COLSPAN) COLSPAN, max(ROWSPAN) ROWSPAN, max(NV) nVALUE, max(DV) dVALUE, max(SV) sVALUE from \n");
        select.append("         (select COLUMN_ID, ROW_ID, STYLE_ID, null as EDIT, null as COLSPAN, null as ROWSPAN, null as NV, null as DV, null as SV \n");
        select.append("             from CELL_STYLE N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId \n");
        select.append("             union all \n");
        select.append("             select COLUMN_ID, ROW_ID,null, 1 as EDIT, null, null, null, null, null from CELL_EDITABLE N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId \n");
        select.append("             union all \n");
        select.append("             select COLUMN_ID, ROW_ID, null, null, COLSPAN, ROWSPAN, null, null, null from CELL_SPAN_INFO N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId \n");
        select.append("             union all \n");
        select.append("             select COLUMN_ID, ROW_ID, null, null, null, null, VALUE, null, null from NUMERIC_VALUE N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId \n");
        select.append("             union all \n");
        select.append("             select COLUMN_ID, ROW_ID,null, null, null, null,null, VALUE, null from DATE_VALUE N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId \n");
        select.append("             union all \n");
        select.append("             select COLUMN_ID, ROW_ID,null, null, null, null, null, null, VALUE from STRING_VALUE N join DATA_ROW RR on RR.ID = N.ROW_ID and RR.FORM_DATA_ID = :formDataId) \n");
        select.append("     group by COLUMN_ID, ROW_ID) \n");

        select.append(" select \n" +
                            " d.ID as ID,\n" +
                            " d.ALIAS as A,\n" +
                            " d.ord as ord");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("formDataId", fd.getId());
        params.put("types", TypeFlag.rtsToKeys(types));

        // генерация "case when C.COLUMN_ID=18740 then C.STYLE_ID else null end S18740,"
        for (Column c : fd.getFormColumns()){
            params.put(String.format("column%sId", c.getId()), c.getId());
            String columnId =  String.format(":column%sId", c.getId());

            char valuePrefix = 's';
            if (c instanceof StringColumn) {
                valuePrefix = 's';
            } else if (c instanceof NumericColumn || c instanceof RefBookColumn) {
                valuePrefix = 'n';
            } else if (c instanceof DateColumn) {
                valuePrefix = 'd';
            } else {
                throw new IllegalArgumentException();
            }

            select.append(",case when C.COLUMN_ID=").append(columnId).append(" then C.").append(valuePrefix).append("VALUE else null end V").append(c.getId()).append(",\n")
                    .append(" case when C.COLUMN_ID=").append(columnId).append(" then C.STYLE_ID else null end S").append(c.getId()).append(",\n")
                    .append(" case when C.COLUMN_ID=").append(columnId).append(" then C.EDIT else 0 end E").append(c.getId()).append(",\n")
                    .append(" case when C.COLUMN_ID=").append(columnId).append(" then C.COLSPAN else null end CSI").append(c.getId()).append(",\n")
                    .append(" case when C.COLUMN_ID=").append(columnId).append(" then C.ROWSPAN else null end RSI").append(c.getId()).append("");
        }

        select.append(" FROM data_row d left join c on d.id = c.row_id \n"+
                "where d.FORM_DATA_ID = :formDataId and d.TYPE IN (:types)) sub \n"+
                "group by sub.id, sub.ord, sub.a \n " +
                "order by sub.ord");

        StringBuilder sql = new StringBuilder();
        sql.append(select);
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
