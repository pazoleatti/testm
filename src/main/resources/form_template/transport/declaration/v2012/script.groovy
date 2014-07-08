package form_template.transport.declaration

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field
import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat

/**
 * Формирование XML для декларации по транспортному налогу.
 * @author auldanov
 * @since 19.03.2013 16:30
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE: // создать / обновить
        checkDeparmentParams(LogLevel.WARNING)
        break
    case FormDataEvent.CHECK: // проверить
        checkDeparmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED: // принять из создана
        checkDeparmentParams(LogLevel.ERROR)
        break
    case FormDataEvent.CALCULATE:
        checkDeparmentParams(LogLevel.WARNING)
        checkAndbildXml()
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
def sdf = new SimpleDateFormat('dd.MM.yyyy')

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

void checkDeparmentParams(LogLevel logLevel) {
    def departmentId = declarationData.departmentId

    // Параметры подразделения
    def departmentParamList = getProvider(31).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null)

    if (departmentParamList == null || departmentParamList.size() == 0 || departmentParamList.get(0) == null) {
        throw new Exception("Ошибка при получении настроек обособленного подразделения")
    }

    def departmentParam = departmentParamList?.get(0)

    // Проверки подразделения
    def List<String> errorList = getErrorDepartment(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Для данного подразделения на форме настроек подразделений отсутствует значение атрибута %s", error))
    }
    errorList = getErrorVersion(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Неверно указано значение атрибута %s на форме настроек подразделений для %s", error, departmentParam.NAME.stringValue))
    }
}

/** Осуществление проверк при создании + генерация xml. */
def checkAndbildXml() {

    // проверка наличия источников в стутусе принят
    def formDataCollection = declarationService.getAcceptedFormDataSources(declarationData)
    // формируем xml

    // Получить параметры по транспортному налогу
    /** Предпослденяя дата отчетного периода на которую нужно получить настройки подразделения из справочника. */
    if (getReportPeriodEndDate() == null) {
        logger.error("Ошибка определения даты конца отчетного периода")
    }

    def departmentId = declarationData.departmentId

    departmentParamTransport = getProvider(31).getRecords(getReportPeriodEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null).get(0)

    bildXml(departmentParamTransport, formDataCollection, departmentId)
}

