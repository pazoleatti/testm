package form_template.vat.declaration_audit.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import groovy.transform.Field
import groovy.xml.MarkupBuilder
import org.apache.commons.collections.map.HashedMap

import javax.xml.namespace.QName

/**
 * Декларация по НДС (аудит, раздел 1-7)
 *
 * совпадает с "Декларация по НДС (раздел 1-7)" и "Декларация по НДС (короткая, раздел 1-7)", кроме заполнения секции "РАЗДЕЛ 2"
 *
 * declarationTemplateId=2007
 */

@Field
def declarationType = 7;

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

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(declarationData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
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

// Мапа соответсвтия id и наименований деклараций 8-11
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
            throw new Exception("Ошибка при получении настроек обособленного подразделения")
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
        errorList.add("«Код налогового органа»")
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

List<String> getErrorVersion(record) {
    List<String> errorList = new ArrayList<String>()
    if (record.FORMAT_VERSION.stringValue == null || !record.FORMAT_VERSION.stringValue.equals('5.04')) {
        errorList.add("«Версия формата»")
    }
    errorList
}

def getDataRowSum(def dataRows, def String rowAlias, def String cellAlias, def useRound = true){
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

    def nameDecl8 =  getDeclarationFileName(has8 ? declarations().declaration8[0] : declarations().declaration8n[0])
    def nameDecl81 = getDeclarationFileName(declarations().declaration81[0])
    def nameDecl9 =  getDeclarationFileName(has9 ? declarations().declaration9[0] : declarations().declaration9n[0])
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
    def formDataList = declarationService.getAcceptedFormDataSources(declarationData).getRecords()
    // Тип формы > Строки формы
    def Map<Integer,List> dataRowsMap = [:]
    for (def formData : formDataList) {
        def dataRows = formDataService.getDataRowHelper(formData)?.getAll()
        if(dataRowsMap.containsKey(formData.formType.id)){
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
        def totalRow1baseSum = getDataRowSum(rows724_1, 'total_1', 'baseSum')
        def totalRow1ndsSum = getDataRowSum(rows724_1, 'total_1', 'ndsSum')
        def totalRow2baseSum = getDataRowSum(rows724_1, 'total_2', 'baseSum')
        def totalRow2ndsSum = getDataRowSum(rows724_1, 'total_2', 'ndsSum')
        def totalRow3baseSum = getDataRowSum(rows724_1, 'total_3', 'baseSum', false)
        def totalRow3ndsSum = getDataRowSum(rows724_1, 'total_3', 'ndsSum', false)
        def totalRow4baseSum = getDataRowSum(rows724_1, 'total_4', 'baseSum')
        def totalRow4ndsSum = getDataRowSum(rows724_1, 'total_4', 'ndsSum')
        def totalRow5baseSum = getDataRowSum(rows724_1, 'total_5', 'baseSum')
        def totalRow5ndsSum = getDataRowSum(rows724_1, 'total_5', 'ndsSum')
        def totalRow6baseSum = getDataRowSum(rows724_1, 'total_6', 'baseSum', false)
        def totalRow6ndsSum  = getDataRowSum(rows724_1, 'total_6', 'ndsSum', false)
        def totalRow7baseSum = getDataRowSum(rows724_1, 'total_7', 'baseSum')
        def totalRow7ndsDealSum = getDataRowSum(rows724_1, 'total_7', 'ndsDealSum')
        def totalRow7ndsBookSum = getDataRowSum(rows724_1, 'total_7', 'ndsBookSum')

        nalBaza010 = totalRow1baseSum + totalRow7baseSum
        sumNal010 = totalRow1ndsSum + totalRow7ndsBookSum

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
    def nalPU164 = (nalVosstObsh - nalVichObsh).abs().intValue()

    def builder = new MarkupBuilder(xml)
    builder.Файл(
            [ИдФайл: declarationService.generateXmlFileId(4, declarationData.departmentReportPeriodId, declarationData.taxOrganCode, declarationData.kpp)] +
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
                                    СтРеалТов: getDataRowSum(dataRowsMap[601], it,'realizeCost'),
                                    СтПриобТов: getDataRowSum(dataRowsMap[601], it,'obtainCost'),
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
    def exist8_12
    def exist8
    def exist81
    def exist9
    def exist91
    def exist10
    def exist11
    def exist12

    def reader = declarationService.getXmlStreamReader(declarationData.id)
    if (reader == null) {
        return
    }
    try {
        while (reader.hasNext()) {
            if (reader.startElement && QName.valueOf('Файл').equals(reader.name)) {
                // Атрибуты записи
                exist8_12 = reader.getAttributeValue(null, "ПризнНал8-12")
                exist8 = reader.getAttributeValue(null, "ПризнНал8")
                exist81 = reader.getAttributeValue(null, "ПризнНал81")
                exist9 = reader.getAttributeValue(null, "ПризнНал9")
                exist91 = reader.getAttributeValue(null, "ПризнНал91")
                exist10 = reader.getAttributeValue(null, "ПризнНал10")
                exist11 = reader.getAttributeValue(null, "ПризнНал11")
                exist12 = reader.getAttributeValue(null, "ПризнНал12")
                break
            }
            reader.next()
        }
    } finally {
        reader.close()
    }

    // 1. Не создан ни один из экземпляров декларации по НДС (раздел 8), (раздел 8 без консолид. формы) текущего периода и подразделения
    // ИЛИ
    // Создан только один из экземпляров декларации по НДС (раздел 8), (раздел 8 без консолид. формы) текущего периода и подразделения.
    def has8 = (isDeclarationExist(declarations().declaration8[0]) == 1)
    def has8n = (isDeclarationExist(declarations().declaration8n[0]) == 1)
    if(has8 && has8n){
        logger.error("Созданы два экземпляра декларации раздела 8 (раздел 8 и раздел 8 без консолид. формы) текущего периода и подразделения! Один из экземпляров декларации раздела 8 необходимо удалить!")
    }

    // 1. Не создан ни один из экземпляров декларации по НДС (раздел 9), (раздел 9 без консолид. формы) текущего периода и подразделения
    // ИЛИ
    // Создан только один из экземпляров декларации по НДС (раздел 9), (раздел 9 без консолид. формы) текущего периода и подразделения.
    def has9 = (isDeclarationExist(declarations().declaration9[0]) == 1)
    def has9n = (isDeclarationExist(declarations().declaration9n[0]) == 1)
    if(has9 && has9n){
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
                : [getXmlValue(exist81) as BigDecimal, isDeclarationExist(declarations().declaration81[0])],
                'Признак наличия сведений из книги продаж об операциях, отражаемых за истекший налоговый период'
                : [getXmlValue(exist9) as BigDecimal, (has9 || has9n) ? 1 : 0],
                'Признак наличия сведений из дополнительного листа книги продаж'
                : [getXmlValue(exist91) as BigDecimal, isDeclarationExist(declarations().declaration91[0])],
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
            result.id = id
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

def String getDeclarationFileName(def declarationId) {
    return getParts().get(declarationId)?.fileName ?: empty
}

def BigDecimal isDeclarationExist(def declarationId) {
    return getParts().get(declarationId)?.exist ? 1 : 0
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

@Field
def knuCodes = ['20860', '20870']

/**
 * Получить значение для НалНеВыч.
 *
 * @param code строки формы 724.2.1
 */
def getNalNeVich(def code) {
    def order = reportPeriodService.get(declarationData.reportPeriodId)?.order
    if (code == specialCode) {
        // сумма кодов ОПУ из отчета 102
        def sumOpu = getSumByOpuCodes(opuCodes)
        if (order == 1) {
            return sumOpu
        } else {
            // сумма из расходов простых
            def sumOutcome = getSumOutcomeSimple(knuCodes)
            // разность сумм
            return sumOpu - sumOutcome
        }
    } else {
        return empty
    }
}

/**
 * Посчитать сумму по кодам ОПУ.
 */
def getSumByOpuCodes(def opuCodes) {
    def tmp = BigDecimal.ZERO
    // берутся данные за текущий период
    for (def income102Row : getIncome102Data(getEndDate())) {
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

def getSumOutcomeSimple(def knuCodes) {
    def tmp = 0
    // получаем период из прибыли соотвествующий текущему периоду НДС
    def List<ReportPeriod> periodList = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, getEndDate(), getEndDate())
    if (periodList.isEmpty()) {
        return 0
    }
    // получаем предыдущий период по прибыли
    def reportPeriodPrevIncome = reportPeriodService.getPrevReportPeriod(periodList.get(0).id)
    if (reportPeriodPrevIncome?.id == null) {
        return 0
    }
    def formDataSimple = getFormDataSimple(reportPeriodPrevIncome.id)
    def dataRowsSimple = (formDataSimple ? formDataService.getDataRowHelper(formDataSimple)?.getAll() : null)
    for (def row : dataRowsSimple) {
        if (row.consumptionTypeId in knuCodes) {
            tmp += row.rnu5Field5Accepted
        }
    }
    return tmp
}

/**
 * Получить данные формы "расходы простые" (id = 304)
 */
def getFormDataSimple(def reportPeriodId) {
    return formDataService.getLast(304, FormDataKind.SUMMARY, declarationData.departmentId, reportPeriodId, null)
}

def getOkato(def id) {
    def String okato = null
    if (id != null) {
        okato = getRefBookValue(96, id)?.CODE?.stringValue
    }
    return okato
}