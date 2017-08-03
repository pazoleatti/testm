package form_template.ndfl.consolidated_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.ndfl.*
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.model.util.Pair
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.service.script.*
import com.aplana.sbrf.taxaccounting.refbook.*
import groovy.transform.CompileStatic
import groovy.transform.Field
import groovy.transform.Memoized
import groovy.transform.TypeChecked
import groovy.util.slurpersupport.NodeChild

import javax.script.ScriptException
import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.Characters
import javax.xml.stream.events.XMLEvent
import com.aplana.sbrf.taxaccounting.model.refbook.*
import com.aplana.sbrf.taxaccounting.model.log.*
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Скрипт макета декларации РНУ-НДФЛ(консолидированная)
 */
initConfiguration()
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreate()
        break
    case FormDataEvent.CHECK: //Проверить
        checkData()
        break
    case FormDataEvent.CALCULATE: //консолидирование с формированием фиктивного xml
        clearData()
        consolidation()
        generateXml()
        break
    case FormDataEvent.AFTER_CALCULATE: // Формирование pdf-отчета формы
        declarationService.createPdfReport(logger, declarationData, userInfo)
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
        checkAccept()
        break
    case FormDataEvent.GET_SOURCES: //формирование списка ПНФ для консолидации
        getSourcesListForTemporarySolution()
        break
    case FormDataEvent.CREATE_EXCEL_REPORT: //создание xlsx отчета
        createXlsxReport()
        break
    case FormDataEvent.PREPARE_SPECIFIC_REPORT:
        // Подготовка для последующего формирования спецотчета
        prepareSpecificReport()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        // Формирование спецотчета
        createSpecificReport()
        break
}

@Field
final Logger logger = getProperty("logger")
@Field
final DeclarationData declarationData = getProperty("declarationData")
@Field
final DepartmentReportPeriodService departmentReportPeriodService = getProperty("departmentReportPeriodService")
@Field
final DeclarationService declarationService = getProperty("declarationService")
@Field
final ReportPeriodService reportPeriodService = getProperty("reportPeriodService")
@Field
final DepartmentService departmentService = getProperty("departmentService")
@Field
final RefBookService refBookService = getProperty("refBookService")
@Field
final FormDataService formDataService = getProperty("formDataService")
@Field
final String DATE_FORMAT = "dd.MM.yyyy"
@Field
final String DATE_FORMAT_FULL = "yyyy-MM-dd_HH-mm-ss"
@Field
Boolean showTiming = false

def initConfiguration() {
    final ConfigurationParamModel configurationParamModel = declarationService.getAllConfig(userInfo)
    String showTiming = configurationParamModel?.get(ConfigurationParam.SHOW_TIMING)?.get(0)?.get(0)
    if (showTiming.equals("1")) {
        this.showTiming = true
    }
}

def logForDebug(String message, Object... args) {
    if (showTiming) {
        logger.info(message, args)
    }
}

def getProperty(String name) {
    try {
        return super.getProperty(name)
    } catch (MissingPropertyException e) {
        return null
    }
}

/**
 * Карта соответствия адреса формы адресу в справочнике ФИАС
 */
@Field Map<Long, FiasCheckInfo> fiasAddressIdsCache = [:];

Map<Long, FiasCheckInfo> getFiasAddressIdsMap() {
    if (fiasAddressIdsCache.isEmpty()) {
        fiasAddressIdsCache = fiasRefBookService.checkAddressByFias(declarationData.id, 1);
    }
    return fiasAddressIdsCache;
}

def calcTimeMillis(long time) {
    long currTime = System.currentTimeMillis();
    return (currTime - time) + " мс)";
}

@Field final FormDataKind FORM_DATA_KIND = FormDataKind.CONSOLIDATED;

/**
 * Идентификатор шаблона РНУ-НДФЛ (консолидированная)
 */
@Field final int CONSOLIDATED_RNU_NDFL_TEMPLATE_ID = 101
@Field final int PRIMARY_RNU_NDFL_TEMPLATE_ID = 100
@Field final int NDFL_2_1_TEMPLATE_ID = 102
@Field final int NDFL_2_2_TEMPLATE_ID = 104
@Field final int NDFL_6_TEMPLATE_ID = 103
@Field def ndflPersonCache = [:]

//>------------------< CONSOLIDATION >----------------------<

/**
 * Консолидировать РНУ-НДФЛ, для получения источников используется метод getDeclarationSourcesInfo
 * Данный метод выполняет вызов скрипта (GET-SOURCES) и
 *
 */
void consolidation() {

    long time = System.currentTimeMillis();

    def declarationDataId = declarationData.id

    //декларация-приемник, true - заполнятся только текстовые данные для GUI и сообщений,true - исключить несозданные источники,ограничение по состоянию для созданных экземпляров список нф-источников
    List<Relation> sourcesInfo = declarationService.getDeclarationSourcesInfo(declarationData, true, false, null, userInfo, logger);

    List<Long> declarationDataIdList = collectDeclarationDataIdList(sourcesInfo);

    if (declarationDataIdList.isEmpty()) {
        throw new ServiceException("Ошибка консолидации. Не найдено ни одной формы-источника.");
    }

    logForDebug("Номера первичных НФ, включенных в консолидацию: " + declarationDataIdList + " (" + declarationDataIdList.size() + " записей, " + calcTimeMillis(time));

    List<NdflPerson> ndflPersonList = collectNdflPersonList(sourcesInfo);

    if (logger.containsLevel(LogLevel.ERROR)) {
        throw new ServiceException("При получении источников возникли ошибки. Консолидация НФ невозможна.");
    }


    time = System.currentTimeMillis();

    Map<Long, List<Long>> deletedPersonMap = getDeletedPersonMap(declarationDataIdList)

    //record_id, Map<String, RefBookValue>
    Map<Long, Map<String, RefBookValue>> refBookPersonMap = getActualRefPersonsByDeclarationDataIdList(declarationDataIdList);
    logForDebug("Выгрузка справочника Физические лица (" + refBookPersonMap.size() + " записей, " + calcTimeMillis(time));

    //id, Map<String, RefBookValue>
    Map<Long, Map<String, RefBookValue>> addressMap = getRefAddressByPersons(refBookPersonMap);
    logForDebug("Выгрузка справочника Адреса физических лиц (" + addressMap.size() + " записей, " + calcTimeMillis(time));

    //id, List<Map<String, RefBookValue>>
    Map<Long, List<Map<String, RefBookValue>>> personDocMap = getActualRefDulByDeclarationDataIdList(declarationDataIdList)
    logForDebug("Выгрузка справочника Документы физических лиц (" + personDocMap.size() + " записей, " + calcTimeMillis(time));

    logForDebug("Инициализация кэша справочников (" + calcTimeMillis(time));

    //Карта в которой хранится актуальный record_id и NdflPerson в котором объединяются данные о даходах
    SortedMap<Long, NdflPerson> ndflPersonMap = consolidateNdflPerson(ndflPersonList, declarationDataIdList);

    logForDebug(String.format("Количество физических лиц, загруженных из первичных НФ-источников: %d", ndflPersonList.size()));
    logForDebug(String.format("Количество уникальных физических лиц в формах-источниках по справочнику ФЛ: %d", ndflPersonMap.size()));


    time = System.currentTimeMillis();

    //разделы в которых идет сплошная нумерация
    def ndflPersonNum = 1;
    def incomesRowNum = 1;
    def deductionRowNum = 1;
    def prepaymentRowNum = 1;

    for (Map.Entry<Long, NdflPerson> entry : ndflPersonMap.entrySet()) {
        ScriptUtils.checkInterrupted()

        Long refBookPersonRecordId = entry.getKey();

        Map<String, RefBookValue> refBookPersonRecord = refBookPersonMap.get(refBookPersonRecordId);

        Long refBookPersonId = refBookPersonRecord?.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue()?.longValue();
        NdflPerson ndflPerson = entry.getValue();

        if (refBookPersonId == null) {
            String fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "")
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
            deletedPersonMap.get(refBookPersonRecordId).each { def personDeclarationDataId ->
                logger.errorExp("%s.", "Отсутствует связь со справочником \"Физические лица\"", fioAndInp,
                        "В налоговой форме № ${personDeclarationDataId} не удалось установить связь со справочником \"$R_PERSON\"")
            }
            continue
        }

        def incomes = ndflPerson.incomes;
        def deductions = ndflPerson.deductions;
        def prepayments = ndflPerson.prepayments;

        //Сортируем сначала по дате начисления, затем по дате выплаты
        incomes.sort { a, b -> (a.incomeAccruedDate <=> b.incomeAccruedDate) ?: a.incomePayoutDate <=> b.incomePayoutDate }
        deductions.sort { a, b -> a.incomeAccrued <=> b.incomeAccrued }
        prepayments.sort { a, b -> a.notifDate <=> b.notifDate }


        List<Map<String, RefBookValue>> personDocumentsList = personDocMap.get(refBookPersonId)

        //Выбираем ДУЛ с признаком включения в отчетность 1
        Map<String, RefBookValue> personDocumentRecord = personDocumentsList?.find {
            it.get("INC_REP")?.getNumberValue() == 1
        };

        Long addressId = refBookPersonRecord.get("ADDRESS")?.getReferenceValue();
        Map<String, RefBookValue> addressRecord = addressMap.get(addressId);

        if (personDocumentsList == null || personDocumentsList.isEmpty()) {
            logger.warn("Физическое лицо: " + buildRefBookPersonFio(refBookPersonRecord) + ", Идентификатор ФЛ: " + buildRefBookPersonId(refBookPersonRecord) + ", включено в форму без указания ДУЛ, отсутствуют данные в справочнике 'Документы, удостоверяющие личность'")
        } else if (personDocumentRecord == null || personDocumentRecord.isEmpty()) {
            logger.warn("Физическое лицо: " + buildRefBookPersonFio(refBookPersonRecord) + ", Идентификатор ФЛ: " + buildRefBookPersonId(refBookPersonRecord) + ", включено в форму без указания ДУЛ, отсутствуют данные в справочнике 'Документы, удостоверяющие личность' с признаком включения в отчетность: 1")
        }

        if (addressId != null && addressRecord == null) {
            logger.warn("Для физического лица: " + buildRefBookNotice(refBookPersonRecord) + ". Отсутствуют данные в справочнике 'Адреса физических лиц'");
            continue;
        }

        //Создание консолидированной записи NdflPerson
        def consolidatePerson = buildNdflPerson(refBookPersonRecord, personDocumentRecord, addressRecord);

        consolidatePerson.rowNum = ndflPersonNum;
        consolidatePerson.declarationDataId = declarationDataId

        //Доходы
        List<NdflPersonIncome> consolidatedIncomesList = new ArrayList<NdflPersonIncome>();
        for (NdflPersonIncome income : incomes) {
            NdflPersonIncome consolidatedIncome = consolidateDetail(income, incomesRowNum);
            consolidatedIncomesList.add(consolidatedIncome);
            incomesRowNum++;
        }
        consolidatePerson.setIncomes(consolidatedIncomesList);

        //Вычеты
        List<NdflPersonIncome> consolidatedDeductionsList = new ArrayList<NdflPersonIncome>();
        for (NdflPersonDeduction deduction : deductions) {
            NdflPersonDeduction consolidatedDeduction = consolidateDetail(deduction, deductionRowNum);
            consolidatedDeductionsList.add(consolidatedDeduction);
            deductionRowNum++;
        }
        consolidatePerson.setDeductions(consolidatedDeductionsList);

        //Авансы
        List<NdflPersonPrepayment> consolidatedPrepaymentsList = new ArrayList<NdflPersonPrepayment>();
        for (NdflPersonPrepayment prepayment : prepayments) {
            NdflPersonPrepayment consolidatedPrepayment = consolidateDetail(prepayment, prepaymentRowNum);
            consolidatedPrepaymentsList.add(consolidatedPrepayment);
            prepaymentRowNum++;
        }
        consolidatePerson.setPrepayments(consolidatedPrepaymentsList);

        ndflPersonService.save(consolidatePerson)
        ndflPersonNum++

    }

    logForDebug("Консолидация завершена, новых записей создано: " + (ndflPersonNum - 1) + ", " + calcTimeMillis(time));

}

String getVal(Map<String, RefBookValue> refBookPersonRecord, String attrName) {
    RefBookValue refBookValue = refBookPersonRecord.get(attrName);
    if (refBookValue != null) {
        return refBookValue.toString();
    } else {
        return null;
    }
}

/**
 * TODO Использовать метод com.aplana.sbrf.taxaccounting.dao.identification.IdentificationUtils#buildRefBookNotice(java.util.Map)
 * @param refBookPersonRecord
 * @return
 */
String buildRefBookNotice(Map<String, RefBookValue> refBookPersonRecord) {
    StringBuilder sb = new StringBuilder();
    sb.append("Номер '").append(getVal(refBookPersonRecord, "RECORD_ID")).append("': ");
    sb.append(getVal(refBookPersonRecord, "LAST_NAME")).append(" ");
    sb.append(getVal(refBookPersonRecord, "FIRST_NAME")).append(" ");
    sb.append(getVal(refBookPersonRecord, "MIDDLE_NAME")).append(" ");
    sb.append(" [id=").append(getVal(refBookPersonRecord, RefBook.RECORD_ID_ALIAS)).append("]");
    return sb.toString();
}

/**
 * TODO Использовать метод com.aplana.sbrf.taxaccounting.dao.identification.IdentificationUtils#buildRefBookNotice(java.util.Map)
 * @param refBookPersonRecord
 * @return
 */
String buildRefBookPersonFio(Map<String, RefBookValue> refBookPersonRecord) {
    StringBuilder sb = new StringBuilder();
    sb.append(getVal(refBookPersonRecord, "LAST_NAME")).append(" ");
    sb.append(getVal(refBookPersonRecord, "FIRST_NAME")).append(" ");
    sb.append(getVal(refBookPersonRecord, "MIDDLE_NAME"));
    return sb.toString();
}

/**
 * @param refBookPersonRecord
 * @return
 */
String buildRefBookPersonId(Map<String, RefBookValue> refBookPersonRecord) {
    return getVal(refBookPersonRecord, "RECORD_ID");
}

/**
 * Получить Записи справочника адреса физлиц, по записям из справочника физлиц
 * @param personMap
 * @return
 */
Map<Long, Map<String, RefBookValue>> getRefAddressByPersons(Map<Long, Map<String, RefBookValue>> personMap) {
    Map<Long, Map<String, RefBookValue>> result = new HashMap<Long, Map<String, RefBookValue>>()
    def addressIds = []
    def count = 0
    personMap.each { recordId, person ->
        if (person.get("ADDRESS").value != null) {
            Long addressId = person.get("ADDRESS")?.getReferenceValue()
            // Адрес может быть не задан
            if (addressId != null) {
                addressIds.add(addressId)
                count++
                if (count >= 1000) {
                    Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(RefBook.Id.PERSON_ADDRESS.getId()).getRecordData(addressIds)
                    refBookMap.each { id, address ->
                        result.put(id, address)
                    }
                    addressIds.clear()
                    count = 0
                }
            }
        }
    }

    if (addressIds.size() > 0) {
        Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(RefBook.Id.PERSON_ADDRESS.getId()).getRecordData(addressIds)
        refBookMap.each { addressId, address ->
            result.put(addressId, address)
        }
    }

    return result
}

/**
 * Получить "ДУЛ" по всем физлицам указвнных в НФ
 * @return
 */
Map<Long, List<Map<String, RefBookValue>>> getActualRefDulByDeclarationDataIdList(List<Long> declarationDataIdList) {
    Map<Long, List<Map<String, RefBookValue>>> result = new HashMap<Long, List<Map<String, RefBookValue>>>();
    declarationDataIdList.each {
        String whereClause = "exists (select 1 from ndfl_person np where np.declaration_data_id = ${it} AND ref_book_id_doc.person_id = np.person_id)"
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordWhere(REF_BOOK_ID_DOC_ID, whereClause)

        refBookMap.each { personId, refBookValues ->
            Long refBookPersonId = refBookValues.get("PERSON_ID").getReferenceValue();
            List<Map<String, RefBookValue>> dulList = result.get(refBookPersonId);
            if (dulList == null) {
                dulList = new ArrayList<Map<String, RefBookValue>>();
                result.put(refBookPersonId, dulList);
            }
            dulList.add(refBookValues);
        }
    }
    return result
}

/**
 * Получение списка удаленных ФЛ в виде мапы personId: List<declarationDataId>
 * @param declarationDataIdList
 */
@TypeChecked
Map<Long, List<Long>> getDeletedPersonMap(List<Long> declarationDataIdList) {
    Map<Long, List<Long>> result = [:]
    declarationDataIdList.each { Long it ->
        String whereClause = "exists (select 1 from ndfl_person np where np.declaration_data_id = ${it} AND ref_book_person.id = np.person_id)"
        Map<Long, Map<String, RefBookValue>> refPersonMap = getProvider(RefBook.Id.PERSON.id).getRecordDataWhere(whereClause)
        refPersonMap.each { Long k, Map<String, RefBookValue> v ->
            Long personId = v.get(RefBook.BUSINESS_ID_ALIAS).getNumberValue().longValue()
            if (!result.containsKey(personId)) {
                result.put(personId, new ArrayList<Long>())
            }
            result.get(personId).add(it)
        }
    }
    return result
}

/**
 * Получить список актуальных записей о физлицах, в нф
 * @param declarationDataIdList список id нф
 * @return
 */
Map<Long, Map<String, RefBookValue>> getActualRefPersonsByDeclarationDataIdList(List<Long> declarationDataIdList) {
    //Если исходных форм достаточно много то можно переделать запрос на получение списка declarationDataIdList
    def result = new HashMap<Long, Map<String, RefBookValue>>()
    declarationDataIdList.each {
        Map mapPersons = personRecordIdToValuesMap = getActualRefPersonsByDeclarationDataId(it)
        mapPersons.each { recordId, refBookValue ->
            result.put(recordId, refBookValue)
        }
    }
    return result
}

/**
 * Создает объект NdlPerson заполненный данными из справочника
 */
def buildNdflPerson(Map<String, RefBookValue> personRecord, Map<String, RefBookValue> identityDocumentRecord, Map<String, RefBookValue> addressRecord) {

    Map<Long, String> countryCodes = getRefCountryCode();
    Map<Long, Map<String, RefBookValue>> documentTypeRefBook = getRefDocumentType();
    Map<Long, String> taxpayerStatusCodes = getRefTaxpayerStatusCode();

    NdflPerson ndflPerson = new NdflPerson()

    //Данные о физлице - заполняется на основе справочника физлиц
    ndflPerson.personId = personRecord.get(RefBook.RECORD_ID_ALIAS)?.getNumberValue() //Идентификатор ФЛ
    ndflPerson.inp = personRecord.get("RECORD_ID")?.getNumberValue()
    ndflPerson.snils = personRecord.get("SNILS")?.getStringValue()
    ndflPerson.lastName = personRecord.get("LAST_NAME")?.getStringValue()
    ndflPerson.firstName = personRecord.get("FIRST_NAME")?.getStringValue()
    ndflPerson.middleName = personRecord.get("MIDDLE_NAME")?.getStringValue()
    ndflPerson.birthDay = personRecord.get("BIRTH_DATE")?.getDateValue()
    ndflPerson.innNp = personRecord.get("INN")?.getStringValue()
    ndflPerson.innForeign = personRecord.get("INN_FOREIGN")?.getStringValue()


    Long countryId = personRecord.get("CITIZENSHIP")?.getReferenceValue();

    ndflPerson.citizenship = countryCodes.get(countryId)

    //ДУЛ - заполняется на основе справочника Документы, удостоверяющие личность
    Map<String, RefBookValue> docTypeRecord = (identityDocumentRecord!= null)?documentTypeRefBook.get(identityDocumentRecord.get("DOC_ID")?.getReferenceValue()):null
    ndflPerson.idDocType = docTypeRecord?.get("CODE")?.getStringValue();

    ndflPerson.idDocNumber = identityDocumentRecord?.get("DOC_NUMBER")?.getStringValue()

    ndflPerson.status = taxpayerStatusCodes.get(personRecord.get("TAXPAYER_STATE")?.getReferenceValue())

    //адрес может быть не задан
    if (addressRecord != null) {
        ndflPerson.postIndex = addressRecord.get("POSTAL_CODE")?.getStringValue()
        ndflPerson.regionCode = addressRecord.get("REGION_CODE")?.getStringValue()
        ndflPerson.area = addressRecord.get("DISTRICT")?.getStringValue()
        ndflPerson.city = addressRecord.get("CITY")?.getStringValue()
        ndflPerson.locality = addressRecord.get("LOCALITY")?.getStringValue()
        ndflPerson.street = addressRecord.get("STREET")?.getStringValue()
        ndflPerson.house = addressRecord.get("HOUSE")?.getStringValue()
        ndflPerson.building = addressRecord.get("BUILD")?.getStringValue()
        ndflPerson.flat = addressRecord.get("APPARTMENT")?.getStringValue()
        ndflPerson.countryCode = countryCodes.get(addressRecord.get("COUNTRY_ID")?.getReferenceValue())
        ndflPerson.address = addressRecord.get("ADDRESS")?.getStringValue()
        //ndflPerson.additionalData = currentNdflPerson.additionalData
    }

    return ndflPerson
}

/**
 * При
 * @param ndflPersonDetail
 * @param i
 * @return
 */
def consolidateDetail(ndflPersonDetail, rowNum) {
    def sourceId = ndflPersonDetail.id;
    ndflPersonDetail.id = null
    ndflPersonDetail.ndflPersonId = null
    ndflPersonDetail.rowNum = rowNum
    ndflPersonDetail.sourceId = sourceId;
    return ndflPersonDetail
}

/**
 * Получаем список NdflPerson которые попадут в консолидированную форму
 * @param sourcesInfo
 * @return
 */
List<NdflPerson> collectNdflPersonList(List<Relation> sourcesInfo) {

    long time = System.currentTimeMillis();

    List<NdflPerson> result = new ArrayList<NdflPerson>();
    // собираем данные из источников
    int i = 0;
    for (Relation relation : sourcesInfo) {
        Long declarationDataId = relation.declarationDataId;
        if (!relation.declarationState.equals(State.ACCEPTED)) {
            logger.error(String.format("Налоговая форма-источник существует, но не может быть использована, так как еще не принята. Вид формы: \"%s\", подразделение: \"%s\", id=\"%d\"", relation.getDeclarationTypeName(), relation.getFullDepartmentName(), declarationDataId))
            continue
        }
        List<NdflPerson> ndflPersonList = findNdflPersonWithData(declarationDataId);

        //logger.info("Физических лиц в НФ "+declarationDataId+ ": " + ndflPersonList.size());

        result.addAll(ndflPersonList);
        i++;
    }

    logger.info(String.format("НФ-источников выбрано для консолидации (" + i + calcTimeMillis(time)))

    return result;
}

/**
 * Получаем список идентификаторов деклараций которые попадут в консолидированную форму
 * @param sourcesInfo
 * @return
 */
List<Long> collectDeclarationDataIdList(List<Relation> sourcesInfo) {
    def result = []
    for (Relation relation : sourcesInfo) {
        if (!result.contains(relation.declarationDataId)) {
            result.add(relation.declarationDataId)
        }
    }
    return result
}

/**
 * Найти все NdflPerson привязанные к НФ вместе с данными о доходах
 * @param declarationDataId
 * @return
 */
List<NdflPerson> findNdflPersonWithData(Long declarationDataId) {

    List<NdflPerson> result = new ArrayList<NdflPerson>();
    List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationDataId);

    Map<Long, List<NdflPersonOperation>> imcomesList = mapToPesonId(ndflPersonService.findNdflPersonIncome(declarationDataId));
    Map<Long, List<NdflPersonOperation>> deductionList = mapToPesonId(ndflPersonService.findNdflPersonDeduction(declarationDataId));
    Map<Long, List<NdflPersonOperation>> prepaymentList = mapToPesonId(ndflPersonService.findNdflPersonPrepayment(declarationDataId));

    for (NdflPerson ndflPerson : ndflPersonList) {
        Long ndflPersonId = ndflPerson.getId();
        ndflPerson.setIncomes(imcomesList.get(ndflPersonId));
        ndflPerson.setDeductions(deductionList.get(ndflPersonId));
        ndflPerson.setPrepayments(prepaymentList.get(ndflPersonId));
        result.add(ndflPerson);
    }
    return result;
}

/**
 * Объединение ndfl-person по record_id, в данном методе происходит объединение ФЛ по record id из одной или нескольких НФ
 * @param refBookPerson
 * @return
 */
