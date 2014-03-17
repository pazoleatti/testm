package form_template.income.rnu26.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций,
 *                                                  РДР, ADR, GDR и опционов эмитента в целях налогообложения".
 * formTemplateId=1325
 *
 * @author rtimerbaev
 */

// Признак периода ввода остатков
@Field
def Boolean isBalancePeriod = null

def getBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

@Field
def isConsolidated
isConsolidated = formData.kind == FormDataKind.CONSOLIDATED

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        if (!prevPeriodCheck()){
            return
        }
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        if (!prevPeriodCheck()){
            return
        }
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        if (!prevPeriodCheck()){
            return
        }
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        if (!prevPeriodCheck()){
            return
        }
        calc()
        logicCheck()
        break
}

// графа 1  - rowNumber
// графа    - fix
// графа 2  - issuer
// графа 3  - shareType
// графа 4  - tradeNumber
// графа 5  - currency Справочник
// графа 6  - lotSizePrev
// графа 7  - lotSizeCurrent
// графа 8  - reserveCalcValuePrev
// графа 9  - cost
// графа 10 - signSecurity Справочник
// графа 11 - marketQuotation
// графа 12 - rubCourse
// графа 13 - marketQuotationInRub
// графа 14 - costOnMarketQuotation
// графа 15 - reserveCalcValue
// графа 16 - reserveCreation
// графа 17 - reserveRecovery

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

// все атрибуты
@Field
def allColumns = ['rowNumber', 'fix', 'issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent',
        'reserveCalcValuePrev', 'cost', 'signSecurity', 'marketQuotation', 'rubCourse', 'marketQuotationInRub',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Редактируемые атрибуты
@Field
def editableColumns = ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent',
        'cost', 'signSecurity', 'marketQuotation', 'rubCourse']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты
@Field
def groupColumns = ['tradeNumber', 'issuer']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent', 'cost',
        'signSecurity']

