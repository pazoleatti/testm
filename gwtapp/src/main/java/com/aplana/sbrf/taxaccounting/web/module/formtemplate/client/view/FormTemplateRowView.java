package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;


import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateRowPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui.StyleCellPopup;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomHeaderBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomTableBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.EditTextColumn;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;


public class FormTemplateRowView extends ViewWithUiHandlers<FormTemplateRowUiHandlers>
		implements FormTemplateRowPresenter.MyView{
	public interface Binder extends UiBinder<Widget, FormTemplateRowView> { }

	private static final String SELECTED_CELL_BACKGROUND_COLOR = "#9A9CFF";
	private final StyleCellPopup styleCellPopup;
	private final NoSelectionModel<DataRow> selectionModel;
	private final DataRowColumnFactory factory = new DataRowColumnFactory();
	private final Widget widget;
	private List<DataRow> rows;
	private List<Column> columns;
	private List<FormStyle> styles;
	private List<Cell> selectedCells = new ArrayList<Cell>();
	private int lastRowIndex;
	private int lastColumnIndex;
	private int initialRowIndex;
	private int initialColumnIndex;

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

	@UiField
	Button uniteCellsButton;

	@UiField
	Button disuniteCellsButton;

	@Inject
	public FormTemplateRowView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);

		factory.setEditOnly(true);
		styleCellPopup = new StyleCellPopup(this);

		selectionModel = new NoSelectionModel<DataRow>();

		formDataTable.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
				checkEnableUpDownButton();
			}
		});

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

		formDataTable.addDomHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(MouseUpEvent event) {
				// не знаю как сделать по другому, чтобы ко мне на таблицу начал приходить евент mouseup
			}
		}, MouseUpEvent.getType());

		widget.addDomHandler(new ContextMenuHandler() {
			@Override
			public void onContextMenu(ContextMenuEvent event) {
				event.stopPropagation();
				event.preventDefault();

				int maxPopupX = Window.getClientWidth() - 250;
				styleCellPopup.setValue(selectedCells);
				styleCellPopup.show(maxPopupX > event.getNativeEvent().getClientX() ? event.getNativeEvent().getClientX()
						: maxPopupX, event.getNativeEvent().getClientY());
			}
		}, ContextMenuEvent.getType());

		formDataTable.addCellPreviewHandler(new CellPreviewEvent.Handler<DataRow>() {
			@Override
			public void onCellPreview(CellPreviewEvent<DataRow> event) {
				TableCellElement cellElement = formDataTable.getRowElement(event.getIndex()).getCells().getItem(event.getColumn());
				// получаем индексы первой ячейки
				if ("mousedown".equals(event.getNativeEvent().getType())
						&& event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
					initialColumnIndex = Integer.valueOf(cellElement.getId().substring(cellElement.getId().lastIndexOf("_") + 1));
					if (initialColumnIndex == 0) {
						initialColumnIndex = 1;
					}
					initialRowIndex = event.getIndex();
				} // запоминаем последнюю ячейку и заполняем ячейки между первой и последней
				else if("mouseup".equals(event.getNativeEvent().getType())
						&& event.getNativeEvent().getButton() == NativeEvent.BUTTON_LEFT) {
					lastColumnIndex = Integer.valueOf(cellElement.getId().substring(cellElement.getId().lastIndexOf("_") + 1));
					if (lastColumnIndex == 0) {
						lastColumnIndex = 1;
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
				DataRow currentRow = rows.get(topRowIndex);
				Cell cell = currentRow.getCell(columns.get(colIndex - 1).getAlias());

				if (select) { // выделяем ячейки
					element.getStyle().setBackgroundColor(SELECTED_CELL_BACKGROUND_COLOR);
					selectedCells.add(cell);
				} // выставляем прежний background color
				else {
					if (cell.getStyle() != null) {
						element.getStyle().setBackgroundColor(convertColorToRGBString(cell.getStyle().getBackColor()));
					} else {
						element.getStyle().clearBackgroundColor();
					}
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
		ColumnContext columnContext = new ColumnContext();
		columnContext.setColumn(aliasColumn);
		columnContext.setMode(ColumnContext.Mode.EDIT_MODE);
		EditTextColumn editTextAliasColumn = new EditTextColumn(aliasColumn, columnContext) {
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
			if (col.getWidth() > 0) {
				formDataTable.setColumnWidth(tableCol, col.getWidth() + "em");
			}
		}
	}

	public void refresh() {
		selectedCells.clear();
		uniteCellsButton.setVisible(false);
		disuniteCellsButton.setVisible(false);
		setRowsData(rows);
		formDataTable.redraw();
	}

	@Override
	public void setRowsData(List<DataRow> rowsData) {
		rows = rowsData;
		if (rowsData.size() != 0) {
			formDataTable.setRowData(rowsData);
			CustomTableBuilder<DataRow> builder = new CustomTableBuilder<DataRow>(formDataTable, styles, true);
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

	@UiHandler("uniteCellsButton")
	public void onUniteButton(ClickEvent event){
		if(getUiHandlers()!=null){
			DataRow currentRow = rows.get(initialRowIndex <= lastRowIndex ? initialRowIndex : lastRowIndex);
			Cell cell = currentRow.getCell(columns.
					get((initialColumnIndex <= lastColumnIndex ? initialColumnIndex : lastColumnIndex) - 1).getAlias());
			cell.setRowSpan(Math.abs(initialRowIndex - lastRowIndex) + 1);
			cell.setColSpan(Math.abs(initialColumnIndex - lastColumnIndex) + 1);
			refresh();
			uniteCellsButton.setVisible(false);
		}
	}

	@UiHandler("disuniteCellsButton")
	public void onDisuniteButton(ClickEvent event){
		if(getUiHandlers()!=null){
			DataRow currentRow = rows.get(initialRowIndex <= lastRowIndex ? initialRowIndex : lastRowIndex);
			Cell cell = currentRow.getCell(columns.
					get((initialColumnIndex <= lastColumnIndex ? initialColumnIndex : lastColumnIndex) - 1).getAlias());
			cell.setRowSpan(1);
			cell.setColSpan(1);
			refresh();
			disuniteCellsButton.setVisible(false);
		}
	}

	@UiHandler("addRowButton")
	public void onAddButton(ClickEvent event){
		rows.add(new DataRow("Новый код", columns, styles));
		setRowsData(rows);
	}

	@UiHandler("removeRowButton")
	public void onRemoveButton(ClickEvent event){
		if(getUiHandlers()!=null){
			getUiHandlers().onRemoveButton(selectionModel.getLastSelectedObject());
		}
	}

	@UiHandler("upRowButton")
	public void onUpRowButton(ClickEvent event){
		if(getUiHandlers()!=null){
			DataRow row = selectionModel.getLastSelectedObject();
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
			DataRow row = selectionModel.getLastSelectedObject();
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

		DataRow row = selectionModel.getLastSelectedObject();
		int index = rows.indexOf(row);

		if (index != rows.size() - 1) {
			downRowButton.setEnabled(true);
		}
		if (index != 0) {
			upRowButton.setEnabled(true);
		}
	}

	private String convertColorToRGBString(Color color) {
		return "rgb(" +
				color.getRed() + "," +
				color.getGreen() + "," +
				color.getBlue() +
				")";
	}
}