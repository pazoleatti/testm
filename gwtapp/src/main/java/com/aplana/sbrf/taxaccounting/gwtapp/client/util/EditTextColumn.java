package com.aplana.sbrf.taxaccounting.gwtapp.client.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.google.gwt.cell.client.EditTextCell;

/** @author Vitalii Samolovskikh */
public class EditTextColumn extends DataRowColumn<String> {

    public EditTextColumn(StringColumn col) {
        super(new EditTextCell(), col);
    }

    @Override
    public String getValue(DataRow dataRow) {
        return String.valueOf(dataRow.get(alias));
    }

}
