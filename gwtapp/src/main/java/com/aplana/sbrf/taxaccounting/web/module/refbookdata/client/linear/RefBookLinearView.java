package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.linear;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.HorizontalAlignment;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;

/**
 * User: avanteev
 */
public class RefBookLinearView extends ViewWithUiHandlers<RefBookDataLinearUiHandlers> implements RefBookLinearPresenter.MyView {

    interface Binder extends UiBinder<Widget, RefBookLinearView> { }

    SingleSelectionModel<RefBookDataRow> selectionModel = new SingleSelectionModel<RefBookDataRow>();

    @UiField
    GenericDataGrid<RefBookDataRow> refBookDataTable;
    @UiField
    FlexiblePager pager;

    @Inject
    @UiConstructor
    public RefBookLinearView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        refBookDataTable.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                getUiHandlers().onSelectionChanged();
            }
        });
        pager.setDisplay(refBookDataTable);
        pager.setPageSize(500);
        refBookDataTable.setPageSize(pager.getPageSize());
        refBookDataTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.DISABLED);
        refBookDataTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(refBookDataTable));
        refBookDataTable.getColumnSortList().setLimit(1);
    }

    @Override
    public RefBookDataRow getSelectedRow() {
        return selectionModel.getSelectedObject();
    }

    @Override
    public int getPage() {
        return pager.getPage();
    }

    @Override
    public int getPageStart() {
        return pager.getPageStart();
    }

    @Override
    public void setPage(int page) {
        pager.setPage(page);
    }

    @Override
    public void updateMode(FormMode mode) {
        refBookDataTable.setEnabled(mode != FormMode.CREATE);
    }

    @Override
    public int getSortColumnIndex() {
        if (refBookDataTable.getColumnSortList().size() == 0) {
            return 0;
        }
        return refBookDataTable.getColumnIndex((Column<RefBookDataRow,?>) refBookDataTable.getColumnSortList().get(0).getColumn());
    }

    @Override
    public boolean isAscSorting() {
        return refBookDataTable.getColumnSortList().size() == 0 || refBookDataTable.getColumnSortList().get(0).isAscending();
    }

    @Override
    public Integer getSelectedRowIndex() {
        List<RefBookDataRow> visibleItems = refBookDataTable.getVisibleItems();
        RefBookDataRow selectedItem = selectionModel.getSelectedObject();
        for(int i = 0; i < visibleItems.size(); i++) {
            if (visibleItems.get(i) == selectedItem)
                return i;
        }
        return null;
    }

    @Override
    public void setEnable(FormMode mode) {
        switch (mode){
            case EDIT:
            case VIEW:
            case READ:
                refBookDataTable.setEnabled(true);
                break;
            case CREATE:
                refBookDataTable.setEnabled(false);
                break;
        }
    }

    @Override
    public void deleteRowButtonClicked() {
        Dialog.confirmMessage("Удаление элемента справочника", "Вы подтверждаете удаление всех версий элемента?", new DialogHandler() {
            @Override
            public void yes() {
                if (getUiHandlers() != null) {
                    getUiHandlers().onDeleteRowClicked();
                }
                Dialog.hideMessage();
            }

            @Override
            public void no() {
                Dialog.hideMessage();
            }

            @Override
            public void close() {
                no();
            }
        });
    }

    @Override
    public int getTotalCount() {
        return refBookDataTable.getRowCount();
    }

    @Override
    public void setTableColumns(List<RefBookColumn> columns) {
        for (final RefBookColumn header : columns) {
            Column column;
            if (Formats.BOOLEAN.equals(header.getFormat())) {
                column = new Column<RefBookDataRow, Boolean>(new AbstractCell<Boolean>() {
                    @Override
                    public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
                        sb.append(value != null && value ? WidgetUtils.UNCHECKABLE_TRUE : WidgetUtils.UNCHECKABLE_FALSE);
                    }
                }) {
                    @Override
                    public Boolean getValue(RefBookDataRow object) {
                        String s = object.getValues().get(header.getAlias());
                        if (s != null && !s.trim().isEmpty()) {
                            try {
                                long l = Long.parseLong(s.trim());
                                return l > 0;
                            } catch (NumberFormatException e) {
                                return false;
                            }

                        } else {
                            return false;
                        }
                    }
                };
                column.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
            } else {
                column = new TextColumn<RefBookDataRow>() {
                    @Override
                    public String getValue(RefBookDataRow object) {
                        return object.getValues().get(header.getAlias());
                    }
                };

                column.setHorizontalAlignment(convertAlignment(header.getAlignment()));
            }
            column.setSortable(true);
            refBookDataTable.addResizableColumn(column, header.getName());
            refBookDataTable.setColumnWidth(column, header.getWidth(), Style.Unit.EM);
        }
    }

    @Override
    public void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows, Long selectedItem) {
        if (dataRows == null) {
            refBookDataTable.setRowCount(0);
            refBookDataTable.setRowData(new ArrayList<RefBookDataRow>());
        } else {
            if (totalCount == 0) {
                start = 0;
                pager.setPage(0);
            }
            refBookDataTable.setRowCount(totalCount);
            refBookDataTable.setRowData(start, dataRows);
            if (selectedItem != null) {
                for(RefBookDataRow item: dataRows) {
                    if (item.getRefBookRowId().equals(selectedItem)) {
                        selectionModel.setSelected(item, true);
                        break;
                    }
                }
            }
            //Если не было среди записей необходимой, то выставляем на первую
            if (selectionModel.getSelectedObject()==null&&!dataRows.isEmpty()){
                selectionModel.setSelected(dataRows.get(0), true);
            }
        }
    }

    @Override
    public void setSelected(Long recordId) {
        selectionModel.clear();
        int i = 0;
        for (RefBookDataRow row : refBookDataTable.getVisibleItems()) {

            if (row.getRefBookRowId().equals(recordId)) {
                selectionModel.setSelected(row, true);
                refBookDataTable.setKeyboardSelectedRow(i, true);
                return;
            }
            i++;
        }
        //Значит, что в справочнике запись добавилась на другую страницу
        if (selectionModel.getSelectedObject()==null && !refBookDataTable.getVisibleItems().isEmpty()){
            selectionModel.setSelected(refBookDataTable.getVisibleItems().get(0), true);
        }
    }

    @Override
    public void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data) {
        refBookDataTable.setPageSize(pageSize);
        data.addDataDisplay(refBookDataTable);
    }

    @Override
    public int getPageSize() {
        return pager.getPageSize();
    }

    @Override
    public void setRange(Range range) {
        refBookDataTable.setVisibleRangeAndClearData(range, true);
    }

    @Override
    public void updateTable() {
        selectionModel.clear();
        Range range = new Range(pager.getPageStart(), pager.getPageSize());
        refBookDataTable.setVisibleRangeAndClearData(range, true);
    }

    @Override
    public void resetRefBookElements() {
        int i;
        while ((i = refBookDataTable.getColumnCount()) != 0) {
            refBookDataTable.removeColumn(i - 1);
        }
    }

    private HasHorizontalAlignment.HorizontalAlignmentConstant convertAlignment(HorizontalAlignment alignment) {
        switch (alignment) {
            case ALIGN_LEFT:
                return HasHorizontalAlignment.ALIGN_LEFT;
            case ALIGN_CENTER:
                return HasHorizontalAlignment.ALIGN_CENTER;
            case ALIGN_RIGHT:
                return HasHorizontalAlignment.ALIGN_RIGHT;
            default:
                return HasHorizontalAlignment.ALIGN_LEFT;
        }
    }
}
