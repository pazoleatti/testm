package form_template.transport.vehicles.v1970

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Сведения о транспортных средствах, по которым уплачивается транспортный налог
 * formTemplateId=201
 *
 * @author ivildanov
 * @author Stanislav Yasinskiy
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        // TODO пока нет возможности добавлять строки при создании: http://jira.aplana.com/browse/SBRFACCTAX-5219
        // copyData()
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
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicCheck()
        break
}

// 1 № пп  -  rowNumber
// 2 Код ОКТМО  -  codeOKATO
// 3 Муниципальное образование, на территории которого зарегистрировано транспортное средство (ТС)  -  regionName
// 4 Код вида ТС  -  tsTypeCode
// 5 Вид ТС  -  tsType
// 6 Идентификационный номер  -  identNumber
// 7 Марка  -  model
// 8 Экологический класс  -  ecoClass
// 9 Регистрационный знак  -  regNumber
// 10 Мощность (величина)  -  powerVal
// 11 Мощность (ед. измерения)  -  baseUnit
// 12 Год изготовления  -  year
// 13 Регистрация (дата регистрации)  -  regDate
// 14 Регистрация (дата снятия с регистрации)  -  regDateEnd
// 15 Сведения об угоне (дата начала розыска ТС)  -  stealDateStart
// 16 Сведения об угоне (дата возврата ТС)  -  stealDateEnd

//// Кэши и константы
@Field
def refBookCache = [:]

// Редактируемые атрибуты
@Field
def copyColumns = ['codeOKATO', 'tsTypeCode', 'identNumber', 'model', 'ecoClass', 'regNumber',
        'powerVal', 'baseUnit', 'year', 'regDate', 'regDateEnd', 'stealDateStart', 'stealDateEnd']

@Field
def editableColumns = ['codeOKATO', 'tsTypeCode', 'identNumber', 'model', 'ecoClass', 'regNumber', 'powerVal',
        'baseUnit', 'year', 'regDate', 'regDateEnd', 'stealDateStart', 'stealDateEnd']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'codeOKATO', 'tsTypeCode', 'identNumber', 'model',
        'regNumber', 'powerVal', 'baseUnit', 'year', 'regDate']

//// Обертки методов

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
        sort()
        def i = 1
        for (def row in dataRows) {
            row.rowNumber = i++
        }
        dataRowHelper.update(dataRows);
    }
}

