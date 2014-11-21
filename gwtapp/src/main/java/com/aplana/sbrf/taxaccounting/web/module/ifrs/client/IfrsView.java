package com.aplana.sbrf.taxaccounting.web.module.ifrs.client;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.model.IfrsRow;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Created by lhaziev on 22.10.2014.
 */
public class IfrsView extends ViewWithUiHandlers<IfrsUiHandlers> implements IfrsPresenter.MyView {

    private static final String TABLE_ROW_ID_PREFIX = "tableRP";

    private SingleSelectionModel<IfrsRow> selectionModel;

    interface Binder extends UiBinder<Widget, IfrsView> {
    }

    @UiField
    PeriodPickerPopupWidget periodPickerPopup;

    @UiField
    Button search;

    @UiField
    LinkButton create;

    @UiField
    FlexiblePager pager;

    @UiField
    GenericDataGrid<IfrsRow> table;

    @UiField
    HTMLPanel htmlPanel;

    private Map<Integer, LinkButton> mapLinkButton;

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
        setupTables();
    }

    private void setupTables() {
        mapLinkButton =  new HashMap<Integer, LinkButton>();
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
        Column<IfrsRow, IfrsRow> statusColumn = new Column<IfrsRow, IfrsRow>(new AbstractCell<IfrsRow>() {
            @Override
            public void render(Context context, IfrsRow value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant(mapLinkButton.get(value.getReportPeriodId()).getElement().toString());
            }
        }) {
            @Override
            public IfrsRow getValue(IfrsRow object) {
                return object;
            }
        };

        table.addResizableColumn(yearColumn, "Год");
        table.setColumnWidth(yearColumn, 3.5, Style.Unit.EM);

        table.addResizableColumn(periodNameColumn, "Период");
        table.setColumnWidth(periodNameColumn, 5.5, Style.Unit.EM);

        table.addResizableColumn(statusColumn, "Архив");
        table.setColumnWidth(statusColumn, 40, Style.Unit.EM);

        table.redrawHeaders();

        table.setPageSize(pager.getPageSize());
        pager.setDisplay(table);

        //добавляем ссылки, нажатие на которые можно отловить
        table.addHandler(new ValueChangeHandler<IfrsRow>() {
            @Override
            public void onValueChange(ValueChangeEvent<IfrsRow> event) {
                addAnchor(((List<IfrsRow>) event.getValue()));
            }
        }, ValueChangeEvent.getType());
    }

    private void addAnchor(List<IfrsRow> ifrsRows) {
        for(IfrsRow ifrsRow: ifrsRows) {
            LinkButton linkButton = mapLinkButton.get(ifrsRow.getReportPeriodId());
            htmlPanel.addAndReplaceElement(linkButton, TABLE_ROW_ID_PREFIX + ifrsRow.getReportPeriodId());
        }
    }

    @Override
    public void setIfrsTableData(int start, int totalCount, List<IfrsRow> records) {
        this.records = records;
        selectionModel.clear();
        mapLinkButton.clear();
        for(IfrsRow ifrsRow: records) {
            LinkButton linkButton = new LinkButton();
            linkButton.getElement().setId(TABLE_ROW_ID_PREFIX + ifrsRow.getReportPeriodId());
            linkButton.setDisableImage(true);
            linkButton.setText(ifrsRow.getStatus().getName());
            linkButton.getElement().getStyle().setDisplay(Style.Display.INLINE);
            linkButton.getElement().getFirstChildElement().getStyle().setDisplay(Style.Display.INLINE);
            linkButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Element element = event.getRelativeElement();
                    Integer id = new Integer(element.getId().replaceFirst(TABLE_ROW_ID_PREFIX, ""));
                    getUiHandlers().onClickCalc(id);
                }
            });
            mapLinkButton.put(ifrsRow.getReportPeriodId(), linkButton);
        }
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
        timer.scheduleRepeating(3000);
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
                LinkButton linkButton = mapLinkButton.get(record.getReportPeriodId());
                linkButton.setText(status.getName());
                linkButton.getElement().getFirstChildElement().getStyle().setDisplay(Style.Display.INLINE);
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
}
