package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;

import java.util.List;

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
	PagingResult<DataRow<Cell>> getDataRows(TAUserInfo userInfo, long formDataId, DataRowRange range, boolean saved);
	
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
	
	
	
	/**
	 * @param userInfo
	 * @param formDataId
	 */
	void rollback(TAUserInfo userInfo, long formDataId);


    /**
     * Поиск по налоговой форме,
     * ищутся совпадения и выдается номер строки и столбца
     * на форме
     *
     * @param formDataId
     * @param range информация о выборке данных, с какой строки и сколько строк выбрать
     * @param key ключ для поиска
     * @return Set<FormDataSearchResult> - Набор из номера столбца, строки, и самой найденной подстроки
     */
    PagingResult<FormDataSearchResult> searchByKey(Long formDataId, DataRowRange range, String key);

}
