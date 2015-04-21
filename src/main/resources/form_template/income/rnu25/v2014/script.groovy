package form_template.income.rnu25.v2014

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "(РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения"
 * formTemplateId=1324
 * Версия 2014
 * @author rtimerbaev
 */

// графа 1  - rowNumber
// графа -  - fix
// графа 2  - regNumber
// графа 3  - tradeNumber
// графа 4  - lotSizePrev
// графа 5  - lotSizeCurrent
// графа 6  - reserve
// графа 7  - cost
// графа 10 - signSecurity              - справочник "Признак ценных бумаг", отображаемый атрибут "Код признака"
//                                              было: текст,
//                                              было: атрибут 621 CODE "Код признака" - справочник 62 "Признаки ценных бумаг"
// графа 9  - marketQuotation
// графа 10 - costOnMarketQuotation
// графа 11 - reserveCalcValue
// графа 12 - reserveCreation
// графа 13 - reserveRecovery

@Field
def isConsolidated
isConsolidated = formData.kind == FormDataKind.CONSOLIDATED

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        def columns = (isBalancePeriod() ? allColumns - 'rowNumber' : editableColumns)
        formDataService.addRow(formData, currentDataRow, columns, null)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        if (UploadFileName.endsWith(".rnu")) {
            importTransportData()
        } else {
            importData()
            calc()
            logicCheck()
        }
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
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

// все атрибуты
@Field
def allColumns = ['rowNumber', 'fix', 'regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost',
                  'signSecurity', 'marketQuotation', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation',
                  'reserveRecovery']

// Редактируемые атрибуты (графа 2..5, 7..9)
@Field
def editableColumns = ['regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', 'cost', 'signSecurity', 'marketQuotation']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Группируемые атрибуты (графа 2, 3)
@Field
def groupColumns = ['regNumber', 'tradeNumber']

