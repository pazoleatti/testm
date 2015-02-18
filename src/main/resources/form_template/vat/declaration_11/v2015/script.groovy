package form_template.vat.declaration_11.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat

/**
 * Декларация по НДС (раздел 11)
 *
 * declarationTemplateId=1017
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
    def inn = departmentParam?.INN?.value
    def formatVersion = departmentParam?.FORMAT_VERSION?.value

    // атрибуты элементов Файл и Документ
    def fileId = TaxType.VAT.declarationPrefix + ".11" + "_" +
            declarationData.taxOrganCode + "_" +
            declarationData.taxOrganCode + "_" +
            inn + "" + declarationData.kpp + "_" +
            (new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime()) + "_" +
            UUID.randomUUID().toString().toUpperCase()
    def index = "0000110"
    def corrNumber = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0

    // атрибуты, заполняемые по форме 937.3 (строка 001 отдельно, остальное в массиве sourceDataRows)
    def sourceDataRows = []
    def code001 = null
    def sourceCorrNumber
    for (def formData : declarationService.getAcceptedFormDataSources(declarationData).getRecords()) {
        if (formData.id == 619) {
            sourceDataRows = formDataService.getDataRowHelper(formData)?.getAll()
            sourceCorrNumber = reportPeriodService.getCorrectionNumber(formData.departmentReportPeriodId) ?: 0
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
                Индекс: index,
                НомКорр: corrNumber,
                ПризнСвед11: code001
        ) {
            ЖУчПолучСчФ() {
                def isFirstSection = true
                for (def row : sourceDataRows) {
                    if (row.getAlias() != null) {
                        isFirstSection = (row.getAlias() == "part_1")
                        continue
                    }
                    if (isFirstSection) {
                        continue
                    }
                    def code005 = row.rowNumber
                    def code010 = row.date
                    def code020 = row.opTypeCode
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
                    def code140 = getLastTextPart(row.currNameCode, "(\\w.{0,254}) ")
                    def code150 = row.cost
                    def code160 = row.vatSum
                    def code170 = row.diffDec
                    def code180 = row.diffInc
                    def code190 = row.diffVatDec
                    def code200 = row.diffVatInc

                    // различаем юр. и физ. лица в строках 110 и 120
                    def code110inn, code110kpp, code120inn, code120kpp
                    def boolean isUL110 = false
                    def slashIndex = code110?.indexOf("/")
                    if (slashIndex != 0) {
                        isUL110 = true
                        code110inn = code110.substring(0, slashIndex)
                        code110kpp = code110.substring(slashIndex + 1)
                    }
                    def boolean isUL120 = false
                    slashIndex = code120?.indexOf("/")
                    if (slashIndex != 0) {
                        isUL120 = true
                        code120inn = code120.substring(0, slashIndex)
                        code120kpp = code120.substring(slashIndex + 1)
                    }

                    ЖУчПолучСчФСтр(
                            НомерПор: code005,
                            ДатаПолуч: code010,
                            НомСчФПрод: code030,
                            ДатаСчФПрод: code040,
                            НомИспрСчФ: code050,
                            ДатаИспрСчФ: code060,
                            НомКСчФПрод: code070,
                            ДатаКСчФПрод: code080,
                            НомИспрКСчФ: code090,
                            ДатаИспрКСчФ: code100,
                            КодВидСд: code130,
                            ОКВ: code140,
                            СтоимТовСчФВс: code150,
                            СумНДССчФ: code160,
                            РазСтКСчФУм: code170,
                            РазСтКСчФУв: code180,
                            РазНДСКСчФУм: code190,
                            РазНДСКСчФУв: code200
                    ) {
                        КодВидОпер { code020 }
                        СвПрод() {
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
                        СвКомис() {
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
    }
}

def checkDeclarationFNS() {
    def declarationFnsId = 4
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    def declarationData = declarationService.getLast(declarationFnsId, declarationData.departmentId, reportPeriod.id)
    if (declarationData != null && bankDeclarationData.accepted) {
        def String event = (formDataEvent == FormDataEvent.MOVE_CREATED_TO_ACCEPTED) ? "Принять данную декларацию" : "Отменить принятие данной декларации"
        throw new ServiceException('%s невозможно, так как в текущем периоде и подразделении принята "Декларация по НДС (раздел 1-7)', event)
    }
}

def getNumber(def String str) {
    if (str != null && str.length > 10) {
        return str.substring(0, str.length - 10)
    }
    return null
}

def getDate(def String str) {
    if (str != null && str.length > 10) {
        return str.substring(str.length - 9)
    }
    return null
}

def getLastTextPart(String value, def pattern) {
    def parts = value?.split(pattern)
    return parts?.length == 2 ? parts[1] : null
}