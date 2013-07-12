package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.cell.TextDictionaryCell;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEvent;
import com.google.gwt.cell.client.FieldUpdater;
/**
 * 
 * @author Eugene Stetsenko
 * Колонка с выбором из справочника
 *
 */
public class EditStringDictionaryColumn extends DataRowColumn<String> {
    public EditStringDictionaryColumn(StringColumn stringColumn, ColumnContext columnContext) {
    	super(new TextDictionaryCell(stringColumn.getDictionaryCode(), columnContext), stringColumn);
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
    public String getValue(DataRow dataRow) {
        return (String) dataRow.get(alias);
    }
}
