package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.LogEntryMessageCell;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodSelectHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;

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

    List<LogEntry> logs = new ArrayList<LogEntry>();

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
    @UiField
    FormPanel uploadFormPanel;
    @UiField
    FileUpload uploader;
    @UiField
    SubmitButton uploadButton;
    @UiField
    Widget logPanel;
    @UiField(provided = true)
    CellList<LogEntry> loggerList = new CellList<LogEntry>(new LogEntryMessageCell());
    @UiField
    DockLayoutPanel dockPanel;

    @Inject
    @UiConstructor
    public BookerStatementsView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        reportPeriodPanel.add(period);
        pager.setDisplay(formDataTable);
        initListeners();
        setAction();
        dockPanel.setWidgetHidden(logPanel, (logs == null || logs.isEmpty()));
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

                currentReportPeriod = null;

                setAction();

                // Обновление налоговых периодов
                reloadTaxPeriods();
            }
        });

        ChangeHandler actionHandler = new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                setAction();
            }
        };
        bookerReportType.addChangeHandler(actionHandler);
        uploader.addChangeHandler(actionHandler);

        uploadFormPanel.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            @Override
            public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
                String error = event.getResults();
                if (!error.toLowerCase().contains("error")) {
                    logs.add(new LogEntry(LogLevel.INFO, "Файл успешно загружен"));
                } else {
                    int index = error.indexOf("ServiceException: ");
                    if (index != -1) {
                        error = error.substring(index + 18);
                        error = error.substring(0, error.indexOf("\n"));
                    }
                    logs.add(new LogEntry(LogLevel.ERROR, error));
                }
                setLogMessages(logs);
                LockInteractionEvent.fire((BookerStatementsPresenter) getUiHandlers(), false);
            }
        });

        uploadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                LockInteractionEvent.fire((BookerStatementsPresenter) getUiHandlers(), true);
            }
        });
    }

    private void setAction() {
        boolean isReady = currentReportPeriod != null && bookerReportType.getSelectedIndex() != -1 && uploader.getFilename() != null
                && !uploader.getFilename().isEmpty() && currentDepartmentId != null;
        uploadButton.setEnabled(isReady);
        if (isReady) {
            uploadFormPanel.setAction(GWT.getHostPageBaseURL() + "upload/bookerstatements/" + currentReportPeriod.getId()
                    + "/" + bookerReportType.getSelectedIndex());
        }
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
        bookerReportType.setSelectedIndex(bookerReportTypes.size() - 1);
        reloadTaxPeriods();
    }

    @Override
    public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
        getUiHandlers().onTaxPeriodSelected(taxPeriod, currentDepartmentId);
    }

    @Override
    public void onReportPeriodsSelected(Map<Integer, ReportPeriod> selectedReportPeriods) {
        currentReportPeriod = null;
        if (!selectedReportPeriods.isEmpty()) {
            currentReportPeriod = selectedReportPeriods.values().iterator().next();
        }
        setAction();
    }

    private void setLogMessages(List<LogEntry> entries) {
        dockPanel.setWidgetHidden(logPanel, (entries == null || entries.isEmpty()));
        if (entries != null && !entries.isEmpty()) {
            logPanel.setVisible(true);
            loggerList.setVisible(true);
            loggerList.setRowData(entries);
        } else {
            loggerList.setRowCount(0);
            loggerList.setRowData(new ArrayList<LogEntry>(0));
        }
        loggerList.redraw();
    }
}