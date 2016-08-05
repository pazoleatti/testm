package form_template.vat.declaration_8_3q2016.v2016

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder

import javax.xml.stream.XMLStreamReader

/**
 * Декларация по НДС (раздел 8 с 3 квартала 2016)
 *
 * declarationTypeId=28
 * declarationTemplateId=2028
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkDepartmentParams(LogLevel.WARNING)
        break
    case FormDataEvent.CHECK:
        checkDepartmentParams(LogLevel.ERROR)
        logicCheckXML(LogLevel.WARNING)
        logicCheck()
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
    def fileId = TaxType.VAT.declarationPrefix + ".8" + "_" +
            taxOrganCodeProm + "_" +
            taxOrganCode + "_" +
            inn + "" + kpp + "_" +
            Calendar.getInstance().getTime().format('yyyyMMdd') + "_" +
            UUID.randomUUID().toString().toUpperCase()
    def index = "0000080"
    def corrNumber = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0

    // атрибуты, заполняемые по форме 937.1 (строки 001 и 190 отдельно, остальное в массиве sourceDataRowsAll)
    def sourceDataRowsAll = []
    def code001 = null
    def code190 = null
    def sourceCorrNumber
    // получаем строки из отсортированного списка
    for (def FormData formData : getSources()) {
        sourceDataRows = formDataService.getDataRowHelper(formData)?.getAll()
        sourceDataRowsAll.addAll(sourceDataRows)
        if (corrNumber > 0 && corrNumber != sourceCorrNumber) {
            sourceCorrNumber = reportPeriodService.getCorrectionNumber(formData.departmentReportPeriodId) ?: 0
        }
        nds = getDataRow(sourceDataRows, 'total')?.nds
        if (nds != null) {
            code190 = (code190 ?: 0) + nds
        }
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
                        (code001 != null ? [ПризнСвед8: code001] : [:])
        ) {
            if (code001 != 1) {
                КнигаПокуп(
                        (code190 != null ? [СумНДСВсКПк: code190] : [:])
                ) {
                    hasPage = false
                    rowNum = 1
                    for (def row : sourceDataRowsAll) {
                        if (row.getAlias() != null) {
                            continue
                        }
                        hasPage = true
                        def code005 = rowNum++
                        def code010 = getRefBookValue(650L, row.typeCode)?.CODE?.value
                        def code020 = getNumber(row.invoice)
                        def code030 = getDate(row.invoice)
                        def code040 = getNumber(row.invoiceCorrecting)
                        def code050 = getDate(row.invoiceCorrecting)
                        def code060 = getNumber(row.invoiceCorrection)
                        def code070 = getDate(row.invoiceCorrection)
                        def code080 = getNumber(row.invoiceCorrectingCorrection)
                        def code090 = getDate(row.invoiceCorrectingCorrection)
                        def code100 = getNumber(row.documentPay)
                        def code110 = getDate(row.documentPay)
                        def code120 = row.dateRegistration?.format('dd.MM.yyyy')
                        def code130 = row.salesmanInnKpp
                        def code140 = row.agentInnKpp
                        def code150 = row.declarationNum
                        def code160 = getCurrencyCode(row.currency)
                        def code170 = row.cost ?: empty
                        def code180 = row.nds ?: empty

                        // различаем юр. и физ. лица в строках 130 и 140
                        def code130inn, code130kpp, code140inn, code140kpp
                        def boolean isUL130 = false
                        def slashIndex = code130?.indexOf("/")
                        if (slashIndex > 0) {
                            isUL130 = true
                            code130inn = code130.substring(0, slashIndex)
                            code130kpp = code130.substring(slashIndex + 1)
                        }
                        def boolean isUL140 = false
                        slashIndex = code140?.indexOf("/")
                        if (slashIndex > 0) {
                            isUL140 = true
                            code140inn = code140.substring(0, slashIndex)
                            code140kpp = code140.substring(slashIndex + 1)
                        }

                        КнПокСтр(
                                [НомерПор: code005] +
                                        [НомСчФПрод: code020] +
                                        (code030 != null ? [ДатаСчФПрод: code030] : [:]) +
                                        (code040 != null ? [НомИспрСчФ: code040] : [:]) +
                                        (code050 != null ? [ДатаИспрСчФ: code050] : [:]) +
                                        (code060 != null ? [НомКСчФПрод: code060] : [:]) +
                                        (code070 != null ? [ДатаКСчФПрод: code070] : [:]) +
                                        (code080 != null ? [НомИспрКСчФ: code080] : [:]) +
                                        (code090 != null ? [ДатаИспрКСчФ: code090] : [:]) +
                                        (code150 != null ? [НомТД: code150] : [:]) +
                                        (code160 != null ? [ОКВ: code160] : [:]) +
                                        [СтоимПокупВ: code170] +
                                        [СумНДСВыч: code180]
                        ) {
                            КодВидОпер(code010)
                            if (code100 != null && code110 != null) {
                                ДокПдтвУпл(
                                        НомДокПдтвУпл: code100,
                                        ДатаДокПдтвУпл: code110
                                )
                            }
                            if (code120 != null) {
                                ДатаУчТов(code120)
                            }
                            if (code130 != null) {
                                СвПрод() {
                                    if (isUL130) {
                                        СведЮЛ(
                                                ИННЮЛ: code130inn,
                                                КПП: code130kpp
                                        )
                                    } else {
                                        СведИП(
                                                ИННФЛ: code130
                                        )
                                    }
                                }
                            }
                            if (code140 != null) {
                                СвПос() {
                                    if (isUL140) {
                                        СведЮЛ(
                                                ИННЮЛ: code140inn,
                                                КПП: code140kpp
                                        )
                                    } else {
                                        СведИП(
                                                ИННФЛ: code140
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (!hasPage) {
                        КнПокСтр() {}
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
        if (formData.formType.id == 855) {
            sourceFormDataList.add(formData)
        }
    }
    sourceFormDataList.sort { FormData formData -> getRefBookValue(30, formData.departmentId)?.NAME?.value }
    return sourceFormDataList
}

void logicCheck() {
    int rowCount = 0
    int rowCountPrev = 1
    for (def FormData formData : getSources()) {
        rowCount += formDataService.getDataRowHelper(formData).getSavedCount()-1
        ReportPeriod reportPeriod =  reportPeriodService.get(formData.reportPeriodId)
        logger.info(String.format("Порядковый номер %s-%s текущего раздела декларации. Данные получены из налоговой формы: %s «%s» подразделения «%s» за %s %s.",
                rowCountPrev, rowCount,
                formData.kind.title, formData.formType.name,
                getRefBookValue(30, formData.departmentId)?.NAME?.value,
                reportPeriod.name,
                reportPeriod.taxPeriod?.year)
        )
        rowCountPrev = rowCount + 1
    }
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