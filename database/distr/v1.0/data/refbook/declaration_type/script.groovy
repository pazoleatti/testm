package refbook.declaration_type

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.service.impl.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
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
@Field final String NO_RASCHSV_PATTERN = "NO_RASCHSV_(.*)_(.*)_(.{10})(.{9})_(.*)_(.*)\\.(xml|XML)";
@Field final String KV_OTCH_PATTERN = "KV_OTCH_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";
@Field final String UO_OTCH_PATTERN = "UO_OTCH_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";
@Field final String IV_OTCH_PATTERN = "IV_OTCH_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";
@Field final String UU_OTCH_PATTERN = "UU_OTCH_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";

@Field final String NDFL2_PATTERN_PROT_1 = "PROT_NO_NDFL2"
@Field final String NDFL2_PATTERN_PROT_2 = "прот_NO_NDFL2"
@Field final String NDFL2_PATTERN_REESTR_1 = "REESTR_NO_NDFL2"
@Field final String NDFL2_PATTERN_REESTR_2 = "реестр_NO_NDFL2"
@Field final String NDFL6_PATTERN_1 = "KV_"
@Field final String NDFL6_PATTERN_2 = "UO_"
@Field final String NDFL6_PATTERN_3 = "IV_"
@Field final String NDFL6_PATTERN_4 = "UU_"
@Field final String NDFL2_KV_FILE_TAG = "СвКвит"
@Field final String NDFL2_KV_FILE_ATTR = "ИмяОбрабФайла"
@Field final String NDFL2_UO_FILE_TAG = "ОбщСвУвед"
@Field final String NDFL2_UO_FILE_ATTR = "ИмяОбрабФайла"
@Field final String NDFL2_IV_FILE_TAG = "СвИзвещВ"
@Field final String NDFL2_IV_FILE_ATTR = "ИмяОбрабФайла"
@Field final String NDFL2_UU_FILE_TAG = "СвКвит"
@Field final String NDFL2_UU_FILE_ATTR = "ИмяОбрабФайла"
@Field final String NDFL2_1 = "2 НДФЛ (1)"
@Field final String NDFL2_2 = "2 НДФЛ (2)"

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

    Pattern patternNoRaschsv = Pattern.compile(NO_RASCHSV_PATTERN)
    Pattern patternKvOtch = Pattern.compile(KV_OTCH_PATTERN)
    Pattern patternUoOtch = Pattern.compile(UO_OTCH_PATTERN)
    Pattern patternIvOtch = Pattern.compile(IV_OTCH_PATTERN)
    Pattern patternUuOtch = Pattern.compile(UU_OTCH_PATTERN)

    if (UploadFileName != null
        && UploadFileName.toLowerCase().endsWith(NAME_EXTENSION_DEC)
        && UploadFileName.length() == NAME_LENGTH_QUARTER_DEC
    ) {
        importNDFL()
    } else if (patternNoRaschsv.matcher(UploadFileName).matches()) {
        import1151111()
    } else if (isNdfl6Response(UploadFileName) && isNdfl6AndNot11151111(UploadFileName)) {
        importNdflResponse()
    } else if (isNdfl2Response(UploadFileName)) {
        importNdflResponse()
    } else if (
        patternKvOtch.matcher(UploadFileName).matches() ||
        patternUoOtch.matcher(UploadFileName).matches() ||
        patternIvOtch.matcher(UploadFileName).matches() ||
        patternUuOtch.matcher(UploadFileName).matches()
    ) {
        importOtch()
    } else {
        throw new IllegalArgumentException(String.format(ERROR_NAME_FORMAT, UploadFileName))
    }
}

/**
 * Импорт ТФ НДФЛ
 */
def importNDFL() {
    //TODO при правке НДФЛ: скопировать содержимое _importTF и удалить лишнее
    _importTF()
}

/**
 * Импорт ТФ 1151111
 */
