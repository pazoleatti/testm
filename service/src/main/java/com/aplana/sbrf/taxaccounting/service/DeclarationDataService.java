package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.CreateResult;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRSwapFile;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с {@link DeclarationData налоговыми декларациями }
 *
 * @author dsultanbekov
 */
public interface DeclarationDataService {
    /**
     * Создание декларации в заданном отчетном периоде подразделения
     *
     * @param userInfo          Информация о текущем пользователе
     * @param declarationTypeId ID вида налоговой формы
     * @param departmentId      ID подразделения
     * @param periodId          ID отчетного периода
     * @return Модель {@link CreateResult}, в которой содержатся данные о результате операции
     */
    CreateResult<Long> create(TAUserInfo userInfo, Long declarationTypeId, Integer departmentId, Integer periodId);

    /**
     * Создание декларации в заданном отчетном периоде подразделения
     *
     * @param logger                 - объект журнала
     * @param declarationTemplateId  идентификатор шаблона декларации
     * @param userInfo               информация о пользователе, выполняющего действие
     * @param departmentReportPeriod отчетный период подразделения
     * @param taxOrganCode           налоговый орган (для налога на имущество)
     * @param taxOrganKpp            КПП (для налога на имущество)
     * @param oktmo                  ОКТМО, для НДФЛ
     * @param fileName
     * @param note
     * @return идентификатор созданной декларации
     */
    Long create(Logger logger, int declarationTemplateId, TAUserInfo userInfo,
                DepartmentReportPeriod departmentReportPeriod, String taxOrganCode, String taxOrganKpp, String oktmo,
                Long asunId, String fileName, String note, boolean writeAudit);

    /**
     * Рассчитать декларацию
     *
     * @param logger            - объект журнала
     * @param declarationDataId - идентификатор декларации
     * @param userInfo          - информация о пользователе, выполняющего операцию
     * @param docDate           - дата обновления декларации
     * @param stateLogger       - логгер для обновления статуса асинхронной задачи
     */
    void calculate(Logger logger, long declarationDataId, TAUserInfo userInfo, Date docDate, Map<String, Object> exchangeParams, LockStateLogger stateLogger);

    /**
     * Формирование Pdf отчета
     *
     * @param logger
     * @param declarationData
     * @param userInfo
     */
    void setPdfDataBlobs(Logger logger,
                         DeclarationData declarationData, TAUserInfo userInfo, LockStateLogger stateLogger);

