package form_template.transport.declaration_1.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field
import groovy.xml.MarkupBuilder

/**
 * Декларация по транспортному налогу 5.03.
 *
 * declarationTypeId = 31
 * declarationTemplateId = 1031
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
        break
    case FormDataEvent.PRE_CALCULATION_CHECK:
        checkDepartmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.CALCULATE:
        generateXML()
        break
    default:
        return
}

// Кэш провайдеров
@Field
def providerCache = [:]
// Кэш значений справочника
@Field
def refBookCache = [:]
@Field
def recordCache = [:]

// значение подразделения из справочника 31
@Field
def departmentParam = null

// значение подразделения из справочника 310 (таблица)
@Field
def departmentParamTable = null

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

def getProvider(def long refBookId) {
    return formDataService.getRefBookProvider(refBookFactory, refBookId, providerCache)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecord(def Long refBookId, def String alias, def String value, def int rowIndex, def String columnName,
              def Date date, boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, date,
            rowIndex, columnName, logger, required)
}

void checkDepartmentParams(LogLevel logLevel) {
    def departmentId = declarationData.departmentId

    // Параметры подразделения
    def departmentParam = getDepartmentParam()
    def departmentParamTransportRow = getDepartmentParamTable(departmentParam.record_id.value)

    // Проверки подразделения
    def List<String> errorList = getErrorDepartment(departmentParamTransportRow)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для параметров текущего экземпляра декларации на форме настроек подразделений отсутствует значение атрибута %s!", error))
    }
    errorList = getErrorTaxPlaceTypeCode(departmentParamTransportRow)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для параметров текущего экземпляра декларации неверно указано значение атрибута %s на форме настроек подразделений!", error))
    }
    errorList = getErrorINN(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для данного подразделения на форме настроек подразделений отсутствует значение атрибута %s!", error))
    }

    // Справочник "Параметры представления деклараций по транспортному налогу"
    def regionId = getRefBookValue(30L, departmentId)?.REGION_ID?.value
    if (regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }
    def filter = String.format("DECLARATION_REGION_ID = %s and LOWER(TAX_ORGAN_CODE) = LOWER('%s') and LOWER(KPP) = LOWER('%s')",
            regionId, declarationData.taxOrganCode, declarationData.kpp)
    def records = refBookFactory.getDataProvider(210).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
    if (records.size() == 0) {
        throw new Exception("В справочнике «Параметры представления деклараций по транспортному налогу» отсутствует запись по выбранным параметрам декларации (период, регион подразделения, налоговый орган, КПП)!")
    }
}

def generateXML() {
    if (xml == null) {
        return
    }

    // проверка наличия источников в стутусе принят
    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData, userInfo, logger)

    /** Предпослденяя дата отчетного периода на которую нужно получить настройки подразделения из справочника. */
    if (getReportPeriodEndDate() == null) {
        logger.error("Ошибка определения даты конца отчетного периода")
    }

    // Получить параметры по транспортному налогу
    def departmentParam = getDepartmentParam()
    def departmentParamRow = getDepartmentParamTable(departmentParam?.record_id?.value)

    def reorgFormCode = getRefBookValue(5L, departmentParamRow?.REORG_FORM_CODE?.referenceValue)?.CODE?.stringValue
    def okvedCode = getRefBookValue(34L, departmentParamRow?.OKVED_CODE?.referenceValue)?.CODE?.stringValue
    def signatoryId = getRefBookValue(35L, departmentParamRow?.SIGNATORY_ID?.referenceValue)?.CODE?.numberValue
    def taxPlaceTypeCode = getRefBookValue(2L, departmentParamRow?.TAX_PLACE_TYPE_CODE?.referenceValue)?.CODE?.stringValue
    def approveDocName = departmentParamRow?.APPROVE_DOC_NAME?.value
    def approveOrgName = departmentParamRow?.APPROVE_ORG_NAME?.value

    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    // все строки сводных-источников
    def dataRows = getDataRows(formDataCollection, 200)
    // подитоговые строки
    def totalRows = dataRows.findAll { row ->
        def record210 = getRefBookValue(210L, row.kno)
        def kno = record210?.TAX_ORGAN_CODE?.value
        def kpp = record210?.KPP?.value
        return (row.getAlias()?.startsWith("total2") && kno == declarationData.taxOrganCode && kpp == declarationData.kpp)
    }
    // мапа со сгруппированными строками (подитоговая строка -> простые строки)
    def dataRowsMap = [:]
    totalRows.each { totalRow ->
        def rows = dataRows.findAll { row ->
            def record210 = getRefBookValue(210L, row.kno)
            def rowKno = record210?.TAX_ORGAN_CODE?.value
            def rowKpp = record210?.KPP?.value

            record210 = getRefBookValue(210L, totalRow.kno)
            def totalRowKno = record210?.TAX_ORGAN_CODE?.value
            def totalRowKpp = record210?.KPP?.value

            return (row.getAlias() == null && rowKno == totalRowKno && rowKpp == totalRowKpp && row.okato == totalRow.okato)
        }
        dataRowsMap.put(totalRow, rows)
    }

    def builder = new MarkupBuilder(xml)
    builder.Файл(
            ИдФайл   : generateXmlFileId(),
            ВерсПрог : applicationVersion,
            ВерсФорм : '5.03') {

        // Титульный лист
        Документ(
                КНД      : "1152004",
                ДатаДок  : (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
                Период   : reorgFormCode ? 50 : 34,
                ОтчетГод : reportPeriod.taxPeriod.year,
                КодНО    : declarationData.taxOrganCode,
                НомКорр  : reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId),
                ПоМесту  : taxPlaceTypeCode
        ) {
            def svnp = [ОКВЭД : okvedCode]
            def phone = departmentParamRow?.PHONE?.value
            if (departmentParamRow?.OKVED_CODE?.value && (phone != null) && !(phone.isEmpty())) {
                svnp.Тлф = phone
            }
            СвНП(svnp) {
                НПЮЛ(
                        НаимОрг : departmentParamRow?.NAME?.value,
                        ИННЮЛ   : departmentParam?.INN?.value,
                        КПП     : declarationData.kpp) {

                    if (reorgFormCode != null && !reorgFormCode.equals("")) {
                        СвРеоргЮЛ([ФормРеорг : reorgFormCode] +
                                (Integer.parseInt(reorgFormCode) in [1, 2, 3, 5, 6] ?
                                    [ИННЮЛ : departmentParamRow?.REORG_INN?.value, КПП : departmentParamRow?.REORG_KPP?.value] : [:])
                        )
                    }
                }
            }

            Подписант(ПрПодп : signatoryId) {
                ФИО(
                        Фамилия  : departmentParamRow?.SIGNATORY_SURNAME?.value,
                        Имя      : departmentParamRow?.SIGNATORY_FIRSTNAME?.value,
                        Отчество : departmentParamRow?.SIGNATORY_LASTNAME?.value
                )
                // СвПред - Сведения о представителе налогоплательщика
                if (signatoryId == 2) {
                    СвПред(
                            [НаимДок : approveDocName] +
                                    (approveOrgName ? [НаимОрг : approveOrgName] : [:])

                    )
                }
            }
            // Титульный лист - конец

            // Раздел 1
            ТрНалНД() {
                СумНалПУ(КБК : "18210604011021000110") {
                    dataRowsMap.each { totalRow, groupRows ->
                        СумПУ(
                                ОКТМО     : getRefBookValue(96L, totalRow.okato)?.CODE?.value,
                                НалИсчисл : getLong(totalRow.taxSumToPay),
                                АвПУКв1   : getLong(totalRow.q1),
                                АвПУКв2   : getLong(totalRow.q2),
                                АвПУКв3   : getLong(totalRow.q3),
                                НалПУ     : getLong(totalRow.q4)
                        ) {
                            // Раздел 1 - конец

                            // Раздел 2
                            groupRows.each { row ->
                                РасчНалТС(
                                        [
                                                КодВидТС  : getRefBookValue(42L, row.tsTypeCode)?.CODE?.stringValue,
                                                ИдНомТС   : row.vi,
                                                МаркаТС   : row.model,
                                                РегЗнакТС : row.regNumber,
                                        ] +
                                                [НалБаза  : row.taxBase] +
                                                [ОКЕИНалБаза : getRefBookValue(12L, row.taxBaseOkeiUnit)?.CODE?.stringValue] +
                                                (row.ecoClass ? [ЭкологКл : getRefBookValue(40L, row.ecoClass)?.CODE?.numberValue] : [:]) +
                                                [
                                                        ВыпускТС : row.years,
                                                        ВладенТС : row.ownMonths,
                                                        ДоляТС   : row.partRight,
                                                        КоэфКв   : row.coefKv
                                                ] +
                                                [НалСтавка : getRefBookValue(41L, row.taxRate)?.VALUE?.numberValue] +
                                                (row.coefKp ? [КоэфКп: row.coefKp] : [:]) +
                                                [СумИсчисл : getLong(row.calculatedTaxSum)] +
                                                (row.benefitMonths ? [ЛьготМесТС : row.benefitMonths] : [:]) +
                                                [СумИсчислУпл : getLong(row.taxSumToPay)] +
                                                (row.coefKl != null ? [КоэфКл : row.coefKl] : [:]),
                                ) {
                                    def record7 = getRefBookValue(7L, row.taxBenefitCode)
                                    def code = getRefBookValue(6L, record7?.TAX_BENEFIT_ID?.value)?.CODE?.value
                                    def base = record7?.BASE?.value

                                    // Код налоговой льготы в виде освобождения от налогообложения
                                    if (code != null && (code.equals('30200') || code.equals('20200') || code.equals('20210'))) {
                                        // вычисление КодОсвНал
                                        def valL = code
                                        def valX = ""
                                        if (!valL.equals("30200")) {
                                            valX = base
                                        }
                                        def kodOsnNal = (valL != "" ? valL.toString() : "0000") + (valX != '' ? "/" + valX : '')

                                        ЛьготОсвНал(
                                                КодОсвНал: kodOsnNal,
                                                СумОсвНал: getLong(row.taxBenefitSum)
                                        )
                                    }

                                    // Код налоговой льготы в виде уменьшения суммы налога
                                    if (code != null && code.equals("20220")) {
                                        // вычисление КодУменСум
                                        def valL = code
                                        def valX = base
                                        def kodUmenSum = (valL != "" ? valL.toString() : "0000") + "/" + valX

                                        ЛьготУменСум(
                                                КодУменСум : kodUmenSum,
                                                СумУменСум : getLong(row.taxBenefitSum)
                                        )
                                    }

                                    // Код налоговой льготы в виде снижения налоговой ставки
                                    if (code != null && code.equals("20230")) {
                                        // вычисление КодСнижСтав
                                        def valL = code
                                        def valX = base
                                        def kodNizhStav = (valL != "" ? valL.toString() : "0000") + "/" + valX

                                        ЛьготСнижСтав(
                                                КодСнижСтав : kodNizhStav,
                                                СумСнижСтав : getLong(row.taxBenefitSum)
                                        )
                                    }
                                }
                            }
                            // Раздел 2 - конец
                        }
                    }
                }
            }
        }
    }
}

