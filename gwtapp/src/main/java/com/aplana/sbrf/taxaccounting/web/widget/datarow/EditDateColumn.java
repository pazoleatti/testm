package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.cell.*;
import com.google.gwt.cell.client.*;

import java.util.*;

/**
 *
 * @author Vitalii Samolovskikh
 * Колонка даты С возможностью редактирования
 *
 */
public class EditDateColumn extends DataRowColumn<Date> {

    public EditDateColumn(DateColumn dateColumn, ColumnContext columnContext) {
    	
    	super(new DateInputWithModesCell(columnContext), dateColumn);
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
