package refbook.declaration_type

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.service.impl.*
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import org.apache.commons.io.IOUtils;
import groovy.transform.Field

import javax.script.ScriptException
import java.util.*;
import java.io.*;

import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import java.util.regex.Pattern

/**
 * Created by lhaziev on 09.02.2017.
 */
switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTF()
        break
}

public class SAXHandler extends DefaultHandler {
    private Map<String, Map<String, String>> values;
    private Map<String, List<String>> tagAttrNames;

    public SAXHandler(Map<String, List<String>> tagAttrNames) {
        this.tagAttrNames = tagAttrNames;
    }

    public Map<String, Map<String, String>> getValues() {
        return values;
    }

    @Override
    public void startDocument() throws SAXException {
        values = new HashMap<String, Map<String, String>>();
        for (Map.Entry<String, List<String>> entry : tagAttrNames.entrySet()) {
            values.put(entry.getKey(), new HashMap<String, String>());
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        for (Map.Entry<String, List<String>> entry : tagAttrNames.entrySet()) {
            if (entry.getKey().equals(qName)) {
                for (String attrName: entry.getValue()) {
                    values.get(qName).put(attrName, attributes.getValue(attrName));
                }
            }
        }
    }
}

@Field
String TAG_DOCUMENT = "Документ";

@Field
String ATTR_PERIOD = "Период";

@Field
String ATTR_YEAR = "ОтчетГод";

@Field String ERROR_NAME_FORMAT = "Имя транспортного файла «%s» не соответствует формату!";
@Field String ERROR_NOT_FOUND_FILE_NAME = "Не найдено имя отчетного файла в файле ответа «%s»!";
@Field String ERROR_NOT_FOUND_FORM = "Файл ответа «%s», для которого сформирован ответ, не найден в формах!";

// Шаблоны имен файлов
@Field final String NO_RASCHSV_PATTERN = "NO_RASCHSV_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";
@Field final String KV_OTCH_PATTERN = "KV_OTCH_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";
@Field final String UO_OTCH_PATTERN = "UO_OTCH_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";
@Field final String IV_OTCH_PATTERN = "IV_OTCH_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";
@Field final String UU_OTCH_PATTERN = "UU_OTCH_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";

// Идентификаторы видов деклараций
@Field final long DECLARATION_TYPE_RNU_NDFL_ID = 100
@Field final long DECLARATION_TYPE_RASCHSV_NDFL_ID = 200

@Field
String NAME_EXTENSION_DEC = ".xml";
@Field
int NAME_LENGTH_QUARTER_DEC = 63;

// Кэш провайдеров
@Field def providerCache = [:]

@Field final long REF_BOOK_DEPARTMENT_ID = RefBook.Id.DEPARTMENT.id

def getPeriodNdflMap() {
    Map<String, String> periodNdflMap = new HashMap<String, String>();
    periodNdflMap.put("21", "21");
    periodNdflMap.put("32", "31");
    periodNdflMap.put("33", "33");
    periodNdflMap.put("34", "34");
    return periodNdflMap;
}

def importTF() {
    logger.setMessageDecorator(null)

    Pattern patternNoRaschsv = Pattern.compile(NO_RASCHSV_PATTERN);
    Pattern patternKvOtch = Pattern.compile(KV_OTCH_PATTERN);
    Pattern patternUoOtch = Pattern.compile(UO_OTCH_PATTERN);
    Pattern patternIvOtch = Pattern.compile(IV_OTCH_PATTERN);
    Pattern patternUuOtch = Pattern.compile(UU_OTCH_PATTERN);

    if (UploadFileName != null
        && UploadFileName.toLowerCase().endsWith(NAME_EXTENSION_DEC)
        && UploadFileName.length() == NAME_LENGTH_QUARTER_DEC
    ) {
        importNDFL()
    } else if (patternNoRaschsv.matcher(UploadFileName).matches()) {
        import115111()
    } else if (
        patternKvOtch.matcher(UploadFileName).matches() ||
        patternUoOtch.matcher(UploadFileName).matches() ||
        patternIvOtch.matcher(UploadFileName).matches() ||
        patternUuOtch.matcher(UploadFileName).matches()
    ) {
        importOtch()
    } else {
        throw new IllegalArgumentException(String.format(ERROR_NAME_FORMAT, UploadFileName));
    }
}

def importNDFL() {
    _importTF()
}

def import115111() {
    _importTF()
}

def importOtch() {
    _importTF()
}

@Deprecated
def _importTF() {
    logger.setMessageDecorator(null)
    Integer declarationTypeId;
    int departmentId;
    String reportPeriodCode;
    String asnuCode = null;
    String guid = null;
    String kpp = null;
    Integer year = null;
    boolean isFNS = false;

    // Дата создания файла
    Date createDateFile = null

    // Категория прикрепленного файла
    AttachFileType attachFileType = null

    Pattern patternNoRaschsv = Pattern.compile(NO_RASCHSV_PATTERN);
    Pattern patternKvOtch = Pattern.compile(KV_OTCH_PATTERN);
    Pattern patternUoOtch = Pattern.compile(UO_OTCH_PATTERN);
    Pattern patternIvOtch = Pattern.compile(IV_OTCH_PATTERN);
    Pattern patternUuOtch = Pattern.compile(UU_OTCH_PATTERN);

    Long declarationDataId = null

    // Имя файла, для которого сформирован ответ
    def String declarationDataFileNameReport = null

    if (UploadFileName != null && UploadFileName.toLowerCase().endsWith(NAME_EXTENSION_DEC)
            & UploadFileName.length() == NAME_LENGTH_QUARTER_DEC) {
        // РНУ_НДФЛ (первичная)
        declarationTypeId = DECLARATION_TYPE_RNU_NDFL_ID;
        attachFileType = AttachFileType.TYPE_1
        String departmentCode = UploadFileName.substring(0, 17).replaceFirst("_*", "").trim();
        Department formDepartment = departmentService.getDepartmentBySbrfCode(departmentCode, false);
        if (formDepartment == null) {
            logger.error("Не удалось определить подразделение \"%s\"", departmentCode)
            return
        }
        departmentId = formDepartment != null ? formDepartment.getId() : null;

        reportPeriodCode = UploadFileName.substring(21, 23).replaceAll("_", "").trim();
        if (reportPeriodCode != null && !reportPeriodCode.isEmpty() && periodNdflMap.containsKey(reportPeriodCode)) {
            reportPeriodCode = periodNdflMap.get(reportPeriodCode);
        }
        asnuCode = UploadFileName.substring(17, 21).replaceFirst("_", "").trim();
        guid = UploadFileName.substring(27, 59).replaceAll("_", "").trim();
        try {
            year = Integer.parseInt(UploadFileName.substring(23, 27));
        } catch (NumberFormatException nfe) {
            logger.error("Ошибка заполнения атрибутов транспортного файла \"%s\".", UploadFileName)
            return
        }
    } else if (patternNoRaschsv.matcher(UploadFileName).matches()) {
        // ТФ 1151111 (первичная)
        declarationTypeId = DECLARATION_TYPE_RASCHSV_NDFL_ID;
        kpp = UploadFileName.replaceAll(NO_RASCHSV_PATTERN, "\$4");
        reportPeriodCode = null;
        createDateFile = new Date().parse("yyyyMMdd", UploadFileName.replaceAll(NO_RASCHSV_PATTERN, "\$5").substring(0,8));
        attachFileType = AttachFileType.TYPE_1
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            def sett = new HashMap<String, List<String>>();
            sett.put(TAG_DOCUMENT, [ATTR_PERIOD, ATTR_YEAR]);
            SAXHandler handler = new SAXHandler(sett);
            saxParser.parse(ImportInputStream, handler);
            reportPeriodCode = handler.getValues().get(TAG_DOCUMENT).get(ATTR_PERIOD);
            try {
                year = Integer.parseInt(handler.getValues().get(TAG_DOCUMENT).get(ATTR_YEAR));
            } catch (NumberFormatException nfe) {
                logger.error("Ошибка заполнения атрибутов транспортного файла \"%s\".", UploadFileName)
                return
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Ошибка чтения файла \"%s\".", UploadFileName);
            return
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            logger.error("Некорректное имя или формат файла \"%s\".", UploadFileName);
            return
        } catch (SAXException e) {
            e.printStackTrace();
            logger.error("Некорректное имя или формат файла \"%s\".", UploadFileName);
            return
        } finally {
            IOUtils.closeQuietly(ImportInputStream);
        }
    } else if (patternKvOtch.matcher(UploadFileName).matches() ||
            patternUoOtch.matcher(UploadFileName).matches() ||
            patternIvOtch.matcher(UploadFileName).matches() ||
            patternUuOtch.matcher(UploadFileName).matches()) {
        // Файлы ответов для ТФ 1151111 (первичная)

        // todo oshelepaev https://jira.aplana.com/browse/SBRFNDFL-383 Добавить xsd валидацию файлов ответа
        // ожидаю https://jira.aplana.com/browse/SBRFNDFL-381

        def String nodeNameFind = null
        def String attrNameFind = "ИмяОбрабФайла"

        if (patternKvOtch.matcher(UploadFileName).matches()) {
            // Квитанция о приеме налоговой декларации
            createDateFile = new Date().parse("yyyyMMdd", UploadFileName.replaceAll(KV_OTCH_PATTERN, "\$5").substring(0,8));
            nodeNameFind = "СвКвит"
        } else if (patternUoOtch.matcher(UploadFileName).matches()) {
            // Уведомление об отказе в приеме налоговой декларации
            createDateFile = new Date().parse("yyyyMMdd", UploadFileName.replaceAll(UO_OTCH_PATTERN, "\$5").substring(0,8));
            nodeNameFind = "ОбщСвУвед"
        } else if (patternIvOtch.matcher(UploadFileName).matches()) {
            // Извещение о вводе
            createDateFile = new Date().parse("yyyyMMdd", UploadFileName.replaceAll(IV_OTCH_PATTERN, "\$5").substring(0,8));
            nodeNameFind = "СвИзвещВ"
        } else if (patternUuOtch.matcher(UploadFileName).matches()) {
            // 	Уведомление об уточнении
            createDateFile = new Date().parse("yyyyMMdd", UploadFileName.replaceAll(UU_OTCH_PATTERN, "\$5").substring(0,8));
            nodeNameFind = "ОбщСвУвед"
        }

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            def sett = new HashMap<String, List<String>>();
            sett.put(nodeNameFind, [attrNameFind]);
            SAXHandler handler = new SAXHandler(sett);
            saxParser.parse(ImportInputStream, handler);
            declarationDataFileNameReport = handler.getValues().get(nodeNameFind).get(attrNameFind);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Ошибка чтения файла \"%s\".", UploadFileName);
            return
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            logger.error("Некорректное имя или формат файла \"%s\".", UploadFileName);
            return
        } catch (SAXException e) {
            e.printStackTrace();
            logger.error("Некорректное имя или формат файла \"%s\".", UploadFileName);
            return
        } finally {
            IOUtils.closeQuietly(ImportInputStream);
        }

        // Не найдено имя файла, для которого сформирован ответ, в файле ответа
        if (!declarationDataFileNameReport) {
            throw new IllegalArgumentException(String.format(ERROR_NOT_FOUND_FILE_NAME, UploadFileName));
        }

    } else {
        // Неизвестный формат имени загружаемого файла
        throw new IllegalArgumentException(String.format(ERROR_NAME_FORMAT, UploadFileName));
    }

    // Файлы ответов - Поиск DeclarationData по имени отчетного файла
    if (patternKvOtch.matcher(UploadFileName).matches() ||
            patternUoOtch.matcher(UploadFileName).matches() ||
            patternIvOtch.matcher(UploadFileName).matches() ||
            patternUuOtch.matcher(UploadFileName).matches()) {

        // Поиск формы по Имени файла ответа
        def declarationDataList = declarationService.find(declarationDataFileNameReport)

        if (declarationDataList == null || declarationDataList.size() == 0) {
            // Ошибка: Не найдена форма, соответсвующая имени отчетного файла
            throw new IllegalArgumentException(String.format(ERROR_NOT_FOUND_FORM, declarationDataFileNameReport));
        } else if (declarationDataList.size() != 1) {
            // Ошибка: Найдено несколько форм, соответствующих имени отчетного файла

            // Выведем ошибку
            def msgError = "Файл ответа \"" + declarationDataFileNameReport + "\" найден в формах: %s"
            def msgErrorList = []
            declarationDataList.each { declarationData ->
                def declarationTypeName = declarationService.getTypeByTemplateId(declarationData.declarationTemplateId)?.name
                def departmentName = departmentService.get(declarationData.departmentId)?.name
                def reportPeriodName = reportPeriodService.get(declarationData.reportPeriodId)?.name
                msgErrorList.add("\"" + declarationTypeName + "\".\"" + departmentName + "\".\"" + reportPeriodName + "\"")
            }
            throw new IllegalArgumentException(String.format(msgError, msgErrorList.join(", ")));
        }

        // todo oshelepaev Проверка того, что файл ответа не был загружен ранее https://jira.aplana.com/browse/SBRFNDFL-338
        // Ожидаю https://jira.aplana.com/browse/SBRFNDFL-381

        declarationDataId = declarationDataList.get(0)?.id
        attachFileType = AttachFileType.TYPE_3

    } else {
        // РНУ-НДФЛ (первичная) и 1151111 (первичная) - Создание DeclarationData

        DeclarationType declarationType = declarationService.getTemplateType(declarationTypeId);

        // Указан недопустимый код периода
        ReportPeriod reportPeriod = reportPeriodService.getByTaxTypedCodeYear(declarationType.getTaxType(), reportPeriodCode, year);
        if (reportPeriod == null) {
            logger.error("Для вида налога «%s» в Системе не создан период с кодом «%s», календарный год «%s»! Загрузка файла «%s» не выполнена.", declarationType.getTaxType().getName(), reportPeriodCode, year, UploadFileName);
            return;
        }

        if (declarationTypeId == DECLARATION_TYPE_RASCHSV_NDFL_ID) {
            def provider = refBookFactory.getDataProvider(RefBook.Id.FOND_DETAIL.getId());
            // Настройки подразделений сборов
            def results = provider.getRecords(new Date(117, 1, 1), null, "kpp = '$kpp'", null);
            if (results.size() == 0) {
                logger.error("Не удалось определить подразделение для транспортного файла \"%s\"", UploadFileName)
                return
            }
            departmentId = results.get(0).DEPARTMENT_ID.getReferenceValue()
            kpp = null
        }

        Department formDepartment = departmentService.get(departmentId);
        if (formDepartment == null) {
            logger.error("Не удалось определить подразделение для транспортного файла \"%s\"", UploadFileName)
            return
        }

        // Актуальный шаблон НФ, введенный в действие
        Integer declarationTemplateId;
        try {
            declarationTemplateId = declarationService.getActiveDeclarationTemplateId(declarationType.getId(), reportPeriod.getId());
        } catch (Exception e) {
            // Если шаблона нет, то не загружаем ТФ
            logger.info("Ошибка при обработке данных транспортного файла. Загрузка файла не выполнена. %s.", e.getMessage());
            return;
        }

        DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationTemplateId);
        if (!TAAbstractScriptingServiceImpl.canExecuteScript(declarationService.getDeclarationTemplateScript(declarationTemplateId), FormDataEvent.IMPORT_TRANSPORT_FILE)) {
            logger.error("Для налоговой формы загружаемого файла \"%s\" не предусмотрена обработка транспортного файла! Загрузка не выполнена.", UploadFileName);
            return;
        }

        // АСНУ
        Long asnuId = null;
        if (asnuCode != null) {
            def asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
            List<Long> asnuIds = asnuProvider.getUniqueRecordIds(null, "CODE = '" + asnuCode + "'");
            if (asnuIds.size() != 1) {
                RefBook refBook = refBookFactory.get(900L);
                logger.error("В справочнике «%s» отсутствует код АСНУ, поле «%s» которого равно «%s»! Загрузка файла «%s» не выполнена.", refBook.getName(), refBook.getAttribute("CODE").getName(), asnuCode, UploadFileName)
                return
            } else {
                asnuId = asnuIds.get(0);
            }
        }

        // Назначение подразделению Декларации
        List<DepartmentDeclarationType> ddts = declarationService.getDDTByDepartment(departmentId,
                declarationTemplate.getType().getTaxType(), reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        boolean found = false;
        for (DepartmentDeclarationType ddt : ddts) {
            if (ddt.getDeclarationTypeId() == declarationType.getId()) {
                found = true;
                break;
            }
        }
        if (!found) {
            logger.error("Для подразделения «%s» не назначено первичной налоговой формы «%s»! Загрузка файла «%s» не выполнена.", formDepartment.getName(), declarationType.getName(), UploadFileName)
            return
        }

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.getLast(departmentId, reportPeriod.getId());
        // Открытость периода
        if (departmentReportPeriod == null || !departmentReportPeriod.isActive()) {
            String reportPeriodName = reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName();
            logger.error("Нет открытых отчетных периодов для \"%s\" за \"%s\".", formDepartment.getName(), reportPeriodName)
            return
        }

        // Проверка GUID
        if (guid != null && !guid.isEmpty()) {
            DeclarationDataFilter declarationFilter = new DeclarationDataFilter();
            declarationFilter.setFileName(guid);
            declarationFilter.setTaxType(declarationType.getTaxType());
            declarationFilter.setSearchOrdering(DeclarationDataSearchOrdering.ID);
            List<Long> declarationDataSearchResultItems = declarationService.getDeclarationIds(declarationFilter, declarationFilter.getSearchOrdering(), false);
            if (!declarationDataSearchResultItems.isEmpty()) {
                logger.error("ТФ с GUID \"%s\" уже загружен в систему. Загрузка файла «%s» не выполнена.", guid, UploadFileName)
            }
        }

        // Поиск экземпляра декларации
        DeclarationData declarationData = declarationService.find(declarationTemplateId, departmentReportPeriod.getId(), null, kpp, null, asnuId, UploadFileName);
        // Экземпляр уже есть
        if (declarationData != null) {
            logger.error("Экземпляр формы \"%s\" в \"%s\" уже существует! Загрузка файла «%s» не выполнена.", declarationType.getName(), formDepartment.getName(), UploadFileName);
            return;
        }

        // Создание экземпляра декларации
        declarationDataId = declarationService.create(logger, declarationTemplateId, userInfo, departmentReportPeriod, null, kpp, null, asnuId, UploadFileName, null);
    }

    InputStream inputStream = new FileInputStream(dataFile)
    try {
        // Запуск события скрипта для разбора полученного файла
        declarationService.importDeclarationData(logger, userInfo, declarationService.getDeclarationData(declarationDataId), inputStream, UploadFileName, dataFile, attachFileType, createDateFile)
    } finally {
        IOUtils.closeQuietly(inputStream);
    }
}

