package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
// com.aplana.sbrf.taxaccounting.refbook.* - используется для получения id-справочников
import com.aplana.sbrf.taxaccounting.refbook.*
import groovy.transform.Field
import groovy.util.slurpersupport.NodeChild
import groovy.xml.MarkupBuilder

import javax.xml.namespace.QName
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.Characters
import javax.xml.stream.events.XMLEvent

/**
 * Справочник "Коды, определяющие налоговый (отчётный) период"
 */
@Field
def PERIOD_CODE_REFBOOK = 8L;

switch (formDataEvent) {
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importData()
        break
    case FormDataEvent.CREATE_SPECIFIC_REPORT:
        createSpecificReport();
        break
    case FormDataEvent.CHECK:
        checkData();
        break
}

//------------------ Create Report ----------------------

def createSpecificReport() {

    //Проверка, подготовка данных
    def params = scriptSpecificReportHolder.subreportParamValues
    def reportParameters = scriptSpecificReportHolder.getSubreportParamValues();

    if (reportParameters.isEmpty()) {
        throw new ServiceException("Для поиска физического лица необходимо задать один из критериев.");
    }

    PagingResult<NdflPerson> pagingResult = ndflPersonService.findNdflPersonByParameters(declarationData.id, reportParameters);

    if (pagingResult.isEmpty()) {
        throw new ServiceException("По заданным параметрам ни одной записи не найдено: " + params);
    }

    if (pagingResult.isEmpty()) {
        throw new ServiceException("По заданным параметрам ни одной записи не найдено: " + params);
    }

    if (pagingResult.size() > 1) {
        pagingResult.getRecords().each() { ndflPerson ->
            StringBuilder sb = new StringBuilder();
            sb.append("[").append("Фамилия: ").append(ndflPerson.lastName).append("],");
            sb.append("[").append("Имя: ").append(ndflPerson.firstName).append("],");
            sb.append("[").append("Отчество: ").append(ndflPerson.middleName).append("],");
            sb.append("[").append("СНИЛС: ").append(ndflPerson.snils).append("],");
            sb.append("[").append("ИНН: ").append(ndflPerson.innNp).append("],");
            sb.append("[").append("Дата рождения: ").append(formatDate(ndflPerson.birthDay)).append("],");
            sb.append("[").append("ДУЛ: ").append(ndflPerson.idDocNumber).append("],");
            logger.info(sb.toString())
        }
        throw new ServiceException("Найдено " + pagingResult.getTotalCount() + " записей. Отображено записей " + pagingResult.size() + ". Уточните критерии поиска.");
    }

    //формирование отчета
    def jasperPrint = declarationService.createJasperReport(scriptSpecificReportHolder.getFileInputStream(), params, {
        calculateReportData(it, pagingResult.get(0))
    });

    declarationService.exportPDF(jasperPrint, scriptSpecificReportHolder.getFileOutputStream());
}

void calculateReportData(writer, ndflPerson) {

    //отчетный период
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    //Идентификатор подразделения
    def departmentId = declarationData.departmentId

    //Подразделение
    def department = departmentService.get(departmentId)

    def reportPeriodCode = findReportPeriodCode(reportPeriod)

    def builder = new MarkupBuilder(writer)
    builder.Файл() {

        СлЧасть('КодПодр': department.sbrfCode) {}
        ИнфЧасть('ПериодОтч': reportPeriodCode, 'ОтчетГод': reportPeriod?.taxPeriod?.year) {
            ПолучДох(ndflPersonAttr(ndflPerson)) {
                def incomes = ndflPerson.incomes.sort { a, b -> (a.rowNum <=> b.rowNum) }
                def deductions = ndflPerson.deductions.sort { a, b -> a.rowNum <=> b.rowNum }
                def prepayments = ndflPerson.prepayments.sort { a, b -> a.rowNum <=> b.rowNum }

                def operationList = incomes.collectEntries { personIncome ->
                    def key = personIncome.operationId
                    def value = ["ИдОпер": personIncome.operationId, "КПП": personIncome.kpp, "ОКТМО": personIncome.oktmo]
                    return [key, value]
                }

                def incomeOperations = mapToOperationId(incomes);
                def deductionOperations = mapToOperationId(deductions);
                def prepaymentOperations = mapToOperationId(prepayments);

                operationList.each { key, value ->
                    СведОпер(value) {
                        //доходы
                        incomeOperations.get(key).each { personIncome ->
                            СведДохНал(incomeAttr(personIncome)) {}
                        }

                        //Вычеты
                        deductionOperations.get(key).each { personDeduction ->
                            СведВыч(deductionAttr(personDeduction)) {}
                        }

                        //Авансовые платежи
                        prepaymentOperations.get(key).each { personPrepayment ->
                            СведАванс(prepaymentAttr(personPrepayment)) {}
                        }
                    }
                }
            }
        }
    }
}

def mapToOperationId(collection) {
    def result = [:]
    collection.collectEntries(result) { personOperation ->
        def key = personOperation.operationId
        def value
        if (result.containsKey(key)) {
            value = result.get(key);
        } else {
            value = [].toList()
        }
        value.add(personOperation)
        return [key, value]
    }
    return result;
}


def formatDate(date) {
    return ScriptUtils.formatDate(date, "dd.MM.yyyy")
}

Date parseDate(xmlDate) {
    return new java.text.SimpleDateFormat('dd.MM.yyyy').parse(xmlDate.text())
}

/**
 * Получить полное название подразделения по id подразделения.
 */
def getDepartmentFullName(def id) {
    return departmentService.getParentsHierarchy(id)
}

/**
 * Находит код периода из справочника "Коды, определяющие налоговый (отчётный) период"
 */
