package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;

/**
 * Сервис для работы с журналом аудита
 */
public interface AuditService {
	/**
	 * Получить информацию из журнала аудита по фильтру
	 * @param logSystemFilter фильтр по которому происходит поиск необходимых данных
	 * @return объект, представляющий искомую информацию из журанала аудита
	 * */
	List<LogSystemSearchResultItem> getLogs(LogSystemFilter logSystemFilter);

	/**
	 * Добавить информацию об логировании
	 */
	void add(FormDataEvent event, TAUserInfo userInfo, int departmentId, int reportPeriodId,
			 Integer declarationTypeId, Integer formTypeId, Integer formKindId, String note);

	/**
	 * Получить данные используемые для фильтрации журналом аудита
	 */
	LogSystemFilterAvailableValues getFilterAvailableValues();
}
