package com.aplana.sbrf.taxaccounting.web.module.formdata.client.util;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

/** @author Vitalii Samolovskikh */
public abstract class DataRowColumn<T> extends Column<DataRow, T> {
    protected String alias;
    protected String rowGroup; 
    
    protected DataRowColumn(Cell<T> cell, com.aplana.sbrf.taxaccounting.model.Column col) {
        super(cell);
        this.alias = col.getAlias();
        this.rowGroup = col.getGroupName();
    }

    public String getAlias() {
        return alias;
    }
    
    public String getRowGroup() {
    	return rowGroup;
    }
}
