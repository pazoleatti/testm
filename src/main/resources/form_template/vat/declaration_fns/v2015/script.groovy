package form_template.vat.declaration_fns.v2015

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder
import org.apache.commons.collections.map.HashedMap

import javax.xml.stream.XMLStreamReader

/**
 * Декларация по НДС (раздел 1-7)
 *
 * совпадает с "Декларация по НДС (аудит, раздел 1-7)" и "Декларация по НДС (короткая, раздел 1-7)", кроме заполнения секции "РАЗДЕЛ 2"
 *
 * declarationTemplateId=2004
 */

@Field
def declarationType = 4;

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkDepartmentParams(LogLevel.WARNING)
        break
    case FormDataEvent.CHECK:
        checkDepartmentParams(LogLevel.ERROR)
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
        checkDepartmentParams(LogLevel.ERROR)
        logicCheck()
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

@Field
def providerCache = [:]
@Field
def refBookCache = [:]
@Field
def income102DataCache = [:]

@Field
def empty = 0

// Параметры подразделения
@Field
def departmentParam = null

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

@Field
def prevReportPeriodEndDate = null

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

def getPrevEndDate() {
    if (prevReportPeriodEndDate == null) {
        prevReportPeriodEndDate = reportPeriodService.getEndDate(reportPeriodService.getPrevReportPeriod(declarationData.reportPeriodId)?.id)?.time
    }
    return prevReportPeriodEndDate
}

// Разыменование с использованием кеширования
def getRefBookValue(def long refBookId, def recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение провайдера с использованием кеширования
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

BigDecimal getXmlDecimal(def reader, String attrName) {
    def value = reader?.getAttributeValue(null, attrName)
    if (!value) {
        return null
    }
    return new BigDecimal(value)
}

/**
 * Ищет точное ли совпадение узлов дерева xml c текущими незакрытыми элементами
 * @param nodeNames ожидаемые элементы xml
 * @param elements незакрытые элементы
 * @return
 */
boolean isCurrentNode(List<String> nodeNames, Map<String, Boolean> elements) {
    nodeNames.add('Файл')
    def enteredNodes = elements.findAll { it.value }.keySet() // узлы в которые вошли, но не вышли еще
    enteredNodes.containsAll(nodeNames) && enteredNodes.size() == nodeNames.size()
}

// Мапа соответсвтия id и наименований типов деклараций 8-11
def declarations() {
    [
            declaration8 : [12, 'Декларация по НДС (раздел 8)'],
            declaration8n: [18, 'Декларация по НДС (раздел 8 без консолид. формы)'],
            declaration81: [13, 'Декларация по НДС (раздел 8.1)'],
            declaration9 : [14, 'Декларация по НДС (раздел 9)'],
            declaration9n: [21, 'Декларация по НДС (раздел 9 без консолид. формы)'],
            declaration91: [15, 'Декларация по НДС (раздел 9.1)'],
            declaration10: [16, 'Декларация по НДС (раздел 10)'],
            declaration11: [17, 'Декларация по НДС (раздел 11)']
    ]
}

/**
 *  Структура для хранения данных о декларациях 8-11
 *  (id, название декларации, признак существования, признак принятости, имя файла)
 */
@Field
def Map<Long, Expando> declarationParts = null

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
            throw new Exception("Ошибка при получении настроек обособленного подразделения. Настройки подразделения заполнены не полностью")
        }
        departmentParam = departmentParamList?.get(0)
    }
    return departmentParam
}

List<String> getErrorDepartment(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.NAME?.stringValue == null || record.NAME.stringValue.isEmpty()) {
        errorList.add("«Наименование подразделения»")
    }
    if (record.OKTMO?.referenceValue == null) {
        errorList.add("«ОКТМО»")
    }
    if (record.INN?.stringValue == null || record.INN.stringValue.isEmpty()) {
        errorList.add("«ИНН»")
    }
    if (record.KPP?.stringValue == null || record.KPP.stringValue.isEmpty()) {
        errorList.add("«КПП»")
    }
    if (record.TAX_ORGAN_CODE?.stringValue == null || record.TAX_ORGAN_CODE.stringValue.isEmpty()) {
        errorList.add("«Код налогового органа (кон.)»")
    }
    if (useTaxOrganCodeProm() && (record.TAX_ORGAN_CODE_PROM?.value == null || record.TAX_ORGAN_CODE_PROM.value.isEmpty())) {
        errorList.add("«Код налогового органа (пром.)»")
    }
    if (record.OKVED_CODE?.referenceValue == null) {
        errorList.add("«Код вида экономической деятельности и по классификатору ОКВЭД»")
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
    //Если ПрПодп (не пусто или не 1) и значение атрибута на форме настроек подразделений не задано
    if ((record.SIGNATORY_ID?.referenceValue != null && getRefBookValue(35, record.SIGNATORY_ID?.value)?.CODE?.value != 1)
            && (record.APPROVE_DOC_NAME?.stringValue == null || record.APPROVE_DOC_NAME.stringValue.isEmpty())) {
        errorList.add("«Наименование документа, подтверждающего полномочия представителя»")
    }
    if (record.TAX_PLACE_TYPE_CODE?.referenceValue == null) {
        errorList.add("«Код места, по которому представляется документ»")
    }
    def reorgFormCode = getRefBookValue(5, record?.REORG_FORM_CODE?.value)?.CODE?.value
    if (reorgFormCode != null && reorgFormCode != '0') {
        if (record.REORG_INN?.value == null || record.REORG_INN.value.isEmpty()) {
            errorList.add("«ИНН реорганизованной организации»")
        }
        if (record.REORG_KPP?.value == null || record.REORG_KPP.value.isEmpty()) {
            errorList.add("«КПП реорганизованной организации»")
        }
    }
    errorList
}

@Field
def declarationReportPeriod

boolean useTaxOrganCodeProm() {
    if (declarationReportPeriod == null) {
        declarationReportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    }
    return (declarationReportPeriod?.taxPeriod?.year > 2015 || declarationReportPeriod?.order > 2)
}

List<String> getErrorVersion(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.FORMAT_VERSION.stringValue == null || !record.FORMAT_VERSION.stringValue.equals('5.04')) {
        errorList.add("«Версия формата»")
    }
    errorList
}

