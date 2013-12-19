package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerModalWidget;
import com.aplana.sbrf.taxaccounting.web.widget.incrementbutton.IncrementButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;


public class PeriodsView extends ViewWithUiHandlers<PeriodsUiHandlers>
		implements PeriodsPresenter.MyView{

	interface Binder extends UiBinder<Widget, PeriodsView> { }

	private static final String[] COLUMN_NAMES = {
            "Период",
            "Состояние",
            "Срок сдачи отчетности",
            "Признак ввода остатков",
            "Период сдачи корректировки"
    };

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
    Widget setDeadlineButton;

	@UiField
    DepartmentPickerModalWidget departmentPicker;

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
        TextColumn<TableRow> periodBalanceColumn = new TextColumn<TableRow>() {
            @Override
            public String getValue(TableRow object) {
                if (object.isBalance() == null) {
                    return "";
                }
                if (object.isBalance()) {
                    return "Да";
                } else {
                    return "Нет";
                }
            }
        };

        TextColumn<TableRow> deadlineColumn = new TextColumn<TableRow>() {
            @Override
            public String getValue(TableRow object) {
                return (object.getDeadline() != null) ? DateTimeFormat.getFormat("dd.MM.yyyy").format(object.getDeadline()) : "";
            }
        };

        TextColumn<TableRow> correctDateColumn = new TextColumn<TableRow>() {
            @Override
            public String getValue(TableRow object) {
                return (object.getCorrectPeriod() != null) ? DateTimeFormat.getFormat("dd.MM.yyyy").format(object.getCorrectPeriod()) : "";
            }
        };

		periodsTable.addColumn(periodNameColumn, COLUMN_NAMES[0]);
		periodsTable.addColumn(periodConditionColumn, COLUMN_NAMES[1]);
        periodsTable.addColumn(deadlineColumn, COLUMN_NAMES[2]);
        periodsTable.addColumn(periodBalanceColumn, COLUMN_NAMES[3]);
        periodsTable.addColumn(correctDateColumn, COLUMN_NAMES[4]);

		periodsTable.setSelectionModel(selectionModel);

		periodsTable.setRowStyles(new RowStyles<TableRow>() {
			@Override
			public String getStyleNames(TableRow row, int rowIndex) {
				if (row.isSubHeader()) {
					return "gwt-CellTable-SubHeader";
				} else {
					return "";
				}
			}
		});

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
	public void setFilterData(List<Department> departments, List<DepartmentPair> selectedDepartments, int yearFrom, int yearTo) {
		departmentPicker.setAvailableValues(departments);
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
	public DepartmentPair getDepartmentId() {
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

    @UiHandler("setDeadlineButton")
    void onSetDeadlineClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().setDeadline();
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
