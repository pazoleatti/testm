package form_template.income.pivot_table.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.Relation
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 850 - Сводная таблица - Лист 08 декларации по прибыли
 * formTemplateId=850
 *
 * @author Bulat Kinzyabulatov
 */

// 1  number       - № п/п
// 2  rnu          - Номер РНУ
// 3  code         - Код КНУ
// 4  corrType     - Вид корректировки (1, 2, 3)
// 5  base         - Основание отнесения сделки к контролируемой в соответствии со ст. 105.14 НК РФ // (\d{1,3}( )*;( )*){0,10}
// 6  countryCode  - Код страны регистрации (инкорпорации) контрагента
// 7  innKio       - ИНН контрагента
// 8  rsk          - Регистрационный номер контрагента в стране регистрации (инкорпорации)
// 9  name         - Наименование организации (ФИО) контрагента
// 10 sign010      - Доходы от реализации. Код строки 010. Признак (0-уменьшение, 1- увеличение)
// 11 sum010       - Доходы от реализации. Код строки 010. Сумма в рублях
// 12 sign020      - Внереализационные доходы. Код строки 020. Признак (0-уменьшение, 1- увеличение)
// 13 sum020       - Внереализационные доходы. Код строки 020. Сумма в рублях
// 14 sign030      - Расходы, уменьшающие сумму доходов от реализации. Код строки 030. Признак (0-уменьшение, 1- увеличение)
// 15 sum030       - Расходы, уменьшающие сумму доходов от реализации. Код строки 030. Сумма в рублях
// 16 sign040      - Внереализационные расходы. Код строки 040. Признак (0-уменьшение, 1- увеличение)
// 17 sum040       - Внереализационные расходы. Код строки 040. Сумма в рублях
// 18 sign050      - Итого сумма корректировки (сумма строк 010,020,030,040) по модулю. Код строки 050. Признак (0-уменьшение, 1- увеличение)
// 19 sum050       - Итого сумма корректировки (сумма строк 010,020,030,040) по модулю. Код строки 050. Сумма в рублях
// 20 sign060      - Доходы от выбытия, в том числе доход от погашения. Код строки 060. Признак (0-уменьшение, 1- увеличение)
// 21 sum060       - Доходы от выбытия, в том числе доход от погашения. Код строки 060. Сумма в рублях
// 22 sign070      - Расходы, связанные с приобретением и реализацией или иным выбытием (в том числе, погашением). Код строки 070. Признак (0-уменьшение, 1- увеличение)
// 23 sum070       - Расходы, связанные с приобретением и реализацией или иным выбытием (в том числе, погашением). Код строки 070. Сумма в рублях
// 24 sign080      - Итого сумма корректировки (сумма строк 060, 070) по модулю. Код строки 080. Признак (0-уменьшение, 1- увеличение)
// 25 sum080       - Итого сумма корректировки (сумма строк 060, 070) по модулю. Код строки 080. Сумма в рублях
// 26 sign100      - Сумма налога, подлежащая исчислению, исходя из итоговой суммы корректировки по строкам 050, 080 и соответствующей налоговой ставки. Код строки 100. Признак (0-уменьшение, 1- увеличение)
// 27 sum100       - Сумма налога, подлежащая исчислению, исходя из итоговой суммы корректировки по строкам 050, 080 и соответствующей налоговой ставки. Код строки 100. Сумма в рублях

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        consolidation()
        calc()
        formDataService.saveCachedDataRows(formData, logger)
        break
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def allColumns = ['number', 'rnu', 'code', 'corrType', 'base', 'countryCode', 'innKio', 'rsk', 'name',
                  'sign010', 'sum010', 'sign020', 'sum020', 'sign030', 'sum030', 'sign040', 'sum040',
                  'sign050', 'sum050', 'sign060', 'sum060', 'sign070', 'sum070', 'sign080', 'sum080',
                  'sign100', 'sum100']

