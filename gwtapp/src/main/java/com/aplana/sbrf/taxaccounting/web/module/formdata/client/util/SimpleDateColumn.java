package com.aplana.sbrf.taxaccounting.web.module.formdata.client.util;

import java.util.Date;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.NullableDatePickerCell;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ReadOnlyDateCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.i18n.client.DateTimeFormat;

/** @author Vitalii Samolovskikh */
public class SimpleDateColumn extends DataRowColumn<Date> {

    private static final DateTimeFormat FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");

    public SimpleDateColumn(DateColumn dateColumn, Boolean isReadOnly) {
    	
    		super(isReadOnly ? new ReadOnlyDateCell(FORMAT) : new NullableDatePickerCell(FORMAT), dateColumn);

        this.setFieldUpdater(new FieldUpdater<DataRow, Date>() {
			@Override
			public void update(int index, DataRow dataRow, Date value) {
				dataRow.put(getAlias(), value);
			}
		});
        
    }

    @Override
    public Date getValue(DataRow dataRow) {
        return (Date) dataRow.get(alias);
    }
}
