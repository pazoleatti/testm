package form_template.income.rnu26.v2014

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
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
        formDataService.saveCachedDataRows(formData, logger)
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
        formDataService.consolidationSimple(formData, logger)
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        if (UploadFileName.endsWith(".rnu")) {
            importTransportData()
        } else {
            importData()
        }
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        formDataService.saveCachedDataRows(formData, logger)
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
    dataRows = sort(dataRows)
    def reportDate = getReportDate()
    // данные предыдущего отчетного периода
    def prevDataRows = getPrevDataRows()
    // список групп кодов классификации для которых надо будет посчитать суммы
    def totalGroups = []
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
        // для подитоговых значений
        def Integer issuerId = getSubAliasName(row)
        if (issuerId != null && !totalGroups.contains(issuerId)) {
            totalGroups.add(issuerId)
        }
    }
    // добавить строку "итого"
    dataRows.add(getCalcTotalRow(dataRows))
    // добавить подитоговые значения
    updateIndexes(dataRows)
    def i = 0
    for (def Integer group : totalGroups) {
        // получить строки группы
        def rows = getGroupRows(dataRows, group)
        // получить алиас для подитоговой строки по ГРН
        def totalRowAlias = 'total' + group.toString()
        // сформировать подитоговую строку ГРН с суммами
        def subTotalRow = getCalcSubtotalsRow(rows, group, totalRowAlias)
        // получить индекс последней строки в группе
        def lastRowIndex = rows[rows.size() - 1].getIndex() + i
        // вставить строку с итогами по ГРН
        dataRows.add(lastRowIndex, subTotalRow)
        i++
    }
    updateIndexes(dataRows)
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (!dataRows) {
        return
    }
    // суммы строки общих итогов
    def totalSums = [:]
    totalColumns.each { alias ->
        if (totalSums[alias] == null) {
            totalSums[alias] = 0
        }
    }
    // список групп кодов классификации для которых надо будет посчитать суммы
    def totalGroups = []
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
            logger.warn(row, errorMsg + 'Резерв сформирован. Графы 7, 9, 14 и 15 неположительные!')
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
        // 18. Проверка итоговых значений по эмитентам
        def Integer issuerId = getSubAliasName(row)
        if (issuerId != null && !totalGroups.contains(issuerId)) {
            totalGroups.add(issuerId)
        }
        // 19. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
        totalColumns.each { alias ->
            totalSums[alias] += (row.getCell(alias).value ?: 0)
        }
    }
    // 18. Проверка подитоговых значений
    for (def Integer group : totalGroups) {
        // получить строки группы
        def rows = getGroupRows(dataRows, group)
        // получить алиас для подитоговой строки
        String totalRowAlias = 'total' + group.toString()
        // получить посчитанную строку с итогами
        def subTotalRow = dataRows.find { totalRowAlias.equals(it.getAlias()) }
        // сформировать подитоговую строку ГРН с суммами
        def tmpRow = getCalcSubtotalsRow(rows, group, totalRowAlias)
        // сравнить строки
        if (subTotalRow == null || isDiffRow(subTotalRow, tmpRow, totalColumns)) {
            def issuerName = getIssueName(group)
            loggerError(subTotalRow, "Итоговые значения по эмитенту $issuerName рассчитаны неверно!")
        }
    }
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
                if (count == 0) {
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
        def totalColumnsIndexMap = [
                'lotSizePrev'           : 6,
                'lotSizeCurrent'        : 7,
                'reserveCalcValuePrev'  : 8,
                'cost'                  : 9,
                'costOnMarketQuotation' : 14,
                'reserveCalcValue'      : 15,
                'reserveCreation'       : 16,
                'reserveRecovery'       : 17
        ]
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
        totalColumns.each { alias ->
            totalRow[alias] = totalTF[alias]
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
        // очистить итоги
        totalColumns.each { alias ->
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
        rowError(logger, newRow, String.format(ROW_FILE_WRONG + "Ошибка при подсчете количества граф '${rowCells.length}' вместо '${columnCount + 2}", fileRowIndex))
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

boolean isEmptyCells(def rowCells) {
    return rowCells.length == 1 && rowCells[0] == ''
}

// обновить индексы строк
def updateIndexes(def dataRows) {
    dataRows.eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

/**
 * Поиск строк с одинаковым кодом классификации дохода.
 *
 * @param dataRows строки формы
 * @param issuer эмитент
 */
def getGroupRows(def dataRows, def Integer issuer) {
    def rows = []
    dataRows.each { row ->
        if (row.getAlias() == null && getSubAliasName(row) == issuer) {
            rows.add(row)
        }
    }
    return rows
}

/** Получить общую итоговую строку с суммами. */
def getCalcTotalRow(def dataRows) {
    return getTotalRow(dataRows, 'Общий итог', 'total')
}

/**
 * Получить подитоговую строку.
 *
 * @param dataRows строки формы
 * @param issuer эмитент
 * @param totalRowAlias псевдоним сформированной строки
 */
def getCalcSubtotalsRow(def dataRows, def Integer issuer, def totalRowAlias) {
    def String title = getIssueName(issuer) + ' Итог'
    return getTotalRow(dataRows, title, totalRowAlias)
}

def String getIssueName(def Integer issuer) {
    String retStr = ""
    for (def entry : mapIssuers) {
        def key = entry.key
        def value = entry.value
        if (issuer.equals(value)) {
            retStr = key.substring(0, key.indexOf("|"))
            return (retStr.equals('null') ? '"Эмитент не задан"' : retStr)
        }
    }
    return retStr
}

/**
 * Сформировать итоговую строку с суммами.
 *
 * @param dataRows строки формы
 * @param value эмитент
 * @param alias алиас сформированной строки
 */
def getTotalRow(def dataRows, def value, def alias) {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()
    newRow.setAlias(alias)
    newRow.fix = value
    newRow.getCell('fix').colSpan = 2
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalColumns)
    return newRow
}

// Отсорировать данные (по графе 2, 4)
def sort(def dataRows) {
    dataRows.sort({ DataRow a, DataRow b ->
        sortRow(groupColumns, a, b)
    })
    return dataRows
}

int sortRow(List<String> params, DataRow a, DataRow b) {
    for (String param : params) {
        def aD = a.getCell(param).value
        def bD = b.getCell(param).value

        if (aD != bD) {
            return aD <=> bD
        }
    }
    return 0
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

/**
 * Получение провайдера с использованием кеширования.
 *
 * @param providerId
 * @return
 */
def getProvider(def long providerId) {
    if (!providerCache.containsKey(providerId)) {
        providerCache.put(providerId, refBookFactory.getDataProvider(providerId))
    }
    return providerCache.get(providerId)
}

def Integer getSubAliasName(def row) {
    if (row.getAlias() != null) {
        return -1
    }
    def String value = row.issuer + "|" + row.currency + "|" + row.signSecurity
    if (!mapIssuers.containsKey(value)) {
        mapIssuers.put(value, ++x)
    }
    return mapIssuers.get(value)
}

@Field
def Map<String, Integer> mapIssuers = [:]
@Field
def x = 0

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 17
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
        def index = 1
        totalRowFromFileMap.each { issuer, subTotalRowFromFile ->
            // получить строки группы
            def groupRows = getGroupRows(rows, index)
            // получить алиас для подитоговой строки по ГРН
            def totalRowAlias = 'total' + index
            // сформировать подитоговую строку ГРН с суммами
            def subTotalRow = getCalcSubtotalsRow(groupRows, index, totalRowAlias)
            // получить индекс последней строки в группе
            def lastRowIndex = (groupRows.isEmpty() ? tmpLastIndex: groupRows[groupRows.size() - 1].getIndex() + i)
            // вставить строку с итогами по ГРН
            rows.add(lastRowIndex, subTotalRow)
            i++
            index++

            subTotalRow.setIndex(lastRowIndex + 1)
            tmpLastIndex = subTotalRow.getIndex()
            if (subTotalRow.fix == ' итог') {
                subTotalRow.fix = issuer + subTotalRow.fix
            }
            compareSimpleTotalValues(subTotalRow, subTotalRowFromFile, groupRows, totalColumns, formData, logger, false)
        }
    }

    // сравнение итогов
    def totalRow = getCalcTotalRow(rows)
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
    if (headerRows.isEmpty()) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[0].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            (headerRows[0][0]) : getColumnName(tmpRow, 'rowNumber'),
            (headerRows[0][2]) : getColumnName(tmpRow, 'issuer'),
            (headerRows[0][3]) : getColumnName(tmpRow, 'shareType'),
            (headerRows[0][4]) : getColumnName(tmpRow, 'tradeNumber'),
            (headerRows[0][5]) : getColumnName(tmpRow, 'currency'),
            (headerRows[0][6]) : getColumnName(tmpRow, 'lotSizePrev'),
            (headerRows[0][7]) : getColumnName(tmpRow, 'lotSizeCurrent'),
            (headerRows[0][8]) : getColumnName(tmpRow, 'reserveCalcValuePrev'),
            (headerRows[0][9]) : getColumnName(tmpRow, 'cost'),
            (headerRows[0][10]): getColumnName(tmpRow, 'signSecurity'),
            (headerRows[0][11]): getColumnName(tmpRow, 'marketQuotation'),
            (headerRows[0][12]): getColumnName(tmpRow, 'rubCourse'),
            (headerRows[0][13]): getColumnName(tmpRow, 'marketQuotationInRub'),
            (headerRows[0][14]): getColumnName(tmpRow, 'costOnMarketQuotation'),
            (headerRows[0][15]): getColumnName(tmpRow, 'reserveCalcValue'),
            (headerRows[0][16]): getColumnName(tmpRow, 'reserveCreation'),
            (headerRows[0][17]): getColumnName(tmpRow, 'reserveRecovery'),
            (headerRows[1][0]) : '1'
    ]
    (2..17).each { index ->
        headerMapping.put((headerRows[1][index]), index.toString())
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

    // графа fix
    def colIndex = 1
    if (isSubTotal) {
        newRow.fix = values[colIndex]
    }
    // графа 2
    colIndex = 2
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
    if (getBalancePeriod()) {// в балансовом периоде грузим рассчитываемые графы
        // графа 8
        colIndex = 8
        newRow.reserveCalcValuePrev = getNumber(values[colIndex], fileRowIndex, colIndex + colOffset)
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
    }

    return newRow
}
