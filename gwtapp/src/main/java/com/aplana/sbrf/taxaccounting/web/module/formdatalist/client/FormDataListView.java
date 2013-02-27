package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.*;
import com.aplana.sbrf.taxaccounting.web.widget.cell.*;
import com.aplana.sbrf.taxaccounting.web.widget.pager.*;
import com.google.gwt.cell.client.*;
import com.google.gwt.safehtml.shared.*;
import com.google.gwt.uibinder.client.*;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.google.inject.*;
import com.gwtplatform.mvp.client.*;

import java.util.*;

public class FormDataListView extends
		ViewWithUiHandlers<FormDataListUiHandlers> implements
		FormDataListPresenter.MyView {

	interface MyBinder extends UiBinder<Widget, FormDataListView> {
	}

	private final Widget widget;

	private FormDataSearchOrdering sortByColumn;

	private boolean isAscSorting;

	@UiField
	Panel filterContentPanel;

	@UiField
	CellTable<FormDataSearchResultItem> formDataTable;
	
	@UiField
	FlexiblePager pager;

	@Inject
	public FormDataListView(final MyBinder binder) {

		widget = binder.createAndBindUi(this);

		TextColumn<FormDataSearchResultItem> formKindColumn = new TextColumn<FormDataSearchResultItem>() {
			@Override
			public String getValue(FormDataSearchResultItem object) {
				return object.getFormDataKind().getName();
			}
		};

		TextColumn<FormDataSearchResultItem> departmentColumn = new TextColumn<FormDataSearchResultItem>() {
			@Override
			public String getValue(FormDataSearchResultItem object) {
				return object.getDepartmentName();
			}
		};

		TextColumn<FormDataSearchResultItem> reportPeriodColumn = new TextColumn<FormDataSearchResultItem>() {
			@Override
			public String getValue(FormDataSearchResultItem object) {
				return object.getReportPeriodName();
			}
		};

		TextColumn<FormDataSearchResultItem> stateColumn = new TextColumn<FormDataSearchResultItem>() {
			@Override
			public String getValue(FormDataSearchResultItem object) {
				return object.getState().getName();
			}
		};

		Column<FormDataSearchResultItem, FormDataSearchResultItem> linkColumn = new Column<FormDataSearchResultItem, FormDataSearchResultItem>(
				new AbstractCell<FormDataSearchResultItem>() {

					@Override
					public void render(Context context,
							FormDataSearchResultItem formData,
							SafeHtmlBuilder sb) {
						if (formData == null) {
							return;
						}
						sb.appendHtmlConstant("<a href=\"#"
								+ FormDataPresenter.NAME_TOKEN + ";"
								+ FormDataPresenter.FORM_DATA_ID + "="
								+ formData.getFormDataId() + "\">"
								+ formData.getFormTypeName() + "</a>");
					}
				}) {
			public FormDataSearchResultItem getValue(
					FormDataSearchResultItem object) {
				return object;
			}
		};

		formDataTable.addColumn(formKindColumn, getHeader("Тип налоговой формы"));
		formDataTable.addColumn(linkColumn, getHeader("Вид налоговой формы"));
		formDataTable.addColumn(departmentColumn, getHeader("Подразделение"));
		formDataTable.addColumn(reportPeriodColumn, getHeader("Отчетный период"));
		formDataTable.addColumn(stateColumn, getHeader("Статус формы"));

		pager.setDisplay(formDataTable);

	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setInSlot(Object slot, Widget content) {
		if (slot == FormDataListPresenter.TYPE_filterPresenter) {
			filterContentPanel.clear();
			if (content != null) {
				filterContentPanel.add(content);
			}
		} else {
			super.setInSlot(slot, content);
		}
	}

	@Override
	public void setFormDataList(int start, long totalCount, List<FormDataSearchResultItem> records) {
		formDataTable.setRowCount((int) totalCount);
		formDataTable.setRowData(start, records);
	}

	@Override
	public void assignDataProvider(int pageSize, AbstractDataProvider<FormDataSearchResultItem> data) {
		formDataTable.setPageSize(pageSize);
		data.addDataDisplay(formDataTable);
	}

	@Override
	public FormDataSearchOrdering getSearchOrdering(){
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
		if("Тип налоговой формы".equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.KIND;
		} else if ("Вид налоговой формы".equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.FORM_TYPE_NAME;
		} else if ("Подразделение".equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.DEPARTMENT_NAME;
		} else if ("Отчетный период".equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.REPORT_PERIOD_NAME;
		} else if ("Статус формы".equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.STATE;
		} else {
			this.sortByColumn = FormDataSearchOrdering.ID;
		}
	}

	private void setAscSorting(boolean ascSorting){
		this.isAscSorting = ascSorting;
	}
}
