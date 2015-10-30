package form_template.income.rnu14.v2012

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Форма "УНП" ("РНУ-14 - Регистр налогового учёта нормируемых расходов")
 * formTemplateId=321
 *
 * @author lhaziev
 * @author bkinzyabulatov
 *
 * графа 1  - knu
 * графа 2  - mode
 * графа 3  - sum
 * графа 4  - normBase
 * графа 5  - normCoef
 * графа 6  - limitSum
 * графа 7  - inApprovedNprms
 * графа 8  - overApprovedNprms
 *
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
        }
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
}

// все атрибуты
@Field
def allColumns = ['knu', 'mode', 'sum', 'normBase', 'normCoef', 'limitSum', 'inApprovedNprms', 'overApprovedNprms']

@Field
def nonEmptyColumns = ['normBase']

@Field
def col = ['21270', '21410', '20698', '20700', '20690']

@Field
def knuBase = ['20480', '20485', '20490', '20500', '20505', '20530']

@Field
def knuTax = ['20480', '20485', '20490', '20500', '20505', '20530', '20510', '20520']

@Field
def knuComplex = ['10633', '10634', '10650', '10670', '10855', '10880', '10900', '10850',
        '11180', '11190', '11200', '11210', '11220', '11230', '11240', '11250',
        '11260', '10840', '10860', '10870', '10890']

@Field
def knuSimpleRNU4 = ['10001', '10006', '10041', '10300', '10310', '10320', '10330', '10340',
        '10350', '10360', '10370', '10380', '10390', '10450', '10460', '10470',
        '10480', '10490', '10571', '10580', '10590', '10600', '10610', '10630',
        '10631', '10632', '10640', '10680', '10690', '10740', '10744', '10748',
        '10752', '10756', '10760', '10770', '10790', '10800', '11140', '11150',
        '11160', '11170', '11320', '11325', '11330', '11335', '11340', '11350',
        '11360', '11370', '11375']

@Field
def knuSimpleRNU6 = ['10001', '10006', '10300', '10310', '10320', '10330', '10340', '10350',
        '10360', '10470', '10480', '10490', '10571', '10590', '10610', '10640',
        '10680', '10690', '11340', '11350', '11370', '11375']

@Field
def koeffNormBase = [4 / 100, 1 / 100, 6 / 100, 12 / 100, 15000]

