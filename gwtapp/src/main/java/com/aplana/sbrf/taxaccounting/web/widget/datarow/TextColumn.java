package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ReadOnlyStringCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;

public class TextColumn extends DataRowColumn<String> {

    public TextColumn(StringColumn col, Boolean isReadOnly) {
        super(isReadOnly ? new ReadOnlyStringCell() : new EditTextCell(), col);
        this.setFieldUpdater(new FieldUpdater<DataRow, String>() {
			@Override
			public void update(int index, DataRow dataRow, String value) {
				dataRow.put(getAlias(), value);
			}
		});
    }

    @Override
    public String getValue(DataRow dataRow) {
    	String value = (String)dataRow.get(alias); 
        return value == null ? "" : value;
    }
}
