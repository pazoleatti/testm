package form_template.income.app2_src_1.v2015

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с
 * финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов (ЦФО НДФЛ).
 * formTemplateId=418
 *
 * Первичная форма.
 */

// графа 1  - innRF
// графа 2  - inn
// графа 3  - surname
// графа 4  - name
// графа 5  - patronymic
// графа 6  - status
// графа 7  - birthday
// графа 8  - citizenship       - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
// графа 9  - code              - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
// графа 10 - series
// графа 11 - postcode
// графа 12 - region            - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
// графа 13 - district
// графа 14 - city
// графа 15 - locality
// графа 16 - street
// графа 17 - house
// графа 18 - housing
// графа 19 - apartment
// графа 20 - country           - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
// графа 21 - address
// графа 22 - taxRate
// графа 23 - income
// графа 24 - deduction
// графа 25 - taxBase
// графа 26 - calculated
// графа 27 - withheld
// графа 28 - listed
// графа 29 - withheldAgent
// графа 30 - nonWithheldAgent

// графа 31 - col_040_1         - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
// графа 32 - col_041_1
// графа 33 - col_042_1_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 34 - col_043_1_1
// графа 35 - col_042_1_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 36 - col_043_1_2
// графа 37 - col_042_1_3       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 38 - col_043_1_3
// графа 39 - col_042_1_4       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 40 - col_043_1_4
// графа 41 - col_042_1_5       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 42 - col_043_1_5

// графа 43 - col_040_2         - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
// графа 44 - col_041_2
// графа 45 - col_042_2_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 46 - col_043_2_1
// графа 47 - col_042_2_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 48 - col_043_2_2
// графа 49 - col_042_2_3       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 50 - col_043_2_3
// графа 51 - col_042_2_4       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 52 - col_043_2_4
// графа 53 - col_042_2_5       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 54 - col_043_2_5

// графа 55 - col_040_3         - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
// графа 56 - col_041_3
// графа 57 - col_042_3_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 58 - col_043_3_1
// графа 59 - col_042_3_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 60 - col_043_3_2
// графа 61 - col_042_3_3       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 62 - col_043_3_3
// графа 63 - col_042_3_4       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 64 - col_043_3_4
// графа 65 - col_042_3_5       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 66 - col_043_3_5
// графа 67 - col_051_3_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 68 - col_052_3_1
// графа 69 - col_051_3_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 70 - col_052_3_2

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
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        calc()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE: // Консолидация
        formDataService.consolidationSimple(formData, logger)
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
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
def allColumns = ['innRF', 'inn', 'surname', 'name', 'patronymic', 'status', 'birthday', 'citizenship', 'code', 'series', 'postcode', 'region', 'district', 'city', 'locality', 'street', 'house', 'housing', 'apartment','country', 'address', 'taxRate', 'income', 'deduction', 'taxBase', 'calculated', 'withheld', 'listed', 'withheldAgent', 'nonWithheldAgent', 'col_040_1', 'col_041_1', 'col_042_1_1', 'col_043_1_1', 'col_042_1_2','col_043_1_2', 'col_042_1_3', 'col_043_1_3', 'col_042_1_4', 'col_043_1_4', 'col_042_1_5', 'col_043_1_5', 'col_040_2', 'col_041_2', 'col_042_2_1', 'col_043_2_1', 'col_042_2_2', 'col_043_2_2', 'col_042_2_3','col_043_2_3', 'col_042_2_4', 'col_043_2_4', 'col_042_2_5', 'col_043_2_5', 'col_040_3', 'col_041_3', 'col_042_3_1', 'col_043_3_1', 'col_042_3_2', 'col_043_3_2', 'col_042_3_3', 'col_043_3_3', 'col_042_3_4','col_043_3_4', 'col_042_3_5', 'col_043_3_5', 'col_051_3_1', 'col_052_3_1', 'col_051_3_2', 'col_052_3_2']

// Автозаполняемые атрибуты (графа 23..25)
@Field
def autoFillColumns = ['income', 'deduction', 'taxBase']

// Редактируемые атрибуты (графа 1..22, 26..70)
@Field
def editableColumns = allColumns - autoFillColumns

// Проверяемые на пустые значения атрибуты (графа 3, 4, 6, 22, 23, 25, 26)
@Field
def nonEmptyColumns = ['surname', 'name', 'status', 'taxRate', 'income', 'taxBase', 'calculated']

