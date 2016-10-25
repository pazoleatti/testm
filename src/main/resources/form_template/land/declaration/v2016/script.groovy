package form_template.land.declaration.v2016

import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field
import groovy.xml.MarkupBuilder

import java.math.RoundingMode

/**
 * Налоговая декларация по земельному налогу
 * версия 2016 года
 * declarationTemplateId=1030
 */

switch (formDataEvent) {
//    case FormDataEvent.CREATE: // создать / обновить
//        checkDepartmentParams(LogLevel.WARNING)
//        break
    case FormDataEvent.CHECK: // проверить
        if (!declarationData.accepted) {
            checkDepartmentParams(LogLevel.ERROR)
        }
        break
//    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED: // принять из создана
//        checkDepartmentParams(LogLevel.ERROR)
//        break
    case FormDataEvent.PRE_CALCULATION_CHECK:
//        checkDepartmentParams(LogLevel.WARNING)
        checkDepartmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.CALCULATE:
//        checkDepartmentParams(LogLevel.WARNING)
        generateXML()
        break
    default:
        return
}

@Field
def version = '5.03'

// Кэш провайдеров
@Field
def providerCache = [:]
// Кэш значений справочника
@Field
def refBookCache = [:]

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

// значение подразделения из справочника 700
@Field
def departmentParam = null

// значение подразделения из справочника 710 (таблица)
@Field
def departmentParamTable = null

/** Получить строки формы. */
def getDataRows(def formDataCollection, def formTypeId) {
    List<FormData> formList = formDataCollection.records.findAll { it.getFormType().getId() == formTypeId }
    def dataRows = []
    for (def form : formList) {
        dataRows += (formDataService.getDataRowHelper(form)?.getAll()?:[])
    }
    return dataRows.isEmpty() ? null : dataRows
}

void checkDepartmentParams(LogLevel logLevel) {
    // Параметры подразделения
    def departmentParam = getDepartmentParam()
    def departmentParamRow = getDepartmentParamTable(departmentParam.record_id.value)

    def taxPlaceTypeCode = getRefBookValue(2, departmentParamRow.TAX_PLACE_TYPE_CODE?.value)?.CODE?.value
    // Проверка заполнения полей формы настроек подразделений
    def List<String> errorList = getErrorDepartment(departmentParamRow, taxPlaceTypeCode)
    for (String error : errorList) {
        logger.log(logLevel, "На форме настроек подразделений для КНО = %s и КПП = %s не заполнено поле «%s»", declarationData.taxOrganCode, declarationData.kpp, error)
    }
    // Проверка заполнения информации о реорганизации и ликвидации
    def reorgFormCode = getRefBookValue(5, departmentParamRow?.REORG_FORM_CODE?.referenceValue)?.CODE?.stringValue
    if (reorgFormCode != null && !reorgFormCode.equals("") && Integer.parseInt(reorgFormCode) in [1, 2, 3, 5, 6]) {
        if (departmentParamRow.REORG_INN?.stringValue == null || departmentParamRow.REORG_INN.stringValue.isEmpty() || departmentParamRow.REORG_KPP?.stringValue == null || departmentParamRow.REORG_KPP.stringValue.isEmpty()) {
            logger.log(logLevel, "На форме настроек подразделений для КНО = %s и КПП = %s информация о реорганизации и ликвидации заполнена не полностью", declarationData.taxOrganCode, declarationData.kpp)
        }
    }
    // Проверка заполнения информации о представителе налогоплательщика
    def signatoryId = getRefBookValue(35, departmentParamRow?.SIGNATORY_ID?.referenceValue)?.CODE?.numberValue
    if ((signatoryId == 2) && (!departmentParamRow.APPROVE_DOC_NAME?.value || !departmentParamRow.APPROVE_ORG_NAME?.value)) {
        logger.log(logLevel, "На форме настроек подразделений для КНО = %s и КПП = %s информация о представителе налогоплательщика заполнена не полностью", declarationData.taxOrganCode, declarationData.kpp)
    }

    if (taxPlaceTypeCode != null && (taxPlaceTypeCode in ['250', '251']) && !departmentParamRow.PRODUCT_AGREEMENT_NAME?.value) {
        logger.log(logLevel, "На форме настроек подразделений для КНО = %s и КПП = %s не заполнено поле «Наименование соглашения о разделе продукции»", declarationData.taxOrganCode, declarationData.kpp)
    }
}

