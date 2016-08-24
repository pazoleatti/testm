package form_template.vat.declaration_9_1.v2015

import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter
import groovy.transform.Field
import groovy.xml.MarkupBuilder

import javax.xml.namespace.QName
import javax.xml.stream.XMLStreamReader
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

/**
 * Декларация по НДС (раздел 9.1)
 *
 * declarationTemplateId=1015
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkDepartmentParams(LogLevel.WARNING)
        break
    case FormDataEvent.CHECK:
        checkDepartmentParams(LogLevel.ERROR)
        logicCheckXML(LogLevel.WARNING)
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
        checkDepartmentParams(LogLevel.WARNING)
        logicCheckXML(LogLevel.ERROR)
        checkDeclarationFNS()
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:
        checkDeclarationFNS()
        break
    case FormDataEvent.PRE_CALCULATION_CHECK:
        checkDepartmentParams(LogLevel.WARNING)
        preCalcCheck()
        break
    case FormDataEvent.CALCULATE:
        checkDepartmentParams(LogLevel.WARNING)
        generateXML()
        break
    default:
        return
}

// Кэш провайдеров
@Field
def providerCache = [:]
@Field
def refBookCache = [:]

@Field
def refBookMap = [:]

@Field
def version = '5.04'

// текущая декларация
@Field
def declarationTypeId = 15

// декларация по НДС (раздел 9 без консолид. формы)
@Field
def declarationType9_sources = 21

@Field
int bankDepartmentId = 1

// id формы источника 724.1.1 (не требующего настройки пользователем, подтягивается скриптом)
@Field
int formType_724_1_1 = 848

@Field
def empty = 0

// Параметры подразделения
@Field
def departmentParam = null

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

// Разыменование с использованием кеширования
def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение провайдера с использованием кеширования
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

void checkDepartmentParams(LogLevel logLevel) {
    // Параметры подразделения
    def departmentParam = getDepartmentParam()
    def refBook = getRefBook(RefBook.DEPARTMENT_CONFIG_VAT)

    // Проверки подразделения
    def List<String> errorList = getErrorDepartment(departmentParam, refBook)
    for (String error : errorList) {
        logger.log(logLevel, "На форме настроек подразделения текущего экземпляра декларации отсутствует значение атрибута «%s»!", error)
    }
    def tmpVersion = departmentParam.FORMAT_VERSION?.stringValue
    if (!version.equals(tmpVersion)) {
        def message = "На форме настроек подразделения текущего экземпляра декларации неверно указано значение атрибута «%s» (%s)! Ожидаемое значение «%s»."
        def attributeName = refBook.getAttribute('FORMAT_VERSION').name
        def value = (tmpVersion != null && '' != tmpVersion ? tmpVersion : 'пустое значение')
        logger.log(logLevel, message, attributeName, value, version)
    }
}

// Получить параметры подразделения
def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = declarationData.departmentId
        def departmentParamList = getProvider(RefBook.DEPARTMENT_CONFIG_VAT).getRecords(getEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

List<String> getErrorDepartment(def record, def refBook) {
    List<String> errorList = new ArrayList<String>()
    String attributeName
    if (record.INN?.stringValue == null || record.INN.stringValue.isEmpty()) {
        attributeName = refBook.getAttribute('INN').name
        errorList.add(attributeName)
    }
    if (record.KPP?.stringValue == null || record.KPP.stringValue.isEmpty()) {
        attributeName = refBook.getAttribute('KPP').name
        errorList.add(attributeName)
    }
    errorList
}

@Field
def declarationReportPeriod

boolean useTaxOrganCodeProm() {
    if (declarationReportPeriod == null) {
        declarationReportPeriod = getReportPeriod()
    }
    return (declarationReportPeriod?.taxPeriod?.year > 2015 || declarationReportPeriod?.order > 2)
}

void generateXML() {
    // атрибуты, заполняемые по настройкам подразделений
    def departmentParam = getDepartmentParam()
    def taxOrganCode = departmentParam?.TAX_ORGAN_CODE?.value
    def taxOrganCodeProm = useTaxOrganCodeProm() ? departmentParam?.TAX_ORGAN_CODE_PROM?.value : taxOrganCode
    def inn = departmentParam?.INN?.value
    def kpp = departmentParam?.KPP?.value
    def formatVersion = departmentParam?.FORMAT_VERSION?.value

    // атрибуты элементов Файл и Документ
    def fileId = TaxType.VAT.declarationPrefix + ".91" + "_" +
            taxOrganCodeProm + "_" +
            taxOrganCode + "_" +
            inn + "" + kpp + "_" +
            Calendar.getInstance().getTime().format('yyyyMMdd') + "_" +
            UUID.randomUUID().toString().toUpperCase()
    def index = "0000091"
    def corrNumber = getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0

    // атрибуты, заполняемые по форме 937.2.1 (строки 001, 020-070 и 310-360 отдельно, остальное в массиве sourceDataRows)
    def sourceDataRows = []
    def code001 = null
    def (code020, code030, code040, code050, code060, code070, code310, code320, code330, code340, code350, code360) =
    [null, null, null, null, null, null, null, null, null, null, null, null]

    def codes = null
    if (corrNumber == 1) {
        codes = getCodesFromDeclaration9()
    } else if (corrNumber > 1) {
        codes = getCodesFromDeclaration9_1()
    }
    code020 = codes?.code020
    code030 = codes?.code030
    code040 = codes?.code040
    code050 = codes?.code050
    code060 = codes?.code060
    code070 = codes?.code070

    def formData724_1_1 = getFormData724_1_1()
    def rowsMap = getTotals724_1_1Map(formData724_1_1)
    def row18 = rowsMap?.row18
    def row10 = rowsMap?.row10
    def total1 = rowsMap?.total1 // итог по секции 1
    def total2 = rowsMap?.total2 // итог по секции 2
    def total7 = rowsMap?.total7 // итог по секции 7
    code310 = ((total1 || total7) ? ((total1?.sumPlus ?: BigDecimal.ZERO) + (total7?.sumPlus ?: BigDecimal.ZERO)) : code020)
    code320 = (total2 ? total2.sumPlus : code030)
    code330 = code040
    code340 = (row18 ? row18.sumNdsPlus : code050)
    code350 = (row10 ? row10.sumNdsPlus : code060)
    code360 = code070

    def departmentReportPeriodId_937_2_1 = null
    for (def formData : declarationService.getAcceptedFormDataSources(declarationData, userInfo, logger).getRecords()) {
        // 937.2.1 - Сведения из дополнительных листов книги продаж
        if (formData.formType.id == 617) {
            sourceDataRows = formDataService.getDataRowHelper(formData)?.getAll()
            departmentReportPeriodId_937_2_1 = formData.departmentReportPeriodId
        }
    }
    code001 = (declarationData.departmentReportPeriodId == departmentReportPeriodId_937_2_1) ? 0 : 1

    def builder = new MarkupBuilder(xml)
    builder.Файл(
            ИдФайл: fileId,
            ВерсПрог: applicationVersion,
            ВерсФорм: formatVersion) {
        Документ(
                [Индекс: index] +
                        [НомКорр: corrNumber] +
                        (code001 != null ? [ПризнСвед91: code001] : [:])
        ) {
            if (code001 != 1) {
                КнигаПродДЛ(
                        (code020 != null ? [ИтСтПродКПр18: code020] : [:]) +
                                (code030 != null ? [ИтСтПродКПр10: code030] : [:]) +
                                (code040 != null ? [ИтСтПродКПр0: code040] : [:]) +
                                (code050 != null ? [СумНДСИтКПр18: code050] : [:]) +
                                (code060 != null ? [СумНДСИтКПр10: code060] : [:]) +
                                (code070 != null ? [ИтСтПродОсвКПр: code070] : [:]) +
                                (code310 != null ? [СтПродВсП1Р9_18: code310] : [:]) +
                                (code320 != null ? [СтПродВсП1Р9_10: code320] : [:]) +
                                (code330 != null ? [СтПродВсП1Р9_0: code330] : [:]) +
                                (code340 != null ? [СумНДСВсП1Р9_18: code340] : [:]) +
                                (code350 != null ? [СумНДСВсП1Р9_10: code350] : [:]) +
                                (code360 != null ? [СтПродОсвП1Р9Вс: code360] : [:])

                ) {
                    def hasPage = false
                    for (def row : sourceDataRows) {
                        if (row.getAlias() != null) {
                            continue
                        }
                        hasPage = true
                        def code080 = row.rowNumber
                        def code090 = row.opTypeCode
                        def code180 = row.buyerInnKpp
                        def code190 = row.mediatorInnKpp
                        def code100 = getNumber(row.invoiceNumDate)
                        def code110 = getDate(row.invoiceNumDate)
                        def code120 = getNumber(row.invoiceCorrNumDate)
                        def code130 = getDate(row.invoiceCorrNumDate)
                        def code140 = getNumber(row.corrInvoiceNumDate)
                        def code150 = getDate(row.corrInvoiceNumDate)
                        def code160 = getNumber(row.corrInvCorrNumDate)
                        def code170 = getDate(row.corrInvCorrNumDate)
                        def code200 = getNumber(row.paymentDocNumDate)
                        def code210 = getDate(row.paymentDocNumDate)
                        def code220 = getCurrencyCode(row.currNameCode)
                        def code230 = row.saleCostACurr ?: empty
                        def code240 = row.saleCostARub ?: empty
                        def code250 = row.saleCostB18 ?: empty
                        def code260 = row.saleCostB10 ?: empty
                        def code270 = row.saleCostB0 ?: empty
                        def code280 = row.vatSum18 ?: empty
                        def code290 = row.vatSum10 ?: empty
                        def code300 = row.bonifSalesSum ?: empty

                        // различаем юр. и физ. лица в строках 180 и 190
                        def code180inn, code180kpp, code190inn, code190kpp
                        def boolean isUL180 = false
                        def slashIndex = code180?.indexOf("/")
                        if (slashIndex > 0) {
                            isUL180 = true
                            code180inn = code180.substring(0, slashIndex)
                            code180kpp = code180.substring(slashIndex + 1)
                        }
                        def boolean isUL190 = false
                        slashIndex = code190?.indexOf("/")
                        if (slashIndex > 0) {
                            isUL190 = true
                            code190inn = code190.substring(0, slashIndex)
                            code190kpp = code190.substring(slashIndex + 1)
                        }

                        КнПродДЛСтр(
                                [НомерПор: code080] +
                                        [НомСчФПрод: code100] +
                                        [ДатаСчФПрод: code110] +
                                        (code120 != null ? [НомИспрСчФ: code120] : [:]) +
                                        (code130 != null ? [ДатаИспрСчФ: code130] : [:]) +
                                        (code140 != null ? [НомКСчФПрод: code140] : [:]) +
                                        (code150 != null ? [ДатаКСчФПрод: code150] : [:]) +
                                        (code160 != null ? [НомИспрКСчФ: code160] : [:]) +
                                        (code170 != null ? [ДатаИспрКСчФ: code170] : [:]) +
                                        (code220 != null ? [ОКВ: code220] : [:]) +
                                        [СтоимПродСФВ: code230] +
                                        [СтоимПродСФ: code240] +
                                        [СтоимПродСФ18: code250] +
                                        [СтоимПродСФ10: code260] +
                                        [СтоимПродСФ0: code270] +
                                        [СумНДССФ18: code280] +
                                        [СумНДССФ10: code290] +
                                        [СтоимПродОсв: code300]
                        ) {
                            КодВидОпер(code090)
                            if (code200 != null && code210 != null) {
                                ДокПдтвОпл(
                                        НомДокПдтвОпл: code200,
                                        ДатаДокПдтвОпл: code210
                                )
                            }
                            if (code180 != null) {
                                СвПокуп() {
                                    if (isUL180) {
                                        СведЮЛ(
                                                ИННЮЛ: code180inn,
                                                КПП: code180kpp
                                        )
                                    } else {
                                        СведИП(
                                                ИННФЛ: code180
                                        )
                                    }
                                }
                            }
                            if (code190 != null) {
                                СвПос() {
                                    if (isUL190) {
                                        СведЮЛ(
                                                ИННЮЛ: code190inn,
                                                КПП: code190kpp
                                        )
                                    } else {
                                        СведИП(
                                                ИННФЛ: code190
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (!hasPage) {
                        КнПродДЛСтр() {}
                    }
                }
            }
        }
    }
}

def checkDeclarationFNS() {
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    def corrNumber = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0
    def found = []
    [4: '«Декларация по НДС (раздел 1-7)»', 7: '«Декларация по НДС (аудит, раздел 1-7)»', 20: '«Декларация по НДС (короткая, раздел 1-7)»'].each { id, name ->
        def declarationData17 = declarationService.getLast(id, declarationData.departmentId, reportPeriod.id)
        if (declarationData17 != null && declarationData17.accepted) {
            def sourceCorrNumber = reportPeriodService.getCorrectionNumber(declarationData17.departmentReportPeriodId) ?: 0
            if (sourceCorrNumber == corrNumber) {
                found.add(name)
            }
        }
    }
    if (!found.isEmpty()) {
        def String event = (formDataEvent == FormDataEvent.MOVE_CREATED_TO_ACCEPTED) ? "Данный экземпляр декларации невозможно принять" : "Отменить принятие данного экземпляра декларации невозможно"
        throw new ServiceException('%s, т.к. в подразделении «%s» в периоде «%s, %s%s» приняты экземпляры декларации вида: %s!',
                event,
                departmentService.get(declarationData.departmentId)?.name,
                reportPeriod.taxPeriod?.year,
                reportPeriod.name,
                corrNumber != 0 ? ' с датой сдачи корректировки ' + departmentReportPeriodService.get(declarationData.departmentReportPeriodId)?.correctionDate?.format('dd.MM.yyyy') + '' : '',
                found.join(', '))
    }
}

def getNumber(def String str) {
    if (str == null) {
        return null
    }
    if (str.length() >= 10) {
        if (str.substring(str.length() - 10).matches("(0[1-9]{1}|[1-2]{1}[0-9]{1}|3[0-1]{1})\\.(0[1-9]{1}|1[0-2]{1})\\.(1[0-9]{3}|20[0-9]{2})")) {
            if (str.length() > 10 && str.codePointAt(str.length() - 11).equals(32)) {
                return str.substring(0, str.length() - 11)
            } else {
                return str.substring(0, str.length() - 10)
            }
        }
    }
    return str
}

def getDate(def String str) {
    if (str != null && str.length() >= 10) {
        if (str.substring(str.length() - 10).matches("(0[1-9]{1}|[1-2]{1}[0-9]{1}|3[0-1]{1})\\.(0[1-9]{1}|1[0-2]{1})\\.(1[0-9]{3}|20[0-9]{2})")) {
            return str.substring(str.length() - 10)
        }
    }
    return null
}

def getCurrencyCode(String str) {
    if (str != null) {
        if ((str.length() > 3 && str.codePointAt(str.length() - 4).equals(32)) || str.length() == 3) {
            return str.substring(str.length() - 3)
        }
    }
    return null
}

/** Получить данные из декларации по НДС (раздел 9 без консолид. формы). */
def getCodesFromDeclaration9() {
    def result = null
    def declarationData = getSourceDeclaration()
    if (declarationData) {
        def reader = declarationService.getXmlStreamReader(declarationData.id)
        if (reader == null) {
            return
        }
        try{
            while (reader.hasNext()) {
                if (reader.startElement && QName.valueOf('КнигаПрод').equals(reader.name)) {
                    def code020 = getXmlDecimal(reader, "СтПродБезНДС18")
                    def code030 = getXmlDecimal(reader, "СтПродБезНДС10")
                    def code040 = getXmlDecimal(reader, "СтПродБезНДС0")
                    def code050 = getXmlDecimal(reader, "СумНДСВсКПр18")
                    def code060 = getXmlDecimal(reader, "СумНДСВсКПр10")
                    def code070 = getXmlDecimal(reader, "СтПродОсвВсКПр")
                    result = ['code020' : code020, 'code030' : code030, 'code040' : code040,
                              'code050' : code050, 'code060' : code060, 'code070' : code070]
                    break
                }
                reader.next()
            }
        } finally {
            reader.close()
        }
    }
    return result
}

