package form_template.income.rnu26.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

/**
 * Форма "(РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций,
 *                                                  РДР, ADR, GDR и опционов эмитента в целях налогообложения".
 * formTemplateId=1325
 *
 * @author rtimerbaev
 */

// графа 1  - rowNumber
// графа    - fix
// графа 2  - issuer                    - текст, было: зависит от графы 5 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
// графа 3  - shareType                 - атрибут 846 - CODE - «Код», справочник 97 «Типы акции»
// графа 4  - tradeNumber
// графа 5  - currency                  - справочник "Общероссийский классификатор валют", отображаемый атрибут "Код валюты. Буквенный",
//                                              было: атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
// графа 6  - lotSizePrev
// графа 7  - lotSizeCurrent
// графа 8  - reserveCalcValuePrev
// графа 9  - cost
// графа 10 - signSecurity              - справочник "Признак ценных бумаг", отображаемый атрибут "Код признака"
//                                              было: текст,
//                                              было: зависит от графы 5 - атрибут 869 - SIGN - «Признак ценной бумаги», справочник 84 «Ценные бумаги»
// графа 11 - marketQuotation
// графа 12 - rubCourse                 - абсолюбтное значение поля «Курс валюты» справочника «Курсы валют» валюты из «Графы 5» отчетную дату
// графа 13 - marketQuotationInRub
// графа 14 - costOnMarketQuotation
// графа 15 - reserveCalcValue
// графа 16 - reserveCreation
// графа 17 - reserveRecovery

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
        def columns = (getBalancePeriod() ? allColumns - ['rowNumber'] : editableColumns)
        formDataService.addRow(formData, currentDataRow, columns, allColumns - columns)
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
def allColumns = ['rowNumber', 'fix', 'issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent',
        'reserveCalcValuePrev', 'cost', 'signSecurity', 'marketQuotation', 'rubCourse', 'marketQuotationInRub',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Редактируемые атрибуты (графа 3..6, 7, 9, 11, 12)
@Field
def editableColumns = ['issuer', 'issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent',
        'cost', 'signSecurity', 'marketQuotation', 'rubCourse']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1..3, 5..9, 13, 14)
@Field
def nonEmptyColumns = ['issuer', 'currency', 'lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev', 'cost',
        'signSecurity', 'marketQuotationInRub', 'costOnMarketQuotation']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 6..9, 14..17)
@Field
def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev', 'cost',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Сортируемые атрибуты (графа 2, 4)
@Field
def sortColumns = ['issuer', 'tradeNumber']

// Группируемые атрибуты (графа 2)
@Field
def groupColumns = ['issuer']

// Дата окончания отчетного периода
@Field
def endDate = null

// Отчетная дата
@Field
def reportDay = null