// Атрибуты итоговых строк для которых вычисляются суммы
@Field
def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev', 'cost',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Дата окончания отчетного периода
@Field
def endDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    def retValue = parseNumber(value, indexRow, indexCol, logger, true)
    if (formDataEvent == FormDataEvent.MIGRATION && retValue == null) {
        retValue = 0
    }
    return retValue
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

    def reportDate = reportPeriodService.getReportDate(formData.reportPeriodId).time

    // данные предыдущего отчетного периода
    def formDataOld = getFormDataOld()
    def dataOld = formDataOld != null ? formDataService.getDataRowHelper(formDataOld) : null

    // номер последний строки предыдущей формы
    def number = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    // список групп кодов классификации для которых надо будет посчитать суммы
    def totalGroupsName = []

    for (row in dataRows) {
        row.rowNumber = ++number
        if (!getBalancePeriod()) {
            // графа 8
            row.reserveCalcValuePrev = calc8(row, dataOld)

            if (formDataEvent != FormDataEvent.IMPORT) {
                // графа 12 курс валют
                row.rubCourse = calc12(row.currency, reportDate)
                // графа 13
                row.marketQuotationInRub = calc13(row)
            }

            // графа 14
            row.costOnMarketQuotation = calc14(row)
            // графа 15
            row.reserveCalcValue = calc15(row)
            // графа 16
            row.reserveCreation = calc16(row)
            // графа 17
            row.reserveRecovery = calc17(row)
        }
    }

    // добавить строку "итого"
    dataRows.add(getCalcTotalRow())

    // посчитать "итого по Эмитенту:..."
    def totalRows = [:]
    def sums = [:]
    tmp = null
    totalColumns.each {
        sums[it] = 0
    }
    // обновить индексы строк
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
    dataRows.eachWithIndex { row, i ->
        if (row.getAlias() == null && row.issuer != null) {
            if (tmp == null) {
                tmp = row.issuer
            }
            // если код расходы поменялся то создать новую строку "итого по Эмитента:..."
            if (tmp != row.issuer) {
                totalRows.put(i, getNewRow(tmp, totalColumns, sums, dataRows))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по Эмитента:..."
            if (i == dataRows.size() - 2) {
                totalColumns.each {
                    sums[it] += (row.getCell(it).getValue() ?: 0)
                }
                totalRows.put(i + 1, getNewRow(row.issuer, totalColumns, sums, dataRows))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                sums[it] += (row.getCell(it).getValue() ?: 0)
            }
            tmp = row.issuer
        }
    }
    // добавить "итого по Эмитенту:..." в таблицу
    def i = 0
    totalRows.each { index, row ->
        dataRows.add(index + i++, row)
    }
    dataRowHelper.save(dataRows)
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    // данные предыдущего отчетного периода
    def formDataOld = getFormDataOld()
    def dataOld = formDataOld != null ? formDataService.getDataRowHelper(formDataOld) : null

    if (formDataOld != null && !dataOld.getAllCached().isEmpty()) {

        // суммы строки общих итогов
        def totalSums = [:]
        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        // алиасы графов для арифметической проверки
        def arithmeticCheckAlias = ['reserveCalcValuePrev', 'marketQuotationInRub', 'costOnMarketQuotation',
                'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
        // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
        def needValue = [:]

        def tmp

        def hasTotal = false
        def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

        for (def row : dataRows) {
            if (row.getAlias() != null) {
                hasTotal = true
                continue
            }
            def index = row.getIndex()
            def errorMsg = "Строка $index: "

            // 1. Проверка на заполнение поля
            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !getBalancePeriod())

            // 2. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 8, 17)
            if (row.lotSizeCurrent == 0 && row.reserveCalcValuePrev != row.reserveRecovery) {
                logger.warn(errorMsg + 'графы 8 и 17 неравны!')
            }

            // 3. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 9, 14, 15)
            if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
                logger.warn(errorMsg + 'графы 9, 14 и 15 ненулевые!')
            }

            // 4. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6, 8, 17)
            if (row.lotSizePrev == 0 && (row.reserveCalcValuePrev != 0 || row.reserveRecovery != 0)) {
                loggerError(errorMsg + 'графы 8 и 17 ненулевые!')
            }

            // 5. Проверка необращающихся акций (графа 10, 15, 16)
            def sign = getSign(row.signSecurity)
            if (sign == '-' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
                logger.warn(errorMsg + 'акции необращающиеся, графы 15 и 16 ненулевые!')
            }

            // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
            tmp = (row.reserveCalcValue ?: 0) - (row.reserveCalcValuePrev ?: 0)
            if (sign == '+' && tmp > 0 && row.reserveRecovery != 0) {
                loggerError(errorMsg + 'акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
            }

            // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 16)
            if (sign == '+' && tmp < 0 && row.reserveCreation != 0) {
                loggerError(errorMsg + 'акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
            }

            // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
            if (sign == '+' && tmp == 0 &&
                    (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
                loggerError(errorMsg + 'акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
            }

            // 9. Проверка корректности формирования резерва (графа 8, 15, 16, 17)
            if ((row.reserveCalcValuePrev ?: 0) + (row.reserveCreation ?: 0) != (row.reserveCalcValue ?: 0) + (row.reserveRecovery ?: 0)) {
                loggerError(errorMsg + 'резерв сформирован неверно!')
            }

            // 10. Проверка на положительные значения при наличии созданного резерва
            if (row.reserveCreation > 0 && row.lotSizeCurrent < 0 && row.cost < 0 &&
                    row.costOnMarketQuotation < 0 && row.reserveCalcValue < 0) {
                logger.warn(errorMsg + 'резерв сформирован. Графы 7, 9, 14 и 15 неположительные!')
            }

            // 11. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 6, 7 (за предыдущий период) )
            if (!getBalancePeriod() && !isConsolidated && checkOld(row, 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', formDataOld)) {
                def curCol = 4
                def curCol2 = 6
                def prevCol = 4
                def prevCol2 = 7
                logger.warn(errorMsg + "РНУ сформирован некорректно! " + errorMsg + "не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-26 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-26 за предыдущий отчётный период.")
            }

            // 12. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 8, 15 (за предыдущий период) )
            if (!getBalancePeriod() && !isConsolidated && checkOld(row, 'tradeNumber', 'reserveCalcValuePrev', 'reserveCalcValue', formDataOld)) {
                def curCol = 4
                def curCol2 = 4
                def prevCol = 8
                def prevCol2 = 15
                loggerError(errorMsg + "РНУ сформирован некорректно! " + errorMsg + "не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-26 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-26 за предыдущий отчётный период.")
            }

            // 16. Проверка на уникальность поля «№ пп» (графа 1)
            if (++rowNumber != row.rowNumber) {
                loggerError(errorMsg + 'Нарушена уникальность номера по порядку!')
            }

            // 17. Арифметическая проверка графы 8, 14..17
            if (!getBalancePeriod()) {
                needValue['reserveCalcValuePrev'] = calc8(row, dataOld)
                needValue['marketQuotationInRub'] = calc13(row)
                needValue['costOnMarketQuotation'] = calc14(row)
                needValue['reserveCalcValue'] = calc15(row)
                needValue['reserveCreation'] = calc16(row)
                needValue['reserveRecovery'] = calc17(row)
                checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
            }

            // 18. Проверка итоговых значений по эмитентам
            if (row.issuer != null && !totalGroupsName.contains(row.issuer)) {
                totalGroupsName.add(row.issuer)
            }

            // 19. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
            }
        }

        if (dataOld != null && hasTotal) {
            totalRow = getDataRow(dataRows, 'total')
            totalRowOld = getDataRow(dataOld.getAllCached(), 'total')

            // 13. Проверка корректности заполнения РНУ (графа 6, 7 (за предыдущий период))
            if (totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
                def curCol = 6
                def prevCol = 7
                loggerError("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе $curCol = «Итого» по графе $prevCol формы РНУ-26 за предыдущий отчётный период.")
            }

            // 14. Проверка корректности заполнения РНУ (графа 8, 15 (за предыдущий период))
            if (totalRow.reserveCalcValuePrev != totalRowOld.reserveCalcValue) {
                def curCol = 8
                def prevCol = 15
                loggerError("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе $curCol = «Итого» по графе $prevCol формы РНУ-26 за предыдущий отчётный период.")
            }
        }

        if (hasTotal) {
            def totalRow = getDataRow(dataRows, 'total')

            // 18. Проверка итоговых значений по эмитенту
            for (def codeName : totalGroupsName) {
                def row
                try {
                    row = getDataRow(dataRows, 'total' + getRowNumber(codeName, dataRows))
                } catch(IllegalArgumentException e) {
                    loggerError("Итоговые значения по эмитенту $codeName не рассчитаны! Необходимо рассчитать данные формы.")
                    continue
                }
                for (def alias : totalColumns) {
                    if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                        loggerError("Итоговые значения по эмитенту $codeName рассчитаны неверно!")
                    }
                }
            }

            // 19. Проверка итогового значений по всей форме
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    loggerError('Итоговые значения рассчитаны неверно!')
                }
            }
        }
    }
}