def import1151111() {
    // 2. Разбор имени файла
    String tranNalog = UploadFileName.replaceAll(NO_RASCHSV_PATTERN, "\$1")
    String endNalog = UploadFileName.replaceAll(NO_RASCHSV_PATTERN, "\$2")
    String inn = UploadFileName.replaceAll(NO_RASCHSV_PATTERN, "\$3")
    String kpp = UploadFileName.replaceAll(NO_RASCHSV_PATTERN, "\$4")
    String dateStr = UploadFileName.replaceAll(NO_RASCHSV_PATTERN, "\$5")
    String guid = UploadFileName.replaceAll(NO_RASCHSV_PATTERN, "\$6")

    // 3. Выполнить извлечение из ТФ элементы
    def periodYearTuple = readXml1151111()
    if (periodYearTuple == null) {
        return
    }
    def (reportPeriodTypeCode, year) = periodYearTuple

    // 4. Определение <Отчетный/корректирующий периода>
    def reportPeriodTypeDataProvider = refBookFactory.getDataProvider(RefBook.Id.PERIOD_CODE.getId())
    if (reportPeriodTypeDataProvider.getRecordsCount(new Date(), "CODE = '$reportPeriodTypeCode'") == 0) {
        logger.error("Файл «%s» не загружен: Значение элемента Файл.Документ.Период \"%s\" отсутствует в справочнике \"Коды. определяющие налоговый (отчетный) период", UploadFileName, reportPeriodTypeCode)
        return
    }

    if (reportPeriodTypeDataProvider.getRecordsCount(new Date(), "CODE = '$reportPeriodTypeCode' AND F = 1 ") == 0) {
        logger.error("Файл «%s» не загружен: Значение элемента Файл.Документ.Период \"%s\" не разрешен для ФП \"Сборы, взносы\" ", UploadFileName, reportPeriodTypeCode)
        return
    }

    DeclarationType declarationType = declarationService.getTemplateType(DECLARATION_TYPE_RASCHSV_NDFL_ID.intValue())
    ReportPeriod reportPeriod = reportPeriodService.getByTaxTypedCodeYear(declarationType.getTaxType(), reportPeriodTypeCode, year)
    if (reportPeriod == null) {
        logger.error("Файл «%s» не загружен: " +
                "Для 1151111 (первичная) в системе не создан период с кодом «%s», календарный год «%s»!",
                UploadFileName, reportPeriodCode, year)
        return
    }

    def reportPeriodTypeId = reportPeriodTypeDataProvider.getUniqueRecordIds(new Date(), "CODE = '$reportPeriodTypeCode' AND F = 1").get(0).intValue()
    def reportPeriodType = reportPeriodTypeDataProvider.getRecordData(reportPeriodTypeId)

    // 5. Определение подразделения
    def fondDetailProvider = refBookFactory.getDataProvider(RefBook.Id.FOND_DETAIL.getId())
    def results = fondDetailProvider.getRecords(new Date(117, 1, 1), null, "kpp = '$kpp'", null)
    if (results.size() == 0) {
        logger.error("Файл «%s» не загружен: Не найдено Подразделение, для которого указан КПП \"%s\" в настройках подразделения", UploadFileName, kpp)
        return
    }

    if (results.size() > 1) {
        def departmentIds = results*.DEPARTMENT_ID*.getReferenceValue()*.intValue()
        def departmentsMap = departmentService.getDepartments(departmentIds)
        def joinName = departmentsMap.values()*.getName().join(', ')
        logger.error("Файл «%s» не загружен: Найдено несколько подразделений, для которого указан КПП \"%s\": \"%s\"", UploadFileName, kpp, joinName)
        return
    }
    def departmentId = results.get(0).DEPARTMENT_ID.getReferenceValue().intValue()
    def departmentName = departmentService.get(departmentId)

    // 4. Для <Подразделения>, <Период>, <Календаный год> открыт отчетный либо корректирующий период
    def departmentReportPeriod = departmentReportPeriodService.getLast(departmentId, reportPeriod.id)
    if (departmentReportPeriod == null || !departmentReportPeriod.isActive()) {
        logger.error("Файл «%s» не загружен: Не найден период код \"%s\" %s %s", UploadFileName, reportPeriodType.NAME, reportPeriodTypeCode, year)
        return
    }

    // 6. Определение <Макета> (версии макета), по которому необходимо создать форму
    Integer declarationTemplateId
    try {
        declarationTemplateId = declarationService.getActiveDeclarationTemplateId(declarationType.getId(), reportPeriod.id)
    } catch (Exception ignored) {
        logger.info("Файл «%s» не загружен: " +
                "В подразделении %s не назначено ни одного актуального макета с параметрами: Вид = 1151111, Тип = Первичная либо Отчетная",
                UploadFileName, departmentName
        )
        return
    }

    // Проверка GUID
    if (guid != null && !guid.isEmpty()) {
        DeclarationDataFilter declarationFilter = new DeclarationDataFilter()
        declarationFilter.setFileName(guid)
        declarationFilter.setTaxType(declarationType.getTaxType())
        declarationFilter.setSearchOrdering(DeclarationDataSearchOrdering.ID)
        List<Long> declarationDataSearchResultItems = declarationService.getDeclarationIds(declarationFilter, declarationFilter.getSearchOrdering(), false)
        if (!declarationDataSearchResultItems.isEmpty()) {
            logger.error("Файл «%s» не загружен: ТФ с GUID \"%s\" уже загружен в систему", UploadFileName, guid)
        }
    }

    createDateFile = new Date().parse("yyyyMMdd", dateStr)
    attachFileType = AttachFileType.TYPE_1

    // Создание экземпляра декларации
    declarationDataId = declarationService.create(
            logger, declarationTemplateId, userInfo, departmentReportPeriod,
            null, kpp, null, null, UploadFileName, null
    )

    InputStream inputStream = new FileInputStream(dataFile)
    try {
        // Запуск события скрипта для разбора полученного файла
        declarationService.importDeclarationData(
                logger, userInfo, declarationService.getDeclarationData(declarationDataId),
                inputStream, UploadFileName, dataFile, attachFileType, createDateFile
        )
    } finally {
        IOUtils.closeQuietly(inputStream)
    }
}

