package form_template.income.rnu26.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
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
// графа 2  - issuer                    - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
// графа 3  - shareType                 - атрибут 847 - TYPE - «Типы акции», справочник 97 «Типы акции»
// графа 4  - tradeNumber
// графа 5  - currency                  - зависит от графы 2 - атрибут 810 - CODE_CUR - «Цифровой код валюты выпуска», справочник 84 «Ценные бумаги»
// графа 6  - lotSizePrev
// графа 7  - lotSizeCurrent
// графа 8  - reserveCalcValuePrev
// графа 9  - cost
// графа 10 - signSecurity              - атрибут 621 - CODE - «Код признака», справочник 62 «Признаки ценных бумаг»
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
        break
    case FormDataEvent.ADD_ROW:
        def columns = (getBalancePeriod() ? allColumns - ['rowNumber', 'currency']: editableColumns)
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
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        prevPeriodCheck()
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
def allColumns = ['rowNumber', 'fix', 'issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent',
        'reserveCalcValuePrev', 'cost', 'signSecurity', 'marketQuotation', 'rubCourse', 'marketQuotationInRub',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Редактируемые атрибуты (графа 2..4, 6, 7, 9..12)
@Field
def editableColumns = ['issuer', 'shareType', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent',
        'cost', 'signSecurity', 'marketQuotation', 'rubCourse']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1..3, 6..10, 13, 14)
@Field
def nonEmptyColumns = ['rowNumber', 'issuer', 'shareType', 'lotSizePrev', 'lotSizeCurrent',
        'reserveCalcValuePrev', 'cost', 'signSecurity', 'marketQuotationInRub', 'costOnMarketQuotation']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 6..9, 14..17)