/** Получить данные из декларации по НДС (раздел 9.1). */
def getCodesFromDeclaration9_1() {
    def result = null
    def declarationData = getSourceDeclaration()
    if (declarationData) {
        def reader = declarationService.getXmlStreamReader(declarationData.id)
        if (reader == null) {
            return
        }
        try{
            while (reader.hasNext()) {
                if (reader.startElement && QName.valueOf('КнигаПродДЛ').equals(reader.name)) {
                    def code020 = getXmlDecimal(reader, "СтПродВсП1Р9_18")
                    def code030 = getXmlDecimal(reader, "СтПродВсП1Р9_10")
                    def code040 = getXmlDecimal(reader, "СтПродВсП1Р9_0")
                    def code050 = getXmlDecimal(reader, "СумНДСВсП1Р9_18")
                    def code060 = getXmlDecimal(reader, "СумНДСВсП1Р9_10")
                    def code070 = getXmlDecimal(reader, "СтПродОсвП1Р9Вс")
                    result = ['code020' : code020, 'code030' : code030, 'code040' : code040,
                            'code050' : code050, 'code060' : code060, 'code070' : code070]
                    break
                }
                reader.next()
            }
        } finally {
            reader.close()
        }
    }
    return result
}

BigDecimal getXmlDecimal(def reader, String attrName) {
    def value = reader?.getAttributeValue(null, attrName)
    if (!value) {
        return null
    }
    return new BigDecimal(value)
}

