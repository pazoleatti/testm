package form_template.income.rnu51.v2008

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 6.5	(РНУ-51) Регистр налогового учёта финансового результата от реализации (выбытия) ОФЗ
 * formTypeId=345
 *
 * @author Dmitriy Levykin
 */

//1.		rowNumber               Число/15/               № пп
//2.		tradeNumber             Число/1/                Код сделки
//3.		singSecurirty           Строка /1/	            Признак ценной бумаги
//4.		issue                   Строка /255/            Выпуск
//5.		acquisitionDate         Дата	ДД.ММ.ГГГГ      Дата приобретения, закрытия короткой позиции
//6.		saleDate                Дата	ДД.ММ.ГГГГ      Дата реализации, погашения, прочего выбытия, открытия короткой позиции
//7.		amountBonds             Число/15/               Количество облигаций (шт.)
//8.		acquisitionPrice        Число/17.2/             Цена приобретения (руб.коп.)
//9.		costOfAcquisition       Число/17.2/	            Расходы по приобретению (руб.коп.)
//10.	    marketPriceInPerc       Число/18.3/             Рыночная цена на дату приобретения. % к номиналу
//11.	    marketPriceInRub        Число/17.2/             Рыночная цена на дату приобретения. В рублях и коп.
//12.	    acquisitionPriceTax     Число/17.2/             Цена приобретения для целей налогообложения (руб.коп.)
//13.	    redemptionValue         Число/17.2/             Стоимость погашения (руб.коп.)
//14.	    priceInFactPerc         Число/18.3/             Фактическая цена реализации. % к номиналу
//15.	    priceInFactRub          Число/17.2/             Фактическая цена реализации. В рублях и коп.
//16.	    marketPriceInPerc1      Число/18.3/	            Рыночная цена на дату реализации. % к номиналу
//17.	    marketPriceInRub1       Число/17.2/	            Рыночная цена на дату реализации. В рублях и коп.
//18.	    salePriceTax            Число/17.2/             Цена реализации (выбытия) для целей налогообложения (руб.коп.)
//19.	    expensesOnSale          Число/17.2/             Расходы по реализации (выбытию) (руб.коп.)
//20.	    expensesTotal           Число/17.2/             Всего расходы (руб. коп.)
//21.	    profit                  Число/17.2/	            Прибыль (+), убыток (-) от реализации (погашения) (руб.коп.)
//22.	    excessSalePriceTax      Число/17.2/	            Превышение цены реализации для целей налогообложения над ценой реализации (руб.коп.)

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.CHECK:
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
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationTotal(formData, logger, userInfo, ['itogoKvartal', 'itogo'])
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
        def fileName = UploadFileName?.toLowerCase()
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xlsm")) {
            importDataXLS()
        } else {
            importData()
        }
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.MIGRATION:
        importData()
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

@Field
def allColumns = ['rowNumber', 'tradeNumber', 'singSecurirty', 'issue', 'acquisitionDate', 'saleDate',
        'amountBonds', 'acquisitionPrice', 'costOfAcquisition', 'marketPriceInPerc', 'marketPriceInRub',
        'acquisitionPriceTax', 'redemptionValue', 'priceInFactPerc', 'priceInFactRub', 'marketPriceInPerc1',
        'marketPriceInRub1', 'salePriceTax', 'expensesOnSale', 'expensesTotal', 'profit', 'excessSalePriceTax']

// Редактируемые атрибуты
@Field
def editableColumns = ['tradeNumber', 'singSecurirty', 'issue', 'acquisitionDate', 'saleDate', 'amountBonds',
        'acquisitionPrice', 'costOfAcquisition', 'marketPriceInPerc', 'marketPriceInRub', 'redemptionValue',
        'priceInFactPerc', 'priceInFactRub', 'expensesOnSale', 'marketPriceInPerc1']

@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = allColumns

//// Атрибуты, по которым рассчитываются итоговые значения
@Field
def totalColumns = ['amountBonds', 'acquisitionPrice', 'costOfAcquisition', 'marketPriceInRub', 'acquisitionPriceTax',
        'redemptionValue', 'priceInFactRub', 'marketPriceInRub1', 'salePriceTax', 'expensesOnSale', 'expensesTotal',
        'profit', 'excessSalePriceTax']

