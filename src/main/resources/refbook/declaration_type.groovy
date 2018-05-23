package refbook // declaration_type_ref комментарий для локального поиска скрипта

import com.aplana.sbrf.taxaccounting.AbstractScriptClass

import com.aplana.sbrf.taxaccounting.model.TAUserInfo
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import com.aplana.sbrf.taxaccounting.script.dao.BlobDataService
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentReportPeriodService
import com.aplana.sbrf.taxaccounting.script.service.DepartmentService
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService
import com.aplana.sbrf.taxaccounting.service.impl.TAAbstractScriptingServiceImpl
import groovy.transform.TypeChecked
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
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 * Created by lhaziev on 09.02.2017.
 */

(new DeclarationType(this)).run();

@TypeChecked
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

    private DeclarationType() {
    }

    @TypeChecked(groovy.transform.TypeCheckingMode.SKIP)
    DeclarationType(scriptClass) {
        super(scriptClass)
        if (scriptClass.getBinding().hasVariable("departmentReportPeriodService")) {
            this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("declarationService")) {
            this.declarationService = (DeclarationService) scriptClass.getProperty("declarationService");
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService");
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("userInfo")) {
            this.userInfo = (TAUserInfo) scriptClass.getProperty("userInfo");
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory");
        }
        if (scriptClass.getBinding().hasVariable("blobDataServiceDaoImpl")) {
            this.blobDataServiceDaoImpl = (BlobDataService) scriptClass.getBinding().getProperty("blobDataServiceDaoImpl");
        }
        if (scriptClass.getBinding().hasVariable("msgBuilder")) {
            msgBuilder = (StringBuilder) scriptClass.getBinding().getProperty("msgBuilder")
        }
        if (scriptClass.getBinding().hasVariable("userInfo")) {
            this.userInfo = (TAUserInfo) scriptClass.getProperty("userInfo");
        }
        if (scriptClass.getBinding().hasVariable("dataFile")) {
            this.dataFile = (File) scriptClass.getProperty("dataFile");
        }
        if (scriptClass.getBinding().hasVariable("fileType")) {
            this.fileType = (TransportFileType) scriptClass.getProperty("fileType");
        }
        if (scriptClass.getBinding().hasVariable("UploadFileName")) {
            this.UploadFileName = (String) scriptClass.getProperty("UploadFileName");
        }
        if (scriptClass.getBinding().hasVariable("ImportInputStream")) {
            this.ImportInputStream = (InputStream) scriptClass.getProperty("ImportInputStream");
        }
    }

    @Override
    public void run() {
        switch (formDataEvent) {
            case FormDataEvent.IMPORT_TRANSPORT_FILE:
                importTF()
                break
        }
    }

    public class SAXHandler extends DefaultHandler {
        // Хранит содержимое атрибута
        private Map<String, Map<String, String>> attrValues;
        private Map<String, List<String>> findAttrNames;

        // Хранит содержимое узла
        private List<String> nodeValueList;
        private boolean isNodeNameFind;
        private boolean isParentNodeNameFind;
        private boolean isGetValueAttributesTag = false;
        private String nodeNameFind;
        private String parentNodeNameFind;

        //Хранит содержимое атрибутов(название атрибута, значение)
        public Map<String, String> ListValueAttributesTag = null;

        public SAXHandler(Map<String, List<String>> findAttrNames) {
            this.findAttrNames = findAttrNames;
        }

        public Map<String, String> getListValueAttributesTag() {
            return ListValueAttributesTag;
        }

        public SAXHandler(String nodeNameFind, String parentNodeNameFind) {
            this.findAttrNames = new HashMap<String, List<String>>();
            this.nodeValueList = new ArrayList<>();
            this.nodeNameFind = nodeNameFind;
            this.parentNodeNameFind = parentNodeNameFind;
        }

        public SAXHandler(String nodeNameFind, String parentNodeNameFind, boolean isGetValueAttributes) {
            this(nodeNameFind, parentNodeNameFind);
            this.ListValueAttributesTag = new HashMap<String, String>();
            this.isGetValueAttributesTag = isGetValueAttributes;
        }

        public Map<String, Map<String, String>> getAttrValues() {
            return attrValues;
        }

        public List<String> getNodeValueList() {
            return nodeValueList;
        }

        @Override
        public void startDocument() throws SAXException {
            attrValues = new HashMap<String, Map<String, String>>();
            for (Map.Entry<String, List<String>> entry : findAttrNames.entrySet()) {
                attrValues.put(entry.getKey(), new HashMap<String, String>());
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            for (Map.Entry<String, List<String>> entry : findAttrNames.entrySet()) {
                if (entry.getKey().equals(qName)) {
                    for (String attrName : entry.getValue()) {
                        attrValues.get(qName).put(attrName, attributes.getValue(attrName));
                    }
                }
            }
            if (qName.equals(nodeNameFind)) {
                isNodeNameFind = true;
            }
            if (qName.equals(parentNodeNameFind)) {
                isParentNodeNameFind = true;
            }
            //Заполняем атрибуты тега
            if (isGetValueAttributesTag && isParentNodeNameFind && isNodeNameFind) {
                for (int i = 0; i < attributes.length; i++) {
                    ListValueAttributesTag.put(attributes.getQName(i), attributes.getValue(attributes.getQName(i)))
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (qName.equals(nodeNameFind)) {
                isNodeNameFind = false;
            }
            if (qName.equals(parentNodeNameFind)) {
                isParentNodeNameFind = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (isNodeNameFind && isParentNodeNameFind) {
                nodeValueList.add(new String(ch, start, length));
            }
        }
    }

    String TAG_DOCUMENT = "Документ";

    String ATTR_PERIOD = "Период";

    String ATTR_YEAR = "ОтчетГод";

    String ERROR_NAME_FORMAT = "Имя транспортного файла «%s» не соответствует формату!";
    String ERROR_NOT_FOUND_FILE_NAME = "Не найдено имя отчетного файла в файле ответа «%s»!";
    String ERROR_NOT_FOUND_FORM = "Не найдена форма, содержащая «%s», для файла ответа «%s»";

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
    final String NDFL2_UU_FILE_TAG = "СвКвит"
    final String NDFL2_UU_FILE_ATTR = "ИмяОбрабФайла"
    final String NDFL2_1 = "2 НДФЛ (1)"
    final String NDFL2_2 = "2 НДФЛ (2)"
    final String NDFL6 = "6 НДФЛ"

    final KND_ACCEPT = 1166002 // Принят
    final KND_REFUSE = 1166006 // Отклонен
    final KND_SUCCESS = 1166007 // Успешно отработан
    final KND_REQUIRED = 1166009 // Требует уточнения

// Идентификаторы видов деклараций
    final Integer DECLARATION_TYPE_RNU_NDFL_ID = 100

    String NAME_EXTENSION_DEC = ".xml";
    int NAME_LENGTH_QUARTER_DEC = 63;

// Кэш провайдеров
    def providerCache = [:]

    final long REF_BOOK_DEPARTMENT_ID = RefBook.Id.DEPARTMENT.id

    Map<String, String> getPeriodNdflMap() {
        Map<String, String> periodNdflMap = new HashMap<String, String>();
        periodNdflMap.put("21", "21");
        periodNdflMap.put("32", "31");
        periodNdflMap.put("33", "33");
        periodNdflMap.put("34", "34");
        return periodNdflMap;
    }

    def importTF() {
        switch (fileType) {
            case TransportFileType.RNU_NDFL:
                importNDFL()
                break;
            case TransportFileType.RESPONSE_6_NDFL:
                importNdflResponse()
                break;
            case TransportFileType.RESPONSE_2_NDFL:
                importNdflResponse()
                break;
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
    def getFileName(Map<String, Map<String, String>> contentMap, String fileName) {
        if (fileName.startsWith(ANSWER_PATTERN_NDFL_1)) {
            return contentMap.get(NDFL2_KV_FILE_TAG).get(NDFL2_KV_FILE_ATTR)
        }

        if (fileName.startsWith(ANSWER_PATTERN_NDFL_2)) {
            return contentMap.get(NDFL2_UO_FILE_TAG).get(NDFL2_UO_FILE_ATTR)
        }

        if (fileName.startsWith(ANSWER_PATTERN_NDFL_3)) {
            return contentMap.get(NDFL2_IV_FILE_TAG).get(NDFL2_IV_FILE_ATTR)
        }

        if (fileName.startsWith(ANSWER_PATTERN_NDFL_4)) {
            return contentMap.get(NDFL2_UU_FILE_TAG).get(NDFL2_UU_FILE_ATTR)
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
    final String NDFL2_NOT_CORRECT_SKIP = "---"
    final String NDFL2_CORRECT_NUMB_REF = "Номер справки:"
    final Pattern NDFL2_CORRECT_ADDRESS_PATTERN_BEFORE = Pattern.compile("\\s*Адрес ДО исправления:.+")
    final Pattern NDFL2_CORRECT_ADDRESS_PATTERN_AFTER = Pattern.compile("\\s*Адрес ПОСЛЕ исправления:.+")
    final Pattern NDFL2_CORRECT_ADDRESS_PATTERN_VALID = Pattern.compile("\\s*Адрес ПРИЗНАН ВЕРНЫМ \\(ИФНСМЖ - (.+)\\)\\s*")
    final String NDFL2_PROTOCOL_DATE = "ПРОТОКОЛ №"
    final Pattern NDFL2_PROTOCOL_DATE_PATTERN = Pattern.compile("ПРОТОКОЛ № .+ от (\\d{2}\\.\\d{2}\\.\\d{4})")
    final String NDFL2_REGISTER_DATE = "РЕЕСТР N"
    final Pattern NDFL2_REGISTER_DATE_PATTERN = Pattern.compile("РЕЕСТР N .+ от (\\d{2}\\.\\d{2}\\.\\d{4}) в 9979")
    final Pattern NDFL6_FILE_NAME_PATTERN = Pattern.compile("((KV)|(UO)|(IV)|(UU))_(NONDFL.)_(.{19})_(.{19})_(.{4})_(\\d{4}\\d{2}\\d{2})_(.{1,36})\\.(xml|XML)")
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
                        if ((lastEntry != null) && (lastEntry.size()==sizeLastEntry)){
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
    Map<String, Map<String, String>> readNdfl6ResponseContent() {
        def sett = [:]

        SAXHandler handler

        if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_1)) {
            handler = new SAXHandler('ИмяОбрабФайла', 'СвКвит')
        }

        if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_2)) {
            handler = new SAXHandler('ИмяОбрабФайла', 'ВыявлНарФайл')
        }

        if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_3)) {
            sett.put(NDFL2_IV_FILE_TAG, [NDFL2_IV_FILE_ATTR])
            handler = new SAXHandler(sett)
        }

        if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_4)) {
            handler = new SAXHandler('ИмяОбрабФайла', 'ВыявлОшФайл')
        }

        InputStream inputStream
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

        Map<String, Map<String, String>> result = [:]
        Map<String, String> value = [:]
        if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_1)) {
            value.put(NDFL2_KV_FILE_ATTR, handler.nodeValueList.size() > 0 ? handler.nodeValueList.get(0) : null)
            result.put(NDFL2_KV_FILE_TAG, value)
        } else if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_2)) {
            value.put(NDFL2_UO_FILE_ATTR, handler.nodeValueList.size() > 0 ? handler.nodeValueList.get(0) : null)
            result.put(NDFL2_UO_FILE_TAG, value)
        } else if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_3)) {
            result = handler.getAttrValues()
        } else if (UploadFileName.startsWith(ANSWER_PATTERN_NDFL_4)) {
            value.put(NDFL2_UU_FILE_ATTR, handler.nodeValueList.size() > 0 ? handler.nodeValueList.get(0) : null)
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
            Map<String, Map<String, String>> ndfl6Content = readNdfl6ResponseContent()

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
        def fileTypeId = fileTypeProvider.getUniqueRecordIds(new Date(), "CODE = ${AttachFileType.TYPE_2.code}").get(0)
        List<DeclarationData> declarationDataList = declarationService.findDeclarationDataByFileNameAndFileType(reportFileName, fileTypeId)

        if (declarationDataList.isEmpty()) {
            logger.error(ERROR_NOT_FOUND_FORM, reportFileName, UploadFileName);
            return
        }
        if (declarationDataList.size() > 1) {
            def result = ""
            declarationDataList.each { DeclarationData declData ->
                result += "\"${AttachFileType.TYPE_2.title}\", \"${declData.kpp}\", \"${declData.oktmo}\"; "
            }
            logger.error(ERROR_NOT_FOUND_FORM + ": " + result, reportFileName, UploadFileName);
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
        InputStream inputStream
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
            // handl.getListValueAttributesTag().get('xsi:noNamespaceSchemaLocation');
            handl.getListValueAttributesTag().get('xsi:noNamespaceSchemaLocation')

            DeclarationTemplateFile templateFile = null
            for (DeclarationTemplateFile tf : declarationTemplate.declarationTemplateFiles) {
                if (tf.fileName.equals(handl.getListValueAttributesTag().get('xsi:noNamespaceSchemaLocation'))) {
                    templateFile = tf;
                    break
                }
            }

            if (!templateFile) {
                logger.error("Для файл ответа \"%s\" не найдена xsd схема", UploadFileName)
                return
            }

            declarationService.validateDeclaration(userInfo, logger, dataFile, UploadFileName, templateFile.blobDataId)

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

                                ndflRef.ERRTEXT.value = "Путь к реквизиту: \"${entry.path}\"; Значение элемента: \"${entry.val}\"; Текст ошибки: \"${entry.text}\"".toString()
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

                                String errtext;
                                if (ndflRef.ERRTEXT.value == null || ndflRef.ERRTEXT.value.toString().isEmpty()) {
                                    errtext = ""
                                } else {
                                    errtext = ndflRef.ERRTEXT.value.toString() + ".\n"
                                }
                                if (entry.valid) {
                                    ndflRef.ERRTEXT.setValueForce(errtext + "Текст ошибки от ФНС: \"${entry.addressBefore}\"; (Адрес признан верным (ИФНСМЖ - ${entry.valid}))".toString())
                                } else {
                                    ndflRef.ERRTEXT.setValueForce(errtext + "Текст ошибки от ФНС: \"${entry.addressBefore}\" ДО исправления; (\"${entry.addressAfter}\" ПОСЛЕ исправления)".toString())
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
                        .append(", Вид: \"").append(declarationTemplate.type.getName()).append("\"");
                logger.info(msgBuilder.toString())
            }
        }
        // "Дата-время файла" = "Дата и время документа" раздела Параметры файла ответа
        Date fileDate = null

        if (isNdfl6Response(UploadFileName)) {
            fileDate = Date.parse("yyyyMMdd", UploadFileName.substring(56, 64))
        } else if (isNdfl2ResponseReestr(UploadFileName)) {
            fileDate = Date.parse("dd.MM.yyyy", ndfl2ContentReestrMap.get(NDFL2_REGISTER_DATE))
        } else if (isNdfl2ResponseProt(UploadFileName)) {
            fileDate = Date.parse("dd.MM.yyyy", (String) ndfl2ContentMap.get(NDFL2_PROTOCOL_DATE))
        }

        // Сохранение файла ответа в форме
        def fileUuid = blobDataServiceDaoImpl.create(dataFile, UploadFileName, new Date())
        def createUser = declarationService.getSystemUserInfo().getUser()
        def fileTypeSaveId = fileTypeProvider.getUniqueRecordIds(new Date(), "CODE = ${AttachFileType.TYPE_3.code}").get(0)

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
                declarationService.setDocStateId(declarationData.id, docStateId)
            }
        }
        declarationService.createPdfReport(logger, declarationData, userInfo)
    }

    /**
     * Валидирует xml-файл по xsd схеме макета
     * @param declarationTemplate макет формы
     */
    def boolean validate(DeclarationTemplate declarationTemplate) {
        declarationService.validateDeclaration(userInfo, logger, dataFile, UploadFileName, declarationTemplate.xsdId)

        if (logger.containsLevel(LogLevel.ERROR)) {
            logger.error("Файл ответа \"%s\" не соответствует формату", UploadFileName)
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
        Integer declarationTypeId;
        int departmentId;
        String departmentCode;
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

        Long declarationDataId = null

        // Имя файла, для которого сформирован ответ
        def String declarationDataFileNameReport = null

        if (UploadFileName != null && UploadFileName.toLowerCase().endsWith(NAME_EXTENSION_DEC)
                & UploadFileName.length() == NAME_LENGTH_QUARTER_DEC) {
            // РНУ_НДФЛ (первичная)
            declarationTypeId = DECLARATION_TYPE_RNU_NDFL_ID;
            attachFileType = AttachFileType.TYPE_1
            departmentCode = UploadFileName.substring(0, 17).replaceFirst("_*", "").trim();

            List<Department> formDepartments = departmentService.getDepartmentsBySbrfCode(departmentCode, true);
            if (formDepartments.size() == 0) {
                logger.error("Не удалось определить подразделение \"%s\"", departmentCode)
                return
            }
            if (formDepartments.size() > 1) {
                String departments = "";
                for(Department department : formDepartments) {
                    departments = departments + "\"" + department.getName().replaceAll("\"", "") + "\", ";
                }
                departments = departments.substring(0, departments.length() - 2);

                logger.error("ТФ с именем \"%s\" не может быть загружен в Систему, в справочнике «Подразделения» АС «Учет налогов» с кодом \"%s\" найдено " +
                        "несколько подразделений: %s. Обратитесь к пользователю с ролью \"Контролёр УНП\"", UploadFileName, departmentCode, departments);

                return
            }

            Department formDepartment = formDepartments.get(0);
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
        } else {
            // Неизвестный формат имени загружаемого файла
            throw new IllegalArgumentException(String.format(ERROR_NAME_FORMAT, UploadFileName));
        }

        com.aplana.sbrf.taxaccounting.model.DeclarationType declarationType = declarationService.getTemplateType(declarationTypeId);

        // Указан недопустимый код периода
        ReportPeriod reportPeriod = reportPeriodService.getByTaxTypedCodeYear(TaxType.NDFL, reportPeriodCode, year);
        if (reportPeriod == null) {
            logger.error("Для вида налога «%s» в Системе не создан период с кодом «%s», календарный год «%s»! Загрузка файла «%s» не выполнена.", TaxType.NDFL.getName(), reportPeriodCode, year, UploadFileName);
            return;
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
            logger.error("Ошибка при обработке данных транспортного файла. Загрузка файла не выполнена. %s", e.getMessage());
            return;
        }

        DeclarationTemplate declarationTemplate = declarationService.getTemplate(declarationTemplateId);
        if (!TAAbstractScriptingServiceImpl.canExecuteScript(declarationService.getDeclarationTemplateScript(declarationTemplateId), FormDataEvent.IMPORT_TRANSPORT_FILE)) {
            logger.error("Для налоговой формы загружаемого файла \"%s\" не предусмотрена обработка транспортного файла! Загрузка не выполнена.", UploadFileName);
            return;
        }

        // АСНУ
        Long asnuId = null;
        def asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
        if (asnuCode != null) {
            List<Long> asnuIds = asnuProvider.getUniqueRecordIds(null, "CODE = '" + asnuCode + "'");
            if (asnuIds.size() == 1) {
                asnuId = asnuIds.get(0);
            }
        }

        // Назначение подразделению Декларации
        List<DepartmentDeclarationType> ddts = declarationService.getDDTByDepartment(departmentId,
                TaxType.NDFL, reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
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

        if (validate(declarationTemplate)) {
            //достать из XML файла атрибуты тега СлЧасть
            SAXHandler handler = new SAXHandler('СлЧасть', 'Файл', true)
            InputStream inputStream
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
            handler.getListValueAttributesTag();

            //Проверка на соответствие имени и содержимого ТФ в теге Файл.СлЧасть
            def xmlDepartmentCode = handler.getListValueAttributesTag()?.get(KOD_DEPARTMENT)?.replaceFirst("_*", "")?.trim()
            if (!departmentCode.equals(xmlDepartmentCode)) {
                logger.error("В ТФ не совпадают значения параметра имени «Код подразделения» = «%s» и параметра содержимого «Файл.СлЧасть.КодПодр» = «%s»",
                        departmentCode, xmlDepartmentCode)
            }

            def xmlAsnuCode = handler.getListValueAttributesTag()?.get(KOD_ASNU)
            if (!asnuCode.equals(xmlAsnuCode)) {
                logger.error("В ТФ не совпадают значения параметра имени «Код АСНУ» = «%s» и параметра содержимого «Файл.СлЧасть.КодАС» = «%s»",
                        asnuCode, xmlAsnuCode)
            }

            def xmlFileName = handler.getListValueAttributesTag()?.get(NAME_TF_NOT_EXTENSION)
            if (!UploadFileName.trim().substring(0, UploadFileName.length() - 4).equals(xmlFileName)) {
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
            if (!reportPeriodCode.equals(xmlReportPeriodCode)) {
                logger.error("В ТФ не совпадают значения параметра имени «Код периода» = «%s» и параметра содержимого «Файл.ИнфЧасть.ПериодОтч» = «%s»",
                        reportPeriodCode, xmlReportPeriodCode)
            }

            def xmlReportYear = handler.getListValueAttributesTag()?.get(REPORT_YEAR)
            Integer reportYear = null
            try {
                reportYear = Integer.parseInt(xmlReportYear);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace()
            }

            if (!year.equals(reportYear)) {
                logger.error("В ТФ не совпадают значения параметра имени «Год» = «%s» и параметра содержимого «Файл.ИнфЧасть.ОтчетГод» = «%s»",
                        year, xmlReportYear)
            }

            // Проверка не загружен ли уже такой файл в систему
            if (UploadFileName != null && !UploadFileName.isEmpty()) {
                DeclarationDataFilter declarationFilter = new DeclarationDataFilter();

                declarationFilter.setFileName(UploadFileName);
                declarationFilter.setTaxType(TaxType.NDFL);
                declarationFilter.setSearchOrdering(DeclarationDataSearchOrdering.ID);

                List<Long> declarationDataSearchResultItems = declarationService.getDeclarationIds(declarationFilter, declarationFilter.getSearchOrdering(), false);
                if (!declarationDataSearchResultItems.isEmpty()) {
                    logger.error("ТФ с именем «%s» уже загружен в систему.", UploadFileName)
                }
            }

            // Поиск экземпляра декларации
            DeclarationData declarationData = declarationService.find(declarationTemplateId, departmentReportPeriod.getId(), null, kpp, null, asnuId, UploadFileName);
            // Экземпляр уже есть
            if (declarationData != null) {
                logger.error("Экземпляр формы \"%s\" в \"%s\" уже существует! Загрузка файла «%s» не выполнена.", declarationType.getName(), formDepartment.getName(), UploadFileName);
                return;
            }

            if (logger.containsLevel(LogLevel.ERROR)) {
                return
            }
            // Создание экземпляра декларации
            declarationDataId = declarationService.create(logger, declarationTemplateId, userInfo, departmentReportPeriod, null, kpp, null, asnuId, UploadFileName, null, true);

            inputStream = new FileInputStream(dataFile)
            try {
                // Запуск события скрипта для разбора полученного файла
                declarationService.importDeclarationData(logger, userInfo, declarationService.getDeclarationData(declarationDataId), inputStream, UploadFileName, dataFile, attachFileType, createDateFile)
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
            msgBuilder.append("Выполнено создание налоговой формы: ")
                    .append("№: \"").append(declarationDataId).append("\"")
                    .append(", Период: \"").append(reportPeriod.getTaxPeriod().getYear() + " - " + reportPeriod.getName()).append("\"")
                    .append(getCorrectionDateString(departmentReportPeriod))
                    .append(", Подразделение: \"").append(formDepartment.getName()).append("\"")
                    .append(", Вид: \"").append(declarationType.getName()).append("\"")
                    .append(", АСНУ: \"").append(asnuProvider.getRecordData(asnuId).get("NAME").getStringValue()).append("\"");
            logger.info(msgBuilder.toString())
        }
    }

    String getSAXParseExceptionMessage(SAXParseException e) {
        return "Ошибка: ${e.getLineNumber() != -1 ? "Строка: ${e.getLineNumber()}" : ""}" +
                "${e.getColumnNumber() != -1 ? "; Столбец: ${e.getColumnNumber()}" : ""}" +
                "${e.getLocalizedMessage() ? "; ${e.getLocalizedMessage()}" : ""}"
    }

    String getCorrectionDateString(DepartmentReportPeriod reportPeriod) {
        def dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return reportPeriod.getCorrectionDate() != null ? String.format(" с датой сдачи корректировки %s", dateFormat.format(reportPeriod.getCorrectionDate())) : "";
    }

}