// Логические проверки (Проверки значений атрибутов формы настроек подразделения, атрибутов файла формата законодателя)
void logicCheckXML(LogLevel logLevel) {
    // получение данных из xml'ки
    def reader = declarationService.getXmlStreamReader(declarationData.id)
    if (reader == null) {
        return
    }
    def elements = [:]

    def idFile
    def versForm
    def fileFound = false

    try{
        while (reader.hasNext()) {
            if (reader.startElement) {
                elements[reader.name.localPart] = true
                if (!fileFound && isCurrentNode(['Файл'], elements)) {
                    fileFound = true
                    idFile = getXmlValue(reader, 'ИдФайл')
                    versForm = getXmlValue(reader, 'ВерсФорм')
                }
            }
            if (reader.endElement) {
                elements[reader.name.localPart] = false
            }
            reader.next()
        }
    } finally {
        reader.close()
    }

    def refBook = getRefBook(RefBook.DEPARTMENT_CONFIG_VAT)
    if (!checkInnKpp(idFile)) {
        ['INN', 'KPP'].each { attributeAlias ->
            def attributeName = refBook.getAttribute(attributeAlias).name
            def message = "Обязательный для заполнения атрибут «%s» в наименовании xml файла не заполнен! На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения отсутствовало значение атрибута «%s»."
            logger.log(logLevel, message, attributeName, attributeName)
        }

    }
    if (versForm == null || !version.equals(versForm)) {
        def message = "Обязательный для заполнения атрибут «%s» (%s) заполнен неверно (%s)! Ожидаемое значение «%s». На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения было указано неверное значение атрибута «%s»."
        def attributeName = refBook.getAttribute('FORMAT_VERSION').name
        def value = (versForm == null || versForm.isEmpty() ? 'пустое значение' : versForm)
        logger.log(logLevel, message, attributeName, "Файл.ВерсФорм", value, version, attributeName)
    }
}

