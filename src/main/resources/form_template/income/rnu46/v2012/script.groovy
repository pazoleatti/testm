package form_template.income.rnu46.v2012

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Скрипт для РНУ-46 (rnu46.groovy).
 * Форма "(РНУ-46) Регистр налогового учёта «карточка по учёту основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества»".
 * formTypeId=342
 *
 * @author rtimerbaev
 * @author Stanislav Yasinskiy
 * @author Dmitriy Levykin
 */

// графа 1  - rowNumber
// графа 2  - invNumber
// графа 3  - name
// графа 4  - cost
// графа 5  - amortGroup
// графа 6  - usefulLife
// графа 7  - monthsUsed
// графа 8  - usefulLifeWithUsed
// графа 9  - specCoef
// графа 10 - cost10perMonth
// графа 11 - cost10perTaxPeriod
// графа 12 - cost10perExploitation
// графа 13 - amortNorm
// графа 14 - amortMonth
// графа 15 - amortTaxPeriod
// графа 16 - amortExploitation
// графа 17 - exploitationStart
// графа 18 - usefullLifeEnd
// графа 19 - rentEnd

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
        formDataService.addRow(formData, currentDataRow,
                isMonthBalance() ? balanceColumns : editableColumns,
                isMonthBalance() ? ['rowNumber', 'usefulLife'] : autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
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
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        importData()
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

// Редактируемые атрибуты
@Field
def editableColumns = ['invNumber', 'name', 'cost', 'amortGroup', 'monthsUsed', 'usefulLifeWithUsed',
                       'specCoef', 'exploitationStart', 'rentEnd']

@Field
def balanceColumns = ['invNumber', 'name', 'cost', 'amortGroup', 'monthsUsed', 'usefulLifeWithUsed',
                      'specCoef', 'cost10perMonth', 'cost10perTaxPeriod', 'cost10perExploitation', 'amortNorm', 'amortMonth',
                      'amortTaxPeriod', 'amortExploitation', 'exploitationStart', 'usefullLifeEnd', 'rentEnd']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['invNumber', 'name', 'cost', 'amortGroup', 'monthsUsed',
                       'usefulLifeWithUsed', 'specCoef', 'cost10perMonth', 'cost10perTaxPeriod', 'cost10perExploitation',
                       'amortNorm', 'amortMonth', 'amortTaxPeriod', 'amortExploitation', 'exploitationStart', 'usefullLifeEnd']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['rowNumber', 'cost10perMonth', 'cost10perTaxPeriod', 'cost10perExploitation',
                       'amortNorm', 'amortMonth', 'usefulLife', 'amortTaxPeriod', 'amortExploitation', 'usefullLifeEnd']

@Field
def totalColumns = ['cost', 'specCoef', 'cost10perMonth', 'cost10perTaxPeriod', 'cost10perExploitation', 'amortTaxPeriod', 'amortExploitation']

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache);
}

@Field
def formDataPrev = null // Форма предыдущего месяца
@Field
def dataRowHelperPrev = null // DataRowHelper формы предыдущего месяца
@Field
def isBalance = null
@Field
def check17 = Date.parse('dd.MM.yyyy', '01.01.2006')
@Field
def lastDay2001 = Date.parse('dd.MM.yyyy', '31.12.2001')
// Дата начала отчетного периода
@Field
def startDate = null
// Дата окончания отчетного периода
@Field
def endDate = null

//// Кастомные методы

