package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import java.math.BigDecimal;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ValidatedInputCell;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEvent;
import com.google.gwt.cell.client.FieldUpdater;

/**
 * 
 * @author Eugene Stetsenko
 * Текстовая колонка С возможностью редактирования
 *
 */
public class EditTextColumn extends DataRowColumn<String> {

    public EditTextColumn(StringColumn col, ColumnContext columnContext) {
        super(new ValidatedInputCell(columnContext, false), col);
	    this.setHorizontalAlignment(ALIGN_LEFT);
        this.setFieldUpdater(new FieldUpdater<DataRow<Cell>, String>() {
			@Override
			public void update(int index, DataRow<Cell> dataRow, String value) {
				dataRow.put(getAlias(), value);
				CellModifiedEvent event = new CellModifiedEvent(dataRow);
				fireEvent(event);
			}
		});
    }

    @Override
    public String getValue(DataRow<Cell> dataRow) {
    	String value = null;
    	if (dataRow.get(alias) instanceof BigDecimal) {
    		value = ((BigDecimal) dataRow.get(alias)).toPlainString();
    	} else if (dataRow.get(alias) instanceof String){
    		value = (String) dataRow.get(alias);
            value = value.replaceAll("»", "\"");
            value = value.replaceAll("«", "\"");
    	}
    	return value == null ? "" : value;
    }
}
