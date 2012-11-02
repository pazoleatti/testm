package com.aplana.sbrf.taxaccounting.gwtapp.client.util;

import com.aplana.sbrf.taxaccounting.gwtapp.cell.DictionaryCell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.google.gwt.cell.client.FieldUpdater;

public class EditStringDictionaryColumn extends DataRowColumn<String> {
    public EditStringDictionaryColumn(StringColumn stringColumn) {
    	super(new DictionaryCell(), stringColumn);
        this.setFieldUpdater(new FieldUpdater<DataRow, String>() {
			@Override
			public void update(int index, DataRow dataRow, String value) {
				dataRow.put(getAlias(), value);
			}
		});
    }

    @Override
    public String getValue(DataRow dataRow) {
        return (String) dataRow.get(alias);
    }
}
