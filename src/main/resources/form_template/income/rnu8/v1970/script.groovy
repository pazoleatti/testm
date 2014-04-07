package form_template.income.rnu8.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * (РНУ-8) Простой регистр налогового учёта «Требования»
 * formTemplateId=320
 *
 * графа 1  - number
 * графа 2  - code
 * графа 3  - balance
 * графа 4  - name
 * графа 5  - income
 * графа 6  - outcome
 *
 * @author Stanislav Yasinskiy
 */
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
        importData()
        calc()
        logicCheck()
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
def editableColumns = ['balance', 'income', 'outcome']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['number', 'balance', 'income', 'outcome']

// Сумируемые колонки в фиксированной с троке
@Field
def totalColumns = ['income', 'outcome']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number', 'code', 'name']

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                def Date date, boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value, date, rowIndex,
            cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    if (!dataRows.isEmpty()) {

        // Удаление подитогов
        deleteAllAliased(dataRows)

        // сортируем по кодам
        dataRowHelper.save(dataRows.sort { getKnu(it.balance) })

        // номер последний строки предыдущей формы
        def number = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')

        for (row in dataRows) {
            row.number = ++number
        }

        // посчитать "итого по коду"
        def totalRows = [:]
        def tmp = null
        def sum = 0, sum2 = 0
        dataRows.eachWithIndex { row, i ->
            if (tmp == null) {
                tmp = row.balance
            }
            // если код расходы поменялся то создать новую строку "итого по коду"
            if (tmp != row.balance) {
                totalRows.put(i, getNewRow(getKnu(tmp), sum, sum2))
                sum = 0
                sum2 = 0
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
            if (i == dataRows.size() - 1) {
                sum += (row.income ?: 0)
                sum2 += (row.outcome ?: 0)
                def totalRowCode = getNewRow(getKnu(row.balance), sum, sum2)
                totalRows.put(i + 1, totalRowCode)
                sum = 0
                sum2 = 0
            }
            sum += (row.income ?: 0)
            sum2 += (row.outcome ?: 0)
            tmp = row.balance
        }

        // добавить "итого по коду" в таблицу
        def i = 1
        totalRows.each { index, row ->
            dataRowHelper.insert(row, index + i++)
        }
    }

    dataRowHelper.insert(calcTotalRow(dataRows), dataRows.size() + 1)
    dataRowHelper.save(dataRows)
}

def calcTotalRow(def dataRows) {
    def totalRow = getTotalRow('total', 'Итого')
    calcTotalSum(dataRows, totalRow, totalColumns)

    return totalRow
}

// Получить новую строку подитога
def getNewRow(def alias, def sum, def sum2) {
    def newRow = getTotalRow('total' + alias, 'Итого по КНУ ' + alias)
    newRow.income = sum
    newRow.outcome = sum2
    return newRow
}

def getTotalRow(def alias, def title) {
    def newRow = formData.createDataRow()
    newRow.setAlias(alias)
    newRow.fix = title
    newRow.getCell('fix').colSpan = 4
    ['number', 'fix', 'income', 'outcome'].each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    return newRow
}

// Логические проверки
void logicCheck() {
    def dataRows = formDataService.getDataRowHelper(formData).getAllCached()
    if (dataRows.isEmpty()) {
        return
    }
    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    //две карты: одна с реальными значениями итого по кодам, а вторая - с рассчитанными
    def totalRows = [:]
    def sum1 = [:]
    //две карты: одна с реальными значениями итого по кодам, а вторая - с рассчитанными
    def totalRows2 = [:]
    def sum2 = [:]

    for (def row : dataRows) {
        if (row.getAlias() ==~ /total\d+/) { // если подитог
            totalRows[row.getAlias().replace('total', '')] = row.income
            totalRows2[row.getAlias().replace('total', '')] = row.outcome
            continue
        }
        if (row.getAlias() != null) {
            continue
        }
        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на уникальность поля «№ пп»
        if (++i != row.number) {
            logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
        }

        // 3. Арифметическая проверка итоговых значений строк «Итого по КНУ»
        def code = getKnu(row.balance)
        if (sum1[code] != null) {
            sum1[code] += row.income ?: 0
        } else {
            sum1[code] = row.income ?: 0
        }
        if (sum2[code] != null) {
            sum2[code] += row.outcome ?: 0
        } else {
            sum2[code] = row.outcome ?: 0
        }
    }

    // 3. Арифметическая проверка итоговых значений строк «Итого по КНУ»
    totalRows.each { key, val ->
        if (totalRows.get(key) != sum1.get(key)) {
            def msg = formData.createDataRow().getCell('income').column.name
            logger.error("Неверное итоговое значение по коду '$key' графы «$msg»!")
        }
    }
    totalRows2.each { key, val ->
        if (totalRows2.get(key) != sum2.get(key)) {
            def msg = formData.createDataRow().getCell('outcome').column.name
            logger.error("Неверное итоговое значение по коду '$key' графы «$msg»!")
        }
    }

    // 4. Арифметическая проверка итогового значения по всем строкам
    checkTotalSum(dataRows, totalColumns, logger, true)
}

def String getKnu(def code) {
    return getRefBookValue(28, code)?.CODE?.stringValue
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = []

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).allCached.each { sRow ->
                    if (sRow.getAlias() == null || sRow.getAlias() == '') {
                        def isFind = false
                        for (def row : dataRows) {
                            if (sRow.balance == row.balance) {
                                isFind = true
                                totalColumns.each { alias ->
                                    def tmp = (row.getCell(alias).value ?: 0) + (sRow.getCell(alias).value ?: 0)
                                    row.getCell(alias).setValue(tmp, null)
                                }
                                break
                            }
                        }
                        if (!isFind) {
                            dataRows.add(sRow)
                        }
                    }
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info('Формирование консолидированной формы прошло успешно.')
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Код налогового учета',
            (xml.row[0].cell[3]): 'Балансовый счёт',
            (xml.row[0].cell[5]): 'Входящий остаток',
            (xml.row[0].cell[6]): 'Исходящий остаток',
            (xml.row[1].cell[3]): 'Номер',
            (xml.row[1].cell[4]): 'Наименование счёта',
            (xml.row[2].cell[0]): '1'
    ]
    (2..6).each { index ->
        headerMapping.put((xml.row[2].cell[index]), index.toString())
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

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != "") {
            continue
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 2
        // TODO Зависимая http://jira.aplana.com/browse/SBRFACCTAX-6587

        // графа 3
        newRow.balance = getRecordIdImport(28, 'NUMBER', row.cell[3].text(), xlsIndexRow, 3 + colOffset)

        // графа 4
        // TODO Зависимая http://jira.aplana.com/browse/SBRFACCTAX-6587

        // графа 5
        newRow.income = parseNumber(row.cell[5].text(), xlsIndexRow, 5 + colOffset, logger, false)

        // графа 6
        newRow.outcome = parseNumber(row.cell[6].text(), xlsIndexRow, 6 + colOffset, logger, false)
        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}