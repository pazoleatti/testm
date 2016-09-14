package com.aplana.sbrf.taxaccounting.web.module.formdata.client.search;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResult;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.utils.TextUtils;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
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

    private AsyncDataProvider<FormDataSearchResult> dataProvider = new AsyncDataProvider<FormDataSearchResult>() {
        @Override
        protected void onRangeChanged(HasData<FormDataSearchResult> display) {
            if (getUiHandlers() != null && !filterText.getText().isEmpty()){
                Range range = display.getVisibleRange();
                getUiHandlers().onRangeChange(range.getStart(), range.getLength());
            }
        }
    };

    private SingleSelectionModel<FormDataSearchResult> selectionModel;
    private boolean readOnlyMode;
    private boolean manual;

    @UiField
    DataGrid<FormDataSearchResult> searchResultTable;
    @UiField
    TextBox filterText;
    @UiField
    Button search;
    @UiField
    FlexiblePager pager;
    @UiField
    Label countLabel;
    @UiField
    Button close;
    @UiField
    ModalWindow modalWindow;
    @UiField
    CheckBox caseSensitive;

    HandlerRegistration handlerRegistration;

    @Inject
    public FormSearchView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
        widget.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                filterText.setFocus(true);
            }
        });
        init();

        handlerRegistration = Window.addWindowClosingHandler(new Window.ClosingHandler() {
            @Override
            public void onWindowClosing(Window.ClosingEvent event) {
                handlerRegistration.removeHandler();
                getUiHandlers().clearSearchResults();
            }
        });
    }

    private void init(){
        pager.setDisplay(searchResultTable);
        searchResultTable.setPageSize(pager.getPageSize());

        dataProvider.addDataDisplay(searchResultTable);

        selectionModel = new SingleSelectionModel<FormDataSearchResult>();

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
                String key = filterText.getText();
                String flags;
                if (!caseSensitive.getValue())
                    flags = "gi";
                else
                    flags = "g";
                String link =
                    "<p style=\"color: #0000CD\">" +
                            RegExp.compile(TextUtils.quote(key), flags).replace(object.getStringFound(), "<span style=\"color: #ff0000;\">$&</span>") +
                    "<p>";
                sb.appendHtmlConstant(link);
            }
        };

        valueColumn.setCellStyleNames("linkCell");

        searchResultTable.setSelectionModel(selectionModel);
        searchResultTable.addCellPreviewHandler(new CellPreviewEvent.Handler<FormDataSearchResult>(){
            @Override
            public void onCellPreview(final CellPreviewEvent<FormDataSearchResult> event) {
                if (Event.getTypeInt(event.getNativeEvent().getType()) == Event.ONCLICK) {
                    FormDataSearchResult selRow = event.getValue();
                    if (selRow != null){
                        getUiHandlers().onClickFoundItem(selRow.getRowIndex());
                    }
                }
            }
        });

        searchResultTable.addColumn(counterColumn, "№");
        searchResultTable.setColumnWidth(counterColumn, 50, Style.Unit.PX);
        searchResultTable.addColumn(rowIndexColumn, "Строка");
        searchResultTable.setColumnWidth(rowIndexColumn, 50, Style.Unit.PX);
        searchResultTable.addColumn(columnIndexColumn, "Графа");
        searchResultTable.setColumnWidth(columnIndexColumn, 50, Style.Unit.PX);
        searchResultTable.addColumn(valueColumn, "Значение");
        searchResultTable.setColumnWidth(valueColumn, 100, Style.Unit.PCT);
        searchResultTable.setRowCount(0);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @UiHandler("search")
    public void onSearchClicked(ClickEvent event){
        onSearch();
    }

    @SuppressWarnings("GwtUiHandlerErrors")
    @UiHandler("filterText")
    void onFilterPressClicked(KeyPressEvent event) {
        if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
            onSearch();
        }
    }

    private void onSearch() {
        if (!filterText.getText().isEmpty()){
            if (filterText.getText().length()>=3) {
                if (pager.getPage() != 0) {
                    pager.firstPage();
                } else {
                    getUiHandlers().onRangeChange(0, pager.getPageSize());
                }
            } else {
                Dialog.warningMessage("Слишком короткая строка запроса. Для поиска наберите не менее 3-х символов.");
            }
        } else {
            clearTableData();
        }
    }

    @UiHandler("close")
    public void onCloseClicked(ClickEvent event){
        modalWindow.hide();
    }

    @Override
    public String getSearchKey() {
        return filterText.getText();
    }

    @Override
    public void setSearchKey(String searchKey) {
        filterText.setText(searchKey);
    }

    @Override
    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
    }

    @Override
    public void setManual(boolean manual) {
        this.manual = manual;
    }

    @Override
    public void setTableData(int start, List<FormDataSearchResult> resultList, int size) {
        searchResultTable.setRowData(start, resultList);
        searchResultTable.setRowCount(size, true);
        countLabel.setText("Найдено: " + size);
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
    public void updateData() {
        searchResultTable.setVisibleRangeAndClearData(searchResultTable.getVisibleRange(), true);
    }

    @Override
    public void clearTableData() {
        searchResultTable.setRowCount(0);
        countLabel.setText("Найдено: 0");
        filterText.setFocus(true);
    }

    @Override
    public void clearSearchInput() {
        filterText.setText("");
    }

    @Override
    public boolean isCaseSensitive(){
        return caseSensitive.getValue();
    }

    @Override
    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    @Override
    public boolean isManual() {
        return manual;
    }

    @Override
    public void clearSelection() {
        selectionModel.clear();
    }
}