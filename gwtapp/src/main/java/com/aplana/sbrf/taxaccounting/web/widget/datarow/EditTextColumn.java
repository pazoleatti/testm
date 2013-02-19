package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import java.math.BigDecimal;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.cell.TextValidationStrategy;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ValidatedInputCell;
import com.google.gwt.cell.client.FieldUpdater;

/**
 * 
 * @author Eugene Stetsenko
 * Текстовая колонка С возможностью редактирования
 *
 */
public class EditTextColumn extends DataRowColumn<String> {

    public EditTextColumn(StringColumn col, ColumnContext columnContext) {
        super(new ValidatedInputCell(new TextValidationStrategy(col.getMaxLength()), columnContext), col);
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