// сортировка ОКТМО - Муниципальное образование - Код вида ТС
void sort() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.getAllCached().sort { a, b ->
        def valA = getRefBookValue(96, a.codeOKATO)?.CODE?.stringValue
        def valB = getRefBookValue(96, b.codeOKATO)?.CODE?.stringValue
        def val = (valA != null && valB != null) ? valA.compareTo(valB) : 0
        if (val == 0) {
            valA = getRefBookValue(42, a.tsTypeCode)?.CODE?.stringValue
            valB = getRefBookValue(42, b.tsTypeCode)?.CODE?.stringValue
            val = (valA != null && valB != null) ? valA.compareTo(valB) : 0
        }
        return val
    }
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def String dFormat = "dd.MM.yyyy"

    // Проверенные строки (4-ая провека)
    def List<DataRow<Cell>> checkedRows = new ArrayList<DataRow<Cell>>()
    for (def row in dataRows) {

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 2. Проверка на соответствие дат при постановке (снятии) с учёта
        if (!(row.regDateEnd == null || row.regDateEnd.compareTo(row.regDate) > 0)) {
            logger.error(errorMsg + 'Дата постановки (снятия) с учёта неверная!')
        }

        // 3. Проврека на наличие даты угона при указании даты возврата
        if (row.stealDateEnd != null && row.stealDateStart == null) {
            logger.error(errorMsg + 'Не заполнено поле «Дата угона»!')
        }

        // 4. Проверка на наличие в списке ТС строк, для которых графы codeOKATO, identNumber, regNumber одинаковы
        if (!checkedRows.contains(row)) {
            def errorRows = ''
            for (def rowIn in dataRows) {
                if (!checkedRows.contains(rowIn) && row != rowIn && row.codeOKATO.equals(rowIn.codeOKATO)
                        && row.identNumber.equals(rowIn.identNumber) && row.regNumber.equals(rowIn.regNumber)) {
                    checkedRows.add(rowIn)
                    errorRows = ', ' + rowIn.getIndex()
                }
            }
            if (!''.equals(errorRows)) {
                logger.error("Обнаружены строки $index$errorRows, у которых Код ОКТМО = ${getRefBookValue(96, row.codeOKATO).CODE.stringValue}, " +
                        "Идентификационный номер = $row.identNumber, " +
                        "Регистрационный знак = $row.regNumber совпадают!")
            }
        }
        checkedRows.add(row)

        // 5. Проверка на наличие в списке ТС строк, период владения которых не пересекается с отчётным
        if (row.regDate != null && row.regDateEnd != null && (row.regDate > dTo || row.regDateEnd < dFrom)) {
            logger.error(errorMsg + 'Период регистрации ТС ('
                    + row.regDate.format(dFormat) + ' - ' + row.regDateEnd.format(dFormat) + ') ' +
                    ' не пересекается с периодом (' + dFrom.format(dFormat) + " - " + dTo.format(dFormat) +
                    '), за который сформирована налоговая форма!')
        }
    }

    // 6. Проверка наличия формы предыдущего периода
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    def str = ''
    if (reportPeriod.order == 4) {
        str += checkPrevPeriod(prevReportPeriod)
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
        str += checkPrevPeriod(prevReportPeriod)
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
        str += checkPrevPeriod(prevReportPeriod)
    } else {
        str = checkPrevPeriod(prevReportPeriod)
    }
    if (str.length() > 2) {
        logger.warn("Данные ТС из предыдущих отчётных периодов не были скопированы. В Системе " +
                "не создавались формы за следующие периоды: " + str.substring(0, str.size() - 2) + ".")
    }
}

def String checkPrevPeriod(def reportPeriod) {
    if (reportPeriod != null) {
        if (formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriod.id) == null) {
            return reportPeriod.name + " " + reportPeriod.taxPeriod.year + ", "
        }
    }
    return ''
}

// Алгоритм копирования данных из форм предыдущего периода при создании формы
def copyData() {
    def rows = new LinkedList<DataRow<Cell>>()
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    if (reportPeriod.order == 4) {
        rows += getPrevRowsForCopy(prevReportPeriod)
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
        rows += getPrevRowsForCopy(prevReportPeriod)
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
        rows += getPrevRowsForCopy(prevReportPeriod)
    } else {
        rows += getPrevRowsForCopy(prevReportPeriod)
    }

    if (rows.size() > 0) {
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        dataRowHelper.save(rows)
    }
}

def copyRow(def row) {
    def newRow = formData.createDataRow()
    editableColumns.each { alias ->
        newRow.getCell(alias).editable = true
        newRow.getCell(alias).setStyleAlias("Редактируемая")
    }
    copyColumns.each { alias ->
        newRow.getCell(alias).setValue(row.getCell(alias).value, null)
    }
    return newRow
}

//Получить строки для копирования за предыдущий отчетный период
def getPrevRowsForCopy(def reportPeriod) {
    def rows = new LinkedList<DataRow<Cell>>()
    if (reportPeriod != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriod.id)
        def dataRowsOld = (formDataOld != null ? formDataService.getDataRowHelper(formDataOld)?.allCached : null)
        if (dataRowsOld != null && !dataRowsOld.isEmpty()) {
            def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
            def dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time
            for (def row in dataRowsOld) {
                if (row.regDate == null || row.regDateEnd == null || row.regDate > dTo || row.regDateEnd < dFrom) {
                    continue
                }
                rows.add(copyRow(row))
            }
        }
    }
    return rows
}