package form_template.transport.vehicles.v1970

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
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
        break
    case FormDataEvent.AFTER_CREATE:
        if (formData.kind == FormDataKind.PRIMARY) {
            copyData()
        }
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
    case FormDataEvent.IMPORT:
        importData()
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
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def copyColumns = ['codeOKATO', 'tsTypeCode', 'identNumber', 'model', 'ecoClass', 'regNumber',
        'powerVal', 'baseUnit', 'year', 'regDate', 'regDateEnd', 'stealDateStart', 'stealDateEnd']

// Редактируемые атрибуты
@Field
def editableColumns = ['codeOKATO', 'tsTypeCode', 'identNumber', 'model', 'ecoClass', 'regNumber', 'powerVal',
        'baseUnit', 'year', 'regDate', 'regDateEnd', 'stealDateStart', 'stealDateEnd']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'codeOKATO', 'tsTypeCode', 'identNumber', 'model',
        'regNumber', 'powerVal', 'baseUnit', 'year', 'regDate']


// дата начала отчетного периода
@Field
def start = null

// дата окончания отчетного периода
@Field
def endDate = null

//// Обертки методов

// Поиск записи в справочнике по значению (для импорта)
def getRecordIdImport(def Long refBookId, def String alias, def String value, def int rowIndex, def int colIndex,
                      def boolean required = false) {
    return formDataService.getRefBookRecordIdImport(refBookId, recordCache, providerCache, alias, value,
            getReportPeriodEndDate(), rowIndex, colIndex, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    if (!dataRows.isEmpty()) {
        sort(dataRows)
        def i = 1
        for (def row in dataRows) {
            row.rowNumber = i++
        }
        dataRowHelper.update(dataRows);
    }
}

// сортировка ОКТМО - Муниципальное образование - Код вида ТС
void sort(def dataRows) {
    dataRows.sort { a, b ->
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
    def dataRows = formDataService.getDataRowHelper(formData).allCached

    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def dFrom = getReportPeriodStartDate()
    def dTo = getReportPeriodEndDate()
    def String dFormat = "dd.MM.yyyy"

    // Проверенные строки (4-ая провека)
    def List<DataRow<Cell>> checkedRows = new ArrayList<DataRow<Cell>>()
    for (def row in dataRows) {

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index ?: 0, nonEmptyColumns, logger, true)

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
                if (!checkedRows.contains(rowIn) && row != rowIn && isEquals(row, rowIn)) {
                    checkedRows.add(rowIn)
                    errorRows = ', ' + rowIn.getIndex()
                }
            }
            if (!''.equals(errorRows)) {
                logger.error("Обнаружены строки $index$errorRows, у которых " +
                        "Код ОКТМО = ${getRefBookValue(96, row.codeOKATO)?.CODE?.stringValue}, " +
                        "Идентификационный номер = $row.identNumber, " +
                        "Мощность (величина) = $row.powerVal, " +
                        "Мощность (ед. измерения) = ${getRefBookValue(12, row.baseUnit)?.CODE?.stringValue} " +
                        "совпадают!")
            }
        }
        checkedRows.add(row)

        // 5. Проверка на наличие в списке ТС строк, период владения которых не пересекается с отчётным
        if (row.regDate != null && row.regDate > dTo || row.regDateEnd != null && row.regDateEnd < dFrom) {
            logger.error(errorMsg + 'Период регистрации ТС ('
                    + row.regDate.format(dFormat) + ' - ' + ((row.regDateEnd != null) ? row.regDateEnd.format(dFormat) : '...') + ') ' +
                    ' не пересекается с периодом (' + dFrom.format(dFormat) + " - " + dTo.format(dFormat) +
                    '), за который сформирована налоговая форма!')
        }

        // 7. Проверка года изготовления ТС
        if (row.year != null) {
            Calendar calenadarMake = Calendar.getInstance()
            calenadarMake.setTime(row.year)
            if (calenadarMake.get(Calendar.YEAR) > reportPeriod.taxPeriod.year) {
                logger.error(errorMsg + 'Год изготовления ТС не может быть больше отчетного года!')
            }
        }
    }

    // 6. Проверка наличия формы предыдущего периода
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
    def rows = []
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    def prevReportPeriod = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    if (reportPeriod.order == 4) {
        rows.addAll(getPrevRowsForCopy(prevReportPeriod, []))
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
        rows.addAll(getPrevRowsForCopy(prevReportPeriod, rows))
        prevReportPeriod = reportPeriodService.getPrevReportPeriod(prevReportPeriod.id)
        rows.addAll(getPrevRowsForCopy(prevReportPeriod, rows))
    } else {
        rows += getPrevRowsForCopy(prevReportPeriod, [])
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
def getPrevRowsForCopy(def reportPeriod, def rowsOldE) {
    def rows = []
    def rowsOld = []
    rowsOld.addAll(rowsOldE)
    if (reportPeriod != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriod.id)
        def dataRowsOld = (formDataOld != null ? formDataService.getDataRowHelper(formDataOld)?.allCached : null)
        if (dataRowsOld != null && !dataRowsOld.isEmpty()) {
            def dFrom = getReportPeriodStartDate()
            def dTo = getReportPeriodEndDate()
            for (def row in dataRowsOld) {
                if ((row.regDateEnd != null && row.regDateEnd < dFrom) || (row.regDate > dTo)) {
                    continue
                }

                // эта часть вроде как лишняя
                def regDateEnd = row.regDateEnd
                if (regDateEnd == null || regDateEnd > dTo) {
                    regDateEnd = dTo
                }
                def regDate = row.regDate
                if (regDate < dFrom) {
                    regDate = dFrom
                }
                if (regDate > dTo || regDateEnd < dFrom) {
                    continue
                }

                // исключаем дубли
                def need = true
                for (def rowOld in rowsOld) {
                    if (isEquals(row, rowOld)) {
                        need = false
                        break
                    }
                }
                if (need) {
                    row.setIndex(rowsOld.size())
                    newRow = copyRow(row)
                    rows.add(newRow)
                    rowsOld.add(newRow)
                }
            }
        }
    }
    return rows
}

