package com.aplana.sbrf.taxaccounting.web.module.formdata.client.search;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchResult;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.List;

/**
 * Представление для попапа с формой поска данных
 * внутри НФ
 *
 * @author auldanov
 * Created on 27.03.2014.
 */
public class FormSearchView extends PopupViewWithUiHandlers<FormSearchUiHandlers> implements FormSearchPresenter.MyView {

    public interface Binder extends UiBinder<PopupPanel, FormSearchView> {
    }

    private final PopupPanel widget;

    private AsyncDataProvider<FormDataSearchResult> dataProvider = new  AsyncDataProvider<FormDataSearchResult>() {
        @Override
        protected void onRangeChanged(HasData<FormDataSearchResult> display) {
            if (getUiHandlers() != null){
                Range range = display.getVisibleRange();
                getUiHandlers().onRangeChange(range.getStart(), range.getLength());
            }
        }
    };

    @UiField
    DataGrid<FormDataSearchResult> searchResultTable;
    @UiField
    TextBox filterText;
    @UiField
    Button search;
    @UiField
    FlexiblePager pager;

    @Inject
    public FormSearchView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
        init();
    }

    private void init(){
        dataProvider = new AsyncDataProvider() {
            @Override
            protected void onRangeChanged(HasData display) {
                if (getUiHandlers() != null){
                    final Range range = display.getVisibleRange();
                    getUiHandlers().onRangeChange(range.getStart(), range.getLength());
                }
            }
        };

        pager.setDisplay(searchResultTable);
        searchResultTable.setPageSize(pager.getPageSize());

        dataProvider.addDataDisplay(searchResultTable);

        TextColumn<FormDataSearchResult> counterColumn = new TextColumn<FormDataSearchResult>() {
            @Override
            public String getValue(FormDataSearchResult object) {
                return object.getIndex().toString();
            }
        };
        counterColumn.setFieldUpdater(new FieldUpdater<FormDataSearchResult, String>() {
            @Override
            public void update(int index, FormDataSearchResult object, String value) {
                object.setIndex(new Long(value));
            }
        });

        TextColumn<FormDataSearchResult> rowIndexColumn = new TextColumn<FormDataSearchResult>() {
            @Override
            public String getValue(FormDataSearchResult object) {
                return object.getRowIndex().toString();
            }
        };

        TextColumn<FormDataSearchResult> columnIndexColumn = new TextColumn<FormDataSearchResult>() {
            @Override
            public String getValue(FormDataSearchResult object) {
                Integer columnId = object.getColumnIndex().intValue();
                columnId -= getUiHandlers().getHiddenColumnsCountBefore(columnId);
                return columnId.toString();
            }
        };

        Column<FormDataSearchResult, String> valueColumn = new Column<FormDataSearchResult, String>(new ClickableTextCell()){

            @Override
            public String getValue(FormDataSearchResult object)  {
                return object.getStringFound();
            }

            @Override
            public void render(Cell.Context context, FormDataSearchResult object, SafeHtmlBuilder sb) {
                LinkAnchor linkAnchor = new LinkAnchor();
                linkAnchor.setText(object.getStringFound());
                linkAnchor.setDisableImage(true);
                sb.appendHtmlConstant(linkAnchor.getHTML());
            }
        };

        valueColumn.setCellStyleNames("linkCell");

        valueColumn.setFieldUpdater(new FieldUpdater<FormDataSearchResult, String>() {
            @Override
            public void update(int index, FormDataSearchResult object, String value) {
                getUiHandlers().onClickFoundItem(object.getRowIndex());
            }
        });

        searchResultTable.addColumn(counterColumn, "№");
        searchResultTable.addColumn(rowIndexColumn, "Строка");
        searchResultTable.addColumn(columnIndexColumn, "Графа");
        searchResultTable.addColumn(valueColumn, "Значение");
        searchResultTable.setRowCount(0);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @UiHandler("search")
    public void onSearchClicked(ClickEvent event){
        getUiHandlers().onRangeChange(0, pager.getPageSize());
    }

    @Override
    public String getSearchKey() {
        return filterText.getText();
    }

    @Override
    public void setTableData(int start, List<FormDataSearchResult> resultList, int size) {
        searchResultTable.setRowData(start, resultList);
        searchResultTable.setRowCount(size, true);
    }

    @Override
    public void updateData(int pageNumber) {
        if (pager.getPage() == pageNumber){
            updateData();
        } else {
            pager.setPage(pageNumber);
        }
    }

    @Override
    public void updatePageSize() {
        searchResultTable.setPageSize(pager.getPageSize());
    }

    @Override
    public void updateData() {
        searchResultTable.setVisibleRangeAndClearData(searchResultTable.getVisibleRange(), true);
    }

    @Override
    public void clearTableData() {
        searchResultTable.setRowCount(0);
    }

    @Override
    public void clearSearchInput() {
        filterText.setText("");
    }
}