/**
 * Получить сумму столбца.
 */
def getSum(def data, def columnAlias) {
    def rows = data.getAllCached()
    def from = 0
    def to = rows.size() - 1
    if (from > to) {
        return 0
    }
    return summ(formData, rows, new ColumnRange(columnAlias, from, to))
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums, def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + getRowNumber(alias, dataRows))
    newRow.fix = alias + ' итог'
    newRow.getCell('fix').colSpan = 2
    setTotalStyle(newRow)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it], null)
    }
    return newRow
}

/**
 * Сверить данные с предыдущим периодом.
 *
 * @param row строка нф текущего периода
 * @param likeColumnName псевдоним графы по которому ищутся соответствующиеся строки
 * @param curColumnName псевдоним графы текущей нф для второго условия
 * @param prevColumnName псевдоним графы предыдущей нф для второго условия
 * @param prevForm данные нф предыдущего периода
 */
def checkOld(def row, def likeColumnName, def curColumnName, def prevColumnName, def prevForm) {
    if (prevForm == null) {
        return false
    }
    if (row.getCell(likeColumnName).getValue() == null) {
        return false
    }
    for (def prevRow : formDataService.getDataRowHelper(prevForm).getAllCached()) {
        if (row.getCell(likeColumnName).getValue() == prevRow.getCell(likeColumnName).getValue() &&
                row.getCell(curColumnName).getValue() != prevRow.getCell(prevColumnName).getValue()) {
            return true
        }
    }
    return false
}