// Признак периода ввода остатков
@Field
def isBalancePeriod = null

// Форма предыдущего периода
@Field
def formDataPrev = null

// DataRowHelper формы предыдущего периода
@Field
def dataRowHelperPrev = null

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

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

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, cellName, logger, required)
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
        if (formDataPrev != null) {
            dataRowHelperPrev = formDataService.getDataRowHelper(formDataPrev)
        }
    }
    return dataRowHelperPrev
}

void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // Сортировка
//    dataRows.sort({ DataRow a, DataRow b ->
//        if (a.getAlias() != null && b.getAlias() == null) {
//            return 1
//        }
//        if (a.getAlias() == null && b.getAlias() != null) {
//            return -1
//        }
//        if (a.getAlias() != null && b.getAlias() != null) {
//            return b.getAlias() <=> a.getAlias()
//        }
//        def codeA = getCode(a.tradeNumber)
//        def codeB = getCode(b.tradeNumber)
//        if (codeA == codeB && a.singSecurirty == b.singSecurirty) {
//            return a.issue <=> b.issue
//        }
//        if (codeA == codeB) {
//            return a.singSecurirty <=> b.singSecurirty
//        }
//        return codeA <=> codeB
//    })

    for (row in dataRows) {
        if (row.getAlias() != null || isBalancePeriod() || formData.kind != FormDataKind.PRIMARY) {
            continue;
        }
        // Графа 12
        row.acquisitionPriceTax = calc12(row)
        // Графа 16
        row.marketPriceInPerc1 = calc16(row)
        // Графа 17
        row.marketPriceInRub1 = calc17(row)
        // Графа 18
        row.salePriceTax = calc18(row)
        // Графа 20
        row.expensesTotal = calc20(row)
        // Графа 21
        row.profit = calc21(row)
        // Графа 22
        row.excessSalePriceTax = calc22(row)
    }

    // Добавление итогов по текущей форме
    def totalOneSum = calcTotalOne(dataRows)
    def totalOneRow = getDataRow(dataRows, 'itogoKvartal')

    // Добавление итогов по налоговому периоду
    def totalTwoSum = calcTotalTwo(totalOneSum)
    def totalTwoRow = getDataRow(dataRows, 'itogo')

    totalColumns.each {
        totalOneRow[it] = totalOneSum[it]
        totalTwoRow[it] = totalTwoSum[it]
    }
}

// Итого по форме
def calcTotalOne(def dataRows) {
    def result = [:]

    // Инициализация
    totalColumns.each {
        result[it] = 0
    }

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        totalColumns.each {
            result[it] += row.get(it) ?: 0
        }
    }
    return result
}

// Итого по налоговому периоду
def calcTotalTwo(def totalOneSum) {
    def result = [:]
    if (formData.kind == FormDataKind.PRIMARY) {
        def prevTotal = null
        def prevFormData = getFormDataPrev()
        if (prevFormData != null && prevFormData.state == WorkflowState.ACCEPTED) {
            def prevRows = getDataRowHelperPrev().allSaved
            prevTotal = getDataRow(prevRows, 'itogoKvartal')
        }
        totalColumns.each {
            result[it] = totalOneSum[it] ?: 0 + (prevTotal == null ? 0 : prevTotal[it])
        }
    } else if (formData.kind == FormDataKind.CONSOLIDATED) {
        departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind, getReportPeriodStartDate(), getReportPeriodEndDate()).each {
            if (it.formTypeId == formData.getFormType().getId()) {
                def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder, formData.comparativePeriodId, formData.accruing)
                if (source != null && source.state == WorkflowState.ACCEPTED) {
                    formDataService.getDataRowHelper(source).getAllSaved().each { row ->
                        if (row.getAlias() == 'itogoKvartal') {
                            totalColumns.each {
                                result[it] = (totalOneSum[it] ?: 0) + (row[it] ?: 0)
                            }
                        }
                    }
                }
            }
        }
    }

    return result
}

def BigDecimal calc12(def row) {
    return round(row.acquisitionPrice > row.marketPriceInRub ? row.marketPriceInRub : row.acquisitionPrice)
}

