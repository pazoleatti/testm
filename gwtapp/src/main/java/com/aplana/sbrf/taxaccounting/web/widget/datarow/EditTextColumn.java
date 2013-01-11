package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import java.math.BigDecimal;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;

/**
 * 
 * @author Eugene Stetsenko
 * Текстовая колонка С возможностью редактирования
 *
 */
public class EditTextColumn extends DataRowColumn<String> {

    public EditTextColumn(StringColumn col) {
        super(new EditTextCell(), col);
        this.setFieldUpdater(new FieldUpdater<DataRow, String>() {
			@Override
			public void update(int index, DataRow dataRow, String value) {
				dataRow.put(getAlias(), value);
			}
		});
    }

    @Override
    public String getValue(DataRow dataRow) {
    	String value = null;
    	if (dataRow.get(alias) instanceof BigDecimal) {
    		value = ((BigDecimal) dataRow.get(alias)).toPlainString();
    	} else if (dataRow.get(alias) instanceof String){
    		value = (String) dataRow.get(alias);
    	}
    	return value == null ? "" : value;
    }
}
