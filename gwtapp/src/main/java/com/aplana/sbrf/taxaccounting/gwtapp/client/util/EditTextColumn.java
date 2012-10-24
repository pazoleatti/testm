package com.aplana.sbrf.taxaccounting.gwtapp.client.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.google.gwt.cell.client.EditTextCell;

/** @author Vitalii Samolovskikh */
public class EditTextColumn extends AliasedColumn<String> {

    public EditTextColumn() {
        super(new EditTextCell());
    }

    public EditTextColumn(String alias) {
        super(new EditTextCell(), alias);
    }

    @Override
    public String getValue(DataRow dataRow) {
        return String.valueOf(dataRow.get(alias));
    }

}
