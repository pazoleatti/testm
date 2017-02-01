package form_template.ndfl.primary_rnu_ndfl.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonPrepayment
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
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

// Страны
@Field def countryCodesCache = []
@Field final long REF_BOOK_COUNTRY_ID = 10

// Документы, удостоверяющие личность
@Field def documentCodesCache = []
@Field final long REF_BOOK_DOCUMENT_ID = 360

// Статус налогоплательщика
@Field def taxpayerStatusCache = []
@Field final long REF_BOOK_TAXPAYER_STATUS_ID = 903

// Коды видов доходов
@Field def incomeCodeCache = []
@Field final long REF_BOOK_INCOME_CODE_ID = 922

// Коды видов вычетов
@Field def deductionTypeCache = []
@Field final long REF_BOOK_DEDUCTION_TYPE_ID = 921

// todo Пустой справочник
// Коды налоговых органов
@Field def notifSourceCache = []
@Field final long REF_BOOK_NOTIF_SOURCE_ID = 204

// Дата окончания отчетного периода
@Field def reportPeriodEndDate = null

@Field final ERROR_MESSAGE_REF_BOOK = "Ошибка в значении \"%s\". Значение не соответсвует справочнику \"%s\"."

def checkData() {
    // Страны
    def countryCodesList = getCountry()

    // Документы, удостоверяющие личность
    def documentCodesList = getDocument()

    // Статус налогоплательщика
    def taxpayerStatusList = getTaxpayerStatus()

    ndflPersonList = ndflPersonService.findNdflPerson(declarationData.id)
    if (ndflPersonList.size() > 0) {
        throw new Exception("Не найдены записи в таблице \"%s\".", "Данные о физическом лице - получателе дохода")
    }
    ndflPersonList.each { ndflPerson ->
        // Страны
        if (!countryCodesList.containsValue(ndflPerson.citizenship)) {
            logger.error(ERROR_MESSAGE_REF_BOOK, "Гражданство (код страны)", "ОК 025-2001 (Общероссийский классификатор стран мира)");
        }

        // Документы, удостоверяющие личность
        if (!documentCodesList.containsValue(ndflPerson.idDocType)) {
            logger.error(ERROR_MESSAGE_REF_BOOK, "Код вида документа", "Коды документов");
        }

        // Статус налогоплательщика
        if (!taxpayerStatusList.containsValue(ndflPerson.status)) {
            logger.error(ERROR_MESSAGE_REF_BOOK, "Cтатус налогоплательщика", "Статусы налогоплательщика");
        }
    }


    // Коды видов доходов
    def incomeCodeList = getIncomeCode()

    ndflPersonIncomeList = ndflPersonService.findNdflPersonIncome(declarationData.id)
    if (ndflPersonIncomeList != null || ndflPersonIncomeList.size() > 0 || ndflPersonIncomeList.get(0) != null) {
        throw new Exception("Не найдены записи в таблице \"%s\".", "Сведения о доходах физического лица")
    }
    ndflPersonIncomeList.each { ndflPersonIncome ->
        // Коды видов доходов
        if (!incomeCodeList.containsValue(ndflPersonIncome.incomeCode)) {
            logger.error(ERROR_MESSAGE_REF_BOOK, "Код вида дохода", "Коды видов доходов");
        }

        // todo Реализовать после добавления справочника
        // Признак вида дохода

        // todo Реализовать после добавления справочника
        // Ставка
    }

    // Коды видов вычетов
    def deductionTypeList = getDeductionType()

    // Коды налоговых органов
    def notifSourceList = getNotifSource()

    ndflPersonDeductionList = ndflPersonService.findNdflPersonDeduction(declarationData.id)
    if (ndflPersonDeductionList != null || ndflPersonDeductionList.size() > 0 || ndflPersonDeductionList.get(0) != null) {
        throw new Exception("Не найдены записи в таблице \"%s\".", "Стандартные, социальные и имущественные налоговые вычеты")
    }
    ndflPersonDeductionList.each { ndflPersonDeduction ->
        // Коды видов вычетов
        if (!deductionTypeList.containsValue(ndflPersonDeduction.typeCode)) {
            logger.error(ERROR_MESSAGE_REF_BOOK, "Код вида вычета", "Коды видов вычетов");
        }

        // Коды налоговых органов
        if (!notifSourceList.containsValue(ndflPersonDeduction.notifSource)) {
            logger.error(ERROR_MESSAGE_REF_BOOK, "Код налогового органа, выдавшего уведомление", "Коды налоговых органов");
        }
    }

    ndflPersonPrepaymentList = ndflPersonService.findNdflPersonPrepayment(declarationData.id)
    if (ndflPersonPrepaymentList != null || ndflPersonPrepaymentList.size() > 0 || ndflPersonPrepaymentList.get(0) != null) {
        throw new Exception("Не найдены записи в таблице \"%s\".", "Cведения о доходах в виде авансовых платежей")
    }
    ndflPersonPrepaymentList.each { ndflPersonPrepayment ->
        // Коды налоговых органов
        if (!notifSourceList.containsValue(ndflPersonPrepayment.notifSource)) {
            logger.error(ERROR_MESSAGE_REF_BOOK, "Код налогового органа, выдавшего уведомление", "Коды налоговых органов");
        }
    }
}