def findReportPeriodCode(reportPeriod) {
    RefBookDataProvider dataProvider = refBookFactory.getDataProvider(PERIOD_CODE_REFBOOK)
    Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData(reportPeriod.getDictTaxPeriodId());
    if (refBookValueMap.isEmpty()) {
        throw new ServiceException("Некорректные данные в справочнике \"Коды, определяющие налоговый (отчётный) период\"");
    }
    return refBookValueMap.get("CODE").getStringValue();
}

//------------------Import Data ----------------------

void importData() {

    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }

    //Каждый элемент ИнфЧасть содержит данные об одном физ лице, максимальное число элементов в документе 15000
    QName infoPartName = QName.valueOf('ИнфЧасть')

    //Используем StAX парсер для импорта
    def xmlFactory = XMLInputFactory.newInstance()
    xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE)
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE)
    def reader = xmlFactory.createXMLEventReader(ImportInputStream)


    def sb;
    try {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent()

            if (event.isCharacters() && ((Characters) event).isWhiteSpace()) {
                continue;
            }

            if (!event.isStartElement() && !event.isEndElement()) {
                continue;
            }

            //Последовательно обрабатываем все элементы ИнфЧасть в документе
            if (event.isStartElement() && event.getName().equals(infoPartName)) {
                sb = new StringBuilder()
            }

            sb?.append(event.toString())

            if (event.isEndElement() && event.getName().equals(infoPartName)) {
                def infoPart = new XmlSlurper().parseText(sb.toString())
                processInfoPart(infoPart)
            }
        }
    } finally {
        reader?.close()
    }
}

void processInfoPart(infoPart) {

    def ndflPersonNode = infoPart.'ПолучДох'[0]

    NdflPerson ndflPerson = transformNdflPersonNode(ndflPersonNode)

    def ndflPersonOperations = infoPart.'СведОпер'
    ndflPersonOperations.each {
        processNdflPersonOperation(ndflPerson, it)
    }

    //Идентификатор декларации для которой загружаются данные
    ndflPerson.declarationDataId = declarationData.getId()
    ndflPersonService.save(ndflPerson)
}

void processNdflPersonOperation(NdflPerson ndflPerson, NodeChild ndflPersonOperationsNode) {

    ndflPerson.incomes = ndflPersonOperationsNode.'СведДохНал'.collect {
        transformNdflPersonIncome(it)
    }

    ndflPerson.deductions = ndflPersonOperationsNode.'СведВыч'.collect {
        transformNdflPersonDeduction(it)
    }

    ndflPerson.prepayments = ndflPersonOperationsNode.'СведАванс'.collect {
        transformNdflPersonPrepayment(it)
    }

}

// ----------------- Data -----------------------

NdflPerson transformNdflPersonNode(NodeChild node) {
    NdflPerson ndflPerson = new NdflPerson()
    //uses some Groovy magic
    ndflPerson.inp = node.'@ИНП'
    ndflPerson.snils = node.'@СНИЛС'
    ndflPerson.lastName = node.'@ФамФЛ'
    ndflPerson.firstName = node.'@ИмяФЛ'
    ndflPerson.middleName = node.'@ОтчФЛ'
    ndflPerson.birthDay = parseDate(node.'@ДатаРожд')
    ndflPerson.citizenship = node.'@Гражд'
    ndflPerson.innNp = node.'@ИННФЛ'
    ndflPerson.innForeign = node.'@ИННИно'
    ndflPerson.idDocType = node.'@УдЛичнФЛКод'
    ndflPerson.idDocNumber = node.'@УдЛичнФЛНом'
    ndflPerson.status = node.'@СтатусФЛ'
    ndflPerson.postIndex = node.'@Индекс'
    ndflPerson.regionCode = node.'@КодРегион'
    ndflPerson.area = node.'@Район'
    ndflPerson.city = node.'@Город'
    ndflPerson.locality = node.'@НаселПункт'
    ndflPerson.street = node.'@Улица'
    ndflPerson.house = node.'@Дом'
    ndflPerson.building = node.'@Корпус'
    ndflPerson.flat = node.'@Кварт'
    ndflPerson.countryCode = node.'@КодСтрИно'
    ndflPerson.address = node.'@АдресИно'
    ndflPerson.additionalData = node.'@ДопИнф'
    return ndflPerson
}

NdflPersonIncome transformNdflPersonIncome(NodeChild node) {
    def operationNode = node.parent();
    NdflPersonIncome personIncome = new NdflPersonIncome()
    personIncome.rowNum = node.'@НомСтр'.toInteger()
    personIncome.incomeCode = node.'@КодДох'
    personIncome.incomeType = node.'@ТипДох'

    personIncome.operationId = operationNode.'@ИдОпер'.toBigDecimal()
    personIncome.oktmo = operationNode.'@ОКТМО'
    personIncome.kpp = operationNode.'@КПП'

    personIncome.incomeAccruedDate = parseDate(node.'@ДатаДохНач')
    personIncome.incomePayoutDate = parseDate(node.'@ДатаДохВыпл')
    personIncome.incomeAccruedSumm = node.'@СуммДохНач'.toBigDecimal()
    personIncome.incomePayoutSumm = node.'@СуммДохВыпл'.toBigDecimal()
    personIncome.totalDeductionsSumm = node.'@СумВыч'.toBigDecimal()
    personIncome.taxBase = node.'@НалБаза'.toBigDecimal()
    personIncome.taxRate = node.'@Ставка'.toInteger()
    personIncome.taxDate = parseDate(node.'@ДатаНалог')
    personIncome.calculatedTax = node.'@НИ'.toInteger()
    personIncome.withholdingTax = node.'@НУ'.toInteger()
    personIncome.notHoldingTax = node.'@ДолгНП'.toInteger()
    personIncome.overholdingTax = node.'@ДолгНА'.toInteger()
    personIncome.refoundTax = node.'@ВозврНал'.toInteger()
    personIncome.taxTransferDate = parseDate(node.'@СрокПрчслНал')
    personIncome.paymentDate = parseDate(node.'@ПлПоручДат')
    personIncome.paymentNumber = node.'@ПлатПоручНом'
    personIncome.taxSumm = node.'@НалПерСумм'.toInteger()
    return personIncome
}

