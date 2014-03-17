package form_template.income.rnu64.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException

/**
 * РНУ-64 "Регистр налогового учёта затрат, связанных с проведением сделок РЕПО"
 * formTemplateId=355
 *
 * @author auldanov
 * @author bkinzyabulatov
 *
 * 1. number - № пп
 * 2. date - Дата сделки
 * 3. part - Часть сделки Справочник
 * 4. dealingNumber - Номер сделки
 * -5. bondKind - Вид ценных бумаг //графу удалили
 * 5. costs - Затраты (руб.коп.)
 */

@Field
def isConsolidated
isConsolidated = formData.kind == FormDataKind.CONSOLIDATED

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
        break
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow?.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
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
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
    case FormDataEvent.MIGRATION:
        importData()
        calc()
        logicCheck()
        break
}

@Field
def refBookCache = [:]

@Field
def recordCache = [:]

@Field
def providerCache = [:]

@Field
def isBalancePeriod

// все атрибуты
@Field
def allColumns = ['number', 'fix', 'date', 'part', 'dealingNumber', 'costs']

// Редактируемые атрибуты
@Field
def editableColumns = ['date', 'part', 'dealingNumber', 'costs']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number']

@Field
def sortColumns = ['date', 'dealingNumber']

@Field
def totalColumns = ['costs']

@Field
def nonEmptyColumns = ['number', 'date', 'part', 'dealingNumber', 'costs']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol + 1, logger, true)
}

/** Получить дату по строковому представлению (формата дд.ММ.гггг) */
def getDate(def value, def indexRow, def indexCol) {
    return parseDate(value, 'dd.MM.yyyy', indexRow, indexCol + 1, logger, true)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    if (formDataEvent != FormDataEvent.IMPORT && formDataEvent != FormDataEvent.MIGRATION) {
        sortRows(dataRows, sortColumns)
    }

    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    for (def row in dataRows) {
        if (row.getAlias() == null) {
            row.number = ++i
        }
    }

    // пересчитываем строки итого
    calcTotalSum(dataRows, getDataRow(dataRows, 'totalQuarter'), totalColumns)

    if (formData.kind == FormDataKind.PRIMARY) {
        // строка Итого за текущий отчетный (налоговый) период
        def total = getDataRow(dataRows, 'total')
        def dataRowsPrev = getDataRowsPrev()
        total.costs = getTotalValue(dataRows, dataRowsPrev)
    }

    dataRowHelper.update(dataRows)
}

def getDataRowsPrev() {
    if (!isBalancePeriod() && formData.kind == FormDataKind.PRIMARY) {
        def formDataPrev = formDataService.getFormDataPrev(formData, formData.departmentId)
        formDataPrev = (formDataPrev?.state == WorkflowState.ACCEPTED ? formDataPrev : null)
        if (formDataPrev != null) {
            return formDataService.getDataRowHelper(formDataPrev)?.allCached
        }
    }
    return null
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalRow = null
    def totalQuarterRow = null
    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId)?.time
    def dTo = getEndDate()
    def i = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    for (def row : dataRows) {
        // 1. Проверка на заполнение поля
        if (row.getAlias() == null) {

            def index = row.getIndex()
            def errorMsg = "Строка $index: "

            checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

            // 2. Проверка даты совершения операции и границ отчетного периода
            if (row.date != null && dFrom != null && dTo != null && !(row.date >= dFrom && row.date <= dTo)) {
                loggerError(errorMsg + "Дата совершения операции вне границ отчетного периода!")
            }

            // 3. Проверка на уникальность поля «№ пп»
            if (++i != row.number) {
                loggerError(errorMsg + 'Нарушена уникальность номера по порядку!')
            }

            // 4. Проверка на нулевые значения
            if (row.costs == 0) {
                loggerError(errorMsg + "Все суммы по операции нулевые!")
            }
        } else if (row.getAlias() == 'total') {
            totalRow = row
        } else if (row.getAlias() == 'totalQuarter') {
            totalQuarterRow = row
        }
    }
    def testRows = dataRows.findAll { it -> it.getAlias() == null }
    // проверка на наличие итоговых строк, иначе будет ошибка
    if (totalQuarterRow == null || totalRow == null) {
        // 5. Проверка итоговых значений за текущий квартал
        def testRow = formData.createDataRow()
        calcTotalSum(testRows, testRow, totalColumns)
        if (totalQuarterRow == null || totalQuarterRow != null && totalQuarterRow.costs != testRow.costs) {
            loggerError('Итоговые значения за текущий квартал рассчитаны неверно!')
        }
        // 6. Проверка итоговых значений за текущий отчётный (налоговый) период
        if (!isConsolidated) {
            def dataRowsPrev = getDataRowsPrev()
            if (totalRow == null || totalRow != null && totalRow.costs != getTotalValue(dataRows, dataRowsPrev)) {
                loggerError('Итоговые значения за текущий отчётный (налоговый ) период рассчитаны неверно!')
            }
        }
    }
}

// Функция возвращает итоговые значения за текущий отчётный (налоговый) период
def getTotalValue(def dataRows, def dataRowsPrev) {
    def quarterRow = getDataRow(dataRows, 'totalQuarter')
    def prevQuarterTotalRow
    if (dataRowsPrev != null) {
        prevQuarterTotalRow = getDataRow(dataRowsPrev, "total")
    }
    if (prevQuarterTotalRow != null) {
        return (quarterRow.costs ?: 0) + prevQuarterTotalRow.costs ?: 0
    } else {
        return quarterRow.costs ?: 0
    }
}

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            reportPeriodEndDate, rowIndex, colIndex, logger, required)
}