/** Получить строки формы. */
def getDataRows(def formDataCollection, def formTypeId) {
    def formList = formDataCollection.records.findAll { it.getFormType().getId() == formTypeId }
    def dataRows = []
    for (def form : formList) {
        dataRows += (formDataService.getDataRowHelper(form)?.getAll() ?: [])
    }
    return dataRows.isEmpty() ? null : dataRows
}

List<String> getErrorDepartment(def record) {
    List<String> errorList = new ArrayList<String>()
    if (record.NAME?.stringValue == null || record.NAME.stringValue.isEmpty()) {
        errorList.add("«Наименование подразделения»")
    }
    if (record.KPP?.stringValue == null || record.KPP.stringValue.isEmpty()) {
        errorList.add("«КПП»")
    }
    if (record.TAX_ORGAN_CODE?.stringValue == null || record.TAX_ORGAN_CODE.stringValue.isEmpty()) {
        errorList.add("«Код налогового органа»")
    }
    if (record.OKVED_CODE?.referenceValue == null) {
        errorList.add("«Код вида экономической деятельности и по классификатору ОКВЭД»")
    }
    def reorgFormCode = getRefBookValue(5L, record?.REORG_FORM_CODE?.referenceValue)?.CODE?.stringValue
    if (reorgFormCode != null && !reorgFormCode.equals("") && Integer.parseInt(reorgFormCode) in [1, 2, 3, 5, 6]) {
        if (record.REORG_INN?.stringValue == null || record.REORG_INN.stringValue.isEmpty()) {
            errorList.add("«ИНН реорганизованного обособленного подразделения»")
        }
        if (record.REORG_KPP?.stringValue == null || record.REORG_KPP.stringValue.isEmpty()) {
            errorList.add("«КПП реорганизованного обособленного подразделения»")
        }
    }
    if (record.SIGNATORY_ID?.referenceValue == null) {
        errorList.add("«Признак лица подписавшего документ»")
    }
    if (record.SIGNATORY_SURNAME?.stringValue == null || record.SIGNATORY_SURNAME.stringValue.isEmpty()) {
        errorList.add("«Фамилия подписанта»")
    }
    if (record.SIGNATORY_FIRSTNAME?.stringValue == null || record.SIGNATORY_FIRSTNAME.stringValue.isEmpty()) {
        errorList.add("«Имя подписанта»")
    }
    def signatoryId = getRefBookValue(35L, record?.SIGNATORY_ID?.referenceValue)?.CODE?.numberValue
    if (signatoryId != null && signatoryId != 1) {
        if (record.APPROVE_DOC_NAME?.stringValue == null || record.APPROVE_DOC_NAME.stringValue.isEmpty()) {
            errorList.add("«Наименование документа, подтверждающего полномочия представителя»")
        }
    }
    if (record.TAX_ORGAN_CODE_PROM?.value == null || record.TAX_ORGAN_CODE_PROM.value.isEmpty()) {
        errorList.add("«Код налогового органа (пром.)»")
    }
    errorList
}