void prevPeriodCheck() {
    if (getFormDataOutcomeSimple() == null) {
        throw new ServiceException("Не найден экземпляр Сводной формы «Расходы, учитываемые в простых РНУ» за текущий отчетный период!")
    }
    if (getFormDataSimple() == null) {
        throw new ServiceException("Не найден экземпляр Сводной формы «Доходы, учитываемые в простых РНУ»!")
    }
    if (getFormDataComplex() == null) {
        throw new ServiceException("Не найден экземпляр Сводной формы «Сводная форма начисленных доходов»!")
    }
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def formDataComplex = getFormDataComplex()
    def formDataSimple = getFormDataSimple()
    def formDataOutcome = getFormDataOutcomeSimple()
    def dataRowsComplex
    if (formDataComplex != null) {
        dataRowsComplex = formDataService.getDataRowHelper(formDataComplex)?.allCached
    }
    def dataRowsSimple
    if (formDataSimple != null) {
        dataRowsSimple = formDataService.getDataRowHelper(formDataSimple)?.allCached
    }
    def dataRowsRNU
    if (formDataOutcome != null) {
        dataRowsRNU = formDataService.getDataRowHelper(formDataOutcome)?.allCached
    }
    /** Отчётный период. */
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    for (def row : dataRows) {
        def index = dataRows.indexOf(row)
        def rowA = getTotalRowFromRNU(col[index], dataRowsRNU)
        if (rowA != null) {
            // 3 - графа 8 строки А + (графа 5 строки А – графа 6 строки А)
            row.sum = (rowA.rnu5Field5Accepted ?: 0) + (rowA.rnu7Field10Sum ?: 0) - (rowA.rnu7Field12Accepted ?: 0)
            // 4 - сумма по всем (графа 8 строки B + (графа 5 строки B – графа 6 строки B)),
            // КНУ которых совпадает со значениями в colBase (или colTax если налоговый период)
            if (index != 4 && index != 1) {//не 5-я и 2-я строка
                def normBase = 0
                /** Признак налоговый ли это период. */
                def isTaxPeriod = (reportPeriod != null && reportPeriod.order == 4)
                for (def knu : (isTaxPeriod ? knuTax : knuBase)) {
                    def rowB = getTotalRowFromRNU(knu, dataRowsRNU)
                    if (rowB != null) {
                        normBase += (rowB.rnu5Field5Accepted ?: 0) + (rowB.rnu7Field10Sum ?: 0) - (rowB.rnu7Field12Accepted ?: 0)
                    }
                }
                row.normBase = normBase
            } else if (index == 1) {//2-я строка(сложнее)
                def normBase = 0
                //Сумма значений по графе 9 (столбец «Доход по данным налогового учёта. Сумма») в сложных доходах где КНУ = ...
                //объединил
                if (dataRowsComplex != null) {
                    for (def rowComplex : dataRowsComplex) {
                        if (rowComplex.incomeTypeId in knuComplex) {
                            normBase += (rowComplex.incomeTaxSumS ?: 0)
                        }
                    }
                }
                //простые доходы
                if (dataRowsSimple != null) {
                    for (def rowSimple : dataRowsSimple) {
                        //+ Сумма значений по графе 8 (столбец «РНУ-4 (графа 5) сумма») в простых доходах
                        if (rowSimple.incomeTypeId in knuSimpleRNU4) {
                            normBase += (rowSimple.rnu4Field5Accepted ?: 0)
                        }
                        //+ Сумма значений по графе 5 (столбец «РНУ-6 (графа 10) сумма»)
                        //- Сумма значений по графе 6 (столбец «РНУ-6 (графа 12). Сумма»)
                        // КНУ одни, поэтому объединил
                        if (rowSimple.incomeTypeId in knuSimpleRNU6) {
                            normBase += ((rowSimple.rnu6Field10Sum ?: 0) - (rowSimple.rnu6Field12Accepted ?: 0))
                        }
                    }
                }
                row.normBase = normBase
            }
            // 6
            if (row.normBase != null) {
                row.limitSum = koeffNormBase[index] * row.normBase
            }
            def diff6_3 = (row.limitSum ?: 0) - (row.sum ?: 0)
            if (diff6_3 != null) {
                // 7 - 1. ЕСЛИ («графа 6» – «графа 3») ≥ 0, то «графа 3»;
                //     2. ЕСЛИ («графа 6» – «графа 3») < 0, то «графа 6»;
                if (diff6_3 >= 0) {
                    row.inApprovedNprms = row.sum
                } else {
                    row.inApprovedNprms = row.limitSum
                }
                // 8 - 1. ЕСЛИ («графа 6» – «графа 3») ≥ 0, то 0;
                //     2. ЕСЛИ («графа 6» – «графа 3») < 0, то «графа 3» - «графа 6».
                if (diff6_3 >= 0) {
                    row.overApprovedNprms = 0
                } else {
                    row.overApprovedNprms = -diff6_3
                }
            }
        }
    }
    dataRowHelper.save(dataRows);
}

void logicCheck() {
    def row = formDataService.getDataRowHelper(formData)?.allCached?.get(4)
    // 1. Обязательность заполнения полей графы 4 строки 5
    checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
}

/**
 * Получить строку из сводной налоговой формы "Расходы, учитываемые в простых РНУ", у которой графа 1 ("КНУ") = knu
 * @param knu КНУ
 * @param dataRowsOutcome строки НФ простые доходы
 */
def getTotalRowFromRNU(def knu, def dataRowsOutcome) {
    for (def row : dataRowsOutcome) {
        if (row.consumptionTypeId == knu) {
            return row
        }
    }
    return null
}