// Проверяемые на пустые значения атрибуты (графа 1..3, 5..13)
@Field
def nonEmptyColumns = ['regNumber', 'tradeNumber', 'lotSizeCurrent', 'reserve', 'cost', 'signSecurity',
                       'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4..7, 10..13)
@Field
def totalSumColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'costOnMarketQuotation',
        'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Признак периода ввода остатков
@Field
def isBalancePeriod

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Признак периода ввода остатков для отчетного периода подразделения
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить строку "итого" и "итого по ГРН: ..."
    deleteAllAliased(dataRows)

    def isImport = (formDataEvent == FormDataEvent.IMPORT)

    // отсортировать/группировать
    if (!isImport) {
        sortRows(dataRows, groupColumns)
    }

    // список групп кодов классификации для которых надо будет посчитать суммы
    def totalGroupsName = []
    // строки предыдущего периода
    def prevDataRows = getPrevDataRows()
    def tradeNumberRowMap = getTradeNumberObjectMap(prevDataRows)
    def countMap = getTradeNumberCountMap(prevDataRows)

    dataRows.each { row ->
        if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY) {
            // графа 4
            row.lotSizePrev = calc4(tradeNumberRowMap, row)

            // графа 6
            row.reserve = calc6(tradeNumberRowMap, countMap, row)

            // графа 10
            row.costOnMarketQuotation = calc10(row)

            // графа 11
            def sign = getSign(row)
            row.reserveCalcValue = calc11(row, sign)

            // графа 12
            row.reserveCreation = calc12(row)

            // графа 13
            row.reserveRecovery = calc13(row)
        }
        // для итоговых значений по ГРН
        if (row.regNumber != null && !totalGroupsName.contains(row.regNumber)) {
            totalGroupsName.add(row.regNumber)
        }
    }
    // добавить строку "итого"
    def totalRow = getCalcTotalRow(dataRows)
    dataRows.add(totalRow)
    if (dataRows.size() == 1) {
        dataRowHelper.save(dataRows)
        return
    }

    // обновить индексы строк
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }

    updateIndexes(dataRows)
    // итоговые значения по ГРН
    def i = 0
    for (def codeName : totalGroupsName) {
        // получить строки группы
        def rows = getGroupRows(dataRows, codeName)
        // получить алиас для подитоговой строки по ГРН
        def totalRowAlias = 'total' + codeName
        // сформировать подитоговую строку ГРН с суммами
        def subTotalRow = getCalcSubtotalsRow(rows, codeName, totalRowAlias)
        // получить индекс последней строки в группе
        def lastRowIndex = rows[rows.size() - 1].getIndex() + i
        // вставить строку с итогами по ГРН
        dataRows.add(lastRowIndex, subTotalRow)
        i++
    }
    updateIndexes(dataRows)
    // запись
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached

    def prevDataRows = getPrevDataRows()
    def countMap = getTradeNumberCountMap(prevDataRows)
    def rowMap = getTradeNumberObjectMap(prevDataRows)
    if (prevDataRows != null && !prevDataRows.isEmpty() && dataRows.size() > 1) {
        // 1. Проверка на полноту отражения данных предыдущих отчетных периодов (графа 11)
        //      в текущем отчетном периоде (выполняется один раз для всего экземпляра)
        def missContract = []
        def severalContract = []
        prevDataRows.each { prevRow ->
            if (prevRow.getAlias() == null && prevRow.reserveCalcValue > 0) {
                def count = countMap[prevRow.tradeNumber]
                if (count == 0) {
                    missContract.add(prevRow.tradeNumber)
                } else if (count > 1) {
                    severalContract.add(prevRow.tradeNumber)
                }
            }
        }
        if (!missContract.isEmpty()) {
            def message = missContract.join(', ')
            logger.warn("Отсутствуют строки с номерами сделок: $message!")
        }
        if (!severalContract.isEmpty()) {
            def message = severalContract.join(', ')
            logger.warn("Существует несколько строк с номерами сделок: $message!")
        }
    }

    // алиасы графов для арифметической проверки (графа )
    def arithmeticCheckAlias = ['reserve', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]
    // список групп кодов классификации для которых надо будет посчитать суммы
    def totalGroupsName = []

    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 2. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 6, 13)
        if (row.lotSizeCurrent == 0 && row.reserve != row.reserveRecovery) {
            rowWarning(logger, row, errorMsg + 'графы 6 и 13 неравны!')
        }
        // 3. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 7, 10, 11)
        if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
            rowWarning(logger, row, errorMsg + 'графы 7, 10 и 11 ненулевые!')
        }
        // 4. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 4, 6, 13)
        if (row.lotSizePrev == 0 && (row.reserve != 0 || row.reserveRecovery != 0)) {
            loggerError(row, errorMsg + 'графы 6 и 13 ненулевые!')
        }
        // 5. Проверка необращающихся акций (графа 8, 11, 12)
        def sign = getSign(row)
        if (sign == '-' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
            rowWarning(logger, row, errorMsg + 'облигации необращающиеся, графы 11 и 12 ненулевые!')
        }
        // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
        if (row.reserveCalcValue != null && row.reserve != null &&
                sign == '+' && row.reserveCalcValue - row.reserve > 0 && row.reserveRecovery != 0) {
            loggerError(row, errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно! Не выполняется условие: если «Графа 11» – «Графа 6» > 0, то «Графа 13» = 0')
        }
        // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 12)
        if (row.reserveCalcValue != null && row.reserve != null &&
                sign == '+' && row.reserveCalcValue - row.reserve < 0 && row.reserveCreation != 0) {
            loggerError(row, errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно! Не выполняется условие: если «Графа 11» – «Графа 6» < 0, то «Графа 12» = 0')
        }
        // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
        if (row.reserveCalcValue != null && row.reserve != null &&
                sign == '+' && row.reserveCalcValue - row.reserve == 0 &&
                (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
            loggerError(row, errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно! Не выполняется условие: если «Графа 11» – «Графа 6» = 0, то «Графа 12» и «Графа 13» = 0')
        }
        // 9. Проверка на положительные значения при наличии созданного резерва
        if (row.reserveCreation > 0 && row.lotSizeCurrent < 0 && row.cost < 0 &&
                row.costOnMarketQuotation < 0 && row.reserveCalcValue < 0) {
            rowWarning(logger, row, errorMsg + 'резерв сформирован. Графы 5, 7, 10 и 11 неположительные!')
        }
        // 10. Проверка корректности создания резерва (графа 6, 11, 12, 13)
        if (row.reserve != null && row.reserveCreation != null &&
                row.reserveCalcValue != null && row.reserveRecovery != null &&
                row.reserve + row.reserveCreation != row.reserveCalcValue + row.reserveRecovery) {
            loggerError(row, errorMsg + 'резерв сформирован некорректно! Не выполняется условие: «Графа 6» + «Графа 12» - «Графа 11» - «Графа 13» = 0')
        }
        // 11. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 4, 5 (за предыдущий период) )
        if (!isBalancePeriod() && !isConsolidated) {
            def result = checkOld(row, 'lotSizePrev', 'lotSizeCurrent', rowMap)
            if (result) {
                loggerError(row, errorMsg + "РНУ сформирован некорректно! Не выполняется условие: «Графа 4» (${row.lotSizePrev}) текущей строки РНУ-25 за текущий период = «Графе 5» ($result) строки РНУ-25 за предыдущий период, значение «Графы 3» которой соответствует значению «Графы 3» РНУ-25 за текущий период.")
            }
        }
        // 12. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 6, 11 (за предыдущий период) )
        if (!isBalancePeriod() && !isConsolidated) {
            def result = checkOld(row, 'reserve', 'reserveCalcValue', rowMap)
            if (result) {
                loggerError(row, errorMsg + "РНУ сформирован некорректно! Не выполняется условие: «Графа 6» (${row.reserve}) текущей строки РНУ-25 за текущий период = «Графе 11» ($result) строки РНУ-25 за предыдущий период, значение «Графы 3» которой соответствует значению «Графы 3» РНУ-25 за текущий период.")
            }
        }
        // 15. Обязательность заполнения поля графы 1..3, 5..13
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())
        // 17. Арифметические проверки граф 6, 10..13
        if (!isBalancePeriod()) {
            needValue['reserve'] = calc6(rowMap, countMap, row)
            needValue['costOnMarketQuotation'] = calc10(row)
            needValue['reserveCalcValue'] = calc11(row, sign)
            needValue['reserveCreation'] = calc12(row)
            needValue['reserveRecovery'] = calc13(row)
            checkCalc(row, arithmeticCheckAlias, needValue, logger, !isBalancePeriod())
        }
        // 18. Проверка итоговых значений по ГРН
        if (row.regNumber != null && !totalGroupsName.contains(row.regNumber)) {
            totalGroupsName.add(row.regNumber)
        }
    }

    if (prevDataRows != null) {
        def totalRow = getDataRow(dataRows, 'total')
        def totalRowOld = getDataRow(prevDataRows, 'total')

        // 13. Проверка корректности заполнения РНУ (графа 4, 5 (за предыдущий период))
        if (totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
            loggerError(totalRow, "РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе 4 (${totalRow.lotSizePrev}) = «Общий итог» по графе 5 (${totalRowOld.lotSizeCurrent}) Формы РНУ-25 за предыдущий отчетный период.")
        }
        // 14. Проверка корректности заполнения РНУ (графа 6, 11 (за предыдущий период))
        if (totalRow.reserve != totalRowOld.reserveCalcValue) {
            loggerError(totalRow, "РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе 6 (${totalRow.reserve})= «Общий итог» по графе 11 (${totalRowOld.reserveCalcValue}) формы РНУ-25 за предыдущий отчётный период")
        }
    }

    // 17. Проверка итоговых значений по ГРН
    for (def codeName : totalGroupsName) {
        // получить строки группы
        def rows = getGroupRows(dataRows, codeName)
        // получить алиас для подитоговой строки по ГРН
        def totalRowAlias = 'total' + codeName
        // получить посчитанную строку с итогами по ГРН
        def row
        try {
            row = getDataRow(dataRows, totalRowAlias)
        } catch (IllegalArgumentException e) {
            loggerError(null, "Итоговые значения по ГРН $codeName не рассчитаны! Необходимо рассчитать данные формы.")
            continue
        }
        // сформировать подитоговую строку ГРН с суммами
        def tmpRow = getCalcSubtotalsRow(rows, codeName, totalRowAlias)

        // сравнить строки
        if (isDiffRow(row, tmpRow, totalSumColumns)) {
            loggerError(row, "Итоговые значения по ГРН $codeName рассчитаны неверно!")
        }
    }

    // 18. Проверка итогового значений по всей форме
    checkTotalSum(dataRows, totalSumColumns, logger, !isBalancePeriod)
}

// Группирует данные по графе tradeNumber и считает количество строк, в которых данное значение встречается
// Результат: Map[tradeNumber:count]
def getTradeNumberCountMap(def rows) {
    def result = [:]
    rows.each {
        def tnum = it.tradeNumber
        def count = result[tnum]
        result[tnum] = count == null ? 1 : ++count
    }
    return result
}

// Группирует данные по графе tradeNumber с сохранением одной из ссылок на соответствующую строку
// Результат: Map[tradeNumber:row]
def getTradeNumberObjectMap(def rows) {
    def result = [:]
    rows.each {
        def tnum = it.tradeNumber
        if (result[tnum] == null) result[tnum] = it
    }
    return result
}

/*
 * Вспомогательные методы.
 */

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()
    def columns = (isBalancePeriod() ? allColumns - 'rowNumber' : editableColumns)
    columns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

/**
 * Сверить данные с предыдущим периодом. Если данные отличаются, то вернуть предыдущее значение
 *
 * @param row строка нф текущего периода
 * @param curColumnName псевдоним графы текущей нф для второго условия
 * @param prevColumnName псевдоним графы предыдущей нф для второго условия
 * @param prevRowMap строки нф предыдущего периода в карте по tradeNumber
 */
def checkOld(def row, def curColumnName, def prevColumnName, def prevRowMap) {
    def prevRow = prevRowMap[row.tradeNumber]
    if (prevRowMap != null && prevRow != null && row[curColumnName] != prevRow[prevColumnName]) {
        return prevRow[prevColumnName]
    } else {
        return null
    }
}

/** Получить строки за предыдущий отчетный период. */
def getPrevDataRows() {
    if (isBalancePeriod() || isConsolidated) {
        return null
    }
    def prevFormData = formDataService.getFormDataPrev(formData)
    return (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allCached : null)
}

/** Получить общую итоговую строку с суммами. */
def getCalcTotalRow(def dataRows) {
    return getTotalRow(dataRows, 'Общий итог', 'total')
}

/**
 * Получить подитоговую строку ГРН по коду классификации дохода.
 *
 * @param dataRows строки формы
 * @param regNumber код классификации дохода
 * @param totalRowAlias псевдоним сформированной строки
 */
def getCalcSubtotalsRow(def dataRows, def regNumber, def totalRowAlias) {
    return getTotalRow(dataRows, regNumber.trim() + ' итог', totalRowAlias)
}

/**
 * Сформировать итоговую строку с суммами.
 *
 * @param dataRows строки формы
 * @param regNumberValue значение графы "код классификации дохода"
 * @param alias алиас сформированной строки
 */
def getTotalRow(def dataRows, def regNumberValue, def alias) {
    def newRow = formData.createDataRow()
    newRow.setAlias(alias)
    newRow.fix = regNumberValue
    newRow.getCell('fix').colSpan = 2
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalSumColumns)
    return newRow
}