def isEquals(def row1, def row2) {
    if (row1.codeOKATO == null || row1.identNumber == null || row1.powerVal == null || row1.baseUnit == null) {
        return true
    }
    return (row1.codeOKATO.equals(row2.codeOKATO) && row1.identNumber.equals(row2.identNumber)
            && row1.powerVal.equals(row2.powerVal) && row1.baseUnit.equals(row2.baseUnit))
}

def getReportPeriodStartDate() {
    if (!start) {
        start = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return start
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 16, 1)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[1]): 'Код ОКТМО',
            (xml.row[0].cell[2]): 'Муниципальное образование, на территории которого зарегистрировано транспортное средство (ТС)',
            (xml.row[0].cell[3]): 'Код вида ТС',
            (xml.row[0].cell[4]): 'Вид ТС',
            (xml.row[0].cell[5]): 'Идентификационный номер',
            (xml.row[0].cell[6]): 'Марка',
            (xml.row[0].cell[7]): 'Экологический класс',
            (xml.row[0].cell[8]): 'Регистрационный знак',
            (xml.row[0].cell[9]): 'Мощность (величина)',
            (xml.row[0].cell[10]): 'Мощность (ед. измерения)',
            (xml.row[0].cell[11]): 'Год изготовления',
            (xml.row[0].cell[12]): 'Регистрация (дата регистрации)',
            (xml.row[0].cell[13]): 'Регистрация (дата снятия с регистрации)',
            (xml.row[0].cell[14]): 'Сведения об угоне (дата начала розыска ТС)',
            (xml.row[0].cell[15]): 'Сведения об угоне (дата возврата ТС)',
            (xml.row[1].cell[0]): '1',
            (xml.row[1].cell[1]): '2',
            (xml.row[1].cell[2]): '3',
            (xml.row[1].cell[3]): '4',
            (xml.row[1].cell[4]): '5',
            (xml.row[1].cell[5]): '6',
            (xml.row[1].cell[6]): '7',
            (xml.row[1].cell[7]): '8',
            (xml.row[1].cell[8]): '9',
            (xml.row[1].cell[9]): '10',
            (xml.row[1].cell[10]): '11',
            (xml.row[1].cell[11]): '12',
            (xml.row[1].cell[12]): '13',
            (xml.row[1].cell[13]): '14',
            (xml.row[1].cell[14]): '15',
            (xml.row[1].cell[15]): '16'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 1)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
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

        // графа 1
        newRow.rowNumber = parseNumber(row.cell[0].text(), xlsIndexRow, 0 + colOffset, logger, false)

        // графа 2
        newRow.codeOKATO = getRecordIdImport(96, 'CODE', row.cell[1].text(), xlsIndexRow, 1 + colOffset)

        // графа 3

        // графа 4
        newRow.tsTypeCode = getRecordIdImport(42, 'CODE', row.cell[3].text(), xlsIndexRow, 3 + colOffset)

        // графа 5

        // графа 6
        newRow.identNumber = row.cell[5].text()

        // графа 7
        newRow.model = row.cell[6].text()

        // графа 8
        newRow.ecoClass = getRecordIdImport(40, 'CODE', row.cell[7].text(), xlsIndexRow, 7 + colOffset)

        // графа 9
        newRow.regNumber = row.cell[8].text()

        // графа 10
        newRow.powerVal = parseNumber(row.cell[9].text(), xlsIndexRow, 9 + colOffset, logger, false)

        // графа 11
        newRow.baseUnit = getRecordIdImport(12, 'CODE', row.cell[10].text(), xlsIndexRow, 10 + colOffset)

        // графа 12
        newRow.year = parseDate(row.cell[11].text(), "dd.MM.yyyy", xlsIndexRow, 11 + colOffset, logger, false)

        // графа 13
        newRow.regDate = parseDate(row.cell[12].text(), "dd.MM.yyyy", xlsIndexRow, 12 + colOffset, logger, false)

        // графа 14
        newRow.regDateEnd = parseDate(row.cell[13].text(), "dd.MM.yyyy", xlsIndexRow, 13 + colOffset, logger, false)

        // графа 15
        newRow.stealDateStart = parseDate(row.cell[14].text(), "dd.MM.yyyy", xlsIndexRow, 14 + colOffset, logger, false)

        // графа 16
        newRow.stealDateEnd = parseDate(row.cell[15].text(), "dd.MM.yyyy", xlsIndexRow, 15 + colOffset, logger, false)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}