package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateHeaderPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomTableBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumn;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.EditTextColumn;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;

public class FormTemplateHeaderView extends ViewWithUiHandlers<FormTemplateHeaderUiHandlers>
		implements FormTemplateHeaderPresenter.MyView{

	public interface Binder extends UiBinder<Widget, FormTemplateHeaderView> { }

	private final Widget widget;

	private List<DataRow<HeaderCell>> rows;

	private List<com.aplana.sbrf.taxaccounting.model.Column> columns = new ArrayList<Column>();

	private final NoSelectionModel<DataRow> selectionModel;

	private List<HeaderCell> selectedCells = new ArrayList<HeaderCell>();

	private int lastRowIndex;
	private int lastColumnIndex;
	private int initialRowIndex;
	private int initialColumnIndex;

	private static final String SELECTED_CELL_BACKGROUND_COLOR = "#9A9CFF";
	private static final int COLUMN_OFFSET = 0;

	@UiField
	DataGrid<DataRow<HeaderCell>> formDataTable;

	@UiField
	Button upRowButton;

	@UiField
	Button downRowButton;

	@UiField
	Button uniteCellsButton;

	@UiField
	Button disuniteCellsButton;

	@Inject
	public FormTemplateHeaderView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);

		selectionModel = new NoSelectionModel<DataRow>();
		formDataTable.setSelectionModel(selectionModel);

		formDataTable.addDomHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
					if (!selectedCells.isEmpty()) {
						// освобождаем выделенные ячейки
						selectOrClearCells(false);
					}
					uniteCellsButton.setVisible(false);
					disuniteCellsButton.setVisible(false);
				}
			}
		}, MouseDownEvent.getType());

		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
				checkEnableUpDownButton();
			}
		});

		formDataTable.addDomHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(MouseUpEvent event) {
				// не знаю как сделать по другому, чтобы ко мне на таблицу начал приходить евент mouseup
			}
		}, MouseUpEvent.getType());

		formDataTable.addCellPreviewHandler(new CellPreviewEvent.Handler<DataRow<HeaderCell>>() {
			@Override
			public void onCellPreview(CellPreviewEvent<DataRow<HeaderCell>> event) {
				TableCellElement cellElement = formDataTable.getRowElement(event.getIndex()).getCells().getItem(event.getColumn());
				// получаем индексы первой ячейки
				if ("mousedown".equals(event.getNativeEvent().getType())
						&& event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
					initialColumnIndex = Integer.valueOf(cellElement.getId().substring(cellElement.getId().lastIndexOf("_") + 1));
					if (initialColumnIndex < COLUMN_OFFSET) {
						initialColumnIndex = COLUMN_OFFSET;
					}
					initialRowIndex = event.getIndex();
				} // запоминаем последнюю ячейку и заполняем ячейки между первой и последней
				else if("mouseup".equals(event.getNativeEvent().getType())
						&& event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
					lastColumnIndex = Integer.valueOf(cellElement.getId().substring(cellElement.getId().lastIndexOf("_") + 1));
					if (lastColumnIndex < COLUMN_OFFSET) {
						lastColumnIndex = COLUMN_OFFSET;
					}
					lastRowIndex = event.getIndex();
					// выделяем ячейки
					selectOrClearCells(true);
				}
				else if ("mouseover".equals(event.getNativeEvent().getType())) {
					if (cellElement.getInnerText().replace("\u00A0", "").trim().isEmpty()) {
						cellElement.removeAttribute("title");
					} else {
						cellElement.setTitle(cellElement.getInnerText());
					}
				}
			}
		});

	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setHeaderData( List<DataRow<HeaderCell>> headers) {
		rows = headers;

		if (!headers.isEmpty()) {
			formDataTable.setRowData(headers);
			CustomTableBuilder<DataRow<HeaderCell>> builder = new CustomTableBuilder<DataRow<HeaderCell>>(formDataTable, true);
			formDataTable.setTableBuilder(builder);
		} else {
			formDataTable.setRowCount(0);
			formDataTable.setRowData(new ArrayList<DataRow<HeaderCell>>(0));
		}
	}

	@Override
	public void setColumnsData(List<Column> columnsData) {
		columns = columnsData;

		// Clean columns
		while (formDataTable.getColumnCount() > 0) {
			formDataTable.removeColumn(0);
		}


		//create form columns
		for (final Column col : columns) {
			StringColumn column = new StringColumn();
			column.setAlias(col.getAlias());
			ColumnContext columnContext = new ColumnContext();
			columnContext.setColumn(column);
			columnContext.setMode(ColumnContext.Mode.SUPER_EDIT_MODE);
			DataRowColumn editTextAliasColumn = new EditTextColumn(column, columnContext) {
				@Override
				public String getValue(DataRow aliasRow) {
					return aliasRow.getCell(col.getAlias()).getValue() == null ? col.getName() : aliasRow.getCell(col.getAlias()).getValue().toString();
				}
			};
			if (col.getWidth() >= 0) {
				formDataTable.setColumnWidth(editTextAliasColumn, col.getWidth() + "em");
			}
			formDataTable.addColumn(editTextAliasColumn);
		}

		//TODO КОСТЫЛИ! По возможности убрать.
		float tableWidth = 0;
		for (int i=0; i<formDataTable.getColumnCount(); i++) {
			String width = formDataTable.getColumnWidth(formDataTable.getColumn(i));
			if (width == null) {
				continue;
			}
			for (Style.Unit unit : Style.Unit.values()) {
				if (width.contains(unit.getType())) {
					width = width.replace(unit.getType(), "");
					break;
				}
			}
			tableWidth += Float.parseFloat(width);
		}
		formDataTable.setTableWidth(tableWidth, Style.Unit.EM);

	}

	private void selectOrClearCells(boolean select) {
		int topRowIndex;
		int bottomRowIndex;
		int leftColumnIndex;
		int rightColumnIndex;
		if (initialRowIndex - lastRowIndex >= 0) {
			topRowIndex = lastRowIndex;
			bottomRowIndex = initialRowIndex;
		} else {
			topRowIndex = initialRowIndex;
			bottomRowIndex = lastRowIndex;
		}
		if (initialColumnIndex - lastColumnIndex >= 0) {
			rightColumnIndex = lastColumnIndex;
			leftColumnIndex = initialColumnIndex;
		} else {
			rightColumnIndex = initialColumnIndex;
			leftColumnIndex = lastColumnIndex;
		}

		selectedCells.clear();
		// Проходим по всем елементам выделенного прямоуголника
		while (topRowIndex <= bottomRowIndex) {
			for (int colIndex = rightColumnIndex; colIndex <= leftColumnIndex; colIndex++) {
				Element element = DOM.getElementById(CustomTableBuilder.TD + "_" + topRowIndex + "_" + colIndex);
                DataRow<HeaderCell> currentRow;
				try{
                    currentRow = rows.get(topRowIndex);
                } catch (IndexOutOfBoundsException e){
                    break;
                }
				HeaderCell cell = currentRow.getCell(columns.get(colIndex - COLUMN_OFFSET).getAlias());

				if (select) { // выделяем ячейки
					element.getStyle().setBackgroundColor(SELECTED_CELL_BACKGROUND_COLOR);
					selectedCells.add(cell);
				} else { // выставляем прежний background color
					element.getStyle().clearBackgroundColor();
				}
			}
			topRowIndex++;
		}

		if (selectedCells.size() > 1) {
			uniteCellsButton.setVisible(true);
		} else if (selectedCells.size() == 1 &&
				(selectedCells.get(0).getColSpan() > 1 || selectedCells.get(0).getRowSpan() > 1)) {
			disuniteCellsButton.setVisible(true);
		}
	}

	public void refresh() {
		selectedCells.clear();
		uniteCellsButton.setVisible(false);
		disuniteCellsButton.setVisible(false);
		setHeaderData(rows);
		formDataTable.redraw();
	}

	@UiHandler("addRowButton")
	public void onAddButton(ClickEvent event){
		if(getUiHandlers()!= null && !columns.isEmpty()){
			getUiHandlers().onAddButton(new DataRow<HeaderCell>("", FormDataUtils.createHeaderCells(columns)));
		}
	}

	@UiHandler("addNumberedHeaderButton")
	public void onAddNumberedHeaderButton(ClickEvent event){
		if(getUiHandlers()!= null){
			getUiHandlers().onAddNumberedHeaderButton(new DataRow<HeaderCell>("", FormDataUtils.createHeaderCells(columns)));
		}
	}

	@UiHandler("removeRowButton")
	public void onRemoveButton(ClickEvent event){
		if(getUiHandlers()!= null){
			getUiHandlers().onRemoveButton(selectionModel.getLastSelectedObject());
		}
	}

	@UiHandler("uniteCellsButton")
	public void onUniteButton(ClickEvent event){
		if(getUiHandlers()!=null){
			DataRow<HeaderCell> currentRow = rows.get(initialRowIndex <= lastRowIndex ? initialRowIndex : lastRowIndex);
			HeaderCell cell = currentRow.getCell(columns.
					get((initialColumnIndex <= lastColumnIndex ? initialColumnIndex : lastColumnIndex) - COLUMN_OFFSET).getAlias());
			cell.setRowSpan(Math.abs(initialRowIndex - lastRowIndex) + 1);
			cell.setColSpan(Math.abs(initialColumnIndex - lastColumnIndex) + 1);
			refresh();
			uniteCellsButton.setVisible(false);
		}
	}

	@UiHandler("disuniteCellsButton")
	public void onDisuniteButton(ClickEvent event){
		if(getUiHandlers()!=null){
			DataRow<HeaderCell> currentRow = rows.get(initialRowIndex <= lastRowIndex ? initialRowIndex : lastRowIndex);
			HeaderCell cell = currentRow.getCell(columns.
					get((initialColumnIndex <= lastColumnIndex ? initialColumnIndex : lastColumnIndex) - COLUMN_OFFSET).getAlias());
			cell.setRowSpan(1);
			cell.setColSpan(1);
			refresh();
			disuniteCellsButton.setVisible(false);
		}
	}

	@UiHandler("upRowButton")
	public void onUpRowButton(ClickEvent event){
		if(getUiHandlers()!=null){
			DataRow row = selectionModel.getLastSelectedObject();
			int index = rows.indexOf(row);
			rows.set(index, rows.get(index - 1));
			rows.set(index - 1, row);
			setHeaderData(rows);
		}
		checkEnableUpDownButton();
	}

	@UiHandler("downRowButton")
	public void onDownRowButton(ClickEvent event){
		if(getUiHandlers()!=null){
			DataRow row = selectionModel.getLastSelectedObject();
			int index = rows.indexOf(row);
			rows.set(index, rows.get(index + 1));
			rows.set(index + 1, row);
			setHeaderData(rows);
		}
		checkEnableUpDownButton();
	}

	private void checkEnableUpDownButton() {
		upRowButton.setEnabled(false);
		downRowButton.setEnabled(false);

		DataRow row = selectionModel.getLastSelectedObject();
		int index = rows.indexOf(row);

		if (index != rows.size() - 1) {
			downRowButton.setEnabled(true);
		}
		if (index != 0) {
			upRowButton.setEnabled(true);
		}
	}

}
