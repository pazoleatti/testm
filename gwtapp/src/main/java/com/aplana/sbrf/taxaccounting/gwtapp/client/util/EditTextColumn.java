package com.aplana.sbrf.taxaccounting.gwtapp.client.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.user.cellview.client.Column;

/** @author Vitalii Samolovskikh */
public class EditTextColumn extends Column<DataRow, String> {
    private String alias;

    public EditTextColumn() {
        super(new EditTextCell());
    }

    public EditTextColumn(String alias) {
        super(new EditTextCell());
        this.alias = alias;
    }

    @Override
    public String getValue(DataRow dataRow) {
        return String.valueOf(dataRow.get(alias));
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
