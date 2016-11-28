package form_template.transport.vehicles.v2016

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import groovy.transform.Field

/**
 * Сведения о транспортных средствах, по которым уплачивается транспортный налог.
 *
 * formTypeId = 201
 * formTemplateId = 3201
 */

// графа 1  - rowNumber
// графа 2  - codeOKATO         - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
// графа 3  - regionName        - зависит от графы 2 - атрибут 841 - NAME - «Наименование», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
// графа 4  - tsTypeCode        - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
// графа 5  - tsType            - зависит от графы 4 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
// графа 6  - model
// графа 7  - ecoClass          - атрибут 400 - CODE - «Код экологического класса», справочник 40 «Экологические классы»
// графа 8  - identNumber
// графа 9  - regNumber
// графа 10 - regDate
// графа 11 - regDateEnd
// графа 12 - month
// графа 13 - taxBase
// графа 14 - baseUnit          - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
// графа 15 - year
// графа 16 - pastYear
// графа 17 - stealDateStart
// графа 18 - stealDateEnd
// графа 19 - share
// графа 20 - costOnPeriodBegin
// графа 21 - costOnPeriodEnd
// графа 22 - version           - атрибут 2183 - MODEL - «Модель (версия)», справочник 218 «Средняя стоимость транспортных средств»
// графа 23 - averageCost       - зависит от графы 22 - атрибут 2181.2111 - AVG_COST.NAME - «Средняя стоимость».«Наименование», справочник 218 «Средняя стоимость транспортных средств»
// графа 24 - deductionCode     - атрибут 15 - CODE - «Код налоговой льготы», справочник 6 «Коды налоговых льгот транспортного налога»
// графа 25 - deduction

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        if (formData.kind == FormDataKind.PRIMARY) {
            copyFromPrevForm()
            formDataService.saveCachedDataRows(formData, logger)
        }
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
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
def allColumns = ['rowNumber', 'codeOKATO', 'regionName', 'tsTypeCode', 'tsType', 'model',
        'ecoClass', 'identNumber', 'regNumber', 'regDate', 'regDateEnd', 'month', 'taxBase',
        'baseUnit', 'year', 'pastYear', 'stealDateStart', 'stealDateEnd', 'share', 'costOnPeriodBegin',
        'costOnPeriodEnd', 'version', 'averageCost', 'deductionCode', 'deduction']

// графа 2, 4, 6..11, 13..15, 17..22, 24, 25
@Field
def editableColumns = ['codeOKATO', 'tsTypeCode', 'model', 'ecoClass', 'identNumber', 'regNumber',
        'regDate', 'regDateEnd', 'taxBase', 'baseUnit', 'year', 'stealDateStart', 'stealDateEnd',
        'share', 'costOnPeriodBegin', 'costOnPeriodEnd', 'version', 'deductionCode', 'deduction']

// Автозаполняемые атрибуты (все кроме редактируемых)
@Field
def autoFillColumns = allColumns - editableColumns

// графа 1..6, 8..10, 12..16, 19,
@Field
def nonEmptyColumns = ['codeOKATO', /* 'regionName', */ 'tsTypeCode', /* 'tsType', */ 'model', 'identNumber',
        'regNumber', 'regDate', 'month', 'taxBase', 'baseUnit', 'year', 'pastYear', 'share']

// графа 2 и остальные
@Field
def sortColumns = ['codeOKATO'] + (allColumns - 'codeOKATO')

// дата начала отчетного периода
@Field
def start = null

// дата окончания отчетного периода
@Field
def endDate = null

// отчетный период формы
@Field
def currentReportPeriod = null

