package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.CreateDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.model.action.CreateDeclarationReportAction;
import com.aplana.sbrf.taxaccounting.model.action.CreateReportAction;
import com.aplana.sbrf.taxaccounting.model.action.PrepareSubreportAction;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.filter.NdflPersonFilter;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.*;
import com.aplana.sbrf.taxaccounting.permissions.logging.TargetIdAndLogger;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRSwapFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с {@link DeclarationData налоговыми декларациями }
 */
public interface DeclarationDataService {
    /**
     * Создание декларации в заданном отчетном периоде подразделения
     *
     * @param userInfo информация о текущем пользователе
     * @param action   объект с параметрами для создания налоговой формы
     * @return Модель {@link CreateResult}, в которой содержатся данные о результате операции
     */
    CreateResult<Long> create(TAUserInfo userInfo, CreateDeclarationDataAction action);

    /**
     * Создание декларации в заданном отчетном периоде подразделения
     *
     * @param newDeclaration         данные формы
     * @param departmentReportPeriod отчетный период подразделения
     * @param logger                 объект журнала
     * @param userInfo               информация о пользователе, выполняющего действие
     * @param writeAudit             надо ли писать в ЖА
     * @return идентификатор созданной декларации
     */
    Long create(DeclarationData newDeclaration, DepartmentReportPeriod departmentReportPeriod, Logger logger, TAUserInfo userInfo, boolean writeAudit);

    /**
     * Идентифицировать ФЛ
     *
     * @param targetIdAndLogger объект журнала
     * @param userInfo          информация о пользователе, выполняющего операцию
     * @param docDate           дата обновления декларации
     * @param stateLogger       логгер для обновления статуса асинхронной задачи
     */
    void identify(TargetIdAndLogger targetIdAndLogger, TAUserInfo userInfo, Date docDate, Map<String, Object> exchangeParams, LockStateLogger stateLogger);

    /**
     * Консолидировать НФ
     *
     * @param targetIdAndLogger объект журнала
     * @param userInfo          информация о пользователе, выполняющего операцию
     * @param docDate           дата обновления декларации
     * @param stateLogger       логгер для обновления статуса асинхронной задачи
     */
    void consolidate(TargetIdAndLogger targetIdAndLogger, TAUserInfo userInfo, Date docDate, Map<String, Object> exchangeParams, LockStateLogger stateLogger);

    /**
     * Формирование Pdf отчета
     */
    void setPdfDataBlobs(Logger logger,
                         DeclarationData declarationData, TAUserInfo userInfo, LockStateLogger stateLogger);

    /**
     * Формирование Xlsx отчета
     */
    String setXlsxDataBlobs(Logger logger,
                            DeclarationData declarationData, TAUserInfo userInfo, LockStateLogger stateLogger);

    /**
     * Формирование специфичного отчета декларации
     *
     * @return uuid записи с данными из таблицы BLOB_DATA
     */
    String createSpecificReport(Logger logger, DeclarationData declarationData, DeclarationDataReportType ddReportType, Map<String, Object> subreportParamValues, Map<String, String> viewParamValues, DataRow<Cell> selectedRecord, TAUserInfo userInfo, LockStateLogger stateLogger);

    /**
     * Подготовить данные для спец. отчета
     *
     * @return предварительные результаты для формирования спец. отчета
     */
    PrepareSpecificReportResult prepareSpecificReport(Logger logger, DeclarationData declarationData, DeclarationDataReportType ddReportType, Map<String, Object> subreportParamValues, TAUserInfo userInfo);

    /**
     * Получить декларацию
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          пользователь
     * @return объекты декларации
     */
    DeclarationData get(long declarationDataId, TAUserInfo userInfo);

    /**
     * Получить список деклараций
     *
     * @param ids идентификаторы декларации
     * @return объекты декларации
     */
    List<DeclarationData> get(List<Long> ids);

    /**
     * Удаление налоговой формы без использования асинхронной задачи
     *
     * @param declarationDataId идентификатор НФ
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param createLock        флаг указывающий создавать ли блокировку
     */
    void deleteSync(long declarationDataId, TAUserInfo userInfo, boolean createLock);

