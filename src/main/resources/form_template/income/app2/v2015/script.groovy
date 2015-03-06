package form_template.income.app2.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с
 * финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов
 * formTemplateId=1415
 *
 * TODO:
 *      - консолидация
 *      - убрать лишнее
 *
 * @author SYasinskiy
 */

// графа 1  - refNum
// графа 2  - date
// графа 3  - type
// графа 4  - innRF
// графа 5  - inn
// графа 6  - surname
// графа 7  - name
// графа 8  - patronymic
// графа 9  - status
// графа 10 - birthday
// графа 11 - citizenship       - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
// графа 12 - code              - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
// графа 13 - series
// графа 14 - postcode
// графа 15 - region            - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
// графа 16 - district
// графа 17 - city
// графа 18 - locality
// графа 19 - street
// графа 20 - house
// графа 21 - housing
// графа 22 - apartment
// графа 23 - country           - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
// графа 24 - address
// графа 25 - taxRate
// графа 26 - income
// графа 27 - deduction
// графа 28 - taxBase
// графа 29 - calculated
// графа 30 - withheld
// графа 31 - listed
// графа 32 - withheldAgent
// графа 33 - nonWithheldAgent
// графа 34 - decoding

// графа 35 - col_040_1         - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
// графа 36 - col_041_1
// графа 37 - col_042_1_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 38 - col_043_1_1
// графа 39 - col_042_1_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 40 - col_043_1_2
// графа 41 - col_042_1_3       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 42 - col_043_1_3
// графа 43 - col_042_1_4       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 44 - col_043_1_4
// графа 45 - col_042_1_5       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 46 - col_043_1_5

// графа 47 - col_040_2         - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
// графа 48 - col_041_2
// графа 49 - col_042_2_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 50 - col_043_2_1
// графа 51 - col_042_2_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 52 - col_043_2_2
// графа 53 - col_042_2_3       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 54 - col_043_2_3
// графа 55 - col_042_2_4       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 56 - col_043_2_4
// графа 57 - col_042_2_5       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 58 - col_043_2_5

// графа 59 - col_040_3         - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
// графа 60 - col_041_3
// графа 61 - col_042_3_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 62 - col_043_3_1
// графа 63 - col_042_3_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 64 - col_043_3_2
// графа 65 - col_042_3_3       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 66 - col_043_3_3
// графа 67 - col_042_3_4       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 68 - col_043_3_4
// графа 69 - col_042_3_5       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 70 - col_043_3_5
// графа 71 - col_051_3_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 72 - col_052_3_1
// графа 73 - col_051_3_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 74 - col_052_3_2

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
def allColumns = ['refNum', 'date', 'type', 'innRF', 'inn', 'surname', 'name', 'patronymic', 'status', 'birthday', 'citizenship', 'code', 'series', 'postcode', 'region', 'district', 'city', 'locality', 'street', 'house', 'housing', 'apartment','country', 'address', 'taxRate', 'income', 'deduction', 'taxBase', 'calculated', 'withheld', 'listed', 'withheldAgent', 'nonWithheldAgent', 'decoding', 'col_040_1', 'col_041_1', 'col_042_1_1', 'col_043_1_1', 'col_042_1_2','col_043_1_2', 'col_042_1_3', 'col_043_1_3', 'col_042_1_4', 'col_043_1_4', 'col_042_1_5', 'col_043_1_5', 'col_040_2', 'col_041_2', 'col_042_2_1', 'col_043_2_1', 'col_042_2_2', 'col_043_2_2', 'col_042_2_3','col_043_2_3', 'col_042_2_4', 'col_043_2_4', 'col_042_2_5', 'col_043_2_5', 'col_040_3', 'col_041_3', 'col_042_3_1', 'col_043_3_1', 'col_042_3_2', 'col_043_3_2', 'col_042_3_3', 'col_043_3_3', 'col_042_3_4','col_043_3_4', 'col_042_3_5', 'col_043_3_5', 'col_051_3_1', 'col_052_3_1', 'col_051_3_2', 'col_052_3_2']

// Автозаполняемые атрибуты (графа 26..28)
@Field
def autoFillColumns = ['income', 'deduction', 'taxBase']

// Редактируемые атрибуты (графа 1..25, 29..74)
@Field
def editableColumns = allColumns - autoFillColumns