NdflPersonDeduction transformNdflPersonDeduction(NodeChild node) {

    NdflPersonDeduction personDeduction = new NdflPersonDeduction()
    personDeduction.rowNum = node.'@НомСтр'.toInteger()
    personDeduction.operationId = node.parent().'@ИдОпер'.toBigDecimal()
    personDeduction.typeCode = node.'@ВычетКод'
    personDeduction.notifType = node.'@УведТип'
    personDeduction.notifDate = parseDate(node.'@УведДата')
    personDeduction.notifNum = node.'@УведНом'
    personDeduction.notifSource = node.'@УведИФНС'
    personDeduction.notifSumm = node.'@УведСум'.toBigDecimal()
    personDeduction.incomeAccrued = parseDate(node.'@ДатаДохНач')
    personDeduction.incomeCode = node.'@КодДох'
    personDeduction.incomeSumm = node.'@СуммДохНач'.toBigDecimal()
    personDeduction.periodPrevDate = parseDate(node.'@ДатаПредВыч')
    personDeduction.periodPrevSumm = node.'@СумПредВыч'.toBigDecimal()
    personDeduction.periodCurrDate = parseDate(node.'@ДатаТекВыч')
    personDeduction.periodCurrSumm = node.'@СумТекВыч'.toBigDecimal()
    return personDeduction
}

NdflPersonPrepayment transformNdflPersonPrepayment(NodeChild node) {

    NdflPersonPrepayment personPrepayment = new NdflPersonPrepayment();
    personPrepayment.rowNum = node.'@НомСтр'.toInteger()
    personPrepayment.operationId = node.parent().'@ИдОпер'.toBigDecimal()
    personPrepayment.summ = node.'@Аванс'.toBigDecimal()
    personPrepayment.notifNum = node.'@УведНом'
    personPrepayment.notifDate = parseDate(node.'@УведДата')
    personPrepayment.notifSource = node.'@УведИФНС'

    return personPrepayment;
}

def ndflPersonAttr(ndflPerson) {
    [
            'ИНП'        : ndflPerson.inp,
            'СНИЛС'      : ndflPerson.snils,
            'ФамФЛ'      : ndflPerson.lastName,
            'ИмяФЛ'      : ndflPerson.firstName,
            'ОтчФЛ'      : ndflPerson.middleName,
            'ДатаРожд'   : formatDate(ndflPerson.birthDay),
            'Гражд'      : ndflPerson.citizenship,
            'ИННФЛ'      : ndflPerson.innNp,
            'ИННИно'     : ndflPerson.innForeign,
            'УдЛичнФЛКод': ndflPerson.idDocType,
            'УдЛичнФЛНом': ndflPerson.idDocNumber,
            'СтатусФЛ'   : ndflPerson.status,
            'Индекс'     : ndflPerson.postIndex,
            'КодРегион'  : ndflPerson.regionCode,
            'Район'      : ndflPerson.area,
            'Город'      : ndflPerson.city,
            'НаселПункт' : ndflPerson.locality,
            'Улица'      : ndflPerson.street,
            'Дом'        : ndflPerson.house,
            'Корпус'     : ndflPerson.building,
            'Кварт'      : ndflPerson.flat,
            'КодСтрИно'  : ndflPerson.countryCode,
            'АдресИно'   : ndflPerson.address,
            'ДопИнф'     : ndflPerson.additionalData]


}

def incomeAttr(personIncome) {
    [
            'НомСтр'      : personIncome.rowNum,
            'КодДох'      : personIncome.incomeCode,
            'ТипДох'      : personIncome.incomeType,

            'ИдОпер'      : personIncome.operationId,//toBigDecimal()
            'ОКТМО'       : personIncome.oktmo,
            'КПП'         : personIncome.kpp,

            'ДатаДохНач'  : formatDate(personIncome.incomeAccruedDate),
            'ДатаДохВыпл' : formatDate(personIncome.incomePayoutDate),
            'СуммДохНач'  : personIncome.incomeAccruedSumm,//toBigDecimal()
            'СуммДохВыпл' : personIncome.incomePayoutSumm,//toBigDecimal()
            'СумВыч'      : personIncome.totalDeductionsSumm,//toBigDecimal()
            'НалБаза'     : personIncome.taxBase,//toBigDecimal()
            'Ставка'      : personIncome.taxRate,//toInteger()
            'ДатаНалог'   : formatDate(personIncome.taxDate),
            'НИ'          : personIncome.calculatedTax,//toInteger()
            'НУ'          : personIncome.withholdingTax,//toInteger()
            'ДолгНП'      : personIncome.notHoldingTax,//toInteger()
            'ДолгНА'      : personIncome.overholdingTax,//toInteger()
            'ВозврНал'    : personIncome.refoundTax,//toInteger()
            'СрокПрчслНал': formatDate(personIncome.taxTransferDate),
            'ПлПоручДат'  : formatDate(personIncome.paymentDate),
            'ПлатПоручНом': personIncome.paymentNumber,
            'НалПерСумм'  : personIncome.taxSumm,//toInteger()
    ]
}

