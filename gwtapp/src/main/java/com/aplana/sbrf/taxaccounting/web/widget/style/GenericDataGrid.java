package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomTableBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.ResizableHeader;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.ui.*;


import java.util.List;

import static com.google.gwt.dom.client.Style.Unit;

public class GenericDataGrid<T> extends DataGrid<T> implements HasEnabled{

    private DivElement glassElement;
    private boolean enabled;
    private CustomTableBuilder<T> tableBuilder;

	public GenericDataGrid() {
		super(15, GWT.<GenericDataGridResources>create(GenericDataGridResources.class));
        HeaderPanel headerPanel = (HeaderPanel) getWidget();
        final CustomScrollPanel scrollPanel = (CustomScrollPanel ) headerPanel.getContentWidget();
        scrollPanel.getWidget().addStyleName("AplanaScrollPanel");

        glassElement = WidgetUtils.createGlassElement();
	}

    public CustomScrollPanel getContentPanel() {
        return (CustomScrollPanel) ((HeaderPanel) getWidget()).getContentWidget();
    }

    public Widget getHeaderWidget() {
        return ((HeaderPanel) getWidget()).getHeaderWidget();
    }

    public TableSectionElement getTableHeadElement() {
        return super.getTableHeadElement();
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

    public void addResizableColumn(Column<T, ?> col, String headerString, double width, Unit unit){
        super.addColumn(col, this.createResizableHeader(headerString, col));
        setColumnWidth(col, width, unit);
    }

    public void addResizableColumn(Column<T, ?> col, String headerString, AbstractCell<String> cell, double width, Unit unit){
        super.addColumn(col, this.createResizableHeader(headerString, col, cell));
        setColumnWidth(col, width, unit);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        getElement().appendChild(glassElement);
        if (enabled) {
            getElement().removeChild(glassElement);
        }
    }

    @Override
    public boolean isEnabled(){
        return enabled;
    }

    public void removeAllColumns() {
        int columnCount = getColumnCount();
        for (int i = 0; i < columnCount; i++) {
//            setColumnWidth(getColumn(0), null);
//            clearColumnWidth(getColumn(0));
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

    @Override
    public void setTableBuilder(CellTableBuilder<T> tableBuilder) {
        super.setTableBuilder(tableBuilder);
        if (tableBuilder instanceof CustomTableBuilder) {
            this.tableBuilder = (CustomTableBuilder)tableBuilder;
        } else {
            this.tableBuilder = null;
        }
    }

    @Override
    public void setRowData(int start, List<? extends T> values) {
        super.setRowData(start, values);
        if (tableBuilder != null)
            tableBuilder.clearGlobalSpans();
    }
}