/** Осуществление проверк при создании + генерация xml. */
def generateXML() {

    def empty = 0
    int precision = 20

    // Параметры подразделения
    def departmentParams = getDepartmentParam()
    def departmentParamRow = getDepartmentParamTable(departmentParams.record_id.value)

    def knd = "1153005"
    def versionFormat = version
    def reorgFormCode = getRefBookValue(5, departmentParamRow?.REORG_FORM_CODE?.referenceValue)?.CODE?.stringValue
    def okvedCode = getRefBookValue(34, departmentParamRow?.OKVED_CODE?.referenceValue)?.CODE?.stringValue
    def taxPlaceTypeCode = getRefBookValue(2, departmentParamRow?.TAX_PLACE_TYPE_CODE?.referenceValue)?.CODE?.stringValue
    def phone = departmentParamRow?.PHONE?.value
    def name = departmentParamRow?.NAME?.value
    def inn = departmentParams?.INN?.value
    def kpp = declarationData.kpp
    def kno = declarationData.taxOrganCode
    def reorgInn = departmentParamRow?.REORG_INN?.value
    def reorgKpp = departmentParamRow?.REORG_KPP?.value
    def prPodp = getRefBookValue(35, departmentParamRow?.SIGNATORY_ID?.referenceValue)?.CODE?.numberValue
    def signatorySurname = departmentParamRow?.SIGNATORY_SURNAME?.value
    def signatoryFirstName = departmentParamRow?.SIGNATORY_FIRSTNAME?.value
    def signatoryLastName = departmentParamRow?.SIGNATORY_LASTNAME?.value
    def approveDocName = departmentParamRow?.APPROVE_DOC_NAME?.value
    def approveOrgName = departmentParamRow?.APPROVE_ORG_NAME?.value
    def productAgreementName = departmentParamRow.PRODUCT_AGREEMENT_NAME?.value
    def prepayment = getRefBookValue(38, departmentParamRow.PREPAYMENT.value)?.VALUE?.value
    boolean isPrepayment = "Да".equals(prepayment)

    // проверка наличия источников в стутусе принят
    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData, userInfo, logger)

    // Расчет земельного налога по земельным участкам, подлежащим включению в декларацию
    def dataRows = getDataRows(formDataCollection, 918)
    // фильтруем фиксированные 'ИТОГО' строки по КНО и КПП (ОКТМО отличаются)
    def totalRows = dataRows.findAll { row ->
        row.getAlias() != null && row.getAlias().startsWith("total2") && row.kno == kno && row.kpp == kpp
    }

    if (xml == null) {
        return
    }

    // map [(фиксированная ИТОГО) : [нефиксированные строки]]
    def dataRowsMap = [:]
    totalRows.each { totalRow ->
        dataRowsMap.put(totalRow, dataRows.findAll { row ->
            row.getAlias() == null && row.kno == totalRow.kno && row.kpp == totalRow.kpp && row.oktmo == totalRow.oktmo
        })
    }
    def builder = new MarkupBuilder(xml)
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    builder.Файл(
            ИдФайл: generateXmlFileId(departmentParams, departmentParamRow),
            ВерсПрог: applicationVersion,
            ВерсФорм: versionFormat) {
        Документ(
                НомКорр: reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId),
                Период: reorgFormCode ? 50 : 34,
                ОтчетГод: reportPeriod.taxPeriod.year,
                КодНО: declarationData.taxOrganCode,
                ПоМесту: taxPlaceTypeCode,
                ДатаДок: (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
                КНД: knd
        ) {
            СвНП(
                    [ОКВЭД : okvedCode] + (phone ? [Тлф : phone] : [:])) {
                НПЮЛ(
                        НаимОрг : name,
                        ИННЮЛ : inn,
                        КПП : kpp) {
                    if (reorgFormCode != null && !reorgFormCode.equals("")) {
                        СвРеоргЮЛ([ФормРеорг: reorgFormCode] +
                                (Integer.parseInt(reorgFormCode) in [1, 2, 3, 5, 6] ?
                                        [ИННЮЛ: reorgInn, КПП: reorgKpp] : [])
                        )
                    }
                }
            }

            Подписант(ПрПодп : prPodp) {
                ФИО(
                        [Фамилия : signatorySurname, Имя : signatoryFirstName] +
                                (signatoryLastName != null ? [Отчество : signatoryLastName] : [:]))
                if (prPodp == 2) {
                    СвПред(
                            [НаимДок : approveDocName] +
                                    (approveOrgName != null ? [НаимОрг : approveOrgName] : [:] )
                    )
                }
            }
            // 1..1
            ЗемНалНД((taxPlaceTypeCode in ['250', '251']) ? [НаимСРП : productAgreementName] : [:]) {
                dataRowsMap.each { totalRow, groupRows ->
                    def lastRow = groupRows[groupRows.size() - 1]
                    def kbk = getRefBookValue(703, lastRow.kbk).CODE.value
                    // ОКТМО
                    def oktmo = getRefBookValue(96, totalRow.oktmo).CODE.value
                    groupRows.each { row ->
                        // НалИсчисл
                        def nalIschisl = row.q1 + row.q2 + row.q3 + row.year
                        // АвПУКв1
                        def avPUKv1 = isPrepayment ? row.q1 : empty
                        // АвПУКв2
                        def avPUKv2 = isPrepayment ? row.q2 : empty
                        // АвПУКв3
                        def avPUKv3 = isPrepayment ? row.q3 : empty
                        // НалПУ
                        def nalPU = nalIschisl - (avPUKv1 + avPUKv2 + avPUKv3)
                        // НомКадастрЗУ
                        def nomKadastrZU = row.cadastralNumber
                        // КатегорЗем
                        def kategorZem = getRefBookValue(702, row.landCategory).CODE.value
                        // ПерСтр
                        def perStr = getRefBookValue(701, row.constructionPhase)?.CODE?.value
                        // СтКадастрЗУ
                        BigDecimal stKadastrZU = getLong(row.cadastralCost)
                        // ДоляЗУ
                        String dolyaZU = row.taxPart
                        // НалСтав
                        BigDecimal nalStav = row.taxRate
                        String[] partArray = dolyaZU?.split('/') ?: ['1', '1']
                        //BigDecimal dolyaZUValue = (partArray[0].toBigDecimal()) / (partArray[1].toBigDecimal())
                        // Строка  050 * 060
                        BigDecimal nalBazaPlus = stKadastrZU.multiply(partArray[0].toBigDecimal()).divide(partArray[1].toBigDecimal(), precision, BigDecimal.ROUND_HALF_UP) // неполный расчет
                        // СумНалИсчисл
                        BigDecimal kolMesVlZU = row.period
                        // Кв
                        BigDecimal kv = row.kv
                        // Кл
                        BigDecimal kl = row.kl
                        // Параметры налоговых льгот земельного налога
                        def landBenefit = null
                        String benefitCode = null
                        def benefitParam = ''
                        // Если графа 15 заполнена
                        if (row.benefitCode != null) {
                            landBenefit = getRefBookValue(705, row.benefitCode)
                            benefitCode = getRefBookValue(704, landBenefit.TAX_BENEFIT_ID.value)?.CODE?.value
                            benefitParam = landBenefit?.REDUCTION_PARAMS?.value
                        }
                        // КодНалЛьгот
                        def kodNalLgot
                        // СумНеОбл
                        BigDecimal sumNeObl
                        // СумЛьг
                        BigDecimal sumLg = BigDecimal.ZERO
                        // ДоляПлЗУ
                        String dolyaPlZU
                        BigDecimal nalBazaMinus = BigDecimal.ZERO
                        if(benefitCode == '3022100'){
                            kodNalLgot = "3022100/" + benefitParam
                            sumNeObl = getLong(landBenefit.REDUCTION_SUM.value)
                            nalBazaMinus = sumNeObl
                        } else if(benefitCode?.startsWith('30212')){
                            kodNalLgot = benefitCode
                            sumNeObl = getLong(landBenefit.REDUCTION_SUM.value)
                            nalBazaMinus = sumNeObl
                        } else if(benefitCode == '3022300'){
                            kodNalLgot = "3022300/" + benefitParam
                            dolyaPlZU = landBenefit.REDUCTION_SEGMENT.value
                            String[] reductArray = dolyaPlZU?.split('/') ?: ['1', '1']
                            //BigDecimal dolyaPlZUValue = (reductArray[0].toBigDecimal()) / (reductArray[1].toBigDecimal())
                            // Строка 050 * Строка 060 * Строка 120 * (1 - графа 23 формы)
                            nalBazaMinus = (stKadastrZU * (partArray[0].toBigDecimal()) * (1 - kl) * (reductArray[0].toBigDecimal())).divide(reductArray[1].toBigDecimal() * partArray[1].toBigDecimal(), precision, BigDecimal.ROUND_HALF_UP)
                        } else if (benefitCode == '3022400') {
                            kodNalLgot = "3022400/" + benefitParam
                            sumLg = row.sum
                        } else if (benefitCode?.startsWith('30211')) {
                            kodNalLgot = benefitCode
                            sumLg = row.sum
                        } else if (benefitCode == '3022200') {
                            kodNalLgot = "3022200/" + benefitParam
                            sumLg = row.sum
                        } else if (benefitCode == '3022500') {
                            kodNalLgot = "3022500/" + benefitParam
                            sumLg = row.sum
                        }
                        // НалБаза
                        BigDecimal nalBaza = getLong(nalBazaPlus - nalBazaMinus) // дорасчет
                        // СумНалИсчисл
                        BigDecimal sumNalIschisl = BigDecimal.ZERO
                        if(perStr == 1) {
                            sumNalIschisl = ("2".toBigDecimal() * nalBaza * nalStav * kv).divide("100".toBigDecimal(), 0, BigDecimal.ROUND_HALF_UP)
                        } else if(perStr == 2) {
                            sumNalIschisl = ("4".toBigDecimal() * nalBaza * nalStav * kv).divide("100".toBigDecimal(), 0, BigDecimal.ROUND_HALF_UP)
                        } else {
                            sumNalIschisl = (nalBaza * nalStav * kv).divide("100".toBigDecimal(), 0, BigDecimal.ROUND_HALF_UP)
                        }
                        // СумНалУплат
                        BigDecimal sumNalUplat = getLong(sumNalIschisl - sumLg) // дорасчет
                        // КолМесЛьгот
                        BigDecimal kolMesLgot = row.benefitPeriod
                        // 1..n
                        СумПУ(КБК: kbk,
                                ОКТМО: oktmo,
                                НалИсчисл: nalIschisl,
                                АвПУКв1: avPUKv1,
                                АвПУКв2: avPUKv2,
                                АвПУКв3: avPUKv3,
                                НалПУ: nalPU
                        ) {
                            // 1..n (у нас одна)
                            РасчПлатЗН(
                                    [НомКадастрЗУ: nomKadastrZU,
                                     КатегорЗем: kategorZem] +
                                    (perStr ? [ПерСтр: perStr] : [:]) +
                                    [СтКадастрЗУ: stKadastrZU] +
                                    (dolyaZU ? [ДоляЗУ: dolyaZU] : [:]) +
                                    [НалСтав: nalStav]
                            ){
                                // 1..1
                                ОпрНалБаза(НалБаза: (nalBaza)) {
                                    if(benefitCode == '3022100'){
                                        Льгот387_2Сум(КодНалЛьгот: kodNalLgot, СумНеОбл: sumNeObl)
                                    } else if(benefitCode?.startsWith('30212')){
                                        Льгот391_5(КодНалЛьгот: kodNalLgot, СумНеОбл: sumNeObl)
                                    } else if(benefitCode == '3022300'){
                                        Льгот387_2Пл(КодНалЛьгот: kodNalLgot, ДоляПлЗУ: dolyaPlZU)
                                    }
                                }
                                СумНалИсчисл(
                                        [КолМесВлЗУ: kolMesVlZU,
                                        Кв: kv,
                                        СумНалИсчисл: sumNalIschisl] +
                                        (kolMesLgot ? [КолМесЛьгот: kolMesLgot] : [:]) +
                                        [Кл: kl,
                                        СумНалУплат: sumNalUplat]
                                ) {
                                    if(benefitCode == '3022400'){
                                        Льгот387_2Осв(КодНалЛьгот: kodNalLgot, СумЛьг: sumLg)
                                    } else if(benefitCode?.startsWith('30211')){
                                        Льгот395(КодНалЛьгот: kodNalLgot, СумЛьг: sumLg)
                                    } else if(benefitCode == '3022200'){
                                        Льгот387_2УмСум(КодНалЛьгот: kodNalLgot, СумЛьг: sumLg)
                                    } else if(benefitCode == '3022500'){
                                        Льгот387_2СнСтав(КодНалЛьгот: kodNalLgot, СумЛьг: sumLg)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

List<String> getErrorDepartment(def record, def taxPlaceTypeCode) {
    List<String> errorList = new ArrayList<String>()
    if (taxPlaceTypeCode == null || taxPlaceTypeCode.isEmpty()) {
        errorList.add("«Код по месту нахождения (учета)»")
    }
    if (record.NAME?.stringValue == null || record.NAME.stringValue.isEmpty()) {
        errorList.add("«Наименование (налогоплательщик)»")
    }
    if (record.OKVED_CODE?.referenceValue == null) {
        errorList.add("«ОКВЭД»")
    }
    if (record.SIGNATORY_ID?.referenceValue == null) {
        errorList.add("«Признак лица подписавшего документ»")
    }
    if (record.SIGNATORY_SURNAME?.stringValue == null || record.SIGNATORY_SURNAME.stringValue.isEmpty()) {
        errorList.add("«Фамилия»")
    }
    if (record.SIGNATORY_FIRSTNAME?.stringValue == null || record.SIGNATORY_FIRSTNAME.stringValue.isEmpty()) {
        errorList.add("«Имя»")
    }
    return errorList
}

/**
 * Получение провайдера с использованием кеширования
 * @param providerId
 * @return
 */
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

// Разыменование с использованием кеширования
def getRefBookValue(def refBookId, def recordId) {
    if (refBookId == null || recordId == null) {
        return null
    }
    def key = getRefBookCacheKey(refBookId, recordId)
    if (!refBookCache.containsKey(key)) {
        refBookCache.put(key, refBookService.getRecordData(refBookId, recordId))
    }
    return refBookCache.get(key)
}

// Получить параметры подразделения (из справочника 700)
def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = declarationData.departmentId
        def departmentParamList = getProvider(700).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
        if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

// Получить параметры подразделения (из справочника 710)
def getDepartmentParamTable(def departmentParamId) {
    if (departmentParamTable == null) {
        def filter = "LINK = $departmentParamId and TAX_ORGAN_CODE ='${declarationData.taxOrganCode}' and KPP ='${declarationData.kpp}'"
        def departmentParamTableList = getProvider(710).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
}

def generateXmlFileId(def departmentParam, def departmentParamRow) {
    if (departmentParam && departmentParamRow) {
        def r_t = TaxType.LAND.declarationPrefix
        def a = departmentParamRow?.TAX_ORGAN_CODE_PROM?.value
        def k = departmentParamRow?.TAX_ORGAN_CODE?.value
        def o = departmentParam?.INN?.value + departmentParamRow?.KPP?.value
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
// Получить округленное, целочисленное значение.
def getLong(def value) {
    if (value == null) {
        return 0
    }
    return ((BigDecimal) value).setScale(0, BigDecimal.ROUND_HALF_UP)
}