def deductionAttr(personDeduction) {
    [
            'НомСтр'     : personDeduction.rowNum,//toInteger()
            'ИдОпер'     : personDeduction.operationId,//toBigDecimal()
            'ВычетКод'   : personDeduction.typeCode,
            'УведТип'    : personDeduction.notifType,
            'УведДата'   : formatDate(personDeduction.notifDate),
            'УведНом'    : personDeduction.notifNum,
            'УведИФНС'   : personDeduction.notifSource,
            'УведСум'    : personDeduction.notifSumm,//toBigDecimal()
            'ДатаДохНач' : formatDate(personDeduction.incomeAccrued),
            'КодДох'     : personDeduction.incomeCode,
            'СуммДохНач' : personDeduction.incomeSumm,//toBigDecimal()
            'ДатаПредВыч': formatDate(personDeduction.periodPrevDate),
            'СумПредВыч' : personDeduction.periodPrevSumm,//toBigDecimal()
            'ДатаТекВыч' : formatDate(personDeduction.periodCurrDate),
            'СумТекВыч'  : personDeduction.periodCurrSumm//toBigDecimal()
    ]
}

def prepaymentAttr(personPrepayment) {
    [
            "НомСтр"  : personPrepayment.rowNum,//.toInteger()
            "ИдОпер"  : personPrepayment.operationId,//toBigDecimal()
            "Аванс"   : personPrepayment.summ,//toBigDecimal()
            "УведНом" : personPrepayment.notifNum,
            "УведДата": formatDate(personPrepayment.notifDate),
            "УведИФНС": personPrepayment.notifSource
    ]
}

//------------------Check Data ----------------------
// Кэш провайдеров
@Field def providerCache = [:]

// Физические лица
@Field def personsCache = [:]
@Field final long REF_BOOK_PERSON_ID = RefBook.Id.PERSON.id

// Мапа <ID_Данные о физическом лице - получателе дохода, Физическое лицо: <ФИО> ИНП:<ИНП>>
@Field def ndflPersonFLMap = [:]
@Field final TEMPLATE_PERSON_FL = "Физическое лицо: \"%s\", ИНП: \"%s\""

// Страны Мапа <Идентификатор, Код>
@Field def citizenshipCache = [:]
@Field final long REF_BOOK_COUNTRY_ID = RefBook.Id.COUNTRY.id

// Виды документов, удостоверяющих личность Мапа <Идентификатор, Код>
@Field def documentCodesCache = [:]
@Field final long REF_BOOK_DOCUMENT_ID = RefBook.Id.DOCUMENT_CODES.id

// Статус налогоплательщика Мапа <Идентификатор, Код>
@Field def taxpayerStatusCache = [:]
@Field final long REF_BOOK_TAXPAYER_STATUS_ID = RefBook.Id.TAXPAYER_STATUS.id

// Адрес Мапа <Идентификатор, Адрес>
@Field def addressCache = [:]
@Field final long REF_BOOK_ADDRESS_ID = RefBook.Id.PERSON_ADDRESS.id

// ИНП Мапа <person_id, массив_инп>
@Field def inpCache = [:]
@Field final long REF_BOOK_ID_TAX_PAYER_ID = RefBook.Id.ID_TAX_PAYER.id

@Field def dulCache = [:]
@Field final long REF_BOOK_ID_DOC_ID = RefBook.Id.ID_DOC.id

// Коды видов доходов Мапа <Идентификатор, Код>
@Field def incomeCodeCache = [:]
@Field final long REF_BOOK_INCOME_CODE_ID = RefBook.Id.INCOME_CODE.id

// Виды дохода Мапа <Признак, Идентификатор_кода_вида_дохода>
@Field def incomeKindCache = [:]
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

// Дата окончания отчетного периода
@Field def reportPeriodEndDate = null

@Field final MESSAGE_ERROR_NOT_FOUND_REF = "Ошибка в значении: Раздел \"%s\". Строка \"%s\". Графа \"%s\". %s. Текст ошибки: \"%s\" не соответствует справочнику \"%s\"."
@Field final MESSAGE_ERROR_NOT_FOUND_PERSON = "Не удалось установить связь со справочником \"%s\" для Раздел: \"%s\". Строка: \"%s\", ИНП: \"%s\"."

@Field final SUCCESS_GET_REF_BOOK = "Получен справочник \"%s\" размером %d."
@Field final SUCCESS_GET_TABLE = "Получены записи таблицы \"%s\" в колличестве %d."

// Таблицы
@Field final T_PERSON = "Данные о физическом лице - получателе дохода"
@Field final T_PERSON_INCOME = "Сведения о доходах физического лица"
@Field final T_PERSON_DEDUCTION = "Стандартные, социальные и имущественные налоговые вычеты"
@Field final T_PERSON_PREPAYMENT = "Cведения о доходах в виде авансовых платежей"

// Справочники
@Field final R_PERSON = "Физические лица"
@Field final R_CITIZENSHIP = "ОК 025-2001 (Общероссийский классификатор стран мира)"
@Field final R_ID_DOC_TYPE = "Коды документов"
@Field final R_STATUS = "Статусы налогоплательщика"
@Field final R_INCOME_CODE = "Коды видов доходов"
@Field final R_INCOME_KIND = "Виды доходов"
@Field final R_RATE = "Ставки"
@Field final R_TYPE_CODE = "Коды видов вычетов"
@Field final R_NOTIF_SOURCE = "Коды налоговых органов"
@Field final R_ADDRESS = "Адреса"
@Field final R_INP = "Идентификаторы налогоплательщиков"
@Field final R_DUL = "Документы, удостоверяющий личность"

