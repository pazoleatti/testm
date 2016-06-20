package form_template.income.app2.v2014

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Сведения о доходах физического лица, выплаченных ему налоговым агентом, от операций с ценными бумагами, операций с
 * финансовыми инструментами срочных сделок, а также при осуществлении выплат по ценным бумагам российских эмитентов
 * formTemplateId=1415
 *
 * TODO:
 *      - неясно какой код формы
 *      - неясно какой верхний колонтитул
 *      - консолидация: в чтз не описано как консолидировать второй источник
 *
 * @author SYasinskiy
 */

// графа 1  - refNum
// графа 2  - innRF
// графа 3  - inn
// графа 4  - surname
// графа 5  - name
// графа 6  - patronymic
// графа 7  - status
// графа 8  - birthday
// графа 9  - citizenship       - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
// графа 10 - code              - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
// графа 11 - series
// графа 12 - postcode
// графа 13 - region            - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
// графа 14 - district
// графа 15 - city
// графа 16 - locality
// графа 17 - street
// графа 18 - house
// графа 19 - housing
// графа 20 - apartment
// графа 21 - country           - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
// графа 22 - address
// графа 23 - taxRate
// графа 24 - income
// графа 25 - deduction
// графа 26 - taxBase
// графа 27 - calculated
// графа 28 - withheld
// графа 29 - listed
// графа 30 - withheldAgent
// графа 31 - nonWithheldAgent

// графа 32 - col_040_1         - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
// графа 33 - col_041_1
// графа 34 - col_042_1_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 35 - col_043_1_1
// графа 36 - col_042_1_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 37 - col_043_1_2
// графа 38 - col_042_1_3       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 39 - col_043_1_3
// графа 40 - col_042_1_4       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 41 - col_043_1_4
// графа 42 - col_042_1_5       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 43 - col_043_1_5

// графа 44 - col_040_2         - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
// графа 45 - col_041_2
// графа 46 - col_042_2_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 47 - col_043_2_1
// графа 48 - col_042_2_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 49 - col_043_2_2
// графа 50 - col_042_2_3       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 51 - col_043_2_3
// графа 52 - col_042_2_4       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 53 - col_043_2_4
// графа 54 - col_042_2_5       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 55 - col_043_2_5

// графа 56 - col_040_3         - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
// графа 57 - col_041_3
// графа 58 - col_042_3_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 59 - col_043_3_1
// графа 60 - col_042_3_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 61 - col_043_3_2
// графа 62 - col_042_3_3       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 63 - col_043_3_3
// графа 64 - col_042_3_4       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 65 - col_043_3_4
// графа 66 - col_042_3_5       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 67 - col_043_3_5
// графа 68 - col_051_3_1       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 69 - col_052_3_1
// графа 70 - col_051_3_2       - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
// графа 71 - col_052_3_2

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
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
        calc()
        logicCheck()
        formDataService.saveCachedDataRows(formData, logger)
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
        formDataService.saveCachedDataRows(formData, logger)
        break
    case FormDataEvent.IMPORT:
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
def allColumns = ['refNum', 'innRF', 'inn', 'surname', 'name', 'patronymic', 'status', 'birthday', 'citizenship', 'code', 'series', 'postcode', 'region', 'district', 'city', 'locality', 'street', 'house', 'housing', 'apartment','country', 'address', 'taxRate', 'income', 'deduction', 'taxBase', 'calculated', 'withheld', 'listed', 'withheldAgent', 'nonWithheldAgent', 'col_040_1', 'col_041_1', 'col_042_1_1', 'col_043_1_1', 'col_042_1_2','col_043_1_2', 'col_042_1_3', 'col_043_1_3', 'col_042_1_4', 'col_043_1_4', 'col_042_1_5', 'col_043_1_5', 'col_040_2', 'col_041_2', 'col_042_2_1', 'col_043_2_1', 'col_042_2_2', 'col_043_2_2', 'col_042_2_3','col_043_2_3', 'col_042_2_4', 'col_043_2_4', 'col_042_2_5', 'col_043_2_5', 'col_040_3', 'col_041_3', 'col_042_3_1', 'col_043_3_1', 'col_042_3_2', 'col_043_3_2', 'col_042_3_3', 'col_043_3_3', 'col_042_3_4','col_043_3_4', 'col_042_3_5', 'col_043_3_5', 'col_051_3_1', 'col_052_3_1', 'col_051_3_2', 'col_052_3_2']