@Field
def copyColumns = allColumns - 'rowNumber'

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(Long refBookId, String alias, String value, int rowIndex, int colIndex,
                      boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value, null,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(Long refBookId, String alias, String value, int rowIndex, int colIndex,
                    boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value, null,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

//// Кастомные методы

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    for (def row : dataRows) {
        // графа 12
        row.month = calc12(row)

        // графа 16
        row.pastYear = calc16(row)

        // убираем стиль "Ошибка" со строки если графу 22 заполнили
        repaintRow(row)
    }
}

// убираем стиль "Ошибка" если графу 22 заполнили
void repaintRow(def row) {
    if (row.version != null && row.getCell("version").styleAlias == "Ошибка") {
        editableColumns.each {
            row.getCell(it).styleAlias = 'Редактируемая'
        }
        autoFillColumns.each {
            row.getCell(it).styleAlias = 'Автозаполняемая'
        }
    }
}

def calc12(def row) {
    def tmp1 = subCalc12(row, 'regDate', 'regDateEnd')
    if (tmp1 == null || tmp1 == BigDecimal.ZERO) {
        return tmp1
    }
    def tmp2 = subCalc12(row, 'stealDateStart', 'stealDateEnd', true)
    if (tmp2 == null) {
        tmp2 = BigDecimal.ZERO
    }
    return round(tmp1 - tmp2, 0)
}

/**
 * Для расчета графы 12.
 *
 * @param row строка нф
 * @param startAlias алиас даты начала
 * @param endAlias алиас даты окончания
 * @param isSteal true - расчет месяцев угона, false - расчет месяцев владения
 */
def subCalc12(def row, def startAlias, def endAlias, def isSteal = false) {
    // общая часть
    if (row[startAlias] == null) {
        return null
    }
    if (row[endAlias] && row[endAlias] < getReportPeriodStartDate() || row[startAlias] > getReportPeriodEndDate()) {
        return round(BigDecimal.ZERO, 0)
    }
    def end = (row[endAlias] == null || row[endAlias] > getReportPeriodEndDate() ? getReportPeriodEndDate() : row[endAlias])
    def start = (row[startAlias] < getReportPeriodStartDate() ? getReportPeriodStartDate() : row[startAlias])
    // специфика
    BigDecimal tmp
    if (isSteal) {
        tmp = end.format('M').toInteger() - start.format('M').toInteger() - 1
        if (tmp < 0) {
            tmp = BigDecimal.ZERO
        }
    } else {
        def m1 = start.format('M').toInteger() + (start.format('d').toInteger() > 15 ? 1 : 0)
        def m2 = end.format('M').toInteger() - (end.format('d').toInteger() > 15 ? 0 : 1)
        tmp = m2 - m1 + 1
    }
    return round(tmp, 0)
}

def calc16(def row) {
    if (row.year == null) {
        return null
    }
    def currentYear = getReportPeriod()?.taxPeriod?.year
    BigDecimal tmp = currentYear - row.year.format('yyyy')?.toInteger()
    if (row.version) {
        def a = tmp + 1
        def record209 = getRecord209(row.version, a)
        tmp = (record209 ? a : tmp)
    }
    return round(tmp, 0)
}

/**
 * Получить запись справочника «Повышающие коэффициенты транспортного налога» (209)
 * по записи справочника «Средняя стоимость транспортных средств» (218) и по количеству лет ТС.
 *
 * @param record218Id id на запись справочника 218
 * @param year количество лет ТС
 */
def getRecord209(def record218Id, def year) {
    if (record218Id == null) {
        return null
    }
    def record218 = getAllRecords(218L)?.get(record218Id)
    def record209 = getAllRecords(209L)?.values()?.find {
        it?.AVG_COST?.value == record218?.AVG_COST?.value && it?.YEAR_FROM?.value < year && year <= it?.YEAR_TO?.value
    }
    return record209
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

def logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def reportPeriod = getReportPeriod()
    def year = reportPeriod.taxPeriod.year
    def startYearDate = Date.parse('dd.MM.yyyy', '01.01.' + year)
    def isCalc = (formDataEvent == FormDataEvent.CALCULATE)

    // для логической проверки 2
    def equalsRowsMap = [:]
    // для логической проверки 6
    def needValue = [:]
    // для логической проверки 15
    def identRowsMap = [identNumber: [:], regNumber: [:]]

    for (def row : dataRows) {
        def index = row.getIndex()

        // 1. Проверка заполнения обязательных граф
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на наличие в форме строк с одинаковым значением граф 2, 4, 8, 9, 13, 14
        // сбор данных
        def columnsForEquals = ['codeOKATO', 'tsTypeCode', 'identNumber', 'regNumber', 'taxBase', 'baseUnit']
        def keyValues = columnsForEquals.collect { row[it] }
        def key = keyValues.join('#')
        if (equalsRowsMap[key] == null) {
            equalsRowsMap[key] = []
        }
        equalsRowsMap[key].add(row)

        // 3. Проверка корректности заполнения даты регистрации ТС
        if (row.regDate && row.regDate > getReportPeriodEndDate()) {
            def columnName10 = getColumnName(row, 'regDate')
            def dateInStr = getReportPeriodEndDate().format('dd.MM.yyyy')
            logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно %s", index, columnName10, dateInStr)
        }

        // 4. Проверка корректности заполнения даты снятия с регистрации ТС
        if (row.regDateEnd && (row.regDateEnd < startYearDate || row.regDate && row.regDateEnd < row.regDate)) {
            def columnName11 = getColumnName(row, 'regDateEnd')
            def dateInStr = startYearDate?.format('dd.MM.yyyy')
            def columnName10 = getColumnName(row, 'regDate')
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно %s, и больше либо равно значению графы «%s»",
                    index, columnName11, dateInStr, columnName10)
        }

        // 5. Проверка корректности заполнения года выпуска ТС
        if (row.year && row.year?.format('yyyy')?.toInteger() > year) {
            def columnName15 = getColumnName(row, 'year')
            logger.error("Строка %s: Значение графы «%s» должно быть меньше либо равно «%s»", index, columnName15, year)
        }

        // 6. Проверка корректности заполнения расчетных граф 12, 16
        if (!isCalc) {
            needValue.month = calc12(row)
            needValue.pastYear = calc16(row)
            def errorColumns = []
            for (def alias : needValue.keySet().toList()) {
                if (needValue[alias] == null && row[alias] == null) {
                    continue
                }
                if (needValue[alias] == null || row[alias] == null || needValue[alias].compareTo(row[alias]) != 0) {
                    errorColumns.add(getColumnName(row, alias))
                }
            }
            if (!errorColumns.isEmpty()) {
                def columnNames = errorColumns.join('», «')
                logger.warn("Строка %s: Графы «%s» заполнены неверно. Выполните расчет формы", index, columnNames)
            }
        }

        // 7. Проверка заполнения даты начала розыска ТС при указании даты возврата ТС
        if (row.stealDateEnd && row.stealDateStart == null) {
            def columnName17 = getColumnName(row, 'stealDateStart')
            def columnName18 = getColumnName(row, 'stealDateEnd')
            logger.error("Строка %s: Графа «%s» должна быть заполнена, если заполнена графа «%s»",
                    index, columnName17, columnName18)
        }

        // 8. Проверка корректности заполнения даты начала розыска ТС
        if (row.stealDateStart && row.regDate && row.stealDateStart < row.regDate) {
            def columnName17 = getColumnName(row, 'stealDateStart')
            def columnName10 = getColumnName(row, 'regDate')
            logger.error("Строка %s: Графа «%s» должна быть больше либо равна значению графы «%s»",
                    index, columnName17, columnName10)
        }

        // 9. Проверка корректности заполнения даты возврата ТС
        if (row.stealDateStart && row.stealDateEnd && (row.stealDateStart > row.stealDateEnd || row.stealDateEnd < startYearDate)) {
            def columnName18 = getColumnName(row, 'stealDateEnd')
            def columnName17 = getColumnName(row, 'stealDateStart')
            def dateInStr = startYearDate?.format('dd.MM.yyyy')
            logger.error("Строка %s: Значение графы «%s» должно быть больше либо равно значению графы «%s» и больше либо равно «%s»",
                    index, columnName18, columnName17, dateInStr)
        }

        // 10. Проверка заполнения даты возврата ТС
        if (row.regDateEnd && row.stealDateStart && row.stealDateEnd && row.regDateEnd < row.stealDateEnd) {
            def columnName18 = getColumnName(row, 'stealDateEnd')
            def columnName11 = getColumnName(row, 'regDateEnd')
            def dateInStr = startYearDate?.format('dd.MM.yyyy')
            logger.error("Строка %s: Значение графы «%s» должно быть заполнено и должно быть меньше либо равно значению графы «%s» и больше либо равно «%s»",
                    index, columnName18, columnName11, dateInStr)
        }

        // 11. Проверка корректности заполнения доли налогоплательщика в праве на ТС
        if (row.share != null) {
            def partArray = row.share.split('/')
            if (!(row.share ==~ /\d{1,10}\/\d{1,10}/) ||
                    partArray[0].toString().startsWith('0') ||
                    partArray[1].toString().startsWith('0') ||
                    partArray[0].toBigDecimal() > partArray[1].toBigDecimal()) {
                def columnName19 = getColumnName(row, 'share')
                logger.error("Строка %s: Графа «%s» должна быть заполнена согласно формату: " +
                        "«(от 1 до 10 числовых знаков)/(от 1 до 10 числовых знаков)», " +
                        "числитель должен быть меньше либо равен знаменателю, " +
                        "числитель и знаменатель не должны быть равны нулю",
                        index, columnName19)
            }
        }

        // 12. Проверка наличия формы предыдущего периода в состоянии «Принята»
        // Выполняется в методе copyFromPrevForm()

        // 13. Проверка единицы измерения для наземных видов ТС
        if (row.tsTypeCode && row.baseUnit &&
                getRefBookValue(42L, row.tsTypeCode)?.CODE?.value?.startsWith('5') &&
                getRefBookValue(12L, row.baseUnit)?.CODE?.value == '214') {
            def columnName13 = getColumnName(row, 'taxBase')
            def columnName14 = getColumnName(row, 'baseUnit')
            logger.warn("Строка %s: Значение графы «%s», указанное в киловаттах должно быть переведено в лошадиные силы и значение графы «%s» должно быть равно «251»",
                    index, columnName13, columnName14)
        }

        // 14. Проверка наличия на форме строк, к которым применен стиль «Ошибка»
        if (!isCalc && row.getCell("version").styleAlias == "Ошибка") {
            def columnName22 = getColumnName(row, 'version')
            logger.error("Строка %s: Графа «%s» должна быть заполнена", index, columnName22)
        }

        // 15. Проверка на наличие в форме строк с одинаковым значением граф 8, 9 и пересекающимися периодами владения
        // сбор данных
        def columnsIdent = ['identNumber', 'regNumber']
        for (alias in columnsIdent) {
            def indentKey = row[alias]
            if (row.regDate == null || !indentKey) {
                continue
            }
            if (identRowsMap[alias][indentKey] == null) {
                identRowsMap[alias][indentKey] = []
            }
            identRowsMap[alias][indentKey].add(row)
        }

        // 16. Проверка одновременного заполнения данных о налоговом вычете
        if (row.deductionCode != null && row.deduction == null || row.deductionCode == null && row.deduction != null) {
            logger.error("Строка %s: Данные о налоговом вычете указаны не полностью", index)
        }
    }

    // 2. Проверка на наличие в форме строк с одинаковым значением граф 2, 4, 8, 12, 13, 14
    for (def key : equalsRowsMap.keySet().toList()) {
        def rows = equalsRowsMap[key]
        if (rows.size() < 2) {
            continue
        }
        def hasCross = checkHasCross(rows)
        def indexes = rows?.collect { it.getIndex() }
        def indexesInStr = indexes?.join(', ')
        if (hasCross) {
            def row = rows[0]
            def value2 = getRefBookValue(96, row.codeOKATO)?.CODE?.value ?: ''
            def value4 = getRefBookValue(42, row.tsTypeCode)?.CODE?.value ?: ''
            def value8 = row.identNumber ?: ''
            def value9 = row.regNumber ?: ''
            def value13 = row.taxBase ?: ''
            def value14 = getRefBookValue(12, row.baseUnit)?.CODE?.value ?: ''
            logger.error("Строки %s: Код ОКТМО «%s», Код вида ТС «%s», Идентификационный номер ТС «%s», " +
                    "Регистрационный знак «%s», Налоговая база «%s», Единица измерения налоговой базы по ОКЕИ «%s»: " +
                    "на форме не должно быть строк с одинаковым кодом ОКТМО, кодом вида ТС, идентификационным номером ТС, " +
                    "регистрационным знаком ТС, налоговой базой, единицей измерения налоговой базы по ОКЕИ и пересекающимися периодами владения ТС",
                    indexesInStr, value2, value4, value8, value9, value13, value14)
        } else {
            logger.warn("Строки %s: На форме присутствуют несколько строк по одному ТС. Проверьте периоды регистрации ТС", indexesInStr)
        }
    }

    // 15. Проверка на наличие в форме строк с одинаковым значением граф 8 (9) и пересекающимися периодами владения
    identRowsMap.keySet().toList().each { alias ->
        def identMap = identRowsMap[alias]
        for (def key : identMap.keySet().toList()) {
            def rows = identMap[key]
            if (rows.size() < 2) {
                continue
            }
            def hasCross = checkHasCross(rows)
            if (hasCross) {
                def indexes = rows?.collect { it.getIndex() }
                def indexesInStr = indexes?.join(', ')
                def row = rows[0]
                def value2 = getColumnName(row, alias)
                def value3 = row[alias]
                logger.error("Строки %s: На форме не должно быть строк с одинаковым значением графы «%s» («%s») и пересекающимися периодами владения ТС",
                        indexesInStr, value2, value3)
            }
        }
    }
}