// Редактируемые атрибуты
@Field
def editableColumns = ['base']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rnu', 'code', 'corrType', /*'base', */'countryCode', 'innKio', 'rsk', 'name',
                       'sign010', 'sum010', 'sign020', 'sum020', 'sign030', 'sum030', 'sign040', 'sum040',
                       'sign050', 'sum050', 'sign060', 'sum060', 'sign070', 'sum070', 'sign080', 'sum080',
                       'sign100', 'sum100']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['corrType', 'base', 'name']

@Field
def sortColumns = ['innKio', 'rsk']

@Field
def nonZeroColumns = ['sum010', 'sum020', 'sum030', 'sum040']

@Field
def totalColumns = ['sum010', 'sum020', 'sum030', 'sum040', 'sum050', 'sum100']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value, null,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов)
def Long getRecordId(def Long refBookId, def String alias, def String value) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, null,
            getReportPeriodEndDate(), -1, null, logger, true)
}

@Field
def foreignCodeId

def getForeignOrgCodeId() {
    if (foreignCodeId == null) {
        foreignCodeId = getRecordId(513, 'CODE', '2') // число, не строка
    }
    return foreignCodeId
}

@Field
def russianCodeId

def getRussianOrgCodeId() {
    if (russianCodeId == null) {
        russianCodeId = getRecordId(513, 'CODE', '1') // число, не строка
    }
    return russianCodeId
}

@Field
def corrCodeId

def getCorrectionCodeId() {
    if (corrCodeId == null) {
        corrCodeId = getRecordId(540, 'CODE', '1') // число, не строка
    }
    return corrCodeId
}

@Field
def vzlId

def getVzlTypeId() {
    if (vzlId == null) {
        vzlId = getRecordId(525, 'CODE', 'ВЗЛ')
    }
    return vzlId
}

@Field
def nlId

def getNlTypeId() {
    if (nlId == null) {
        nlId = getRecordId(525, 'CODE', 'НЛ')
    }
    return nlId
}

@Field
def rozId

def getRozTypeId() {
    if (rozId == null) {
        rozId = getRecordId(525, 'CODE', 'РОЗ')
    }
    return rozId
}

@Field
def taxStatus1Id

def getTaxStatus1() {
    if (taxStatus1Id == null) {
        taxStatus1Id = getRecordId(511, 'CODE', '1')
    }
    return taxStatus1Id
}

@Field
def taxStatus2Id

