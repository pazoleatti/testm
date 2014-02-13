package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client;

import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.CellPreviewEvent;
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
    GenericDataGrid<LogSearchResultItem> logBusinessTable;

    @UiField
    FlexiblePager pager;

    final AsyncDataProvider<LogSearchResultItem> dataProvider = new AsyncDataProvider<LogSearchResultItem>() {
        @Override
        protected void onRangeChanged(HasData<LogSearchResultItem> display) {
            if (getUiHandlers() != null){
                final Range range = display.getVisibleRange();
                getUiHandlers().onRangeChange(range.getStart(), range.getLength());
            }
        }
    };


    private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");

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
        TextColumn<LogSearchResultItem> dateColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                return format.format(object.getLogDate());
            }
        };

        TextColumn<LogSearchResultItem> eventColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                return object.getEvent().getTitle();
            }
        };

        TextColumn<LogSearchResultItem> noteColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                return object.getNote();
            }
        };

        TextColumn<LogSearchResultItem> reportPeriodColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                return object.getReportPeriod()!=null?object.getReportPeriod().getName() + " " + object.getReportPeriod().getTaxPeriod().getYear()
                        : "";
            }
        };

        TextColumn<LogSearchResultItem> departmentColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                return object.getDepartment().getName();
            }
        };

        TextColumn<LogSearchResultItem> typeColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                if (object.getFormType() != null) {
                    return "Налоговые формы";
                }
                else if (object.getDeclarationType() != null) {
                    return "Декларации";
                }
                return null;
            }
        };

        TextColumn<LogSearchResultItem> formDataKindColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                if (object.getFormKind() != null) {
                    return object.getFormKind().getName();
                } else {
                    return "";
                }
            }
        };

        TextColumn<LogSearchResultItem> formDeclTypeColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                if(object.getFormType() != null)
                    return object.getFormType().getName();
                else
                    return object.getDeclarationType()!= null?object.getDeclarationType().getName():"";
            }
        };

        TextColumn<LogSearchResultItem> userLoginColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                return object.getUser().getLogin();
            }
        };

        TextColumn<LogSearchResultItem> userRolesColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                return object.getRoles();
            }
        };

        TextColumn<LogSearchResultItem> userIpColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                return object.getIp();
            }
        };

        logBusinessTable.setPageSize(pager.getPageSize());
        logBusinessTable.addColumn(dateColumn, dateColumnHeader);
        logBusinessTable.addColumn(eventColumn, eventColumnHeader);
        logBusinessTable.addColumn(noteColumn, noteColumnHeader);
        logBusinessTable.addColumn(reportPeriodColumn, reportPeriodColumnHeader);
        logBusinessTable.addColumn(departmentColumn, departmentColumnHeader);
        logBusinessTable.addColumn(typeColumn, typeColumnHeader);
        logBusinessTable.addColumn(formDataKindColumn, formDataKindColumnHeader);
        logBusinessTable.addColumn(formDeclTypeColumn, formTypeColumnHeader);
        logBusinessTable.addColumn(userLoginColumn, userLoginColumnHeader);
        logBusinessTable.addColumn(new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < object.getUser().getRoles().size(); i++) {
                    stringBuilder.append(object.getUser().getRoles().get(i).getName());
                    if (i != object.getUser().getRoles().size() - 1) {
                        stringBuilder.append(", ");
                    }
                }
                return stringBuilder.toString();
            }
        }, userRolesColumnHeader);
        logBusinessTable.addColumn(userIpColumn, userIpColumnHeader);
        logBusinessTable.addCellPreviewHandler(new CellPreviewEvent.Handler<LogSearchResultItem>() {
            @Override
            public void onCellPreview(CellPreviewEvent<LogSearchResultItem> event) {
                if ("mouseover".equals(event.getNativeEvent().getType())) {
                    long index = (event.getIndex() - (pager.getPage() * logBusinessTable.getPageSize()));
                    TableCellElement cellElement = logBusinessTable.getRowElement((int) index).getCells().getItem(event.getColumn());
                    if (cellElement.getInnerText().replace("\u00A0", "").trim().isEmpty()) {
                        cellElement.removeAttribute("title");
                    } else {
                        cellElement.setTitle(cellElement.getInnerText());
                    }
                }
            }
        });

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
    public void setAuditTableData(int startIndex, long count, List<LogSearchResultItem> itemList) {
        logBusinessTable.setRowCount((int) count);
        logBusinessTable.setRowData(startIndex, itemList);
    }

    @Override
    public void updateData(int pageNumber) {
        if (pager.getPage() == pageNumber){
            logBusinessTable.setVisibleRangeAndClearData(logBusinessTable.getVisibleRange(), true);
        } else {
            pager.setPage(pageNumber);
        }
    }

    @Override
    public void getBlobFromServer(String uuid) {
        Window.open(GWT.getHostPageBaseURL() + "download/downloadBlobController/processLogDownload/" + uuid, "", "");
    }

    @UiHandler("printButton")
    void onPrintClicked(ClickEvent event){
        if (getUiHandlers() != null)
            getUiHandlers().onPrintButtonClicked();
    }
}