Map<Long, NdflPerson> consolidateNdflPerson(List<NdflPerson> ndflPersonList, List<Long> declarationDataIdList) {

    Map<Long, NdflPerson> result = new TreeMap<Long, NdflPerson>();

    for (NdflPerson ndflPerson : ndflPersonList) {

        if (ndflPerson.personId == null || ndflPerson.recordId == null) {
            throw new ServiceException("Ошибка при консолидации данных. Необходимо повторно выполнить расчет формы " + ndflPerson.declarationDataId);
        }

        Long personRecordId = ndflPerson.recordId;

        NdflPerson consNdflPerson = result.get(personRecordId)

        //Консолидируем данные о доходах ФЛ, должны быть в одном разделе
        if (consNdflPerson == null) {
            consNdflPerson = new NdflPerson()
            consNdflPerson.recordId = personRecordId;
            consNdflPerson.inp = ndflPerson.inp
            consNdflPerson.lastName = ndflPerson.lastName
            consNdflPerson.firstName = ndflPerson.firstName
            consNdflPerson.middleName = ndflPerson.middleName
            result.put(personRecordId, consNdflPerson)
        }
        consNdflPerson.incomes.addAll(ndflPerson.incomes);
        consNdflPerson.deductions.addAll(ndflPerson.deductions);
        consNdflPerson.prepayments.addAll(ndflPerson.prepayments);
    }

    return result;
}

Map<Long, List<NdflPersonOperation>> mapToPesonId(List<NdflPersonOperation> operationList) {
    Map<Long, List<NdflPersonOperation>> result = new HashMap<Long, List<NdflPersonOperation>>()
    for (NdflPersonOperation personOperation : operationList) {
        Long ndflPersonId = personOperation.getNdflPersonId();
        if (!result.containsKey(ndflPersonId)) {
            result.put(ndflPersonId, new ArrayList<NdflPersonOperation>());
        }
        result.get(ndflPersonId).add(personOperation);
    }
    return result;
}

/**
 * Получить актуальные на дату окончания отчетного периода, записи справочника физлиц по record_id
 * @param personRecordIds
 * @return
 */
Long getActualPersonId(Long recordId) {
    Date version = getReportPeriodEndDate();
    List<Long> personIds = getProvider(RefBook.Id.PERSON.getId()).getUniqueRecordIds(version, "RECORD_ID = " + recordId);
    if (personIds.isEmpty()) {
        logger.error("Ошибка при получении записи из справочника 'Физические лица' с recordId=" + recordId);
        return null;
    } else {
        return personIds.get(0);
    }
}

/**
 * Создание фиктивной xml для привязки к экземпляру
 * declarationDataId
 * @return
 */
def generateXml() {
    def builder = new MarkupBuilder(xml)
    builder.Файл(имя: declarationData.id)
}

/**
 * Очистка данных налоговой формы
 * @return
 */
def clearData() {
    //удаляем рассчитанные данные если есть
    ndflPersonService.deleteAll(declarationData.id)
    //Удаляем все сформированные ранее отчеты налоговой формы
    declarationService.deleteReport(declarationData.id)
}

//>------------------< GET SOURCES >----------------------<

/**
 * Система (замена шага 1 ОС для целевого решения):
 * Ищет и включает в КНФ данные налоговых форм, у которых:
 * Вид = РНУ НДФЛ (первичная)
 * Состояние = "Принята"
 * Подразделением = КНФ.Подразделение.
 * Период = КНФ.Период
 * Далее без изменений по сравнению с целевым решением
 * @return
 */
def getSourcesListForTemporarySolution() {
    //отчетный период в котором выполняется консолидация
    ReportPeriod declarationDataReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    //Идентификатор подразделения по которому формируется консолидированная форма
    def parentDepartmentId = declarationData.departmentId
    //Department department = departmentService.get(parentDepartmentId)
    List<Department> departments = departmentService.getAllChildren(parentDepartmentId)

    List<DeclarationData> declarationDataList = findConsolidateDeclarationData(parentDepartmentId, departments.id, declarationDataReportPeriod.id)
    for (DeclarationData declarationData : declarationDataList) {
        //Формируем связь источник-приемник
        Department department = departmentService.get(declarationData.departmentId)
        def relation = getRelation(declarationData, department, declarationDataReportPeriod)
        sources.sourceList.add(relation)
    }
    sources.sourcesProcessedByScript = true
    //logger.info("sources found: " + sources.sourceList.size)
}

/**
 * Получить набор НФ источников события FormDataEvent.GET_SOURCES.
 *
 * Ищет и включает в КНФ данные налоговых форм, у которых:
 *      Подразделение является подчиненным по отношению к ТБ (уточнить у заказчика - включая сам ТБ?) согласно справочнику подразделений.
 *      Вид = РНУ НДФЛ (первичная)
 *      Состояние = "Принята"
 *      Отчетный период = Отчетный период КНФ или любой предыдущий отчетный период в рамках одного года.
 */
def getSourcesList() {

    if (!needSources) {
        return
    }

    //отчетный период в котором выполняется консолидация
    ReportPeriod declarationDataReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    //Идентификатор подразделения по которому формируется консолидированная форма
    def parentDepartmentId = declarationData.departmentId

    //Подразделения которые является подчиненным по отношению к ТБ, включая сам ТБ
    List<Department> departments = departmentService.getAllChildren(parentDepartmentId)

    //Список отчетных периодов которые должны быть включены в консолидированную форму (1 квартал, полугодие, 9 месяцев, год)
    List<ReportPeriod> reportPeriodList = reportPeriodService.getReportPeriodsByDate(TaxType.NDFL, declarationDataReportPeriod.startDate, declarationDataReportPeriod.endDate)

    for (Department department : departments) {

        for (ReportPeriod primaryReportPeriod : reportPeriodList) {

            List<DeclarationData> declarationDataList = findConsolidateDeclarationData(department.id, primaryReportPeriod.id)

            for (DeclarationData declarationData : declarationDataList) {
                //Формируем связь источник-приемник
                def relation = getRelation(declarationData, department, primaryReportPeriod)
                sources.sourceList.add(relation)
            }
        }
    }
    sources.sourcesProcessedByScript = true
    //logger.info("sources found: " + sources.sourceList.size)
}

/**
 * Ищет и включает в КНФ данные налоговых форму которых:
 * Вид = РНУ НДФЛ (первичная),
 * Подразделение является подчиненным по отношению к ТБ согласно справочнику подразделений.
 * Отчетный период = Отчетный период КНФ или любой предыдущий отчетный период в рамках одного года.
 * Если в некорректирующем и корректирующем (корректирующих) периодах, относящихся к одному отчетному периоду, найдены группы (множества, наборы) ПНФ с совпадающими параметрами: "Подразделение" И "АСНУ":
 * Система включает в КНФ множество ПНФ, относящихся к периоду с наиболее старшим периодом сдачи корректировки
 */
List<DeclarationData> findConsolidateDeclarationData(currDepartmentId, departmentIdList, reportPeriodId) {
    if (needSources) {
        //Список отчетных периодов подразделения
        List<DepartmentReportPeriod> departmentReportPeriodList = new ArrayList<DepartmentReportPeriod>();
        List<DeclarationData> allDeclarationDataList = []
        //List<List<Integer>> departmentsIdForSearch = departmentIdList.collate(1000)
        for (dep in departmentIdList) {
            //allDeclarationDataList.addAll(declarationService.findAllDeclarationDataForManyDepartments(PRIMARY_RNU_NDFL_TEMPLATE_ID, departmentIdList, reportPeriodId))
            List<DeclarationData> ddList = declarationService.findAllDeclarationData(PRIMARY_RNU_NDFL_TEMPLATE_ID, dep, reportPeriodId)
            if (ddList != null && !ddList.isEmpty()) {
                allDeclarationDataList.addAll(declarationService.findAllDeclarationData(PRIMARY_RNU_NDFL_TEMPLATE_ID, dep, reportPeriodId))
            }
        }
        DepartmentReportPeriod depReportPeriod = getDepartmentReportPeriodById(declarationData.departmentReportPeriodId)

        //Разбивка НФ по АСНУ и отчетным периодам <АСНУ+Подразделение, <Период, <Список НФ созданных в данном периоде>>>
        Map<String, Map<Integer, List<DeclarationData>>> asnuDataMap = new HashMap<String, HashMap<Integer, List<DeclarationData>>>();
        for (DeclarationData dD : allDeclarationDataList) {
            ScriptUtils.checkInterrupted();
            DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(dD?.departmentReportPeriodId) as DepartmentReportPeriod;
            // Период для того чтобы объединить первичные формы с разных подразделений для одного ТБ в рамках задачи https://jira.aplana.com/browse/SBRFNDFL-939
            DepartmentReportPeriod tempDepartmentReportPeriod = new DepartmentReportPeriod()
            tempDepartmentReportPeriod.setId(dD.departmentReportPeriodId)
            tempDepartmentReportPeriod.setDepartmentId(dD.departmentId)
            tempDepartmentReportPeriod.setReportPeriod(departmentReportPeriod.reportPeriod)
            tempDepartmentReportPeriod.setCorrectionDate(departmentReportPeriod.correctionDate)
            if (!(departmentReportPeriod.correctionDate == null || depReportPeriod.correctionDate != null && depReportPeriod.correctionDate >= departmentReportPeriod.correctionDate)) {
                continue
            }
            String asnuId = dD.getAsnuId() + "_" + dD.getDepartmentId()
            Integer departmentReportPeriodId = dD.departmentReportPeriodId;
            departmentReportPeriodList.add(tempDepartmentReportPeriod);
            if (asnuId != null) {
                Map<Integer, List<DeclarationData>> asnuMap = asnuDataMap.get(asnuId);
                if (asnuMap == null) {
                    asnuMap = new HashMap<String, DeclarationData>();
                    asnuDataMap.put(asnuId, asnuMap);
                }
                List<DeclarationData> declarationDataList = asnuMap.get(departmentReportPeriodId);
                if (declarationDataList == null) {
                    declarationDataList = new ArrayList<DeclarationData>();
                    asnuMap.put(departmentReportPeriodId, declarationDataList);
                }

                declarationDataList.add(dD);
            } else {
                logger.warn("Найдены НФ для которых не заполнено поле АСНУ. Подразделение: " + getDepartmentFullName(currDepartmentId) + ", отчетный период: " + reportPeriodId + ", id: " + dD.id);
            }
        }

        //Сортировка "Отчетных периодов" в порядке: Кор.период 1, Кор.период 2, некорректирующий период (не задана дата корректировки)
        departmentReportPeriodList.sort { a, b -> departmentReportPeriodComp(a, b) }

        //Включение в результат НФ с наиболее старшим периодом сдачи корректировки
        Set<DeclarationData> result = new HashSet<DeclarationData>();
        for (Map.Entry<String, Map<Integer, List<DeclarationData>>> entry : asnuDataMap.entrySet()) {
            ScriptUtils.checkInterrupted();
            Map<Long, List<DeclarationData>> asnuDeclarationDataMap = entry.getValue();
            List<DeclarationData> declarationDataList = getLast(asnuDeclarationDataMap, departmentReportPeriodList)
            result.addAll(declarationDataList);
//            if (depReportPeriod.correctionDate != null) {
//                result.addAll(getUncorrectedPeriodDeclarationData(asnuDeclarationDataMap, departmentReportPeriodList))
//            }
        }
        return result.toList();
    } else {
        ReportPeriod declarationDataReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(declarationData.departmentReportPeriodId)

        Department department = departmentService.get(declarationData.departmentId)

        List<DeclarationData> toReturn = []
        List<DeclarationData> declarationDataList = declarationService.findAllDeclarationData(NDFL_2_1_TEMPLATE_ID, department.id, declarationDataReportPeriod.id);
        declarationDataList.addAll(declarationService.findAllDeclarationData(NDFL_2_2_TEMPLATE_ID, department.id, declarationDataReportPeriod.id))
        declarationDataList.addAll(declarationService.findAllDeclarationData(NDFL_6_TEMPLATE_ID, department.id, declarationDataReportPeriod.id))
        for (DeclarationData declarationDataDestination : declarationDataList) {
            ScriptUtils.checkInterrupted();
            DepartmentReportPeriod departmentReportPeriodDestination = getDepartmentReportPeriodById(declarationDataDestination.departmentReportPeriodId)
            if (departmentReportPeriod.correctionDate != departmentReportPeriodDestination.correctionDate) {
                continue
            }
            toReturn.add(declarationDataDestination)
        }
        return toReturn
    }

}

/**
 * Возвращает список НФ по одной АСНУ, относящихся к периоду с наиболее старшим периодом сдачи корректировки
 * @param declarationDataMap НФ разбитые по периодам
 * @param departmentReportPeriodList список периодов, отстортированный по убыванию даты сдачи корректировки, null last
 * @return список НФ созданный АСНУ в старшем отчетном периоде
 */
List<DeclarationData> getLast(Map<Integer, List<DeclarationData>> declarationDataMap, List<DepartmentReportPeriod> departmentReportPeriodList) {
    for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
        Integer departmentReportPeriodId = departmentReportPeriod.getId()
        if (declarationDataMap.containsKey(departmentReportPeriodId)) {
            return declarationDataMap.get(departmentReportPeriodId)
        }
    }
    return Collections.emptyList();
}

/**
 * Реализует условие из временного решения: "Если период является корректирующим, в КНФ дополнительно надо включить ПНФ основного периода, соответствующего корректирующему."
 * @param declarationDataMap
 * @param departmentReportPeriodList
 * @return
 */
List<DeclarationData> getUncorrectedPeriodDeclarationData(Map<Integer, List<DeclarationData>> declarationDataMap, List<DepartmentReportPeriod> departmentReportPeriodList) {
    Set<DeclarationData> toReturn = [].toSet()

    List<DepartmentReportPeriod> uncorrectedPeriodDrpList = departmentReportPeriodList.findAll {
        it.correctionDate == null
    }

    for (DepartmentReportPeriod departmentReportPeriod : uncorrectedPeriodDrpList) {
        Integer departmentReportPeriodId = departmentReportPeriod.getId()
        if (declarationDataMap.containsKey(departmentReportPeriodId)) {
            toReturn.addAll(declarationDataMap.get(departmentReportPeriodId))
        }
    }
    return toReturn.toList()
}

def departmentReportPeriodComp(DepartmentReportPeriod a, DepartmentReportPeriod b) {

    if (a.getCorrectionDate() == null && b.getCorrectionDate() == null) {
        return b.getId().compareTo(a.getId());
    }

    if (a.getCorrectionDate() == null) {
        return 1;
    }

    if (b.getCorrectionDate() == null) {
        return -1;
    }

    int comp = b.getCorrectionDate().compareTo(a.getCorrectionDate());

    if (comp != 0) {
        return comp;
    }

    return b.getId().compareTo(a.getId());
}

/**
 * Получить запись для источника-приемника.
 *
 * @param declarationData первичная форма
 * @param department подразделение
 * @param period период нф
 * @param monthOrder номер месяца (для ежемесячной формы)
 */
def getRelation(DeclarationData declarationData, Department department, ReportPeriod period) {

    Relation relation = new Relation()

    //Привязка отчетных периодов к подразделениям
    DepartmentReportPeriod departmentReportPeriod = getDepartmentReportPeriodById(declarationData?.departmentReportPeriodId) as DepartmentReportPeriod

    //Макет НФ
    DeclarationTemplate declarationTemplate = getDeclarationTemplateById(declarationData?.declarationTemplateId)

    def isSource = declarationTemplate.id == PRIMARY_RNU_NDFL_TEMPLATE_ID ? true : false
    ReportPeriod rp = departmentReportPeriod.getReportPeriod();

    if (light) {
        //Идентификатор подразделения
        relation.departmentId = department.id
        //полное название подразделения
        relation.fullDepartmentName = getDepartmentFullName(department.id)
        //Дата корректировки
        relation.correctionDate = departmentReportPeriod?.correctionDate
        //Вид нф
        relation.declarationTypeName = declarationTemplate?.name
        //Год налогового периода
        relation.year = period.taxPeriod.year
        //Название периода
        relation.periodName = period.name
    }

    //Общие параметры

    //подразделение
    relation.department = department
    //Период
    relation.departmentReportPeriod = departmentReportPeriod
    //Статус ЖЦ
    relation.declarationState = declarationData?.state
    //форма/декларация создана/не создана
    relation.created = (declarationData != null)
    //является ли форма источников, в противном случае приемник
    relation.source = isSource;
    // Введена/выведена в/из действие(-ия)
    relation.status = declarationTemplate.status == VersionedObjectStatus.NORMAL
    // Налог
    relation.taxType = TaxType.NDFL

    //Параметры НФ

    // Идентификатор созданной формы
    relation.declarationDataId = declarationData?.id
    // Вид НФ
    relation.declarationTemplate = declarationTemplate
    return relation

}

//------------------ PREPARE_SPECIFIC_REPORT ----------------------

def prepareSpecificReport() {
    def reportAlias = scriptSpecificReportHolder?.declarationSubreport?.alias;
    if ('rnu_ndfl_person_db' != reportAlias) {
        throw new ServiceException("Обработка данного спец. отчета не предусмотрена!");
    }
    PrepareSpecificReportResult result = new PrepareSpecificReportResult();
    List<Column> tableColumns = createTableColumns();
    List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
    def rowColumns = createRowColumns()

    //Проверка, подготовка данных
    def params = scriptSpecificReportHolder.subreportParamValues
    def reportParameters = scriptSpecificReportHolder.getSubreportParamValues();

    if (reportParameters.isEmpty()) {
        throw new ServiceException("Для поиска физического лица необходимо задать один из критериев.");
    }

    def resultReportParameters = [:]
    reportParameters.each { key, value ->
        if (value != null) {
            def val = value;
            if (key in ["lastName", "firstName", "middleName", "inp"]) {
                val = '%'+value+'%'
            }
            resultReportParameters.put(key, val)
        }
    }

    // Ограничение числа выводимых записей
    int startIndex = 1
    int pageSize = 10

    PagingResult<NdflPerson> pagingResult = ndflPersonService.findNdflPersonByParameters(declarationData.id, resultReportParameters, startIndex, pageSize);

    if (pagingResult.isEmpty()) {
        subreportParamsToString = { it.collect { (it.value != null ? (((it.value instanceof Date)?it.value.format('dd.MM.yyyy'):it.value) + ";") : "") } join " " }
        logger.warn("Физическое лицо: " + subreportParamsToString(reportParameters) + " не найдено в форме");
        //throw new ServiceException("Физическое лицо: " + subreportParamsToString(reportParameters)+ " не найдено в форме");
    }

    pagingResult.getRecords().each() { ndflPerson ->
        DataRow<Cell> row = new DataRow<Cell>(FormDataUtils.createCells(rowColumns, null));
        row.getCell("id").setStringValue(ndflPerson.id.toString())
        row.lastName = ndflPerson.lastName
        row.firstName = ndflPerson.firstName
        row.middleName = ndflPerson.middleName
        row.snils = ndflPerson.snils
        row.innNp = ndflPerson.innNp
        row.inp = ndflPerson.inp
        row.birthDay = ndflPerson.birthDay
        row.idDocNumber = ndflPerson.idDocNumber
        row.statusNp = getPersonStatusName(ndflPerson.status)
        row.innForeign = ndflPerson.innForeign
        dataRows.add(row)
    }

    int countOfAvailableNdflPerson = pagingResult.size()

    if (countOfAvailableNdflPerson >= pageSize) {
        countOfAvailableNdflPerson = ndflPersonService.findNdflPersonCountByParameters(declarationData.id, resultReportParameters);
    }

    result.setTableColumns(tableColumns);
    result.setDataRows(dataRows);
    result.setCountAvailableDataRows(countOfAvailableNdflPerson)
    scriptSpecificReportHolder.setPrepareSpecificReportResult(result)
    scriptSpecificReportHolder.setSubreportParamValues(params)
}

String getPersonStatusName(String statusCode) {
    RefBookDataProvider provider = getProvider(RefBook.Id.TAXPAYER_STATUS.getId())
    PagingResult<Long, Map<String, RefBookValue>> record = provider.getRecords(getReportPeriodEndDate(), null, "CODE = '$statusCode'", null)
    return record.get(0).get("NAME").getValue()
}

def createTableColumns() {
    List<Column> tableColumns = new ArrayList<Column>()

    Column column1 = new StringColumn()
    column1.setAlias("lastName")
    column1.setName("Фамилия")
    column1.setWidth(10)
    tableColumns.add(column1)

    Column column2 = new StringColumn()
    column2.setAlias("firstName")
    column2.setName("Имя")
    column2.setWidth(10)
    tableColumns.add(column2)

    Column column3 = new StringColumn()
    column3.setAlias("middleName")
    column3.setName("Отчество")
    column3.setWidth(10)
    tableColumns.add(column3)

    Column column4 = new StringColumn()
    column4.setAlias("snils")
    column4.setName("СНИЛС")
    column4.setWidth(10)
    tableColumns.add(column4)

    Column column5 = new StringColumn()
    column5.setAlias("innNp")
    column5.setName("ИНН РФ")
    column5.setWidth(10)
    tableColumns.add(column5)

    Column column6 = new StringColumn()
    column6.setAlias("inp")
    column6.setName("ИНП")
    column6.setWidth(10)
    tableColumns.add(column6)

    Column column7 = new DateColumn()
    column7.setAlias("birthDay")
    column7.setName("Дата рождения")
    column7.setWidth(10)
    tableColumns.add(column7)

    Column column8 = new StringColumn()
    column8.setAlias("idDocNumber")
    column8.setName("№ ДУЛ")
    column8.setWidth(10)
    tableColumns.add(column8)

    Column column9 = new StringColumn()
    column9.setAlias("statusNp")
    column9.setName("Статус налогоплательщика")
    column9.setWidth(30)
    tableColumns.add(column9)

    Column column10 = new StringColumn()
    column10.setAlias("innForeign")
    column10.setName("ИНН Страны гражданства")
    column10.setWidth(10)
    tableColumns.add(column10)

    return tableColumns;
}

def createRowColumns() {
    List<Column> tableColumns = new ArrayList<Column>();

    Column columnId = new StringColumn()
    columnId.setAlias("id")
    columnId.setName("id")
    columnId.setWidth(10)
    tableColumns.add(columnId)

    Column column1 = new StringColumn()
    column1.setAlias("lastName")
    column1.setName("Фамилия")
    column1.setWidth(10)
    tableColumns.add(column1)

    Column column2 = new StringColumn()
    column2.setAlias("firstName")
    column2.setName("Имя")
    column2.setWidth(10)
    tableColumns.add(column2)

    Column column3 = new StringColumn()
    column3.setAlias("middleName")
    column3.setName("Отчество")
    column3.setWidth(10)
    tableColumns.add(column3)

    Column column4 = new StringColumn()
    column4.setAlias("snils")
    column4.setName("СНИЛС")
    column4.setWidth(10)
    tableColumns.add(column4)

    Column column5 = new StringColumn()
    column5.setAlias("innNp")
    column5.setName("ИНН РФ")
    column5.setWidth(10)
    tableColumns.add(column5)

    Column column6 = new StringColumn()
    column6.setAlias("inp")
    column6.setName("ИНП")
    column6.setWidth(10)
    tableColumns.add(column6)

    Column column7 = new DateColumn()
    column7.setAlias("birthDay")
    column7.setName("Дата рождения")
    column7.setWidth(10)
    tableColumns.add(column7)

    Column column8 = new StringColumn()
    column8.setAlias("idDocNumber")
    column8.setName("№ ДУЛ")
    column8.setWidth(10)
    tableColumns.add(column8)

    Column column9 = new StringColumn()
    column9.setAlias("statusNp")
    column9.setName("Статус налогоплательщика")
    column9.setWidth(30)
    tableColumns.add(column9)

    Column column10 = new StringColumn()
    column10.setAlias("innForeign")
    column10.setName("ИНН Страны гражданства")
    column10.setWidth(10)
    tableColumns.add(column10)

    return tableColumns;
}

//------------------ Create Report ----------------------

/**
 * Создать XLSX отчет
 * @return
 */
@TypeChecked
def createXlsxReport() {
    ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder = (ScriptSpecificDeclarationDataReportHolder)getProperty("scriptSpecificReportHolder")
    def params = new HashMap<String, Object>()
    params.put("declarationId", declarationData.getId());

    JasperPrint jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, declarationService.getXmlStream(declarationData.id));

    StringBuilder fileName = new StringBuilder("Реестр_загруженных_данных_").append(declarationData.id).append("_").append(new Date().format(DATE_FORMAT_FULL)).append(".xlsx")
    exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
    scriptSpecificReportHolder.setFileName(fileName.toString())
}

