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
	
	
	
	/**
	 * Проверяет, имеет ли пользователь права на просмотр декларации.
	 * @param userId идентфикатор пользователя
	 * @param declarationDataId идентификатор декларации
	 * @return true если пользователь имеет права на просмотр декларации, false - в противном случае
	 */
	boolean canRead(int userId, long declarationDataId);
	
	/**
	 * Проверяет права пользователя для операции создания декларации.
	 * Данная проверка проверяет только полномочия пользователя, бизнес-проверок 
	 * (например, что декларация уже сформирована, или что не хватает каких-то данных не выполняется)
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @param declarationTemplateId идентификатор шаблона декларации
	 * @param departmentId идентификатор подразделения, в котором формируется декларация
	 * @param reportPeriodId идентфикатор отчётного периода, в котором формируется декларация
	 * @return true, если у пользователя есть права на создание декларации с указанными параметрами, false - в противном случае
	 */
	boolean canCreate(int userId, int declarationTemplateId, int departmentId, int reportPeriodId);
	
	/**
	 * Проверяет, может ли пользователь удалить декларацию
	 * @param userId идентификатор пользователя
	 * @param declarationDataId идентификатор декларации
	 * @return true - если у пользователя есть права на удаление декларации, false - в противном случае
	 */
	boolean canDelete(int userId, long declarationDataId);

	/**
	 * Проверяет, имеет ли пользователь права на обновление декларации
	 * @param userId идентификатор пользователя
	 * @param declarationDataId идентификатор декларации
	 * @return true если пользователь имеет права на обновление декларации, false - в противном случае
	 */
	boolean canRefresh(int userId, long declarationDataId);
	
	/**
	 * Проверяет, может ли пользователь скачать файл в формате законодателя (XML)
	 * @param userId идентификатор пользователя
	 * @param declarationDataId идентификатор декларации
	 * @return true - если у пользователя есть права на скачиваение XML декларации, false - в противном случае
	 */
	boolean canDownloadXml(int userId, long declarationDataId);
}
