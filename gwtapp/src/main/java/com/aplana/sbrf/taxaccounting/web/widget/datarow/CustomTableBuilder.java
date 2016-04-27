package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.Color;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.builder.shared.DivBuilder;
import com.google.gwt.dom.builder.shared.TableCellBuilder;
import com.google.gwt.dom.builder.shared.TableRowBuilder;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.AbstractCellTableBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.view.client.SelectionModel;

import java.util.*;

public class CustomTableBuilder<T> extends AbstractCellTableBuilder<T> {

	public static final String TD = "td";
	private static final String STRIPE_ICON_PROPERTY = "url(resources/img/stripe.png)";

	private final String evenRowStyle;
	private final String oddRowStyle;
	private final String selectedRowStyle;
	private final String cellStyle;
	private final String evenCellStyle;
	private final String oddCellStyle;
	private final String firstColumnStyle;
	private final String lastColumnStyle;
	private final String selectedCellStyle;
	private final boolean isStriped;

    //Переменная инициализируется когда инициализируется во время инициализации таблицы
    //потом меняется при добавлении новой строки.
    //Сделано чтобы очищать правильно globalSpans при добавлении новой строки
    private int rowCountWhenInitialize;

	private Map<Integer, Collection<Integer>> globalSpans = new HashMap<Integer, Collection<Integer>>();

	/**
	 * Construct a new table builder.
	 *
	 * @param cellTable the table this builder will build rows for
	 */
	public CustomTableBuilder(AbstractCellTable<T> cellTable, boolean isStriped) {
		super(cellTable);
		this.isStriped = isStriped;

		AbstractCellTable.Style style = cellTable.getResources().style();
		evenRowStyle = style.evenRow();
		oddRowStyle = style.oddRow();
		selectedRowStyle = " " + style.selectedRow();
		cellStyle = style.cell();
		evenCellStyle = " " + style.evenRowCell();
		oddCellStyle = " " + style.oddRowCell();
		firstColumnStyle = " " + style.firstColumn();
		lastColumnStyle = " " + style.lastColumn();
		selectedCellStyle = " " + style.selectedRowCell();
        rowCountWhenInitialize = cellTable.getRowCount();
        clearGlobalSpans();
	}

    public void clearGlobalSpans(){
        globalSpans.clear();
    }

	@Override
	public void buildRowImpl(T rowValue, int absRowIndex) {
        if (cellTable.getRowCount() != rowCountWhenInitialize){
            clearGlobalSpans();
            rowCountWhenInitialize = cellTable.getRowCount();
        }
		// Calculate the row styles.
		SelectionModel<? super T> selectionModel = cellTable.getSelectionModel();
		boolean isSelected =
				(selectionModel == null || rowValue == null) ? false : selectionModel.isSelected(rowValue);
		boolean isEven = absRowIndex % 2 == 0;
		StringBuilder trClasses = new StringBuilder(isEven ? evenRowStyle : oddRowStyle);
		if (isSelected) {
			trClasses.append(selectedRowStyle);
		}

		// Add custom row styles.
		RowStyles<T> rowStyles = cellTable.getRowStyles();
		if (rowStyles != null) {
			String extraRowStyles = rowStyles.getStyleNames(rowValue, absRowIndex);
			if (extraRowStyles != null) {
				trClasses.append(" ").append(extraRowStyles);
			}
		}

		// Build the row.
		TableRowBuilder tr = startRow();
		tr.className(trClasses.toString());

		// Build the columns.
		int columnCount = cellTable.getColumnCount();
		int rowCount = cellTable.getRowCount();
		for (int curColumn = 0; curColumn < columnCount; curColumn++) {
			if (globalSpans.get(absRowIndex) == null || !globalSpans.get(absRowIndex).contains(curColumn)) {
				Column<T, ?> column = cellTable.getColumn(curColumn);
				AbstractCell currentCell = null;
				if (((DataRowColumn<?>)column).getAlias() != null) {
					currentCell =
							((DataRow<? extends AbstractCell>) rowValue).getCell(((DataRowColumn<?>)column).getAlias() );
				}
				// Create the cell styles.
				StringBuilder tdClasses = new StringBuilder(cellStyle);
				tdClasses.append(isEven ? evenCellStyle : oddCellStyle);
				if (curColumn == 0) {
					tdClasses.append(firstColumnStyle);
				}
				if (isSelected) {
					tdClasses.append(selectedCellStyle);
				}
				// The first and last column could be the same column.
				if (curColumn == columnCount - 1) {
					tdClasses.append(lastColumnStyle);
				}

				// Add class names specific to the cell.
				Cell.Context context = new Cell.Context(absRowIndex, curColumn, cellTable.getValueKey(rowValue));
				String cellStyles = column.getCellStyleNames(context, rowValue);
				if (cellStyles != null) {
					tdClasses.append(" " + cellStyles);
				}

				// Build the cell.
				HasHorizontalAlignment.HorizontalAlignmentConstant hAlign = column.getHorizontalAlignment();
				HasVerticalAlignment.VerticalAlignmentConstant vAlign = column.getVerticalAlignment();

				TableCellBuilder td = tr.startTD();
				td.id(TD + "_" + absRowIndex + "_" + curColumn);

				td.className(tdClasses.toString());
				if (hAlign != null) {
					td.align(hAlign.getTextAlignString());
				}
				if (vAlign != null) {
					td.vAlign(vAlign.getVerticalAlignString());
				}

				if ( (currentCell != null) && ((currentCell.getRowSpan() > 1) || (currentCell.getColSpan() > 1) )) {
					int rowSpan = currentCell.getRowSpan();
					int colSpan = currentCell.getColSpan();
					if ((curColumn+colSpan <= columnCount) && (absRowIndex+rowSpan <= rowCount)) {
						spanCells(td, absRowIndex, curColumn, rowSpan, colSpan);
					} else {
						throw new RuntimeException("Ошибка построения таблицы, сolspan за пределами таблицы!");
					}
				}
				// Добавляем стили. Они должны идти друг за другом
				if (isStriped && currentCell != null
						&& (currentCell instanceof com.aplana.sbrf.taxaccounting.model.Cell)
						&& ((com.aplana.sbrf.taxaccounting.model.Cell)currentCell).isEditable()) {
					td.style().trustedBackgroundImage(STRIPE_ICON_PROPERTY);
				}

				String colWidth = cellTable.getColumnWidth(column);
				if ((colWidth.equals("0em") || colWidth.equals("0px") || colWidth.equals("0.0em")  || colWidth.equals("0.0px"))
                        && (currentCell.getColSpan() == 1)) {
					td.style().borderStyle(Style.BorderStyle.NONE);
				} else {
                    td.style().width(0, com.google.gwt.dom.client.Style.Unit.EM);
                }

				if ((currentCell != null) && (currentCell instanceof com.aplana.sbrf.taxaccounting.model.Cell)) {
                    FormStyle clientCellStyle = ((com.aplana.sbrf.taxaccounting.model.Cell)currentCell).getClientStyle();
                    if (clientCellStyle != null) { // если на ячейку назначен стиль
                        applyOurCustomStyles(td, clientCellStyle);
                    } else {
                        FormStyle currentCellStyle = ((com.aplana.sbrf.taxaccounting.model.Cell)currentCell).getStyle();
                        if (currentCellStyle != null) { // если на ячейку назначен стиль
                            applyOurCustomStyles(td, currentCellStyle);
                        }
                    }
				}



				// Add the inner div.
				DivBuilder div = td.startDiv();
				div.style().outlineStyle(Style.OutlineStyle.NONE).endStyle();

				// Render the cell into the div.
				renderCell(div, context, column, rowValue);

				// End the cell.
				div.endDiv();
				td.endTD();
			}
		}

		// End the row.
		tr.endTR();
	}