// мапа с алиасами граф и номерами колонокв в xml (алиас -> номер колонки в xml)
@Field
def totalColumnsIndexMap = [
        'income'           : 23,
        'deduction'        : 24,
        'taxBase'          : 25,
        'calculated'       : 26,
        'withheld'         : 27,
        'listed'           : 28,
        'withheldAgent'    : 29,
        'nonWithheldAgent' : 30,
        'col_041_1'        : 32,
        'col_043_1_1'      : 34,
        'col_043_1_2'      : 36,
        'col_043_1_3'      : 38,
        'col_043_1_4'      : 40,
        'col_043_1_5'      : 42,
        'col_041_2'        : 44,
        'col_043_2_1'      : 46,
        'col_043_2_2'      : 48,
        'col_043_2_3'      : 50,
        'col_043_2_4'      : 52,
        'col_043_2_5'      : 54,
        'col_041_3'        : 56,
        'col_043_3_1'      : 58,
        'col_043_3_2'      : 60,
        'col_043_3_3'      : 62,
        'col_043_3_4'      : 64,
        'col_043_3_5'      : 66,
        'col_052_3_1'      : 68,
        'col_052_3_2'      : 70
]

@Field
def endDate = null

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = true) {
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
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

// Поиск записи в справочнике по значению (для расчетов) + по дате
def getRefBookRecord(
        def Long refBookId, def String alias, def String value, def Date day, def int rowIndex, def String cellName,
        boolean required) {
    return formDataService.getRefBookRecord(refBookId, recordCache, providerCache, refBookCache, alias, value,
            day, rowIndex, cellName, logger, required)
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

//// Кастомные методы

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    // адрес (графа 11, 13..19)
    def address = ['postcode', 'district', 'city', 'locality', 'street', 'house', 'housing', 'apartment']

    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1-7. Проверка на заполнение поля «<Наименование поля>»
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 8. Проверка на заполнение поля «<Наименование поля>»
        if (address.find { row.getCell(it).value != null }) {
            checkNonEmptyColumns(row, index, ['region'], logger, true)
        }

        // 9. Проверка вводимых символов в поле «Серия и номер документа»
        if (row.series != null && !row.series?.matches("^[а-яА-ЯёЁa-zA-Z0-9]+\$")) {
            def name = getColumnName(row, 'series')
            logger.error(errorMsg + "Графа «$name» содержит недопустимые символы!")
        }

        // 10. Проверка заполнения поля «Номер дома (владения)»
        if (row.house && !row.house?.matches("^[а-яА-ЯёЁa-zA-Z0-9/-]+\$")) {
            def name = getColumnName(row, 'house')
            logger.error(errorMsg + "Графа «$name» содержит недопустимые символы!")
        }

        // 11-18. Проверка заполнения полей 11, 13, 14, 15, 16, 17, 18, 19
        if (row.region != null) {
            checkNonEmptyColumns(row, row.getIndex(), address, logger, true)
        }

        // 19. Проверка заполнения графы «Регион (код)»
        if (row.country == null && row.address == null && row.region == null) {
            def name = getColumnName(row, 'region')
            logger.error(errorMsg + "Графа «$name» не заполнена!")
        }

        // 20. Проверка правильности заполнения графы «ИНН в стране гражданства»
        if (row.inn && row.citizenship && getRefBookValue(10L, row.citizenship)?.CODE?.value == '643') {
            def nameInn = getColumnName(row, 'inn')
            def nameCitizenship = getColumnName(row, 'citizenship')
            logger.error(errorMsg + "Графа «$nameInn» не должно быть заполнено, если графа «$nameCitizenship» равна «643»")
        }

        // 21. Проверка правильности заполнения графы «Статус налогоплательщика»
        if (row.status && !row.status?.matches("^[1-3]+\$")) {
            def name = getColumnName(row, 'status')
            logger.error(errorMsg + "Графа «$name» содержит недопустимое значение! Поле может содержать только одно из значений: «1», «2», «3»")
        }

        // 22-24. Проверка соответствия суммы дохода суммам вычета
        def String errorMsg1 = errorMsg + "Сумма граф «Сумма вычета» для кода дохода = «%s» превышает значение поля «Сумма дохода» для данного кода."
        if (getSum1(row) > roundValue(row.col_041_1 ?: 0)) {
            def value = getRefBookValue(370L, row.col_040_1)?.CODE?.value
            logger.error(String.format(errorMsg1, value))
        }
        if (getSum2(row) > roundValue(row.col_041_2 ?: 0)) {
            def value = getRefBookValue(370L, row.col_040_2)?.CODE?.value
            logger.error(String.format(errorMsg1, value))
        }
        if (getSum3(row) > roundValue(row.col_041_3 ?: 0)) {
            def value = getRefBookValue(370L, row.col_040_3)?.CODE?.value
            logger.error(String.format(errorMsg1, value))
        }

        // 27. Арифметические проверки расчета граф 23, 25, 26
        needValue['income'] = calc23(row)
        needValue['deduction'] = calc24(row)
        needValue['taxBase'] = calc25(row)
        def arithmeticCheckAlias = needValue.keySet().asList()
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    if (dataRows.isEmpty()) {
        return
    }

    def forColumn24 = getSumString([34, 36, 38, 40, 42, 46, 48, 50, 52, 54, 58, 60, 62, 64, 66, 68, 70])

    for (DataRow<Cell> row in dataRows) {
        // проверки, выполняемые до расчёта
        def int index = row.getIndex()

        // графа 23
        def BigDecimal value23 = calc23(row)
        checkOverflow(value23, row, 'income', index, 15, '«Графа 32» + «Графа 44» + «Графа 56»')
        row.income = value23

        // графа 24
        def BigDecimal value24 = calc24(row)
        checkOverflow(value24, row, 'deduction', index, 15, forColumn24)
        row.deduction = value24

        // графа 25
        def BigDecimal value25 = calc25(row)
        checkOverflow(value25, row, 'taxBase', index, 15, '«Графа 23» - «Графа 24»')
        row.taxBase = value25
    }

    dataRowHelper.update(dataRows)

    // Сортировка групп и строк
    sortFormDataRows()
}

/**
 * Получить строку с просуммированнвыми графами.
 *
 * @param list список с номерами графов
 */
def getSumString(def list) {
    def tmp = []
    list.each {
        tmp.add("«Графа $it»")
    }
    return tmp.join(' + ')
}

def BigDecimal calc23(def row) {
    return getSum(row, ['col_041_1', 'col_041_2', 'col_041_3'])
}

def BigDecimal calc24(def row) {
    // графа 68, 70
    def tmp = getSum(row, ['col_052_3_1', 'col_052_3_2'])
    return getSum1(row) + getSum2(row) + getSum3(row) + tmp
}

def BigDecimal calc25(def row) {
    return roundValue((row.income ?: 0) - (row.deduction ?: 0))
}

/** Получить сумму «Графа 34» + «Графа 36» + «Графа 38» + «Графа 40» + «Графа 42». */
def BigDecimal getSum1(def row) {
    return getSum(row, ['col_043_1_1', 'col_043_1_2', 'col_043_1_3', 'col_043_1_4', 'col_043_1_5'])
}

/** Получить сумму «Графа 46» + «Графа 48» + « Графа 50» + «Графа 52» + «Графа 54». */
def BigDecimal getSum2(def row) {
    return getSum(row, ['col_043_2_1', 'col_043_2_2', 'col_043_2_3', 'col_043_2_4', 'col_043_2_5'])
}

/** Получить сумму «Графа 58» + «Графа 60» + «Графа 62» + «Графа 64» + «Графа 66». */
def BigDecimal getSum3(def row) {
    return getSum(row, ['col_043_3_1', 'col_043_3_2', 'col_043_3_3', 'col_043_3_4', 'col_043_3_5'])
}

/**
 * Получить сумму графов одной строки.
 *
 * @param row строка
 * @param columns список алиасов столбцов
 */
def BigDecimal getSum(def row, def columns) {
    def tmp = columns.sum { row[it] ?: 0 }
    return roundValue(tmp)
}

// Получение импортируемых данных
void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'innRF'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 70, 1)

    def headerMapping = [:]
    def index = 0
    allColumns.each { alias ->
        headerMapping.put((xml.row[0].cell[index]), getColumnName(tmpRow, alias))
        headerMapping.put((xml.row[1].cell[index]), (index + 1).toString())
        index++
    }
    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1
    def required = true

    for (def row : xml.row) {
        xmlIndexRow++

        // Пропуск строк шапки
        if (xmlIndexRow < headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def int xlsIndexRow = xmlIndexRow + rowOffset

        def newRow = getNewRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)

        // Графа 1
        def xmlIndexCol = 0
        newRow.innRF = row.cell[xmlIndexCol].text()

        // Графа 2
        xmlIndexCol++
        newRow.inn = row.cell[xmlIndexCol].text()

        // Графа 3
        xmlIndexCol++
        newRow.surname = row.cell[xmlIndexCol].text()

        // Графа 4
        xmlIndexCol++
        newRow.name = row.cell[xmlIndexCol].text()

        // Графа 5
        xmlIndexCol++
        newRow.patronymic = row.cell[xmlIndexCol].text()

        // Графа 6
        xmlIndexCol++
        newRow.status = row.cell[xmlIndexCol].text()

        // Графа 7
        xmlIndexCol++
        newRow.birthday = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 8 - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
        xmlIndexCol++
        newRow.citizenship = getRecordIdImport(10L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 9 - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
        xmlIndexCol++
        newRow.code = getRecordIdImport(360L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 10
        xmlIndexCol++
        newRow.series = row.cell[xmlIndexCol].text()

        // Графа 11
        xmlIndexCol++
        newRow.postcode = row.cell[xmlIndexCol].text()

        // Графа 12 - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
        xmlIndexCol++
        newRow.region = getRecordIdImport(4L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 13
        xmlIndexCol++
        newRow.district = row.cell[xmlIndexCol].text()

        // Графа 14
        xmlIndexCol++
        newRow.city = row.cell[xmlIndexCol].text()

        // Графа 15
        xmlIndexCol++
        newRow.locality = row.cell[xmlIndexCol].text()

        // Графа 16
        xmlIndexCol++
        newRow.street = row.cell[xmlIndexCol].text()

        // Графа 17
        xmlIndexCol++
        newRow.house = row.cell[xmlIndexCol].text()

        // Графа 18
        xmlIndexCol++
        newRow.housing = row.cell[xmlIndexCol].text()

        // Графа 19
        xmlIndexCol++
        newRow.apartment = row.cell[xmlIndexCol].text()

        // Графа 20 - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
        xmlIndexCol++
        newRow.country = getRecordIdImport(10L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 21
        xmlIndexCol++
        newRow.address = row.cell[xmlIndexCol].text()

        // Графа 22
        xmlIndexCol++
        newRow.taxRate = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 23
        xmlIndexCol++
        // newRow.income = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 24
        xmlIndexCol++
        // newRow.deduction = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 25
        xmlIndexCol++
        // newRow.taxBase = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 26
        xmlIndexCol++
        newRow.calculated = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 27
        xmlIndexCol++
        newRow.withheld = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 28
        xmlIndexCol++
        newRow.listed = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 29
        xmlIndexCol++
        newRow.withheldAgent = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 30
        xmlIndexCol++
        newRow.nonWithheldAgent = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 31 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
        xmlIndexCol++
        newRow.col_040_1 = getRecordIdImport(370, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 32
        xmlIndexCol++
        newRow.col_041_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 33 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_1 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 34
        xmlIndexCol++
        newRow.col_043_1_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 35 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_2 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 36
        xmlIndexCol++
        newRow.col_043_1_2 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 37 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_3 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 38
        xmlIndexCol++
        newRow.col_043_1_3 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 39 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_4 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 40
        xmlIndexCol++
        newRow.col_043_1_4 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 41 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_5 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 42
        xmlIndexCol++
        newRow.col_043_1_5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 43 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
        xmlIndexCol++
        newRow.col_040_2 = getRecordIdImport(370, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 44
        xmlIndexCol++
        newRow.col_041_2 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 45 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_1 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 46
        xmlIndexCol++
        newRow.col_043_2_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 47 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_2 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 48
        xmlIndexCol++
        newRow.col_043_2_2 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 49 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_3 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 50
        xmlIndexCol++
        newRow.col_043_2_3 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 51 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_4 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 52
        xmlIndexCol++
        newRow.col_043_2_4 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 53 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_5 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 54
        xmlIndexCol++
        newRow.col_043_2_5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 55 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
        xmlIndexCol++
        newRow.col_040_3 = getRecordIdImport(370, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 56
        xmlIndexCol++
        newRow.col_041_3 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 57 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_1 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 58
        xmlIndexCol++
        newRow.col_043_3_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 59 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_2 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 60
        xmlIndexCol++
        newRow.col_043_3_2 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 61 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_3 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 62
        xmlIndexCol++
        newRow.col_043_3_3 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 63 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_4 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 64
        xmlIndexCol++
        newRow.col_043_3_4 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 65 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_5 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 66
        xmlIndexCol++
        newRow.col_043_3_5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 67 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_051_3_1 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 68
        xmlIndexCol++
        newRow.col_052_3_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 69 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_051_3_2 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 70
        xmlIndexCol++
        newRow.col_052_3_2 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

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

// Сортировка групп и строк
void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    dataRowHelper.saveSort()
}

// Округляет число до требуемой точности
def roundValue(def value, int precision = 2) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}

void importTransportData() {
    int COLUMN_COUNT = 70
    int TOTAL_ROW_COUNT = 1
    int ROW_MAX = 1000
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\''

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.clear()

    checkBeforeGetXml(ImportInputStream, UploadFileName)

    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    def dataRows = []
    String[] rowCells
    // количество пустых строк
    int countEmptyRow = 0
    int fileRowIndex = 0    // номер строки в файле
    int rowIndex = 0        // номер строки в НФ
    int totalRowCount = 0   // счетчик кол-ва итогов
    // итоговая строка со значениями из тф для добавления
    def total = null
    // итоговая строка для сверки сумм
    def totalTmp = formData.createDataRow()
    totalColumnsIndexMap.keySet().asList().each { alias ->
        totalTmp.getCell(alias).setValue(BigDecimal.ZERO, null)
    }

    while ((rowCells = reader.readNext()) != null) {
        fileRowIndex++
        // если еще не было пустых строк, то это первая строка - заголовок
        if (rowCells.length == 1 && rowCells[0].length() < 1) { // если встретилась вторая пустая строка, то дальше только строки итогов и ЦП
            if (countEmptyRow > 0) {
                totalRowCount++
                // итоговая строка
                total = getNewRow(reader.readNext(), COLUMN_COUNT, ++fileRowIndex, ++rowIndex)
                break
            }
            countEmptyRow++
            continue
        }

        // обычная строка
        if (countEmptyRow != 0 && !addRow(dataRows, rowCells, COLUMN_COUNT, fileRowIndex, ++rowIndex, totalTmp)) {
            break
        }
        rowCells = null // очищаем кучу
        // периодически сбрасываем строки
        if (dataRows.size() > ROW_MAX) {
            dataRowHelper.insert(dataRows, dataRowHelper.allCached.size() + 1)
            dataRows.clear()
        }
    }
    if (TOTAL_ROW_COUNT != 0 && totalRowCount != TOTAL_ROW_COUNT) {
        logger.error(ROW_FILE_WRONG, fileRowIndex)
    }
    reader.close()

    // сравнение итогов
    if (total) {
        def colOffset = 1
        for (def alias : totalColumnsIndexMap.keySet().asList()) {
            def v1 = total.getCell(alias).value
            def v2 = totalTmp.getCell(alias).value
            if (v1 == null && v2 == null) {
                continue
            }
            if (v1 == null || v1 != null && v1 != v2) {
                logger.warn(TRANSPORT_FILE_SUM_ERROR, totalColumnsIndexMap[alias] + colOffset, fileRowIndex)
            }
        }
    }

    if (dataRows.size() != 0) {
        dataRowHelper.insert(dataRows, dataRowHelper.allCached.size() + 1)
        dataRows.clear()
    }
}

/** Добавляет строку в текущий буфер строк. */
boolean addRow(def dataRowsCut, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex, def totalTmp) {
    if (rowCells == null) {
        return true
    }

    def newRow = getNewRow(rowCells, columnCount, fileRowIndex, rowIndex)
    if (newRow == null) {
        return false
    }
    // подсчет сумм для итогов
    totalColumnsIndexMap.keySet().asList().each { alias ->
        def value1 = totalTmp.getCell(alias).value
        def value2 = (newRow.getCell(alias).value ?: BigDecimal.ZERO)
        totalTmp.getCell(alias).setValue(value1 + value2, null)
    }
    dataRowsCut.add(newRow)
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
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return null
    }

    def required = true
    def int colOffset = 1
    def int colIndex = 1

    // Графа 1
    newRow.innRF = pure(rowCells[colIndex])

    // Графа 2
    colIndex++
    newRow.inn = pure(rowCells[colIndex])

    // Графа 3
    colIndex++
    newRow.surname = pure(rowCells[colIndex])

    // Графа 4
    colIndex++
    newRow.name = pure(rowCells[colIndex])

    // Графа 5
    colIndex++
    newRow.patronymic = pure(rowCells[colIndex])

    // Графа 6
    colIndex++
    newRow.status = pure(rowCells[colIndex])

    // Графа 7
    colIndex++
    newRow.birthday = parseDate(pure(rowCells[colIndex]), "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 8 - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
    colIndex++
    newRow.citizenship = getRecordIdImport(10L, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 9 - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
    colIndex++
    newRow.code = getRecordIdImport(360L, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 10
    colIndex++
    newRow.series = pure(rowCells[colIndex])

    // Графа 11
    colIndex++
    newRow.postcode = pure(rowCells[colIndex])

    // Графа 12 - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
    colIndex++
    newRow.region = getRecordIdImport(4L, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 13
    colIndex++
    newRow.district = pure(rowCells[colIndex])

    // Графа 14
    colIndex++
    newRow.city = pure(rowCells[colIndex])

    // Графа 15
    colIndex++
    newRow.locality = pure(rowCells[colIndex])

    // Графа 16
    colIndex++
    newRow.street = pure(rowCells[colIndex])

    // Графа 17
    colIndex++
    newRow.house = pure(rowCells[colIndex])

    // Графа 18
    colIndex++
    newRow.housing = pure(rowCells[colIndex])

    // Графа 19
    colIndex++
    newRow.apartment = pure(rowCells[colIndex])

    // Графа 20 - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
    colIndex++
    newRow.country = getRecordIdImport(10L, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 21
    colIndex++
    newRow.address = pure(rowCells[colIndex])

    // Графа 22
    colIndex++
    newRow.taxRate = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 23
    colIndex++
    newRow.income = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 24
    colIndex++
    newRow.deduction = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 25
    colIndex++
    newRow.taxBase = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 26
    colIndex++
    newRow.calculated = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 27
    colIndex++
    newRow.withheld = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 28
    colIndex++
    newRow.listed = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 29
    colIndex++
    newRow.withheldAgent = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 30
    colIndex++
    newRow.nonWithheldAgent = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 31 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
    colIndex++
    newRow.col_040_1 = getRecordIdImport(370, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 32
    colIndex++
    newRow.col_041_1 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 33 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_1_1 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 34
    colIndex++
    newRow.col_043_1_1 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 35 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_1_2 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 36
    colIndex++
    newRow.col_043_1_2 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 37 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_1_3 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 38
    colIndex++
    newRow.col_043_1_3 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 39 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_1_4 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 40
    colIndex++
    newRow.col_043_1_4 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 41 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_1_5 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 42
    colIndex++
    newRow.col_043_1_5 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 43 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
    colIndex++
    newRow.col_040_2 = getRecordIdImport(370, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 44
    colIndex++
    newRow.col_041_2 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 45 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_2_1 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 46
    colIndex++
    newRow.col_043_2_1 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 47 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_2_2 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 48
    colIndex++
    newRow.col_043_2_2 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 49 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_2_3 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 50
    colIndex++
    newRow.col_043_2_3 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 51 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_2_4 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 52
    colIndex++
    newRow.col_043_2_4 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 53 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_2_5 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 54
    colIndex++
    newRow.col_043_2_5 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 55 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
    colIndex++
    newRow.col_040_3 = getRecordIdImport(370, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 56
    colIndex++
    newRow.col_041_3 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 57 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_3_1 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 58
    colIndex++
    newRow.col_043_3_1 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 59 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_3_2 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 60
    colIndex++
    newRow.col_043_3_2 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 61 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_3_3 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 62
    colIndex++
    newRow.col_043_3_3 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 63 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_3_4 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 64
    colIndex++
    newRow.col_043_3_4 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 65 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_3_5 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 66
    colIndex++
    newRow.col_043_3_5 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 67 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_051_3_1 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 68
    colIndex++
    newRow.col_052_3_1 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 69 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_051_3_2 = getRecordIdImport(350, 'CODE', pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, false)

    // Графа 70
    colIndex++
    newRow.col_052_3_2 = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, required)

    return newRow
}

static String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}