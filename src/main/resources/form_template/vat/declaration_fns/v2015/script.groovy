package form_template.vat.declaration_fns.v2015

import com.aplana.sbrf.taxaccounting.model.*
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter
import groovy.transform.Field
import groovy.xml.MarkupBuilder
import org.apache.commons.collections.map.HashedMap
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod

import javax.xml.stream.XMLStreamReader

/**
 * Декларация по НДС (раздел 1-7)
 *
 * совпадает с "Декларация по НДС (аудит, раздел 1-7)" и "Декларация по НДС (короткая, раздел 1-7)", кроме заполнения секции "РАЗДЕЛ 2"
 *
 * declarationTemplateId=2004
 */

@Field
def declarationType = 4

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkDepartmentParams(LogLevel.WARNING)
        break
    case FormDataEvent.CHECK:
        checkDepartmentParams(LogLevel.ERROR)
        logicCheck1(LogLevel.WARNING)
        logicCheck2()
        break
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:
        checkDepartmentParams(LogLevel.WARNING)
        logicCheck1(LogLevel.ERROR)
        logicCheck2()
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
def refBookMap = [:]

@Field
def version = '5.04'

@Field
def empty = 0

// Параметры подразделения
@Field
def departmentParam = null

@Field
def bankDepartmentId = 1

// id формы источника 724.1.1 (не требующего настройки пользователем, подтягивается скриптом)
@Field
def formType_724_1_1 = 848

