package com.aplana.sbrf.taxaccounting.gwtapp.client;

import java.util.Collections;
import java.util.List;

import com.aplana.sbrf.taxaccounting.gwtapp.cell.LogEntryCell;
import com.aplana.sbrf.taxaccounting.gwtapp.client.util.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class FormDataView extends ViewImpl implements FormDataPresenter.MyView {
	interface Binder extends UiBinder<Widget, FormDataView> {
	}

	private FormData formData;

	@UiField
	DataGrid<DataRow> formDataTable;
	@UiField
	Button cancelButton;
	@UiField
	Button saveButton;
	@UiField(provided=true) CellList<LogEntry> loggerList = new CellList<LogEntry>(new LogEntryCell());

	private final Widget widget;

	@Inject
	public FormDataView(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}	

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public Button getCancelButton() {
		return cancelButton;
	}

	@Override
	public Button getSaveButton() {
		return saveButton;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void reset() {
		while (formDataTable.getColumnCount() > 0) {
			formDataTable.removeColumn(0);
		}
		loggerList.setRowCount(0);
		loggerList.setRowData(Collections.EMPTY_LIST);
		formDataTable.setRowCount(0);
		formDataTable.setRowData(Collections.EMPTY_LIST);
	}

	@Override
	public void loadFormData(FormData formData) {
		
		this.formData = formData;
		
		DataRowColumnFactory factory = new DataRowColumnFactory();
		for (Column col: formData.getForm().getColumns()) {
			com.google.gwt.user.cellview.client.Column<DataRow, ?> tableCol = factory.createTableColumn(col, formDataTable);
			formDataTable.addColumn(tableCol, col.getName());
			formDataTable.setColumnWidth(tableCol, col.getWidth() + "em");
		}
		
		formDataTable.setRowCount(formData.getDataRows().size());
		formDataTable.setRowData(formData.getDataRows());
		formDataTable.redraw();
	}

	@Override
	public DataGrid<DataRow> getFormDataTable() {
		return formDataTable;
	}

	@Override
	public void setLogMessages(List<LogEntry> logEntries) {
		loggerList.setRowCount(logEntries.size());
		loggerList.setRowData(logEntries);
		loggerList.redraw();
	}

	@Override
	public FormData getFormData() {
		return formData;
	}
}
