package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.DeclarationSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataTokens;
import com.aplana.sbrf.taxaccounting.web.widget.cell.SortingHeaderCell;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

public class DeclarationListView extends
		ViewWithUiHandlers<DeclarationListUiHandlers> implements
		DeclarationListPresenter.MyView {


	interface MyBinder extends UiBinder<Widget, DeclarationListView> {
	}

	private final Widget widget;

	private DeclarationSearchOrdering sortByColumn;

	private boolean isAscSorting;

	@UiField
	Panel filterContentPanel;

	@UiField
	CellTable<DeclarationSearchResultItem> declarationTable;

	@UiField
	VerticalPanel verticalPanelWithTable;

	@Inject
	public DeclarationListView(final MyBinder binder) {
		widget = binder.createAndBindUi(this);


		TextColumn<DeclarationSearchResultItem> departmentColumn = new TextColumn<DeclarationSearchResultItem>() {
			@Override
			public String getValue(DeclarationSearchResultItem object) {
				return object.getDepartmentName();
			}
		};

		TextColumn<DeclarationSearchResultItem> reportPeriodColumn = new TextColumn<DeclarationSearchResultItem>() {
			@Override
			public String getValue(DeclarationSearchResultItem object) {
				return object.getReportPeriodName();
			}
		};

		TextColumn<DeclarationSearchResultItem> declarationTypeColumn = new TextColumn<DeclarationSearchResultItem>() {
			@Override
			public String getValue(DeclarationSearchResultItem object) {
				return object.getDeclarationType();
			}
		};

		TextColumn<DeclarationSearchResultItem> stateColumn = new TextColumn<DeclarationSearchResultItem>() {
			@Override
			public String getValue(DeclarationSearchResultItem object) {
				return object.isAccepted() ? "Принята" : "Создана";
			}
		};

		Column<DeclarationSearchResultItem, DeclarationSearchResultItem> linkColumn = new Column<DeclarationSearchResultItem, DeclarationSearchResultItem>(
				new AbstractCell<DeclarationSearchResultItem>() {

					@Override
					public void render(Context context,
					                   DeclarationSearchResultItem declaration,
					                   SafeHtmlBuilder sb) {
						if (declaration == null) {
							return;
						}
						sb.appendHtmlConstant("<a href=\"#"
								+ DeclarationDataTokens.declarationData + ";"
								+ DeclarationDataTokens.declarationId + "="
								+ declaration.getDeclarationId() + "\">"
								+ declaration.getTaxType().getName() + "</a>");
					}
				}) {
			public DeclarationSearchResultItem getValue(
					DeclarationSearchResultItem object) {
				return object;
			}
		};

		declarationTable.addColumn(departmentColumn, getHeader("Подразделение"));
		declarationTable.addColumn(linkColumn, getHeader("Вид налога"));
		declarationTable.addColumn(reportPeriodColumn, getHeader("Отчетный период"));
		declarationTable.addColumn(declarationTypeColumn, getHeader("Вид декларации"));
		declarationTable.addColumn(stateColumn, getHeader("Статус декларации"));

		FlexiblePager pager = createFlexiblePager();
		pager.setDisplay(declarationTable);
		verticalPanelWithTable.add(pager);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setInSlot(Object slot, Widget content) {
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
	public void setDeclarationsList(int start, long totalCount, List<DeclarationSearchResultItem> records) {
		declarationTable.setRowCount((int) totalCount);
		declarationTable.setRowData(start, records);
	}

	@Override
	public void assignDataProvider(int pageSize, AbstractDataProvider<DeclarationSearchResultItem> data) {
		declarationTable.setPageSize(pageSize);
		data.addDataDisplay(declarationTable);
	}

	@Override
	public DeclarationSearchOrdering getSearchOrdering(){
		final String DEFAULT_SORTING_BY_ID = "";
		if (sortByColumn != null){
			return sortByColumn;
		}
		setSortByColumn(DEFAULT_SORTING_BY_ID);
		return sortByColumn;
	}

	@Override
	public boolean isAscSorting(){
		return isAscSorting;
	}

	@UiHandler("apply")
	void onApplyButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onApplyFilter();
		}
	}

	@UiHandler("create")
	void onCreateButtonClicked(ClickEvent event){
		if (getUiHandlers() != null) {
			getUiHandlers().onCreateClicked();
		}
	}

	private static FlexiblePager createFlexiblePager(){
		final boolean showFastForwardButton = false;
		final int fastForwardRows = 0;
		final boolean showLastPageButton = true;
		FlexiblePager pager =  new FlexiblePager(SimplePager.TextLocation.CENTER, showFastForwardButton, fastForwardRows,
				showLastPageButton);
		pager.setRangeLimited(true);
		pager.getElement().getStyle().setProperty("marginLeft", "auto");
		pager.getElement().getStyle().setProperty("marginRight", "auto");
		return pager;
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
			this.sortByColumn = DeclarationSearchOrdering.DEPARTMENT_NAME;
		} else if ("Отчетный период".equals(sortByColumn)){
			this.sortByColumn = DeclarationSearchOrdering.REPORT_PERIOD_NAME;
		} else if("Вид декларации".equals(sortByColumn)){
			this.sortByColumn = DeclarationSearchOrdering.DECLARATION_TYPE_NAME;
		} else {
			this.sortByColumn = DeclarationSearchOrdering.ID;
		}
	}

	private void setAscSorting(boolean ascSorting){
		this.isAscSorting = ascSorting;
	}
}
