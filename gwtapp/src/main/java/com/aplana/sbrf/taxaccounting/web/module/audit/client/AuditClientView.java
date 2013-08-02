package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.sbrf.taxaccounting.model.LogSystemSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.AbstractPager;
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
 * Date: 2013
 */
public class AuditClientView extends ViewWithUiHandlers<AuditClientUIHandler> implements AuditClientPresenter.MyView {

    interface Binder extends UiBinder<Widget, AuditClientView>{}

    @UiField
    Panel filterContentPanel;

    @UiField
    GenericDataGrid<LogSystemSearchResultItem> auditTable;

    @UiField
    AbstractPager pager;

    private MyDataProvider dataProvider = new MyDataProvider();

    private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm");

    private static final String dateColumnHeader = "Дата-время";
    private static final String eventColumnHeader = "Событие";
    private static final String noteColumnHeader = "Текст события";
    private static final String reportPeriodColumnHeader = "Период";
    private static final String departmentColumnHeader = "Подразделение";
    private static final String formDataKindColumnHeader = "Тип налоговой формы";
    private static final String formTypeColumnHeader = "Вид налоговой формы/декларации";
    private static final String userLoginColumnHeader = "Пользователь";
    private static final String userRolesColumnHeader = "Роль пользователя";
    private static final String userIpColumnHeader = "IP пользователя";


    @Inject
    @UiConstructor
    public AuditClientView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        //Инициализация колонок
        TextColumn<LogSystemSearchResultItem> dateColumn = new TextColumn<LogSystemSearchResultItem>() {
            @Override
            public String getValue(LogSystemSearchResultItem object) {
                return format.format(object.getLogDate());
            }
        };

        TextColumn<LogSystemSearchResultItem> eventColumn = new TextColumn<LogSystemSearchResultItem>() {
            @Override
            public String getValue(LogSystemSearchResultItem object) {
                return object.getEvent().getTitle();
            }
        };

        TextColumn<LogSystemSearchResultItem> noteColumn = new TextColumn<LogSystemSearchResultItem>() {
            @Override
            public String getValue(LogSystemSearchResultItem object) {
                return object.getNote();
            }
        };

        TextColumn<LogSystemSearchResultItem> reportPeriodColumn = new TextColumn<LogSystemSearchResultItem>() {
            @Override
            public String getValue(LogSystemSearchResultItem object) {
                return object.getReportPeriod()!=null?object.getReportPeriod().getName() : "";
            }
        };

        TextColumn<LogSystemSearchResultItem> departmentColumn = new TextColumn<LogSystemSearchResultItem>() {
            @Override
            public String getValue(LogSystemSearchResultItem object) {
                return object.getDepartment().getName();
            }
        };

        TextColumn<LogSystemSearchResultItem> formDataKindColumn = new TextColumn<LogSystemSearchResultItem>() {
            @Override
            public String getValue(LogSystemSearchResultItem object) {
				if (object.getFormKind() != null) {
					return object.getFormKind().getName();
				} else {
					return "";
				}
            }
        };

        TextColumn<LogSystemSearchResultItem> formDeclTypeColumn = new TextColumn<LogSystemSearchResultItem>() {
            @Override
            public String getValue(LogSystemSearchResultItem object) {
                if(object.getFormType() != null)
                    return object.getFormType().getName();
                else
                    return object.getDeclarationType()!= null?object.getDeclarationType().getName():"";
            }
        };

        TextColumn<LogSystemSearchResultItem> userLoginColumn = new TextColumn<LogSystemSearchResultItem>() {
            @Override
            public String getValue(LogSystemSearchResultItem object) {
                return object.getUser().getLogin();
            }
        };

        TextColumn<LogSystemSearchResultItem> userRolesColumn = new TextColumn<LogSystemSearchResultItem>() {
            @Override
            public String getValue(LogSystemSearchResultItem object) {
                return object.getRoles();
            }
        };

        TextColumn<LogSystemSearchResultItem> userIpColumn = new TextColumn<LogSystemSearchResultItem>() {
            @Override
            public String getValue(LogSystemSearchResultItem object) {
                return object.getIp();
            }
        };

        auditTable.addColumn(dateColumn, dateColumnHeader);
        auditTable.addColumn(eventColumn, eventColumnHeader);
        auditTable.addColumn(noteColumn, noteColumnHeader);
        auditTable.addColumn(reportPeriodColumn, reportPeriodColumnHeader);
        auditTable.addColumn(departmentColumn, departmentColumnHeader);
        auditTable.addColumn(formDataKindColumn, formDataKindColumnHeader);
        auditTable.addColumn(formDeclTypeColumn, formTypeColumnHeader);
        auditTable.addColumn(userLoginColumn, userLoginColumnHeader);
        auditTable.addColumn(userRolesColumn, userRolesColumnHeader);
        auditTable.addColumn(userIpColumn, userIpColumnHeader);

        pager.setDisplay(auditTable);

    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == AuditClientPresenter.TYPE_auditFilterPresenter) {
            filterContentPanel.clear();
            if (content!=null){
                filterContentPanel.add(content);
            }
        }
        else {
            super.setInSlot(slot, content);
        }

    }

    @Override
    public void setAuditTableData(int startIndex, long count, List<LogSystemSearchResultItem> itemList) {
        auditTable.setRowCount((int)count);
        auditTable.setRowData(startIndex, itemList);
    }

    @Override
    public void assignDataProvider(int pageSize) {
        auditTable.setPageSize(pageSize);
        dataProvider.addDataDisplay(auditTable);
    }

    @Override
    public void updateDataTable() {
        dataProvider.update();
    }

    private class MyDataProvider extends AsyncDataProvider<LogSystemSearchResultItem> {

        public void update() {
            for (HasData<LogSystemSearchResultItem> display: getDataDisplays()) {
                onRangeChanged(display);
            }
        }

        @Override
        protected void onRangeChanged(HasData<LogSystemSearchResultItem> display) {
            final Range range = display.getVisibleRange();
            getUiHandlers().onRangeChange(range.getStart(), range.getLength());
        }
    }

}
