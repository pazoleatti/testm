package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.incrementbutton.IncrementButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;


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
	GenericCellTable<TableRow> periodsTable;
	
	@UiField
	Widget openPeriod;
	
	@UiField
	Widget closePeriod;

	@UiField
	DepartmentPickerPopupWidget departmentPicker;

	private SingleSelectionModel<TableRow> selectionModel = new SingleSelectionModel<TableRow>();

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
	public void setFilterData(List<Department> departments, Set<Integer> avalDepartments, List<Integer> selectedDepartments, int yearFrom, int yearTo) {
		departmentPicker.setAvalibleValues(departments, avalDepartments);
		departmentPicker.setValue(selectedDepartments);
		fromBox.setValue(yearFrom);
		toBox.setValue(yearTo);
	}

	@Override
	public void setYear(int year) {
		fromBox.setValue(year);
		toBox.setValue(year);
	}

	@Override
	public Integer getFromYear() {
		return fromBox.getValue();
	}

	@Override
	public Integer getToYear() {
		return toBox.getValue();
	}

	@Override
	public long getDepartmentId() {
		return departmentPicker.getValue().get(0);
	}

	@Override
	public TableRow getSelectedRow() {
		return selectionModel.getSelectedObject();
	}

	@UiHandler("find")
	void onFindClicked(ClickEvent event) {
		getUiHandlers().onFindButton();
	}



	@UiHandler("closePeriod")
	void onClosePeriodClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			TableRow selectedRow = selectionModel.getSelectedObject();
			if (!selectedRow.isSubHeader()) {
				getUiHandlers().closePeriod();
			}
		}
	}

	@UiHandler("openPeriod")
	void onOpenPeriodClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().openPeriod();
		}
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		openPeriod.setVisible(!readOnly);
		closePeriod.setVisible(!readOnly);
	}

	@Override
	public boolean isFromYearEmpty() {
		return fromBox.isEmpty();
	}

	@Override
	public boolean isToYearEmpty() {
		return toBox.isEmpty();
	}
}
