package form_template.vat.declaration_fns.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder

/**
 * Декларация по НДС. Генератор XML.
 * http://jira.aplana.com/browse/SBRFACCTAX-6453
 *
 * совпадает с "Декларация по НДС" (аудит) (declaration_audit), кроме заполнения секции "РАЗДЕЛ 2"
 *
 * TODO:
 *      - расчет для НалНеВыч не сделан, сказали пока не делать, потому что неясно как вычислять.
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkDeparmentParams(LogLevel.WARNING)
        generateXML()
        break
    case FormDataEvent.CHECK:
        checkDeparmentParams(LogLevel.ERROR)
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
        checkDeparmentParams(LogLevel.ERROR)
        logicCheck()
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

/** Получение провайдера с использованием кеширования. */
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

/** Разыменование с использованием кеширования. */
def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

void checkDeparmentParams(LogLevel logLevel) {
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
        logger.log(logLevel, String.format("Для данного подразделения на форме настроек подразделений отсутствует значение атрибута %s", error))
    }
    errorList = getErrorVersion(departmentParam)
    for (String error : errorList) {
        logger.log(logLevel, String.format("Неверно указано значение атрибута %s на форме настроек подразделений", error))
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
    if (record.SIGNATORY_ID?.referenceValue == null) {
        errorList.add("«Признак лица подписавшего документ»")
    }
    if (record.SIGNATORY_SURNAME.stringValue == null || record.SIGNATORY_SURNAME.stringValue.isEmpty()) {
        errorList.add("«Фамилия подписанта»")
    }
    if (record.SIGNATORY_FIRSTNAME.stringValue == null || record.SIGNATORY_FIRSTNAME.stringValue.isEmpty()) {
        errorList.add("«Имя подписанта»")
    }
    //Если ПрПодп (не пусто или не 1) и значение атрибута на форме настроек подразделений не задано
    if ((record.SIGNATORY_ID?.referenceValue != null && getRefBookValue(35, record.SIGNATORY_ID?.value)?.CODE?.value != 1) && (record.APPROVE_DOC_NAME.stringValue == null || record.APPROVE_DOC_NAME.stringValue.isEmpty())) {
        errorList.add("«Наименование документа, подтверждающего полномочия представителя»")
    }
    if (record.TAX_PLACE_TYPE_CODE?.referenceValue == null) {
        errorList.add("«Код места, по которому представляется документ»")
    }
    errorList
}

List<String> getErrorVersion(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.FORMAT_VERSION.stringValue == null || !record.FORMAT_VERSION.stringValue.equals('5.03')) {
        errorList.add("«Версия формата»")
    }
    if (record.APP_VERSION.stringValue == null || !record.APP_VERSION.stringValue.equals('XLR_FNP_TAXCOM_5_03')) {
        errorList.add("«Версия программы, с помощью которой сформирован файл»")
    }
    errorList
}

/**
 * Запуск генерации XML
 */