/**
 * Загрузка ответов ФНС по ТФ 1151111 (первичная)
 * @return
 */
def importOtch() {

    Pattern patternKvOtch = Pattern.compile(KV_OTCH_PATTERN)
    Pattern patternUoOtch = Pattern.compile(UO_OTCH_PATTERN)
    Pattern patternIvOtch = Pattern.compile(IV_OTCH_PATTERN)
    Pattern patternUuOtch = Pattern.compile(UU_OTCH_PATTERN)

    // todo oshelepaev https://jira.aplana.com/browse/SBRFNDFL-383 Добавить xsd валидацию файлов ответа
    // ожидаю https://jira.aplana.com/browse/SBRFNDFL-381

    // Дата создания файла
    Date createDateFile = null

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

    def declarationDataId = declarationDataList.get(0)?.id
    AttachFileType attachFileType = AttachFileType.TYPE_3
}

/**
 * Проверяет является ли файл ответом от ФНС 2 НДФЛ
 */
def isNdfl2Response(fileName) {
    return isNdfl2ResponseProt(fileName) || isNdfl2ResponseReestr(fileName)
}

/**
 * Если файл ответа == "Протокол Приема 2НДФЛ"
 */
def isNdfl2ResponseProt(fileName) {
    return fileName.toLowerCase().startsWith(NDFL2_PATTERN_PROT_1.toLowerCase()) ||
            fileName.toLowerCase().startsWith(NDFL2_PATTERN_PROT_2.toLowerCase())
}

/**
 * Если файл ответа == "Реестр Принятых Документов"
 */
def isNdfl2ResponseReestr(fileName) {
    return fileName.toLowerCase().startsWith(NDFL2_PATTERN_REESTR_1.toLowerCase()) ||
            fileName.toLowerCase().startsWith(NDFL2_PATTERN_REESTR_2.toLowerCase())
}

/**
 * Проверяет является ли файл ответом от ФНС 6 НДФЛ
 */
def isNdfl6Response(fileName) {
    return fileName.startsWith(NDFL6_PATTERN_1) ||
            fileName.startsWith(NDFL6_PATTERN_2) ||
            fileName.startsWith(NDFL6_PATTERN_3) ||
            fileName.startsWith(NDFL6_PATTERN_4)
}

/**
 * Проверяет что файл ответа принадлежит 6 НФДЛ и не пренадлежит 1151111
 * Для этого надо проверить содеражание файла
 */
def isNdfl6AndNot11151111(fileName) {
    def contentMap = readNdfl6ResponseContent()
    def reportFileName = getFileName(contentMap, fileName)

    if (reportFileName == null) {
        return false
    }

    // Выполнить поиск ОНФ, для которой пришел файл ответа по условию
    def fileTypeProvider = refBookFactory.getDataProvider(RefBook.Id.ATTACH_FILE_TYPE.getId())
    def fileTypeId = fileTypeProvider.getUniqueRecordIds(new Date(), "CODE = ${AttachFileType.TYPE_2.id}").get(0)

    def declarationData = declarationService.findDeclarationDataByFileNameAndFileType(reportFileName, fileTypeId)
    if (declarationData == null) {
        return false
    }

    return true
}

