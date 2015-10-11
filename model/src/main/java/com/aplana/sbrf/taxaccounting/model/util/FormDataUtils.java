package com.aplana.sbrf.taxaccounting.model.util;

import java.util.*;
import java.util.Map.Entry;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DataRow.MapEntry;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
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
     * Возвращает полное имя периода для нф с нарастающим итогом
     * @return
     */
    public static String getAccName(String name, Date calendarStartDate) {
        Calendar sDate = Calendar.getInstance();
        sDate.setTime(calendarStartDate);
        int day = sDate.get(Calendar.DAY_OF_MONTH);
        int month = sDate.get(Calendar.MONTH) + 1;
        if (day == 1 && month == 4) {
            //2 квартал: 2 квартал (полугодие)
            return name + " (полугодие)";
        } else if (day == 1 && month == 7) {
            //3 квартал: 3 квартал (9 месяцев)
            return name + " (9 месяцев)";
        } else if (day == 1 && month == 10) {
            //4 квартал: 4 квартал (год)
            return name + " (год)";
        } else {
            return name;
        }
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
	 * @param columns
	 * @param styles
	 * @return
	 */
	public static List<Cell> createCells(List<Column> columns, List<FormStyle> styles) {
		List<Cell> cells = new ArrayList<Cell>();
		for (Column column : columns) {
			cells.add(new Cell(column, styles));
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