// Получение xml с общими проверками
def getXML(def String startStr, def String endStr) {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        throw new ServiceException('Имя файла не должно быть пустым')
    }
    def is = ImportInputStream
    if (is == null) {
        throw new ServiceException('Поток данных пуст')
    }
    if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xlsm')) {
        throw new ServiceException('Выбранный файл не соответствует формату xlsx/xlsm!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значения после обработки потока данных')
    }
    return xml
}

/** Получение импортируемых данных. */
void importData() {
    def xml = getXML('№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Дата сделки',
            (xml.row[0].cell[3]): 'Часть сделки',
            (xml.row[0].cell[4]): 'Номер сделки',
            (xml.row[0].cell[5]): 'Вид ценных бумаг',
            (xml.row[0].cell[6]): 'Затраты (руб.коп.)',
            (xml.row[1].cell[0]): '1',
            (xml.row[1].cell[2]): '2',
            (xml.row[1].cell[3]): '3',
            (xml.row[1].cell[4]): '4',
            (xml.row[1].cell[5]): '5',
            (xml.row[1].cell[6]): '6'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 1)
}


// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = 10 // Смещение для индекса колонок в ошибках импорта
    def int colOffset = 1 // Смещение для индекса колонок в ошибках импорта

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
        if (row.cell[0].text() == null || row.cell[0].text() == '') {
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

        // графа 1 - № пп
        newRow.number = parseNumber(row.cell[0].text(), xlsIndexRow, 0 + colOffset, logger, false)

        // графа 2 - Дата сделки
        newRow.date = parseDate(row.cell[2].text(), "dd.MM.yyyy", xlsIndexRow, 2 + colOffset, logger, false)

        // графа 3 - Часть сделки
        newRow.part =  getRecordIdImport(60, 'CODE', row.cell[3].text(), xlsIndexRow, 3 + colOffset)

        // графа 4 - Номер сделки
        newRow.dealingNumber = row.cell[4].text()

        // графа 5 - Вид ценных бумаг
        newRow.bondKind = row.cell[5].text()

        // графа 6 - Затраты (руб.коп.)
        newRow.costs = parseNumber(row.cell[6].text(), xlsIndexRow, 6 + colOffset, logger, false)

        rows.add(newRow)
    }

    // Добавляем итоговые строки
    def existRows = dataRowHelper.allSaved
    rows.add(getDataRow(existRows, 'totalQuarter'))
    rows.add(getDataRow(existRows, 'total'))
    dataRowHelper.save(rows)
}

/** Для получения данных из RNU или XML */
String getCellValue(def row, int index, def type, boolean isTextXml = false) {
    if (type == 1) {
        if (isTextXml) {
            return row.field[index].text()
        } else {
            return row.field[index].@value.text()
        }
    }
    return row.cell[index + 1].text()
}

/**
 * Рассчитать, проверить и сравнить итоги.
 * @param totalRow итоговая строка из транспортного файла
 */
void checkTotalRow(def totalRow) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def totalColumns = [6: 'costs']

    def totalCalc = getCalcTotalRow(dataRows)
    def errorColumns = []
    if (totalCalc != null) {
        totalColumns.each { index, columnAlias ->
            if (totalRow[columnAlias] != null && totalCalc[columnAlias] != totalRow[columnAlias]) {
                errorColumns.add(index)
            }
        }
    }
    if (!errorColumns.isEmpty()) {
        def columns = errorColumns.join(', ')
        logger.error("Итоговая сумма в графе $columns в транспортном файле некорректна")
    }
}

/** Получить итоговую строку с суммами. */
def getCalcTotalRow(def dataRows) {
    def totalRow = formData.createDataRow()
    totalRow.getCell("fix").setColSpan(4)
    totalRow.fix = "Итоги"
    totalRow.setAlias("total")
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    def from = 0
    def to = dataRows.size() - 1
    def sum
    if (from > to) {
        sum = 0
    } else {
        sum = summ(formData, dataRows, new ColumnRange('costs', from, to))
    }
    totalRow.costs = sum
    return totalRow
}

def loggerError(def msg) {
    if (isBalancePeriod()) {
        logger.warn(msg)
    } else {
        logger.error(msg)
    }
}

// Признак периода ввода остатков. Отчетный период является периодом ввода остатков.
def isBalancePeriod() {
    if (isBalancePeriod == null) {
        isBalancePeriod = reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)
    }
    return isBalancePeriod
}

def getEndDate() {
    if (reportPeriodEndDate == null) {
        reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId)?.time
    }
    return reportPeriodEndDate
}

void consolidation() {
    def rows = []
    def sum = 0
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                formDataService.getDataRowHelper(source).getAllCached().each { row ->
                    if (row.getAlias() == null) {
                        rows.add(row)
                    } else if (row.getAlias() == 'total' && row.costs != null) {
                        sum += row.costs
                    }
                }
            }
        }
    }
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def totalRow = dataRowHelper.getDataRow(dataRows, 'total')
    totalRow.costs = sum

    rows.add(getDataRow(dataRows, 'totalQuarter'))
    rows.add(totalRow)

    formDataService.getDataRowHelper(formData).save(rows)
}

/** Если не период ввода остатков, то должна быть форма с данными за предыдущий отчетный период. */
void prevPeriodCheck() {
    if (!isBalancePeriod() && !isConsolidated && !formDataService.existAcceptedFormDataPrev(formData, formDataDepartment.id)) {
        def formName = formData.formType.name
        // http://jira.aplana.com/browse/SBRFACCTAX-6015
        //throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
        logger.warn("Не найдены экземпляры «$formName» за прошлый отчетный период!")
    }
}