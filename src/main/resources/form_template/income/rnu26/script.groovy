package form_template.income.rnu26

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "(РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций,
 *                                                  РДР, ADR, GDR и опционов эмитента в целях налогообложения".
 * formTemplateId=325
 *
 * @author rtimerbaev
 */

// Признак периода ввода остатков
@Field
def boolean isBalancePeriod = null

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
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
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
        break
    case FormDataEvent.MIGRATION:
        migration()
        break
}

// графа 1  - rowNumber
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
def allColumns = ['rowNumber', 'issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent',
        'reserveCalcValuePrev', 'cost', 'signSecurity', 'marketQuotation', 'rubCourse', 'marketQuotationInRub',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Редактируемые атрибуты
@Field
def editableColumns = ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent',
        'reserveCalcValuePrev', 'cost', 'signSecurity', 'marketQuotation', 'rubCourse', 'marketQuotationInRub',
        'costOnMarketQuotation', 'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev', 'lotSizeCurrent', 'cost',
        'signSecurity', 'marketQuotation', 'rubCourse']

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
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            currentDate, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

/** Получить новую строку с заданными стилями. */
def getNewRow() {
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
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
    def number = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

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
    dataRows.eachWithIndex { row, i ->
        if (row.getAlias() == null) {
            if (tmp == null) {
                tmp = row.issuer
            }
            // если код расходы поменялся то создать новую строку "итого по Эмитента:..."
            if (tmp != row.issuer) {
                totalRows.put(i, getNewRow(tmp, totalColumns, sums, dataRowHelper))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по Эмитента:..."
            if (i == dataRows.size() - 2) {
                totalColumns.each {
                    sums[it] += (row.getCell(it).getValue() ?: 0)
                }
                totalRows.put(i + 1, getNewRow(row.issuer, totalColumns, sums, dataRowHelper))
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
    def i = 1
    totalRows.each { index, row ->
        dataRowHelper.insert(row, index + i++)
    }
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

        def index
        def errorMsg
        def hasTotal = false

        for (def row : dataRows) {
            if (row.getAlias() != null) {
                hasTotal = true
                continue
            }
            index = row.getIndex()
            errorMsg = "Строка $index: "

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
            tmp = (row.reserveCalcValue ?: 0) - row.reserveCalcValuePrev
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
            if (row.reserveCalcValuePrev + (row.reserveCreation ?: 0) != (row.reserveCalcValue ?: 0) + (row.reserveRecovery ?: 0)) {
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
                logger.warn("РНУ сформирован некорректно! " + errorMsg + "не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-26 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-26 за предыдущий отчётный период.")
            }

            // 12. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 8, 15 (за предыдущий период) )
            if (!getBalancePeriod() && !isConsolidated && checkOld(row, 'tradeNumber', 'reserveCalcValuePrev', 'reserveCalcValue', formDataOld)) {
                def curCol = 4
                def curCol2 = 4
                def prevCol = 8
                def prevCol2 = 15
                loggerError("РНУ сформирован некорректно! " + errorMsg + "не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-26 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-26 за предыдущий отчётный период.")
            }

            // 16. Проверка на уникальность поля «№ пп» (графа 1)
            for (def rowB : data.getAllCached()) {
                if (!row.equals(rowB) && row.rowNumber == rowB.rowNumber) {
                    loggerError('Нарушена уникальность номера по порядку!')
                }
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
            if (!totalGroupsName.contains(row.issuer)) {
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
            totalRow = data.getDataRow(data.getAllCached(), 'total')
            totalRowOld = data.getDataRow(dataOld.getAllCached(), 'total')

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
            def totalRow = data.getDataRow(data.getAllCached(), 'total')

            // 18. Проверка итоговых значений по эмитенту
            for (def codeName : totalGroupsName) {
                def row = data.getDataRow(data.getAllCached(), 'total' + getRowNumber(codeName, data))
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

    // Проверки соответствия НСИ
    checkNSI(15, row, 'currency')
    checkNSI(62, row, 'signSecurity')
}

/**
 * Получение импортируемых данных.
 */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }

    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }

    if (!fileName.contains('.r')) {
        logger.error('Формат файла должен быть *.rnu')
        return
    }

    def xmlString = importService.getData(is, fileName, 'cp866')
    if (xmlString == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        logger.error('Отсутствие значении после обработки потока данных')
        return
    }

    try {
        // добавить данные в форму
        def totalLoad = addData(xml)

        // расчетать, проверить и сравнить итоги
        if (totalLoad != null) {
            checkTotalRow(totalLoad)
        } else {
            logger.error("Нет итоговой строки.")
        }
    } catch (Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.message)
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
def getNewRow(def alias, def totalColumns, def sums, def data) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + getRowNumber(alias, data))
    newRow.issuer = alias + ' итог'
    setTotalStyle(newRow)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
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
    if (dataOld != null && !dataOld.getAllCached().isEmpty()) {
        for (def oldRow : dataOld.getAllCached()) {
            if (oldRow.getCell('tradeNumber').getValue() == row.tradeNumber) {
                return roundValue(oldRow.getCell('reserveCalcValue').getValue(), 2)
            }
        }
    }
    return 0
}

def BigDecimal calc13(def row) {
    if (row.marketQuotation != null && row.rubCourse != null) {
        return row.marketQuotation * row.rubCourse
    }
    return null
}

def BigDecimal calc14(def row) {
    def tmp = (row.marketQuotationInRub == null ? 0 : row.lotSizeCurrent * row.marketQuotationInRub)
    return roundValue(tmp, 2)
}

def BigDecimal calc15(def row) {
    def tmp
    if (getSign(row.signSecurity) == '+') {
        def a = (row.cost == null ? 0 : row.cost)
        tmp = (a - row.costOnMarketQuotation > 0 ? a - row.costOnMarketQuotation : 0)
    } else {
        tmp = 0
    }
    return roundValue(tmp, 2)
}

def BigDecimal calc16(def row) {
    def tmp = row.reserveCalcValue - row.reserveCalcValuePrev
    return roundValue((tmp > 0 ? tmp : 0), 2)
}

def BigDecimal calc17(def row) {
    def tmp = row.reserveCalcValue - row.reserveCalcValuePrev
    return roundValue((tmp < 0 ? tmp.abs() : 0), 2)
}

/**
 * Получить название графы по псевдониму.
 *
 * @param row строка
 * @param alias псевдоним графы
 */
def getColumnName(def row, def alias) {
    if (row != null && alias != null) {
        return row.getCell(alias).getColumn().getName().replace('%', '%%')
    }
    return ''
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 */
def addData(def xml) {
    Date date = new Date()

    def cache = [:]
    def data = formDataService.getDataRowHelper(formData)
    data.clear()
    def newRows = []
    def indexRow = 0


    for (def row : xml.row) {
        def newRow = getNewRow()

        indexRow++
        def indexCell = 1

        newRow.rowNumber = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
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
        newRow.currency = getRecords(15, 'CODE_2', row.cell[indexCell].text(), date, cache)
        indexCell++

        // графа 6
        newRow.lotSizePrev = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 7
        newRow.lotSizeCurrent = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 8
        newRow.reserveCalcValuePrev = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 9
        newRow.cost = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 10
        newRow.signSecurity = getRecords(62, 'CODE', row.cell[indexCell].text(), date, cache)
        indexCell++

        // графа 11
        newRow.marketQuotation = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 12
        newRow.rubCourse = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 13
        newRow.marketQuotationInRub = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 14
        newRow.costOnMarketQuotation = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 15
        newRow.reserveCalcValue = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 16
        newRow.reserveCreation = getNumber(row.cell[indexCell].text(), indexRow, indexCell)
        indexCell++

        // графа 17
        newRow.reserveRecovery = getNumber(row.cell[indexCell].text(), indexRow, indexCell)

        newRows.add(newRow)
    }
    data.insert(newRows, 1)

    // итоговая строка
    indexRow = 0
    if (xml.rowTotal.size() == 1) {
        indexRow++
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()

        // графа 6
        total.lotSizePrev = getNumber(row.cell[6].text(), indexRow, 6)

        // графа 7
        total.lotSizeCurrent = getNumber(row.cell[7].text(), indexRow, 7)

        // графа 8
        total.reserveCalcValuePrev = getNumber(row.cell[8].text(), indexRow, 8)

        // графа 9
        total.cost = getNumber(row.cell[9].text(), indexRow, 9)

        // графа 14
        total.costOnMarketQuotation = getNumber(row.cell[14].text(), indexRow, 14)

        // графа 15
        total.reserveCalcValue = getNumber(row.cell[15].text(), indexRow, 15)

        // графа 16
        total.reserveCreation = getNumber(row.cell[16].text(), indexRow, 16)

        // графа 17
        total.reserveRecovery = getNumber(row.cell[17].text(), indexRow, 17)

        return total
    } else {
        return null
    }
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
    loggerError("Не удалось найти запись в справочнике (id=$ref_id) с атрибутом $code равным $value!")
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
    return getRefBookValue(62, sign)?.CODE.stringValue
}

/**
 * Получить буквенный код валюты
 */
def getCurrency(def currencyCode) {
    return getRefBookValue(15, currencyCode)?.CODE_2.stringValue
}

/**
 * Получение первого rowNumber по issuer
 * @param alias
 * @param data
 * @return
 */
def getRowNumber(def alias, def data) {
    for (def row : data.getAllCached()) {
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
        totalRow.getCell(alias).setValue(getSum(data, alias))
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
void prevPeriodCheck() {
    if (!getBalancePeriod() && !isConsolidated && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        def formName = formData.getFormType().getName()
        throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
    }
}

void migration() {
    importData()
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.allCached
        def total = getCalcTotalRow(dataRows)
        dataRowHelper.insert(total, dataRows.size() + 1)
    }
}