// id формы источника 724.10 (не требующего настройки пользователем, подтягивается скриптом)
@Field
def formType_724_10 = 623

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
        prevReportPeriodEndDate = (reportPeriodService.getStartDate(declarationData.reportPeriodId)?.time - 1)
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
    [       // key : declarationTypeId, название раздела декларации, номер раздела, флаг проверки наличия принятой декларации
            declaration8  : [12, 'Декларация по НДС (раздел 8)', '8', true],
            declaration8n : [18, 'Декларация по НДС (раздел 8 без консолид. формы)', '8', true],
            declaration8q : [28, 'Декларация по НДС (раздел 8 с 3 квартала 2016)', '8', true],
            declaration81 : [13, 'Декларация по НДС (раздел 8.1)', '8.1', false],
            declaration81q: [29, 'Декларация по НДС (раздел 8.1 с 3 квартала 2016)', '8.1', false],
            declaration9  : [14, 'Декларация по НДС (раздел 9)', '9', true],
            declaration9n : [21, 'Декларация по НДС (раздел 9 без консолид. формы)', '9', true],
            declaration9q : [24, 'Декларация по НДС (раздел 9 с 3 квартала 2016)', '9', true],
            declaration91 : [15, 'Декларация по НДС (раздел 9.1)', '9.1', false],
            declaration91q: [27, 'Декларация по НДС (раздел 9.1 с 3 квартала 2016)', '9.1', false],
            declaration10 : [16, 'Декларация по НДС (раздел 10)', '10', true],
            declaration10q: [26, 'Декларация по НДС (раздел 10 с 3 квартала 2016)', '10', true],
            declaration11 : [17, 'Декларация по НДС (раздел 11)', '11', true],
            declaration11q: [25, 'Декларация по НДС (раздел 11 с 3 квартала 2016)', '11', true]
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
    def refBook = getRefBook(RefBook.DEPARTMENT_CONFIG_VAT)

    // Проверки подразделения
    def List<String> errorList = getErrorDepartment(departmentParam, refBook)
    for (String error : errorList) {
        logger.log(logLevel, "На форме настроек подразделения текущего экземпляра декларации отсутствует значение атрибута «%s»!", error)
    }
    // вынесено отдельно, потому что для этой проверки сообщение немного отличается
    if ((departmentParam.SIGNATORY_ID?.referenceValue != null && getRefBookValue(35, departmentParam.SIGNATORY_ID?.value)?.CODE?.value == 2)
            && (departmentParam.APPROVE_DOC_NAME?.stringValue == null || departmentParam.APPROVE_DOC_NAME.stringValue.isEmpty())) {
        def message = "На форме настроек подразделения текущего экземпляра декларации отсутствует значение атрибута «%s» (Признак лица, подписавшего документ = 2)!"
        def attributeName = refBook.getAttribute('NAME').name
        logger.log(logLevel, message, attributeName)
    }
    def tmpVersion = departmentParam.FORMAT_VERSION?.stringValue
    if (!version.equals(tmpVersion)) {
        def message = "На форме настроек подразделения текущего экземпляра декларации неверно указано значение атрибута «%s» (%s)! Ожидаемое значение «%s»."
        def attributeName = refBook.getAttribute('FORMAT_VERSION').name
        def value = (tmpVersion != null && '' != tmpVersion ? tmpVersion : 'пустое значение')
        logger.log(logLevel, message, attributeName, value, version)
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

List<String> getErrorDepartment(def record, def refBook) {
    List<String> errorList = new ArrayList<String>()
    String attributeName
    if (record.NAME?.stringValue == null || record.NAME.stringValue.isEmpty()) {
        attributeName = refBook.getAttribute('NAME').name
        errorList.add(attributeName)
    }
    if (record.OKTMO?.referenceValue == null) {
        attributeName = refBook.getAttribute('OKTMO').name
        errorList.add(attributeName)
    }
    if (record.INN?.stringValue == null || record.INN.stringValue.isEmpty()) {
        attributeName = refBook.getAttribute('INN').name
        errorList.add(attributeName)
    }
    if (record.KPP?.stringValue == null || record.KPP.stringValue.isEmpty()) {
        attributeName = refBook.getAttribute('KPP').name
        errorList.add(attributeName)
    }
    if (record.TAX_ORGAN_CODE?.stringValue == null || record.TAX_ORGAN_CODE.stringValue.isEmpty()) {
        attributeName = refBook.getAttribute('TAX_ORGAN_CODE').name
        errorList.add(attributeName)
    }
    if (useTaxOrganCodeProm() && (record.TAX_ORGAN_CODE_PROM?.value == null || record.TAX_ORGAN_CODE_PROM.value.isEmpty())) {
        attributeName = refBook.getAttribute('TAX_ORGAN_CODE_PROM').name
        errorList.add(attributeName)
    }
    if (record.OKVED_CODE?.referenceValue == null) {
        attributeName = refBook.getAttribute('OKVED_CODE').name
        errorList.add(attributeName)
    }
    if (record.SIGNATORY_ID?.referenceValue == null) {
        attributeName = refBook.getAttribute('SIGNATORY_ID').name
        errorList.add(attributeName)
    }
    if (record.SIGNATORY_SURNAME?.stringValue == null || record.SIGNATORY_SURNAME.stringValue.isEmpty()) {
        attributeName = refBook.getAttribute('SIGNATORY_SURNAME').name
        errorList.add(attributeName)
    }
    if (record.SIGNATORY_FIRSTNAME?.stringValue == null || record.SIGNATORY_FIRSTNAME.stringValue.isEmpty()) {
        attributeName = refBook.getAttribute('SIGNATORY_FIRSTNAME').name
        errorList.add(attributeName)
    }
    if (record.TAX_PLACE_TYPE_CODE?.referenceValue == null) {
        attributeName = refBook.getAttribute('TAX_PLACE_TYPE_CODE').name
        errorList.add(attributeName)
    }
    def reorgFormCode = getRefBookValue(5, record?.REORG_FORM_CODE?.value)?.CODE?.value
    if (reorgFormCode != null && reorgFormCode != '0') {
        if (record.REORG_INN?.value == null || record.REORG_INN.value.isEmpty()) {
            attributeName = refBook.getAttribute('REORG_INN').name
            errorList.add(attributeName)
        }
        if (record.REORG_KPP?.value == null || record.REORG_KPP.value.isEmpty()) {
            attributeName = refBook.getAttribute('REORG_KPP').name
            errorList.add(attributeName)
        }
    }
    errorList
}

@Field
def declarationReportPeriod

boolean useTaxOrganCodeProm() {
    if (declarationReportPeriod == null) {
        declarationReportPeriod = getReportPeriod()
    }
    return (declarationReportPeriod?.taxPeriod?.year > 2015 || declarationReportPeriod?.order > 2)
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

    def declarationsMap = declarations()
    def parts = getParts(declarationsMap)
    def has8 = (isDeclarationExist(parts, declarationsMap.declaration8[0]) == 1)
    def has8n = (isDeclarationExist(parts, declarationsMap.declaration8n[0]) == 1)
    def has8q = (isDeclarationExist(parts, declarationsMap.declaration8q[0]) == 1)
    def has81 = (isDeclarationExist(parts, declarationsMap.declaration81[0]) == 1)
    def has81q = (isDeclarationExist(parts, declarationsMap.declaration81q[0]) == 1)

    def has9 = (isDeclarationExist(parts, declarationsMap.declaration9[0]) == 1)
    def has9n = (isDeclarationExist(parts, declarationsMap.declaration9n[0]) == 1)
    def has9q = (isDeclarationExist(parts, declarationsMap.declaration9q[0]) == 1)
    def has91 = (isDeclarationExist(parts, declarationsMap.declaration91[0]) == 1)
    def has91q = (isDeclarationExist(parts, declarationsMap.declaration91q[0]) == 1)

    def has10 = (isDeclarationExist(parts, declarationsMap.declaration10[0]) == 1)
    def has10q = (isDeclarationExist(parts, declarationsMap.declaration10q[0]) == 1)
    def has11 = (isDeclarationExist(parts, declarationsMap.declaration11[0]) == 1)
    def has11q = (isDeclarationExist(parts, declarationsMap.declaration11q[0]) == 1)

    def sign812 = hasOneOrMoreDeclaration(parts, declarationsMap)
    def has812 = (sign812 == 1)
    def sign8 = (has8 || has8n || has8q) ? 1 : 0
    def sign81 = (has81 || has81q) ? 1 : 0
    def sign9 = (has9 || has9n || has9q) ? 1 : 0
    def sign91 = (has91 || has91q) ? 1 : 0
    def sign10 = (has10 || has10q) ? 1 : 0
    def sign11 = (has11 || has11q) ? 1 : 0
    def sign12 = 0

    def nameDecl8 = getDeclarationFileName(parts, has8 ? declarationsMap.declaration8[0] : (has8n ? declarationsMap.declaration8n[0] : declarationsMap.declaration8q[0]))
    def nameDecl81 = getDeclarationFileName(parts, has81 ? declarationsMap.declaration81[0] : declarationsMap.declaration81q[0])
    def nameDecl9 = getDeclarationFileName(parts, has9 ? declarationsMap.declaration9[0] : (has9n ? declarationsMap.declaration9n[0] : declarationsMap.declaration9q[0]))
    def nameDecl91 = getDeclarationFileName(parts, has91 ? declarationsMap.declaration91[0] : declarationsMap.declaration91q[0])
    def nameDecl10 = getDeclarationFileName(parts, has10 ? declarationsMap.declaration10[0] : declarationsMap.declaration10q[0])
    def nameDecl11 = getDeclarationFileName(parts, has11 ? declarationsMap.declaration11[0] : declarationsMap.declaration11q[0])

    /** Отчётный период. */
    def reportPeriod = getReportPeriod()
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
    def section3Check = prevSection3Check()

    // TODO Вопрос к заказчику, пока не заполняем
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
    /** НалБаза (КорРеалТов18). Код строки 105 Графа 3. */
    def nalBaza105 = empty
    /** СумНал (КорРеалТов18). Код строки 105 Графа 5. */
    def sumNal105 = empty
    /** НалПредНППриоб . Код строки 120 Графа 3. */
    def nalPredNPPriob = empty
    /** НалИсчПрод. Код строки 170 Графа 3. */
    def nalIschProd = empty
    /** НалУплПокНА . Код строки 180 Графа 3. */
    def nalUplPokNA = empty

    if (section3Check) {
        def correction = getCorrectionNumber()
        // форма 724.1
        def rows724_1 = dataRowsMap[600]
        // форма 724.4
        def rows724_4 = dataRowsMap[603]
        if (rows724_4) {
            nalUplPokNA = getDataRowSum(rows724_4, 'total2', 'sum2')
        }
        if (correction == null || correction == 0 || correction == 1) {
            // первичная декларация (номер корректировки «0») или уточненная декларация (номер корректировки «1»)
            if (rows724_1) {
                def totalRow1baseSum = getDataRowSum(rows724_1, 'total_1', 'baseSum', false)
                def totalRow1ndsSum = getDataRowSum(rows724_1, 'total_1', 'ndsSum', false)
                def totalRow2baseSum = getDataRowSum(rows724_1, 'total_2', 'baseSum', false)
                def totalRow2ndsSum = getDataRowSum(rows724_1, 'total_2', 'ndsSum', false)
                def totalRow3baseSum = getDataRowSum(rows724_1, 'total_3', 'baseSum', false)
                def totalRow3ndsSum = getDataRowSum(rows724_1, 'total_3', 'ndsSum', false)
                def totalRow4baseSum = getDataRowSum(rows724_1, 'total_4', 'baseSum', false)
                def totalRow4ndsSum = getDataRowSum(rows724_1, 'total_4', 'ndsSum', false)
                def totalRow5baseSum = getDataRowSum(rows724_1, 'total_5', 'baseSum', false)
                def totalRow5ndsSum = getDataRowSum(rows724_1, 'total_5', 'ndsSum', false)
                def totalRow6baseSum = getDataRowSum(rows724_1, 'total_6', 'baseSum', false)
                def totalRow6ndsSum = getDataRowSum(rows724_1, 'total_6', 'ndsSum', false)
                def totalRow7baseSum = getDataRowSum(rows724_1, 'total_7', 'baseSum', false)
                def totalRow7ndsDealSum = getDataRowSum(rows724_1, 'total_7', 'ndsDealSum', false)
                def totalRow7ndsBookSum = getDataRowSum(rows724_1, 'total_7', 'ndsBookSum', false)

                nalBaza010 = totalRow1baseSum + totalRow7baseSum
                sumNal010 = totalRow1ndsSum + totalRow7ndsBookSum

                nalBaza020 = totalRow2baseSum
                sumNal020 = totalRow2ndsSum

                nalBaza030 = totalRow3baseSum + totalRow6baseSum
                sumNal030 = totalRow3ndsSum + totalRow6ndsSum

                nalBaza040 = totalRow4baseSum
                sumNal040 = totalRow4ndsSum

                nalBaza070 = totalRow5baseSum
                sumNal070 = totalRow5ndsSum

                nalIschProd = totalRow7ndsDealSum
            }
            if (rows724_4) {
                nalPredNPPriob = getDataRowSum(rows724_4, 'total1', 'sum2', false)
            }
        }
        if (correction == 1 || correction > 1) {
            // уточненная декларация (номер корректировки «1») или уточненная декларация (номер корректировки больше «1»)
            def formData724_1_1 = getFormData724_1_1()
            def rows724_1_1 = (formData724_1_1 ? formDataService.getDataRowHelper(formData724_1_1)?.getAll() : null)
            if (rows724_1_1) {
                def code = getRefBookValue(8, getReportPeriod()?.dictTaxPeriodId)?.CODE?.value

                def rowAlias = 'total_1_' + code
                def row = rows724_1_1.find { it.getAlias() == rowAlias }
                nalBaza010 = nalBaza010 + (row ? row.sumPlus : empty)
                sumNal010 = sumNal010 + (row ? row.sumNdsPlus : empty)

                rowAlias = 'total_2_' + code
                row = rows724_1_1.find { it.getAlias() == rowAlias }
                nalBaza020 = nalBaza020 + (row ? row.sumPlus : empty)
                sumNal020 = sumNal020 + (row ? row.sumNdsPlus : empty)

                def rowAliases = [ 'total_3_' + code, 'total_6_' + code ]
                def rows = rows724_1_1.findAll { it.getAlias() in rowAliases }
                nalBaza030 = nalBaza030 + (rows ? rows.sum { it.sumPlus ?: empty } : empty)
                sumNal030 = sumNal030 + (rows ? rows.sum { it.sumNdsPlus ?: empty } : empty)

                rowAlias = 'total_4_' + code
                row = rows724_1_1.find { it.getAlias() == rowAlias }
                nalBaza040 = nalBaza040 + (row ? row.sumPlus : empty)
                sumNal040 = sumNal040 + (row ? row.sumNdsPlus : empty)

                rowAlias = 'total_5_' + code
                row = rows724_1_1.find { it.getAlias() == rowAlias }
                nalBaza070 = nalBaza070 +(row ? row.sumPlus : empty)
                sumNal070 = sumNal070 + (row ? row.sumNdsPlus : empty)

                rowAlias = 'total_8_' + code
                row = rows724_1_1.find { it.getAlias() == rowAlias }
                nalIschProd = nalIschProd + (row ? row.sumNdsPlus : empty)

                rowAlias = 'total_9_' + code
                row = rows724_1_1.find { it.getAlias() == rowAlias }
                nalPredNPPriob = nalPredNPPriob + (row ? row.sumNdsPlus : empty)
            }

            def formData724_10 = getFormData(formType_724_10, FormDataKind.CONSOLIDATED, bankDepartmentId, getPeriod4Id())
            def rows724_10 = (formData724_10?.state == WorkflowState.ACCEPTED ? formDataService.getDataRowHelper(formData724_10)?.getAll() : null)
            if (rows724_10) {
                def order = getReportPeriod().order

                rowAlias = 'total' + order
                def row = rows724_10.find { it.getAlias() == rowAlias }
                nalBaza105 = nalBaza105 +(row ? row.sum : empty)
                sumNal105 = sumNal105 + (row ? row.ndsSum : empty)
            }
        }
        if (correction > 1) {
            // уточненная декларация (номер корректировки больше «1»)
            DeclarationData prevCorrectionDeclarationData = getPrevCorrectionDeclaration()
            // XMLStreamReader декларации за предыдущий корректирующий период
            XMLStreamReader reader = declarationService.getXmlStreamReader(prevCorrectionDeclarationData.id)
            try {
                def elements = [:]
                while (reader.hasNext()) {
                    if (reader.startElement) {
                        elements[reader.name.localPart] = true
                        if (isCurrentNode(['Документ', 'НДС', 'СумУпл164', 'СумНалОб', 'РеалТов18'], elements)) {
                            nalBaza010 = nalBaza010 + (getXmlDecimal(reader, "НалБаза") ?: 0)
                            sumNal010 = sumNal010 + (getXmlDecimal(reader, "СумНал") ?: 0)
                        }
                        if (isCurrentNode(['Документ', 'НДС', 'СумУпл164', 'СумНалОб', 'РеалТов10'], elements)) {
                            nalBaza020 = nalBaza020 + (getXmlDecimal(reader, "НалБаза") ?: 0)
                            sumNal020 = sumNal020 + (getXmlDecimal(reader, "СумНал") ?: 0)
                        }
                        if (isCurrentNode(['Документ', 'НДС', 'СумУпл164', 'СумНалОб', 'РеалТов118'], elements)) {
                            nalBaza030 = nalBaza030 + (getXmlDecimal(reader, "НалБаза") ?: 0)
                            sumNal030 = sumNal030 + (getXmlDecimal(reader, "СумНал") ?: 0)
                        }
                        if (isCurrentNode(['Документ', 'НДС', 'СумУпл164', 'СумНалОб', 'РеалТов110'], elements)) {
                            nalBaza040 = nalBaza040 + (getXmlDecimal(reader, "НалБаза") ?: 0)
                            sumNal040 = sumNal040 + (getXmlDecimal(reader, "СумНал") ?: 0)
                        }
                        if (isCurrentNode(['Документ', 'НДС', 'СумУпл164', 'СумНалОб', 'ОплПредПост'], elements)) {
                            nalBaza070 = nalBaza070 + (getXmlDecimal(reader, "НалБаза") ?: 0)
                            sumNal070 = sumNal070 + (getXmlDecimal(reader, "СумНал") ?: 0)
                        }
                        if (isCurrentNode(['Документ', 'НДС', 'СумУпл164', 'СумНалВыч'], elements)) {
                            nalPredNPPriob = nalPredNPPriob + (getXmlDecimal(reader, "НалПредНППриоб") ?: 0)
                            nalIschProd = nalIschProd + (getXmlDecimal(reader, "НалИсчПрод") ?: 0)
                        }
                    }
                    if (reader.endElement){
                        elements[reader.name.localPart] = false
                    }
                    reader.next()
                }
            } finally {
                reader.close()
            }
        }
        nalBaza010 = round(nalBaza010)
        sumNal010 = round(sumNal010)

        nalBaza020 = round(nalBaza020)
        sumNal020 = round(sumNal020)

        nalBaza030 = round(nalBaza030)
        sumNal030 = round(sumNal030)

        nalBaza040 = round(nalBaza040)
        sumNal040 = round(sumNal040)

        nalBaza070 = round(nalBaza070)
        sumNal070 = round(sumNal070)

        nalIschProd = round(nalIschProd)

        nalPredNPPriob = round(nalPredNPPriob)
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
                ОтчетГод: getReportPeriod().taxPeriod.year,
                // Код налогового органа
                КодНО: taxOrganCode,
                // Номер корректировки
                НомКорр: getCorrectionNumber(),
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

// Логические проверки (Проверки значений атрибутов признаков наличия разделов 8-11)
void logicCheck2() {
    def reader = declarationService.getXmlStreamReader(declarationData.id)
    if (reader == null) {
        return
    }

    def declarationMap = declarations()
    def parts = getParts(declarationMap)
    def has8 = (isDeclarationExist(parts, declarationMap.declaration8[0]) == 1)
    def has8n = (isDeclarationExist(parts, declarationMap.declaration8n[0]) == 1)
    def has8q = (isDeclarationExist(parts, declarationMap.declaration8q[0]) == 1)
    def has81 = (isDeclarationExist(parts, declarationMap.declaration81[0]) == 1)
    def has81q = (isDeclarationExist(parts, declarationMap.declaration81q[0]) == 1)
    def has9 = (isDeclarationExist(parts, declarationMap.declaration9[0]) == 1)
    def has9n = (isDeclarationExist(parts, declarationMap.declaration9n[0]) == 1)
    def has9q = (isDeclarationExist(parts, declarationMap.declaration9q[0]) == 1)
    def has91 = (isDeclarationExist(parts, declarationMap.declaration91[0]) == 1)
    def has91q = (isDeclarationExist(parts, declarationMap.declaration91q[0]) == 1)
    def has10 = (isDeclarationExist(parts, declarationMap.declaration10[0]) == 1)
    def has10q = (isDeclarationExist(parts, declarationMap.declaration10q[0]) == 1)
    def has11 = (isDeclarationExist(parts, declarationMap.declaration11[0]) == 1)
    def has11q = (isDeclarationExist(parts, declarationMap.declaration11q[0]) == 1)

    def declaration8 = has8q ? declarationMap.declaration8q[0] : (has8n ? declarationMap.declaration8n[0] : declarationMap.declaration8[0])
    def declaration9 = has9q ? declarationMap.declaration9q[0] : (has9n ? declarationMap.declaration9n[0] : declarationMap.declaration9[0])
    def declaration81 = has81q ? declarationMap.declaration81q[0] : declarationMap.declaration81[0]
    def declaration91 = has91q ? declarationMap.declaration91q[0] : declarationMap.declaration91[0]

    def declarationTypeIds = [declaration8, declaration9, declaration81, declaration91]
    def declarationSourceMap = getDeclarationSourcesMap(declarationTypeIds)

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
    def r91str340, r91str350, r91str050, r91str060, r91str020, r91str030, r91str040, r91str310, r91str320, r91str330
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

    // значения из декларации 8
    if (declarationSourceMap[declaration8].isCalc) {
        //elements = [:]
        def readerTmp = declarationService.getXmlStreamReader(declarationSourceMap[declaration8].declarationDataId)
        try {
            def r8str180sumTmp = 0
            while (readerTmp.hasNext()) {
                if (readerTmp.startElement) {
                    //elements[readerTmp.name.localPart] = true
                    if ("КодВидОпер".equals(readerTmp.name.localPart)) {
                        if ('06'.equals(readerTmp.getElementText())) {
                            r8str180sum += r8str180sumTmp
                        }
                    }
                    if (!r8str190 && "КнигаПокуп".equals(readerTmp.name.localPart)) {
                        r8str190 = getXmlDecimal(readerTmp, "СумНДСВсКПк") ?: 0
                    } else if ("КнПокСтр".equals(readerTmp.name.localPart)) {
                        r8str180sumTmp = getXmlDecimal(readerTmp, "СумНДСВыч") ?: 0
                    }
                }
                /*if (readerTmp.endElement) {
                    elements[readerTmp.name.localPart] = false
                } */
                readerTmp.next()
            }
        } finally {
            readerTmp.close()
        }
    }

    // значения из декларации 8.1
    if (declarationSourceMap[declaration81].isCalc) {
        elements = [:]
        def readerTmp = declarationService.getXmlStreamReader(declarationSourceMap[declaration81].declarationDataId)
        try {
            def r81str180sumTmp = 0
            while (readerTmp.hasNext()) {
                if (readerTmp.startElement) {
                    elements[readerTmp.name.localPart] = true
                    if ("КодВидОпер".equals(readerTmp.name.localPart)) {
                        if ('06'.equals(readerTmp.getElementText())) {
                            r81str180sum += r81str180sumTmp
                        }
                    }
                    if (!r81str190 && isCurrentNode(['Документ', 'КнигаПокупДЛ'], elements)) {
                        r81str190 = getXmlDecimal(readerTmp, "СумНДСИтП1Р8") ?: 0
                        r81str005 = getXmlDecimal(readerTmp, "СумНДСИтКПк") ?: 0
                    }
                    if (isCurrentNode(['Документ', 'КнигаПокупДЛ', 'КнПокДЛСтр'], elements)) {
                        r81str180sumTmp = getXmlDecimal(readerTmp, "СумНДС") ?: 0
                    }
                }
                if (readerTmp.endElement) {
                    elements[readerTmp.name.localPart] = false
                }
                readerTmp.next()
            }
        } finally {
            readerTmp.close()
        }
    }

    // значения из декларации 9
    if (declarationSourceMap[declaration9].isCalc) {
        //elements = [:]
        def readerTmp = declarationService.getXmlStreamReader(declarationSourceMap[declaration9].declarationDataId)
        try {
            def r9str200sumTmp = 0
            def r9str210sumTmp = 0
            while (readerTmp.hasNext()) {
                if (readerTmp.startElement) {
                    //elements[readerTmp.name.localPart] = true
                    if ("КодВидОпер".equals(readerTmp.name.localPart)) {
                        if ('06'.equals(readerTmp.getElementText())) {
                            r9str200sum += r9str200sumTmp
                            r9str210sum += r9str210sumTmp
                        }
                    }
                    if (!r9str260 && "КнигаПрод".equals(readerTmp.name.localPart)) {
                        r9str260 = getXmlDecimal(readerTmp, "СумНДСВсКПр18") ?: 0
                        r9str270 = getXmlDecimal(readerTmp, "СумНДСВсКПр10") ?: 0
                        r9str250 = getXmlDecimal(readerTmp, "СтПродБезНДС0") ?: 0
                    }
                    if ("КнПродСтр".equals(readerTmp.name.localPart)) {
                        r9str200sumTmp = getXmlDecimal(readerTmp, "СумНДССФ18") ?: 0
                        r9str210sumTmp = getXmlDecimal(readerTmp, "СумНДССФ10") ?: 0
                        r9str190sum += getXmlDecimal(readerTmp, "СтоимПродСФ0") ?: 0
                    }
                }
                /*if (readerTmp.endElement) {
                    elements[readerTmp.name.localPart] = false
                }*/
                readerTmp.next()
            }
        } finally {
            readerTmp.close()
        }
    }

    // значения из декларации 9.1
    if (declarationSourceMap[declaration91].isCalc) {
        elements = [:]
        def readerTmp = declarationService.getXmlStreamReader(declarationSourceMap[declaration91].declarationDataId)
        try {
            def r91str280sumTmp = 0
            def r91str290sumTmp = 0
            while (readerTmp.hasNext()) {
                if (readerTmp.startElement) {
                    elements[readerTmp.name.localPart] = true
                    if ("КодВидОпер".equals(readerTmp.name.localPart)) {
                        if ('06'.equals(readerTmp.getElementText())) {
                            r91str280sum += r91str280sumTmp
                            r91str290sum += r91str290sumTmp
                        }
                    }
                    if (!r91str340 && isCurrentNode(['Документ', 'КнигаПродДЛ'], elements)) {
                        r91str340 = getXmlDecimal(readerTmp, "СумНДСВсП1Р9_18") ?: 0
                        r91str350 = getXmlDecimal(readerTmp, "СумНДСВсП1Р9_10") ?: 0
                        r91str050 = getXmlDecimal(readerTmp, "СумНДСИтКПр18") ?: 0
                        r91str060 = getXmlDecimal(readerTmp, "СумНДСИтКПр10") ?: 0
                        r91str020 = getXmlDecimal(readerTmp, "ИтСтПродКПр18") ?: 0
                        r91str030 = getXmlDecimal(readerTmp, "ИтСтПродКПр10") ?: 0
                        r91str040 = getXmlDecimal(readerTmp, "ИтСтПродКПр0") ?: 0
                        r91str310 = getXmlDecimal(readerTmp, "СтПродВсП1Р9_18") ?: 0
                        r91str320 = getXmlDecimal(readerTmp, "СтПродВсП1Р9_10") ?: 0
                        r91str330 = getXmlDecimal(readerTmp, "СтПродВсП1Р9_0") ?: 0
                    }
                    if (isCurrentNode(['Документ', 'КнигаПродДЛ', 'КнПродДЛСтр'], elements)) {
                        r91str280sumTmp = getXmlDecimal(readerTmp, "СумНДССФ18") ?: 0
                        r91str290sumTmp = getXmlDecimal(readerTmp, "СумНДССФ10") ?: 0
                        r91str250sum += getXmlDecimal(readerTmp, "СтоимПродСФ18") ?: 0
                        r91str260sum += getXmlDecimal(readerTmp, "СтоимПродСФ10") ?: 0
                        r91str270sum += getXmlDecimal(readerTmp, "СтоимПродСФ0") ?: 0
                        r91str280sumAll += r91str280sumTmp
                        r91str290sumAll += r91str290sumTmp
                    }
                }
                if (readerTmp.endElement) {
                    elements[readerTmp.name.localPart] = false
                }
                readerTmp.next()
            }
        } finally {
            readerTmp.close()
        }
    }

    def reportPeriod = getReportPeriod()
    def department = departmentService.get(declarationData.departmentId)
    // 1. Экземпляры декларации вида «Декларация по НДС (раздел 8)», «Декларация по НДС (раздел 8 без консолид. формы)»,
    // «Декларация по НДС (раздел 8 с 3 квартала 2016)» в текущем периоде и подразделении не созданы либо создан только один из этих экземпляров
    if (((has8 ? 1 : 0) + (has8n ? 1 : 0) + (has8q ? 1 : 0)) > 1) {
        logger.error("В подразделении «%s» в периоде «%s %s» создано несколько экземпляров декларации по разделу 8! Необходимо оставить только один из экземпляров",
                department.name, reportPeriod.name, reportPeriod.taxPeriod.year)
    }

    // 2. Экземпляры декларации вида «Декларация по НДС (раздел 9)», «Декларация по НДС (раздел 9 без консолид. формы)»,
    // «Декларация по НДС (раздел 9 с 3 квартала 2016)» в текущем периоде и подразделении не созданы либо создан только один из этих экземпляров
    if (((has9 ? 1 : 0) + (has9n ? 1 : 0) + (has9q ? 1 : 0)) > 1) {
        logger.error("В подразделении «%s» в периоде «%s %s» создано несколько экземпляров декларации по разделу 9! Необходимо оставить только один из экземпляров",
                department.name, reportPeriod.name, reportPeriod.taxPeriod.year)
    }

    // 3. Экземпляры декларации вида «Декларация по НДС (раздел 10)», «Декларация по НДС (раздел 10 с 3 квартала 2016)»
    // в текущем периоде и подразделении не созданы либо создан только один из этих экземпляров
    if (has10 && has10q) {
        logger.error("В подразделении «%s» в периоде «%s %s» создано несколько экземпляров декларации по разделу 10! Необходимо оставить только один из экземпляров",
                department.name, reportPeriod.name, reportPeriod.taxPeriod.year)
    }

    // 4. Экземпляры декларации вида «Декларация по НДС (раздел 11)», «Декларация по НДС (раздел 11 с 3 квартала 2016)»
    // в текущем периоде и подразделении не созданы либо создан только один из этих экземпляров
    if (has11 && has11q) {
        logger.error("В подразделении «%s» в периоде «%s %s» создано несколько экземпляров декларации по разделу 11! Необходимо оставить только один из экземпляров",
                department.name, reportPeriod.name, reportPeriod.taxPeriod.year)
    }

    // 2. Существующие экземпляры декларации по НДС (раздел 8/раздел 8 без консолид. формы/8.1/9/раздел 9 без консолид. формы/9.1/10/11) текущего периода и подразделения находятся в состоянии «Принята»
    getParts(declarationMap).each { id, declaration ->
        if (declaration.exist && !declaration.accepted) {
            logger.error("Экземпляр декларации вида «%s», периода «%s %s» и подразделения «%s» не находится в состоянии «Принята»!",
                    declaration.name, reportPeriod.name, reportPeriod.taxPeriod.year, department.name)
        }
    }

    // 3. Атрибуты признаки наличия разделов 8-11 (в том числе раздел 8 и 9 без консолид. формы) заполнены согласно алгоритмам
    def hasOneOrMoreDeclaration = hasOneOrMoreDeclaration(parts, declarationMap)
    def checkMap = [
            'Признак наличия разделов с 8 по 12'
            : [getXmlValue(exist8_12) as BigDecimal, hasOneOrMoreDeclaration],
            'Признак наличия сведений из книги покупок об операциях, отражаемых за истекший налоговый период'
            : [getXmlValue(exist8) as BigDecimal, (hasOneOrMoreDeclaration ? (has8 || has8n || has8q ? 1 : 0) : null)],
            'Признак наличия сведений из дополнительного листа книги покупок'
            : [getXmlValue(exist81) as BigDecimal, (hasOneOrMoreDeclaration ? (has81 || has81q ? 1 : 0) : null)],
            'Признак наличия сведений из книги продаж об операциях, отражаемых за истекший налоговый период'
            : [getXmlValue(exist9) as BigDecimal, (hasOneOrMoreDeclaration ? (has9 || has9n || has9q ? 1 : 0) : null)],
            'Признак наличия сведений из дополнительного листа книги продаж'
            : [getXmlValue(exist91) as BigDecimal, (hasOneOrMoreDeclaration ? (has91 || has91q ? 1 : 0) : null)],
            'Признак наличия сведений из журнала учета выставленных счетов-фактур в отношении операций, осуществляемых в интересах другого лица на основе договоров комиссии, агентских договоров или на основе договоров транспортной экспедиции, отражаемых за истекший налоговый период'
            : [getXmlValue(exist10) as BigDecimal, (hasOneOrMoreDeclaration ? (has10 || has10q ? 1 : 0) : null)],
            'Признак наличия сведений из журнала учета полученных счетов-фактур в отношении операций, осуществляемых в интересах другого лица на основе договоров комиссии, агентских договоров или на основе договоров транспортной экспедиции, отражаемых за истекший налоговый период'
            : [getXmlValue(exist11) as BigDecimal, (hasOneOrMoreDeclaration ? (has11 || has11q ? 1 : 0) : null)],
            'Признак наличия сведений из счетов-фактур, выставленных лицами, указанными в пункте 5 статьи 173 Налогового кодекса Российской Федерации'
            : [getXmlValue(exist12) as BigDecimal, (hasOneOrMoreDeclaration ? 0 : null)]
    ]
    checkMap.each { key, value ->
        if (value[0] != value[1]) {
            logger.error("Атрибут «$key» файла формата xml заполнен неверно! Для исправления необходимо пересчитать данные декларации (кнопка «Рассчитать»).")
        }
    }

    // Проверки контрольных соотношений
    // 1.4
    def sum010_040 = r3g3str010 ?: 0 + r3g3str020 ?: 0 + r3g3str030 ?: 0 + r3g3str040 ?: 0
    def sum120_160 = r3g3str120 ?: 0 + r3g3str150 ?: 0 + r3g3str160 ?: 0
    def denom1 = sum010_040 + r7g2sum
    def denom2 = sum120_160 + r7g4sum
    if (denom1 == 0 || denom2 == 0) {
        logger.warn("КС 1.4. Проверка невозможно, так как в результате проверки получен нулевой знаменатель " +
                "(деление на ноль невозможно). Алгоритм проверки: " +
                "(Раздел 3, гр. 3: «Строка 010» + «Строка 020» + «Строка 030» + «Строка 040») / " +
                "[(Раздел 3, гр. 3: («Строка 010» + «Строка 020» + «Строка 030» + «Строка 040») + " +
                "(Раздел 7: сумма значения гр. 2 по блокам, в которых значение гр.1 в диапазоне: 1010800 - 1010821)] = " +
                "(Раздел 3, гр. 3: «Строка 120» + «Строка 150» + «Строка 160») / " +
                "[(Раздел 3, гр. 3: «Строка 120» + «Строка 150» + «Строка 160») + " +
                "(Раздел 7: сумма значения гр. 4 по блокам, в которых значение гр.1 в диапазоне: 1010800 - 1010821)]")
    } else if (sum010_040 / denom1 != sum120_160 / denom2) {
        logger.warn("КС 1.4. Возможно необоснованное применение налоговых вычетов. НК РФ ст. 149, 170 п. 4")
    }
    // 1.11
    if (r3g3str170 ?: 0 > r3g5str010 ?: 0 + r3g5str020 ?: 0 + r3g5str030 ?: 0 + r3g5str040 ?: 0) {
        logger.warn("КС 1.11. Налоговые вычеты не обоснованы, либо налоговая база занижена, " +
                "так как суммы отработанных авансов не включены в реализацию. " +
                "НК РФ ст. 171, п. 8, НК РФ ст. 172, п. 6, либо НК РФ ст. 146, п. 1 ")
    }
    // 1.25 (8, 8.1, 9, 9.1)
    if (checkSources('1.25', declarationSourceMap, [declaration8, declaration81, declaration9, declaration91])) {
        def sum25 = r8str190 + (r81str190 - r81str005) - (r9str260 + r9str270) - (r9str200sum + r9str210sum) + (r91str340 + r91str350 - r91str050 - r91str060) - (r91str280sum + r91str290sum)
        if (r1str050 > 0 && sum25 <= 0) {
            logger.warn("КС 1.25. Завышение суммы НДС, подлежащей возмещению за онп. НК РФ ст. 173")
        }
    }
    // 1.26 (9, 9.1)
    if (checkSources('1.26', declarationSourceMap, [declaration9, declaration91])) {
        if (r2str060sum < r9str200sum + r9str210sum + r91str280sum + r91str290sum) {
            logger.warn("КС 1.26. Занижение суммы НДС, подлежащей уплате в бюджет. НК РФ ст. 161, п. 4 ст. 173")
        }
    }
    // 1.27 (9, 9.1)
    if (checkSources('1.27', declarationSourceMap, [declaration9, declaration91])) {
        if (r3g5str110 + r2str060sum < r9str260 + r9str270 + r91str340 + r91str350 - r91str050 - r91str060) {
            logger.warn("КС 1.27. Занижение суммы НДС, исчисленного к уплате в бюджет. НК РФ ст. 153, 161, 164, 165, 166, 167, 173")
        }
    }
    // 1.28 (8, 8.1)
    if (checkSources('1.28', declarationSourceMap, [declaration8, declaration81])) {
        if (r3g3str190 > r8str190 + r81str190 - r81str005) {
            logger.warn("КС 1.28. Завышение суммы НДС, подлежащей вычету. НК РФ ст. 171, 172")
        }
    }
    // 1.31 (8, 8.1)
    if (checkSources('1.31', declarationSourceMap, [declaration8, declaration81])) {
        if (r3g3str180 > r8str180sum + r81str180sum) {
            logger.warn("КС 1.31. Завышение суммы НДС, подлежащей вычету. НК РФ ст. 161, 171, 172")
        }
    }
    // 1.33 (8.1)
    if (checkSources('1.33', declarationSourceMap, [declaration81])) {
        if (r81str005 + r81str180sum < r81str190) {
            logger.warn("КС 1.33. Возможно завышение суммы НДС, подлежащей вычету. НК РФ ст. 171, 172")
        }
    }
    // 1.36 (9)
    if (checkSources('1.36', declarationSourceMap, [declaration9])) {
        if (r9str250 > r9str190sum) {
            logger.warn("КС 1.36. Возможно занижение исчисленной суммы НДС вследствие неполного отражения НБ " +
                    "либо неверное применение ставки по НДС (при условии, что соотношение 1.37 выполняется). " +
                    "НК РФ ст. 164, 165, 167, 173")
        }
    }
    // 1.39 (9.1)
    if (checkSources('1.39', declarationSourceMap, [declaration91])) {
        if (r91str020 + r91str250sum > r91str310) {
            logger.warn("КС 1.39. Возможно занижение суммы НДС, исчисленного к уплате в бюджет (при условии, что соотношение 1.32 и 1.49 выполняются). " +
                    "НК РФ ст. 153, 173, п. 3 Раздела IV Приложения 5 к Постановлению N 1137")
        }
    }
    // 1.40 (9.1)
    if (checkSources('1.40', declarationSourceMap, [declaration91])) {
        if (r91str030 + r91str260sum > r91str320) {
            logger.warn("КС 1.40. Возможно занижение суммы НДС, исчисленного к уплате в бюджет. " +
                    "НК РФ ст. 153, 173, п. 3 Раздела IV Приложения 5 к Постановлению N 1137")
        }
    }
    // 1.41  (9.1)
    if (checkSources('1.41', declarationSourceMap, [declaration91])) {
        if (r91str040 + r91str270sum > r91str330) {
            logger.warn("КС 1.41. Возможно занижение исчисленной суммы НДС, вследствие неполного отражения НБ " +
                    "либо неверное применение ставки по НДС. НК РФ ст. 164, 165, 167, 173")
        }
    }
    // 1.42 (9.1)
    if (checkSources('1.42', declarationSourceMap, [declaration91])) {
        if (r91str050 + r91str280sumAll > r91str340) {
            logger.warn("КС 1.42. Возможно занижение суммы НДС, исчисленного к уплате в бюджет (при условии, что соотношение 1.32 выполняется). " +
                    "НК РФ ст. 153, 173, п. 3 Раздела IV Приложения 5 к Постановлению N 1137")
        }
    }
    // 1.43 (9.1)
    if (checkSources('1.43', declarationSourceMap, [declaration91])) {
        if (r91str060 + r91str290sumAll > r91str350) {
            logger.warn("КС 1.43. Возможно занижение суммы НДС, исчисленного к уплате в бюджет. " +
                    "НК РФ ст. 153, 173, п. 3 Раздела IV Приложения 5 к Постановлению N 1137")
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
def Map<Long, Expando> getParts(def declarationMap) {
    if (declarationParts == null) {
        declarationParts = new HashedMap<Long, Expando>()
        declarationMap.each { declaration ->
            id = declaration.value[0]
            name = declaration.value[1]
            def declarationData = declarationService.find(id, declarationData.departmentReportPeriodId, null, null)

            def result = new Expando()
            result.id = (declarationData?.id)
            result.name = declaration.value[1]
            result.section = declaration.value[2]
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

def String getDeclarationFileName(def parts, def declarationTypeId) {
    return parts.get(declarationTypeId)?.fileName ?: empty
}

def BigDecimal isDeclarationExist(def parts, def declarationTypeId) {
    return parts.get(declarationTypeId)?.exist ? 1 : 0
}

def BigDecimal hasOneOrMoreDeclaration(def parts, def declarationsMap) {
    def BigDecimal hasDeclaration = 0
    declarationsMap.each { declaration ->
        if (isDeclarationExist(parts, declaration.value[0]) == 1) {
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
def opuCodes2016 = ['48413.01']

/**
 * Получить значение для НалНеВыч.
 *
 * @param code строки формы 724.2.1
 */
def getNalNeVich(def code) {
    def order = getReportPeriod()?.order
    if (code == specialCode) {
        def isBefore2016 = getEndDate()?.format('yyyy')?.toInteger() < 2016
        def tmpOpuCodes = (isBefore2016 ? opuCodes : opuCodes2016)
        // сумма кодов ОПУ из отчета 102
        def sumOpu = getSumByOpuCodes(tmpOpuCodes, getEndDate())
        if (order == 1) {
            return sumOpu
        } else {
            def sumOpuPrev = getSumByOpuCodes(tmpOpuCodes, getPrevEndDate())
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

// Логические проверки (Проверки значений атрибутов формы настроек подразделения, атрибутов файла формата законодателя)
void logicCheck1(LogLevel logLevel) {
    // получение данных из xml'ки
    def reader = declarationService.getXmlStreamReader(declarationData.id)
    if (reader == null) {
        return
    }
    def elements = [:]

    def kodNO
    def poMestu
    def documentFound = false

    def naimOrg
    def innJulNpJul
    def kpp
    def npJulFound = false

    def prPodp
    def podpisantFound = false

    def signatorySurname
    def signatoryFirstName
    def fioFound = false

    def naimDok
    def svPredFound = false

    def oktmoSumUplNP
    def sumUplNPFound = false

    def oktmoSumUplNA
    def sumUplNAFound = false

    def okved
    def svNPFound = false

    def innJulSvReorgJul
    def kppSvReorgJul
    def formReorg
    def svReorgJulFound = false

    def idFile
    def versForm
    def fileFound = false

    try{
        while (reader.hasNext()) {
            if (reader.startElement) {
                elements[reader.name.localPart] = true
                if (!documentFound && isCurrentNode(['Документ'], elements)) {
                    documentFound = true
                    kodNO = getXmlValue(reader, 'КодНО')
                    poMestu = getXmlValue(reader, 'ПоМесту')
                } else if (!svNPFound && isCurrentNode(['Документ', 'СвНП'], elements)) {
                    svNPFound = true
                    okved = getXmlValue(reader, 'ОКВЭД')
                } else if (!podpisantFound && isCurrentNode(['Документ', 'Подписант'], elements)) {
                    podpisantFound = true
                    prPodp = getXmlValue(reader, 'ПрПодп')
                } else if (!fioFound && isCurrentNode(['Документ', 'Подписант', 'ФИО'], elements)) {
                    fioFound = true
                    signatorySurname = getXmlValue(reader, 'Фамилия')
                    signatoryFirstName = getXmlValue(reader, 'Имя')
                } else if (!svPredFound && isCurrentNode(['Документ', 'Подписант', 'СвПред'], elements)) {
                    svPredFound = true
                    naimDok = getXmlValue(reader, 'НаимДок')
                } else if (!npJulFound && isCurrentNode(['Документ', 'СвНП', 'НПЮЛ'], elements)) {
                    npJulFound = true
                    naimOrg = getXmlValue(reader, 'НаимОрг')
                    innJulNpJul = getXmlValue(reader, 'ИННЮЛ')
                    kpp = getXmlValue(reader, 'КПП')
                } else if (!svReorgJulFound && isCurrentNode(['Документ', 'СвНП', 'НПЮЛ', 'СвРеоргЮЛ'], elements)) {
                    svReorgJulFound = true
                    innJulSvReorgJul = getXmlValue(reader, 'ИННЮЛ')
                    kppSvReorgJul = getXmlValue(reader, 'КПП')
                    formReorg = getXmlValue(reader, 'ФормРеорг')
                } else if (!sumUplNPFound && isCurrentNode(['Документ', 'НДС', 'СумУплНП'], elements)) {
                    sumUplNPFound = true
                    oktmoSumUplNP = getXmlValue(reader, 'ОКТМО')
                } else if (!sumUplNAFound && isCurrentNode(['Документ', 'НДС', 'СумУплНА'], elements)) {
                    sumUplNAFound = true
                    oktmoSumUplNA = getXmlValue(reader, 'ОКТМО')
                } else if (!fileFound && isCurrentNode([], elements)) {
                    fileFound = true
                    idFile = getXmlValue(reader, 'ИдФайл')
                    versForm = getXmlValue(reader, 'ВерсФорм')
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

    def refBook = getRefBook(RefBook.DEPARTMENT_CONFIG_VAT)
    if (naimOrg == null || naimOrg.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "Наименование организации (обособленного подразделения)", "НПЮЛ.НаимОрг", refBook.getAttribute('NAME').name))
    }
    def section1HasError = (oktmoSumUplNP == null || oktmoSumUplNP.trim().isEmpty())
    def section2HasError = (sumUplNAFound && (oktmoSumUplNA == null || oktmoSumUplNA.trim().isEmpty()))
    if (section1HasError || section2HasError) {
        def place = (section2HasError ? "Раздел 1, 2" : "Раздел 1")
        def xmlNames = (section2HasError ? "СумУплНП.ОКТМО, СумУплНА.ОКТМО" : "СумУплНП.ОКТМО")
        logger.log(logLevel, getOktmoMessage(place, "Код по ОКТМО", xmlNames, refBook.getAttribute('OKTMO').name))
    }
    if (innJulNpJul == null || innJulNpJul.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "ИНН налогоплательщика", "НПЮЛ.ИННЮЛ", refBook.getAttribute('INN').name))
    }
    if (kpp == null || kpp.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "КПП налогоплательщика", "НПЮЛ.КПП", refBook.getAttribute('KPP').name))
    }
    if (kodNO == null || kodNO.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Наименование xml файла (кон. налоговый орган) и титульный лист", "Код налогового органа", "Документ.КодНО", refBook.getAttribute('TAX_ORGAN_CODE').name))
    }
    if (!checkTaxOrganCodeProm(idFile)) {
        def message = "Обязательный для заполнения атрибут «Код налогового органа (пром.)» в наименовании xml файла не заполнен! На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения отсутствовало значение атрибута «%s»."
        def attributeName = refBook.getAttribute('TAX_ORGAN_CODE_PROM').name
        logger.log(logLevel, message, attributeName)
    }
    if (okved == null || okved.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "Код вида экономической деятельности и по классификатору ОКВЭД", "СвНП.ОКВЭД", refBook.getAttribute('OKVED_CODE').name))
    }
    if (prPodp == null || prPodp.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "Признак лица, подписавшего документ", "Подписант.ПрПодп", refBook.getAttribute('SIGNATORY_ID').name))
    }
    if (signatorySurname == null || signatorySurname.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "Фамилия подписанта", "Подписант.Фамилия", refBook.getAttribute('SIGNATORY_SURNAME').name))
    }
    if (signatoryFirstName == null || signatoryFirstName.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "Имя подписанта", "Подписант.Имя", refBook.getAttribute('SIGNATORY_FIRSTNAME').name))
    }
    if (prPodp == '2' && (naimDok == null || naimDok.trim().isEmpty())) {
        def message = "Титульный лист. Условно обязательный для заполнения (Признак лица, подписавшего документ = 2) атрибут «%s» (%s) не заполнен! На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения отсутствовало значение атрибута «%s»."
        logger.log(logLevel, message, "Наименование документа, подтверждающего полномочия представителя", "СвПред.НаимДок", refBook.getAttribute('APPROVE_DOC_NAME').name)
    }
    if (poMestu == null || poMestu.trim().isEmpty()) {
        logger.log(logLevel, getMessage("Титульный лист", "По месту нахождения (учета) (код)", "Документ.ПоМесту", refBook.getAttribute('TAX_PLACE_TYPE_CODE').name))
    }
    if ((formReorg != null && formReorg != '0') && (innJulSvReorgJul == null || innJulSvReorgJul.trim().isEmpty())) {
        logger.log(logLevel, getReorgMessage("Титульный лист", "ИНН реорганизованной организации", "СвРеоргЮЛ.ИННЮЛ", refBook.getAttribute('REORG_INN').name))
    }
    if ((formReorg != null && formReorg != '0') && (kppSvReorgJul == null || kppSvReorgJul.trim().isEmpty())) {
        logger.log(logLevel, getReorgMessage("Титульный лист", "КПП реорганизованной организации", "СвРеоргЮЛ.КПП", refBook.getAttribute('REORG_KPP').name))
    }
    if (versForm == null || !version.equals(versForm)) {
        def message = "Обязательный для заполнения атрибут «%s» (%s) заполнен неверно (%s)! Ожидаемое значение «%s». На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения было указано неверное значение атрибута «%s»."
        def attributeName = refBook.getAttribute('FORMAT_VERSION').name
        def value = (versForm == null || versForm.isEmpty() ? 'пустое значение' : versForm)
        logger.log(logLevel, message, attributeName, "Файл.ВерсФорм", value, version, attributeName)
    }
}

String getMessage(String place, String printName, String xmlName, String attributeName) {
    return String.format("%s. Обязательный для заполнения атрибут «%s» (%s) не заполнен! На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения отсутствовало значение атрибута «%s».",
            place, printName, xmlName, attributeName)
}

String getOktmoMessage(String place, String printName, String xmlName, String attributeName) {
    return String.format("%s. Атрибут «%s» (%s) не заполнен! На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения отсутствовало значение атрибута «%s».",
            place, printName, xmlName, attributeName)
}

String getReorgMessage(String place, String printName, String xmlName, String attributeName) {
    return String.format("%s. Условно обязательный для заполнения (заполнен код формы реорганизации) атрибут «%s» (%s) не заполнен! На момент расчёта экземпляра декларации (формирование XML) на форме настроек подразделения отсутствовало значение атрибута «%s».",
            place, printName, xmlName, attributeName)
}

/**
 * Проверить значение "Код налогового органа (пром.)" в составе атрибут xml "ИдФайл".
 * ИдФайл имеет следующйю структуру:
 *      NO_NDS_Код налогового органа (пром.)_Код налогового органа (кон.)_ИНН+КПП_ГГГГММДД_UUID.
 *
 * @param value значение ИдФайл
 */
def checkTaxOrganCodeProm(def value) {
    if (!value) {
        return false
    }
    def tmpValues = value.split('_')
    // "Код налогового органа (пром.)" - третий по порядку в ИдФайл
    if (tmpValues.size() < 3 || !tmpValues[2] || 'null' == tmpValues[2]) {
        return false
    }
    return true
}

String getXmlValue(XMLStreamReader reader, String attrName) {
    return reader?.getAttributeValue(null, attrName)
}

def getRefBook(def id) {
    if (refBookMap[id] == null) {
        refBookMap[id] = refBookFactory.get(id)
    }
    return refBookMap[id]
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

@Field
def reportPeriod = null

def getReportPeriod() {
    if (reportPeriod == null) {
        reportPeriod = reportPeriodService.get(declarationData.reportPeriodId)
    }
    return reportPeriod
}

@Field
def correctionNumber = null

def getCorrectionNumber() {
    if (correctionNumber == null) {
        correctionNumber = reportPeriodService.getCorrectionNumber(declarationData.departmentReportPeriodId)
    }
    return correctionNumber
}

@Field
def period4 = null

/** Получить четвертый отчетный период (4 квартал) текущего налогового периода. */
def getPeriod4Id() {
    if (period4 == null) {
        def year = getReportPeriod()?.taxPeriod?.year
        def start = Date.parse('dd.MM.yyyy', '01.01.' + year)
        def end = Date.parse('dd.MM.yyyy', '31.12.' + year)
        def reportPeriods = reportPeriodService.getReportPeriodsByDate(TaxType.VAT, start, end)
        period4 = reportPeriods.find { it.order == 4 }
    }
    return period4?.id
}

@Field
def departmentReportPeriodMap = [:]

DepartmentReportPeriod getDepartmentReportPeriod(def id) {
    if (departmentReportPeriodMap[id] == null) {
        departmentReportPeriodMap[id] = departmentReportPeriodService.get(id)
    }
    return departmentReportPeriodMap[id]
}

@Field
def formDataMap = [:]

def getFormData(def formTypeId, def formDataKind, def departmentId, def reportPeriodId) {
    if (reportPeriodId == null) {
        return null
    }
    def key = formTypeId + '#' + formDataKind.getId() + '#' + departmentId + '#' + reportPeriodId
    if (formDataMap[key] == null) {
        formDataMap[key] = formDataService.getLast(formTypeId, formDataKind, bankDepartmentId, reportPeriodId, null, null, false)
    }
    return formDataMap[key]
}

@Field
DepartmentReportPeriod prevDepartmentReportPeriod = null

/** Получить предыдущий корректирующий период банка. */
DepartmentReportPeriod getPrevDepartmentReportPeriod() {
    if (prevDepartmentReportPeriod) {
        return prevDepartmentReportPeriod
    }
    DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter()
    filter.setDepartmentIdList([bankDepartmentId])
    filter.setReportPeriodIdList([declarationData.reportPeriodId])
    List<DepartmentReportPeriod> departmentReportPeriods = departmentReportPeriodService.getListByFilter(filter)
    // найти предыдущие корректирующие периоды
    for (int i = departmentReportPeriods.size() - 1; i >= 0 ; i--) {
        DepartmentReportPeriod dpr = departmentReportPeriods[i]
        if (dpr.id == declarationData.departmentReportPeriodId && i > 0) {
            prevDepartmentReportPeriod = departmentReportPeriods[i - 1]
            return prevDepartmentReportPeriod
        }
    }
    return null
}

@Field
def needFindPrevCorrectionDeclaration = true

@Field
DeclarationData prevCorrectionDeclarationData = null

/** Получить предыдущую корректирующую декларацию НДС. */
DeclarationData getPrevCorrectionDeclaration() {
    if (!needFindPrevCorrectionDeclaration) {
        return prevCorrectionDeclarationData
    }
    DepartmentReportPeriod departmentReportPeriod = getPrevDepartmentReportPeriod()
    // найти декларацию среди предыдущих корректирующих периодов
    if (departmentReportPeriod) {
        List<DeclarationData> declarationDatas = declarationService.find(declarationType, departmentReportPeriod.id)
        if (declarationDatas != null && !declarationDatas.isEmpty()) {
            prevCorrectionDeclarationData = declarationDatas.get(0)
        }
    }
    needFindPrevCorrectionDeclaration = false
    return prevCorrectionDeclarationData
}

/**
 * Предварительные проверки перед выполнением расчета Раздела 3 в уточненной декларации.
 *
 * @return возвращает false если номер корректировки больше 1 и нет предыдущей корректирующей декларации (в этом случае раздел 3 не будет сформирован)
 */
def prevSection3Check() {
    def correction = getCorrectionNumber()
    if (correction == null || correction == 0) {
        return true
    }
    def departmentName = departmentService.get(bankDepartmentId)?.name
    def year = getReportPeriod()?.taxPeriod?.year
    def period = getReportPeriod()?.name
    def periodName = year + ', ' + period

    // 1. проверка декларации "Декларация по НДС (раздел 1-7)"
    if (correction != null && correction > 1) {
        def declarationName = (declarationService.getType(declarationType)?.name ?: 'Декларация по НДС (раздел 1-7)')
        DeclarationData declarationNDS1_7 = getPrevCorrectionDeclaration()
        DepartmentReportPeriod dpr = getPrevDepartmentReportPeriod()
        def correctionDate = (dpr?.correctionDate?.format('dd.MM.yyyy') ?: '')
        if (declarationNDS1_7 == null || !declarationNDS1_7.accepted) {
            // сообщение 1
            def msg = "Не найдена декларация-источник. Вид: «%s», Подразделение: «%s», Период: «%s», Дата сдачи корректировки: «%s». Расчет раздела 3 не будет выполнен."
            logger.warn(msg, declarationName, departmentName, periodName, correctionDate)
            return false
        } else {
            // сообщение 2
            def msg = "Для заполнения строк 010-040, 070, 120, 170 раздела 3 определена декларация-источник. Вид: «%s», Подразделение: «%s», Период: «%s», Дата сдачи корректировки: «%s»."
            logger.info(msg, declarationName, departmentName, periodName, correctionDate)
        }
    }

    def forFormNamePeriod4Id = (getPeriod4Id() ?: declarationData.reportPeriodId)

    // 2. проверка формы 724.1.1
    year = getReportPeriod()?.taxPeriod?.year
    period = (getPeriod4Id() ? reportPeriodService.get(getPeriod4Id())?.name : 'четвёртый квартал')
    periodName = year + ', ' + period

    def formTypeId = formType_724_1_1
    def formDataKind = FormDataKind.CONSOLIDATED
    def formName = formDataService.getFormTemplate(formTypeId, forFormNamePeriod4Id)?.name
    def formData = getFormData724_1_1()
    if (formData) {
        def subMsg = ''
        def correctionDate = getDepartmentReportPeriod(formData.departmentReportPeriodId)?.correctionDate?.format('dd.MM.yyyy')
        if (correctionDate) {
            subMsg = String.format(", Дата сдачи корректировки: «%s»", correctionDate)
        }
        // сообщение 3
        def msg = "Для заполнения строк 010-040, 070, 120, 170 раздела 3 определена форма-источник. Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s»%s."
        logger.info(msg, formDataKind.title, formName, departmentName, periodName, subMsg)
    } else {
        // сообщение 4
        def msg = "Не найдена форма-источник. Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s». При заполнении строк 010-040, 070, 120, 170 раздела 3 значения требуемых граф формы-источника будут приняты за нулевые."
        logger.warn(msg, formDataKind.title, formName, departmentName, periodName)
    }

    // 3. проверка формы 724.10
    formTypeId = formType_724_10
    formName = formDataService.getFormTemplate(formTypeId, forFormNamePeriod4Id)?.name
    formData = getFormData(formTypeId, formDataKind, bankDepartmentId, getPeriod4Id())
    if (formData && formData.state == WorkflowState.ACCEPTED) {
        def subMsg = ''
        def correctionDate = getDepartmentReportPeriod(formData.departmentReportPeriodId)?.correctionDate?.format('dd.MM.yyyy')
        if (correctionDate) {
            subMsg = String.format(", Дата сдачи корректировки: «%s»", correctionDate)
        }
        // сообщение 5
        def msg = "Для заполнения строки 105 раздела 3 определена форма-источник. Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s»%s."
        logger.info(msg, formDataKind.title, formName, departmentName, periodName, subMsg)
    } else {
        // сообщение 6
        def msg = "Не найдена форма-источник. Тип: «%s», Вид: «%s», Подразделение: «%s», Период: «%s». Строка 105 раздела 3 не будет заполнена."
        logger.warn(msg, formDataKind.title, formName, departmentName, periodName)
    }
    return true
}

@Field
def formData724_1_1 = null

/** Получить строки источника 724.1.1 (форма должна быть только в корректирующем периоде). */
def getFormData724_1_1() {
    if (formData724_1_1 == null) {
        def formData = null
        if (getPeriod4Id() != null) {
            formData = formDataService.getLast(formType_724_1_1, FormDataKind.CONSOLIDATED, bankDepartmentId, getPeriod4Id(), null, null, false)
        }
        if (formData != null) {
            def correctionDate = getDepartmentReportPeriod(formData.departmentReportPeriodId)?.correctionDate
            // период только корректирующий
            if (formData.state == WorkflowState.ACCEPTED && correctionDate) {
                formData724_1_1 = formData
            }
        }
    }
    return formData724_1_1
}

/**
 * Получить мапу с информацией о источниках.
 * Мапа содержит мапу с иформацией о каждом источнике.
 * Возможные значения в мапе с иформацией:
 *   - declarationTypeId		- id типа декларации
 *   - declarationDataId		- id экземляра декларации
 *   - departmentReportPeriod
 *   - code001					- true - значение строки 001 равно 0
 *   - reader					- для чтения xml, используется только в этом методе
 *   - isExist					- для вывода сообщения о несуществовании источника
 *   - isCalc					- для вывода сообщения о нерасчитанности источника
 *   - isHasNotPrevCode001		- для вывода сообщения о том что нет подходящих источников со значением строки 001 равной 0
 *
 * @param declarationTypeIds список id типов декларации источников
 */
def getDeclarationSourcesMap(def declarationTypeIds) {
    def declarationInfoMap = [:]
    // найти все источники находящиеся в одном периоде (в том же корректирующем) что и текущая декларация
    for (def declarationTypeId : declarationTypeIds) {
        def declarationInfo = [:]
        declarationInfo.declarationTypeId = declarationTypeId
        declarationInfo.departmentReportPeriod = getDepartmentReportPeriod(declarationData.departmentReportPeriodId)

        DeclarationData declarationDataTmp = declarationService.find(declarationTypeId, declarationData.departmentReportPeriodId, null, null)
        declarationInfo.declarationDataId = declarationDataTmp?.id
        if (declarationDataTmp?.id) {
            XMLStreamReader reader = declarationService.getXmlStreamReader(declarationDataTmp?.id)
            declarationInfo.reader = reader
        }
        declarationInfo.isExist = (declarationDataTmp != null)
        declarationInfo.isCalc = (declarationInfo.reader != null)
        declarationInfoMap[declarationTypeId] = declarationInfo
    }

    // проверить у источников строку 001 (должны иметь значение 0)
    def hasAllCode001 = true
    for (def declarationTypeId : declarationTypeIds) {
        def declarationInfo = declarationInfoMap[declarationTypeId]
        // пропускать не созданные и не расчитанные декларации
        if (declarationInfo.declarationDataId == null || declarationInfo.reader == null) {
            hasAllCode001 = false
            continue
        }
        def declarationDataId = declarationInfo.declarationDataId
        def reader = declarationInfo.reader
        def code001 = getCode001FromXML(declarationDataId, declarationTypeId, reader)
        if (code001 != 0) {
            hasAllCode001 = false
            // break не нужен, чтобы для всех reader вызвался метод close() в getCode001FromXML()
        }
        declarationInfo.code001 = (code001 == 0)
    }
    if (hasAllCode001) {
        return declarationInfoMap
    }
    // поиск в декларациях предыдущих периодов (налоговых/корректирующих) в которых строку 001 имеет значение 0
    def needDepartmentReportPeriods = getPrevDepartmentReportPeriods()
    for (def declarationTypeId : declarationTypeIds) {
        def declarationInfo = declarationInfoMap[declarationTypeId]
        // пропускать не созданные, не расчитанные декларации и у которых строка 001 имеет значение равное 0
        if (declarationInfo.declarationDataId == null || declarationInfo.reader == null || declarationInfo.code001) {
            continue
        }
        declarationInfo.clear()
        for (def needDepartmentReportPeriod : needDepartmentReportPeriods) {
            DeclarationData declarationDataTmp = declarationService.find(declarationTypeId, needDepartmentReportPeriod.id, null, null)
            if (declarationDataTmp) {
                def code001 = getCode001FromXML(declarationDataTmp.id, declarationTypeId)
                if (code001 == 0) {
                    declarationInfo.declarationTypeId = declarationTypeId
                    declarationInfo.declarationDataId = declarationDataTmp.id
                    declarationInfo.departmentReportPeriod = needDepartmentReportPeriod
                    declarationInfo.isExist = true
                    declarationInfo.isCalc = true
                    declarationInfo.code001 = true
                    break
                }
            }
        }
        // если не найдена ни одна декларация в предыдущих периодах (налоговых/корректирующих)
        if (declarationInfo.isEmpty()) {
            declarationInfo.isHasNotPrevCode001 = true
        }
    }
    return declarationInfoMap
}

/** Получить предыдущие периоды (корректирующие/налоговые). */
def getPrevDepartmentReportPeriods() {
    DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter()
    filter.reportPeriodIdList = [getReportPeriod()?.id]
    filter.departmentIdList = [declarationData.departmentId]
    List<DepartmentReportPeriod> departmentReportPeriods = departmentReportPeriodService.getListByFilter(filter)
    def find = false
    for (int i = departmentReportPeriods.size() - 1; i >= 0; i--) {
        DepartmentReportPeriod dpr = departmentReportPeriods[i]
        if (!find && dpr.id == declarationData.departmentReportPeriodId) {
            find = true
            continue
        }
        if (find) {
            departmentReportPeriods.add(dpr)
        }
    }
    return departmentReportPeriods
}

/** Мапа с названиями атрибутов для строки 001 декларации (ключ: id типа декларации -> значение: название атрибута строки 001). */
@Field
def declarationCode001Map = [
        12 : 'ПризнСвед8',  // 8
        18 : 'ПризнСвед8',  // 8 без конс
        28 : 'ПризнСвед8',  // 8 с 3 квартала 2016
        13 : 'ПризнСвед81', // 8.1
        29 : 'ПризнСвед81', // 8.1 с 3 квартала 2016
        14 : 'ПризнСвед9',  // 9
        21 : 'ПризнСвед9',  // 9 без конс
        24 : 'ПризнСвед9',  // 9 с 3 квартала 2016
        15 : 'ПризнСвед91', // 9.1
        27 : 'ПризнСвед91', // 9.1 с 3 квартала 2016
        16 : 'ПризнСвед10', // 10
        26 : 'ПризнСвед10', // 10 с 3 квартала 2016
        17 : 'ПризнСвед11', // 11
        25 : 'ПризнСвед11', // 11 с 3 квартала 2016
]

/**
 * Получить значение строки 001 декларации истчоника.
 *
 * @param declarationDataId id экземпляра декларации
 * @param declarationTypeId id типа делкларации для определения названия атрибута строка 001
 * @param xmlReader для чтения xml (может быть не задан)
 */
def getCode001FromXML(def declarationDataId, def declarationTypeId, def xmlReader = null) {
    XMLStreamReader reader = xmlReader
    if (reader == null) {
        reader = declarationService.getXmlStreamReader(declarationDataId)
    }
    def code001AttributeName = declarationCode001Map[declarationTypeId]
    def code001 = null
    try {
        def elements = [:]
        while (reader.hasNext()) {
            if (reader.startElement) {
                elements[reader.name.localPart] = true
                if (isCurrentNode(['Документ'], elements)) {
                    code001 = getXmlDecimal(reader, code001AttributeName) ?: 0
                    break
                }
            }
            if (reader.endElement){
                elements[reader.name.localPart] = false
            }
            reader.next()
        }
    } finally {
        reader.close()
    }
    return code001
}

/**
 * Проверка источников: сущестование, расчитанность. Если проверки не проходят, то не выполняются проверки КС.
 *
 * @param checkName имя проверки (1.25, 1.26...)
 * @param declatationSourcesMap мапа с информациями о декларациях-источниках
 * @param declarationTypeIds список id типов декларации для текущей проверки КС
 */
def checkSources(def checkName, def declatationSourcesMap, def declarationTypeIds) {
    def correctionDate = getDepartmentReportPeriod(declarationData.departmentReportPeriodId)?.correctionDate

    // если декларации-источники принадлежат другим периодам (налоговым/корректирующи) отличным от периода текущей декларации,
    // и если текущая декларация имеет налоговый период, то не выполнять КС (для случая п. 8.3.2.3. шаг 2.3.1. из чтз)
    for (def declarationTypeId : declarationTypeIds) {
        def declarationInfo = declatationSourcesMap[declarationTypeId]
        if (declarationInfo?.departmentReportPeriod?.id != declarationData.departmentReportPeriodId && correctionDate == null) {
            return false
        }
    }

    def periodName = getReportPeriod()?.taxPeriod?.year + ', ' + getReportPeriod()?.name
    def correctionDateStr = (correctionDate ? " с датой сдачи корректировки " + correctionDate?.format('dd.MM.yyyy') : '')
    def departmentName = departmentService.get(declarationData.departmentId)?.name
    def result = true

    for (def declarationTypeId : declarationTypeIds) {
        def declarationName = getDeclarationName(declarationTypeId)

        def declarationInfo = declatationSourcesMap[declarationTypeId]
        if (declarationInfo.isHasNotPrevCode001) {
            // нет ни одной декларации в предыдущих периодах (налоговых/корректирующих) которая имела бы строку 001 раной 0
            logger.warn("КС %s. Проверка невозможна, т.к. отсутствует экземпляр декларации со строкой 001 равной «0»: Период: «%s» (налоговый/корректирующий), Подразделение: «%s», Вид: «%s",
                    checkName, periodName, departmentName, declarationName)
            result = false
        } else if (!declarationInfo.isExist) {
            // декларация источник не создана
            logger.warn("КС %s. Проверка невозможна, т.к. не создан экземпляр декларации: Период: «%s%s», Подразделение: «%s», Вид: «%s»",
                    checkName, periodName, correctionDateStr, departmentName, declarationName)
            result = false
        } else if (!declarationInfo.isCalc) {
            // декларация источник не расчитана
            logger.warn("КС %s. Проверка невозможна, т.к. не рассчитан экземпляр декларации: Период: «%s%s», Подразделение: «%s», Вид: «%s»",
                    checkName, periodName, correctionDateStr, departmentName, declarationName)
            result = false
        }
    }
    return result
}

@Field
def declarationNameMap = [:]

def getDeclarationName(def typeId) {
    if (declarationNameMap[typeId] == null) {
        declarationNameMap[typeId] = declarationService.getType(typeId)?.name
    }
    return declarationNameMap[typeId]
}