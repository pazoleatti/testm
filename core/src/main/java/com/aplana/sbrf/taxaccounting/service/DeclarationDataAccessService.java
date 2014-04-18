package com.aplana.sbrf.taxaccounting.service;

import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

/**
 * Интерфейс для проверки прав пользователя на работу с {@link DeclarationData декларациями}
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
	 * @param departmentId - id депортимента декларации (не пользователя)
	 * @param reportPeriodId - id отчетного периода
	 * @param scriptEvent - событие (действие)
	 */
	void checkEvents(TAUserInfo userInfo, Integer declarationTemplateId, Integer departmentId, Integer reportPeriodId,
                     FormDataEvent scriptEvent);
	
	/**
	 * Получить все разрешенные действия над существующим объектом
	 * 
	 * @return
	 */
	Set<FormDataEvent> getPermittedEvents(TAUserInfo userInfo, Long declarationDataId);

	/**
	 * Получить все разрешенные действия над не существующим объектом
	 * 
	 * @return
	 */
	Set<FormDataEvent> getPermittedEvents(TAUserInfo userInfo, Integer declarationTemplateId, Integer departmentId, Integer reportPeriodId);
}
