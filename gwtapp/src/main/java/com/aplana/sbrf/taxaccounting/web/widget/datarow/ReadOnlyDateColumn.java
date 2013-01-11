package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import java.util.Date;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ReadOnlyDateCell;
import com.google.gwt.i18n.client.DateTimeFormat;

/** @author Vitalii Samolovskikh
 * Колонка даты БЕЗ возможности редактирования 
 */
public class ReadOnlyDateColumn extends DataRowColumn<Date> {

    private static final DateTimeFormat FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");

    public ReadOnlyDateColumn(DateColumn dateColumn) {
    		super(new ReadOnlyDateCell(FORMAT), dateColumn);
    }

    @Override
    public Date getValue(DataRow dataRow) {
        return (Date) dataRow.get(alias);
    }
}