/**
 * Проверить значение "ИНН" и "КПП" в составе атрибут xml "ИдФайл".
 * ИдФайл имеет следующйю структуру:
 *      NO_NDS_Код налогового органа (пром.)_Код налогового органа (кон.)_ИНН+КПП_ГГГГММДД_UUID.
 *
 * @param value значение ИдФайл
 */
def checkInnKpp(def value) {
    if (!value) {
        return false
    }
    def tmpValues = value.split('_')
    // "ИНН" и "КПП" - 5ые по порядку в ИдФайл
    def position = 5
    if (tmpValues.size() < position || !tmpValues[position - 1] || 'nullnull' == tmpValues[position - 1]) {
        return false
    }
    return true
}

String getXmlValue(XMLStreamReader reader, String attrName) {
    return reader?.getAttributeValue(null, attrName)
}

def getRefBook(def id) {
    if (refBookMap[id] == null) {
        refBookMap[id] = refBookFactory.get(id)
    }
    return refBookMap[id]
}

/**
 * Ищет точное ли совпадение узлов дерева xml c текущими незакрытыми элементами
 * @param nodeNames ожидаемые элементы xml
 * @param elements незакрытые элементы
 * @return
 */
boolean isCurrentNode(List<String> nodeNames, Map<String, Boolean> elements) {
    def enteredNodes = elements.findAll { it.value }.keySet() // узлы в которые вошли, но не вышли еще
    return enteredNodes.containsAll(nodeNames) && enteredNodes.size() == nodeNames.size()
}