def getTaxStatus2() {
    if (taxStatus2Id == null) {
        taxStatus2Id = getRecordId(511, 'CODE', '2')
    }
    return taxStatus2Id
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def rowNum = row.getIndex()

        // 1. Проверка на заполнение граф
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)
        def record520 = getRefBookValue(520L, row.name)
        // 2. Заполнение идентификационного номера для иностранной организации
        if (row.name != null && record520.ORG_CODE.value == getForeignOrgCodeId() && !record520.INNKIO.value && !record520.RSK.value) {
            logger.error("Строка %s: Для иностранной организации должна быть заполнена хотя бы одна из граф: «%s», «%s»!",
                row.getIndex(), getColumnName(row, 'innKio'), getColumnName(row, 'rsk'))
        }
        // 3. Заполнение ИНН для российской организации
        if (row.name != null && record520.ORG_CODE.value == getRussianOrgCodeId() && !record520.INNKIO.value) {
            logger.error("Строка %s: Графа «%s» для российской организации обязательна к заполнению!",
                    row.getIndex(), getColumnName(row, 'innKio'))
        }
        if (row.base != null) {
            def baseCodes = ((String) row.base).replace(' ', '').split(';')
            // 4. Уникальные основания отнесения сделки к контролируемой
            Set<String> codesSet = new HashSet<String>()
            // ищем повторения
            if (baseCodes.find { !codesSet.add(it) } != null) {
                logger.error("Строка %s: Значения в графе «%s» не должны повторяться!", row.getIndex(), getColumnName(row, 'base'))
            }
            // 5. Корректное заполнение основания отнесения сделки к контролируемой
            if ((baseCodes.contains('122') || baseCodes.contains('123')) && baseCodes.find { it && ((it as Integer) in (131..135)) } != null) {
                logger.error("Строка %s: Коды 122 и 123 не могут быть одновременно указаны с любым из кодов 131- 135!", row.getIndex())
            }
            // Проверка наличия кодов в справочнике "Коды основания отнесения сделки к контролируемой"
            def allRecords = getAllRecords(541).values()
            def absentCodes = baseCodes.findAll { code ->
                code && (allRecords.find { record ->
                    record.CODE.value == (code as Integer)
                } == null)
            }
            if (!absentCodes.isEmpty()) {
                logger.error("Строка %s: Записи с кодами «%s» отсутствуют в справочнике «Коды основания отнесения сделки к контролируемой»!",
                    row.getIndex(), absentCodes.join('», «'))
            }
        }
        // 6. Проверка сумм доходов и расходов
        nonZeroColumns.each { column ->
            if (row[column] != null && row[column] <= 0) {
                logger.error("Строка %s: Значение графы «%s» должно быть больше 0!", row.getIndex(), getColumnName(row, column))
            }
        }
    }
    // 7. Проверка итоговых значений по фиксированной строке «КНУ»
    for(int section = 0; section < formTypeIds.size(); section++) {
        // alias = 'rnu' + (section + 1) для строк РНУ-1хх
        // alias = 'code' + (section + 1) + '_1'/'_2" для строк с КНУ
        // перебираем строки с КНУ из шаблона
        for (codeIndex in [1, 2]) {
            def codeHeaderRow = dataRows.find {
                (('code' + (section + 1) + '_' + codeIndex) == it.getAlias())
            }
            if (codeHeaderRow == null) { // в некоторых группах по одной подгруппе
                continue
            }
            def code = codeHeaderRow.code
            def sectionRows = dataRows.findAll { row ->
                 row.code == code
            }
            // получаем итоговые графы для КНУ
            def totalColumns = codeTotalColumnMap[code]
            def tempCodeHeaderRow =  formData.createDataRow()
            calcTotalSum(sectionRows, tempCodeHeaderRow, totalColumns)
            totalColumns.each { column ->
                if ((codeHeaderRow[column] || tempCodeHeaderRow[column]) && codeHeaderRow[column] != tempCodeHeaderRow[column]) {
                    logger.error("Неверное итоговое значение в графе «%s» по группе КНУ «%s»!",
                        getColumnName(codeHeaderRow, column), code)
                }
            }
        }
    }
}

@Field
def allRecordsMap = [:]

/**
 * Получить все записи справочника.
 *
 * @param refBookId id справочника
 * @return мапа с записями справочника (ключ "id записи" -> запись)
 */
def getAllRecords(def refBookId) {
    if (allRecordsMap[refBookId] == null) {
        def date = getReportPeriodEndDate()
        def provider = formDataService.getRefBookProvider(refBookFactory, refBookId, providerCache)
        List<Long> uniqueRecordIds = provider.getUniqueRecordIds(date, null)
        allRecordsMap[refBookId] = provider.getRecordData(uniqueRecordIds)
    }
    return allRecordsMap[refBookId]
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }
    def totalRow = getDataRow(dataRows, 'total')
    def codeRows = dataRows.findAll { it.getAlias() != null && it.getAlias().startsWith("code") }
    totalColumns.each { alias ->
        totalRow[alias] = codeRows.sum { it[alias] ?: BigDecimal.ZERO }
    }
}

@Field
def formType101 = 818

@Field
def formType102 = 820

@Field
def formType107 = 821

@Field
def formType110 = 822

@Field
def formType111 = 808

@Field
def formType112 = 824

@Field
def formType114 = 829

@Field
def formType115 = 842

@Field
def formType116 = 844

@Field
def formType117 = 809

@Field
def formType122 = 840

@Field
def formType123 = 841

@Field
def formType171 = 843

// порядок важен, т.к. совпадает со строками
@Field
def formTypeIds = [formType101, formType102, formType107, formType110, formType111, formType112, formType114, formType115, formType116,
                   formType117, formType122, formType123, formType171]