void generateXML() {
    // Тип декларации
    def declarationType = 4
    def departmentId = declarationData.departmentId
    def reportPeriodId = declarationData.reportPeriodId

    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)

    // Код формы отчетности по КНД
    def String KND = '1151001'

    // Параметры подразделения - начало
    def departmentParam = getProvider(RefBook.DEPARTMENT_CONFIG_VAT).getRecords(getEndDate() - 1, null, "DEPARTMENT_ID = $departmentId", null).get(0)
    if (departmentParam == null) {
        throw new Exception('Ошибка при получении настроек обособленного подразделения')
    }
    def taxOrganCode = departmentParam?.TAX_ORGAN_CODE?.value
    def okvedCode = getRefBookValue(34, departmentParam?.OKVED_CODE?.value)?.CODE?.value // ОКВЭД
    def okato = getRefBookValue(96, departmentParam?.OKTMO?.value)?.CODE?.value // ОКАТО
    def taxPlaceTypeCode = getRefBookValue(2, departmentParam?.TAX_PLACE_TYPE_CODE?.value)?.CODE?.value // По месту
    def signatoryId = getRefBookValue(35, departmentParam?.SIGNATORY_ID?.value)?.CODE?.value
    def phone = departmentParam?.PHONE?.value
    def name = departmentParam?.NAME?.value
    def inn = departmentParam?.INN?.value
    def kpp = departmentParam?.KPP?.value
    def appVersion = departmentParam?.APP_VERSION?.value
    def formatVersion = departmentParam?.FORMAT_VERSION?.value
    def surname = departmentParam?.SIGNATORY_SURNAME?.value
    def firstname = departmentParam?.SIGNATORY_FIRSTNAME?.value
    def lastname = departmentParam?.SIGNATORY_LASTNAME?.value
    def approveDocName = departmentParam?.APPROVE_DOC_NAME?.value
    def approveOrgName = departmentParam?.APPROVE_ORG_NAME?.value
    // Параметры подразделения - конец

    // Список данных форм-источников
    def formDataList = declarationService.getAcceptedFormDataSources(declarationData).getRecords()
    // Тип формы > Строки формы
    def dataRowsMap = [:]
    for (def formData : formDataList) {
        def dataRows = formDataService.getDataRowHelper(formData)?.getAll()
        dataRowsMap.put(formData.formType.id, dataRows)
    }

    /*
     * Расчет значений декларации.
     */

    /*
     * Расчет значений для текущей декларации.
     */

    def period = 0
    if (reportPeriod.order != null) {
        def values = [21, 31, 33, 34]
        period = values[reportPeriod.order - 1]
    }

    /** ПрПодп. */
    def prPodp = (signatoryId != null ? signatoryId : 1)

    // РАЗДЕЛ 3
    // форма 724.1
    def rows724_1 = dataRowsMap[600]
    // форма 724.4
    def rows724_4 = dataRowsMap[603]
    // форма 937.1
    def rows937_1 = dataRowsMap[606]

    /** НалБаза (РеалТов18). Код строки 010 Графа 3. */
    def nalBaza010 = empty
    /** СумНал (РеалТов18). Код строки 010 Графа 5. */
    def sumNal010 = empty

    /** НалБаза (РеалТов10). Код строки 020 Графа 3. */
    def nalBaza020 = empty
    /** СумНал (РеалТов10). Код строки 020 Графа 5. */
    def sumNal020 = empty

    /** НалБаза (РеалТов118). Код строки 030 Графа 3. */
    def nalBaza030 = empty
    /** СумНал (РеалТов118). Код строки 030 Графа 5. */
    def sumNal030 = empty

    /** НалБаза (РеалТов110). Код строки 040 Графа 3. */
    def nalBaza040 = empty
    /** СумНал (РеалТов110). Код строки 040 Графа 5. */
    def sumNal040 = empty

    /** НалБаза (ОплПредПост). Код строки 070 Графа 3. */
    def nalBaza070 = empty
    /** СумНал (ОплПредПост). Код строки 070 Графа 5. */
    def sumNal070 = empty

    /** НалБаза (ОплНОТовар). Код строки 080 Графа 3. */
    def nalBaza080 = empty
    /** СумНал (ОплНОТовар). Код строки 080 Графа 5. */
    def sumNal080 = empty
    if (rows724_1) {
        def row = getDataRow(rows724_1, 'total_1_1')
        def row2 = getDataRow(rows724_1, 'total_1_2')
        def tmp = (row?.baseSum ?: empty) + (row2?.baseSum ?: empty)
        nalBaza010 = round(tmp)
        tmp = (row?.ndsSum ?: empty) + (row2?.ndsSum ?: empty)
        sumNal010 = round(tmp)

        row = getDataRow(rows724_1, 'total_2')
        nalBaza020 = round(row?.baseSum ?: empty)
        sumNal020 = round(row?.ndsSum ?: empty)

        row = getDataRow(rows724_1, 'total_3')
        nalBaza030 = round(row?.baseSum ?: empty)
        sumNal030 = round(row?.ndsSum ?: empty)

        row = getDataRow(rows724_1, 'total_4')
        nalBaza040 = round(row?.baseSum ?: empty)
        sumNal040 = round(row?.ndsSum ?: empty)

        row = getDataRow(rows724_1, 'total_5')
        nalBaza070 = round(row?.baseSum ?: empty)
        sumNal070 = round(row?.ndsSum ?: empty)

        row = getDataRow(rows724_1, 'total_6')
        nalBaza080 = round(row?.baseSum ?: empty)
        sumNal080 = round(row?.ndsSum ?: empty)
    }
    /** НалВосстОбщ. Код строки 120 Графа 5. */
    def nalVosstObsh = sumNal010 + sumNal020 + sumNal030 + sumNal040 + sumNal070 + sumNal080
    /** НалВыч171Общ. Код строки 130 Графа 5. */
    def nalVich171Obsh = empty
    if (rows724_4) {
        def tmp = getDataRow(rows724_4, 'total1')?.sum2 + getDataRow(rows724_4, 'total2')?.sum2
        nalVich171Obsh = round(tmp)
    }
    /** НалИсчПрод. Код строки 200 Графа 5. */
    def nalIschProd = empty
    if (rows937_1) {
        nalIschProd = round(getDataRow(rows937_1, 'totalA')?.nds)
    }
    /** НалВычОбщ. Код строки 220 Графа 5. */
    def nalVichObsh = nalVich171Obsh + nalIschProd
    /** НалПУ164. Код строки 240 и код строки 230.*/
    def nalPU164 = (nalVosstObsh - nalVichObsh).abs()
    // РАЗДЕЛ 3 - КОНЕЦ

    // РАЗДЕЛ 1
    /** СумПУ_173.1. Код строки декларации 040 или 050. */
    def sumPU_173_1 = nalPU164
    // РАЗДЕЛ 1 - КОНЕЦ

    /*
     * Формирование XML'ки.
     */

    def builder = new MarkupBuilder(xml)
    builder.Файл(
            ИдФайл: declarationService.generateXmlFileId(declarationType, departmentId, reportPeriodId),
            ВерсПрог: appVersion,
            ВерсФорм: formatVersion) {
        Документ(
                // ТИТУЛЬНЫЙ ЛИСТ
                // Код формы отчетности по КНД
                КНД: KND,
                // Дата формирования документа
                ДатаДок: (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
                // Код налогового (отчетного) периода
                Период : period,
                // Отчетный год
                ОтчетГод: reportPeriodService.get(reportPeriodId).taxPeriod.year,
                // Код налогового органа
                КодНО: taxOrganCode,
                // Номер корректировки
                НомКорр: reportPeriodService.getCorrectionPeriodNumber(reportPeriodId, departmentId),
                // Код места, по которому представляется документ
                ПоМесту: taxPlaceTypeCode
        ) {
            // ТИТУЛЬНЫЙ ЛИСТ
            СвНП(
                    ОКВЭД: okvedCode,
                    Тлф: phone
            ) {
                НПЮЛ(
                        НаимОрг: name,
                        ИННЮЛ: inn,
                        КПП: kpp
                )
            }

            Подписант(ПрПодп: prPodp) {
                ФИО(
                        [Фамилия: surname] +
                                [Имя: firstname] +
                                (lastname != null && !lastname.isEmpty() ? [Отчество: lastname] : [:])
                )
                if (prPodp == 2) {
                    СвПред(
                            НаимДок: approveDocName, // в xml быть не должно (чтз 8.1.1). не заполняется если ПрПодп = 1
                            НаимОрг: approveOrgName
                    )
                }
            }
            // ТИТУЛЬНЫЙ ЛИСТ - КОНЕЦ

            НДС() {
                // РАЗДЕЛ 1
                СумУплНП(
                        ОКАТО: okato,
                        КБК: '18210301000011000110',
                        'СумПУ_173.5': empty,
                        'СумПУ_173.1': sumPU_173_1
                )
                // РАЗДЕЛ 1 - КОНЕЦ

                // РАЗДЕЛ 2
                def section2Rows = getSection2Rows(dataRowsMap)
                for (def row : section2Rows) {
                    СумУплНА(
                            // КППИно: empty, // в xml быть не должно (чтз 8.1.1)
                            КБК: '18210301000011000110',
                            ОКАТО: okato,
                            СумИсчисл: row.sumIschisl,
                            КодОпер: row.codeOper,
                            СумИсчислОтгр: empty,
                            СумИсчислОпл: empty,
                            СумИсчислНА: empty
                    ) {
                        if (row.innULProd != null) {
                            СведПродЮЛ(
                                    НаимПрод: row.naimProd,
                                    ИННЮЛПрод: row.innULProd
                            )
                        } else {
                            СведПродЮЛ(
                                    НаимПрод: row.naimProd
                            )
                        }
                    }
                }
                // пустой раздел 2
                if (!section2Rows) {
                    СумУплНА(
                            // КППИно: empty, // в xml быть не должно (чтз 8.1.1)
                            КБК: '18210301000011000110',
                            ОКАТО: okato,
                            СумИсчисл: empty,
                            КодОпер: empty,
                            СумИсчислОтгр: empty,
                            СумИсчислОпл: empty,
                            СумИсчислНА: empty
                    ) {
                        СведПродЮЛ(
                                НаимПрод: empty,
                                ИННЮЛПрод: empty,
                        )
                    }
                }
                // РАЗДЕЛ 2 - КОНЕЦ

                // РАЗДЕЛ 3
                СумУпл164(
                        НалПУ164: nalPU164
                ) {
                    СумНалОб(
                            НалВосстОбщ: nalVosstObsh
                    ) {
                        РеалТов18(
                                НалБаза: nalBaza010,
                                СумНал: sumNal010
                        )
                        РеалТов10(
                                НалБаза: nalBaza020,
                                СумНал: sumNal020
                        )

                        РеалТов118(
                                НалБаза: nalBaza030,
                                СумНал: sumNal030
                        )
                        РеалТов110(
                                НалБаза: nalBaza040,
                                СумНал: sumNal040
                        )
                        РеалПредИК(
                                НалБаза: empty,
                                СумНал: empty
                        )
                        ВыпСМРСоб(
                                НалБаза: empty,
                                СумНал: empty
                        )
                        ОплПредПост(
                                НалБаза: nalBaza070,
                                СумНал: sumNal070
                        )
                        ОплНОТовар(
                                НалБаза: nalBaza080,
                                СумНал: sumNal080
                        )
                        СумНалВосст(
                                СумНалВс: empty,
                                СумНалСтав0: empty,
                                СумНал170: empty
                        )
                    }
                    СумНалВыч(
                            НалВычОбщ: nalVichObsh,
                            НалПредНППок: empty,
                            НалИсчСМР: empty,
                            НалИсчПрод: nalIschProd,
                            НалУплПокНА: empty
                    ) {
                        НалВыч171(
                                НалВыч171Общ: nalVich171Obsh,
                                НалВычКапСтр: empty
                        )
                        НалВычТамож(
                                НалВычВс: empty,
                                НалУплТО: empty,
                                НалУплНО: empty
                        )
                    }
                }
                // РАЗДЕЛ 3 - КОНЕЦ

                // РАЗДЕЛ 4
                НалПодтв0(
                        СумУменИтог: empty
                ) {
                    // форма 724.2.2
                    for (def row : dataRowsMap[602]) {
                        if (row.getAlias() == 'itog') {
                            continue
                        }
                        СумОпер4(
                                КодОпер: row.code,
                                НалБаза: round(row.base),
                                НалВычПод: empty,
                                НалНеПод: empty,
                                НалВосст: empty
                        )
                    }
                    // пустой раздел 4
                    if (!dataRowsMap[602]) {
                        СумОпер4(
                                КодОпер: empty,
                                НалБаза: empty,
                                НалВычПод: empty,
                                НалНеПод: empty,
                                НалВосст: empty
                        )
                    }
                }
                // РАЗДЕЛ 4 - КОНЕЦ

                // РАЗДЕЛ 7
                ОперНеНал(ОплПостСв6Мес: empty) {
                    // форма 724.2.1
                    for (def row : dataRowsMap[601]) {
                        if (row.getAlias() == 'itog') {
                            continue
                        }
                        СумОпер7(
                                КодОпер: row.code,
                                СтРеалТов: round(row.realizeCost),
                                СтПриобТов: round(row.obtainCost ?: empty),
                                НалНеВыч: getNalNeVich(row) // TODO (Ramil Timerbaev)  недоделано
                        )
                    }
                    // пустой раздел 7
                    if (!dataRowsMap[601]) {
                        СумОпер7(
                                КодОпер: empty,
                                СтРеалТов: empty,
                                СтПриобТов: empty,
                                НалНеВыч: empty
                        )
                    }
                }
                // РАЗДЕЛ 7 - КОНЕЦ
            }
        }
    }
}