/**
 * Возвращает имя отчетного файла для 6НДФЛ
 */
def getFileName(contentMap, fileName) {
    if (fileName.startsWith(NDFL6_PATTERN_1)) {
        return contentMap.get(NDFL2_KV_FILE_TAG).get(NDFL2_KV_FILE_ATTR)
    }

    if (fileName.startsWith(NDFL6_PATTERN_2)) {
        return contentMap.get(NDFL2_UO_FILE_TAG).get(NDFL2_UO_FILE_ATTR)
    }

    if (fileName.startsWith(NDFL6_PATTERN_3)) {
        return contentMap.get(NDFL2_IV_FILE_TAG).get(NDFL2_IV_FILE_ATTR)
    }

    if (fileName.startsWith(NDFL6_PATTERN_4)) {
        return contentMap.get(NDFL2_UU_FILE_TAG).get(NDFL2_UU_FILE_ATTR)
    }

    return null
}

/**
 * Определет вес документа
 */
def getDocWeight(fileName) {
    if (isNdfl2Response(fileName)) {
        return 1
    }

    if (fileName.startsWith(NDFL6_PATTERN_1) || fileName.startsWith(NDFL6_PATTERN_2)) {
        return 1
    }

    return 2
}

@Field final String NDFL2_TO_FILE = "К ФАЙЛУ"
@Field final String NDFL2_ERROR_COUNT = "КОЛИЧЕСТВО СВЕДЕНИЙ С ОШИБКАМИ"
@Field final String NDFL2_NOT_CORRECT_ERROR = "ДОКУМЕНТЫ С ВЫЯВЛЕННЫМИ И НЕИСПРАВЛЕННЫМИ ОШИБКАМИ"
@Field final String NDFL2_CORRECT_ADDRESS_COUNT = "КОЛИЧЕСТВО СВЕДЕНИЙ С ИСПРАВЛЕННЫМИ АДРЕСАМИ"
@Field final String NDFL2_CORRECT_ADDRESS = "СВЕДЕНИЯ С ИСПРАВЛЕННЫМИ АДРЕСАМИ"
@Field final String NDFL2_FILE_NAME_PATTERN = "(.+)\\\\([^/\\\\]+\\.(xml|XML))\\s*"
@Field final String NDFL2_NUMBER_PATTERN = "\\s*(.+):\\s*(\\d+)\\s*"
@Field final String NDFL2_STR_PATTERN = "\\s*(.+):\\s*(.+)\\s*"
@Field final String NDFL2_NOT_CORRECT_NUMB = "Номер п/п:"
@Field final String NDFL2_NOT_CORRECT_NUMB_REF = "Номер справки:"
@Field final String NDFL2_NOT_CORRECT_PATH = "Путь к реквизиту:"
@Field final String NDFL2_NOT_CORRECT_VALUE = "Значение элемента:"
@Field final String NDFL2_NOT_CORRECT_TEXT = "Текст ошибки:"
@Field final String NDFL2_NOT_CORRECT_SKIP = "---"
@Field final String NDFL2_CORRECT_NUMB_REF = "Номер справки:"
@Field final Pattern NDFL2_CORRECT_ADDRESS_PATTERN_BEFORE = Pattern.compile("\\s*Адрес ДО исправления:.+")
@Field final Pattern NDFL2_CORRECT_ADDRESS_PATTERN_AFTER = Pattern.compile("\\s*Адрес ПОСЛЕ исправления:.+")
@Field final Pattern NDFL2_CORRECT_ADDRESS_PATTERN_VALID = Pattern.compile("\\s*Адрес ПРИЗНАН ВЕРНЫМ \\(ИФНСМЖ - (.+)\\)\\s*")
@Field final String NDFL2_PROTOCOL_DATE ="ПРОТОКОЛ №"
@Field final Pattern NDFL2_PROTOCOL_DATE_PATTERN = Pattern.compile("ПРОТОКОЛ № .+ от (\\d{2}\\.\\d{2}\\.\\d{4})")
@Field final String NDFL2_REGISTER_DATE = "РЕЕСТР N"
@Field final Pattern NDFL2_REGISTER_DATE_PATTERN = Pattern.compile("РЕЕСТР N .+ от (\\d{2}\\.\\d{2}\\.\\d{4}) в 9979")
@Field final Pattern NDFL6_FILE_NAME_PATTERN = Pattern.compile("(.{17})_(.{4})_(.{2})_(.{4})_(.{32})")