// проверка на наличие пересечений
boolean checkHasCross(def rows) {
    boolean hasCross = false
    for (int i = 0; i < rows.size(); i++) {
        def row1 = rows[i]
        for (int j = i + 1; j < rows.size(); j++) {
            def row2 = rows[j]
            def start1 = row1.regDate
            def start2 = row2.regDate
            def end1 = row1.regDateEnd ?: getReportPeriodEndDate()
            def end2 = row2.regDateEnd ?: getReportPeriodEndDate()
            if (start1 <= start2 && end1 >= start2 || start2 <= start1 && end2 >= start1) {
                hasCross = true
                break
            }
        }
        if (hasCross) {
            break
        }
    }
    return hasCross
}

/** Получить строки за предыдущий отчетный период. */
def getPrevDataRows() {
    if (formData.kind == FormDataKind.CONSOLIDATED || getPrevReportPeriod()?.period == null) {
        return null
    }
    def prevFormData = formDataService.getFormDataPrev(formData)
    return (prevFormData?.state == WorkflowState.ACCEPTED ? formDataService.getDataRowHelper(prevFormData)?.allSaved : null)
}

@Field
def prevReportPeriodMap = null

/**
 * Получить предыдущий отчетный период
 *
 * @return мапа с данными предыдущего периода:
 *      period - период (может быть null, если предыдущего периода нет);
 *      periodName - название;
 *      year - год;
 */
