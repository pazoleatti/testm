package com.aplana.sbrf.taxaccounting.gwtapp.client.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.google.gwt.cell.client.DatePickerCell;
import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

/** @author Vitalii Samolovskikh */
public class EditDateColumn extends AliasedColumn<Date> {

    private static final DateTimeFormat FORMAT = DateTimeFormat.getFormat("d.MM.y");

    public EditDateColumn(String alias) {
        super(new DatePickerCell(FORMAT), alias);
    }

    @Override
    public Date getValue(DataRow dataRow) {
        return (Date) dataRow.get(alias);
    }
}
