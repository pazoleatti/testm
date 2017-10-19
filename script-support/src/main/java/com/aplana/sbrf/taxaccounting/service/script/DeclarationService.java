package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import groovy.lang.Closure;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRSwapFile;
import org.joda.time.LocalDateTime;

import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

@ScriptExposed
public interface DeclarationService {

    /**
     * Получить DeclarationData по id
     */
    DeclarationData getDeclarationData(long declarationDataId);

    /**
     * Получить декларации
     *
     * @param declarationDataIds идентификатор декларации
     * @return объект декларации
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если такой декларации не существует
     */
    List<DeclarationData> getDeclarationData(List<Long> declarationDataIds);

    /**
     * Поиск декларации в отчетном периоде подразделения
     */
    List<DeclarationData> find(int declarationTypeId, int departmentReportPeriodId);

    /**
     * Поиск декларации в отчетном периоде подразделения + «КПП» и «Налоговый орган»
     */
    DeclarationData find(int declarationTypeId, int departmentReportPeriodId, String kpp, String oktmo, String taxOrganCode, Long asnuId, String fileName);

    /**
     * Поиск деклараций по имени файла
     *
     * @param fileName - имя файла
     * @return
     */
    List<DeclarationData> find(String fileName);

    /**
     * Декларация в последнем отчетном периоде подразделения
     */
    @SuppressWarnings("unused")
    DeclarationData getLast(int declarationTypeId, int departmentId, int reportPeriodId);

    /**
     * Найти все декларации созданные в отчетном периоде
     */
    List<DeclarationData> findAllDeclarationData(int declarationTypeId, int departmentId, int reportPeriodId);

    /**
     * Найти все налоговые формы, созданные в отчетном периоде и принадлежащие заданным подразделениям
     *
     * @param declarationTypeId Вид налоговой формы
     * @param departmentIds     Список идентификаторов подразделений
     * @param reportPeriodId    Идентификатор отчетного периода
     * @return Список налоговых форм заданного вида, созданных в отчетном периоде и принадлежащих заданным подразделениям
     */
    List<DeclarationData> fetchAllDeclarationData(int declarationTypeId, List<Integer> departmentIds, int reportPeriodId);

    /**
     * Получить данные декларации в формате законодателя (XML)
     *
     * @param declarationDataId идентификатор декларации
     */
    @SuppressWarnings("unused")
    String getXmlData(long declarationDataId);

    /**
     * Получить данные декларации в формате законодателя (XML) в виде потока
     *
     * @param declarationDataId идентификатор декларации
     */
    ZipInputStream getXmlStream(long declarationDataId);

    /**
     * Получить данные декларации в формате законодателя (XML) в виде потока для чтения StAX
     *
     * @param declarationDataId идентификатор декларации
     */
    XMLStreamReader getXmlStreamReader(long declarationDataId);

    /**
     * Проверить существование декларации в отчетном периоде (без учета подразделения).
     *
     * @param declarationTypeId        идентификатор типа декларации
     * @param departmentReportPeriodId идентификатор периода
     */
    @SuppressWarnings("unused")
    boolean checkExistDeclarationsInPeriod(int declarationTypeId, int departmentReportPeriodId);

    /**
     * Проверка декларации на уникальность с аналогичными параметрам
     */
    /*@SuppressWarnings("unused")
    boolean checkUnique(DeclarationData declarationData, Logger logger);*/

    /**
     * Получить имя файла в формате законодателя
     *
     * @param declarationDataId идентификатор декларации
     * @return имя файла взятого из xml данных
     * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
     */
    String getXmlDataFileName(long declarationDataId);

