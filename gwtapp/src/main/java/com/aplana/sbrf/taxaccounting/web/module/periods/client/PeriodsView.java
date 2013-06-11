package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
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

import java.util.List;


public class PeriodsView extends ViewWithUiHandlers<PeriodsUiHandlers>
		implements PeriodsPresenter.MyView{

	interface Binder extends UiBinder<Widget, PeriodsView> { }

	private static final String[] COLUMN_NAMES = {"Тип периода", "Период", "Состояние"};
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

	private SingleSelectionModel<TableRow> selectionModel = new SingleSelectionModel<TableRow>();

	private TableRow selectedRow;

	@Inject
	@UiConstructor
	public PeriodsView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));

		TextColumn<TableRow> periodKindColumn = new TextColumn<TableRow>() {
			@Override
			public String getValue(TableRow object) {
				return object.getPeriodKind();
			}
		};

		TextColumn<TableRow> periodNameColumn = new TextColumn<TableRow>() {
			@Override
			public String getValue(TableRow object) {
				return object.getPeriodName();
			}
		};

		TextColumn<TableRow> periodConditionColumn = new TextColumn<TableRow>() {
			@Override
			public String getValue(TableRow object) {
				return String.valueOf(object.isPeriodCondition());
			}
		};

		periodsTable.addColumn(periodKindColumn, COLUMN_NAMES[0]);
		periodsTable.addColumn(periodNameColumn, COLUMN_NAMES[1]);
		periodsTable.addColumn(periodConditionColumn, COLUMN_NAMES[2]);

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
		Window.alert("Форма еще не готова!");
	}


	@UiHandler("find")
	void onFindClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().applyFilter(
					Integer.valueOf(fromBox.getValue()),
					Integer.valueOf(toBox.getValue())
			);
		}
	}

	@UiHandler("closePeriod")
	void onClosePeriodClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().closePeriod(selectionModel.getSelectedObject().getId());
		}
	}
}
