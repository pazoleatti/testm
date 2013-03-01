package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import java.util.Date;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.cell.NullableDatePickerCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.i18n.client.DateTimeFormat;

/**  
 * 
 * @author Vitalii Samolovskikh
 * Колонка даты С возможностью редактирования 
 *
 */
public class EditDateColumn extends DataRowColumn<Date> {

    private static final DateTimeFormat FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");

    public EditDateColumn(DateColumn dateColumn, ColumnContext columnContext) {
    	
    	super(new NullableDatePickerCell(FORMAT, columnContext), dateColumn);
	    this.setHorizontalAlignment(ALIGN_CENTER);
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
