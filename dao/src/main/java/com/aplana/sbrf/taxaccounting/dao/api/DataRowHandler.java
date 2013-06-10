package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;

/**
 * Обработчик для потоковой обработки строк НФ
 * 
 * @author sgoryachkin
 *
 */
public interface DataRowHandler {
	
	/**
	 * Метод обрабатывает полученную строку НФ
	 * 
	 * 
	 * @param row - строка
	 * @param index - индекс строки в контексте формы
	 * 
	 * @return 
	 * true - есть необходимость получить следующее значение.
	 * false - означает что дальнейшего перебора не будет.
	 * 
	 */
	boolean handle(DataRow<Cell> row, int index);

}