def createSpecificReport() {
    switch (scriptSpecificReportHolder?.declarationSubreport?.alias) {
        case 'rnu_ndfl_person_db':
            createSpecificReportPersonDb();
            break;
        case 'report_kpp_oktmo':
            createSpecificReportDb();
            ReportPeriod reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
            def reportPeriodName = reportPeriod.getTaxPeriod().year + '_' + reportPeriod.name
            Department department = departmentService.get(declarationData.departmentId)
            scriptSpecificReportHolder.setFileName("Реестр_сформированной_отчетности_${declarationData.id}_${reportPeriodName}_${department.shortName}_${new Date().format('yyyy-MM-dd_HH-mm-ss')}.xlsx")
            break;
        case 'rnu_ndfl_person_all_db':
            createSpecificReportDb();
            scriptSpecificReportHolder.setFileName("РНУ_НДФЛ_${declarationData.id}_${new Date().format('yyyy-MM-dd_HH-mm-ss')}.xlsx")
            break;
        default:
            throw new ServiceException("Обработка данного спец. отчета не предусмотрена!");
    }
}
/**
 * Спец. отчет "РНУ НДФЛ по физическому лицу". Данные макет извлекает непосредственно из бд
 */
def createSpecificReportPersonDb() {
    def row = scriptSpecificReportHolder.getSelectedRecord()
    def ndflPerson = ndflPersonService.get(Long.parseLong(row.id))
    def subReportViewParams = scriptSpecificReportHolder.getViewParamValues()
    subReportViewParams['Фамилия'] = row.lastName
    subReportViewParams['Имя'] = row.firstName
    subReportViewParams['Отчество'] = row.middleName
    subReportViewParams['Дата рождения'] = row.birthDay ? row.birthDay?.format(DATE_FORMAT) : ""
    subReportViewParams['№ ДУЛ'] = row.idDocNumber
    if (ndflPerson != null) {
        def params = [NDFL_PERSON_ID: ndflPerson.id];
        def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, null);
        exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
        scriptSpecificReportHolder.setFileName(createFileName(ndflPerson) + ".xlsx")
    } else {
        throw new ServiceException("Не найдены данные для формирования отчета!");
    }
}

@TypeChecked
void exportXLSX(JasperPrint jasperPrint, OutputStream data) {
    try {
        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT,
                jasperPrint);
        exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, data);
        exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE,
                Boolean.TRUE);
        exporter.setParameter(
                JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND,
                Boolean.FALSE);

        exporter.exportReport();
        exporter.reset();
    } catch (Exception e) {
        throw new ServiceException(
                "Невозможно экспортировать отчет в XLSX", e) as Throwable
    }
}

/**
 * Формирует спец. отчеты, данные для которых макет извлекает непосредственно из бд
 */
def createSpecificReportDb() {
    def params = [declarationId: declarationData.id]
    def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, null);
    exportXLSX(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
}

/**
 * Формат имени файла: РНУ_НДФЛ_<ИД формы>_<ФамилияИО>_<ДУЛ>_<ДатаВремя выгрузки>, где
 * <ИД формы> - ID формы из БД
 * <ФамилияИО> - Фамилия ФЛ полностью + первая буква имени + первая буква отчества (при наличии). Пример: ИвановаИИ
 * <ДУЛ> - Серия и номер документа, удостоверяющего личность в формате "Серия№Номер", Серия и Номер ДУЛ не должны содержать разделителей. Пример: 8888№123321
 * <ДатаВремя выгрузки> - дата и время выгрузки в формате ГГГГММДД_ЧЧММ. Пример: 20160216_1842
 * @return
 */
def createFileName(NdflPerson ndflPerson) {
    StringBuilder sb = new StringBuilder();
    sb.append("РНУ_НДФЛ_");
    sb.append(declarationData.id).append("_");
    sb.append(capitalize(ndflPerson.lastName));
    sb.append(firstChar(ndflPerson.firstName));
    sb.append(firstChar(ndflPerson.middleName)).append("_");
    sb.append(ndflPerson.idDocNumber?.replaceAll("\\s", "")?.toLowerCase()).append("_");
    sb.append(new SimpleDateFormat("yyyy.MM.dd_HHmm").format(new Date()));
    return sb.toString();
}


String firstChar(String str) {
    if (str != null && !str.isEmpty()) {
        return String.valueOf(Character.toUpperCase(str.charAt(0)));
    } else {
        return "";
    }
}

String capitalize(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
        return str;
    }
    return new StringBuilder(strLen)
            .append(Character.toTitleCase(str.charAt(0)))
            .append(str.substring(1).toLowerCase())
            .toString();
}

def formatDate(date) {
    return ScriptUtils.formatDate(date, DATE_FORMAT)
}


//Далее и до конца файла идет часть проверок общая для первичной и консолидированно,
//если проверки различаются то используется параметр {@link #FORM_DATA_KIND}
//При внесении изменений учитывается что эта чать скрипта используется(копируется) и в первичной и в консолидированной

//>------------------< REF BOOK >----------------------<

// Дата начала отчетного периода
@Field def periodStartDate = null

// Дата окончания отчетного периода
@Field def periodEndDate = null

// Кэш провайдеров cправочников
@Field Map<Long, RefBookDataProvider> providerCache = [:]
//Физлица
@Field final long REF_BOOK_PERSON_ID = RefBook.Id.PERSON.id
@Field Map<Long, Map<String, RefBookValue>> personsCache = [:]
@Field Map<Long, Map<String, RefBookValue>> personsActualCache = [:]

//Коды Асну
@Field Map<Long, String> asnuCache = [:]

//Коды стран из справочника
@Field Map<Long, String> countryCodeCache = [:]

//Виды документов, удостоверяющих личность
@Field Map<Long, Map<String, RefBookValue>> documentTypeCache = [:]
@Field Map<Long, String> documentTypeCodeCache = [:]

//Коды статуса налогоплательщика
@Field Map<Long, String> taxpayerStatusCodeCache = [:]

//Адреса физлиц
@Field Map<Long, Map<String, RefBookValue>> addressCache = [:]
//<person_id:  list<id: <record>>>
@Field Map<Long, Map<Long, Map<String, RefBookValue>>> dulCache = [:]
@Field Map<Long, Map<Long, Map<String, RefBookValue>>> dulActualCache = [:]

@Field def sourceReportPeriod = null

@Field Map<Long, DepartmentReportPeriod> departmentReportPeriodMap = [:]

@Field Map<Long, DeclarationTemplate> declarationTemplateMap = [:]

@Field Map<Long, String> departmentFullNameMap = [:]

// Мапа <ID_Данные о физическом лице - получателе дохода, Физическое лицо: <ФИО> ИНП:<ИНП>>
@Field def ndflPersonFLMap = [:]
@Field final TEMPLATE_PERSON_FL = "%s, ИНП: %s"
@Field final SECTION_LINE_MSG = "Раздел %s. Строка %s"

@CompileStatic
class NdflPersonFL {
    String fio
    String inp

    NdflPersonFL(String fio, String inp) {
        this.fio = fio
        this.inp = inp
    }
}

// Страны Мапа <Идентификатор, Код>

// Виды документов, удостоверяющих личность Мапа <Идентификатор, Код>
@Field def documentCodesCache = [:]
@Field final long REF_BOOK_DOCUMENT_ID = RefBook.Id.DOCUMENT_CODES.id

// Статус налогоплательщика Мапа <Идентификатор, Код>
@Field def taxpayerStatusCache = [:]
@Field final long REF_BOOK_TAXPAYER_STATUS_ID = RefBook.Id.TAXPAYER_STATUS.id

@Field final long REF_BOOK_ADDRESS_ID = RefBook.Id.PERSON_ADDRESS.id

// Тербанки Мапа <id, наименование>
@Field def terBankCache = [:]
@Field final long REF_DEPARTMENT_ID = RefBook.Id.DEPARTMENT.id

// ИНП Мапа <person_id, массив_инп>
@Field Map<Long, List<Map<String, RefBookValue>>> inpActualCache = [:]
@Field Map<Long, List<Map<String, RefBookValue>>> inpCache = [:]
@Field final long REF_BOOK_ID_TAX_PAYER_ID = RefBook.Id.ID_TAX_PAYER.id

@Field final long REF_BOOK_ID_DOC_ID = RefBook.Id.ID_DOC.id

// Коды видов доходов Мапа <Идентификатор, Код>
@Field final long REF_BOOK_INCOME_CODE_ID = RefBook.Id.INCOME_CODE.id

// Виды дохода Мапа <Признак, Идентификатор_кода_вида_дохода>
@Field final long REF_BOOK_INCOME_KIND_ID = RefBook.Id.INCOME_KIND.id

// Ставки
@Field def rateCache = []
@Field final long REF_BOOK_RATE_ID = RefBook.Id.NDFL_RATE.id

// Коды видов вычетов
@Field def deductionTypeCache = []
@Field final long REF_BOOK_DEDUCTION_TYPE_ID = RefBook.Id.DEDUCTION_TYPE.id

// Коды налоговых органов
@Field def taxInspectionCache = []
@Field final long REF_TAX_INSPECTION_ID = RefBook.Id.TAX_INSPECTION.id

// Дата начала отчетного периода
@Field def reportPeriodStartDate = null

// Дата окончания отчетного периода
@Field def reportPeriodEndDate = null

/**
 * Получить "АСНУ"
 * @return
 */