/**
 * Чтение содержание файла 2 НДФЛ - протокол
 */
def readNdfl2ResponseContent() {
    def result = [:]
    def notCorrectList = []
    def correctList = []
    result.put(NDFL2_NOT_CORRECT_ERROR, notCorrectList)
    result.put(NDFL2_CORRECT_ADDRESS, correctList)

    try {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ImportInputStream))

        String line = ""
        while (line != null) {
            if (line.contains(NDFL2_TO_FILE)) {
                result.put(NDFL2_TO_FILE, line.replaceAll(NDFL2_FILE_NAME_PATTERN, "\$2"))
            }

            if (line.contains(NDFL2_PROTOCOL_DATE)) {
                result.put(NDFL2_PROTOCOL_DATE, line.replaceAll(NDFL2_PROTOCOL_DATE_PATTERN, "\$1"))
            }

            if (line.contains(NDFL2_ERROR_COUNT)) {
                result.put(NDFL2_ERROR_COUNT, line.replaceAll(NDFL2_NUMBER_PATTERN, "\$2") as Integer)
            }

            if (line.contains(NDFL2_CORRECT_ADDRESS_COUNT)) {
                result.put(NDFL2_CORRECT_ADDRESS_COUNT, line.replaceAll(NDFL2_NUMBER_PATTERN, "\$2") as Integer)
            }

            if (line.contains(NDFL2_NOT_CORRECT_ERROR)) {
                def lastEntry = [:]
                def isEndNotCorrect = false

                while (!isEndNotCorrect) {
                    line = bufferedReader.readLine()

                    if (line.startsWith(NDFL2_NOT_CORRECT_SKIP)) {
                        notCorrectList.add(lastEntry)
                        lastEntry = [:]
                        continue
                    }

                    if (line.startsWith(NDFL2_NOT_CORRECT_NUMB)) {
                        continue
                    }

                    if (line.startsWith(NDFL2_NOT_CORRECT_NUMB_REF)) {
                        lastEntry.ref = line.replaceAll(NDFL2_STR_PATTERN, "\$2")
                        continue
                    }

                    if (line.startsWith(NDFL2_NOT_CORRECT_PATH)) {
                        lastEntry.path = line.replaceAll(NDFL2_STR_PATTERN, "\$2")
                        continue
                    }

                    if (line.startsWith(NDFL2_NOT_CORRECT_VALUE)) {
                        lastEntry.val = line.replaceAll(NDFL2_STR_PATTERN, "\$2")
                        continue
                    }

                    if (line.startsWith(NDFL2_NOT_CORRECT_TEXT)) {
                        lastEntry.text = line.replaceAll(NDFL2_STR_PATTERN, "\$2")
                        continue
                    }

                    isEndNotCorrect = true
                }

                // Блок "ДОКУМЕНТЫ С ВЫЯВЛЕННЫМИ И НЕИСПРАВЛЕННЫМИ ОШИБКАМИ" полносью прочитан
                // Кроме блока прочитана еще последующая стрка "СВЕДЕНИЯ С ИСПРАВЛЕННЫМИ АДРЕСАМИ" или какая-то другая
                // Поэтому вызываем continue, чтобы повторно не вызвать bufferedReader.readLine()
                continue
            }

            if (line.contains(NDFL2_CORRECT_ADDRESS)) {
                def lastEntry = [:]
                def isEndCorrect = false

                while (!isEndCorrect) {
                    line = bufferedReader.readLine()

                    if (line.startsWith(NDFL2_CORRECT_NUMB_REF)) {
                        lastEntry = [:]
                        lastEntry.ref = line.replaceAll(NDFL2_STR_PATTERN, "\$2")
                        correctList.add(lastEntry)
                        continue
                    }

                    if (NDFL2_CORRECT_ADDRESS_PATTERN_BEFORE.matcher(line).matches()) {
                        lastEntry.addressBefore = line.replaceAll(NDFL2_STR_PATTERN, "\$2")
                        continue
                    }

                    if (NDFL2_CORRECT_ADDRESS_PATTERN_AFTER.matcher(line).matches()) {
                        lastEntry.addressAfter = line.replaceAll(NDFL2_STR_PATTERN, "\$2")
                        continue
                    }

                    if (NDFL2_CORRECT_ADDRESS_PATTERN_VALID.matcher(line).matches()) {
                        lastEntry.valid = line.replaceAll(NDFL2_CORRECT_ADDRESS_PATTERN_VALID, "\$1")
                        continue
                    }

                    isEndCorrect = true
                }

                // Блок "СВЕДЕНИЯ С ИСПРАВЛЕННЫМИ АДРЕСАМИ " полносью прочитан
                // Кроме блока прочитана еще последующая стрка "ФАЙЛ ПРИНЯТ. СПРАВКИ ЗАПИСАНЫ В БАЗУ ДАННЫХ ИНСПЕКЦИИ" или какая-то другая
                // Поэтому вызываем continue, чтобы повторно не вызвать bufferedReader.readLine()
                continue
            }

            line = bufferedReader.readLine()
        }
    } catch (Exception exception) {
        exception.printStackTrace()
        logger.error("Файл «%s» не загружен: Некорректное формат файла", UploadFileName)
        return null
    } finally {
        IOUtils.closeQuietly(ImportInputStream)
    }

    return result
}

