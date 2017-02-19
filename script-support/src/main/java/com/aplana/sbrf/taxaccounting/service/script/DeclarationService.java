package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import groovy.lang.Closure;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRSwapFile;

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
     * Поиск декларации в отчетном периоде подразделения
     */
    List<DeclarationData> find(int declarationTypeId, int departmentReportPeriodId);

    /**
     * Поиск декларации в отчетном периоде подразделения + «КПП» и «Налоговый орган»
     */
    DeclarationData find(int declarationTypeId, int departmentReportPeriodId, String kpp, String oktmo, String taxOrganCode, Long asnuId, String fileName);

    /**
     * Поиск деклараций по имени файла
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
     * Возвращает список налоговых форм, являющихся источником для указанной декларации и находящихся в статусе
     * "Принята"
     *
     * @param declarationData декларация
     * @return список НФ-источников в статусе "Принята"
     */
    FormDataCollection getAcceptedFormDataSources(DeclarationData declarationData, TAUserInfo userInfo, Logger logger);

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
    @SuppressWarnings("unused")
    boolean checkUnique(DeclarationData declarationData, Logger logger);

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
     * @param sourceFormData    нф-источник
     * @param light             true - заполнятся только текстовые данные для GUI и сообщений
     * @param excludeIfNotExist true - исключить несозданные приемники
     * @param stateRestriction  ограничение по состоянию для созданных экземпляров
     * @return список нф-источников
     */
    List<Relation> getDeclarationDestinationsInfo(FormData sourceFormData, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction,
                                                  TAUserInfo userInfo, Logger logger);

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
     * @param declarationTypeId
     * @return
     */
    DeclarationType getType(int declarationTypeId);

    /**
     * Получение вида декларации по идентификатору шаблона
     * @param declarationTemplateId
     * @return
     */
    DeclarationType getTypeByTemplateId(int declarationTemplateId);

    /**
     * Получение шаблона налоговой декларации по ее идентификатору
     * @param declarationTemplateId
     * @return
     */
    DeclarationTemplate getTemplate(int declarationTemplateId);

    DeclarationType getTemplateType(int declarationTypeId);

    Long create(Logger logger, int declarationTemplateId, TAUserInfo userInfo,
                DepartmentReportPeriod departmentReportPeriod, String taxOrganCode, String taxOrganKpp, String oktmo, Long asunId, String fileName, String note);

    void delete(long declarationDataId, TAUserInfo userInfo);


    /**
     * Удаляет все отчеты налоговой формы
     * @param declarationDataId
     */
    void deleteReport(long declarationDataId);

    /**
     * Удаляет отчеты заданных типов
     * @param declarationDataId
     */
    void deleteReport(long declarationDataId, List<DeclarationDataReportType> declarationDataReportTypeList);

    /**
     * Метод передающий управление на проверку декларации сторонней утилите
     * @param declarationData
     * @param userInfo
     * @param logger
     * @param dataFile - если не задан, то вызывается проверка привязанной к форме xml
     */
    void validateDeclaration(DeclarationData declarationData, TAUserInfo userInfo, Logger logger, File dataFile);

    /**
     * Возвращает идентификатор действующего {@link DeclarationTemplate описания декларации} по виду декларации
     * Такое описание для каждого вида декларации в любой момент времени может быть только одно
     * @param declarationTypeId идентификатор вида декларации
     * @return идентификатор описания декларации
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если не удалось найти активное описание декларации по заданному типу,
     * 	или если обнаружено несколько действуюшие описаний по данному виду декларации
     */
    int getActiveDeclarationTemplateId(int declarationTypeId, int reportPeriodId);

    /**
     * Получение тела скрипта.
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
     * @param declarationFilter
     * @param ordering
     * @param asc
     * @return
     */
    List<Long> getDeclarationIds(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering, boolean asc);

    /**
     *
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
                               String fileName, File dataFile, AttachFileType attachFileType, Date createDateFile);

    /**
     * Найти декларацию НДФЛ операции по доходам которой имеют заданные КПП и ОКТМО
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
    DeclarationData findDeclarationDataByFileNameAndFileType(String fileName, Long fileTypeId);

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
     * @param declarationDataId идентификатор налоговой формы
     * @param docStateId ссстояние ЭД
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если такой налоговой формы не существует
     */
    void setDocStateId(long declarationDataId, Long docStateId);
}
