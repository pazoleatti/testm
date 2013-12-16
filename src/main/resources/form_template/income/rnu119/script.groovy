package form_template.income.rnu119

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * Форма "(РНУ-119) Регистр налогового учёта доходов и расходов, по сделкам своп, квалифицированным в качестве операций с ФИСС для целей налогообложения".
 * formTemplateId=371
 *
 * TODO:
 *      - расчет графы 15.1 и 15.2: непонтяности с ручным вводом в чтз, а также используется какая то 2.7
 *
 * @author rtimerbaev
 */

// графа 1  - 1    - number
// графа 2  - 2.1  - transactionNumber
// графа 3  - 2.2  - transactionKind                атрибут 831 KIND "Вид сделки" - справочник 91 "Виды сделок"
// графа 4  - 2.3  - contractor
// графа 5  - 2.4  - transactionType                атрибут 70 TYPE "Тип сделки" - справочник 16 "Типы сделок"
// графа 6  - 2.5  - coursFirstPart
// графа 7  - 2.6  - coursSecondPart
// графа 8  - 3    - transactionDate
// графа 9  - 4    - transactionEndDate
// графа 10 - 5.1  - transactionCalcDate
// графа 11 - 5.2  - course
// графа 12 - 6    - price
// графа 13 - 7.1  - minPrice
// графа 14 - 7.2  - maxPrice
// графа 15 - 8.1  - interestRateValue              атрибут 646 CODE "Код ставки" - справочник 72 "Процентные ставки (виды)"
// графа 16 - 8.2  - interestSpreadValue
// графа 17 - 8.3  - interestRateSize
// графа 18 - 9.1  - execFirstPart
// графа 19 - 9.2  - execSecondPart
// графа 20 - 10   - requestAmount
// графа 21 - 11.1 - liabilityInterestRateValue     атрибут 646 CODE "Код ставки" - справочник 72 "Процентные ставки (виды)"
// графа 22 - 11.2 - liabilityInterestSpreadValue
// графа 23 - 11.3 - liabilityInterestRateSize
// графа 24 - 12.1 - liabilityExecFirstPart
// графа 25 - 12.2 - liabilityExecSecondPart
// графа 26 - 13   - liabilityAmount
// графа 27 - 14.1 - income
// графа 28 - 14.2 - outcome
// графа 29 - 15.1 - deviationMinPrice
// графа 30 - 15.2 - deviationMaxPrice

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData)?.delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE :
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT :
        importData()
        calc()
        break
    case FormDataEvent.MIGRATION :
        migration()
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
def allColumns = ['fix', 'number', 'transactionNumber', 'transactionKind', 'contractor', 'transactionType', 'coursFirstPart', 'coursSecondPart', 'transactionDate', 'transactionEndDate', 'transactionCalcDate', 'course', 'price', 'minPrice', 'maxPrice', 'interestRateValue', 'interestSpreadValue', 'interestRateSize', 'execFirstPart', 'execSecondPart', 'requestAmount', 'liabilityInterestRateValue', 'liabilityInterestSpreadValue', 'liabilityInterestRateSize', 'liabilityExecFirstPart', 'liabilityExecSecondPart', 'liabilityAmount', 'income', 'outcome', 'deviationMinPrice', 'deviationMaxPrice']

// Редактируемые атрибуты (графа 2.1 .. 5.1, 6..13)
@Field
def editableColumns = ['transactionNumber', 'transactionKind', 'contractor', 'transactionType', 'coursFirstPart', 'coursSecondPart', 'transactionDate', 'transactionEndDate', 'transactionCalcDate', 'course', 'price', 'minPrice', 'maxPrice', 'interestRateValue', 'interestSpreadValue', 'interestRateSize', 'execFirstPart', 'execSecondPart', 'requestAmount', 'liabilityInterestRateValue', 'liabilityInterestSpreadValue', 'liabilityInterestRateSize', 'liabilityExecFirstPart', 'liabilityExecSecondPart', 'liabilityAmount']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = allColumns - editableColumns