// Автозаполняемые атрибуты (графа 1, 24..26)
@Field
def autoFillColumns = ['refNum', 'income', 'deduction', 'taxBase']

// Редактируемые атрибуты (графа 1..23, 27..71)
@Field
def editableColumns = allColumns - autoFillColumns

// Проверяемые на пустые значения атрибуты (графа 1, 4, 5, 7-11, 23, 24, 26, 27)
@Field
def nonEmptyColumns = [/*'refNum',*/ 'surname', 'name', 'status', 'birthday', 'citizenship', 'code', 'series', 'taxRate', 'income', 'taxBase', 'calculated']

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
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    // адрес (графа 12, 14..20)
    def address = ['postcode', 'district', 'city', 'locality', 'street', 'house', 'housing', 'apartment']

    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    boolean wasError = false

    for (row in dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "
        def citizenshipCode = getRefBookValue(10L, row.citizenship)?.CODE?.value

        // 1. Проверка на заполнение поля «<Наименование поля>»
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // 2. Проверка на заполнение графы 13 «Регион (код)»
        if ((address.find { row.getCell(it).value != null }
                || (row.country == null && row.address == null)
                || ('1'.equals(row.status))
                || ('643'.equals(citizenshipCode)))
                && row.region == null) {
            rowError(logger, row, errorMsg + String.format("Графа «%s» не заполнена! " +
                    "Данная графа обязательна для заполнения, если выполняется хотя бы одно из следующих условий: " +
                    "1. Заполнена хотя бы одна из граф по адресу места жительства в РФ (графы 12, 14-20). " +
                    "2. Не заполнены графы по адресу места жительства за пределами РФ (графы 21 и 22). " +
                    "3. Графа «%s» равна значению «1» и/или графа «%s» равна значению «643».",
                    getColumnName(row, "region"), getColumnName(row, "status"), getColumnName(row, "citizenship")))
        }

        // 3. Проверка вводимых символов в поле «Серия и номер документа»
        def code = (row.code != null ? getRefBookValue(360, row.code)?.CODE?.value : null)
        if (code == '21' && row.series != null && !row.series?.matches("^[а-яА-ЯёЁa-zA-Z0-9]+\$")) {
            def name = getColumnName(row, 'series')
            rowError(logger, row, errorMsg + "Графа «$name» содержит недопустимые символы!")
        }

        // 4. Проверка заполнения поля «Номер дома (владения)»
        if (row.house && !row.house?.matches("^[а-яА-ЯёЁa-zA-Z0-9/-]+\$")) {
            def name = getColumnName(row, 'house')
            rowWarning(logger, row, errorMsg + "Графа «$name» содержит недопустимые символы!")
        }

        // 5. Проверка заполнения полей 12, 14..20
        if (row.region != null) {
            checkNonEmptyColumns(row, row.getIndex(), address, logger, false)
        }

        // 7. Проверка правильности заполнения графы «ИНН в стране гражданства»
        if (row.inn && row.citizenship && citizenshipCode == '643') {
            def nameInn = getColumnName(row, 'inn')
            def nameCitizenship = getColumnName(row, 'citizenship')
            rowError(logger, row, errorMsg + "Графа «$nameInn» не должно быть заполнено, если графа «$nameCitizenship» равна «643»")
        }

        // 8. Проверка правильности заполнения графы «Статус налогоплательщика»
        if (row.status && !row.status?.matches("^[1-3]+\$")) {
            def name = getColumnName(row, 'status')
            rowError(logger, row, errorMsg + "Графа «$name» содержит недопустимое значение! Поле может содержать только одно из значений: «1», «2», «3»")
        }

        // 9. Проверка соответствия суммы дохода суммам вычета
        def String errorMsg1 = errorMsg + "Сумма граф «Сумма вычета» для кода дохода = «%s» превышает значение поля «Сумма дохода» для данного кода."
        if (getSum1(row) > roundValue(row.col_041_1 ?: 0)) {
            def value = getRefBookValue(370L, row.col_040_1)?.CODE?.value
            rowWarning(logger, row, String.format(errorMsg1, value))
        }
        if (getSum2(row) > roundValue(row.col_041_2 ?: 0)) {
            def value = getRefBookValue(370L, row.col_040_2)?.CODE?.value
            rowWarning(logger, row, String.format(errorMsg1, value))
        }
        if (getSum3(row) > roundValue(row.col_041_3 ?: 0)) {
            def value = getRefBookValue(370L, row.col_040_3)?.CODE?.value
            rowWarning(logger, row, String.format(errorMsg1, value))
        }

        // 10. Арифметические проверки расчета граф 24, 25, 26
        needValue['income'] = calc24(row)
        needValue['deduction'] = calc25(row)
        needValue['taxBase'] = calc26(row)
        def arithmeticCheckAlias = needValue.keySet().asList()
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // 11, 12. Проверка на соответствие паттерну
        if (row.innRF && checkPattern(logger, row, 'innRF', row.innRF, INN_IND_PATTERN, wasError ? null : INN_IND_MEANING, true)) {
            checkControlSumInn(logger, row, 'innRF', row.innRF, true)
        } else if (row.innRF) {
            wasError = true
        }

        // 13. Проверка заполнения графы 21, 22
        if ((citizenshipCode != '643') && ((address + "region").find { row[it] } == null)) {
            ["country", "address"].each { alias ->
                if (!row[alias]) {
                    rowError(logger, row, errorMsg + String.format("Графа «%s» не заполнена! " +
                            "Данная графа обязательна для заполнения, если графа «%s» не равна значению «643» и графы по адресу места жительства в РФ (графы 12-20) не заполнены.",
                            getColumnName(row, alias), getColumnName(row, 'citizenship')))
                }
            }
        }
    }
}

