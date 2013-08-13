package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
import java.util.List;

public class RefBookDataView extends ViewWithUiHandlers<RefBookDataUiHandlers> implements RefBookDataPresenter.MyView {

	interface Binder extends UiBinder<Widget, RefBookDataView> {
	}

	@UiField
	GenericDataGrid<RefBookDataRow> refbookDataTable;
	@UiField
	FlexiblePager pager;
	@UiField
	VerticalPanel content;

	SingleSelectionModel<RefBookDataRow> selectionModel = new SingleSelectionModel<RefBookDataRow>();

	@Inject
	public RefBookDataView(final Binder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));

	}

	@Override
	public void setTableColumns(final List<RefBookAttribute> headers) {

		for (final RefBookAttribute header : headers) {
			TextColumn<RefBookDataRow> column = new TextColumn<RefBookDataRow>() {
				@Override
				public String getValue(RefBookDataRow object) {
					return object.getValues().get(header.getAlias()).getStringValue();
				}
			};
			refbookDataTable.addColumn(column, header.getName());
		}
		refbookDataTable.setSelectionModel(selectionModel);

		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				content.clear();
				for (final RefBookAttribute header : headers) {
					HorizontalPanel hp = new HorizontalPanel();
					hp.add(new Label(header.getName()));
					TextBox tb = new TextBox();
					tb.setValue(selectionModel.getSelectedObject().getValues().get(header.getAlias()).getStringValue());
					tb.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							selectionModel.getSelectedObject().getValues().get(header.getAlias()).setStringValue(event.getValue());
							refbookDataTable.redraw();
						}
					});
					hp.add(tb);
					content.add(hp);
				}
			}
		});
		refbookDataTable.setRowCount(0);
//		pager.setDisplay(refbookDataTable);
		System.out.println("Set done!");

	}

	@Override
	public void setTableData(List<RefBookDataRow> dataRows) {
		if (dataRows == null) {
			refbookDataTable.setRowData(new ArrayList<RefBookDataRow>());
		}
		refbookDataTable.setRowData(dataRows);
	}

	@UiHandler("cancel")
	void cancelButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onCancelClicked();
		}
	}
}
