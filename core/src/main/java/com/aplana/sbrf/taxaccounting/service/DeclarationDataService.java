package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.exception.*;
import com.aplana.sbrf.taxaccounting.log.*;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.*;

import java.util.Date;

/**
 * Сервис для работы с {@link DeclarationData налоговыми декларациями }
 * @author dsultanbekov
 */
public interface DeclarationDataService {
	/**
	 * Создать декларацию в текущем отчётном периоде. Созданная декларация сразу же сохраняется в БД и возвращается идентификатор созданной записи.
	 * @param logger - объект журнала
	 * @param declarationTemplateId идентификатор шаблона декларации
	 * @param departmentId идентификатор подразделения, в котором создаваётся декларация
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @param reportPeriodId идентификатор отчетного периода
	 * @return идентификатор созданной декларации
	 * @throws AccessDeniedException - если у пользователя нет прав на создание декларации с заданными параметрами
	 * 	ServiceException - если при создании декларации произошла ошибка (например декларация с такими параметрами уже существует)
	 */
	long create(Logger logger, int declarationTemplateId, int departmentId, TAUserInfo userInfo, int reportPeriodId);
	
	/**
	 * Обновить декларацию (сформировать декларацию заново на основе данных, которые есть в БД)
	 * @param logger - объект журнала
	 * @param declarationDataId - идентификатор декларации
	 * @param docDate - дата обновления декларации
	 * @param userInfo - информация о пользователе, выполняющего операцию
	 */
	void reCreate(Logger logger, long declarationDataId, TAUserInfo userInfo, Date docDate);
	
	/**
	 * Получить декларацию
	 * @param declarationDataId
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @return объект декларации
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	DeclarationData get(long declarationDataId, TAUserInfo userInfo);

	/**
	 * Удалить декларацию
	 * @param declarationDataId идентификатор декларации
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @throws DaoException если такой декларации не существует
	 * @throws AccessDeniedException если у пользователя не хватает прав на удаление
	 */
	void delete(long declarationDataId, TAUserInfo userInfo);

	/**
	 * метод запускает скрипты с событием проверить
	 * @param declarationDataId идентификатор декларации
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @param logger - объект журнала
	 * @throws DaoException если такой декларации не существует
	 * @throws AccessDeniedException если у пользователя не хватает прав на удаление
	 */
	void check(Logger logger, long declarationDataId, TAUserInfo userInfo);

	/**
	 * Установить в декларации флаг принятия
	 * @param logger - объект журнала
	 * @param declarationDataId идентификатор декларации
	 * @param accepted значение флага
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @throws AccessDeniedException - если у пользователя нет прав на такое изменение статуса у декларации
	 */
	void setAccepted(Logger logger, long declarationDataId, boolean accepted, TAUserInfo userInfo);
	/**
	 * Получить данные декларации в формате законодателя (XML)
	 * @param declarationDataId идентификатор декларации
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @return строка, содержащая данные декларации в формате законодателя
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	String getXmlData(long declarationDataId, TAUserInfo userInfo);
	/**
	 * Получить печатное представление данные декларации.
	 * Получается путём подстановки данных декларации в формате xml в Jasper-reports отчёт, шаблона декларации
	 * @param declarationDataId идентификатор декларации
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @return файл Xlsx в виде байтового массива
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	byte[] getXlsxData(long declarationDataId, TAUserInfo userInfo);
	/**
	 * Получить печатное представление данных декларации в PDF формате
	 * @param declarationId идентификатор декларации
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @return файл Pdf в виде байтового массива
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	byte[] getPdfData(long declarationId, TAUserInfo userInfo);
	/**
	 * Получить имя файла в формате законодателя
	 * @param declarationDataId идентификатор декларации
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @return имя файла взятого из xml данных
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	String getXmlDataFileName(long declarationDataId, TAUserInfo userInfo);
	/**
	 * Получить дату последнего изменения декларации
	 * @param declarationDataId идентификатор декларации
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @return дату последнего изменения декларации из xml данных
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	Date getXmlDataDocDate(long declarationDataId, TAUserInfo userInfo);
}