// Проверяемые на пустые значения атрибуты (графа 1 .. 8.3, 11.1 .. 11.3, 13 .. 15.2)
@Field
def nonEmptyColumns = ['number', 'transactionNumber', 'transactionKind', 'contractor', 'transactionType', 'coursFirstPart', 'coursSecondPart', 'transactionDate', 'transactionEndDate', 'transactionCalcDate', 'course', 'price', 'minPrice', 'maxPrice', 'interestRateValue', 'interestSpreadValue', 'interestRateSize', 'liabilityInterestRateValue', 'liabilityInterestSpreadValue', 'liabilityInterestRateSize', 'liabilityAmount', 'income', 'outcome', 'deviationMinPrice', 'deviationMaxPrice']

// Атрибуты итоговых строк для которых вычисляются суммы (графа 9.1 .. 10, 12.2 .. 15.2)
@Field
def totalSumColumns = ['execFirstPart', 'execSecondPart', 'requestAmount', 'liabilityExecFirstPart', 'liabilityExecSecondPart', 'liabilityAmount','income', 'outcome', 'deviationMinPrice', 'deviationMaxPrice']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Текущая дата
@Field
def currentDate = new Date()

@Field
def sdf = new SimpleDateFormat('dd.MM.yyyy')

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

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
                     boolean required = true) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

/** Расчеты. Алгоритмы заполнения полей формы. */
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    def rowNumber = 0
    dataRows.each { row ->
        // графа 1
        row.number = ++rowNumber

        // графа 14.1
        row.income = calc14_1(row)

        // графа 14.2
        row.outcome = calc14_2(row)

        def values = calc15(row)

        // графа 15.1
        row.deviationMinPrice = values[0]
        // графа 15.2
        row.deviationMaxPrice = values[1]
    }

    // добавить строку "итого"
    def totalRow = getTotalRow(dataRows)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

/** Логические проверки. */
void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // дополнительно проверяемые на пустые значения атрибуты 8.1 .. 8.3, 10, 11.1 .. 11.3, 13
    def nonEmptyColumns2 = ['interestRateValue', 'interestSpreadValue', 'interestRateSize',
            'requestAmount', 'liabilityInterestRateValue', 'liabilityInterestSpreadValue',
            'liabilityInterestRateSize', 'liabilityAmount']
    // алиасы графов для арифметической проверки (графа 14.11 .. 15.2)
    def arithmeticCheckAlias = ['income', 'outcome', 'deviationMinPrice', 'deviationMaxPrice']
    // для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    def rowNumber = 0
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Обязательность заполнения полей
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка заполнения граф 9 и 12
        if ((row.execFirstPart != null && row.execSecondPart == null && row.liabilityExecFirstPart) ||
                (row.execSecondPart != null && row.execFirstPart == null && row.liabilityExecSecondPart)) {
            logger.errro(errorMsg + 'Неверно заполнены графы 9 и 12')
        }

        // 3. Проверка на заполнение граф 8.1 - 8.3, 10, 11, 13
        def kind = getRefBookValue(91, row.transactionKind)?.KIND?.value
        if (kind == 'CCIRS' || kind == 'IRS') {
            checkNonEmptyColumns(row, index, nonEmptyColumns2, logger, true)
        }

        // . Проверка на уникальность поля «№ пп» (графа 1)
        if (++rowNumber != row.number) {
            logger.error(errorMsg + 'Нарушена уникальность номера по порядку!')
        }

        // 4. Арифметические проверки расчета неитоговых граф
        def values = calc15(row)
        needValue['income'] = calc14_1(row)
        needValue['outcome'] = calc14_2(row)
        needValue['deviationMinPrice'] = values[0]
        needValue['deviationMaxPrice'] = values[1]
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // Проверки соответствия НСИ
        // 1. Проверка соответствия справочнику «Вид сделки» (графа 2.2)
        checkNSI(91, row, 'transactionKind')

        // 2. Проверка соответствия справочнику «Типы сделок» (графа 2.4)
        checkNSI(16, row, 'transactionType')

        // 3. Проверка курса валюты	(графа 5.2)
        if (row.transactionDate != null && row.course != null &&
                getRecord(22, "RATE = $row.course", row.transactionDate) == null) {
            RefBook rb = refBookFactory.get(22)
            def rbName = rb.getName()
            def attrName = rb.getAttribute('RATE').getName()
            logger.error(errorMsg + "В справочнике «$rbName» не найдено значение «${row.course}», соответствующее атрибуту «$attrName»!")
        }

        // 4. Проверка значения процентной ставки
        checkNSI(72, row, 'interestRateValue')
        checkNSI(72, row, 'liabilityInterestRateValue')
    }

    // 5. Арифметические проверки расчета итоговой строки
    checkTotalSum(dataRows, totalSumColumns, logger, true)
}