    /**
     * Возвращает список нф-источников для указанной декларации (включая несозданные)
     *
     * @param declaration       декларациz-приемник
     * @param light             true - заполнятся только текстовые данные для GUI и сообщений
     * @param excludeIfNotExist true - исключить несозданные источники
     * @param stateRestriction  ограничение по состоянию для созданных экземпляров
     * @return список нф-источников
     */
    List<Relation> getDeclarationSourcesInfo(DeclarationData declaration, boolean light, boolean excludeIfNotExist, State stateRestriction,
                                             TAUserInfo userInfo, Logger logger);

    /**
     * Получить список id типов декларации для указанного налога.
     *
     * @param taxType тип налога
     */
    List<Integer> getDeclarationTypeIds(TaxType taxType);

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
     * Формирование jasper-отчета, отличие от предыдущего метода берет на себя управление формированием xml данных
     *
     * @param jrxml      исходный jrxml-шаблон отчета
     * @param parameters
     * @param xmlBuilder метод подготовки xml данных отчета описанный в groovy скрипте
     * @return сформированный отчет
     */
    JasperPrint createJasperReport(InputStream jrxml, Map<String, Object> parameters, Closure xmlBuilder);

    /**
     * Формирование jasper-отчета, отличие от предыдущего метода берет на себя управление формированием xml данных
     *
     * @param jrxml      исходный jrxml-шаблон отчета
     * @param parameters
     * @param xmlData    xml поток входных данных
     * @return сформированный отчет
     */
    JasperPrint createJasperReport(InputStream jrxml, Map<String, Object> parameters, InputStream xmlData);

    /**
     * Формирование jasper-отчета
     *
     * @param jrxml      исходный jrxml-шаблон отчета
     * @param parameters
     * @return сформированный отчет
     */
    JasperPrint createJasperReport(InputStream jrxml, Map<String, Object> parameters);

    /**
     * Метод записывает xml данные в буфер формирует поток на чтение
     *
     * @param xmlBuilder замыкание в котором описано формирование xml
     * @return поток xml данных отчета
     */
    ByteArrayInputStream generateXmlData(Closure xmlBuilder);

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
     * Получение вида декларации по ее идентификатору
     *
     * @param declarationTypeId
     * @return
     */
    DeclarationType getType(int declarationTypeId);

    /**
     * Получение вида декларации по идентификатору шаблона
     *
     * @param declarationTemplateId
     * @return
     */
    DeclarationType getTypeByTemplateId(int declarationTemplateId);

    /**
     * Получение шаблона налоговой декларации по ее идентификатору
     *
     * @param declarationTemplateId
     * @return
     */
    DeclarationTemplate getTemplate(int declarationTemplateId);

    DeclarationType getTemplateType(int declarationTypeId);

    Long create(Logger logger, int declarationTemplateId, TAUserInfo userInfo,
                DepartmentReportPeriod departmentReportPeriod, String taxOrganCode, String taxOrganKpp, String oktmo, Long asunId, String fileName, String note, boolean writeAudit);

    void delete(long declarationDataId, TAUserInfo userInfo);


    /**
     * Удаляет все отчеты налоговой формы
     *
     * @param declarationDataId
     */
    void deleteReport(long declarationDataId);

    /**
     * Удаляет отчеты заданных типов
     *
     * @param declarationDataId
     */
    void deleteReport(long declarationDataId, List<DeclarationDataReportType> declarationDataReportTypeList);

    /**
     * Метод передающий управление на проверку декларации сторонней утилите
     *
     * @param declarationData
     * @param userInfo
     * @param logger
     * @param dataFile        - если не задан, то вызывается проверка привязанной к форме xml
     */
    void validateDeclaration(DeclarationData declarationData, TAUserInfo userInfo, Logger logger, File dataFile);

    /**
     * Метод передающий управление на проверку декларации сторонней утилите
     *
     * @param declarationData
     * @param userInfo
     * @param logger
     * @param dataFile        - если не задан, то вызывается проверка привязанной к форме xml
     */
    void validateDeclaration(DeclarationData declarationData, TAUserInfo userInfo, Logger logger, File dataFile, String fileName);

