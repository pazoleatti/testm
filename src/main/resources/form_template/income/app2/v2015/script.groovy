package form_template.income.app2.v2015

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с
 * финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов
 * formTemplateId=1415
 *
 * TODO:
 *      - консолидация: в чтз не описано как консолидировать второй источник
 *
 * @author SYasinskiy
 */

// графа 1  - refNum
// графа 2  - date
// графа 3  - innRF
// графа 4  - inn
// графа 5  - surname
// графа 6  - name
// графа 7  - patronymic
// графа 8  - status
// графа 9  - birthday
// графа 10 - citizenship       - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
// графа 11 - code              - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
// графа 12 - series
// графа 13 - postcode
// графа 14 - region            - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
// графа 15 - district
// графа 16 - city
// графа 17 - locality
// графа 18 - street
// графа 19 - house
// графа 20 - housing
// графа 21 - apartment
// графа 22 - country           - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
// графа 23 - address
// графа 24 - taxRate
// графа 25 - income
// графа 26 - deduction
// графа 27 - taxBase
// графа 28 - calculated
// графа 29 - withheld
// графа 30 - listed
// графа 31 - withheldAgent
// графа 32 - nonWithheldAgent

// графа 33 - col_040_1         - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
// графа 34 - col_041_1
// графа 35 - col_042_1_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 36 - col_043_1_1
// графа 37 - col_042_1_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 38 - col_043_1_2
// графа 39 - col_042_1_3       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 40 - col_043_1_3
// графа 41 - col_042_1_4       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 42 - col_043_1_4
// графа 43 - col_042_1_5       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 44 - col_043_1_5

// графа 45 - col_040_2         - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
// графа 46 - col_041_2
// графа 47 - col_042_2_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 48 - col_043_2_1
// графа 49 - col_042_2_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 50 - col_043_2_2
// графа 51 - col_042_2_3       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 52 - col_043_2_3
// графа 53 - col_042_2_4       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 54 - col_043_2_4
// графа 55 - col_042_2_5       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 56 - col_043_2_5

// графа 57 - col_040_3         - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
// графа 58 - col_041_3
// графа 59 - col_042_3_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 60 - col_043_3_1
// графа 61 - col_042_3_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 62 - col_043_3_2
// графа 63 - col_042_3_3       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 64 - col_043_3_3
// графа 65 - col_042_3_4       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 66 - col_043_3_4
// графа 67 - col_042_3_5       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 68 - col_043_3_5
// графа 69 - col_051_3_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 70 - col_052_3_1
// графа 71 - col_051_3_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 72 - col_052_3_2

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
    case FormDataEvent.COMPOSE:
        consolidation()
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
def allColumns = ['refNum', 'date', 'innRF', 'inn', 'surname', 'name', 'patronymic', 'status', 'birthday', 'citizenship', 'code', 'series', 'postcode', 'region', 'district', 'city', 'locality', 'street', 'house', 'housing', 'apartment','country', 'address', 'taxRate', 'income', 'deduction', 'taxBase', 'calculated', 'withheld', 'listed', 'withheldAgent', 'nonWithheldAgent', 'col_040_1', 'col_041_1', 'col_042_1_1', 'col_043_1_1', 'col_042_1_2','col_043_1_2', 'col_042_1_3', 'col_043_1_3', 'col_042_1_4', 'col_043_1_4', 'col_042_1_5', 'col_043_1_5', 'col_040_2', 'col_041_2', 'col_042_2_1', 'col_043_2_1', 'col_042_2_2', 'col_043_2_2', 'col_042_2_3','col_043_2_3', 'col_042_2_4', 'col_043_2_4', 'col_042_2_5', 'col_043_2_5', 'col_040_3', 'col_041_3', 'col_042_3_1', 'col_043_3_1', 'col_042_3_2', 'col_043_3_2', 'col_042_3_3', 'col_043_3_3', 'col_042_3_4','col_043_3_4', 'col_042_3_5', 'col_043_3_5', 'col_051_3_1', 'col_052_3_1', 'col_051_3_2', 'col_052_3_2']

// Автозаполняемые атрибуты (графа 1, 25..27)
@Field
def autoFillColumns = ['refNum', 'income', 'deduction', 'taxBase']

// Редактируемые атрибуты (графа 1..24, 28..72)
@Field
def editableColumns = allColumns - autoFillColumns

