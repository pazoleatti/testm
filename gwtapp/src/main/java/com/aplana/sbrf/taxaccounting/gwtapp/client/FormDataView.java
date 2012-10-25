package com.aplana.sbrf.taxaccounting.gwtapp.client;

import com.aplana.sbrf.taxaccounting.gwtapp.client.util.EditDateColumn;
import com.aplana.sbrf.taxaccounting.gwtapp.client.util.EditNumericColumn;
import com.aplana.sbrf.taxaccounting.gwtapp.client.util.EditTextColumn;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
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

    public void loadFormData(FormData formData) {
		Form form = formData.getForm();
		System.out.println("Adding columns to formDataTable");
		for (Column col: form.getColumns()) {
			final String alias = col.getAlias();
			com.google.gwt.user.cellview.client.Column<DataRow, ?> tableCol = null;
			System.out.println("adding column: " + col.getAlias());
			if (col instanceof StringColumn) {
				tableCol = new EditTextColumn(alias);
			} else if (col instanceof NumericColumn) {
				tableCol = new EditNumericColumn(alias);
			} else if (col instanceof DateColumn) {
				tableCol = new EditDateColumn(alias);
			}
			formDataTable.addColumn(tableCol, col.getName());
		}
		formDataTable.setRowCount(formData.getDataRows().size());
		formDataTable.setRowData(0, formData.getDataRows());
	}

	@Override
	public DataGrid<DataRow> getFormDataTable() {
		return formDataTable;
	}
}
