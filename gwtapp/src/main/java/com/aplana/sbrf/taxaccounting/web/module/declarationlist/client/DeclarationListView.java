package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.*;
import com.aplana.sbrf.taxaccounting.web.widget.cell.*;
import com.aplana.sbrf.taxaccounting.web.widget.pager.*;
import com.google.gwt.cell.client.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.safehtml.shared.*;
import com.google.gwt.uibinder.client.*;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.google.inject.*;
import com.gwtplatform.mvp.client.*;

import java.util.*;

public class DeclarationListView extends
		ViewWithUiHandlers<DeclarationListUiHandlers> implements
		DeclarationListPresenter.MyView {


	interface MyBinder extends UiBinder<Widget, DeclarationListView> {
	}

	private DeclarationDataSearchOrdering sortByColumn;

	private boolean isAscSorting;

	@UiField
	Panel filterContentPanel;

	@UiField
	CellTable<DeclarationDataSearchResultItem> declarationTable;

	@UiField
	Panel tablePanel;

	@UiField
	Label titleDesc;

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

		AbstractPager pager = createFlexiblePager();
		pager.setDisplay(declarationTable);
		tablePanel.add(pager);
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
	public void setDeclarationsList(int start, long totalCount, List<DeclarationDataSearchResultItem> records) {
		declarationTable.setRowCount((int) totalCount);
		declarationTable.setRowData(start, records);
	}

	@Override
	public void assignDataProvider(int pageSize, AbstractDataProvider<DeclarationDataSearchResultItem> data) {
		declarationTable.setPageSize(pageSize);
		data.addDataDisplay(declarationTable);
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

	private static AbstractPager createFlexiblePager(){
		final boolean showFastForwardButton = false;
		final int fastForwardRows = 0;
		final boolean showLastPageButton = true;

		AbstractPager pager =  new FlexiblePager(FlexiblePager.TextLocation.CENTER, showFastForwardButton, fastForwardRows,
				showLastPageButton);

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