// Признак периода ввода остатков
@Field
def Boolean isBalancePeriod = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName, def date,
                boolean required = true) {
    if (value == null) {
        return null
    }
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            date, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    if (recordId == null) {
        return null
    }
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    deleteAllAliased(dataRows)
    // отсортировать/группировать
    sortRows(dataRows, groupColumns)
    // данные предыдущего отчетного периода
    def prevDataRows = getPrevDataRows()
    def rowsMap = getTradeNumberObjectMap(prevDataRows)
    for (row in dataRows) {
        if (!getBalancePeriod() && !isConsolidated) {
            // строка из предыдущего периода
            def prevRow = rowsMap[row.tradeNumber]
            // графа 6
            row.lotSizePrev = calc6(row, prevRow, prevDataRows)
            // графа 8
            row.reserveCalcValuePrev = calc8(prevRow)
            // графа 13
            row.marketQuotationInRub = calc13(row)
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
    dataRows.add(getTotalRow(dataRows))
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

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (!dataRows) {
        return
    }
    // данные предыдущего отчетного периода
    def prevDataRows = getPrevDataRows()
    def rowsMap = getTradeNumberObjectMap(prevDataRows)
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !getBalancePeriod())
        // 4. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 8, 17)
        if (row.lotSizeCurrent == 0 && row.reserveCalcValuePrev != row.reserveRecovery) {
            rowWarning(logger, row, errorMsg + 'Графы 8 и 17 неравны!')
        }
        // 5. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 9, 14, 15)
        if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
            rowWarning(logger, row, errorMsg + 'Графы 9, 14 и 15 ненулевые!')
        }
        // 6. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6, 8, 17)
        if (row.lotSizePrev == 0 && (row.reserveCalcValuePrev != 0 || row.reserveRecovery != 0)) {
            loggerError(row, errorMsg + 'Графы 8 и 17 ненулевые!')
        }
        // 7. Проверка необращающихся акций (графа 10, 15, 16)
        def sign = getSign(row)
        if (sign == '-' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
            rowWarning(logger, row, errorMsg + 'Акции необращающиеся, графы 15 и 16 ненулевые!')
        }
        // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
        def tmp = (row.reserveCalcValue ?: 0) - (row.reserveCalcValuePrev ?: 0)
        if (sign == '+' && tmp > 0 && row.reserveRecovery != 0) {
            loggerError(row, errorMsg + 'Акции обращающиеся – резерв сформирован (восстановлен) некорректно! Графа 17 ненулевая')
        }
        // 9. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 16)
        if (sign == '+' && tmp < 0 && row.reserveCreation != 0) {
            loggerError(row, errorMsg + 'Акции обращающиеся – резерв сформирован (восстановлен) некорректно! Графа 16 ненулевая')
        }
        // 10. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
        if (sign == '+' && tmp == 0 && (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
            loggerError(row, errorMsg + 'Акции обращающиеся – резерв сформирован (восстановлен) некорректно! Графы 16 и 17 ненулевые')
        }
        // 11. Проверка корректности формирования резерва (графа 8, 15, 16, 17)
        if ((row.reserveCalcValuePrev ?: 0) + (row.reserveCreation ?: 0) != (row.reserveCalcValue ?: 0) + (row.reserveRecovery ?: 0)) {
            loggerError(row, errorMsg + 'Резерв сформирован неверно! Сумма граф 8 и 16 не равна сумме граф 15 и 17')
        }
        // 12. Проверка на положительные значения при наличии созданного резерва
        if (row.reserveCreation > 0 && (row.lotSizeCurrent < 0 || row.cost < 0 ||
                row.costOnMarketQuotation < 0 || row.reserveCalcValue < 0)) {
            rowWarning(logger, row, errorMsg + 'Резерв сформирован. Графы 7, 9, 14 и 15 неположительные!')
        }

        def prevRow = null
        if (!getBalancePeriod() && !isConsolidated) {
            prevRow = rowsMap[row.tradeNumber]
            // 13. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 6, 7 (за предыдущий период) )
            if (prevRow != null && row.lotSizePrev != null && row.lotSizePrev != prevRow.lotSizeCurrent) {
                rowWarning(logger, row, errorMsg + "РНУ сформирован некорректно! Не выполняется условие: «Графа 6» (${row.lotSizePrev}) текущей строки РНУ-26 за текущий период = «Графе 7» (${prevRow.lotSizeCurrent}) строки РНУ-26 за предыдущий период, значение «Графы 4» которой соответствует значению «Графы 4» РНУ-26 за текущий период.")
            }
            // 14. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 8, 15 (за предыдущий период) )
            if (prevRow != null && row.reserveCalcValuePrev != null && row.reserveCalcValuePrev != prevRow.reserveCalcValue) {
                loggerError(row, errorMsg + "РНУ сформирован некорректно! Не выполняется условие: «Графа 8» (${row.reserveCalcValuePrev}) текущей строки РНУ-26 за текущий период = «Графе 15» (${prevRow.reserveCalcValue}) строки РНУ-26 за предыдущий период, значение «Графы 4» которой соответствует значению «Графы 4» РНУ-26 за текущий период.")
            }
        }
        // 15, 16 проверки идут после проверки 18
        // 17. Арифметическая проверка графы 8, 13..17
        if (!getBalancePeriod()) {
            // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
            def needValue = [:]
            def arithmeticCheckAlias = ['marketQuotationInRub', 'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
            if (!isConsolidated) {
                needValue['lotSizePrev'] = calc6(row, prevRow, prevDataRows)
                needValue['reserveCalcValuePrev'] = calc8(prevRow)
                arithmeticCheckAlias.add(0, 'reserveCalcValuePrev')
                arithmeticCheckAlias.add(0, 'lotSizePrev')
            }
            needValue['marketQuotationInRub'] = calc13(row)
            needValue['costOnMarketQuotation'] = calc14(row)
            needValue['reserveCalcValue'] = calc15(row)
            needValue['reserveCreation'] = calc16(row)
            needValue['reserveRecovery'] = calc17(row)
            checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
        }
    }
    // 18. Проверка подитоговых значений
    // Проверка наличия всех фиксированных строк
    // Проверка отсутствия лишних фиксированных строк
    // Проверка итоговых значений по фиксированным строкам
    checkItog(dataRows)

    // получение итоговой строки
    def totalRow = dataRows.find { 'total'.equals(it.getAlias()) }
    if (totalRow != null && prevDataRows) {
        def totalRowOld = getDataRow(prevDataRows, 'total')

        // 15. Проверка корректности заполнения РНУ (графа 6, 7 (за предыдущий период))
        if (totalRow.lotSizePrev != null && totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
            loggerError(totalRow, "РНУ сформирован некорректно! Не выполняется условие: «Итого» по «Графе 6» (${totalRow.lotSizePrev}) = «Общий итог» по графе 7 (${totalRowOld.lotSizeCurrent}) формы РНУ-26 за предыдущий отчётный период")
        }

        // 16. Проверка корректности заполнения РНУ (графа 8, 15 (за предыдущий период))
        if (totalRow.reserveCalcValuePrev != null && totalRow.reserveCalcValuePrev != totalRowOld.reserveCalcValue) {
            loggerError(totalRow, "РНУ сформирован некорректно! Не выполняется условие: «Итого» по «Графе 8» (${totalRow.reserveCalcValuePrev}) = «Общий итог» по графе 15 (${totalRowOld.reserveCalcValue}) формы РНУ-26 за предыдущий отчётный период")
        }
    }
    // 19. Проверка итогового значений по всей форме
    if (totalRow != null) {
        checkTotalSum(dataRows, totalColumns, logger, !getBalancePeriod())
    } else {
        loggerError(null, "Итоговые значения рассчитаны неверно!")
    }
    // 3. Проверка на полноту отражения данных предыдущих отчетных периодов (графа 15) в текущем отчетном периоде
    if (prevDataRows) {
        def missContract = []
        def severalContract = []
        def countMap = getTradeNumberCountMap(dataRows)
        prevDataRows.each { prevRow ->
            if (prevRow.getAlias() == null && prevRow.reserveCalcValue > 0) {
                def tnum = prevRow.tradeNumber
                def count = countMap[tnum]
                if (count == null) {
                    missContract.add(tnum)
                } else if (count > 1) {
                    severalContract.add(tnum)
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
}

/** Получить данные за предыдущий отчетный период. */
def getPrevDataRows() {
    if (getBalancePeriod() || isConsolidated) {
        return null
    }

    def formDataOld = formDataService.getFormDataPrev(formData)
    return formDataOld != null ? formDataService.getDataRowHelper(formDataOld).allSaved : null
}

def calc6(def row, def prevRow, def hasPrev) {
    if (row.currency == null) {
        return null
    }
    if (!hasPrev) {
        return roundValue(0, 0)
    }
    def tmp = 0
    if (prevRow) {
        def prev = getSign(prevRow)
        def curr = getSign(row)
        if (prev == '+' && curr == '-') {
            tmp = prevRow.lotSizeCurrent
        } else if (prev == '-' && curr == '+') {
            tmp = 0
        } else {
            tmp = row.lotSizePrev
        }
    }
    return roundValue(tmp, 0)
}

def BigDecimal calc8(def prevRow) {
    def tmp = 0
    if (prevRow != null) {
        tmp = prevRow.reserveCalcValue
    }
    return roundValue(tmp, 2)
}

def BigDecimal calc13(def row) {
    if (row.marketQuotation != null && row.rubCourse != null) {
        return roundValue(row.marketQuotation * row.rubCourse, 6)
    }
    return null
}

def BigDecimal calc14(def row) {
    if (row.marketQuotationInRub == null) {
        return roundValue(0, 2)
    }
    if (row.lotSizeCurrent == null) {
        return null
    }
    return roundValue(row.lotSizeCurrent * row.marketQuotationInRub, 2)
}

def BigDecimal calc15(def row) {
    if (row.currency == null) {
        return null
    }
    def tmp = 0
    if (getSign(row) == '+') {
        if (row.costOnMarketQuotation == null) {
            return null
        }
        def a = (row.cost == null ? 0 : row.cost)
        def tmp2 = a - row.costOnMarketQuotation
        tmp = (tmp2 > 0 ? tmp2 : 0)
    }
    return roundValue(tmp, 2)
}

def BigDecimal calc16(def row) {
    if (row.reserveCalcValue == null || row.reserveCalcValuePrev == null) {
        return null
    }
    def tmp = row.reserveCalcValue - row.reserveCalcValuePrev
    return roundValue((tmp > 0 ? tmp : 0), 2)
}

def BigDecimal calc17(def row) {
    if (row.reserveCalcValue == null || row.reserveCalcValuePrev == null) {
        return null
    }
    def tmp = row.reserveCalcValue - row.reserveCalcValuePrev
    return roundValue((tmp < 0 ? tmp.abs() : 0), 2)
}

/** Получить признак ценной бумаги. */
def getSign(def row) {
    return getRefBookValue(62, row.signSecurity)?.CODE?.value
}

def roundValue(def value, def int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def row, def msg) {
    if (getBalancePeriod()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
void prevPeriodCheck() {
    if (formData.kind == FormDataKind.PRIMARY && !getBalancePeriod()) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, formData.kind, formData.departmentId,
                formData.reportPeriodId, true, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

def getReportDate() {
    if (reportDay == null) {
        reportDay = reportPeriodService.getReportDate(formData.reportPeriodId).time
    }
    return reportDay
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

def getBalancePeriod() {
    if (isBalancePeriod == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        isBalancePeriod = departmentReportPeriod.isBalance()
    }
    return isBalancePeriod
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def columns = sortColumns + (allColumns - sortColumns)
    // Сортировка (внутри групп)
    refBookService.dataRowsDereference(logger, dataRows, formData.getFormColumns().findAll { columns.contains(it.getAlias())})
    def newRows = []
    def tempRows = []
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            if (!tempRows.isEmpty()) {
                sortRows(tempRows, columns)
                newRows.addAll(tempRows)
                tempRows = []
            }
            newRows.add(row)
            continue
        }
        tempRows.add(row)
    }
    if (!tempRows.isEmpty()) {
        sortRows(tempRows, columns)
        newRows.addAll(tempRows)
    }
    dataRowHelper.setAllCached(newRows)

    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
}

// Получение подитоговых строк
def getSubTotalRows(def dataRows) {
    return dataRows.findAll { it.getAlias() != null && !'total'.equals(it.getAlias()) }
}

void importTransportData() {
    checkTF(ImportInputStream, UploadFileName)

    int COLUMN_COUNT = 17
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
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    (getBalancePeriod() ? (allColumns - ['rowNumber']) : editableColumns).each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    (getBalancePeriod() ? ['rowNumber'] : autoFillColumns).each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}'", fileRowIndex))
        return newRow
    }

    def int colOffset = 1

    // графа 2
    def colIndex = 2
    newRow.issuer = pure(rowCells[colIndex])
    // графа 3
    colIndex = 3
    newRow.shareType = getRecordIdImport(97, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    // графа 4
    colIndex = 4
    newRow.tradeNumber = pure(rowCells[colIndex])
    // графа 5
    colIndex = 5
    newRow.currency = getRecordIdImport(15, 'CODE_2', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    // графа 6
    colIndex = 6
    newRow.lotSizePrev = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // графа 7
    colIndex = 7
    newRow.lotSizeCurrent = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // графа 9
    colIndex = 9
    newRow.cost = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // графа 8
    colIndex = 8
    newRow.reserveCalcValuePrev = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // графа 10
    colIndex = 10
    newRow.signSecurity = getRecordIdImport(62, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    // графа 11
    colIndex = 11
    newRow.marketQuotation = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // графа 12
    colIndex = 12
    newRow.rubCourse = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // графа 13
    colIndex = 13
    newRow.marketQuotationInRub = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // графа 14
    colIndex = 14
    newRow.costOnMarketQuotation = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // графа 15
    colIndex = 15
    newRow.reserveCalcValue = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // графа 16
    colIndex = 16
    newRow.reserveCreation = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)
    // графа 17
    colIndex = 17
    newRow.reserveRecovery = getNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset)

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

/**
 * Получить подитоговую строку.
 *
 * @param issuer эмитент
 * @param key ключ для сравнения подитоговых строк при импорте
 */
def getSubTotalRow(def issuer, def key) {
    def alias = 'total' + key.toString()
    def title = getTitle(issuer)
    return getTotalRow(title, alias)
}

String getTitle(def issuer) {
    return (!issuer || 'null'.equals(issuer?.trim()) ? '"Эмитент не задан"' : issuer?.trim()) + ' Итог'
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
 * @param value эмитент
 * @param alias алиас сформированной строки
 */
def getTotalRow(def value, def alias) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.setAlias(alias)
    newRow.fix = value
    newRow.getCell('fix').colSpan = 2
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
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

def getRate(def row, def date) {
    def currency = row.currency
    if (date == null || currency == null) {
        return null
    }
    if (!isRubleCurrency(currency)) {
        def res = formDataService.getRefBookRecord(22, recordCache, providerCache, refBookCache,
                'CODE_NUMBER', currency.toString(), date, row.getIndex(), getColumnName(row, "currency"), logger, false)
        return res?.RATE?.numberValue
    }
    return 1;
}

def isRubleCurrency(def currencyCode) {
    return currencyCode != null ? (getRefBookValue(15, currencyCode)?.CODE?.stringValue in ['810', '643']) : false
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 17
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
            if (totalRowFromFileMap[subTotalRow.getAlias()] == null) {
                totalRowFromFileMap[subTotalRow.getAlias()] = []
            }
            totalRowFromFileMap[subTotalRow.getAlias()].add(subTotalRow)
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
            def totalRows = totalRowFromFileMap[subTotalRow.getAlias()]
            if (totalRows) {
                totalRows.each { totalRow ->
                    compareTotalValues(totalRow, subTotalRow, totalColumns, logger, false)
                }
                totalRowFromFileMap.remove(subTotalRow.getAlias())
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
            ([(headerRows[0][2]): tmpRow.getCell('issuer').column.name]),
            ([(headerRows[0][3]): tmpRow.getCell('shareType').column.name]),
            ([(headerRows[0][4]): tmpRow.getCell('tradeNumber').column.name]),
            ([(headerRows[0][5]): tmpRow.getCell('currency').column.name]),
            ([(headerRows[0][6]): tmpRow.getCell('lotSizePrev').column.name]),
            ([(headerRows[0][7]): tmpRow.getCell('lotSizeCurrent').column.name]),
            ([(headerRows[0][8]): tmpRow.getCell('reserveCalcValuePrev').column.name]),
            ([(headerRows[0][9]): tmpRow.getCell('cost').column.name]),
            ([(headerRows[0][10]): tmpRow.getCell('signSecurity').column.name]),
            ([(headerRows[0][11]): tmpRow.getCell('marketQuotation').column.name]),
            ([(headerRows[0][12]): tmpRow.getCell('rubCourse').column.name]),
            ([(headerRows[0][13]): tmpRow.getCell('marketQuotationInRub').column.name]),
            ([(headerRows[0][14]): tmpRow.getCell('costOnMarketQuotation').column.name]),
            ([(headerRows[0][15]): tmpRow.getCell('reserveCalcValue').column.name]),
            ([(headerRows[0][16]): tmpRow.getCell('reserveCreation').column.name]),
            ([(headerRows[0][17]): tmpRow.getCell('reserveRecovery').column.name]),
            ([(headerRows[1][0]): '1'])
    ]
    (2..17).each { index ->
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
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    (getBalancePeriod() ? (allColumns - ['rowNumber']) : editableColumns).each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    (getBalancePeriod() ? ['rowNumber'] : autoFillColumns).each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    // графа 2
    def colIndex = 2
    newRow.issuer = values[colIndex]
    // графа 3 - справочник 97 «Типы акции»
    colIndex = 3
    newRow.shareType = getRecordIdImport(97, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 4
    colIndex = 4
    newRow.tradeNumber = values[colIndex]
    // графа 5
    colIndex = 5
    newRow.currency = getRecordIdImport(15, 'CODE_2', values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 6
    colIndex = 6
    newRow.lotSizePrev = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 7
    colIndex = 7
    newRow.lotSizeCurrent = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 8
    colIndex = 8
    newRow.reserveCalcValuePrev = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 9
    colIndex = 9
    newRow.cost = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 10
    colIndex = 10
    newRow.signSecurity = getRecordIdImport(62, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 11
    colIndex = 11
    newRow.marketQuotation = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 12 - абсолюбтное значение поля «Курс валюты» справочника «Курсы валют» валюты из «Графы 5» отчетную дату
    colIndex = 12
    newRow.rubCourse = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    // + графа 13 - так как после импорта эта графа 13 не должна пересчитываться
    colIndex = 13
    newRow.marketQuotationInRub = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 14
    colIndex = 14
    newRow.costOnMarketQuotation = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 15
    colIndex = 15
    newRow.reserveCalcValue = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 16
    colIndex = 16
    newRow.reserveCreation = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
    // графа 17
    colIndex = 17
    newRow.reserveRecovery = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)

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

    def newRow = getSubTotalRow(name, key)
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    // графа 6..9
    def colIndex = 5
    ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev', 'cost'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 14..17
    colIndex = 13
    ['costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each { alias ->
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
    def newRow = getSubTotalRow(tmpRow?.issuer, key)

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
                String value1 = (row1.getAlias() == null ? getTitle(row1.issuer) : row1.fix) ?: ""
                String value2 = (row2.getAlias() == null ? getTitle(row2.issuer) : row2.fix) ?: ""
                return !value1.equalsIgnoreCase(value2)
            }
        }
    })
}

// Возвращает строку со значениями полей строки по которым идет группировка
String getValuesByGroupColumn(DataRow row) {
    // 2
    return (row.issuer != null ? row.issuer : 'Эмитент не задан')
}

/** Получить уникальный ключ группы. */
def getKey(def row) {
    def key = ''
    groupColumns.each { def alias ->
        key = key + (row[alias] != null ? row[alias] : "").toString()
    }
    return key.toLowerCase().hashCode()
}