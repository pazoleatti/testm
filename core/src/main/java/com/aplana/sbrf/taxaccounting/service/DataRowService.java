package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;

public interface DataRowService {
	
	/**
	 * Получение страныцы с набором строк НФ
	 * 
	 * @param userInfo
	 * @param formDataId
	 * @param range
	 * @param saved
	 * @return
	 */
	PaginatedSearchResult<DataRow<Cell>> getDataRows(TAUserInfo userInfo, long formDataId, DataRowRange range, boolean saved);
	
	/**
	 * Получени количество строк НФ
	 * 
	 * @param userInfo
	 * @param formDataId
	 * @param saved
	 * @return
	 */
	int getRowCount(TAUserInfo userInfo, long formDataId, boolean saved); 
	
	/**
	 * Обновление набора строк во временном срезе НФ
	 * 
	 * @param userInfo
	 * @param formDataId
	 * @param dataRows
	 */
	void update(TAUserInfo userInfo, long formDataId, List<DataRow<Cell>> dataRows);

}