/**
 * Получить данные за предыдущий отчетный период
 */
def getFormDataOld() {
    if (getBalancePeriod() || isConsolidated) {
        return null
    }
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-26 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

/**
 * * Посчитать сумму указанного графа для строк с общим значением
 *
 * @param value значение общее для всех строк суммирования
 * @param alias название графа
 */
def calcSumByCode(def value, def alias) {
    def data = formDataService.getDataRowHelper(formData)
    def sum = 0
    data.getAllCached().each { row ->
        if (row.getAlias() == null && row.issuer == value) {
            sum += (row.getCell(alias).getValue() ?: 0)
        }
    }
    return sum
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    allColumns.each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
        row.getCell(it).editable = false
    }
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formDataService.getDataRowHelper(formData).getAllCached().indexOf(row)
}

def BigDecimal calc8(def row, def dataOld) {
    if (getBalancePeriod() || isConsolidated) {
        return row.reserveCalcValuePrev
    }
    if (row.tradeNumber != null && dataOld != null && !dataOld.getAllCached().isEmpty()) {
        for (def oldRow : dataOld.getAllCached()) {
            if (oldRow.getCell('tradeNumber').getValue() == row.tradeNumber) {
                return roundValue(oldRow.getCell('reserveCalcValue').getValue(), 2)
            }
        }
    }
    return roundValue(0, 2)
}

def BigDecimal calc13(def row) {
    if (row.marketQuotation != null && row.rubCourse != null) {
        return roundValue(row.marketQuotation * row.rubCourse, 6)
    }
    return null
}

def BigDecimal calc14(def row) {
    def tmp = 0
    if (row.lotSizeCurrent != null && row.marketQuotationInRub != null) {
        tmp = (row.marketQuotationInRub == null ? 0 : row.lotSizeCurrent * row.marketQuotationInRub)
    }
    return roundValue(tmp, 2)
}

def BigDecimal calc15(def row) {
    def tmp
    if (row.signSecurity != null && row.costOnMarketQuotation != null && getSign(row.signSecurity) == '+') {
        def a = (row.cost == null ? 0 : row.cost)
        tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
    } else {
        tmp = 0
    }
    return roundValue(tmp, 2)
}

def BigDecimal calc16(def row) {
    if (row.reserveCalcValue != null && row.reserveCalcValuePrev != null) {
        def tmp = row.reserveCalcValue - row.reserveCalcValuePrev
        return roundValue((tmp > 0 ? tmp : 0), 2)
    }
    return null
}

def BigDecimal calc17(def row) {
    if (row.reserveCalcValue != null && row.reserveCalcValuePrev != null) {
        def tmp = row.reserveCalcValue - row.reserveCalcValuePrev
        return roundValue((tmp < 0 ? tmp.abs() : 0), 2)
    }
    return null
}

