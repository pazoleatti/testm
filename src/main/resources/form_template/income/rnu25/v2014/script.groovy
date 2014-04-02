package form_template.income.rnu25.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
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
// графа 8  - signSecurity          атрибут 621 CODE "Код признака" - справочник 62 "Признаки ценных бумаг"
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
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
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
def allColumns = ['rowNumber', 'fix', 'regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent',
        'reserve', 'cost', 'signSecurity', 'marketQuotation', 'costOnMarketQuotation',
        'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

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
def nonEmptyColumns = ['rowNumber', 'regNumber', 'tradeNumber', 'lotSizeCurrent', 'reserve',
        'cost', 'signSecurity', 'costOnMarketQuotation',
        'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 4..7, 10..13)
@Field
def totalSumColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'costOnMarketQuotation',
        'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

// Признак периода ввода остатков
@Field
def isBalancePeriod

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    def retValue = parseNumber(value, indexRow, indexCol, logger, true)
    return retValue
}

// Признак периода ввода остатков.
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
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
    // получить номер последний строки предыдущей формы если это не событие импорта
    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    dataRows.each { row ->
        // графа 1
        row.rowNumber = ++rowNumber

        if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY) {
            // графа 4
            row.lotSizePrev = calc4(prevDataRows, row)

            // графа 6
            row.reserve = calc6(prevDataRows, row)

            // графа 10
            row.costOnMarketQuotation = calc10(row)

            // графа 11
            def sign = getSign(row.signSecurity)
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
        def totalRowAlias = 'total' + rows[0].rowNumber.toString()
        // сформировать подитоговую строку ГРН с суммами
        def subTotalRow = getCalcSubtotalsRow(rows, codeName, totalRowAlias)
        // получить индекс последней строки в группе
        def lastRowIndex = rows[rows.size() - 1].getIndex() + i
        // вставить строку с итогами по ГРН
        dataRows.add(lastRowIndex, subTotalRow)
        i++
    }
    updateIndexes(dataRows)
    dataRowHelper.save(dataRows)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached

    def prevDataRows = getPrevDataRows()
    if (prevDataRows != null && !prevDataRows.isEmpty() && dataRows.size() > 1) {
        // 1. Проверка на полноту отражения данных предыдущих отчетных периодов (графа 11)
        //      в текущем отчетном периоде (выполняется один раз для всего экземпляра)
        def count
        def missContract = []
        def severalContract = []
        prevDataRows.each { prevRow ->
            if (prevRow.getAlias() != null && prevRow.reserveCalcValue > 0) {
                count = 0
                dataRows.each { row ->
                    if (row.getAlias() == null && row.tradeNumber == prevRow.tradeNumber) {
                        count += 1
                    }
                }
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

    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 2. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 6, 13)
        if (row.lotSizeCurrent == 0 && row.reserve != row.reserveRecovery) {
            logger.warn(errorMsg + 'графы 6 и 13 неравны!')
        }

        // 3. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 7, 10, 11)
        if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
            logger.warn(errorMsg + 'графы 7, 10 и 11 ненулевые!')
        }

        // 4. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 4, 6, 13)
        if (row.lotSizePrev == 0 && (row.reserve != 0 || row.reserveRecovery != 0)) {
            loggerError(errorMsg + 'графы 6 и 13 ненулевые!')
        }

        // 5. Проверка необращающихся акций (графа 8, 11, 12)
        def sign = getSign(row.signSecurity)
        if (sign == '-' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
            logger.warn(errorMsg + 'облигации необращающиеся, графы 11 и 12 ненулевые!')
        }

        // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
        if (row.reserveCalcValue != null && row.reserve != null &&
                sign == '+' && row.reserveCalcValue - row.reserve > 0 && row.reserveRecovery != 0) {
            loggerError(errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
        }

        // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 12)
        if (row.reserveCalcValue != null && row.reserve != null &&
                sign == '+' && row.reserveCalcValue - row.reserve < 0 && row.reserveCreation != 0) {
            loggerError(errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
        }

        // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
        if (row.reserveCalcValue != null && row.reserve != null &&
                sign == '+' && row.reserveCalcValue - row.reserve == 0 &&
                (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
            loggerError(errorMsg + 'облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
        }

        // 9. Проверка на положительные значения при наличии созданного резерва
        if (row.reserveCreation > 0 && row.lotSizeCurrent < 0 && row.cost < 0 &&
                row.costOnMarketQuotation < 0 && row.reserveCalcValue < 0) {
            logger.warn(errorMsg + 'резерв сформирован. Графы 5, 7, 10 и 11 неположительные!')
        }

        // 10. Проверка корректности создания резерва (графа 6, 11, 12, 13)
        if (row.reserve != null && row.reserveCreation != null &&
                row.reserveCalcValue != null && row.reserveRecovery != null &&
                row.reserve + row.reserveCreation != row.reserveCalcValue + row.reserveRecovery) {
            loggerError(errorMsg + 'резерв сформирован некорректно!')
        }

        // 11. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 4, 5 (за предыдущий период) )
        if (!isBalancePeriod() && !isConsolidated && checkOld(row, 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', prevDataRows)) {
            loggerError("РНУ сформирован некорректно! " + errorMsg + "Не выполняется условие: Если «графа 3» = «графа 3» формы РНУ-25 за предыдущий отчётный период, то «графа 4» = «графа 5» формы РНУ-25 за предыдущий отчётный период.")
        }

        // 12. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 6, 11 (за предыдущий период) )
        if (!isBalancePeriod() && !isConsolidated && checkOld(row, 'tradeNumber', 'reserve', 'reserveCalcValue', prevDataRows)) {
            loggerError("РНУ сформирован некорректно! " + errorMsg + "Не выполняется условие: Если «графа 3» = «графа 6» формы РНУ-25 за предыдущий отчётный период, то «графа 3» = «графа 11» формы РНУ-25 за предыдущий отчётный период.")
        }

        // 15. Обязательность заполнения поля графы 1..3, 5..13
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isBalancePeriod())

        // 16. Проверка на уникальность поля «№ пп» (графа 1)
        if (++rowNumber != row.rowNumber) {
            loggerError(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // 17. Арифметические проверки граф 6, 10..13
        if (!isBalancePeriod()) {
            needValue['reserve'] = calc6(prevDataRows, row)
            needValue['costOnMarketQuotation'] = calc10(row)
            needValue['reserveCalcValue'] = calc11(row, sign)
            needValue['reserveCreation'] = calc12(row)
            needValue['reserveRecovery'] = calc13(row)
            checkCalc(row, arithmeticCheckAlias, needValue, logger, false)
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
            loggerError("РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе 4 = «Общий итог» по графе 5 формы РНУ-25 за предыдущий отчётный период.")
        }
        // 14. Проверка корректности заполнения РНУ (графа 6, 11 (за предыдущий период))
        if (totalRow.reserve != totalRowOld.reserveCalcValue) {
            loggerError("РНУ сформирован некорректно! Не выполняется условие: «Общий итог» по графе 6 = «Общий итог» по графе 11 формы РНУ-25 за предыдущий отчётный период.")
        }
    }

    // 17. Проверка итоговых значений по ГРН
    for (def codeName : totalGroupsName) {
        // получить строки группы
        def rows = getGroupRows(dataRows, codeName)
        // получить алиас для подитоговой строки по ГРН
        def totalRowAlias = 'total' + rows[0].rowNumber.toString()
        // получить посчитанную строку с итогами по ГРН
        def row
        try {
            row = getDataRow(dataRows, totalRowAlias)
        } catch (IllegalArgumentException e) {
            loggerError("Итоговые значения по ГРН $codeName не рассчитаны! Необходимо рассчитать данные формы.")
            continue
        }
        // сформировать подитоговую строку ГРН с суммами
        def tmpRow = getCalcSubtotalsRow(rows, codeName, totalRowAlias)

        // сравнить строки
        if (isDiffRow(row, tmpRow, totalSumColumns)) {
            loggerError("Итоговые значения по ГРН $codeName рассчитаны неверно!")
        }
    }

    // 18. Проверка итогового значений по всей форме
    def totalRow = getDataRow(dataRows, 'total')
    def tmpTotalRow = getCalcTotalRow(dataRows)
    if (isDiffRow(totalRow, tmpTotalRow, totalSumColumns)) {
        loggerError('Итоговые значения рассчитаны неверно!')
    }
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
 * Сверить данные с предыдущим периодом.
 *
 * @param row строка нф текущего периода
 * @param likeColumnName псевдоним графы по которому ищутся соответствующиеся строки
 * @param curColumnName псевдоним графы текущей нф для второго условия
 * @param prevColumnName псевдоним графы предыдущей нф для второго условия
 * @param dataRowsOld строки нф предыдущего периода
 */
def checkOld(def row, def likeColumnName, def curColumnName, def prevColumnName, def dataRowsOld) {
    if (dataRowsOld == null || row.getCell(likeColumnName).value == null) {
        return false
    }
    for (def prevRow : dataRowsOld) {
        if (prevRow.getAlias() != null || prevRow.getAlias() != '') {
            continue
        }
        if (row.getCell(likeColumnName).value == prevRow.getCell(likeColumnName).value &&
                row.getCell(curColumnName).value != prevRow.getCell(prevColumnName).value) {
            return true
        }
    }
    return false
}

/** Получить строки за предыдущий отчетный период. */
def getPrevDataRows() {
    if (isBalancePeriod() || isConsolidated) {
        return null
    }
    def prevFormData = formDataService.getFormDataPrev(formData, formDataDepartment.id)
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
    def rows = []
    dataRows.each { row ->
        if (row.getAlias() == null && row.regNumber == regNumber) {
            rows.add(row)
        }
    }
    return rows
}

/**
 * Вычисление значения графы 4.
 *
 * @param dataRowsOld строки за предыдущий период
 * @param row строка текущего периода
 */
def BigDecimal calc4(def dataRowsOld, def row) {
    if (dataRowsOld == null) {
        return 0
    }

    // Строка с совпадающим значением графы 3
    def prevMatchRow = null
    for (def prevRow : dataRowsOld) {
        if (prevRow.tradeNumber == row.tradeNumber) {
            prevMatchRow = prevRow
            break
        }
    }

    if (prevMatchRow == null) {
        return 0
    } else {
        if (prevMatchRow.signSecurity != null && row.signSecurity != null) {
            def valPrev = getRefBookValue(62, prevMatchRow.signSecurity)
            def val = getRefBookValue(62, row.signSecurity)
            if (valPrev?.CODE?.stringValue == '+' && val?.CODE?.stringValue == '-') {
                return prevMatchRow.lotSizePrev
            }
            if (valPrev?.CODE?.stringValue == '-' && val?.CODE?.stringValue == '+') {
                return 0
            }
        }
    }

    // Иначе подставляется значение, введенное вручную
    return row.lotSizePrev
}

/**
 * Получить значение за предыдущий отчетный период для графы 6.
 *
 * @param dataRowsOld строки за предыдущий период
 * @param row строка текущего периода
 * @return возвращает найденое значение, иначе возвратит 0
 */
def BigDecimal calc6(def dataRowsOld, def row) {
    if (isConsolidated) {
        return row.reserve
    }
    if (row.tradeNumber == null) {
        return 0
    }
    def value = 0
    def count = 0
    if (dataRowsOld != null && !dataRowsOld.isEmpty()) {
        for (def rowOld : dataRowsOld) {
            if (rowOld.tradeNumber == row.tradeNumber) {
                value = rowOld.reserveCalcValue
                if (value != null) {
                    count += 1
                }
            }
        }
    }
    // если count не равно 1, то или нет формы за предыдущий период,
    // или нет соответствующей записи в предыдущем периода или записей несколько
    return roundTo2(count == 1 ? value : 0)
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
        tmp = (a > row.costOnMarketQuotation ? a - row.costOnMarketQuotation : 0)
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

/*
 * Получение импортируемых данных
 */

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

/* Заполнить форму данными */

void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
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
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        /* Графа 2 */
        newRow.regNumber = row.cell[2].text()

        /* Графа 3 */
        newRow.tradeNumber = row.cell[3].text()

        /* Графа 4 */
        newRow.lotSizePrev = parseNumber(row.cell[4].text(), xlsIndexRow, 4 + colOffset, logger, false)

        /* Графа 5 */
        newRow.lotSizeCurrent = parseNumber(row.cell[5].text(), xlsIndexRow, 5 + colOffset, logger, false)

        /* Графа 7 */
        newRow.cost = parseNumber(row.cell[7].text(), xlsIndexRow, 7 + colOffset, logger, false)

        /* Графа 8 */
        newRow.signSecurity = getRecordIdImport(62, 'CODE', row.cell[8].text(), xlsIndexRow, 8 + colOffset, false)

        /* Графа 9 */
        newRow.marketQuotation = parseNumber(row.cell[9].text(), xlsIndexRow, 9 + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

/** Получить признак ценной бумаги. */
def getSign(def recordId) {
    return getRefBookValue(62, recordId)?.CODE?.value
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
        loggerError("Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
void prevPeriodCheck() {
    if (!isBalancePeriod() && !isConsolidated && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        throw new ServiceException('Форма предыдущего периода не существует, или не находится в статусе «Принята»')
    }
}

def loggerError(def msg) {
    if (isBalancePeriod()) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}

// обновить индексы строк
def updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}