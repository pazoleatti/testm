package com.aplana.sbrf.taxaccounting.web.module.ifrs.client;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.model.IfrsRow;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;
import java.util.Map;

/**
 * Created by lhaziev on 22.10.2014.
 */
public class IfrsView extends ViewWithUiHandlers<IfrsUiHandlers> implements IfrsPresenter.MyView {

    private SingleSelectionModel<IfrsRow> selectionModel;

    interface Binder extends UiBinder<Widget, IfrsView> {
    }

    @UiField
    PeriodPickerPopupWidget periodPickerPopup;

    @UiField
    Button search;

    @UiField
    LinkButton create, calculate;

    @UiField
    FlexiblePager pager;

    @UiField
    GenericDataGrid<IfrsRow> table;

    private Timer timer;
    private List<IfrsRow> records;

    @Inject
    @UiConstructor
    public IfrsView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        init();
    }

    private void init() {
        timer = new Timer() {
            @Override
            public void run() {
                try {
                    getUiHandlers().updateStatus(records);
                } catch (Exception e) {
                }
            }
        };
        timer.scheduleRepeating(3000);
        setupTables();
    }

    private void setupTables() {
        selectionModel = new SingleSelectionModel<IfrsRow>();
        table.setSelectionModel(selectionModel);

        TextColumn<IfrsRow> yearColumn = new TextColumn<IfrsRow>() {
            @Override
            public String getValue(IfrsRow object) {
                return String.valueOf(object.getYear());
            }
        };
        TextColumn<IfrsRow> periodNameColumn = new TextColumn<IfrsRow>() {
            @Override
            public String getValue(IfrsRow object) {
                return object.getPeriodName();
            }
        };
        TextColumn<IfrsRow> statusColumn = new TextColumn<IfrsRow>() {
            @Override
            public String getValue(IfrsRow object) {
                return object.getStatus().getName();
            }
        };

        table.addResizableColumn(yearColumn, "Год");
        table.setColumnWidth(yearColumn, 20, Style.Unit.PX);

        table.addResizableColumn(periodNameColumn, "Период");
        table.setColumnWidth(periodNameColumn, 50, Style.Unit.PX);

        table.addResizableColumn(statusColumn, "Состояние архива");
        table.setColumnWidth(statusColumn, 50, Style.Unit.PX);

        table.redrawHeaders();

        table.setPageSize(pager.getPageSize());
        pager.setDisplay(table);
    }

    @Override
    public void setIfrsTableData(int start, int totalCount, List<IfrsRow> records) {
        this.records = records;
        selectionModel.clear();
        table.setRowCount(totalCount);
        table.setRowData(start, records);
    }

    @Override
    public void assignDataProvider(int pageSize, AbstractDataProvider<IfrsRow> data) {
        table.setPageSize(pageSize);
        data.addDataDisplay(table);
    }

    @Override
    public int getPageSize() {
        return pager.getPageSize();
    }

    @Override
    public void updateTable() {
        Range range = new Range(pager.getPageStart(), pager.getPageSize());
        table.setVisibleRangeAndClearData(range, true);
    }

    @Override
    public void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods) {
        periodPickerPopup.setPeriods(reportPeriods);
    }

    @Override
    public List<Integer> getReportPeriodIds() {
        return periodPickerPopup.getValue();
    }

    @Override
    public Integer getReportPeriodId() {
        IfrsRow selectedObject = selectionModel.getSelectedObject();
        if (selectedObject != null)
            return selectedObject.getReportPeriodId();
        return null;
    }

    @Override
    public void startTimer() {
        timer.run();
    }

    @Override
    public void stopTimer() {
        timer.cancel();
    }

    @Override
    public void updateStatus(Map<Integer, IfrsRow.StatusIfrs> statusMap) {
        for(IfrsRow record: records) {
            IfrsRow.StatusIfrs status = statusMap.get(record.getReportPeriodId());
            if (status != null) {
                record.setStatus(status);
            }
        }
        table.redraw();
    }

    @UiHandler("search")
    public void onSearchClick(ClickEvent event) {
        getUiHandlers().reloadTable();
    }

    @UiHandler("create")
    public void onCreateClick(ClickEvent event) {
        getUiHandlers().onClickCreate();
    }


    @UiHandler("calculate")
    public void onCalcClick(ClickEvent event) {
        getUiHandlers().onCalc();
    }

}