@Field
def reportPeriod = null

def getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    }
    return reportPeriod
}

@Field
def correctionNumberMap = [:]

def getCorrectionNumber(def id) {
    if (correctionNumberMap[id] == null) {
        correctionNumberMap[id] = reportPeriodService.getCorrectionNumber(id)
    }
    return correctionNumberMap[id]
}

@Field
def period4 = null

/** Получить четвертый отчетный период (4 квартал) текущего налогового периода. */
Integer getPeriod4Id() {
    if (period4 == null) {
        def year = getReportPeriod()?.taxPeriod?.year
        def start = Date.parse('dd.MM.yyyy', '01.01.' + year)
        def end = Date.parse('dd.MM.yyyy', '31.12.' + year)
        def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.VAT, start, end)
        period4 = reportPeriods.find { it.order == 4 }
    }
    return period4?.id
}

@Field
def departmentReportPeriodMap = [:]

DepartmentReportPeriod getDepartmentReportPeriod(def id) {
    if (departmentReportPeriodMap[id] == null) {
        departmentReportPeriodMap[id] = departmentReportPeriodService.get(id)
    }
    return departmentReportPeriodMap[id]
}

@Field
def formData724_1_1 = null

/** Получить строки источника 724.1.1 (форма должна быть только в корректирующем периоде). */
def getFormData724_1_1() {
    if (formData724_1_1 == null) {
        def formData = null
        if (getPeriod4Id() != null) {
            formData = formDataService.getLast(formType_724_1_1, FormDataKind.CONSOLIDATED, bankDepartmentId, getPeriod4Id(), null, null, false)
        }
        if (formData != null) {
            def correctionDate = getDepartmentReportPeriod(formData.departmentReportPeriodId)?.correctionDate
            // период только корректирующий
            if (formData.state == WorkflowState.ACCEPTED && correctionDate) {
                formData724_1_1 = formData
            }
        }
    }
    return formData724_1_1
}

