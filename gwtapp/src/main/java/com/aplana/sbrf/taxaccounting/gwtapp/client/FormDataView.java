package com.aplana.sbrf.taxaccounting.gwtapp.client;

import com.aplana.sbrf.taxaccounting.gwtapp.client.util.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class FormDataView extends ViewImpl implements FormDataPresenter.MyView {
	interface Binder extends UiBinder<Widget, FormDataView> {
	}

	@UiField
	DataGrid<DataRow> formDataTable;
	@UiField
	Button cancelButton;
    @UiField
    Button saveButton;

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
    
    public void reset() {
		while (formDataTable.getColumnCount() > 0) {
			formDataTable.removeColumn(0);
		}
    }

    public void loadFormData(FormData formData) {
		Form form = formData.getForm();
		DataRowColumnFactory factory = new DataRowColumnFactory();
		
		for (Column col: form.getColumns()) {
			com.google.gwt.user.cellview.client.Column<DataRow, ?> tableCol = factory.createTableColumn(col, formDataTable);
			formDataTable.addColumn(tableCol, col.getName());
			formDataTable.setColumnWidth(tableCol, col.getWidth() + "em;");
		}
		formDataTable.setRowCount(formData.getDataRows().size());
		formDataTable.setRowData(0, formData.getDataRows());
	}

	@Override
	public DataGrid<DataRow> getFormDataTable() {
		return formDataTable;
	}
}