def getRefAsnu() {
    if (asnuCache.size() == 0) {
        def refBookMap = getRefBook(RefBook.Id.ASNU.id)
        refBookMap.each { refBook ->
            asnuCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return asnuCache;
}


def getDeclarationTemplateById(def id) {
    if (id != null && declarationTemplateMap[id] == null) {
        declarationTemplateMap[id] = declarationService.getTemplate(id)
    }
    return declarationTemplateMap[id]
}

def getReportPeriod() {
    if (sourceReportPeriod == null) {
        sourceReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    }
    return sourceReportPeriod
}


def getDepartmentReportPeriodById(def id) {
    if (id != null && departmentReportPeriodMap[id] == null) {
        departmentReportPeriodMap[id] = departmentReportPeriodService.get(id)
    }
    return departmentReportPeriodMap[id]
}

/** Получить полное название подразделения по id подразделения. */
def getDepartmentFullName(def id) {
    if (departmentFullNameMap[id] == null) {
        departmentFullNameMap[id] = departmentService.getParentsHierarchy(id)
    }
    return departmentFullNameMap[id]
}

/**
 * Получить дату начала отчетного периода
 * @return
 */
Date getReportPeriodStartDate() {
    if (reportPeriodStartDate == null) {
        reportPeriodStartDate = reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodStartDate
}

/**
 * Получить дату окончания отчетного периода
 * @return
 */
Date getReportPeriodEndDate() {
    if (periodEndDate == null) {
        periodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return periodEndDate
}

/**
 *
 * @return
 */
def getRefPersons() {
    if (personsCache.size() == 0) {
        throw new ServiceException("Не проинициализированны данные кэша справочника 'Физических лиц'!")
    }
    return personsCache;
}

/**
 * Получить актуальные на отчетную дату записи справочника "Физические лица"
 * @return Map < person_id , Map < имя_поля , значение_поля > >
 */
Map<Long, Map<String, RefBookValue>> getActualRefPersonsByDeclarationDataId(declarationDataId) {
    String whereClause = """
            JOIN ref_book_person p ON (frb.record_id = p.record_id)
            JOIN ndfl_person np ON (np.declaration_data_id = ${declarationDataId} AND p.id = np.person_id)
        """
    def refBookMap = getRefBookByRecordVersionWhere(REF_BOOK_PERSON_ID, whereClause, getReportPeriodEndDate())
    def refBookMapResult = new HashMap<Long, Map<String, RefBookValue>>();
    refBookMap.each { personId, refBookValue ->
        Long refBookRecordId = refBookValue.get(RF_RECORD_ID).value
        refBookMapResult.put(refBookRecordId, refBookValue)
    }
    return refBookMapResult
}

/**
 * Получить "Страны"
 * @return
 */
def getRefCountryCode() {
    if (countryCodeCache.size() == 0) {
        def refBookMap = getRefBookAll(RefBook.Id.COUNTRY.getId())
        refBookMap.each { refBook ->
            countryCodeCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return countryCodeCache;
}

/**
 * Получить "Виды документов"
 */
def getRefDocumentType() {
    if (documentTypeCache.size() == 0) {
        def refBookList = getRefBookAll(RefBook.Id.DOCUMENT_CODES.getId())
        refBookList.each { refBook ->
            documentTypeCache.put(refBook?.id?.numberValue, refBook)
        }
    }
    return documentTypeCache;
}

def getRefDocumentTypeCode() {
    if (documentTypeCodeCache.size() == 0) {
        def refBookList = getRefDocumentType()
        refBookList.each { id, refBookValueMap ->
            documentTypeCodeCache.put(id, refBookValueMap?.get("CODE")?.getStringValue())
        }
    }
    return documentTypeCodeCache;
}

/**
 * Получить "ИНП"
 */
Map<Long, Map<String, RefBookValue>> getActualRefInpMapByDeclarationDataId() {
    if (inpActualCache.isEmpty()) {
        String whereClause = "exists (select 1 from ndfl_person np where np.declaration_data_id = ${declarationData.id} AND ref_book_id_tax_payer.person_id = np.person_id)"
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordWhere(REF_BOOK_ID_TAX_PAYER_ID, whereClause)

        refBookMap.each { id, refBook ->
            def inpList = inpActualCache.get(refBook?.PERSON_ID?.referenceValue)
            if (inpList == null) {
                inpList = []
            }
            inpList.add(refBook?.INP?.stringValue)
            inpActualCache.put(refBook?.PERSON_ID?.referenceValue, inpList)
        }
    }
    return inpActualCache
}

/**
 * Получить "Статусы налогоплательщика"
 * @return
 */
def getRefTaxpayerStatusCode() {
    if (taxpayerStatusCodeCache.size() == 0) {
        def refBookMap = getRefBookAll(RefBook.Id.TAXPAYER_STATUS.getId())
        refBookMap.each { refBook ->
            taxpayerStatusCodeCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return taxpayerStatusCodeCache;
}

/**
 * Получить "Коды видов доходов"
 * @return
 */
def getRefIncomeCode() {
    // Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
    def mapResult = [:]
    def refBookMap = getRefBook(REF_BOOK_INCOME_CODE_ID)
    refBookMap.each { refBook ->
        mapResult.put(refBook?.id?.numberValue, refBook)
    }
    return mapResult;
}

/**
 * Получить "Виды доходов"
 * @return
 */
def getRefIncomeType() {
    // Map<REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>
    Map<String, List<Long>> mapResult = [:]
    def refBookList = getRefBook(REF_BOOK_INCOME_KIND_ID)
    refBookList.each { refBook ->
        String mark = refBook?.MARK?.stringValue
        List<Long> incomeTypeIdList = mapResult.get(mark)
        if (incomeTypeIdList == null) {
            incomeTypeIdList = []
        }
        incomeTypeIdList.add(refBook?.INCOME_TYPE_ID?.referenceValue)
        mapResult.put(mark, incomeTypeIdList)
    }
    return mapResult
}

/**
 * Получить "Ставки"
 * @return
 */
def getRefRate() {
    if (rateCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_RATE_ID)
        refBookList.each { refBook ->
            rateCache.add(refBook?.RATE?.stringValue)
        }
    }
    return rateCache;
}

/**
 * Получить "Коды видов вычетов"
 * @return
 */
def getRefDeductionType() {
    if (deductionTypeCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_DEDUCTION_TYPE_ID)
        refBookList.each { refBook ->
            deductionTypeCache.add(refBook?.CODE?.stringValue)
        }
    }
    return deductionTypeCache;
}

/**
 * Получить "Коды налоговых органов"
 * @return
 */
def getRefNotifSource() {
    if (taxInspectionCache.size() == 0) {
        def refBookList = getRefBook(REF_TAX_INSPECTION_ID)
        refBookList.each { refBook ->
            taxInspectionCache.add(refBook?.CODE?.stringValue)
        }
    }
    return taxInspectionCache;
}

/**
 * Получить "Адреса налогоплательщика"
 * @return
 */
def getRefAddress(def addressIds) {
    if (addressCache.size() == 0) {
        def refBookMap = getRefBookByRecordIds(RefBook.Id.PERSON_ADDRESS.getId(), addressIds)
        refBookMap.each { addressId, address ->
            addressCache.put(addressId, address)
        }
    }
    return addressCache;
}

/**
 * Получить "Документ, удостоверяющий личность (ДУЛ)"
 */
Map<Long, Map<String, RefBookValue>> getActualRefDulByDeclarationDataId() {
    if (dulActualCache.isEmpty()) {

        def declarationDataId = declarationData.id

        String whereClause = """
            JOIN ref_book_person p ON (frb.person_id = p.id)
            JOIN ndfl_person np ON (np.declaration_data_id = ${declarationDataId} AND p.id = np.person_id)
        """
        Map<Long, Map<String, RefBookValue>> refBookMap = getRefBookByRecordVersionWhere(REF_BOOK_ID_DOC_ID, whereClause, getReportPeriodEndDate() - 1)

        refBookMap.each { personId, refBookValues ->
            Long refBookPersonId = refBookValues.get("PERSON_ID").getReferenceValue();
            def dulList = dulActualCache.get(refBookPersonId);
            if (dulList == null) {
                dulList = [];
            }
            dulList.add(refBookValues);
            dulActualCache.put(refBookPersonId, dulList)
        }
    }
    return dulActualCache
}

/**
 * Получить записи справочника по его идентификатору в отчётном периоде
 * @param refBookId - идентификатор справочника
 * @return - список записей справочника
 */
def getRefBook(def long refBookId) {
    // Передаем как аргумент только срок действия версии справочника
    def refBookList = getProvider(refBookId).getRecordsVersion(getReportPeriodStartDate(), getReportPeriodEndDate(), null, null)
    if (refBookList == null || refBookList.size() == 0) {
        throw new Exception("Ошибка при получении записей справочника " + refBookId)
    }
    return refBookList
}

/**
 * Получить все записи справочника по его идентификатору
 * @param refBookId - идентификатор справочника
 * @return - список всех версий всех записей справочника
 */
def getRefBookAll(long refBookId) {
    def recordData = getProvider(refBookId).getRecordDataWhere("1 = 1")
    def refBookList = []
    if (recordData != null) {
        recordData.each { key, value ->
            refBookList.add(value)
        }
    }

    if (refBookList.size() == 0) {
        throw new Exception("Ошибка при получении записей справочника " + refBookId)
    }
    return refBookList
}

/**
 * Выгрузка из справочников по условию и версии
 * @param refBookId
 * @param whereClause
 * @return
 * Поскольку поиск осуществляется с использованием оператора EXISTS необходимодимо всегда связывать поле подзапроса через ALIAS frb
 */
def getRefBookByRecordVersionWhere(def long refBookId, def whereClause, def version) {
    Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordDataVersionWhere(whereClause, version)
    if (refBookMap == null || refBookMap.size() == 0) {
        //throw new ScriptException("Не найдены записи справочника " + refBookId)
        return Collections.emptyMap();
    }
    return refBookMap
}

/**
 * Выгрузка из справочников по условию
 * @param refBookId
 * @param whereClause
 * @return
 * Поскольку поиск осуществляется с использованием оператора EXISTS необходимодимо всегда связывать поле подзапроса через ALIAS frb
 */
def getRefBookByRecordWhere(def long refBookId, def whereClause) {
    Map<Long, Map<String, RefBookValue>> refBookMap = getProvider(refBookId).getRecordDataWhere(whereClause)
    if (refBookMap == null || refBookMap.size() == 0) {
        //throw new ScriptException("Не найдены записи справочника " + refBookId)
        return Collections.emptyMap();
    }
    return refBookMap
}

/**
 * Получить все записи справочника по его идентификатору и коллекции идентификаторов записей справочника
 * @param refBookId - идентификатор справочника
 * @param recordIds - коллекция идентификаторов записей справочника
 * @return - возвращает мапу
 */
def getRefBookByRecordIds(def long refBookId, def recordIds) {
    Map<Long, Map<String, RefBookValue>> refBookMap = [:]
    recordIds.collate(1000).each {
        if (it.size() != 0) {
            refBookMap.putAll(getProvider(refBookId).getRecordData(it))
        }
    }
    if (refBookMap == null || refBookMap.size() == 0) {
        throw new ScriptException("Ошибка при получении записей справочника " + refBookId)
    }
    return refBookMap
}

/**
 * Получение провайдера с использованием кеширования.
 * @param providerId
 * @return
 */
RefBookDataProvider getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

//>------------------< UTILS >----------------------<

// Параметры для подразделения Мапа <ОКТМО, Лист_КПП>
@Field final long REF_BOOK_NDFL_ID = RefBook.Id.NDFL.id
@Field final long REF_BOOK_NDFL_DETAIL_ID = RefBook.Id.NDFL_DETAIL.id

@Field final String MESSAGE_ERROR_DUBL_OR_ABSENT = "В ТФ имеются пропуски или повторы в нумерации строк."
@Field final String MESSAGE_ERROR_DUBL = " Повторяются строки:"
@Field final String MESSAGE_ERROR_ABSENT = " Отсутствуют строки:"


@Field final String SUCCESS_GET_REF_BOOK = "Получен справочник \"%s\" (%d записей)."
@Field final String SUCCESS_GET_TABLE = "Получены записи таблицы \"%s\" (%d записей)."

// Таблицы
@Field final String T_PERSON = "1" //"Реквизиты"
@Field final String T_PERSON_INCOME = "2" // "Сведения о доходах и НДФЛ"
@Field final String T_PERSON_DEDUCTION = "3" // "Сведения о вычетах"
@Field final String T_PERSON_PREPAYMENT = "4" //"Сведения о доходах в виде авансовых платежей"

@Field final String T_PERSON_NAME = "Реквизиты"
@Field final String T_PERSON_INCOME_NAME  = "Сведения о доходах и НДФЛ"
@Field final String T_PERSON_DEDUCTION_NAME  =  "Сведения о вычетах"
@Field final String T_PERSON_PREPAYMENT_NAME  = "Сведения о доходах в виде авансовых платежей"

// Справочники
@Field final String R_FIAS = "КЛАДР"
@Field final String R_PERSON = "Физические лица"
@Field final String R_CITIZENSHIP = "ОК 025-2001 (Общероссийский классификатор стран мира)"
@Field final String R_ID_DOC_TYPE = "Коды документов"
@Field final String R_STATUS = "Статусы налогоплательщика"
@Field final String R_INCOME_CODE = "Коды видов доходов"
@Field final String R_INCOME_TYPE = "Виды дохода"
@Field final String R_RATE = "Ставки"
@Field final String R_TYPE_CODE = "Коды видов вычетов"
@Field final String R_NOTIF_SOURCE = "Налоговые инспекции"
@Field final String R_ADDRESS = "Адреса"
@Field final String R_INP = "Идентификаторы налогоплательщиков"
@Field final String R_DUL = "Документы, удостоверяющие личность"
@Field final String R_DETAIL = "Настройки подразделений"

// Реквизиты
@Field final String C_ADDRESS = "Адрес регистрации в Российской Федерации "
@Field final String C_CITIZENSHIP = "Гражданство (код страны)"
@Field final String C_ID_DOC = "Документ удостоверяющий личность.Номер"
@Field final String C_ID_DOC_TYPE = "Документ удостоверяющий личность.Код"
@Field final String C_STATUS = "Статус (код)"
@Field final String C_RATE = "Ставка"
@Field final String C_TYPE_CODE = "Код вычета" // "Форма.Сведения о вычетах.Код вычета"
@Field final String C_NOTIF_SOURCE = "Подтверждающий документ. Код источника" //"Документ о праве на налоговый вычет.Код источника"
@Field final String C_LAST_NAME = "Налогоплательщик.Фамилия"
@Field final String C_FIRST_NAME = "Налогоплательщик.Имя"
@Field final String C_MIDDLE_NAME = "Налогоплательщик.Отчество"
@Field final String C_BIRTH_DATE = "Налогоплательщик.Дата рождения"
@Field final String C_INN_NP = "ИНН.В Российской федерации"
@Field final String C_SNILS = "СНИЛС"
@Field final String C_INN_FOREIGN = "ИНН.В стране гражданства"
@Field final String C_REGION_CODE = "Адрес регистрации в Российской Федерации.Код субъекта"
@Field final String C_AREA = "Адрес регистрации в Российской Федерации.Район"
@Field final String C_CITY = "Адрес регистрации в Российской Федерации.Город"
@Field final String C_LOCALITY = "Адрес регистрации в Российской Федерации.Населенный пункт"
@Field final String C_STREET = "Адрес регистрации в Российской Федерации.Улица"
@Field final String C_HOUSE = "Адрес регистрации в Российской Федерации.Дом"
@Field final String C_BUILDING = "Адрес регистрации в Российской Федерации.Корпус"
@Field final String C_FLAT = "Адрес регистрации в Российской Федерации.Квартира"

// Сведения о доходах и НДФЛ
@Field final String C_INCOME_CODE = "Код дохода" //"Доход.Вид.Код"
@Field final String C_INCOME_TYPE = "Признак дохода" //"Доход.Вид.Признак"
@Field final String C_INCOME_ACCRUED_DATE = "Дата начисления дохода" //"Доход.Дата.Начисление"
@Field final String C_INCOME_PAYOUT_DATE = "Дата выплаты дохода" //"Доход.Дата.Выплата"
@Field final String C_INCOME_ACCRUED_SUMM = "Сумма начисленного дохода" //" Доход.Сумма.Начисление"
@Field final String C_INCOME_PAYOUT_SUMM = "Сумма выплаченного дохода" //"Доход.Сумма.Выплата"
@Field final String C_TOTAL_DEDUCTIONS_SUMM = "Сумма вычета" //"Сумма вычета"
@Field final String C_TAX_BASE = "Налоговая база" //"Налоговая база"
@Field final String C_TAX_RATE = "Процентная ставка (%)" //"НДФЛ.Процентная ставка"
@Field final String C_TAX_DATE = "Дата НДФЛ" //"НДФЛ.Расчет.Дата"
@Field final String C_CALCULATED_TAX = "НДФЛ исчисленный" //"НДФЛ.Расчет.Сумма.Исчисленный"
@Field final String C_WITHHOLDING_TAX = "НДФЛ удержанный" //"НДФЛ.Расчет.Сумма.Удержанный"
@Field final String C_NOT_HOLDING_TAX = "НДФЛ не удержанный" //"НДФЛ.Расчет.Сумма.Не удержанный"
@Field final String C_OVERHOLDING_TAX = "НДФЛ излишне удержанный" //"НДФЛ.Расчет.Сумма.Излишне удержанный"
@Field final String C_REFOUND_TAX = "НДФЛ возвращенный НП" //"НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику"
@Field final String C_TAX_TRANSFER_DATE = "Срок перечисления в бюджет" //"НДФЛ.Перечисление в бюджет.Срок"
@Field final String C_PAYMENT_DATE = "Дата платежного поручения" //"НДФЛ.Перечисление в бюджет.Платежное поручение.Дата"
@Field final String C_PAYMENT_NUMBER = "Номер платежного поручения" //"НДФЛ.Перечисление в бюджет.Платежное поручение.Номер"
@Field final String C_TAX_SUMM = "Сумма платежного поручения" //"НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма"
@Field final String C_OKTMO = "ОКТМО" //"Доход.Источник выплаты.ОКТМО"
@Field final String C_KPP = "КПП" //"Доход.Источник выплаты.КПП"

// Сведения о вычетах
@Field final String C_NOTIF_DATE = "Подтверждающий документ. Дата" //"Документ о праве на налоговый вычет.Дата"
@Field final String C_NOTIF_SUMM = "Подтверждающий документ. Сумма" //"Документ о праве на налоговый вычет.Сумма"
@Field final String C_NOTIF_NUMBER = "Подтверждающий документ. Номер" //" Документ о праве на налоговый вычет.Номер"
@Field final String C_INCOME_ACCRUED = "Доход. Дата" //"Начисленный доход.Дата"
@Field final String C_INCOME_ACCRUED_P_SUMM = "Доход. Сумма" //"Начисленный доход.Сумма"
@Field final String C_INCOME_ACCRUED_CODE = "Доход. Код дохода" //" Начисленный доход.Код дохода"
@Field final String C_PERIOD_PREV_DATE = "Вычет. Предыдущий период. Дата" //"Применение вычета.Предыдущий период.Дата"
@Field final String C_PERIOD_CURR_DATE = "Вычет. Текущий период. Дата" //"Применение вычета.Текущий период.Дата"
@Field final String C_PERIOD_CURR_SUMM = "Вычет. Текущий период. Сумма" //" Применение вычета.Текущий период.Сумма"

// Сведения о доходах в виде авансовых платежей
@Field final String P_NOTIF_SOURCE = "Код налогового органа, выдавшего уведомление" //"Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление"

// Поля справочника Физические лица
@Field final String RF_LAST_NAME = "LAST_NAME"
@Field final String RF_FIRST_NAME = "FIRST_NAME"
@Field final String RF_MIDDLE_NAME = "MIDDLE_NAME"
@Field final String RF_BIRTH_DATE = "BIRTH_DATE"
@Field final String RF_CITIZENSHIP = "CITIZENSHIP"
@Field final String RF_INN = "INN"
@Field final String RF_INN_FOREIGN = "INN_FOREIGN"
@Field final String RF_SNILS = "SNILS"
@Field final String RF_TAXPAYER_STATE = "TAXPAYER_STATE"
@Field final String RF_ADDRESS = "ADDRESS"
@Field final String RF_RECORD_ID = "RECORD_ID"

//Адрес
@Field final String RF_REGION_CODE = "REGION_CODE"
@Field final String RF_DISTRICT = "DISTRICT"
@Field final String RF_CITY = "CITY"
@Field final String RF_LOCALITY = "LOCALITY"
@Field final String RF_STREET = "STREET"
@Field final String RF_HOUSE = "HOUSE"
@Field final String RF_BUILD = "BUILD"
@Field final String RF_APPARTMENT = "APPARTMENT"

//ДУЛ
@Field final String RF_DOC_ID = "DOC_ID"
@Field final String RF_DOC_NUMBER = "DOC_NUMBER"
@Field final String RF_INC_REP = "INC_REP"

//>------------------< CHECK DATA >----------------------<

@Field final String LOG_TYPE_REFERENCES = "Значение не соответствует справочнику \"%s\""
@Field final String LOG_TYPE_PERSON_MSG = "Значение гр. \"%s\" (\"%s\") не соответствует справочнику \"%s\""
@Field final String LOG_TYPE_PERSON_MSG_2 = "Значение гр. \"%s\" (\"%s\") отсутствует в справочнике \"%s\""

@Field final String LOG_TYPE_2_6 = "Дата начисления дохода указана некорректно"
@Field final String LOG_TYPE_2_12 = "Сумма вычета указана некорректно"
@Field final String LOG_TYPE_2_14 = "\"Налоговая ставка\" указана некорректно"
@Field final String LOG_TYPE_2_14_MSG = "Значение гр. \"%s\" (\"%s\") указано некорректно. Для \"Кода дохода\" (\"%s\") и \"Статуса НП\" (\"%s\") предусмотрены ставки: %s"
@Field final String LOG_TYPE_2_16 = "\"НДФЛ исчисленный\" рассчитан некорректно"
@Field final String LOG_TYPE_2_17 = "\"НДФЛ удержанный\" рассчитан некорректно"
@Field final String LOG_TYPE_2_18 = "\"НДФЛ не удержанный\" рассчитан некорректно"
@Field final String LOG_TYPE_2_19 = "\"НДФЛ излишне удержанный\" рассчитан некорректно"
@Field final String LOG_TYPE_2_20 = "\"НДФЛ возвращеный НП\" рассчитан некорректно"
@Field final String LOG_TYPE_2_21 = "\"Срок перечисления в бюджет\" рассчитан некорректно"
@Field final String LOG_TYPE_NOT_ZERO = "Значение не может быть \"0\""

@Field final String LOG_TYPE_3_7 = "\"Код источника подтверждающего документа\" указан некорректно"
@Field final String LOG_TYPE_3_10 = "\"Дата начисленного дохода\" указана некорректно"
@Field final String LOG_TYPE_3_10_2 = "\"Дата применения вычета в текущем периоде\" не соответствует \"Дате начисления дохода\""
@Field final String LOG_TYPE_3_11= "\"Код начисленного дохода\" указан некорректно"
@Field final String LOG_TYPE_3_12 = "\"Сумма начисленного дохода\" указана некорректно"
@Field final String LOG_TYPE_3_16 = "\"Сумма применения вычета\" указана некорректно"


/**
 * Проверки НДФЛ (первичная и консолидированная)
 * @return
 */
def checkData() {

    ScriptUtils.checkInterrupted();

    long time = System.currentTimeMillis();
    // Реквизиты
    List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
    logForDebug(SUCCESS_GET_TABLE, T_PERSON_NAME, ndflPersonList.size())

    // Сведения о доходах и НДФЛ
    List<NdflPersonIncome> ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
    logForDebug(SUCCESS_GET_TABLE, T_PERSON_INCOME_NAME, ndflPersonIncomeList.size())

    // Сведения о вычетах
    List<NdflPersonDeduction> ndflPersonDeductionList = ndflPersonService.findNdflPersonDeduction(declarationData.id)
    logForDebug(SUCCESS_GET_TABLE, T_PERSON_DEDUCTION_NAME, ndflPersonDeductionList.size())

    // Сведения о доходах в виде авансовых платежей
    List<NdflPersonPrepayment> ndflPersonPrepaymentList = ndflPersonService.findNdflPersonPrepayment(declarationData.id)
    logForDebug(SUCCESS_GET_TABLE, T_PERSON_PREPAYMENT_NAME, ndflPersonPrepaymentList.size())

    logForDebug("Получение записей из таблиц НФДЛ (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();
    // ФЛ Map<person_id, RefBook>
    Map<Long, Map<String, RefBookValue>> personMap = getActualRefPersonsByDeclarationDataId(declarationData.id)
    logForDebug(SUCCESS_GET_TABLE, R_PERSON, personMap.size())

    logForDebug("Проверки на соответствие справочникам / Выгрузка справочника Физические лица (" + (System.currentTimeMillis() - time) + " мс)");

    ScriptUtils.checkInterrupted();

    // Проверки на соответствие справочникам
    checkDataReference(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList, personMap)

    ScriptUtils.checkInterrupted();

    // Общие проверки
    checkDataCommon(ndflPersonList, ndflPersonIncomeList, personMap)

    ScriptUtils.checkInterrupted();

    // Проверки сведений о доходах
    checkDataIncome(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, ndflPersonPrepaymentList, personMap)

    ScriptUtils.checkInterrupted();

    // Проверки Сведения о вычетах
    checkDataDeduction(ndflPersonList, ndflPersonIncomeList, ndflPersonDeductionList, personMap)

    logForDebug("Все проверки (" + (System.currentTimeMillis() - time) + " мс)");
}

/**
 * Проверки наличия данных при принятии
 * @return
 */
def checkAccept() {
    List<NdflPerson> ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
    if (ndflPersonList.isEmpty()) {
        logger.error("Консолидированная форма не содержит данных, принятие формы невозможно")
    }
}

/**
 * Проверки на соответствие справочникам
 * @return
 */
def checkDataReference(
        List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList,
        List<NdflPersonPrepayment> ndflPersonPrepaymentList, Map<Long, Map<String, RefBookValue>> personMap) {

    long time = System.currentTimeMillis();
    // Страны
    def citizenshipCodeMap = getRefCountryCode()
    logForDebug(SUCCESS_GET_REF_BOOK, R_CITIZENSHIP, citizenshipCodeMap.size())

    // Виды документов, удостоверяющих личность
    def documentTypeMap = getRefDocumentTypeCode()
    logForDebug(SUCCESS_GET_REF_BOOK, R_ID_DOC_TYPE, documentTypeMap.size())

    // Статус налогоплательщика
    def taxpayerStatusMap = getRefTaxpayerStatusCode()
    logForDebug(SUCCESS_GET_REF_BOOK, R_STATUS, taxpayerStatusMap.size())

    // Коды видов доходов Map<REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
    def incomeCodeMap = getRefIncomeCode()
    logForDebug(SUCCESS_GET_REF_BOOK, R_INCOME_CODE, incomeCodeMap.size())

    // Виды доходов Map<REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>
    def incomeTypeMap = getRefIncomeType()
    logForDebug(SUCCESS_GET_REF_BOOK, R_INCOME_TYPE, incomeTypeMap.size())

    // Коды видов вычетов
    def deductionTypeList = getRefDeductionType()
    logForDebug(SUCCESS_GET_REF_BOOK, R_TYPE_CODE, deductionTypeList.size())

    // Коды налоговых органов
    def taxInspectionList = getRefNotifSource()
    logForDebug(SUCCESS_GET_REF_BOOK, R_NOTIF_SOURCE, taxInspectionList.size())

    logForDebug("Проверки на соответствие справочникам / Выгрузка справочников (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();
    // ИНП Map<person_id, List<RefBook>>
    def inpMap = getActualRefInpMapByDeclarationDataId()
    logForDebug(SUCCESS_GET_TABLE, R_INP, inpMap.size())
    logForDebug("Проверки на соответствие справочникам / Выгрузка справочника ИНП (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();
    // ДУЛ Map<person_id, List<RefBook>>
    def dulMap = getActualRefDulByDeclarationDataId()
    logForDebug(SUCCESS_GET_TABLE, R_DUL, dulMap.size())
    logForDebug("Проверки на соответствие справочникам / Выгрузка справочника ДУЛ (" + (System.currentTimeMillis() - time) + " мс)");

    // Получим Мапу адресов
    // Адреса
    def addressIds = []
    def addressMap = [:]
    time = System.currentTimeMillis();
    personMap.each { recordId, person ->
        // Сохраним идентификаторы адресов в коллекцию
        if (person.get(RF_ADDRESS).value != null) {
            addressIds.add(person.get(RF_ADDRESS).value)
        }
    }
    if (addressIds.size() > 0) {
        addressMap = getRefAddress(addressIds)
        logForDebug(SUCCESS_GET_TABLE, R_ADDRESS, addressMap.size())
    }
    logForDebug("Проверки на соответствие справочникам / Выгрузка справочника Адреса (" + (System.currentTimeMillis() - time) + " мс)");

    //поиск всех адресов формы в справочнике ФИАС
    time = System.currentTimeMillis();
    Map<Long, FiasCheckInfo> checkFiasAddressMap = getFiasAddressIdsMap();
    logForDebug(SUCCESS_GET_TABLE, R_FIAS, checkFiasAddressMap.size());
    logForDebug("Проверки на соответствие справочникам / Выгрузка справочника $R_FIAS (" + (System.currentTimeMillis() - time) + " мс)");

    long timeIsExistsAddress = 0
    time = System.currentTimeMillis();
    //в таком цикле не отображается номер строки при ошибках ndflPersonList.each { ndflPerson ->}
    for (NdflPerson ndflPerson : ndflPersonList) {

        ScriptUtils.checkInterrupted();

        NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
        if (ndflPersonFL == null) {
            // РНУ-НДФЛ консолидированная
            def personRecord = personMap.get(ndflPerson.recordId)
            if (personRecord == null) {
                // РНУ-НДФЛ первичная
                String fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + (ndflPerson.middleName ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp)
            } else {
                String fio = (personRecord.get(RF_LAST_NAME).value?:"") + " " + (personRecord.get(RF_FIRST_NAME).value?:"") + " " + (personRecord.get(RF_MIDDLE_NAME).value ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
            }
            ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
        }
        String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

        // Спр1 ФИАС
        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-448
        long tIsExistsAddress = System.currentTimeMillis();
        if (!isPersonAddressEmpty(ndflPerson)) {

            List<String> address = []
            FiasCheckInfo fiasCheckInfo = checkFiasAddressMap.get(ndflPerson.id)
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
            if (!ScriptUtils.isEmpty(ndflPerson.regionCode) && !fiasCheckInfo.validRegion) {
                logFiasError(fioAndInp, pathError, "Код субъекта", ndflPerson.regionCode)
            } else if (!ScriptUtils.isEmpty(ndflPerson.area) && !fiasCheckInfo.validArea ) {
                logFiasError(fioAndInp, pathError, "Район", ndflPerson.area)
            } else if (!ScriptUtils.isEmpty(ndflPerson.city) && !fiasCheckInfo.validCity) {
                logFiasError(fioAndInp, pathError, "Город", ndflPerson.city)
            } else if (!ScriptUtils.isEmpty(ndflPerson.locality) && !fiasCheckInfo.validLoc) {
                logFiasError(fioAndInp, pathError, "Населенный пункт", ndflPerson.locality)
            } else if (!ScriptUtils.isEmpty(ndflPerson.street) && !fiasCheckInfo.validStreet) {
                logFiasError(fioAndInp, pathError, "Улица", ndflPerson.street)
            }
            if (!(ndflPerson.postIndex != null && ndflPerson.postIndex.matches("[0-9]{6}"))){
                logFiasIndexError(fioAndInp, pathError, "Индекс", ndflPerson.postIndex)
            }
        }
        timeIsExistsAddress += System.currentTimeMillis() - tIsExistsAddress

        // Спр2 Гражданство (Обязательное поле)
        if (ndflPerson.citizenship != null && !citizenshipCodeMap.find { key, value -> value == ndflPerson.citizenship }) {
            //TODO turn_to_error
            String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                    C_CITIZENSHIP, ndflPerson.citizenship ?: "",
                    R_CITIZENSHIP
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_CITIZENSHIP), fioAndInp, pathError, errMsg)
        }

        // Спр3 Документ удостоверяющий личность.Код (Обязательное поле)
        if (ndflPerson.idDocType != null && !documentTypeMap.find { key, value -> value == ndflPerson.idDocType }) {
            //TODO turn_to_error
            String errMsg = String.format(LOG_TYPE_PERSON_MSG_2, "ДУЛ Код", ndflPerson.idDocType ?: "", R_ID_DOC_TYPE)
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_ID_DOC_TYPE), fioAndInp, pathError, errMsg)
        }

        // Спр4 Статус (Обязательное поле)
        if (ndflPerson.status != "0" && !taxpayerStatusMap.find { key, value -> value == ndflPerson.status }) {
            //TODO turn_to_error
            String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                    C_STATUS, ndflPerson.status ?: "",
                    R_STATUS
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_STATUS), fioAndInp, pathError, errMsg)
        }

        // Спр10 Наличие связи с "Физическое лицо"
        if (ndflPerson.personId == null || ndflPerson.personId == 0) {
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
            logger.errorExp("%s. %s.", "Отсутствует связь со справочником \"Физические лица\"", fioAndInp, pathError,
                    "Не удалось установить связь со справочником \"$R_PERSON\"")
        } else {
            def personRecord = personMap.get(ndflPerson.recordId)

            if (!personRecord) {
                //TODO turn_to_error
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.errorExp("%s. %s.", "Отсутствует связь со справочником \"Физические лица\"", fioAndInp, pathError,
                        "Не удалось установить связь со справочником \"$R_PERSON\"")
            } else {
                // Спр11 Фамилия (Обязательное поле)
                if (ndflPerson.lastName != null && !ndflPerson.lastName.equals(personRecord.get(RF_LAST_NAME).value)) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "ФИО не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                            String.format(LOG_TYPE_PERSON_MSG, "Фамилия", ndflPerson.lastName ?: "", R_PERSON))
                }

                // Спр11 Имя (Обязательное поле)
                if (ndflPerson.firstName != null && !ndflPerson.firstName.equals(personRecord.get(RF_FIRST_NAME).value)) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "ФИО не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                            String.format(LOG_TYPE_PERSON_MSG, "Имя", ndflPerson.firstName ?: "", R_PERSON))
                }

                // Спр11 Отчество (Необязательное поле)
                if (ndflPerson.middleName != null && !ndflPerson.middleName.equals(personRecord.get(RF_MIDDLE_NAME).value)) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "ФИО не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                            String.format(LOG_TYPE_PERSON_MSG, "Отчество", ndflPerson.middleName ?: "", R_PERSON))
                }

                if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                    // Спр12 ИНП первичная (Обязательное поле)
                    def inpList = inpMap.get(personRecord.get("id")?.value)
                    if (!(ndflPerson.inp == personRecord.get(RF_SNILS)?.value || inpList?.contains(ndflPerson.inp))) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ИНП не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "ИНП", ndflPerson.inp ?: "", R_PERSON))
                    }
                } else {
                    //Спр12.1 ИНП консолидированная - проверка соответствия RECORD_ID
                    //if (formType == CONSOLIDATE){}
                    String recordId = String.valueOf(personRecord.get(RF_RECORD_ID).getNumberValue().longValue());
                    if (!ndflPerson.inp.equals(recordId)) {
                        //TODO turn_to_error
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "ИНП не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "ИНП", ndflPerson.inp ?: "", R_PERSON))
                    }
                }

                // Спр13 Дата рождения (Обязательное поле)
                if (ndflPerson.birthDay != null && !ndflPerson.birthDay.equals(personRecord.get(RF_BIRTH_DATE).value)) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "Дата рождения не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                            String.format(LOG_TYPE_PERSON_MSG, "Дата рождения", ndflPerson.birthDay ?(ndflPerson.birthDay?.format("dd.MM.yyyy")): "", R_PERSON))
                }

                // Спр14 Гражданство (Обязательное поле)
                def citizenship = citizenshipCodeMap.get(personRecord.get(RF_CITIZENSHIP).value)
                if (ndflPerson.citizenship != null && !ndflPerson.citizenship.equals(citizenship)) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "Код гражданства не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                            String.format(LOG_TYPE_PERSON_MSG, C_CITIZENSHIP, ndflPerson.citizenship ?: "", R_PERSON))
                }

                // Спр15 ИНН.В Российской федерации (Необязательное поле)
                if (ndflPerson.innNp != null && !ndflPerson.innNp.equals(personRecord.get(RF_INN).value)) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "ИНН в РФ не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                            String.format(LOG_TYPE_PERSON_MSG, "ИНН в РФ", ndflPerson.innNp ?: "", R_PERSON))
                }

                // Спр16 ИНН.В стране гражданства (Необязательное поле)
                if (ndflPerson.innForeign != null && !ndflPerson.innForeign.equals(personRecord.get(RF_INN_FOREIGN).value)) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "ИНН в ИНО не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                            String.format(LOG_TYPE_PERSON_MSG, "ИНН в ИНО", ndflPerson.innForeign ?: "", R_PERSON))
                }

                if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                    // Спр17 Документ удостоверяющий личность (Первичная) (Обязательное поле)
                    def allDocList = dulMap.get(personRecord.get("id")?.value)
                    // Вид документа
                    def personDocTypeList = []
                    // Серия и номер документа
                    def personDocNumberList = []
                    allDocList.each { dul ->
                        personDocTypeList.add(documentTypeMap.get(dul.get(RF_DOC_ID).value))
                        personDocNumberList.add(dul.get(RF_DOC_NUMBER).value)
                    }
                    if (ndflPerson.idDocType != null && !personDocTypeList.contains(ndflPerson.idDocType)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "ДУЛ Код", ndflPerson.idDocType ?: "", R_PERSON))
                    }
                    if (ndflPerson.idDocNumber != null && !personDocNumberList.contains(ndflPerson.idDocNumber)) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "ДУЛ Номер", ndflPerson.idDocNumber ?: "", R_PERSON))
                    }
                } else {
                    def allDocList = dulMap.get(ndflPerson.personId)
                    //Ищем в справочнике запись по параметрам код документа и номер
                    Map<String, RefBookValue> dulRecordValues = allDocList.find { recordValues ->
                        String docTypeCode = documentTypeMap.get(recordValues.get(RF_DOC_ID).getReferenceValue())
                        String docNumber = recordValues.get(RF_DOC_NUMBER).getStringValue()
                        return ndflPerson.idDocType.equals(docTypeCode) && ndflPerson.idDocNumber.equals(docNumber)
                    }

                    if (dulRecordValues == null) {
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                        logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют справочнику \"Физические лица\"", fioAndInp, pathError,
                                String.format(LOG_TYPE_PERSON_MSG, "ДУЛ Код\" (\"${ndflPerson.idDocType ?: ""}\"), \"ДУЛ Номер", ndflPerson.idDocNumber ?: "", R_PERSON))
                    } else {
                        int incRep = dulRecordValues.get(RF_INC_REP).getNumberValue().intValue()
                        if (incRep != 1) {
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                            logger.warnExp("%s. %s.", "Код и номер ДУЛ не соответствуют справочнику \"Физические лица\"", fioAndInp, pathError,
                                    "\"ДУЛ Номер\" не включается в отчетность")
                        }
                    }
                }

                // Спр18 Статус налогоплательщика (Обязательное поле)
                def taxpayerStatus = taxpayerStatusMap.get(personRecord.get(RF_TAXPAYER_STATE).value)
                if (ndflPerson.status!= null && !ndflPerson.status.equals(taxpayerStatus)) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "Статус налогоплательщица не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                            String.format(LOG_TYPE_PERSON_MSG, C_STATUS, ndflPerson.status ?: "", R_PERSON))
                }

                // Спр19 Адрес (Необязательное поле)
                // Сравнение должно быть проведено даже с учетом пропусков
                def address = addressMap.get(personRecord.get(RF_ADDRESS).value)
                def regionCode
                def area
                def city
                def locality
                def street
                def house
                def building
                def flat
                if (address != null) {
                    regionCode = address.get(RF_REGION_CODE).value
                    area = address.get(RF_DISTRICT).value
                    city = address.get(RF_CITY).value
                    locality = address.get(RF_LOCALITY).value
                    street = address.get(RF_STREET).value
                    house = address.get(RF_HOUSE).value
                    building = address.get(RF_BUILD).value
                    flat = address.get(RF_APPARTMENT).value
                }

                List<String> ndflPersonAddress = []

                // Адрес регистрации в Российской Федерации.Код субъекта
                if (ndflPerson.regionCode != null && !ndflPerson.regionCode.equals(regionCode)) {
                    ndflPersonAddress.add("Код субъекта='${ndflPerson.regionCode ?: ""}'")
                }

                // Адрес регистрации в Российской Федерации.Район
                if (ndflPerson.area != null && !ndflPerson.area.equals(area)) {
                    ndflPersonAddress.add("Район='${ndflPerson.area ?: ""}'")
                }

                // Адрес регистрации в Российской Федерации.Город
                if (ndflPerson.city != null && !ndflPerson.city.equals(city)) {
                    ndflPersonAddress.add("Город='${ndflPerson.city ?: ""}'")
                }

                // Адрес регистрации в Российской Федерации.Населенный пункт
                if (ndflPerson.locality != null && !ndflPerson.locality.equals(locality)) {
                    ndflPersonAddress.add("Населенный пункт='${ndflPerson.locality ?: ""}'")
                }

                // Адрес регистрации в Российской Федерации.Улица
                if (ndflPerson.street != null && !ndflPerson.street.equals(street)) {
                    ndflPersonAddress.add("Улица='${ndflPerson.street ?: ""}'")
                }

                // Адрес регистрации в Российской Федерации.Дом
                if (ndflPerson.house != null && !ndflPerson.house.equals(house)) {
                    ndflPersonAddress.add("Дом='${ndflPerson.house ?: ""}'")
                }

                // Адрес регистрации в Российской Федерации.Корпус
                if (ndflPerson.building != null && !ndflPerson.building.equals(building)) {
                    ndflPersonAddress.add("Корпус='${ndflPerson.building ?: ""}'")
                }

                // Адрес регистрации в Российской Федерации.Квартира
                if (ndflPerson.flat != null && !ndflPerson.flat.equals(flat)) {
                    ndflPersonAddress.add("Квартира='${ndflPerson.flat ?: ""}'")
                }
                if (!ndflPersonAddress.isEmpty()) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "Адрес не соответствует справочнику \"Физические лица\"", fioAndInp, pathError,
                            String.format(LOG_TYPE_PERSON_MSG, "Форма.Реквизиты.Адрес регистрации в Российской Федерации", ndflPersonAddress.join(", "), R_PERSON))

                }
            }
        }
    }
    logForDebug("Проверки на соответствие справочникам / '${T_PERSON_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");

    logForDebug("Проверки на соответствие справочникам / Проверка существования адреса (" + timeIsExistsAddress + " мс)");

    time = System.currentTimeMillis();
    for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {

        ScriptUtils.checkInterrupted();

        NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
        String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

        // Спр5 Код вида дохода (Необязательное поле)
        if (ndflPersonIncome.incomeCode != null && ndflPersonIncome.incomeAccruedDate != null && !incomeCodeMap.find { key, value ->
            value.CODE?.stringValue == ndflPersonIncome.incomeCode &&
                    ndflPersonIncome.incomeAccruedDate >= value.record_version_from?.dateValue &&
                    ndflPersonIncome.incomeAccruedDate <= value.record_version_to?.dateValue
        }) {
            String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                    C_INCOME_CODE, ndflPersonIncome.incomeCode ?: "",
                    R_INCOME_CODE
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_CODE), fioAndInp, pathError, errMsg)
        }

        /*
        Спр6
        При проверке Вида дохода должно проверятся не только наличие признака дохода в справочнике, но и принадлежность признака к конкретному Коду вида дохода

        Доход.Вид.Признак (Графа 5) - (Необязательное поле)
        incomeTypeMap <REF_BOOK_INCOME_KIND.MARK, List<REF_BOOK_INCOME_KIND.INCOME_TYPE_ID>>

        Доход.Вид.Код (Графа 4) - (Необязательное поле)
        incomeCodeMap <REF_BOOK_INCOME_TYPE.ID, REF_BOOK_INCOME_TYPE>
         */
        if (!ScriptUtils.isEmpty(ndflPersonIncome.incomeType)) {
            List<Long> incomeTypeIdList = incomeTypeMap.get(ndflPersonIncome.incomeType)
            if (incomeTypeIdList == null || incomeTypeIdList.isEmpty()) {
                String errMsg = String.format(LOG_TYPE_PERSON_MSG_2,
                        C_INCOME_TYPE, ndflPersonIncome.incomeType ?: "",
                        R_INCOME_TYPE
                )
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_TYPE), fioAndInp, pathError, errMsg)
            } else {
                if (!ScriptUtils.isEmpty(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeAccruedDate != null) {
                    def incomeCodeRefList = []
                    incomeTypeIdList.each { incomeTypeId ->
                        def incomeCodeRef = incomeCodeMap.get(incomeTypeId)
                        incomeCodeRefList.add(incomeCodeRef)
                    }
                    def incomeCodeRef = incomeCodeRefList.find {
                        it?.CODE?.stringValue == ndflPersonIncome.incomeCode &&
                                ndflPersonIncome.incomeAccruedDate >= it.record_version_from?.dateValue &&
                                ndflPersonIncome.incomeAccruedDate <= it.record_version_to?.dateValue
                    }
                    if (!incomeCodeRef) {
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\"), \"%s\" (\"%s\") отсутствует в справочнике \"%s\"",
                                C_INCOME_CODE, ndflPersonIncome.incomeCode ?: "",
                                C_INCOME_TYPE, ndflPersonIncome.incomeType ?: "",
                                R_INCOME_TYPE
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_INCOME_TYPE), fioAndInp, pathError,
                                errMsg)
                    }
                }
            }
        }
    }
    logForDebug("Проверки на соответствие справочникам / '${T_PERSON_INCOME_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();
    for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

        ScriptUtils.checkInterrupted();

        NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)
        String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

        // Спр8 Код вычета (Обязательное поле)
        if (ndflPersonDeduction.typeCode != "000" && ndflPersonDeduction.typeCode != null && !deductionTypeList.contains(ndflPersonDeduction.typeCode)) {
            String errMsg = String.format(LOG_TYPE_PERSON_MSG,
                    C_TYPE_CODE, ndflPersonDeduction.typeCode ?: "",
                    R_TYPE_CODE
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_TYPE_CODE), fioAndInp, pathError, errMsg)
        }

        // Спр9 Документ о праве на налоговый вычет.Код источника (Обязательное поле)
        if (ndflPersonDeduction.notifSource != null && !taxInspectionList.contains(ndflPersonDeduction.notifSource)) {
            //TODO turn_to_error
            String errMsg = String.format(LOG_TYPE_PERSON_MSG,
                    C_NOTIF_SOURCE, ndflPersonDeduction.notifSource ?: "",
                    R_NOTIF_SOURCE
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_NOTIF_SOURCE), fioAndInp, pathError, errMsg)
        }
    }
    logForDebug("Проверки на соответствие справочникам / '${T_PERSON_DEDUCTION_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();
    for (NdflPersonPrepayment ndflPersonPrepayment : ndflPersonPrepaymentList) {

        ScriptUtils.checkInterrupted();

        NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonPrepayment.ndflPersonId)
        String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

        // Спр9 Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Обязательное поле)
        if (ndflPersonPrepayment.notifSource != null && !taxInspectionList.contains(ndflPersonPrepayment.notifSource)) {
            //TODO turn_to_error
            String errMsg = String.format(LOG_TYPE_PERSON_MSG,
                    P_NOTIF_SOURCE, ndflPersonPrepayment.notifSource ?: "",
                    R_NOTIF_SOURCE
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_PREPAYMENT, ndflPersonPrepayment.rowNum ?: "")
            logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, R_NOTIF_SOURCE), fioAndInp, pathError, errMsg)
        }
    }
    logForDebug("Проверки на соответствие справочникам / '${T_PERSON_PREPAYMENT_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");
}

