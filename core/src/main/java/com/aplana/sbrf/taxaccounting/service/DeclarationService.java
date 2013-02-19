package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.*;

/**
 * Сервис для работы с {@link налоговыми декларациями Declaration}
 * @author dsultanbekov
 */
public interface DeclarationService {
	/**
	 * Создать декларацию в текущем отчётном периоде. Созданная декларация сразу же сохраняется в БД и возвращается идентификатор созданной записи.
	 * @param logger - объект журнала
	 * @param declarationTemplateId идентификатор шаблона декларации
	 * @param departmentId идентификатор подразделения, в котором создаваётся декларация
	 * @param userId идентификатор пользователя, выполняющего действие
	 * @param reportPeriodId идентификатор отчетного периода
	 * @return идентификатор созданной декларации
	 * @throws AccessDeniedException - если у пользователя нет прав на создание декларации с заданными параметрами
	 * 	ServiceException - если при создании декларации произошла ошибка (например декларация с такими параметрами уже существует)
	 */
	long createDeclaration(Logger logger, int declarationTemplateId, int departmentId, int userId, int reportPeriodId);
	
	/**
	 * Обновить декларацию (сформировать декларацию заново на основе данных, которые есть в БД)
	 * @param logger - объект журнала
	 * @param declarationId - идентификатор декларации
	 * @param userId - идентификатор пользователя, выполняющего операцию
	 */
	void refreshDeclaration(Logger logger, long declarationId, int userId);
	
	/**
	 * Получить декларацию
	 * @param declarationId
	 * @param userId идентификатор пользователя, выполняющего действие
	 * @return объект декларации
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	Declaration get(long declarationId, int userId);
	/**
	 * Установить в декларации флаг принятия
	 * @param declarationId идентификатор декларации
	 * @param accepted значение флага
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @throws AccessDeniedException - если у пользователя нет прав на такое изменение статуса у декларации
	 */
	void setAccepted(long declarationId, boolean accepted, int userId);
	/**
	 * Получить данные декларации в формате законодателя (XML)
	 * @param declarationId идентификатор декларации
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @return строка, содержащая данные декларации в формате законодателя
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	String getXmlData(long declarationId, int userId);
	/**
	 * Получить печатное представление данные декларации.
	 * Получается путём подстановки данных декларации в формате xml в Jasper-reports отчёт, шаблона декларации
	 * @param declarationId идентификатор декларации
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @return файл Xlsx в виде байтового массива
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	byte[] getXlsxData(long declarationId, int userId);

	/**
	 * Данный метод, вызывает FormDataDao#findPage() для выполнения запроса к базе по заданным параметрам фильтра.
	 * @param declarationFilter фильтр, по параметрам которого происходит поиск данных по декларациям
	 * @return список идентификаторов данных по декларациям, соответствующие критериям поиска.
	 */
	PaginatedSearchResult<DeclarationSearchResultItem> search(DeclarationFilter declarationFilter);
}