    /**
     * Метод передающий управление на проверку декларации сторонней утилите
     *
     * @param declarationData
     * @param userInfo
     * @param logger
     * @param dataFile        - если не задан, то вызывается проверка привязанной к форме xml
     * @param xsdBlobDataId
     */
    void validateDeclaration(DeclarationData declarationData, TAUserInfo userInfo, Logger logger, File dataFile, String fileName, String xsdBlobDataId);

    /**
     * Метод передающий управление на проверку xml по xsd схеме сторонней утилите
     *
     * @param userInfo
     * @param logger
     * @param xmlFile
     * @param fileName
     * @param xsdBlobDataId
     */
    void validateDeclaration(TAUserInfo userInfo, Logger logger, File xmlFile, String fileName, String xsdBlobDataId);

    /**
     * Возвращает идентификатор действующего {@link DeclarationTemplate описания декларации} по виду декларации
     * Такое описание для каждого вида декларации в любой момент времени может быть только одно
     *
     * @param declarationTypeId идентификатор вида декларации
     * @return идентификатор описания декларации
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если не удалось найти активное описание декларации по заданному типу,
     *                                                                    или если обнаружено несколько действуюшие описаний по данному виду декларации
     */
    int getActiveDeclarationTemplateId(int declarationTypeId, int reportPeriodId);

    /**
     * Получение тела скрипта.
     *
     * @param declarationTemplateId идентификатор вида декларации
     * @return тело скрипта
     */
    String getDeclarationTemplateScript(int declarationTemplateId);

    /**
     * Возвращает информацию о назначенных подразделению декларациях по заданному виду налога
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return список назначенных подразделению деклараций (с учётом вида и типа) по заданному виду налога
     */
    List<DepartmentDeclarationType> getDDTByDepartment(int departmentId, TaxType taxType, Date periodStart, Date periodEnd);

    /**
     * Получние id для всех деклараций по фильтру.
     *
     * @param declarationFilter
     * @param ordering
     * @param asc
     * @return
     */
    List<Long> getDeclarationIds(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering, boolean asc);

    /**
     * @param logger
     * @param userInfo
     * @param declarationData
     * @param inputStream
     * @param fileName
     * @param dataFile
     * @param attachFileType
     * @param createDateFile
     */
    void importDeclarationData(Logger logger, TAUserInfo userInfo, DeclarationData declarationData, InputStream inputStream,
                               String fileName, File dataFile, AttachFileType attachFileType, LocalDateTime createDateFile);

    /**
     * Найти декларацию НДФЛ операции по доходам которой имеют заданные КПП и ОКТМО
     *
     * @param declarationTypeId
     * @param departmentReportPeriodId
     * @param departmentId
     * @param reportPeriod
     * @param oktmo
     * @param kpp
     * @return
     */
    @SuppressWarnings("unused")
    DeclarationData findDeclarationDataByKppOktmoOfNdflPersonIncomes(int declarationTypeId, int departmentReportPeriodId, int departmentId, int reportPeriod, String oktmo, String kpp);

    /**
     * Поиск ОНФ по имени файла и типу файла
     */
    @SuppressWarnings("unused")
    List<DeclarationData> findDeclarationDataByFileNameAndFileType(String fileName, Long fileTypeId);

    /**
     * Сохраняет отдельный файл
     */
    void saveFile(DeclarationDataFile file);

    /**
     * Возвращает информацию о системном пользователе
     */
    TAUserInfo getSystemUserInfo();

    /**
     * Находит файл с максимальным "весом"
     * https://conf.aplana.com/pages/viewpage.action?pageId=27184983
     */
    DeclarationDataFile findFileWithMaxWeight(Long declarationDataId);

    /**
     * Установить ссстояние ЭД налоговой формы
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param docStateId        ссстояние ЭД
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если такой налоговой формы не существует
     */
    void setDocStateId(long declarationDataId, Long docStateId);

