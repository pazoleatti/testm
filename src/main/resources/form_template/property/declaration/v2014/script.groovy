package form_template.property.declaration.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field
import groovy.xml.MarkupBuilder

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
        throw new Exception("Ошибка при получении настроек обособленного подразделения!")
    }

    departmentParam = departmentParam.get(0)

    // Проверки подразделения
    def List<String> errorList = getErrorDepartment(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для параметров текущего экземпляра декларации на форме настроек подразделений отсутствует значение атрибута %s!", error))
    }
    errorList = getErrorAcceptable(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для параметров текущего экземпляра декларации неверно указано значение атрибута %s на форме настроек подразделений", error))
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
        throw new Exception('Ошибка при получении настроек обособленного подразделения!')
    }
    def formatVersion = incomeParams?.FORMAT_VERSION?.value

    def inn = incomeParams?.INN?.value
    def kpp = declarationData.kpp

    def taxOrganCode = declarationData.taxOrganCode
    def taxPlaceTypeCode = getRefBookValue(2, incomeParams?.TAX_PLACE_TYPE_CODE?.value)?.CODE?.value
    def okvedCode = getRefBookValue(34, incomeParams?.OKVED_CODE?.value)?.CODE?.value
    def reorgFormCode = getRefBookValue(5, incomeParams?.REORG_FORM_CODE?.value)?.CODE?.value
    def reorgInn = incomeParams?.REORG_INN?.value
    def reorgKpp = incomeParams?.REORG_KPP?.value
    def phone = incomeParams?.PHONE?.value
    def name = incomeParams?.NAME?.value
    def signatoryId = getRefBookValue(35, incomeParams?.SIGNATORY_ID?.value)?.CODE?.value
    def signatorySurname = incomeParams?.SIGNATORY_SURNAME?.value
    def signatoryFirstName = incomeParams?.SIGNATORY_FIRSTNAME?.value
    def signatoryLastName = incomeParams?.SIGNATORY_LASTNAME?.value
    def approveOrgName = incomeParams?.APPROVE_ORG_NAME?.value
    def approveDocName = incomeParams?.APPROVE_DOC_NAME?.value

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
    def dataRowsSummary = getDataRows(taxOrganCode, kpp, 10640, formDataCollection)

    // разделим строки по субъектам и октмо
    def dataRowsAverageMap = splitDataRows(dataRowsAverage)
    def dataRowsSummaryMap = splitDataRows(dataRowsSummary)

    // Период.
    def period = reorgFormCode ? 50 : 34 // TODO

    // ПрПодп.
    def prPodp = signatoryId

    if (xml == null) {
        return
    }

    // Формирование XML'ки.
    def builder = new MarkupBuilder(xml)
    builder.Файл(
            ИдФайл : declarationService.generateXmlFileId(3, departmentId, reportPeriodId),
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
                            ОКТМО : oktmo,
                            // КБК (СумНалПУ) 020
                            КБК : kbk,
                            // НалПУ (СумНалПУ) 030
                            НалПУ : nalPU
                    ) {
                        // Лист 03 (Раздел 2)
                        РасОбДеятРФ() {
                            // строка из 945.3
                            rowsAverageOKTMO.each{ row ->
                                // СтИмущ 150 Значение атрибута «Средняя/среднегодовая стоимость имущества» налоговой формы источника (Форма 945.3)
                                def stImush = row.priceAverage
                                // КодНалЛьг 160
                                def kodNalLg = getCodeBasis(row.taxBenefitCode, row.benefitBasis, '2012000')
                                // СтИмущНеобл 170 Значение атрибута «Средняя/Среднегодовая стоимость необлагаемого имущества» налоговой формы источника (Форма 945.3)
                                def stImushNeobl = row.priceAverageTaxFree
                                // НалБаза 190 Значение атрибута «Налоговая база» налоговой формы источника (Форма 945.3)
                                def nalBaza = row.taxBase
                                // КодЛгПНС 200
                                def kodLgPNS = getCodeBasis(row.taxBenefitCodeReduction, row.benefitReductionBasis, '2012400')
                                // НалСтав 210 Значение атрибута «Налоговая ставка» налоговой формы источника (Форма 945.3)
                                def nalStav = row.taxRate
                                // СумНалИсчисл 220 Значение атрибута «Сумма налога (авансового платежа)» налоговой формы источника (Форма 945.3)
                                def sumNalIschisl = row.taxSum
                                // СумАвИсчисл 230 Значение атрибута «Сумма авансовых платежей, исчисленная за отчетные периоды» налоговой формы источника (Форма 945.3)
                                def sumAvIschisl = row.sumPayment
                                // КодЛгУмен 240
                                def kodLgUmen = getCodeBasis(row.taxBenefitCodeDecrease, row.benefitDecreaseBasis, '2012500')
                                // СумЛгУмен 250 Значение атрибута «Сумма уменьшения платежа» налоговой формы источника (Форма 945.3)
                                def sumLgUmen = row.sumDecrease
                                // СтОстВс 260 Значение атрибута «Остаточная стоимость основных средств» налоговой формы источника (Форма 945.3)
                                def stOstVs = row.residualValue

                                def key = row.subject + '#' + row.oktmo
                                def rowsAverage = dataRowsAverageMap[key]
                                def rowsSummary = dataRowsSummaryMap[key]
                                // строки из 945.5 по коду субъекта, коду НО, КПП и Коду ОКТМО соотвествующие строке из 945.3
                                def rows = getSubjectOktmoRows(rowsSummary, row)
                                def taRow = null
                                def specialRow = null
                                if (rowsAverage.indexOf(row) == 0) {
                                    taRow = rows[0]
                                    if (getBenefitCode(row.taxBenefitCode) == '2012000') {
                                        specialRow = rows[2]
                                    }
                                } else {
                                    taRow = specialRow = rows[2]
                                }

                                РасОб(  ВидИмущ : vidImush ) {
                                    ДанРас(
                                            [СтИмущ : stImush] +
                                            (kodNalLg ? [КодНалЛьг : kodNalLg] : [:]) +
                                            [СтИмущНеобл : stImushNeobl] +
                                            (kodLgPNS ? [КодЛгПНС : kodLgPNS] : [:]) +
                                            [НалСтав : nalStav]
                                    ) {
                                        СтоимМес( СтОстВс : stOstVs) {
                                            for (int i = 1; i <= 12; i++) {
                                                def elemName = "ОстСтом01" + String.valueOf(i).padLeft(2, '0')
                                                "$elemName" (
                                                        СтОстОН : taRow.getCell("cost$i").value,
                                                        СтЛьгИмущ: specialRow ? specialRow.getCell("cost$i").value : 0
                                                )
                                            }
                                            ОстСтом3112(
                                                    СтОстОН: taRow.getCell("cost13").value,
                                                    СтЛьгИмущ: specialRow ? specialRow.getCell("cost13").value : 0
                                            )
                                            ВтчНедИм(
                                                    СтОстОН: taRow.getCell("cost31_12").value,
                                                    СтЛьгИмущ: specialRow ? specialRow.getCell("cost31_12").value : 0
                                            )
                                        }
                                    }
                                    НалПер(
                                            [НалБаза : nalBaza,
                                            СумНалИсчисл : sumNalIschisl,
                                            СумАвИсчисл : sumAvIschisl] +
                                            (kodLgUmen ? [КодЛгУмен  : kodLgUmen] : [:]) +
                                            [СумНалПред : empty,
                                            СумЛгУмен   : sumLgUmen]
                                    )
                                }
                            }
                        }
                        // Лист 04 (Раздел 3)
                        РасОбНедИО() {
                            rowsCadastreOKTMO.each { row ->
                                // НомКадЗдан 014 Заполняется значением атрибута «Кадастровый номер. Здание» налоговой формы источника (Форма 945.4)
                                def nomKadZdan = row.cadastreNumBuilding
                                // НомКадПом 015
                                def nomKadPom = (row.sign == '2') ? row.cadastreNumRoom : empty
                                // СтИмущК 020 Значение атрибута «Кадастровая стоимость. на 1 января» налоговой формы источника (Форма 945.4)
                                def stImushK = row.cadastrePriceJanuary
                                // СтИмущНеоблК 025 Значение атрибута «Кадастровая стоимость. В т.ч. необлагаемая налогом» налоговой формы источника (Форма 945.4)
                                def stImushNeoblK = row.cadastrePriceTaxFree
                                // КодНалЛьг 040
                                def kodNalLg = getCodeBasis(row.taxBenefitCode, row.benefitBasis, '2012000')
                                // НалБаза 060 Значение атрибута «Налоговая база» налоговой формы источника (Форма 945.4)
                                def nalBaza2 = row.taxBase
                                // КодЛгПНС 070
                                def kodLgPNS2 = getCodeBasis(row.taxBenefitCode, row.benefitBasis, '2012400')
                                // НалСтав 080 Значение атрибута «Налоговая ставка» налоговой формы источника (Форма 945.4)
                                def nalStav2 = row.taxRate
                                // СумНалИсчисл 100 Значение атрибута «Сумма налога (авансового платежа)» налоговой формы источника (Форма 945.4)
                                def sumNalIschisl2 = row.sum
                                // СумАвИсчисл 110 Значение атрибута «Сумма авансовых платежей за отчетные периоды» налоговой формы источника (Форма 945.4)
                                def sumAvIschisl2 = row.periodSum
                                // КодЛгУмен 120
                                def kodLgUmen2 = getCodeBasis(row.taxBenefitCode, row.benefitBasis, '2012500')
                                // СумЛгУмен 130 Значение атрибута «Сумма уменьшения платежа» налоговой формы источника (Форма 945.4)
                                def sumLgUmen2 = row.reductionPaymentSum
                                РасОб() {
                                    ДанРас(
                                            [НомКадЗдан : nomKadZdan] +
                                            (nomKadPom ? [НомКадПом : nomKadPom] : [:]) +
                                            (kodNalLg ? [КодНалЛьг : kodNalLg] : [:]) +
                                            (kodLgPNS2 ? [КодЛгПНС : kodLgPNS2] : [:]) +
                                            [НалСтав : nalStav2]
                                    ) {
                                        СведСтКад (
                                                СтИмущК : stImushK,
                                                СтИмущНеоблК : stImushNeoblK
                                        )
                                        СведСтИнв (
                                                СтИмущИ : empty,
                                                СтИмущНеоблИ : empty,
                                        )
                                    }
                                    НалПер(
                                            [НалБаза : nalBaza2,
                                            СумНалИсчисл : sumNalIschisl2,
                                            СумАвИсчисл : sumAvIschisl2] +
                                            (kodLgUmen2 ? [КодЛгУмен  : kodLgUmen2] : [:]) +
                                            [СумЛгУмен : sumLgUmen2]
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

List<String> getErrorDepartment(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.NAME == null || record.NAME.value == null || record.NAME.value.isEmpty()) {
        errorList.add("«Наименование подразделения»")
    }
    if (record.INN == null || record.INN.value == null || record.INN.value.isEmpty()) {
        errorList.add("«ИНН»")
    }
    if (record.OKVED_CODE == null || record.OKVED_CODE.value == null) {
        errorList.add("«Код вида экономической деятельности и по классификатору ОКВЭД»")
    }
    def reorgFormCode = getRefBookValue(5, record?.REORG_FORM_CODE?.value)?.CODE?.value
    if (reorgFormCode != null && reorgFormCode != '0') {
        if (record.REORG_INN == null || record.REORG_INN.value == null || record.REORG_INN.value.isEmpty()) {
            errorList.add("«ИНН реорганизованного обособленного подразделения»")
        }
        if (record.REORG_KPP == null || record.REORG_KPP.value == null || record.REORG_KPP.value.isEmpty()) {
            errorList.add("«КПП реорганизованного обособленного подразделения»")
        }
    }
    if (record.SIGNATORY_ID == null || record.SIGNATORY_ID.value == null) {
        errorList.add("«Признак лица подписавшего документ»")
    }
    if (record.SIGNATORY_SURNAME == null || record.SIGNATORY_SURNAME.value == null || record.SIGNATORY_SURNAME.value.isEmpty()) {
        errorList.add("«Фамилия подписанта»")
    }
    if (record.SIGNATORY_FIRSTNAME == null || record.SIGNATORY_FIRSTNAME.value == null || record.SIGNATORY_FIRSTNAME.value.isEmpty()) {
        errorList.add("«Имя подписанта»")
    }
    def signatoryId = getRefBookValue(35, record?.SIGNATORY_ID?.value)?.CODE?.value
    if ((signatoryId != null && signatoryId != 1) && (record.APPROVE_DOC_NAME == null || record.APPROVE_DOC_NAME.value == null || record.APPROVE_DOC_NAME.value.isEmpty())) {
        errorList.add("«Наименование документа, подтверждающего полномочия представителя»")
    }
    errorList
}

List<String> getErrorAcceptable(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.TAX_PLACE_TYPE_CODE != null && !getRefBookValue(2, record.TAX_PLACE_TYPE_CODE.value)?.CODE?.value in ['213', '214', '215', '216', '221', '245', '281']) {
        errorList.add("«Код по месту нахождения (учета)»")
    }
    if (record.FORMAT_VERSION == null || record.FORMAT_VERSION.value == null || !record.FORMAT_VERSION.value.equals('5.03')) {
        errorList.add("«Версия формата»")
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
def getDataRows(def taxOrganCode, def kpp, def formTemplateId, def formDataCollection) {
    def formList = formDataCollection?.findAllByFormTypeAndKind(formTemplateId, FormDataKind.SUMMARY)
    def dataRows = []
    for (def form : formList) {
        dataRows += (formDataService.getDataRowHelper(form)?.getAll()?.findAll() { row ->
            !row.getAlias()?.contains('total') && row.taxAuthority == taxOrganCode && row.kpp == kpp
        } ?: [])
    }
    return dataRows.isEmpty() ? null : dataRows
}

def splitDataRows(def dataRows) {
    def rowsMap = [:]
    dataRows.each { row ->
        def complexKey = row.subject + '#' + row.oktmo
        if (rowsMap[complexKey] == null) {
            rowsMap.put(complexKey, [])
        } else {
            rowsMap[complexKey].add(row)
        }
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
        row.subject == exampleRow.subject && row.oktmo == exampleRow.oktmo
    }
}