package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.log.Logger;

/**
 * Интерфейс сервиса для запуска скриптов по декларациями (пока существует только один скрипт - скрипт создания)
 * @author dsultanbekov
 */
public interface DeclarationDataScriptingService {
	String create(Logger logger, int departmentId, int declarationTemplateId, int reportPeriodId);
}
