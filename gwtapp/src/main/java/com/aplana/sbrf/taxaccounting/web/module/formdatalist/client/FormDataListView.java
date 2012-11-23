package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

public class FormDataListView extends ViewWithUiHandlers<FormDataListUiHandlers> implements
		FormDataListPresenter.MyView {

	interface MyBinder extends UiBinder<Widget, FormDataListView> {
	}

	private final Widget widget;

	@UiField
    HorizontalPanel filterContentPanel;

	@UiField
	CellTable<FormData> formDataTable;

	@Inject
	public FormDataListView(final MyBinder binder) {

		widget = binder.createAndBindUi(this);

		TextColumn<FormData> idColumn = new TextColumn<FormData>() {
			@Override
			public String getValue(FormData object) {
				return String.valueOf(object.getId());
			}
		};


		TextColumn<FormData> formTypeColumn = new TextColumn<FormData>() {
			@Override
			public String getValue(FormData object) {
				return object.getFormType().getName();
			}
		};
		formDataTable.addColumn(idColumn, "id");
		formDataTable.addColumn(formTypeColumn, "Тип формы");
		//TODO: добавить department и period для отображения в таблице

	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setInSlot(Object slot, Widget content) {
		if (slot == FormDataListPresenter.TYPE_filterPresenter) {
			filterContentPanel.clear();
			if (content != null) {
				filterContentPanel.add(content);
			}
		} else {
			super.setInSlot(slot, content);
		}
	}

	@Override
	public void setFormDataList(List<FormData> records) {
		formDataTable.setRowCount(records.size());
		formDataTable.setRowData(0, records);
	}

	@Override
	public <C> Column<FormData, C> addTableColumn(Cell<C> cell,
			String headerText, final ValueGetter<C> getter,
			FieldUpdater<FormData, C> fieldUpdater) {
		Column<FormData, C> column = new Column<FormData, C>(cell) {
			public C getValue(FormData object) {
				return getter.getValue(object);
			}
		};
		column.setFieldUpdater(fieldUpdater);
		formDataTable.addColumn(column, headerText);
		return column;
	}


	@UiHandler("apply")
	void onApplyButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onApplyFilter();
		}
	}

    @UiHandler("create")
    void onCreateButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onCreateClicked();
        }
    }
}