/** Получение импортируемых данных. */
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
        logger.error('Формат файла должен быть *.r??')
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

        // рассчитать, проверить и сравнить итоги
        if (totalLoad != null) {
            checkTotalRow(totalLoad)
        } else {
            logger.error("Нет итоговой строки.")
        }
    } catch(Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.message)
    }
}

void migration() {
    importData()
    if (!logger.containsLevel(LogLevel.ERROR)) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.allCached
        def total = getTotalRow(dataRows)
        dataRowHelper.insert(total, dataRows.size() + 1)
    }
}

/*
 * Вспомогательные методы.
 */

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

/** Сформировать итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.getCell('fix').setColSpan(2)
    newRow.fix = 'Итого:'
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalSumColumns)
    return newRow
}

def calc14_1(def row) {
    if (row.execFirstPart == null || row.execSecondPart == null || row.liabilityExecFirstPart == null ||
            row.liabilityExecSecondPart == null || row.requestAmount == null || row.liabilityAmount == null) {
        return null
    }
    def tmp
    if (row.execFirstPart + row.execSecondPart + row.liabilityExecFirstPart + row.liabilityExecSecondPart > 0 &&
            row.requestAmount + row.liabilityAmount > 0) {
        tmp = row.execFirstPart + row.execSecondPart + row.liabilityExecFirstPart +
                row.liabilityExecSecondPart + row.requestAmount + row.liabilityAmount
    }
    return roundValue(tmp, 2)
}

def calc14_2(def row) {
    if (row.execFirstPart == null || row.execSecondPart == null || row.liabilityExecFirstPart == null ||
            row.liabilityExecSecondPart == null || row.requestAmount == null || row.liabilityAmount == null) {
        return null
    }
    def tmp
    if (row.execFirstPart + row.execSecondPart + row.liabilityExecFirstPart + row.liabilityExecSecondPart < 0 &&
            row.requestAmount + row.liabilityAmount < 0) {
        tmp = row.execFirstPart + row.execSecondPart + row.liabilityExecFirstPart +
                row.liabilityExecSecondPart + row.requestAmount + row.liabilityAmount
    }
    return roundValue(tmp, 2)
}

/**
 * Получить значения для графы 15.1 и 15.2.
 *
 * @param row строка формы
 * @return список со значениями (1 элемент - для графы 15.1, 2 элемент - 15.2)
 */