def BigDecimal calc16(def row) {
    if (row.redemptionValue != null && row.redemptionValue > 0) {
        return 100
    }
    return row.marketPriceInPerc1
}

def BigDecimal calc17(def row) {
    return row.redemptionValue
}

def BigDecimal calc18(def row) {
    def code = getCode(row.tradeNumber)
// Debug:
//    println("---------------------- "+row)
//    println(" code = " + code)
//    println(" 13 = " + row.redemptionValue)
//    println(" 14 = " + row.priceInFactPerc)
//    println(" 15 = " + row.priceInFactRub)
//    println(" 16 = " + row.marketPriceInPerc1)
//    println(" 17 = " + row.marketPriceInRub1)
    if (code == 1) {
        return row.priceInFactRub
    }
    if (code == 4) {
        return row.redemptionValue
    }
    if (code == 2 || code == 5) {
        if (row.marketPriceInPerc1 > row.priceInFactPerc && row.marketPriceInRub1 > row.priceInFactRub){
            return row.marketPriceInRub1
        }
        if (row.marketPriceInPerc1 < row.priceInFactPerc && row.marketPriceInRub1 < row.priceInFactRub){
            return row.priceInFactRub
        }
        if (row.marketPriceInRub1 == 0 && row.priceInFactRub == 0){
            return row.priceInFactRub
        }
    }

    // Для всех остальных случаев значение графы не изменяется
    return row.salePriceTax
}

def BigDecimal calc20(def row) {
    if (row.costOfAcquisition == null || row.acquisitionPriceTax == null || row.expensesOnSale == null) {
        return null
    }
    return row.costOfAcquisition + row.acquisitionPriceTax + row.expensesOnSale
}

def BigDecimal calc21(def row) {
    if (row.salePriceTax == null || row.expensesTotal == null) {
        return null
    }
    return row.salePriceTax - row.expensesTotal
}

def BigDecimal calc22(def row) {
    if (getCode(row.tradeNumber) == 4) {
        return 0
    }
    if (row.salePriceTax == null || row.priceInFactRub == null) {
        return null
    }
    return row.salePriceTax - row.priceInFactRub
}

// Округление
def BigDecimal round(BigDecimal value, def int precision = 2) {
    return value?.setScale(precision, RoundingMode.HALF_UP)
}

void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // Алиасы граф для арифметической проверки
    def arithmeticCheckAlias = ['acquisitionPriceTax', 'marketPriceInPerc1', 'marketPriceInRub1', 'salePriceTax',
            'expensesTotal', 'profit', 'excessSalePriceTax']

    for (def row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        // 1. Проверка заполнения граф
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, !isBalancePeriod())

        if (formData.kind == FormDataKind.PRIMARY) {
            // 3. Арифметическая проверка граф 12, 16, 17, 18, 20, 21, 22
            needValue['acquisitionPriceTax'] = calc12(row)
            needValue['marketPriceInPerc1'] = calc16(row)
            needValue['marketPriceInRub1'] = calc17(row)
            needValue['salePriceTax'] = calc18(row)
            needValue['expensesTotal'] = calc20(row)
            needValue['profit'] = calc21(row)
            needValue['excessSalePriceTax'] = calc22(row)
            checkCalc(row, arithmeticCheckAlias, needValue, logger, false)
        }
    }

    // 4. Проверка корректности расчета итоговых значений за текущий квартал
    def totalOneSum = calcTotalOne(dataRows)
    def totalOneRow = getDataRow(dataRows, 'itogoKvartal')
    if (!checkTotalSum(totalOneRow, totalOneSum)) {
        loggerError("Итоговые значения за текущий квартал рассчитаны неверно!")
    }

    // 5. Проверка корректности расчета итоговых значений за текущий отчётный (налоговый) период
    def totalTwoSum = calcTotalTwo(totalOneSum)
    def totalTwoRow = getDataRow(dataRows, 'itogo')
    if (!checkTotalSum(totalTwoSum, totalTwoRow)) {
        loggerError("Итоговые значения за текущий отчётный (налоговый) период рассчитаны неверно!")
    }
}

