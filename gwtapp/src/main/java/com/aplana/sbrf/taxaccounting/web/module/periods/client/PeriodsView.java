package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import java.util.List;

import com.aplana.gwt.client.Spinner;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
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
	Spinner fromBox;

	@UiField
	Spinner toBox;

	@UiField
	GenericCellTable<TableRow> periodsTable;
	
	@UiField
    LinkButton openPeriod;
	
	@UiField
    LinkButton closePeriod;

    @UiField
    Button setDeadlineButton;

	@UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    Label departmentPickerRO;

	private SingleSelectionModel<TableRow> selectionModel = new SingleSelectionModel<TableRow>();
    @UiField
    Label nalogTypeLabel;

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
        periodConditionColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
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
        periodBalanceColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        TextColumn<TableRow> deadlineColumn = new TextColumn<TableRow>() {
            @Override
            public String getValue(TableRow object) {
                return (object.getDeadline() != null) ? DateTimeFormat.getFormat("dd.MM.yyyy").format(object.getDeadline()) : "";
            }
        };
        deadlineColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        TextColumn<TableRow> correctDateColumn = new TextColumn<TableRow>() {
            @Override
            public String getValue(TableRow object) {
                return (object.getCorrectPeriod() != null) ? DateTimeFormat.getFormat("dd.MM.yyyy").format(object.getCorrectPeriod()) : "";
            }
        };
        correctDateColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
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
    public void setTaxTitle(String title) {
        nalogTypeLabel.setText(title);
    }

	@Override
	public void setTableData(List<TableRow> data) {
		periodsTable.setRowData(data);
	}

	@Override
	public void setFilterData(List<Department> departments, List<DepartmentPair> selectedDepartments, int yearFrom, int yearTo) {
        departmentPicker.setAvalibleValues(departments, null);
        departmentPicker.setValueByDepartmentPair(selectedDepartments, false);
        departmentPickerRO.setText(selectedDepartments.get(0).getDepartmentName());
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
        List<DepartmentPair> departmentPairs = departmentPicker.getDepartmentPairValues();
		return departmentPairs != null && !departmentPairs.isEmpty() ? departmentPairs.get(0) : null;
	}

	@Override
	public TableRow getSelectedRow() {
		return selectionModel.getSelectedObject();
	}

	@UiHandler("find")
	void onFindClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onFindButton();
        }
    }

	@UiHandler("closePeriod")
	void onClosePeriodClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
            getUiHandlers().closePeriod();
		}
	}

	@UiHandler("removePeriod")
	void onRemovePeriodClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().removePeriod();
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

	public boolean isFromYearEmpty() {
		return fromBox.getValue() == null;
	}

	@Override
	public boolean isToYearEmpty() {
		return toBox.getValue() == null;
	}

    @Override
    public void setCanChangeDepartment(boolean canChange) {
        departmentPicker.setVisible(canChange);
        departmentPickerRO.setVisible(!canChange);
    }
}