def getDataRowSum(def dataRows, def String rowAlias, def String cellAlias, def useRound = true) {
    def sum = empty
    for (DataRow<Cell> row : dataRows) {
        if (rowAlias.equals(row.getAlias())) {
            sum += row.getCell(cellAlias).value ?: empty
        }
    }
    return (useRound ? round(sum) : sum)
}

void generateXML() {
    // атрибуты, заполняемые по настройкам подразделений
    def departmentParam = getDepartmentParam()
    def taxOrganCode = departmentParam?.TAX_ORGAN_CODE?.value
    def taxOrganCodeProm = useTaxOrganCodeProm() ? departmentParam?.TAX_ORGAN_CODE_PROM?.value : taxOrganCode
    def okvedCode = getRefBookValue(34, departmentParam?.OKVED_CODE?.value)?.CODE?.value
    def okato = getOkato(departmentParam?.OKTMO?.value)
    def taxPlaceTypeCode = getRefBookValue(2, departmentParam?.TAX_PLACE_TYPE_CODE?.value)?.CODE?.value
    def signatoryId = getRefBookValue(35, departmentParam?.SIGNATORY_ID?.value)?.CODE?.value
    def phone = departmentParam?.PHONE?.value
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
    def reorgFormCode = departmentParam?.REORG_FORM_CODE?.referenceValue
    def prPodp = (signatoryId != null ? signatoryId : 1)

    def has8 = (isDeclarationExist(declarations().declaration8[0]) == 1)
    def has8n = (isDeclarationExist(declarations().declaration8n[0]) == 1)

    def has9 = (isDeclarationExist(declarations().declaration9[0]) == 1)
    def has9n = (isDeclarationExist(declarations().declaration9n[0]) == 1)

    def sign812 = hasOneOrMoreDeclaration()
    def has812 = (sign812 == 1)
    def sign8 = (has8 || has8n) ? 1 : 0
    def sign81 = isDeclarationExist(declarations().declaration81[0])
    def sign9 = (has9 || has9n) ? 1 : 0
    def sign91 = isDeclarationExist(declarations().declaration91[0])
    def sign10 = isDeclarationExist(declarations().declaration10[0])
    def sign11 = isDeclarationExist(declarations().declaration11[0])
    def sign12 = 0

    def nameDecl8 = getDeclarationFileName(has8 ? declarations().declaration8[0] : declarations().declaration8n[0])
    def nameDecl81 = getDeclarationFileName(declarations().declaration81[0])
    def nameDecl9 = getDeclarationFileName(has9 ? declarations().declaration9[0] : declarations().declaration9n[0])
    def nameDecl91 = getDeclarationFileName(declarations().declaration91[0])
    def nameDecl10 = getDeclarationFileName(declarations().declaration10[0])
    def nameDecl11 = getDeclarationFileName(declarations().declaration11[0])

    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    def period = 0
    if (reorgFormCode != null) {
        def values = [51, 54, 53, 56]
        period = values[reportPeriod.order - 1]
    } else if (reportPeriod.order != null) {
        def values = [21, 22, 23, 24]
        period = values[reportPeriod.order - 1]
    }

    // Список данных форм-источников
    def formDataList = declarationService.getAcceptedFormDataSources(declarationData, userInfo, logger).getRecords()
    // Тип формы > Строки формы
    def Map<Integer, List> dataRowsMap = [:]
    for (def formData : formDataList) {
        def dataRows = formDataService.getDataRowHelper(formData)?.getAll()
        if (dataRowsMap.containsKey(formData.formType.id)) {
            dataRowsMap.get(formData.formType.id).addAll(dataRows)
        } else {
            dataRowsMap.put(formData.formType.id, dataRows)
        }
    }

    // РАЗДЕЛ 3
    // форма 724.1
    def rows724_1 = dataRowsMap[600]
    // форма 724.4
    def rows724_4 = dataRowsMap[603]

    // TODO Вопрос к заказчику, пока не заполняем
    def nalBaza105 = empty
    def sumNal105 = empty
    def nalBaza106 = empty
    def sumNal106 = empty
    def nalBaza107 = empty
    def sumNal107 = empty
    def nalBaza108 = empty
    def sumNal108 = empty

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
    /** НалИсчПрод. Код строки 170 Графа 3. */
    def nalIschProd = empty
    if (rows724_1) {
        def totalRow1baseSum = getDataRowSum(rows724_1, 'total_1', 'baseSum', false)
        def totalRow1ndsSum = getDataRowSum(rows724_1, 'total_1', 'ndsSum', false)
        def totalRow2baseSum = getDataRowSum(rows724_1, 'total_2', 'baseSum')
        def totalRow2ndsSum = getDataRowSum(rows724_1, 'total_2', 'ndsSum')
        def totalRow3baseSum = getDataRowSum(rows724_1, 'total_3', 'baseSum', false)
        def totalRow3ndsSum = getDataRowSum(rows724_1, 'total_3', 'ndsSum', false)
        def totalRow4baseSum = getDataRowSum(rows724_1, 'total_4', 'baseSum')
        def totalRow4ndsSum = getDataRowSum(rows724_1, 'total_4', 'ndsSum')
        def totalRow5baseSum = getDataRowSum(rows724_1, 'total_5', 'baseSum')
        def totalRow5ndsSum = getDataRowSum(rows724_1, 'total_5', 'ndsSum')
        def totalRow6baseSum = getDataRowSum(rows724_1, 'total_6', 'baseSum', false)
        def totalRow6ndsSum = getDataRowSum(rows724_1, 'total_6', 'ndsSum', false)
        def totalRow7baseSum = getDataRowSum(rows724_1, 'total_7', 'baseSum', false)
        def totalRow7ndsDealSum = getDataRowSum(rows724_1, 'total_7', 'ndsDealSum')
        def totalRow7ndsBookSum = getDataRowSum(rows724_1, 'total_7', 'ndsBookSum', false)

        nalBaza010 = round(totalRow1baseSum + totalRow7baseSum)
        sumNal010 = round(totalRow1ndsSum + totalRow7ndsBookSum)

        nalBaza020 = totalRow2baseSum
        sumNal020 = totalRow2ndsSum

        nalBaza030 = round(totalRow3baseSum + totalRow6baseSum)
        sumNal030 = round(totalRow3ndsSum + totalRow6ndsSum)

        nalBaza040 = totalRow4baseSum
        sumNal040 = totalRow4ndsSum

        nalBaza070 = totalRow5baseSum
        sumNal070 = totalRow5ndsSum

        nalIschProd = totalRow7ndsDealSum
    }

    /** НалПредНППриоб . Код строки 120 Графа 3. */
    def nalPredNPPriob = empty
    /** НалУплПокНА . Код строки 180 Графа 3. */
    def nalUplPokNA = empty
    if (rows724_4) {
        nalPredNPPriob = getDataRowSum(rows724_4, 'total1', 'sum2')
        nalUplPokNA = getDataRowSum(rows724_4, 'total2', 'sum2')
    }

    /** НалВосстОбщ. Код строки 110 Графа 5. */
    def nalVosstObsh = sumNal010 + sumNal020 + sumNal030 + sumNal040 + sumNal070 + sumNal105 + sumNal106 + sumNal107 + sumNal108
    /** НалВычОбщ. Код строки 190 Графа 5. */
    def nalVichObsh = round(nalPredNPPriob + nalIschProd + nalUplPokNA)
    /** НалПУ164. Код строки 200 и код строки 210.*/
    def nalPU164 = (nalVosstObsh - nalVichObsh).longValue()

    def builder = new MarkupBuilder(xml)
    builder.Файл(
            [ИдФайл: generateXmlFileId(taxOrganCodeProm, taxOrganCode)] +
                    [ВерсПрог: applicationVersion] +
                    [ВерсФорм: formatVersion] +
                    ['ПризнНал8-12': sign812] +
                    (has812 ? [ПризнНал8: sign8] : [:]) +
                    (has812 ? [ПризнНал81: sign81] : [:]) +
                    (has812 ? [ПризнНал9: sign9] : [:]) +
                    (has812 ? [ПризнНал91: sign91] : [:]) +
                    (has812 ? [ПризнНал10: sign10] : [:]) +
                    (has812 ? [ПризнНал11: sign11] : [:]) +
                    (has812 ? [ПризнНал12: sign12] : [:])) {
        Документ(
                // ТИТУЛЬНЫЙ ЛИСТ
                КНД: '1151001',
                // Дата формирования документа
                ДатаДок: (docDate != null ? docDate : new Date()).format("dd.MM.yyyy"),
                // Налоговый период (код)
                Период: period,
                // Отчетный год
                ОтчетГод: reportPeriodService.get(declarationData.reportPeriodId).taxPeriod.year,
                // Код налогового органа
                КодНО: taxOrganCode,
                // Номер корректировки
                НомКорр: reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId),
                // Код места, по которому представляется документ
                ПоМесту: taxPlaceTypeCode
        ) {
            // ТИТУЛЬНЫЙ ЛИСТ
            СвНП(
                    [ОКВЭД: okvedCode] +
                            (phone ? [Тлф: phone] : [:])
            )
                    {
                        НПЮЛ(
                                НаимОрг: name,
                                ИННЮЛ: inn,
                                КПП: kpp
                        ) {
                            reorgFormCode = reorgFormCode != null ? getRefBookValue(5, reorgFormCode).CODE.stringValue : null
                            def boolean isReorg = reorgFormCode != null && !reorgFormCode.equals('0')

                            if (reorgFormCode != null) {
                                СвРеоргЮЛ(
                                        [ФормРеорг: reorgFormCode] +
                                                (isReorg ? [ИННЮЛ: reorgINN] : [:]) +
                                                (isReorg ? [КПП: reorgKPP] : [:])
                                )
                            }
                        }
                    }
            Подписант(ПрПодп: prPodp) {
                ФИО(
                        [Фамилия: surname] +
                                [Имя: firstname] +
                                (lastname != null && !lastname.isEmpty() ? [Отчество: lastname] : [:])
                )
                if (prPodp == 2) {
                    СвПред(
                            НаимДок: approveDocName,
                            НаимОрг: approveOrgName
                    )
                }
            }

            НДС() {
                // РАЗДЕЛ 1
                СумУплНП(
                        ОКТМО: okato,
                        КБК: '18210301000011000110',
                        'СумПУ_173.5': empty,
                        'СумПУ_173.1': nalPU164
                )

                // РАЗДЕЛ 2
                if (declarationType == 4 || declarationType == 7) {
                    for (def row : getSection2Rows(dataRowsMap)) {
                        СумУплНА(
                                КБК: '18210301000011000110',
                                ОКТМО: okato,
                                СумИсчисл: round(row.sumIschisl ?: empty),
                                КодОпер: row.codeOper,
                                СумИсчислОтгр: empty,
                                СумИсчислОпл: empty,
                                СумИсчислНА: empty
                        ) {
                            if (declarationType == 4) {
                                СведПродЮЛ(
                                        [НаимПрод: row.naimProd ?: empty] +
                                                (row.innULProd != null ? [ИННЮЛПрод: row.innULProd] : [:])
                                )
                            }
                        }
                    }
                } else if (declarationType == 20) {
                    СумУплНА(
                            КБК: '18210301000011000110',
                            ОКТМО: okato,
                            СумИсчисл: getSection2Agg(dataRowsMap)
                    )
                }

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
                        СумНалВосст(
                                СумНалВс: empty,
                                'СумНал170.3.3': empty,
                                СумНалОперСт0: empty
                        )
                        КорРеалТов18(
                                НалБаза: nalBaza105,
                                СумНал: sumNal105
                        )
                        КорРеалТов10(
                                НалБаза: nalBaza106,
                                СумНал: sumNal106
                        )
                        КорРеалТов118(
                                НалБаза: nalBaza107,
                                СумНал: sumNal107
                        )
                        КорРеалТов110(
                                НалБаза: nalBaza108,
                                СумНал: sumNal108
                        )
                        КорРеалПредИК(
                                НалБаза: empty,
                                СумНал: empty
                        )
                    }
                    СумНалВыч(
                            НалПредНППриоб: nalPredNPPriob,
                            НалПредНППок: empty,
                            НалИсчСМР: empty,
                            НалУплТамож: empty,
                            НалУплНОТовТС: empty,
                            НалИсчПрод: nalIschProd,
                            НалУплПокНА: nalUplPokNA,
                            НалВычОбщ: nalVichObsh
                    )
                }

                // РАЗДЕЛ 4
                if (dataRowsMap[602]) {
                    НалПодтв0(
                            СумИсчислИтог: empty
                    ) {
                        // форма 724.2.2
                        getDataRowSum(dataRowsMap[602], it, 'code')
                        ['R1', 'R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10', 'R11'].each {
                            code = dataRowsMap[602].find { row -> row.getAlias() == it }.code
                            СумОпер4(
                                    КодОпер: code,
                                    НалБаза: getDataRowSum(dataRowsMap[602], it, 'base'),
                                    НалВычПод: empty,
                                    НалНеПод: empty,
                                    НалВосст: empty
                            )
                        }
                    }
                }

                // РАЗДЕЛ 7
                if (dataRowsMap[601]) {
                    ОперНеНал(
                            ОплПостСв6Мес: empty
                    ) {
                        // форма 724.2.1
                        ['R1', 'R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10',
                         'R11', 'R12', 'R13', 'R14', 'R15', 'R16', 'R17', 'R18', 'R19', 'R20',
                         'R21', 'R22', 'R23', 'R24', 'R25'].each {
                            code = dataRowsMap[601].find { row -> row.getAlias() == it }.code
                            СумОпер7(
                                    КодОпер: code,
                                    СтРеалТов: getDataRowSum(dataRowsMap[601], it, 'realizeCost'),
                                    СтПриобТов: getDataRowSum(dataRowsMap[601], it, 'obtainCost'),
                                    НалНеВыч: round(getNalNeVich(code))
                            )
                        }
                    }
                }

                if (sign8 != 0) КнигаПокуп(НаимКнПок: nameDecl8)
                if (sign81 != 0) КнигаПокупДЛ(НаимКнПокДЛ: nameDecl81)
                if (sign9 != 0) КнигаПрод(НаимКнПрод: nameDecl9)
                if (sign91 != 0) КнигаПродДЛ(НаимКнПродДЛ: nameDecl91)
                if (sign10 != 0) ЖУчВыстСчФ(НаимЖУчВыстСчФ: nameDecl10)
                if (sign11 != 0) ЖУчПолучСчФ(НаимЖУчПолучСчФ: nameDecl11)
            }
        }
    }
}

