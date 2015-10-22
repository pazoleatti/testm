package form_template.transport.declaration.v2014

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
 * Формирование XML для декларации по транспортному налогу.
 *
 * declarationTemplateId=21127
 *
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE: // создать / обновить
        checkDepartmentParams(LogLevel.WARNING)
        break
    case FormDataEvent.CHECK: // проверить
        checkDepartmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED: // принять из создана
        checkDepartmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.PRE_CALCULATION_CHECK:
        checkDepartmentParams(LogLevel.WARNING)
        break
    case FormDataEvent.CALCULATE:
        checkDepartmentParams(LogLevel.WARNING)
        checkAndBuildXml()
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
@Field
def String dateFormat = 'dd.MM.yyyy'

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
    errorList = getErrorVersion(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Неверно указано значение атрибута %s на форме настроек подразделений!", error))
    }
    errorList = getErrorINN(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для данного подразделения на форме настроек подразделений отсутствует значение атрибута %s!", error))
    }

    // Справочник "Параметры представления деклараций по налогу на имущество"
    def regionId = getProvider(30).getRecordData(departmentId).REGION_ID?.value
    if (regionId == null) {
        throw new Exception("Атрибут «Регион» подразделения текущей налоговой формы не заполнен (справочник «Подразделения»)!")
    }
    def String filter = String.format("DECLARATION_REGION_ID = ${regionId} and LOWER(TAX_ORGAN_CODE) = LOWER('${declarationData.taxOrganCode}') and LOWER(KPP) = LOWER('${declarationData.kpp}')")
    def records = refBookFactory.getDataProvider(210).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
    if (records.size() == 0) {
        throw new Exception("В справочнике «Параметры представления деклараций по транспортному налогу» отсутствует запись по выбранным параметрам декларации (период, регион подразделения, налоговый орган, КПП)!")
    }
}

/** Осуществление проверк при создании + генерация xml. */
def checkAndBuildXml() {

    // проверка наличия источников в стутусе принят
    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData, userInfo, logger)
    // формируем xml

    // Получить параметры по транспортному налогу
    /** Предпослденяя дата отчетного периода на которую нужно получить настройки подразделения из справочника. */
    if (getReportPeriodEndDate() == null) {
        logger.error("Ошибка определения даты конца отчетного периода")
    }

    def departmentId = declarationData.departmentId

    def departmentParamTransport = getDepartmentParam()
    def departmentParamTransportRow = getDepartmentParamTable(departmentParamTransport.record_id.value)

    buildXml(departmentParamTransport, departmentParamTransportRow, formDataCollection, departmentId)
}

