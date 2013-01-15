package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplateRowPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomHeaderBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.EditTextColumn;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;


public class FormTemplateRowView extends ViewWithUiHandlers<FormTemplateRowUiHandlers> implements FormTemplateRowPresenter.MyView{
	public interface Binder extends UiBinder<Widget, FormTemplateRowView> { }

	private SingleSelectionModel<DataRow> selectionModel;
	private DataRowColumnFactory factory = new DataRowColumnFactory();
	private final Widget widget;
	private List<DataRow> rows;

	@UiField
	DataGrid<DataRow> formDataTable;

	@UiField
	Button addRowButton;

	@UiField
	Button removeRowButton;

	@UiField
	Button upRowButton;

	@UiField
	Button downRowButton;

	// Элементы управления редактирования скриптов
	@Inject
	public FormTemplateRowView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);

		CustomHeaderBuilder builder = new CustomHeaderBuilder(formDataTable, false);
		formDataTable.setHeaderBuilder(builder);

		selectionModel = new SingleSelectionModel<DataRow>();
		formDataTable.setSelectionModel(selectionModel);

		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
				checkEnableUpDownButton();
			}
		});
		factory.setEditOnly(true);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setColumnsData(List<Column> columnsData) {
		// Clean columns
		while (formDataTable.getColumnCount() > 0) {
			formDataTable.removeColumn(0);
		}

		//Create alias column
		StringColumn aliasColumn = new StringColumn();
		EditTextColumn editTextAliasColumn = new EditTextColumn(aliasColumn) {
			@Override
			public String getValue(DataRow aliasRow) {
				return aliasRow.getAlias();
			}
		};
		editTextAliasColumn.setFieldUpdater(new FieldUpdater<DataRow, String>() {
			@Override
			public void update(int index, DataRow dataRow, String value) {
				dataRow.setAlias(value);
			}
		});
		formDataTable.addColumn(editTextAliasColumn, "Код строки");
		formDataTable.setColumnWidth(editTextAliasColumn, "4em");

		//create form columns
		for (Column col : columnsData) {
			com.google.gwt.user.cellview.client.Column<DataRow, ?> tableCol = factory
					.createTableColumn(col, formDataTable);
			formDataTable.addColumn(tableCol, col.getName());
			formDataTable.setColumnWidth(tableCol, col.getWidth() + "em");
		}

	}

	@Override
	public void setRowsData(List<DataRow> rowsData) {
		if (rowsData != null && rowsData.size() != 0) {
			rows = rowsData;
			formDataTable.setRowData(rowsData);
		} else {
			formDataTable.setRowCount(0);
			formDataTable.setRowData(new ArrayList<DataRow>(0));
		}

		upRowButton.setEnabled(false);
		downRowButton.setEnabled(false);
		formDataTable.redraw();
	}

	@UiHandler("addRowButton")
	public void onAddButton(ClickEvent event){
		if(getUiHandlers()!=null){
			getUiHandlers().onAddButton();
		}
	}

	@UiHandler("removeRowButton")
	public void onRemoveButton(ClickEvent event){
		if(getUiHandlers()!=null){
			getUiHandlers().onRemoveButton(selectionModel.getSelectedObject());
		}
	}

	@UiHandler("upRowButton")
	public void onUpRowButton(ClickEvent event){
		if(getUiHandlers()!=null){
			DataRow row = selectionModel.getSelectedObject();
			int index = rows.indexOf(row);
			rows.set(index, rows.get(index - 1));
			rows.set(index - 1, row);
			setRowsData(rows);
		}
		checkEnableUpDownButton();
	}

	@UiHandler("downRowButton")
	public void onDownRowButton(ClickEvent event){
		if(getUiHandlers()!=null){
			DataRow row = selectionModel.getSelectedObject();
			int index = rows.indexOf(row);
			rows.set(index, rows.get(index + 1));
			rows.set(index + 1, row);
			setRowsData(rows);
		}
		checkEnableUpDownButton();
	}

	void checkEnableUpDownButton() {
		upRowButton.setEnabled(false);
		downRowButton.setEnabled(false);

		DataRow row = selectionModel.getSelectedObject();
		int index = rows.indexOf(row);

		if (index != rows.size() - 1) {
			downRowButton.setEnabled(true);
		}
		if (index != 0) {
			upRowButton.setEnabled(true);
		}
	}
}