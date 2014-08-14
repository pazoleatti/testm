package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataTokens;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SingleSelectionModel;
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
    public static final String DEPARTMENT_TITLE = "Подразделение";
    public static final String STATE_TITLE = "Состояние";
    public static final String PERIOD_TITLE = "Период";

	interface MyBinder extends UiBinder<Widget, DeclarationListView> {
	}

    private GenericDataGrid.DataGridResizableHeader declarationTypeHeader;
    private TextColumn<DeclarationDataSearchResultItem> declarationTypeColumn;
    private GenericDataGrid.DataGridResizableHeader reportPeriodHeader;
    private Column<DeclarationDataSearchResultItem, DeclarationDataSearchResultItem> reportPeriodColumn;

	private DeclarationDataSearchOrdering sortByColumn;

	private boolean isAscSorting;

    private Map<Integer, String> departmentFullNames;


    private SingleSelectionModel<DeclarationDataSearchResultItem> selectionModel;

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
                // заполенине параметров по какой сортировать
                if (declarationTable.getColumnSortList().size() > 0) {
                    isAscSorting = declarationTable.getColumnSortList().get(0).isAscending();
                    setSortByColumn((String) declarationTable.getHeader(declarationTable.getColumnIndex((Column<DeclarationDataSearchResultItem, ?>) declarationTable.getColumnSortList().get(0).getColumn())).getValue());
                }
                final Range range = display.getVisibleRange();
                getUiHandlers().onRangeChange(range.getStart(), range.getLength());
            }
        }
    };

	@Inject
	public DeclarationListView(final MyBinder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));

        selectionModel = new SingleSelectionModel<DeclarationDataSearchResultItem>();
        declarationTable.setSelectionModel(selectionModel);

        pager.setDisplay(declarationTable);
        declarationTable.setPageSize(pager.getPageSize());
        dataProvider.addDataDisplay(declarationTable);
        declarationTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(declarationTable));

        declarationTable.getColumnSortList().setLimit(1);       // сортировка только по одной колонке
	}

    @Override
    public void initTable(TaxType taxType) {
        clearTable();

        TextColumn<DeclarationDataSearchResultItem> departmentColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return departmentFullNames.get(object.getDepartmentId());
            }
        };
        
        Column reportPeriodYearColumn = null;
        if (taxType == TaxType.DEAL) {
            reportPeriodYearColumn = new Column<DeclarationDataSearchResultItem, DeclarationDataSearchResultItem>(
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
                                    + declaration.getReportPeriodYear() + ": " + declaration.getReportPeriodName() + "</a>");
                        }
                    }) {
                @Override
                public DeclarationDataSearchResultItem getValue(
                        DeclarationDataSearchResultItem object) {
                    return object;
                }
            };
        } else {
            reportPeriodYearColumn = new TextColumn<DeclarationDataSearchResultItem>() {
                @Override
                public String getValue(DeclarationDataSearchResultItem object) {
                    return String.valueOf(object.getReportPeriodYear());
                }
            };
        }

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
                                + declaration.getReportPeriodYear() + ": " + declaration.getReportPeriodName() + "</a>");
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

        departmentColumn.setSortable(true);
        reportPeriodYearColumn.setSortable(true);
        reportPeriodColumn.setSortable(true);
        declarationTypeColumn.setSortable(true);
        stateColumn.setSortable(true);

        declarationTypeHeader = declarationTable.createResizableHeader(DECLARATION_TYPE_TITLE, declarationTypeColumn);
        reportPeriodHeader = declarationTable.createResizableHeader(PERIOD_TITLE, reportPeriodColumn);

        if (taxType == TaxType.DEAL) {
            declarationTable.addColumn(departmentColumn, declarationTable.createResizableHeader(DEPARTMENT_TITLE, departmentColumn));
            declarationTable.addColumn(reportPeriodYearColumn, declarationTable.createResizableHeader(PERIOD_TITLE, reportPeriodYearColumn));
            declarationTable.addColumn(stateColumn, declarationTable.createResizableHeader(STATE_TITLE, stateColumn));
        } else {
            declarationTable.addColumn(declarationTypeColumn, declarationTypeHeader);
            // http://jira.aplana.com/browse/SBRFACCTAX-7742
            //declarationTable.setColumnWidth(declarationTypeColumn, 0, Style.Unit.EM);
            declarationTable.addColumn(departmentColumn, declarationTable.createResizableHeader(DEPARTMENT_TITLE, departmentColumn));
            declarationTable.addColumn(reportPeriodColumn, reportPeriodHeader);
            // http://jira.aplana.com/browse/SBRFACCTAX-7742
            //declarationTable.setColumnWidth(reportPeriodColumn, 0, Style.Unit.EM);
            declarationTable.addColumn(stateColumn, declarationTable.createResizableHeader(STATE_TITLE, stateColumn));
        }
    }

    @Override
    public Long getSelectedId() {
        DeclarationDataSearchResultItem item = selectionModel.getSelectedObject();
        if (item != null) {
            return item.getDeclarationDataId();
        }
        return null;
    }

    @Override
    public void clearTable() {
        while (declarationTable.getColumnCount() > 0) {
            declarationTable.removeColumn(0);
        }
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
        if(pageNumber == 0){
            declarationTable.getColumnSortList().clear();
        }
        if (pager.getPage() == pageNumber){
            updateData();
        } else {
            pager.setPage(pageNumber);
        }
    }

    @Override
    public void setTableData(int start, long totalCount, List<DeclarationDataSearchResultItem> records, Map<Integer, String> departmentFullNames, Long selectedItemId) {
        declarationTable.setRowCount((int) totalCount);
        declarationTable.setRowData(start, records);
        this.departmentFullNames = departmentFullNames;
        selectionModel.clear();
        if (selectedItemId != null) {
            for(DeclarationDataSearchResultItem item: records) {
                if (item.getDeclarationDataId().equals(selectedItemId)) {
                    selectionModel.setSelected(item, true);
                    break;
                }
            }
        }
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
            declarationTable.clearColumnWidth(reportPeriodColumn);
            create.setText(DECLARATION_CREATE);
            create.setTitle(DECLARATION_CREATE_TITLE);
            declarationTypeHeader.setTitle(DECLARATION_TYPE_TITLE);
            reportPeriodHeader.setTitle(PERIOD_TITLE);
            declarationHeader.setText(DECLARATION_HEADER);
        } else {
            create.setText(DECLARATION_CREATE_D);
            create.setTitle(DECLARATION_CREATE_TITLE_D);
            declarationTypeHeader.setTitle("");
            reportPeriodHeader.setTitle("");
            declarationHeader.setText(DECLARATION_HEADER_D);
            reportPeriodHeader.setTitle(PERIOD_TITLE);
            declarationTable.clearColumnWidth(reportPeriodColumn);
        }
        declarationTable.redrawHeaders();
	}

    @UiHandler("create")
    void onCreateButtonClicked(ClickEvent event){
        if (getUiHandlers() != null) {
            getUiHandlers().onCreateClicked();
        }
    }

    @Override
    public void setPage(Integer page) {
        if (page != null) pager.setPage(page);
    }

    @Override
    public int getPage() {
        return pager.getPage();
    }

	private void setSortByColumn(String sortByColumn){
		if (DEPARTMENT_TITLE.equals(sortByColumn)){
			this.sortByColumn = DeclarationDataSearchOrdering.DEPARTMENT_NAME;
		} else if (PERIOD_TITLE.equals(sortByColumn)){
			this.sortByColumn = DeclarationDataSearchOrdering.REPORT_PERIOD_YEAR;
		} else if(DECLARATION_TYPE_TITLE.equals(sortByColumn)){
			this.sortByColumn = DeclarationDataSearchOrdering.DECLARATION_TYPE_NAME;
        } else if(STATE_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.DECLARATION_ACCEPTED;
        } else {
			this.sortByColumn = DeclarationDataSearchOrdering.ID;
		}
	}

    @Override
    public void updatePageSize(TaxType taxType) {
        pager.setType("declarationList" + taxType.getCode());
        declarationTable.setPageSize(pager.getPageSize());
    }
}
