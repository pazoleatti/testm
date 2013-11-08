package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client;

import com.aplana.sbrf.taxaccounting.model.LogBusinessSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

/**
 * User: avanteev
 */
public class HistoryBusinessView extends ViewWithUiHandlers<HistoryBusinessUIHandler> implements HistoryBusinessPresenter.MyView {

    interface Binder extends UiBinder<Widget, HistoryBusinessView> {}

    @UiField
    Panel filterContentPanel;

    @UiField
    GenericDataGrid<LogBusinessSearchResultItem> logBusinessTable;

    @UiField
    FlexiblePager pager;

    final AsyncDataProvider<LogBusinessSearchResultItem> dataProvider = new AsyncDataProvider<LogBusinessSearchResultItem>() {
        @Override
        protected void onRangeChanged(HasData<LogBusinessSearchResultItem> display) {
            System.out.println("!!!");
            if (getUiHandlers() != null){
                System.out.println("!!!!");
                final Range range = display.getVisibleRange();
                getUiHandlers().onRangeChange(range.getStart(), range.getLength());
            }
        }
    };


    private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");

    private static final int PAGE_SIZE = 20;

    private static final String dateColumnHeader = "Дата-время";
    private static final String eventColumnHeader = "Событие";
    private static final String noteColumnHeader = "Текст события";
    private static final String reportPeriodColumnHeader = "Период";
    private static final String departmentColumnHeader = "Подразделение";
    private static final String typeColumnHeader = "Тип формы";
    private static final String formDataKindColumnHeader = "Тип налоговой формы";
    private static final String formTypeColumnHeader = "Вид налоговой формы/декларации";
    private static final String userLoginColumnHeader = "Пользователь";
    private static final String userRolesColumnHeader = "Роль пользователя";
    private static final String userIpColumnHeader = "IP пользователя";

    @Inject
    public HistoryBusinessView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        //Инициализация колонок
        TextColumn<LogBusinessSearchResultItem> dateColumn = new TextColumn<LogBusinessSearchResultItem>() {
            @Override
            public String getValue(LogBusinessSearchResultItem object) {
                return format.format(object.getLogDate());
            }
        };

        TextColumn<LogBusinessSearchResultItem> eventColumn = new TextColumn<LogBusinessSearchResultItem>() {
            @Override
            public String getValue(LogBusinessSearchResultItem object) {
                return object.getEvent().getTitle();
            }
        };

        TextColumn<LogBusinessSearchResultItem> noteColumn = new TextColumn<LogBusinessSearchResultItem>() {
            @Override
            public String getValue(LogBusinessSearchResultItem object) {
                return object.getNote();
            }
        };

        TextColumn<LogBusinessSearchResultItem> reportPeriodColumn = new TextColumn<LogBusinessSearchResultItem>() {
            @Override
            public String getValue(LogBusinessSearchResultItem object) {
                return object.getReportPeriod()!=null?object.getReportPeriod().getName() + " " + object.getReportPeriod().getYear()
                        : "";
            }
        };

        TextColumn<LogBusinessSearchResultItem> departmentColumn = new TextColumn<LogBusinessSearchResultItem>() {
            @Override
            public String getValue(LogBusinessSearchResultItem object) {
                return object.getDepartment().getName();
            }
        };

        TextColumn<LogBusinessSearchResultItem> typeColumn = new TextColumn<LogBusinessSearchResultItem>() {
            @Override
            public String getValue(LogBusinessSearchResultItem object) {
                if (object.getFormType() != null) {
                    return "Налоговые формы";
                }
                else if (object.getDeclarationType() != null) {
                    return "Декларации";
                }
                return null;
            }
        };

        TextColumn<LogBusinessSearchResultItem> formDataKindColumn = new TextColumn<LogBusinessSearchResultItem>() {
            @Override
            public String getValue(LogBusinessSearchResultItem object) {
                if (object.getFormKind() != null) {
                    return object.getFormKind().getName();
                } else {
                    return "";
                }
            }
        };

        TextColumn<LogBusinessSearchResultItem> formDeclTypeColumn = new TextColumn<LogBusinessSearchResultItem>() {
            @Override
            public String getValue(LogBusinessSearchResultItem object) {
                if(object.getFormType() != null)
                    return object.getFormType().getName();
                else
                    return object.getDeclarationType()!= null?object.getDeclarationType().getName():"";
            }
        };

        TextColumn<LogBusinessSearchResultItem> userLoginColumn = new TextColumn<LogBusinessSearchResultItem>() {
            @Override
            public String getValue(LogBusinessSearchResultItem object) {
                return object.getUser().getLogin();
            }
        };

        TextColumn<LogBusinessSearchResultItem> userRolesColumn = new TextColumn<LogBusinessSearchResultItem>() {
            @Override
            public String getValue(LogBusinessSearchResultItem object) {
                return object.getRoles();
            }
        };

        TextColumn<LogBusinessSearchResultItem> userIpColumn = new TextColumn<LogBusinessSearchResultItem>() {
            @Override
            public String getValue(LogBusinessSearchResultItem object) {
                return object.getIp();
            }
        };

        logBusinessTable.setPageSize(PAGE_SIZE);
        logBusinessTable.addColumn(dateColumn, dateColumnHeader);
        logBusinessTable.addColumn(eventColumn, eventColumnHeader);
        logBusinessTable.addColumn(noteColumn, noteColumnHeader);
        logBusinessTable.addColumn(reportPeriodColumn, reportPeriodColumnHeader);
        logBusinessTable.addColumn(departmentColumn, departmentColumnHeader);
        logBusinessTable.addColumn(typeColumn, typeColumnHeader);
        logBusinessTable.addColumn(formDataKindColumn, formDataKindColumnHeader);
        logBusinessTable.addColumn(formDeclTypeColumn, formTypeColumnHeader);
        logBusinessTable.addColumn(userLoginColumn, userLoginColumnHeader);
        logBusinessTable.addColumn(userRolesColumn, userRolesColumnHeader);
        logBusinessTable.addColumn(userIpColumn, userIpColumnHeader);

        dataProvider.addDataDisplay(logBusinessTable);
        pager.setDisplay(logBusinessTable);
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == HistoryBusinessPresenter.TYPE_historyBusinessPresenter){
            filterContentPanel.clear();
            if (content != null)
                filterContentPanel.add(content);
        }else
            super.setInSlot(slot, content);
    }

    @Override
    public void setAuditTableData(int startIndex, long count, List<LogBusinessSearchResultItem> itemList) {
        logBusinessTable.setRowCount((int) count);
        logBusinessTable.setRowData(itemList);
    }

    @Override
    public void updateData(int pageNumber) {
        if (pager.getPage() == pageNumber){
            logBusinessTable.setVisibleRangeAndClearData(logBusinessTable.getVisibleRange(), true);
        } else {
            pager.setPage(pageNumber);
        }
    }

}
