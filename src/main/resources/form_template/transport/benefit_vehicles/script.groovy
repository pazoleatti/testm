package form_template.transport.benefit_vehicles

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 * Сведения о льготируемых транспортных средствах, по которым уплачивается транспортный налог
 * formTemplateId=202
 *
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
// 2 Код ОКАТО  -  codeOKATO
// 3 Идентификационный номер  -  identNumber
// 4 Регистрационный знак  -  regNumber
// 5 Код налоговой льготы - taxBenefitCode
// 6 Дата начала Использование льготы - benefitStartDate
// 7 Дата окончания Использование льготы - benefitEndDate

//// Кэши и константы
@Field
def refBookCache = [:]

// Редактируемые атрибуты
@Field
def copyColumns = ['codeOKATO', 'identNumber', 'regNumber', 'taxBenefitCode', 'benefitStartDate', 'benefitEndDate']

@Field
def editableColumns = ['codeOKATO', 'identNumber', 'regNumber', 'taxBenefitCode', 'benefitStartDate', 'benefitEndDate']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNumber', 'codeOKATO', 'identNumber', 'regNumber', 'taxBenefitCode', 'benefitStartDate',
        'benefitEndDate']

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
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
        sort()
        def i = 1
        for (def row in dataRows) {
            row.rowNumber = i++
        }
        dataRowHelper.update(dataRows);
    }
}

// сортировка ОКАТО
void sort() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.getAllCached().sort { a, b ->
        def valA = getRefBookValue(3, a.codeOKATO).OKATO.stringValue
        def valB = getRefBookValue(3, b.codeOKATO).OKATO.stringValue
        return valA.compareTo(valB)
    }
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def dFrom = reportPeriodService.getStartDate(formData.reportPeriodId).time
    def dTo = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def String dFormat = "dd.MM.yyyy"

    // Проверенные строки (3-я провека)
    def List<DataRow<Cell>> checkedRows = new ArrayList<DataRow<Cell>>()
    for (def row in dataRows) {

        def index = row.getIndex()
        def errorMsg = "Строка $index: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        if (row.benefitStartDate != null && row.benefitEndDate != null){
            // 2. Поверка на соответствие дат использования льготы
            if (row.benefitEndDate.compareTo(row.benefitStartDate) < 0) {
                logger.error(errorMsg + 'Неверно указаны даты начала и окончания использования льготы!')
            }

            // 4. Проверка на наличие в списке ТС строк, период использования льготы которых не пересекается
            // с отчётным / налоговым периодом, к которому относится налоговая форма
            if(row.benefitStartDate > dTo || row.benefitEndDate < dFrom) {
                logger.error(errorMsg + 'Период использования льготы ТС ('
                        + row.benefitStartDate.format(dFormat) + ' - ' + row.benefitEndDate.format(dFormat) + ') ' +
                        ' не пересекается с периодом (' + dFrom.format(dFormat) + " - " + dTo.format(dFormat) +
                        '), за который сформирована налоговая форма!')
            }
        }

        // 3. Проверка на наличие в списке ТС строк, для которых графы 2, 3, 4
        // («Код ОКАТО», «Идентификационный номер», «Регистрационный знак») одинаковы
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
                logger.error("Обнаружены строки $index$errorRows, у которых Код ОКАТО = $row.codeOKATO, " +
                        "Идентификационный номер = $row.identNumber, " +
                        "Регистрационный знак = $row.regNumber совпадают!")
            }
        }
        checkedRows.add(row)

        // Проверки соответствия НСИ
        checkNSI(3, row, "codeOKATO")
        checkNSI(6, row, "taxBenefitCode")
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
            return reportPeriod.name + " " + reportPeriod.getYear() + ", "
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
        newRow.getCell(alias).value = row.getCell(alias).value
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