// Проверка итоговых значений
def checkTotalSum(def row, def totalSum) {
    def check = true
    if (row == null) {
        check = false
    } else {
        for (def alias : totalColumns) {
            if (totalSum[alias] != row[alias]) {
                check = false
                break
            }
        }
    }
    return check
}

// Код сделки
def getCode(def code) {
    if (code == null) {
        return null
    }
    return getRefBookValue(61, code)?.CODE?.numberValue
}

void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        throw new ServiceException('Имя файла не должно быть пустым')
    }

    String charset = ""
    // TODO в дальнейшем убрать возможность загружать RNU для импорта!
    if (formDataEvent == FormDataEvent.IMPORT && fileName.contains('.xml') ||
            formDataEvent == FormDataEvent.MIGRATION && fileName.contains('.xml')) {
        if (!fileName.contains('.xml')) {
            throw new ServiceException('Формат файла должен быть *.xml')
        }
    } else {
        if (!fileName.contains('.r')) {
            throw new ServiceException('Формат файла должен быть *.rnu')
        }
        charset = 'cp866'
    }

    def is = ImportInputStream
    if (is == null) {
        throw new ServiceException('Поток данных пуст')
    }

    def xmlString = importService.getData(is, fileName, charset)
    if (xmlString == null || xmlString == '') {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
    }

    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
    }

    addData(xml, fileName)
}

