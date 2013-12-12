package form_template.income.rnu51

import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

import java.math.RoundingMode

/**
 * 6.5	(РНУ-51) Регистр налогового учёта финансового результата от реализации (выбытия) ОФЗ
 * formTemplateId=345
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
//10.	    marketPriceInPerc       Число/18.3/             Рыночная цена на дату приобретения. В % к номиналу
//11.	    marketPriceInRub        Число/17.2/             Рыночная цена на дату приобретения. В рублях и коп.
//12.	    acquisitionPriceTax     Число/17.2/             Цена приобретения для целей налогообложения (руб.коп.)
//13.	    redemptionValue         Число/17.2/             Стоимость погашения (руб.коп.)
//14.	    priceInFactPerc         Число/18.3/             Фактическая цена реализации. В % к номиналу
//15.	    priceInFactRub          Число/17.2/             Фактическая цена реализации. В рублях и коп.
//16.	    marketPriceInPerc1      Число/18.3/	            Рыночная цена на дату реализации. В % к номиналу
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
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, null)
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
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
    case FormDataEvent.MIGRATION:
        importData()
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
        'priceInFactPerc', 'priceInFactRub', 'expensesOnSale']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = allColumns

//// Атрибуты, по которым рассчитываются итоговые значения
@Field
def totalColumns = ['amountBonds', 'acquisitionPrice', 'costOfAcquisition', 'marketPriceInRub', 'acquisitionPriceTax',
        'redemptionValue', 'priceInFactRub', 'marketPriceInRub1', 'salePriceTax', 'expensesOnSale', 'expensesTotal',
        'profit', 'excessSalePriceTax']

// Текущая дата
@Field
def currentDate = new Date()

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Признак периода ввода остатков
@Field
def isBalancePeriod = null

// Форма предыдущего периода
@Field
def formDataPrev = null

// DataRowHelper формы предыдущего периода
@Field
def dataRowHelperPrev = null

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

// Признак периода ввода остатков
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

// Получение формы предыдущего месяца
FormData getFormDataPrev() {
    if (formDataPrev == null) {
        formDataPrev = formDataService.getFormDataPrev(formData, formDataDepartment.id)
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def isImport = (formDataEvent == FormDataEvent.IMPORT)

    // Номер последний строки предыдущей формы
    def index = isImport ? 0 : formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')

    // Сортировка
    dataRows.sort({ DataRow a, DataRow b ->
        if (a.getAlias() != null && b.getAlias() == null) {
            return 1
        }
        if (a.getAlias() == null && b.getAlias() != null) {
            return -1
        }
        if (a.getAlias() != null && b.getAlias() != null) {
            return b.getAlias()<=>a.getAlias()
        }

        def codeA = getCode(a.tradeNumber)
        def codeB = getCode(b.tradeNumber)
        if (codeA == codeB && a.singSecurirty == b.singSecurirty) {
            return a.issue<=>b.issue
        }
        if (codeA == codeB) {
            return a.singSecurirty<=>b.singSecurirty
        }
        return codeA<=>codeB
    })

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        // Графа 1
        row.rowNumber = ++index
        // В периоде ввода остатков рассчитываются только итоги и порядковый номер
        if (isBalancePeriod()) {
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

    dataRowHelper.save(dataRows)
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
    def prevTotal = null
    def prevFormData = getFormDataPrev()
    if (prevFormData != null && prevFormData.state == WorkflowState.ACCEPTED) {
        def prevRows = getDataRowHelperPrev().allCached
        prevTotal = getDataRow(prevRows, 'itogoKvartal')
    }
    totalColumns.each {
        result[it] = totalOneSum[it] ?: 0 + (prevTotal == null ? 0 : prevTotal[it])
    }
    return result
}

BigDecimal calc12(def row) {
    return round(row.acquisitionPrice > row.marketPriceInRub ? row.marketPriceInRub : row.acquisitionPrice)
}

BigDecimal calc16(def row) {
    // TODO Левыкин: Для всех остальных случаев значение графы не изменяется?
    return row.redemptionValue > 0 ? 100 : row.marketPriceInPerc1
}

BigDecimal calc17(def row) {
    // TODO Левыкин: Для всех остальных случаев значение графы не изменяется?
    return row.redemptionValue > 0 ? 100 : row.marketPriceInRub1
}

BigDecimal calc18(def row) {
    def code = getCode(row.tradeNumber)
    if ((code == 1 || code == 2 || code == 5)
            && (row.priceInFactPerc > row.marketPriceInPerc1 && row.priceInFactRub > row.marketPriceInRub1)) {
        return row.priceInFactRub
    }
    if (code == 4) {
        return row.redemptionValue
    }
    if ((code == 2 || code == 5)
            && (row.priceInFactPerc < row.marketPriceInPerc1 && row.priceInFactRub < row.marketPriceInRub1)) {
        return row.marketPriceInRub1
    }
    if (code == 1) {
        return row.priceInFactRub
    }
    if (code == 4) {
        return row.redemptionValue
    }
    if (code == 2 || code == 5) {
        if (row.marketPriceInPerc1 > row.priceInFactPerc && row.marketPriceInRub1 > row.priceInFactRub) {
            return row.marketPriceInRub1
        }
        if (row.marketPriceInPerc1 < row.priceInFactPerc && row.marketPriceInRub1 < row.priceInFactRub
                || row.marketPriceInPerc1 == 0 && row.marketPriceInRub1 == 0) {
            return row.priceInFactRub
        }
    }
    // Для всех остальных случаев значение графы не изменяется
    return row.salePriceTax
}

BigDecimal calc20(def row) {
    if (row.costOfAcquisition == null || row.acquisitionPriceTax == null || row.expensesOnSale == null) {
        return null
    }
    return row.costOfAcquisition + row.acquisitionPriceTax + row.expensesOnSale
}

BigDecimal calc21(def row) {
    if (row.salePriceTax == null || row.expensesTotal == null) {
        return null
    }
    return row.salePriceTax - row.expensesTotal
}

BigDecimal calc22(def row) {
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
    if (isBalancePeriod()) {
        // В периоде ввода остатков нет лог. проверок
        return
    }

    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()

    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    // Алиасы граф для арифметической проверки
    def arithmeticCheckAlias = ['acquisitionPriceTax', 'marketPriceInPerc1', 'marketPriceInRub1', 'salePriceTax',
            'expensesTotal', 'profit', 'excessSalePriceTax']

    for (def row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // Проверка на заполнение обязательных граф
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 1. Проверка цены приобретения для целей налогообложения
        if (row.acquisitionPrice > row.marketPriceInRub && row.acquisitionPriceTax != row.marketPriceInRub
                || row.acquisitionPrice <= row.marketPriceInRub && row.acquisitionPriceTax != row.acquisitionPrice) {
            logger.error(errorMsg + "Неверно определена цена приобретения для целей налогообложения!")
        }
        // 2. Проверка рыночной цены в процентах при погашении
        if (row.redemptionValue != null && row.redemptionValue > 0 && row.marketPriceInPerc1 != 100) {
            logger.error(errorMsg + "Неверно указана рыночная цена в % при погашении!")
        }
        // 3. Проверка рыночной цены в рублях при погашении
        if (row.redemptionValue != null && row.redemptionValue > 0 && row.marketPriceInRub1 != row.redemptionValue) {
            logger.error(errorMsg + "Неверно указана рыночная цена в рублях при погашении!")
        }
        // 4. Проверка цены реализации (выбытия) для целей налогообложения
        def code = getCode(row.tradeNumber)
        if ((code == 1 || code == 2 || code == 5) && row.priceInFactPerc > row.marketPriceInPerc1
                && row.priceInFactRub > row.marketPriceInRub1 && row.salePriceTax != row.priceInFactRub
        ) {
            logger.error(errorMsg + "Неверно определена цена реализации для целей налогообложения по сделкам на ОРЦБ!")
        }
        // 5. Проверка цены реализации для целей налогообложения при погашении
        if (code == 4 && row.salePriceTax != row.redemptionValue) {
            logger.error(errorMsg + "Неверно определена цена реализации для целей налогообложения при погашении!")
        }
        // 6. Проверка цены реализации для целей налогообложения по переговорным сделкам на ОРЦБ и сделкам,
        // связанным с открытием-закрытием короткой позиции
        if ((code == 2 || code == 5) && row.tradeNumber < row.marketPriceInPerc1
                && row.priceInFactRub < row.marketPriceInRub1 && row.salePriceTax != row.marketPriceInRub1) {
            logger.error(errorMsg + "Неверно определена цена реализации для целей налогообложения по переговорным " +
                    "сделкам на ОРЦБ и сделкам, связанным с открытием-закрытием короткой позиции!")
        }
        // 7. Проверка итоговой суммы расходов
        if (row.expensesTotal != (row.costOfAcquisition ?: 0) + (row.acquisitionPriceTax ?: 0) + (row.expensesOnSale ?: 0)) {
            logger.error(errorMsg + "Неверно определены расходы!")
        }
        // 8. Проверка суммы финансового результата
        if (row.profit != (row.salePriceTax ?: 0) - (row.expensesTotal ?: 0)) {
            logger.error(errorMsg + "неверно определен финансовый результат реализации (выбытия)!")
        }
        // 9. Проверка превышения цены реализации для целей налогообложения над фактической ценой реализации
        if ((code != 4 && row.excessSalePriceTax != (row.salePriceTax ?: 0) - (row.priceInFactRub ?: 0))
                || (code == 4 && row.excessSalePriceTax != 0)
                || row.excessSalePriceTax < 0
        ) {
            logger.error(errorMsg + "Неверно определено превышение цены реализации для целей налогообложения " +
                    "над фактической ценой реализации!")
        }

        // 10. Арифметическая проверка
        needValue['acquisitionPriceTax'] = calc12(row)
        needValue['marketPriceInPerc1'] = calc16(row)
        needValue['marketPriceInRub1'] = calc17(row)
        needValue['salePriceTax'] = calc18(row)
        needValue['expensesTotal'] = calc20(row)
        needValue['profit'] = calc21(row)
        needValue['excessSalePriceTax'] = calc22(row)

        checkCalc(row, arithmeticCheckAlias, needValue, logger, false)

        // Проверки НСИ
        checkNSI(61, row, 'tradeNumber')
        checkNSI(62, row, 'singSecurirty')
    }

    // 11. Проверка корректности расчета итоговых значений за текущий квартал
    def totalOneSum = calcTotalOne(dataRows)
    def totalOneRow = getDataRow(dataRows, 'itogoKvartal')
    if (!checkTotalSum(totalOneRow, totalOneSum)) {
        logger.error("Итоговые значения за текущий квартал рассчитаны неверно!")
    }

    // 12. Проверка корректности расчета итоговых значений за текущий отчётный (налоговый) период
    def totalTwoSum = calcTotalTwo(totalOneSum)
    def totalTwoRow = getDataRow(dataRows, 'itogo')
    if (!checkTotalSum(totalTwoSum, totalTwoRow)) {
        logger.error("Итоговые значения за текущий отчётный (налоговый) период рассчитаны неверно!")
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

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).getAllCached().each { row ->
                    if (row.getAlias() == null) {
                        dataRows.add(row)
                    }
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
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
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

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
        newRow.rowNumber = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 2 - справочник 61 "Коды сделок"
        def val = getCellValue(row, fileColIndex, type)
        if (val != null && !val.trim().isEmpty()) {
            newRow.tradeNumber = getRecordIdImport(61, 'CODE', val, fileRowIndex + rowOffset, fileColIndex + colOffset, true)
        }
        fileColIndex++

        // графа 3 - справочник 62 "Признаки ценных бумаг"
        val = getCellValue(row, fileColIndex, type, true)
        if (val != null && !val.trim().isEmpty()) {
            newRow.singSecurirty = getRecordIdImport(62, 'CODE', val, fileRowIndex + rowOffset, fileColIndex + colOffset, true)
        }
        fileColIndex++

        // графа 4
        newRow.issue = getCellValue(row, fileColIndex, type, true)
        fileColIndex++

        // графа 5
        newRow.acquisitionDate = parseDate(getCellValue(row, fileColIndex, type), 'dd.MM.yyyy', fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 6
        newRow.saleDate = parseDate(getCellValue(row, fileColIndex, type), 'dd.MM.yyyy', fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 7
        newRow.amountBonds = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 8
        newRow.acquisitionPrice = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 9
        newRow.costOfAcquisition = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 10
        newRow.marketPriceInPerc = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 11
        newRow.marketPriceInRub = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 12
        newRow.acquisitionPriceTax = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 13
        newRow.redemptionValue = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 14
        newRow.priceInFactPerc = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 15
        newRow.priceInFactRub = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 16
        newRow.marketPriceInPerc1 = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 17
        newRow.marketPriceInRub1 = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 18
        newRow.salePriceTax = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 19
        newRow.expensesOnSale = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 20
        newRow.expensesTotal = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 21
        newRow.profit = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)
        fileColIndex++

        // графа 22
        newRow.excessSalePriceTax = parseNumber(getCellValue(row, fileColIndex, type), fileRowIndex + rowOffset, fileColIndex + colOffset, logger, true)

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

        // Проверка итогов
        for (def alias : totalColumns) {
            if (total[alias] != totalOneSum[alias]) {
                logger.error("Итоговые значения за текущий квартал рассчитаны неверно!")
            }
        }
    }

    dataRowHelper.save(dataRows)
}

// для получения данных из RNU или XML
String getCellValue(def row, int index, def type, boolean isTextXml = false) {
    if (type == 1) {
        return isTextXml ? row.field[index].text() : row.field[index].@value.text()
    }
    return row.cell[index + 1].text()
}