// Поиск записи в справочнике по значению (для импорта)
def getRecordImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                    def boolean required = true) {
    if (value == null || value == '') {
        return null
    }
    return formDataService.getRefBookRecordImport(refBookId, recordCache, providerCache, refBookCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Получение формы предыдущего месяца
def getFormDataPrev() {
    if (formDataPrev == null) {
        formDataPrev = formDataService.getFormDataPrev(formData)
    }
    return formDataPrev
}

// Получение DataRowHelper формы предыдущего месяца
def getDataRowHelperPrev() {
    if (dataRowHelperPrev == null) {
        def formDataPrev = getFormDataPrev()
        if (!isMonthBalance() && formDataPrev != null && formDataPrev.state == WorkflowState.ACCEPTED) {
            dataRowHelperPrev = formDataService.getDataRowHelper(formDataPrev)
        }
    }
    return dataRowHelperPrev
}

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков и месяц первый в периоде.
def isMonthBalance() {
    if (isBalance == null) {
        def departmentReportPeriod = departmentReportPeriodService.get(formData.departmentReportPeriodId)
        if (!departmentReportPeriod.isBalance() || formData.periodOrder == null) {
            isBalance = false
        } else {
            isBalance = (formData.periodOrder - 1) % 3 == 0
        }
    }
    return isBalance
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    // Принятый отчет за предыдущий месяц
    def prevRowMap = [:]
    if (!isMonthBalance() && formData.kind == FormDataKind.PRIMARY) {
        def prevDataRows = getPrevDataRows()
        prevRowMap = getInvNumberObjectMap(prevDataRows)
    }

    for (def row in dataRows) {
        if (isMonthBalance() || formData.kind != FormDataKind.PRIMARY) {
            // Для периода ввода остатков расчитывается только порядковый номер
            continue;
        }

        def map = row.amortGroup == null ? null : getRefBookValue(71, row.amortGroup)

        // Строка из предыдущей формы с тем же инвентарным номером
        def prevRow = prevRowMap[row.invNumber]

        // Графа 8
        row.usefulLifeWithUsed = calc8(row)

        // Графа 10
        row.cost10perMonth = calc10(row, map)

        // графа 12
        row.cost10perExploitation = calc12(row, prevRow)

        // графа 18
        row.usefullLifeEnd = calc18(row)

        // графа 14
        row.amortMonth = calc14(row, prevRow)

        // Графа 11, 15, 16
        def calc11and15and16 = calc11and15and16(row, prevRow)
        row.cost10perTaxPeriod = calc11and15and16[0]
        row.amortTaxPeriod = calc11and15and16[1]
        row.amortExploitation = calc11and15and16[2]

        // графа 13
        row.amortNorm = calc13(row)
    }
}

// Получить строки за предыдущий отчетный период./
def getPrevDataRows() {
    def prevFormData = formDataService.getFormDataPrev(formData)
    return (prevFormData != null ? formDataService.getDataRowHelper(prevFormData)?.allSaved : null)
}

// Группирует данные по графе invNumber с сохранением одной из ссылок на соответствующую строку
// Результат: Map[invNumber:row]
def getInvNumberObjectMap(def rows) {
    def result = [:]
    rows.each {
        def inum = it.invNumber
        if (result[inum] == null) result[inum] = it
    }
    return result
}

// Ресчет графы 8
BigDecimal calc8(def row) {
    if (row.monthsUsed == null || row.amortGroup == null || row.specCoef == null) {
        return null
    }
    def map = getRefBookValue(71, row.amortGroup)
    def term = map.TERM.numberValue
    if (row.monthsUsed < term) {
        if (row.specCoef > 0) {
            return round((term - row.monthsUsed) / row.specCoef, 0)
        } else {
            return round(term - row.monthsUsed, 0)
        }
    }
    return row.usefulLifeWithUsed
}

// Ресчет графы 10
BigDecimal calc10(def row, def map) {
    def Integer group = map?.GROUP?.numberValue
    def result = null
    if ([1, 2, 8, 9, 10].contains(group) && row.cost != null) {
        result = round(row.cost * 0.1)
    } else if ((3..7).contains(group) && row.cost != null) {
        result = round(row.cost * 0.3)
    } else if (row.exploitationStart != null && row.exploitationStart < check17) {
        result = 0
    }
    return result
}

// Ресчет граф 11, 15, 16
BigDecimal[] calc11and15and16(def row, def prevRow) {
    def BigDecimal[] values = new BigDecimal[3]
    def reportMonth = formData.periodOrder

    // Calendar.JANUARY == 0, FormData нумерация с 1
    if (reportMonth == (Calendar.JANUARY + 1)) {
        values[0] = row.cost10perMonth
        values[1] = row.amortMonth
        values[2] = (prevRow != null ? prevRow.amortExploitation : 0)
    } else if (prevRow != null) {
        if (row.cost10perMonth != null && prevRow.cost10perTaxPeriod != null) {
            values[0] = row.cost10perMonth + prevRow.cost10perTaxPeriod
        }
        if (row.amortMonth != null && prevRow.amortTaxPeriod != null) {
            values[1] = row.amortMonth + prevRow.amortTaxPeriod
        }
        if (row.amortMonth != null && prevRow.amortExploitation != null) {
            values[2] = row.amortMonth + prevRow.amortExploitation
        }
    }
    return values
}

// Ресчет графы 12
BigDecimal calc12(def row, def prevRow) {
    def startDate = getReportPeriodStartDate()
    def endDate = getReportPeriodEndDate()

    if (prevRow == null || row.exploitationStart == null) {
        return 0 as BigDecimal
    }
    if (startDate < row.exploitationStart && row.exploitationStart < endDate) {
        return row.cost10perMonth
    }
    if (prevRow != null && row.cost10perMonth != null && prevRow.cost10perExploitation != null) {
        return row.cost10perMonth + prevRow.cost10perExploitation
    }
    return 0 as BigDecimal
}

// Ресчет графы 13
BigDecimal calc13(def row) {
    if (row == null || row.usefulLifeWithUsed == null || row.usefulLifeWithUsed == 0) {
        return null
    }
    return round(100 / row.usefulLifeWithUsed, 0)
}

// Ресчет графы 14
BigDecimal calc14(def row, def prevRow) {
    def endDate = getReportPeriodStartDate() - 1
    if (prevRow == null || row.usefullLifeEnd == null || row.cost10perExploitation == null || row.cost == null
            || prevRow?.cost == null || prevRow?.amortExploitation == null) {
        return 0 as BigDecimal
    }
    if (row.usefullLifeEnd > lastDay2001) {
        def date1 = Long.valueOf(row.usefullLifeEnd.format("MM")) + Long.valueOf(row.usefullLifeEnd.format("yyyy")) * 12
        def date2 = Long.valueOf(endDate.format("MM")) + Long.valueOf(endDate.format("yyyy")) * 12
        if ((date1 - date2) == 0) {
            return 0 as BigDecimal
        }
        return round((prevRow.cost - row.cost10perExploitation - prevRow.amortExploitation) / (date1 - date2))
    }
    return round(row.cost / 84)
}

// Ресчет графы 18
Date calc18(def row) {
    if (row.exploitationStart == null || row.usefulLifeWithUsed == null) {
        return null
    }
    def Calendar tmpCal = Calendar.getInstance()
    tmpCal.setTime(row.exploitationStart)
    tmpCal.add(Calendar.MONTH, row.usefulLifeWithUsed.intValue() + 1)
    tmpCal.set(Calendar.DAY_OF_MONTH, 0)
    return tmpCal.getTime()
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // Алиасы граф для арифметической проверки
    def arithmeticCheckAlias = ['usefulLifeWithUsed', 'cost10perMonth', 'cost10perTaxPeriod', 'amortTaxPeriod',
                                'amortExploitation', 'cost10perExploitation', 'amortNorm', 'amortMonth']

    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // Инвентарные номера
    def Set<String> invSet = new HashSet<String>()
    def prevRowMap = [:]
    if (formData.kind == FormDataKind.PRIMARY && !isMonthBalance()) {
        def prevDataRows = getPrevDataRows()
        prevRowMap = getInvNumberObjectMap(prevDataRows)
    }
    def inventoryNumbersOld = prevRowMap.keySet()

    // Строки в предыдущих формах(по месяцам)
    def monthsRowMap = [:]
    for (def month = formData.periodOrder - 1; month >= 1; month--) {
        def prevFormData = formDataService.getLast(formData.formType.id, formData.kind, formData.departmentId,
                formData.reportPeriodId, month, formData.comparativePeriodId, formData.accruing)
        if (prevFormData != null && prevFormData.state == WorkflowState.ACCEPTED) {
            def prevDataRows = formDataService.getDataRowHelper(prevFormData).allSaved
            monthsRowMap[month] = getInvNumberObjectMap(prevDataRows)
        } else {
            monthsRowMap[month] = [:]
        }
    }

    for (def row : dataRows) {
        def map = null
        if (row.amortGroup != null) {
            map = getRefBookValue(71, row.amortGroup)
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение (графа 1..18)
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, !isMonthBalance())

        // 2. Проверка на уникальность поля «инвентарный номер»
        if (invSet.contains(row.invNumber)) {
            loggerError(row, errorMsg + "Инвентарный номер не уникальный!")
        } else {
            invSet.add(row.invNumber)
        }

        // 5. Проверка на нулевые значения (графа 9, 10, 11, 13, 14, 15)
        if (row.specCoef == 0 &&
                row.cost10perMonth == 0 &&
                row.cost10perTaxPeriod == 0 &&
                row.amortNorm &&
                row.amortMonth == 0 &&
                row.amortTaxPeriod) {
            loggerError(row, errorMsg + 'Все суммы по операции нулевые!')
        }

        if (formData.kind == FormDataKind.PRIMARY) {

            def prevRow = prevRowMap[row.invNumber]
            def prevSum = getYearSum(['cost10perMonth', 'amortMonth'], row, monthsRowMap)

            // 6. Проверка суммы расходов в виде капитальных вложений с начала года
            if (prevRow == null ||
                    row.cost10perTaxPeriod == null ||
                    row.cost10perMonth == null ||
                    prevRow.cost10perTaxPeriod == null ||
                    row.cost10perTaxPeriod < row.cost10perMonth ||
                    row.cost10perTaxPeriod != row.cost10perMonth + prevRow.cost10perTaxPeriod ||
                    row.cost10perTaxPeriod != prevSum.cost10perMonth) {
                loggerError(row, errorMsg + 'Неверная сумма расходов в виде капитальных вложений с начала года!')
            }

            // 7. Проверка суммы начисленной амортизации с начала года
            if (prevRow == null ||
                    row.amortTaxPeriod == null ||
                    row.amortMonth == null ||
                    row.amortTaxPeriod < row.amortMonth ||
                    row.amortTaxPeriod != row.amortMonth + prevRow.amortTaxPeriod ||
                    row.amortTaxPeriod != prevSum.amortMonth) {
                loggerError(row, errorMsg + 'Неверная сумма начисленной амортизации с начала года!')
            }

            // 8. Арифметические проверки расчета граф 8, 10-16, 18
            needValue['usefulLifeWithUsed'] = calc8(row)
            needValue['cost10perMonth'] = calc10(row, map)
            needValue['cost10perExploitation'] = calc12(row, prevRow)
            needValue['amortNorm'] = calc13(row)
            needValue['amortMonth'] = calc14(row, prevRow)
            def calc11and15and16 = calc11and15and16(row, prevRow)
            needValue['cost10perTaxPeriod'] = calc11and15and16[0]
            needValue['amortTaxPeriod'] = calc11and15and16[1]
            needValue['amortExploitation'] = calc11and15and16[2]

            // Для проверок
            // logger.info("10 = " + needValue['cost10perMonth'] + " : " + row.cost10perMonth)
            // logger.info("11 = " + needValue['cost10perTaxPeriod'] + " : " + row.cost10perTaxPeriod)
            // logger.info("12 = " + needValue['cost10perExploitation'] + " : " + row.cost10perExploitation)

            // logger.info("14 = " + needValue['amortMonth'] + " : " + row.amortMonth)
            // logger.info("15 = " + needValue['amortTaxPeriod'] + " : " + row.amortTaxPeriod)
            // logger.info("16 = " + needValue['amortExploitation'] + " : " + row.amortExploitation)

            checkCalc(row, arithmeticCheckAlias, needValue, logger, !isMonthBalance())

            if (row.usefullLifeEnd != calc18(row)) {
                loggerError(row, errorMsg + "Неверное значение графы: ${getColumnName(row, 'usefullLifeEnd')}!")
            }
        }
    }

    // 10. Проверки существования необходимых экземпляров форм
    if (formData.kind == FormDataKind.PRIMARY && !isMonthBalance()) {
        for (def inv in invSet) {
            if (!inventoryNumbersOld.contains(inv)) {
                logger.warn('Отсутствуют данные за прошлые отчетные периоды!')
                break
            }
        }
    }
}
// Округление
def BigDecimal round(BigDecimal value, def int precision = 2) {
    return value?.setScale(precision, RoundingMode.HALF_UP)
}

// Получение суммы по графе всех предыдущих принятых форм и по графе текущей формы
def getYearSum(def aliases, def rowCurrent, def monthsRowMap) {
    def retVal = [:]

    for (def alias : aliases) {
        retVal[alias] = 0
    }

    // Сумма в текущей форме
    for (def alias : aliases) {
        def val = rowCurrent.get(alias)
        retVal[alias] += val == null ? 0 : val
    }
    // Сумма в предыдущих формах
    for (def month = formData.periodOrder - 1; month >= 1; month--) {
        def row = monthsRowMap[month][rowCurrent.invNumber]
        if (row) {
            for (def alias : aliases) {
                def val = row.get(alias)
                retVal[alias] += val == null ? 0 : val
            }
        }
    }
    return retVal
}

void importTransportData() {
    ScriptUtils.checkTF(ImportInputStream, UploadFileName)

    int COLUMN_COUNT = 19
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\0'

    String[] rowCells
    int fileRowIndex = 2    // номер строки в файле (1, 2..). Начинается с 2, потому что первые две строки - заголовок и пустая строка
    int rowIndex = 0        // номер строки в НФ
    def total = null
    def rows = []

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
        if (rowCells == null || !isEmptyCells(rowCells)) {
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
                    total = getNewRow(rowCells, COLUMN_COUNT, ++fileRowIndex, rowIndex)
                }
                break
            }
            rows.add(getNewRow(rowCells, COLUMN_COUNT, fileRowIndex, rowIndex))
        }
    } finally {
        reader.close()
    }

    showMessages(rows, logger)

    def totalRow = getDataRow(dataRows, 'total')
    rows.add(totalRow)

    // сравнение итогов
    if (!logger.containsLevel(LogLevel.ERROR) && total) {
        // мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки)
        def totalColumnsIndexMap = [
                'cost'                 : 4,
                'specCoef'             : 9,
                'cost10perMonth'       : 10,
                'cost10perTaxPeriod'   : 11,
                'cost10perExploitation': 12,
                'amortTaxPeriod'       : 15,
                'amortExploitation'    : 16,
        ]
        totalColumnsIndexMap.keySet().asList().each { alias ->
            totalRow.getCell(alias).setValue(BigDecimal.ZERO, null)
        }
        // подсчет итогов
        for (def row : newRows) {
            if (row.getAlias()) {
                continue
            }
            totalColumnsIndexMap.keySet().asList().each { alias ->
                def value1 = totalRow.getCell(alias).value
                def value2 = (row.getCell(alias).value ?: BigDecimal.ZERO)
                totalRow.getCell(alias).setValue(value1 + value2, null)
            }
        }

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
        // задать итоговой строке нф значения из итоговой строки тф
        totalColumns.each { alias ->
            totalRow[alias] = total[alias]
        }
    } else {
        logger.warn("В транспортном файле не найдена итоговая строка")
        // очистить итоги
        totalColumns.each { alias ->
            totalRow[alias] = null
        }
    }

    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
        formDataService.getDataRowHelper(formData).allCached = rows
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
    def newRow = formData.createDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    if (isMonthBalance()) {
        balanceColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        newRow.getCell('rowNumber').setStyleAlias('Автозаполняемая')
        newRow.getCell('usefulLife').setStyleAlias('Автозаполняемая')
    } else {
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }
    }

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    def int colOffset = 1
    def colIndex

    // графа 2
    colIndex = 2
    newRow.invNumber = pure(rowCells[colIndex])
    // графа 3
    colIndex = 3
    newRow.name = pure(rowCells[colIndex])
    // графа 4
    colIndex = 4
    newRow.cost = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    // графа 5
    colIndex = 5
    def record71 = getRecordImport(71, 'GROUP', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)
    newRow.amortGroup = record71?.record_id?.value
    // графа 6
    colIndex = 6
    if (record71 != null) {
        // графа 6 - зависит от графы 5 - атрибут 645 - TERM - "Срок полезного использования (месяцев)", справочник 71 "Амортизационные группы"
        formDataService.checkReferenceValue(71, pure(rowCells[colIndex]), record71?.TERM?.value?.toString(), fileRowIndex, colIndex + colOffset, logger, false)
    }
    // графа 7..16
    ['monthsUsed', 'usefulLifeWithUsed', 'specCoef', 'cost10perMonth', 'cost10perTaxPeriod',
     'cost10perExploitation', 'amortNorm', 'amortMonth', 'amortTaxPeriod', 'amortExploitation'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 17..19
    ['exploitationStart', 'usefullLifeEnd', 'rentEnd'].each { alias ->
        colIndex++
        newRow[alias] = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}

String pure(String cell) {
    return StringUtils.cleanString(cell)?.intern()
}

// Начальная дата отчетного периода (для ежемесячной формы)
def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return startDate
}