void logFiasError (fioAndInp, pathError, name, value) {
    logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, "КЛАДР"), fioAndInp, pathError,
            "Значение гр. \"" + name + "\" (\""+ (value?:"") + "\") отсутствует в справочнике \"КЛАДР\"")
}

void logFiasIndexError (fioAndInp, pathError, name, value) {
    logger.warnExp("%s. %s.", String.format(LOG_TYPE_REFERENCES, "КЛАДР"), fioAndInp, pathError,
            "Значение гр. \"" + name + "\" (\""+ (value?:"") + "\") не соответствует требуемому формату")
}

/**
 * Проверка адреса на пустоту
 * @param Данные о ФЛ из формы
 * @return
 */
boolean isPersonAddressEmpty(NdflPerson ndflPerson) {
    boolean  emptyAddress = ScriptUtils.isEmpty(ndflPerson.regionCode) && ScriptUtils.isEmpty(ndflPerson.area) &&
            ScriptUtils.isEmpty(ndflPerson.city) &&  ScriptUtils.isEmpty(ndflPerson.locality) &&
            ScriptUtils.isEmpty(ndflPerson.street) && ScriptUtils.isEmpty(ndflPerson.house) &&
            ScriptUtils.isEmpty(ndflPerson.building) &&  ScriptUtils.isEmpty(ndflPerson.flat);
    return emptyAddress;
}

/**
 * Общие проверки
 */
def checkDataCommon(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, Map<Long, Map<String, RefBookValue>> personMap) {
    long time = System.currentTimeMillis();
    long timeTotal = time
    // Параметры подразделения
    def mapRefBookNdfl = getRefBookNdfl()
    def mapRefBookNdflDetail = getRefBookNdflDetail(mapRefBookNdfl.id)

    logForDebug("Общие проверки: инициализация (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();

    for (NdflPerson ndflPerson : ndflPersonList) {

        ScriptUtils.checkInterrupted();

        NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
        if (ndflPersonFL == null) {
            if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                // РНУ-НДФЛ первичная
                String fio = (ndflPerson.lastName?:"") + " " + (ndflPerson.firstName?:"") + " " + (ndflPerson.middleName ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp)
            } else {
                // РНУ-НДФЛ консолидированная
                def personRecord = personMap.get(ndflPerson.recordId)
                String fio = (personRecord.get(RF_LAST_NAME).value?:"") + " " + (personRecord.get(RF_FIRST_NAME).value?:"") + " " + (personRecord.get(RF_MIDDLE_NAME).value ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
            }
            ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
        }
        String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

        // Общ1 Корректность ИНН (Необязательное поле)
        if (ndflPerson.citizenship == "643") {
            if (ndflPerson.innNp == null) {
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.warnExp("%s. %s.", "ИНН не указан", fioAndInp, pathError,
                        "Значение гр. \"ИНН в РФ\" не указано. Прием налоговым органом обеспечивается, может быть предупреждение")
            } else {
                String checkInn = ScriptUtils.checkInn(ndflPerson.innNp)
                if (checkInn != null) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.errorExp("%s. %s.", "ИНН не соответствует формату", fioAndInp, pathError,
                            checkInn)
                }
            }
        }

        //Общ2 Наличие обязательных реквизитов для формирования отчетности
        boolean checkLastName = checkRequiredAttribute(ndflPerson, fioAndInp, "lastName", "Фамилия")
        boolean checkFirstName = checkRequiredAttribute(ndflPerson, fioAndInp, "firstName", "Имя")
        checkRequiredAttribute(ndflPerson, fioAndInp, "birthDay", "Дата рождения")
        boolean checkCitizenship = checkRequiredAttribute(ndflPerson, fioAndInp, "citizenship", C_CITIZENSHIP)
        boolean checkIdDocType = checkRequiredAttribute(ndflPerson, fioAndInp, "idDocType", "ДУЛ Код")
        boolean checkIdDocNumber = checkRequiredAttribute(ndflPerson, fioAndInp, "idDocNumber", "ДУЛ Номер")
        checkRequiredAttribute(ndflPerson, fioAndInp, "status", C_STATUS)
        if (checkCitizenship) {
            if (ndflPerson.citizenship == "643") {
                checkRequiredAttribute(ndflPerson, fioAndInp, "regionCode", "Код субъекта")
            } else {
                checkRequiredAttribute(ndflPerson, fioAndInp, "countryCode", "Код страны проживания вне РФ")
                checkRequiredAttribute(ndflPerson, fioAndInp, "address", "Адрес проживания вне РФ ")
            }
        }

        if (ndflPerson.citizenship == "643") {
            if (checkLastName) {
                String checkName = ScriptUtils.checkName(ndflPerson.lastName, "Фамилия")
                if (checkName != null) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "Фамилия, Имя не соответствует формату", fioAndInp, pathError,
                            checkName)
                }
            }
            if (checkFirstName) {
                String checkName = ScriptUtils.checkName(ndflPerson.firstName, "Имя")
                if (checkName != null) {
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                    logger.warnExp("%s. %s.", "Фамилия, Имя не соответствует формату", fioAndInp, pathError,
                            checkName)
                }
            }
        }
        if (checkIdDocType && checkIdDocNumber) {
            String checkDul = ScriptUtils.checkDul(ndflPerson.idDocType, ndflPerson.idDocNumber, "ДУЛ Номер")
            if (checkDul != null) {
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
                logger.warnExp("%s. %s.", "ДУЛ не соответствует формату", fioAndInp, pathError,
                        checkDul)
            }
        }

        // Общ11 СНИЛС (Необязательное поле)
        if (ndflPerson.snils != null && !ScriptUtils.checkSnils(ndflPerson.snils)) {
            String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует формату",
                    "СНИЛС", ndflPerson.snils?:""
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
            logger.warnExp("%s. %s.", "СНИЛС не соответствует формату", fioAndInp, pathError,
                    errMsg)
        }
    }
    logForDebug("Общие проверки / '${T_PERSON_NAME}' (" + (System.currentTimeMillis() - time) + " мс)");

    time = System.currentTimeMillis();

    Department department = departmentService.get(declarationData.departmentId)

    for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {

        ScriptUtils.checkInterrupted();

        boolean applyTemporalySolution = false
        if (ndflPersonIncome.incomeAccruedSumm == ndflPersonIncome.totalDeductionsSumm) {
            applyTemporalySolution = true
        }

        NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
        String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

        // Общ5 Принадлежность дат операций к отчетному периоду. Проверка перенесана в событие загрузки ТФ

        // Общ7 Наличие или отсутствие значения в графе в зависимости от условий
        List<ColumnFillConditionData> columnFillConditionDataList = []
        //1 Раздел 2. Графы 4,5 должны быть заполнены, если не заполнены Раздел 2. Графы 22,23,24
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column22And23And24NotFill(),
                new Column4Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                        C_INCOME_CODE,
                        C_PAYMENT_DATE,
                        C_PAYMENT_NUMBER,
                        C_TAX_SUMM
                )
        )
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column22And23And24NotFill(),
                new Column5Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                        C_INCOME_CODE,
                        C_PAYMENT_DATE,
                        C_PAYMENT_NUMBER,
                        C_TAX_SUMM
                )
        )
        //2 Раздел 2. Графа 6 должна быть заполнена, если заполнена Раздел 2. Графа 10
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column10Fill(),
                new Column6Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_ACCRUED_DATE,
                        C_INCOME_ACCRUED_SUMM
                )
        )
        //3 Раздел 2. Графа 7 должна быть заполнена, если заполнена Раздел 2. Графа 11
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column11Fill(),
                new Column7Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_PAYOUT_DATE,
                        C_INCOME_PAYOUT_SUMM
                )
        )
        //3 Раздел 2. Графа 8 Должна быть всегда заполнена
        columnFillConditionDataList << new ColumnFillConditionData(
                new ColumnTrueFillOrNotFill(),
                new Column8Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Не заполнена гр. \"%s\"",
                        C_OKTMO
                )
        )
        //3 Раздел 2. Графа 9 Должна быть всегда заполнена
        columnFillConditionDataList << new ColumnFillConditionData(
                new ColumnTrueFillOrNotFill(),
                new Column8Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Не заполнена гр. \"%s\"",
                        C_KPP
                )
        )
        //4 Раздел 2. Графа 10 должна быть заполнена, если заполнена Раздел 2. Графа 6
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column6Fill(),
                new Column10Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_ACCRUED_SUMM,
                        C_INCOME_ACCRUED_DATE
                )
        )
        //5 Раздел 2. Графа 11 должна быть заполнена, если заполнена Раздел 2. Графа 7
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column7Fill(),
                new Column11Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_PAYOUT_SUMM,
                        C_INCOME_PAYOUT_DATE
                )
        )
        //6 Раздел 2. Графа 12 должна быть не заполнена, если заполнены Раздел 2. Графы 22, 23, 24
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column22And23And24Fill(),
                new Column12NotFill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" (\"%s\") не должна быть заполнена, так как заполнены гр. \"%s\", \"%s\", \"%s\"",
                        C_TOTAL_DEDUCTIONS_SUMM, ndflPersonIncome.totalDeductionsSumm ?:"",
                        C_PAYMENT_DATE,
                        C_PAYMENT_NUMBER,
                        C_TAX_SUMM
                )
        )
        //7 Раздел 2. Графы 13 должны быть заполнены, если не заполнены Раздел 2. Графы 22, 23, 24
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column22And23And24NotFill(),
                new Column13Fill(applyTemporalySolution),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                        C_TAX_BASE,
                        C_PAYMENT_DATE,
                        C_PAYMENT_NUMBER,
                        C_TAX_SUMM
                )
        )
        //7 Раздел 2. Графы 14 должны быть заполнены, если не заполнены Раздел 2. Графы 22, 23, 24
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column22And23And24NotFill(),
                new Column14Fill(applyTemporalySolution),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                        C_TAX_BASE,
                        C_PAYMENT_DATE,
                        C_PAYMENT_NUMBER,
                        C_TAX_SUMM
                )
        )
        //7 Раздел 2. Графы 15 должны быть заполнены, если не заполнены Раздел 2. Графы 22, 23, 24
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column22And23And24NotFill(),
                new Column15Fill(applyTemporalySolution),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как не заполнены гр. \"%s\", \"%s\", \"%s\"",
                        C_TAX_DATE,
                        C_PAYMENT_DATE,
                        C_PAYMENT_NUMBER,
                        C_TAX_SUMM
                )
        )
        //8 Раздел 2. Графы 6 должны быть заполнены, если заполнена Раздел 2. Графа 61
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column16Fill(),
                new Column6Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_ACCRUED_DATE,
                        C_CALCULATED_TAX
                )
        )
        //8 Раздел 2. Графы 10 должны быть заполнены, если заполнена Раздел 2. Графа 16
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column16Fill(),
                new Column10Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_ACCRUED_SUMM,
                        C_CALCULATED_TAX
                )
        )
        //9 Раздел 2. Графа 6 должны быть заполнены, если заполнена Раздел 2. Графа 18 или 19
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column18Fill(),
                new Column6Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_ACCRUED_DATE,
                        C_NOT_HOLDING_TAX
                )
        )
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column19Fill(),
                new Column6Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_ACCRUED_DATE,
                        C_OVERHOLDING_TAX
                )
        )
        //9 Раздел 2. Графа 10 должны быть заполнены, если заполнена Раздел 2. Графа 18 или 19
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column18Fill(),
                new Column10Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_ACCRUED_SUMM,
                        C_NOT_HOLDING_TAX
                )
        )
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column19Fill(),
                new Column10Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_ACCRUED_SUMM,
                        C_OVERHOLDING_TAX
                )
        )
        //10 Раздел 2. Графы 7 должны быть заполнены, если заполнена Раздел 2. Графа 17
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column17Fill(),
                new Column7Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_PAYOUT_DATE,
                        C_WITHHOLDING_TAX
                )

        )
        //10 Раздел 2. Графы 11 должны быть заполнены, если заполнена Раздел 2. Графа 17
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column17Fill(),
                new Column11Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_PAYOUT_SUMM,
                        C_WITHHOLDING_TAX
                )
        )
        //11 Раздел 2. Графы 7 должны быть заполнены, если заполнена Раздел 2. Графа 20
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column20Fill(),
                new Column7Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_PAYOUT_DATE,
                        C_REFOUND_TAX
                )

        )
        //11 Раздел 2. Графы 11 должны быть заполнены, если заполнена Раздел 2. Графа 20
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column20Fill(),
                new Column11Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнена гр. \"%s\"",
                        C_INCOME_PAYOUT_SUMM,
                        C_REFOUND_TAX
                )
        )
        //12 Раздел 2. Графа 21 должна быть заполнена, если заполнены Раздел 2. Графы 7, 11
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column7And11Fill(),
                new Column21Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнены гр. \"%s\", \"%s\"",
                        C_TAX_TRANSFER_DATE,
                        C_INCOME_PAYOUT_DATE,
                        C_INCOME_PAYOUT_SUMM
                )

        )
        //12 Раздел 2. Графа 21 должна быть заполнена, если заполнены Раздел 2. Графы 23, 23, 24
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column22And23And24Fill(),
                new Column21Fill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" должна быть заполнена, так как заполнены гр. \"%s\", \"%s\", \"%s\"",
                        C_TAX_TRANSFER_DATE,
                        C_PAYMENT_DATE,
                        C_PAYMENT_NUMBER,
                        C_TAX_SUMM
                )

        )
        //12 Раздел 2. Графа 21 должна быть НЕ заполнена, если НЕ заполнены Раздел 2. Графы 7, 11 и 22, 23, 24
        columnFillConditionDataList << new ColumnFillConditionData(
                new Column7And11And22And23And24NotFill(),
                new Column21NotFill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\" (\"%s\") не должна быть заполнена, так как не заполнены гр. \"%s\", гр. \"%s\", и не заполнены гр. \"%s\", гр. \"%s\", гр. \"%s\"",
                        C_TAX_TRANSFER_DATE, ndflPersonIncome.taxTransferDate ? ndflPersonIncome.taxTransferDate.format(DATE_FORMAT): "",
                        C_INCOME_PAYOUT_DATE,
                        C_INCOME_PAYOUT_SUMM,
                        C_PAYMENT_DATE,
                        C_PAYMENT_NUMBER,
                        C_TAX_SUMM
                )

        )
        //13 Должны быть либо заполнены все 3 Графы 22, 23, 24, либо ни одна из них
        columnFillConditionDataList << new ColumnFillConditionData(
                new ColumnTrueFillOrNotFill(),
                new Column22And23And24FillOrColumn22And23And24NotFill(),
                String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: ""),
                String.format("Гр. \"%s\", гр. \"%s\", гр. \"%s\" должны быть заполнены одновременно или не заполнена ни одна из них",
                        C_PAYMENT_DATE,
                        C_PAYMENT_NUMBER,
                        C_TAX_SUMM
                )

        )
        columnFillConditionDataList.each { columnFillConditionData ->
            if (columnFillConditionData.columnConditionCheckerAsIs.check(ndflPersonIncome) &&
                    !columnFillConditionData.columnConditionCheckerToBe.check(ndflPersonIncome)) {
                logger.errorExp("%s. %s.", "Наличие (отсутствие) значения в графе не соответствует алгоритму заполнения РНУ НДФЛ",
                        fioAndInp, columnFillConditionData.conditionPath, columnFillConditionData.conditionMessage)
                //                println(String.format("%s. %s.", columnFillConditionData.conditionPath, columnFillConditionData.conditionMessage))
            }
        }

        // Общ10 Соответствие КПП и ОКТМО Тербанку
        if (ndflPersonIncome.oktmo != null) {
            def kppList = mapRefBookNdflDetail.get(ndflPersonIncome.oktmo)
            if (kppList == null || !kppList?.contains(ndflPersonIncome.kpp)) {

                if (kppList == null) {
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") отсутствует в справочнике \"%s\" для \"%s\"",
                            C_OKTMO, ndflPersonIncome.oktmo ?: "",
                            R_DETAIL,
                            department ? department.name : ""
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", "КПП и ОКТМО не соответствуют Тербанку", fioAndInp, pathError,
                            errMsg)
                } else {
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") отсутствует в справочнике \"%s\" для \"%s\"",
                            C_KPP, ndflPersonIncome.kpp ?: "",
                            R_DETAIL,
                            department ? department.name : ""
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", "КПП и ОКТМО не соответствуют Тербанку", fioAndInp, pathError,
                            errMsg)
                }
            }
        }
    }

    ScriptUtils.checkInterrupted();

    logForDebug("Общие проверки / '$T_PERSON_INCOME_NAME' (" + (System.currentTimeMillis() - time) + " мс)");

    logForDebug("Общие проверки всего (" + (System.currentTimeMillis() - timeTotal) + " мс)");
}

