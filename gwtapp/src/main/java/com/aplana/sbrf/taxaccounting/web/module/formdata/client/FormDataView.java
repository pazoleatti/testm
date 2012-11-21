package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import java.util.Collections;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.util.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.AccessFlags;
import com.aplana.sbrf.taxaccounting.web.widget.cell.LogEntryCell;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class FormDataView extends ViewImpl implements FormDataPresenter.MyView {
	interface Binder extends UiBinder<Widget, FormDataView> {
	}

	private FormData formData;
	
	private DataRowColumnFactory factory = new DataRowColumnFactory();
	
	//Form status
	private Boolean readOnly = true;
	
	private AccessFlags flags = null;

	@UiField
	DataGrid<DataRow> formDataTable;
	@UiField
	Button cancelButton;
	@UiField
	Button saveButton;
	@UiField
	Button addRowButton;
	@UiField
	Button removeRowButton;
	@UiField
	Button manualInputButton;
	@UiField
	Button originalVersionButton;
	@UiField
	Button recalculateButton;
	@UiField
	Button printButton;
	
	
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
	public void loadFormData(FormData formData, AccessFlags flags) {
		if (this.flags == null) {
			this.flags = flags;
			activateReadOnlyModeWithoutUpdate(flags);
		}
		this.formData = formData;
		
		factory.setReadOnly(readOnly);
		for (Column col: formData.getFormColumns()) {
			com.google.gwt.user.cellview.client.Column<DataRow, ?> tableCol = factory.createTableColumn(col, formDataTable);
			formDataTable.addColumn(tableCol, col.getName());
			formDataTable.setColumnWidth(tableCol, col.getWidth() + "em");
			final SingleSelectionModel<DataRow> selectionModel = new SingleSelectionModel<DataRow>();
			formDataTable.setSelectionModel(selectionModel);
		}
		
		reloadRows();
	}
	
	@Override
	public void reloadFormData(FormData formData, AccessFlags flags) {
//		Window.alert(String.valueOf(formData.getFormColumns().size()));
		reset();
		loadFormData(formData, flags);
	}
	

	@Override
	public void reloadRows() {
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

	@Override
	public Button getAddRowButton() {
		return addRowButton;
	}

	@Override
	public Button getRemoveRowButton() {
		return removeRowButton;
	}
	
	@Override
	public Button getManualInputButton() {
		return manualInputButton;
	}
	
	@Override
	public Button getPrintButton() {
		return printButton;
	}
	
	@Override
	public Button getOriginalVersionButton() {
		return originalVersionButton;
	}
	
//	@OverridegetOriginalVersionButton
//	public void setReadOnly(Boolean readOnly) {
//		this.readOnly = readOnly;
//	}
	
	@Override
	public Boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public void activateEditMode() {
		activateEditModeWithoutUpdate();
		reloadFormData(formData, flags);
	}
	
	private void activateEditModeWithoutUpdate() {
		this.readOnly = false;
		originalVersionButton.setVisible(true);
		saveButton.setVisible(true);
		recalculateButton.setVisible(true);
		manualInputButton.setVisible(false);
		removeRowButton.setVisible(false);
		printButton.setVisible(false);
	}

	@Override
	public void activateReadOnlyMode() {
		activateReadOnlyModeWithoutUpdate(flags);
		reloadFormData(formData, flags);
		
	}
	
	private void activateReadOnlyModeWithoutUpdate(AccessFlags flags) {
		this.readOnly = true;
		manualInputButton.setVisible(flags.getCanEdit());
		removeRowButton.setVisible(flags.getCanDelete());
		saveButton.setVisible(flags.getCanEdit());
		
		originalVersionButton.setVisible(false);
		recalculateButton.setVisible(false);
		addRowButton.setVisible(false);
		printButton.setVisible(true);
	}

	
	
}
