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
    // Параметры подразделения
    def departmentParam = getDepartmentParam()

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

    def fileId = TaxType.VAT.declarationPrefix + ".10" + "_" +
            declarationData.taxOrganCode + "_" +
            declarationData.taxOrganCode + "_" +
            inn + "" + declarationData.kpp + "_" +
            (new SimpleDateFormat("yyyyMMdd")).format(Calendar.getInstance().getTime()) + "_" +
            UUID.randomUUID().toString().toUpperCase()
    def index = "0000100"
    def corrNumber = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId) ?: 0

    // TODO получение остальных данных для заполнения

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
                    def code130 = row.todo
                    def code130inn
                    def code130kpp
                    def code140 = row.todo
                    def code140inn
                    def code140kpp

                    // различаем юр. и физ. лица в строках 130 и 140
                    def boolean isUL130 = false
                    def slashIndex = code130?.indexOf("/")
                    if (slashIndex != 0) {
                        isUL130 = true
                        code130inn = code130.substring(0, slashIndex)
                        code130kpp = code130.substring(slashIndex + 1)
                    }
                    def boolean isUL140 = false
                    slashIndex = code140?.indexOf("/")
                    if (slashIndex != 0) {
                        isUL140 = true
                        code140inn = code140.substring(0, slashIndex)
                        code140kpp = code140.substring(slashIndex + 1)
                    }

                    ЖУчВыстСчФСтр(
                            НомерПор: code005,
                            ДатаВыст: code020,
                            НомСчФПрод: code030,
                            ДатаСчФПрод: code040,
                            НомИспрСчФ: code050,
                            ДатаИспрСчФ: code060,
                            НомКСчФПрод: code070,
                            ДатаКСчФПрод: code080,
                            НомИспрКСчФ: code090,
                            ДатаИспрКСчФ: code150
                    ) {
                        КодВидОпер { code010 }
                        СвПокуп() {
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
                        СвПосрДеят(
                                НомСчФОтПрод: code,
                                ДатаСчФОтПрод: code,
                                ОКВ: code,
                                СтоимТовСчФВс: code,
                                СумНДССчФ: code,
                                РазСтКСчФУм: code,
                                РазСтКСчФУв: code,
                                РазНДСКСчФУм: code,
                                РазНДСКСчФУв: code
                        )  {
                            СвПрод() {
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