package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataTokens;
import com.aplana.sbrf.taxaccounting.web.widget.cell.SortingHeaderCell;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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

public class DeclarationListView extends
		ViewWithUiHandlers<DeclarationListUiHandlers> implements
		DeclarationListPresenter.MyView {


	interface MyBinder extends UiBinder<Widget, DeclarationListView> {
	}

    private static final int PAGE_SIZE = 20;

	private DeclarationDataSearchOrdering sortByColumn;

	private boolean isAscSorting;

	@UiField
	Panel filterContentPanel;

	@UiField
	CellTable<DeclarationDataSearchResultItem> declarationTable;

    @UiField
    FlexiblePager pager;

	@UiField
	Label titleDesc;

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
				return object.getDepartmentName();
			}
		};

        TextColumn<DeclarationDataSearchResultItem> reportPeriodYearColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return String.valueOf(object.getReportPeriodYear());
            }
        };

		TextColumn<DeclarationDataSearchResultItem> reportPeriodColumn = new TextColumn<DeclarationDataSearchResultItem>() {
			@Override
			public String getValue(DeclarationDataSearchResultItem object) {
				return object.getReportPeriodName();
			}
		};

		TextColumn<DeclarationDataSearchResultItem> declarationTypeColumn = new TextColumn<DeclarationDataSearchResultItem>() {
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

		Column<DeclarationDataSearchResultItem, DeclarationDataSearchResultItem> linkColumn = new Column<DeclarationDataSearchResultItem, DeclarationDataSearchResultItem>(
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
								+ declaration.getTaxType().getName() + "</a>");
					}
				}) {
			@Override
			public DeclarationDataSearchResultItem getValue(
					DeclarationDataSearchResultItem object) {
				return object;
			}
		};

		declarationTable.addColumn(departmentColumn, getHeader("Подразделение"));
		declarationTable.addColumn(linkColumn, getHeader("Вид налога"));
		declarationTable.addColumn(reportPeriodYearColumn, getHeader("Год"));
		declarationTable.addColumn(reportPeriodColumn, getHeader("Период"));
		declarationTable.addColumn(declarationTypeColumn, getHeader("Вид декларации"));
		declarationTable.addColumn(stateColumn, getHeader("Состояние"));

        declarationTable.setPageSize(PAGE_SIZE);
        dataProvider.addDataDisplay(declarationTable);

		pager.setDisplay(declarationTable);
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
    public void setTableData(int start, long totalCount, List<DeclarationDataSearchResultItem> records) {
        declarationTable.setRowCount((int) totalCount);
        declarationTable.setRowData(start, records);
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
	public void updateTitle(String title){
		titleDesc.setText(title);
	}

	private Header<String> getHeader(final String columnName){
		Header<String> columnHeader = new Header<String>(new SortingHeaderCell()) {
			@Override
			public String getValue() {
				return columnName;
			}
		};

		columnHeader.setUpdater(new ValueUpdater<String>() {
			@Override
			public void update(String value) {
				setAscSorting(!isAscSorting);
				setSortByColumn(columnName);
				if (getUiHandlers() != null) {
					getUiHandlers().onSortingChanged();
				}
			}
		});
		return columnHeader;
	}

	private void setSortByColumn(String sortByColumn){
		if ("Подразделение".equals(sortByColumn)){
			this.sortByColumn = DeclarationDataSearchOrdering.DEPARTMENT_NAME;
		} else if ("Отчетный период".equals(sortByColumn)){
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
}