// Заполнить форму данными
def addData(def xml, def fileName) {
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    // Поиск итоговых строк
    def totalOneRow = getDataRow(dataRows, 'itogoKvartal')
    def totalTwoRow = getDataRow(dataRows, 'itogo')

    // Обнуление итогов
    totalColumns.each {
        totalOneRow[it] = 0
        totalTwoRow[it] = 0
    }

    dataRows = [totalOneRow, totalTwoRow]

    def newRows = []
    def records = null
    def totalRecords = null
    def type = null

    if (formDataEvent == FormDataEvent.MIGRATION || formDataEvent == FormDataEvent.IMPORT && fileName.contains('.xml')) {
        records = xml.exemplar.table.detail.record
        totalRecords = xml.exemplar.table.total.record
        type = 1 // XML
    } else {
        records = xml.row
        totalRecords = xml.rowTotal
        type = 2 // RNU
    }

    def fileRowIndex = 0
    // Смещение для вывода сообщений об ошибках
    def rowOffset = 1
    def colOffset = 1

    for (def row : records) {
        def fileColIndex = 0
        fileRowIndex++

        def newRow = formData.createDataRow()
        newRow.setIndex(fileRowIndex)

        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 1

        // графа 2 - справочник 61 "Коды сделок"
        fileColIndex++
        def val = getCellValue(row, fileColIndex, type)
        if (val != null && !val.trim().isEmpty()) {
            newRow.tradeNumber = getRecordIdImport(61, 'CODE', val, fileRowIndex + rowOffset, fileColIndex + colOffset, false)
        }

        // графа 3 - справочник 62 "Признаки ценных бумаг"
        fileColIndex++
        val = getCellValue(row, fileColIndex, type, true)
        if (val != null && !val.trim().isEmpty()) {
            newRow.singSecurirty = getRecordIdImport(62, 'CODE', val, fileRowIndex + rowOffset, fileColIndex + colOffset, false)
        }

        // графа 4
        fileColIndex++
        newRow.issue = getCellValue(row, fileColIndex, type, true)

        // графа 5
        fileColIndex++
        newRow.acquisitionDate = parseDate(getCellValue(row, fileColIndex, type), 'dd.MM.yyyy', fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)

        // графа 6
        fileColIndex++
        newRow.saleDate = parseDate(getCellValue(row, fileColIndex, type), 'dd.MM.yyyy', fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)

        // графа 7..22
        ['amountBonds', 'acquisitionPrice', 'costOfAcquisition', 'marketPriceInPerc',
         'marketPriceInRub', 'acquisitionPriceTax', 'redemptionValue', 'priceInFactPerc',
         'priceInFactRub', 'marketPriceInPerc1', 'marketPriceInRub1', 'salePriceTax',
         'expensesOnSale', 'expensesTotal', 'profit', 'excessSalePriceTax'].each { alias ->
            fileColIndex++
            newRow[alias] = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        }

        newRows.add(newRow)
    }
    // Добавление строк перед итогами
    dataRows.addAll(0, newRows)

    // Расчет итогов
    def totalOneSum = calcTotalOne(dataRows)
    def totalTwoSum = calcTotalTwo(totalOneSum)

    totalColumns.each {
        totalOneRow[it] = totalOneSum[it]
        totalTwoRow[it] = totalTwoSum[it]
    }

    // В файле есть итоговая строка
    if (totalRecords.size() >= 1) {
        def row = totalRecords[0]

        def total = formData.createDataRow()

        // графа 7
        total.amountBonds = parseNumber(getCellValue(row, 6, type), fileRowIndex + rowOffset, 6 + colOffset, logger, true)

        // графа 8
        total.acquisitionPrice = parseNumber(getCellValue(row, 7, type), fileRowIndex + rowOffset, 7 + colOffset, logger, true)

        // графа 9
        total.costOfAcquisition = parseNumber(getCellValue(row, 8, type), fileRowIndex + rowOffset, 8 + colOffset, logger, true)

        // графа 11
        total.marketPriceInRub = parseNumber(getCellValue(row, 10, type), fileRowIndex + rowOffset, 10 + colOffset, logger, true)

        // графа 12
        total.acquisitionPriceTax = parseNumber(getCellValue(row, 11, type), fileRowIndex + rowOffset, 11 + colOffset, logger, true)

        // графа 13
        total.redemptionValue = parseNumber(getCellValue(row, 12, type), fileRowIndex + rowOffset, 12 + colOffset, logger, true)

        // графа 15
        total.priceInFactRub = parseNumber(getCellValue(row, 14, type), fileRowIndex + rowOffset, 14 + colOffset, logger, true)

        // графа 17
        total.marketPriceInRub1 = parseNumber(getCellValue(row, 16, type), fileRowIndex + rowOffset, 16 + colOffset, logger, true)

        // графа 18
        total.salePriceTax = parseNumber(getCellValue(row, 17, type), fileRowIndex + rowOffset, 17 + colOffset, logger, true)

        // графа 19
        total.expensesOnSale = parseNumber(getCellValue(row, 18, type), fileRowIndex + rowOffset, 18 + colOffset, logger, true)

        // графа 20
        total.expensesTotal = parseNumber(getCellValue(row, 19, type), fileRowIndex + rowOffset, 19 + colOffset, logger, true)

        // графа 21
        total.profit = parseNumber(getCellValue(row, 20, type), fileRowIndex + rowOffset, 20 + colOffset, logger, true)

        // графа 22
        total.excessSalePriceTax = parseNumber(getCellValue(row, 21, type), fileRowIndex + rowOffset, 21 + colOffset, logger, true)

        if (formDataEvent == FormDataEvent.IMPORT) {
            // Проверка итогов
            for (def alias : totalColumns) {
                if (total[alias] != totalOneSum[alias]) {
                    logger.error("Итоговые значения за текущий квартал рассчитаны неверно!")
                }
            }
        }
    }

    updateIndexes(dataRows)
    formDataService.getDataRowHelper(formData).allCached = dataRows
}

// для получения данных из RNU или XML
String getCellValue(def row, int index, def type, boolean isTextXml = false) {
    if (type == 1) {
        return isTextXml ? row.field[index].text() : row.field[index].@value.text()
    }
    return row.cell[index + 1].text()
}

/** Вывести сообщение. В периоде ввода остатков сообщения должны быть только НЕфатальными. */
void loggerError(def msg) {
    if (isBalancePeriod()) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}

// Сортировка групп и строк
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, [getDataRow(dataRows, 'itogoKvartal')], getDataRow(dataRows, 'itogo'), true)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
}