/** Предварительные проверки перед расчетом раздела 9.1. */
void preCalcCheck() {
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    def correctionNumber = getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0

    if(correctionNumber == 0){
        logger.error("Расчет текущего экземпляра декларации не будет выполнен, т.к. раздел 9.1 должен формироваться в корректирующем периоде")
        return
    }

    def departmentName = departmentService.get(bankDepartmentId)?.name
    def year = getReportPeriod()?.taxPeriod?.year
    def period = getReportPeriod()?.name
    def periodName = year + ', ' + period
    def sourceDeclarationTypeId = (correctionNumber == 1 ? declarationType9_sources : declarationTypeId)
    def declarationName = declarationService.getType(sourceDeclarationTypeId)?.name
    if (!declarationName) {
        declarationName = (sourceDeclarationTypeId == declarationTypeId ? 'Декларация по НДС (раздел 9.1)' : 'Декларация по НДС (раздел 9 без консолид. формы)')
    }

    // проверка декларации-источника «Декларация по НДС (раздел 9 без консолид. формы)» / «Декларация по НДС (раздел 9.1)»
    DeclarationData declarationData9 = getSourceDeclaration()
    if (declarationData9 == null) {
        // сообщение 1
        if (correctionNumber == 1) {
            def msg = "Не найдена декларация-источник. Вид: «%s», Подразделение: «%s», Период: «%s». Расчет раздела 9.1 не будет выполнен."
            logger.error(msg, declarationName, departmentName, periodName)
        } else if (correctionNumber > 1) {
            def correctionDate = getDepartmentReportPeriod(declarationData.departmentReportPeriodId)?.correctionDate?.format('dd.MM.yyyy')
            def msg = "Не найдена декларация-источник со строкой «001» равной «0». Вид: «%s», Подразделение: «%s», Период: «%s», Дата сдачи корректировки: «меньше %s». Расчет раздела 9.1 не будет выполнен."
            logger.error(msg, declarationName, departmentName, periodName, correctionDate)
        }
        return
    }
    // сообщение 2
    def subMsg = ''
    def correctionDate = getDepartmentReportPeriod(declarationData9.departmentReportPeriodId)?.correctionDate?.format('dd.MM.yyyy')
    if (correctionDate) {
        subMsg = String.format(", Дата сдачи корректировки: «%s»", correctionDate)
    }
    def msg = "Для заполнения строк 020-070, 330, 360 раздела 9.1 определена декларация-источник. Вид: «%s», Подразделение: «%s», Период: «%s»%s."
    logger.info(msg, declarationName, departmentName, periodName, subMsg)

    // проверка формы 724.1.1
    def formDataKind = FormDataKind.CONSOLIDATED.title
    def forFormNamePeriod4Id = (getPeriod4Id() ?: declarationData.departmentReportPeriodId)
    def formName = formDataService.getFormTemplate(formType_724_1_1, forFormNamePeriod4Id)?.name
    def formData = getFormData724_1_1()
    if (formData) {
        correctionDate = getDepartmentReportPeriod(formData.departmentReportPeriodId)?.correctionDate?.format('dd.MM.yyyy')
        // поиск строк
        def rowsMap = getTotals724_1_1Map(formData)
        def row18 = rowsMap?.row18
        def row10 = rowsMap?.row10
        def total1 = rowsMap?.total1 // итог по секции 1
        def total2 = rowsMap?.total2 // итог по секции 2
        def total7 = rowsMap?.total7 // итог по секции 7

        def used724_1_1Map = ['310' : (total1 != null || total7 != null), '320' : (total2 != null), '340' : (row18 != null), '350' : (row10 != null)]
        def codes724_1_1 = []
        def codesDeclaration = []
        used724_1_1Map.each { def code, use724_1_1 ->
            if (use724_1_1) {
                codes724_1_1 += code
            } else {
                codesDeclaration += code
            }
        }

        // сообщение 5.1
        if (!codes724_1_1.isEmpty()) {
            msg = "Для заполнения строк %s раздела 9.1 определена форма-источник. Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s», Дата сдачи корректировки: «%s»."
            logger.info(msg, codes724_1_1.join(', '), formDataKind, formName, departmentName, periodName, correctionDate)
        }

        // сообщение 5.2
        if (!codesDeclaration.isEmpty()) {
            msg = "Т.к. не найдены требуемые строки формы-источника 724.1.1, для заполнения строк %s раздела 9.1 определена декларация-источник. Вид: «%s», Подразделение: «%s», Период: «%s»%s."
            logger.info(msg, codesDeclaration.join(', '), declarationName, departmentName, periodName, subMsg)
        }
    } else {
        // сообщение 6
        msg = "Т.к. не найдена форма-источник 724.1.1, для заполнения строк 310, 320, 340, 350 раздела 9.1 определена декларация-источник. Вид: «%s», Подразделение: «%s», Период: «%s»%s."
        logger.info(msg, declarationName, departmentName, periodName, subMsg)
    }
}