    /**
     * Формирование Xlsx отчета
     *
     * @param logger
     * @param declarationData
     * @param userInfo
     */
    String setXlsxDataBlobs(Logger logger,
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
     *
     * @param logger
     * @param declarationData
     * @param ddReportType
     * @param userInfo
     * @param stateLogger
     * @return uuid записи с данными из таблицы BLOB_DATA
     */
    String createSpecificReport(Logger logger, DeclarationData declarationData, DeclarationDataReportType ddReportType, Map<String, Object> subreportParamValues, Map<String, String> viewParamValues, DataRow<Cell> selectedRecord, TAUserInfo userInfo, LockStateLogger stateLogger);

    /**
     * Подготовить данные для спец. отчета
     *
     * @param logger
     * @param declarationData
     * @param ddReportType
     * @param userInfo
     * @return предварительные результаты для формирования спец. отчета
     */
    PrepareSpecificReportResult prepareSpecificReport(Logger logger, DeclarationData declarationData, DeclarationDataReportType ddReportType, Map<String, Object> subreportParamValues, TAUserInfo userInfo);

    DeclarationData get(long declarationDataId, TAUserInfo userInfo);

    /**
     * Удалить декларацию, при этом создается блокировка
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @throws AccessDeniedException если у пользователя не хватает прав на удаление
     */
    void delete(long declarationDataId, TAUserInfo userInfo);

    /**
     * Удалить налоговую форму, если она существует, при этом создается блокировка
     *
     * @param declarationDataId Идентификатор налоговой формы
     * @param userInfo          Информация о пользователе, выполняющем действие
     * @throws AccessDeniedException если у пользователя не хватает прав на удаление
     */
    void deleteIfExists(long declarationDataId, TAUserInfo userInfo);

    /**
     * Удалить декларацию
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param createLock        если true, то создается блокировка, иначе блокировка не создается
     * @throws AccessDeniedException если у пользователя не хватает прав на удаление
     */
    void delete(long declarationDataId, TAUserInfo userInfo, boolean createLock);

    /**
     * Удалить все налоговые формы из списка
     *
     * @param userInfo           Информация о пользователе, выполняющем действие
     * @param declarationDataIds Список идентификаторов налоговых форм
     * @return Модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    ActionResult deleteDeclarationList(TAUserInfo userInfo, List<Long> declarationDataIds);

    /**
     * метод запускает скрипты с событием проверить
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param logger            - объект журнала
     * @throws AccessDeniedException если у пользователя не хватает прав на удаление
     */
    void check(Logger logger, long declarationDataId, TAUserInfo userInfo, LockStateLogger lockStateLogger);

    /**
     * Рассчитать декларацию
     *
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param declarationDataId идентификатор декларации
     * @param force             признак для перезапуска задачи
     * @param cancelTask        признак для отмены задачи
     * @return Модель {@link RecalculateDeclarationResult}, в которой содержатся данные о результате операции
     */
    RecalculateDeclarationResult recalculateDeclaration(TAUserInfo userInfo, long declarationDataId, boolean force, boolean cancelTask);

    /**
     * Рассчитать список налоговых форм
     *
     * @param userInfo           Информация о пользователе, выполняющем действие
     * @param declarationDataIds Список идентификаторов налоговых форм
     * @return Модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    ActionResult recalculateDeclarationList(TAUserInfo userInfo, List<Long> declarationDataIds);

    /**
     * Формирует DeclarationResult
     *
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param declarationDataId идентификатор декларации
     * @return модель {@link DeclarationResult}, в которой содержаться данные о декларации
     */
    DeclarationResult fetchDeclarationData(TAUserInfo userInfo, long declarationDataId);


    /**
     * Проверить декларацию
     *
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param declarationDataId идентификатор декларации
     * @param force             признак для перезапуска задачи
     * @return модель {@link CheckDeclarationResult}, в которой содержаться данные о результате проверки декларации
     */
    CheckDeclarationResult checkDeclaration(TAUserInfo userInfo, long declarationDataId, boolean force);

    /**
     * Проверить список деклараций
     *
     * @param userInfo           Информация о пользователе, выполняющем действие
     * @param declarationDataIds Список идентификаторов налоговых форм
     * @return Модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    ActionResult checkDeclarationList(TAUserInfo userInfo, List<Long> declarationDataIds);

    /**
     * Получение дополнительной информации о файлах декларации с комментариями
     *
     * @param declarationDataId идентификатор декларации
     * @return объект модели {@link DeclarationDataFileComment}, в которой содержаться данные о файлах
     * и комментарий для текущей декларации.
     */
    DeclarationDataFileComment fetchFilesComments(long declarationDataId);

    /**
     * Сохранение дополнительной информации о файлах декларации с комментариями
     *
     * @param dataFileComment сохраняемый объект декларации, в котором содержаться
     *                        данные о файлах и комментарий для текущей декларации.
     * @return новый объект модели {@link DeclarationDataFileComment}, в котором содержаться данные
     * о файлах и комментарий для текущей декларации.
     */
    DeclarationDataFileComment saveDeclarationFilesComment(TAUserInfo userInfo, DeclarationDataFileComment dataFileComment);


    /**
     * Получение источников и приемников декларации
     *
     * @param declarationDataId идентификатор декларации
     * @return источники и приемники декларации {@link Relation}
     */
    List<Relation> getDeclarationSourcesAndDestinations(TAUserInfo userInfo, long declarationDataId);

    /**
     * Получение списка налоговых форм
     *
     * @param pagingParams параметры для пагинации
     * @return список налоговых форм {@link DeclarationDataSearchResultItem}
     */
    PagingResult<DeclarationDataJournalItem> fetchDeclarations(TAUserInfo userInfo, DeclarationDataFilter filter, PagingParams pagingParams);

    /**
     * Формирование рну ндфл для отдельного физ лица`
     *
     * @param declarationDataId идентификатор декларации
     * @param personId          идентификатор физ лица
     * @param ndflPersonFilter  заполненные поля при поиске
     * @return источники и приемники декларации
     */
    CreateDeclarationReportResult createReportRnu(TAUserInfo userInfo, long declarationDataId, long personId, NdflPersonFilter ndflPersonFilter);

    /**
     * Формирование рну ндфл по всем физ лицам`
     *
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param declarationDataId идентификатор декларации
     * @param force             признак для перезапуска задачи
     * @return результат о формировании отчета
     */
    CreateDeclarationReportResult createReportAllRnu(TAUserInfo userInfo, final long declarationDataId, boolean force);

    /**
     * Формирование отчета в xlsx
     *
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param declarationDataId идентификатор декларации
     * @param force             признак для перезапуска задачи
     * @return результат о формировании отчета
     */
    CreateDeclarationReportResult createReportXlsx(TAUserInfo userInfo, long declarationDataId, boolean force);

    /**
     * Возвращает информацию о доступности скачивания отчетов
     *
     * @param userInfo          текущий пользователь
     * @param declarationDataId идентификатор декларации
     * @return информация о доступности скачивания отчетов
     */
    ReportAvailableResult checkAvailabilityReports(TAUserInfo userInfo, long declarationDataId);

    /**
     * метод запускает скрипты с событием предрасчетные проверки
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param logger            - объект журнала
     * @throws AccessDeniedException если у пользователя не хватает прав на удаление
     */
    void preCalculationCheck(Logger logger, long declarationDataId, TAUserInfo userInfo);

    /**
     * Принятие декларации
     *
     * @param logger            - объект журнала
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @throws AccessDeniedException - если у пользователя нет прав на такое изменение статуса у декларации
     */
    void accept(Logger logger, long declarationDataId, TAUserInfo userInfo, LockStateLogger lockStateLogger);

    /**
     * Принятие списка налоговых форм
     *
     * @param userInfo           Информация о пользователе, выполняющем действие
     * @param declarationDataIds Идентификатор налоговых форм
     * @return Модель {@link ActionResult}, в которой содержатся данные о результате операции
     * @throws AccessDeniedException - если у пользователя нет прав на такое изменение статуса у декларации
     */
    ActionResult acceptDeclarationList(TAUserInfo userInfo, List<Long> declarationDataIds);

    /**
     * Метод передающий управление на проверку декларации сторонней утилите
     *
     * @param userInfo
     * @param declarationData
     * @param logger
     * @param operation       - не используется
     * @param isErrorFatal
     * @param xmlFile
     * @param stateLogger
     */
    void validateDeclaration(TAUserInfo userInfo, DeclarationData declarationData, final Logger logger, final boolean isErrorFatal,
                             FormDataEvent operation, File xmlFile, String fileName, String xsdBlobDataId, LockStateLogger stateLogger);

    /**
     * Отмена принятия декларации
     *
     * @param logger            - объект журнала
     * @param declarationDataId идентификатор декларации
     * @param note              - причина возврата
     * @param userInfo          информация о пользователе, выполняющего действие
     * @throws AccessDeniedException - если у пользователя нет прав на такое изменение статуса у декларации
     */
    void cancel(Logger logger, long declarationDataId, String note, TAUserInfo userInfo);

    /**
     * Отмена принятия списка налоговых форм
     *
     * @param declarationDataIds Идентификаторы налоговых форм
     * @param note               Причина
     * @param userInfo           Информация о пользователе, выполняющем действие
     * @return Модель {@link ActionResult}, в которой содержатся данные о результате операции
     * @throws AccessDeniedException - если у пользователя нет прав на такое изменение статуса у декларации
     */
    ActionResult cancelDeclarationList(List<Long> declarationDataIds, String note, TAUserInfo userInfo);

    /**
     * Проверяет есть ли у формы приемники в состоянии Принята или Подготовлена
     *
     * @param declarationDataId - идентификатор декларации
     * @param logger            - объект журнала
     * @param userInfo          - информация о пользователе, выполняющего действие
     * @return - список ИД приемников  в состоянии Принята или Подготовлена
     */
    public List<Long> getReceiversAcceptedPrepared(long declarationDataId, Logger logger, TAUserInfo userInfo);

    /**
     * Получить данные декларации в формате законодателя (XML)
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @return поток с данными
     * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
     */
    InputStream getXmlDataAsStream(long declarationDataId, TAUserInfo userInfo);

    /**
     * Получить печатное представление данных декларации в PDF формате
     *
     * @param declarationId идентификатор декларации
     * @param userInfo      информация о пользователе, выполняющего действие
     * @return файл Pdf в виде байтового массива
     * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
     */
    InputStream getPdfDataAsStream(long declarationId, TAUserInfo userInfo);

    /**
     * Получить имя файла в формате законодателя
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @return имя файла взятого из xml данных
     * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
     */
    String getXmlDataFileName(long declarationDataId, TAUserInfo userInfo);

    /**
     * Получить дату последнего изменения декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @return дату последнего изменения декларации из xml данных
     * @throws AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
     */
    LocalDateTime getXmlDataDocDate(long declarationDataId, TAUserInfo userInfo);

    /**
     * Поиск декларации
     *
     * @param declarationTypeId      Тип декларации
     * @param departmentReportPeriod Отчетный период подразделения
     * @return
     */
    DeclarationData find(int declarationTypeId, Long departmentReportPeriod, String kpp, String oktmo, String taxOrganCode, Long asnuId, String fileName);

    List<Long> getFormDataListInActualPeriodByTemplate(int declarationTemplateId, Date startDate);

    /**
     * Проверить наличие форм декларации
     *
     * @param declarationTypeId идентификатор типа декларации
     * @param departmentId      подразделение
     * @param logs              записи лога
     * @return наличие форм декларации
     */
    boolean existDeclaration(int declarationTypeId, int departmentId, List<LogEntry> logs);

    /**
     * Генерация ключа блокировки для асинхронных задач по декларациям
     *
     * @param declarationDataId
     * @param type
     * @return код блокировки
     */
    String generateAsyncTaskKey(long declarationDataId, DeclarationDataReportType type);

    /**
     * Генерация ключа блокировки для асинхронных задач по декларациям
     *
     * @return код блокировки
     */
    String generateAsyncTaskKey(int declarationTypeId, int reportPeriodId, int departmentId);

    /**
     * Заблокировать DeclarationData.
     *
     * @param declarationDataId - идентификатор декларации
     * @param userInfo          информация о пользователе
     */
    LockData lock(long declarationDataId, TAUserInfo userInfo);

    /**
     * Снять блокировку с DeclarationData.
     *
     * @param declarationDataId - идентификатор декларации
     * @param userInfo          информация о пользователе
     */
    void unlock(long declarationDataId, TAUserInfo userInfo);

    /**
     * Проверяет, не заблокирована ли декларация другим пользователем
     *
     * @param declarationDataId - идентификатор декларации
     * @param userInfo          - информация о пользователе
     */
    void checkLockedMe(Long declarationDataId, TAUserInfo userInfo);

    /**
     * Удаление отчетов и блокировок на задачи формирования отчетов связанных с декларациями
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация пользователя
     * @param cause             причина остановки задачи
     */
    void deleteReport(long declarationDataId, TAUserInfo userInfo, boolean isCalc, TaskInterruptCause cause);

    void findDDIdsByRangeInReportPeriod(int decTemplateId, Date startDate, Date endDate, Logger logger);

    Long getTaskLimit(AsyncTaskType reportType);

    Long getValueForCheckLimit(TAUserInfo userInfo, long declarationDataId, DeclarationDataReportType reportType);

    /**
     * Возвращает полное название декларации с указанием подразделения, периода и прочего
     *
     * @param declarationId идентификатор декларации
     * @param ddReportType  тип отчета. Может быть null
     * @return название
     */
    String getDeclarationFullName(long declarationId, DeclarationDataReportType ddReportType, String... args);
    /**
     * Возвращает полное название декларации с указанием подразделения, периода и прочего
     *
     * @param declarationTypeId тип декларации
     * @param departmentReportPeriodId  идентификатор отчетного периода привязанного к подразделению
     * @param taskType  тип асинхронной задачи, формирующей имя
     * @return название
     */
    String getDeclarationFullName(int declarationTypeId, int departmentReportPeriodId, AsyncTaskType taskType);

    /**
     * Проверяет существование операции, по которым требуется удалить блокировку
     *
     * @param declarationDataId
     * @param reportType
     * @param logger
     * @return
     */
    boolean checkExistAsyncTask(long declarationDataId, AsyncTaskType reportType, Logger logger);

    /**
     * Отмена операции, по которым требуется удалить блокировку(+удаление отчетов)
     *
     * @param declarationDataId
     * @param userInfo
     * @param reportType
     * @param cause             причина остановки задачи
     */
    void interruptAsyncTask(long declarationDataId, TAUserInfo userInfo, AsyncTaskType reportType, TaskInterruptCause cause);

    /**
     * Метод для очитски blob-ов у деклараций.
     * Применяется в случае удаления jrxml макета декларации.
     *
     * @param ids         идентификаторы деклараций
     * @param reportTypes типы отчетов, которые надо удалить
     */
    void cleanBlobs(Collection<Long> ids, List<DeclarationDataReportType> reportTypes);

    /**
     * Формирование jasper-отчета
     *
     * @param xmlIn      поток данных xml
     * @param jrxml      текст jrxml
     * @param jrSwapFile
     * @param params
     * @return
     */
    JasperPrint createJasperReport(InputStream xmlIn, String jrxml, JRSwapFile jrSwapFile, Map<String, Object> params);


    /**
     * Формирование jasper-отчета, данный метод не использует JRSwapFile для промежуточного хранения данных, вместо этого данные загружаются в буффер
     *
     * @param xmlData       xml поток входных данных
     * @param jrxmlTemplate шаблон отчета
     * @param params        набор параметров отчета
     * @return сформированный отчет
     */
    JasperPrint createJasperReport(InputStream xmlData, InputStream jrxmlTemplate, Map<String, Object> params);


    /**
     * Формирование jasper-отчета, расширенный вариант метода
     * {@link DeclarationDataService#createJasperReport(InputStream, InputStream, Map)}
     */
    JasperPrint createJasperReport(InputStream xmlIn, InputStream jrxml, Map<String, Object> parameters, Connection connection);

    /**
     * Получить соединение для передачи в отчет
     *
     * @return
     */
    Connection getReportConnection();

    /**
     * Формирование PDF отчета
     *
     * @param jasperPrint
     * @param data
     */
    void exportPDF(JasperPrint jasperPrint, OutputStream data);

    /**
     * Формирование XLSX отчета
     *
     * @param jasperPrint
     * @param data
     */
    void exportXLSX(JasperPrint jasperPrint, OutputStream data);

    /**
     * Получение возможности отображения формы предварительного просмотра
     *
     * @param declarationData
     * @param userInfo
     * @return
     */
    boolean isVisiblePDF(DeclarationData declarationData, TAUserInfo userInfo);

    /**
     * Импорт ТФ Декларации
     */
    void importDeclarationData(Logger logger, TAUserInfo userInfo, long declarationDataId, InputStream is,
                               String fileName, FormDataEvent formDataEvent, LockStateLogger stateLogger, File dataFile,
                               AttachFileType fileType, LocalDateTime createDateFile);

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

    /**
     * Создание экземпляров форм
     *
     * @param departmentReportPeriod - отчетный период
     * @param declarationTypeId      идентификатор типа декларации
     */
    public void createForms(Logger logger, TAUserInfo userInfo, DepartmentReportPeriod departmentReportPeriod, int declarationTypeId, LockStateLogger stateLogger);

    /**
     * Создание отчетности для выгрузки
     *
     * @param departmentReportPeriod - отчетный период
     * @param declarationTypeId      идентификатор типа декларации
     */
    public String createReports(Logger logger, TAUserInfo userInfo, DepartmentReportPeriod departmentReportPeriod, int declarationTypeId, LockStateLogger stateLogger);

    /**
     * Изменение Состояния ЭД
     *
     * @param logger
     * @param userInfo
     * @param declarationDataId
     * @param docStateId
     */
    void changeDocState(Logger logger, TAUserInfo userInfo, long declarationDataId, Long docStateId);

    /**
     * Проверка существования формы
     *
     * @param declarationDataId
     * @return
     */
    boolean existDeclarationData(long declarationDataId);

    /**
     * Получает мапу созданных блокировок по основным операциям формы
     *
     * @param declarationDataId
     * @return
     */
    Map<DeclarationDataReportType, LockData> getLockTaskType(long declarationDataId);

    /**
     * Создание отчётности
     *
     * @param userInfo          информация о пользователе
     * @param declarationTypeId идентификатор типа отчётности
     * @param departmentId      идентификатор подразделения
     * @param periodId          идентификатор периода
     * @return
     */
    CreateDeclarationReportResult createReports(TAUserInfo userInfo, Integer declarationTypeId, Integer departmentId, Integer periodId);

    /**
     * Создает задачу на принятии налоговой формы, перед созданием задачи выполняются необходимые проверки
     * @param userInfo
     * @param declarationDataId
     * @param force если true, то удаляем старую задачу(и оправляем оповещения подписавщимся пользователям), иначе, если задача уже запущена, вызываем диалог
     * @param cancelTask если true, то удаляем задачи, которые должны удаляться при запуске текущей, иначе, если есть такие задачи, вызываем диалог
     */
    AcceptDeclarationResult createAcceptDeclarationTask(TAUserInfo userInfo, final long declarationDataId, final boolean force, final boolean cancelTask);

    /**
     * Выгрузка отчетности
     * @param userInfo
     * @return
     */
    ActionResult downloadReports(TAUserInfo userInfo, List<Long> declarationDataIdList);
}