void importDataXLS() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 22
    int HEADER_ROW_COUNT = 3
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
        // Пропуск итоговых строк
        if (!rowValues[INDEX_FOR_SKIP]) {
            allValues.remove(rowValues)
            rowValues.clear()
            continue
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    def totalOneRow = getDataRow(dataRows, 'itogoKvartal')
    def totalTwoRow = getDataRow(dataRows, 'itogo')
    // Обнуление итогов
    totalColumns.each {
        totalOneRow.getCell(it).setCheckMode(true)
        totalOneRow[it] = 0
        totalTwoRow.getCell(it).setCheckMode(true)
        totalTwoRow[it] = 0
    }
    rows.add(totalOneRow)
    rows.add(totalTwoRow)

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
    if (headerRows.isEmpty() || headerRows.size() < rowCount) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    // размер заголовка проверяется по последней строке (нумерация столбцов) потому что в первых строках есть объединения
    checkHeaderSize(headerRows[rowCount - 1].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            ([(headerRows[0][0]) : tmpRow.getCell('rowNumber').column.name]),
            ([(headerRows[0][1]) : tmpRow.getCell('tradeNumber').column.name]),
            ([(headerRows[0][2]) : tmpRow.getCell('singSecurirty').column.name]),
            ([(headerRows[0][3]) : tmpRow.getCell('issue').column.name]),
            ([(headerRows[0][4]) : tmpRow.getCell('acquisitionDate').column.name]),
            ([(headerRows[0][5]) : tmpRow.getCell('saleDate').column.name]),
            ([(headerRows[0][6]) : tmpRow.getCell('amountBonds').column.name]),
            ([(headerRows[0][7]) : tmpRow.getCell('acquisitionPrice').column.name]),
            ([(headerRows[0][8]) : tmpRow.getCell('costOfAcquisition').column.name]),
            ([(headerRows[0][9]) : 'Рыночная цена на дату приобретения']),
            ([(headerRows[0][11]): tmpRow.getCell('acquisitionPriceTax').column.name]),
            ([(headerRows[0][12]): tmpRow.getCell('redemptionValue').column.name]),
            ([(headerRows[0][13]): 'Фактическая цена реализации']),
            ([(headerRows[0][15]): 'Рыночная цена на дату реализации']),
            ([(headerRows[0][17]): tmpRow.getCell('salePriceTax').column.name]),
            ([(headerRows[0][18]): tmpRow.getCell('expensesOnSale').column.name]),
            ([(headerRows[0][19]): tmpRow.getCell('expensesTotal').column.name]),
            ([(headerRows[0][20]): tmpRow.getCell('profit').column.name]),
            ([(headerRows[0][21]): tmpRow.getCell('excessSalePriceTax').column.name]),

            ([(headerRows[1][9]) : '% к номиналу']),
            ([(headerRows[1][10]): 'руб.коп.']),
            ([(headerRows[1][13]): '% к номиналу']),
            ([(headerRows[1][14]): 'руб.коп.']),
            ([(headerRows[1][15]): '% к номиналу']),
            ([(headerRows[1][16]): 'руб.коп.'])
    ]
    (1..22).each {
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
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    autoFillColumns.each {
        newRow.getCell(it).setStyleAlias('Автозаполняемая')
    }

    // графа 1
    def colIndex = 0
    newRow.rowNumber = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)

    // графа 2 - справочник 61 "Коды сделок"
    colIndex++
    newRow.tradeNumber = getRecordIdImport(61, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 3 - справочник 62 "Признаки ценных бумаг"
    colIndex++
    newRow.singSecurirty = getRecordIdImport(62, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset)

    // графа 4
    colIndex++
    newRow.issue = values[colIndex]

    // графа 5
    colIndex++
    newRow.acquisitionDate = parseDate(values[colIndex], 'dd.MM.yyyy', fileRowIndex, colIndex + colOffset, logger, true)

    // графа 6
    colIndex++
    newRow.saleDate = parseDate(values[colIndex], 'dd.MM.yyyy', fileRowIndex, colIndex + colOffset, logger, true)

    // графа 7..22
    ['amountBonds', 'acquisitionPrice', 'costOfAcquisition', 'marketPriceInPerc',
            'marketPriceInRub', 'acquisitionPriceTax', 'redemptionValue', 'priceInFactPerc',
            'priceInFactRub', 'marketPriceInPerc1', 'marketPriceInRub1', 'salePriceTax',
            'expensesOnSale', 'expensesTotal', 'profit', 'excessSalePriceTax'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}