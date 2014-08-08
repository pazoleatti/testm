package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.sbrf.taxaccounting.model.HistoryBusinessSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AplanaUiHandlers;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.AsyncDataProviderWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditClientView extends ViewWithUiHandlers<AuditClientUIHandler>
        implements AuditClientPresenter.MyView {

    interface Binder extends UiBinder<Widget, AuditClientView> {
    }

    private static final DateTimeFormat FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");
    private static final String DATE_COLUMN_HEADER = "Дата-время";
    private static final String EVENT_COLUMN_HEADER = "Событие";
    private static final String NOTE_COLUMN_HEADER = "Текст события";
    private static final String REPORT_PERIOD_COLUMN_HEADER = "Период";
    private static final String DEPARTMENT_COLUMN_HEADER = "Подразделение";
    private static final String TYPE_COLUMN_HEADER = "Тип формы";
    private static final String FORM_DATA_KIND_COLUMN_HEADER = "Тип налоговой формы";
    private static final String FORM_TYPE_COLUMN_HEADER = "Вид налоговой формы/декларации";
    private static final String USER_LOGIN_COLUMN_HEADER = "Пользователь";
    private static final String USER_ROLES_COLUMN_HEADER = "Роль пользователя";
    private static final String USER_IP_COLUMN_HEADER = "IP пользователя";
    @UiField
    GenericDataGrid<LogSearchResultItem> table;
    @UiField
    Panel filterContentPanel;
    @UiField
    FlexiblePager pager;
    @UiField
    LinkAnchor printButton;
    @UiField
    LinkAnchor archive;
    @UiField
    Label archiveDateLbl;
    @UiField
    Label archiveLbl;
    private HistoryBusinessSearchOrdering sortByColumn;
    private AsyncDataProviderWithSortableTable dataProvider;

    @Inject
    @UiConstructor
    public AuditClientView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));

        setTableColumns();

        dataProvider = new AsyncDataProviderWithSortableTable(table, this) {
            @Override
            public AplanaUiHandlers getUiHandlersX() {
                return getUiHandlers();
            }
        };
        dataProvider.setAscSorting(false);
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == AuditClientPresenter.TYPE_AUDIT_FILTER_PRESENTER) {
            filterContentPanel.clear();
            if (content != null) {
                filterContentPanel.add(content);
            }
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public void setAuditTableData(int startIndex, long count, List<LogSearchResultItem> itemList) {
        table.setRowCount((int) count);
        table.setRowData(startIndex, itemList);
    }

    @Override
    public void getBlobFromServer(String uuid) {
        Window.open(GWT.getHostPageBaseURL() + "download/downloadBlobController/processLogDownload/" + uuid, "", "");
    }

    @Override
    public void updateData() {
        table.setVisibleRangeAndClearData(table.getVisibleRange(), true);
    }

    @Override
    public void updateData(int pageNumber) {
        if (pageNumber == 0) {
            table.getColumnSortList().clear();
        }
        if (pager.getPage() == pageNumber) {
            table.setVisibleRangeAndClearData(table.getVisibleRange(), true);
        } else {
            pager.setPage(pageNumber);
        }
    }

    @Override
    public void updateArchiveDateLbl(String archiveDate) {
        archiveDateLbl.setText(archiveDate);
    }

    @Override
    public HistoryBusinessSearchOrdering getSearchOrdering() {
        if (sortByColumn == null) {
            sortByColumn = HistoryBusinessSearchOrdering.DATE;
        }
        return sortByColumn;
    }

    @Override
    public void setVisibleArchiveButton(boolean isVisible) {
        archive.setVisible(isVisible);
        archiveLbl.setVisible(isVisible);
    }

    @Override
    public void setSortByColumn(String sortByColumn) {
        this.sortByColumn = HistoryBusinessSearchOrdering.valueOf(sortByColumn);
    }

    @Override
    public boolean isAscSorting() {
        return dataProvider.isAscSorting();
    }

    @UiHandler("printButton")
    void onPrintButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onPrintButtonClicked();
        }
    }

    @UiHandler("archive")
    void onArchive(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onArchiveButtonClicked();
        }
    }

    private void setTableColumns() {
        //Инициализация колонок
        TextColumn<LogSearchResultItem> dateColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                return FORMAT.format(object.getLogDate());
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
                return object.getReportPeriodName();
            }
        };

        TextColumn<LogSearchResultItem> departmentColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                return object.getDepartmentName();
            }
        };

        TextColumn<LogSearchResultItem> typeColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                if (object.getFormTypeName() != null) {
                    return "Налоговые формы";
                } else if (object.getDeclarationTypeName() != null) {
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
                if (object.getFormTypeName() != null)
                    return object.getFormTypeName();
                else
                    return object.getDeclarationTypeName() != null ? object.getDeclarationTypeName() : "";
            }
        };

        TextColumn<LogSearchResultItem> userLoginColumn = new TextColumn<LogSearchResultItem>() {
            @Override
            public String getValue(LogSearchResultItem object) {
                return object.getUser();
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

        table.addColumn(dateColumn, DATE_COLUMN_HEADER);
        table.setColumnWidth(dateColumn, 8.5, Style.Unit.EM);
        table.addColumn(eventColumn, EVENT_COLUMN_HEADER);
        table.addColumn(noteColumn, NOTE_COLUMN_HEADER);
        table.addColumn(reportPeriodColumn, REPORT_PERIOD_COLUMN_HEADER);
        table.setColumnWidth(reportPeriodColumn, 9, Style.Unit.EM);
        table.addColumn(departmentColumn, DEPARTMENT_COLUMN_HEADER);
        table.addColumn(typeColumn, TYPE_COLUMN_HEADER);
        table.setColumnWidth(typeColumn, 8, Style.Unit.EM);
        table.addColumn(formDataKindColumn, FORM_DATA_KIND_COLUMN_HEADER);
        table.setColumnWidth(formDataKindColumn, 9, Style.Unit.EM);
        table.addColumn(formDeclTypeColumn, FORM_TYPE_COLUMN_HEADER);
        table.addColumn(userLoginColumn, USER_LOGIN_COLUMN_HEADER);
        table.setColumnWidth(userLoginColumn, 9, Style.Unit.EM);
        table.addColumn(userRolesColumn, USER_ROLES_COLUMN_HEADER);
        table.setColumnWidth(userRolesColumn, 7, Style.Unit.EM);
        table.addColumn(userIpColumn, USER_IP_COLUMN_HEADER);
        table.setColumnWidth(userIpColumn, 8, Style.Unit.EM);
        table.addCellPreviewHandler(new CellPreviewEvent.Handler<LogSearchResultItem>() {
            @Override
            public void onCellPreview(CellPreviewEvent<LogSearchResultItem> event) {
                if ("mouseover".equals(event.getNativeEvent().getType())) {
                    long index = (event.getIndex() - (pager.getPage() * table.getPageSize()));
                    TableCellElement cellElement = table.getRowElement((int) index).getCells().getItem(event.getColumn());
                    if (cellElement.getInnerText().replace("\u00A0", "").trim().isEmpty()) {
                        cellElement.removeAttribute("title");
                    } else {
                        cellElement.setTitle(cellElement.getInnerText());
                    }
                }
            }
        });

        table.setPageSize(pager.getPageSize());
        pager.setDisplay(table);

        dateColumn.setDataStoreName(HistoryBusinessSearchOrdering.DATE.name());
        eventColumn.setDataStoreName(HistoryBusinessSearchOrdering.EVENT.name());
        noteColumn.setDataStoreName(HistoryBusinessSearchOrdering.NOTE.name());
        reportPeriodColumn.setDataStoreName(HistoryBusinessSearchOrdering.REPORT_PERIOD.name());
        departmentColumn.setDataStoreName(HistoryBusinessSearchOrdering.DEPARTMENT.name());
        typeColumn.setDataStoreName(HistoryBusinessSearchOrdering.TYPE.name());
        formDataKindColumn.setDataStoreName(HistoryBusinessSearchOrdering.FORM_DATA_KIND.name());
        formDeclTypeColumn.setDataStoreName(HistoryBusinessSearchOrdering.FORM_TYPE.name());
        userLoginColumn.setDataStoreName(HistoryBusinessSearchOrdering.USER.name());
        userRolesColumn.setDataStoreName(HistoryBusinessSearchOrdering.USER_ROLE.name());
        userIpColumn.setDataStoreName(HistoryBusinessSearchOrdering.IP_ADDRESS.name());
    }
}
