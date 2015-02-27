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
    def (code230, code240, code250, code260, code270, code280) = [empty, empty, empty, empty, empty, empty]
    def sourceCorrNumber
    for (def formData : declarationService.getAcceptedFormDataSources(declarationData).getRecords()) {
        if (formData.formType.id == 608) {
            sourceDataRows = formDataService.getDataRowHelper(formData)?.getAll()
            sourceCorrNumber = reportPeriodService.getCorrectionNumber(formData.departmentReportPeriodId) ?: 0
            def totalRow = getDataRow(sourceDataRows, 'total')
            code230 = totalRow?.saleCostB18 ?: empty
            code240 = totalRow?.saleCostB10 ?: empty
            code250 = totalRow?.saleCostB0 ?: empty
            code260 = totalRow?.vatSum18 ?: empty
            code270 = totalRow?.vatSum10 ?: empty
            code280 = totalRow?.bonifSalesSum ?: empty
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
            КнигаПрод(
                    СтПродБезНДС18: code230,
                    СтПродБезНДС10: code240,
                    СтПродБезНДС0: code250,
                    СумНДСВсКПр18: code260,
                    СумНДСВсКПр10: code270,
                    СтПродОсвВсКПр: code280
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
                    def code140 = getLastTextPart(row.currNameCode, "(\\w.{0,254}) ")
                    def code150 = row.saleCostACurr
                    def code160 = row.saleCostARub
                    def code170 = row.saleCostB18
                    def code180 = row.saleCostB10
                    def code190 = row.saleCostB0
                    def code200 = row.vatSum18
                    def code210 = row.vatSum10
                    def code220 = row.saleCostARub

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
                            НомерПор: code005,
                            НомСчФПрод: code020,
                            ДатаСчФПрод: code030,
                            НомИспрСчФ: code040,
                            ДатаИспрСчФ: code050,
                            НомКСчФПрод: code060,
                            ДатаКСчФПрод: code070,
                            НомИспрКСчФ: code080,
                            ДатаИспрКСчФ: code090,
                            ОКВ: code140,
                            СтоимПродСФВ: code150,
                            СтоимПродСФ: code160,
                            СтоимПродСФ18: code170,
                            СтоимПродСФ10: code180,
                            СтоимПродСФ0: code190,
                            СумНДССФ18: code200,
                            СумНДССФ10: code210,
                            СтоимПродОсв: code220
                    ) {
                        КодВидОпер(code010)
                        ДокПдтвОпл(
                                НомДокПдтвОпл: code120,
                                ДатаДокПдтвОпл: code130
                        )
                        СвПокуп() {
                            if (isUL100) {
                                СведЮЛ(
                                        ИННЮЛ: code100inn,
                                        КПП: code100kpp
                                )
                            } else {
                                СведИП(
                                        ИННФЛ: code110
                                )
                            }
                        }
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
                if (!hasPage) {
                    КнПродСтр() {}
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
    if (str != null && str.length() > 11) {
        return str.substring(0, str.length() - 11)
    }
    return null
}

def getDate(def String str) {
    if (str != null && str.length() > 10) {
        return str.substring(str.length() - 10)
    }
    return null
}

def getLastTextPart(String value, def pattern) {
    def parts = value?.split(pattern)
    return parts?.length == 2 ? parts[1] : null
}