@Field
def codeTotalColumnMap = ['19000' : ['sum010', 'sum050', 'sum100'],
                           '19030' : ['sum020', 'sum050', 'sum100'],
                           '19360' : ['sum030', 'sum050', 'sum100'],
                           '19390' : ['sum040', 'sum050', 'sum100'],
                           '19060' : ['sum010', 'sum050', 'sum100'],
                           '19090' : ['sum020', 'sum050', 'sum100'],
                           '19120' : ['sum020', 'sum050', 'sum100'],
                           '19150' : ['sum020', 'sum050', 'sum100'],
                           '19180' : ['sum020', 'sum050', 'sum100'],
                           '19210' : ['sum020', 'sum050', 'sum100'],
                           '19240' : ['sum020', 'sum050', 'sum100'],
                           '19420' : ['sum040', 'sum050', 'sum100'],
                           '19270' : ['sum020', 'sum050', 'sum100'],
                           '19450' : ['sum040', 'sum050', 'sum100'],
                           '19480' : ['sum040', 'sum050', 'sum100'],
                           '19300' : ['sum010', 'sum050', 'sum100'],
                           '19510' : ['sum030', 'sum050', 'sum100'],
                           '19330' : ['sum010', 'sum050', 'sum100'],
                           '19540' : ['sum010', 'sum050', 'sum100'],
                           '19570' : ['sum030', 'sum050', 'sum100']
]

void consolidation() {
    // получить строки из шаблона
    def formTemplate = formDataService.getFormTemplate(formData.formTemplateId)
    def dataRows = formTemplate.rows
    updateIndexes(dataRows)
    List<Relation> sourcesInfo = formDataService.getSourcesInfo(formData, false, true, WorkflowState.ACCEPTED, userInfo, logger)
    def sourceMap = [:] // map [formTypeId : [кну : [record520Id : dataRows]]]
    for (Relation sourceInfo in sourcesInfo) {
        def sourceFormData = formDataService.get(sourceInfo.formDataId, null)
        def sourceType = sourceInfo.formType.id
        if (!formTypeIds.contains(sourceType)) {
            continue
        }
        if (sourceMap[sourceType] == null) {
            sourceMap[sourceType] = [:]
        }
        def sourceRows = formDataService.getDataRowHelper(sourceFormData).allSaved
        for (sourceRow in sourceRows) {
            if (sourceRow.getAlias() != null) {
                continue
            }
            def code = getCodeValue(sourceType, sourceRow)
            if (code == null) {
                continue
            }
            if (sourceMap[sourceType][code] == null) {
                sourceMap[sourceType][code] = [:]
            }
            def record520Id = sourceRow.name
            if (sourceMap[sourceType][code][record520Id] == null) {
                sourceMap[sourceType][code][record520Id] = []
            }
            sourceMap[sourceType][code][record520Id].add(sourceRow)
        }
    }

    // проходим по РНУ
    for(int section = 0; section < formTypeIds.size(); section++) {
        def formTypeId = formTypeIds[section]
        // alias = 'rnu' + (section + 1) для строк РНУ-1хх
        // alias = 'code' + (section + 1) + '_1'/'_2" для строк с КНУ
        def codeMap = sourceMap[formTypeId] // map [кну : [record520Id : dataRows]]
        if (codeMap == null) {
            continue
        }
        // перебираем строки с КНУ из шаблона
        for (codeIndex in [1, 2]) {
            def codeHeaderRow = dataRows.find {
                (('code' + (section + 1) + '_' + codeIndex) == it.getAlias())
            }
            if (codeHeaderRow == null) {
                continue
            }
            def code = codeHeaderRow.code
            // чтобы не искать для лишних кодов
            def recordIdRowsMap = codeMap[code]
            if (recordIdRowsMap == null || recordIdRowsMap.isEmpty()) {
                continue
            }
            def newRows = []
            recordIdRowsMap.each { recordId, rows ->
                // общая часть
                def newRow = getNewRow(code, recordId)
                // различия
                fillNewRow(newRow, formTypeId, code, rows)
                newRows.add(newRow)
            }
            // сортируем
            sortRows(newRows, sortColumns)
            // получаем итоговые графы для КНУ
            def totalColumns = codeTotalColumnMap[code]
            calcTotalSum(newRows, codeHeaderRow, totalColumns)
            // добавляем строки после строки с кодами
            dataRows.addAll(codeHeaderRow.getIndex(), newRows)
            updateIndexes(dataRows)
        }
    }
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

// общая часть
def getNewRow(def code, def recordId) {
    def row = formData.createDataRow()
    editableColumns.each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        row.getCell(it).setStyleAlias('Автозаполняемая')
    }
    // Графа 3 = значение графы 3 фиксированной строки «КНУ», к которой относится нефиксированная строка
    row.code = code
    // Графа 4 = значение из справочника «Вид корректировки», где «Код» = 1
    row.corrType = getCorrectionCodeId()
    // Графа 9
    row.name = recordId
    def record520 = getRefBookValue(520, row.name)
    // Графа 5
    def type = record520.TYPE.value
    if (type == getVzlTypeId()) {
        def taxStatus = record520.TAX_STATUS.value
        if (taxStatus == getTaxStatus1()) {
            row.base = "121;134;"
        } else if (taxStatus == getTaxStatus2()) {
            row.base = "121;131;"
        }
    } else if (type == getNlTypeId()) {
        row.base = '121;'
    } else if (type == getRozTypeId()) {
        row.base = '123;'
    }
    // Графа 6, 7, 8 автоматом заполняются
    return row
}