// Логические проверки
void logicCheck() {
    def reader = declarationService.getXmlStreamReader(declarationData.id)
    if (reader == null) {
        return
    }

    def has8 = (isDeclarationExist(declarations().declaration8[0]) == 1)
    def has8n = (isDeclarationExist(declarations().declaration8n[0]) == 1)
    def has9 = (isDeclarationExist(declarations().declaration9[0]) == 1)
    def has9n = (isDeclarationExist(declarations().declaration9n[0]) == 1)

    declaration8 = has8n ? declarations().declaration8n[0] : declarations().declaration8[0]
    declaration9 = has9n ? declarations().declaration9n[0] : declarations().declaration9[0]
    declaration81 = declarations().declaration81[0]
    declaration91 = declarations().declaration91[0]

    def elements = [:]

    def exist8_12, exist8, exist81, exist9, exist91, exist10, exist11, exist12, existFound = false

    // Раздел 1
    def r1str050
    // Раздел 2
    def r2str060sum = 0
    // Раздел 3
    def r3g3str010, r3g3str020, r3g3str030, r3g3str040, r3g3str120, r3g3str150, r3g3str160
    def r3g3str170, r3g5str010, r3g5str020, r3g5str030, r3g5str040
    def r3g5str110, r3g3str190, r3g3str180
    // Раздел 7
    def r7g2sum = 0, r7g4sum = 0

    // Раздел 8
    def r8str190
    def r8str180sum = 0
    // Приложение 1 к Разделу  8
    def r81str190, r81str005
    def r81str180sum = 0

    // Раздел 9
    def r9str260, r9str270, r9str250
    def r9str200sum = 0, r9str210sum = 0, r9str190sum = 0
    // Приложение 1 к Разделу 9
    def r91str340, r91str350, r91str050, r91str060, r91str020, r91str310, r91str320, r91str330
    def r91str280sum = 0, r91str290sum = 0, r91str280sumAll = 0, r91str290sumAll = 0, r91str250sum = 0, r91str260sum = 0, r91str270sum = 0

    try { // ищем пока есть элементы и есть что искать
        while (reader.hasNext()) {
            if (reader.startElement) {
                elements[reader.name.localPart] = true
                if (!existFound && isCurrentNode([], elements)) {
                    existFound = true
                    exist8_12 = reader.getAttributeValue(null, "ПризнНал8-12")
                    exist8 = reader.getAttributeValue(null, "ПризнНал8")
                    exist81 = reader.getAttributeValue(null, "ПризнНал81")
                    exist9 = reader.getAttributeValue(null, "ПризнНал9")
                    exist91 = reader.getAttributeValue(null, "ПризнНал91")
                    exist10 = reader.getAttributeValue(null, "ПризнНал10")
                    exist11 = reader.getAttributeValue(null, "ПризнНал11")
                    exist12 = reader.getAttributeValue(null, "ПризнНал12")
                } else if (isCurrentNode(['Документ', 'НДС', 'СумУплНП'], elements)) {
                    r1str050 = getXmlDecimal(reader, "СумПУ_173.1") ?: 0
                } else if (isCurrentNode(['Документ', 'НДС', 'СумУплНА'], elements)) {
                    r2str060sum += getXmlDecimal(reader, "СумИсчисл") ?: 0
                } else if (!r3g5str110 && isCurrentNode(['Документ', 'НДС', 'СумУпл164', 'СумНалОб'], elements)) {
                    r3g5str110 = getXmlDecimal(reader, "НалВосстОбщ") ?: 0
                } else if (!r3g3str010 && isCurrentNode(['Документ', 'НДС', 'СумУпл164', 'СумНалОб', 'РеалТов18'], elements)) {
                    r3g3str010 = getXmlDecimal(reader, "НалБаза") ?: 0
                    r3g5str010 = getXmlDecimal(reader, "СумНал") ?: 0
                } else if (!r3g3str020 && isCurrentNode(['Документ', 'НДС', 'СумУпл164', 'СумНалОб', 'РеалТов10'], elements)) {
                    r3g3str020 = getXmlDecimal(reader, "НалБаза") ?: 0
                    r3g5str020 = getXmlDecimal(reader, "СумНал") ?: 0
                } else if (!r3g3str030 && isCurrentNode(['Документ', 'НДС', 'СумУпл164', 'СумНалОб', 'РеалТов118'], elements)) {
                    r3g3str030 = getXmlDecimal(reader, "НалБаза") ?: 0
                    r3g5str030 = getXmlDecimal(reader, "СумНал") ?: 0
                } else if (!r3g3str040 && isCurrentNode(['Документ', 'НДС', 'СумУпл164', 'СумНалОб', 'РеалТов110'], elements)) {
                    r3g3str040 = getXmlDecimal(reader, "НалБаза") ?: 0
                    r3g5str040 = getXmlDecimal(reader, "СумНал") ?: 0
                } else if (!r3g3str120 && isCurrentNode(['Документ', 'НДС', 'СумУпл164', 'СумНалВыч'], elements)) {
                    r3g3str120 = getXmlDecimal(reader, "НалПредНППриоб") ?: 0
                    r3g3str150 = getXmlDecimal(reader, "НалУплТамож") ?: 0
                    r3g3str160 = getXmlDecimal(reader, "НалУпл10ТовТС") ?: 0
                    r3g3str170 = getXmlDecimal(reader, "НалИсчПрод") ?: 0
                    r3g3str180 = getXmlDecimal(reader, "НалУплПокНА") ?: 0
                    r3g3str190 = getXmlDecimal(reader, "НалВычОбщ") ?: 0
                } else if (isCurrentNode(['Документ', 'НДС', 'ОперНеНал', 'СумОпер7'], elements)) {
                    codeOper = reader.getAttributeValue(null, "КодОпер")
                    if (codeOper.matches("10108([0,1][0-9]|20|21)")) {
                        r7g2sum += getXmlDecimal(reader, "СтРеалТов") ?: 0
                        r7g4sum += getXmlDecimal(reader, "НалНеВыч") ?: 0
                    }
                }
            }
            if (reader.endElement) {
                elements[reader.name.localPart] = false
            }
            reader.next()
        }
    } finally {
        reader.close()
    }

    def reader8
    if (has8) {
        reader8 = declarationService.getXmlStreamReader(getParts().get(declarations().declaration8[0]).id)
    } else if (has8n) {
        reader8 = declarationService.getXmlStreamReader(getParts().get(declarations().declaration8n[0]).id)
    }
    def reader81
    if (isDeclarationExist(declaration81) == 1) {
        reader81 = declarationService.getXmlStreamReader(getParts().get(declaration81).id)
    }
    def reader9
    if (has9) {
        reader9 = declarationService.getXmlStreamReader(getParts().get(declarations().declaration9[0]).id)
    } else if (has9n) {
        reader9 = declarationService.getXmlStreamReader(getParts().get(declarations().declaration9n[0]).id)
    }
    def reader91
    if (isDeclarationExist(declaration91) == 1) {
        reader91 = declarationService.getXmlStreamReader(getParts().get(declaration91).id)
    }

    if (reader8 != null) {
        //elements = [:]
        try {
            while (reader8.hasNext()) {
                if (reader8.startElement) {
                    //elements[reader8.name.localPart] = true
                    if ("КодВидОпер".equals(reader8.name.localPart)) {
                        if ('06'.equals(reader8.getElementText())) {
                            r8str180sum += r8str180sumTmp
                        }
                    }
                    if (!r8str190 && "КнигаПокуп".equals(reader8.name.localPart)) {
                        r8str190 = getXmlDecimal(reader8, "СумНДСВсКПк") ?: 0
                    } else if ("КнПокСтр".equals(reader8.name.localPart)) {
                        r8str180sumTmp = getXmlDecimal(reader8, "СумНДСВыч") ?: 0
                    }
                }
                /*if (reader8.endElement) {
                    elements[reader8.name.localPart] = false
                } */
                reader8.next()
            }
        } finally {
            reader8.close()
        }
    }

    if (reader81 != null) {
        elements = [:]
        try {
            while (reader81.hasNext()) {
                if (reader81.startElement) {
                    elements[reader81.name.localPart] = true
                    if ("КодВидОпер".equals(reader81.name.localPart)) {
                        if ('06'.equals(reader81.getElementText())) {
                            r81str180sum += r81str180sumTmp
                        }
                    }
                    if (!r81str190 && isCurrentNode(['Документ', 'КнигаПокупДЛ'], elements)) {
                        r81str190 = getXmlDecimal(reader81, "СумНДСИтП1Р8") ?: 0
                        r81str005 = getXmlDecimal(reader81, "СумНДСИтКПк") ?: 0
                    }
                    if (isCurrentNode(['Документ', 'КнигаПокупДЛ', 'КнПокДЛСтр'], elements)) {
                        r81str180sumTmp = getXmlDecimal(reader81, "СумНДС") ?: 0
                    }
                }
                if (reader81.endElement) {
                    elements[reader81.name.localPart] = false
                }
                reader81.next()
            }
        } finally {
            reader81.close()
        }
    }

    if (reader9 != null) {
        //elements = [:]
        try {
            while (reader9.hasNext()) {
                if (reader9.startElement) {
                    //elements[reader9.name.localPart] = true
                    if ("КодВидОпер".equals(reader9.name.localPart)) {
                        if ('06'.equals(reader9.getElementText())) {
                            r9str200sum += r9str200sumTmp
                            r9str210sum += r9str210sumTmp
                        }
                    }
                    if (!r9str260 && "КнигаПрод".equals(reader9.name.localPart)) {
                        r9str260 = getXmlDecimal(reader9, "СумНДСВсКПр18") ?: 0
                        r9str270 = getXmlDecimal(reader9, "СумНДСВсКПр10") ?: 0
                        r9str250 = getXmlDecimal(reader9, "СтПродБезНДС0") ?: 0
                    }
                    if ("КнПродСтр".equals(reader9.name.localPart)) {
                        r9str200sumTmp = getXmlDecimal(reader9, "СумНДССФ18") ?: 0
                        r9str210sumTmp = getXmlDecimal(reader9, "СумНДССФ10") ?: 0
                        r9str190sum += getXmlDecimal(reader9, "СтоимПродСФ0") ?: 0
                    }
                }
                /*if (reader9.endElement) {
                    elements[reader9.name.localPart] = false
                }*/
                reader9.next()
            }
        } finally {
            reader9.close()
        }
    }

    if (reader91 != null) {
        elements = [:]
        try {
            while (reader91.hasNext()) {
                if (reader91.startElement) {
                    elements[reader91.name.localPart] = true
                    if ("КодВидОпер".equals(reader91.name.localPart)) {
                        if ('06'.equals(reader91.getElementText())) {
                            r91str280sum += r91str280sumTmp
                            r91str290sum += r91str290sumTmp
                        }
                    }
                    if (!r91str340 && isCurrentNode(['Документ', 'КнигаПродДЛ'], elements)) {
                        r91str340 = getXmlDecimal(reader91, "СумНДСВсП1Р9_18") ?: 0
                        r91str350 = getXmlDecimal(reader91, "СумНДСВсП1Р9_10") ?: 0
                        r91str050 = getXmlDecimal(reader91, "СумНДСИтКПр18") ?: 0
                        r91str060 = getXmlDecimal(reader91, "СумНДСИтКПр10") ?: 0
                        r91str020 = getXmlDecimal(reader91, "ИтСтПродКПр18") ?: 0
                        r91str310 = getXmlDecimal(reader91, "СтПродВсП1Р9_18") ?: 0
                        r91str320 = getXmlDecimal(reader91, "СтПродВсП1Р9_10") ?: 0
                        r91str330 = getXmlDecimal(reader91, "СтПродВсП1Р9_0") ?: 0
                    }
                    if (isCurrentNode(['Документ', 'КнигаПродДЛ', 'КнПродДЛСтр'], elements)) {
                        r91str280sumTmp = getXmlDecimal(reader91, "СумНДССФ18") ?: 0
                        r91str290sumTmp = getXmlDecimal(reader91, "СумНДССФ10") ?: 0
                        r91str250sum += getXmlDecimal(reader91, "СтоимПродСФ18") ?: 0
                        r91str260sum += getXmlDecimal(reader91, "СтоимПродСФ10") ?: 0
                        r91str270sum += getXmlDecimal(reader91, "СтоимПродСФ0") ?: 0
                        r91str280sumAll += r91str280sumTmp
                        r91str290sumAll += r91str290sumTmp
                    }
                }
                if (reader91.endElement) {
                    elements[reader91.name.localPart] = false
                }
                reader91.next()
            }
        } finally {
            reader91.close()
        }
    }

    // 1. Не создан ни один из экземпляров декларации по НДС (раздел 8), (раздел 8 без консолид. формы) текущего периода и подразделения
    // ИЛИ
    // Создан только один из экземпляров декларации по НДС (раздел 8), (раздел 8 без консолид. формы) текущего периода и подразделения.
    if (has8 && has8n) {
        logger.error("Созданы два экземпляра декларации раздела 8 (раздел 8 и раздел 8 без консолид. формы) текущего периода и подразделения! Один из экземпляров декларации раздела 8 необходимо удалить!")
    }

    // 1. Не создан ни один из экземпляров декларации по НДС (раздел 9), (раздел 9 без консолид. формы) текущего периода и подразделения
    // ИЛИ
    // Создан только один из экземпляров декларации по НДС (раздел 9), (раздел 9 без консолид. формы) текущего периода и подразделения.
    if (has9 && has9n) {
        logger.error("Созданы два экземпляра декларации раздела 9 (раздел 9 и раздел 9 без консолид. формы) текущего периода и подразделения! Один из экземпляров декларации раздела 9 необходимо удалить!")
    }

    // 2. Существующие экземпляры декларации по НДС (раздел 8/раздел 8 без консолид. формы/8.1/9/раздел 9 без консолид. формы/9.1/10/11) текущего периода и подразделения находятся в состоянии «Принята»
    def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    declarations().each { declaration ->
        def declarationData = declarationService.getLast(declaration.value[0], declarationData.departmentId, reportPeriod.id)
        if (declarationData != null && !declarationData.accepted) {
            logger.error("Экземпляр декларации вида «${declaration.value[1]}» текущего периода и подразделения не находится в состоянии «Принята»!")
        }
    }

    // 3. Атрибуты признаки наличия разделов 8-11 (в том числе раздел 8 и 9 без консолид. формы) заполнены согласно алгоритмам
    if (hasOneOrMoreDeclaration() == 1) {
        def checkMap = [
                'Признак наличия разделов с 8 по 12'
                : [getXmlValue(exist8_12) as BigDecimal, hasOneOrMoreDeclaration()],
                'Признак наличия сведений из книги покупок об операциях, отражаемых за истекший налоговый период'
                : [getXmlValue(exist8) as BigDecimal, (has8 || has8n) ? 1 : 0],
                'Признак наличия сведений из дополнительного листа книги покупок'
                : [getXmlValue(exist81) as BigDecimal, isDeclarationExist(declaration81)],
                'Признак наличия сведений из книги продаж об операциях, отражаемых за истекший налоговый период'
                : [getXmlValue(exist9) as BigDecimal, (has9 || has9n) ? 1 : 0],
                'Признак наличия сведений из дополнительного листа книги продаж'
                : [getXmlValue(exist91) as BigDecimal, isDeclarationExist(declaration91)],
                'Признак наличия сведений из журнала учета выставленных счетов-фактур в отношении операций, осуществляемых в интересах другого лица на основе договоров комиссии, агентских договоров или на основе договоров транспортной экспедиции, отражаемых за истекший налоговый период'
                : [getXmlValue(exist10) as BigDecimal, isDeclarationExist(declarations().declaration10[0])],
                'Признак наличия сведений из журнала учета полученных счетов-фактур в отношении операций, осуществляемых в интересах другого лица на основе договоров комиссии, агентских договоров или на основе договоров транспортной экспедиции, отражаемых за истекший налоговый период'
                : [getXmlValue(exist11) as BigDecimal, isDeclarationExist(declarations().declaration11[0])],
                'Признак наличия сведений из счетов-фактур, выставленных лицами, указанными в пункте 5 статьи 173 Налогового кодекса Российской Федерации'
                : [getXmlValue(exist12) as BigDecimal, 0]
        ]
        checkMap.each { key, value ->
            if (value[0] != value[1]) {
                logger.error("Атрибут «$key» файла формата xml заполнен неверно! Для исправления необходимо пересчитать данные декларации (кнопка «Рассчитать»).")
            }
        }
    }

    // Проверки контрольных соотношений
    // 1.4
    def sum010_040 = r3g3str010 ?: 0 + r3g3str020 ?: 0 + r3g3str030 ?: 0 + r3g3str040 ?: 0
    def sum120_160 = r3g3str120 ?: 0 + r3g3str150 ?: 0 + r3g3str160 ?: 0
    def denom1 = sum010_040 + r7g2sum
    def denom2 = sum120_160 + r7g4sum
    //logger.info("(" + r3g3str010 + "+" + r3g3str020 + "+" + r3g3str030  + "+" + r3g3str040 + ")/(" + sum010_040 + "+" + r7g2sum + ") == (" + r3g3str120 + "+" +r3g3str150 + "+" + r3g3str160  + ")/(" + sum120_160+ "+" + r7g4sum  + ")")
    if (denom1 == 0 || denom2 == 0) {
        logger.warn("КС 1.4. Выполнение проверки невозможно, так как в результате расчета получен нулевой знаменатель (деление на ноль невозможно). " +
                "Алгоритм проверки: Раздел 3, гр. 3: («Строка 010» + «Строка 020» + «Строка 030» + «Строка 040») / Раздел 3, гр. 3: («Строка 010» + «Строка 020» + «Строка 030» + «Строка 040») + (Раздел 7: «Сумма по гр. 2»)) = (Раздел 3, гр. 3: «Строка 120» + «Строка 150» + «Строка 160») / ((Раздел 3, гр. 3: «Строка 120» + «Строка 150» + «Строка 160») + (Раздел 7: «Сумма по гр. 4»))")
    } else if (sum010_040 / denom1 != sum120_160 / denom2) {
        logger.warn("КС 1.4. Возможно нарушение ст. 149, 170 п.4 возможно необоснованное применение налоговых вычетов.")
    }
    // 1.11
    //logger.info("" + r3g3str170 ?: 0 + " > " + r3g5str010 ?: 0 + " + " + r3g5str020 ?: 0 + " + " + r3g5str030 ?: 0 + " + " + r3g5str040 ?: 0)
    if (r3g3str170 ?: 0 > r3g5str010 ?: 0 + r3g5str020 ?: 0 + r3g5str030 ?: 0 + r3g5str040 ?: 0) {
        logger.warn("КС 1.11. Возможно нарушение ст. 171, п. 8, НК РФ ст. 172, п. 6, либо НК РФ ст. 146, п. 1 налоговые " +
                "вычеты не обоснованы, либо налоговая база занижена, так как суммы отработанных авансов не включены в реализацию.")
    }
    // 1.25 (8, 8.1, 9, 9.1)
    if (checkReader('1.25', [(declaration8): reader8, (declaration81): reader81, (declaration9): reader9, (declaration91): reader91])) {
        def sum25 = r8str190 + (r81str190 - r81str005) - (r9str260 + r9str270) - (r9str200sum + r9str210sum) + (r91str340 + r91str350 - r91str050 - r91str060) - (r91str280sum + r91str290sum)
        if (r1str050 > 0 && sum25 <= 0) {
            logger.warn("КС 1.25. Возможно нарушение ст. 173 завышение суммы НДС, подлежащей возмещению за онп.")
        }
    }
    // 1.26 (9, 9.1)
    if (checkReader('1.26', [(declaration9): reader9, (declaration91): reader91])) {
        if (r2str060sum < r9str200sum + r9str210sum + r91str280sum + r91str290sum) {
            logger.warn("КС 1.26. Возможно нарушение ст. 161, п. 4 ст. 173 занижение суммы НДС, подлежащей уплате в бюджет.")
        }
    }
    // 1.27 (9, 9.1)
    if (checkReader('1.27', [(declaration9): reader9, (declaration91): reader91])) {
        if (r3g5str110 + r2str060sum < r9str260 + r9str270 + r91str340 + r91str350 - r91str050 - r91str060) {
            logger.warn("КС 1.27. Возможно нарушение РФ ст. 153, 161, 164, 165, 166, 167, 173 занижение суммы НДС, исчисленного к уплате в бюджет.")
        }
    }
    // 1.28 (8, 8.1)
    if (checkReader('1.28', [(declaration8): reader8, (declaration81): reader81])) {
        if (r3g3str190 > r8str190 + r81str190 - r81str005) {
            logger.warn("КС 1.28. Возможно нарушение ст. 171, 172 завышение суммы НДС, подлежащей вычету.")
        }
    }
    // 1.31 (8, 8.1)
    if (checkReader('1.31', [(declaration8): reader8, (declaration81): reader81])) {
        if (r3g3str180 > r8str180sum + r81str180sum) {
            logger.warn("КС 1.31. Возможно нарушение ст. 161, 171, 172 завышение суммы НДС, подлежащей вычету.")
        }
    }
    // 1.33 (8.1)
    if (checkReader('1.33', [(declaration81): reader81])) {
        if (r81str005 + r81str180sum < r81str190) {
            logger.warn("КС 1.33. Возможно нарушение ст. 171, 172 возможно завышение суммы НДС, подлежащей вычету.")
        }
    }
    // 1.36 (9)
    if (checkReader('1.36', [(declaration9): reader9])) {
        //logger.info(""+r9str190sum + " >= " +r9str250)
        if (r9str250 > r9str190sum) {
            logger.warn("КС 1.36. Возможно нарушение ст. 164, 165, 167, 173 возможно занижение исчисленной суммы НДС вследствие " +
                    "неполного отражения НБ либо неверное применение ставки по НДС (при условии, что соотношение 1.37 выполняется).")
        }
    }
    // 1.39 (9.1)
    if (checkReader('1.39', [(declaration91): reader91])) {
        if (r91str020 + r91str250sum > r91str310) {
            logger.warn("КС 1.39. Возможно нарушение ст. 153, 173, п. 3 Раздела IV Приложения 5 к Постановлению N 1137 возможно " +
                    "занижение суммы НДС, исчисленного к уплате в бюджет (при условии, что соотношение 1.32 и 1.49 выполняются)")
        }
    }
    // 1.40 (9.1)
    if (checkReader('1.40', [(declaration91): reader91])) {
        if (r91str020 + r91str260sum > r91str320) {
            logger.warn("КС 1.40. Возможно нарушение ст. 153, 173, п. 3 Раздела IV Приложения 5 к Постановлению N 1137 " +
                    "возможно занижение суммы НДС, исчисленного к уплате в бюджет")
        }
    }
    // 1.41  (9.1)
    if (checkReader('1.41', [(declaration91): reader91])) {
        if (r91str020 + r91str270sum > r91str330) {
            logger.warn("КС 1.41. Возможно нарушение ст. 164, 165, 167, 173 возможно занижение исчисленной суммы НДС, " +
                    "вследствие неполного отражения НБ либо неверное применение ставки по НДС")
        }
    }
    // 1.42 (9.1)
    if (checkReader('1.42', [(declaration91): reader91])) {
        if (r91str020 + r91str280sumAll > r91str340) {
            logger.warn("КС 1.42. Возможно нарушение ст. 153, 173, п. 3 Раздела IV Приложения 5 к Постановлению N 1137 " +
                    "возможно занижение суммы НДС, исчисленного к уплате в бюджет (при условии, что соотношение 1.32 выполняется)")
        }
    }
    // 1.43 (9.1)
    if (checkReader('1.43', [(declaration91): reader91])) {
        if (r91str020 + r91str290sumAll > r91str350) {
            logger.warn("КС 1.43. Возможно нарушение ст. 153, 173, п. 3 Раздела IV Приложения 5 к Постановлению N 1137 " +
                    "возможно занижение суммы НДС, исчисленного к уплате в бюджет")
        }
    }
}