/**
 * Поиск строк с одинаковым кодом классификации дохода.
 *
 * @param dataRows строки формы
 * @param regNumber код классификации дохода
 */
def getGroupRows(def dataRows, def regNumber) {
    return dataRows.findAll {
        it.getAlias() == null && it.regNumber == regNumber
    }
}

/**
 * Вычисление значения графы 4.
 * @param rowMap карта строк предыдущего периода по инвентарному номеру
 * @param row строка текущего периода
 */
def BigDecimal calc4(def rowMap, def row) {
    // Строка с совпадающим значением графы 3
    def prevMatchRow = rowMap[row.tradeNumber]

    if (prevMatchRow == null) {
        return 0
    } else {
        if (prevMatchRow.signSecurity != null && row.signSecurity != null) {
            if (getSign(prevMatchRow) == '+' && getSign(row) == '-') {
                return prevMatchRow.lotSizeCurrent
            }
            if (getSign(prevMatchRow) == '-' && getSign(row) == '+') {
                return 0
            }
        }
    }

    // Иначе подставляется значение, введенное вручную
    return row.lotSizePrev
}

/**
 * Получить значение за предыдущий отчетный период для графы 6.
 * @param rowMap карта строк предыдущего периода по tradeNumber
 * @param countMap карта числа строк с одинаковым tradeNumber за пред. период
 * @param row строка текущего периода
 * @return возвращает найденое значение, иначе возвратит 0
 */
