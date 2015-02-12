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
    def fileId = TaxType.VAT.declarationPrefix + ".8" + "_" +
            declarationData.taxOrganCode + "_" +
            declarationData.taxOrganCode + "_" +
            inn + "" + declarationData.kpp + "_" +
            (new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime()) + "_" +
            UUID.randomUUID().toString().toUpperCase()
    def index = "0000080"
    def corrNumber = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0

    // атрибуты, заполняемые по форме 937.1 (строки 001 и 190 отдельно, остальное в массиве rows9371)
    def code001 = null
    def code190 = null
    def rows9371 = []
    def formDataList = declarationService.getAcceptedFormDataSources(declarationData).getRecords()
    def corrNumber9371
    for (def formData : formDataList) {
        if (formData.id == 607) {
            def sourceDataRows = formDataService.getDataRowHelper(formData)?.getAll()
            for (def row : sourceDataRows) {
                if (row.getAlias() != null) {
                    // заполняем строку 190 отдельно по итоговой строке
                    code190 = row.nds
                    continue
                }
                rows9371.add(row)
            }
            corrNumber9371 = reportPeriodService.getCorrectionNumber(formData.departmentReportPeriodId) ?: 0
        }
    }
    if (corrNumber > 0) {
        code001 = (corrNumber == corrNumber9371) ? 0 : 1
    }


    def builder = new MarkupBuilder(xml)
    builder.Файл(
            ИдФайл: fileId,
            ВерсПрог: applicationVersion,
            ВерсФорм: formatVersion) {
        Документ(
                Индекс: index,
                НомКорр: corrNumber,
                ПризнСвед8: code001
        ) {
            КнигаПокуп(
                    СумНДСВсКПк: code190
            ) {
                for (def row : rows9371) {
                    // TODO http://jira.aplana.com/browse/SBRFACCTAX-10383
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
                    def code120 = row.todo
                    def code150 = row.todo
                    def code160 = row.todo
                    def code170 = row.todo
                    def code180 = row.todo
                    def code100 = row.todo
                    def code110 = row.todo
                    def String code130 = row.todo
                    def code130inn
                    def code130kpp
                    def code140 = row.todo
                    def code140inn
                    def code140kpp

                    // различаем юр. и физ. лица в строках 130 и 140
                    def boolean isUL130 = false
                    if (code130.contains("/")) {
                        isUL130 = true
                        code130inn = code130.substring(0, code130.indexOf("/"))
                        code130kpp = code130.substring(code130.indexOf("/") + 1)
                    }
                    def boolean isUL140 = false
                    if (code140.contains("/")) {
                        isUL140 = true
                        code140inn = code140.substring(0, code140.indexOf("/"))
                        code140kpp = code140.substring(code140.indexOf("/") + 1)
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
                        КодВидОпер { code010 }
                        ДокПдтвУпл(
                                НомДокПдтвУпл: code100,
                                ДатаДокПдтвУпл: code110
                        )
                        ДатаУчТов { code120 }
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