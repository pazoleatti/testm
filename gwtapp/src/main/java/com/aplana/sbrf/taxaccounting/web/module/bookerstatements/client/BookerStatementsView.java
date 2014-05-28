package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.client.BookerStatementsDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.HorizontalAlignment;
import com.aplana.sbrf.taxaccounting.web.widget.cell.SortingHeaderCell;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopup;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * View для Формы фильтрации бухгалтерской отчётности
 *
 * @author Dmitriy Levykin
 */
public class BookerStatementsView extends ViewWithUiHandlers<BookerStatementsUiHandlers>
        implements BookerStatementsPresenter.MyView {

    private boolean isAscSorting;
    private BookerStatementsSearchOrdering sortByColumn;

    interface Binder extends UiBinder<Widget, BookerStatementsView> {
    }

    public static final String PERIOD_YEAR_TITLE = "Год";
    public static final String REPORT_PERIOD_TITLE = "Период";
    public static final String DEPARTMENT_TITLE = "Подразделение";
    public static final String BOOKER_STATEMENTS_TYPE_TITLE = "Вид бухгалтерской отчётности";

    private Map<Integer, String> departmentFullNames;

    @UiField
    PeriodPickerPopup reportPeriodIds;

    @UiField
    DepartmentPickerPopupWidget departmentIds;

    @UiField(provided = true)
    ValueListBox<BookerStatementsType> bookerReportType;

    @UiField
    Button searchButton;

    @UiField
    GenericDataGrid<BookerStatementsSearchResultItem> dataTable;

    @UiField
    FlexiblePager pager;

    @UiField
    LinkButton create;

    @Inject
    @UiConstructor
    public BookerStatementsView(final Binder uiBinder) {
        super();

        bookerReportType = new ValueListBox<BookerStatementsType>(new AbstractRenderer<BookerStatementsType>() {
            @Override
            public String render(BookerStatementsType object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        initWidget(uiBinder.createAndBindUi(this));

        initListeners();

        setTableColumns();

        dataTable.setPageSize(pager.getPageSize());
        pager.setDisplay(dataTable);
    }

    @Override
    public void initFilter() {
        reportPeriodIds.setValue(null);
        departmentIds.setValue(null);
        bookerReportType.setValue(null);
    }

    private void initListeners() {
        // Подразделение
        departmentIds.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> event) {
            }
        });
    }

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartments) {
        departmentIds.setAvalibleValues(departments, availableDepartments);
    }

    @Override
    public void setDepartment(List<Integer> departments) {
        departmentIds.setValue(departments);
    }

    @Override
    public void setReportPeriods(List<ReportPeriod> reportPeriods) {
        reportPeriodIds.setPeriods(reportPeriods);
    }

    @Override
    public void setReportPeriod(List<Integer> reportPeriods) {
        reportPeriodIds.setValue(reportPeriods);
    }

    @Override
    public void setBookerReportTypes(List<BookerStatementsType> bookerReportTypes) {
        bookerReportType.setAcceptableValues(bookerReportTypes);
    }

    @Override
    public void setBookerReportType(BookerStatementsType bookerReportType) {
        this.bookerReportType.setValue(bookerReportType);
    }
    @Override
    public List<Integer> getDepartments() {
        return departmentIds.getValue();
    }

    @Override
    public List<Integer> getReportPeriods() {
        return reportPeriodIds.getValue();
    }

    @Override
    public BookerStatementsType getType() {
        return bookerReportType.getValue();
    }


    @Override
    public void setTableData(int start, int totalCount, List<BookerStatementsSearchResultItem> dataRows, Map<Integer, String> departmentFullNames) {
        this.departmentFullNames = departmentFullNames;
        if (dataRows == null) {
            dataTable.setRowCount(0);
            dataTable.setRowData(new ArrayList<BookerStatementsSearchResultItem>());
        } else {
            if (totalCount == 0) {
                start = 0;
                pager.setPage(0);
            }
            dataTable.setRowCount(totalCount);
            dataTable.setRowData(start, dataRows);
        }
    }

    @Override
    public void updateTable() {
        Range range = new Range(pager.getPageStart(), pager.getPageSize());
        dataTable.setVisibleRangeAndClearData(range, true);
    }

    public void setTableColumns() {
        TextColumn<BookerStatementsSearchResultItem> yearColumn = new TextColumn<BookerStatementsSearchResultItem>() {
            @Override
            public String getValue(BookerStatementsSearchResultItem object) {
                return object.getReportPeriodYear().toString();
            }
        };

        TextColumn<BookerStatementsSearchResultItem> reportPeriodColumn = new TextColumn<BookerStatementsSearchResultItem>() {
            @Override
            public String getValue(BookerStatementsSearchResultItem object) {
                return object.getReportPeriodName();
            }
        };

        TextColumn<BookerStatementsSearchResultItem> departmentColumn = new TextColumn<BookerStatementsSearchResultItem>() {
            @Override
            public String getValue(BookerStatementsSearchResultItem object) {
                return departmentFullNames.get(object.getDepartmentId());
            }
        };

        Column<BookerStatementsSearchResultItem, BookerStatementsSearchResultItem> linkColumn = new Column<BookerStatementsSearchResultItem, BookerStatementsSearchResultItem>(
                new AbstractCell<BookerStatementsSearchResultItem>() {

                    @Override
                    public void render(Context context,
                                       BookerStatementsSearchResultItem bookerStatementsData,
                                       SafeHtmlBuilder sb) {
                        if (bookerStatementsData == null) {
                            return;
                        }
                        sb.appendHtmlConstant("<a href=\"#"
                                + BookerStatementsDataTokens.bookerStatements + ";"
                                + BookerStatementsDataTokens.DEPARTMENT_ID + "="
                                + bookerStatementsData.getDepartmentId() + ";"
                                + BookerStatementsDataTokens.REPORT_PERIOD_ID + "="
                                + bookerStatementsData.getReportPeriodId() + ";"
                                + BookerStatementsDataTokens.TYPE_ID + "="
                                + bookerStatementsData.getBookerStatementsTypeId() + "\">"
                                + (bookerStatementsData.getBookerStatementsTypeId()==0 ? "Форма 101" : "Форма 102") + "</a>");
                    }
                }) {
            public BookerStatementsSearchResultItem getValue(
                    BookerStatementsSearchResultItem object) {
                return object;
            }
        };

        dataTable.addColumn(yearColumn, getHeader(PERIOD_YEAR_TITLE, yearColumn));
        dataTable.setColumnWidth(yearColumn, 5, Style.Unit.EM);

        dataTable.addColumn(reportPeriodColumn, getHeader(REPORT_PERIOD_TITLE, reportPeriodColumn));
        dataTable.setColumnWidth(reportPeriodColumn, 9, Style.Unit.EM);

        dataTable.addColumn(departmentColumn, getHeader(DEPARTMENT_TITLE, departmentColumn));

        dataTable.addColumn(linkColumn, getHeader(BOOKER_STATEMENTS_TYPE_TITLE, linkColumn));
        dataTable.setColumnWidth(linkColumn, 9, Style.Unit.EM);
    }

    @Override
    public int getPageSize() {
        return pager.getPageSize();
    }

    @Override
    public void assignDataProvider(int pageSize, AbstractDataProvider<BookerStatementsSearchResultItem> data) {
        dataTable.setPageSize(pageSize);
        data.addDataDisplay(dataTable);
    }

    @UiHandler("searchButton")
    void onSearchClick(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onSearch();
        }
    }

    private void setSortByColumn(String sortByColumn){
        if (BOOKER_STATEMENTS_TYPE_TITLE.equals(sortByColumn)) {
            this.sortByColumn = BookerStatementsSearchOrdering.BOOKER_STATEMENTS_TYPE_NAME;
        } else if (DEPARTMENT_TITLE.equals(sortByColumn)){
            this.sortByColumn = BookerStatementsSearchOrdering.DEPARTMENT_NAME;
        } else if (REPORT_PERIOD_TITLE.equals(sortByColumn)){
            this.sortByColumn = BookerStatementsSearchOrdering.REPORT_PERIOD_NAME;
        } else if (PERIOD_YEAR_TITLE.equals(sortByColumn)){
            this.sortByColumn = BookerStatementsSearchOrdering.YEAR;
        } else {
            this.sortByColumn = BookerStatementsSearchOrdering.ID;
        }
    }

    private void setAscSorting(boolean ascSorting){
        this.isAscSorting = ascSorting;
    }
    /**
     * Добавление заголовка для столбца, который может резайзиться
     * @param columnName название
     * @param returnColumn объект колонки
     * @return заголовок с сортировкой и резайзом
     */
    private Header<String> getHeader(final String columnName, Column<BookerStatementsSearchResultItem, ?> returnColumn){
        GenericDataGrid.DataGridResizableHeader resizableHeader;
        final SortingHeaderCell headerCell = new SortingHeaderCell();
        resizableHeader = dataTable.createResizableHeader(columnName, returnColumn, headerCell);

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

    private HasHorizontalAlignment.HorizontalAlignmentConstant convertAlignment(HorizontalAlignment alignment) {
        switch (alignment) {
            case ALIGN_LEFT:
                return HasHorizontalAlignment.ALIGN_LEFT;
            case ALIGN_CENTER:
                return HasHorizontalAlignment.ALIGN_CENTER;
            case ALIGN_RIGHT:
                return HasHorizontalAlignment.ALIGN_RIGHT;
            default:
                return HasHorizontalAlignment.ALIGN_LEFT;
        }
    }

    @Override
    public boolean isAscSorting() {
        return isAscSorting;
    }

    @Override
    public BookerStatementsSearchOrdering getSearchOrdering() {
        if (sortByColumn == null){
            setSortByColumn("");
        }
        return sortByColumn;
    }

    @UiHandler("create")
    void onCreateButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onCreateClicked();
        }
    }
}