def BigDecimal calc6(def rowMap, def countMap, def row) {
    if (isConsolidated) {
        return row.reserve
    }
    if (row.tradeNumber == null) {
        return 0
    }
    def count = countMap[row.tradeNumber]
    def value = (rowMap[row.tradeNumber]?.reserveCalcValue)?:0
    // если count не равно 1, то или нет формы за предыдущий период,
    // или нет соответствующей записи в предыдущем периода или записей несколько
    return roundTo2((count == 1) ? value : 0)
}

def BigDecimal calc10(def row) {
    if (row.lotSizeCurrent == null) {
        return 0
    }
    return roundTo2(row.marketQuotation ? row.lotSizeCurrent * row.marketQuotation : 0)
}

def BigDecimal calc11(def row, def sign) {
    if (sign == null) {
        return 0
    }
    def tmp
    if (sign == '+') {
        if (row.costOnMarketQuotation == null) {
            return null
        }
        def a = (row.cost ?: 0)
        tmp = ((a > row.costOnMarketQuotation) ? (a - row.costOnMarketQuotation) : 0)
    } else {
        tmp = 0
    }
    return roundTo2(tmp)
}

def BigDecimal calc12(def row) {
    if (row.reserve == null || row.reserveCalcValue == null) {
        return 0
    }
    def tmp = row.reserveCalcValue - row.reserve
    return roundTo2(tmp > 0 ? tmp : 0)
}

