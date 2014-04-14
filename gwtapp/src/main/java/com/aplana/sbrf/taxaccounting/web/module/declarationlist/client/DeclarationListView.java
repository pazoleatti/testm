package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataTokens;
import com.aplana.sbrf.taxaccounting.web.widget.cell.SortingHeaderCell;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;
import java.util.Map;

public class DeclarationListView extends
		ViewWithUiHandlers<DeclarationListUiHandlers> implements
		DeclarationListPresenter.MyView {

    public static final String DECLARATION_HEADER = "Cписок деклараций";
    public static final String DECLARATION_HEADER_D = "Cписок уведомлений";
    public static final String DECLARATION_CREATE = "Создать декларацию...";
    public static final String DECLARATION_CREATE_D = "Создать уведомление...";
    public static final String DECLARATION_CREATE_TITLE = "Создание деклараций";
    public static final String DECLARATION_CREATE_TITLE_D = "Создание уведомления";

    public static final String DECLARATION_TYPE_TITLE = "Вид декларации";
    public static final String PERIOD_TITLE = "Период";
    public static final String PERIOD_YEAR_TITLE = "Год";

	interface MyBinder extends UiBinder<Widget, DeclarationListView> {
	}

    private GenericDataGrid.DataGridResizableHeader declarationTypeHeader;
    private TextColumn<DeclarationDataSearchResultItem> declarationTypeColumn;
    private GenericDataGrid.DataGridResizableHeader reportPeriodHeader;
    private Column<DeclarationDataSearchResultItem, DeclarationDataSearchResultItem> reportPeriodColumn;
    private GenericDataGrid.DataGridResizableHeader reportPeriodYearHeader;
    private TextColumn<DeclarationDataSearchResultItem> reportPeriodYearColumn;

	private DeclarationDataSearchOrdering sortByColumn;

	private boolean isAscSorting;

    private Map<Integer, String> departmentFullNames;

    @UiField
    Label declarationHeader;

	@UiField
	Panel filterContentPanel;

	@UiField
    GenericDataGrid<DeclarationDataSearchResultItem> declarationTable;

    @UiField
    FlexiblePager pager;

	@UiField
	Label titleDesc;
    @UiField
    LinkButton create;

    private final AsyncDataProvider<DeclarationDataSearchResultItem> dataProvider = new AsyncDataProvider<DeclarationDataSearchResultItem>() {
        @Override
        protected void onRangeChanged(HasData<DeclarationDataSearchResultItem> display) {
            if (getUiHandlers() != null){
                final Range range = display.getVisibleRange();
                getUiHandlers().onRangeChange(range.getStart(), range.getLength());
            }
        }
    };

	@Inject
	public DeclarationListView(final MyBinder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));


		TextColumn<DeclarationDataSearchResultItem> departmentColumn = new TextColumn<DeclarationDataSearchResultItem>() {
			@Override
			public String getValue(DeclarationDataSearchResultItem object) {
				return departmentFullNames.get(object.getDepartmentId());
			}
		};

        reportPeriodYearColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return String.valueOf(object.getReportPeriodYear());
            }
        };

        reportPeriodColumn = new Column<DeclarationDataSearchResultItem, DeclarationDataSearchResultItem>(
                new AbstractCell<DeclarationDataSearchResultItem>() {

                    @Override
                    public void render(Context context,
                                       DeclarationDataSearchResultItem declaration,
                                       SafeHtmlBuilder sb) {
                        if (declaration == null) {
                            return;
                        }
                        sb.appendHtmlConstant("<a href=\"#"
                                + DeclarationDataTokens.declarationData + ";"
                                + DeclarationDataTokens.declarationId + "="
                                + declaration.getDeclarationDataId() + "\">"
                                + declaration.getReportPeriodName() + "</a>");
                    }
                }) {
            @Override
            public DeclarationDataSearchResultItem getValue(
                    DeclarationDataSearchResultItem object) {
                return object;
            }
		};

        declarationTypeColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return object.getDeclarationType();
            }
        };

		TextColumn<DeclarationDataSearchResultItem> stateColumn = new TextColumn<DeclarationDataSearchResultItem>() {
			@Override
			public String getValue(DeclarationDataSearchResultItem object) {
				return object.isAccepted() ? "Принята" : "Создана";
			}
		};

        declarationTypeHeader = (GenericDataGrid.DataGridResizableHeader) getHeader(DECLARATION_TYPE_TITLE, declarationTypeColumn);
        reportPeriodHeader = (GenericDataGrid.DataGridResizableHeader) getHeader(PERIOD_TITLE, reportPeriodColumn);
        reportPeriodYearHeader = (GenericDataGrid.DataGridResizableHeader) getHeader(PERIOD_YEAR_TITLE, reportPeriodYearColumn);

        declarationTable.addColumn(declarationTypeColumn, declarationTypeHeader);
        declarationTable.setColumnWidth(declarationTypeColumn, 0, Style.Unit.EM);
		declarationTable.addColumn(departmentColumn, getHeader("Подразделение", departmentColumn));
		declarationTable.addColumn(reportPeriodYearColumn, reportPeriodYearHeader);
        declarationTable.setColumnWidth(reportPeriodYearColumn, 0, Style.Unit.EM);
		declarationTable.addColumn(reportPeriodColumn, reportPeriodHeader);
        declarationTable.setColumnWidth(reportPeriodColumn, 0, Style.Unit.EM);
		declarationTable.addColumn(stateColumn, getHeader("Состояние", stateColumn));

        pager.setDisplay(declarationTable);

        declarationTable.setPageSize(pager.getPageSize());
        dataProvider.addDataDisplay(declarationTable);
	}
	

	@Override
	public void setInSlot(Object slot, IsWidget content) {
		if (slot == DeclarationListPresenter.TYPE_filterPresenter) {
			filterContentPanel.clear();
			if (content != null) {
				filterContentPanel.add(content);
			}
		} else {
			super.setInSlot(slot, content);
		}
	}

    @Override
    public void updateData(int pageNumber) {
        if (pager.getPage() == pageNumber){
            updateData();
        } else {
            pager.setPage(pageNumber);
        }
    }

    @Override
    public void setTableData(int start, long totalCount, List<DeclarationDataSearchResultItem> records, Map<Integer, String> departmentFullNames) {
        declarationTable.setRowCount((int) totalCount);
        declarationTable.setRowData(start, records);
        this.departmentFullNames = departmentFullNames;
    }

    @Override
    public void updateData() {
        declarationTable.setVisibleRangeAndClearData(declarationTable.getVisibleRange(), true);
    }

    @Override
	public DeclarationDataSearchOrdering getSearchOrdering(){
		if (sortByColumn == null){
			setSortByColumn("");
		}
		return sortByColumn;
	}

	@Override
	public boolean isAscSorting(){
		return isAscSorting;
	}

	@Override
	public void updateTitle(TaxType taxType){
		titleDesc.setText(taxType.getName());
        if (!taxType.equals(TaxType.DEAL)) {
            declarationHeader.setText(DECLARATION_HEADER);
            declarationTable.clearColumnWidth(declarationTypeColumn);
            declarationTable.clearColumnWidth(reportPeriodYearColumn);
            declarationTable.clearColumnWidth(reportPeriodColumn);
            create.setText(DECLARATION_CREATE);
            create.setTitle(DECLARATION_CREATE_TITLE);
            declarationTypeHeader.setTitle(DECLARATION_TYPE_TITLE);
            reportPeriodYearHeader.setTitle(PERIOD_YEAR_TITLE);
            reportPeriodHeader.setTitle(PERIOD_TITLE);
            declarationHeader.setText(DECLARATION_HEADER);
        } else {
            declarationTable.setColumnWidth(declarationTypeColumn, 0, Style.Unit.EM);
            declarationTable.setColumnWidth(reportPeriodYearColumn, 0, Style.Unit.EM);
            declarationTable.setColumnWidth(reportPeriodColumn, 0, Style.Unit.EM);
            create.setText(DECLARATION_CREATE_D);
            create.setTitle(DECLARATION_CREATE_TITLE_D);
            declarationTypeHeader.setTitle("");
            reportPeriodYearHeader.setTitle("");
            reportPeriodHeader.setTitle("");
            declarationHeader.setText(DECLARATION_HEADER_D);
        }
        declarationTable.redrawHeaders();
	}

    @UiHandler("create")
    void onCreateButtonClicked(ClickEvent event){
        if (getUiHandlers() != null) {
            getUiHandlers().onCreateClicked();
        }
    }

    private Header<String> getHeader(final String columnName, Column<DeclarationDataSearchResultItem, ?> returnColumn) {
        GenericDataGrid.DataGridResizableHeader resizableHeader;
        final SortingHeaderCell headerCell = new SortingHeaderCell();
        resizableHeader = declarationTable.createResizableHeader(columnName, returnColumn, headerCell);

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

	private void setSortByColumn(String sortByColumn){
		if ("Подразделение".equals(sortByColumn)){
			this.sortByColumn = DeclarationDataSearchOrdering.DEPARTMENT_NAME;
		} else if ("Период".equals(sortByColumn)){
			this.sortByColumn = DeclarationDataSearchOrdering.REPORT_PERIOD_NAME;
		} else if("Вид декларации".equals(sortByColumn)){
			this.sortByColumn = DeclarationDataSearchOrdering.DECLARATION_TYPE_NAME;
		} else {
			this.sortByColumn = DeclarationDataSearchOrdering.ID;
		}
	}

	private void setAscSorting(boolean ascSorting){
		this.isAscSorting = ascSorting;
	}

    @Override
    public void updatePageSize(TaxType taxType) {
        pager.setType("declarationList" + taxType.getCode());
        declarationTable.setPageSize(pager.getPageSize());
    }
}