boolean checkRequiredAttribute(def ndflPerson, String fioAndInp, String alias, String attributeName) {
    if (ndflPerson[alias] == null || (ndflPerson[alias]) instanceof String && (org.apache.commons.lang3.StringUtils.isBlank(ndflPerson[alias]) || ndflPerson[alias] == "0")) {
        String pathError = String.format(SECTION_LINE_MSG, T_PERSON, ndflPerson.rowNum ?: "")
        String msg
        if (ndflPerson[alias] == "0") {
            msg = "Значение гр. \"$attributeName\" не может быть равно \"0\""
        } else {
            msg = "Значение гр. \"$attributeName\" не указано"
        }
        logger.warnExp("%s. %s.", "Не указан обязательный реквизит ФЛ", fioAndInp, pathError, msg)
        return false
    }
    return true
}

// Кэш для справочников
@Field Map<String, Map<String, RefBookValue>> refBookCache = [:]

/**
 * Разыменование записи справочника
 */
@TypeChecked
Map<String, RefBookValue> getRefBookValue(long refBookId, Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

/**
 * Проверки сведений о доходах
 * @param ndflPersonList
 * @param ndflPersonIncomeList
 * @param ndflPersonDeductionList
 * @param ndflPersonPrepaymentList
 */
def checkDataIncome(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList, List<NdflPersonDeduction> ndflPersonDeductionList,
                    List<NdflPersonPrepayment> ndflPersonPrepaymentList, Map<Long, Map<String, RefBookValue>> personMap) {

    long time = System.currentTimeMillis()

    def personsCache = [:]
    ndflPersonList.each { ndflPerson ->
        personsCache.put(ndflPerson.id, ndflPerson)

        NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
        if (ndflPersonFL == null) {
            if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                // РНУ-НДФЛ первичная
                String fio = (ndflPerson.lastName?:"") + " " + (ndflPerson.firstName?:"") + " " + (ndflPerson.middleName ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp)
            } else {
                // РНУ-НДФЛ консолидированная
                def personRecord = personMap.get(ndflPerson.recordId)
                String fio = (personRecord.get(RF_LAST_NAME).value?:"") + " " + (personRecord.get(RF_FIRST_NAME).value?:"") + " " + (personRecord.get(RF_MIDDLE_NAME).value ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
            }
            ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
        }
    }

    def ndflPersonPrepaymentCache = [:]
    ndflPersonPrepaymentList.each { ndflPersonPrepayment ->
        List<NdflPersonPrepayment> ndflPersonPrepaymentListByPersonIdList = ndflPersonPrepaymentCache.get(ndflPersonPrepayment.ndflPersonId) ?: []
        ndflPersonPrepaymentListByPersonIdList.add(ndflPersonPrepayment)
        ndflPersonPrepaymentCache.put(ndflPersonPrepayment.ndflPersonId, ndflPersonPrepaymentListByPersonIdList)
    }

    List<DateConditionData> dateConditionDataList = []

    List<DateConditionData> dateConditionDataListForBudget = []

    DateConditionWorkDay dateConditionWorkDay = new DateConditionWorkDay(calendarService)

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["1010", "1011", "3020", "1110", "1400", "2001", "2010", "2012",
                                                    "2300", "2710", "2760", "2762", "2770", "2900", "4800"],
            ["00"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                    "1541", "1542", "1543", "1544", "1545", "1546", "1547",
                                                    "1548", "1549", "1551", "1552", "1554"],
            ["01", "02"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

    // Соответствует маске 31.12.20**
    dateConditionDataList << new DateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                    "1541", "1542", "1543"],
            ["04"], new MatchMask("31.12.20\\d{2}"), "Значение гр. \"%s\" (\"%s\") должно быть равно \"31.12.20**\"")

    // Последний календарный день месяца
    dateConditionDataList << new DateConditionData(["2000"], ["05"], new LastMonthCalendarDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца, за который был начислен доход")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["07"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

    // Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7», иначе «графа 6» = 31.12.20**
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["08", "09", "10"], new Column7LastDayOfYear1(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["08", "09", "10"], new Column7LastDayOfYear2(), "Значение гр. \"%s\" (\"%s\") должно быть равно \"31.12.20**\"")

    // Последний календарный день месяца
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["11"], new LastMonthCalendarDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца, в котором утверждён авансовый отчёт о командировке")

    // Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7», иначе «графа 6» = 31.12.20**
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["12"], new Column7LastDayOfYear1(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")
    dateConditionDataList << new DateConditionData(["2000", "2002"], ["12"], new Column7LastDayOfYear2(), "Значение гр. \"%s\" (\"%s\") должно быть равно \"31.12.20**\"")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["2520", "2720", "2740", "2750", "2790"], ["00"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

    // Доход.Дата.Начисление (Графа 6) последний календарный день месяца
    dateConditionDataList << new DateConditionData(["2610"], ["00"], new LastMonthWorkDayIncomeAccruedDate(), "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню каждого месяца, в течение срока, на который были предоставлены кредитные (заёмные) средства")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["2640", "2641"], ["00"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

    // "Графа 6" = "Графе 7"
    dateConditionDataList << new DateConditionData(["2800"], ["00"], new Column6EqualsColumn7(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"Дата выплаты дохода\"")

    // 1,2 "Графа 21" = "Графа 7" + "1 рабочий день"
    dateConditionDataListForBudget << new DateConditionData(["1010", "1011", "3020", "1110", "1400", "2001", "2010",
                                                             "2710", "2760", "2762", "2770", "2900", "4800"], ["00"],
            new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

    // 3,4 "Графа 21" <= "Графа 7" + "30 календарных дней", если "Графа 7" + "30 календарных дней" - выходной день, то "Графа 21" <= "Следующий рабочий день" после "Графа 7" + "30 календарных дней"
    dateConditionDataListForBudget << new DateConditionData(["1530", "1531", "1533", "1535", "1536", "1537", "1539",
                                                             "1541", "1542", "1543", "1544", "1545", "1546", "1547",
                                                             "1548", "1549", "1551", "1552", "1553", "1554"], ["01", "02", "03", "04"],
            new Column21EqualsColumn7Plus30WorkingDays(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

    // 6 "Графа 21" = "Графа 7" + "1 рабочий день"
    dateConditionDataListForBudget << new DateConditionData(["2000"], ["05", "06", "07", "08", "09", "10", "11", "12"],
            new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

    // 7 "Графа 21" = "Графа 7" + "1 рабочий день"
    dateConditionDataListForBudget << new DateConditionData(["2002"], ["07", "08", "09", "10"],
            new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

    // 8 "Графа 21" = "Графа 7" + "1 рабочий день"
    dateConditionDataListForBudget << new DateConditionData(["2003"], ["13"],
            new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

    // 9 "Графа 21" = Последний календарный день месяца для месяца "Графы 7", если Последний календарный день месяца - выходной день, то "Графа 21" = следующий рабочий день
    dateConditionDataListForBudget << new DateConditionData(["2012", "2300"], ["00"],
            new Column21EqualsColumn7LastDayOfMonth(), "Значение гр. \"%s\" (\"%s\") должно быть равно последнему календарному дню месяца выплаты дохода")

    // 10 "Графа 21" = "Графа 7" + "1 рабочий день"
    dateConditionDataListForBudget << new DateConditionData(["2520", "2740", "2750", "2790", "4800"], ["13"],
            new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

    // 12,13,14 "Графа 21" = "Графа 7" + "1 рабочий день"
    dateConditionDataListForBudget << new DateConditionData(["2610", "2640", "2641", "2800"], ["00"],
            new Column21EqualsColumn7Plus1WorkingDay(), "Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день")

    // Сгруппируем Сведения о доходах на основании принадлежности к плательщику
    def ndflPersonIncomeCache = [:]
    ndflPersonIncomeList.each { ndflPersonIncome ->
        List<NdflPersonIncome> ndflPersonIncomeByNdflPersonIdList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: []
        ndflPersonIncomeByNdflPersonIdList.add(ndflPersonIncome)
        ndflPersonIncomeCache.put(ndflPersonIncome.ndflPersonId, ndflPersonIncomeByNdflPersonIdList)
    }

    ndflPersonIncomeCache.each {

        ScriptUtils.checkInterrupted();

        for (NdflPersonIncome ndflPersonIncome : it.value) {
            def ndflPerson = personsCache.get(ndflPersonIncome.ndflPersonId)

            NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)
            String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

            // СведДох1 Доход.Дата.Начисление (Графа 6)
            if (dateConditionDataList != null && !(ndflPersonIncome.incomeAccruedSumm == null || ndflPersonIncome.incomeAccruedSumm == 0)) {
                dateConditionDataList.each { dateConditionData ->
                    if (dateConditionData.incomeCodes.contains(ndflPersonIncome.incomeCode) && dateConditionData.incomeTypes.contains(ndflPersonIncome.incomeType)) {
                        if (!dateConditionData.checker.check(ndflPersonIncome, dateConditionWorkDay)) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            String errMsg = String.format(dateConditionData.conditionMessage,
                                    C_INCOME_ACCRUED_DATE, ndflPersonIncome.incomeAccruedDate ? ndflPersonIncome.incomeAccruedDate.format(DATE_FORMAT): ""
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.warnExp("%s. %s.", LOG_TYPE_2_6, fioAndInp, pathError, errMsg)
                        }
                    }
                }
            }

            // СведДох2 Сумма вычета (Графа 12)
            if (ndflPersonIncome.totalDeductionsSumm != null && ndflPersonIncome.totalDeductionsSumm != 0) {
                BigDecimal sumNdflDeduction = getDeductionSumForIncome(ndflPersonIncome, ndflPersonDeductionList)
                if (!comparNumbEquals(ndflPersonIncome.totalDeductionsSumm ?: 0, sumNdflDeduction)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно сумме гр. \"%s\" (\"%s\") раздела 3",
                            C_TOTAL_DEDUCTIONS_SUMM, ndflPersonIncome.totalDeductionsSumm ?: 0,
                            C_TOTAL_DEDUCTIONS_SUMM, sumNdflDeduction ?: 0)
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_2_12, fioAndInp, pathError, errMsg)
                }
                if (comparNumbGreater(sumNdflDeduction, ndflPersonIncome.incomeAccruedSumm ?: 0)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть не меньше значение гр. \"%s\" (\"%s\")",
                            C_INCOME_ACCRUED_SUMM, ndflPersonIncome.incomeAccruedSumm ?: 0,
                            C_PERIOD_CURR_SUMM, sumNdflDeduction
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_2_12, fioAndInp, pathError, errMsg)
                }
            }

            // СведДох4 НДФЛ.Процентная ставка (Графа 14)
            if ((ndflPersonIncome.taxRate?:0) > 0) {
                boolean checkNdflPersonIncomingTaxRateTotal = false;

                boolean presentCitizenship = ndflPerson.citizenship != null && ndflPerson.citizenship != "0"
                boolean presentIncomeCode = ndflPersonIncome.incomeCode != null && ndflPersonIncome.incomeCode != "0"
                boolean presentStatus = ndflPerson.status != null && ndflPerson.status != "0"
                boolean presentTaxRate = ndflPersonIncome.taxRate != null && ndflPersonIncome.taxRate != 0
                def ndflPersonIncomingTaxRates = []
                CHECK_NDFL_PERSON_INCOMING_TAX_RATE_13: {
                    if (presentCitizenship && presentIncomeCode && presentStatus && presentTaxRate) {
                        Boolean conditionA = ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode != "1010" && ndflPerson.status != "2"
                        Boolean conditionB = ndflPerson.citizenship == "643" && ["1010", "1011"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status == "1"
                        Boolean conditionC = ndflPerson.citizenship != "643" && ["2000", "2001", "2010", "2002", "2003"].contains(ndflPersonIncome.incomeCode) && Integer.parseInt(ndflPerson.status ?: 0) >= 3
                        if (conditionA || conditionB || conditionC) {
                            if (ndflPersonIncome.taxRate == 13) {
                                checkNdflPersonIncomingTaxRateTotal = true
                            } else {
                                ndflPersonIncomingTaxRates << "\"13\""
                            }
                        }
                    }
                }
                CHECK_NDFL_PERSON_INCOMING_TAX_RATE_15: {
                    if ((presentIncomeCode && presentStatus && presentTaxRate) && (ndflPersonIncome.incomeCode == "1010" && ndflPerson.status != "1")) {
                        if (ndflPersonIncome.taxRate == 15) {
                            checkNdflPersonIncomingTaxRateTotal = true
                        } else {
                            ndflPersonIncomingTaxRates << "\"15\""
                        }
                    }
                }
                CHECK_NDFL_PERSON_INCOMING_TAX_RATE_35: {
                    if ((presentIncomeCode && presentStatus && presentTaxRate) && (["2740", "3020", "2610"].contains(ndflPersonIncome.incomeCode) && ndflPerson.status != "2")) {
                        if (ndflPersonIncome.taxRate == 35) {
                            checkNdflPersonIncomingTaxRateTotal = true
                        } else {
                            ndflPersonIncomingTaxRates << "\"35\""
                        }
                    }
                }
                CHECK_NDFL_PERSON_INCOMING_TAX_RATE_30: {
                    if (presentIncomeCode && presentStatus && presentTaxRate) {
                        def conditionA = Integer.parseInt(ndflPerson.status ?: 0) >= 2 && ndflPersonIncome.incomeCode != "1010"
                        def conditionB = Integer.parseInt(ndflPerson.status ?: 0) > 2 && !["2000", "2001", "2010"].contains(ndflPersonIncome.incomeCode)
                        if (conditionA || conditionB) {
                            if (ndflPersonIncome.taxRate == 30) {
                                checkNdflPersonIncomingTaxRateTotal = true
                            } else {
                                ndflPersonIncomingTaxRates << "\"30\""
                            }
                        }
                    }
                }
                CHECK_NDFL_PERSON_INCOMING_TAX_RATE_9: {
                    if ((presentCitizenship && presentIncomeCode && presentStatus && presentTaxRate) && (ndflPerson.citizenship == "643" && ndflPersonIncome.incomeCode == "1110" && ndflPerson.status == "1")) {
                        if (ndflPersonIncome.taxRate == 9) {
                            checkNdflPersonIncomingTaxRateTotal = true
                        } else {
                            ndflPersonIncomingTaxRates << "\"9\""
                        }
                    }
                }
                CHECK_NDFL_PERSON_INCOMING_TAX_RATE_OTHER: {
                    if ((presentCitizenship && presentIncomeCode && presentStatus && presentTaxRate) && (ndflPerson.citizenship != "643" && ndflPersonIncome.incomeCode == "1010" && ndflPerson.status != "1")) {
                        if (![13, 15, 35, 30, 9].contains(ndflPersonIncome.taxRate)) {
                            checkNdflPersonIncomingTaxRateTotal = true
                        } else {
                            ndflPersonIncomingTaxRates << "\"Специальная ставка\""
                        }
                    }
                }
                if (!checkNdflPersonIncomingTaxRateTotal && !ndflPersonIncomingTaxRates.isEmpty()) {
                    String errMsg = String.format(LOG_TYPE_2_14_MSG, "Процентная ставка (%)", ndflPersonIncome.taxRate ?: "",
                            ndflPersonIncome.incomeCode ?: "", ndflPerson.status?:"",
                            ndflPersonIncomingTaxRates.join(", ")
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_2_14, fioAndInp, pathError, errMsg)
                }
            }

            // СведДох5 НДФЛ.Расчет.Дата (Графа 15)
            if (ndflPersonIncome.taxDate != null) {

                boolean checkTaxDate = true
                List<Pair<String, String>> logTypeMessagePairList = []
                boolean calculatedTaxPresented = isPresentedByTempSolution(ndflPersonIncome.calculatedTax, ndflPersonIncome.incomeAccruedSumm, ndflPersonIncome.totalDeductionsSumm)
                boolean withholdingTaxPresented = isPresentedByTempSolution(ndflPersonIncome.withholdingTax, ndflPersonIncome.incomeAccruedSumm, ndflPersonIncome.totalDeductionsSumm)
                // СведДох5.1
                if (calculatedTaxPresented && (ndflPersonIncome.calculatedTax ?: 0 > 0) && ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                    // «Графа 15 Раздел 2» = «Графа 6 Раздел 2»
                    if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomeAccruedDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomeAccruedDate) {
                        checkTaxDate = false
                        logTypeMessagePairList.add(new Pair("\"${"Дата исчисленного налога"}\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT) : ""}\") должно быть равно значению гр. \"${C_INCOME_ACCRUED_DATE}\" (\"${ndflPersonIncome.incomeAccruedDate ? ndflPersonIncome.incomeAccruedDate.format(DATE_FORMAT) : ""}\")"))
                    }
                }
                // СведДох5.2
                if (withholdingTaxPresented && (ndflPersonIncome.withholdingTax ?: 0 > 0) && ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                    // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                    if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                        checkTaxDate = false
                        logTypeMessagePairList.add(new Pair("\"${"Дата удержанного налога"}\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT) : ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ndflPersonIncome.incomePayoutDate.format(DATE_FORMAT) : ""}\")"))
                    }
                }
                // СведДох5.3
                if (ndflPersonIncome.notHoldingTax != null && withholdingTaxPresented && (ndflPersonIncome.notHoldingTax ?: 0 > 0) &&
                        (ndflPersonIncome.withholdingTax ?: 0) < (ndflPersonIncome.calculatedTax ?: 0) &&
                        ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null &&
                        !["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543"].contains(ndflPersonIncome.incomeCode)) {
                    // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                    if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                        checkTaxDate = false
                        logTypeMessagePairList.add(new Pair("\"Дата не удержаннного налога\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT) : ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ndflPersonIncome.incomePayoutDate.format(DATE_FORMAT) : ""}\")"))
                    }
                }
                // СведДох5.4
                if (ndflPersonIncome.notHoldingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.notHoldingTax ?: 0 > 0) &&
                        (ndflPersonIncome.withholdingTax ?: 0) < (ndflPersonIncome.calculatedTax ?: 0) &&
                        ["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543"].contains(ndflPersonIncome.incomeCode) &&
                        ndflPersonIncome.incomePayoutDate >= getReportPeriodStartDate() && ndflPersonIncome.incomePayoutDate <= getReportPeriodEndDate()) {
                    // «Графа 15 Раздел 2» = «Графа 6 Раздел 2»
                    if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomeAccruedDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomeAccruedDate) {
                        checkTaxDate = false
                        logTypeMessagePairList.add(new Pair("\"Дата не удержаннного налога\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT) : ""}\") должно быть равно значению гр. \"${C_INCOME_ACCRUED_DATE}\" (\"${ndflPersonIncome.incomeAccruedDate ? ndflPersonIncome.incomeAccruedDate.format(DATE_FORMAT) : ""}\")"))
                    }
                }
                // СведДох5.5
                if (ndflPersonIncome.notHoldingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.notHoldingTax ?: 0 > 0) &&
                        (ndflPersonIncome.withholdingTax ?: 0) < (ndflPersonIncome.calculatedTax ?: 0) &&
                        ["1530", "1531", "1533", "1535", "1536", "1537", "1539", "1541", "1542"].contains(ndflPersonIncome.incomeCode) &&
                        (ndflPersonIncome.incomeAccruedDate < getReportPeriodStartDate() || ndflPersonIncome.incomeAccruedDate > getReportPeriodEndDate())) {
                    // «Графа 15 Раздел 2"» = "31.12.20**"
                    if (ndflPersonIncome.taxDate != null) {
                        Calendar calendarPayout = Calendar.getInstance()
                        calendarPayout.setTime(ndflPersonIncome.taxDate)
                        int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
                        int month = calendarPayout.get(Calendar.MONTH)
                        if (!(dayOfMonth == 31 && month == 12)) {
                            checkTaxDate = false
                            logTypeMessagePairList.add(new Pair("\"Дата не удержаннного налога\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT) : ""}\") должно быть равно \"31.12.20**\""))
                        }
                    }
                }
                // СведДох5.6
                if (ndflPersonIncome.overholdingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.overholdingTax ?: 0 > 0) &&
                        (ndflPersonIncome.withholdingTax ?: 0) > (ndflPersonIncome.calculatedTax ?: 0) &&
                        ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                    // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                    if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                        checkTaxDate = false
                        logTypeMessagePairList.add(new Pair("\"Дата излишне удержанного налога\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT) : ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ndflPersonIncome.incomePayoutDate.format(DATE_FORMAT) : ""}\")"))
                    }
                }
                // СведДох5.7
                if (ndflPersonIncome.refoundTax != null && ndflPersonIncome.overholdingTax != null && withholdingTaxPresented && calculatedTaxPresented && (ndflPersonIncome.refoundTax ?: 0 > 0) &&
                        (ndflPersonIncome.withholdingTax ?: 0) > (ndflPersonIncome.calculatedTax ?: 0) &&
                        (ndflPersonIncome.overholdingTax ?: 0) &&
                        ndflPersonIncome.incomeCode != "0" && ndflPersonIncome.incomeCode != null) {
                    // «Графа 15 Раздел 2» = «Графа 7 Раздел 2»
                    if (ndflPersonIncome.taxDate != null && ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.taxDate != ndflPersonIncome.incomePayoutDate) {
                        checkTaxDate = false
                        logTypeMessagePairList.add(new Pair("\"Дата расчета возвращенного налогоплательщику налога\" рассчитана некорректно", "Значение гр. \"${C_TAX_DATE}\" (\"${ndflPersonIncome.taxDate ? ndflPersonIncome.taxDate.format(DATE_FORMAT): ""}\") должно быть равно значению гр. \"${C_INCOME_PAYOUT_DATE}\" (\"${ndflPersonIncome.incomePayoutDate ? ndflPersonIncome.incomePayoutDate.format(DATE_FORMAT): ""}\")"))
                    }
                }
                if (!checkTaxDate) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    for (Pair<String, String> logTypeMessagePair : logTypeMessagePairList) {
                        logger.warnExp("%s. %s.", logTypeMessagePair.getFirst(), fioAndInp, pathError, logTypeMessagePair.getSecond())
                    }
                }
            }

            // СведДох6 НДФЛ.Расчет.Сумма.Исчисленный (Графа 16)
            if (ndflPersonIncome.calculatedTax != null) {
                // СведДох6.1
                if (ndflPersonIncome.taxRate != 13) {
                    if ((ndflPersonIncome.calculatedTax ?: 0) != ScriptUtils.round(((ndflPersonIncome.taxBase ?: 0) * (ndflPersonIncome.taxRate ?: 0))/100, 0)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно произведению значений гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\") с округлением до целого числа",
                                C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                C_TAX_BASE, ndflPersonIncome.taxBase ?: 0,
                                "Процентная ставка", (ndflPersonIncome.taxRate ?: 0)
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_16, fioAndInp, pathError, errMsg)
                    }
                }
                // СведДох6.2
                if (ndflPersonIncome.taxRate == 13 && ndflPersonIncome.incomeCode != "1010" && ndflPerson.status != "6") {
                    /*
                    S1 - сумма значений по "Графе 13" (taxBase)
                    Для суммирования строк по "Графе 13" (taxBase) должны быть соблюдены ВСЕ следующие условия:
                    1. Суммирование значений должно осуществляться для каждого ФЛ по отдельности
                    2. Для суммирования значений должны учитывать только те строки, в которых "Графа 6" (incomeAccruedDate) <= "Графы 6" для текущей строки (МЕНЬШЕ ИЛИ РАВНО)
                    3. Значение "Графы 10" (incomeAccruedSumm) != 0
                    4. Значение "Графы 6" должно >= даты начала отчетного периода и <= даты окончания отчетного периода
                    5. Значение "Графы 14" (taxRate) = 13
                    6. Значение "Графы 4" (incomeCode) != "1010"
                     */
                    List<NdflPersonIncome> ndflPersonIncomeCurrentList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: []
                    List<NdflPersonIncome> S1List = ndflPersonIncomeCurrentList.findAll {
                        it.incomeAccruedDate <= ndflPersonIncome.incomeAccruedDate &&
                        it.incomeAccruedSumm != null && it.incomeAccruedSumm != 0 &&
                                ndflPersonIncome.incomeAccruedDate >= getReportPeriodStartDate() && ndflPersonIncome.incomeAccruedDate <= getReportPeriodEndDate() &&
                                it.taxRate == 13 && it.incomeCode != "1010"
                    } ?: []
                    BigDecimal S1 = S1List.sum { it.taxBase ?: 0 } ?: 0
                    /*
                    S2 - сумма значений по "Графе 16" (calculatedTax)
                    Для суммирования строк по "Графе 16" (calculatedTax) должны быть соблюдены ВСЕ следующие условия:
                    1. Суммирование значений должно осуществляться для каждого ФЛ по отдельности
                    2. Для суммирования значений должны учитывать только те строки, в которых "Графа 6" (incomeAccruedDate) < "Графы 6" для текущей строки (МЕНЬШЕ)
                    2. Значение "Графы 6" должно >= даты начала отчетного периода и <= даты окончания отчетного периода
                    3. Значение "Графы 14" (taxRate) = 13
                    4. Значение "Графы 4" (incomeCode) != "1010"
                     */
                    List<NdflPersonIncome> S2List = S2List = ndflPersonIncomeCurrentList.findAll {
                        it.incomeAccruedDate < ndflPersonIncome.incomeAccruedDate &&
                        ndflPersonIncome.incomeAccruedDate >= getReportPeriodStartDate() && ndflPersonIncome.incomeAccruedDate <= getReportPeriodEndDate() &&
                                it.taxRate == 13 && it.incomeCode != "1010"
                    } ?: []
                    BigDecimal S2 = S2List.sum { it.calculatedTax ?: 0 } ?: 0
                    // Сумма по «Графа 16» текущей операции = S1 x 13% - S2
                    if (ndflPersonIncome.calculatedTax != ScriptUtils.round((S1 * 0.13 - S2), 0)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно выражению: Сумма значений гр. \"%s\" с начала периода на отчетную дату х 13%% - сумма значений гр. \"%s\" за предыдущие отчетные периоды",
                                C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                C_TAX_BASE,
                                C_CALCULATED_TAX
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_16, fioAndInp, pathError, errMsg)
                    }
                }
                // СведДох6.3
                if (ndflPersonIncome.taxRate == 13 && ndflPerson.status == "6") {
                    List<NdflPersonPrepayment> ndflPersonPrepaymentListByBersonIdList = ndflPersonPrepaymentCache.get(ndflPersonIncome.ndflPersonId) ?: []
                    if (!ndflPersonPrepaymentListByBersonIdList.isEmpty()) {
                        List<NdflPersonPrepayment> ndflPersonPrepaymentCurrentList = ndflPersonPrepaymentListByBersonIdList.findAll {
                            it.operationId == ndflPersonIncome.operationId
                        } ?: []
                        Long ndflPersonPrepaymentSum = ndflPersonPrepaymentCurrentList.sum { it.summ } ?: 0
                        if (!(ndflPersonIncome.calculatedTax ==
                                ScriptUtils.round(((ndflPersonIncome.taxBase ?: 0) * 0.13 - ndflPersonPrepaymentSum ?: 0), 0))
                        ) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно выражению: гр. \"%s\" (\"%s\") х 13%% - \"%s\" (\"%s\")",
                                    C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                    C_TAX_BASE, ndflPersonIncome.taxBase ?: 0,
                                    "Сумма фиксированного авансового платежа", ndflPersonPrepaymentSum
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.warnExp("%s. %s.", LOG_TYPE_2_16, fioAndInp, pathError, errMsg)
                        }
                    }
                }
            }

            // СведДох7 НДФЛ.Расчет.Сумма.Удержанный (Графа 17)
            if (ndflPersonIncome.withholdingTax != null && ndflPersonIncome.withholdingTax != 0) {
                // СведДох7.1
                if ((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "13")
                        || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543", "1544",
                             "1545", "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "02")
                        && (ndflPersonIncome.overholdingTax == null || ndflPersonIncome.overholdingTax == 0)
                ) {
                    // «Графа 17 Раздел 2» = «Графа 16 Раздел 2» = «Графа 24 Раздел 2»
                    if (!(ndflPersonIncome.withholdingTax == ndflPersonIncome.calculatedTax
                            && (ndflPersonIncome.withholdingTax == ndflPersonIncome.taxSumm ?: 0))) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значениям гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")",
                                C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                C_TAX_SUMM, ndflPersonIncome.taxSumm ?: 0
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_17, fioAndInp, pathError, errMsg)
                    }
                } else if (((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "13")
                        || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1543", "1544",
                             "1545", "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType != "02"))
                        && ndflPersonIncome.overholdingTax > 0
                ) {
                    // «Графа 17 Раздел 2» = («Графа 16 Раздел 2» + «Графа 16 Раздел 2» предыдущей записи) = «Графа 24 Раздел 2» и «Графа 17 Раздел 2» <= ((«Графа 13 Раздел 2» - «Графа 16 Раздел 2») × 50%)
                    List<NdflPersonIncome> ndflPersonIncomeCurrentList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: []
                    NdflPersonIncome ndflPersonIncomePreview = null
                    if (!ndflPersonIncomeCurrentList.isEmpty()) {
                        ndflPersonIncomePreview = ndflPersonIncomeCurrentList.find {
                            it.incomeAccruedDate <= ndflPersonIncome.incomeAccruedDate &&
                                    (ndflPersonIncomePreview == null || ndflPersonIncomePreview.incomeAccruedDate < it.incomeAccruedDate)
                        }
                    }
                    if (!(ndflPersonIncome.withholdingTax == (ndflPersonIncome.calculatedTax ?: 0) + (ndflPersonIncomePreview.calculatedTax ?: 0))) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно сумме значений гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\") предыдущей записи",
                                C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0,
                                C_CALCULATED_TAX, ndflPersonIncomePreview.calculatedTax ?: 0
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_17, fioAndInp, pathError, errMsg)
                    }
                    if (!(ndflPersonIncome.withholdingTax == (ndflPersonIncome.taxSumm ?: 0))) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\")",
                                C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                C_TAX_SUMM, ndflPersonIncome.taxSumm ?: 0
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_17, fioAndInp, pathError, errMsg)
                    }
                    if (!(ndflPersonIncome.withholdingTax <= (ScriptUtils.round(ndflPersonIncome.taxBase ?: 0, 0) - ndflPersonIncome.calculatedTax ?: 0) * 0.50)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не должно превышать 50%% от разности значение гр. \"%s\" (\"%s\") и гр. \"%s\" (\"%s\")",
                                C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                C_TAX_BASE, ndflPersonIncome.taxBase ?: 0,
                                C_CALCULATED_TAX, ndflPersonIncome.calculatedTax ?: 0
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_17, fioAndInp, pathError, errMsg)
                    }
                } else if ((["2520", "2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "14")
                        || (["1530", "1531", "1532", "1533", "1535", "1536", "1537", "1539", "1541", "1542", "1544", "1545",
                             "1546", "1547", "1548", "1549", "1551", "1552", "1554"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "02")
                ) {
                    if (!(ndflPersonIncome.withholdingTax == 0 || ndflPersonIncome.withholdingTax == null)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно \"0\"",
                                C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_17, fioAndInp, pathError, errMsg)
                    }
                } else if (!(ndflPersonIncome.incomeCode != null)) {
                    if (!(ndflPersonIncome.withholdingTax != ndflPersonIncome.taxSumm ?: 0)) {
                        // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                        String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\")",
                                C_WITHHOLDING_TAX, ndflPersonIncome.withholdingTax ?: 0,
                                C_TAX_SUMM, ndflPersonIncome.taxSumm ?: 0
                        )
                        String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                        logger.warnExp("%s. %s.", LOG_TYPE_2_17, fioAndInp, pathError, errMsg)
                    }
                }
            }

            List<NdflPersonIncome> ndflPersonIncomeCurrentByPersonIdList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: []
            List<NdflPersonIncome> ndflPersonIncomeCurrentByPersonIdAndOperationIdList = ndflPersonIncomeCurrentByPersonIdList.findAll {
                it.operationId == ndflPersonIncome.operationId
            } ?: []
            // "Сумма Граф 16"
            Long calculatedTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum {
                it.calculatedTax ?: 0
            } ?: 0
            // "Сумма Граф 17"
            Long withholdingTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum {
                it.withholdingTax ?: 0
            } ?: 0
            // "Сумма Граф 18"
            Long notHoldingTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum {
                it.notHoldingTax ?: 0
            } ?: 0
            // "Сумма Граф 19"
            Long overholdingTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum {
                it.overholdingTax ?: 0
            } ?: 0
            // "Сумма Граф 20"
            Long refoundTaxSum = ndflPersonIncomeCurrentByPersonIdAndOperationIdList.sum { it.refoundTax ?: 0 } ?: 0

            // СведДох8 НДФЛ.Расчет.Сумма.Не удержанный (Графа 18)
            if (ndflPersonIncome.notHoldingTax != null && calculatedTaxSum > withholdingTaxSum) {
                if (!(notHoldingTaxSum == calculatedTaxSum - withholdingTaxSum)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String errMsg = String.format("Сумма значений гр. \"%s\" (\"%s\") должна быть равна разнице сумм значений гр.\"%s\" (\"%s\") и гр.\"%s\" (\"%s\") для всех строк одной операции",
                            C_NOT_HOLDING_TAX, notHoldingTaxSum ?: "0",
                            C_CALCULATED_TAX, calculatedTaxSum ?: "0",
                            C_WITHHOLDING_TAX, withholdingTaxSum ?: "0"
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_2_18, fioAndInp, pathError, errMsg)
                }
            }

            // СведДох9 НДФЛ.Расчет.Сумма.Излишне удержанный (Графа 19)
            if (ndflPersonIncome.overholdingTax != null && calculatedTaxSum < withholdingTaxSum) {
                if (!(overholdingTaxSum == withholdingTaxSum - calculatedTaxSum)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String errMsg = String.format("Сумма значений гр. \"%s\" (\"%s\") должна быть равна разнице сумм значений гр.\"%s\" (\"%s\") и гр.\"%s\" (\"%s\") для всех строк одной операции",
                            C_OVERHOLDING_TAX, overholdingTaxSum ?: "0",
                            C_WITHHOLDING_TAX, withholdingTaxSum ?: "0",
                            C_CALCULATED_TAX, calculatedTaxSum ?: "0"
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_2_19, fioAndInp, pathError, errMsg)
                }
            }

            // СведДох10 НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику (Графа 20)
            if (ndflPersonIncome.refoundTax != null && ndflPersonIncome.refoundTax > 0) {
                if (!(refoundTaxSum <= overholdingTaxSum)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String errMsg = String.format("Сумма значений гр. \"%s\" (\"%s\") не должна превышать сумму значений гр.\"%s\" (\"%s\") для всех строк одной операции",
                            C_REFOUND_TAX, refoundTaxSum ?: "0",
                            C_OVERHOLDING_TAX, overholdingTaxSum ?: "0"
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_2_20, fioAndInp, pathError, errMsg)
                }
            }

            // СведДох11 НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма (Графа 24)
            // Заменил проверку заполненности 2.24, на проверку заполненности 2.21
            if (ndflPersonIncome.taxTransferDate != null) {
                dateConditionDataListForBudget.each { dateConditionData ->
                    if (dateConditionData.incomeCodes.contains(ndflPersonIncome.incomeCode) && dateConditionData.incomeTypes.contains(ndflPersonIncome.incomeType)) {
                        // Все подпункты, кроме 11-го
                        if (!dateConditionData.checker.check(ndflPersonIncome, dateConditionWorkDay)) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            String errMsg = String.format(dateConditionData.conditionMessage,
                                    C_TAX_TRANSFER_DATE, ndflPersonIncome.taxTransferDate ? ndflPersonIncome.taxTransferDate.format(DATE_FORMAT): "",
                                    C_INCOME_PAYOUT_DATE, ndflPersonIncome.incomePayoutDate ? ndflPersonIncome.incomePayoutDate.format(DATE_FORMAT): ""
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.warnExp("%s. %s.", LOG_TYPE_2_21, fioAndInp, pathError, errMsg)
                        }
                    }
                }
                if (["2720", "2740", "2750", "2790", "4800"].contains(ndflPersonIncome.incomeCode) && ndflPersonIncome.incomeType == "14") {
                    // 11 подпункт "Графа 21" = "Графа 7" + "1 рабочий день"
                    /*
                        Найти следующую за текущей строкой, удовлетворяющую условиям:
                        "Графа 10" > "0"
                        "Графа 5" не равно "02"
                        "Графа 5"не равно "14"
                        "Графа 7" является минимальной из "Граф 7", удовлетворяющих условию: ("Графа 7" (следующей строки) >= "Графа 7" (текущей строки))
                        "Графа 7" <= "31.12.20**" + "1 календарный день", где 31.12.20** - последний день текущего года
                         */

                    // Получим 1-ый рабочий день следующего года
                    Calendar firstWorkingDay = Calendar.getInstance()
                    firstWorkingDay.setTime(getReportPeriodStartDate())
                    firstWorkingDay.set(Calendar.DAY_OF_YEAR, firstWorkingDay.getActualMaximum(Calendar.DAY_OF_YEAR))
                    firstWorkingDay.add(Calendar.DATE, 1)
                    if (firstWorkingDay.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                        firstWorkingDay.add(Calendar.DATE, 2);
                    }
                    if (firstWorkingDay.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        firstWorkingDay.add(Calendar.DATE, 1);
                    }

                    // Найдем следующую за текущей строку в РНУ
                    List<NdflPersonIncome> ndflPersonIncomeCurrentList = ndflPersonIncomeCache.get(ndflPersonIncome.ndflPersonId) ?: []
                    NdflPersonIncome ndflPersonIncomeFind = null;
                    ndflPersonIncomeCurrentList.each {
                        if ((it.incomeAccruedSumm ?: 0 > 0) && !["02", "14"].contains(it.incomeType)
                                && (ndflPersonIncomeFind == null || ndflPersonIncomeFind.incomePayoutDate > it.incomePayoutDate)
                                && ndflPersonIncome.incomePayoutDate <= it.incomePayoutDate
                                && ndflPersonIncome.operationId < it.operationId) {
                            if (it.incomePayoutDate.before(firstWorkingDay.getTime()) || it.incomePayoutDate.equals(firstWorkingDay.getTime())) {
                                ndflPersonIncomeFind = it
                            }
                        }
                    }
                    if (ndflPersonIncomeFind != null) {
                        Column21EqualsColumn7Plus1WorkingDay column7Plus1WorkingDay = new Column21EqualsColumn7Plus1WorkingDay()
                        if (!column7Plus1WorkingDay.check(ndflPersonIncomeFind, dateConditionWorkDay)) {
                            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                            String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно значению гр. \"%s\" (\"%s\") + 1 рабочий день",
                                    C_TAX_TRANSFER_DATE, ndflPersonIncome.taxTransferDate ? ndflPersonIncome.taxTransferDate.format(DATE_FORMAT): "",
                                    C_INCOME_PAYOUT_DATE, ndflPersonIncome.incomePayoutDate ? ndflPersonIncome.incomePayoutDate.format(DATE_FORMAT): ""
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.warnExp("%s. %s.", LOG_TYPE_2_21, fioAndInp, pathError, errMsg)
                        }
                    } else {
                        // ToDo https://jira.aplana.com/browse/SBRFNDFL-1448
                        if (false) {
                            String errMsg = String.format("Значение гр. \"%s\" (\"%s\") должно быть равно \"00.00.0000\"",
                                    C_TAX_TRANSFER_DATE, ndflPersonIncome.taxTransferDate ? ndflPersonIncome.taxTransferDate.format(DATE_FORMAT) : ""
                            )
                            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                            logger.warnExp("%s. %s.", LOG_TYPE_2_21, fioAndInp, pathError, errMsg)
                        }
                    }
                }
            }

            //СведДох12	 Отсутствие нулевых значений
            LOG_TYPE_NOT_ZERO_CHECK: {
                if (ndflPersonIncome.incomeAccruedSumm != null && ScriptUtils.isEmpty(ndflPersonIncome.incomeAccruedSumm)) {
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не может быть равно \"0\"",
                            C_INCOME_ACCRUED_SUMM, ndflPersonIncome.incomeAccruedSumm
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_NOT_ZERO, fioAndInp, pathError, errMsg)
                }
                if (ndflPersonIncome.incomePayoutSumm != null && ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) {
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не может быть равно \"0\"",
                            C_INCOME_PAYOUT_SUMM, ndflPersonIncome.incomePayoutSumm
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_NOT_ZERO, fioAndInp, pathError, errMsg)
                }
                if (ndflPersonIncome.taxRate != null && ScriptUtils.isEmpty(ndflPersonIncome.taxRate)) {
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не может быть равно \"0\"",
                            C_TAX_RATE, ndflPersonIncome.taxRate
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_NOT_ZERO, fioAndInp, pathError, errMsg)
                }
                if (ndflPersonIncome.taxSumm != null && ScriptUtils.isEmpty(ndflPersonIncome.taxSumm)) {
                    String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не может быть равно \"0\"",
                            C_TAX_SUMM, ndflPersonIncome.taxSumm
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_INCOME, ndflPersonIncome.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_NOT_ZERO, fioAndInp, pathError, errMsg)
                }
            }
        }
    }
    logForDebug("Проверки сведений о доходах (" + (System.currentTimeMillis() - time) + " мс)");
}