/**
 * Получить id справочника.
 *
 * @param ref_id идентификатор справончика
 * @param code атрибут справочника
 * @param value значение для поиска
 * @param date дата актуальности
 * @param cache кеш
 * @return
 */
def getRecords(def ref_id, String code, String value, Date date, def cache) {
    String filter = code + " like '" + value.replaceAll(' ', '') + "%'"
    if (cache[ref_id] != null) {
        if (cache[ref_id][filter] != null) return cache[ref_id][filter]
    } else {
        cache[ref_id] = [:]
    }
    def refDataProvider = refBookFactory.getDataProvider(ref_id)
    def records = refDataProvider.getRecords(date, null, filter, null).getRecords()
    if (records.size() == 1) {
        cache[ref_id][filter] = (records.get(0).record_id.toString() as Long)
        return cache[ref_id][filter]
    }
    loggerError("Не удалось найти запись в справочнике «" + refBookFactory.get(ref_id).getName() + "» с атрибутом $code равным $value!")
    return null
}

// Проверка валюты currencyCode на рубли
def isRubleCurrency(def currencyCode) {
    return currencyCode != null ? (getRefBookValue(15, currencyCode)?.CODE.stringValue == '810') : false
}

/**
 * Получить курс валюты
 */
def BigDecimal calc12(def currency, def date) {
    if (currency != null && !isRubleCurrency(currency)) {
        def refCourseDataProvider = refBookFactory.getDataProvider(22)
        def res = refCourseDataProvider.getRecords(date, null, 'CODE_NUMBER=' + currency, null);
        return (!res.getRecords().isEmpty()) ? res.getRecords().get(0).RATE.getNumberValue() : 0//Правильнее null, такой ситуации быть не должно, она должна отлавливаться проверками НСИ
    } else {
        return null;
    }
}

/**
 * Получить признак ценной бумаги
 */
def getSign(def sign) {
    return getRefBookValue(62, sign)?.CODE?.stringValue
}

/**
 * Получить буквенный код валюты
 */
def getCurrency(def currencyCode) {
    return getRefBookValue(15, currencyCode)?.CODE_2?.stringValue
}

/**
 * Получение первого rowNumber по issuer
 * @param alias
 * @param dataRows
 * @return
 */
def getRowNumber(def alias, def dataRows) {
    for (def row : dataRows) {
        if (row.issuer == alias) {
            return row.rowNumber.toString()
        }
    }
}

/**
 * Расчетать, проверить и сравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def totalColumnsIndex = [6: 'lotSizePrev', 7: 'lotSizeCurrent', 8: 'reserveCalcValuePrev', 9: 'cost', 14: 'costOnMarketQuotation',
            15: 'reserveCalcValue', 16: 'reserveCreation', 17: 'reserveRecovery']
    def totalCalc = getCalcTotalRow()
    def errorColums = []
    if (totalCalc != null) {
        totalColumnsIndex.each { index, columnAlias ->
            if (totalRow[columnAlias] != null && totalCalc[columnAlias] != totalRow[columnAlias]) {
                errorColums.add(index)
            }
        }
    }
    if (!errorColums.isEmpty()) {
        def columns = errorColums.join(', ')
        loggerError("Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

/**
 * Получить итоговую строку с суммами.
 */
def getCalcTotalRow() {
    // добавить строку "итого"
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.issuer = 'Общий итог'
    setTotalStyle(totalRow)
    def data = formDataService.getDataRowHelper(formData)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(data, alias), null)
    }
    return totalRow
}

/**
 * Округляет число до требуемой точности.
 *
 * @param value округляемое число
 * @param precision точность округления, знаки после запятой
 * @return округленное число
 */
