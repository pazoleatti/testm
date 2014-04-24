package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.HorizontalAlignment;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.CheckHandler;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopup;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * View для формы "Загрузка бухгалтерской отчётности"
 *
 * @author Dmitriy Levykin
 */
public class BookerStatementsView extends ViewWithUiHandlers<BookerStatementsUiHandlers>
        implements BookerStatementsPresenter.MyView {

    interface Binder extends UiBinder<Widget, BookerStatementsView> {
    }

    // Выбранное подразделение
    private Integer currentDepartmentId;

    @UiField
    PeriodPickerPopup periodPickerPopup;

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    ListBox bookerReportType;

    @UiField
    FileUploadWidget fileUploader;

    @UiField
    Button searchButton;

    @UiField
    LinkButton deleteButton;

    @UiField
    GenericDataGrid<RefBookDataRow> dataTable;
    @UiField
    FlexiblePager pager;

    @Inject
    @UiConstructor
    public BookerStatementsView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        initListeners();

        dataTable.setSelectionModel(new NoSelectionModel<RefBookDataRow>());
        dataTable.setPageSize(pager.getPageSize());
        pager.setDisplay(dataTable);
        dataTable.setVisible(false);
        pager.setVisible(false);
        fileUploader.setCheckHandler(new CheckHandler() {
            @Override
            public boolean onCheck() {
                return getUiHandlers().isFilterFilled();
            }
        });
    }

    @Override
    public void init() {
        periodPickerPopup.setValue(null);
        departmentPicker.setValue(null);
    }

    private void initListeners() {
        // Подразделение
        departmentPicker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> event) {

                if (event == null || event.getValue().isEmpty()) {
                    return;
                }

                Integer selDepartmentId = event.getValue().iterator().next();

                // Проверка совпадения выбранного подразделения с текущим
                if (BookerStatementsView.this.currentDepartmentId != null
                        && BookerStatementsView.this.currentDepartmentId.equals(selDepartmentId)) {
                    return;
                }

                BookerStatementsView.this.currentDepartmentId = selDepartmentId;

            }
        });
    }

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartments) {
        departmentPicker.setAvalibleValues(departments, availableDepartments);
    }

    @Override
    public void setDepartment(final Department department) {
        if (department != null) {
            departmentPicker.setValue(new ArrayList<Integer>() {{
                add(department.getId());
            }});
        }
        this.currentDepartmentId = department != null ? department.getId() : null;
    }

    @Override
    public void setReportPeriods(List<ReportPeriod> reportPeriods) {
        periodPickerPopup.setPeriods(reportPeriods);
    }

    @Override
    public void setBookerReportTypes(Map<String, String> bookerReportTypes) {
        bookerReportType.clear();
        if (bookerReportTypes != null) {
            List<String> keyList = new ArrayList<String>(bookerReportTypes.keySet());
            Collections.sort(keyList);
            for (String key : keyList) {
                bookerReportType.addItem(bookerReportTypes.get(key), key);
            }
        }
    }

    @Override
    public Pair<Integer, String> getDepartment() {
        Pair<Integer, String> result = null;
        if (departmentPicker.getValue() != null && departmentPicker.getValue().size() == 1) {
            result = new Pair<Integer, String> (departmentPicker.getValue().get(0), departmentPicker.getText());
        }
        return result;
    }

    @Override
    public Pair<Integer, String> getReportPeriod() {
        Pair<Integer, String> result = null;
        if (periodPickerPopup.getValue() != null && periodPickerPopup.getValue().size() == 1) {
            result = new Pair<Integer, String> (periodPickerPopup.getValue().get(0), periodPickerPopup.getText());
        }
        return result;
    }

    @Override
    public Pair<Integer, String> getType() {
        Pair<Integer, String> result = null;
        if (bookerReportType.getSelectedIndex() != -1) {
            result = new Pair<Integer, String>(bookerReportType.getSelectedIndex(),
                    bookerReportType.getItemText(bookerReportType.getSelectedIndex()));
        }
        return result;
    }

    @Override
    public void addAccImportValueChangeHandler(ValueChangeHandler<String> valueChangeHandler) {
        fileUploader.addValueChangeHandler(valueChangeHandler);
    }


    @Override
    public void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows) {
        dataTable.setVisible(true);
        pager.setVisible(true);
        if (dataRows == null) {
            dataTable.setRowCount(0);
            dataTable.setRowData(new ArrayList<RefBookDataRow>());
        } else {
            if (totalCount == 0) {
                start = 0;
                pager.setPage(0);
            }
            dataTable.setRowCount(totalCount);
            dataTable.setRowData(start, dataRows);
        }
    }

    @Override
    public void updateTable() {
        Range range = new Range(pager.getPageStart(), pager.getPageSize());
        if(getUiHandlers().isSearchEnabled())
            dataTable.setVisibleRangeAndClearData(range, true);
        else{
            dataTable.setVisible(false);
            pager.setVisible(false);
        }

    }

    @Override
    public void setTableColumns(final List<RefBookColumn> columns) {
        while (dataTable.getColumnCount() > 0) {
            dataTable.removeColumn(0);
        }

        for (final RefBookColumn header : columns) {
            TextColumn<RefBookDataRow> column = new TextColumn<RefBookDataRow>() {
                @Override
                public String getValue(RefBookDataRow object) {
                    return object.getValues().get(header.getAlias());
                }
            };
            column.setHorizontalAlignment(convertAlignment(header.getAlignment()));
            dataTable.addResizableSortableColumn(column, header.getName());
            dataTable.setColumnWidth(column, header.getWidth(), Style.Unit.EM);
        }
    }

    @Override
    public int getPageSize() {
        return pager.getPageSize();
    }

    @Override
    public void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data) {
        dataTable.setPageSize(pageSize);
        data.addDataDisplay(dataTable);
    }

    @Override
    public Date getReportPeriodEndDate() {
        return periodPickerPopup.getPeriodDates(getReportPeriod().getFirst()).getSecond();
    }

    @UiHandler("searchButton")
    void onSearchClick(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onSearch();
        }
    }

    @UiHandler("deleteButton")
    void onDeleteClick(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onDelete();
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
