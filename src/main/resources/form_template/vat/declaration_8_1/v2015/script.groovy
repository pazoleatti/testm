package form_template.vat.declaration_8_1.v2015

import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.FormData
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

import javax.xml.stream.XMLStreamReader

/**
 * Декларация по НДС (раздел 8.1)
 *
 * declarationTemplateId=1013
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
        preXmlCheck()
        break
    case FormDataEvent.CALCULATE:
        checkDepartmentParams(LogLevel.WARNING)
        preXmlCheck()
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

// Получение провайдера с использованием кеширования
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

// Разыменование с использованием кеширования
def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
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
        declarationReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    }
    return (declarationReportPeriod?.taxPeriod?.year > 2015 || declarationReportPeriod?.order > 2)
}

@Field
def declarationType8sources = 18
@Field
def declarationType8_1 = 13
@Field
def declarationType8sourcesName = "Декларация по НДС (раздел 8 без консолид. формы)"
@Field
def declarationType8_1Name = "Декларация по НДС (раздел 8.1)"
@Field
def unpId = 1
@Field
def DeclarationData declarationSource
@Field
def formSource
@Field
def sourceSuperRow

void preXmlCheck() {
    def corrNumber = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0
    declarationReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    def periodName = declarationReportPeriod.name
    def unpName = departmentService.get(unpId).name
    def year = declarationReportPeriod.taxPeriod.year
    def declarationDRP
    if (corrNumber == 1) {
        // Декларация по НДС (раздел 8 без консолид. формы)
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter()
        // ищем не в корректирующем периоде
        filter.isCorrection = false
        filter.correctionDate = null
        filter.departmentIdList = [unpId]
        filter.taxTypeList = [TaxType.VAT]
        filter.yearStart = year
        filter.yearEnd = year
        filter.reportPeriodIdList = [declarationData.reportPeriodId]
        def departmentReportPeriods = departmentReportPeriodService.getListByFilter(filter)
        def declarations = declarationService.find(declarationType8sources, departmentReportPeriods[0].id)
        declarationSource = declarations.find { it.accepted }
    } else if (corrNumber > 1) {
        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter()
        // ищем в корректирующем периоде
        filter.isCorrection = true
        filter.departmentIdList = [unpId]
        filter.taxTypeList = [TaxType.VAT]
        filter.yearStart = year
        filter.yearEnd = year
        filter.reportPeriodIdList = [declarationData.reportPeriodId]
        def departmentReportPeriods = departmentReportPeriodService.getListByFilter(filter)
        if (!departmentReportPeriods.isEmpty()) {
            departmentReportPeriods.remove(departmentReportPeriods.size() - 1)
        }
        // начиная с последней корректировки ищем декларацию с ПризнСвед81 = 0
        for (drp in departmentReportPeriods.reverse()) {
            def declarations = declarationService.find(declarationType8_1, drp.id)
            if (declarations != null && declarations.size() == 1 && declarations[0].accepted && getDeclarationXmlAttr(declarations[0], ['Документ'], 'ПризнСвед81') == '0') {
                declarationSource = declarations[0]
                declarationDRP = drp
                break
            }
        }
    } else {
        logger.error("Расчет текущего экземпляра декларации не будет выполнен, т.к. раздел 8.1 должен формироваться в корректирующем периоде")
        return
    }
    boolean isFirstCorrection = corrNumber == 1
    def declarationTypeName = isFirstCorrection ? declarationType8sourcesName : declarationType8_1Name
    if (declarationSource == null) { // сообщение 1
        if (isFirstCorrection) {
            logger.error("Не найдена декларация-источник. Вид: «%s», Подразделение: «%s», Период: «%s, %s». Расчет раздела 8.1 не будет выполнен.",
                    declarationTypeName, unpName, year, periodName)
        } else {
            def correctionDateStr = departmentReportPeriodService.get(declarationData.departmentReportPeriodId)?.correctionDate?.format("dd.MM.yyyy")
            logger.error("Не найдена декларация-источник со строкой «001» равной «0». Вид: «%s», Подразделение: «%s», Период: «%s, %s», Дата сдачи корректировки: «меньше %s». Расчет раздела 8.1 не будет выполнен.",
                    declarationTypeName, unpName, year, periodName, correctionDateStr)
        }
    } else { // сообщение №2
        if (formDataEvent != FormDataEvent.CALCULATE) {
            logger.info("Для заполнения строки 005 раздела 8.1 определена декларация-источник. Вид: «%s», Подразделение: «%s», Период: «%s, %s»%s.",
                    declarationTypeName, unpName, year, periodName, declarationDRP != null ? String.format(", Дата сдачи корректировки: «%s»", declarationDRP.correctionDate.format("dd.MM.yyyy")) : "")
        }
        // период 4 квартал
        def period = reportPeriodService.listByTaxPeriod(declarationReportPeriod.taxPeriod.id).find { it.order == 4 }
        // Выполнить поиск формы-источника «(724.1.1) Корректировка сумм НДС и налоговых вычетов за прошедшие налоговые периоды»
        def form
        if (period) {
            form = formDataService.getLast(848, FormDataKind.CONSOLIDATED, unpId, period.id, null, null, false)
        }
        if (form != null && form.state == WorkflowState.ACCEPTED && reportPeriodService.getCorrectionNumber(form.departmentReportPeriodId) != 0) {
            formSource = form
            def dataRows = formDataService.getDataRowHelper(formSource).allSaved
            def periodCode = getRefBookValue(8, declarationReportPeriod.dictTaxPeriodId).CODE.value
            def superRow = dataRows.find { it.getAlias() == ('super_purchase_' + periodCode) }
            if (superRow != null) { // сообщение 3
                sourceSuperRow = superRow
                if (formDataEvent != FormDataEvent.CALCULATE) {
                    def drp = departmentReportPeriodService.get(formSource.departmentReportPeriodId)
                    logger.info("Для заполнения строки 190 раздела 8.1 определена форма-источник. Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s, %s», Дата сдачи корректировки: «%s».",
                            formSource.kind.title, formSource.formType.name, unpName, year, period.name, drp.correctionDate.format("dd.MM.yyyy"))
                }
            } else  if (formDataEvent != FormDataEvent.CALCULATE) { // сообщение 4
                logger.info("Т.к. не найдена требуемая строка формы-источника 724.1.1, для заполнения строки 190 раздела 8.1 определена декларация-источник. Вид: «%s», Подразделение: «%s», Период: «%s, %s»%s.",
                        declarationTypeName, unpName, year, periodName, declarationDRP != null ? String.format(", Дата сдачи корректировки: «%s»", declarationDRP.correctionDate.format("dd.MM.yyyy")) : "")
            }
        } else if (formDataEvent != FormDataEvent.CALCULATE) { // сообщение 5
            logger.info("Т.к. не найдена форма-источник 724.1.1, для заполнения строки 190 раздела 8.1 определена декларация-источник. Вид: «%s», Подразделение: «%s», Период: «%s, %s»%s.",
                    declarationTypeName, unpName, year, periodName, declarationDRP != null ? String.format(", Дата сдачи корректировки: «%s»", declarationDRP.correctionDate.format("dd.MM.yyyy")) : "")
        }
    }
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

void generateXML() {
    // атрибуты, заполняемые по настройкам подразделений
    def departmentParam = getDepartmentParam()
    def taxOrganCode = departmentParam?.TAX_ORGAN_CODE?.value
    def taxOrganCodeProm = useTaxOrganCodeProm() ? departmentParam?.TAX_ORGAN_CODE_PROM?.value : taxOrganCode
    def inn = departmentParam?.INN?.value
    def kpp = departmentParam?.KPP?.value
    def formatVersion = departmentParam?.FORMAT_VERSION?.value

    // атрибуты элементов Файл и Документ
    def fileId = TaxType.VAT.declarationPrefix + ".81" + "_" +
            taxOrganCodeProm + "_" +
            taxOrganCode + "_" +
            inn + "" + kpp + "_" +
            Calendar.getInstance().getTime().format('yyyyMMdd') + "_" +
            UUID.randomUUID().toString().toUpperCase()
    def index = "0000081"
    def corrNumber = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0

    // атрибуты, заполняемые по форме 937.1.1 (строки 001, 005 и 190 отдельно, остальное в массиве sourceDataRowsAll)
    def sourceDataRowsAll = []
    def code001 = 1
    for (def formData : getSources()) {
        sourceDataRowsAll.addAll(formDataService.getDataRowHelper(formData)?.getAll())
        code001 = 0
    }
    def code005 = getCode005(corrNumber)
    def code190 = (sourceSuperRow != null) ? sourceSuperRow.sumNdsPlus : code005

    def builder = new MarkupBuilder(xml)
    builder.Файл(
            ИдФайл: fileId,
            ВерсПрог: applicationVersion,
            ВерсФорм: formatVersion) {
        Документ(
                [Индекс: index] +
                        [НомКорр: corrNumber] +
                        (code001 != null ? [ПризнСвед81: code001] : [:])
        ) {
            if (code001 != 1) {
                КнигаПокупДЛ(
                        (code005 != null ? [СумНДСИтКПк: code005] : [:]) +
                                (code190 != null ? [СумНДСИтП1Р8: code190] : [:])
                ) {
                    hasPage = false
                    def rowNum = 1
                    for (def row : sourceDataRowsAll) {
                        if (row.getAlias() != null) {
                            continue
                        }
                        hasPage = true
                        def code008 = rowNum++
                        def code010 = row.typeCode
                        def code020 = getNumber(row.invoice)
                        def code030 = getDate(row.invoice)
                        def code040 = getNumber(row.invoiceCorrecting)
                        def code050 = getDate(row.invoiceCorrecting)
                        def code060 = getNumber(row.invoiceCorrection)
                        def code070 = getDate(row.invoiceCorrection)
                        def code080 = getNumber(row.invoiceCorrectingCorrection)
                        def code090 = getDate(row.invoiceCorrectingCorrection)
                        def code100 = getDate(row.documentPay)
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

                        КнПокДЛСтр(
                                [НомерПор: code008] +
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
                                        [СумНДС: code180]
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
                        КнПокДЛСтр() {}
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

def getCode005(def corrNumber) {
    def result = BigDecimal.ZERO
    if (declarationSource != null && declarationSource.id != null) {
        boolean isDeclSource = corrNumber == 1
        def treePath = isDeclSource ? ['Документ', 'КнигаПокуп'] : ['Документ', 'КнигаПокупДЛ']
        def attrName = isDeclSource ? 'СумНДСВсКПк' : 'СумНДСИтП1Р8'
        def code = getDeclarationXmlAttr(declarationSource, treePath, attrName)
        if (code) {
            result = new BigDecimal(code)
        }
    }
    return result
}

// получаем источники и сортируем по ТБ
def getSources() {
    def sourceFormDataList = []
    for (def FormData formData : declarationService.getAcceptedFormDataSources(declarationData, userInfo, logger).getRecords()) {
        if (formData.formType.id == 616) {
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
    def tempNodes = nodeNames + 'Файл'
    def enteredNodes = elements.findAll { it.value }.keySet() // узлы в которые вошли, но не вышли еще
    return enteredNodes.containsAll(tempNodes) && enteredNodes.size() == tempNodes.size()
}