// заполняются в зависмости от источника
void fillNewRow(def newRow, def formTypeId, def code, def rows) {
    switch(formTypeId) {
        case formType101 :
            fillRow101(newRow, code, rows, 'sum3')
            break
        case formType102 :
            fillRow102(newRow, code, rows, 'sum3')
            break
        case formType107 :
            fillRow107(newRow, code, rows, 'sum4')
            break
        case formType110 :
            fillRowIncome(newRow, rows, 'sum3') // строки уже отфильтрованы по коду из шаблона
            break
        case formType111 :
            fillRowIncome(newRow, rows, 'sum3') // строки уже отфильтрованы по коду из шаблона
            break
        case formType112 :
            fillRowIncome(newRow, rows, 'incomeCorrection') // строки уже отфильтрованы по коду из шаблона
            break
        case formType114 :
            fillRowIncome(newRow, rows, 'sum1') // строки уже отфильтрованы по коду из шаблона
            break
        case formType115 :
            fillRow115(newRow, code, rows)
            break
        case formType116 :
            fillRow116(newRow, code, rows)
            break
        case formType117 :
            fillRow117(newRow, rows, 'sum3') // строки уже отфильтрованы по коду из шаблона
            break
        case formType122 :
            fillRow122(newRow, code, rows, 'sum6')
            break
        case formType123 :
            fillRow123(newRow, rows, 'sum10') // строки уже отфильтрованы по коду из шаблона
            break
        case formType171 :
            fillRow171(newRow, code, rows, 'incomeCorrection')
            break
    }
    newRow.sign100 = newRow.sign050
    newRow.sum100 = new BigDecimal("0.2").multiply(newRow.sum050).setScale(0, RoundingMode.HALF_UP)
}

void fillRow101(def newRow, def code, def rows, def alias) {
    def signAlias
    def sumAlias
    if (code == '19000') {
        signAlias = 'sign010'
        sumAlias = 'sum010'
    } else if (code == '19030') {
        signAlias = 'sign020'
        sumAlias = 'sum020'
    }
    newRow.sign050 = newRow[signAlias] = 1
    newRow.sum050 = newRow[sumAlias] = rows.sum { it[alias] } ?: BigDecimal.ZERO
}

void fillRow102(def newRow, def code, def rows, def alias) {
    def signAlias
    def sumAlias
    if (code == '19360') {
        signAlias = 'sign030'
        sumAlias = 'sum030'
    } else if (code == '19390') {
        signAlias = 'sign040'
        sumAlias = 'sum040'
    }
    newRow.sign050 = newRow[signAlias] = 0
    newRow.sum050 = newRow[sumAlias] = rows.sum { it[alias] } ?: BigDecimal.ZERO
}

