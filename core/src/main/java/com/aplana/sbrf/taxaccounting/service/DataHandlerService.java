package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DataFilter;
import com.aplana.sbrf.taxaccounting.model.FormData;

import java.util.List;

/**
 * Интерфейс, позволяющий пользователю получать данные из базы по запросу
 * @author srybakov
 *
 */
public interface DataHandlerService {

	List<FormData> findDataByUserIdAndFilter(Long userId, DataFilter dataFilter);
}
