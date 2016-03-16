package com.aplana.sbrf.taxaccounting.model.util;

import java.util.*;
import java.util.Map.Entry;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DataRow.MapEntry;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;

/**
 * Утилита для безопасной работы с обьектом FormData
 * 
 * @author sgoryachkin
 *
 */
public final class FormDataUtils {
	
	private FormDataUtils(){	
	}
	
	/**
	 * Метод приводит список строк в форму, которая позволяет получать/сохранять
	 * значение в перекрытых с помощью colSpan rowSpan ячейках - в главной
	 * ячейке. (SBRFACCTAX-2082)
	 * 
	 * @param dataRows список строк
	 */
	public static <T extends AbstractCell> void setValueOwners(List<DataRow<T>> dataRows) {
		
		Map<Pair<Integer, Integer>, AbstractCell> valueOwners = new HashMap<Pair<Integer, Integer>, AbstractCell>();

		int rowIdx = 0;
		for (DataRow<? extends AbstractCell> dataRow : dataRows) {
			int colIdx = 0;
			for (Entry<String, Object> entry : dataRow.entrySet()) {
				@SuppressWarnings("rawtypes")
				AbstractCell currentCell = ((MapEntry) entry).getCell();
				AbstractCell ownerCell = valueOwners.get(new Pair<Integer, Integer>(
						rowIdx, colIdx));
				
				// Проверяем - перекрыта ли ячейка с текущими индексами
				if (ownerCell != null) {
					// Если да, устанавливаем ей главную ячейку как хозяина
					currentCell.setRowSpan(1);
					currentCell.setColSpan(1);
					currentCell.setValueOwner(ownerCell);
				}

				// Заполняем мапу с перекрытыми ячейками для текущей ячейки.
				if (currentCell.getColSpan() > 1
						|| currentCell.getRowSpan() > 1) {
					for (int overRowIdx = rowIdx; overRowIdx < rowIdx
							+ currentCell.getRowSpan(); overRowIdx++) {
						for (int overColIdx = colIdx; overColIdx < colIdx
								+ currentCell.getColSpan(); overColIdx++) {
							if (overRowIdx == rowIdx && overColIdx == colIdx) {
								continue;
							}
							valueOwners.put(new Pair<Integer, Integer>(	overRowIdx, overColIdx), currentCell);
						}
					}
				}
				colIdx++;
			}
			rowIdx++;
		}
	}
	
	/**
	 * Метод очищает все значения valueOwner для ячеек. Обычно нужен перед сохранением формы в БД 
	 * (SBRFACCTAX-2201, SBRFACCTAX-2082)
	 * 
	 * @param dataRows список строк
	 */
	public static <T extends AbstractCell> void cleanValueOwners(List<DataRow<T>> dataRows) {
		for (DataRow<? extends AbstractCell> dataRow : dataRows) {
			for (Entry<String, Object> entry : dataRow.entrySet()) {
				@SuppressWarnings("rawtypes")
				AbstractCell currentCell = ((MapEntry) entry).getCell();
				currentCell.setValueOwner(null);
			}
		}
	}

	/**
	 * Создает группу Cell 
	 * 
	 * @param formTemplate версия макета НФ, в которой хранится информация о стилях и столбцах
	 * @return
	 */
	public static List<Cell> createCells(FormTemplate formTemplate) {
		List<Cell> cells = new ArrayList<Cell>();
		for (Column column : formTemplate.getColumns()) {
			cells.add(new Cell(column, formTemplate.getStyles()));
		}
		return cells;
	}

	/**
	 * Создает группу CellHeader
	 * 
	 * @param columns
	 * @return
	 */
	public static List<HeaderCell> createHeaderCells(List<Column> columns) {
		List<HeaderCell> cells = new ArrayList<HeaderCell>();
		for (Column column : columns) {
			cells.add(new HeaderCell(column));
		}
		return cells;
	}
	
	public static <T extends AbstractCell> DataRow<T> getDataRowByAlias(List<DataRow<T>> dataRows, String rowAlias){
			if (rowAlias == null) {
				throw new IllegalArgumentException("Row alias cannot be null");
			}
			for (DataRow<T> row : dataRows) {
				if (rowAlias.equals(row.getAlias())) {
					return row;
				}
			}
			throw new IllegalArgumentException("Wrong row alias requested: " + rowAlias);
	}

}