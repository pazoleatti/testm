package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.*;
import com.aplana.sbrf.taxaccounting.web.widget.cell.*;
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

	public static final String FORM_DATA_KIND_TITLE = "Тип налоговой формы";
	public static final String FORM_DATA_TYPE_TITLE = "Вид налоговой формы";
	public static final String DEPARTMENT_TITLE = "Подразделение";
	public static final String REPORT_PERIOD_TITLE = "Отчетный период";
	public static final String FORM_DATA_STATE_TITLE = "Состояние";

	interface MyBinder extends UiBinder<Widget, FormDataListView> {
	}

	private FormDataSearchOrdering sortByColumn;

	private boolean isAscSorting;

	@UiField
	Panel filterContentPanel;

	@UiField
	CellTable<FormDataSearchResultItem> formDataTable;
	
	@UiField
	AbstractPager pager;

	@UiField
	Label titleDesc;

	@Inject
	public FormDataListView(final MyBinder binder) {
		initWidget(binder.createAndBindUi(this));

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

		formDataTable.addColumn(formKindColumn, getHeader(FORM_DATA_KIND_TITLE));
		formDataTable.addColumn(linkColumn, getHeader(FORM_DATA_TYPE_TITLE));
		formDataTable.addColumn(departmentColumn, getHeader(DEPARTMENT_TITLE));
		formDataTable.addColumn(reportPeriodColumn, getHeader(REPORT_PERIOD_TITLE));
		formDataTable.addColumn(stateColumn, getHeader(FORM_DATA_STATE_TITLE));

		pager.setDisplay(formDataTable);

	}

	@Override
	public void setInSlot(Object slot, IsWidget content) {
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
		if(FORM_DATA_KIND_TITLE.equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.KIND;
		} else if (FORM_DATA_TYPE_TITLE.equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.FORM_TYPE_NAME;
		} else if (DEPARTMENT_TITLE.equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.DEPARTMENT_NAME;
		} else if (REPORT_PERIOD_TITLE.equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.REPORT_PERIOD_NAME;
		} else if (FORM_DATA_STATE_TITLE.equals(sortByColumn)){
			this.sortByColumn = FormDataSearchOrdering.STATE;
		} else {
			this.sortByColumn = FormDataSearchOrdering.ID;
		}
	}

	private void setAscSorting(boolean ascSorting){
		this.isAscSorting = ascSorting;
	}
}