def getPrevReportPeriod() {
    if (prevReportPeriodMap != null) {
        return prevReportPeriodMap
    }
    def reportPeriod = getReportPeriod()
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def find = false
    // предыдущий период в том же году, что и текущий, и номера периодов отличаются на единицу
    if (prevReportPeriod && reportPeriod.order > 1 && reportPeriod.order - 1 == prevReportPeriod.order &&
            reportPeriod.taxPeriod.year == prevReportPeriod.taxPeriod.year) {
        find = true
    }
    // если текущий период первый в налоговом периоде, то предыдущий период должен быть последним, и года налоговых периодов должны отличаться на единицу
    if (!find && prevReportPeriod && reportPeriod.order == 1 && prevReportPeriod.order == 4 &&
            reportPeriod.taxPeriod.year - 1 == prevReportPeriod.taxPeriod.year) {
        find = true
    }
    prevReportPeriodMap = [:]
    if (find) {
        prevReportPeriodMap.period = prevReportPeriod
        prevReportPeriodMap.periodName = prevReportPeriod.name
        prevReportPeriodMap.year = prevReportPeriod.taxPeriod.year
    } else {
        // получение названии периодов
        def filter = 'T = 1'
        def provider = formDataService.getRefBookProvider(refBookFactory, 8L, providerCache)
        def records = provider.getRecords(getReportPeriodEndDate(), null, filter, null)
        records?.sort { it?.END_DATE?.value }

        prevReportPeriodMap.period = null
        prevReportPeriodMap.periodName = records[reportPeriod.order - 2].NAME?.value
        prevReportPeriodMap.year = (reportPeriod.order == 1 ? reportPeriod.taxPeriod.year - 1 : reportPeriod.taxPeriod.year)
    }
    return prevReportPeriodMap
}

