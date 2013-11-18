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
	PagingResult<LogSearchResultItem> getLogsByFilter(LogSystemFilter logSystemFilter);

	/**
	 * Добавить информацию об логировании
	 */
	void add(FormDataEvent event, TAUserInfo userInfo, int departmentId, Integer reportPeriodId,
			 Integer declarationTypeId, Integer formTypeId, Integer formKindId, String note);

	/**
	 * Получить данные используемые для фильтрации журналом аудита
	 */
	LogSystemFilterAvailableValues getFilterAvailableValues();

    /**
     * Удаляем набор записей из журнала и сразу создаем запись в ЖА об архивировании.
     * @param itemList
     */
    void removeRecords(List<LogSearchResultItem> itemList, TAUserInfo userInfo);
}