/**
 * Проверяется заполненность согласно временному решению
 * Если сумма начисленного дохода равна сумме вычетов, будет ноль в графах:
 Раздел 2. Графа 13. Налоговая база
 Раздел 2. Графа 16. Сумма исчисленного налога
 Раздел 2. Графа 17. Сумма удержанного налога
 * @param checkingValue
 * @param incomeAccruedSum
 * @param totalDeductionSum
 * @return true - заполнен, false - не заполнен
 */
boolean isPresentedByTempSolution(BigDecimal checkingValue, BigDecimal incomeAccruedSum, BigDecimal totalDeductionSum) {
    if (checkingValue == null) {
        return false
    }
    if (incomeAccruedSum != totalDeductionSum && checkingValue == new BigDecimal(0)) {
        return false
    }
    return true
}

/**
 * Класс для проверки заполненности полей
 */
@CompileStatic
class ColumnFillConditionData {
    ColumnFillConditionChecker columnConditionCheckerAsIs
    ColumnFillConditionChecker columnConditionCheckerToBe
    String conditionPath
    String conditionMessage

    ColumnFillConditionData(ColumnFillConditionChecker columnConditionCheckerAsIs, ColumnFillConditionChecker columnConditionCheckerToBe, String conditionPath, String conditionMessage) {
        this.columnConditionCheckerAsIs = columnConditionCheckerAsIs
        this.columnConditionCheckerToBe = columnConditionCheckerToBe
        this.conditionPath = conditionPath
        this.conditionMessage = conditionMessage
    }
}

interface ColumnFillConditionChecker {
    boolean check(NdflPersonIncome ndflPersonIncome)
}
/**
 * Проверка: "Раздел 2. Графа 4,5 заполнены"
 */
@TypeChecked
class Column4Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.incomeCode)
    }
}
/**
 * Проверка: "Раздел 2. Графа 4,5 заполнены"
 */
@TypeChecked
class Column5Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.incomeType)
    }
}
/**
 * Проверка: "Раздел 2. Графа 6 заполнена"
 */
@TypeChecked
class Column6Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.incomeAccruedDate != null
    }
}
/**
 * Проверка: "Раздел 2. Графа 7 заполнена"
 */
@TypeChecked
class Column7Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.incomePayoutDate != null
    }
}
/**
 * Проверка: "Раздел 2. Графа 8 заполнена"
 */
@TypeChecked
class Column8Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.oktmo != null
    }
}
/**
 * Проверка: "Раздел 2. Графа 9 заполнена"
 */
@TypeChecked
class Column9Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.kpp != null
    }
}
/**
 * Проверка: "Раздел 2. Графа 10 заполнена"
 */
@TypeChecked
class Column10Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.incomeAccruedSumm != null
    }
}

/**
 * Проверка: "Раздел 2. Графа 11 заполнена"
 */
@TypeChecked
class Column11Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.incomePayoutSumm != null
    }
}
/**
 * Проверка: "Раздел 2. Графы 7, 11 заполнены"
 */
@TypeChecked
class Column7And11Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.incomePayoutDate != null && ndflPersonIncome.incomePayoutSumm != null
    }
}
/**
 * Проверка: "Раздел 2. Графа 12 НЕ заполнена"
 */
@TypeChecked
class Column12NotFill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ScriptUtils.isEmpty(ndflPersonIncome.totalDeductionsSumm)
    }
}
/**
 * Проверка: "Раздел 2. Графы 13 заполнены"
 */
@TypeChecked
class Column13Fill implements ColumnFillConditionChecker {
    boolean temporalySolution

    Column13Fill() {
        temporalySolution = false
    }

    Column13Fill(boolean temporalySolution) {
        this.temporalySolution = temporalySolution
    }

    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        if (temporalySolution) {
            return ndflPersonIncome.taxBase != null
        }
        return !ScriptUtils.isEmpty(ndflPersonIncome.taxBase)
    }
}
/**
 * Проверка: "Раздел 2. Графы  14 заполнены"
 */
@TypeChecked
class Column14Fill implements ColumnFillConditionChecker {
    boolean temporalySolution

    Column14Fill() {
        temporalySolution = false
    }

    Column14Fill(boolean temporalySolution) {
        this.temporalySolution = temporalySolution
    }

    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        if (temporalySolution) {
            return ndflPersonIncome.taxRate != null
        }
        return ndflPersonIncome.taxRate != null
    }
}
/**
 * Проверка: "Раздел 2. Графы 15 заполнены"
 */
@TypeChecked
class Column15Fill implements ColumnFillConditionChecker {
    boolean temporalySolution

    Column15Fill() {
        temporalySolution = false
    }

    Column15Fill(boolean temporalySolution) {
        this.temporalySolution = temporalySolution
    }

    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        if (temporalySolution) {
            return ndflPersonIncome.taxDate != null
        }
        return ndflPersonIncome.taxDate != null
    }
}/**
 * Проверка: "Раздел 2. Графы 16 заполнена"
 */
@TypeChecked
class Column16Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.calculatedTax)
    }
}
/**
 * Проверка: "Раздел 2. Графы 17 заполнена"
 */
@TypeChecked
class Column17Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.withholdingTax)
    }
}
/**
 * Проверка: "Раздел 2. Графа 18 заполнена"
 */
@TypeChecked
class Column18Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.notHoldingTax)
    }
}
/**
 * Проверка: "Раздел 2. Графа 19 заполнена"
 */
@TypeChecked
class Column19Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.overholdingTax)
    }
}
/**
 * Проверка: "Раздел 2. Графы 20 заполнена"
 */
@TypeChecked
class Column20Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !ScriptUtils.isEmpty(ndflPersonIncome.refoundTax)
    }
}
/**
 * Проверка: "Раздел 2. Графы 21 заполнена"
 */
@TypeChecked
class Column21Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.taxTransferDate != null
    }
}
/**
 * Проверка: "Раздел 2. Графы 21 НЕ заполнена"
 */
@TypeChecked
class Column21NotFill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.taxTransferDate == null
    }
}
/**
 * Проверка: "Раздел 2. Графы 7, 11 ИЛИ 22, 23, 24 заполнены"
 */
@TypeChecked
class Column7And11Or22And23And24Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return (ndflPersonIncome.incomePayoutDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.incomePayoutSumm)) ||
                (ndflPersonIncome.paymentDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.paymentNumber) && !ScriptUtils.isEmpty(ndflPersonIncome.taxSumm))
    }
}
/**
 * Проверка: "Раздел 2. Графы 7, 11 И 22, 23, 24 НЕ заполнены"
 */
@TypeChecked
class Column7And11And22And23And24NotFill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return !(new Column7And11Fill().check(ndflPersonIncome)) && (new Column22And23And24NotFill().check(ndflPersonIncome))
    }
}
/**
 * Проверка: "Раздел 2. Графы 22, 23, 24 НЕ заполнены"
 */
@TypeChecked
class Column22And23And24NotFill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.paymentDate == null && ScriptUtils.isEmpty(ndflPersonIncome.paymentNumber) && ndflPersonIncome.taxSumm == null
    }
}
/**
 * Проверка: "Раздел 2. Графы 22, 23, 24 заполнены"
 */
@TypeChecked
class Column22And23And24Fill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return ndflPersonIncome.paymentDate != null && !ScriptUtils.isEmpty(ndflPersonIncome.paymentNumber) && ndflPersonIncome.taxSumm != null
    }
}
/**
 * 	Должны быть либо заполнены все 3 Графы 22, 23, 24, либо ни одна из них
 */
@TypeChecked
class Column22And23And24FillOrColumn22And23And24NotFill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return new Column22And23And24NotFill().check(ndflPersonIncome) || new Column22And23And24Fill().check(ndflPersonIncome)
    }
}
/**
 * 	Всегда возвращает true
 */
@TypeChecked
class ColumnTrueFillOrNotFill implements ColumnFillConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome) {
        return true
    }
}

