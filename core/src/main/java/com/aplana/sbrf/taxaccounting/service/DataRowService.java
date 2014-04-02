package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;

public interface DataRowService {
	
	/**
	 * Получение страныцы с набором строк НФ
	 * 
	 *
     * @param userInfo
     * @param formDataId
     * @param range
     * @param saved
     * @param manual
     * @return
	 */
	PagingResult<DataRow<Cell>> getDataRows(TAUserInfo userInfo, long formDataId, DataRowRange range, boolean saved, boolean manual);
	
	/**
	 * Получени количество строк НФ
	 * 
	 *
     * @param userInfo
     * @param formDataId
     * @param saved
     * @param manual
     * @return
	 */
	int getRowCount(TAUserInfo userInfo, long formDataId, boolean saved, boolean manual);
	
	/**
	 * Обновление набора строк во временном срезе НФ
	 *
     * @param userInfo
     * @param formDataId
     * @param dataRows
     * @param manual
     */
	void update(TAUserInfo userInfo, long formDataId, List<DataRow<Cell>> dataRows, boolean manual);
	
	
	
	/**
	 * @param userInfo
	 * @param formDataId
	 */
	void rollback(TAUserInfo userInfo, long formDataId);
	
	
	

}