// Получить данные формы "расходы простые" (id = 304)
def getFormDataOutcomeSimple() {
    def tmp = formDataService.getLast(310, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
    if (tmp == null) {
        tmp = formDataService.getLast(304, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
    } else if (formDataService.getLast(304, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing) != null) {
        logger.warn("Неверно настроены источники формы УНП! Одновременно созданы в качестве источников налоговые формы: «%s», «%s». Расчет произведен из «%s».",
                formTypeService.get(310).name, formTypeService.get(304)?.name, formTypeService.get(310)?.name)
    }
    return tmp
}

// Получить данные формы "доходы сложные" (id = 302)
def getFormDataComplex() {
    return formDataService.getLast(302, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
}

// Получить данные формы "доходы простые" (id = 305/301)
def getFormDataSimple() {
    def tmp = formDataService.getLast(305, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
    if (tmp == null) {
        tmp = formDataService.getLast(301, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
    } else if (formDataService.getLast(301, FormDataKind.SUMMARY, formDataDepartment.id, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing) != null) {
        logger.warn("Неверно настроены источники формы УНП! Одновременно созданы в качестве источников налоговые формы: «%s», «%s». Расчет произведен из «%s».",
                formTypeService.get(305).name, formTypeService.get(301)?.name, formTypeService.get(305)?.name)
    }
    return tmp
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 8, 0)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    dataRows.each { dataRow ->
        def values = xml.row[dataRows.indexOf(dataRow)]
        def tmpValues = [:]
        // графа 1
        tmpValues.knu = values.cell[1].text()
        // графа 2
        tmpValues.mode = values.cell[2].text()
        // графа 5
        tmpValues.normCoef = values.cell[5].text()

        // Проверить фиксированные значения (графа 1, 2, 5)
        tmpValues.keySet().toArray().each { alias ->
            def value = StringUtils.cleanString(tmpValues[alias]?.toString())
            def valueExpected = StringUtils.cleanString(dataRow.getCell(alias).value?.toString())
            checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), alias, logger, true)
        }
    }
    ((DataRow<Cell>)dataRows[4]).getCell('normBase').setCheckMode(true)
    // графа 4 строки 5
    if (xml.row[4] != null) {
        dataRows[4].normBase = parseNumber(xml.row[4].cell[4].text(), 7, 5, logger, true)
    } else {
        dataRows[4].normBase = null
    }
    showMessages(dataRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        dataRowHelper.update(dataRows)
    }
}

void importData() {
    int COLUMN_COUNT = 8
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'КНУ'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
    if (logger.containsLevel(LogLevel.ERROR)) {
        return;
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

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

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
        if (rowIndex > dataRows.size()) {
            break
        }
        // найти нужную строку нф
        def dataRow = dataRows.get(rowIndex)
        // заполнить строку нф значениями из эксель
        fillRowFromXls(dataRow, rowValues, fileRowIndex, colOffset)
        rowIndex++

        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }
    showMessages(dataRows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).save(dataRows)
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    if (headerRows.isEmpty() || headerRows.size() < rowCount) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[2].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            ([(headerRows[0][0]): 'КНУ']),
            ([(headerRows[0][1]): 'Вид нормируемых расходов']),
            ([(headerRows[0][2]): 'Сумма расходов по данным налогового учёта']),
            ([(headerRows[0][3]): 'Норматив, установленный законодателем']),
            ([(headerRows[0][5]): 'Предельная сумма расходов, учитываемая для целей налогообложения']),
            ([(headerRows[0][6]): 'Сумма расхода, рассчитанная по установленным нормам']),
            ([(headerRows[1][3]): 'База для расчёта нормы расходов']),
            ([(headerRows[1][4]): 'коэффициент']),
            ([(headerRows[1][6]): 'в пределах утверждённых норм']),
            ([(headerRows[1][7]): 'сверх утверждённых норм'])
    ]
    (0..7).each { index ->
        headerMapping.add(([(headerRows[2][index]): (index + 1).toString()]))
    }
    checkHeaderEquals(headerMapping, logger)
}

/**
 * Заполняет заданную строку нф значениями из экселя.
 *
 * @param dataRow строка нф
 * @param values список строк со значениями
 * @param fileRowIndex номер строки в тф
 * @param colOffset отступ по столбцам
 */
def fillRowFromXls(def dataRow, def values, int fileRowIndex, int colOffset) {
    dataRow.setImportIndex(fileRowIndex)

    def tmpValues = [:]
    // графа 1
    tmpValues.knu = values[0]
    // графа 2
    tmpValues.mode = values[1]
    // графа 5
    tmpValues.normCoef = values[4]

    // Проверить фиксированные значения (графа 1, 2, 5)
    tmpValues.keySet().toArray().each { alias ->
        def value = StringUtils.cleanString(tmpValues[alias]?.toString())
        def valueExpected = StringUtils.cleanString(dataRow.getCell(alias).value?.toString())
        checkFixedValue(dataRow, value, valueExpected, dataRow.getIndex(), alias, logger, true)
    }

    // графа 4 строки 5
    if (dataRow.getIndex() == 5) {
        dataRow.getCell('normBase').setCheckMode(true)
        def colIndex = 3
        dataRow.normBase = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }
}