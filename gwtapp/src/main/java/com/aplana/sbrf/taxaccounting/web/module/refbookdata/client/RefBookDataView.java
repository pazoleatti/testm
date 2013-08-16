package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookAttributeSerializable;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerPopup;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefBookDataView extends ViewWithUiHandlers<RefBookDataUiHandlers> implements RefBookDataPresenter.MyView {

	interface Binder extends UiBinder<Widget, RefBookDataView> {
	}

	@UiField
	GenericDataGrid<RefBookDataRow> refbookDataTable;
	@UiField
	FlexiblePager pager;
	@UiField
	VerticalPanel content;
	@UiField
	Button save;

	Map<String, HasValue> inputFields = new HashMap<String, HasValue>();

	SingleSelectionModel<RefBookDataRow> selectionModel = new SingleSelectionModel<RefBookDataRow>();

	@Inject
	public RefBookDataView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));

		refbookDataTable.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				if (getUiHandlers() != null) {
					getUiHandlers().onSelectionChanged(selectionModel.getSelectedObject().getRefBookRowId());
					getUiHandlers().onSelectionChanged(selectionModel.getSelectedObject().getRefBookRowId());
				}
					}
		});
		pager.setDisplay(refbookDataTable);
		save.setEnabled(false);
	}

	@Override
	public void setTableColumns(final List<RefBookAttribute> headers) {

		for (final RefBookAttribute header : headers) {
			TextColumn<RefBookDataRow> column = new TextColumn<RefBookDataRow>() {
				@Override
				public String getValue(RefBookDataRow object) {
					return object.getValues().get(header.getAlias());
				}
			};
			refbookDataTable.addColumn(column, header.getName());
		}
	}

	@Override
	public void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data) {
		refbookDataTable.setPageSize(pageSize);
		data.addDataDisplay(refbookDataTable);
	}

	@Override
	public void setRange(Range range) {
		refbookDataTable.setVisibleRangeAndClearData(range, true);
	}

	@Override
	public void updateTable() {
		Range range = new Range(pager.getPageStart(), pager.getPageSize());
		refbookDataTable.setVisibleRangeAndClearData(range, true);
	}

	@Override
	public void addRowToEnd(RefBookDataRow newRow, boolean select) {
		List<RefBookDataRow> list = new ArrayList<RefBookDataRow>();
		list.add(newRow);
		refbookDataTable.setRowData(refbookDataTable.getRowCount(), list);
		if (select) {
			selectionModel.clear();

			selectionModel.setSelected(newRow, true);
		}
	}

	@Override
	public Map<String, Object> getChangedValues() {
		Map<String, Object> inputFields = getInputFieldsValues();
		inputFields.put(RefBook.RECORD_ID_ALIAS, selectionModel.getSelectedObject().getRefBookRowId());
		return inputFields;
	}

	@Override
	public void createInputFields(final List<RefBookAttribute> headers) {
		for (final RefBookAttribute header : headers) {
			// Сформируем поля ввода
			HorizontalPanel hp = new HorizontalPanel();
			hp.add(new Label(header.getName()));
			HasValue inputWidget;
			switch (header.getAttributeType()) {
				case STRING:
					inputWidget = new TextBox();
					break;
				case DATE:
					inputWidget = new CustomDateBox();
					break;
				case NUMBER:
					inputWidget = new TextBox();
					break;
				case REFERENCE:
					inputWidget = new RefBookPickerPopupWidget();
					((RefBookPickerPopup)inputWidget).setAttributeId(header.getRefBookAttributeId());
					break;
				default:
					inputWidget = new TextBox();
					break;
			}
			inputWidget.addValueChangeHandler(new ValueChangeHandler() {
				@Override
				public void onValueChange(ValueChangeEvent event) {
					save.setEnabled(true);
					if (getUiHandlers() != null) {
						getUiHandlers().onValueChanged();
					}
				}
			});
			inputFields.put(header.getAlias(), inputWidget);

			hp.add((Widget)inputWidget);
			content.add(hp);
		}
	}

	@Override
	public void fillInputFields(Map<String, RefBookAttributeSerializable> data) {
		for (Map.Entry<String, HasValue> w : inputFields.entrySet()) {
			switch (data.get(w.getKey()).getAttributeType()) {
				case STRING:
					((TextBox)w.getValue()).setValue(data.get(w.getKey()).getStringValue());
					break;
				case DATE:
					((CustomDateBox)w.getValue()).setValue(data.get(w.getKey()).getDateValue());
					break;
				case NUMBER:
					((TextBox)w.getValue()).setValue(data.get(w.getKey()).getNumberValue().toString());
					break;
				case REFERENCE:
					((RefBookPickerPopup)w.getValue()).setValue(data.get(w.getKey()).getReferenceValue());
					break;
			}
		}
	}

	@Override
	public void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows) {
		if (dataRows == null) {
			refbookDataTable.setRowCount(totalCount);
			refbookDataTable.setRowData(new ArrayList<RefBookDataRow>());
		}
		refbookDataTable.setRowCount(totalCount);
		refbookDataTable.setRowData(start, dataRows);
	}

//	@UiHandler("cancel")
//	void cancelButtonClicked(ClickEvent event) {
//		if (getUiHandlers() != null) {
//			getUiHandlers().onCancelClicked();
//		}
//	}

	@UiHandler("save")
	void saveButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onSaveClicked();
		}
	}

	@UiHandler("addRow")
	void addRowButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onAddRowClicked();
		}
	}

	@UiHandler("deleteRow")
	void deleteRowButtonClicked(ClickEvent event) {
		boolean confirm = Window.confirm("Удалить выбранную запись справочника?");
		if (confirm) {
			if (getUiHandlers() != null) {
				save.setEnabled(true);
				getUiHandlers().onDeleteRowClicked(selectionModel.getSelectedObject());
			}
		}
	}

	private Map<String, Object> getInputFieldsValues() {
		Map<String, Object> values = new HashMap<String, Object>();
		for (Map.Entry<String, HasValue> w : inputFields.entrySet()) {
			values.put(w.getKey(), w.getValue().getValue());
		}
		return values;
	}
}
