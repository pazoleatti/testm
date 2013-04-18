package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.exception.*;
import com.aplana.sbrf.taxaccounting.log.*;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.*;

/**
 * Сервис для работы с {@link налоговыми декларациями DeclarationData}
 * @author dsultanbekov
 */
public interface DeclarationDataService {
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
	long create(Logger logger, int declarationTemplateId, int departmentId, int userId, int reportPeriodId);
	
	/**
	 * Обновить декларацию (сформировать декларацию заново на основе данных, которые есть в БД)
	 * @param logger - объект журнала
	 * @param declarationDataId - идентификатор декларации
	 * @param docDate - дата обновления декларации
	 * @param userId - идентификатор пользователя, выполняющего операцию
	 */
	void reCreate(Logger logger, long declarationDataId, int userId, String docDate);
	
	/**
	 * Получить декларацию
	 * @param declarationDataId
	 * @param userId идентификатор пользователя, выполняющего действие
	 * @return объект декларации
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	DeclarationData get(long declarationDataId, int userId);

	/**
	 * Удалить декларацию
	 * @param declarationDataId идентификатор декларации
	 * @param userId идентификатор пользователя, выполняющего действие
	 * @throws DaoException если такой декларации не существует
	 * @throws AccessDeniedException если у пользователя не хватает прав на удаление
	 */
	void delete(long declarationDataId, int userId);

	/**
	 * Установить в декларации флаг принятия
	 * @param logger - объект журнала
	 * @param declarationDataId идентификатор декларации
	 * @param accepted значение флага
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @throws AccessDeniedException - если у пользователя нет прав на такое изменение статуса у декларации
	 */
	void setAccepted(Logger logger, long declarationDataId, boolean accepted, int userId);
	/**
	 * Получить данные декларации в формате законодателя (XML)
	 * @param declarationDataId идентификатор декларации
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @return строка, содержащая данные декларации в формате законодателя
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	String getXmlData(long declarationDataId, int userId);
	/**
	 * Получить печатное представление данные декларации.
	 * Получается путём подстановки данных декларации в формате xml в Jasper-reports отчёт, шаблона декларации
	 * @param declarationDataId идентификатор декларации
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @return файл Xlsx в виде байтового массива
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	byte[] getXlsxData(long declarationDataId, int userId);
	/**
	 * Получить печатное представление данных декларации в PDF формате
	 * @param declarationId идентификатор декларации
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @return файл Pdf в виде байтового массива
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	byte[] getPdfData(long declarationId, int userId);
	/**
	 * Получить имя файла в формате законодателя
	 * @param declarationDataId идентификатор декларации
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @return имя файла взятого из xml данных
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	String getXmlDataFileName(long declarationDataId, int userId);
	/**
	 * Получить дату последнего изменения декларации
	 * @param declarationDataId идентификатор декларации
	 * @param userId идентификатор пользователя, выполняющего операцию
	 * @return дату последнего изменения декларации из xml данных
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	String getXmlDataDocDate(long declarationDataId, int userId);
}