/**
 * Получить "Страны"
 * @return
 */
def getCountry() {
    if (countryCodesCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_COUNTRY_ID)
        if (refBookList != null || refBookList.size() > 0 || refBookList.get(0) != null) {
            refBookList.each { refBook ->
                countryCodesCache.add(refBook?.CODE?.stringValue)
            }
        }
    }
    return countryCodesCache;
}

/**
 * Получить "Коды документов, удостоверяющих личность"
 * @return
 */
def getDocument() {
    if (documentCodesCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_DOCUMENT_ID)
        if (refBookList != null || refBookList.size() > 0 || refBookList.get(0) != null) {
            refBookList.each { refBook ->
                documentCodesCache.add(refBook?.CODE?.stringValue)
            }
        }
    }
    return documentCodesCache;
}

/**
 * Получить "Статусы налогоплательщика"
 * @return
 */
def getTaxpayerStatus() {
    if (taxpayerStatusCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_TAXPAYER_STATUS_ID)
        if (refBookList.size() > 0) {
            refBookList.each { refBook ->
                taxpayerStatusCache.add(refBook?.CODE?.stringValue)
            }
        }
    }
    return taxpayerStatusCache;
}

/**
 * Получить "Коды видов доходов"
 * @return
 */
def getIncomeCode() {
    if (incomeCodeCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_INCOME_CODE_ID)
        if (refBookList.size() > 0) {
            refBookList.each { refBook ->
                incomeCodeCache.add(refBook?.CODE?.stringValue)
            }
        }
    }
    return incomeCodeCache;
}

/**
 * Получить "Коды видов вычетов"
 * @return
 */
def getDeductionType() {
    if (deductionTypeCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_DEDUCTION_TYPE_ID)
        if (refBookList.size() > 0) {
            refBookList.each { refBook ->
                deductionTypeCache.add(refBook?.CODE?.stringValue)
            }
        }
    }
    return deductionTypeCache;
}

/**
 * Получить "Коды налоговых органов"
 * @return
 */
def getNotifSource() {
    if (notifSourceCache.size() == 0) {
        def refBookList = getRefBook(REF_BOOK_NOTIF_SOURCE_ID)
        if (refBookList.size() > 0) {
            refBookList.each { refBook ->
                notifSourceCache.add(refBook?.CODE?.stringValue)
            }
        }
    }
    return notifSourceCache;
}

/**
 * Получить все записи справочника по его идентификатору
 * @param refBookId - идентификатор справочника
 * @return
 */
def getRefBook(def long refBookId) {
    // Передаем как аргумент только срок действия версии справочника
    def refBookList = getProvider(refBookId).getRecords(getReportPeriodEndDate() - 1, null, null, null)
    // todo Пустые справочники
    if (refBookId != REF_BOOK_NOTIF_SOURCE_ID) {
        if (refBookList == null || refBookList.size() == 0 || refBookList.get(0) == null) {
            throw new Exception("Ошибка при получении записей справочника " + refBookId)
        }
    }
    return refBookList
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