def calc15(def row) {
    if (row.transactionKind == null || row.transactionType == null || row.coursFirstPart == null ||
            row.coursSecondPart == null || row.price == null || row.minPrice == null ||
            row.maxPrice == null || row.interestRateValue == null || row.interestSpreadValue == null ||
            row.interestRateSize == null || row.requestAmount == null || row.liabilityInterestRateValue == null ||
            row.liabilityInterestSpreadValue == null || row.liabilityInterestRateSize == null ||
            row.liabilityExecFirstPart == null || row.liabilityAmount == null) {
        return [null, null]
    }
    def value15_1 = null
    def value15_2 = null
    def kind = getRefBookValue(91, row.transactionKind)?.KIND?.value
    if (kind == 'CCIRS' || kind == 'IRS') {
        def code11 = getRefBookValue(72, row.liabilityInterestRateValue)?.CODE?.value
        if (code11 == 'fix') {
            if (row.liabilityInterestRateSize <= row.maxPrice) {
                value15_2 = 0
            } else {
                value15_2 = row.maxPrice * row.liabilityAmount / row.liabilityInterestRateSize - row.liabilityAmount
            }
            value15_1 = 0
        } else if (code11 == 'float') {
            // Если знак в «Графе 11.2» совпадает со знаком в «Графе 6»
            if ((row.liabilityInterestSpreadValue > 0 && row.coursFirstPart > 0) ||
                    (row.liabilityInterestSpreadValue < 0 && row.coursFirstPart < 0)) {
                if (row.liabilityInterestSpreadValue >= 0) {
                    if (row.liabilityInterestSpreadValue > row.maxPrice) {
                        value15_2 = -((row.liabilityInterestSpreadValue - row.maxPrice) * row.liabilityAmount) / row.liabilityInterestRateSize
                    } else if (row.liabilityInterestSpreadValue <= row.minPrice) {
                        value15_2 = 0
                    }
                } else {
                    if (row.liabilityInterestSpreadValue < row.maxPrice) {
                        value15_2 = 0
                    } else if (row.liabilityInterestSpreadValue > row.minPrice) {
                        value15_2 = -((row.liabilityInterestSpreadValue - row.minPrice) * row.liabilityAmount) / row.liabilityInterestRateSize
                    }
                    value15_1 = 0
                }
            } else {
                if (row.liabilityInterestSpreadValue > 0) {
                    value15_2 = -((row.liabilityInterestSpreadValue - row.minPrice) * row.liabilityAmount) / row.liabilityInterestRateSize
                } else {
                    value15_2 = 0
                }
                value15_1 = 0
            }
        }
        def code8 = getRefBookValue(72, row.interestRateValue)?.CODE?.value
        if (code8 == 'fix') {
            if (row.interestRateSize >= row.minPrice) {
                value15_1 = 0
            } else {
                value15_1 = row.minPrice * row.requestAmount / row.interestRateSize - row.requestAmount
            }
            value15_2 = 0
        } else if (code8 == 'float') {
            // Если знак в »Графа 8.2» совпадает со знаком в «Графе 6», то
            if ((row.interestSpreadValue > 0 && row.price > 0) || (row.interestSpreadValue <0 && row.price < 0)) {
                if (row.interestSpreadValue >= 0) {
                    if (row.interestSpreadValue >= row.maxPrice) {
                        value15_1 = 0
                    } else {
                        value15_1 = -((row.interestSpreadValue - row.minPrice) * row.requestAmount) / row.interestRateSize
                    }
                } else {
                    if (row.interestSpreadValue < row.maxPrice) {
                        value15_1 = -((row.interestSpreadValue - row.maxPrice) * row.requestAmount) / row.interestRateSize
                    }
                    if (abs(row.interestSpreadValue) <= abs(row.maxPrice)) {
                        value15_1 = 0
                    }
                    value15_2 = 0
                }
            } else {
                if (row.interestSpreadValue > 0) {
                    value15_1 = 0
                } else if (row.interestSpreadValue > 0) {
                    value15_1 = -((row.interestSpreadValue - row.minPrice) * row.requestAmount) / row.interestRateSize
                }
                value15_2 = 0
            }
        }
    } else if (kind == 'CCS') {
        if (row.execSecondPart == null && row.liabilityExecSecondPart == null &&
                row.deviationMinPrice == 0 && row.deviationMaxPrice == 0) {
            // TODO (Ramil Timerbaev) Заполняется вручную?!
        }
        if (row.execSecondPart != null && row.liabilityExecSecondPart != null) {
            def tmp2 = row.coursSecondPart - row.coursFirstPart
            if (row.minPrice <= tmp2 && tmp2 <= row.maxPrice) {
                value15_1 = 0
                value15_2 = 0
            } else {
                def type2 = getRefBookValue(16, row.transactionType)?.TYPE?.value
                if (type2 == 'покупка' && tmp2 > row.maxPrice) {
                    value15_1 = 0
                    // TODO (Ramil Timerbaev) что за графа 2.7
                    value15_2 = (row.income + row.outcome) * (row.course - row.maxPrice + row.coursSecondPart) /
                            (row.course - row.XXXX) - (row.income + row.outcome)
                } else if (type2 == 'покупка' && tmp2 <= row.minPrice) {
                    value15_1 = 0
                    value15_2 = 0
                } else if (type2 == 'продажа' && tmp2 > row.maxPrice) {
                    value15_1 = 0
                    value15_2 = 0
                } else if (type2 == 'продажа' && tmp2 <= row.minPrice) {
                    // TODO (Ramil Timerbaev) что за графа 2.7
                    value15_1 = (row.income + row.outcome) * (row.minPrice + row.coursSecondPart - row.course) /
                            (row.XXXX - row.course) - (row.income + row.outcome)
                }
            }
        }
        value15_2 = 0
    }
    return [roundValue(value15_1, 2), roundValue(value15_2, 2)]
}