    /**
     * Удалить все налоговые формы из списка
     *
     * @param userInfo           информация о пользователе, выполняющем действие
     * @param declarationDataIds список идентификаторов налоговых форм
     * @return модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    ActionResult createDeleteDeclarationDataTask(TAUserInfo userInfo, List<Long> declarationDataIds);

    /**
     * Метод запускает скрипты с событием проверить
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param logger            объект журнала
     * @throws AccessDeniedException если у пользователя не хватает прав на удаление
     */
    void check(Logger logger, long declarationDataId, TAUserInfo userInfo, LockStateLogger lockStateLogger);

    /**
     * Идентифицировать ФЛ в списке налоговых форм
     *
     * @param userInfo           информация о пользователе, выполняющем действие
     * @param declarationDataIds список идентификаторов налоговых форм
     * @return модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    ActionResult createIdentifyDeclarationDataTask(TAUserInfo userInfo, List<Long> declarationDataIds);

    /**
     * Консолидировать список налоговых форм
     *
     * @param userInfo           информация о пользователе, выполняющем действие
     * @param declarationDataIds список идентификаторов налоговых форм
     * @return модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    ActionResult consolidateDeclarationDataList(final TAUserInfo userInfo, List<Long> declarationDataIds);

    /**
     * Формирует DeclarationResult
     *
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param declarationDataId идентификатор декларации
     * @return модель {@link DeclarationResult}, в которой содержаться данные о декларации
     */
    DeclarationResult fetchDeclarationData(TAUserInfo userInfo, long declarationDataId);

    /**
     * Найти все формы созданные в отчетном периоде
     */
    List<DeclarationData> findAllDeclarationData(int declarationTypeId, int departmentId, int reportPeriodId);

    /**
     * Проверить список деклараций
     *
     * @param userInfo           информация о пользователе, выполняющем действие
     * @param declarationDataIds список идентификаторов налоговых форм
     * @return модель {@link ActionResult}, в которой содержатся данные о результате операции
     */
    ActionResult createCheckDeclarationDataTask(TAUserInfo userInfo, List<Long> declarationDataIds);

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
     * @param ndflPersonId      идентификатор данных о физическом лице {@link com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson}
     * @param ndflPersonFilter  заполненные поля при поиске
     * @return источники и приемники декларации
     */
    CreateDeclarationReportResult createReportRnu(TAUserInfo userInfo, long declarationDataId, long ndflPersonId, NdflPersonFilter ndflPersonFilter);

    /**
     * Формирование спецотчета по форме
     *
     * @param declarationDataId идентификатор декларации
     * @param alias             вид спецотчета (subreport)
     * @param reportParams            параметры для формирования отчета
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param force             признак для перезапуска задачи
     * @return результат о формировании отчета
     */
    CreateDeclarationReportResult createTaskToCreateSpecificReport(final long declarationDataId, String alias, Map<String, Object> reportParams, TAUserInfo userInfo, boolean force);

    /**
     * Формирование Реестра сформированной отчетности
     *
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param declarationDataId идентификатор декларации
     * @param force             признак для перезапуска задачи
     * @return результат с даннымми для представления об операции формирования отчета
     */
    CreateDeclarationReportResult createPairKppOktmoReport(TAUserInfo userInfo, long declarationDataId, boolean force);

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
     * Проверка возможности доступности отчетов для отчетной НФ
     *
     * @param userInfo          текущий пользователь
     * @param declarationDataId идентификатор декларации
     * @return результат с даннымми для представления о доступности отчетов для отчетной НФ
     */
    ReportAvailableReportDDResult checkAvailabilityReportDD(TAUserInfo userInfo, long declarationDataId);

    /**
     * Метод запускает скрипты с событием предрасчетные проверки
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @param logger            объект журнала
     * @throws AccessDeniedException если у пользователя не хватает прав на удаление
     */
    void preCalculationCheck(Logger logger, long declarationDataId, TAUserInfo userInfo);

    /**
     * Принятие декларации
     *
     * @param logger            объект журнала
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @throws AccessDeniedException если у пользователя нет прав на такое изменение статуса у декларации
     */
    void accept(Logger logger, long declarationDataId, TAUserInfo userInfo, LockStateLogger lockStateLogger);

    /**
     * Принятие списка налоговых форм
     *
     * @param userInfo           информация о пользователе, выполняющем действие
     * @param declarationDataIds идентификатор налоговых форм
     * @return модель {@link ActionResult}, в которой содержатся данные о результате операции
     * @throws AccessDeniedException если у пользователя нет прав на такое изменение статуса у декларации
     */
    ActionResult createAcceptDeclarationDataTask(TAUserInfo userInfo, List<Long> declarationDataIds);