@Field
DeclarationData declarationSource = null

/**
 * Получить декларацию источник.
 *
 * @return «Декларация по НДС (раздел 9 без консолид. формы)» - если номер корректировки 1
 *         «Декларация по НДС (раздел 9.1)» - если номер корректировки больше 1
 */
DeclarationData getSourceDeclaration() {
    if (declarationSource != null) {
        return declarationSource
    }
    DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter()
    filter.setDepartmentIdList([bankDepartmentId])
    filter.setReportPeriodIdList([declarationData.reportPeriodId])
    List<DepartmentReportPeriod> departmentReportPeriods = departmentReportPeriodService.getListByFilter(filter)
    def correctionNumber = getCorrectionNumber(declarationData.departmentReportPeriodId)
    if (correctionNumber == 1) {
        // декларация по НДС (раздел 9 без консолид. формы)
        DepartmentReportPeriod dpr = departmentReportPeriods.get(0)
        if (dpr && dpr.correctionDate == null) {
            List<DeclarationData> declarations = declarationService.find(declarationType9_sources, dpr.id)
            if (declarations != null && declarations.size() == 1 && declarations[0].accepted) {
                declarationSource = declarations[0]
            }
        }
        return declarationSource
    }

    // декларация по НДС (раздел 9.1)
    if (!departmentReportPeriods.isEmpty()) {
        departmentReportPeriods.remove(departmentReportPeriods.size() - 1)
    }
    // начиная с последней корректировки ищем декларацию с ПризнСвед91 = 0 (код строки декларации - 001)
    for (drp in departmentReportPeriods.reverse()) {
        List<DeclarationData> declarations = declarationService.find(declarationTypeId, drp.id)
        if (declarations != null && declarations.size() == 1 && declarations[0].accepted && getDeclarationXmlAttr(declarations[0], ['Документ'], 'ПризнСвед91') == '0') {
            declarationSource = declarations[0]
            break
        }
    }
    return declarationSource
}

