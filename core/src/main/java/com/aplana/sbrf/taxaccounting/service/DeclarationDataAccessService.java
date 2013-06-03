package com.aplana.sbrf.taxaccounting.service;

import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;

/**
 * Интерфейс для проверки прав пользователя на работу с {@link DeclarationData декларациями}
 * @author dsultanbekov
 */
public interface DeclarationDataAccessService {


	/**
	 * Проверяет возможность выполнения действия пользователем, над существующей декларацией.
	 * Метод генерит AccessDenitedException если есть проблемы с выполнение действия
	 * 
	 * @param userId - id пользователя
	 * @param declarationDataId - id декларации
	 * @param scriptEvents - событие (действие)
	 */
	void checkEvents(Integer userId, Long declarationDataId, FormDataEvent... scriptEvents);
	
	/**
	 * Проверяет возможность выполнения действия пользователем, над ещё не существующей декларацией
	 * (Теоретически это может быть только создание декларации)
	 * Метод генерит AccessDenitedException если есть проблемы с выполнение действия
	 * 
	 * @param userId - id пользователя
	 * @param declarationTemplateId - id шаблона декларации
	 * @param departmentId - id депортимента декларации (не пользователя)
	 * @param reportPeriodId - id отчетного периода
	 * @param scriptEvents - событие (действие)
	 */
	void checkEvents(Integer userId, Integer declarationTemplateId, Integer departmentId, Integer reportPeriodId, FormDataEvent... scriptEvents);
	
	/**
	 * Получить все разрешенные действия над существующим объектом
	 * 
	 * @return
	 */
	Set<FormDataEvent> getPermittedEvents(Integer userId, Long declarationDataId);
	
	
	/**
	 * Получить все разрешенные действия над не существующим объектом
	 * 
	 * @return
	 */
	Set<FormDataEvent> getPermittedEvents(Integer userId, Integer declarationTemplateId, Integer departmentId, Integer reportPeriodId);
	

}
