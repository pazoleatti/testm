package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.aplana.sbrf.taxaccounting.web.widget.cell.SortingHeaderCell;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.ResizableHeader;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.CustomScrollPanel;
import com.google.gwt.user.client.ui.HeaderPanel;

public class GenericDataGrid<T> extends DataGrid<T>{

	public GenericDataGrid() {
		super(15, GWT.<GenericDataGridResources>create(GenericDataGridResources.class));
        HeaderPanel headerPanel = (HeaderPanel) getWidget();
        final CustomScrollPanel scrollPanel = (CustomScrollPanel ) headerPanel.getContentWidget();
        scrollPanel.getWidget().addStyleName("AplanaScrollPanel");
	}

    public class DataGridResizableHeader extends ResizableHeader<T> {
        public DataGridResizableHeader(String title, Column<T, ?> column) {
            super(title, GenericDataGrid.this, column);
        }

        public DataGridResizableHeader(String title, Column<T, ?> column, AbstractCell<String> cell) {
            super(title, GenericDataGrid.this, column, cell);
        }

        @Override
        protected int getTableBodyHeight() {
            return GenericDataGrid.this.getTableBodyElement().getOffsetHeight();
        }

        public void setUpdater(ValueUpdater<String> updater) {
            super.setUpdater(updater);
        }
    }

    public DataGridResizableHeader createResizableHeader(String title, Column<T, ?> column) {
        return new DataGridResizableHeader(title, column);
    }

    public DataGridResizableHeader createResizableHeader(String title, Column<T, ?> column, AbstractCell<String> cell) {
        return new DataGridResizableHeader(title, column, cell);
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