/** Копирование данных из форм предыдущего периода. */
void copyFromPrevForm() {
    // Логическая проверка 12 - нет формы предыдущего периода
    def prevDataRows = getPrevDataRows()
    if (prevDataRows == null) {
        def prevReportPeriod = getPrevReportPeriod()
        def periodName = prevReportPeriod?.periodName
        def year = prevReportPeriod?.year
        logger.warn("Данные по транспортным средствам из формы предыдущего отчетного периода не были скопированы. " +
                "В Системе отсутствует форма в состоянии «Принята» за период: «%s %s» для подразделения «%s»",
                periodName, year, formDataDepartment.name)
        return
    }

    def reportPeriod = getReportPeriod()
    def year = reportPeriod.taxPeriod.year
    def endYearDate = Date.parse('dd.MM.yyyy', '31.12.' + year)
    def startYearDate = Date.parse('dd.MM.yyyy', '01.01.' + year)
    def dataRows = []
    // 1 квартал - отбор подходящих строк
    // 2, 3, 4 квартал (год) - простое копирование всех строк
    def isFirstPeriod = (reportPeriod.order == 1)

    for (def prevRow : prevDataRows) {
        def useRow = (isFirstPeriod ? prevRow.regDate <= endYearDate && (prevRow.regDateEnd == null || prevRow.regDateEnd >= startYearDate) : true)
        if (!useRow) {
            continue
        }
        def newRow = getNewRow()
        copyColumns.each { alias ->
            newRow[alias] = prevRow[alias]
        }
        dataRows.add(newRow)
    }

    updateIndexes(dataRows)
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

// Получить новую строку с заданными стилями.
def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }
    autoFillColumns.each {
        newRow.getCell(it).styleAlias = 'Автозаполняемая'
    }
    return newRow
}

