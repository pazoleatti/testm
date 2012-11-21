package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;

import java.util.List;

/**
 * Интерфейс, позволяющий пользователю получать данные из базы по запросу
 * @author srybakov
 *
 */
public interface DataHandlerService {

	List<FormData> findDataByUserIdAndFilter(Long userId, FormDataFilter formDataFilter);
}
