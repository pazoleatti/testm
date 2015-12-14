package form_template.income.rnu25.v2014

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
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
        return
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
        if (!totalGroupsName.contains(row.regNumber)) {
            totalGroupsName.add(row.regNumber)
        }
    }

    def totalRow
    if (prevDataRows != null) {
        totalRow = dataRows.find { 'total'.equals(it.getAlias()) }
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
    for (def codeName : totalGroupsName) {
        // получить строки группы
        def rows = getGroupRows(dataRows, codeName)
        // получить алиас для подитоговой строки по ГРН
        def totalRowAlias = 'total' + codeName
        // получить посчитанную строку с итогами по ГРН
        def row = dataRows.find { totalRowAlias.equals(it.getAlias()) }
        // сформировать подитоговую строку ГРН с суммами
        def tmpRow = getCalcSubtotalsRow(rows, codeName, totalRowAlias)

        // сравнить строки
        if (row == null || isDiffRow(row, tmpRow, totalSumColumns)) {
            loggerError(row, "Итоговые значения по ГРН ${((!codeName || 'null'.equals(codeName?.trim())) ? "\"ГРН не задан\"" : codeName?.trim())} рассчитаны неверно!")
        }
    }

    // 18. Проверка итогового значений по всей форме
    if (totalRow != null) {
        checkTotalSum(dataRows, totalSumColumns, logger, !isBalancePeriod)
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
    return getTotalRow(dataRows, ((regNumber || 'null'.equals(regNumber?.trim())) ? "ГРН не задан" : regNumber?.trim()) + ' Итог', totalRowAlias)
}

/**
 * Сформировать итоговую строку с суммами.
 *
 * @param dataRows строки формы
 * @param regNumberValue значение графы "код классификации дохода"
 * @param alias алиас сформированной строки
 */
def getTotalRow(def dataRows, def regNumberValue, def alias) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
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
    def dataRows = formDataService.getDataRowHelper(formData).allCached
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
    sortRows(refBookService, logger, dataRows, getSubTotalRows(dataRows), getDataRow(dataRows, 'total'), true)
    dataRowHelper.saveSort()
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
}

void importTransportData() {
    checkBeforeGetXml(ImportInputStream, UploadFileName)
    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }
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
        // пропускаем заголовок
        rowCells = reader.readNext()
        if (isEmptyCells(rowCells)) {
            logger.error('Первой строкой должен идти заголовок, а не пустая строка')
        }
        // пропускаем пустую строку
        rowCells = reader.readNext()
        if (!isEmptyCells(rowCells)) {
            logger.error('Вторая строка должна быть пустой')
        }
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
    def totalRow = getCalcTotalRow(newRows)
    newRows.add(totalRow)

    showMessages(newRows, logger)

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && totalTF) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = ['lotSizePrev' : 4, 'lotSizeCurrent' : 5, 'reserve' : 6, 'cost' : 7,
                'costOnMarketQuotation' : 10, 'reserveCalcValue' : 11, 'reserveCreation' : 12, 'reserveRecovery' : 13]

        // сравнение контрольных сумм
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = totalTF.getCell(alias).value
            def v2 = totalRow.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR + " Из файла: $v1, рассчитано: $v2", totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
        // задать итоговой строке нф значения из итоговой строки тф
        totalSumColumns.each { alias ->
            totalRow[alias] = totalTF[alias]
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
        // очистить итоги
        totalSumColumns.each { alias ->
            totalRow[alias] = null
        }
    }

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
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}", fileRowIndex))
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

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 13
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = getColumnName(tmpRow, 'rowNumber')
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
        if (rowValues[INDEX_FOR_SKIP] == "Общий итог") {
            totalRowFromFile = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)

            allValues.remove(rowValues)
            rowValues.clear()
            continue
        } else if (rowValues[INDEX_FOR_SKIP].contains(" итог")) {
            def subTotalRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex, true)
            def index = (subTotalRow.fix ? subTotalRow.fix.indexOf(" итог") : 0)
            def key = (index > 0 ? subTotalRow.fix.substring(0, index) : null)
            if (key) {
                totalRowFromFileMap[key] = subTotalRow
            }

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

    // сравнение подитогов
    updateIndexes(rows)
    if (!totalRowFromFileMap.isEmpty()) {
        // итоговые значения по ГРН
        def tmpLastIndex = 0
        def i = 0
        totalRowFromFileMap.each { regNumber, subTotalRowFromFile ->
            // получить строки группы
            def groupRows = getGroupRows(rows, regNumber)
            // получить алиас для подитоговой строки по ГРН
            def totalRowAlias = 'total' + regNumber
            // сформировать подитоговую строку ГРН с суммами
            def subTotalRow = getCalcSubtotalsRow(groupRows, regNumber, totalRowAlias)
            // получить индекс последней строки в группе
            def lastRowIndex = (groupRows.isEmpty() ? tmpLastIndex: groupRows[groupRows.size() - 1].getIndex() + i)
            // вставить строку с итогами по ГРН
            rows.add(lastRowIndex, subTotalRow)
            i++

            subTotalRow.setIndex(lastRowIndex + 1)
            tmpLastIndex = subTotalRow.getIndex()
            compareSimpleTotalValues(subTotalRow, subTotalRowFromFile, groupRows, totalSumColumns, formData, logger, false)
        }
    }

    // сравнение итогов
    def totalRow = getCalcTotalRow(rows)
    rows.add(totalRow)
    updateIndexes(rows)
    if (totalRowFromFile) {
        compareSimpleTotalValues(totalRow, totalRowFromFile, rows, totalSumColumns, formData, logger, false)
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
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[0].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            ([(headerRows[0][0]): getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[0][2]): getColumnName(tmpRow, 'regNumber')]),
            ([(headerRows[0][3]): getColumnName(tmpRow, 'tradeNumber')]),
            ([(headerRows[0][4]): getColumnName(tmpRow, 'lotSizePrev')]),
            ([(headerRows[0][5]): getColumnName(tmpRow, 'lotSizeCurrent')]),
            ([(headerRows[0][6]): getColumnName(tmpRow, 'reserve')]),
            ([(headerRows[0][7]): getColumnName(tmpRow, 'cost')]),
            ([(headerRows[0][8]): getColumnName(tmpRow, 'signSecurity')]),
            ([(headerRows[0][9]): getColumnName(tmpRow, 'marketQuotation')]),
            ([(headerRows[0][10]): getColumnName(tmpRow, 'costOnMarketQuotation')]),
            ([(headerRows[0][11]): getColumnName(tmpRow, 'reserveCalcValue')]),
            ([(headerRows[0][12]): getColumnName(tmpRow, 'reserveCreation')]),
            ([(headerRows[0][13]): getColumnName(tmpRow, 'reserveRecovery')]),
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
 * @param isSubTotal подитоговая строка
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex, def isSubTotal = false) {
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа fix
    def colIndex = 1
    if (isSubTotal) {
        newRow.fix = values[colIndex]
    }

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