    /**
     * Метод, передающий управление на проверку декларации сторонней утилите
     */
    void validateDeclaration(DeclarationData declarationData,
                             final Logger logger,
                             File xmlFile,
                             String fileName,
                             String xsdBlobDataId);

    /**
     * Отмена принятия списка налоговых форм
     *
     * @param declarationDataIds идентификаторы налоговых форм
     * @param note               причина
     * @param userInfo           информация о пользователе, выполняющем действие
     */
    void cancelDeclarationList(List<Long> declarationDataIds, String note, TAUserInfo userInfo);

    /**
     * Получить данные декларации в формате законодателя (XML)
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @return поток с данными
     * @throws AccessDeniedException если у пользователя нет прав на просмотр данной декларации
     */
    InputStream getXmlDataAsStream(long declarationDataId, TAUserInfo userInfo);

    /**
     * Получить печатное представление данных декларации в PDF формате
     *
     * @param declarationId идентификатор декларации
     * @param userInfo      информация о пользователе, выполняющего действие
     * @return файл Pdf в виде байтового массива
     * @throws AccessDeniedException если у пользователя нет прав на просмотр данной декларации
     */
    InputStream getPdfDataAsStream(long declarationId, TAUserInfo userInfo);

    /**
     * Получить имя файла в формате законодателя
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @return имя файла взятого из xml данных
     * @throws AccessDeniedException если у пользователя нет прав на просмотр данной декларации
     */
    String getXmlDataFileName(long declarationDataId, TAUserInfo userInfo);

    /**
     * Получить дату последнего изменения декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @return дату последнего изменения декларации из xml данных
     * @throws AccessDeniedException если у пользователя нет прав на просмотр данной декларации
     */
    Date getXmlDataDocDate(long declarationDataId, TAUserInfo userInfo);

    /**
     * Поиск декларации
     *
     * @param declarationTypeId      тип декларации
     * @param departmentReportPeriod отчетный период подразделения
     */
    DeclarationData find(int declarationTypeId, int departmentReportPeriod, String kpp, String oktmo, String taxOrganCode, Long asnuId, String fileName);

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
     * @return код блокировки
     */
    String generateAsyncTaskKey(long declarationDataId, DeclarationDataReportType type);

    /**
     * Заблокировать DeclarationData.
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе
     */
    LockData lock(long declarationDataId, TAUserInfo userInfo);

    /**
     * Заблокировать налоговую форму
     *
     * @param declarationDataId идентификатор формы
     * @param userInfo          информация о пользователе
     */
    DeclarationLockResult createLock(long declarationDataId, TAUserInfo userInfo);

    /**
     * Снять блокировку с DeclarationData.
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе
     */
    void unlock(long declarationDataId, TAUserInfo userInfo);

    void findDDIdsByRangeInReportPeriod(int decTemplateId, Date startDate, Date endDate, Logger logger);

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
     * @param declarationTypeId        тип декларации
     * @param departmentReportPeriodId идентификатор отчетного периода привязанного к подразделению
     * @param taskType                 тип асинхронной задачи, формирующей имя
     * @return название
     */
    String getDeclarationFullName(int declarationTypeId, int departmentReportPeriodId, AsyncTaskType taskType);

    /**
     * Проверяет существование операции, по которым требуется удалить блокировку
     */
    boolean checkExistAsyncTask(long declarationDataId, AsyncTaskType reportType, Logger logger);

    /**
     * Отмена операции, по которым требуется удалить блокировку(+удаление отчетов)
     *
     * @param cause причина остановки задачи
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
     * @param xmlIn поток данных xml
     * @param jrxml текст jrxml
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
     */
    Connection getReportConnection();

    /**
     * Формирование PDF отчета
     */
    void exportPDF(JasperPrint jasperPrint, OutputStream data);

    /**
     * Формирование XLSX отчета
     */
    void exportXLSX(JasperPrint jasperPrint, OutputStream data);

