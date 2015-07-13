package form_template.property.declaration.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field
import groovy.xml.MarkupBuilder

import java.math.RoundingMode

/**
 * Декларация по налогу на имущество
 *
 * declarationTemplateId=3000
 *
 * @author bkinzyabulatov
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE : // создать
        checkDepartmentParams(LogLevel.WARNING)
        break
    case FormDataEvent.CHECK : // проверить
        checkDepartmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED : // принять из создана
        checkDepartmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.PRE_CALCULATION_CHECK:
        checkDepartmentParams(LogLevel.WARNING)
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

// Кэш значений справочника
@Field
def refBookCache = [:]

@Field
def empty = 0

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

void checkDepartmentParams(LogLevel logLevel) {
    def departmentId = declarationData.departmentId

    // Параметры подразделения
    def departmentParam = getProvider(99).getRecords(getEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
    if (departmentParam == null ||  departmentParam.size() ==0 || departmentParam.get(0) == null) {
        throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
    }
    departmentParam = departmentParam.get(0)

    // Параметры подразделения (таблица)
    def departmentParamAdd = getProvider(206).getRecords(getEndDate() - 1, null, "LINK = ${departmentParam.record_id.value} AND LOWER(TAX_ORGAN_CODE) = LOWER('${declarationData.taxOrganCode}') AND LOWER(KPP) = LOWER('${declarationData.kpp}')", null)
    if (departmentParamAdd == null ||  departmentParamAdd.size() ==0 || departmentParamAdd.get(0) == null) {
        throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
    }
    departmentParamAdd = departmentParamAdd.get(0)

    // Проверки подразделения
    def List<String> errorList = getErrorDepartment(departmentParam, departmentParamAdd)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для параметров текущего экземпляра декларации на форме настроек подразделений отсутствует значение атрибута %s!", error))
    }
    errorList = getErrorAcceptable(departmentParam, departmentParamAdd)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для параметров текущего экземпляра декларации неверно указано значение атрибута %s на форме настроек подразделений", error))
    }

    // Справочник "Параметры представления деклараций по налогу на имущество"
    def regionId = getProvider(30).getRecordData(departmentId).REGION_ID?.value
    if (regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }
    def String filter = String.format("DECLARATION_REGION_ID = ${regionId} and LOWER(TAX_ORGAN_CODE) = LOWER('${declarationData.taxOrganCode}') and LOWER(KPP) = LOWER('${declarationData.kpp}')")
    records = refBookFactory.getDataProvider(200).getRecords(getEndDate() - 1, null, filter, null)
    if (records.size() == 0) {
        throw new Exception("В справочнике «Параметры представления деклараций по налогу на имущество» отсутствует запись по выбранным параметрам декларации (период, регион подразделения, налоговый орган, КПП)!")
    }
}

// Запуск генерации XML
void generateXML() {

    def knd = '1152026'
    def kbk = '18210602010021000110'
    def vidImush = 3

    def departmentId = declarationData.departmentId
    def reportPeriodId = declarationData.reportPeriodId

    def incomeParams = getProvider(99).getRecords(getEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)?.get(0)
    if (incomeParams == null) {
        throw new Exception('Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью')
    }

    def incomeParamsAdd = getProvider(206).getRecords(getEndDate() - 1, null, "LINK = ${incomeParams.record_id.value} AND LOWER(TAX_ORGAN_CODE) = LOWER('${declarationData.taxOrganCode}') AND LOWER(KPP) = LOWER('${declarationData.kpp}')", null)?.get(0)

    def formatVersion = incomeParams?.FORMAT_VERSION?.value

    def inn = incomeParams?.INN?.value
    def kpp = declarationData.kpp

    def taxOrganCode = declarationData.taxOrganCode
    def taxPlaceTypeCode = getRefBookValue(2, incomeParamsAdd?.TAX_PLACE_TYPE_CODE?.value)?.CODE?.value
    def okvedCode = getRefBookValue(34, incomeParamsAdd?.OKVED_CODE?.value)?.CODE?.value
    def reorgFormCode = getRefBookValue(5, incomeParamsAdd?.REORG_FORM_CODE?.value)?.CODE?.value
    def reorgInn = incomeParamsAdd?.REORG_INN?.value
    def reorgKpp = incomeParamsAdd?.REORG_KPP?.value
    def phone = incomeParamsAdd?.PHONE?.value
    def name = incomeParamsAdd?.NAME?.value
    def signatoryId = getRefBookValue(35, incomeParamsAdd?.SIGNATORY_ID?.value)?.CODE?.value
    def signatorySurname = incomeParamsAdd?.SIGNATORY_SURNAME?.value
    def signatoryFirstName = incomeParamsAdd?.SIGNATORY_FIRSTNAME?.value
    def signatoryLastName = incomeParamsAdd?.SIGNATORY_LASTNAME?.value
    def approveOrgName = incomeParamsAdd?.APPROVE_ORG_NAME?.value
    def approveDocName = incomeParamsAdd?.APPROVE_DOC_NAME?.value

    // Отчётный период.
    def reportPeriod = reportPeriodService.get(reportPeriodId)
    // Налоговый период.
    def taxPeriod = (reportPeriod != null ? taxPeriodService.get(reportPeriod.getTaxPeriod().getId()) : null)

    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData)

    // Форма 945.3 получаем только строки с Кодом НО и КПП
    def dataRowsAverage = getDataRows(taxOrganCode, kpp, 613, formDataCollection)
    // Форма 945.4 получаем только строки с Кодом НО и КПП
    def dataRowsCadastre = getDataRows(taxOrganCode, kpp, 612, formDataCollection)
    // Форма 945.5 получаем только строки с Кодом НО и КПП
    def dataRowsSummary = getDataRows(taxOrganCode, kpp, 615, formDataCollection)

    // разделим строки по субъектам и октмо
    def dataRowsSummaryMap = splitDataRows(dataRowsSummary)

    // Период.
    def period = reorgFormCode ? 50 : 34

    // ПрПодп.
    def prPodp = signatoryId

    if (xml == null) {
        return
    }

    // Формирование XML'ки.
    def builder = new MarkupBuilder(xml)
    builder.Файл(
            ИдФайл : declarationService.generateXmlFileId(3, declarationData.departmentReportPeriodId, declarationData.taxOrganCode, declarationData.kpp),
            ВерсПрог : applicationVersion,
            ВерсФорм : formatVersion) {

        // Титульный лист
        Документ(
                КНД : knd,
                ДатаДок : (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
                Период : period,
                ОтчетГод : (taxPeriod != null ? taxPeriod.year : empty),
                КодНО : taxOrganCode,
                НомКорр : reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId),
                ПоМесту : taxPlaceTypeCode) {

            СвНП(
                    ОКВЭД : okvedCode,
                    Тлф : phone) {

                НПЮЛ(
                        НаимОрг : name,
                        ИННЮЛ : inn,
                        КПП : kpp) {

                    if (reorgFormCode != null) {
                        СвРеоргЮЛ(
                                ФормРеорг : reorgFormCode,
                                ИННЮЛ : reorgInn,
                                КПП : reorgKpp)
                    }
                }
            }

            Подписант(ПрПодп : prPodp) {
                ФИО(
                        Фамилия : signatorySurname,
                        Имя : signatoryFirstName,
                        Отчество : signatoryLastName)
                if (prPodp != 1) {
                    СвПред(
                            (approveDocName ? [НаимДок : approveDocName] : [:]) +
                                    (approveOrgName != null ? [НаимОрг : approveOrgName] : [:] )
                    )
                }
            }
            // Титульный лист - конец
            // Лист 02 (Раздел 1)
            ИмущНД() {
                def oktmos = (dataRowsAverage || dataRowsCadastre) ? (dataRowsAverage + dataRowsCadastre).collect(){ it.oktmo }.unique(true) : []
                oktmos.each{ oktmo ->
                    def rowsAverageOKTMO = dataRowsAverage.findAll {it.oktmo == oktmo}
                    def rowsCadastreOKTMO = dataRowsCadastre.findAll {it.oktmo == oktmo}
                    def nalPU = (rowsAverageOKTMO.sum { it.taxSum - ((it.sumPayment?:0) + (it.sumDecrease?:0)) } ?:0) +
                            (rowsCadastreOKTMO.sum { it.sum - ((it.periodSum?:0) + (it.reductionPaymentSum?:0)) } ?:0)
                    СумНалПУ(
                            // ОКТМО (СумНалПУ) 010
                            ОКТМО : getRefBookValue(96, oktmo).CODE.value,
                            // КБК (СумНалПУ) 020
                            КБК : kbk,
                            // НалПУ (СумНалПУ) 030
                            НалПУ : ((BigDecimal)nalPU)?.setScale(0,RoundingMode.HALF_UP)
                    ) {
                        // Лист 03 (Раздел 2)
                        if (!rowsAverageOKTMO.empty) {
                            РасОбДеятРФ() {
                                def summaryIndex
                                // строка из 945.3
                                rowsAverageOKTMO.each{ row ->
                                    // СтИмущ 150 Значение атрибута «Средняя/среднегодовая стоимость имущества» налоговой формы источника (Форма 945.3)
                                    def stImush = row.priceAverage ?: 0
                                    // КодНалЛьг 160
                                    def kodNalLg = getCodeBasis(row.taxBenefitCode, row.benefitBasis, '2012000')
                                    // СтИмущНеобл 170 Значение атрибута «Средняя/Среднегодовая стоимость необлагаемого имущества» налоговой формы источника (Форма 945.3)
                                    def stImushNeobl = row.priceAverageTaxFree ?: 0
                                    // НалБаза 190 Значение атрибута «Налоговая база» налоговой формы источника (Форма 945.3)
                                    def nalBaza = row.taxBase ?: 0
                                    // КодЛгПНС 200
                                    def kodLgPNS = getCodeBasis(row.taxBenefitCodeReduction, row.benefitReductionBasis, '2012400')
                                    // НалСтав 210 Значение атрибута «Налоговая ставка» налоговой формы источника (Форма 945.3)
                                    def nalStav = row.taxRate ?: 0
                                    // СумНалИсчисл 220 Значение атрибута «Сумма налога (авансового платежа)» налоговой формы источника (Форма 945.3)
                                    def sumNalIschisl = row.taxSum ?: 0
                                    // СумАвИсчисл 230 Значение атрибута «Сумма авансовых платежей, исчисленная за отчетные периоды» налоговой формы источника (Форма 945.3)
                                    def sumAvIschisl = row.sumPayment ?: 0
                                    // КодЛгУмен 240
                                    def kodLgUmen = getCodeBasis(row.taxBenefitCodeDecrease, row.benefitDecreaseBasis, '2012500')
                                    // СумЛгУмен 250 Значение атрибута «Сумма уменьшения платежа» налоговой формы источника (Форма 945.3)
                                    def sumLgUmen = row.sumDecrease ?: 0
                                    // СтОстВс 260 Значение атрибута «Остаточная стоимость основных средств» налоговой формы источника (Форма 945.3)
                                    def stOstVs = row.residualValue ?: 0

                                    def key = row.subject + '#' + row.oktmo
                                    def rowsSummary = dataRowsSummaryMap[key]
                                    // строки из 945.5 по коду субъекта, коду НО, КПП и Коду ОКТМО соотвествующие строке из 945.3
                                    def rows = getSubjectOktmoRows(rowsSummary, row)
                                    def commonRow = null
                                    def benefitRow = null
                                    if (rowsAverageOKTMO.indexOf(row) == 0) {
                                        commonRow = rows[0]
                                        summaryIndex = 1
                                        if (getBenefitCode(row.taxBenefitCode) == '2012000') {
                                            benefitRow = rows[2]
                                            summaryIndex = 2
                                        }
                                    } else {
                                        commonRow = benefitRow = rows[++summaryIndex]
                                    }
                                    РасОб(  ВидИмущ : vidImush ) {
                                        ДанРас(
                                                [СтИмущ : ((BigDecimal)stImush)?.setScale(0,RoundingMode.HALF_UP)] +
                                                (kodNalLg ? [КодНалЛьг : kodNalLg] : [:]) +
                                                [СтИмущНеобл : ((BigDecimal)stImushNeobl)?.setScale(0,RoundingMode.HALF_UP)] +
                                                (kodLgPNS ? [КодЛгПНС : kodLgPNS] : [:]) +
                                                [НалСтав : nalStav]
                                        ) {
                                            СтоимМес() {
                                                for (int i = 1; i <= 12; i++) {
                                                    def elemName = "ОстСтом01" + String.valueOf(i).padLeft(2, '0')
                                                    def stOstOn = commonRow ? commonRow.getCell("cost$i").value : 0
                                                    def stLgImush = benefitRow ? benefitRow.getCell("cost$i").value : 0
                                                    "$elemName" (
                                                            СтОстОН : ((BigDecimal)stOstOn ?: 0)?.setScale(0,RoundingMode.HALF_UP),
                                                            СтЛьгИмущ: ((BigDecimal)stLgImush ?: 0)?.setScale(0,RoundingMode.HALF_UP)
                                                    )
                                                }
                                                def stOstOn = commonRow ? commonRow.getCell("cost13").value : 0
                                                def stLgImush = benefitRow ? benefitRow.getCell("cost13").value : 0
                                                ОстСтом3112(
                                                        СтОстОН: ((BigDecimal)stOstOn ?: 0)?.setScale(0,RoundingMode.HALF_UP),
                                                        СтЛьгИмущ: ((BigDecimal)stLgImush ?: 0)?.setScale(0,RoundingMode.HALF_UP)
                                                )
                                                def stOstOn31 = commonRow ? commonRow.getCell("cost31_12").value : 0
                                                def stLgImush31 = benefitRow ? benefitRow.getCell("cost31_12").value : 0
                                                ВтчНедИм(
                                                        СтОстОН: ((BigDecimal)stOstOn31 ?: 0)?.setScale(0,RoundingMode.HALF_UP),
                                                        СтЛьгИмущ: ((BigDecimal)stLgImush31 ?: 0)?.setScale(0,RoundingMode.HALF_UP)
                                                )
                                                СтОстВс(((BigDecimal)stOstVs)?.setScale(0,RoundingMode.HALF_UP))
                                            }
                                        }
                                        НалПер(
                                                [НалБаза : ((BigDecimal)nalBaza)?.setScale(0,RoundingMode.HALF_UP),
                                                СумНалИсчисл : ((BigDecimal)sumNalIschisl)?.setScale(0,RoundingMode.HALF_UP),
                                                СумАвИсчисл : ((BigDecimal)sumAvIschisl)?.setScale(0,RoundingMode.HALF_UP)] +
                                                (kodLgUmen ? [КодЛгУмен  : kodLgUmen] : [:]) +
                                                [СумНалПред : empty,
                                                СумЛгУмен   : ((BigDecimal)sumLgUmen)?.setScale(0,RoundingMode.HALF_UP)]
                                        )
                                    }
                                }
                            }
                        }
                        // Лист 04 (Раздел 3)
                        if (!rowsCadastreOKTMO.empty) {
                            РасОБНедИО() {
                                rowsCadastreOKTMO.each { row ->
                                    // НомКадЗдан 014 Заполняется значением атрибута «Кадастровый номер. Здание» налоговой формы источника (Форма 945.4)
                                    def nomKadZdan = row.cadastreNumBuilding
                                    // НомКадПом 015
                                    def nomKadPom = (row.sign == '2') ? row.cadastreNumRoom : empty
                                    // СтИмущК 020 Значение атрибута «Кадастровая стоимость. на 1 января» налоговой формы источника (Форма 945.4)
                                    def stImushK = row.cadastrePriceJanuary ?: 0
                                    // СтИмущНеоблК 025 Значение атрибута «Кадастровая стоимость. В т.ч. необлагаемая налогом» налоговой формы источника (Форма 945.4)
                                    def stImushNeoblK = row.cadastrePriceTaxFree ?: 0
                                    // КодНалЛьг 040
                                    def kodNalLg = getCodeBasis(row.taxBenefitCode, row.benefitBasis, '2012000')
                                    // НалБаза 060 Значение атрибута «Налоговая база» налоговой формы источника (Форма 945.4)
                                    def nalBaza2 = row.taxBase ?: 0
                                    // КодЛгПНС 070
                                    def kodLgPNS2 = getCodeBasis(row.taxBenefitCode, row.benefitBasis, '2012400')
                                    // НалСтав 080 Значение атрибута «Налоговая ставка» налоговой формы источника (Форма 945.4)
                                    def nalStav2 = row.taxRate ?: 0
                                    // СумНалИсчисл 100 Значение атрибута «Сумма налога (авансового платежа)» налоговой формы источника (Форма 945.4)
                                    def sumNalIschisl2 = row.sum ?: 0
                                    // СумАвИсчисл 110 Значение атрибута «Сумма авансовых платежей за отчетные периоды» налоговой формы источника (Форма 945.4)
                                    def sumAvIschisl2 = row.periodSum ?: 0
                                    // КодЛгУмен 120
                                    def kodLgUmen2 = getCodeBasis(row.taxBenefitCode, row.benefitBasis, '2012500')
                                    // СумЛгУмен 130 Значение атрибута «Сумма уменьшения платежа» налоговой формы источника (Форма 945.4)
                                    def sumLgUmen2 = row.reductionPaymentSum ?: 0
                                    РасОб() {
                                        ДанРас(
                                                [НомКадЗдан : nomKadZdan] +
                                                (nomKadPom ? [НомКадПом : nomKadPom] : [:]) +
                                                (kodNalLg ? [КодНалЛьг : kodNalLg] : [:]) +
                                                (kodLgPNS2 ? [КодЛгПНС : kodLgPNS2] : [:]) +
                                                [НалСтав : nalStav2]
                                        ) {
                                            СведСтКад (
                                                    СтИмущК : ((BigDecimal)stImushK)?.setScale(0,RoundingMode.HALF_UP),
                                                    СтИмущНеоблК : ((BigDecimal)stImushNeoblK)?.setScale(0,RoundingMode.HALF_UP)
                                            )
                                        }
                                        НалПер(
                                                [НалБаза : ((BigDecimal)nalBaza2)?.setScale(0,RoundingMode.HALF_UP),
                                                СумНалИсчисл : ((BigDecimal)sumNalIschisl2)?.setScale(0,RoundingMode.HALF_UP),
                                                СумАвИсчисл : ((BigDecimal)sumAvIschisl2)?.setScale(0,RoundingMode.HALF_UP)] +
                                                (kodLgUmen2 ? [КодЛгУмен  : kodLgUmen2] : [:]) +
                                                [СумЛгУмен : ((BigDecimal)sumLgUmen2)?.setScale(0,RoundingMode.HALF_UP)]
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
}

List<String> getErrorDepartment(def record, def recordAdd) {
    List<String> errorList = new ArrayList<String>()
    if (recordAdd.NAME == null || recordAdd.NAME.value == null || recordAdd.NAME.value.isEmpty()) {
        errorList.add("«Наименование подразделения»")
    }
    if (record.INN == null || record.INN.value == null || record.INN.value.isEmpty()) {
        errorList.add("«ИНН»")
    }
    if (recordAdd.OKVED_CODE == null || recordAdd.OKVED_CODE.value == null) {
        errorList.add("«Код вида экономической деятельности и по классификатору ОКВЭД»")
    }
    def reorgFormCode = getRefBookValue(5, recordAdd?.REORG_FORM_CODE?.value)?.CODE?.value
    if (reorgFormCode != null && reorgFormCode != '0') {
        if (recordAdd.REORG_INN == null || recordAdd.REORG_INN.value == null || recordAdd.REORG_INN.value.isEmpty()) {
            errorList.add("«ИНН реорганизованного обособленного подразделения»")
        }
        if (recordAdd.REORG_KPP == null || recordAdd.REORG_KPP.value == null || recordAdd.REORG_KPP.value.isEmpty()) {
            errorList.add("«КПП реорганизованного обособленного подразделения»")
        }
    }
    if (recordAdd.SIGNATORY_ID == null || recordAdd.SIGNATORY_ID.value == null) {
        errorList.add("«Признак лица подписавшего документ»")
    }
    if (recordAdd.SIGNATORY_SURNAME == null || recordAdd.SIGNATORY_SURNAME.value == null || recordAdd.SIGNATORY_SURNAME.value.isEmpty()) {
        errorList.add("«Фамилия подписанта»")
    }
    if (recordAdd.SIGNATORY_FIRSTNAME == null || recordAdd.SIGNATORY_FIRSTNAME.value == null || recordAdd.SIGNATORY_FIRSTNAME.value.isEmpty()) {
        errorList.add("«Имя подписанта»")
    }
    def signatoryId = getRefBookValue(35, recordAdd?.SIGNATORY_ID?.value)?.CODE?.value
    if ((signatoryId != null && signatoryId != 1) && (recordAdd.APPROVE_DOC_NAME == null || recordAdd.APPROVE_DOC_NAME.value == null || recordAdd.APPROVE_DOC_NAME.value.isEmpty())) {
        errorList.add("«Наименование документа, подтверждающего полномочия представителя»")
    }
    errorList
}

List<String> getErrorAcceptable(def record, def recordAdd) {
    List<String> errorList = new ArrayList<String>()
    if (recordAdd.TAX_PLACE_TYPE_CODE != null && !(getRefBookValue(2, recordAdd.TAX_PLACE_TYPE_CODE.value)?.CODE?.value in ['213', '214', '215', '216', '221', '245', '281'])) {
        errorList.add("«Код по месту нахождения (учета)»")
    }
    if (record.FORMAT_VERSION == null || record.FORMAT_VERSION.value == null || !record.FORMAT_VERSION.value.equals('5.03')) {
        errorList.add("«Версия формата декларации»")
    }
    errorList
}

// Получение провайдера с использованием кеширования.
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

// Получить строки формы.
def getDataRows(def taxOrganCode, def kpp, def formTypeId, def formDataCollection) {
    def formList = formDataCollection?.records?.findAll { it.formType.id == formTypeId }
    def dataRows = []
    for (def form : formList) {
        dataRows += (formDataService.getDataRowHelper(form)?.getAll()?.findAll() { row ->
            row.getAlias() == null && taxOrganCode.equals(getOwnerValue(row, 'taxAuthority')) && kpp.equals(getOwnerValue(row,'kpp'))
        } ?: [])
    }
    return dataRows.isEmpty() ? [] : dataRows
}

def splitDataRows(def dataRows) {
    def rowsMap = [:]
    dataRows.each { row ->
        def subject = getOwnerValue(row, 'subject')
        def oktmo = getOwnerValue(row, 'oktmo')
        def complexKey = subject + '#' + oktmo
        if (rowsMap[complexKey] == null) {
            rowsMap.put(complexKey, [])
        }
        rowsMap[complexKey].add(row)
    }
    return rowsMap
}

def getCodeBasis(def recordId, def basis, def trueCode) {
    if (recordId == null) return null
    def code = getBenefitCode(recordId)
    return ((code == trueCode) ? (code + '/' + basis) : empty)
}

def getBenefitCode(def parentRecordId) {
    if (parentRecordId == null) return null
    def recordId = getRefBookValue(203, parentRecordId).TAX_BENEFIT_ID.value
    return  getRefBookValue(202, recordId).CODE.value
}

def getSubjectOktmoRows(def rows, def exampleRow) {
    return rows.findAll { row ->
        getOwnerValue(row, 'subject') == exampleRow.subject && getOwnerValue(row, 'oktmo') == exampleRow.oktmo
    }
}