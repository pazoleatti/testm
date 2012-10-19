package com.aplana.sbrf.taxaccounting.gwtapp.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class FormDataListView extends ViewImpl implements FormDataListPresenter.MyView {

	private static final String html = "<h1>Список данных по налоговым формам</h1>\n"
			+ "<div id=\"formDataListContainer\"></div>";

	private final HTMLPanel panel = new HTMLPanel(html);

	private CellTable<FormData> formDataTable;

	@Inject
	public FormDataListView() {
		formDataTable = new CellTable<FormData>();

		TextColumn<FormData> idColumn = new TextColumn<FormData>() {
			@Override
			public String getValue(FormData object) {
				return String.valueOf(object.getId());
			}
		};
		TextColumn<FormData> formTypeColumn = new TextColumn<FormData>() {
			@Override
			public String getValue(FormData object) {
				return object.getForm().getType().getName();
			}
		};
		formDataTable.addColumn(idColumn, "id");
		formDataTable.addColumn(formTypeColumn, "Тип формы");
		
		panel.add(formDataTable, "formDataListContainer");
	}

	@Override
	public Widget asWidget() {
		return panel;
	}

	@Override
	public void setFormDataList(List<FormData> records) {
		formDataTable.setRowCount(records.size());
		formDataTable.setRowData(0, records);
	}

	@Override
	public <C> Column<FormData, C> addTableColumn(Cell<C> cell, String headerText, final ValueGetter<C> getter, FieldUpdater<FormData, C> fieldUpdater) {
		Column<FormData, C> column = new Column<FormData, C>(cell) {
			public C getValue(FormData object) {
				return getter.getValue(object);
			}
		};
		column.setFieldUpdater(fieldUpdater);
		formDataTable.addColumn(column, headerText);
		return column;
	}
}
