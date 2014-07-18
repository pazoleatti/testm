package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.aplana.sbrf.taxaccounting.web.widget.cell.SortingHeaderCell;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.ResizableHeader;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.ui.CustomScrollPanel;
import com.google.gwt.user.client.ui.HeaderPanel;


import static com.google.gwt.dom.client.Style.Unit;

public class GenericDataGrid<T> extends DataGrid<T>{

    private DivElement glassElement;
    private boolean enabled;

	public GenericDataGrid() {
		super(15, GWT.<GenericDataGridResources>create(GenericDataGridResources.class));
        HeaderPanel headerPanel = (HeaderPanel) getWidget();
        final CustomScrollPanel scrollPanel = (CustomScrollPanel ) headerPanel.getContentWidget();
        scrollPanel.getWidget().addStyleName("AplanaScrollPanel");

        glassElement = WidgetUtils.createGlassElement();
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

    public void addResizableColumn(Column<T, ?> col, String headerString, double width, Unit unit){
        super.addColumn(col, this.createResizableHeader(headerString, col));
        setColumnWidth(col, width, unit);
    }

    public void addResizableColumn(Column<T, ?> col, String headerString, AbstractCell<String> cell, double width, Unit unit){
        super.addColumn(col, this.createResizableHeader(headerString, col, cell));
        setColumnWidth(col, width, unit);
    }

    public void addResizableSortableColumn(Column<T, ?> col, String headerString, double width, Unit unit){
        addResizableColumn(col, headerString, new SortingHeaderCell());
        setColumnWidth(col, width, unit);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            getElement().removeChild(glassElement);
        } else {
            getElement().appendChild(glassElement);
        }
    }

    public boolean getEnabled(){
        return enabled;
    }

    public void removeAllColumns() {
        int columnCount = getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            removeColumn(0);
        }
    }

    public void addColumn(Column<T, ?> col, double width, Unit unit) {
        super.addColumn(col);
        setColumnWidth(col, width, unit);
    }

    public void addColumn(Column<T, ?> col, Header<?> header, double width, Unit unit) {
        super.addColumn(col, header);
        setColumnWidth(col, width, unit);
    }

    public void addColumn(Column<T, ?> col, Header<?> header, Header<?> footer, double width, Unit unit) {
        super.addColumn(col, header, footer);
        setColumnWidth(col, width, unit);
    }

    public void addColumn(Column<T, ?> col, String headerString, double width, Unit unit) {
        super.addColumn(col, headerString);
        setColumnWidth(col, width, unit);
    }

    public void addColumn(Column<T, ?> col, SafeHtml headerHtml, double width, Unit unit) {
        super.addColumn(col, headerHtml);
        setColumnWidth(col, width, unit);
    }

    public void addColumn(Column<T, ?> col, String headerString, String footerString, double width, Unit unit) {
        super.addColumn(col, headerString, footerString);
        setColumnWidth(col, width, unit);
    }

    public void addColumn(Column<T, ?> col, SafeHtml headerHtml, SafeHtml footerHtml, double width, Unit unit) {
        super.addColumn(col, headerHtml, footerHtml);
        setColumnWidth(col, width, unit);
    }

}