/**
 * Заполнить форму данными.
 *
 * @param xml данные
 *
 * return итоговая строка
 */
def addData(def xml) {
    reportPeriodEndDate = reportPeriodService?.get(formData?.reportPeriodId)?.taxPeriod?.getEndDate()
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.clear()
    def newRows = []

    def indexRow = 0
    for (def row : xml.row) {
        indexRow++

        def newRow = getNewRow()
        newRow.setIndex(indexRow)
        def indexCell = 0

        // TODO (Ramil Timerbaev) доделать
        // графа 1
        newRow.rowNumber = indexRow
        indexCell++

        // графа
        newRow.xxx = row.cell[indexCell].text()
        indexCell++

        // графа
        newRow.yyy = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)
        indexCell++

        // графа
        newRow.zzz = getRecordIdImport(62, 'CODE', row.cell[indexCell].text(), indexRow, indexCell + 1, true)
        indexCell++

        newRows.add(newRow)
    }
    dataRowHelper.save(newRows)

    // итоговая строка
    if (xml.rowTotal.size() == 1) {
        def row = xml.rowTotal[0]
        def total = formData.createDataRow()
        def indexCell

        // TODO (Ramil Timerbaev) поменять
        // графа 4
        indexCell = 3
        total.lotSizePrev = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        // графа 5
        indexCell = 4
        total.lotSizeCurrent = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        // графа 7
        indexCell = 6
        total.cost = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        // графа 10
        indexCell = 9
        total.costOnMarketQuotation = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        // графа 11
        indexCell = 10
        total.reserveCalcValue = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        // графа 12
        indexCell = 11
        total.reserveCreation = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        // графа 13
        indexCell = 12
        total.reserveRecovery = getNumber(row.cell[indexCell].text(), indexRow, indexCell + 1)

        return total
    } else {
        return null
    }
}

def roundValue(def value, int precision) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

/**
 * Cравнить итоги.
 *
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    // TODO (Ramil Timerbaev) переделать
    return
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalCalc = getCalcTotalRow(dataRows)

    def totalSumColumns = [4 : 'lotSizePrev', 5 : 'lotSizeCurrent', 7 : 'cost', 10 : 'costOnMarketQuotation',
            11 : 'reserveCalcValue', 12 : 'reserveCreation', 13 : 'reserveRecovery']
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
        logger.error("Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

def abs(def value) {
    return (value < 0 ? -value : value)
}

/**
 * Аналог FormDataServiceImpl.getRefBookRecord(...) но ожидающий получения из справочника больше одной записи.
 *
 * @return первая из найденных записей
 */
def getRecord(def refBookId, def filter, Date date) {
    if (refBookId == null) {
        return null
    }
    String dateStr = sdf.format(date)
    if (recordCache.containsKey(refBookId)) {
        Long recordId = recordCache.get(refBookId).get(dateStr + filter)
        if (recordId != null) {
            if (refBookCache != null) {
                return refBookCache.get(recordId)
            } else {
                def retVal = new HashMap<String, RefBookValue>()
                retVal.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, recordId))
                return retVal
            }
        }
    } else {
        recordCache.put(refBookId, [:])
    }

    def provider
    if (!providerCache.containsKey(refBookId)) {
        providerCache.put(refBookId, refBookFactory.getDataProvider(refBookId))
    }
    provider = providerCache.get(refBookId)

    def records = provider.getRecords(date, null, filter, null)
    // отличие от FormDataServiceImpl.getRefBookRecord(...)
    if (records.size() > 0) {
        def retVal = records.get(0)
        Long recordId = retVal.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue()
        recordCache.get(refBookId).put(dateStr + filter, recordId)
        if (refBookCache != null)
            refBookCache.put(recordId, retVal)
        return retVal
    }
    return null
}