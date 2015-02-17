package form_template.vat.declaration_10.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat

/**
 * Декларация по НДС (раздел 10)
 *
 * declarationTemplateId=1016
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
    def fileId = TaxType.VAT.declarationPrefix + ".10" + "_" +
            declarationData.taxOrganCode + "_" +
            declarationData.taxOrganCode + "_" +
            inn + "" + declarationData.kpp + "_" +
            (new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime()) + "_" +
            UUID.randomUUID().toString().toUpperCase()
    def index = "0000100"
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
                ПризнСвед10: code001
        ) {
            ЖУчВыстСчФ() {
                for (def row : sourceDataRows) {
                    if (row.getAlias() != null) {
                        continue
                    }
                    // TODO http://jira.aplana.com/browse/SBRFACCTAX-10400
                    def code005 = row.todo
                    def code010 = row.todo
                    def code020 = row.todo
                    def code030 = row.todo
                    def code040 = row.todo
                    def code050 = row.todo
                    def code060 = row.todo
                    def code070 = row.todo
                    def code080 = row.todo
                    def code090 = row.todo
                    def code100 = row.todo
                    def code110 = row.todo
                    def code120 = row.todo
                    def code130 = row.todo
                    def code140 = row.todo
                    def code150 = row.todo
                    def code160 = row.todo
                    def code170 = row.todo
                    def code180 = row.todo
                    def code190 = row.todo
                    def code200 = row.todo
                    def code210 = row.todo

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

                    ЖУчВыстСчФСтр(
                            НомерПор: code005,
                            ДатаВыст: code010,
                            НомСчФПрод: code030,
                            ДатаСчФПрод: code040,
                            НомИспрСчФ: code050,
                            ДатаИспрСчФ: code060,
                            НомКСчФПрод: code070,
                            ДатаКСчФПрод: code080,
                            НомИспрКСчФ: code090,
                            ДатаИспрКСчФ: code100
                    ) {
                        КодВидОпер { code020 }
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
                        СвПосрДеят(
                                НомСчФОтПрод: code130,
                                ДатаСчФОтПрод: code140,
                                ОКВ: code150,
                                СтоимТовСчФВс: code160,
                                СумНДССчФ: code170,
                                РазСтКСчФУм: code180,
                                РазСтКСчФУв: code190,
                                РазНДСКСчФУм: code200,
                                РазНДСКСчФУв: code210
                        )  {
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