package com.aplana.sbrf.taxaccounting.web.module.formdata.client.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.DictionaryCell;
import com.aplana.sbrf.taxaccounting.web.widget.cell.TextDictionaryCell;
import com.google.gwt.cell.client.FieldUpdater;
/**
 * 
 * @author Eugene Stetsenko
 * Колонка с выбором из справочника
 *
 */
public class EditStringDictionaryColumn extends DataRowColumn<String> {
    public EditStringDictionaryColumn(StringColumn stringColumn) {
    	super(new TextDictionaryCell(stringColumn.getDictionaryCode()), stringColumn);
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