void fillRow107(def newRow, def code, def rows, def alias) {
    def signAlias
    def sumAlias
    if (code == '19060') {
        signAlias = 'sign010'
        sumAlias = 'sum010'
    } else if (code == '19090') {
        signAlias = 'sign020'
        sumAlias = 'sum020'
    }
    newRow.sign050 = newRow[signAlias] = 1
    newRow.sum050 = newRow[sumAlias] = rows.sum { it[alias] } ?: BigDecimal.ZERO
}

void fillRowIncome(def newRow, def rows, def alias) {
    newRow.sign050 = newRow.sign020 = 1
    newRow.sum050 = newRow.sum020 = rows.sum { it[alias] }
}

void fillRow115(def newRow, def code, def rows) {
    if (code == '19240') {
        newRow.sign050 = newRow.sign020 = 1
        newRow.sum050 = newRow.sum020 = rows.sum { it.incomeDelta } ?: BigDecimal.ZERO
    } else if (code == '19420') {
        newRow.sign050 = newRow.sign040 = 0
        newRow.sum050 = newRow.sum040 = rows.sum { it.outcomeDelta } ?: BigDecimal.ZERO
    }
}

void fillRow116(def newRow, def code, def rows) {
    if (code == '19270') {
        newRow.sign050 = newRow.sign020 = 1
        newRow.sum050 = newRow.sum020 = rows.sum { it.incomeDelta } ?: BigDecimal.ZERO
    } else if (code == '19450') {
        newRow.sign050 = newRow.sign040 = 0
        newRow.sum050 = newRow.sum040 = rows.sum { it.outcomeDelta } ?: BigDecimal.ZERO
    }
}

void fillRow117(def newRow, def rows, def alias) {
    newRow.sign050 = newRow.sign040 = 0
    newRow.sum050 = newRow.sum040 = rows.sum { it[alias] } ?: BigDecimal.ZERO
}

void fillRow122(def newRow, def code, def rows, def alias) {
    if (code == '19300') {
        newRow.sign050 = newRow.sign010 = 1
        newRow.sum050 = newRow.sum010 = rows.sum { it[alias] } ?: BigDecimal.ZERO
    } else if (code == '19510') {
        newRow.sign050 = newRow.sign030 = 0
        newRow.sum050 = newRow.sum030 = rows.sum { it[alias] } ?: BigDecimal.ZERO
    }
}

void fillRow123(def newRow, def rows, def alias) {
    newRow.sign050 = newRow.sign010 = 1
    newRow.sum050 = newRow.sum010 = rows.sum { it[alias] } ?: BigDecimal.ZERO
}

void fillRow171(def newRow, def code, def rows, def alias) {
    if (code == '19540') {
        newRow.sign050 = newRow.sign010 = 1
        newRow.sum050 = newRow.sum010 = rows.sum { it[alias] } ?: BigDecimal.ZERO
    } else if (code == '19570') {
        newRow.sign050 = newRow.sign030 = 0
        newRow.sum050 = newRow.sum030 = rows.sum { it[alias] } ?: BigDecimal.ZERO
    }
}

def getCodeValue(def sourceType, def sourceRow) {
    switch(sourceType) {
        case formType101:
            return sourceRow.incomeCode
        case formType102:
            return sourceRow.outcomeCode
        case formType107:
        case formType110:
        case formType111:
        case formType112:
        case formType114:
        case formType117:
        case formType122:
        case formType123:
        case formType171:
            return sourceRow.code
        case formType115:
            if (sourceRow.incomeDelta != null) {
                return '19240'
            } else if (sourceRow.outcomeDelta != null){
                return '19420'
            }
            break
        case formType116:
            if (sourceRow.incomeDelta != null) {
                return '19270'
            } else if (sourceRow.outcomeDelta != null){
                return '19450'
            }
            break
    }
}