@Field
def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev', 'cost',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

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
// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required) {
    if (value == null) {
        return null
    }
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

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
                      def boolean required = true) {
    if (!value) {
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    deleteAllAliased(dataRows)

    // отсортировать/группировать
    sort(dataRows)

    def reportDate = getReportDate()

    // данные предыдущего отчетного периода
    def prevDataRows = getPrevDataRows()

    // номер последний строки предыдущей формы
    def number = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    // список групп кодов классификации для которых надо будет посчитать суммы
    def totalGroupsName = []

    for (row in dataRows) {
        // графа 1
        row.rowNumber = ++number

        if (!getBalancePeriod() && !isConsolidated) {
            // строка из предыдущего периода
            def prevRow = getPrevRowByColumn4(prevDataRows, row.tradeNumber)
            // графа 6
            row.lotSizePrev = calc6(row, prevRow, prevDataRows)
            // графа 8
            row.reserveCalcValuePrev = calc8(prevRow)
            // графа 12 курс валют
            row.rubCourse = calc12(row, reportDate)
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
        if (row.issuer != null && !totalGroupsName.contains(row.issuer)) {
            totalGroupsName.add(row.issuer)
        }
    }

    // добавить строку "итого"
    dataRows.add(getCalcTotalRow(dataRows))

    // добавить подитоговые значения
    updateIndexes(dataRows)
    def i = 0
    for (def codeName : totalGroupsName) {
        // получить строки группы
        def rows = getGroupRows(dataRows, codeName)
        // получить алиас для подитоговой строки по ГРН
        def totalRowAlias = 'total' + rows[0].issuer.toString()
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
    def totalGroupsName = []

    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    // данные предыдущего отчетного периода
    def prevDataRows = getPrevDataRows()

    // 3. Проверка на полноту отражения данных предыдущих отчетных периодов (графа 15) в текущем отчетном периоде
    if (prevDataRows) {
        def missContract = []
        def severalContract = []
        prevDataRows.each { prevRow ->
            if (prevRow.getAlias() == null && prevRow.reserveCalcValue > 0) {
                def count = 0
                dataRows.each { row ->
                    if (row.tradeNumber == prevRow.tradeNumber) {
                        count++
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

    for (def row : dataRows) {
        if (row.getAlias() != null) {
             continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !getBalancePeriod())

        // 2. Проверка на уникальность поля «№ пп» (графа 1)
        if (++rowNumber != row.rowNumber) {
            loggerError(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // 4. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 8, 17)
        if (row.lotSizeCurrent == 0 && row.reserveCalcValuePrev != row.reserveRecovery) {
            logger.warn(errorMsg + 'Графы 8 и 17 неравны!')
        }

        // 5. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 9, 14, 15)
        if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
            logger.warn(errorMsg + 'Графы 9, 14 и 15 ненулевые!')
        }

        // 6. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6, 8, 17)
        if (row.lotSizePrev == 0 && (row.reserveCalcValuePrev != 0 || row.reserveRecovery != 0)) {
            loggerError(errorMsg + 'Графы 8 и 17 ненулевые!')
        }

        // 7. Проверка необращающихся акций (графа 10, 15, 16)
        def sign = getSign(row.signSecurity)
        if (sign == '-' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
            logger.warn(errorMsg + 'Акции необращающиеся, графы 15 и 16 ненулевые!')
        }

        // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
        def tmp = (row.reserveCalcValue ?: 0) - (row.reserveCalcValuePrev ?: 0)
        if (sign == '+' && tmp > 0 && row.reserveRecovery != 0) {
            loggerError(errorMsg + 'Акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
        }

        // 9. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 16)
        if (sign == '+' && tmp < 0 && row.reserveCreation != 0) {
            loggerError(errorMsg + 'Акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
        }

        // 10. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
        if (sign == '+' && tmp == 0 && (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
            loggerError(errorMsg + 'Акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
        }

        // 11. Проверка корректности формирования резерва (графа 8, 15, 16, 17)
        if ((row.reserveCalcValuePrev ?: 0) + (row.reserveCreation ?: 0) != (row.reserveCalcValue ?: 0) + (row.reserveRecovery ?: 0)) {
            loggerError(errorMsg + 'Резерв сформирован неверно!')
        }

        // 12. Проверка на положительные значения при наличии созданного резерва
        if (row.reserveCreation > 0 && (row.lotSizeCurrent < 0 || row.cost < 0 ||
                row.costOnMarketQuotation < 0 || row.reserveCalcValue < 0)) {
            logger.warn(errorMsg + 'Резерв сформирован. Графы 7, 9, 14 и 15 неположительные!')
        }

        def prevRow = null
        if (!getBalancePeriod() && !isConsolidated) {
            prevRow = getPrevRowByColumn4(prevDataRows, row.tradeNumber)

            // 13. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 6, 7 (за предыдущий период) )
            if (prevRow != null && row.lotSizePrev != prevRow.lotSizeCurrent) {
                logger.warn(errorMsg + "РНУ сформирован некорректно! Не выполняется условие: " +
                        "Если «графа 4» = «графа 4» формы РНУ-26 за предыдущий отчётный период, " +
                        "то «графа 6» = «графа 7» формы РНУ-26 за предыдущий отчётный период.")
            }

            // 14. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 8, 15 (за предыдущий период) )
            if (prevRow != null && row.reserveCalcValuePrev != prevRow.reserveCalcValue) {
                loggerError(errorMsg + "РНУ сформирован некорректно! Не выполняется условие: " +
                        "Если «графа 4» = «графа 4» формы РНУ-26 за предыдущий отчётный период, " +
                        "то «графа 8» = «графа 15» формы РНУ-26 за предыдущий отчётный период.")
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
        if (row.issuer != null && !totalGroupsName.contains(row.issuer)) {
            totalGroupsName.add(row.issuer)
        }

        // 19. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
        totalColumns.each { alias ->
            totalSums[alias] += (row.getCell(alias).value ?: 0)
        }
    }

    // 18. Проверка подитоговых значений
    for (def codeName : totalGroupsName) {
        // получить строки группы
        def rows = getGroupRows(dataRows, codeName)
        // получить алиас для подитоговой строки
        def totalRowAlias = 'total' + rows[0].issuer.toString()
        // получить посчитанную строку с итогами
        def subTotalRow
        try {
            subTotalRow = getDataRow(dataRows, totalRowAlias)
        } catch(IllegalArgumentException e) {
            def issuer = getRefBookValue(84, codeName.toLong())?.ISSUER?.value
            loggerError("Итоговые значения по эмитенту $issuer не рассчитаны! Необходимо рассчитать данные формы.")
            continue
        }
        // сформировать подитоговую строку ГРН с суммами
        def tmpRow = getCalcSubtotalsRow(rows, codeName, totalRowAlias)

        // сравнить строки
        if (isDiffRow(subTotalRow, tmpRow, totalColumns)) {
            def issuer = getRefBookValue(84, codeName.toLong())?.ISSUER?.value
            loggerError("Итоговые значения по эмитенту $issuer рассчитаны неверно!")
        }
    }

    // получение итоговой строки
    def totalRow = null
    try {
        totalRow = getDataRow(dataRows, 'total')
    } catch(IllegalArgumentException e) {
        loggerError("Итоговые значения не рассчитаны! Необходимо рассчитать данные формы.")
    }

    if (totalRow != null && prevDataRows) {
        def totalRowOld = getDataRow(prevDataRows, 'total')

        // 15. Проверка корректности заполнения РНУ (графа 6, 7 (за предыдущий период))
        if (totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
            loggerError("РНУ сформирован некорректно! Не выполняется условие: " +
                    "«Итого» по графе 6 = «Итого» по графе 7 формы РНУ-26 за предыдущий отчётный период.")
        }

        // 16. Проверка корректности заполнения РНУ (графа 8, 15 (за предыдущий период))
        if (totalRow.reserveCalcValuePrev != totalRowOld.reserveCalcValue) {
            loggerError("РНУ сформирован некорректно! Не выполняется условие: " +
                    "«Итого» по графе 8 = «Итого» по графе 15 формы РНУ-26 за предыдущий отчётный период.")
        }
    }

    // 19. Проверка итогового значений по всей форме
    if (totalRow != null) {
        checkTotalSum(dataRows, totalColumns, logger, !getBalancePeriod())
    }
}

/** Получить данные за предыдущий отчетный период. */
def getPrevDataRows() {
    if (getBalancePeriod() || isConsolidated) {
        return null
    }

    def formDataOld = formDataService.getFormDataPrev(formData, formDataDepartment.id)
    return formDataOld != null ? formDataService.getDataRowHelper(formDataOld).allCached : null
}

def calc6(def row, def prevRow, def hasPrev) {
    if (row.signSecurity == null) {
        return null
    }
    if (!hasPrev) {
        return roundValue(0, 0)
    }
    def tmp = 0
    if (prevRow) {
        def prev = getSign(prevRow.signSecurity)
        def curr = getSign(row.signSecurity)
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

//def calc11(def row) {
//    def tmp = row.marketQuotation // на случай ручного ввода
//    def currency = getRefBookValue(84, row.currency)?.CODE_CUR?.value
//    if (currency == '810') {
//        tmp = row.marketQuotationInRub
//    }
//    return roundValue(tmp, 6)
//}

def BigDecimal calc12(def row, def date) {
    return roundValue(getRate(row, date), 4)
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
    if (row.signSecurity == null) {
        return null
    }
    def tmp = 0
    if (getSign(row.signSecurity) == '+') {
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
def getSign(def sign) {
    return getRefBookValue(62, sign)?.CODE?.stringValue
}

def roundValue(def value, def int precision) {
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
void prevPeriodCheck() {
    if (!isConsolidated && !getBalancePeriod()) {
        formDataService.checkFormExistAndAccepted(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, true, logger, true)
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
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNumber'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 17, 1)

    def headerMapping = [
            (xml.row[0].cell[0]):  getColumnName(tmpRow, 'rowNumber'),
            (xml.row[0].cell[2]):  getColumnName(tmpRow, 'issuer'),
            (xml.row[0].cell[3]):  getColumnName(tmpRow, 'shareType'),
            (xml.row[0].cell[4]):  getColumnName(tmpRow, 'tradeNumber'),
            (xml.row[0].cell[5]):  getColumnName(tmpRow, 'currency'),
            (xml.row[0].cell[6]):  getColumnName(tmpRow, 'lotSizePrev'),
            (xml.row[0].cell[7]):  getColumnName(tmpRow, 'lotSizeCurrent'),
            (xml.row[0].cell[8]):  getColumnName(tmpRow, 'reserveCalcValuePrev'),
            (xml.row[0].cell[9]):  getColumnName(tmpRow, 'cost'),
            (xml.row[0].cell[10]): getColumnName(tmpRow, 'signSecurity'),
            (xml.row[0].cell[11]): getColumnName(tmpRow, 'marketQuotation'),
            (xml.row[0].cell[12]): getColumnName(tmpRow, 'rubCourse'),
            (xml.row[0].cell[13]): getColumnName(tmpRow, 'marketQuotationInRub'),
            (xml.row[0].cell[14]): getColumnName(tmpRow, 'costOnMarketQuotation'),
            (xml.row[0].cell[15]): getColumnName(tmpRow, 'reserveCalcValue'),
            (xml.row[0].cell[16]): getColumnName(tmpRow, 'reserveCreation'),
            (xml.row[0].cell[17]): getColumnName(tmpRow, 'reserveRecovery'),
            (xml.row[1].cell[0]):  '1'
    ]
    (2..17).each { index ->
        headerMapping.put((xml.row[1].cell[index]), index.toString())
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
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
        (getBalancePeriod() ? (allColumns - ['rowNumber', 'currency']) : editableColumns).each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        (getBalancePeriod() ? ['rowNumber', 'currency'] : autoFillColumns).each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        def xmlIndexCol = 2

        // графа 2 - атрибут 809 - ISSUER - «Эмитент», справочник 84 «Ценные бумаги»
        def record84 = getRecordImport(84, 'ISSUER', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        newRow.issuer = record84?.record_id?.value
        xmlIndexCol++

        // графа 3 - атрибут 847 - TYPE - «Типы акции», справочник 97 «Типы акции»
        newRow.shareType = getRecordIdImport(97, 'TYPE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 4
        newRow.tradeNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++

        if (record84 != null) {
            // графа 5 - зависит от графы 2 - атрибут 810 - CODE_CUR - "Цифровой код валюты выпуска", справочник 84 "Ценные бумаги"
            formDataService.checkReferenceValue(84, row.cell[xmlIndexCol].text(), record84?.CODE_CUR?.value, xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        }
        xmlIndexCol++

        // графа 6
        newRow.lotSizePrev = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 7
        newRow.lotSizeCurrent = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        xmlIndexCol++

        // графа 9
        newRow.cost = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 10 - атрибут 621 - CODE - «Код признака», справочник 62 «Признаки ценных бумаг»
        newRow.signSecurity = getRecordIdImport(62, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 11
        newRow.marketQuotation = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // графа 12 - абсолюбтное значение поля «Курс валюты» справочника «Курсы валют» валюты из «Графы 5» отчетную дату
        newRow.rubCourse = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
        xmlIndexCol++

        // + графа 13 - так как после импорта эта графа 13 не должна пересчитываться
        newRow.marketQuotationInRub = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
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
def getGroupRows(def dataRows, def issuer) {
    def rows = []
    dataRows.each { row ->
        if (row.getAlias() == null && row.issuer == issuer) {
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
def getCalcSubtotalsRow(def dataRows, def issuer, def totalRowAlias) {
    def record84 = getRefBookValue(84, issuer)
    def title = record84?.ISSUER?.value + ' итог'
    return getTotalRow(dataRows, title, totalRowAlias)
}

/**
 * Сформировать итоговую строку с суммами.
 *
 * @param dataRows строки формы
 * @param value эмитент
 * @param alias алиас сформированной строки
 */
def getTotalRow(def dataRows, def value, def alias) {
    def newRow = formData.createDataRow()
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
void sort(def dataRows) {
    dataRows.sort { def a, def b ->
        // графа 2  - issuer (справочник)
        // графа 4  - tradeNumber

        def valueA = (a.issuer ? getRefBookValue(84, a.issuer)?.ISSUER?.value : null)
        def valueB = (b.issuer ? getRefBookValue(84, b.issuer)?.ISSUER?.value : null)

        if (valueA == valueB) {
            return a.tradeNumber <=> b.tradeNumber
        }
        return valueA <=> valueA
    }
}

/**
 * Получить строку предыдущего периода, в которой совпадают значения графы 4.
 *
 * @param prevDataRows данные предыдущего периода
 * @param column4Value значение графы 4
 */
def getPrevRowByColumn4(def prevDataRows, def column4Value) {
    if (column4Value == null || column4Value == '' || !prevDataRows) {
        return null
    }
    for (def prevRow : prevDataRows) {
        if (prevRow.tradeNumber == column4Value) {
            return prevRow
        }
    }
    return null
}

// Получить курс валюты по id записи из справочнкиа ценной бумаги (84)
def getRate(def row, def Date date) {
    if (row.issuer == null) {
        return null
    }
    // получить запись (поле Цифровой код валюты выпуска) из справочника ценные бумаги (84) по id записи
    def code = getRefBookValue(84, row.issuer)?.CODE_CUR?.value

    // получить id записи из справочника валют (15) по цифровому коду валюты
    def recordId = getRecordId(15L, 'CODE', code?.toString(), row.getIndex(), getColumnName(row, 'issuer'), date, true)

    // получить запись (поле курс валюты) из справочника курс валют (22) по цифровому коду валюты
    def record22 = getRefBookRecord(22L, 'CODE_NUMBER', recordId?.toString(), date, row.getIndex(), null, true)
    return record22?.RATE?.value
}