def getXmlValue(def value) {
    if (!value) {
        return null
    }

    return new BigDecimal(value)
}

// Заполнение мапы с данными о декларациях 8-11
def Map<Long, Expando> getParts() {
    if (declarationParts == null) {
        declarationParts = new HashedMap<Long, Expando>()
        def reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
        declarations().each { declaration ->
            id = declaration.value[0]
            name = declaration.value[1]
            def declarationData = declarationService.getLast(id, declarationData.departmentId, reportPeriod.id)

            def result = new Expando()
            result.id = (declarationData?.id)
            result.name = declaration.value[1]
            result.exist = (declarationData != null)
            result.accepted = (declarationData?.accepted)

            if (result.exist) {
                result.fileName = declarationService.getXmlDataFileName(declarationData.id)
            }
            declarationParts[id] = result
        }
    }
    return declarationParts
}

def String getDeclarationFileName(def declarationTypeId) {
    return getParts().get(declarationTypeId)?.fileName ?: empty
}

def BigDecimal isDeclarationExist(def declarationTypeId) {
    return getParts().get(declarationTypeId)?.exist ? 1 : 0
}

def boolean checkReader(String number, Map<Integer, XMLStreamReader> map) {
    boolean exist = true
    map.each { id, reader ->
        if (!isDeclarationExist(id)) {
            logger.warn("%s. Экземпляр декларации вида «%s» не создан. Проверка контрольного соотношения невозможна.", number, getParts().get(id).name)
            exist = false
        } else if (!reader) {
            logger.warn("%s. Экземпляр декларации вида «%s» создан, но не рассчитан. Проверка контрольного соотношения невозможна.", number, getParts().get(id).name)
            exist = false
        }
    }
    return exist
}