// Получает из декларации атрибут
def getDeclarationXmlAttr(def declarationData, def treePath, def attrName) {
    // получение данных из xml'ки
    def reader = declarationService.getXmlStreamReader(declarationData.id)
    if (reader == null) {
        return
    }
    def elements = [:]

    try{
        treePath.add('Файл')
        while (reader.hasNext()) {
            if (reader.startElement) {
                elements[reader.name.localPart] = true
                if (isCurrentNode(treePath, elements)) {
                    return getXmlValue(reader, attrName)
                }
            }
            if (reader.endElement) {
                elements[reader.name.localPart] = false
            }
            reader.next()
        }
    } finally {
        reader.close()
    }
    return null
}

def getTotals724_1_1Map(def formData) {
    if (formData == null) {
        return null
    }
    def rows = formDataService.getDataRowHelper(formData)?.getAll()
    def code = getRefBookValue(8, getReportPeriod()?.dictTaxPeriodId)?.CODE?.value
    String rowAlias = 'super_sale_10_' + code
    def row10 = rows.find { it.getAlias() == rowAlias }
    rowAlias = 'super_sale_18_' + code
    def row18 = rows.find { it.getAlias() == rowAlias }
    def map = [ 'row10' : row10, 'row18' : row18 ]
    [1, 2, 7].each { section ->
        rowAlias = 'total_' + section + '_' + code
        def totalSection = rows.find { it.getAlias() == rowAlias }
        map.put(((String) ("total" + section)), totalSection)
    }
    return map
}