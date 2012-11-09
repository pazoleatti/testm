package com.aplana.sbrf.taxaccounting.gwtapp.client.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

/** @author Vitalii Samolovskikh */
public abstract class AliasedColumn<T> extends Column<DataRow, T> {
    protected String alias;

    public AliasedColumn(Cell<T> cell) {
        super(cell);
    }

    protected AliasedColumn(Cell<T> cell, String alias) {
        super(cell);
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
