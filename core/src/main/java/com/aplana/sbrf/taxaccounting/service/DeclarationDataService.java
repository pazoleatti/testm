package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.Date;
import java.util.List;

/**
 * Сервис для работы с {@link DeclarationData налоговыми декларациями }
 * @author dsultanbekov
 */
public interface DeclarationDataService {
    /**
     * Создание декларации в заданном отчетном периоде подразделения
     * @param logger - объект журнала
     * @param declarationTemplateId идентификатор шаблона декларации
     * @param userInfo информация о пользователе, выполняющего действие
     * @param departmentReportPeriod отчетный период подразделения
     * @param taxOrganCode налоговый орган (для налога на имущество)
     * @param taxOrganKpp КПП (для налога на имущество)
     * @return идентификатор созданной декларации
     */
    long create(Logger logger, int declarationTemplateId, TAUserInfo userInfo,
                DepartmentReportPeriod departmentReportPeriod, String taxOrganCode, String taxOrganKpp);

	/**
	 * Рассчитать декларацию
     * @param logger - объект журнала
     * @param declarationDataId - идентификатор декларации
     * @param userInfo - информация о пользователе, выполняющего операцию
     * @param docDate - дата обновления декларации
     */
	void calculate(Logger logger, long declarationDataId, TAUserInfo userInfo, Date docDate);
	
	/**
	 * Получить декларацию
	 * @param declarationDataId идентификатор декларации
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @return объект декларации
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
	DeclarationData get(long declarationDataId, TAUserInfo userInfo);

	/**
	 * Удалить декларацию
	 * @param declarationDataId идентификатор декларации
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @throws AccessDeniedException если у пользователя не хватает прав на удаление
	 */
	void delete(long declarationDataId, TAUserInfo userInfo);

	/**
	 * метод запускает скрипты с событием проверить
	 * @param declarationDataId идентификатор декларации
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @param logger - объект журнала
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

    /**
     * Поиск декларации
     * @param declarationTypeId Тип декларации
     * @param departmentReportPeriod Отчетный период подразделения
     * @return
     */
    DeclarationData find(int declarationTypeId, int departmentReportPeriod);

    List<Long> getFormDataListInActualPeriodByTemplate(int declarationTemplateId, Date startDate);

    /**
     * Проверить наличие форм декларации
     * @param declarationTypeId идентификатор типа декларации
     * @param departmentId подразделение
     * @param logs записи лога
     * @return наличие форм декларации
     */
    boolean existDeclaration(int declarationTypeId, int departmentId, List<LogEntry> logs);

    /**
     * Заблокировать DeclarationData.
     * @param declarationDataId - идентификатор декларации
     * @param userInfo информация о пользователе
     */
    LockData lock(long declarationDataId, TAUserInfo userInfo);

    /**
     * Снять блокировку с DeclarationData.
     * @param declarationDataId - идентификатор декларации
     * @param userInfo информация о пользователе
     * */
    void unlock(long declarationDataId, TAUserInfo userInfo);

    /**
     * Проверяет, не заблокирована ли декларация другим пользователем
     * @param declarationDataId - идентификатор декларации
     * @param userInfo - информация о пользователе
     */
    void checkLockedMe(Long declarationDataId, TAUserInfo userInfo);

    /**
     * Удаление отчетов и блокировок на задачи формирования отчетов связанных с декларациями
     * @param declarationDataId
     */
    void deleteReport(long declarationDataId, boolean isLock);
}
