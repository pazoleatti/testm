package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;
import java.util.Map;

public class FormDataListView extends
		ViewWithUiHandlers<FormDataListUiHandlers> implements
		FormDataListPresenter.MyView {

	interface MyBinder extends UiBinder<Widget, FormDataListView> {
	}

	private final Widget widget;

	@UiField
	Panel filterContentPanel;

	@UiField
	CellTable<FormDataSearchResultItem> formDataTable;

	@UiField
	VerticalPanel verticalPanelWithTable;

	private Map<Integer, String> departmentsMap;
	private Map<Integer, String> reportPeriodsMap;

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
				return departmentsMap.get(object.getDepartmentId());
			}
		};

		TextColumn<FormDataSearchResultItem> reportPeriodColumn = new TextColumn<FormDataSearchResultItem>() {
			@Override
			public String getValue(FormDataSearchResultItem object) {
				return reportPeriodsMap.get(object.getReportPeriodId());
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

		formDataTable.addColumn(formKindColumn, "Тип налоговой формы");
		formDataTable.addColumn(linkColumn, "Вид налоговой формы");
		formDataTable.addColumn(departmentColumn, "Подразделение");
		formDataTable.addColumn(reportPeriodColumn, "Отчетный период");
		formDataTable.addColumn(stateColumn, "Статус формы");

		SimplePager pager = createDefaultPager();
		pager.setDisplay(formDataTable);
		verticalPanelWithTable.add(pager);

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
	public void setDepartmentMap(Map<Integer, String> departmentMap) {
		this.departmentsMap = departmentMap;
	}

	@Override
	public void setReportPeriodMap(Map<Integer, String> reportPeriodMap) {
		this.reportPeriodsMap = reportPeriodMap;
	}

	@UiHandler("apply")
	void onApplyButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onApplyFilter();
		}
	}

	@UiHandler("create")
	void onCreateButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onCreateClicked();
		}
	}

	private static SimplePager createDefaultPager(){
		final boolean showFastForwardButton = false;
		final int fastForwardRows = 0;
		final boolean showLastPageButton = true;
		SimplePager pager =  new SimplePager(SimplePager.TextLocation.CENTER, showFastForwardButton, fastForwardRows,
				showLastPageButton);
		pager.setRangeLimited(true);
		pager.getElement().getStyle().setProperty("marginLeft", "auto");
		pager.getElement().getStyle().setProperty("marginRight", "auto");
		return pager;
	}
}