// Проверка на принадлежность операций периоду при загрузке ТФ
@TypeChecked
boolean operationNotRelateToCurrentPeriod(Date incomeAccruedDate, Date incomePayoutDate, Date taxDate,
                                          String kpp, String oktmo, String inp, String fio, NdflPersonIncome ndflPersonIncome) {
    // Доход.Дата.Начисление
    boolean incomeAccruedDateOk = dateRelateToCurrentPeriod("Файл/ИнфЧасть/СведОпер/СведДохНал/ДатаДохНач", incomeAccruedDate, kpp, oktmo, inp, fio, ndflPersonIncome)
    // Доход.Дата.Выплата
    boolean incomePayoutDateOk = dateRelateToCurrentPeriod("Файл/ИнфЧасть/СведОпер/СведДохНал/ДатаДохВыпл", incomePayoutDate, kpp, oktmo, inp, fio, ndflPersonIncome)
    // НДФЛ.Расчет.Дата
    boolean taxDateOk = dateRelateToCurrentPeriod("Файл/ИнфЧасть/СведОпер/СведДохНал/ДатаНалог", taxDate, kpp, oktmo, inp, fio, ndflPersonIncome)
    if (incomeAccruedDateOk && incomePayoutDateOk && taxDateOk) {
        return false
    }
    return true
}

@TypeChecked
boolean dateRelateToCurrentPeriod(
        def paramName, Date date, String kpp, String oktmo, String inp, String fio, NdflPersonIncome ndflPersonIncome) {
    //https://jira.aplana.com/browse/SBRFNDFL-581 замена getReportPeriodCalendarStartDate() на getReportPeriodStartDate
    if (date == null || (date >= getReportPeriodStartDate() && date <= getReportPeriodEndDate())) {
        return true
    }
    logger.warn("У параметра ТФ $paramName недопустимое значение: ${date ? date.format(DATE_FORMAT): ""}: дата операции не входит в отчетный период ТФ. " +
            "КПП = $kpp, " +
            "ОКТМО = $oktmo, " +
            "ФЛ ИНП = $inp, " +
            "ФИО = $fio, " +
            "ИдОперации = ${ndflPersonIncome.operationId}, " +
            "Номер строки = ${ndflPersonIncome.rowNum}.")
    return false
}

/**
 * Возвращает "Сумму применения вычета в текущем периоде"
 * @param ndflPersonIncome
 * @param ndflPersonDeductionList
 * @return
 */
@TypeChecked
BigDecimal getDeductionSumForIncome(NdflPersonIncome ndflPersonIncome, List<NdflPersonDeduction> ndflPersonDeductionList) {
    BigDecimal sumNdflDeduction = new BigDecimal(0)
    for (NdflPersonDeduction ndflPersonDeduction in ndflPersonDeductionList) {
        if (ndflPersonIncome.operationId == ndflPersonDeduction.operationId
                && ndflPersonIncome.incomeAccruedDate?.format(DATE_FORMAT) == ndflPersonDeduction.incomeAccrued?.format(DATE_FORMAT)
                && ndflPersonIncome.ndflPersonId == ndflPersonDeduction.ndflPersonId) {
            sumNdflDeduction += ndflPersonDeduction.periodCurrSumm ?: 0
        }
    }
    return sumNdflDeduction
}

/**
 * Класс для получения рабочих дней
 */
@TypeChecked
class DateConditionWorkDay {

    // Мапа рабочих дней со сдвигом
    private Map<Date, Date> workDayWithOffset0Cache
    private Map<Date, Date> workDayWithOffset1Cache
    private Map<Date, Date> workDayWithOffset30Cache
    CalendarService calendarService

    DateConditionWorkDay(CalendarService calendarService) {
        workDayWithOffset0Cache = [:]
        workDayWithOffset1Cache = [:]
        workDayWithOffset30Cache = [:]
        this.calendarService = calendarService
    }

    /**
     * Возвращает дату рабочего дня, смещенного относительно даты startDate.
     *
     * @param startDate начальная дата, может быть и рабочим днем и выходным
     * @param offset на сколько рабочих дней необходимо сдвинуть начальную дату. Может быть меньше 0, тогда сдвигается в обратную сторону
     * @return смещенная на offset рабочих дней дата
     */
    Date getWorkDay(Date startDate, int offset) {
        Date resultDate
        if (offset == 0) {
            resultDate = workDayWithOffset0Cache.get(startDate)
            if (resultDate == null) {
                resultDate = calendarService.getWorkDay(startDate, offset)
                workDayWithOffset0Cache.put(startDate, resultDate)
            }
        } else if (offset == 1) {
            resultDate = workDayWithOffset1Cache.get(startDate)
            if (resultDate == null) {
                resultDate = calendarService.getWorkDay(startDate, offset)
                workDayWithOffset1Cache.put(startDate, resultDate)
            }
        } else if (offset == 30) {
            resultDate = workDayWithOffset30Cache.get(startDate)
            if (resultDate == null) {
                resultDate = calendarService.getWorkDay(startDate, offset)
                workDayWithOffset30Cache.put(startDate, resultDate)
            }
        }
        return resultDate
    }
}

/**
 * Класс для соотнесения вида проверки в зависимости от значений "Код вида дохода" и "Признак вида дохода"
 */
@TypeChecked
class DateConditionData {
    List<String> incomeCodes
    List<String> incomeTypes
    DateConditionChecker checker
    String conditionMessage

    DateConditionData(List<String> incomeCodes, List<String> incomeTypes, DateConditionChecker checker, String conditionMessage) {
        this.incomeCodes = incomeCodes
        this.incomeTypes = incomeTypes
        this.checker = checker
        this.conditionMessage = conditionMessage
    }
}

interface DateConditionChecker {
    boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay)
}

/**
 * Проверка: "Графа 6" = "Графе 7"
 */
@TypeChecked
class Column6EqualsColumn7 implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
        String accrued = ndflPersonIncome.incomeAccruedDate?.format("dd.MM.yyyy")
        String payout = ndflPersonIncome.incomePayoutDate?.format("dd.MM.yyyy")
        return accrued == payout
    }
}

/**
 * Проверка: Соответствия маске
 */
@TypeChecked
class MatchMask implements DateConditionChecker {
    String maskRegex

    MatchMask(String maskRegex) {
        this.maskRegex = maskRegex
    }

    @Override
    boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
        if (ndflPersonIncome.incomeAccruedDate == null) {
            return false
        }
        String accrued = ndflPersonIncome.incomeAccruedDate.format("dd.MM.yyyy")
        Pattern pattern = Pattern.compile(maskRegex)
        Matcher matcher = pattern.matcher(accrued)
        if (matcher.matches()) {
            return true
        }
        return false
    }
}

/**
 * Проверка "Последний календарный день месяца"
 */
@TypeChecked
class LastMonthCalendarDay implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
        if (ndflPersonIncome.incomeAccruedDate == null) {
            return true
        }
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(ndflPersonIncome.incomeAccruedDate)
        int currentMonth = calendar.get(Calendar.MONTH)
        calendar.add(calendar.DATE, 1)
        int comparedMonth = calendar.get(Calendar.MONTH)
        return currentMonth != comparedMonth
    }
}

/**
 * Проверка: Если «графа 7» < 31.12.20**, то «графа 6» = «графа 7»
 */
@TypeChecked
class Column7LastDayOfYear1 implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
        if (ndflPersonIncome.incomePayoutDate == null) {
            return false
        }
        Calendar calendarPayout = Calendar.getInstance()
        calendarPayout.setTime(ndflPersonIncome.incomePayoutDate)
        int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
        int month = calendarPayout.get(Calendar.MONTH)
        if (dayOfMonth != 31 || month != 11) {
            return new Column6EqualsColumn7().check(ndflPersonIncome, dateConditionWorkDay)
        } else {
            return true
        }
    }
}

/**
 * Проверка: Если «графа 7» < 31.12.20**, то «графа 6» = 31.12.20**
 */
@TypeChecked
class Column7LastDayOfYear2 implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
        if (ndflPersonIncome.incomePayoutDate == null) {
            return false
        }
        Calendar calendarPayout = Calendar.getInstance()
        calendarPayout.setTime(ndflPersonIncome.incomePayoutDate)
        int dayOfMonth = calendarPayout.get(Calendar.DAY_OF_MONTH)
        int month = calendarPayout.get(Calendar.MONTH)
        if (dayOfMonth != 31 || month != 11) {
            return true
        } else {
            return new MatchMask("31.12.20\\d{2}").check(ndflPersonIncome, dateConditionWorkDay)
        }
    }
}

/**
 * Проверка: Доход.Дата.Начисление (Графа 6) последний календарный день месяца (если последний день месяца приходится на выходной, то следующий первый рабочий день)
 */
@TypeChecked
class LastMonthWorkDayIncomeAccruedDate implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
        if (ndflPersonIncome.incomeAccruedDate == null) {
            return false
        }
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(ndflPersonIncome.incomeAccruedDate)
        // находим последний день месяца
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        Date workDay = calendar.getTime()
        // если последний день месяца приходится на выходной, то следующий первый рабочий день
        int offset = 0
        workDay = dateConditionWorkDay.getWorkDay(workDay, offset)
        return workDay.getTime() == ndflPersonIncome.incomeAccruedDate.getTime()
    }
}

/**
 * Проверка: "Графа 21" = "Графа 7" + "1 рабочий день"
 */
@TypeChecked
class Column21EqualsColumn7Plus1WorkingDay implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
        if (ndflPersonIncome.taxTransferDate == null || ndflPersonIncome.incomePayoutDate == null) {
            return false
        }
        Calendar calendar21 = Calendar.getInstance();
        calendar21.setTime(ndflPersonIncome.taxTransferDate);

        // "Графа 7" + "1 рабочий день"
        int offset = 1
        Date workDay = dateConditionWorkDay.getWorkDay(ndflPersonIncome.incomePayoutDate, offset)
        Calendar calendar7 = Calendar.getInstance();
        calendar7.setTime(workDay);

        return calendar21.equals(calendar7);
    }
}

/**
 * Проверка: "Графа 21" <= "Графа 7" + "30 календарных дней", если "Графа 7" + "30 календарных дней" - выходной день, то "Графа 21" <= "Следующий рабочий день" после "Графа 7" + "30 календарных дней"
 */
@TypeChecked
class Column21EqualsColumn7Plus30WorkingDays implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
        if (ndflPersonIncome.taxTransferDate == null || ndflPersonIncome.incomePayoutDate == null) {
            return false
        }
        Calendar calendar21 = Calendar.getInstance();
        calendar21.setTime(ndflPersonIncome.taxTransferDate);

        // "Следующий рабочий день" после "Графа 7" + "30 календарных дней"
        int offset = 30
        Date workDay = dateConditionWorkDay.getWorkDay(ndflPersonIncome.incomePayoutDate, offset)
        Calendar calendar7 = Calendar.getInstance();
        calendar7.setTime(workDay);

        return calendar21.before(calendar7) || calendar21.equals(calendar7);
    }
}

/**
 * "Графа 21" = Последний календарный день месяца для месяца "Графы 7", если Последний календарный день месяца - выходной день, то "Графа 21" = следующий рабочий день
 */
@TypeChecked
class Column21EqualsColumn7LastDayOfMonth implements DateConditionChecker {
    @Override
    boolean check(NdflPersonIncome ndflPersonIncome, DateConditionWorkDay dateConditionWorkDay) {
        if (ndflPersonIncome.taxTransferDate == null || ndflPersonIncome.incomePayoutDate == null) {
            return false
        }
        Calendar calendar21 = Calendar.getInstance();
        calendar21.setTime(ndflPersonIncome.taxTransferDate);

        Calendar calendar7 = Calendar.getInstance();
        calendar7.setTime(ndflPersonIncome.incomePayoutDate);

        // находим последний день месяца
        calendar7.set(Calendar.DAY_OF_MONTH, calendar7.getActualMaximum(Calendar.DAY_OF_MONTH))
        Date workDay = calendar7.getTime()
        // если последний день месяца приходится на выходной, то следующий первый рабочий день
        int offset = 0
        workDay = dateConditionWorkDay.getWorkDay(workDay, offset)
        calendar7.setTime(workDay);

        return calendar21.equals(calendar7);
    }
}

/**
 * Проверки Сведения о вычетах
 * @param ndflPersonList
 * @param ndflPersonIncomeList
 * @param ndflPersonDeductionList
 */
def checkDataDeduction(List<NdflPerson> ndflPersonList, List<NdflPersonIncome> ndflPersonIncomeList,
                       List<NdflPersonDeduction> ndflPersonDeductionList, Map<Long, Map<String, RefBookValue>> personMap) {
    long time = System.currentTimeMillis()

    for (NdflPerson ndflPerson : ndflPersonList) {
        NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPerson.id)
        if (ndflPersonFL == null) {
            if (FORM_DATA_KIND.equals(FormDataKind.PRIMARY)) {
                // РНУ-НДФЛ первичная
                String fio = (ndflPerson.lastName?:"") + " " + (ndflPerson.firstName?:"") + " " + (ndflPerson.middleName ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.inp)
            } else {
                // РНУ-НДФЛ консолидированная
                def personRecord = personMap.get(ndflPerson.recordId)
                String fio = (personRecord.get(RF_LAST_NAME).value?:"") + " " + (personRecord.get(RF_FIRST_NAME).value?:"") + " " + (personRecord.get(RF_MIDDLE_NAME).value ?: "")
                ndflPersonFL = new NdflPersonFL(fio, ndflPerson.recordId.toString())
            }
            ndflPersonFLMap.put(ndflPerson.id, ndflPersonFL)
        }
    }

    def mapNdflPersonIncome = [:]
    for (NdflPersonIncome ndflPersonIncome : ndflPersonIncomeList) {
        String operationIdNdflPersonId = "${ndflPersonIncome.operationId}_${ndflPersonIncome.ndflPersonId}"
        if (!mapNdflPersonIncome.containsKey(operationIdNdflPersonId)) {
            mapNdflPersonIncome.put(operationIdNdflPersonId, [:])
        }
        mapNdflPersonIncome.get(operationIdNdflPersonId).put(ndflPersonIncome.incomeAccruedDate ? formatDate(ndflPersonIncome.incomeAccruedDate): "", ndflPersonIncome)
    }

    for (NdflPersonDeduction ndflPersonDeduction : ndflPersonDeductionList) {

        ScriptUtils.checkInterrupted();

        NdflPersonFL ndflPersonFL = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)
        String fioAndInp = sprintf(TEMPLATE_PERSON_FL, [ndflPersonFL.fio, ndflPersonFL.inp])

        // Выч14 Документ о праве на налоговый вычет.Код источника (Графа 7)
        if (ndflPersonDeduction.notifType == "1" && ndflPersonDeduction.notifSource != "0000") {
            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
            String errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует значению гр. \"%s\" (\"%s\")",
                    C_NOTIF_SOURCE, ndflPersonDeduction.notifSource ?: "",
                    C_TYPE_CODE, ndflPersonDeduction.typeCode ?:""
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
            logger.warnExp("%s. %s.", LOG_TYPE_3_7, fioAndInp, pathError, errMsg)
        }

        // Выч15 (Графы 9)
        // Выч16 (Графы 10)
        String operationIdNdflPersonIdDate = "${ndflPersonDeduction.operationId}_${ndflPersonDeduction.ndflPersonId}"
        Map<String, NdflPersonIncome> mapNdflPersonIncomeDate = mapNdflPersonIncome.get(operationIdNdflPersonIdDate)
        if (mapNdflPersonIncomeDate == null) {
            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
            String errMsg = String.format("Для гр. \"%s\" (\"%s\") отсутствуют операция или физическое лицо в разделе 2",
                    C_INCOME_ACCRUED, ndflPersonDeduction.incomeAccrued ? formatDate(ndflPersonDeduction.incomeAccrued) : ""
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
            logger.warnExp("%s. %s.", LOG_TYPE_3_10, fioAndInp, pathError, errMsg)
        } else {
            NdflPersonIncome ndflPersonIncome = mapNdflPersonIncomeDate.get(ndflPersonDeduction.incomeAccrued ? formatDate(ndflPersonDeduction.incomeAccrued) : "")
            if (ndflPersonIncome == null) {
                // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                ndflPersonIncome = mapNdflPersonIncomeDate.values().find{
                    it.incomeAccruedDate != null
                }
                String errMsg
                if (ndflPersonIncome != null) {
                    errMsg = String.format("Значение гр. \"%s\" (\"%s\") не соответствует значению гр. \"%s\" (\"%s\")",
                            C_INCOME_ACCRUED, ndflPersonDeduction.incomeAccrued ? formatDate(ndflPersonDeduction.incomeAccrued) : "",
                            C_INCOME_ACCRUED_DATE, formatDate(ndflPersonIncome.incomeAccruedDate)
                    )
                } else {
                    errMsg = String.format("Для гр. \"%s\" (\"%s\") не найдено заполненных гр. \"%s\" Раздела 2",
                            C_INCOME_ACCRUED, ndflPersonDeduction.incomeAccrued ? formatDate(ndflPersonDeduction.incomeAccrued) : "",
                            C_INCOME_ACCRUED_DATE
                    )
                }
                String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                logger.warnExp("%s. %s.", LOG_TYPE_3_10, fioAndInp, pathError, errMsg)
            } else {
                // Выч17 Начисленный доход.Код дохода (Графы 11)
                if (ndflPersonDeduction.incomeCode != ndflPersonIncome.incomeCode) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String errMsg = String.format("Обнаружены расхождения между значением гр. \"%s\", к которому был применен вычет (\"%s\"), указанным в Разделе 2, и значением гр. \"%s\" (\"%s\"), указанным в Разделе 3",
                            C_INCOME_CODE, ndflPersonIncome.incomeCode ?: "",
                            C_INCOME_ACCRUED_CODE, ndflPersonDeduction.incomeCode ?: ""
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_3_11, fioAndInp, pathError, errMsg)
                }


                // Выч18 Начисленный доход.Сумма (Графы 12)
                if (!comparNumbEquals(ndflPersonDeduction.incomeSumm, ndflPersonIncome.incomeAccruedSumm)) {
                    // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
                    String errMsg = String.format("Обнаружены расхождения между значением гр. \"%s\", к которому был применен вычет (\"%s\"), указанным в Разделе 2, и значением гр. \"%s\" (\"%s\"), указанным в Разделе 3",
                            C_INCOME_ACCRUED_SUMM, ndflPersonIncome.incomeAccruedSumm ?: "",
                            C_INCOME_ACCRUED_P_SUMM, ndflPersonDeduction.incomeSumm ?: ""
                    )
                    String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
                    logger.warnExp("%s. %s.", LOG_TYPE_3_12, fioAndInp, pathError, errMsg)
                }
            }
        }

        // Выч20 Применение вычета.Текущий период.Дата (Графы 15)
        if (ndflPersonDeduction.periodCurrDate != ndflPersonDeduction.incomeAccrued) {
            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
            String errMsg = String.format("Значение гр. \"%s\" (\"%s\")\" не соответствует значению гр. \"%s\" (\"%s\")",
                    C_PERIOD_CURR_DATE, ndflPersonDeduction.periodCurrDate ? formatDate(ndflPersonDeduction.periodCurrDate): "",
                    C_INCOME_ACCRUED, ndflPersonDeduction.incomeAccrued ? formatDate(ndflPersonDeduction.incomeAccrued): ""
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
            logger.warnExp("%s. %s.", LOG_TYPE_3_10_2, fioAndInp, pathError, errMsg)
        }

        // Выч21 Документ о праве на налоговый вычет.Сумма (Графы 16) (Графы 8)
        if (comparNumbGreater(ndflPersonDeduction.periodCurrSumm ?: 0, ndflPersonDeduction.notifSumm ?: 0)) {
            // todo turn_to_error https://jira.aplana.com/browse/SBRFNDFL-637
            String errMsg = String.format("Значение гр. \"%s\" (%s) не должно превышать значение гр. \"%s\" (%s) согласно подтверждающему документу",
                    C_PERIOD_CURR_SUMM, ndflPersonDeduction.periodCurrSumm ?: "",
                    C_NOTIF_SUMM, ndflPersonDeduction.notifSumm ?:""
            )
            String pathError = String.format(SECTION_LINE_MSG, T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum ?: "")
            logger.warnExp("%s. %s.", LOG_TYPE_3_16, fioAndInp, pathError, errMsg)
        }
    }
    logForDebug("Проверки сведений о вычетах (" + (System.currentTimeMillis() - time) + " мс)");
}

/**
 * Сравнение чисел с плавающей точкой через эпсилон-окрестности
 */
boolean comparNumbEquals(def d1, def d2) {
    if (d1 == null || d2 == null) return false
    return (Math.abs(d1 - d2) < 0.001)
}

boolean comparNumbGreater(double d1, double d2) {
    if (d1 == null || d2 == null) return false
    return (d1 - d2 > 0.001)
}

//>------------------< CHECK DATA UTILS >----------------------<

/**
 * Получить параметры для конкретного тербанка
 * @return
 */
def getRefBookNdfl() {
    def departmentId = declarationData.departmentId
    def departmentParamList = getProvider(REF_BOOK_NDFL_ID).getRecords(getReportPeriodEndDate(), null, "DEPARTMENT_ID = $departmentId", null)
    if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
        departmentParamException(departmentId, declarationData.reportPeriodId)
    }
    return departmentParamList?.get(0)
}

/**
 * Получить параметры подразделения
 * @param departmentParamId
 * @return
 */
def getRefBookNdflDetail(def departmentParamId) {
    def mapNdflDetail = [:]
    def filter = "REF_BOOK_NDFL_ID = $departmentParamId"
    def departmentParamTableList = getProvider(REF_BOOK_NDFL_DETAIL_ID).getRecords(getReportPeriodEndDate(), null, filter, null)
    if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
        departmentParamException(declarationData.departmentId, declarationData.reportPeriodId)
    }
    def kppList = []
    def mapOktmo = getRefOktmoByDepartmentId()
    departmentParamTableList.each { departmentParamTable ->

        String oktmoCode = mapOktmo.get(departmentParamTable?.OKTMO?.referenceValue)?.CODE?.stringValue

        kppList = mapNdflDetail.get(oktmoCode)
        if (kppList == null) {
            kppList = []
        }

        if (!kppList.contains(departmentParamTable?.KPP?.stringValue)) {
            kppList.add(departmentParamTable?.KPP?.stringValue)
            mapNdflDetail.put(oktmoCode, kppList)
        }
    }
    return mapNdflDetail
}

/**
 * Получить "ОКТМО"
 */
def getRefOktmoByDepartmentId() {
    String whereClause = """
        JOIN REF_BOOK_NDFL_DETAIL nd ON (frb.id = nd.OKTMO)
        JOIN REF_BOOK_NDFL n ON (n.ID = nd.ref_book_ndfl_id)
            where n.department_id = ${declarationData.departmentId}
                   and nd.ref_book_ndfl_id = n.ID
                   and exists(select 1 from t where t.record_id = frb.record_id and t.version = frb.version)
                   and frb.status = 0
"""
    return getRefBookByRecordVersionWhere(RefBook.Id.OKTMO.id, whereClause, getReportPeriodEndDate())
}

/**
 * Найти адресообразующий объект
 * @param regionCode код региона (обязательный параметр)
 * @param area район
 * @param city город
 * @param locality населенный пункт
 * @param street улица
 */
/**
 * Существует ли адрес в справочнике адресов
 */
@Memoized
boolean isExistsAddress(ndflPersonId) {
    Map<Long, FiasCheckInfo> checkFiasAddressMap = getFiasAddressIdsMap();
    return (checkFiasAddressMap.get(ndflPersonId) != null)
}

@TypeChecked
void checkCreate() {
    def departmentReportPeriod = departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId())
    if (departmentReportPeriod.correctionDate != null) {
        def prevDepartmentReportPeriod = departmentReportPeriodService.getFirst(declarationData.getDepartmentId(), declarationData.getReportPeriodId())
        def declarationList = declarationService.find(102, prevDepartmentReportPeriod.getId())
        declarationList.addAll(declarationService.find(103, prevDepartmentReportPeriod.getId()))
        declarationList.addAll(declarationService.find(104, prevDepartmentReportPeriod.getId()))
        if (declarationList.isEmpty()) {
            logger.warn("Отсутствуют отчетные налоговые формы в некорректировочном периоде, Отчетные налоговые формы не будут сформированы текущем периоде")
        }
    }
}

@TypeChecked
void departmentParamException(int departmentId, int reportPeriodId) {
    ReportPeriod reportPeriod = reportPeriodService.get(reportPeriodId)
    throw new ServiceException("Отсутствуют настройки подразделения \"%s\" периода \"%s\". Необходимо выполнить настройку в разделе меню \"Налоги->НДФЛ->Настройки подразделений\"",
            departmentService.get(departmentId).getName(),
            reportPeriod.getTaxPeriod().getYear() + ", " + reportPeriod.getName()
    ) as Throwable
}