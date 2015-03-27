package form_template.vat.declaration_9.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat

/**
 * Декларация по НДС (раздел 9)
 *
 * declarationTemplateId=1014
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkDepartmentParams(LogLevel.WARNING)
        break
    case FormDataEvent.CHECK:
        checkDepartmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
        checkDepartmentParams(LogLevel.ERROR)
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

void checkDepartmentParams(LogLevel logLevel) {
    // Параметры подразделения
    def departmentParam = getDepartmentParam()

    // Проверки подразделения
    def List<String> errorList = getErrorDepartment(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для данного подразделения на форме настроек подразделений отсутствует значение атрибута %s!", error))
    }
    errorList = getErrorVersion(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Неверно указано значение атрибута %s на форме настроек подразделений", error))
    }
}

// Получить параметры подразделения
def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = declarationData.departmentId
        def departmentParamList = getProvider(RefBook.DEPARTMENT_CONFIG_VAT).getRecords(getEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

List<String> getErrorDepartment(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.INN?.stringValue == null || record.INN.stringValue.isEmpty()) {
        errorList.add("«ИНН»")
    }
    if (record.KPP?.stringValue == null || record.KPP.stringValue.isEmpty()) {
        errorList.add("«КПП»")
    }
    errorList
}

List<String> getErrorVersion(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.FORMAT_VERSION.stringValue == null || !record.FORMAT_VERSION.stringValue.equals('5.04')) {
        errorList.add("«Версия формата»")
    }
    errorList
}

void generateXML() {
    // атрибуты, заполняемые по настройкам подразделений
    def departmentParam = getDepartmentParam()
    def taxOrganCode = departmentParam?.TAX_ORGAN_CODE?.value
    def inn = departmentParam?.INN?.value
    def kpp = departmentParam?.KPP?.value
    def formatVersion = departmentParam?.FORMAT_VERSION?.value

    // атрибуты элементов Файл и Документ
    def fileId = TaxType.VAT.declarationPrefix + ".9" + "_" +
            taxOrganCode + "_" +
            taxOrganCode + "_" +
            inn + "" + kpp + "_" +
            (new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime()) + "_" +
            UUID.randomUUID().toString().toUpperCase()
    def index = "0000090"
    def corrNumber = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0

    // атрибуты, заполняемые по форме 937.2 (строки 001 и 230-280 отдельно, остальное в массиве sourceDataRows)
    def sourceDataRows = []
    def code001 = null
    def (code230, code240, code250, code260, code270, code280) = [null, null, null, null, null, null]
    def sourceCorrNumber
    for (def formData : declarationService.getAcceptedFormDataSources(declarationData).getRecords()) {
        if (formData.formType.id == 608) {
            sourceDataRows = formDataService.getDataRowHelper(formData)?.getAll()
            sourceCorrNumber = reportPeriodService.getCorrectionNumber(formData.departmentReportPeriodId) ?: 0
            def totalRow = getDataRow(sourceDataRows, 'total')
            code230 = totalRow?.saleCostB18
            code240 = totalRow?.saleCostB10
            code250 = totalRow?.saleCostB0
            code260 = totalRow?.vatSum18
            code270 = totalRow?.vatSum10
            code280 = totalRow?.bonifSalesSum
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
                        (code001 != null ? [ПризнСвед9: code001] : [:])
        ) {
            if (code001 != 1) {
                КнигаПрод(
                        (code230 != null ? [СтПродБезНДС18: code230] : [:]) +
                                (code240 != null ? [СтПродБезНДС10: code240] : [:]) +
                                (code250 != null ? [СтПродБезНДС0: code250] : [:]) +
                                (code260 != null ? [СумНДСВсКПр18: code260] : [:]) +
                                (code270 != null ? [СумНДСВсКПр10: code270] : [:]) +
                                (code280 != null ? [СтПродОсвВсКПр: code280] : [:])
                ) {
                    hasPage = false
                    for (def row : sourceDataRows) {
                        if (row.getAlias() != null) {
                            continue
                        }
                        hasPage = true
                        def code005 = row.rowNumber
                        def code010 = row.opTypeCode
                        def code020 = getNumber(row.invoiceNumDate)
                        def code030 = getDate(row.invoiceNumDate)
                        def code040 = getNumber(row.invoiceCorrNumDate)
                        def code050 = getDate(row.invoiceCorrNumDate)
                        def code060 = getNumber(row.corrInvoiceNumDate)
                        def code070 = getDate(row.corrInvoiceNumDate)
                        def code080 = getNumber(row.corrInvCorrNumDate)
                        def code090 = getDate(row.corrInvCorrNumDate)
                        def code100 = row.buyerInnKpp
                        def code110 = row.mediatorInnKpp
                        def code120 = getNumber(row.paymentDocNumDate)
                        def code130 = getDate(row.paymentDocNumDate)
                        def code140 = getCurrencyCode(row.currNameCode)
                        def code150 = row.saleCostACurr ?: empty
                        def code160 = row.saleCostARub ?: empty
                        def code170 = row.saleCostB18 ?: empty
                        def code180 = row.saleCostB10 ?: empty
                        def code190 = row.saleCostB0 ?: empty
                        def code200 = row.vatSum18 ?: empty
                        def code210 = row.vatSum10 ?: empty
                        def code220 = row.saleCostARub ?: empty

                        // различаем юр. и физ. лица в строках 100 и 110
                        def code100inn, code100kpp, code110inn, code110kpp
                        def boolean isUL100 = false
                        def slashIndex = code100?.indexOf("/")
                        if (slashIndex > 0) {
                            isUL100 = true
                            code100inn = code100.substring(0, slashIndex)
                            code100kpp = code100.substring(slashIndex + 1)
                        }
                        def boolean isUL110 = false
                        slashIndex = code110?.indexOf("/")
                        if (slashIndex > 0) {
                            isUL110 = true
                            code110inn = code110.substring(0, slashIndex)
                            code110kpp = code110.substring(slashIndex + 1)
                        }

                        КнПродСтр(
                                [НомерПор: code005] +
                                        [НомСчФПрод: code020] +
                                        [ДатаСчФПрод: code030] +
                                        (code040 != null ? [НомИспрСчФ: code040] : [:]) +
                                        (code050 != null ? [ДатаИспрСчФ: code050] : [:]) +
                                        (code060 != null ? [НомКСчФПрод: code060] : [:]) +
                                        (code070 != null ? [ДатаКСчФПрод: code070] : [:]) +
                                        (code080 != null ? [НомИспрКСчФ: code080] : [:]) +
                                        (code090 != null ? [ДатаИспрКСчФ: code090] : [:]) +
                                        (code140 != null ? [ОКВ: code140] : [:]) +
                                        [СтоимПродСФВ: code150] +
                                        [СтоимПродСФ: code160] +
                                        [СтоимПродСФ18: code170] +
                                        [СтоимПродСФ10: code180] +
                                        [СтоимПродСФ0: code190] +
                                        [СумНДССФ18: code200] +
                                        [СумНДССФ10: code210] +
                                        [СтоимПродОсв: code220]
                        ) {
                            КодВидОпер(code010)
                            if (code120 != null && code130 != null) {
                                ДокПдтвОпл(
                                        НомДокПдтвОпл: code120,
                                        ДатаДокПдтвОпл: code130
                                )
                            }
                            if (code100 != null) {
                                СвПокуп() {
                                    if (isUL100) {
                                        СведЮЛ(
                                                ИННЮЛ: code100inn,
                                                КПП: code100kpp
                                        )
                                    } else {
                                        СведИП(
                                                ИННФЛ: code100
                                        )
                                    }
                                }
                            }
                            if (code110 != null) {
                                СвПос() {
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
                        }
                    }
                    if (!hasPage) {
                        КнПродСтр() {}
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