	/**
	 * Метод для объединения ячеек таблицы.
	 * Основная идея в том, что мы сохраням координаты ячеек которые необходимо пропускать.
	 * @param out часть строки таблицы для формирования
	 * @param rowIndex номер строки
	 * @param colIndex номер столбца
	 * @param rowSpan количество объединяемых строк
	 * @param colSpan количество объединяемых столбцов
	 */
	private void spanCells(TableCellBuilder out, Integer rowIndex, int colIndex, int rowSpan, int colSpan) {
		out.rowSpan(rowSpan);
		out.colSpan(colSpan);

		List<Integer> spn = new ArrayList<Integer>();

		for (int sp=0; sp<colSpan; sp++) {
			spn.add(colIndex + sp);
		}
		// Оставляем текущую ячейку
		//TODO: попробовать сделать проще.
		if ((globalSpans.get(rowIndex) != null) ) {
			Set<Integer> tmp = new HashSet<Integer>(globalSpans.get(rowIndex));
			tmp.addAll(spn.subList(1, spn.size()));
			globalSpans.put(rowIndex, tmp);
		} else {
			globalSpans.put(rowIndex, spn.subList(1,spn.size()));
		}
		for (int rsp=0; rsp<rowSpan-1; rsp++) {

			if ((globalSpans.get(rowIndex+1+rsp) != null)) {
				Set<Integer> tmp = new HashSet<Integer>(globalSpans.get(rowIndex+1+rsp));
				tmp.addAll(spn);
				globalSpans.put(rowIndex+1+rsp, tmp);
			} else {
				globalSpans.put(rowIndex+1+rsp, spn);
			}

		}
	}

	private void applyOurCustomStyles(TableCellBuilder out, FormStyle ourStyle) {
        out.style()
                .fontStyle(ourStyle.isItalic() ? Style.FontStyle.ITALIC : Style.FontStyle.NORMAL)
                .fontWeight(ourStyle.isBold() ? Style.FontWeight.BOLD : Style.FontWeight.NORMAL)
                .trustedBackgroundColor(convertColorToRGBString(ourStyle.getBackColor()))
                .trustedColor(convertColorToRGBString(ourStyle.getFontColor()))
                .endStyle();
    }

	private String convertColorToRGBString(Color color) {
		return "rgb(" +
					color.getRed() + "," +
					color.getGreen() + "," +
					color.getBlue() +
				")";
	}
}