    /**
     * Получение возможности отображения формы предварительного просмотра
     */
    boolean isVisiblePDF(DeclarationData declarationData, TAUserInfo userInfo);

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
     * Создание экземпляров отчетных форм
     *
     * @param knfId                  ид КНФ, по которой будет создана отчетность. Может быть не задан, тогда КНФ будет искаться в заданном периоде
     * @param departmentReportPeriod если knfId не задан, то отчетный период, в котором будет искаться КНФ
     * @param declarationTypeId      идентификатор типа декларации
     * @param adjustNegativeValues   надо ли выполнять корректировку отрицательных значений для 6-НДФЛ
     */
    void createReportForms(Long knfId, DepartmentReportPeriod departmentReportPeriod, int declarationTypeId, boolean adjustNegativeValues, LockStateLogger stateLogger, Logger logger, TAUserInfo userInfo);

    /**
     * Создание отчетности для выгрузки
     *
     * @param departmentReportPeriod отчетный период
     * @param declarationTypeId      идентификатор типа декларации
     */
    String createReports(Logger logger, TAUserInfo userInfo, DepartmentReportPeriod departmentReportPeriod, int declarationTypeId, LockStateLogger stateLogger);

    /**
     * Проверка существования формы
     */
    boolean existDeclarationData(long declarationDataId);

    /**
     * Проверка существования формы и ее типа
     *
     * @param userInfo          пользователь
     * @param declarationDataId id формы
     */
    DeclarationDataExistenceAndKindResult fetchDeclarationDataExistenceAndKind(TAUserInfo userInfo, long declarationDataId);

    /**
     * Получает мапу созданных блокировок по основным операциям формы
     */
    Map<DeclarationDataReportType, LockData> getLockTaskType(long declarationDataId);

    /**
     * Создание отчётности
     *
     * @param action   параметры создания отчетности
     * @param userInfo информация о пользователе
     * @return результат создания отчетности
     */
    CreateDeclarationReportResult createReports(CreateDeclarationReportAction action, TAUserInfo userInfo);

    /**
     * Запускает ассинхронную задачу на выгрузку отчетности для форм
     *
     * @param filter   фильтр, по которому выбираются формы
     * @param userInfo пользователь запустивший операцию
     * @return результат запуска задачи
     */
    ActionResult asyncExportReports(DeclarationDataFilter filter, TAUserInfo userInfo);

    /**
     * Запускает ассинхронную задачу на массовую выгрузку отчетности для форм
     *
     * @param declarationDataIds список ид форм
     * @param userInfo           пользователь запустивший операцию
     * @return результат запуска задачи
     */
    ActionResult asyncExportReports(List<Long> declarationDataIds, TAUserInfo userInfo);

    /**
     * Выполняет массовую выгрузку отчетности для форм
     *
     * @param declarationDataIds список идентификаторов налоговых форм
     * @param userInfo           информация о пользователе
     * @param logger             логгер
     * @return uuid сформированного файла отчетности
     */
    String exportReports(List<Long> declarationDataIds, TAUserInfo userInfo, Logger logger);

    /**
     * Создание отчета для отчетной НФ
     *
     * @param userInfo информация о пользователе
     * @param action   объект с параметрами для создания отчета для отчетной налоговой формы
     * @return объект с результатом для представления об операции создания отчета для отчетной налоговой формы
     */
    CreateReportResult createReportForReportDD(TAUserInfo userInfo, CreateReportAction action);

    /**
     * Подготовка данных для спецотчета
     *
     * @param userInfo информация о пользователе
     * @param action   объект с параметрами для подготовки даннных для спецотчета
     * @return объект с результатом для представления о подготовке данных для спецотчета
     */
    PrepareSubreportResult prepareSubreport(TAUserInfo userInfo, PrepareSubreportAction action);

    /**
     * Создаёт задачу на формирование шаблона Excel-файла для формы
     *
     * @param declarationDataId ид формы
     * @param userInfo          информация о пользователе
     * @param force             будет ли остановлена уже запущенная задача и запущена занова
     * @return результат создания задачи
     */
    CreateDeclarationExcelTemplateResult createTaskToCreateExcelTemplate(final long declarationDataId, TAUserInfo userInfo, boolean force);

    /**
     * Выполняет формирование шаблона Excel-файла для формы
     *
     * @param declarationData форма
     * @param userInfo        информация о пользователе
     * @return uuid сформированного файла
     */
    String createExcelTemplate(DeclarationData declarationData, TAUserInfo userInfo, Logger logger, LockStateLogger lockStateLogger) throws IOException;

