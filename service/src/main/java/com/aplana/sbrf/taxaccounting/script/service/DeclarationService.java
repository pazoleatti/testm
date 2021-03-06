package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocState;
import com.aplana.sbrf.taxaccounting.service.LockStateLogger;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;
import groovy.lang.Closure;
import net.sf.jasperreports.engine.JasperPrint;

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
     * Возвращяет список КПП, включаемые в КНФ
     *
     * @param declarationDataId ид КНФ
     * @return список КПП
     */
    List<String> getDeclarationDataKppList(long declarationDataId);

    /**
     * Возвращяет список ид ФЛ, включаемые в КНФ
     *
     * @param declarationDataId ид КНФ
     * @return список ид ФЛ
     */
    List<Long> getDeclarationDataPersonIds(long declarationDataId);

    /**
     * Возвращяет список форм по типу и отчетному периоду подразделения
     *
     * @param declarationTypeId        типу формы
     * @param departmentReportPeriodId отчетный период подразделения
     * @return список форм
     */
    List<DeclarationData> findAllByTypeIdAndPeriodId(int declarationTypeId, int departmentReportPeriodId);

    /**
     * Поиск декларации в отчетном периоде подразделения + «КПП» и «Налоговый орган»
     */
    DeclarationData find(int declarationTypeId, int departmentReportPeriodId, String kpp, String oktmo, String taxOrganCode, Long asnuId, String fileName);

    /**
     * Возвращяет список форм по типу и отчетному периоду и паре кпп/октмо
     *
     * @param declarationTypeId типу формы
     * @param reportPeriodId    отчетный период
     * @param kpp               КПП
     * @param oktmo             ОКТМО
     * @return список форм
     */
    List<DeclarationData> findAllByTypeIdAndReportPeriodIdAndKppAndOktmo(int declarationTypeId, int reportPeriodId, String kpp, String oktmo);

    /**
     * Возвращяет список форм по указанным параметрам.
     * Для определения предыдущего ОНФ при формировании аннулирующей 2НДФЛ
     *
     * @param declarationTypeId     ТекущаяОНФ."Макет формы"."Вид формы"
     * @param reportPeriodTypeCode  ТекущаяОНФ."Период"."Код Периода"
     * @param year                  ТекущаяОНФ."Период"."Год"
     * @param kpp                   ТекущаяОНФ.КПП
     * @param oktmo                 ТекущаяОНФ.ОКТМО
     * @return список форм
     */
    List<DeclarationData> findONFFor2Ndfl(int declarationTypeId, String reportPeriodTypeCode, int year, String kpp, String oktmo);

    /**
     * Возвращяет предыдущую форму того же типа и КПП/ОКТМО в состоянии ЭД из заданного множества
     *
     * @return форма
     */
    DeclarationData findPrev(DeclarationData declarationData, RefBookDocState... docStates);

    /**
     * Найти все декларации созданные в отчетном периоде
     */
    List<DeclarationData> findAllDeclarationData(int declarationTypeId, int departmentId, int reportPeriodId);

    /**
     * Получить данные декларации в формате законодателя (XML)
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе
     */
    String getXmlData(long declarationDataId, TAUserInfo userInfo);

    /**
     * Получить данные декларации в формате законодателя (XML) в виде потока
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе
     */
    ZipInputStream getXmlStream(long declarationDataId, TAUserInfo userInfo);

    /**
     * Формирование jasper-отчета, отличие от предыдущего метода берет на себя управление формированием xml данных
     *
     * @param jrxml      исходный jrxml-шаблон отчета
     * @param xmlBuilder метод подготовки xml данных отчета описанный в groovy скрипте
     * @return сформированный отчет
     */
    JasperPrint createJasperReport(InputStream jrxml, Map<String, Object> parameters, Closure xmlBuilder);

    /**
     * Формирование jasper-отчета
     *
     * @param jrxml исходный jrxml-шаблон отчета
     * @return сформированный отчет
     */
    JasperPrint createJasperReport(InputStream jrxml, Map<String, Object> parameters);

    /**
     * Формирование XLSX отчета
     */
    void exportXLSX(JasperPrint jasperPrint, OutputStream data);

    /**
     * Получение шаблона налоговой декларации по ее идентификатору
     */
    DeclarationTemplate getTemplate(int declarationTemplateId);

    DeclarationType getTemplateType(int declarationTypeId);

    /**
     * Создание декларации в заданном отчетном периоде подразделения
     *
     * @param newDeclaration данные формы
     * @param logger         объект журнала
     * @param userInfo       информация о пользователе, выполняющего действие
     * @param writeAudit
     */
    void createWithoutChecks(DeclarationData newDeclaration, Logger logger, TAUserInfo userInfo, boolean writeAudit);

    /**
     * Установить имя файла
     *
     * @param declarationDataId идентификатор налоговой формы
     * @param fileName          имя файла
     */
    void setFileName(long declarationDataId, String fileName);

    /**
     * Удаляет все отчеты налоговой формы
     */
    void deleteReport(long declarationDataId);

    /**
     * Метод передающий управление на проверку xml по xsd схеме сторонней утилите
     */
    void validateDeclaration(Logger logger, File xmlFile, String fileName, String xsdBlobDataId);

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
     */
    @Deprecated
    List<Long> getDeclarationIds(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering, boolean asc);


    /**
     * Импортировать в систему данные из ТФ в формате xml.
     *
     * @param xmlTransportFile импортируемый файл
     * @param xmlFileName      название файла
     * @param declarationData  целевая ПНФ
     * @param userInfo         данные пользователя, загрузившего файл
     * @param logger           логгер панели уведомлений
     */
    void importXmlTransportFile(File xmlTransportFile, String xmlFileName, DeclarationData declarationData, TAUserInfo userInfo, Logger logger);

    /**
     * Поиск ОНФ по имени файла и типу файла
     */
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
     */
    void createPdfReport(Logger logger, DeclarationData declarationData, TAUserInfo userInfo);

    /**
     * Найти данные по файлам НФ имеющие указаныый тип
     */
    List<DeclarationDataFile> findAllFilesByDeclarationIdAndType(Long declarationDataId, AttachFileType fileType);

    /**
     * Найти НФ по типу, периоду, и значениям Налоговый орган, КПП, ОКТМО
     */
    List<DeclarationData> find(int declarationTemplate, int departmentReportPeriodId, String taxOrganCode, String kpp, String oktmo);

    /**
     * Найти существующие ОНФ Приложения 2 по отчетному году
     * @param reportYear год отчетного периода
     * @return список ранее созданных ОНФ
     */
    List<DeclarationData> findApplication2ByReportYear(int reportYear);

    /**
     * Удаляет форму
     *
     * @param declarationDataId идентификатор формы
     * @param userInfo          пользователь
     */
    void delete(long declarationDataId, TAUserInfo userInfo);

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
     * Возвращает признак фатальности проверки внутри формы по ее коду
     *
     * @param code       код проверки
     * @param templateId идентификатор макета
     * @return ошибка фатальна?
     */
    boolean isCheckFatal(DeclarationCheckCode code, int templateId);

    /**
     * Получить дату создания налоговой формы
     *
     * @param declarationDataId иденитфикатор налоговой формы
     * @return дата
     */
    Date getDeclarationDataCreationDate(Long declarationDataId);

    /**
     * Находит налоговые формы операции из которых используются для создания Приложения 2 к НП
     *
     * @param reportYear отчетный год
     * @return идентификаторы найденых налоговых форм
     */
    List<Long> findApplication2DeclarationDataId(int reportYear);

    /**
     * Поиск ID блоба содержащего XSD файл макета по ID макета
     *
     * @param declarationTemplateId ID макета
     * @return ID блоба содержащего XSD файл макета
     */
    String findXsdIdByTemplateId(Integer declarationTemplateId);

    /**
     * Получает файл из макета по его имени
     *
     * @param declarationTemplateId идентификатор макета
     * @param fileName              название файла, приложенного к макету
     * @return содержимое файла
     */
    InputStream getTemplateFileContent(int declarationTemplateId, String fileName);

    /**
     * Получить имя файла в формате законодателя
     *
     * @param declarationDataId идентификатор декларации
     * @param userInfo          информация о пользователе, выполняющего действие
     * @return имя файла взятого из xml данных
     */
    String getXmlDataFileName(long declarationDataId, TAUserInfo userInfo);

    /**
     * Полное Описание налоговой формы
     *
     * @param declarationDataId идентификатор налоговой формы
     * @return строка с описанием
     */
    String getFullDeclarationDescription(Long declarationDataId);

    /**
     * Возвращяет дату последней записи в историю формы о событию
     *
     * @param declarationId идентификатор формы
     * @param event         искомое событие
     * @return записи выбранного типа события
     */
    Date getMaxLogDateByDeclarationIdAndEvent(Long declarationId, FormDataEvent event);

}
