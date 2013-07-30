package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.incrementbutton.IncrementButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class PeriodsView extends ViewWithUiHandlers<PeriodsUiHandlers>
		implements PeriodsPresenter.MyView{

	interface Binder extends UiBinder<Widget, PeriodsView> { }

	private static final String[] COLUMN_NAMES = {"Период", "Состояние"};
	@UiField
	Anchor returnAnchor;

	@UiField
	Label title;

	@UiField
	IncrementButton fromBox;

	@UiField
	IncrementButton toBox;

	@UiField
	GenericCellTable periodsTable;

	@UiField
	DepartmentPicker departmentPicker;

	private SingleSelectionModel<TableRow> selectionModel = new SingleSelectionModel<TableRow>();

	private TableRow selectedRow;

	@Inject
	@UiConstructor
	public PeriodsView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));

		TextColumn<TableRow> periodNameColumn = new TextColumn<TableRow>() {
			@Override
			public String getValue(TableRow object) {
				return object.getPeriodName();
			}
		};

		TextColumn<TableRow> periodConditionColumn = new TextColumn<TableRow>() {
			@Override
			public String getValue(TableRow object) {
				if (object.isOpen() == null) {
					return "";
				}
				if (object.isOpen()) { //TODO вынести в константы
					return "Открыт";
				} else {
					return "Закрыт";
				}
			}
		};



		periodsTable.addColumn(periodNameColumn, COLUMN_NAMES[0]);
		periodsTable.addColumn(periodConditionColumn, COLUMN_NAMES[1]);

		periodsTable.setSelectionModel(selectionModel);


	}

	@Override
	public void setTitle(String title) {
		this.title.setText(title);
		this.title.setTitle(title);
	}

	@Override
	public void setTableData(List<TableRow> data) {
		periodsTable.setRowData(data);
	}

	@Override
	public void setDepartmentPickerEnable(boolean enable) {
		departmentPicker.setEnabled(enable);
	}

	@Override
	public void setFilterData(List<Department> departments, Map<String, Integer> selectedDepartments, int yearFrom, int yearTo) {
		Set<Integer> available = new HashSet<Integer>();
		for (Department dep : departments) {
			available.add(dep.getId());
		}
		departmentPicker.setTreeValues(departments, available);
		departmentPicker.setSelectedItems(selectedDepartments);
		fromBox.setValue(yearFrom);
		toBox.setValue(yearTo);

	}

	@UiHandler("find")
	void onFindClicked(ClickEvent event) {
		if ( (fromBox.getValue() > toBox.getValue())) {
			Window.alert("Интервал поиска периодов указан неверно!");
			return;
		}
		if (getUiHandlers() != null) {
			getUiHandlers().applyFilter(
					Integer.valueOf(fromBox.getValue()),
					Integer.valueOf(toBox.getValue()),
					departmentPicker.getSelectedItems().values().isEmpty() ?
							0 : departmentPicker.getSelectedItems().values().iterator().next()
			);
		}
	}

	@UiHandler("closePeriod")
	void onClosePeriodClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			TableRow selectedRow = selectionModel.getSelectedObject();
			if (!selectedRow.isSubHeader()) {
				getUiHandlers().closePeriod(selectedRow);
			}
		}
	}

	@UiHandler("openPeriod")
	void onOpenPeriodClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().openPeriod();
		}
	}
}