// Проверяемые на пустые значения атрибуты (графа 1, 2, 6, 7, 9, 25, 26, 28, 29)
@Field
def nonEmptyColumns = ['refNum', 'date', 'surname', 'name', 'status', 'taxRate', 'income', 'taxBase', 'calculated']

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

    // адрес (графа 14, 16..22)
    def address = ['postcode', 'district', 'city', 'locality', 'street', 'house', 'housing', 'apartment']

    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1-10. Проверка на заполнение поля «<Наименование поля>»
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 11. Проверка на заполнение поля «<Наименование поля>»
        if (address.find { row.getCell(it).value != null }) {
            checkNonEmptyColumns(row, index, ['region'], logger, true)
        }

        // 12. Проверка вводимых символов в поле «Серия и номер документа»
        if (row.series != null && !row.series?.matches("^[а-яА-ЯёЁa-zA-Z0-9]+\$")) {
            def name = getColumnName(row, 'series')
            logger.error(errorMsg + "Графа «$name» содержит недопустимые символы!")
        }

        // 13. Проверка заполнения поля «Номер дома (владения)»
        if (row.house && !row.house?.matches("^[а-яА-ЯёЁa-zA-Z0-9/ ]+\$")) {
            def name = getColumnName(row, 'house')
            logger.error(errorMsg + "Графа «$name» содержит недопустимые символы!")
        }

        // 14-21. Проверка заполнения полей 14, 16, 17, 18, 19, 20, 21, 22
        if (row.region != null) {
            checkNonEmptyColumns(row, row.getIndex(), address, logger, true)
        }

        // 22. Проверка заполнения графы «Регион (код)»
        if (row.country == null && row.address == null && row.region == null) {
            def name = getColumnName(row, 'region')
            logger.error(errorMsg + "Графа «$name» не заполнена!")
        }

        // 23. Проверка правильности заполнения графы «ИНН в стране гражданства»
        if (row.inn && row.citizenship && getRefBookValue(10L, row.citizenship)?.CODE?.value == '643') {
            def nameInn = getColumnName(row, 'inn')
            def nameCitizenship = getColumnName(row, 'citizenship')
            logger.error(errorMsg + "Графа «$nameInn» не должно быть заполнено, если графа «$nameCitizenship» равна «643»")
        }

        // 24. Проверка правильности заполнения графы «Статус налогоплательщика»
        if (row.status && !row.status?.matches("^[1-3]+\$")) {
            def name = getColumnName(row, 'status')
            logger.error(errorMsg + "Графа «$name» содержит недопустимое значение! Поле может содержать только одно из значений: «1», «2», «3»")
        }

        // 25-27. Проверка соответствия суммы дохода суммам вычета
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

        // 27. Арифметические проверки расчета граф 26, 27, 28
        needValue['income'] = calc26(row)
        needValue['deduction'] = calc27(row)
        needValue['taxBase'] = calc28(row)
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

    def forColumn27 = getSumString([38, 40, 42, 44, 46, 50, 52, 54, 56, 58, 62, 64, 66, 68, 70, 72, 74])

    for (DataRow<Cell> row in dataRows) {
        // проверки, выполняемые до расчёта
        def int index = row.getIndex()

        // графа 26
        def BigDecimal value26 = calc26(row)
        checkOverflow(value26, row, 'income', index, 15, '«Графа 36» + «Графа 48» + «Графа 60»')
        row.income = value26

        // графа 27
        def BigDecimal value27 = calc27(row)
        checkOverflow(value27, row, 'deduction', index, 15, forColumn27)
        row.deduction = value27

        // графа 28
        def BigDecimal value28 = calc28(row)
        checkOverflow(value28, row, 'taxBase', index, 15, '«Графа 26» - «Графа 27»')
        row.taxBase = value28
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

def BigDecimal calc26(def row) {
    return getSum(row, ['col_041_1', 'col_041_2', 'col_041_3'])
}

def BigDecimal calc27(def row) {
    return getSum1(row) + getSum2(row) + getSum3(row)
}

def BigDecimal calc28(def row) {
    return roundValue((row.income ?: 0) + (row.deduction ?: 0))
}

/** Получить сумму «Графа 38» + «Графа 40» + «Графа 42» + «Графа 44» + «Графа 46». */
def BigDecimal getSum1(def row) {
    return getSum(row, ['col_043_1_1', 'col_043_1_2', 'col_043_1_3', 'col_043_1_4', 'col_043_1_5'])
}

/** Получить сумму «Графа 50» + «Графа 52» + « Графа 54» + «Графа 56» + «Графа 58». */
def BigDecimal getSum2(def row) {
    return getSum(row, ['col_043_2_1', 'col_043_2_2', 'col_043_2_3', 'col_043_2_4', 'col_043_2_5'])
}

/** Получить сумму «Графа 62» + «Графа 64» + «Графа 66» + «Графа 68» + «Графа 70» + «Графа 72» + «Графа 74». */
def BigDecimal getSum3(def row) {
    return getSum(row, ['col_043_3_1', 'col_043_3_2', 'col_043_3_3', 'col_043_3_4', 'col_043_3_5', 'col_052_3_1', 'col_052_3_2'])
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
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'refNum'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 74, 1)

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
        newRow.refNum = row.cell[xmlIndexCol].text()

        // Графа 2
        xmlIndexCol++
        newRow.date = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 3
        xmlIndexCol++
        newRow.type = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 4
        xmlIndexCol++
        newRow.innRF = row.cell[xmlIndexCol].text()

        // Графа 5
        xmlIndexCol++
        newRow.inn = row.cell[xmlIndexCol].text()

        // Графа 6
        xmlIndexCol++
        newRow.surname = row.cell[xmlIndexCol].text()

        // Графа 7
        xmlIndexCol++
        newRow.name = row.cell[xmlIndexCol].text()

        // Графа 8
        xmlIndexCol++
        newRow.patronymic = row.cell[xmlIndexCol].text()

        // Графа 9
        xmlIndexCol++
        newRow.status = row.cell[xmlIndexCol].text()

        // Графа 10
        xmlIndexCol++
        newRow.birthday = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 11 - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
        xmlIndexCol++
        newRow.citizenship = getRecordIdImport(10L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 12 - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
        xmlIndexCol++
        newRow.code = getRecordIdImport(360L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 13
        xmlIndexCol++
        newRow.series = row.cell[xmlIndexCol].text()

        // Графа 14
        xmlIndexCol++
        newRow.postcode = row.cell[xmlIndexCol].text()

        // Графа 15 - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
        xmlIndexCol++
        newRow.region = getRecordIdImport(4L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 16
        xmlIndexCol++
        newRow.district = row.cell[xmlIndexCol].text()

        // Графа 17
        xmlIndexCol++
        newRow.city = row.cell[xmlIndexCol].text()

        // Графа 18
        xmlIndexCol++
        newRow.locality = row.cell[xmlIndexCol].text()

        // Графа 19
        xmlIndexCol++
        newRow.street = row.cell[xmlIndexCol].text()

        // Графа 20
        xmlIndexCol++
        newRow.house = row.cell[xmlIndexCol].text()

        // Графа 21
        xmlIndexCol++
        newRow.housing = row.cell[xmlIndexCol].text()

        // Графа 22
        xmlIndexCol++
        newRow.apartment = row.cell[xmlIndexCol].text()

        // Графа 23 - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
        xmlIndexCol++
        newRow.country = getRecordIdImport(10L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 24
        xmlIndexCol++
        newRow.address = row.cell[xmlIndexCol].text()

        // Графа 25
        xmlIndexCol++
        newRow.taxRate = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 26
        xmlIndexCol++
        // newRow.income = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 27
        xmlIndexCol++
        // newRow.deduction = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 28
        xmlIndexCol++
        // newRow.taxBase = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 29
        xmlIndexCol++
        newRow.calculated = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 30
        xmlIndexCol++
        newRow.withheld = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 31
        xmlIndexCol++
        newRow.listed = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 32
        xmlIndexCol++
        newRow.withheldAgent = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 33
        xmlIndexCol++
        newRow.nonWithheldAgent = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 34
        xmlIndexCol++
        newRow.decoding = row.cell[xmlIndexCol].text()

        // Графа 35 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
        xmlIndexCol++
        newRow.col_040_1 = getRecordIdImport(370, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 36
        xmlIndexCol++
        newRow.col_041_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 37 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_1 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 38
        xmlIndexCol++
        newRow.col_043_1_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 39 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_2 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 40
        xmlIndexCol++
        newRow.col_043_1_2 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 41 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_3 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 42
        xmlIndexCol++
        newRow.col_043_1_3 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 43 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_4 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 44
        xmlIndexCol++
        newRow.col_043_1_4 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 45 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_5 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 46
        xmlIndexCol++
        newRow.col_043_1_5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 47 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
        xmlIndexCol++
        newRow.col_040_2 = getRecordIdImport(370, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 48
        xmlIndexCol++
        newRow.col_041_2 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 49 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_1 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 50
        xmlIndexCol++
        newRow.col_043_2_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 51 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_2 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 52
        xmlIndexCol++
        newRow.col_043_2_2 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 53 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_3 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 54
        xmlIndexCol++
        newRow.col_043_2_3 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 55 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_4 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 56
        xmlIndexCol++
        newRow.col_043_2_4 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 57 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_5 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 58
        xmlIndexCol++
        newRow.col_043_2_5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 59 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
        xmlIndexCol++
        newRow.col_040_3 = getRecordIdImport(370, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 60
        xmlIndexCol++
        newRow.col_041_3 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 61 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_1 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 62
        xmlIndexCol++
        newRow.col_043_3_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 63 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_2 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 64
        xmlIndexCol++
        newRow.col_043_3_2 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 65 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_3 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 66
        xmlIndexCol++
        newRow.col_043_3_3 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 67 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_4 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 68
        xmlIndexCol++
        newRow.col_043_3_4 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 69 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_5 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 70
        xmlIndexCol++
        newRow.col_043_3_5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 71 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_051_3_1 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 72
        xmlIndexCol++
        newRow.col_052_3_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 73 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_051_3_2 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 74
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