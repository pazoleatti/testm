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

// Получение провайдера с использованием кеширования
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

void checkDepartmentParams(LogLevel logLevel) {
    def departmentId = declarationData.departmentId

    // Параметры подразделения
    def departmentParamList = getProvider(RefBook.DEPARTMENT_CONFIG_VAT).getRecords(getEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
    if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
        throw new Exception("Ошибка при получении настроек обособленного подразделения")
    }
    def departmentParam = departmentParamList?.get(0)

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
    // Параметры подразделения
    def departmentParam = getDepartmentParam()

    // атрибуты, заполняемые по настройкам подразделений
    def taxOrganCode = departmentParam?.TAX_ORGAN_CODE?.value
    def okvedCode = getRefBookValue(34, departmentParam?.OKVED_CODE?.value)?.CODE?.value
    def okato = getOkato(departmentParam?.OKTMO?.value)
    def taxPlaceTypeCode = getRefBookValue(2, departmentParam?.TAX_PLACE_TYPE_CODE?.value)?.CODE?.value
    def signatoryId = getRefBookValue(35, departmentParam?.SIGNATORY_ID?.value)?.CODE?.value
    def name = departmentParam?.NAME?.value
    def inn = departmentParam?.INN?.value
    def kpp = departmentParam?.KPP?.value
    def formatVersion = departmentParam?.FORMAT_VERSION?.value
    def surname = departmentParam?.SIGNATORY_SURNAME?.value
    def firstname = departmentParam?.SIGNATORY_FIRSTNAME?.value
    def lastname = departmentParam?.SIGNATORY_LASTNAME?.value
    def approveDocName = departmentParam?.APPROVE_DOC_NAME?.value
    def approveOrgName = departmentParam?.APPROVE_ORG_NAME?.value
    def reorgINN = departmentParam?.REORG_INN?.value
    def reorgKPP = departmentParam?.REORG_KPP?.value

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
            def dataRows9371 = formDataService.getDataRowHelper(formData)?.getAll()
            for (def row : dataRows9371) {
                if (row.getAlias() != null) {
                    // заполняем строку 190 отдельно по итоговой строке
                    code190 = row.nds
                    continue
                }
                rows9371.add(row)
            }

            corrNumber9371 = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0
        }
    }
    if (corrNumber > 0) {
        code001 = corrNumber == corrNumber9371 ? 0 : 1
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
                    def code130innUL = row.todo
                    def code130kpp = row.todo
                    def code130innFL = row.todo

                    // TODO как отличить ЮЛ от ФЛ?
                    def boolean isUL = true

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
                            if (isUL) {
                                СведЮЛ(
                                        ИННЮЛ: code130innUL,
                                        КПП: code130kpp
                                )
                            } else {
                                СведИП(
                                        ИННФЛ: code130innFL
                                )
                            }
                        }
                        СвПос() {
                            if (isUL) {
                                СведЮЛ(
                                        ИННЮЛ: code130innUL,
                                        КПП: code130kpp
                                )
                            } else {
                                СведИП(
                                        ИННФЛ: code130innFL
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