/**
 * Получить список значений для раздела 2.
 *
 * @param dataRowsMap мапа со строками форм-источников
 * @return список мап со значениями
 */
def getSection2Rows(def dataRowsMap) {
    def rows = []
    // форма 724.6
    for (def row : dataRowsMap[604]) {
        if (row.getAlias() != null) {
            continue
        }
        def newRow = [:]
        newRow.sumIschisl = round(row.sum2)
        newRow.codeOper = '1011712'
        newRow.naimProd = row.contragent
        //newRow.innULProd = empty
        rows.add(newRow)
    }
    // форма 724.7
    for (def row : dataRowsMap[605]) {
        if (row.getAlias() != null) {
            continue
        }
        def newRow = [:]
        newRow.sumIschisl = round(row.ndsSum)
        newRow.codeOper = '1011703'
        newRow.naimProd = row.name
        newRow.innULProd = row.inn
        rows.add(newRow)
    }
    return rows
}

def round(def value) {
    return ((BigDecimal) value)?.setScale(0, BigDecimal.ROUND_HALF_UP)
}

/**
 * Получить значение для НалНеВыч.
 *
 * @param row строка формы 724.2.1
 */
def getNalNeVich(def row) {
    // TODO (Ramil Timerbaev) сказали пока не делать, потому что неясно как вычислять.
    // сумма из отчета 102
    // сумма из расходов простых
    // разность сумм
    return empty
}

