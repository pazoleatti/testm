package refbook.declaration_type

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.service.impl.*
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import org.apache.commons.io.IOUtils;
import groovy.transform.Field
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

@Field
String NAME_FORMAT_ERROR_DEC = "Имя транспортного файла «%s» не соответствует формату!";

@Field
String NO_RASCHSV_PATTERN = "NO_RASCHSV_(.*)_(.*)_(.{10})(.{9})_(.*)\\.(xml|XML)";
@Field
String NAME_EXTENSION_DEC = ".xml";
@Field
int NAME_LENGTH_QUARTER_DEC = 63;

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
    Integer declarationTypeId;
    int departmentId;
    String reportPeriodCode;
    String asnuCode = null;
    String guid = null;
    String kpp = null;
    Integer year = null;
    boolean isFNS = false;
    Pattern pattern = Pattern.compile(NO_RASCHSV_PATTERN);
    if (UploadFileName != null && UploadFileName.toLowerCase().endsWith(NAME_EXTENSION_DEC)
            & UploadFileName.length() == NAME_LENGTH_QUARTER_DEC) {
        declarationTypeId = 100;
        String departmentCode = UploadFileName.substring(0, 17).replaceFirst("_*", "").trim();
        Department formDepartment = departmentService.getDepartmentBySbrfCode(departmentCode, false);
        if (formDepartment) {
            logger.error("Не удалось определить подразделение")
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
    } else if (pattern.matcher(UploadFileName).matches()) {
        declarationTypeId = 200;
        reportPeriodCode = null;
        kpp = UploadFileName.replaceAll(NO_RASCHSV_PATTERN, "\$4");

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
        throw new IllegalArgumentException(String.format(NAME_FORMAT_ERROR_DEC, UploadFileName));
    }

    DeclarationType declarationType = declarationService.getTemplateType(declarationTypeId);

    // Указан недопустимый код периода
    ReportPeriod reportPeriod = reportPeriodService.getByTaxTypedCodeYear(declarationType.getTaxType(), reportPeriodCode, year);
    if (reportPeriod == null) {
        logger.error("Для вида налога «%s» в Системе не создан период с кодом «%s», календарный год «%s»! Загрузка файла «%s» не выполнена.", declarationType.getTaxType().getName(), reportPeriodCode, year, UploadFileName);
        return;
    }

    if (declarationTypeId == 200) {
        def provider = refBookFactory.getDataProvider(RefBook.Id.FOND_DETAIL.getId()); // Настройки подразделений сборов
        def results = provider.getRecords(new Date(117,1,1),null, "kpp = '$kpp'", null);
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

    Long declarationDataId = declarationService.create(logger, declarationTemplateId, userInfo, departmentReportPeriod, null, kpp, null, asnuId, UploadFileName, null);
    InputStream inputStream = new FileInputStream(dataFile)
    try {
        declarationService.importDeclarationData(logger, userInfo, declarationService.getDeclarationData(declarationDataId), inputStream, UploadFileName, dataFile, AttachFileType.TYPE_1)
    } finally {
        IOUtils.closeQuietly(inputStream);
    }
}
