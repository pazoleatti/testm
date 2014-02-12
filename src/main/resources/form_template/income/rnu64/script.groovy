package form_template.income.rnu64

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

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
 *
 * !!! bondKind выпилена из РНУ, а из файла для импорта/миграции нет
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
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
        }
        break
}

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

@Field
def sortColumns = ['date', 'dealingNumber']

@Field
def totalColumns = ['costs']

@Field
def nonEmptyColumns = ['number', 'date', 'part', 'dealingNumber', 'costs']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['number']

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
        def total =  getDataRow(dataRows, 'total')
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
        def dataRowsPrev = getDataRowsPrev()
        if (totalRow == null || totalRow != null && totalRow.costs != getTotalValue(dataRows, dataRowsPrev)) {
            loggerError('Итоговые значения за текущий отчётный (налоговый ) период рассчитаны неверно!')
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
        return (quarterRow.costs ?: 0) + (prevQuarterTotalRow.costs ?: 0)
    } else {
        return quarterRow.costs ?: 0
    }
}

/** Получение импортируемых данных. */
void importData() {
    def fileName = (UploadFileName ? UploadFileName.toLowerCase() : null)
    if (fileName == null || fileName == '') {
        logger.error('Имя файла не должно быть пустым')
        return
    }
    String charset = ""
    if (formDataEvent == FormDataEvent.MIGRATION) {
        if (!fileName.contains('.xml')) {
            logger.error('Формат файла должен быть *.xml')
            return
        }
    } else if (formDataEvent == FormDataEvent.IMPORT){
        if (!fileName.contains('.r')) {
            logger.error('Формат файла должен быть *.rnu')
            return
        }
        charset = 'cp866'
    }
    def is = ImportInputStream
    if (is == null) {
        logger.error('Поток данных пуст')
        return
    }
    def xmlString = importService.getData(is, fileName, charset)
    if (xmlString == null || xmlString == '') {
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
        def totalLoad = addData(xml, fileName)
        // рассчитать, проверить и сравнить итоги
        if (formDataEvent == FormDataEvent.IMPORT) {
            if (totalLoad != null) {
                checkTotalRow(totalLoad)
            } else {
                logger.error("Нет итоговой строки.")
            }
        }
    } catch (Exception e) {
        logger.error('Во время загрузки данных произошла ошибка! ' + e.message)
    }
}

/** Заполнить форму данными. */
def addData(def xml, def fileName) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def newRows = []

    def records
    def totalRecords
    def type
    if (formDataEvent == FormDataEvent.MIGRATION ||
            formDataEvent == FormDataEvent.IMPORT && fileName.contains('.xml')) {
        records = xml.exemplar.table.detail.record
        totalRecords = xml.exemplar.table.total.record
        type = 1 // XML
    } else {
        records = xml.row
        totalRecords = xml.rowTotal
        type = 2 // RNU
    }

    def indexRow = 0
    for (def row : records) {
        indexRow++
        def indexCol = 0
        def newRow = formData.createDataRow()
        newRow.setIndex(indexRow)

        // графа 2..5
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        // графа 1
        newRow.number = getNumber(getCellValue(row, indexCol, type), indexRow, indexCol)
        indexCol++

        // графа 2
        newRow.date = getDate(getCellValue(row, indexCol, type), indexRow, indexCol)
        indexCol++

        // графа 3 - справочник 60 "Части сделок"
        tmp = null
        if (getCellValue(row, indexCol, type, true) != null && getCellValue(row, indexCol, type).trim() != '') {
            tmp = formDataService.getRefBookRecordIdImport(60, recordCache, providerCache, 'CODE',
                    getCellValue(row, indexCol, type), getEndDate(), indexRow, indexCol, logger, false)
        }
        newRow.part = tmp
        indexCol++

        // графа 4
        newRow.dealingNumber = getCellValue(row, indexCol, type, true)
        indexCol++

        // графа 5
        // !!! bondKind выпилена из РНУ, а из файла для импорта/миграции нет
        indexCol++

        // графа 6
        newRow.costs = getNumber(getCellValue(row, indexCol, type), indexRow, indexCol)
        newRows.add(newRow)
    }
    def totalRow = null

    // итоговая строка
    if (totalRecords.size() >= 1) {
        def row = totalRecords[0]
        totalRow = getCleanTotalRow(true)
        // графа 5
        totalRow.costs = getNumber(getCellValue(row, 6, type), indexRow, 6)
        newRows.add(totalRow)
        newRows.add(getCleanTotalRow(false))
    }
    dataRowHelper.save(newRows)
    return totalRow
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
    def totalRow = getCleanTotalRow(true)
    def from = 0
    def to = dataRows.size() - 2
    def BigDecimal sum
    if (from > to) {
        sum = 0
    } else {
        sum = summ(formData, dataRows, new ColumnRange('costs', from, to))
    }
    totalRow.costs = sum?.setScale(2, BigDecimal.ROUND_HALF_UP)
    return totalRow
}

def getCleanTotalRow(boolean isQuarter) {
    def totalRow = formData.createDataRow()
    totalRow.getCell("fix").setColSpan(4)
    totalRow.fix = isQuarter ? "Итого за текущий квартал" : "Итого за текущий отчетный (налоговый) период"
    totalRow.setAlias(isQuarter ? "totalQuarter" : "total")
    allColumns.each {
        totalRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
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
                    } else if (row.getAlias() == 'total' && row.costs!=null) {
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
        throw new ServiceException("Не найдены экземпляры «$formName» за прошлый отчетный период!")
    }
}