    /**
     * Получение конфигурационных параметров (табл. CONFIGURATION)
     */
    ConfigurationParamModel getAllConfig(TAUserInfo userInfo);

    /**
     * Формирует Pdf отчет формы
     *
     * @param logger
     * @param declarationData
     * @param userInfo
     */
    void createPdfReport(Logger logger, DeclarationData declarationData, TAUserInfo userInfo);

    /**
     * Найти данные по файлам НФ имеющие указаныый тип
     *
     * @param declarationDataId
     * @param fileTypeName
     * @return
     */
    List<DeclarationDataFile> findFilesWithSpecificType(Long declarationDataId, String fileTypeName);

    /**
     * Найти id деклараций для периода, вида декларации, вида подразделения, статусу
     *
     * @param reportPeriodId
     * @param ndflId                       id подразделения в шапке справочника подразделений
     * @param declarationTypeId            вид декларации
     * @param departmentType               вид подразделения
     * @param departmentReportPeriodStatus статус
     * @param declarationState             статус декларации
     * @return
     */
    List<Integer> findDeclarationDataIdByTypeStatusReportPeriod(Integer reportPeriodId, Long ndflId,
                                                                Integer declarationTypeId, Integer departmentType,
                                                                Boolean departmentReportPeriodStatus, Integer declarationState);

    /**
     * Найти все формы всех подразделений в активном периоде по виду и периоду
     *
     * @param declarationTypeId
     * @param reportPeriodId
     * @return
     */
    List<DeclarationData> findAllActive(int declarationTypeId, int reportPeriodId);

    /**
     * Найти НФ по типу, периоду, и значениям Налоговый орган, КПП, ОКТМО
     *
     * @param declarationTemplate
     * @param departmentReportPeriodId
     * @param taxOrganCode
     * @param kpp
     * @param oktmo
     * @return
     */
    List<DeclarationData> find(int declarationTemplate, int departmentReportPeriodId, String taxOrganCode, String kpp, String oktmo);

    /**
     * Находит все пары КПП/ОКТМО которых нет в справочнике Подразделений, но которые представлены у  операций относящихся к НФ
     *
     * @param declarationDataId
     * @return
     */
    List<Pair<String, String>> findNotPresentedPairKppOktmo(Long declarationDataId);

    /**
     * Получает мапу созданных блокировок по основным операциям формы
     *
     * @param declarationDataId
     * @return
     */
    Map<DeclarationDataReportType, LockData> getLockTaskType(long declarationDataId);

    /**
     * Генерация ключа блокировки для асинхронных задач по НФ
     *
     * @param declarationDataId
     * @param type
     * @return код блокировки
     */
    String generateAsyncTaskKey(long declarationDataId, DeclarationDataReportType type);

    /**
     * Создание блокировки на удаление НФ
     *
     * @param declarationDataId
     * @param userInfo
     * @return если блокировка успешно создана, то возвращает её, иначе null
     */
    LockData createDeleteLock(long declarationDataId, TAUserInfo userInfo);

    /**
     * Удаляет все формы заданного вида в заданном отчетном периоде
     *
     * @param declarationTypeId        вид НФ
     * @param departmentReportPeriodId отчетный период
     * @param logger
     * @param userInfo
     * @return если удаление прошло успешно, то возвращает пустой список, иначе список Pair<id-формы, типа блокировки>, по которым существует блокировка или произошла ошибка удаления
     */
    List<Pair<Long, DeclarationDataReportType>> deleteForms(int declarationTypeId, int departmentReportPeriodId, Logger logger, TAUserInfo userInfo);

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
     * Возвращает полное название декларации с указанием подразделения, периода и прочего
     *
     * @param declarationId идентификатор декларации
     * @param ddReportType  тип отчета. Может быть null
     * @return название
     */
    String getDeclarationFullName(long declarationId, DeclarationDataReportType ddReportType, String... args);
}
