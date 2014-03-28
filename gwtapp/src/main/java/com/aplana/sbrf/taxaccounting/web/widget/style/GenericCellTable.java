package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.aplana.sbrf.taxaccounting.web.widget.cell.SortingHeaderCell;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.ResizableHeader;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;

public class GenericCellTable<T> extends CellTable<T>{
	
	public GenericCellTable() {
		super(20, GWT.<GenericCellTableResources> create(GenericCellTableResources.class));
		this.setStyleName("genericTable");
	}

    public class TableCellResizableHeader extends ResizableHeader<T> {

        public TableCellResizableHeader(String title, Column<T, ?> column) {
            super(title, GenericCellTable.this, column);
        }

        public TableCellResizableHeader(String title, Column<T, ?> column, AbstractCell<String> cell) {
            super(title, GenericCellTable.this, column, cell);
        }

        @Override
        protected int getTableBodyHeight() {
            return GenericCellTable.this.getTableBodyElement().getOffsetHeight();
        }

        public void setUpdater(ValueUpdater<String> updater) {
            super.setUpdater(updater);
        }
    }

    public TableCellResizableHeader createResizableHeader(String title, Column<T, ?> column) {
        return new TableCellResizableHeader(title, column);
    }

    public TableCellResizableHeader createResizableHeader(String title, Column<T, ?> column, AbstractCell<String> cell) {
        return new TableCellResizableHeader(title, column, cell);
    }

    public void addResizableColumn(Column<T, ?> col, String headerString){
        super.addColumn(col, this.createResizableHeader(headerString, col));
    }

    public void addResizableColumn(Column<T, ?> col, String headerString, AbstractCell<String> cell){
        super.addColumn(col, this.createResizableHeader(headerString, col, cell));
    }

    public void addResizableSortableColumn(Column<T, ?> col, String headerString){
        addResizableColumn(col, headerString, new SortingHeaderCell());
    }
}