/** Логические проверки. */
void logicCheck() {
    // получение данных из xml'ки
    def xmlString = declarationService.getXmlData(declarationData.id)
    xmlString = xmlString.replace('<?xml version="1.0" encoding="windows-1251"?>', '')
    def xmlData = new XmlSlurper().parseText(xmlString)

    // 1.4
    /** НалВосстОбщ. Код строки 120 Графа 5. */
    def nalVosstObsh = xmlData.Документ.НДС.СумУпл164.СумНалОб.@НалВосстОбщ.text() as BigDecimal
    /** НалВычОбщ. Код строки 220 Графа 5. */
    def nalVichObsh = xmlData.Документ.НДС.СумУпл164.СумНалВыч.@НалВычОбщ.text() as BigDecimal
    /** НалПУ164. Код строки 240 и код строки 230.*/
    def nalPU164 = xmlData.Документ.НДС.СумУпл164.@НалПУ164.text() as BigDecimal
    if (nalVosstObsh == 0 && nalVichObsh != 0 && nalPU164 != 0) {
        logger.warn('КС 1.4. Возможно нарушение ст. 170, 171, 172 НК РФ: Необоснованное применение налоговых вычетов. ' +
                'При проведении мероприятий налогового контроля следует учитывать письмо ФНС России от 27.06.2007 N ММ-14-08/275 дсп')
    }

    // 1.5
    /** НалБаза (РеалТов18). Код строки 010 Графа 3. */
    def nalBaza010 = xmlData.Документ.НДС.СумУпл164.СумНалОб.РеалТов18.@НалБаза.text() as BigDecimal
    /** НалБаза (РеалТов10). Код строки 020 Графа 3. */
    def nalBaza020 = xmlData.Документ.НДС.СумУпл164.СумНалОб.РеалТов18.@НалБаза.text() as BigDecimal
    /** НалБаза (РеалТов118). Код строки 030 Графа 3. */
    def nalBaza030 = xmlData.Документ.НДС.СумУпл164.СумНалОб.РеалТов18.@НалБаза.text() as BigDecimal
    /** НалБаза (РеалТов110). Код строки 040 Графа 3. */
    def nalBaza040 = xmlData.Документ.НДС.СумУпл164.СумНалОб.РеалТов18.@НалБаза.text() as BigDecimal
    def tmp1 = nalBaza010 + nalBaza020 + nalBaza030 + nalBaza040
    def divider = tmp1 + getSumSection7ByCode(xmlData, 'СтРеалТов')
    tmp1 = (divider != 0 ? tmp1 / divider : empty)

    /** НалВыч171Общ. Код строки 130 Графа 5. */
    def nalVich171Obsh = xmlData.Документ.НДС.СумУпл164.СумНалВыч.НалВыч171.@НалВыч171Общ.text() as BigDecimal
    def tmp2 = nalVich171Obsh
    divider = tmp2 + getSumSection7ByCode(xmlData, 'НалНеВыч')
    tmp2 = (divider != 0 ? tmp2 / divider : empty)

    if (tmp1 != tmp2) {
        logger.warn('КС 1.5. Возможно нарушение ст. 149, 170 п. 4 НК РФ: Возможно необоснованное применение налоговых вычетов')
    }

    // 1.14
    /** НалИсчПрод. Код строки 200 Графа 5. */
    def nalIschProd = xmlData.Документ.НДС.СумУпл164.СумНалВыч.@НалИсчПрод.text() as BigDecimal
    /** СумНал (РеалТов18). Код строки 010 Графа 5. */
    def sumNal010 = xmlData.Документ.НДС.СумУпл164.СумНалОб.РеалТов18.@СумНал.text() as BigDecimal
    /** СумНал (РеалТов10). Код строки 020 Графа 5. */
    def sumNal020 = xmlData.Документ.НДС.СумУпл164.СумНалОб.РеалТов10.@СумНал.text() as BigDecimal
    /** СумНал (РеалТов118). Код строки 030 Графа 5. */
    def sumNal030 = xmlData.Документ.НДС.СумУпл164.СумНалОб.РеалТов118.@СумНал.text() as BigDecimal
    /** СумНал (РеалТов110). Код строки 040 Графа 5. */
    def sumNal040 = xmlData.Документ.НДС.СумУпл164.СумНалОб.РеалТов110.@СумНал.text() as BigDecimal

    if (nalIschProd < (sumNal010 + sumNal020 + sumNal030 + sumNal040)) {
        logger.warn('КС 1.14. Возможно нарушение ст. 171 п. 8, 172 п. 6 либо ст. 146 п. 1 НК РФ: ' +
                'Налоговые вычеты не обоснованы, либо налоговая база занижена, так как суммы отработанных авансов не включены в реализацию')
    }
}

/**
 * Получить сумму атрибута paramName из раздела 7.
 *
 * @param xmlData xml
 * @param paramName имя атрибута в xml (графа 2 'СтРеалТов' или графа 4 'НалНеВыч')
 */
def getSumSection7ByCode(def xmlData, def paramName) {
    def sum = BigDecimal.ZERO
    xmlData.Документ.НДС.ОперНеНал.СумОпер7.each {
        def code = it.@КодОпер.text() as BigDecimal
        def value = it.@"$paramName".text() as BigDecimal
        if (1010800 <= code && code <= 1010814) {
            sum += value
        }
    }
    return sum
}