/**
 * Чтение содержание файла 2 НДФЛ - реестр
 */
def readNdfl2ResponseReestrContent() {
    def result = [:]

    try {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ImportInputStream))

        String line = ""
        while (line != null) {
            if (line.contains(NDFL2_TO_FILE)) {
                result.put(NDFL2_TO_FILE, line.replaceAll(NDFL2_FILE_NAME_PATTERN, "\$2"))
            }

            if (line.contains(NDFL2_REGISTER_DATE)) {
                result.put(NDFL2_REGISTER_DATE, line.replaceAll(NDFL2_REGISTER_DATE_PATTERN, "\$1"))
            }

            line = bufferedReader.readLine()
        }
    } catch (Exception exception) {
        exception.printStackTrace()
        logger.error("Файл «%s» не загружен: Некорректное формат файла", UploadFileName)
        return null
    } finally {
        IOUtils.closeQuietly(ImportInputStream)
    }

    return result
}

/**
 * Чтение содержание файла 6 НДФЛ
 */
def readNdfl6ResponseContent() {
    def sett = [:]
    sett.put(NDFL2_KV_FILE_TAG, [NDFL2_KV_FILE_ATTR])
    sett.put(NDFL2_UO_FILE_TAG, [NDFL2_UO_FILE_ATTR])
    sett.put(NDFL2_IV_FILE_TAG, [NDFL2_IV_FILE_ATTR])
    sett.put(NDFL2_UU_FILE_TAG, [NDFL2_UU_FILE_ATTR])

    try {
        SAXParserFactory factory = SAXParserFactory.newInstance()
        SAXParser saxParser = factory.newSAXParser()
        SAXHandler handler = new SAXHandler(sett)
        saxParser.parse(ImportInputStream, handler)

        reportPeriodCode = handler.getValues().get(TAG_DOCUMENT).get(ATTR_PERIOD)
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

    return handler.getValues()
}


/**
 * Загрузка ответов ФНС 2 и 6 НДФЛ
 */
def importNdflResponse() {
    // Выполнить проверку структуры файла ответа на соответствие XSD
    if (isNdfl6Response(UploadFileName)) {
        //TODO xsd
    }

    // Прочитать Имя отчетного файла из файла ответа
    def ndfl2ContentMap = [:]
    def ndfl2ContentReestrMap = [:]
    def reportFileName
    def docWeight = getDocWeight(UploadFileName)

    if (isNdfl2Response(UploadFileName)) {
        if (isNdfl2ResponseProt(UploadFileName)) {
            ndfl2ContentMap = readNdfl2ResponseContent()
            if (ndfl2ContentMap == null) {
                return
            }
            reportFileName = ndfl2ContentMap.get(NDFL2_TO_FILE)
        } else if (isNdfl2ResponseReestr(UploadFileName)) {
            ndfl2ContentReestrMap = readNdfl2ResponseReestrContent()
            if (ndfl2ContentMap == null) {
                return
            }
            reportFileName = ndfl2ContentReestrMap.get(NDFL2_TO_FILE)
        }
    } else {
        def ndfl6Content = readNdfl6ResponseContent()
        reportFileName = getFileName(ndfl6Content, UploadFileName)
    }

    if (reportFileName == null) {
        logger.error("Не найдено имя отчетного файла в файле ответа  \"%s\"", UploadFileName)
        return
    }

    // Выполнить поиск ОНФ, для которой пришел файл ответа по условию
    def fileTypeProvider = refBookFactory.getDataProvider(RefBook.Id.ATTACH_FILE_TYPE.getId())
    def fileTypeId = fileTypeProvider.getUniqueRecordIds(new Date(), "CODE = ${AttachFileType.TYPE_2.id}").get(0)

    def declarationData = declarationService.findDeclarationDataByFileNameAndFileType(reportFileName, fileTypeId)
    if (declarationData == null) {
        //TODO несколкько
        logger.error("Файл ответа \"%s\", для которого сформирован ответ, не найден в отчетных формах", UploadFileName)
        return
    }

    // Проверить ОНФ на отсутствие ранее загруженного Файла ответа по условию: "Имя Файла ответа" не найдено в ОНФ."Файлы и комментарии"
    DeclarationDataFilter declarationFilter = new DeclarationDataFilter()
    declarationFilter.declarationDataId = declarationData.id
    declarationFilter.fileName = UploadFileName
    declarationFilter.searchOrdering = DeclarationDataSearchOrdering.ID
    if (!declarationService.getDeclarationIds(declarationFilter, declarationFilter.getSearchOrdering(), false).isEmpty()) {
        logger.error("Файл ответа \"%s\" уже загружен", UploadFileName)
        return
    }

    def declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
    def declarationFormTypeId = declarationTemplate.declarationFormTypeId
    def formTypeTypeProvider = refBookFactory.getDataProvider(RefBook.Id.DECLARATION_DATA_TYPE_REF_BOOK.getId())
    def formType = formTypeTypeProvider.getRecordData(declarationFormTypeId)
    def formTypeCode = formType.CODE.stringValue

    if (NDFL2_1 == formTypeCode || NDFL2_2 == formTypeCode) {
        if (isNdfl2ResponseReestr(UploadFileName)) {
            // Ничего не делаль: Переход к шагу 6 ОС
        }

        if (isNdfl2ResponseProt(UploadFileName)) {
            def ndflRefProvider = refBookFactory.getDataProvider(RefBook.Id.NDFL_REFERENCES.getId())

            def errorCount = ndfl2ContentMap.get(NDFL2_ERROR_COUNT)
            def notCorrect = ndfl2ContentMap.get(NDFL2_NOT_CORRECT_ERROR)

            // Если значение в строке "КОЛИЧЕСТВО СВЕДЕНИЙ С ОШИБКАМИ" > 0
            if (errorCount > 0) {
                if (!notCorrect) {
                    logger.error("Не найден раздел \"ДОКУМЕНТЫ С ВЫЯВЛЕННЫМИ И НЕИСПРАВЛЕННЫМИ ОШИБКАМИ\" в файле ответа \"%s\"", UploadFileName)
                } else {
                    notCorrect.each { entry ->
                        def ndflRefIds = ndflRefProvider.getUniqueRecordIds(
                                new Date(),
                                "DECLARATION_DATA_ID = ${declarationData.id} AND NUM = ${entry.ref}"
                        )

                        if (ndflRefIds.isEmpty()) {
                            logger.error("В реестре справок формы \"${formTypeCode}\" \"${declarationData.kpp}\" \"${declarationData.oktmo}\" не найдено справки \"${entry.ref}\"", UploadFileName)
                        } else {
                            def ndflRef = ndflRefProvider.getRecordData(ndflRefIds.get(0))

                            ndflRef.ERRTEXT.value = "Путь к реквизиту: \"${entry.path}\"; Значение элемента: \"${entry.val}\"; Текст ошибки: \"${entry.text}\"".toString()
                            ndflRefProvider.updateRecordVersion(logger, ndflRefIds.get(0), null, null, ndflRef)
                        }
                    }
                }
            }

            def correctAddressCount = ndfl2ContentMap.get(NDFL2_CORRECT_ADDRESS_COUNT)
            def correctAddresses = ndfl2ContentMap.get(NDFL2_CORRECT_ADDRESS)

            if (correctAddressCount > 0) {
                if (!correctAddresses) {
                    logger.error("Не найден раздел \"СВЕДЕНИЯ С ИСПРАВЛЕННЫМИ АДРЕСАМИ\" в файле ответа \"%s\"", UploadFileName)
                } else {
                    correctAddresses.each { entry ->
                        def ndflRefIds = ndflRefProvider.getUniqueRecordIds(
                                new Date(),
                                "DECLARATION_DATA_ID = ${declarationData.id} AND NUM = ${entry.ref}"
                        )

                        if (ndflRefIds.isEmpty()) {
                            logger.error("В реестре справок формы \"${formTypeCode}\" \"${declarationData.kpp}\" \"${declarationData.oktmo}\" не найдено справки \"${entry.ref}\"", UploadFileName)
                        } else {
                            def ndflRef = ndflRefProvider.getRecordData(ndflRefIds.get(0))

                            if (entry.valid) {
                                ndflRef.ERRTEXT.value = "Текст ошибки от ФНС: \"${entry.addressBefore}\"; (Адрес признан верным (ИФНСМЖ - ${entry.valid}))".toString()
                            } else {
                                ndflRef.ERRTEXT.value = "Текст ошибки от ФНС: \"${entry.addressBefore}\" ДО исправления; (\"${entry.addressAfter}\" ПОСЛЕ исправления)".toString()
                            }
                            ndflRefProvider.updateRecordVersion(logger, ndflRefIds.get(0), null, null, ndflRef)
                        }
                    }
                }
            }

            if (logger.containsLevel(LogLevel.ERROR)) {
                return
            }
        }
    }
    // "Дата-время файла" = "Дата и время документа" раздела Параметры файла ответа
    def fileDate = null

    if (isNdfl6Response(UploadFileName)) {
        fileDate = Date.parse("yyyy", UploadFileName.replaceAll(NDFL6_FILE_NAME_PATTERN, "\$4"))
    } else if (isNdfl2ResponseReestr(UploadFileName)) {
        fileDate = Date.parse("dd.MM.yyyy", ndfl2ContentReestrMap.get(NDFL2_REGISTER_DATE))
    } else if (isNdfl2ResponseProt(UploadFileName)) {
        fileDate = Date.parse("dd.MM.yyyy", ndfl2ContentMap.get(NDFL2_PROTOCOL_DATE))
    }

    // Сохранение файла ответа в форме
    def fileUuid = blobDataServiceDaoImpl.create(dataFile, UploadFileName, new Date())
    def createUser = declarationService.getSystemUserInfo().getUser()

    def declarationDataFile = new DeclarationDataFile()
    declarationDataFile.setDeclarationDataId(declarationData.id)
    declarationDataFile.setUuid(fileUuid)
    declarationDataFile.setUserName(createUser.getName())
    declarationDataFile.setUserDepartmentName(departmentService.getParentsHierarchyShortNames(createUser.getDepartmentId()))
    declarationDataFile.setFileTypeId(fileTypeId)
    declarationDataFile.setDate(fileDate)

    declarationService.saveFile(declarationDataFile)

    def declarationDataFileMaxWeight = declarationService.findFileWithMaxWeight(declarationData.id)
    def prevWeight

    if (declarationDataFileMaxWeight != null) {
        prevWeight = getDocWeight(declarationDataFileMaxWeight.fileName)
    }

    if (prevWeight == null || prevWeight <= docWeight) {
        def nextKnd

        def kndAccept = 1166002	// Принят
        def kndRefuse = 1166006	// Отклонен
        def kndSuccess = 1166007 //	Успешно отработан
        def kndRequired = 1166009 // Требует уточнения

        if (isNdfl2ResponseProt(UploadFileName)) {
            def errorCount = ndfl2ContentMap.get(NDFL2_ERROR_COUNT)
            if (errorCount && errorCount > 0) {
                // Требует уточнения
                nextKnd = kndRequired
            } else {
                //Принят
                nextKnd = kndAccept
            }
        }

        //Принят
        if (UploadFileName.startsWith(NDFL6_PATTERN_1)) {
            nextKnd = kndAccept
        }

        //Отклонен
        if (UploadFileName.startsWith(NDFL6_PATTERN_2)) {
            nextKnd = kndRefuse
        }

        //Успешно обработан
        if (UploadFileName.startsWith(NDFL6_PATTERN_3)) {
            nextKnd = kndSuccess
        }

        //Требует уточнения
        if (UploadFileName.startsWith(NDFL6_PATTERN_4)) {
            nextKnd = kndRequired
        }

        if (nextKnd != null) {
            def docStateProvider = refBookFactory.getDataProvider(RefBook.Id.DOC_STATE.getId())
            def docStateId = docStateProvider.getUniqueRecordIds(new Date(), "KND = '${nextKnd}'").get(0)
            declarationService.setDocStateId(declarationData.id, docStateId)
        }
    }
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
    } else {
        // Неизвестный формат имени загружаемого файла
        throw new IllegalArgumentException(String.format(ERROR_NAME_FORMAT, UploadFileName));
    }

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