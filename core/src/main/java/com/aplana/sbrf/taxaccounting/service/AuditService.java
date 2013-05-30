package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.LogSystem;
import com.aplana.sbrf.taxaccounting.model.LogSystemFilter;

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
	List<LogSystem> getLogs(LogSystemFilter logSystemFilter);

	/**
	 * Добавить информацию об логировании
	 */
	void add(LogSystem logSystem);
}