def bildXml(def departmentParamTransport, def formDataCollection, def departmentId) {

    def reorgFormCode = getRefBookValue(5, departmentParamTransport?.REORG_FORM_CODE?.referenceValue)?.CODE?.stringValue
    def okvedCode = getRefBookValue(34, departmentParamTransport?.OKVED_CODE?.referenceValue)?.CODE?.stringValue
    def signatoryId = getRefBookValue(35, departmentParamTransport?.SIGNATORY_ID?.referenceValue)?.CODE?.numberValue
    def taxPlaceTypeCode = getRefBookValue(2, departmentParamTransport?.TAX_PLACE_TYPE_CODE?.referenceValue)?.CODE?.stringValue

    def builder = new MarkupBuilder(xml)
    if (!declarationData.isAccepted()) {
        def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        builder.Файл(ИдФайл: declarationService.generateXmlFileId(1, departmentId, declarationData.getReportPeriodId()), ВерсПрог: departmentParamTransport.APP_VERSION, ВерсФорм: departmentParamTransport.FORMAT_VERSION) {
            Документ(
                    КНД: "1152004",
                    ДатаДок: (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
                    Период: 34,
                    ОтчетГод: reportPeriod.taxPeriod.year,
                    КодНО: departmentParamTransport.TAX_ORGAN_CODE,
                    НомКорр: reportPeriodService.getCorrectionPeriodNumber(declarationData.getReportPeriodId(), departmentId),
                    ПоМесту: taxPlaceTypeCode
            ) {
                def svnp = [ОКВЭД: okvedCode]
                if (departmentParamTransport.OKVED_CODE) {
                    svnp.Тлф = departmentParamTransport.PHONE
                }
                СвНП(svnp) {
                    НПЮЛ(
                            НаимОрг: departmentParamTransport.NAME,
                            ИННЮЛ: (departmentParamTransport.INN),
                            КПП: (departmentParamTransport.KPP)) {

                        if (reorgFormCode != null && !reorgFormCode.equals("")) {
                            СвРеоргЮЛ(
                                    ФормРеорг: reorgFormCode,
                                    ИННЮЛ: (Integer.parseInt(reorgFormCode) in [1, 2, 3, 5, 6] ? departmentParamTransport.REORG_INN : 0),
                                    КПП: (Integer.parseInt(reorgFormCode) in [1, 2, 3, 5, 6] ? departmentParamTransport.REORG_KPP : 0)
                            )
                        }
                    }
                }

                Подписант(ПрПодп: signatoryId) {
                    ФИО(
                            "Фамилия": departmentParamTransport.SIGNATORY_SURNAME,
                            "Имя": departmentParamTransport.SIGNATORY_FIRSTNAME,
                            "Отчество": departmentParamTransport.SIGNATORY_LASTNAME
                    )
                    // СвПред - Сведения о представителе налогоплательщика
                    if (signatoryId == 2) {
                        def svPred = ["НаимДок": departmentParamTransport.APPROVE_DOC_NAME]
                        if (departmentParamTransport.APPROVE_ORG_NAME)
                            svPred.НаимОрг = departmentParamTransport.APPROVE_ORG_NAME
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
                        formDataMap[reportPeriod.order] = formDataCollection?.find(departmentId, 200, FormDataKind.SUMMARY)
                        if (formDataMap[reportPeriod.order] != null) {
                            // «Своя» сводная есть и «Принята»
                            rowsDataMap[reportPeriod.order] = formDataService.getDataRowHelper(formDataMap[reportPeriod.order]).getAllCached()

                            // Заполнение данных предыдущих кварталов
                            if (reportPeriod.order > 1) {
                                ((reportPeriod.order - 1)..1).each { order ->
                                    reportPeriodMap[order] = reportPeriodService.getPrevReportPeriod(reportPeriodMap[order + 1].id)
                                    formDataMap[order] = formDataService.find(formDataMap[reportPeriod.order].formType.id, formDataMap[reportPeriod.order].kind, formDataMap[reportPeriod.order].departmentId, reportPeriodMap[order].id)
                                    if (formDataMap[order] != null && formDataMap[order].state == WorkflowState.ACCEPTED) {
                                        rowsDataMap[order] = formDataService.getDataRowHelper(formDataMap[order]).getAllCached()
                                    }
                                }
                            }
                        }

                        // Формирование данных для СумПУ
                        def resultMap = [:]

                        // По кварталам с начала года до текущего
                        (1..reportPeriod.order).each { order ->
                            def rowsData = rowsDataMap[order]
                            rowsData.each { row ->
                                if (row.getAlias() != "total") {
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

                                    def boolean obligation = (departmentParamTransport?.PREPAYMENT?.numberValue == 1)

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
                                            resultMap[row.okato].amountOfTheAdvancePayment1 += (obligation ? 0.25 * row.taxBase * taxRate : 0.0)
                                            break;
                                        case 2:
                                            // АвПУКв2 = В т.ч. сумма авансовых платежей, исчисленная к уплате в бюджет за второй квартал //// Заполняется во 2, 3, 4 отчетном периоде.
                                            resultMap[row.okato].amountOfTheAdvancePayment2 += (obligation ? 0.25 * row.taxBase * taxRate : 0.0)
                                            break;
                                        case 3:
                                            // АвПУКв3 = В т.ч. сумма авансовых платежей, исчисленная к уплате в бюджет за третий квартал //// Заполняется во 3, 4 отчетном периоде.
                                            resultMap[row.okato].amountOfTheAdvancePayment3 += (obligation ? 0.25 * row.taxBase * taxRate : 0.0)
                                            break;
                                    }

                                    // НалПУ = НалИсчисл – (АвПУКв1+ АвПУКв2+ АвПУКв3)
                                    resultMap[row.okato].amountOfTaxPayable = resultMap[row.okato].calculationOfTaxes - (
                                            resultMap[row.okato].amountOfTheAdvancePayment1 + resultMap[row.okato].amountOfTheAdvancePayment2 + resultMap[row.okato].amountOfTheAdvancePayment3
                                    )
                                    // В случае  если полученное значение отрицательно, - не заполняется
                                    // resultMap[row.okato].amountOfTaxPayable = resultMap[row.okato].amountOfTaxPayable < 0 ? 0:resultMap[row.okato].amountOfTaxPayable;
                                }
                            }
                        }

                        resultMap.each { okato, row ->
                            СумПУ(
                                    ОКТМО: getRefBookValue(96, okato)?.CODE?.stringValue,
                                    НалИсчисл: row.taxSumToPay,
                                    АвПУКв1: row.amountOfTheAdvancePayment1.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                                    АвПУКв2: row.amountOfTheAdvancePayment2.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                                    АвПУКв3: row.amountOfTheAdvancePayment3.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                                    НалПУ: row.amountOfTaxPayable.setScale(0, BigDecimal.ROUND_HALF_UP).intValue(),
                            ) {
                                row.rowData.each { tRow ->
                                    def taxBenefitCode = tRow.taxBenefitCode ? getRefBookValue(6, tRow.taxBenefitCode)?.CODE?.stringValue : null
                                    // TODO есть поля которые могут не заполняться, в нашем случае опираться какой логики?
                                    РасчНалТС(
                                            [
                                                    КодВидТС: getRefBookValue(42, tRow.tsTypeCode)?.CODE?.stringValue,
                                                    ИдНомТС: tRow.vi, //
                                                    МаркаТС: tRow.model, //
                                                    РегЗнакТС: tRow.regNumber,
                                                    НалБаза: tRow.taxBase,
                                                    ОКЕИНалБаза: getRefBookValue(12, tRow.taxBaseOkeiUnit)?.CODE?.stringValue,
                                            ]
                                                    + (tRow.ecoClass ? [ЭкологКл: getRefBookValue(40, tRow.ecoClass)?.CODE?.numberValue] : []) + //
                                                    [
                                                            ВыпускТС: tRow.years, //
                                                            ВладенТС: tRow.ownMonths,
                                                            КоэфКв: tRow.coef362,
                                                            НалСтавка: getRefBookValue(41, tRow.taxRate)?.VALUE?.numberValue,
                                                            СумИсчисл: tRow.calculatedTaxSum,
                                                    ]
                                                    + (taxBenefitCode && tRow.benefitStartDate ? [ЛьготМесТС: getBenefitMonths(tRow)] : []) +
                                                    [
                                                            СумИсчислУпл: tRow.taxSumToPay,
                                                    ] +
                                                    (taxBenefitCode && tRow.coefKl ? [КоэфКл: tRow.coefKl] : []),
                                    ) {
                                        // генерация КодОсвНал
                                        if (taxBenefitCode != null && (taxBenefitCode.equals('30200') || taxBenefitCode.equals('20210'))) {
                                            def l = taxBenefitCode;
                                            def x = "";
                                            if (l.equals("30200")) {
                                                //
                                            } else {
                                                def param = getParam(tRow.taxBenefitCode, tRow.okato);

                                                if (param != null) {
                                                    def section = param.SECTION.toString()
                                                    def item = param.ITEM.toString()
                                                    def subitem = param.SUBITEM.toString()
                                                    x = ((section.size() < 4 ? '0' * (4 - section.size()) + section : section)
                                                            + (item.size() < 4 ? '0' * (4 - item.size()) + item : item)
                                                            + (subitem.size() < 4 ? '0' * (4 - subitem.size()) + subitem : subitem))
                                                }
                                            }
                                            def kodOsnNal = (l != "" ? l.toString() : "0000") +
                                                    (x != '' ? "/" + x : '')
                                            ЛьготОсвНал(
                                                    КодОсвНал: kodOsnNal,
                                                    СумОсвНал: tRow.benefitSum
                                            )
                                        }

                                        // вычисление ЛьготУменСум
                                        // не заполняется если Код налоговой льготы = 30200, 20200, 20210 или 20230
                                        if (taxBenefitCode != null && taxBenefitCode.equals("20220")) {

                                            // вычисление КодУменСум
                                            def param = getParam(tRow.taxBenefitCode, tRow.okato);
                                            def valL = taxBenefitCode;
                                            if (param != null) {
                                                def section = param.SECTION.toString()
                                                def item = param.ITEM.toString()
                                                def subitem = param.SUBITEM.toString()
                                                def valX = ((section.size() < 4 ? '0' * (4 - section.size()) + section : section)
                                                        + (item.size() < 4 ? '0' * (4 - item.size()) + item : item)
                                                        + (subitem.size() < 4 ? '0' * (4 - subitem.size()) + subitem : subitem))

                                                def kodUmenSum = (valL != "" ? valL.toString() : "0000") + "/" + valX
                                                ЛьготУменСум(КодУменСум: kodUmenSum, СумУменСум: tRow.benefitSum)
                                            }
                                        }

                                        // ЛьготСнижСтав
                                        // не заполняется если Код налоговой льготы = 30200, 20200, 20210 или 20220
                                        if (taxBenefitCode != null && taxBenefitCode.equals("20230")) {

                                            // вычисление КодСнижСтав
                                            def valL = taxBenefitCode;
                                            def param = getParam(tRow.taxBenefitCode, tRow.okato);
                                            if (param != null) {
                                                def section = param.SECTION.toString()
                                                def item = param.ITEM.toString()
                                                def subitem = param.SUBITEM.toString()
                                                def valX = ((section.size() < 4 ? '0' * (4 - section.size()) + section : section)
                                                        + (item.size() < 4 ? '0' * (4 - item.size()) + item : item)
                                                        + (subitem.size() < 4 ? '0' * (4 - subitem.size()) + subitem : subitem))

                                                def kodNizhStav = (valL != "" ? valL.toString() : "0000") + "/" + valX
                                                ЛьготСнижСтав(КодСнижСтав: kodNizhStav, СумСнижСтав: tRow.benefitSum)
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
    String dateStr = sdf.format(date)
    if (recordCache.containsKey(refBookId)) {
        Long recordId = recordCache.get(refBookId).get(dateStr + filter)
        if (recordId != null) {
            if (refBookCache != null) {
                return refBookCache.get(recordId)
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
        if (refBookCache != null)
            refBookCache.put(recordId, retVal)
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

def getBenefitMonths(def row) {
    def periodStart = reportPeriodService.getStartDate(declarationData.reportPeriodId).time
    def periodEnd = getReportPeriodEndDate()
    if ((row.benefitEndDate != null && row.benefitEndDate < periodStart) || row.benefitStartDate > periodEnd) {
        return 0
    } else {
        def end = row.benefitEndDate == null || row.benefitEndDate > periodEnd ? periodEnd : row.benefitEndDate
        def start = row.benefitStartDate < periodStart ? periodStart : row.benefitStartDate
        return (end.year * 12 + end.month) - (start.year * 12 + start.month) + 1
    }
}

List<String> getErrorDepartment(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.NAME.stringValue == null || record.NAME.stringValue.isEmpty()) {
        errorList.add("«Наименование подразделения»")
    }
    if (record.OKTMO?.referenceValue == null) {
        errorList.add("«Код по ОКТМО»")
    }
    if (record.INN.stringValue == null || record.INN.stringValue.isEmpty()) {
        errorList.add("«ИНН»")
    }
    if (record.KPP.stringValue == null || record.KPP.stringValue.isEmpty()) {
        errorList.add("«КПП»")
    }
    if (record.TAX_ORGAN_CODE.stringValue == null || record.TAX_ORGAN_CODE.stringValue.isEmpty()) {
        errorList.add("«Код налогового органа»")
    }
    if (record.OKVED_CODE?.referenceValue == null) {
        errorList.add("«Код вида экономической деятельности и по классификатору ОКВЭД»")
    }
    if (record.NAME.stringValue == null || record.NAME.stringValue.isEmpty()) {
        errorList.add("«ИНН реорганизованного обособленного подразделения»")
    }
    if (record.SIGNATORY_ID?.referenceValue == null) {
        errorList.add("«Признак лица подписавшего документ»")
    }
    if (record.SIGNATORY_SURNAME.stringValue == null || record.SIGNATORY_SURNAME.stringValue.isEmpty()) {
        errorList.add("«Фамилия подписанта»")
    }
    if (record.SIGNATORY_FIRSTNAME.stringValue == null || record.SIGNATORY_FIRSTNAME.stringValue.isEmpty()) {
        errorList.add("«Имя подписанта»")
    }
    if (record.APPROVE_DOC_NAME.stringValue == null || record.APPROVE_DOC_NAME.stringValue.isEmpty()) {
        errorList.add("«Наименование документа, подтверждающего полномочия представителя»")
    }
    if (record.TAX_PLACE_TYPE_CODE?.referenceValue == null) {
        errorList.add("«Код места, по которому представляется документ»")
    }
    errorList
}

List<String> getErrorVersion(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.FORMAT_VERSION == null || record.FORMAT_VERSION.stringValue == null || !record.FORMAT_VERSION.stringValue.equals('5.02')) {
        errorList.add("«Версия формата»")
    }
    if (record.APP_VERSION == null || record.APP_VERSION.stringValue == null || !record.APP_VERSION.stringValue.equals('XLR_FNP_TAXCOM_5_02')) {
        errorList.add("«Версия программы, с помощью которой сформирован файл»")
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
    if(refBookId == null || recordId == null){
        return null
    }
    if (!refBookCache.containsKey(recordId)) {
        refBookCache.put(recordId, refBookService.getRecordData(refBookId, recordId))
    }
    return refBookCache.get(recordId)
}