// Комментарии к полям
@Field final C_CITIZENSHIP = "Гражданство (код страны)"
@Field final C_ID_DOC_TYPE = "Код вида документа"
@Field final C_ID_DOC_NUMBER = "Серия и номер документа"
@Field final C_STATUS = "Статус"
@Field final C_INCOME_CODE = "Код вида дохода"
@Field final C_INCOME_KIND = "Вид дохода"
@Field final C_RATE = "Ставка"
@Field final C_TYPE_CODE = "Код вычета"
@Field final C_NOTIF_SOURCE = "Код налогового органа, выдавшего уведомление"
@Field final C_LAST_NAME = "Фамилия"
@Field final C_FIRST_NAME = "Имя"
@Field final C_MIDDLE_NAME = "Отчество"
@Field final C_INP = "Уникальный код клиента"
@Field final C_BIRTH_DATE = "Дата рождения"
@Field final C_INN_NP = "ИНН  физического лица"
@Field final C_INN_FOREIGN = "ИНН  иностранного гражданина"
@Field final C_REGION_CODE = "Код Региона"
@Field final C_AREA = "Район"
@Field final C_CITY = "Город"
@Field final C_LOCALITY = "Населенный пункт"
@Field final C_STREET = "Улица"
@Field final C_HOUSE = "Дом"
@Field final C_BUILDING = "Корпус"
@Field final C_FLAT = "Квартира"

// Поля справочника Физические лица
@Field final RF_LAST_NAME = "LAST_NAME"
@Field final RF_FIRST_NAME = "FIRST_NAME"
@Field final RF_MIDDLE_NAME = "MIDDLE_NAME"
@Field final RF_BIRTH_DATE = "BIRTH_DATE"
@Field final RF_CITIZENSHIP = "CITIZENSHIP"
@Field final RF_INN = "INN"
@Field final RF_INN_FOREIGN = "INN_FOREIGN"
@Field final RF_SNILS = "SNILS"
@Field final RF_TAXPAYER_STATE = "TAXPAYER_STATE"
@Field final RF_ADDRESS = "ADDRESS"
@Field final RF_RECORD_ID = "RECORD_ID"
@Field final RF_COUNTRY = "COUNTRY"
@Field final RF_REGION_CODE = "REGION_CODE"
@Field final RF_DISTRICT = "DISTRICT"
@Field final RF_CITY = "CITY"
@Field final RF_LOCALITY = "LOCALITY"
@Field final RF_STREET = "STREET"
@Field final RF_HOUSE = "HOUSE"
@Field final RF_BUILD = "BUILD"
@Field final RF_APPARTMENT = "APPARTMENT"
@Field final RF_DUBLICATES = "DUBLICATES"
@Field final RF_DOC_ID = "DOC_ID"
@Field final RF_DOC_NUMBER = "DOC_NUMBER"

/**
 * Проверки НДФЛ
 * @return
 */
def checkData() {
    // Проверки на соответствие справочникам
    checkDataReference()
}

/**
 * Проверки на соответствие справочникам
 * @return
 */