def getReportPeriodStartDate() {
    if (!start) {
        start = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return start
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getReportPeriod() {
    if (currentReportPeriod == null) {
        currentReportPeriod = reportPeriodService.get(formData.reportPeriodId)
    }
    return currentReportPeriod
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    // массовое разыменование справочных и зависимых значений
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns())
    sortRows(dataRows, sortColumns)
    dataRowHelper.saveSort()
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 25
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset': 0, 'colOffset': 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT, tmpRow)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return
    }
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()

    // заполнить кэш данными из справочника ОКТМО
    def limitRows = 10
    if (allValuesCount > limitRows) {
        fillRefBookCache(96L)
        fillRecordCache(96L, 'CODE', getReportPeriodEndDate())
    }

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++

        // все строки пустые - выход
        if (!rowValues || rowValues.isEmpty() || !rowValues.find { it }) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)

    updateIndexes(rows)
    formDataService.getDataRowHelper(formData).allCached = rows
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 * @param tmpRow вспомогательная строка для получения названии графов
 */
void checkHeaderXls(def headerRows, def colCount, rowCount, def tmpRow) {
    checkHeaderSize(headerRows, colCount, rowCount)

    def headers = formDataService.getFormTemplate(formData.formTemplateId).headers
    def headerMapping = [
            [(headerRows[0][0]) : headers[0].rowNumber],
            [(headerRows[0][1]) : headers[0].codeOKATO],
            [(headerRows[0][2]) : headers[0].regionName],
            [(headerRows[0][3]) : headers[0].tsTypeCode],
            [(headerRows[0][4]) : headers[0].tsType],
            [(headerRows[0][5]) : headers[0].model],
            [(headerRows[0][6]) : headers[0].ecoClass],
            [(headerRows[0][7]) : headers[0].identNumber],
            [(headerRows[0][8]) : headers[0].regNumber],
            [(headerRows[0][9]) : headers[0].regDate],
            [(headerRows[0][10]): headers[0].regDateEnd],
            [(headerRows[0][11]): headers[0].month],
            [(headerRows[0][12]): headers[0].taxBase],
            [(headerRows[0][13]): headers[0].baseUnit],
            [(headerRows[0][14]): headers[0].year],
            [(headerRows[0][15]): headers[0].pastYear],
            [(headerRows[0][16]): headers[0].stealDateStart],
            [(headerRows[1][16]): headers[1].stealDateStart],
            [(headerRows[1][17]): headers[1].stealDateEnd],
            [(headerRows[0][18]): headers[0].share],
            [(headerRows[0][19]): headers[0].costOnPeriodBegin],
            [(headerRows[0][20]): headers[0].costOnPeriodEnd],
            [(headerRows[0][21]): headers[0].version],
            [(headerRows[0][22]): headers[0].averageCost],
            [(headerRows[0][23]): headers[0].deductionCode],
            [(headerRows[0][24]): headers[0].deduction],
            [(headerRows[2][0]) : '1']
    ]
    (0..24).each {
        headerMapping.add([(headerRows[2][it]) : (it + 1).toString()])
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    def int colIndex

    // графа 2 - атрибут 840 - CODE - «Код», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
    colIndex = 1
    def record96 = getRecordImport(96, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    newRow.codeOKATO = record96?.record_id?.value

    // графа 3 - зависит от графы 2 - атрибут 841 - NAME - «Наименование», справочник 96 «Общероссийский классификатор территорий муниципальных образований»
    colIndex++
    if (record96 != null) {
        def expectedValues = [record96?.NAME?.value]
        formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'regionName'), record96?.CODE?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 4 - атрибут 422 - CODE - «Код вида ТС», справочник 42 «Коды видов транспортных средств»
    colIndex++
    def record42 = getRecordImport(42, 'CODE', values[colIndex].replace(' ', ''), fileRowIndex, colIndex + colOffset, false)
    newRow.tsTypeCode = record42?.record_id?.value

    // графа 5 - зависит от графы 4 - атрибут 423 - NAME - «Наименование вида транспортного средства», справочник 42 «Коды видов транспортных средств»
    colIndex++
    if (record42 != null) {
        def expectedValues = [record42?.NAME?.value]
        formDataService.checkReferenceValue(values[colIndex], expectedValues, getColumnName(newRow, 'tsType'), record42?.CODE?.value, fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 6
    colIndex++
    newRow.model = values[colIndex]

    // графа 7 - атрибут 400 - CODE - «Код экологического класса», справочник 40 «Экологические классы»
    colIndex++
    newRow.ecoClass = getRecordIdImport(40, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 8
    colIndex++
    newRow.identNumber = values[colIndex]

    // графа 9
    colIndex++
    newRow.regNumber = values[colIndex]?.replace(' ', '')

    // графа 10
    colIndex++
    newRow.regDate = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 11
    colIndex++
    newRow.regDateEnd = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 12
    colIndex++
    newRow.month = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 13
    colIndex++
    newRow.taxBase = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 14 - атрибут 57 - CODE - «Код единицы измерения», справочник 12 «Коды единиц измерения налоговой базы на основании ОКЕИ»
    colIndex++
    newRow.baseUnit = getRecordIdImport(12, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 15
    colIndex++
    newRow.year = parseDate(values[colIndex], "yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 16
    colIndex++
    newRow.pastYear = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 17
    colIndex++
    newRow.stealDateStart = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 18
    colIndex++
    newRow.stealDateEnd = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 19
    colIndex++
    newRow.share = values[colIndex]

    // графа 20
    colIndex++
    newRow.costOnPeriodBegin = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 21
    colIndex++
    newRow.costOnPeriodEnd = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 22 - атрибут 2183 - MODEL - «Модель (версия)», справочник 218 «Средняя стоимость транспортных средств»
    colIndex++
    boolean repaint = false
    // Если графа 22 заполнена, то
    if (values[21]) {
        // Если графа 23 не заполнена
        if (!values[22]) {
            // 1. Проверка заполнения средней стоимости
            def columnName22 = getColumnName(newRow, 'version')
            def columnName23 = getColumnName(newRow, 'averageCost')
            repaint = true
            logger.warn("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. графа «%s» не заполнена",
                    fileRowIndex, getXLSColumnName(22), columnName22, columnName23)
        } else {
            def record211 = getAllRecords(211L)?.values()?.find { it?.NAME?.value == values[22] }
            if (!record211) {
                // 2. Проверка корректности заполнения средней стоимости
                def columnName22 = getColumnName(newRow, 'version')
                repaint = true
                logger.warn("Строка %s, столбец %s: Не удалось заполнить графу «%s», " +
                        "т.к. в справочнике «Категории средней стоимости транспортных средств» " +
                        "не найдена категория «%s»",
                        fileRowIndex, getXLSColumnName(22), columnName22, values[22])
            } else {
                def records218 = getAllRecords(218L)?.values()?.findAll {
                    it?.AVG_COST?.value == record211?.record_id?.value && it?.MODEL?.value == values[21]
                }
                if (records218 == null || records218?.isEmpty()) {
                    // 3. Проверка наличия информации о модели в справочнике «Средняя стоимость транспортных средств (с 2015)»
                    def columnName22 = getColumnName(newRow, 'version')
                    repaint = true
                    logger.warn("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. в справочнике " +
                            "«Средняя стоимость транспортных средств (с 2015)» не найдена запись " +
                            "со значением поля «Модель(версия)» равным «%s» и значением поля «Средняя стоимость» равным «%s»",
                            fileRowIndex, getXLSColumnName(22), columnName22, values[21], values[22])
                } else if (records218?.size() > 1) {
                    // 4. Проверка возможности однозначного выбора информации о модели в справочнике «Средняя стоимость транспортных средств (с 2015)»
                    def columnName22 = getColumnName(newRow, 'version')
                    repaint = true
                    logger.warn("Строка %s, столбец %s: Не удалось заполнить графу «%s», т.к. в справочнике " +
                            "«Средняя стоимость транспортных средств (с 2015)» найдено несколько записей " +
                            "со значением поля «Модель(версия)» равным «%s» и значением поля «Средняя стоимость» равным «%s»",
                            fileRowIndex, getXLSColumnName(22), columnName22, values[21], values[22])
                } else if (records218?.size() == 1) {
                    newRow.version = records218[0]?.record_id?.value
                }
            }
        }
    }

    // графа 23 - зависит от графы 22 - не проверяется, потому что используется для нахождения записи для графы 22
    colIndex++

    // графа 24 - атрибут 15 - CODE - «Код налоговой льготы», справочник 6 «Коды налоговых льгот транспортного налога»
    colIndex++
    newRow.deductionCode = getRecordIdImport(6, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 25
    colIndex++
    newRow.deduction = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    if (repaint) {
        allColumns.each { alias ->
            newRow.getCell(alias).styleAlias = "Ошибка"
        }
    }
    return newRow
}

/** Заполнить refBookCache всеми записями справочника refBookId. */
void fillRefBookCache(def refBookId) {
    def records = getAllRecords2(refBookId)
    for (def record : records) {
        def recordId = record?.record_id?.value
        def key = getRefBookCacheKey(refBookId, recordId)
        if (refBookCache[key] == null) {
            refBookCache.put(key, record)
        }
    }
}

/**
 * Заполнить recordCache всеми записями справочника refBookId из refBookCache.
 *
 * @param refBookId идентификатор справочника
 * @param alias алиас атрибута справочника по которому будет осуществляться поиск
 * @param date дата по которой будет осуществляться поиск
 */
void fillRecordCache(def refBookId, def alias, def date) {
    def keys = refBookCache.keySet().toList()
    def needKeys = keys.findAll { it.contains(refBookId + SEPARATOR) }
    def dateSts = date.format('dd.MM.yyyy')
    def rb = refBookFactory.get(refBookId)
    for (def needKey : needKeys) {
        def recordId = refBookCache[needKey]?.record_id?.value
        def value = refBookCache[needKey][alias]?.value
        def filter = getFilter(alias, value, rb)
        def key = dateSts + filter
        if (recordCache[refBookId] == null) {
            recordCache[refBookId] = [:]
        }
        recordCache[refBookId][key] = recordId
    }
}

/**
 * Формирование фильтра. Взято из FormDataServiceImpl.getRefBookRecord(...)
 *
 * @param alias алиас атрибута справочника по которому будет осуществляться поиск
 * @param value значение атрибута справочника
 * @param rb справочник
 */
def getFilter(def alias, def value, def rb) {
    def filter
    if (value == null || value.isEmpty()) {
        filter = alias + " is null"
    } else {
        RefBookAttributeType type = rb.getAttribute(alias).getAttributeType()
        String template
        // TODO: поиск по выражениям с датами не реализован
        if (type == RefBookAttributeType.REFERENCE || type == RefBookAttributeType.NUMBER) {
            if (!isNumeric(value)) {
                // В справочнике поле числовое, а у нас строка, которая не парсится — ничего не ищем выдаем ошибку
                return null
            }
            template = "%s = %s"
        } else {
            template = "LOWER(%s) = LOWER('%s')"
        }
        filter = String.format(template, alias, value)
    }
    return filter
}

boolean isNumeric(String str) {
    return str.matches("-?\\d+(\\.\\d+)?")
}

@Field
def allRecordsMap2 = [:]

def getAllRecords2(def refbookId) {
    if (allRecordsMap2[refbookId] == null) {
        def provider = formDataService.getRefBookProvider(refBookFactory, refbookId, providerCache)
        allRecordsMap2[refbookId] = provider.getRecords(getReportPeriodEndDate(), null, null, null)
    }
    return allRecordsMap2[refbookId]
}