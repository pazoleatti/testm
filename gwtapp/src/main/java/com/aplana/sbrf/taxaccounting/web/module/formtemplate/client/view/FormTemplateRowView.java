package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;


import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter.FormTemplateRowPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.ui.StyleCellPopup;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.cell.IndexCell;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.*;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
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

import java.util.ArrayList;
import java.util.List;


public class FormTemplateRowView extends ViewWithUiHandlers<FormTemplateRowUiHandlers>
		implements FormTemplateRowPresenter.MyView{
	public interface Binder extends UiBinder<Widget, FormTemplateRowView> { }

	private static final String SELECTED_CELL_BACKGROUND_COLOR = "#9A9CFF";
	private static final int COLUMN_OFFSET = 2;
    private static final int WIDTH_FOR_ZERO_COLUMN = 10;
	private final StyleCellPopup styleCellPopup;
	private final NoSelectionModel<DataRow> selectionModel;
	private final DataRowColumnFactory factory = new DataRowColumnFactory();
	private List<DataRow<Cell>> rows;
	private List<Column> columns;
	private List<FormStyle> styles;
	private List<Cell> selectedCells = new ArrayList<Cell>();
	private int lastRowIndex;
	private int lastColumnIndex;
	private int initialRowIndex;
	private int initialColumnIndex;

	@UiField
	DataGrid<DataRow<Cell>> formDataTable;

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
	public FormTemplateRowView(Binder binder) {
		initWidget(binder.createAndBindUi(this));

		factory.setSuperEditMode(true);
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

		asWidget().addDomHandler(new ContextMenuHandler() {
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

		formDataTable.addCellPreviewHandler(new CellPreviewEvent.Handler<DataRow<Cell>>() {
			@Override
			public void onCellPreview(CellPreviewEvent<DataRow<Cell>> event) {
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
				DataRow<Cell> currentRow;
                try{
                    currentRow = rows.get(topRowIndex);
                } catch (IndexOutOfBoundsException e){
                    break;
                }
				Cell cell = currentRow.getCell(columns.get(colIndex - COLUMN_OFFSET).getAlias());

                if (element != null) {
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
	public void setColumnsData(List<Column> columnsData) {
		columns = columnsData;

		// Clean columns
		while (formDataTable.getColumnCount() > 0) {
			formDataTable.removeColumn(0);
		}
		//Create order column
		NumericColumn numericColumn = new NumericColumn();
		DataRowColumn<Integer> indexColumn = new DataRowColumn<Integer>(new IndexCell(), numericColumn) {
			@Override
			public Integer getValue(DataRow<Cell> object) {
				return object.getIndex();
			}
		};
		indexColumn.setCellStyleNames("order");
		formDataTable.addColumn(indexColumn, "№");
		formDataTable.setColumnWidth(indexColumn, "3em");

		//Create alias column
		StringColumn aliasColumn = new StringColumn();
		ColumnContext columnContext = new ColumnContext();
		columnContext.setColumn(aliasColumn);
		columnContext.setMode(ColumnContext.Mode.SUPER_EDIT_MODE);
		EditTextColumn editTextAliasColumn = new EditTextColumn(aliasColumn, columnContext) {
			@Override
			public String getValue(DataRow aliasRow) {
				return aliasRow.getAlias();
			}
		};
		editTextAliasColumn.setFieldUpdater(new FieldUpdater<DataRow<Cell>, String>() {
			@Override
			public void update(int index, DataRow<Cell> dataRow, String value) {
				dataRow.setAlias(value);
			}
		});
		formDataTable.addColumn(editTextAliasColumn, "Код строки");
		formDataTable.setColumnWidth(editTextAliasColumn, "10em");

		//create form columns
		for (Column col : columns) {
			com.google.gwt.user.cellview.client.Column<DataRow<Cell>, ?> tableCol = factory
					.createTableColumn(col, formDataTable);

			if (col.getWidth() > 0) {
				formDataTable.setColumnWidth(tableCol, col.getWidth() + "em");
			} else if (col.getWidth() == 0) {
                formDataTable.setColumnWidth(tableCol, WIDTH_FOR_ZERO_COLUMN + "em");
            }
            formDataTable.addColumn(tableCol, col.getName());
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

	public void refresh() {
		selectedCells.clear();
		uniteCellsButton.setVisible(false);
		disuniteCellsButton.setVisible(false);
		setRowsData(rows);
		formDataTable.redraw();
	}

	@Override
	public void setRowsData(List<DataRow<Cell>> rowsData) {
		rows = rowsData;
		if (!rowsData.isEmpty()) {
			formDataTable.setRowData(rowsData);
			int i = 1;
			for (DataRow<Cell> row : rowsData) {
				row.setIndex(i++);
			}
			CustomTableBuilder<DataRow<Cell>> builder = new CustomTableBuilder<DataRow<Cell>>(formDataTable, true);
			formDataTable.setTableBuilder(builder);
		} else {
			formDataTable.setRowCount(0);
			formDataTable.setRowData(new ArrayList<DataRow<Cell>>(0));
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
	public void addCustomHeader(List<DataRow<HeaderCell>> newHeaders) {
		CustomHeaderBuilder builder = new CustomHeaderBuilder(formDataTable, false, 2, newHeaders);
		formDataTable.setHeaderBuilder(builder);
	}

	@UiHandler("uniteCellsButton")
	public void onUniteButton(ClickEvent event){
		if(getUiHandlers()!=null){
			DataRow<Cell> currentRow = rows.get(initialRowIndex <= lastRowIndex ? initialRowIndex : lastRowIndex);
			Cell cell = currentRow.getCell(columns.
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
			DataRow<Cell> currentRow = rows.get(initialRowIndex <= lastRowIndex ? initialRowIndex : lastRowIndex);
			Cell cell = currentRow.getCell(columns.
					get((initialColumnIndex <= lastColumnIndex ? initialColumnIndex : lastColumnIndex) - COLUMN_OFFSET).getAlias());
			cell.setRowSpan(1);
			cell.setColSpan(1);
			refresh();
			disuniteCellsButton.setVisible(false);
		}
	}

	@UiHandler("addRowButton")
	public void onAddButton(ClickEvent event){
        try {
            if (!columns.isEmpty()){
				FormTemplate formTemplate = new FormTemplate();
				formTemplate.getColumns().addAll(columns);
				formTemplate.getStyles().addAll(styles);

                rows.add(new DataRow<Cell>("Новый код", FormDataUtils.createCells(formTemplate)));
                setRowsData(rows);
            }
        } catch (IllegalArgumentException e){
            Dialog.errorMessage("Ошибка", e.getMessage());
        }
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