// Проверяемые на пустые значения атрибуты (графа 1, 2, 5, 6, 8, 24, 25, 27, 28)
@Field
def nonEmptyColumns = [/*'refNum',*/ 'date', 'surname', 'name', 'status', 'taxRate', 'income', 'taxBase', 'calculated']

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
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
                      def boolean required = true) {
    if (value == null || value.trim().isEmpty()) {
        return null
    }
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    if (dataRows.isEmpty()) {
        return
    }

    // адрес (графа 13, 15..21)
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

        // 14-21. Проверка заполнения полей 13, 15, 16, 17, 18, 19, 20, 21
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

        // 27. Арифметические проверки расчета граф 25, 26, 27
        needValue['income'] = calc25(row)
        needValue['deduction'] = calc26(row)
        needValue['taxBase'] = calc27(row)
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

    def forColumn26 = getSumString([36, 38, 40, 42, 44, 48, 50, 52, 54, 56, 60, 62, 64, 66, 68, 70, 72])

    for (DataRow<Cell> row in dataRows) {
        // проверки, выполняемые до расчёта
        def int index = row.getIndex()

        // графа 25
        def BigDecimal value25 = calc25(row)
        checkOverflow(value25, row, 'income', index, 15, '«Графа 34» + «Графа 46» + «Графа 58»')
        row.income = value25

        // графа 26
        def BigDecimal value26 = calc26(row)
        checkOverflow(value26, row, 'deduction', index, 15, forColumn26)
        row.deduction = value26

        // графа 27
        def BigDecimal value27 = calc27(row)
        checkOverflow(value27, row, 'taxBase', index, 15, '«Графа 25» - «Графа 26»')
        row.taxBase = value27
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

def BigDecimal calc25(def row) {
    return getSum(row, ['col_041_1', 'col_041_2', 'col_041_3'])
}

def BigDecimal calc26(def row) {
    return getSum1(row) + getSum2(row) + getSum3(row)
}

def BigDecimal calc27(def row) {
    return roundValue((row.income ?: 0) + (row.deduction ?: 0))
}

/** Получить сумму «Графа 36» + «Графа 38» + «Графа 40» + «Графа 42» + «Графа 44». */
def BigDecimal getSum1(def row) {
    return getSum(row, ['col_043_1_1', 'col_043_1_2', 'col_043_1_3', 'col_043_1_4', 'col_043_1_5'])
}

/** Получить сумму «Графа 48» + «Графа 50» + « Графа 52» + «Графа 54» + «Графа 56». */
def BigDecimal getSum2(def row) {
    return getSum(row, ['col_043_2_1', 'col_043_2_2', 'col_043_2_3', 'col_043_2_4', 'col_043_2_5'])
}

/** Получить сумму «Графа 60» + «Графа 62» + «Графа 64» + «Графа 66» + «Графа 68» + «Графа 70» + «Графа 72». */
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

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 72, 1)

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
        // newRow.refNum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 2
        xmlIndexCol++
        newRow.date = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 3
        xmlIndexCol++
        newRow.innRF = row.cell[xmlIndexCol].text()

        // Графа 4
        xmlIndexCol++
        newRow.inn = row.cell[xmlIndexCol].text()

        // Графа 5
        xmlIndexCol++
        newRow.surname = row.cell[xmlIndexCol].text()

        // Графа 6
        xmlIndexCol++
        newRow.name = row.cell[xmlIndexCol].text()

        // Графа 7
        xmlIndexCol++
        newRow.patronymic = row.cell[xmlIndexCol].text()

        // Графа 8
        xmlIndexCol++
        newRow.status = row.cell[xmlIndexCol].text()

        // Графа 9
        xmlIndexCol++
        newRow.birthday = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 10 - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
        xmlIndexCol++
        newRow.citizenship = getRecordIdImport(10L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 11 - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
        xmlIndexCol++
        newRow.code = getRecordIdImport(360L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 12
        xmlIndexCol++
        newRow.series = row.cell[xmlIndexCol].text()

        // Графа 13
        xmlIndexCol++
        newRow.postcode = row.cell[xmlIndexCol].text()

        // Графа 14 - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
        xmlIndexCol++
        newRow.region = getRecordIdImport(4L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 15
        xmlIndexCol++
        newRow.district = row.cell[xmlIndexCol].text()

        // Графа 16
        xmlIndexCol++
        newRow.city = row.cell[xmlIndexCol].text()

        // Графа 17
        xmlIndexCol++
        newRow.locality = row.cell[xmlIndexCol].text()

        // Графа 18
        xmlIndexCol++
        newRow.street = row.cell[xmlIndexCol].text()

        // Графа 19
        xmlIndexCol++
        newRow.house = row.cell[xmlIndexCol].text()

        // Графа 20
        xmlIndexCol++
        newRow.housing = row.cell[xmlIndexCol].text()

        // Графа 21
        xmlIndexCol++
        newRow.apartment = row.cell[xmlIndexCol].text()

        // Графа 22 - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
        xmlIndexCol++
        newRow.country = getRecordIdImport(10L, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 23
        xmlIndexCol++
        newRow.address = row.cell[xmlIndexCol].text()

        // Графа 24
        xmlIndexCol++
        newRow.taxRate = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 25
        xmlIndexCol++
        // newRow.income = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 26
        xmlIndexCol++
        // newRow.deduction = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 27
        xmlIndexCol++
        // newRow.taxBase = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 28
        xmlIndexCol++
        newRow.calculated = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 29
        xmlIndexCol++
        newRow.withheld = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 30
        xmlIndexCol++
        newRow.listed = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 31
        xmlIndexCol++
        newRow.withheldAgent = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 32
        xmlIndexCol++
        newRow.nonWithheldAgent = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 33 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
        xmlIndexCol++
        newRow.col_040_1 = getRecordIdImport(370, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 34
        xmlIndexCol++
        newRow.col_041_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 35 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_1 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 36
        xmlIndexCol++
        newRow.col_043_1_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 37 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_2 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 38
        xmlIndexCol++
        newRow.col_043_1_2 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 39 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_3 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 40
        xmlIndexCol++
        newRow.col_043_1_3 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 41 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_4 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 42
        xmlIndexCol++
        newRow.col_043_1_4 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 43 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_1_5 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 44
        xmlIndexCol++
        newRow.col_043_1_5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 45 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
        xmlIndexCol++
        newRow.col_040_2 = getRecordIdImport(370, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 46
        xmlIndexCol++
        newRow.col_041_2 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 47 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_1 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 48
        xmlIndexCol++
        newRow.col_043_2_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 49 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_2 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 50
        xmlIndexCol++
        newRow.col_043_2_2 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 51 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_3 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 52
        xmlIndexCol++
        newRow.col_043_2_3 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 53 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_4 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 54
        xmlIndexCol++
        newRow.col_043_2_4 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 55 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_2_5 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 56
        xmlIndexCol++
        newRow.col_043_2_5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 57 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
        xmlIndexCol++
        newRow.col_040_3 = getRecordIdImport(370, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 58
        xmlIndexCol++
        newRow.col_041_3 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 59 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_1 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 60
        xmlIndexCol++
        newRow.col_043_3_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 61 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_2 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 62
        xmlIndexCol++
        newRow.col_043_3_2 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 63 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_3 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 64
        xmlIndexCol++
        newRow.col_043_3_3 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 65 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_4 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 66
        xmlIndexCol++
        newRow.col_043_3_4 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 67 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_042_3_5 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 68
        xmlIndexCol++
        newRow.col_043_3_5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 69 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_051_3_1 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 71
        xmlIndexCol++
        newRow.col_052_3_1 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, required)

        // Графа 71 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
        xmlIndexCol++
        newRow.col_051_3_2 = getRecordIdImport(350, 'CODE', row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, false)

        // Графа 72
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

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    def source1FormTypeId = 418
    def source2FormTypeId = 419

    // 2..32, 33..72
    def consilidationColumns = allColumns - ['refNum', 'date']
    def currentDate = new Date()

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == source1FormTypeId) {
            // Сведения о доходах физического лица, выплаченных ему налоговым агентом ... (ЦФО НДФЛ)
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, null)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allCached
                sourceDataRows.each { row ->
                    def newRow = getNewRow()
                    newRow.date = currentDate
                    consilidationColumns.each { column ->
                        newRow[column] = row[column]
                    }
                    dataRows.add(newRow)
                }
            }
        } else if (it.formTypeId == source2FormTypeId) {
            // Расчет налога на прибыль организаций с доходов, удерживаемого налоговым агентом (источником выплаты доходов)
            // TODO (Ramil Timerbaev) в чтз не описано как консолидировать второй источник
            // TODO (Ramil Timerbaev) эта форма в 0.3.9.1, еще не смержили в 0.5.1,
        }
    }
    dataRowHelper.save(dataRows)
}