// Алгоритмы заполнения полей формы
void calc() {
    def dataRows = formDataService.getDataRowHelper(formData).allCached
    if (dataRows.isEmpty()) {
        return
    }

    def forColumn25 = getSumString([35, 37, 39, 41, 43, 47, 49, 51, 53, 55, 59, 61, 63, 65, 67, 69, 71])

    for (DataRow<Cell> row in dataRows) {
        // проверки, выполняемые до расчёта
        def int index = row.getIndex()

        // графа 24
        def BigDecimal value24 = calc24(row)
        checkOverflow(value24, row, 'income', index, 15, '«Графа 33» + «Графа 45» + «Графа 57»')
        row.income = value24

        // графа 25
        def BigDecimal value25 = calc25(row)
        checkOverflow(value25, row, 'deduction', index, 15, forColumn25)
        row.deduction = value25

        // графа 26
        def BigDecimal value26 = calc26(row)
        checkOverflow(value26, row, 'taxBase', index, 15, '«Графа 24» - «Графа 25»')
        row.taxBase = value26
    }
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

def BigDecimal calc24(def row) {
    return getSum(row, ['col_041_1', 'col_041_2', 'col_041_3'])
}

def BigDecimal calc25(def row) {
    // графа 68, 70
    def tmp = getSum(row, ['col_052_3_1', 'col_052_3_2'])
    return getSum1(row) + getSum2(row) + getSum3(row) + tmp
}

def BigDecimal calc26(def row) {
    return roundValue((row.income ?: 0) - (row.deduction ?: 0))
}

/** Получить сумму «Графа 35» + «Графа 37» + «Графа 39» + «Графа 41» + «Графа 43». */
def BigDecimal getSum1(def row) {
    return getSum(row, ['col_043_1_1', 'col_043_1_2', 'col_043_1_3', 'col_043_1_4', 'col_043_1_5'])
}

/** Получить сумму «Графа 47» + «Графа 49» + « Графа 51» + «Графа 53» + «Графа 55». */
def BigDecimal getSum2(def row) {
    return getSum(row, ['col_043_2_1', 'col_043_2_2', 'col_043_2_3', 'col_043_2_4', 'col_043_2_5'])
}

/** Получить сумму «Графа 59» + «Графа 61» + «Графа 63» + «Графа 65» + «Графа 67». */
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

def getNewRow() {
    def newRow = (formDataEvent in [FormDataEvent.IMPORT, FormDataEvent.IMPORT_TRANSPORT_FILE]) ? formData.createStoreMessagingDataRow() : formData.createDataRow()


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
void sortFormDataRows(def saveInDB = true) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    if (saveInDB) {
        dataRowHelper.saveSort()
    } else {
        updateIndexes(dataRows);
    }
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
    def rows = []

    def source1FormTypeId = 418
    def source2FormTypeIds = [419, 10070, 314]

    // 2..31, 32..71
    def consilidationColumns = allColumns - 'refNum'

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == source1FormTypeId) {
            // Сведения о доходах физического лица, выплаченных ему налоговым агентом ... (ЦФО НДФЛ)
            def source = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, null, formData.comparativePeriodId, formData.accruing)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(source).allSaved
                sourceDataRows.each { row ->
                    def newRow = getNewRow()
                    consilidationColumns.each { column ->
                        newRow[column] = row[column]
                    }
                    rows.add(newRow)
                }
            }
        } else if (source2FormTypeIds.contains(it.formTypeId)) {
            // Расчет налога на прибыль организаций с доходов, удерживаемого налоговым агентом (источником выплаты доходов)
            // TODO (Ramil Timerbaev) в чтз не описано как консолидировать второй источник
        }
    }

    rows.eachWithIndex { row, index ->
        row.refNum = index + 1
    }
    updateIndexes(rows)
    formDataService.getDataRowHelper(formData).allCached = rows
}

