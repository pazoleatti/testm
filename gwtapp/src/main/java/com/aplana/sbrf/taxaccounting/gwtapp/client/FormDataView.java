package com.aplana.sbrf.taxaccounting.gwtapp.client;

import java.math.BigDecimal;
import java.util.Date;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
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
	
	public void loadFormData(FormData formData) {
		Form form = formData.getForm();
		final DateTimeFormat dateFormat = DateTimeFormat.getFormat("dd.MM.YYYY");
		System.out.println("Adding columns to formDataTable");
		for (Column col: form.getColumns()) {
			final String alias = col.getAlias();
			com.google.gwt.user.cellview.client.Column<DataRow, ?> tableCol = null;
			System.out.println("adding column: " + col.getAlias());
			if (col instanceof StringColumn) {
				tableCol = new TextColumn<DataRow>() {
					@Override
					public String getValue(DataRow object) {
						return (String)object.get(alias);
					}
				};
			} else if (col instanceof NumericColumn) {
				tableCol = new TextColumn<DataRow>() {
					@Override
					public String getValue(DataRow object) {
						BigDecimal value = (BigDecimal)object.get(alias);
						return value == null ? null : String.valueOf(value.doubleValue());
					}
				};
			} else if (col instanceof DateColumn) {
				tableCol = new TextColumn<DataRow>() {
					@Override
					public String getValue(DataRow object) {
						Date value = (Date)object.get(alias);
						return value == null ? null : dateFormat.format(value);
					}
				};
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
