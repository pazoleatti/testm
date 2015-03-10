package form_template.vat.declaration_9_1.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat

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
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
        checkDepartmentParams(LogLevel.ERROR)
        checkDeclarationFNS()
        break
    case FormDataEvent.MOVE_ACCEPTED_TO_CREATED:
        checkDeclarationFNS()
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
    def fileId = TaxType.VAT.declarationPrefix + ".91" + "_" +
            taxOrganCode + "_" +
            taxOrganCode + "_" +
            inn + "" + kpp + "_" +
            (new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime()) + "_" +
            UUID.randomUUID().toString().toUpperCase()
    def index = "0000091"
    def corrNumber = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0

    // атрибуты, заполняемые по форме 937.2.1 (строки 001, 020-070 и 310-360 отдельно, остальное в массиве sourceDataRows)
    def sourceDataRows = []
    def code001 = null
    def (code020, code030, code040, code050, code060, code070, code310, code320, code330, code340, code350, code360) =
    [null, null, null, null, null, null, null, null, null, null, null, null]
    def sourceCorrNumber
    for (def formData : declarationService.getAcceptedFormDataSources(declarationData).getRecords()) {
        if (formData.formType.id == 617) {
            sourceDataRows = formDataService.getDataRowHelper(formData)?.getAll()
            sourceCorrNumber = reportPeriodService.getCorrectionNumber(formData.departmentReportPeriodId) ?: 0
            def headRow = getDataRow(sourceDataRows, 'head') // "Итого"
            def totalRow = getDataRow(sourceDataRows, 'total') // "Всего"
            code020 = headRow?.saleCostB18
            code030 = headRow?.saleCostB10
            code040 = headRow?.saleCostB0
            code050 = headRow?.vatSum18
            code060 = headRow?.vatSum10
            code070 = headRow?.bonifSalesSum
            code310 = totalRow?.saleCostB18
            code320 = totalRow?.saleCostB10
            code330 = totalRow?.saleCostB0
            code340 = totalRow?.vatSum18
            code350 = totalRow?.vatSum10
            code360 = totalRow?.bonifSalesSum
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
                        (code001 != null ? [ПризнСвед91: code001] : [:])
        ) {
            if(code001 != 1) {
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
                    hasPage = false
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
                        def code230 = row.saleCostACurr
                        def code240 = row.saleCostARub
                        def code250 = row.saleCostB18
                        def code260 = row.saleCostB10
                        def code270 = row.saleCostB0
                        def code280 = row.vatSum18
                        def code290 = row.vatSum10
                        def code300 = row.bonifSalesSum

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
                            ДокПдтвОпл(
                                    (code200 != null ? [НомДокПдтвОпл: code200] : [:]) +
                                            (code210 != null ? [ДатаДокПдтвОпл: code210] : [:])
                            )
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
                    if (!hasPage) {
                        КнПродДЛСтр() {}
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
        throw new ServiceException('%s невозможно, так как в текущем периоде и подразделении принята "Декларация по НДС (раздел 1-7)', event)
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