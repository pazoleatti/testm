package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import java.util.Set;

/**
 * Интерфейс для проверки прав пользователя на работу с {@link com.aplana.sbrf.taxaccounting.model.DeclarationData декларациями}
 * @author dsultanbekov
 */
public interface DeclarationDataAccessService {
	/**
	 * Проверяет возможность выполнения действия пользователем, над существующей декларацией.
	 * Метод генерит AccessDeniedException если есть проблемы с выполнение действия
	 * 
	 * @param userInfo - информация о пользователе
	 * @param declarationDataId - id декларации
	 * @param scriptEvent - событие (действие)
	 */
	void checkEvents(TAUserInfo userInfo, Long declarationDataId, FormDataEvent scriptEvent);
	
	/**
	 * Проверяет возможность выполнения действия пользователем, над ещё не существующей декларацией
	 * (Теоретически это может быть только создание декларации)
	 * Метод генерит AccessDenitedException если есть проблемы с выполнение действия
	 * 
	 * @param userInfo - информация о пользователе
	 * @param declarationTemplateId - id шаблона декларации
	 * @param departmentReportPeriod Отчетный период подразделения
	 * @param scriptEvent - событие (действие)
	 */
	void checkEvents(TAUserInfo userInfo, int declarationTemplateId, DepartmentReportPeriod departmentReportPeriod,
                     FormDataEvent scriptEvent);
	
	/**
	 * Получить все разрешенные действия над существующим объектом
	 */
	Set<FormDataEvent> getPermittedEvents(TAUserInfo userInfo, Long declarationDataId);

	/**
	 * Получить все разрешенные действия над не существующим объектом
	 */
	Set<FormDataEvent> getPermittedEvents(TAUserInfo userInfo, int declarationTemplateId,
                                          DepartmentReportPeriod departmentReportPeriod);
}