def checkDataReference() {
    // Страны
    def citizenshipCodeMap = getRefCitizenship()
    logger.info(SUCCESS_GET_REF_BOOK, R_CITIZENSHIP, citizenshipCodeMap.size())

    // Виды документов, удостоверяющих личность
    def documentTypeMap = getRefDocument()
    logger.info(SUCCESS_GET_REF_BOOK, R_ID_DOC_TYPE, documentTypeMap.size())

    // Статус налогоплательщика
    def taxpayerStatusMap = getRefTaxpayerStatus()
    logger.info(SUCCESS_GET_REF_BOOK, R_STATUS, taxpayerStatusMap.size())

    ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
    logger.info(SUCCESS_GET_TABLE, T_PERSON, ndflPersonList.size())

    // Физические лица
    def personIds = getPersonIds(ndflPersonList)
    def personMap = [:]

    // ИНП <person_id, массив_ИНП>
    def inpMap = [:]

    // ДУЛ <person_id, массив_ДУЛ>
    def dulMap = [:]

    // Адреса
    def addressIds = []
    def addressMap = [:]

    if (personIds.size() > 0) {
        // todo Сделать получение записей оригиналов, если текущая запись - дубликат
        personMap = getRefPersons(personIds)
        logger.info(SUCCESS_GET_TABLE, R_PERSON, personMap.size())

        // Получим мапу ИНП
        inpMap = getRefINP(personIds)
        logger.info(SUCCESS_GET_TABLE, R_INP, inpMap.size())

        // Получим мапу ДУЛ
        dulMap = getRefDul(personIds)
        logger.info(SUCCESS_GET_TABLE, R_DUL, dulMap.size())

        // Получим Мапу адресов
        personMap.each {personId, person ->
            // Сохраним идентификаторы адресов в коллекцию
            if (person.get(RF_ADDRESS).value != null) {
                addressIds.add(person.get(RF_ADDRESS).value)
            }
        }
        if (addressIds.size() > 0) {
            addressMap = getRefAddress(addressIds)
            logger.info(SUCCESS_GET_TABLE, R_ADDRESS, addressMap.size())
        }
    }

    ndflPersonList.each { ndflPerson ->
        def fio = ndflPerson.lastName + " " + ndflPerson.firstName + " " + ndflPerson.middleName ?: "";
        def fioAndInp = sprintf(TEMPLATE_PERSON_FL, [fio, ndflPerson.inp])
        ndflPersonFLMap.put(ndflPerson.id, fioAndInp)

        // Спр2 Гражданство (Обязательное поле)
        if (!citizenshipCodeMap.find{key, value -> value == ndflPerson.citizenship}) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON, ndflPerson.rowNum, C_CITIZENSHIP, fioAndInp, C_CITIZENSHIP, R_CITIZENSHIP);
        }

        // Спр3 Документ удостоверяющий личность (Обязательное поле)
         if (!documentTypeMap.find{key, value -> value == ndflPerson.idDocType}) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON, ndflPerson.rowNum, C_ID_DOC_TYPE, fioAndInp, C_ID_DOC_TYPE, R_ID_DOC_TYPE);
        }

        // Спр4 Статусы налогоплательщиков (Обязательное поле)
        if (!taxpayerStatusMap.find{key, value -> value == ndflPerson.status}) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON, ndflPerson.rowNum, C_STATUS, fioAndInp, C_STATUS, R_STATUS);
        }

        // Спр10 Наличие связи с "Физическое лицо"
        // todo Почему в объект помещается 0 - ndflPerson.personId == 0
        if (ndflPerson.personId == null || ndflPerson.personId == 0) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_PERSON,
                    R_PERSON, T_PERSON, fio, ndflPerson.inp);
        } else {
            def person = personMap.get(ndflPerson.personId)

            // Спр11 Фамилия (Обязательное поле)
            if (!ndflPerson.lastName.equals(person.get(RF_LAST_NAME).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_LAST_NAME, fioAndInp, C_LAST_NAME, R_PERSON);
            }

            // Спр11 Имя (Обязательное поле)
            if (!ndflPerson.firstName.equals(person.get(RF_FIRST_NAME).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_FIRST_NAME, fioAndInp, C_FIRST_NAME, R_PERSON);
            }

            // Спр11 Отчество (Необязательное поле)
            if (ndflPerson.middleName != null && !ndflPerson.middleName.equals(person.get(RF_MIDDLE_NAME).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_MIDDLE_NAME, fioAndInp, C_MIDDLE_NAME, R_PERSON);
            }

            // Спр12 ИНП первичная (Обязательное поле)
            def inpList = inpMap.get(ndflPerson.personId)
            if (!ndflPerson.inp.equals(person.get(RF_SNILS).value) && !inpList.contains(ndflPerson.inp)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_INP, fioAndInp, C_INP, R_INP);
            }

            // todo Спр12.1 ИНП консолидированная - Реализовать позже

            // Спр13 Дата рождения (Обязательное поле)
            if (!ndflPerson.birthDay.equals(person.get(RF_BIRTH_DATE).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_BIRTH_DATE, fioAndInp, C_BIRTH_DATE, R_PERSON);
            }

            // Спр14 Гражданство (Обязательное поле)
            def citizenship = citizenshipCodeMap.get(person.get(RF_CITIZENSHIP).value)
            if (!ndflPerson.citizenship.equals(citizenship)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_CITIZENSHIP, fioAndInp, C_CITIZENSHIP, R_PERSON);
            }

            // Спр15 ИНН (Необязательное поле)
            if (ndflPerson.innNp != null && !ndflPerson.innNp.equals(person.get(RF_INN).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_INN_NP, fioAndInp, C_INN_NP, R_PERSON);
            }

            // Спр16 ИНН в стране Гражданства (Необязательное поле)
            if (ndflPerson.innForeign != null && !ndflPerson.innForeign.equals(person.get(RF_INN_FOREIGN).value)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_INN_FOREIGN, fioAndInp, C_INN_FOREIGN, R_PERSON);
            }

            // Спр17 Документ удостоверяющий личность (Обязательное поле)
            def dulList = dulMap.get(ndflPerson.personId)
            // Вид документа
            def idDocTypeList = []
            // Серия и номер документа
            def idDocNumberList = []
            dulList.each { dul ->
                idDocTypeList.add(documentTypeMap.get(dul.get(RF_DOC_ID).value))
                idDocNumberList.add(dul.get(RF_DOC_NUMBER).value)
            }
            if (!idDocTypeList.contains(ndflPerson.idDocType)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_ID_DOC_TYPE, fioAndInp, C_ID_DOC_TYPE, R_PERSON);
            }
            if (!idDocNumberList.contains(ndflPerson.idDocNumber)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_ID_DOC_NUMBER, fioAndInp, C_ID_DOC_NUMBER, R_PERSON);
            }

            // todo Спр17.1 Документ удостоверяющий личность (Обязательное поле) - Реализовать позже

            // Спр18 Статус налогоплательщика (Обязательное поле)
            def taxpayerStatus = taxpayerStatusMap.get(person.get(RF_TAXPAYER_STATE).value)
            if (!ndflPerson.status.equals(taxpayerStatus)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_STATUS, fioAndInp, C_STATUS, R_PERSON);
            }

            // Спр19 Адрес (Необязательное поле)
            // Сравнение должны быть проведено даже с учетом пропусков
            def address = addressMap.get(person.get(RF_ADDRESS).value)
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

            // Код Региона
            if (!ndflPerson.regionCode.equals(regionCode)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_REGION_CODE, fioAndInp, C_REGION_CODE, R_PERSON);
            }

            // Район
            if (!ndflPerson.area.equals(area)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_AREA, fioAndInp, C_AREA, R_PERSON);
            }

            // Город
            if (!ndflPerson.city.equals(city)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_CITY, fioAndInp, C_CITY, R_PERSON);
            }

            // Населенный пункт
            if (!ndflPerson.locality.equals(locality)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_LOCALITY, fioAndInp, C_LOCALITY, R_PERSON);
            }

            // Улица
            if (!ndflPerson.street.equals(street)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_STREET, fioAndInp, C_STREET, R_PERSON);
            }

            // Дом
            if (!ndflPerson.house.equals(house)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_HOUSE, fioAndInp, C_HOUSE, R_PERSON);
            }

            // Корпус
            if (!ndflPerson.building.equals(building)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_BUILDING, fioAndInp, C_BUILDING, R_PERSON);
            }

            // Квартира
            if (!ndflPerson.flat.equals(flat)) {
                logger.warn(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON, ndflPerson.rowNum, C_FLAT, fioAndInp, C_FLAT, R_PERSON);
            }
        }
    }

    // Коды видов доходов Мапа <Идентификатор, Код>
    def incomeCodeMap = getRefIncomeCode()
    logger.info(SUCCESS_GET_REF_BOOK, R_INCOME_CODE, incomeCodeMap.size())

    // Виды доходов Мапа <Признак, Идентификатор_кода_вида_дохода>
    def incomeKindMap = getRefIncomeKind()
    logger.info(SUCCESS_GET_REF_BOOK, R_INCOME_KIND, incomeKindMap.size())

    // Ставки
    def rateList = getRefRate()
    logger.info(SUCCESS_GET_REF_BOOK, R_RATE, rateList.size())

    ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
    logger.info(SUCCESS_GET_TABLE, T_PERSON_INCOME, ndflPersonIncomeList.size())

    ndflPersonIncomeList.each { ndflPersonIncome ->
        def fioAndInp = ndflPersonFLMap.get(ndflPersonIncome.ndflPersonId)

        // Спр5 Код вида дохода (Необязательное поле)
        if (ndflPersonIncome.incomeCode != null && !incomeCodeMap.find{key, value -> value == ndflPersonIncome.incomeCode}) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_CODE, fioAndInp, C_INCOME_CODE, R_INCOME_CODE);
        }

        // Спр6 Вида дохода (Необязательное поле)
        // При проверке Вида дохода должно проверятся не только наличие признака дохода в справочнике, но и принадлежность признака к конкретному Коду вида дохода
        if (ndflPersonIncome.incomeType != null) {
            def idIncomeCode = incomeKindMap.get(ndflPersonIncome.incomeType)
            if (!idIncomeCode) {
                logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_KIND, fioAndInp, C_INCOME_KIND, R_INCOME_KIND);
            } else if (!incomeCodeMap.get(idIncomeCode.value)) {
                logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                        T_PERSON_INCOME, ndflPersonIncome.rowNum, C_INCOME_KIND, fioAndInp, C_INCOME_KIND, R_INCOME_KIND);
            }
        }

        // Спр7 Ставка (Необязательное поле)
        if (ndflPersonIncome.taxRate != null && !rateList.contains(ndflPersonIncome.taxRate)) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON_INCOME, ndflPersonIncome.rowNum, C_RATE, fioAndInp, C_RATE, R_RATE);
        }
    }

    // Коды видов вычетов
    def deductionTypeList = getRefDeductionType()
    logger.info(SUCCESS_GET_REF_BOOK, R_TYPE_CODE, deductionTypeList.size())

    // Коды налоговых органов
    def taxInspectionList = getRefNotifSource()
    logger.info(SUCCESS_GET_REF_BOOK, R_NOTIF_SOURCE, taxInspectionList.size())

    ndflPersonDeductionList = ndflPersonService.findNdflPersonDeduction(declarationData.id)
    logger.info(SUCCESS_GET_TABLE, T_PERSON_DEDUCTION, ndflPersonDeductionList.size())

    ndflPersonDeductionList.each { ndflPersonDeduction ->
        def fioAndInp = ndflPersonFLMap.get(ndflPersonDeduction.ndflPersonId)

        // Спр8 Код вычета (Обязательное поле)
        if (!deductionTypeList.contains(ndflPersonDeduction.typeCode)) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_TYPE_CODE, fioAndInp, C_TYPE_CODE, R_TYPE_CODE);
        }

        // Спр9 Код налоговой иснпекции (Обязательное поле)
        if (ndflPersonDeduction.notifSource != null && !taxInspectionList.contains(ndflPersonDeduction.notifSource)) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON_DEDUCTION, ndflPersonDeduction.rowNum, C_NOTIF_SOURCE, fioAndInp, C_NOTIF_SOURCE, R_NOTIF_SOURCE);
        }
    }

    ndflPersonPrepaymentList = ndflPersonService.findNdflPersonPrepayment(declarationData.id)
    logger.info(SUCCESS_GET_TABLE, T_PERSON_PREPAYMENT, ndflPersonPrepaymentList.size())

    ndflPersonPrepaymentList.each { ndflPersonPrepayment ->
        def fioAndInp = ndflPersonFLMap.get(ndflPersonPrepayment.ndflPersonId)

        // Спр9 Код налоговой иснпекции (Обязательное поле)
        if (ndflPersonPrepayment.notifSource != null && !taxInspectionList.contains(ndflPersonPrepayment.notifSource)) {
            logger.error(MESSAGE_ERROR_NOT_FOUND_REF,
                    T_PERSON_PREPAYMENT, ndflPersonPrepayment.rowNum, C_NOTIF_SOURCE, fioAndInp, C_NOTIF_SOURCE, R_NOTIF_SOURCE);
        }
    }
}

