package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.sbrf.taxaccounting.model.HistoryBusinessSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.widget.cell.SortingHeaderCell;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
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
 * Date: 2013
 */
public class AuditClientView extends ViewWithUiHandlers<AuditClientUIHandler> implements AuditClientPresenter.MyView {

    interface Binder extends UiBinder<Widget, AuditClientView>{}

    private HistoryBusinessSearchOrdering sortByColumn;
    private boolean isAscSorting;

    @UiField
    Panel filterContentPanel;

    @UiField
    GenericDataGrid<LogSearchResultItem> auditTable;

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

    private final AsyncDataProvider<LogSearchResultItem> dataProvider = new AsyncDataProvider<LogSearchResultItem>() {
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
    @UiConstructor
    public AuditClientView(final Binder uiBinder) {
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

        dataProvider.addDataDisplay(auditTable);

        auditTable.addColumn(dateColumn, getHeader(dateColumnHeader, dateColumn));
        auditTable.addColumn(eventColumn, getHeader(eventColumnHeader, eventColumn));
        auditTable.addColumn(noteColumn, getHeader(noteColumnHeader, noteColumn));
        auditTable.addColumn(reportPeriodColumn, getHeader(reportPeriodColumnHeader, reportPeriodColumn));
        auditTable.addColumn(departmentColumn, getHeader(departmentColumnHeader, departmentColumn));
        auditTable.addColumn(typeColumn, getHeader(typeColumnHeader, typeColumn));
        auditTable.addColumn(formDataKindColumn, getHeader(formDataKindColumnHeader, formDataKindColumn));
        auditTable.addColumn(formDeclTypeColumn, getHeader(formTypeColumnHeader, formDeclTypeColumn));
        auditTable.addColumn(userLoginColumn, getHeader(userLoginColumnHeader, userLoginColumn));
        auditTable.addColumn(userRolesColumn, getHeader(userRolesColumnHeader, userRolesColumn));
        auditTable.addColumn(userIpColumn, getHeader(userIpColumnHeader, userIpColumn));
	    auditTable.addCellPreviewHandler(new CellPreviewEvent.Handler<LogSearchResultItem>() {
		    @Override
		    public void onCellPreview(CellPreviewEvent<LogSearchResultItem> event) {
			    if ("mouseover".equals(event.getNativeEvent().getType())) {
				    long index = (event.getIndex() - (pager.getPage() * auditTable.getPageSize()));
				    TableCellElement cellElement = auditTable.getRowElement((int) index).getCells().getItem(event.getColumn());
				    if (cellElement.getInnerText().replace("\u00A0", "").trim().isEmpty()) {
					    cellElement.removeAttribute("title");
				    } else {
					    cellElement.setTitle(cellElement.getInnerText());
				    }
			    }
		    }
	    });

        auditTable.setPageSize(pager.getPageSize());
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
    public void setAuditTableData(int startIndex, long count, List<LogSearchResultItem> itemList) {
        auditTable.setRowCount((int)count);
        auditTable.setRowData(startIndex, itemList);
    }

    @Override
    public void getBlobFromServer(String uuid) {
        Window.open(GWT.getHostPageBaseURL() + "download/downloadBlobController/processLogDownload/" + uuid, "", "");
    }

    @Override
    public void updateData() {
        auditTable.setVisibleRangeAndClearData(auditTable.getVisibleRange(), true);
    }

    @Override
    public void updateData(int pageNumber) {
        if (pager.getPage() == pageNumber){
            auditTable.setVisibleRangeAndClearData(auditTable.getVisibleRange(), true);
        } else {
            pager.setPage(pageNumber);
        }
    }

    @Override
    public void updateArchiveDateLbl(String archiveDate) {
        archiveDateLbl.setText(archiveDate);
    }

    @Override
    public boolean isAscSorting() {
        return isAscSorting;
    }

    @Override
    public HistoryBusinessSearchOrdering getSearchOrdering() {
        if (sortByColumn == null){
            setSortByColumn("");
        }
        return sortByColumn;
    }

    @Override
    public void setVisibleArchiveButton(boolean isVisible) {
        archive.setVisible(isVisible);
        archiveLbl.setVisible(isVisible);
    }

    @UiHandler("printButton")
    void onPrintButtonClicked(ClickEvent event){
        if (getUiHandlers() != null){
            getUiHandlers().onPrintButtonClicked();
        }
    }

    @UiHandler("archive")
    void onArchive(ClickEvent event){
        if(getUiHandlers() != null){
            getUiHandlers().onArchiveButtonClicked();
        }
    }

    private void setSortByColumn(String sortByColumn) {
        if (dateColumnHeader.equals(sortByColumn)) {
            this.sortByColumn = HistoryBusinessSearchOrdering.DATE;
        } else if (eventColumnHeader.equals(sortByColumn)) {
            this.sortByColumn = HistoryBusinessSearchOrdering.EVENT;
        } else if (noteColumnHeader.equals(sortByColumn)) {
            this.sortByColumn = HistoryBusinessSearchOrdering.NOTE;
        } else if (reportPeriodColumnHeader.equals(sortByColumn)) {
            this.sortByColumn = HistoryBusinessSearchOrdering.REPORT_PERIOD;
        } else if (departmentColumnHeader.equals(sortByColumn)) {
            this.sortByColumn = HistoryBusinessSearchOrdering.DEPARTMENT;
        } else if (typeColumnHeader.equals(sortByColumn)) {
            this.sortByColumn = HistoryBusinessSearchOrdering.TYPE;
        } else if (formDataKindColumnHeader.equals(sortByColumn)) {
            this.sortByColumn = HistoryBusinessSearchOrdering.FORM_DATA_KIND;
        } else if (formTypeColumnHeader.equals(sortByColumn)) {
            this.sortByColumn = HistoryBusinessSearchOrdering.FORM_TYPE;
        } else if (userLoginColumnHeader.equals(sortByColumn)) {
            this.sortByColumn = HistoryBusinessSearchOrdering.USER;
        } else if (userRolesColumnHeader.equals(sortByColumn)) {
            this.sortByColumn = HistoryBusinessSearchOrdering.USER_ROLE;
        } else if (userIpColumnHeader.equals(sortByColumn)) {
            this.sortByColumn = HistoryBusinessSearchOrdering.IP_ADDRESS;
        }
    }

    private Header<String> getHeader(final String columnName, Column<LogSearchResultItem, ?> returnColumn){
        GenericDataGrid.DataGridResizableHeader resizableHeader;
        final SortingHeaderCell headerCell = new SortingHeaderCell();
        resizableHeader = auditTable.createResizableHeader(columnName, returnColumn, headerCell);

        resizableHeader.setUpdater(new ValueUpdater<String>() {
            @Override
            public void update(String value) {
                setAscSorting(headerCell.isAscSort());
                setSortByColumn(columnName);
                if (getUiHandlers() != null) {
                    getUiHandlers().onSortingChanged();
                }
            }
        });
        return resizableHeader;
    }

    private void setAscSorting(boolean ascSorting){
        this.isAscSorting = ascSorting;
    }
}