    /**
     * Создаёт задачу на загрузку данных из Excel-файла в форму
     *
     * @param declarationDataId ид формы
     * @param fileName          имя загружаемого файла
     * @param inputStream       данные файла
     * @param userInfo          информация о пользователе
     * @return результат создания задачи
     */
    ActionResult createTaskToImportExcel(long declarationDataId, String fileName, InputStream inputStream, TAUserInfo userInfo);

    /**
     * Выполняет загрузку данных из Excel-файла в форму
     *
     * @param declarationDataId ид формы
     * @param blobData          загружаемый файл
     * @param userInfo          информация о пользователе
     */
    void importExcel(long declarationDataId, BlobData blobData, TAUserInfo userInfo, Logger logger);

    /**
     * Обновляет данные строки для раздела 2 (Сведения о доходах и НДФЛ) и производит пересортировку данных
     *
     * @param declarationDataId идентификатор формы, строка которой редактируется
     * @param taUserInfo        пользователь, выполняющий изменения
     * @param personIncome      данные строки раздела 2
     */
    void updateNdflIncomesAndTax(Long declarationDataId, TAUserInfo taUserInfo, NdflPersonIncomeDTO personIncome);

    /**
     * Обновляет данные строки для раздела 3 (Сведения о вычетах) и производит пересортировку данных
     *
     * @param declarationDataId идентификатор формы, строка которой редактируется
     * @param taUserInfo        пользователь, выполняющий изменения
     * @param personDeduction   данные строки раздела 3
     */
    void updateNdflDeduction(Long declarationDataId, TAUserInfo taUserInfo, NdflPersonDeductionDTO personDeduction);

    /**
     * Обновляет данные строки для раздела 4 (Сведения о доходах в виде авансовых платежей) и производит пересортировку данных
     *
     * @param declarationDataId идентификатор формы, строка которой редактируется
     * @param taUserInfo        пользователь, выполняющий изменения
     * @param personPrepayment  данные строки раздела 4
     */
    void updateNdflPrepayment(Long declarationDataId, TAUserInfo taUserInfo, NdflPersonPrepaymentDTO personPrepayment);

    /**
     * Создать имя Pdf отчета
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param userInfo          информация о пользователе
     * @return имя Pdf отчета
     */
    String createPdfFileName(Long declarationDataId, TAUserInfo userInfo);

    /**
     * Создает задачу на обновление данных ФЛ в КНФ
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param userInfo          информация о пользователе
     * @return uuid уведомлений
     */
    String createUpdatePersonsDataTask(Long declarationDataId, TAUserInfo userInfo);

    /**
     * Запускает бизнес-логику по обновлению данных ФЛ в КНФ
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param logger            объект для логирования информации
     * @param userInfo          информация о пользователе
     */
    void performUpdatePersonsData(Long declarationDataId, Logger logger, TAUserInfo userInfo);

    /**
     * Создает doc отчет "Уведомление о задолженности"
     *
     * @param declarationData налоговая форма
     * @param selectedPerson  выбранное ФЛ, для которого будет создан отчет
     * @param userInfo        пользователь запустивший задачу
     * @param logger          логгер
     * @return uuid созданного отчета
     */
    String createDocReportByPerson(DeclarationData declarationData, DataRow<Cell> selectedPerson, TAUserInfo userInfo, Logger logger);

    /**
     * Загрузить файл налоговой формы на сервер.
     *
     * @param fileInputStream   поток данных файла
     * @param fileName          имя файла
     * @param declarationDataId идентификатор налоговой формы для которой загружается файл
     * @return uuid загруженного файла
     */
    String uploadFile(InputStream fileInputStream, String fileName, Long declarationDataId);

    /**
     * Скачать файл налоговой формы
     *
     * @param declarationDataFile объект файла налоговой формы. Нам нужен не понлоценный объект, а огрызок объекта, где заполнены поля DeclarationDataFile.declarationDataId и DeclarationDataFile.uuid
     * @return данные файла
     */
    BlobData downloadFile(DeclarationDataFile declarationDataFile);

    /**
     * Описание налоговой формы
     * @param declarationDataId идентификатор налоговой формы
     * @return строка с описанием
     */
    String getStandardDeclarationDescription(Long declarationDataId);
}