/**
 * Получить коллекцию идентификаторов записей справочника "Физические лица"
 * @param ndflPersonList
 * @return
 */
def getPersonIds(def ndflPersonList) {
    def personIds = []
    ndflPersonList.each { ndflPerson ->
        // todo Почему ndflPerson.personId != 0
        if (ndflPerson.personId != null && ndflPerson.personId != 0) {
            personIds.add(ndflPerson.personId)
        }
    }
    return personIds;
}

/**
 * Получить "Физические лица"
 * @param personIds
 * @return
 */
// todo добавить обработку дублей
def getRefPersons(def personIds) {
    if (personsCache.size() == 0) {
        def refBookMap = getRefBookByRecordIds(REF_BOOK_PERSON_ID, personIds)
//        def dublMap = [:]
//        def dublList = []
        refBookMap.each { personId, person ->
            personsCache.put(personId, person)

            // Добавим дубликат в Мапу
//            if (person.get(RF_DUBLICATES).value != null && !dublMap.get(person.get(RF_DUBLICATES).value)) {
//                dublMap.put(person.get(RF_DUBLICATES).value, personId)
//                dublList.add(person.get(RF_DUBLICATES).value)
//            }
        }
        // Получим оригинальные записи, если имеются дубликаты
//        if (dublMap.size() > 0) {
//            def refBookDublMap = getRefBookByRecordIds(REF_BOOK_PERSON_ID, dublList)
//            refBookDublMap.each { originalPersonId, person ->
//                dublPersonId = dublMap.get(originalPersonId).value
//            }
//        }
    }
    return personsCache;
}

