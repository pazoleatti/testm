package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;


import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter.FormTemplateRowPresenter;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.ui.StyleCellPopup;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomHeaderBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomTableBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.EditTextColumn;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;


public class FormTemplateRowView extends ViewWithUiHandlers<FormTemplateRowUiHandlers> implements FormTemplateRowPresenter.MyView{
	public interface Binder extends UiBinder<Widget, FormTemplateRowView> { }

	private final StyleCellPopup styleCellPopup;
	private final SingleSelectionModel<DataRow> selectionModel;
	private final DataRowColumnFactory factory = new DataRowColumnFactory();
	private final Widget widget;
	private static final String GWT_CELL_ATTR = "__gwt_cell";
	private List<DataRow> rows;
	private List<Column> columns;
	private List<FormStyle> styles;
	private int currentRowIndex;
	private int currentColumnIndex;

	@UiField
	DataGrid<DataRow> formDataTable;

	@UiField
	Button addRowButton;

	@UiField
	Button removeRowButton;

	@UiField
	Button upRowButton;

	@UiField
	Button downRowButton;

	@Inject
	public FormTemplateRowView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);

		factory.setEditOnly(true);
		styleCellPopup = new StyleCellPopup(this);

		selectionModel = new SingleSelectionModel<DataRow>();
		formDataTable.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
				checkEnableUpDownButton();
			}
		});

		formDataTable.addDomHandler(new ContextMenuHandler() {
			@Override
			public void onContextMenu(ContextMenuEvent event) {
				EventTarget eventTarget = event.getNativeEvent().getEventTarget();
                if (!Element.is(eventTarget)) {
					return;
				}
                com.google.gwt.user.client.Element target = eventTarget.cast();
                if (target == null || target.getAttribute(GWT_CELL_ATTR).isEmpty()) {
					return;
				}

				Element td = DOM.getParent(target);
				int tdAttr = Integer.valueOf(td.getAttribute(CustomTableBuilder.TD_ATTRIBUTE));

				if (tdAttr > 0) {
			 		currentColumnIndex = tdAttr - 1;

			 		event.preventDefault();
			 		event.stopPropagation();

			 		Element tr = DOM.getParent(td);
			 		Element body = DOM.getParent(tr);
			 		currentRowIndex = DOM.getChildIndex(body, tr);

			 		int popupLeft = target.getAbsoluteLeft() + (target.getAbsoluteRight() - target.getAbsoluteLeft())/2 - 100;
			 		DataRow currentRow = rows.get(currentRowIndex);
			 		Cell cell = currentRow.getCell(columns.get(currentColumnIndex).getAlias());
			 		styleCellPopup.setValue(cell);
			 		styleCellPopup.show(popupLeft, target.getAbsoluteTop());
				}
			}
		}, ContextMenuEvent.getType());
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setColumnsData(List<Column> columnsData) {
		columns = columnsData;

		// Clean columns
		while (formDataTable.getColumnCount() > 0) {
			formDataTable.removeColumn(0);
		}

		//Create alias column
		StringColumn aliasColumn = new StringColumn();
		EditTextColumn editTextAliasColumn = new EditTextColumn(aliasColumn) {
			@Override
			public String getValue(DataRow aliasRow) {
				return aliasRow.getAlias();
			}
		};
		editTextAliasColumn.setFieldUpdater(new FieldUpdater<DataRow, String>() {
			@Override
			public void update(int index, DataRow dataRow, String value) {
				dataRow.setAlias(value);
			}
		});
		formDataTable.addColumn(editTextAliasColumn, "Код строки");
		formDataTable.setColumnWidth(editTextAliasColumn, "10em");

		//create form columns
		for (Column col : columns) {
			com.google.gwt.user.cellview.client.Column<DataRow, ?> tableCol = factory
					.createTableColumn(col, formDataTable);
			formDataTable.addColumn(tableCol, col.getName());
			formDataTable.setColumnWidth(tableCol, col.getWidth() + "em");
		}
	}

	public void refresh() {
		setRowsData(rows);
	}

	public boolean validateCellsUnionRange(int rowSpan, int colSpan) {
		if (columns.size() < currentColumnIndex + colSpan || rows.size() < currentRowIndex + rowSpan) {
			return false;
		}

		for (int i = currentRowIndex; i < currentRowIndex + rowSpan; i++) {
			for (int j = currentColumnIndex; j < currentColumnIndex + colSpan; j++) {
				if (i != currentRowIndex || j != currentColumnIndex) {
					DataRow currentRow = rows.get(i);
					Cell cell = currentRow.getCell(columns.get(j).getAlias());
					if (cell.getRowSpan() > 1 || cell.getColSpan() > 1) {
						return false;
					}
				}
 			}
		}
		return true;
	}

	@Override
	public void setRowsData(List<DataRow> rowsData) {
		rows = rowsData;
		if (rowsData.size() != 0) {
			formDataTable.setRowData(rowsData);
			CustomTableBuilder<DataRow> builder = new CustomTableBuilder<DataRow>(formDataTable, styles);
			formDataTable.setTableBuilder(builder);
		} else {
			formDataTable.setRowCount(0);
			formDataTable.setRowData(new ArrayList<DataRow>(0));
		}

		upRowButton.setEnabled(false);
		downRowButton.setEnabled(false);
	}

	@Override
	public void setStylesData(List<FormStyle> styles) {
		this.styles = styles;
		styleCellPopup.setStyleAlias(styles);
	}

	@Override
	public void addCustomHeader(boolean addNumberedHeader) {
		CustomHeaderBuilder builder = new CustomHeaderBuilder(formDataTable, false, addNumberedHeader, true);
		formDataTable.setHeaderBuilder(builder);
	}

	@UiHandler("addRowButton")
	public void onAddButton(ClickEvent event){
		rows.add(new DataRow("Новый код", columns));
		setRowsData(rows);
	}

	@UiHandler("removeRowButton")
	public void onRemoveButton(ClickEvent event){
		if(getUiHandlers()!=null){
			getUiHandlers().onRemoveButton(selectionModel.getSelectedObject());
		}
	}

	@UiHandler("upRowButton")
	public void onUpRowButton(ClickEvent event){
		if(getUiHandlers()!=null){
			DataRow row = selectionModel.getSelectedObject();
			int index = rows.indexOf(row);
			rows.set(index, rows.get(index - 1));
			rows.set(index - 1, row);
			setRowsData(rows);
		}
		checkEnableUpDownButton();
	}

	@UiHandler("downRowButton")
	public void onDownRowButton(ClickEvent event){
		if(getUiHandlers()!=null){
			DataRow row = selectionModel.getSelectedObject();
			int index = rows.indexOf(row);
			rows.set(index, rows.get(index + 1));
			rows.set(index + 1, row);
			setRowsData(rows);
		}
		checkEnableUpDownButton();
	}

	private void checkEnableUpDownButton() {
		upRowButton.setEnabled(false);
		downRowButton.setEnabled(false);

		DataRow row = selectionModel.getSelectedObject();
		int index = rows.indexOf(row);

		if (index != rows.size() - 1) {
			downRowButton.setEnabled(true);
		}
		if (index != 0) {
			upRowButton.setEnabled(true);
		}
	}
}