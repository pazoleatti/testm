package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerPopup;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
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

	Map<String, Widget> inputFields = new HashMap<String, Widget>();

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
				}
			}
		});
		pager.setDisplay(refbookDataTable);

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
	public void createInputFields(final List<RefBookAttribute> headers) {
		for (final RefBookAttribute header : headers) {
			// Сформируем поля ввода
			HorizontalPanel hp = new HorizontalPanel();
			hp.add(new Label(header.getName()));
			Widget inputWidget;
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
			inputFields.put(header.getAlias(), inputWidget);
			System.out.println("Alias: " + header.getAlias() + " " + header.getAttributeType());

			hp.add(inputWidget);
			content.add(hp);
		}
	}

	@Override
	public void fillInputFields(Map<String, com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookAttribute> data) {
		for (Map.Entry<String, Widget> w : inputFields.entrySet()) {
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
	public void setTableData(List<RefBookDataRow> dataRows) {
		if (dataRows == null) {
			refbookDataTable.setRowData(new ArrayList<RefBookDataRow>());
		}
		refbookDataTable.setRowData(dataRows);
	}

//	@UiHandler("cancel")
//	void cancelButtonClicked(ClickEvent event) {
//		if (getUiHandlers() != null) {
//			getUiHandlers().onCancelClicked();
//		}
//	}

	@UiHandler("addRow")
	void addRowButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onAddRowClicked();
		}
	}
}