def BigDecimal hasOneOrMoreDeclaration() {
    def BigDecimal hasDeclaration = 0
    declarations().each { declaration ->
        if (isDeclarationExist(declaration.value[0]) == 1) {
            hasDeclaration = 1
        }
    }
    return hasDeclaration
}

def getSection2Rows(def dataRowsMap) {
    def rows = []
    // форма 724.6
    for (def row : dataRowsMap[604]) {
        if (row.getAlias() != null) {
            continue
        }
        def newRow = [:]
        newRow.naimProd = row.contragent
        newRow.innULProd = null
        newRow.sumIschisl = round(row.sum2)
        newRow.codeOper = '1011712'
        rows.add(newRow)
    }
    // форма 724.7
    for (def row : dataRowsMap[605]) {
        if (row.getAlias() != null) {
            continue
        }
        def newRow = [:]
        newRow.naimProd = row.name
        newRow.innULProd = row.inn
        newRow.sumIschisl = round(row.ndsSum)
        newRow.codeOper = '1011703'
        rows.add(newRow)
    }
    return rows
}

def getSection2Agg(def dataRowsMap) {
    def sumIschisl = empty
    // форма 724.6
    for (def row : dataRowsMap[604]) {
        if (row.getAlias() != null) {
            continue
        }
        sumIschisl += row.sum2
    }
    // форма 724.7
    for (def row : dataRowsMap[605]) {
        if (row.getAlias() != null) {
            continue
        }
        sumIschisl += row.ndsSum
    }
    return round(sumIschisl)
}

