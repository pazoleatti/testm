package refbook // declaration_type_ref комментарий для локального поиска скрипта

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.SharedConstants
import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService
import com.aplana.sbrf.taxaccounting.script.service.*
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService
import com.aplana.sbrf.taxaccounting.service.LogBusinessService
import com.aplana.sbrf.taxaccounting.service.impl.TAAbstractScriptingServiceImpl
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.apache.commons.io.IOUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import org.xml.sax.helpers.DefaultHandler

import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.regex.Pattern


(new DeclarationType(this)).run()

@TypeChecked
@SuppressWarnings("GrMethodMayBeStatic")
class DeclarationType extends AbstractScriptClass {
    private static final Log LOG = LogFactory.getLog(DeclarationType.class)

    String UploadFileName
    InputStream ImportInputStream
    File dataFile
    TransportFileType fileType
    RefBookFactory refBookFactory
    DeclarationService declarationService
    TAUserInfo userInfo
    DepartmentService departmentService
    StringBuilder msgBuilder
    BlobDataService blobDataServiceDaoImpl
    DepartmentReportPeriodService departmentReportPeriodService
    ReportPeriodService reportPeriodService
    ScriptSpecificRefBookReportHolder scriptSpecificRefBookReportHolder
    DeclarationTemplateService declarationTemplateService
    OutputStream outputStream
    int reportYear
    NdflPersonService ndflPersonService
    LogBusinessService logBusinessService
    String version

