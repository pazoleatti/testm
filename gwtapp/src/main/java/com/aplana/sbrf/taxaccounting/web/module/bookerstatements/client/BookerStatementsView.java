package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.SelectDepartmentsEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.popup.SelectDepartmentsEvent;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodSelectHandler;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * View для формы "Загрузка бухгалтерской отчётности"
 *
 * @author Dmitriy Levykin
 */
public class BookerStatementsView extends ViewWithUiHandlers<BookerStatementsUiHandlers>
        implements BookerStatementsPresenter.MyView, ReportPeriodSelectHandler {

    interface Binder extends UiBinder<Widget, BookerStatementsView> {
    }

    // Признак УНП
    private boolean isUnp = false;

    // Выбранное подразделение
    private Integer currentDepartmentId;
    private String currentDepartmentName;

    // Выбранный период
    private ReportPeriod currentReportPeriod;

    // Контейнер для справочника периодов
    @UiField
    @Editor.Ignore
    SimplePanel reportPeriodPanel;
    ReportPeriodPicker period = new ReportPeriodPicker(this, false);

    @UiField
    DataGrid<DataRow<Cell>> formDataTable;

    @UiField
    FlexiblePager pager;

    @UiField
    DepartmentPicker departmentPicker;

    @UiField
    ListBox bookerReportType;

    private DataRowColumnFactory factory = new DataRowColumnFactory();

    @Inject
    @UiConstructor
    public BookerStatementsView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        reportPeriodPanel.add(period);
        pager.setDisplay(formDataTable);
        initListeners();
    }

    private void initListeners() {
        // Подразделение
        departmentPicker.addDepartmentsReceivedEventHandler(new SelectDepartmentsEventHandler() {
            @Override
            public void onDepartmentsReceived(SelectDepartmentsEvent event) {

                if (event == null || event.getItems().isEmpty()) {
                    return;
                }

                Integer selDepartmentId = event.getItems().values().iterator().next();
                String selDepartmentName = event.getItems().keySet().iterator().next();

                // Проверка совпадения выбранного подразделения с текущим
                if (BookerStatementsView.this.currentDepartmentId != null
                        && BookerStatementsView.this.currentDepartmentId.equals(selDepartmentId)) {
                    return;
                }

                BookerStatementsView.this.currentDepartmentId = selDepartmentId;
                BookerStatementsView.this.currentDepartmentName = selDepartmentName;

                currentReportPeriod = null;

                // Обновление налоговых периодов
                reloadTaxPeriods();
            }
        });
    }

    @Override
    public void setUnpFlag(boolean isUnp) {
        this.isUnp = isUnp;
    }

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartments) {
        departmentPicker.setTreeValues(departments, availableDepartments);
    }

    @Override
    public void setDepartment(final Department department) {
        if (department != null) {
            departmentPicker.setSelectedItems(new HashMap<String, Integer>() {{
                put(department.getName(), department.getId());
                reloadTaxPeriods();
            }});
        }
        this.currentDepartmentId = department != null ? department.getId() : null;
        this.currentDepartmentName = department != null ? department.getName() : null;
    }

    @Override
    public void setTaxPeriods(List<TaxPeriod> taxPeriods) {
        reportPeriodPanel.clear();
        period = new ReportPeriodPicker(this, false);
        period.setTaxPeriods(taxPeriods);
        reportPeriodPanel.add(period);
    }

    @Override
    public void setReportPeriods(List<ReportPeriod> reportPeriods) {
        period.setReportPeriods(reportPeriods);
    }

    @Override
    public void reloadTaxPeriods() {
        getUiHandlers().reloadTaxPeriods(currentDepartmentId);
    }

    @Override
    public void setReportPeriod(ReportPeriod reportPeriod) {
        currentReportPeriod = reportPeriod;
        period.setSelectedReportPeriods(Arrays.asList(currentReportPeriod));
    }

    @Override
    public void setBookerReportTypes(Map<String, String> bookerReportTypes) {
        bookerReportType.clear();
        if (bookerReportTypes != null) {
            for (Map.Entry<String, String> e : bookerReportTypes.entrySet()) {
                bookerReportType.addItem(e.getValue(), e.getKey());
            }
        }
        reloadTaxPeriods();
    }

    @Override
    public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
        getUiHandlers().onTaxPeriodSelected(taxPeriod, currentDepartmentId);
    }

    @Override
    public void onReportPeriodsSelected(Map<Integer, ReportPeriod> selectedReportPeriods) {
    }
}