def round(def value) {
    return ((BigDecimal) value)?.setScale(0, BigDecimal.ROUND_HALF_UP)
}

@Field
def specialCode = '1010276'

@Field
def opuCodes = ['26411.01']

/**
 * Получить значение для НалНеВыч.
 *
 * @param code строки формы 724.2.1
 */
def getNalNeVich(def code) {
    def order = reportPeriodService.get(declarationData.reportPeriodId)?.order
    if (code == specialCode) {
        // сумма кодов ОПУ из отчета 102
        def sumOpu = getSumByOpuCodes(opuCodes, getEndDate())
        if (order == 1) {
            return sumOpu
        } else {
            def sumOpuPrev = getSumByOpuCodes(opuCodes, getPrevEndDate())
            // разность сумм
            return sumOpu - sumOpuPrev
        }
    } else {
        return empty
    }
}

/**
 * Посчитать сумму по кодам ОПУ.
 */
def getSumByOpuCodes(def opuCodes, def date) {
    def tmp = BigDecimal.ZERO
    // берутся данные за текущий период
    for (def income102Row : getIncome102Data(date)) {
        if (income102Row?.OPU_CODE?.value in opuCodes) {
            tmp += (income102Row?.TOTAL_SUM?.value ?: 0)
        }
    }
    return tmp
}

// Получение данных из справочника «Отчет о прибылях и убытках» для текужего подразделения и отчетного периода
def getIncome102Data(def date) {
    if (!income102DataCache.containsKey(date)) {
        def records = bookerStatementService.getRecords(52L, declarationData.departmentId, date, null)
        income102DataCache.put(date, records)
    }
    return income102DataCache.get(date)
}

def getOkato(def id) {
    def String okato = null
    if (id != null) {
        okato = getRefBookValue(96, id)?.CODE?.stringValue
    }
    return okato
}

def generateXmlFileId(String taxOrganCodeProm, String taxOrganCode) {
    def departmentParam = getDepartmentParam()
    if (departmentParam) {
        def date = Calendar.getInstance().getTime()?.format("yyyyMMdd")
        def fileId = TaxType.VAT.declarationPrefix + '_' +
                taxOrganCodeProm + '_' +
                taxOrganCode + '_' +
                departmentParam.INN?.value +
                departmentParam.KPP?.value + "_" +
                date + "_" +
                UUID.randomUUID().toString().toUpperCase()
        return fileId
    }
    return null
}