def buildXml(def departmentParamTransport, def departmentParamTransportRow, def formDataCollection, def departmentId) {

    def reorgFormCode = getRefBookValue(5, departmentParamTransportRow?.REORG_FORM_CODE?.referenceValue)?.CODE?.stringValue
    def okvedCode = getRefBookValue(34, departmentParamTransportRow?.OKVED_CODE?.referenceValue)?.CODE?.stringValue
    def signatoryId = getRefBookValue(35, departmentParamTransportRow?.SIGNATORY_ID?.referenceValue)?.CODE?.numberValue
    def taxPlaceTypeCode = getRefBookValue(2, departmentParamTransportRow?.TAX_PLACE_TYPE_CODE?.referenceValue)?.CODE?.stringValue

    def builder = new MarkupBuilder(xml)
    if (!declarationData.isAccepted()) {
        def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        Date yearStartDate = Date.parse(dateFormat, "01.01.${reportPeriod.taxPeriod.year}")
        def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.TRANSPORT, yearStartDate, getReportPeriodEndDate())
        builder.Файл(
                ИдФайл: generateXmlFileId(declarationData.taxOrganCode),
                ВерсПрог: applicationVersion,
                ВерсФорм: departmentParamTransport.FORMAT_VERSION) {
            Документ(
                    КНД: "1152004",
                    ДатаДок: (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
                    Период: reorgFormCode ? 50 : 34,
                    ОтчетГод: reportPeriod.taxPeriod.year,
                    КодНО: declarationData.taxOrganCode,
                    НомКорр: reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId),
                    ПоМесту: taxPlaceTypeCode
            ) {
                def svnp = [ОКВЭД: okvedCode]
                if (departmentParamTransportRow.OKVED_CODE) {
                    svnp.Тлф = departmentParamTransportRow.PHONE
                }
                СвНП(svnp) {
                    НПЮЛ(
                            НаимОрг: departmentParamTransportRow.NAME,
                            ИННЮЛ: (departmentParamTransport.INN),
                            КПП: (declarationData.kpp)) {

                        if (reorgFormCode != null && !reorgFormCode.equals("")) {
                            СвРеоргЮЛ([ФормРеорг: reorgFormCode] +
                                    (Integer.parseInt(reorgFormCode) in [1, 2, 3, 5, 6] ?
                                            [ИННЮЛ: departmentParamTransportRow.REORG_INN, КПП: departmentParamTransportRow.REORG_KPP] : [])
                            )
                        }
                    }
                }

                Подписант(ПрПодп: signatoryId) {
                    ФИО(
                            "Фамилия": departmentParamTransportRow.SIGNATORY_SURNAME,
                            "Имя": departmentParamTransportRow.SIGNATORY_FIRSTNAME,
                            "Отчество": departmentParamTransportRow.SIGNATORY_LASTNAME
                    )
                    // СвПред - Сведения о представителе налогоплательщика
                    if (signatoryId == 2) {
                        def svPred = ["НаимДок": departmentParamTransportRow.APPROVE_DOC_NAME]
                        if (departmentParamTransportRow.APPROVE_ORG_NAME)
                            svPred.НаимОрг = departmentParamTransportRow.APPROVE_ORG_NAME
                        СвПред(svPred)
                    }
                }

                ТрНалНД() {
                    СумНалПУ("КБК": "18210604011021000110") {
                        /*
                        * Получить сводную НФ по трансп. со статусом принята
                        * Сгруппировать строки сводной налоговой формы по атрибуту «Код по ОКТМО». (okato)
                        */

                        // Сводны формы по кварталам
                        def formDataMap = [:]
                        // Отчетные периоды по кварталам
                        def reportPeriodMap = [:]
                        // Строки сводных по кварталам
                        def rowsDataMap = [:]

                        // Данные текущего квартала
                        reportPeriodMap[reportPeriod.order] = reportPeriod
                        formDataMap[reportPeriod.order] = formDataCollection?.records?.find { it.formType.id == 200 }
                        if (formDataMap[reportPeriod.order] != null) {
                            // «Своя» сводная есть и «Принята»
                            rowsDataMap[reportPeriod.order] = formDataService.getDataRowHelper(formDataMap[reportPeriod.order]).allSaved

                            // Заполнение данных предыдущих кварталов
                            if (reportPeriod.order > 1) {
                                reportPeriods.each { period ->
                                    def order = period.order
                                    reportPeriodMap[order] = period
                                    formDataMap[order] = formDataService.getLast(formDataMap[reportPeriod.order].formType.id, formDataMap[reportPeriod.order].kind,
                                            formDataMap[reportPeriod.order].departmentId, reportPeriodMap[order].id, formDataMap[reportPeriod.order].periodOrder,
                                            formDataMap[reportPeriod.order].comparativePeriodId, formDataMap[reportPeriod.order].accruing)
                                    if (formDataMap[order] != null && formDataMap[order].state == WorkflowState.ACCEPTED) {
                                        rowsDataMap[order] = formDataService.getDataRowHelper(formDataMap[order]).allSaved
                                    }
                                }
                            }
                        }

                        // Формирование данных для СумПУ
                        def resultMap = [:]

                        // По кварталам с начала года до текущего
                        for (period in reportPeriods) {
                            def order = period.order
                            def rowsData = rowsDataMap[order]
                            if (rowsData == null) {
                                continue
                            }
                            rowsData.each { row ->
                                if (row.getAlias() == null && row.taxAuthority == declarationData.taxOrganCode && row.kpp == declarationData.kpp) {
                                    // Новый код ОКТМО — инициализация результирующего набора
                                    if (!resultMap[row.okato]) {
                                        resultMap[row.okato] = [:]
                                        resultMap[row.okato].rowData = [];

                                        resultMap[row.okato].calculationOfTaxes = 0 as BigDecimal
                                        resultMap[row.okato].taxBase = 0 as BigDecimal
                                        resultMap[row.okato].taxRate = 0 as BigDecimal
                                        resultMap[row.okato].amountOfTheAdvancePayment1 = 0 as BigDecimal
                                        resultMap[row.okato].amountOfTheAdvancePayment2 = 0 as BigDecimal
                                        resultMap[row.okato].amountOfTheAdvancePayment3 = 0 as BigDecimal
                                        resultMap[row.okato].amountOfTaxPayable = 0 as BigDecimal
                                        resultMap[row.okato].taxSumToPay = 0 as BigDecimal
                                    }

                                    def boolean obligation = (departmentParamTransportRow?.PREPAYMENT?.numberValue == 1)

                                    // вспомогательный taxBase
                                    resultMap[row.okato].taxBase += row.taxBase ?: 0 as BigDecimal
                                    def taxRate = getRefBookValue(41, row.taxRate)?.VALUE?.numberValue
                                    // вспомогательный taxRate
                                    resultMap[row.okato].taxRate += taxRate ?: 0 as BigDecimal

                                    if (order == reportPeriod.order) {
                                        // Строка текущего квартала
                                        // НалИсчисл = сумма Исчисленная сумма налога, подлежащая уплате в бюджет
                                        resultMap[row.okato].calculationOfTaxes += row.taxSumToPay ?: 0 as BigDecimal;
                                        // суммма
                                        resultMap[row.okato].taxSumToPay += row.taxSumToPay ?: 0 as BigDecimal;

                                        // Формирование данных для РасчНалТС, собираем строки с текущим значением ОКТМО
                                        resultMap[row.okato].rowData.add(row);
                                    }

                                    switch (order) {
                                        case 1:
                                            // АвПУКв1 = В т.ч. сумма авансовых платежей, исчисленная к уплате в бюджет за первый квартал //// Заполняется в 1, 2, 3, 4 отчетном периоде.
                                            resultMap[row.okato].amountOfTheAdvancePayment1 += (obligation ? 0.25 * row.taxBase * taxRate * row.coef362 : 0.0)
                                            break;
                                        case 2:
                                            // АвПУКв2 = В т.ч. сумма авансовых платежей, исчисленная к уплате в бюджет за второй квартал //// Заполняется во 2, 3, 4 отчетном периоде.
                                            resultMap[row.okato].amountOfTheAdvancePayment2 += (obligation ? 0.25 * row.taxBase * taxRate * row.coef362 : 0.0)
                                            break;
                                        case 3:
                                            // АвПУКв3 = В т.ч. сумма авансовых платежей, исчисленная к уплате в бюджет за третий квартал //// Заполняется во 3, 4 отчетном периоде.
                                            resultMap[row.okato].amountOfTheAdvancePayment3 += (obligation ? 0.25 * row.taxBase * taxRate * row.coef362 : 0.0)
                                            break;
                                    }

                                    // НалПУ = НалИсчисл – (АвПУКв1+ АвПУКв2+ АвПУКв3)
                                    resultMap[row.okato].amountOfTaxPayable = roundInt(resultMap[row.okato].calculationOfTaxes) -
                                            (roundInt(resultMap[row.okato].amountOfTheAdvancePayment1) +
                                                    roundInt(resultMap[row.okato].amountOfTheAdvancePayment2) +
                                                    roundInt(resultMap[row.okato].amountOfTheAdvancePayment3))
                                    // В случае  если полученное значение отрицательно, - не заполняется
                                    // resultMap[row.okato].amountOfTaxPayable = resultMap[row.okato].amountOfTaxPayable < 0 ? 0:resultMap[row.okato].amountOfTaxPayable;
                                }
                            }
                        }

                        resultMap.each { okato, row ->
                            СумПУ(
                                    ОКТМО: getOkato(okato),
                                    НалИсчисл: roundInt(row.taxSumToPay),
                                    АвПУКв1: roundInt(row.amountOfTheAdvancePayment1),
                                    АвПУКв2: roundInt(row.amountOfTheAdvancePayment2),
                                    АвПУКв3: roundInt(row.amountOfTheAdvancePayment3),
                                    НалПУ: roundInt(row.amountOfTaxPayable),
                            ) {
                                row.rowData.each { tRow ->
                                    def taxBenefitCode = getBenefitCode(tRow.taxBenefitCode)
                                    // TODO есть поля которые могут не заполняться, в нашем случае опираться какой логики?
                                    РасчНалТС(
                                            [
                                                    КодВидТС   : getRefBookValue(42, tRow.tsTypeCode)?.CODE?.stringValue,
                                                    ИдНомТС    : tRow.vi, //
                                                    МаркаТС    : tRow.model, //
                                                    РегЗнакТС  : tRow.regNumber,
                                                    НалБаза    : tRow.taxBase,
                                                    ОКЕИНалБаза: getRefBookValue(12, tRow.taxBaseOkeiUnit)?.CODE?.stringValue,
                                            ]
                                                    + (tRow.ecoClass ? [ЭкологКл: getRefBookValue(40, tRow.ecoClass)?.CODE?.numberValue] : []) + //
                                                    [
                                                            ВыпускТС: tRow.years, //
                                                            ВладенТС: tRow.ownMonths,
                                                            ДоляТС  : tRow.partRight
                                                    ] +
                                                    (tRow.coef362 ? [КоэфКв: tRow.coef362] : []) +
                                                    [НалСтавка: getRefBookValue(41, tRow.taxRate)?.VALUE?.numberValue] +
                                                    (tRow.koefKp ? [КоэфКп: tRow.koefKp] : []) +
                                                    [СумИсчисл: roundInt(tRow.calculatedTaxSum)] +
                                                    (tRow.benefitMonths ? [ЛьготМесТС: tRow.benefitMonths] : []) +
                                                    [СумИсчислУпл: roundInt(tRow.taxSumToPay)] +
                                                    (tRow.coefKl ? [КоэфКл: tRow.coefKl] : []),
                                    ) {
                                        // генерация КодОсвНал
                                        if (taxBenefitCode != null && (taxBenefitCode.equals('30200') || taxBenefitCode.equals('20200') || taxBenefitCode.equals('20210'))) {
                                            def valL = taxBenefitCode;
                                            def valX = "";
                                            if (!valL.equals("30200")) {
                                                valX = tRow.benefitBase
                                            }
                                            def kodOsnNal = (valL != "" ? valL.toString() : "0000") + (valX != '' ? "/" + valX : '')
                                            ЛьготОсвНал(
                                                    КодОсвНал: kodOsnNal,
                                                    СумОсвНал: roundInt(tRow.benefitSum)
                                            )
                                        }

                                        // вычисление ЛьготУменСум
                                        // заполняется если Код налоговой льготы = 20220
                                        def taxBenefitCodeDecrease = getBenefitCode(tRow.taxBenefitCodeDecrease)
                                        if (taxBenefitCodeDecrease != null && taxBenefitCodeDecrease.equals("20220")) {

                                            // вычисление КодУменСум
                                            def valL = taxBenefitCodeDecrease;
                                            def valX = tRow.benefitBase
                                            def kodUmenSum = (valL != "" ? valL.toString() : "0000") + "/" + valX
                                            ЛьготУменСум(КодУменСум: kodUmenSum, СумУменСум: roundInt(tRow.benefitSumDecrease))
                                        }

                                        // ЛьготСнижСтав
                                        // заполняется если Код налоговой льготы = 20230
                                        def benefitCodeReduction = getBenefitCode(tRow.benefitCodeReduction)
                                        if (benefitCodeReduction != null && benefitCodeReduction.equals("20230")) {

                                            // вычисление КодСнижСтав
                                            def valL = benefitCodeReduction;
                                            def valX = tRow.benefitBase
                                            def kodNizhStav = (valL != "" ? valL.toString() : "0000") + "/" + valX
                                            ЛьготСнижСтав(КодСнижСтав: kodNizhStav, СумСнижСтав: roundInt(tRow.benefitSumReduction))
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
}

/**
 * Получение региона по коду ОКТМО
 */
def getRegionByOKTMO(def oktmo) {
    def oktmo3 = getRefBookValue(96, oktmo)?.CODE?.stringValue.substring(0, 2)
    if (oktmo3.equals("719")) {
        return getRecord(4, 'CODE', '89', null, null, getReportPeriodEndDate());
    } else if (oktmo3.equals("718")) {
        return getRecord(4, 'CODE', '86', null, null, getReportPeriodEndDate());
    } else if (oktmo3.equals("118")) {
        return getRecord(4, 'CODE', '83', null, null, getReportPeriodEndDate());
    } else {
        def filter = "OKTMO_DEFINITION like '" + oktmo3.substring(0, 2) + "%'"
        def record = getRecord(4, filter, getReportPeriodEndDate())
        if (record != null) {
            return record
        } else {
            logger.error("Не удалось определить регион по коду ОКТМО")
            return null;
        }
    }
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecord(def Long refBookId, def String alias, def String value, def int rowIndex, def String columnName,
              def Date date, boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value, date,
            rowIndex, columnName, logger, required)
}

/**
 * Аналог FormDataServiceImpl.getRefBookRecord(...) но ожидающий получения из справочника больше одной записи.
 * @return первая из найденных записей
 */
def getRecord(def refBookId, def filter, Date date) {
    if (refBookId == null) {
        return null
    }
    String dateStr = date?.format(dateFormat)
    if (recordCache.containsKey(refBookId)) {
        Long recordId = recordCache.get(refBookId).get(dateStr + filter)
        if (recordId != null) {
            if (refBookCache != null) {
                def key = getRefBookCacheKey(refBookId, recordId)
                return refBookCache.get(key)
            } else {
                def retVal = new HashMap<String, RefBookValue>()
                retVal.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordId))
                return retVal
            }
        }
    } else {
        recordCache.put(refBookId, [:])
    }

    def provider
    if (!providerCache.containsKey(refBookId)) {
        providerCache.put(refBookId, refBookFactory.getDataProvider(refBookId))
    }
    provider = providerCache.get(refBookId)

    def records = provider.getRecords(date, null, filter, null)
    // отличие от FormDataServiceImpl.getRefBookRecord(...)
    if (records.size() > 0) {
        def retVal = records.get(0)
        Long recordId = retVal.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue()
        recordCache.get(refBookId).put(dateStr + filter, recordId)
        if (refBookCache != null) {
            def key = getRefBookCacheKey(refBookId, recordId)
            refBookCache.put(key, retVal)
        }
        return retVal
    }
    return null
}

/*
* 2.2. Получить в справочнике «Параметры налоговых льгот» запись,
* соответствующую значениям атрибутов «Код субъекта» и «Код налоговой льготы»;
*/

def getParam(taxBenefitCode, oktmo) {
    if (taxBenefitCode != null) {
        // получения региона по коду ОКТМО по справочнику Регионов
        def region = getRegionByOKTMO(oktmo);

        def refBookProvider = refBookFactory.getDataProvider(7)

        def query = "DICT_REGION_ID = ${region?.record_id?.value} AND TAX_BENEFIT_ID = $taxBenefitCode"
        def params = refBookProvider.getRecords(getReportPeriodEndDate(), null, query, null).getRecords()

        if (params.size() == 1)
            return params.get(0)
        else {
            logger.error("Ошибка при получении данных из справочника «Параметры налоговых льгот» $taxBenefitCode")
            return null
        }
    }
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
    def reorgFormCode = getRefBookValue(5, record?.REORG_FORM_CODE?.referenceValue)?.CODE?.stringValue
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
    def signatoryId = getRefBookValue(35, record?.SIGNATORY_ID?.referenceValue)?.CODE?.numberValue
    if (signatoryId != null && signatoryId != 1) {
        if (record.APPROVE_DOC_NAME?.stringValue == null || record.APPROVE_DOC_NAME.stringValue.isEmpty()) {
            errorList.add("«Наименование документа, подтверждающего полномочия представителя»")
        }
    }
    errorList
}

List<String> getErrorTaxPlaceTypeCode(def record) {
    List<String> errorList = new ArrayList<String>()
    def code = record.TAX_PLACE_TYPE_CODE?.referenceValue
    if (code == null || !(getRefBookValue(2, code)?.CODE?.stringValue in ['213', '216', '260'])) {
        errorList.add("«Код места, по которому представляется документ»")
    }
    errorList
}

List<String> getErrorVersion(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.FORMAT_VERSION == null || record.FORMAT_VERSION.stringValue == null || !record.FORMAT_VERSION.stringValue.equals('5.03')) {
        errorList.add("«Версия формата»")
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

def getOkato(def id) {
    def String okato = null
    if (id != null) {
        okato = getRefBookValue(96, id)?.CODE?.stringValue
    }
    return okato
}

// Получить параметры подразделения (из справочника 31)
def getDepartmentParam() {
    if (departmentParam == null) {
        def departmentId = declarationData.departmentId
        def departmentParamList = getProvider(31).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)
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
        def departmentParamTableList = getProvider(310).getRecords(getReportPeriodEndDate() - 1, null, filter, null)
        if (departmentParamTableList == null || departmentParamTableList.size() == 0 || departmentParamTableList.get(0) == null) {
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParamTable = departmentParamTableList.get(0)
    }
    return departmentParamTable
}

def roundInt(def value) {
    ((BigDecimal) value)?.setScale(0, RoundingMode.HALF_UP)
}

def getBenefitCode(def parentRecordId) {
    def recordId = getRefBookValue(7, parentRecordId)?.TAX_BENEFIT_ID?.value
    return getRefBookValue(6, recordId)?.CODE?.value
}

def generateXmlFileId(String taxOrganCode) {
    def departmentParam = getDepartmentParam()
    if (departmentParam) {
        def date = Calendar.getInstance().getTime()?.format("yyyyMMdd")
        def fileId = TaxType.TRANSPORT.declarationPrefix + '_' +
                taxOrganCode + '_' +
                taxOrganCode + '_' +
                departmentParam.INN?.value +
                declarationData.kpp + "_" +
                date + "_" +
                UUID.randomUUID().toString().toUpperCase()
        return fileId
    }
    return null
}