def BigDecimal calc13(def row) {
    if (row.reserve == null || row.reserveCalcValue == null) {
        return 0
    }
    def tmp = (row.reserveCalcValue ?: 0) - (row.reserve ?: 0)
    return roundTo2(tmp < 0 ? -tmp : 0)
}


// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 13, 1)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Государственный регистрационный номер',
            (xml.row[0].cell[3]): 'Номер сделки',
            (xml.row[0].cell[4]): 'Размер лота на предыдущую отчетную дату, шт.',
            (xml.row[0].cell[5]): 'Размер лота на текущую отчетную дату, шт.',
            (xml.row[0].cell[6]): 'Расчётная величина резерва на предыдущую отчётную дату, руб.коп.',
            (xml.row[0].cell[7]): 'Стоимость по цене приобретения, руб.коп.',
            (xml.row[0].cell[8]): 'Признак ценной бумаги на текущую отчётную дату',
            (xml.row[0].cell[9]): 'Рыночная котировка одной облигации, руб.коп.',
            (xml.row[0].cell[10]): 'Стоимость по рыночной котировке, руб.коп.',
            (xml.row[0].cell[11]): 'Расчётная величина резерва на текущую отчётную дату, руб.коп.',
            (xml.row[0].cell[12]): 'Создание резерва, руб.коп.',
            (xml.row[0].cell[13]): 'Восстановление резерва, руб.коп.',
            (xml.row[1].cell[0]): '1'
    ]
    (2..13).each { index ->
        headerMapping.put((xml.row[1].cell[index]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 1)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        /* Пропуск строк шапок */
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[0].text() == null || row.cell[0].text() == "") {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        def columns = (isBalancePeriod() ? allColumns - 'rowNumber' : editableColumns)
        columns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // Графа 2
        newRow.regNumber = row.cell[2].text()
        // Графа 3
        newRow.tradeNumber = row.cell[3].text()
        // Графа 4
        newRow.lotSizePrev = parseNumber(row.cell[4].text(), xlsIndexRow, 4 + colOffset, logger, true)
        // Графа 5
        newRow.lotSizeCurrent = parseNumber(row.cell[5].text(), xlsIndexRow, 5 + colOffset, logger, true)
        // Графа 7
        newRow.cost = parseNumber(row.cell[7].text(), xlsIndexRow, 7 + colOffset, logger, true)
        // Графа 8
        newRow.signSecurity = getRecordIdImport(62, 'CODE', row.cell[8].text(), xlsIndexRow, 8 + colOffset)
        // Графа 9
        newRow.marketQuotation = parseNumber(row.cell[9].text(), xlsIndexRow, 9 + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

/** Получить признак ценной бумаги. */
def getSign(def row) {
    return getRefBookValue(62, row.signSecurity)?.CODE?.value
}

BigDecimal roundTo2(BigDecimal value) {
    return value?.setScale(2, RoundingMode.HALF_UP)
}

/**
 * Cравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalCalc = getCalcTotalRow(dataRows)

    def totalSumColumns = [4: 'lotSizePrev', 5: 'lotSizeCurrent', 7: 'cost', 10: 'costOnMarketQuotation',
            11: 'reserveCalcValue', 12: 'reserveCreation', 13: 'reserveRecovery']
    def errorColums = []
    if (totalCalc != null) {
        totalSumColumns.each { index, columnAlias ->
            if (totalRow[columnAlias] != null && totalCalc[columnAlias] != totalRow[columnAlias]) {
                errorColums.add(index)
            }
        }
    }
    if (!errorColums.isEmpty()) {
        def columns = errorColums.join(', ')
        loggerError(null, "Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
void prevPeriodCheck() {
    if (!isConsolidated && !isBalancePeriod()) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, true, logger, true)
    }
}

def loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

// обновить индексы строк
def updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), getDataRow(dataRows, 'total'), true)
    dataRowHelper.saveSort()
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
}

void importTransportData() {
    int COLUMN_COUNT = 13
    int TOTAL_ROW_COUNT = 1
    int ROW_MAX = 1000
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    checkBeforeGetXml(ImportInputStream, UploadFileName)

    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.clear()

    String[] rowCells
    int countEmptyRow = 0	// количество пустых строк
    int fileRowIndex = 0    // номер строки в файле
    int rowIndex = 0        // номер строки в НФ
    int totalRowCount = 0   // счетчик кол-ва итогов
    def total = null		// итоговая строка со значениями из тф для добавления
    def newRows = []

    while ((rowCells = reader.readNext()) != null) {
        fileRowIndex++

        def isEmptyRow = (rowCells.length == 1 && rowCells[0].length() < 1)
        if (isEmptyRow) {
            if (countEmptyRow > 0) {
                // если встретилась вторая пустая строка, то дальше только строки итогов и ЦП
                totalRowCount++
                // итоговая строка тф
                total = getNewRow(reader.readNext(), COLUMN_COUNT, ++fileRowIndex, ++rowIndex)
                break
            }
            countEmptyRow++
            continue
        }

        // если еще не было пустых строк, то это первая строка - заголовок (пропускается)
        // обычная строка
        if (countEmptyRow != 0 && !addRow(newRows, rowCells, COLUMN_COUNT, fileRowIndex, ++rowIndex)) {
            break
        }

        // периодически сбрасываем строки
        if (newRows.size() > ROW_MAX) {
            dataRowHelper.insert(newRows, dataRowHelper.allCached.size() + 1)
            newRows.clear()
        }
    }
    reader.close()

    // проверка итоговой строки
    if (TOTAL_ROW_COUNT != 0 && totalRowCount != TOTAL_ROW_COUNT) {
        logger.error(ROW_FILE_WRONG, fileRowIndex)
    }

    if (newRows.size() != 0) {
        dataRowHelper.insert(newRows, dataRowHelper.allCached.size() + 1)
    }

    // сравнение итогов
    if (total) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = ['lotSizePrev' : 4, 'lotSizeCurrent' : 5, 'reserve' : 6, 'cost' : 7,
                'costOnMarketQuotation' : 10, 'reserveCalcValue' : 11, 'reserveCreation' : 12, 'reserveRecovery' : 13]

        // подсчет итогов
        def dataRows = dataRowHelper.allCached
        def totalRow = getCalcTotalRow(dataRows)

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = total.getCell(alias).value
            def v2 = totalRow.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }

        // добавить в нф итоговую строку
        dataRowHelper.insert(totalRow, dataRowHelper.allCached.size() + 1)
    }
}

/** Добавляет строку в текущий буфер строк. */
boolean addRow(def rows, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    if (rowCells == null) {
        return true
    }
    def newRow = getNewRow(rowCells, columnCount, fileRowIndex, rowIndex)
    if (newRow == null) {
        return false
    }
    rows.add(newRow)
    return true
}

/**
 * Получить новую строку нф по строке из тф (*.rnu).
 *
 * @param rowCells список строк со значениями
 * @param columnCount количество колонок
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 *
 * @return вернет строку нф или null, если количество значений в строке тф меньше
 */
def getNewRow(String[] rowCells, def columnCount, def fileRowIndex, def rowIndex) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    def int colOffset = 1
    def int colIndex = 1

    // графа 2
    colIndex++
    newRow.regNumber = pure(rowCells[colIndex])

    // графа 3
    colIndex++
    newRow.tradeNumber = pure(rowCells[colIndex])

    // графа 4..7
    ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 8
    colIndex++
    newRow.signSecurity = getRecordIdImport(62, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // графа 9..13
    ['marketQuotation', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

def getReportPeriodEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return reportPeriodEndDate
}