package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodSelectHandler;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

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
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    ListBox bookerReportType;

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
        departmentPicker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<List<Integer>> event) {

                if (event == null || event.getValue().isEmpty()) {
                    return;
                }

                Integer selDepartmentId = event.getValue().iterator().next();
                //String selDepartmentName = event.ge.keySet().iterator().next();

                // Проверка совпадения выбранного подразделения с текущим
                if (BookerStatementsView.this.currentDepartmentId != null
                        && BookerStatementsView.this.currentDepartmentId.equals(selDepartmentId)) {
                    return;
                }

                BookerStatementsView.this.currentDepartmentId = selDepartmentId;

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
        departmentPicker.setAvalibleValues(departments, availableDepartments);
    }

    @Override
    public void setDepartment(final Department department) {
        if (department != null) {
            departmentPicker.setValue(new ArrayList<Integer>() {{
                add(department.getId());
                reloadTaxPeriods();
            }});
        }
        this.currentDepartmentId = department != null ? department.getId() : null;
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
        bookerReportType.setSelectedIndex(bookerReportTypes.size()-1);
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