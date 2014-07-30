package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.aplana.sbrf.taxaccounting.model.BookerStatementsSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.BookerStatementsSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.BookerStatementsType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AplanaUiHandlers;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.AsyncDataProviderWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.client.BookerStatementsDataTokens;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Style;
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
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.Range;
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

    interface Binder extends UiBinder<Widget, BookerStatementsView> {
    }

    public static final String PERIOD_YEAR_TITLE = "Год";
    public static final String ACCOUNT_PERIOD_TITLE = "Период";
    public static final String DEPARTMENT_TITLE = "Подразделение";
    public static final String BOOKER_STATEMENTS_TYPE_TITLE = "Вид бух. отчётности";
    @UiField
    RefBookPickerWidget accountPeriodIds;
    @UiField
    DepartmentPickerPopupWidget departmentIds;
    @UiField(provided = true)
    ValueListBox<BookerStatementsType> bookerReportType;
    @UiField
    Button searchButton;
    @UiField
    GenericDataGrid<BookerStatementsSearchResultItem> table;
    @UiField
    FlexiblePager pager;
    @UiField
    LinkButton create;
    private BookerStatementsSearchOrdering sortByColumn;
    private AsyncDataProviderWithSortableTable dataProvider;
    private Map<Integer, String> departmentFullNames;

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

        dataProvider = new AsyncDataProviderWithSortableTable(table, this) {
            @Override
            public AplanaUiHandlers getUiHandlersX() {
                return getUiHandlers();
            }
        };

        Date current = new Date();
        accountPeriodIds.setPeriodDates(current, current);
    }

    @Override
    public void initFilter() {
        accountPeriodIds.setValue(null);
        departmentIds.setValue(null);
        bookerReportType.setValue(null);
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
    public List<Long> getAccountPeriods() {
        return accountPeriodIds.getValue();
    }

    @Override
    public BookerStatementsType getType() {
        return bookerReportType.getValue();
    }

    @Override
    public void setTableData(int start, int totalCount, List<BookerStatementsSearchResultItem> dataRows, Map<Integer, String> departmentFullNames) {
        this.departmentFullNames = departmentFullNames;
        if (dataRows == null) {
            table.setRowCount(0);
            table.setRowData(new ArrayList<BookerStatementsSearchResultItem>());
        } else {
            if (totalCount == 0) {
                start = 0;
                pager.setPage(0);
            }
            table.setRowCount(totalCount);
            table.setRowData(start, dataRows);
        }
    }

    @Override
    public void updateTable() {
        Range range = new Range(pager.getPageStart(), pager.getPageSize());
        table.setVisibleRangeAndClearData(range, true);
    }

    @Override
    public int getPageSize() {
        return pager.getPageSize();
    }

    @Override
    public BookerStatementsSearchOrdering getSearchOrdering() {
        if (sortByColumn == null) {
            sortByColumn = BookerStatementsSearchOrdering.YEAR;
        }
        return sortByColumn;
    }

    @Override
    public void setSortByColumn(String sortByColumn) {
        this.sortByColumn = BookerStatementsSearchOrdering.valueOf(sortByColumn);
    }

    @Override
    public boolean isAscSorting() {
        return dataProvider.isAscSorting();
    }

    @UiHandler("searchButton")
    void onSearchClick(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onSearch();
        }
    }

    @UiHandler("create")
    void onCreateButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onCreateClicked();
        }
    }

    private void initListeners() {
        // Подразделение
        departmentIds.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> event) {
            }
        });
    }

    private void setTableColumns() {
        TextColumn<BookerStatementsSearchResultItem> yearColumn = new TextColumn<BookerStatementsSearchResultItem>() {
            @Override
            public String getValue(BookerStatementsSearchResultItem object) {
                return object.getAccountPeriodYear().toString();
            }
        };

        TextColumn<BookerStatementsSearchResultItem> accountPeriodColumn = new TextColumn<BookerStatementsSearchResultItem>() {
            @Override
            public String getValue(BookerStatementsSearchResultItem object) {
                return object.getAccountPeriodName();
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
                                + BookerStatementsDataTokens.ACCOUNT_PERIOD_ID + "="
                                + bookerStatementsData.getAccountPeriodId() + ";"
                                + BookerStatementsDataTokens.TYPE_ID + "="
                                + bookerStatementsData.getBookerStatementsTypeId() + "\">"
                                + (bookerStatementsData.getBookerStatementsTypeId() == 0 ? "Форма 101" : "Форма 102") + "</a>");
                    }
                }) {
            public BookerStatementsSearchResultItem getValue(
                    BookerStatementsSearchResultItem object) {
                return object;
            }
        };

        table.addColumn(yearColumn, PERIOD_YEAR_TITLE);
        table.setColumnWidth(yearColumn, 5, Style.Unit.EM);

        table.addColumn(accountPeriodColumn, ACCOUNT_PERIOD_TITLE);
        table.setColumnWidth(accountPeriodColumn, 9, Style.Unit.EM);

        table.addColumn(linkColumn, BOOKER_STATEMENTS_TYPE_TITLE);
        table.setColumnWidth(linkColumn, 9, Style.Unit.EM);

        table.addColumn(departmentColumn, DEPARTMENT_TITLE);

        table.setPageSize(pager.getPageSize());
        pager.setDisplay(table);

        yearColumn.setDataStoreName(BookerStatementsSearchOrdering.YEAR.name());
        accountPeriodColumn.setDataStoreName(BookerStatementsSearchOrdering.ACCOUNT_PERIOD_NAME.name());
        departmentColumn.setDataStoreName(BookerStatementsSearchOrdering.DEPARTMENT_NAME.name());
        linkColumn.setDataStoreName(BookerStatementsSearchOrdering.BOOKER_STATEMENTS_TYPE_NAME.name());
    }
}
