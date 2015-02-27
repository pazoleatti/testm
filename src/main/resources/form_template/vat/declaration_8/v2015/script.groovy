package form_template.vat.declaration_8.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat

/**
 * Декларация по НДС (раздел 8)
 *
 * declarationTemplateId=1012
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
    def fileId = TaxType.VAT.declarationPrefix + ".8" + "_" +
            taxOrganCode + "_" +
            taxOrganCode + "_" +
            inn + "" + kpp + "_" +
            (new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime()) + "_" +
            UUID.randomUUID().toString().toUpperCase()
    def index = "0000080"
    def corrNumber = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0

    // атрибуты, заполняемые по форме 937.1 (строки 001 и 190 отдельно, остальное в массиве sourceDataRows)
    def sourceDataRows = []
    def code001 = null
    def code190 = empty
    def sourceCorrNumber
    for (def formData : declarationService.getAcceptedFormDataSources(declarationData).getRecords()) {
        if (formData.formType.id == 606) {
            sourceDataRows = formDataService.getDataRowHelper(formData)?.getAll()
            sourceCorrNumber = reportPeriodService.getCorrectionNumber(formData.departmentReportPeriodId) ?: 0
            code190 = getDataRow(sourceDataRows, 'total')?.nds ?: empty
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
            КнигаПокуп(
                    СумНДСВсКПк: code190
            ) {
                hasPage = false
                for (def row : sourceDataRows) {
                    if (row.getAlias() != null) {
                        continue
                    }
                    hasPage = true
                    def code005 = row.rowNum
                    def code010 = row.typeCode
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
                    def code160 = getLastTextPart(row.currency, "(\\w.{0,254}) ")
                    def code170 = row.cost
                    def code180 = row.nds

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
                            НомерПор: code005,
                            НомСчФПрод: code020,
                            ДатаСчФПрод: code030,
                            НомИспрСчФ: code040,
                            ДатаИспрСчФ: code050,
                            НомКСчФПрод: code060,
                            ДатаКСчФПрод: code070,
                            НомИспрКСчФ: code080,
                            ДатаИспрКСчФ: code090,
                            НомТД: code150,
                            ОКВ: code160,
                            СтоимПокупВ: code170,
                            СумНДСВыч: code180
                    ) {
                        КодВидОпер(code010)
                        ДокПдтвУпл(
                                НомДокПдтвУпл: code100,
                                ДатаДокПдтвУпл: code110
                        )
                        ДатаУчТов(code120)
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
                if (!hasPage) {
                    КнПокСтр() {}
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