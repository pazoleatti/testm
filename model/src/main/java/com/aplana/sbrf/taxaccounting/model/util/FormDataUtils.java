package com.aplana.sbrf.taxaccounting.model.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DataRow.MapEntry;

/**
 * Утилита для безопасной работы с обьектом FormData
 * 
 * @author sgoryachkin
 *
 */
public class FormDataUtils {
	
	private FormDataUtils(){	
	}
	
	/**
	 * Метод приводит список строк в форму, которая позволяет получать/сохранять
	 * значение в перекрытых с помощью colSpan rowSpan ячейках - в главной
	 * ячейке. (SBRFACCTAX-2082)
	 * 
	 * @param dataRows список строк
	 */
	public static void setValueOners(List<DataRow> dataRows) {
		
		Map<Pair<Integer, Integer>, Cell> valueOwners = new HashMap<Pair<Integer, Integer>, Cell>();

		int rowIdx = 0;
		for (DataRow dataRow : dataRows) {
			int colIdx = 0;
			for (Entry<String, Object> entry : dataRow.entrySet()) {
				Cell currentCell = ((MapEntry) entry).getCell();
				Cell ownerCell = valueOwners.get(new Pair<Integer, Integer>(
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
	public static void cleanValueOners(List<DataRow> dataRows) {
		for (DataRow dataRow : dataRows) {
			for (Entry<String, Object> entry : dataRow.entrySet()) {
				Cell currentCell = ((MapEntry) entry).getCell();
				currentCell.setValueOwner(null);
			}
		}
	}
	
	

}