def readXml1151111() {
    def sett = [:]
    sett.put(TAG_DOCUMENT, [ATTR_PERIOD, ATTR_YEAR])

    try {
        SAXParserFactory factory = SAXParserFactory.newInstance()
        SAXParser saxParser = factory.newSAXParser()
        SAXHandler handler = new SAXHandler(sett)
        saxParser.parse(ImportInputStream, handler)
        reportPeriodCode = handler.getValues().get(TAG_DOCUMENT).get(ATTR_PERIOD)
        try {
            year = Integer.parseInt(handler.getValues().get(TAG_DOCUMENT).get(ATTR_YEAR))
        } catch (NumberFormatException nfe) {
            logger.error("Файл «%s» не загружен: Не удалось извлечь данные о календарном годе из элемента Файл.Документ.ОтчетГод", UploadFileName)
            return null
        }
    } catch (IOException e) {
        e.printStackTrace()
        logger.error("Файл «%s» не загружен: Ошибка чтения файла", UploadFileName)
        return null
    } catch (ParserConfigurationException e) {
        e.printStackTrace()
        logger.error("Файл «%s» не загружен: Некорректное формат файла", UploadFileName)
        return null
    } catch (SAXException e) {
        e.printStackTrace()
        logger.error("Файл «%s» не загружен: Некорректное формат файла", UploadFileName)
        return null
    } finally {
        IOUtils.closeQuietly(ImportInputStream)
    }

    return [reportPeriodCode, year]
}