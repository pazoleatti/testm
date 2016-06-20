package form_template.income.rnu25.v2014

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
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
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
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
        formDataService.consolidationSimple(formData, logger, userInfo)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.IMPORT:
        if (UploadFileName.endsWith(".rnu")) {
            importTransportData()
        } else {
            importData()
        }
        formDataService.saveCachedDataRows(formData, logger, formDataEvent, scriptStatusHolder)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
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

// Сортируемые атрибуты (графа 2, 3) совпадают с порядком граф
//@Field
//def sortColumns = ['regNumber', 'tradeNumber']

// Группируемые атрибуты (графа 2)
@Field
def groupColumns = ['regNumber']

// Проверяемые на пустые значения атрибуты (графа 1..3, 5..13)
@Field
def nonEmptyColumns = ['regNumber', 'tradeNumber', 'lotSizeCurrent', 'reserve', 'cost', 'signSecurity',
                       'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4..7, 10..13)
@Field
def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'costOnMarketQuotation',
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
                      def boolean required = false) {
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
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // удалить строку "итого" и "итого по ГРН: ..."
    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sortRows(dataRows, groupColumns)

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
    }
    // добавить строку "итого"
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
    if (dataRows.size() == 1) {
        return
    }
    updateIndexes(dataRows)

    // Добавление подитогов
    addAllAliased(dataRows, new ScriptUtils.CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, rows)
        }
    }, groupColumns)

    updateIndexes(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached

    def prevDataRows = getPrevDataRows()
    def currCountMap = getTradeNumberCountMap(dataRows)
    def prevCountMap = getTradeNumberCountMap(prevDataRows)
    def prevRowMap = getTradeNumberObjectMap(prevDataRows)
    if (prevDataRows != null && !prevDataRows.isEmpty() && dataRows.size() > 1) {
        // 1. Проверка на полноту отражения данных предыдущих отчетных периодов (графа 11)
        //      в текущем отчетном периоде (выполняется один раз для всего экземпляра)
        def missContract = []
        def severalContract = []
        prevDataRows.each { prevRow ->
            if (prevRow.getAlias() == null && prevRow.reserveCalcValue > 0) {
                def count = currCountMap[prevRow.tradeNumber]
                if (count == null) {
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
            def result = checkOld(row, 'lotSizePrev', 'lotSizeCurrent', prevRowMap)
            if (result) {
                loggerError(row, errorMsg + "РНУ сформирован некорректно! Не выполняется условие: «Графа 4» (${row.lotSizePrev}) текущей строки РНУ-25 за текущий период = «Графе 5» ($result) строки РНУ-25 за предыдущий период, значение «Графы 3» которой соответствует значению «Графы 3» РНУ-25 за текущий период.")
            }
        }
        // 12. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 6, 11 (за предыдущий период) )
        if (!isBalancePeriod() && !isConsolidated) {
            def result = checkOld(row, 'reserve', 'reserveCalcValue', prevRowMap)
            if (result) {
                loggerError(row, errorMsg + "РНУ сформирован некорректно! Не выполняется условие: «Графа 6» (${row.reserve}) текущей строки РНУ-25 за текущий период = «Графе 11» ($result) строки РНУ-25 за предыдущий период, значение «Графы 3» которой соответствует значению «Графы 3» РНУ-25 за текущий период.")
            }
        }
        // 15. Обязательность заполнения поля графы 1..3, 5..13
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())
        // 16. Арифметические проверки граф 6, 10..13
        if (!isBalancePeriod()) {
            needValue['reserve'] = calc6(prevRowMap, prevCountMap, row)
            needValue['costOnMarketQuotation'] = calc10(row)
            needValue['reserveCalcValue'] = calc11(row, sign)
            needValue['reserveCreation'] = calc12(row)
            needValue['reserveRecovery'] = calc13(row)
            checkCalc(row, arithmeticCheckAlias, needValue, logger, !isBalancePeriod())
        }
    }

    def totalRow = dataRows.find { 'total'.equals(it.getAlias()) }
    if (prevDataRows != null) {
        def totalRowOld = getDataRow(prevDataRows, 'total')

        // 13. Проверка корректности заполнения РНУ (графа 4, 5 (за предыдущий период))
        if (totalRow?.lotSizePrev != null && totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
            loggerError(totalRow, "РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе 4 (${totalRow.lotSizePrev}) = «Общий итог» по графе 5 (${totalRowOld.lotSizeCurrent}) Формы РНУ-25 за предыдущий отчетный период.")
        }
        // 14. Проверка корректности заполнения РНУ (графа 6, 11 (за предыдущий период))
        if (totalRow?.reserve != null && totalRow.reserve != totalRowOld.reserveCalcValue) {
            loggerError(totalRow, "РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе 6 (${totalRow.reserve})= «Общий итог» по графе 11 (${totalRowOld.reserveCalcValue}) формы РНУ-25 за предыдущий отчётный период")
        }
    }

    // 17. Проверка итоговых значений по ГРН
    // Проверка наличия всех фиксированных строк
    // Проверка отсутствия лишних фиксированных строк
    // Проверка итоговых значений по фиксированным строкам
    checkItog(dataRows)

    // 18. Проверка итогового значений по всей форме
    if (totalRow != null) {
        checkTotalSum(dataRows, totalColumns, logger, !isBalancePeriod)
    } else {
        loggerError(null, "Итоговые значения рассчитаны неверно!")
    }
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
    def newRow = formData.createStoreMessagingDataRow()
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
    if (prevRowMap != null && prevRow != null && row[curColumnName] != null && row[curColumnName] != prevRow[prevColumnName]) {
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
    return (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allSaved : null)
}

/** Получить общую итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def totalRow = getTotalRow('Общий итог', 'total')
    calcTotalSum(dataRows, totalRow, totalColumns)
    return totalRow
}

/**
 * Сформировать итоговую строку.
 *
 * @param title значение графы "код классификации дохода"
 * @param alias алиас сформированной строки
 */
def getTotalRow(def title, def alias) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.setAlias(alias)
    newRow.fix = title
    newRow.getCell('fix').colSpan = 2
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

/**
 * Получить подитоговую строку с заданными стилями.
 *
 * @param rowNumber номер строки
 * @param title надпись для "... Итого"
 * @param key ключ для сравнения подитоговых строк при импорте
 */
def getSubTotalRow(def rowNumber, def regNumber, def key) {
    def alias = 'total' + key.toString() + '#' + rowNumber
    def title = getTitle(regNumber)
    return getTotalRow(title, alias)
}

String getTitle (def regNumber) {
    return (!regNumber || 'null'.equals(regNumber?.trim()) ? "ГРН не задан" : regNumber?.trim()) + ' Итог'
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

/** Получить признак ценной бумаги. */
def getSign(def row) {
    return getRefBookValue(62, row.signSecurity)?.CODE?.value
}

BigDecimal roundTo2(BigDecimal value) {
    return value?.setScale(2, RoundingMode.HALF_UP)
}

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
void prevPeriodCheck() {
    if (formData.kind == FormDataKind.PRIMARY && !isBalancePeriod()) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, formData.kind, formData.departmentId,
                formData.reportPeriodId, true, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

def loggerError(def row, def msg) {
    if (isBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    // совпадает с порядком граф
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), getDataRow(dataRows, 'total'), true)
    dataRowHelper.saveSort()
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
}

void importTransportData() {
    checkTF(ImportInputStream, UploadFileName)

    int COLUMN_COUNT = 13
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def totalTF = null		// итоговая строка со значениями из тф для добавления
    def newRows = []

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    try {
        // проверить первые строки тф - заголовок и пустая строка
        checkFirstRowsTF(reader, logger)

        // грузим основные данные
        while ((rowCells = reader.readNext()) != null) {
            fileRowIndex++
            rowIndex++
            if (isEmptyCells(rowCells)) { // проверка окончания блока данных, пустая строка
                // итоговая строка тф
                rowCells = reader.readNext()
                if (rowCells != null) {
                    totalTF = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }
            newRows.add(getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex))
        }
    } finally {
        reader.close()
    }

    // подсчет итогов
    def totalRow = getTotalRow(newRows)
    newRows.add(totalRow)

    showMessages(newRows, logger)

    // сравнение итогов
    checkAndSetTFSum(totalRow, totalTF, totalColumns, totalTF?.getImportIndex(), logger, false)

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(newRows)
        formDataService.getDataRowHelper(formData).allCached = newRows
    }
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
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}'", fileRowIndex))
        return newRow
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

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 13
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = tmpRow.getCell('rowNumber').column.name
    String TABLE_END_VALUE = null
    int INDEX_FOR_SKIP = 1

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

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
    def totalRowFromFile = null
    def totalRowFromFileMap = [:]           // мапа для хранения строк подитогов со значениями из файла (стили простых строк)

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        rowIndex++
        // Пропуск итоговых строк
        if (rowValues[INDEX_FOR_SKIP]?.trim()?.equalsIgnoreCase("Общий итог")) {
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (rowValues[INDEX_FOR_SKIP].toLowerCase().contains(" итог")) {
            // для расчета уникального среди групп(groupColumns) ключа берем строку перед Подитоговой
            def key = !rows.isEmpty() ? getKey(rows[-1]) : null
            def subTotalRow = getNewSubTotalRowFromXls(key, rowValues, colOffset, fileRowIndex, rowIndex)

            // наш ключ - row.getAlias() до решетки. так как индекс после решетки не равен у расчитанной и импортированной подитогововых строк
            if (totalRowFromFileMap[subTotalRow.getAlias().split('#')[0]] == null) {
                totalRowFromFileMap[subTotalRow.getAlias().split('#')[0]] = []
            }
            totalRowFromFileMap[subTotalRow.getAlias().split('#')[0]].add(subTotalRow)
            rows.add(subTotalRow)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }
    updateIndexes(rows)

    // сравнение подитогов
    if (!totalRowFromFileMap.isEmpty()) {
        def tmpSubTotalRowsMap = calcSubTotalRowsMap(rows)
        tmpSubTotalRowsMap.each { subTotalRow, groupValues ->
            def totalRows = totalRowFromFileMap[subTotalRow.getAlias().split('#')[0]]
            if (totalRows) {
                totalRows.each { totalRow ->
                    compareTotalValues(totalRow, subTotalRow, totalColumns, logger, false)
                }
                totalRowFromFileMap.remove(subTotalRow.getAlias().split('#')[0])
            } else {
                rowWarning(logger, null, String.format(GROUP_WRONG_ITOG, groupValues))
            }
        }
        if (!totalRowFromFileMap.isEmpty()) {
            // для этих подитогов из файла нет групп
            totalRowFromFileMap.each { key, totalRows ->
                totalRows.each { totalRow ->
                    rowWarning(logger, totalRow, String.format(GROUP_WRONG_ITOG_ROW, totalRow.getIndex()))
                }
            }
        }
    }

    // сравнение итогов
    def totalRow = getTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
    if (totalRowFromFile) {
        compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalColumns, formData, logger, false)
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).allCached = rows
    }
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

    def headerMapping = [
            ([(headerRows[0][0]): tmpRow.getCell('rowNumber').column.name]),
            ([(headerRows[0][2]): tmpRow.getCell('regNumber').column.name]),
            ([(headerRows[0][3]): tmpRow.getCell('tradeNumber').column.name]),
            ([(headerRows[0][4]): tmpRow.getCell('lotSizePrev').column.name]),
            ([(headerRows[0][5]): tmpRow.getCell('lotSizeCurrent').column.name]),
            ([(headerRows[0][6]): tmpRow.getCell('reserve').column.name]),
            ([(headerRows[0][7]): tmpRow.getCell('cost').column.name]),
            ([(headerRows[0][8]): tmpRow.getCell('signSecurity').column.name]),
            ([(headerRows[0][9]): tmpRow.getCell('marketQuotation').column.name]),
            ([(headerRows[0][10]): tmpRow.getCell('costOnMarketQuotation').column.name]),
            ([(headerRows[0][11]): tmpRow.getCell('reserveCalcValue').column.name]),
            ([(headerRows[0][12]): tmpRow.getCell('reserveCreation').column.name]),
            ([(headerRows[0][13]): tmpRow.getCell('reserveRecovery').column.name]),
            ([(headerRows[1][0]): '1'])
    ]
    (2..13).each { index ->
        headerMapping.add(([(headerRows[1][index]): index.toString()]))
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
    def colIndex = 1

    // графа 2, 3
    ['regNumber', 'tradeNumber'].each { alias ->
        colIndex++
        newRow[alias] = values[colIndex]
    }

    // графа 4..7
    ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 8
    colIndex = 8
    newRow.signSecurity = getRecordIdImport(62, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 9..13
    ['marketQuotation', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

/**
 * Получить новую подитоговую строку нф по значениям из экселя.
 *
 * @param key ключ для сравнения подитоговых строк при импорте
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewSubTotalRowFromXls(def key, def values, def colOffset, def fileRowIndex, def rowIndex) {
    // графа fix
    def title = values[1]
    def name = title?.substring(0, title.toLowerCase().indexOf(' итог'))?.trim()

    def newRow = getSubTotalRow(rowIndex, name, key)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 4..7
    def colIndex = 3
    ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 9..13
    colIndex = 8
    ['marketQuotation', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

// Получить посчитанные подитоговые строки
def calcSubTotalRowsMap(def dataRows) {
    def tmpRows = dataRows.findAll { !it.getAlias() }
    // Добавление подитогов
    addAllAliased(tmpRows, new ScriptUtils.CalcAliasRow() {
        @Override
        DataRow<Cell> calc(int i, List<DataRow<Cell>> rows) {
            return calcItog(i, rows)
        }
    }, groupColumns)

    // сформировать мапу (строка подитога -> значения группы)
    def map = [:]
    def prevRow = null
    for (def row : tmpRows) {
        if (!row.getAlias()) {
            prevRow = row
            continue
        }
        if (row.getAlias() && prevRow) {
            map[row] = getValuesByGroupColumn(prevRow)
        }
    }

    return map
}

// Расчет подитогового значения
DataRow<Cell> calcItog(def int i, def List<DataRow<Cell>> dataRows) {
    def tmpRow = dataRows.get(i)
    def key = getKey(tmpRow)
    def newRow = getSubTotalRow(i, tmpRow?.regNumber, key)

    // Расчеты подитоговых значений
    def rows = []
    for (int j = i; j >= 0 && dataRows.get(j).getAlias() == null; j--) {
        rows.add(dataRows.get(j))
    }
    calcTotalSum(rows, newRow, totalColumns)

    return newRow
}

// Проверки подитоговых сумм
void checkItog(def dataRows) {
    // Рассчитанные строки итогов
    def testItogRowsMap = calcSubTotalRowsMap(dataRows)
    // Имеющиеся строки итогов
    def itogRows = dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
    // все строки, кроме общего итога
    def groupRows = dataRows.findAll { !'total'.equals(it.getAlias()) }
    def testItogRows = testItogRowsMap.keySet().asList()
    checkItogRows(groupRows, testItogRows, itogRows, groupColumns, logger, true, new ScriptUtils.GroupString() {
        @Override
        String getString(DataRow<Cell> row) {
            return getValuesByGroupColumn(row)
        }
    }, new ScriptUtils.CheckGroupSum() {
        @Override
        String check(DataRow<Cell> row1, DataRow<Cell> row2) {
            for (def alias : totalColumns) {
                if (row1[alias] != row2[alias]) {
                    return getColumnName(row1, alias)
                }
            }
            return null
        }
    }, new ScriptUtils.CheckDiffGroup() {
        @Override
        Boolean check(DataRow<Cell> row1, DataRow<Cell> row2, List<String> groupColumns) {
            if (groupColumns.find{ row1[it] != null } == null) {
                return null // для строк с пустыми графами группировки не надо проверять итоги
            }
            if (row1.getAlias() == null && row2.getAlias() == null) {
                return isDiffRow(row1, row2, groupColumns)
            } else {
                String value1 = (row1.getAlias() == null ? getTitle(row1.regNumber) : row1.fix) ?: ""
                String value2 = (row2.getAlias() == null ? getTitle(row2.regNumber) : row2.fix) ?: ""
                return !value1.equalsIgnoreCase(value2)
            }
        }
    })
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    // 2
    return (row.regNumber != null ? row.regNumber : 'ГРН не задан')
}

/** Получить уникальный ключ группы. */
def getKey(def row) {
    def key = ''
    groupColumns.each { def alias ->
        key = key + (row[alias] != null ? row[alias] : "").toString()
    }
    return key.toLowerCase().hashCode()
}