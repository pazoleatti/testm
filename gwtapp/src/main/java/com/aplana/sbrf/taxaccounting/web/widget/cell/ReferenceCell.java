package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.ReferenceColumn;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Ячейка-ссылка на другую справочную ячейку
 * 
 * @author Dmitriy Levykin
 *
 */
public class ReferenceCell extends com.google.gwt.cell.client.AbstractCell<Object> {
    private ReferenceColumn column;

    public ReferenceCell(ColumnContext columnContext) {
        this.column = (ReferenceColumn) columnContext.getColumn();
    }

    @Override
    public void render(com.google.gwt.cell.client.Cell.Context context, Object value, SafeHtmlBuilder sb) {
        DataRow<com.aplana.sbrf.taxaccounting.model.Cell> dataRow = (DataRow<com.aplana.sbrf.taxaccounting.model.Cell>) context.getKey();
        Cell cell = dataRow.getCell(column.getAlias());
        String rendValue = cell.getRefBookDereference();
        if (rendValue == null) {
            rendValue = "";
        } else {
            rendValue = column.getFormatter().format(rendValue);
        }
        SafeHtml safeValue = SafeHtmlUtils.fromString(String.valueOf(rendValue));
        sb.append(safeValue);
    }
}