    private DeclarationType() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    DeclarationType(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("departmentReportPeriodService")) {
            this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService")
        }
        if (scriptClass.getBinding().hasVariable("declarationService")) {
            this.declarationService = (DeclarationService) scriptClass.getProperty("declarationService")
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService")
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService")
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService")
        }
        if (scriptClass.getBinding().hasVariable("userInfo")) {
            this.userInfo = (TAUserInfo) scriptClass.getProperty("userInfo")
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory")
        }
        if (scriptClass.getBinding().hasVariable("blobDataServiceDaoImpl")) {
            this.blobDataServiceDaoImpl = (BlobDataService) scriptClass.getBinding().getProperty("blobDataServiceDaoImpl")
        }
        if (scriptClass.getBinding().hasVariable("msgBuilder")) {
            msgBuilder = (StringBuilder) scriptClass.getBinding().getProperty("msgBuilder")
        }
        if (scriptClass.getBinding().hasVariable("dataFile")) {
            this.dataFile = (File) scriptClass.getProperty("dataFile")
        }
        if (scriptClass.getBinding().hasVariable("fileType")) {
            this.fileType = (TransportFileType) scriptClass.getProperty("fileType")
        }
        if (scriptClass.getBinding().hasVariable("UploadFileName")) {
            this.UploadFileName = (String) scriptClass.getProperty("UploadFileName")
        }
        if (scriptClass.getBinding().hasVariable("ImportInputStream")) {
            this.ImportInputStream = (InputStream) scriptClass.getProperty("ImportInputStream")
        }
        if (scriptClass.getBinding().hasVariable("reportYear")) {
            this.reportYear = (int) scriptClass.getProperty("reportYear")
        }
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService")
        }
        if (scriptClass.getBinding().hasVariable("dataHolder")) {
            this.scriptSpecificRefBookReportHolder = (ScriptSpecificRefBookReportHolder) scriptClass.getProperty("dataHolder")
            this.outputStream = scriptSpecificRefBookReportHolder.getFileOutputStream()
        }
        if (scriptClass.getBinding().hasVariable("version")) {
            this.version = (String) scriptClass.getProperty("version")
        }
        if (scriptClass.getBinding().hasVariable("logBusinessService")) {
            this.logBusinessService = (LogBusinessService) scriptClass.getProperty("logBusinessService")
        }
    }

    @Override
    void run() {
        switch (formDataEvent) {
            case FormDataEvent.IMPORT_TRANSPORT_FILE:
                importTF()
                break
            case FormDataEvent.CREATE_APPLICATION_2:
                createApplication2()
                break
        }
    }

    class SAXHandler extends DefaultHandler {
        // Хранит содержимое атрибута
        private Map<String, Map<String, String>> attrValues
        private Map<String, List<String>> findAttrNames

        // Хранит содержимое узла
        private List<String> nodeValueList
        private boolean isNodeNameFind
        private boolean isParentNodeNameFind
        private boolean isGetValueAttributesTag = false
        private String nodeNameFind
        private String parentNodeNameFind

        //Хранит содержимое атрибутов(название атрибута, значение)
        private Map<String, String> ListValueAttributesTag = null


        SAXHandler(Map<String, List<String>> findAttrNames) {
            this.findAttrNames = findAttrNames
        }

        SAXHandler(String nodeNameFind, String parentNodeNameFind) {
            this.findAttrNames = new HashMap<String, List<String>>()
            this.nodeValueList = new ArrayList<>()
            this.nodeNameFind = nodeNameFind
            this.parentNodeNameFind = parentNodeNameFind
        }

        SAXHandler(String nodeNameFind, String parentNodeNameFind, boolean isGetValueAttributes) {
            this(nodeNameFind, parentNodeNameFind)
            this.ListValueAttributesTag = new HashMap<String, String>()
            this.isGetValueAttributesTag = isGetValueAttributes
        }


        Map<String, String> getListValueAttributesTag() {
            return ListValueAttributesTag
        }

        Map<String, Map<String, String>> getAttrValues() {
            return attrValues
        }

        List<String> getNodeValueList() {
            return nodeValueList
        }

        @Override
        void startDocument() throws SAXException {
            attrValues = new HashMap<String, Map<String, String>>()
            for (Map.Entry<String, List<String>> entry : findAttrNames.entrySet()) {
                attrValues.put(entry.getKey(), new HashMap<String, String>())
            }
        }

        @Override
        void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            for (Map.Entry<String, List<String>> entry : findAttrNames.entrySet()) {
                if (entry.getKey() == qName) {
                    for (String attrName : entry.getValue()) {
                        attrValues.get(qName).put(attrName, attributes.getValue(attrName))
                    }
                }
            }
            if (qName == nodeNameFind) {
                isNodeNameFind = true
            }
            if (qName == parentNodeNameFind) {
                isParentNodeNameFind = true
            }
            //Заполняем атрибуты тега
            if (isGetValueAttributesTag && isParentNodeNameFind && isNodeNameFind) {
                for (int i = 0; i < attributes.length; i++) {
                    ListValueAttributesTag.put(attributes.getQName(i), attributes.getValue(attributes.getQName(i)))
                }
            }
        }

        @Override
        void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (qName == nodeNameFind) {
                isNodeNameFind = false
            }
            if (qName == parentNodeNameFind) {
                isParentNodeNameFind = false
            }
        }

        @Override
        void characters(char[] ch, int start, int length) throws SAXException {
            if (isNodeNameFind && isParentNodeNameFind) {
                nodeValueList.add(new String(ch, start, length))
            }
        }
    }

    String ERROR_NAME_FORMAT = "Имя транспортного файла «%s» не соответствует формату!"
    String ERROR_NOT_FOUND_FORM = "Не найдена форма, содержащая «%s», для файла ответа «%s»"

    final String NDFL2_PATTERN_PROT_1 = "PROT_NO_NDFL2"
    final String NDFL2_PATTERN_PROT_2 = "прот_NO_NDFL2"
    final String NDFL2_PATTERN_REESTR_1 = "REESTR_NO_NDFL2"
    final String NDFL2_PATTERN_REESTR_2 = "реестр_NO_NDFL2"
    final String ANSWER_PATTERN_1 = "KV_"
    final String ANSWER_PATTERN_2 = "UO_"
    final String ANSWER_PATTERN_3 = "IV_"
    final String ANSWER_PATTERN_4 = "UU_"
    final String ANSWER_PATTERN_NDFL_1 = "KV_NONDFL"
    final String ANSWER_PATTERN_NDFL_2 = "UO_NONDFL"
    final String ANSWER_PATTERN_NDFL_3 = "IV_NONDFL"
    final String ANSWER_PATTERN_NDFL_4 = "UU_NONDFL"
    final String NDFL2_KV_FILE_TAG = "СвКвит"
    final String NDFL2_KV_FILE_ATTR = "ИмяОбрабФайла"
    final String NDFL2_UO_FILE_TAG = "ОбщСвУвед"
    final String NDFL2_UO_FILE_ATTR = "ИмяОбрабФайла"
    final String NDFL2_IV_FILE_TAG = "СвИзвещВ"
    final String NDFL2_IV_FILE_ATTR = "ИмяОбрабФайла"
    final String NDFL2_UU_FILE_TAG = "ОбщСвУвед"
    final String NDFL2_UU_FILE_ATTR = "ИмяОбрабФайла"
    final String NDFL2_1 = "2 НДФЛ (1)"
    final String NDFL2_2 = "2 НДФЛ (2)"
    final String NDFL6 = "6 НДФЛ"
    final String NDFL6_FILENAME_PREFIX = "NO_NDFL6_"

    final KND_ACCEPT = 1166002 // Принят
    final KND_REFUSE = 1166006 // Отклонен
    final KND_SUCCESS = 1166007 // Успешно отработан
    final KND_REQUIRED = 1166009 // Требует уточнения

    // Идентификаторы видов деклараций
    final Integer DECLARATION_TYPE_RNU_NDFL_ID = 100

    String NAME_EXTENSION_DEC = ".xml"
    int NAME_LENGTH_QUARTER_DEC = 63

    // Кэш провайдеров
    def providerCache = [:]

    Map<String, String> getPeriodNdflMap() {
        Map<String, String> periodNdflMap = new HashMap<String, String>()
        periodNdflMap.put("21", "21")
        periodNdflMap.put("32", "31")
        periodNdflMap.put("33", "33")
        periodNdflMap.put("34", "34")
        return periodNdflMap
    }

    def importTF() {
        switch (fileType) {
            case TransportFileType.RNU_NDFL:
                importNDFL()
                break
            case TransportFileType.RESPONSE_6_NDFL:
                importNdflResponse()
                break
            case TransportFileType.RESPONSE_2_NDFL:
                importNdflResponse()
                break
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
 * Проверяет является ли файл ответом от ФНС 2 НДФЛ
 */
    def isNdfl2Response(String fileName) {
        return isNdfl2ResponseProt(fileName) || isNdfl2ResponseReestr(fileName)
    }

/**
 * Если файл ответа == "Протокол Приема 2НДФЛ"
 */
    def isNdfl2ResponseProt(String fileName) {
        return fileName.toLowerCase().startsWith(NDFL2_PATTERN_PROT_1.toLowerCase()) ||
                fileName.toLowerCase().startsWith(NDFL2_PATTERN_PROT_2.toLowerCase())
    }

/**
 * Если файл ответа == "Реестр Принятых Документов"
 */
    def isNdfl2ResponseReestr(String fileName) {
        return fileName.toLowerCase().startsWith(NDFL2_PATTERN_REESTR_1.toLowerCase()) ||
                fileName.toLowerCase().startsWith(NDFL2_PATTERN_REESTR_2.toLowerCase())
    }

/**
 * Проверяет является ли файл ответом от ФНС 6 НДФЛ
 */
    def isNdfl6Response(String fileName) {
        return fileName.startsWith(ANSWER_PATTERN_NDFL_1) ||
                fileName.startsWith(ANSWER_PATTERN_NDFL_2) ||
                fileName.startsWith(ANSWER_PATTERN_NDFL_3) ||
                fileName.startsWith(ANSWER_PATTERN_NDFL_4)
    }

/**
 * Возвращает имя отчетного файла для 6НДФЛ
 */
    def getFileName(Map<String, Map<String, List<String>>> contentMap, String fileName) {
        if (fileName.startsWith(ANSWER_PATTERN_NDFL_1)) {
            Map<String, List<String>> names = contentMap.get(NDFL2_KV_FILE_TAG)
            return names.get(NDFL2_KV_FILE_ATTR).find {String name -> name.startsWith(NDFL6_FILENAME_PREFIX)}
        }

        if (fileName.startsWith(ANSWER_PATTERN_NDFL_2)) {
            Map<String, List<String>> names = contentMap.get(NDFL2_UO_FILE_TAG)
            return names.get(NDFL2_UO_FILE_ATTR).find {String name -> name.startsWith(NDFL6_FILENAME_PREFIX)}
        }

        if (fileName.startsWith(ANSWER_PATTERN_NDFL_3)) {
            return contentMap.get(NDFL2_IV_FILE_TAG).get(NDFL2_IV_FILE_ATTR).get(0)
        }

        if (fileName.startsWith(ANSWER_PATTERN_NDFL_4)) {
            Map<String, List<String>> names = contentMap.get(NDFL2_UU_FILE_TAG)
            return names.get(NDFL2_UU_FILE_ATTR).find {String name -> name.startsWith(NDFL6_FILENAME_PREFIX)}
        }

        return null
    }

/**
 * Определет вес документа
 */
    Integer getDocWeight(String fileName) {
        if (isNdfl2Response(fileName)) {
            return 1
        }

        if (fileName.startsWith(ANSWER_PATTERN_NDFL_1) || fileName.startsWith(ANSWER_PATTERN_NDFL_2)) {
            return 1
        }

        return 2
    }

    final String NDFL2_TO_FILE = "К ФАЙЛУ"
    final String NDFL2_ERROR_COUNT = "КОЛИЧЕСТВО СВЕДЕНИЙ С ОШИБКАМИ"
    final String NDFL2_NOT_CORRECT_ERROR = "ДОКУМЕНТЫ С ВЫЯВЛЕННЫМИ И НЕИСПРАВЛЕННЫМИ ОШИБКАМИ"
    final String NDFL2_CORRECT_ADDRESS_COUNT = "КОЛИЧЕСТВО СВЕДЕНИЙ С ИСПРАВЛЕННЫМИ АДРЕСАМИ"
    final String NDFL2_CORRECT_ADDRESS = "СВЕДЕНИЯ С ИСПРАВЛЕННЫМИ АДРЕСАМИ"
    final String NDFL2_FILE_NAME_PATTERN = "(.+)\\\\([^/\\\\]+\\.(xml|XML))\\s*"
    final String NDFL2_NUMBER_PATTERN = "\\s*(.+):\\s*(\\d+)\\s*"
    final String NDFL2_STR_PATTERN = "\\s*(.+):\\s*(.+)\\s*"
    final String NDFL2_NOT_CORRECT_NUMB = "Номер п/п:"
    final String NDFL2_NOT_CORRECT_NUMB_REF = "Номер справки:"
    final String NDFL2_NOT_CORRECT_PATH = "Путь к реквизиту:"
    final String NDFL2_NOT_CORRECT_VALUE = "Значение элемента:"
    final String NDFL2_NOT_CORRECT_TEXT = "Текст ошибки:"
    final String NDFL2_CORRECT_NUMB_REF = "Номер справки:"
    final Pattern NDFL2_CORRECT_ADDRESS_PATTERN_BEFORE = Pattern.compile("\\s*Адрес ДО исправления:.+")
    final Pattern NDFL2_CORRECT_ADDRESS_PATTERN_AFTER = Pattern.compile("\\s*Адрес ПОСЛЕ исправления:.+")
    final Pattern NDFL2_CORRECT_ADDRESS_PATTERN_VALID = Pattern.compile("\\s*Адрес ПРИЗНАН ВЕРНЫМ \\(ИФНСМЖ - (.+)\\)\\s*")
    final String NDFL2_PROTOCOL_DATE = "ПРОТОКОЛ №"
    final Pattern NDFL2_PROTOCOL_DATE_PATTERN = Pattern.compile("ПРОТОКОЛ № .+ от (\\d{2}\\.\\d{2}\\.\\d{4})")
    final String NDFL2_REGISTER_DATE = "РЕЕСТР N"
    final Pattern NDFL2_REGISTER_DATE_PATTERN = Pattern.compile("РЕЕСТР N .+ от (\\d{2}\\.\\d{2}\\.\\d{4}) в 9979")
    final int sizeLastEntry = 4

/**
 * Чтение содержание файла 2 НДФЛ - протокол
 */
    Map<Object, Object> readNdfl2ResponseContent() {
        Map<Object, Object> result = [:]
        List<Map<String, String>> notCorrectList = []
        List<Map<String, String>> correctList = []
        result.put(NDFL2_NOT_CORRECT_ERROR, notCorrectList)
        result.put(NDFL2_CORRECT_ADDRESS, correctList)

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ImportInputStream))

            String line = ""
            while (line != null) {
                if (line.contains(NDFL2_TO_FILE)) {
                    if (line ==~ NDFL2_FILE_NAME_PATTERN) {
                        result.put(NDFL2_TO_FILE, line.replaceAll(NDFL2_FILE_NAME_PATTERN, "\$2"))
                    } else {
                        result.put(NDFL2_TO_FILE, null)
                    }
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
                    Map<String, String> lastEntry = [:]
                    boolean isEndNotCorrect = false

                    while (!isEndNotCorrect) {
                        line = bufferedReader.readLine()

                        //Необходимо условие если две ошибки идут подряд и не разделены между собой спец строкой
                        if ((lastEntry != null) && (lastEntry.size() == sizeLastEntry)) {
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

                    if (!lastEntry.isEmpty()) {
                        notCorrectList.add(lastEntry)
                    }

                    // Блок "ДОКУМЕНТЫ С ВЫЯВЛЕННЫМИ И НЕИСПРАВЛЕННЫМИ ОШИБКАМИ" полносью прочитан
                    // Кроме блока прочитана еще последующая стрка "СВЕДЕНИЯ С ИСПРАВЛЕННЫМИ АДРЕСАМИ" или какая-то другая
                    // Поэтому вызываем continue, чтобы повторно не вызвать bufferedReader.readLine()
                    continue
                }

                if (line.contains(NDFL2_CORRECT_ADDRESS)) {
                    Map<String, String> lastEntry = [:]
                    boolean isEndCorrect = false

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
    Map<String, String> readNdfl2ResponseReestrContent() {
        Map<String, String> result = [:]

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ImportInputStream))

            String line = ""
            while (line != null) {
                if (line.contains(NDFL2_TO_FILE)) {
                    result.put(NDFL2_TO_FILE, line.replaceAll(NDFL2_STR_PATTERN, "\$2"))
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
    Map<String, Map<String, List<String>>> readNdfl6ResponseContent() {
        def sett = [:]

        SAXHandler handler = null

        if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_1)) {
            handler = new SAXHandler('ИмяОбрабФайла', 'СвКвит')
        }

        if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_2)) {
            handler = new SAXHandler('ИмяОбрабФайла', NDFL2_UO_FILE_TAG)
        }

        if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_3)) {
            sett.put(NDFL2_IV_FILE_TAG, [NDFL2_IV_FILE_ATTR])
            handler = new SAXHandler(sett)
        }

        if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_4)) {
            handler = new SAXHandler('ИмяОбрабФайла', NDFL2_UU_FILE_TAG)
        }

        InputStream inputStream = null
        try {
            inputStream = new FileInputStream(dataFile)
            SAXParserFactory factory = SAXParserFactory.newInstance()
            SAXParser saxParser = factory.newSAXParser()
            saxParser.parse(inputStream, handler)
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
            IOUtils.closeQuietly(inputStream)
        }

        Map<String, Map<String, List<String>>> result = [:]
        Map<String, List<String>> value = [:]
        if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_1)) {
            value.put(NDFL2_KV_FILE_ATTR, handler.nodeValueList.size() > 0 ? handler.nodeValueList : null)
            result.put(NDFL2_KV_FILE_TAG, value)
        } else if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_2)) {
            value.put(NDFL2_UO_FILE_ATTR, handler.nodeValueList.size() > 0 ? handler.nodeValueList : null)
            result.put(NDFL2_UO_FILE_TAG, value)
        } else if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_3)) {
            value.put(NDFL2_IV_FILE_ATTR, [handler.getAttrValues().get(NDFL2_IV_FILE_TAG).get(NDFL2_IV_FILE_ATTR)])
            result.put(NDFL2_IV_FILE_TAG, value)
        } else if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_4)) {
            value.put(NDFL2_UU_FILE_ATTR, handler.nodeValueList.size() > 0 ? handler.nodeValueList : null)
            result.put(NDFL2_UU_FILE_TAG, value)
        }

        return result
    }

    /**
     * Загрузка ответов ФНС 2 и 6 НДФЛ
     */
    def importNdflResponse() {
        // Прочитать Имя отчетного файла из файла ответа
        Map<Object, Object> ndfl2ContentMap = [:]
        Map<String, String> ndfl2ContentReestrMap = [:]
        String reportFileName
        Integer docWeight = getDocWeight(UploadFileName)
        if (isNdfl2Response(UploadFileName)) {
            if (isNdfl2ResponseProt(UploadFileName)) {
                ndfl2ContentMap = readNdfl2ResponseContent()
                if (ndfl2ContentMap == null) {
                    return
                }
                reportFileName = (String) ndfl2ContentMap.get(NDFL2_TO_FILE)
            } else if (isNdfl2ResponseReestr(UploadFileName)) {
                ndfl2ContentReestrMap = readNdfl2ResponseReestrContent()
                if (ndfl2ContentMap == null) {
                    return
                }
                reportFileName = (String) ndfl2ContentReestrMap.get(NDFL2_TO_FILE)
            }
        } else {
            Map<String, Map<String, List<String>>> ndfl6Content = readNdfl6ResponseContent()

            if (ndfl6Content == null) {
                return
            }

            reportFileName = getFileName(ndfl6Content, (String) UploadFileName)
        }

        if (reportFileName == null || reportFileName == "") {
            logger.error("Не найдено имя отчетного файла в файле ответа  \"%s\"", UploadFileName)
            return
        }

        // Выполнить поиск ОНФ, для которой пришел файл ответа по условию
        def fileTypeProvider = refBookFactory.getDataProvider(RefBook.Id.ATTACH_FILE_TYPE.getId())
        def fileTypeId = fileTypeProvider.getUniqueRecordIds(new Date(), "CODE = ${AttachFileType.OUTGOING_TO_FNS.code}").get(0)
        List<DeclarationData> declarationDataList = declarationService.findDeclarationDataByFileNameAndFileType(reportFileName, fileTypeId)

        if (declarationDataList.isEmpty()) {
            logger.error(ERROR_NOT_FOUND_FORM, reportFileName, UploadFileName)
            return
        }
        if (declarationDataList.size() > 1) {
            def result = ""
            declarationDataList.each { DeclarationData declData ->
                result += "\"${AttachFileType.OUTGOING_TO_FNS.title}\", \"${declData.kpp}\", \"${declData.oktmo}\" "
            }
            logger.error(ERROR_NOT_FOUND_FORM + ": " + result, reportFileName, UploadFileName)
            return
        }
        DeclarationData declarationData = declarationDataList.get(0)

        // Проверить ОНФ на отсутствие ранее загруженного Файла ответа по условию: "Имя Файла ответа" не найдено в ОНФ."Файлы и комментарии"
        def beforeUploadDeclarationDataList = declarationService.findDeclarationDataByFileNameAndFileType(UploadFileName, null)
        if (!beforeUploadDeclarationDataList.isEmpty()) {
            return
        }

        DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationData.declarationTemplateId)
        Long declarationFormTypeId = declarationTemplate.declarationFormTypeId
        def formTypeTypeProvider = refBookFactory.getDataProvider(RefBook.Id.DECLARATION_DATA_TYPE_REF_BOOK.getId())
        Map<String, RefBookValue> formType = formTypeTypeProvider.getRecordData(declarationFormTypeId)
        String formTypeCode = formType.CODE.stringValue

        // Выполнить проверку структуры файла ответа на соответствие XSD
        InputStream inputStream = null
        if (NDFL6 == formTypeCode) {
            SAXHandler handl = new SAXHandler('Файл', 'Файл', true)
            try {
                inputStream = new FileInputStream((File) dataFile)
                SAXParserFactory factory = SAXParserFactory.newInstance()
                SAXParser saxParser = factory.newSAXParser()
                saxParser.parse((InputStream) inputStream, handl)
            } catch (Exception e) {

                e.printStackTrace()

            } finally {
                IOUtils.closeQuietly(inputStream)
            }
            handl.getListValueAttributesTag().get('xsi:noNamespaceSchemaLocation')

            DeclarationTemplateFile templateFile = null
            for (DeclarationTemplateFile tf : declarationTemplate.declarationTemplateFiles) {
                if (tf.fileName.equals(handl.getListValueAttributesTag().get('xsi:noNamespaceSchemaLocation'))) {
                    templateFile = tf
                    break
                }
            }

            if (!templateFile) {
                logger.error("Для файл ответа \"%s\" не найдена xsd схема", UploadFileName)
                return
            }

            declarationService.validateDeclaration(logger, dataFile, UploadFileName, templateFile.blobDataId)

            if (logger.containsLevel(LogLevel.ERROR)) {
                logger.error("Файл ответа \"%s\" не соответствует формату", UploadFileName)
                return
            }
        }

        if (NDFL2_1 == formTypeCode || NDFL2_2 == formTypeCode) {
            if (isNdfl2ResponseReestr(UploadFileName)) {
                // Ничего не делаль: Переход к шагу 6 ОС
            }

            if (isNdfl2ResponseProt(UploadFileName)) {
                def ndflRefProvider = refBookFactory.getDataProvider(RefBook.Id.NDFL_REFERENCES.getId())

                Integer errorCount = (Integer) ndfl2ContentMap.get(NDFL2_ERROR_COUNT)
                List<Map<String, String>> notCorrect = (List<Map<String, String>>) ndfl2ContentMap.get(NDFL2_NOT_CORRECT_ERROR)

                // Если значение в строке "КОЛИЧЕСТВО СВЕДЕНИЙ С ОШИБКАМИ" > 0
                if (errorCount > 0) {
                    if (!notCorrect) {
                        logger.error("Не найден раздел \"ДОКУМЕНТЫ С ВЫЯВЛЕННЫМИ И НЕИСПРАВЛЕННЫМИ ОШИБКАМИ\" в файле ответа \"%s\"", UploadFileName)
                    } else {
                        notCorrect.each { Map<String, String> entry ->
                            def ndflRefIds = ndflRefProvider.getUniqueRecordIds(
                                    new Date(),
                                    "DECLARATION_DATA_ID = ${declarationData.id} AND NUM = ${entry.ref}"
                            )

                            if (ndflRefIds.isEmpty()) {
                                logger.error("В реестре справок формы \"${formTypeCode}\" \"${declarationData.kpp}\" \"${declarationData.oktmo}\" не найдено справки \"${entry.ref}\"", UploadFileName)
                            } else {
                                def ndflRef = ndflRefProvider.getRecordData(ndflRefIds.get(0))

                                ndflRef.ERRTEXT.value = "Путь к реквизиту: \"${entry.path}\" Значение элемента: \"${entry.val}\" Текст ошибки: \"${entry.text}\"".toString()
                                ndflRefProvider.updateRecordVersion(logger, ndflRefIds.get(0), null, null, ndflRef)
                            }
                        }
                    }
                }

                Integer correctAddressCount = (Integer) ndfl2ContentMap.get(NDFL2_CORRECT_ADDRESS_COUNT)
                List<Map<String, String>> correctAddresses = (List<Map<String, String>>) ndfl2ContentMap.get(NDFL2_CORRECT_ADDRESS)

                if (correctAddressCount > 0) {
                    if (!correctAddresses) {
                        logger.error("Не найден раздел \"СВЕДЕНИЯ С ИСПРАВЛЕННЫМИ АДРЕСАМИ\" в файле ответа \"%s\"", UploadFileName)
                    } else {
                        correctAddresses.each { Map<String, String> entry ->
                            List<Long> ndflRefIds = ndflRefProvider.getUniqueRecordIds(
                                    new Date(),
                                    "DECLARATION_DATA_ID = ${declarationData.id} AND NUM = ${entry.ref}".toString()
                            )

                            if (ndflRefIds.isEmpty()) {
                                logger.error("В реестре справок формы \"${formTypeCode}\" \"${declarationData.kpp}\" \"${declarationData.oktmo}\" не найдено справки \"${entry.ref}\"", UploadFileName)
                            } else {
                                def ndflRef = ndflRefProvider.getRecordData(ndflRefIds.get(0))

                                String errtext
                                if (ndflRef.ERRTEXT.value == null || ndflRef.ERRTEXT.value.toString().isEmpty()) {
                                    errtext = ""
                                } else {
                                    errtext = ndflRef.ERRTEXT.value.toString() + ".\n"
                                }
                                if (entry.valid) {
                                    ndflRef.ERRTEXT.setValueForce(errtext + "Текст ошибки от ФНС: \"${entry.addressBefore}\" (Адрес признан верным (ИФНСМЖ - ${entry.valid}))".toString())
                                } else {
                                    ndflRef.ERRTEXT.setValueForce(errtext + "Текст ошибки от ФНС: \"${entry.addressBefore}\" ДО исправления (\"${entry.addressAfter}\" ПОСЛЕ исправления)".toString())
                                }
                                ndflRefProvider.updateRecordVersion(logger, ndflRefIds.get(0), null, null, ndflRef)
                            }
                        }
                    }
                }

                if (logger.containsLevel(LogLevel.ERROR)) {
                    return
                }
                def departmentReportPeriod = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)
                def departmentName = departmentService.get(declarationData.departmentId)

                msgBuilder.append("Выполнена загрузка ответа ФНС для формы: ")
                        .append("№: \"").append(declarationData.id).append("\"")
                        .append(", Период: \"").append(departmentReportPeriod.reportPeriod.getTaxPeriod().getYear() + " - " + departmentReportPeriod.reportPeriod.getName()).append("\"")
                        .append(", Подразделение: \"").append(departmentName.getName()).append("\"")
                        .append(", Вид: \"").append(declarationTemplate.type.getName()).append("\"")
                logger.info(msgBuilder.toString())
            }
        }
        // "Дата-время файла" = "Дата и время документа" раздела Параметры файла ответа
        Date fileDate = null

        if (isNdfl6Response(UploadFileName)) {
            String[] fileNameParts = UploadFileName.split("_")
            String date = fileNameParts[fileNameParts.length - 2]
            fileDate = Date.parse("yyyyMMdd", date)
        } else if (isNdfl2ResponseReestr(UploadFileName)) {
            fileDate = Date.parse("dd.MM.yyyy", ndfl2ContentReestrMap.get(NDFL2_REGISTER_DATE) as String)
        } else if (isNdfl2ResponseProt(UploadFileName)) {
            fileDate = Date.parse("dd.MM.yyyy", (String) ndfl2ContentMap.get(NDFL2_PROTOCOL_DATE))
        }

        // Сохранение файла ответа в форме

        def fileUuid = blobDataServiceDaoImpl.create(dataFile, UploadFileName, new Date())
        def createUser = declarationService.getSystemUserInfo().getUser()
        def fileTypeSaveId = fileTypeProvider.getUniqueRecordIds(new Date(), "CODE = ${AttachFileType.INCOMING_FROM_FNS.code}").get(0)

        def declarationDataFile = new DeclarationDataFile()
        declarationDataFile.setDeclarationDataId(declarationData.id)
        declarationDataFile.setUuid(fileUuid)
        declarationDataFile.setUserName(createUser.getName())
        declarationDataFile.setUserDepartmentName(departmentService.getParentsHierarchyShortNames(createUser.getDepartmentId()))
        declarationDataFile.setFileTypeId(fileTypeSaveId)
        declarationDataFile.setDate(fileDate)

        declarationService.saveFile(declarationDataFile)

        def declarationDataFileMaxWeight = declarationService.findFileWithMaxWeight(declarationData.id)
        Integer prevWeight

        if (declarationDataFileMaxWeight != null) {
            prevWeight = getDocWeight(declarationDataFileMaxWeight.fileName)
        }

        if (prevWeight == null || prevWeight <= docWeight) {
            def nextKnd

            if (isNdfl2ResponseProt(UploadFileName)) {
                Integer errorCount = (Integer) ndfl2ContentMap.get(NDFL2_ERROR_COUNT)
                if (errorCount && errorCount > 0) {
                    // Требует уточнения
                    nextKnd = KND_REQUIRED
                } else {
                    //Принят
                    nextKnd = KND_ACCEPT
                }
            }

            //Принят
            if (UploadFileName.startsWith(ANSWER_PATTERN_1)) {
                nextKnd = KND_ACCEPT
            }

            //Отклонен
            if (UploadFileName.startsWith(ANSWER_PATTERN_2)) {
                nextKnd = KND_REFUSE
            }

            //Успешно обработан
            if (UploadFileName.startsWith(ANSWER_PATTERN_3)) {
                nextKnd = KND_SUCCESS
            }

            //Требует уточнения
            if (UploadFileName.startsWith(ANSWER_PATTERN_4)) {
                nextKnd = KND_REQUIRED
            }

            if (nextKnd != null) {
                def docStateProvider = refBookFactory.getDataProvider(RefBook.Id.DOC_STATE.getId())
                def docStateId = docStateProvider.getUniqueRecordIds(new Date(), "KND = '${nextKnd}'").get(0)
                if (declarationData.docStateId != docStateId) {
                    declarationService.setDocStateId(declarationData.id, docStateId)
                }
            }
        }

        logBusinessService.logFormEvent(declarationData.id, FormDataEvent.ATTACH_RESPONSE_FILE, logger.getLogId(),
                "Загружен файл ответа: $UploadFileName, для формы № $declarationData.id.", userInfo ?: declarationService.getSystemUserInfo())

        declarationService.createPdfReport(logger, declarationData, userInfo)
    }

    /**
     * Валидирует xml-файл по xsd схеме макета
     * @param declarationTemplate макет формы
     */
    boolean validate(DeclarationTemplate declarationTemplate) {
        declarationService.validateDeclaration(logger, dataFile, UploadFileName, declarationService.findXsdIdByTemplateId(declarationTemplate.id))

        if (logger.containsLevel(LogLevel.ERROR)) {
            return false
        }
        return true
    }