List<String> getErrorTaxPlaceTypeCode(def record) {
    List<String> errorList = new ArrayList<String>()
    def code = record.TAX_PLACE_TYPE_CODE?.referenceValue
    if (code == null || !(getRefBookValue(2L, code)?.CODE?.stringValue in ['213', '216', '260'])) {
        errorList.add("«Код места, по которому представляется документ»")
    }
    errorList
}

List<String> getErrorINN(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.INN == null || record.INN.stringValue == null || record.INN.stringValue.isEmpty()) {
        errorList.add("«ИНН»")
    }
    errorList
}

// Получить параметры подразделения (из справочника 31)
def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = declarationData.departmentId
        def departmentParamList = getProvider(31L).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

// Получить параметры подразделения (из справочника 310)
def getDepartmentParamTable(def departmentParamId) {
    if (departmentParamTable == null) {
        def filter = "LINK = $departmentParamId and TAX_ORGAN_CODE ='${declarationData.taxOrganCode}' and KPP ='${declarationData.kpp}'"
        def departmentParamTableList = getProvider(310L).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
}

def getLong(def value) {
    return ((BigDecimal) value)?.setScale(0, BigDecimal.ROUND_HALF_UP)
}

def generateXmlFileId() {
    def departmentParam = getDepartmentParam()
    def departmentParamTransportRow = departmentParam ? getDepartmentParamTable(departmentParam?.record_id?.value) : null
    if (departmentParam && departmentParamTransportRow) {
        def r_t = TaxType.TRANSPORT.declarationPrefix
        def a = departmentParamTransportRow?.TAX_ORGAN_CODE_PROM?.value
        def k = departmentParamTransportRow?.TAX_ORGAN_CODE?.value
        def o = departmentParam?.INN?.value + departmentParamTransportRow?.KPP?.value
        def date = Calendar.getInstance().getTime()?.format("yyyyMMdd")
        def n = UUID.randomUUID().toString().toUpperCase()
        // R_T_A_K_O_GGGGMMDD_N
        def fileId = r_t + '_' +
                a + '_' +
                k + '_' +
                o + '_' +
                date + '_' +
                n
        return fileId
    }
    return null
}