package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;
import java.util.Map;

public class FormDataListView extends ViewWithUiHandlers<FormDataListUiHandlers> implements
		FormDataListPresenter.MyView {

	interface MyBinder extends UiBinder<Widget, FormDataListView> {
	}

	private final Widget widget;

	@UiField
    HorizontalPanel filterContentPanel;

	@UiField
	CellTable<FormData> formDataTable;

	private Map<Integer, String> departmentsMap;
	private Map<Integer, String> reportPeriodsMap;

	@Inject
	public FormDataListView(final MyBinder binder) {

		widget = binder.createAndBindUi(this);

		TextColumn<FormData> formKindColumn = new TextColumn<FormData>() {
			@Override
			public String getValue(FormData object) {
				return object.getKind().getName();
			}
		};

		TextColumn<FormData> formTypeColumn = new TextColumn<FormData>() {
			@Override
			public String getValue(FormData object) {
				return object.getFormType().getName();
			}
		};

		TextColumn<FormData> departmentColumn = new TextColumn<FormData>() {
			@Override
			public String getValue(FormData object) {
				return departmentsMap.get(object.getDepartmentId());
			}
		};

		TextColumn<FormData> reportPeriodColumn = new TextColumn<FormData>() {
			@Override
			public String getValue(FormData object) {
				return reportPeriodsMap.get(object.getReportPeriodId());
			}
		};

		formDataTable.addColumn(formKindColumn, "Тип");
		formDataTable.addColumn(formTypeColumn, "Вид");
		formDataTable.addColumn(departmentColumn, "Подразделение");
		formDataTable.addColumn(reportPeriodColumn, "Отчетный период");

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
	public void setFormDataList(List<FormData> records) {
		formDataTable.setRowCount(records.size());
		formDataTable.setRowData(0, records);
	}

	@Override
	public void setDepartmentMap(Map<Integer, String> departmentMap) {
		this.departmentsMap = departmentMap;
	}

	@Override
	public void setReportPeriodMap(Map<Integer, String> reportPeriodMap) {
		this.reportPeriodsMap = reportPeriodMap;
	}

	@Override
	public <C> Column<FormData, C> addTableColumn(Cell<C> cell,
			String headerText, final ValueGetter<C> getter,
			FieldUpdater<FormData, C> fieldUpdater) {
		Column<FormData, C> column = new Column<FormData, C>(cell) {
			public C getValue(FormData object) {
				return getter.getValue(object);
			}
		};
		column.setFieldUpdater(fieldUpdater);
		formDataTable.addColumn(column, headerText);
		return column;
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
}