/**
 * Получить "Страны"
 * @return
 */
def getRefCitizenship() {
    if (citizenshipCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_COUNTRY_ID)
        refBookMap.each { refBook ->
            citizenshipCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return citizenshipCache;
}

/**
 * Получить "Коды документов, удостоверяющих личность"
 * @return
 */
def getRefDocument() {
    if (documentCodesCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_DOCUMENT_ID)
        refBookList.each { refBook ->
            documentCodesCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return documentCodesCache;
}

/**
 * Получить "Статусы налогоплательщика"
 * @return
 */
def getRefTaxpayerStatus() {
    if (taxpayerStatusCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_TAXPAYER_STATUS_ID)
        refBookMap.each { refBook ->
            taxpayerStatusCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return taxpayerStatusCache;
}

/**
 * Получить "Статусы налогоплательщика"
 * @return
 */
def getRefAddress(def addressIds) {
    if (addressCache.size() == 0) {
        def refBookMap = getRefBookByRecordIds(REF_BOOK_ADDRESS_ID, addressIds)
        refBookMap.each { addressId, address ->
            addressCache.put(addressId, address)
        }
    }
    return addressCache;
}

/**
 * Получить "ИНП"
 * todo Получение ИНП реализовано путем отдельных запросов для каждого personId, в будущем переделать на использование одного запроса
 * @return
 */
def getRefINP(def personIds) {
    if (inpCache.size() == 0) {
        personIds.each { personId ->
            def refBookMap = getRefBookByFilter(REF_BOOK_ID_TAX_PAYER_ID, "PERSON_ID = " + personId.toString())
            def inpList = []
            refBookMap.each { refBook ->
                inpList.add(refBook?.INP?.stringValue)
            }
            inpCache.put(personId, inpList)
        }
    }
    return inpCache;
}

/**
 * Получить "Документ, удостоверяющий личность (ДУЛ)"
 * todo Получение ДУЛ реализовано путем отдельных запросов для каждого personId, в будущем переделать на использование одного запроса
 * @return
 */
def getRefDul(def personIds) {
    if (dulCache.size() == 0) {
        personIds.each { personId ->
            def refBookMap = getRefBookByFilter(REF_BOOK_ID_DOC_ID, "PERSON_ID = " + personId.toString())
            def dulList = []
            refBookMap.each { refBook ->
                dulList.add(refBook)
            }
            dulCache.put(personId, dulList)
        }
    }
    return dulCache;
}

/**
 * Получить "Коды видов доходов"
 * @return
 */
def getRefIncomeCode() {
    if (incomeCodeCache.size() == 0) {
        def refBookMap = getRefBook(REF_BOOK_INCOME_CODE_ID)
        refBookMap.each { refBook ->
            incomeCodeCache.put(refBook?.id?.numberValue, refBook?.CODE?.stringValue)
        }
    }
    return incomeCodeCache;
}

/**
 * Получить "Виды доходов"
 * @return
 */
def getRefIncomeKind() {
    if (incomeKindCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_INCOME_KIND_ID)
        refBookList.each { refBook ->
            // todo Так refBook?.INCOME_TYPE_ID?.numberValue не работает
            def incomeTypeId = refBook.find{key, value -> key == "INCOME_TYPE_ID"}.value
            incomeKindCache.put(refBook?.MARK?.stringValue, incomeTypeId)
        }
    }
    return incomeKindCache;
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
 * Получить все записи справочника по его идентификатору
 * @param refBookId - идентификатор справочника
 * @return - возвращает лист
 */
def getRefBook(def long refBookId) {
    // Передаем как аргумент только срок действия версии справочника
    def refBookList = getProvider(refBookId).getRecords(getReportPeriodEndDate() - 1, null, null, null)
    if (refBookList == null || refBookList.size() == 0) {
        throw new Exception("Ошибка при получении записей справочника " + refBookId)
    }
    return refBookList
}

/**
 * Получить все записи справочника по его идентификатору и фильтру (отсутствие значений не является ошибкой)
 * @param refBookId - идентификатор справочника
 * @param filter - фильтр
 * @return - возвращает лист
 */
def getRefBookByFilter(def long refBookId, def filter) {
    // Передаем как аргумент только срок действия версии справочника
    def refBookList = getProvider(refBookId).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
    return refBookList
}

/**
 * Получить все записи справочника по его идентификатору и коллекции идентификаторов записей справочника
 * @param refBookId - идентификатор справочника
 * @param recordIds - коллекция идентификаторов записей справочника
 * @return - возвращает мапу
 */
def getRefBookByRecordIds(def long refBookId, def recordIds) {
    def refBookMap = getProvider(refBookId).getRecordData(recordIds)
    if (refBookMap == null || refBookMap.size() == 0) {
        throw new Exception("Ошибка при получении записей справочника " + refBookId)
    }
    return refBookMap
}

/**
 * Получение провайдера с использованием кеширования.
 * @param providerId
 * @return
 */
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

/**
 * Получить окончание отчетного периода
 * @return
 */
def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}