package form_template.vat.declaration_10_3q2016.v2016

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder

import javax.xml.stream.XMLStreamReader

/**
 * Декларация по НДС (раздел 10 с 3 квартала 2016)
 *
 * declarationTypeId=26
 * declarationTemplateId=1026
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

void generateXML() {
    // атрибуты, заполняемые по настройкам подразделений
    def departmentParam = getDepartmentParam()
    def taxOrganCode = departmentParam?.TAX_ORGAN_CODE?.value
    def taxOrganCodeProm = departmentParam?.TAX_ORGAN_CODE_PROM?.value
    def inn = departmentParam?.INN?.value
    def kpp = departmentParam?.KPP?.value
    def formatVersion = departmentParam?.FORMAT_VERSION?.value

    // атрибуты элементов Файл и Документ
    def fileId = TaxType.VAT.declarationPrefix + ".10" + "_" +
            taxOrganCodeProm + "_" +
            taxOrganCode + "_" +
            inn + "" + kpp + "_" +
            Calendar.getInstance().getTime().format('yyyyMMdd') + "_" +
            UUID.randomUUID().toString().toUpperCase()
    def index = "0000100"
    def corrNumber = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0

    // атрибуты, заполняемые по форме 937.3 (строка 001 отдельно, остальное в массиве sourceDataRowsAll)
    def sourceDataRowsAll = []
    def code001 = null
    def sourceCorrNumber
    for (def formData : getSources()) {
        def sourceDataRows = formDataService.getDataRowHelper(formData)?.getAll()
        sourceDataRowsAll.addAll(sourceDataRows)
        sourceCorrNumber = reportPeriodService.getCorrectionNumber(formData.departmentReportPeriodId) ?: 0
    }
    if (corrNumber > 0) {
        code001 = (corrNumber == sourceCorrNumber) ? 0 : 1
    }

    def builder = new MarkupBuilder(xml)
    builder.Файл(
            ИдФайл: fileId,
            ВерсПрог: applicationVersion,
            ВерсФорм: formatVersion) {
        Документ(
                [Индекс: index] +
                        [НомКорр: corrNumber] +
                        (code001 != null ? [ПризнСвед10: code001] : [:])
        ) {
            if (code001 != 1) {
                ЖУчВыстСчФ() {
                    hasPage = false
                    def rowNum = 1
                    for (def row : sourceDataRowsAll) {
                        if (row.getAlias() != null) {
                            if (row.getAlias() == "part_2") {
                                break
                            }
                            continue
                        }
                        if (row.fix != null) {
                            continue
                        }
                        hasPage = true
                        def code005 = rowNum++
                        def code010 = row.date?.format('dd.MM.yyyy')
                        def code020 = getRefBookValue(650L, row.opTypeCode)?.CODE?.value
                        def code030 = getNumber(row.invoiceNumDate)
                        def code040 = getDate(row.invoiceNumDate)
                        def code050 = getNumber(row.invoiceCorrNumDate)
                        def code060 = getDate(row.invoiceCorrNumDate)
                        def code070 = getNumber(row.corrInvoiceNumDate)
                        def code080 = getDate(row.corrInvoiceNumDate)
                        def code090 = getNumber(row.corrInvCorrNumDate)
                        def code100 = getDate(row.corrInvCorrNumDate)
                        def code110 = row.buyerInnKpp
                        def code120 = row.mediatorInnKpp
                        def code130 = getNumber(row.mediatorNumDate)
                        def code140 = getDate(row.mediatorNumDate)
                        def code150 = getCurrencyCode(row.currNameCode)
                        def code160 = row.cost
                        def code170 = row.vatSum
                        def code180 = row.diffDec ?: empty
                        def code190 = row.diffInc ?: empty
                        def code200 = row.diffVatDec ?: empty
                        def code210 = row.diffVatInc ?: empty

                        // различаем юр. и физ. лица в строках 110 и 120
                        def code110inn, code110kpp, code120inn, code120kpp
                        def boolean isUL110 = false
                        def slashIndex = code110?.indexOf("/")
                        if (slashIndex > 0) {
                            isUL110 = true
                            code110inn = code110.substring(0, slashIndex)
                            code110kpp = code110.substring(slashIndex + 1)
                        }
                        def boolean isUL120 = false
                        slashIndex = code120?.indexOf("/")
                        if (slashIndex > 0) {
                            isUL120 = true
                            code120inn = code120.substring(0, slashIndex)
                            code120kpp = code120.substring(slashIndex + 1)
                        }

                        ЖУчВыстСчФСтр(
                                [НомерПор: code005] +
                                        [ДатаВыст: code010] +
                                        [НомСчФПрод: code030] +
                                        [ДатаСчФПрод: code040] +
                                        (code050 != null ? [НомИспрСчФ: code050] : [:]) +
                                        (code060 != null ? [ДатаИспрСчФ: code060] : [:]) +
                                        (code070 != null ? [НомКСчФПрод: code070] : [:]) +
                                        (code080 != null ? [ДатаКСчФПрод: code080] : [:]) +
                                        (code090 != null ? [НомИспрКСчФ: code090] : [:]) +
                                        (code100 != null ? [ДатаИспрКСчФ: code100] : [:])
                        ) {
                            КодВидОпер(code020)
                            if (code110 != null) {
                                СвПокуп() {
                                    if (isUL110) {
                                        СведЮЛ(
                                                ИННЮЛ: code110inn,
                                                КПП: code110kpp
                                        )
                                    } else {
                                        СведИП(
                                                ИННФЛ: code110
                                        )
                                    }
                                }
                            }
                            СвПосрДеят(
                                    [НомСчФОтПрод: code130] +
                                            [ДатаСчФОтПрод: code140] +
                                            (code150 != null ? [ОКВ: code150] : [:]) +
                                            (code160 != null ? [СтоимТовСчФВс: code160] : [:]) +
                                            (code170 != null ? [СумНДССчФ: code170] : [:]) +
                                            [РазСтКСчФУм: code180] +
                                            [РазСтКСчФУв: code190] +
                                            [РазНДСКСчФУм: code200] +
                                            [РазНДСКСчФУв: code210]
                            ) {
                                if (code120 != null) {
                                    СвПрод() {
                                        if (isUL120) {
                                            СведЮЛ(
                                                    ИННЮЛ: code120inn,
                                                    КПП: code120kpp
                                            )
                                        } else {
                                            СведИП(
                                                    ИННФЛ: code120
                                            )
                                        }
                                    }
                                }
                            }

                        }
                    }
                    if (!hasPage) {
                        ЖУчВыстСчФСтр() {}
                    }
                }
            }
        }
    }
}

def checkDeclarationFNS() {
    def declarationFnsId = 4
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    def declarationData = declarationService.getLast(declarationFnsId, declarationData.departmentId, reportPeriod.id)
    if (declarationData != null && declarationData.accepted) {
        def String event = (formDataEvent == FormDataEvent.MOVE_CREATED_TO_ACCEPTED) ? "Принять данную декларацию" : "Отменить принятие данной декларации"
        throw new ServiceException('%s невозможно, так как в текущем периоде и подразделении принята "Декларация по НДС (раздел 1-7)"', event)
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

// получаем источники и сортируем по ТБ
def getSources() {
    sourceFormDataList = []
    for (def FormData formData : declarationService.getAcceptedFormDataSources(declarationData, userInfo, logger).getRecords()) {
        if (formData.formType.id == 859) {
            sourceFormDataList.add(formData)
        }
    }
    sourceFormDataList.sort { FormData formData -> getRefBookValue(30, formData.departmentId)?.NAME?.value }
    return sourceFormDataList
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
                if (!fileFound && isCurrentNode([], elements)) {
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
    nodeNames.add('Файл')
    def enteredNodes = elements.findAll { it.value }.keySet() // узлы в которые вошли, но не вышли еще
    return enteredNodes.containsAll(nodeNames) && enteredNodes.size() == nodeNames.size()
}