// Конечная дата отчетного периода (для ежемесячной формы)
def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

def loggerError(def row, def msg) {
    if (isMonthBalance()) {
        rowWarning(logger, row, msg)
    } else {
        rowError(logger, row, msg)
    }
}

void prevPeriodCheck() {
    if (!isMonthBalance() && formData.kind == FormDataKind.PRIMARY) {
        formDataService.checkMonthlyFormExistAndAccepted(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, formData.periodOrder, true, logger, true, formData.comparativePeriodId, formData.accruing)
    }
}

void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows)
    }
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 19
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
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        updateIndexes(rows)
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
    checkHeaderSize(headerRows[headerRows.size() - 1].size(), headerRows.size(), colCount, rowCount)

    def headerMapping = [
            ([(headerRows[0][0]) : getColumnName(tmpRow, 'rowNumber')]),
            ([(headerRows[0][1]) : getColumnName(tmpRow, 'invNumber')]),
            ([(headerRows[0][2]) : getColumnName(tmpRow, 'name')]),
            ([(headerRows[0][3]) : getColumnName(tmpRow, 'cost')]),
            ([(headerRows[0][4]) : getColumnName(tmpRow, 'amortGroup')]),
            ([(headerRows[0][5]) : getColumnName(tmpRow, 'usefulLife')]),
            ([(headerRows[0][6]) : getColumnName(tmpRow, 'monthsUsed')]),
            ([(headerRows[0][7]) : getColumnName(tmpRow, 'usefulLifeWithUsed')]),
            ([(headerRows[0][8]) : getColumnName(tmpRow, 'specCoef')]),
            ([(headerRows[0][9]) : '10% (30%) от первоначальной стоимости, включаемые в расходы']),
            ([(headerRows[1][9]) : 'За месяц']),
            ([(headerRows[1][10]): 'с начала налогового периода']),
            ([(headerRows[1][11]): 'с даты ввода в эксплуатацию']),
            ([(headerRows[0][12]): getColumnName(tmpRow, 'amortNorm')]),
            ([(headerRows[0][13]): 'Сумма начисленной амортизации']),
            ([(headerRows[1][13]): 'за месяц']),
            ([(headerRows[1][14]): 'с начала налогового периода']),
            ([(headerRows[1][15]): 'с даты ввода в эксплуатацию']),
            ([(headerRows[0][16]): getColumnName(tmpRow, 'exploitationStart')]),
            ([(headerRows[0][17]): getColumnName(tmpRow, 'usefullLifeEnd')]),
            ([(headerRows[0][18]): getColumnName(tmpRow, 'rentEnd')])
    ]
    (1..19).each {
        headerMapping.add(([(headerRows[2][it - 1]): it.toString()]))
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
    if (isMonthBalance()) {
        balanceColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        newRow.getCell('rowNumber').setStyleAlias('Автозаполняемая')
        newRow.getCell('usefulLife').setStyleAlias('Автозаполняемая')
    } else {
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }
    }

    // графа 2
    def colIndex = 1
    newRow.invNumber = values[colIndex]

    // графа 3
    colIndex++
    newRow.name = values[colIndex]

    // графа 4
    colIndex++
    newRow.cost = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 5 - атрибут 643 - GROUP - "Группа", справочник 71 "Амортизационные группы"
    colIndex++
    def record71 = getRecordImport(71, 'GROUP', values[colIndex], fileRowIndex, colIndex + colOffset, false)
    newRow.amortGroup = record71?.record_id?.value

    // графа 6
    colIndex++
    if (record71 != null) {
        // графа 6 - зависит от графы 5 - атрибут 645 - TERM - "Срок полезного использования (месяцев)", справочник 71 "Амортизационные группы"
        formDataService.checkReferenceValue(71, values[colIndex], record71?.TERM?.value?.toString(), fileRowIndex, colIndex + colOffset, logger, false)
    }

    // графа 7..16
    ['monthsUsed', 'usefulLifeWithUsed', 'specCoef', 'cost10perMonth', 'cost10perTaxPeriod',
     'cost10perExploitation', 'amortNorm', 'amortMonth', 'amortTaxPeriod', 'amortExploitation'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    // графа 17..19
    ['exploitationStart', 'usefullLifeEnd', 'rentEnd'].each { alias ->
        colIndex++
        newRow[alias] = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}
