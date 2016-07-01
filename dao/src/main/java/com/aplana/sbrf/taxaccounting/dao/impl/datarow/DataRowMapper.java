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

/**
 * @author sgoryachkin
 *
 */
class DataRowMapper implements RowMapper<DataRow<Cell>> {

    //private static final Log LOG = LogFactory.getLog(DataRowMapper.class);

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
            sql.append("+ (SELECT count(*) from form_data_").append(formData.getFormTemplateId())
                    .append(" WHERE (alias IS NULL OR alias LIKE '%").append(ALIASED_WITH_AUTO_NUMERATION_AFFIX).append("%') AND")
                    .append(" form_data_id = :formDataId AND temporary = :temporary AND manual = :manual")
                    .append(" AND ord < :from)");
        }
        sql.append(" numeration");
        getColumnNamesString(formData, sql);
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
     * @return
     */
    static Map<Integer, String[]> getColumnNames(FormData formData) {
        Map<Integer, String[]> columnNames = new HashMap<Integer, String[]>();
        for (Column column : formData.getFormColumns()){
            String id = ('c' + column.getId().toString()).intern();
            columnNames.put(column.getId(), new String[]{
                    id,
                    (id + "_style_id").intern(),
                    (id + "_editable").intern(),
                    (id + "_colspan").intern(),
                    (id + "_rowspan").intern()});
        }
        return columnNames;
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
                cell.setValue(getCellValue(cell.getColumn(), rs, String.format("c%s", columnId)), rowNum, true);
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
        dataRow.setId(SqlUtils.getLong(rs, "id"));
        dataRow.setIndex(SqlUtils.getInteger(rs,"ord"));
        return dataRow;
    }

    /**
     * Извлекает значение ячейки в зависимости от её типа
     * @param column
     * @param rs
     * @param columnLabel
     * @return
     * @throws SQLException
     */
    private Object getCellValue(Column column, ResultSet rs, String columnLabel) throws SQLException {
        switch (column.getColumnType()) {
            case STRING:
                return rs.getString(columnLabel);
            case DATE:
                return rs.getDate(columnLabel);
            default:
                return rs.getBigDecimal(columnLabel);
        }
    }
}