void importData() {
    def tmpRow = formData.createDataRow()
    int COLUMN_COUNT = 71
    int HEADER_ROW_COUNT = 2
    String TABLE_START_VALUE = tmpRow.getCell('refNum').column.name
    String TABLE_END_VALUE = null

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
    checkHeaderSize(headerRows, colCount, rowCount)

    def headerMapping = [[:]]
    def index = 0
    allColumns.each { alias ->
        headerMapping.add(([(headerRows[0][index]): getColumnName(tmpRow, alias)]))
        headerMapping.add(([(headerRows[1][index]): (index + 1).toString()]))
        index++
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
    def newRow = getNewRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    def required = true

    // Графа 1
    def colIndex = 0
    // newRow.refNum = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 2
    colIndex++
    newRow.innRF = values[colIndex]

    // Графа 3
    colIndex++
    newRow.inn = values[colIndex]

    // Графа 4
    colIndex++
    newRow.surname = values[colIndex]

    // Графа 5
    colIndex++
    newRow.name = values[colIndex]

    // Графа 6
    colIndex++
    newRow.patronymic = values[colIndex]

    // Графа 7
    colIndex++
    newRow.status = values[colIndex]

    // Графа 8
    colIndex++
    newRow.birthday = parseDate(values[colIndex], "dd.MM.yyyy", fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 9 - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
    colIndex++
    newRow.citizenship = getRecordIdImport(10L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 10 - атрибут 3601 - CODE - «Код», справочник 360 «Коды документов»
    colIndex++
    newRow.code = getRecordIdImport(360L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 11
    colIndex++
    newRow.series = values[colIndex]

    // Графа 12
    colIndex++
    newRow.postcode = values[colIndex]

    // Графа 13 - атрибут 9 - CODE - «Код», справочник 4 «Коды субъектов Российской Федерации»
    colIndex++
    newRow.region = getRecordIdImport(4L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 14
    colIndex++
    newRow.district = values[colIndex]

    // Графа 15
    colIndex++
    newRow.city = values[colIndex]

    // Графа 16
    colIndex++
    newRow.locality = values[colIndex]

    // Графа 17
    colIndex++
    newRow.street = values[colIndex]

    // Графа 18
    colIndex++
    newRow.house = values[colIndex]

    // Графа 19
    colIndex++
    newRow.housing = values[colIndex]

    // Графа 20
    colIndex++
    newRow.apartment = values[colIndex]

    // Графа 21 - атрибут 50 - CODE - «Код», справочник 10 «Общероссийский классификатор стран мира»
    colIndex++
    newRow.country = getRecordIdImport(10L, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 22
    colIndex++
    newRow.address = values[colIndex]

    // Графа 23
    colIndex++
    newRow.taxRate = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 24
    colIndex++
    // newRow.income = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 25
    colIndex++
    // newRow.deduction = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 26
    colIndex++
    // newRow.taxBase = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 27
    colIndex++
    newRow.calculated = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 28
    colIndex++
    newRow.withheld = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 29
    colIndex++
    newRow.listed = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 30
    colIndex++
    newRow.withheldAgent = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 31
    colIndex++
    newRow.nonWithheldAgent = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 32 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
    colIndex++
    newRow.col_040_1 = getRecordIdImport(370, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 33
    colIndex++
    newRow.col_041_1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 34 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_1_1 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 35
    colIndex++
    newRow.col_043_1_1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 36 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_1_2 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 37
    colIndex++
    newRow.col_043_1_2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 38 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_1_3 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 39
    colIndex++
    newRow.col_043_1_3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 40 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_1_4 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 41
    colIndex++
    newRow.col_043_1_4 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 42 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_1_5 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 43
    colIndex++
    newRow.col_043_1_5 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 44 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
    colIndex++
    newRow.col_040_2 = getRecordIdImport(370, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 45
    colIndex++
    newRow.col_041_2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 46 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_2_1 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 47
    colIndex++
    newRow.col_043_2_1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 48 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_2_2 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 49
    colIndex++
    newRow.col_043_2_2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 50 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_2_3 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 51
    colIndex++
    newRow.col_043_2_3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 52 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_2_4 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 53
    colIndex++
    newRow.col_043_2_4 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 54 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_2_5 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 55
    colIndex++
    newRow.col_043_2_5 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 56 - атрибут 3701 - CODE - «Код», справочник 370 «Коды доходов»
    colIndex++
    newRow.col_040_3 = getRecordIdImport(370, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 57
    colIndex++
    newRow.col_041_3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 58 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_3_1 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 59
    colIndex++
    newRow.col_043_3_1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 60 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_3_2 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 61
    colIndex++
    newRow.col_043_3_2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 62 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_3_3 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 63
    colIndex++
    newRow.col_043_3_3 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 64 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_3_4 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 65
    colIndex++
    newRow.col_043_3_4 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 66 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_042_3_5 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 67
    colIndex++
    newRow.col_043_3_5 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 68 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_051_3_1 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 69
    colIndex++
    newRow.col_052_3_1 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    // Графа 70 - атрибут 3501 - CODE - «Код», справочник 350 «Коды вычетов»
    colIndex++
    newRow.col_051_3_2 = getRecordIdImport(350, 'CODE', values[colIndex], fileRowIndex, colIndex + colOffset, false)

    // Графа 71
    colIndex++
    newRow.col_052_3_2 = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, required)

    return newRow
}