def roundValue(def value, def precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def msg) {
    if (getBalancePeriod()) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
boolean prevPeriodCheck() {
    if (!getBalancePeriod() && !isConsolidated && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        logger.error("Форма предыдущего периода не существует, или не находится в статусе «Принята»")
        return false
    }
    return true
}

void migration() {
    importData()
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.allCached
        def total = getCalcTotalRow()
        dataRowHelper.insert(total, dataRows.size() + 1)
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}
// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 17, 1)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Эмитент',
            (xml.row[0].cell[3]): 'Тип акции',
            (xml.row[0].cell[4]): 'Номер сделки',
            (xml.row[0].cell[5]): 'Валюта выпуска ценной бумаги',
            (xml.row[0].cell[6]): 'Размер лота на предыдущую отчетную дату, шт. (по депозитарному учету)',
            (xml.row[0].cell[7]): 'Размер лота на текущую отчетную дату, шт. (по депозитарному учету)',
            (xml.row[0].cell[8]): 'Расчётная величина резерва на предыдущую отчетную дату, руб. коп.',
            (xml.row[0].cell[9]): 'Стоимость по цене приобретения, руб. коп.',
            (xml.row[0].cell[10]): 'Признак ценной бумаги на текущую отчетную дату',
            (xml.row[0].cell[11]): 'Рыночная котировка одной ценной бумаги в иностранной валюте',
            (xml.row[0].cell[12]): 'Курс рубля к валюте рыночной котировки',
            (xml.row[0].cell[13]): 'Рыночная котировка одной ценной бумаги в валюте Российской Федерации',
            (xml.row[0].cell[14]): 'Стоимость по рыночной котировке, руб. коп.',
            (xml.row[0].cell[15]): 'Расчетная величина резерва на текущую отчетную дату, руб. коп.',
            (xml.row[0].cell[16]): 'Создание резерва, руб. коп.',
            (xml.row[0].cell[17]): 'Восстановление резерва, руб. коп.',
            (xml.row[1].cell[0]): '1',
            (xml.row[1].cell[2]): '2',
            (xml.row[1].cell[3]): '3',
            (xml.row[1].cell[4]): '4',
            (xml.row[1].cell[5]): '5',
            (xml.row[1].cell[6]): '6',
            (xml.row[1].cell[7]): '7',
            (xml.row[1].cell[8]): '8',
            (xml.row[1].cell[9]): '9',
            (xml.row[1].cell[10]): '10',
            (xml.row[1].cell[11]): '11',
            (xml.row[1].cell[12]): '12',
            (xml.row[1].cell[13]): '13',
            (xml.row[1].cell[14]): '14',
            (xml.row[1].cell[15]): '15',
            (xml.row[1].cell[16]): '16',
            (xml.row[1].cell[17]): '17'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 1)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = 10 // Смещение для индекса колонок в ошибках импорта
    def int colOffset = 1 // Смещение для индекса колонок в ошибках импорта

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[0].text() == null || row.cell[0].text() == '') {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }


        def indexCell = 0

        newRow.rowNumber = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++
        indexCell++

        // графа 2
        newRow.issuer = row.cell[indexCell].text()
        indexCell++

        // графа 3
        newRow.shareType = row.cell[indexCell].text()
        indexCell++

        // графа 4
        newRow.tradeNumber = row.cell[indexCell].text()
        indexCell++

        // графа 5
        newRow.currency = getRecordIdImport(15, 'CODE_2', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
        indexCell++

        // графа 6
        newRow.lotSizePrev = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 7
        newRow.lotSizeCurrent = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 8
        newRow.reserveCalcValuePrev = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 9
        newRow.cost = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 10
        newRow.signSecurity = getRecordIdImport(62, 'CODE', row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset)
        indexCell++

        // графа 11
        newRow.marketQuotation = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 12
        newRow.rubCourse = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 13
        newRow.marketQuotationInRub = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 14
        newRow.costOnMarketQuotation = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 15
        newRow.reserveCalcValue = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 16
        newRow.reserveCreation = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)
        indexCell++

        // графа 17
        newRow.reserveRecovery = parseNumber(row.cell[indexCell].text(), xlsIndexRow, indexCell + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}