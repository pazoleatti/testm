package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.LogSystem;

/**
 * DAO-Интерфейс для работы с журналом аудита
 */
public interface AuditDao {

	/**
	 * Добавить информацию об логировании
	 */
	void add(LogSystem logSystem);
}