//Параметры имени ТФ
    final String KOD_DEPARTMENT = "КодПодр"
    final String KOD_ASNU = "КодАС"
    final String NAME_TF_NOT_EXTENSION = "ИдФайл"

    final String KOD_REPORT_PERIOD = "ПериодОтч"
    final String REPORT_YEAR = "ОтчетГод"

    @Deprecated
    def _importTF() {

        Integer declarationTypeId
        int departmentId
        String departmentCode
        String reportPeriodCode
        String asnuCode
        String guid
        Integer year
        Long declarationDataId

        if (UploadFileName != null
                && UploadFileName.toLowerCase().endsWith(NAME_EXTENSION_DEC)
                & UploadFileName.length() == NAME_LENGTH_QUARTER_DEC) {
            // РНУ_НДФЛ (первичная)
            declarationTypeId = DECLARATION_TYPE_RNU_NDFL_ID
            departmentCode = UploadFileName.substring(0, 17).replaceFirst("_*", "").trim()

            List<Department> formDepartments = departmentService.getDepartmentsBySbrfCode(departmentCode, true)
            if (formDepartments.size() == 0) {
                logger.error("Не удалось определить подразделение \"%s\"", departmentCode)
                return
            }
            if (formDepartments.size() > 1) {
                String departments = ""
                for (Department department : formDepartments) {
                    departments = departments + "\"" + department.getName().replaceAll("\"", "") + "\", "
                }
                departments = departments.substring(0, departments.length() - 2)

                logger.error("ТФ с именем \"%s\" не может быть загружен в Систему, в справочнике «Подразделения» АС «Учет налогов» с кодом \"%s\" найдено " +
                        "несколько подразделений: %s. Обратитесь к пользователю с ролью \"Контролёр УНП\"", UploadFileName, departmentCode, departments)

                return
            }

            Department formDepartment = formDepartments.get(0)
            departmentId = formDepartment != null ? formDepartment.getId() : null

            reportPeriodCode = UploadFileName.substring(21, 23).replaceAll("_", "").trim()
            if (reportPeriodCode != null && !reportPeriodCode.isEmpty() && periodNdflMap.containsKey(reportPeriodCode)) {
                reportPeriodCode = periodNdflMap.get(reportPeriodCode)
            }
            asnuCode = UploadFileName.substring(17, 21).replaceFirst("_", "").trim()
            guid = UploadFileName.substring(27, 59).replaceAll("_", "").trim()
            try {
                year = Integer.parseInt(UploadFileName.substring(23, 27))
            } catch (NumberFormatException nfe) {
                logger.error("Ошибка заполнения атрибутов транспортного файла \"%s\".", UploadFileName)
                return
            }
        } else {
            // Неизвестный формат имени загружаемого файла
            throw new IllegalArgumentException(String.format(ERROR_NAME_FORMAT, UploadFileName))
        }

        com.aplana.sbrf.taxaccounting.model.DeclarationType declarationType = declarationService.getTemplateType(declarationTypeId)

        // Указан недопустимый код периода
        ReportPeriod reportPeriod = reportPeriodService.getByTaxTypedCodeYear(TaxType.NDFL, reportPeriodCode, year)
        if (reportPeriod == null) {
            logger.error("Для вида налога «%s» в Системе не создан период с кодом «%s», календарный год «%s»! Загрузка файла «%s» не выполнена.", TaxType.NDFL.getName(), reportPeriodCode, year, UploadFileName)
            return
        }

        Department formDepartment = departmentService.get(departmentId)
        if (formDepartment == null) {
            logger.error("Не удалось определить подразделение для транспортного файла \"%s\"", UploadFileName)
            return
        }

        // Актуальный шаблон НФ, введенный в действие
        Integer declarationTemplateId
        try {
            declarationTemplateId = declarationService.getActiveDeclarationTemplateId(declarationType.getId(), reportPeriod.getId())
        } catch (Exception e) {
            // Если шаблона нет, то не загружаем ТФ
            logger.error("Ошибка при обработке данных транспортного файла. Загрузка файла не выполнена. %s", e.getMessage())
            return
        }

        DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationTemplateId)
        if (!TAAbstractScriptingServiceImpl.canExecuteScript(declarationService.getDeclarationTemplateScript(declarationTemplateId), FormDataEvent.IMPORT_TRANSPORT_FILE)) {
            logger.error("Для налоговой формы загружаемого файла \"%s\" не предусмотрена обработка транспортного файла! Загрузка не выполнена.", UploadFileName)
            return
        }

        // АСНУ
        Long asnuId = null
        def asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId())
        if (asnuCode != null) {
            List<Long> asnuIds = asnuProvider.getUniqueRecordIds(null, "CODE = '" + asnuCode + "'")
            if (asnuIds.size() == 1) {
                asnuId = asnuIds.get(0)
            }
        }

        // Назначение подразделению Декларации
        List<DepartmentDeclarationType> ddts = declarationService.getDDTByDepartment(departmentId,
                TaxType.NDFL, reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate())
        boolean found = false
        for (DepartmentDeclarationType ddt : ddts) {
            if (ddt.getDeclarationTypeId() == declarationType.getId()) {
                found = true
                break
            }
        }
        if (!found) {
            logger.error("Для подразделения «%s» не назначено первичной налоговой формы «%s»! Загрузка файла «%s» не выполнена.", formDepartment.getName(), declarationType.getName(), UploadFileName)
            return
        }

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.getLast(departmentId, reportPeriod.getId())
        // Открытость периода
        if (departmentReportPeriod == null || !departmentReportPeriod.isActive()) {
            String reportPeriodName = reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName()
            logger.error("Нет открытых отчетных периодов для \"%s\" за \"%s\".", formDepartment.getName(), reportPeriodName)
            return
        }

        if (validate(declarationTemplate)) {
            //достать из XML файла атрибуты тега СлЧасть
            SAXHandler handler = new SAXHandler('СлЧасть', 'Файл', true)
            InputStream inputStream = null
            try {
                inputStream = new FileInputStream(dataFile)
                SAXParserFactory factory = SAXParserFactory.newInstance()
                SAXParser saxParser = factory.newSAXParser()
                saxParser.parse(inputStream, handler)
            } catch (SAXParseException e) {
                LOG.error(getSAXParseExceptionMessage(e), e)
                logger.error("Ошибка чтения файла «${UploadFileName}»")
                return
            } catch (Exception e) {
                LOG.error("Ошибка чтения файла ${UploadFileName}", e)
                logger.error("Ошибка чтения файла «${UploadFileName}»")
                return
            } finally {
                IOUtils.closeQuietly(inputStream)
            }
            handler.getListValueAttributesTag()

            //Проверка на соответствие имени и содержимого ТФ в теге Файл.СлЧасть
            def xmlDepartmentCode = handler.getListValueAttributesTag()?.get(KOD_DEPARTMENT)?.replaceFirst("_*", "")?.trim()
            if (departmentCode != xmlDepartmentCode) {
                logger.error("В ТФ не совпадают значения параметра имени «Код подразделения» = «%s» и параметра содержимого «Файл.СлЧасть.КодПодр» = «%s»",
                        departmentCode, xmlDepartmentCode)
            }

            def xmlAsnuCode = handler.getListValueAttributesTag()?.get(KOD_ASNU)
            if (asnuCode != xmlAsnuCode) {
                logger.error("В ТФ не совпадают значения параметра имени «Код АСНУ» = «%s» и параметра содержимого «Файл.СлЧасть.КодАС» = «%s»",
                        asnuCode, xmlAsnuCode)
            }

            def xmlFileName = handler.getListValueAttributesTag()?.get(NAME_TF_NOT_EXTENSION)
            if (UploadFileName.trim().substring(0, UploadFileName.length() - 4) != xmlFileName) {
                logger.error("В ТФ не совпадают значения параметра имени «Имя ТФ без расширения» = «%s» и параметра содержимого «Файл.СлЧасть.ИдФайл» = «%s»",
                        UploadFileName.trim()[0..-5], xmlFileName)
            }

            //достать из XML файла атрибуты тега СлЧасть
            handler = new SAXHandler('Файл', 'ИнфЧасть', true)
            try {
                inputStream = new FileInputStream(dataFile)
                SAXParserFactory factory = SAXParserFactory.newInstance()
                SAXParser saxParser = factory.newSAXParser()
                saxParser.parse(inputStream, handler)
            } catch (SAXParseException e) {
                LOG.error(getSAXParseExceptionMessage(e), e)
                logger.error("Ошибка чтения файла «${UploadFileName}»")
                return
            } catch (Exception e) {
                LOG.error("Ошибка чтения файла ${UploadFileName}", e)
                logger.error("Ошибка чтения файла «${UploadFileName}»")
                return
            }

            //Проверка на соответствие имени и содержимого ТФ в теге Все элементы Файл.ИнфЧасть файла
            def xmlReportPeriodCode = handler.getListValueAttributesTag()?.get(KOD_REPORT_PERIOD)
            if (reportPeriodCode != xmlReportPeriodCode) {
                logger.error("В ТФ не совпадают значения параметра имени «Код периода» = «%s» и параметра содержимого «Файл.ИнфЧасть.ПериодОтч» = «%s»",
                        reportPeriodCode, xmlReportPeriodCode)
            }

            def xmlReportYear = handler.getListValueAttributesTag()?.get(REPORT_YEAR)
            Integer reportYear = null
            try {
                reportYear = Integer.parseInt(xmlReportYear)
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace()
            }

            if (year != reportYear) {
                logger.error("В ТФ не совпадают значения параметра имени «Год» = «%s» и параметра содержимого «Файл.ИнфЧасть.ОтчетГод» = «%s»",
                        year, xmlReportYear)
            }

            // Проверка не загружен ли уже такой файл в систему
            if (UploadFileName) {
                DeclarationDataFilter declarationFilter = new DeclarationDataFilter()

                declarationFilter.setFileName(UploadFileName)
                declarationFilter.setTaxType(TaxType.NDFL)
                declarationFilter.setSearchOrdering(DeclarationDataSearchOrdering.ID)

                List<Long> declarationDataSearchResultItems = declarationService.getDeclarationIds(declarationFilter, declarationFilter.getSearchOrdering(), false)
                if (!declarationDataSearchResultItems.isEmpty()) {
                    logger.error("ТФ с именем «%s» уже загружен в систему.", UploadFileName)
                }
            }

            // Поиск экземпляра декларации
            DeclarationData declarationData = declarationService.find(declarationTemplateId, departmentReportPeriod.getId(), null, null, null, asnuId, UploadFileName)
            // Экземпляр уже есть
            if (declarationData != null) {
                logger.error("Экземпляр формы \"%s\" в \"%s\" уже существует! Загрузка файла «%s» не выполнена.", declarationType.getName(), formDepartment.getName(), UploadFileName)
                return
            }

            if (logger.containsLevel(LogLevel.ERROR)) {
                return
            }
            // Создание экземпляра декларации
            DeclarationData newDeclarationData = new DeclarationData()
            newDeclarationData.declarationTemplateId = declarationTemplateId
            newDeclarationData.departmentReportPeriodId = departmentReportPeriod.id
            newDeclarationData.asnuId = asnuId
            newDeclarationData.fileName = UploadFileName
            newDeclarationData.reportPeriodId = departmentReportPeriod.reportPeriod.id
            newDeclarationData.departmentId = departmentReportPeriod.departmentId
            newDeclarationData.state = State.CREATED
            // в ЖА и историю не пишем, будет записано отдельно по окончании импорта файла
            declarationService.createWithoutChecks(newDeclarationData, logger, userInfo, false)
            if (logger.containsLevel(LogLevel.ERROR)) {
                return
            }
            // Запуск события скрипта для разбора полученного файла
            DeclarationData createdDeclarationData = declarationService.getDeclarationData(newDeclarationData.id)
            declarationService.importXmlTransportFile(dataFile, UploadFileName, createdDeclarationData, userInfo, logger)

            if (!logger.containsLevel(LogLevel.ERROR)) {
                msgBuilder.append("Выполнено создание налоговой формы: ")
                        .append("№: \"").append(newDeclarationData.id).append("\"")
                        .append(", Период: \"").append(reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName()).append("\"")
                        .append(getCorrectionDateString(departmentReportPeriod))
                        .append(", Подразделение: \"").append(formDepartment.getName()).append("\"")
                        .append(", Вид: \"").append(declarationType.getName()).append("\"")
                        .append(", АСНУ: \"").append(asnuProvider.getRecordData(asnuId).get("NAME").getStringValue()).append("\"")
                logger.info(msgBuilder.toString())
            }
        }
    }

    String getSAXParseExceptionMessage(SAXParseException e) {
        return "Ошибка: ${e.getLineNumber() != -1 ? "Строка: ${e.getLineNumber()}" : ""}" +
                "${e.getColumnNumber() != -1 ? " Столбец: ${e.getColumnNumber()}" : ""}" +
                "${e.getLocalizedMessage() ? " ${e.getLocalizedMessage()}" : ""}"
    }

    String getCorrectionDateString(DepartmentReportPeriod reportPeriod) {
        def dateFormat = new SimpleDateFormat("dd.MM.yyyy")
        return reportPeriod.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s", dateFormat.format(reportPeriod.getCorrectionDate())) : ""
    }

    // кол-во созданных строк в Приложении2
    int rowCount = 0
    // Итоговая строка
    TotalRow totalRow = new TotalRow()
    // Поля Приложения2
    List<String> fieldNames = [1 : "Номер строки", 2: "ИНН в РФ", 3: "ИНН в стране гражданства", 4: "Фамилия", 5: "Имя",
                               6 : "Отчество", 7: "Статус налогоплательщика", 8: "Дата рождения", 9: "Гражданство (код страны)", 10: "Код вида документа, удостоверяющего личность",
                               11: "Серия и номер документа", 12: "Почтовый индекс", 13: "Регион (код)", 14: "Район", 15: "Город",
                               16: "Населенный пункт (село, поселок)", 17: "Улица (проспект, переулок)", 18: "Номер дома (владения)", 19: "Номер корпуса (строения)",
                               20: "Номер квартиры", 21: "Код страны", 22: "Адрес места жительства за пределами Российской Федерации", 23: "Налоговая ставка",
                               24: "Общая сумма дохода", 25: "Общая сумма вычетов", 26: "Налоговая база", 27: "Сумма налога исчисленная", 28: "Сумма налога удержанная",
                               29: "Сумма налога, излишне удержанная налоговым агентом", 30: "Сумма налога, не удержанная налоговым агентом",
                               31: "Сумма налога, не удержанная налоговым агентом"].values() as List

    void createApplication2() {
        List<Long> declarationDataIdList = declarationService.findApplication2DeclarationDataId(reportYear)
        if (declarationDataIdList.isEmpty()) {
            logger.error("Файл Приложения 2 не может быть сформирован: не обнаружено КНФ в состоянии \"Принята\"")
            return
        }
        logger.info("Для формирования Приложения 2 используются КНФ в состоянии \"Принята\": %s", declarationDataIdList.join(", "))
        Map<Long, NdflPerson> ndflPersonsById = ndflPersonService.findAllNdflPersonsByDeclarationIds(declarationDataIdList).collectEntries {
            [it.id, it]
        }
        List<NdflPersonIncome> allIncomes = ndflPersonService.findAllIncomesByDeclarationIds(declarationDataIdList)
        List<NdflPersonDeduction> allDeductions = ndflPersonService.findAllDeductionsByDeclarationIds(declarationDataIdList)
        if (allIncomes.isEmpty()) {
            logger.error("Файл Приложения 2 не может быть сформирован: не обнаружено строк Раздела 2 \"Сведения о доходах и НДФЛ\"")
            return
        }

        // Пока формируем строки по ФЛ и ставке, после этого они будут при необходимости дробится на несколько строк
        List<App2PersonRateRowGroup> personRateRowGroups = []
        (allIncomes.groupBy(
                { NdflPersonIncome it -> it.ndflPersonId }, { NdflPersonIncome it -> it.taxRate }
        ) as Map<Long, Map<Integer, List<NdflPersonIncome>>>).each { personId, incomesOfPersonByTaxRate ->
            for (def taxRate : incomesOfPersonByTaxRate.keySet()) {
                if (taxRate != null) {
                    NdflPerson ndflPerson = ndflPersonsById.get(personId)
                    def personRateRowGroup = new App2PersonRateRowGroup(ndflPerson, taxRate, incomesOfPersonByTaxRate)
                    checkRequired(personRateRowGroup)
                    personRateRowGroups.add(personRateRowGroup)
                }
            }
        }
        if (!logger.containsLevel(LogLevel.ERROR)) {
            // Первая строка
            StringBuilder headerBuilder = new StringBuilder()
            headerBuilder << new Date().format(SharedConstants.FULL_DATE_FORMAT) << "|"
            headerBuilder << "Управление налогового планирования" << "|"
            headerBuilder << "Приложение 2" << "|"
            headerBuilder << String.format("АС УН, ФП \"%s\" %s", "НДФЛ", version) << "|" << "\r\n"
            // Пустая строка
            headerBuilder << "\r\n"
            write(headerBuilder.toString())
            // Строки с данными по ФЛ и ставке. Могут дробится на несколько, в зависимости от того будут ли данные по кодам доходов и вычетов влезать в одну строку
            for (def personRateRowGroup : personRateRowGroups) {
                Long ndflPersonId = personRateRowGroup.person.id

                def row = personRateRowGroup.addRow()
                def operationIds = personRateRowGroup.incomes.operationId.toSet()
                def deductionsOfOperations = allDeductions.findAll {
                    it.ndflPersonId == ndflPersonId && it.operationId in operationIds
                }
                List<NdflPersonDeduction> standartDeductions = deductionsOfOperations.findAll {
                    getDeductionMarkCodeByDeductionTypeCode(it.typeCode) == 1
                }
                deductionsOfOperations.removeAll(standartDeductions)
                def incomesByIncomeCode = personRateRowGroup.incomes.groupBy { it.incomeCode }
                for (def incomeCode : incomesByIncomeCode.keySet()) {
                    def incomesOfIncomeCode = incomesByIncomeCode.get(incomeCode)
                    List<NdflPersonDeduction> deductionsOfIncomeCode = deductionsOfOperations.findAll {
                        it.incomeCode == incomeCode
                    }
                    deductionsOfOperations.removeAll(deductionsOfIncomeCode)
                    // Добавляем новый набор полей для кода дохода. Если он не влазит в текущую строку, то дублируем эту строку внутри группы строк
                    if (!row.hasNextIncomeColGroup()) {
                        row = personRateRowGroup.addRow()
                    }
                    App2IncomeColGroup incomeColGroup = row.nextIncomeColGroup()
                    incomeColGroup.setData(incomeCode, incomesOfIncomeCode)

                    def deductionsByDeductionCode = deductionsOfIncomeCode.groupBy { it.typeCode }
                    for (def deductionCode : deductionsByDeductionCode.keySet()) {
                        def deductionsOfDeductionCode = deductionsByDeductionCode.get(deductionCode)
                        // Добавляем новый набор полей для кода вычета внутри набора полей для кода дохода.
                        // Если не влазит в текущий набор полей для кода дохода, то дублируем его и добавляем туда
                        if (!incomeColGroup.hasNextApp2DeductionColGroup()) {
                            // Если сдублированный набор полей для кода дохода не влез в текущуй строку, то дублируем эту строку
                            if (!row.hasNextIncomeColGroup()) {
                                row = personRateRowGroup.addRow()
                            }
                            incomeColGroup = row.nextIncomeColGroup()
                            incomeColGroup.setData(incomeCode, incomesOfIncomeCode)
                        }
                        App2DeductionColGroup deductionColGroup = incomeColGroup.nextApp2DeductionColGroup()
                        deductionColGroup.setData(deductionCode, deductionsOfDeductionCode)
                    }
                }
                // deductionsOfOperations - оставшиеся строки раздела 3 не попавшие ни в одну группу полей для кода дохода
                check16(personRateRowGroup, deductionsOfOperations)
                // Добавляем стандартные вычеты в последние столбцы строк из группы строк по ФЛ-ставка, если в уже созданные строки не влазят,
                // то добавляем новую с задублированными данными
                def standartDeductionsByDeductionCode = standartDeductions.groupBy { it.typeCode }
                def rowsIterator = personRateRowGroup.rows.iterator()
                row = rowsIterator.next()
                for (def deductionCode : standartDeductionsByDeductionCode.keySet()) {
                    def deductionsOfDeductionCode = standartDeductionsByDeductionCode.get(deductionCode)

                    if (!row.hasNextStandartDeductionColGroup()) {
                        row = rowsIterator.hasNext() ? rowsIterator.next() : personRateRowGroup.addRow()
                    }
                    def deductionColGroup = row.nextStandartDeductionColGroup()
                    deductionColGroup.setData(deductionCode, deductionsOfDeductionCode)
                }

                check(personRateRowGroup)
                write(personRateRowGroup.toString())
                totalRow.add(personRateRowGroup)
            }
            write(totalRow.toString())
            IOUtils.closeQuietly(outputStream)
            scriptSpecificRefBookReportHolder.setFileName(createApplication2FileName())
        }
    }

    /**
     * Заполнены все Поля, отмеченные как обязательные для заполнения в таблице "Заполнение полей строк файла"
     */
    void checkRequired(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        List<String> errFields = []
        !person.lastName && errFields.add("\"Фамилия\"")
        !person.firstName && errFields.add("\"Имя\"")
        !person.status && errFields.add("\"Статус налогоплательщика\"")
        !person.birthDay && errFields.add("\"Дата рождения\"")
        !person.citizenship && errFields.add("\"Гражданство (код страны)\"")
        !person.idDocType && errFields.add("\"Код вида документа, удостоверяющего личность\"")
        !person.idDocNumber && errFields.add("\"Серия и номер документа\"")
        personRateRowGroup.rate == null && errFields.add("\"Налоговая ставка\"")
        personRateRowGroup.incomeAccruedSum == null && errFields.add("\"Общая сумма дохода\"")
        personRateRowGroup.taxBaseSum == null && errFields.add("\"Налоговая база\"")
        personRateRowGroup.calculatedTaxSum == null && errFields.add("\"Сумма налога исчисленная\"")
        if (errFields) {
            logger.error("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                    " не заполнены обязательные поля: ${errFields.join(", ")}")
        }
    }

    /**
     * Проверки данных в полученных строках. Группы строк, т.к. данные в них могут быть задублированны
     */
    void check(App2PersonRateRowGroup personRateRowGroup) {
        check2(personRateRowGroup)
        check5(personRateRowGroup)
        check8(personRateRowGroup)
        check10(personRateRowGroup)
        check12(personRateRowGroup)
        check12_1(personRateRowGroup)
        check13(personRateRowGroup)
        check13_1(personRateRowGroup)
        check13_2(personRateRowGroup)
        check14(personRateRowGroup)
        check15(personRateRowGroup)
        check18(personRateRowGroup)
        check19(personRateRowGroup)
    }

    /**
     * Поля не должны иметь значений, содержащих разделитель «|» - символ с ASCII кодом 124.
     */
    void check2(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        List<String> errFields = []
        personRateRowGroup.person.innNp?.contains('|') && errFields.add("\"ИНН в РФ\"")
        personRateRowGroup.person.innForeign?.contains('|') && errFields.add("\"ИНН в стране гражданства\"")
        personRateRowGroup.person.lastName?.contains('|') && errFields.add("\"Фамилия\"")
        personRateRowGroup.person.firstName?.contains('|') && errFields.add("\"Имя\"")
        personRateRowGroup.person.middleName?.contains('|') && errFields.add("\"Отчество\"")
        personRateRowGroup.person.status?.contains('|') && errFields.add("\"Статус налогоплательщика\"")
        personRateRowGroup.person.citizenship?.contains('|') && errFields.add("\"Гражданство (код страны)\"")
        personRateRowGroup.person.idDocType?.contains('|') && errFields.add("\"Код вида документа, удостоверяющего личность\"")
        personRateRowGroup.person.idDocNumber?.contains('|') && errFields.add("\"Серия и номер документа\"")
        personRateRowGroup.person.postIndex?.contains('|') && errFields.add("\"Почтовый индекс\"")
        personRateRowGroup.person.regionCode?.contains('|') && errFields.add("\"Регион (код)\"")
        personRateRowGroup.person.area?.contains('|') && errFields.add("\"Район\"")
        personRateRowGroup.person.city?.contains('|') && errFields.add("\"Город\"")
        personRateRowGroup.person.locality?.contains('|') && errFields.add("\"Населенный пункт (село, поселок)\"")
        personRateRowGroup.person.street?.contains('|') && errFields.add("\"Улица (проспект, переулок)\"")
        personRateRowGroup.person.house?.contains('|') && errFields.add("\"Номер дома (владения)\"")
        personRateRowGroup.person.building?.contains('|') && errFields.add("\"Номер корпуса (строения)\"")
        personRateRowGroup.person.flat?.contains('|') && errFields.add("\"Номер квартиры\"")
        personRateRowGroup.person.countryCode?.contains('|') && errFields.add("\"Код страны\"")
        personRateRowGroup.person.address?.contains('|') && errFields.add("\"Адрес места жительства за пределами Российской Федерации\"")
        for (def field : errFields) {
            logger.error("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                    "${person.idDocNumber} в поле ${field} содержится недопустимый символ разделителя \"|\"")
        }
    }

    /**
     * Поле 3 не заполнено, если значение поля «Код» записи справочника «ОК 025-2001 (Общероссийский классификатор стран мира» см.
     * (000) Справочник ОКСМ), указанное в поле 9, равно «643»
     */
    void check5(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        if (person.citizenship == "643" && person.innForeign) {
            logger.error("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                    " в поле 9 указан код страны = \"643\". При этом значении кода страны поле 3 (\"ИНН в стране гражданства\") не должно быть заполнено.")
        }
    }

    /**
     * Проверка выполняется, если графа заполнена.
     * Значение графы должно соответствовать паттерну: [0-9]{6}*/
    void check8(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        if (person.postIndex && !person.postIndex.matches("[0-9]{6}")) {
            logger.error("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                    " в поле 12 (Почтовый индекс) указано значение не соответствующее формату почтового индекса.")
        }
    }

    /**
     * Поле 13 должно быть заполнено, если выполняется хотя бы одно из следующих условий:
     1) Заполнено хотя бы одно из полей: 12, 14-20.
     2) Графы 21 и 22 не заполнены.
     3) Поле 7 (статус налогоплательщика) равно значению «1» (налоговый резидент РФ)
     4) Значение поля «Код» записи справочника «ОК 025-2001 (Общероссийский классификатор стран мира» (000) Справочник ОКСМ), указанной в поле 9, равно «643»
     */
    void check10(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        if (!person.regionCode) {
            if ((person.postIndex || person.area || person.city || person.locality || person.street || person.house || person.building || person.flat) ||
                    (!person.countryCode && !person.address) ||
                    (person.status == "1") ||
                    (person.citizenship == "643")) {
                logger.error("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                        " не заполнено поле 13 \"Регион (код)\"")
            }
        }
    }

    /**
     * Поля 21 и 22 заполнены, если одновременно выполняются следующие условия:
     a) Значение поля «Код» записи справочника «ОК 025-2001 (Общероссийский классификатор стран мира» (000) Справочник ОКСМ), указанной в графе 10, не равно «643»;
     b) Ни одно из полей 12-20 (адрес места жительства в РФ) не заполнено
     */
    void check12(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        if (person.citizenship != "643" &&
                !person.postIndex && !person.regionCode && !person.area && !person.city && !person.locality && !person.street && !person.house && !person.building && !person.flat) {
            if (!(person.countryCode && person.address)) {
                logger.error("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                        " не заполнены поля 21 \"Код страны\" и 22 \"Адрес места жительства за пределами Российской Федерации\"")
            }
        }
        if (personRateRowGroup.countryCode && !personRateRowGroup.address || !personRateRowGroup.countryCode && personRateRowGroup.address) {
            logger.error("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                    " поля «Код страны» и «Адрес места жительства за пределами Российской Федерации» должны быть одновременно заполнены либо одновременно не заполнены")
        }
    }

    /**
     * Поле 21 ("Код страны") и Поле 22 ("Адрес места жительства за пределами Российской Федерации") должны быть одновременно заполнены либо одновременно не заполнены
     */
    void check12_1(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        if (personRateRowGroup.countryCode && !personRateRowGroup.address || !personRateRowGroup.countryCode && personRateRowGroup.address) {
            logger.error("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                    " поля «Код страны» и «Адрес места жительства за пределами Российской Федерации» должны быть одновременно заполнены либо одновременно не заполнены")
        }
    }

    /**
     * «Поле 24» = Сумма значений всех полей "Сумма дохода", указанных для всех кодов дохода ФЛ"
     */
    void check13(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        BigDecimal sum = 0
        Set<String> incomeCodes = []
        for (def row : personRateRowGroup.rows) {
            for (def incomeColGroup : row.incomeColGroups) {
                if (!incomeCodes.contains(incomeColGroup.incomeCode)) {
                    sum += (incomeColGroup.incomeAccruedSum ?: 0)
                    incomeCodes.add(incomeColGroup.incomeCode)
                }
            }
        }
        if ((personRateRowGroup.incomeAccruedSum ?: 0) != sum) {
            logger.warn("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                    " общая сумма дохода ($personRateRowGroup.incomeAccruedSum) не равна сумме всех сумм доходов ($sum)")
        }
    }

    /**
     * «Поле 25» = Сумма значений всех полей "Сумма вычета", указанных для всех кодов вычета по всем кодам дохода ФЛ
     */
    void check13_1(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        BigDecimal sum = 0
        for (def row : personRateRowGroup.rows) {
            for (def incomeColGroup : row.incomeColGroups) {
                for (def deductionColGroup : incomeColGroup.deductionColGroups) {
                    sum += (deductionColGroup.periodCurrSum ?: 0)
                }
            }
            for (def deductionColGroup : row.standartDeductionColGroups) {
                sum += (deductionColGroup.periodCurrSum ?: 0)
            }
        }
        if ((personRateRowGroup.incomeDeductionSum ?: 0) != sum) {
            logger.warn("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                    " общая сумма вычетов ($personRateRowGroup.incomeDeductionSum) не равна сумме всех сумм вычетов ($sum)")
        }
    }

    /**
     * «Поле 26» = «Поле 24» - «Поле 25»
     */
    void check13_2(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        if ((personRateRowGroup.taxBaseSum ?: 0) != ((personRateRowGroup.incomeAccruedSum ?: 0) - (personRateRowGroup.incomeDeductionSum ?: 0))) {
            logger.warn("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                    " налоговая база ($personRateRowGroup.taxBaseSum) не равна разности между \"Общая сумма дохода\" (${personRateRowGroup.incomeAccruedSum ?: 0})" +
                    " и \"Общая сумма вычета\" (${personRateRowGroup.incomeDeductionSum ?: 0})")
        }
    }

    /**
     * Значение поля 27, должно быть равно ОКРУГЛ ((графа 23/100) * графа 26;0),
     где ОКРУГЛ () – функция округления значения атрибута до указанного количества десятичных разрядов
     */
    void check14(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        BigDecimal v = (personRateRowGroup.taxBaseSum ?: 0) * personRateRowGroup.rate / 100
        if ((personRateRowGroup.calculatedTaxSum ?: 0) != ScriptUtils.round(v)) {
            logger.warn("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                    " в поле 27 «Сумма налога исчисленная» (${personRateRowGroup.calculatedTaxSum ?: 0}) указано некорректное значение.")
        }
    }

    /**
     * Для каждого кода дохода в группе строк "ФЛ-Ставка" значение в каждом поле "Сумма вычета" не превышает значения "Сумма дохода"
     */
    @SuppressWarnings("GroovyVariableNotAssigned")
    void check15(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        BigDecimal deductionSum = 0
        for (int rowIndex = 0; rowIndex < personRateRowGroup.rows.size(); rowIndex++) {
            def row = personRateRowGroup.rows[rowIndex]
            App2Row nextRow = rowIndex < personRateRowGroup.rows.size() - 1 ? personRateRowGroup.rows[rowIndex + 1] : null
            for (int incomeColGroupIndex = 0; incomeColGroupIndex < row.incomeColGroups.size(); incomeColGroupIndex++) {
                App2IncomeColGroup incomeColGroup = row.incomeColGroups[incomeColGroupIndex]
                App2IncomeColGroup nextIncomeColGroup = incomeColGroupIndex < row.incomeColGroups.size() - 1 ? row.incomeColGroups[incomeColGroupIndex + 1] : nextRow?.incomeColGroups?.first()
                // Суммируем вычеты у всех групп полей по коду дохода с одинаковым кодом дохода
                for (def deductionColGroup : incomeColGroup.deductionColGroups) {
                    if (deductionColGroup.deductionCode) {
                        deductionSum += (deductionColGroup.periodCurrSum ?: 0)
                    }
                }
                // проверяем только если достигли последней группы по коду дохода
                if (nextIncomeColGroup == null || incomeColGroup.incomeCode != nextIncomeColGroup.incomeCode) {
                    if (deductionSum > (incomeColGroup.incomeAccruedSum ?: 0)) {
                        logger.warn("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                                " для кода дохода $incomeColGroup.incomeCode сумма вычета ($deductionSum) превышает сумму дохода (${incomeColGroup.incomeAccruedSum ?: 0}).")
                    }
                    deductionSum = 0
                }
            }
        }
    }

    /**
     * @param deductions строки раздела 3 не попавшие ни в одну группу полей для кода дохода
     */
    void check16(App2PersonRateRowGroup personRateRowGroup, List<NdflPersonDeduction> deductions) {
        def person = personRateRowGroup.person
        if (!deductions.isEmpty()) {
            logger.error("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                    " существуют заполненные вычеты, но не заполнены поля \"Код дохода\", \"Сумма дохода\"")
        }
    }

    /**
     * Одновременно заполнены либо одновременно не заполнены значения в парах полей ("Код вычета", "Сумма вычета")
     */
    void check18(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        List<App2DeductionColGroup> allDeductionColGroups = []
        for (def row : personRateRowGroup.rows) {
            for (def incomeColGroup : row.incomeColGroups) {
                allDeductionColGroups += incomeColGroup.deductionColGroups
            }
            allDeductionColGroups += row.standartDeductionColGroups
        }
        for (def deductionColGroup : allDeductionColGroups) {
            if (deductionColGroup.deductionCode && deductionColGroup.periodCurrSum == null) {
                logger.error("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                        " заполнен код вычета, но не заполнена сумма вычета в поле ${deductionColGroup.colNum + 1}")
            }
            if (!deductionColGroup.deductionCode && deductionColGroup.periodCurrSum != null) {
                logger.error("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                        " заполнена Сумма вычета, но не заполнен \"Код вычета\" в поле ${deductionColGroup.colNum}")
            }
        }
    }

    /**
     * Значение "Сумма вычета" в поле 69 и в поле 71 не должно превышать значения "Общая сумма дохода" в поле 24.
     */
    void check19(App2PersonRateRowGroup personRateRowGroup) {
        def person = personRateRowGroup.person
        for (def row : personRateRowGroup.rows) {
            for (def standartDeductionColGroup : row.standartDeductionColGroups) {
                if ((standartDeductionColGroup.periodCurrSum ?: 0) > (personRateRowGroup.incomeAccruedSum ?: 0)) {
                    logger.warn("Для ${person.lastName} ${person.firstName} ${person.middleName ?: ""} ${person.birthDay.format("dd.MM.yyyy") ?: ""} ${person.idDocNumber}" +
                            " значение суммы стандартного вычета (${standartDeductionColGroup.periodCurrSum ?: 0}) в поле ${standartDeductionColGroup.colNum + 1}" +
                            " превышает общую сумму дохода (${personRateRowGroup.incomeAccruedSum ?: 0})")
                    return
                }
            }
        }
    }

    String createApplication2FileName() {
        StringBuilder nameBuilder = new StringBuilder("_____app2")
        String unpDepartmentCode = "99_6200_00"
        for (int i = 0; i < 17 - unpDepartmentCode.length(); i++) {
            nameBuilder.append("_")
        }
        nameBuilder.append(unpDepartmentCode)
        nameBuilder.append("34")
        nameBuilder.append(reportYear)
        nameBuilder.append(".rnu")
    }

    void write(String string) {
        if (!logger.containsLevel(LogLevel.ERROR)) {
            IOUtils.write(string, outputStream, "IBM866")
        }
    }

    DecimalFormat format17_2
    {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault())
        symbols.setDecimalSeparator('.' as char)
        format17_2 = new DecimalFormat("0.00", symbols)
    }

    String format17_2(BigDecimal value) {
        return value != null ? format17_2.format(value) : ""
    }

    String format(Object value) {
        return value != null ? value : ""
    }

    // Группа строк Приложения2 по ФЛ и ставке
    class App2PersonRateRowGroup {
        NdflPerson person
        Integer rate
        List<NdflPersonIncome> incomes
        List<App2Row> rows = []

        BigDecimal incomeAccruedSum = null
        BigDecimal incomeDeductionSum = null
        BigDecimal taxBaseSum = null
        BigDecimal calculatedTaxSum = null
        BigDecimal withholdingTaxSum = null
        BigDecimal taxSum = null
        BigDecimal overholdingTaxSum = null
        BigDecimal notholdingTaxSum = null

        App2PersonRateRowGroup(NdflPerson person, Integer rate, Map<Integer, List<NdflPersonIncome>> incomesOfPersonByTaxRate) {
            this.person = person
            this.incomes = incomesOfPersonByTaxRate.get(rate)
            this.rate = rate
            def incomesOfPerson = incomesOfPersonByTaxRate.values().flatten() as List<NdflPersonIncome>
            for (def income : incomes) {
                if (income.incomeAccruedSumm != null) {
                    incomeAccruedSum = (incomeAccruedSum ?: 0) + income.incomeAccruedSumm
                }
                if (income.totalDeductionsSumm != null) {
                    incomeDeductionSum = (incomeDeductionSum ?: 0) + income.totalDeductionsSumm
                }
                if (income.taxBase != null) {
                    taxBaseSum = (taxBaseSum ?: 0) + income.taxBase
                }
                if (income.calculatedTax != null) {
                    calculatedTaxSum = (calculatedTaxSum ?: 0) + income.calculatedTax
                }
                if (income.withholdingTax != null) {
                    withholdingTaxSum = (withholdingTaxSum ?: 0) + income.withholdingTax
                }
                if (income.overholdingTax != null) {
                    overholdingTaxSum = (overholdingTaxSum ?: 0) + income.overholdingTax
                }
                if (income.notHoldingTax != null) {
                    notholdingTaxSum = (notholdingTaxSum ?: 0) + income.notHoldingTax
                }
            }
            for (NdflPersonIncome income : incomesOfPerson) {
                if (income.taxSumm != null) {
                    taxSum = (taxSum ?: 0) + income.taxSumm
                }
            }
        }

        App2Row addRow() {
            def row = new App2Row(this)
            rows.add(row)
            return row
        }

        String getCountryCode() {
            return person.countryCode && person.countryCode != "643" ? person.countryCode : ""
        }

        String getAddress() {
            return getCountryCode() && person.address ? person.address : ""
        }

        @Override
        String toString() {
            StringBuilder stringBuilder = new StringBuilder()
            for (def row : rows) {
                stringBuilder << row.toString()
            }
            return stringBuilder.toString()
        }
    }
    // строка Приложения2
    class App2Row {
        int rowNum
        // Группа строк с одинаковыми данными в столбцах 1-31, в которую входит текущая строка
        App2PersonRateRowGroup personRateRowGroup
        // 3 группы столбцов по коду дохода. Если кодов дохода больше, то они переносятся в след-ую строку
        List<App2IncomeColGroup> incomeColGroups = []
        // 2 последних столбца кодов стандартных вычетов
        List<App2DeductionColGroup> standartDeductionColGroups = []
        // Итератор группы полей для кодов дохода для их поочередного заполнения
        private Iterator<App2IncomeColGroup> incomeColGroupsIterator
        // Итератор группы полей для стандартных вычетов для их поочередного заполнения
        private Iterator<App2DeductionColGroup> standartDeductionColGroupsIterator

        App2Row(App2PersonRateRowGroup personRateRowGroup) {
            rowNum = ++rowCount
            this.personRateRowGroup = personRateRowGroup
            3.times { incomeColGroups.add(new App2IncomeColGroup(32 + (it * 12))) }
            2.times { standartDeductionColGroups.add(new App2DeductionColGroup(68 + (it * 2))) }
            incomeColGroupsIterator = incomeColGroups.iterator()
            standartDeductionColGroupsIterator = standartDeductionColGroups.iterator()
        }

        boolean hasNextIncomeColGroup() {
            return incomeColGroupsIterator.hasNext()
        }

        App2IncomeColGroup nextIncomeColGroup() {
            return incomeColGroupsIterator.next()
        }

        boolean hasNextStandartDeductionColGroup() {
            return standartDeductionColGroupsIterator.hasNext()
        }

        App2DeductionColGroup nextStandartDeductionColGroup() {
            return standartDeductionColGroupsIterator.next()
        }

        @Override
        String toString() {
            StringBuilder dataBuilder = new StringBuilder()
            dataBuilder << rowNum++ << "|"
            dataBuilder << (personRateRowGroup.person.innNp ?: "") << "|"
            dataBuilder << (personRateRowGroup.person.innForeign ?: "") << "|"
            dataBuilder << personRateRowGroup.person.lastName << "|"
            dataBuilder << personRateRowGroup.person.firstName << "|"
            dataBuilder << (personRateRowGroup.person.middleName ?: "") << "|"
            dataBuilder << personRateRowGroup.person.status << "|"
            dataBuilder << personRateRowGroup.person.birthDay.format(SharedConstants.DATE_FORMAT) << "|"
            dataBuilder << personRateRowGroup.person.citizenship << "|"
            dataBuilder << personRateRowGroup.person.idDocType << "|"
            dataBuilder << getIdDocType() << "|"
            dataBuilder << (personRateRowGroup.person.postIndex ?: "") << "|"
            dataBuilder << (personRateRowGroup.person.regionCode ?: "") << "|"
            dataBuilder << (personRateRowGroup.person.area ?: "") << "|"
            dataBuilder << (personRateRowGroup.person.city ?: "") << "|"
            dataBuilder << (personRateRowGroup.person.locality ?: "") << "|"
            dataBuilder << (personRateRowGroup.person.street ?: "") << "|"
            dataBuilder << (personRateRowGroup.person.house ?: "") << "|"
            dataBuilder << (personRateRowGroup.person.building ?: "") << "|"
            dataBuilder << (personRateRowGroup.person.flat ?: "") << "|"
            dataBuilder << personRateRowGroup.getCountryCode() << "|"
            dataBuilder << personRateRowGroup.getAddress() << "|"
            dataBuilder << personRateRowGroup.rate << "|"
            dataBuilder << format17_2(personRateRowGroup.incomeAccruedSum) << "|"
            dataBuilder << format17_2(personRateRowGroup.incomeDeductionSum) << "|"
            dataBuilder << format17_2(personRateRowGroup.taxBaseSum) << "|"
            dataBuilder << format(personRateRowGroup.calculatedTaxSum) << "|"
            dataBuilder << format(personRateRowGroup.withholdingTaxSum) << "|"
            dataBuilder << format(personRateRowGroup.taxSum) << "|"
            dataBuilder << format(personRateRowGroup.overholdingTaxSum) << "|"
            dataBuilder << format(personRateRowGroup.notholdingTaxSum) << "|"
            for (def incomeColGroup : incomeColGroups) {
                dataBuilder << incomeColGroup.toString()
            }
            for (def deductionColGroup : standartDeductionColGroups) {
                dataBuilder << deductionColGroup.toString()
            }
            dataBuilder << "\r\n"

            return dataBuilder.toString()
        }

        String getIdDocType() {
            if (personRateRowGroup.person.idDocType == "21") {
                return personRateRowGroup.person.idDocNumber.replaceAll(" ", "")
            } else {
                return personRateRowGroup.person.idDocNumber
            }
        }
    }
    // группа столбцов строки Приложения2 с данными по отдельному коду дохода
    class App2IncomeColGroup {
        // номер поля, с которого начинается эта группа полей
        int colNum
        // код дохода
        String incomeCode
        // Сумма начислений для строк 2 раздела с текущим кодом дохода
        BigDecimal incomeAccruedSum = null
        // 5 групп полей для кода вычета
        List<App2DeductionColGroup> deductionColGroups = []
        // Итератор групп полей для их поочередного заполнения
        Iterator<App2DeductionColGroup> deductionColGroupsIterator

        App2IncomeColGroup(int colNum) {
            this.colNum = colNum
            5.times { deductionColGroups.add(new App2DeductionColGroup(colNum + 2 + it * 2)) }
            deductionColGroupsIterator = deductionColGroups.iterator()
        }

        void setData(String incomeCode, List<NdflPersonIncome> incomes) {
            this.incomeCode = incomeCode
            for (NdflPersonIncome income : incomes) {
                if (income.incomeAccruedSumm != null) {
                    incomeAccruedSum = (incomeAccruedSum ?: 0) + income.incomeAccruedSumm
                }
            }
        }

        boolean hasNextApp2DeductionColGroup() {
            return deductionColGroupsIterator.hasNext()
        }

        App2DeductionColGroup nextApp2DeductionColGroup() {
            return deductionColGroupsIterator.next()
        }

        @Override
        String toString() {
            StringBuilder stringBuilder = new StringBuilder()
            stringBuilder << (incomeCode ?: "") << "|"
            stringBuilder << format17_2(incomeAccruedSum) << "|"
            for (def deductionColGroup : deductionColGroups) {
                stringBuilder << deductionColGroup.toString()
            }
            return stringBuilder.toString()
        }
    }
    // группа столбцов строки Приложения2 с данными по отдельному коду вычета
    class App2DeductionColGroup {
        // номер столбца, с которого начинается эта группа столбцов
        int colNum
        // код вычета
        String deductionCode
        // сумма вычета для строк 3 раздела с текущим кодом вычета
        BigDecimal periodCurrSum = null

        App2DeductionColGroup(int colNum) {
            this.colNum = colNum
        }

        void setData(String deductionCode, List<NdflPersonDeduction> deductions) {
            this.deductionCode = deductionCode
            for (def deduction : deductions) {
                if (deduction.periodCurrSumm != null) {
                    periodCurrSum = (periodCurrSum ?: 0) + deduction.periodCurrSumm
                }
            }
        }

        @Override
        String toString() {
            StringBuilder stringBuilder = new StringBuilder()
            stringBuilder << (deductionCode ?: "") << "|"
            stringBuilder << format17_2(periodCurrSum) << "|"
            return stringBuilder.toString()
        }
    }
    // Итоговая строка
    class TotalRow {
        // значения строки по позиции поля (начиная с 1)
        Map<Integer, BigDecimal> values = [:]

        void add(App2PersonRateRowGroup rowGroup) {
            add(24, rowGroup.incomeAccruedSum)
            add(25, rowGroup.incomeDeductionSum)
            add(26, rowGroup.taxBaseSum)
            add(27, rowGroup.calculatedTaxSum)
            add(28, rowGroup.withholdingTaxSum)
            add(29, rowGroup.taxSum ? rowGroup.taxSum : null)
            add(30, rowGroup.overholdingTaxSum)
            add(31, rowGroup.notholdingTaxSum)
        }

        void add(int index, BigDecimal valueToAdd) {
            BigDecimal value = values.get(index) ?: 0
            value += (valueToAdd ?: 0)
            values.put(index, value)
        }

        @Override
        String toString() {
            StringBuilder stringBuilder = new StringBuilder("\r\n")
            for (int i = 1; i <= 71; i++) {
                stringBuilder << format17_2(values.get(i)) << "|"
            }
            return stringBuilder.toString()
        }
    }


    Map<String, Integer> deductionMarkCodeByDeductionTypeCodeCache = [:]

    /**
     * Возвращяет код признака кода вычета по коду вычета
     */
    Integer getDeductionMarkCodeByDeductionTypeCode(String code) {
        Integer markCode = deductionMarkCodeByDeductionTypeCodeCache.get(code)
        if (!markCode) {
            def typeRecords = refBookFactory.getDataProvider(RefBook.Id.DEDUCTION_TYPE.id).getRecordDataVersionWhere(" where code = '${code}'", new Date())
            if (1 == typeRecords.entrySet().size()) {
                def typeRecord = typeRecords.entrySet().iterator().next().getValue()
                def markId = typeRecord.get("DEDUCTION_MARK").referenceValue
                def markRecords = refBookFactory.getDataProvider(RefBook.Id.DEDUCTION_MARK.id).getRecordDataVersionWhere(" where id = ${markId}", new Date())
                if (1 == markRecords.entrySet().size()) {
                    def markRecord = markRecords.entrySet().iterator().next().getValue()
                    markCode = markRecord.get("CODE").numberValue.intValue()
                    deductionMarkCodeByDeductionTypeCodeCache.put(code, markCode)
                }
            } else {
                throw new ServiceException("Не найден код вычета по коду \"${code}\"")
            }
        }
        return markCode
    }
}