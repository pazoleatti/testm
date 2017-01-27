package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRSwapFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
	 * @param oktmo ОКТМО, для НДФЛ
     * @return идентификатор созданной декларации
     */
    Long create(Logger logger, int declarationTemplateId, TAUserInfo userInfo,
                DepartmentReportPeriod departmentReportPeriod, String taxOrganCode, String taxOrganKpp, String oktmo,
                Long asunId, String guid);

	/**
	 * Рассчитать декларацию
     * @param logger - объект журнала
     * @param declarationDataId - идентификатор декларации
     * @param userInfo - информация о пользователе, выполняющего операцию
     * @param docDate - дата обновления декларации
     * @param stateLogger - логгер для обновления статуса асинхронной задачи
     */
	void calculate(Logger logger, long declarationDataId, TAUserInfo userInfo, Date docDate, LockStateLogger stateLogger);

    /**
     * Формирование Pdf отчета
     * @param logger
     * @param declarationData
     * @param userInfo
     */
    void setPdfDataBlobs(Logger logger,
                         DeclarationData declarationData, TAUserInfo userInfo, LockStateLogger stateLogger);
    /**
     * Формирование Xlsx отчета
     * @param logger
     * @param declarationData
     * @param userInfo
     */
    void setXlsxDataBlobs(Logger logger,
                         DeclarationData declarationData, TAUserInfo userInfo, LockStateLogger stateLogger);
	/**
	 * Получить декларацию
	 * @param declarationDataId идентификатор декларации
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @return объект декларации
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */

    /**
     * Формирование специфичного отчета декларации
     * @param logger
     * @param declarationData
     * @param ddReportType
     * @param userInfo
     * @param stateLogger
     * @return uuid записи с данными из таблицы BLOB_DATA
     */
    String createSpecificReport(Logger logger, DeclarationData declarationData, DeclarationDataReportType ddReportType, Map<String, Object> subreportParamValues, TAUserInfo userInfo, LockStateLogger stateLogger);

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
	void check(Logger logger, long declarationDataId, TAUserInfo userInfo, LockStateLogger lockStateLogger);

    /**
     * метод запускает скрипты с событием предрасчетные проверки
     * @param declarationDataId идентификатор декларации
     * @param userInfo информация о пользователе, выполняющего действие
     * @param logger - объект журнала
     * @throws AccessDeniedException если у пользователя не хватает прав на удаление
     */
    void preCalculationCheck(Logger logger, long declarationDataId, TAUserInfo userInfo);

	/**
	 * Принятие декларации
	 * @param logger - объект журнала
	 * @param declarationDataId идентификатор декларации
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @throws AccessDeniedException - если у пользователя нет прав на такое изменение статуса у декларации
	 */
    void accept(Logger logger, long declarationDataId, TAUserInfo userInfo, LockStateLogger lockStateLogger);


    /**
     * Отмена принятия декларации
     * @param logger - объект журнала
     * @param declarationDataId идентификатор декларации
     * @param userInfo информация о пользователе, выполняющего действие
     * @throws AccessDeniedException - если у пользователя нет прав на такое изменение статуса у декларации
     */
    void cancel(Logger logger, long declarationDataId, TAUserInfo userInfo);

    /**
     * Получить данные декларации в формате законодателя (XML)
     * @param declarationDataId идентификатор декларации
     * @param userInfo информация о пользователе, выполняющего действие
     * @return поток с данными
     * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
     */
    InputStream getXmlDataAsStream(long declarationDataId, TAUserInfo userInfo);
	/**
	 * Получить печатное представление данных декларации в PDF формате
	 * @param declarationId идентификатор декларации
	 * @param userInfo информация о пользователе, выполняющего действие
	 * @return файл Pdf в виде байтового массива
	 * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
	 */
    InputStream getPdfDataAsStream(long declarationId, TAUserInfo userInfo);
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
    DeclarationData find(int declarationTypeId, int departmentReportPeriod, String kpp, String oktmo, String taxOrganCode, Long asnuId, String fileName);

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
     * Генерация ключа блокировки для асинхронных задач по декларациям
     * @param declarationDataId
     * @param type
     * @return код блокировки
     */
    String generateAsyncTaskKey(long declarationDataId, DeclarationDataReportType type);

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
	 * @param declarationDataId идентификатор декларации
	 * @param userInfo информация пользователя
	 * @param cause причина остановки задачи
	 */
    void deleteReport(long declarationDataId, TAUserInfo userInfo, boolean isCalc, TaskInterruptCause cause);

    void findDDIdsByRangeInReportPeriod(int decTemplateId, Date startDate, Date endDate, Logger logger);

    Long getTaskLimit(ReportType reportType);

    Long getValueForCheckLimit(TAUserInfo userInfo, long declarationDataId, DeclarationDataReportType reportType);

    /**
     * Возвращает полное название декларации с указанием подразделения, периода и прочего
     * @param declarationId идентификатор декларации
     * @param ddReportType тип отчета. Может быть null
     * @return название
     */
    String getDeclarationFullName(long declarationId, DeclarationDataReportType ddReportType, String... args);

    /**
     * Проверяет существование операции, по которым требуется удалить блокировку
     * @param declarationDataId
     * @param reportType
     * @param logger
     * @return
     */
    boolean checkExistTask(long declarationDataId, ReportType reportType, Logger logger);

    /**
     * Отмена операции, по которым требуется удалить блокировку(+удаление отчетов)
	 * @param declarationDataId
     * @param userInfo
	 * @param reportType
	 * @param cause причина остановки задачи
	 */
    void interruptTask(long declarationDataId, TAUserInfo userInfo, ReportType reportType, TaskInterruptCause cause);

    /**
     * Метод для очитски blob-ов у деклараций.
     * Применяется в случае удаления jrxml макета декларации.
     * @param ids идентификаторы деклараций
     * @param reportTypes типы отчетов, которые надо удалить
     */
    void cleanBlobs(Collection<Long> ids, List<DeclarationDataReportType> reportTypes);

    /**
     * Формирует название операции
     * @param ddReportType
     * @param taxType
     * @return
     */
    String getTaskName(DeclarationDataReportType ddReportType, TaxType taxType);

    /**
     * Формирование jasper-отчета
     * @param xmlIn поток данных xml
     * @param jrxml текст jrxml
     * @param jrSwapFile
     * @param params
     * @return
     */
    JasperPrint createJasperReport(InputStream xmlIn, String jrxml, JRSwapFile jrSwapFile, Map<String, Object> params);

    /**
     * Формирование PDF отчета
     * @param jasperPrint
     * @param data
     */
    void exportPDF(JasperPrint jasperPrint, OutputStream data);

    /**
     * Формирование XLSX отчета
     * @param jasperPrint
     * @param data
     */
    void exportXLSX(JasperPrint jasperPrint, OutputStream data);

    /**
     * Получение возможности отображения формы предварительного просмотра
     * @param declarationData
     * @param userInfo
     * @return
     */
    boolean isVisiblePDF(DeclarationData declarationData, TAUserInfo userInfo);

    /**
     * Импорт ТФ Декларации
     */
    void importDeclarationData(Logger logger, TAUserInfo userInfo, long declarationDataId, InputStream is,
                        String fileName, FormDataEvent formDataEvent, LockStateLogger stateLogger, String lock);

    /**
     * Получение данных по файлам для формы "Файлы и комментарии"
     */
    List<DeclarationDataFile> getFiles(long declarationDataId);

    /**
     * Получения комментария для формы "Файлы и комментарии"
     */
    String getNote(long declarationDataId);

    /**
     * Сохранение данных формы "Файлы и комментарии"
     */
    void saveFilesComments(long declarationDataId, String note, List<DeclarationDataFile> files);
}
