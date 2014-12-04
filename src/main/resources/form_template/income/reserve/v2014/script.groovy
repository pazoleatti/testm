package form_template.income.reserve.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType
import groovy.transform.Field

/**
 * Форма "(РСД) Расчет резерва по сомнительным долгам в целях налогообложения"
 * графа  1 - rowNum			- № п/п
 * графа  2 - bankName			- Наименование банка
 * графа  3 - sum45			    - Задолженность от 45 до 90 дней. Сумма долга
 * графа  4 - norm45			- Задолженность от 45 до 90 дней. Норматив отчислений 50%
 * графа  5 - reserve45			- Задолженность от 45 до 90 дней. Расчетный резерв
 * графа  6 - sum90			    - Задолженность свыше 90 дней. Сумма долга
 * графа  7 - norm90			- Задолженность свыше 90 дней. Норматив отчислений 100%
 * графа  8 - reserve90			- Задолженность свыше 90 дней. Расчетный резерв
 * графа  9 - totalReserve		- Итого расчетный резерв гр.9=гр.5+гр.8
 * графа 10 - sumIncome			- Сумма доходов за отчетный период
 * графа 11 - normIncome		- Норматив отчислений от суммы доходов 10%
 * графа 12 - valueReserve		- Величина созданного резерва в отчетном периоде
 * графа 13 - reservePrev		- Резерв на предыдущую отчетную дату
 * графа 14 - reserveCurrent	- Резерв на отчетную дату
 * графа 15 - addChargeReserve	- Изменение фактического резерва. Доначисление резерва с отнесением на расходы код 22670
 * графа 16 - restoreReserve	- Изменение фактического резерва. Восстановление резерва на доходах код 13091
 * графа 17 - usingReserve		- Изменение фактического резерва. Использование резерва на погашение процентов по безнадежным долгам в отчетном периоде
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
        preCalcLogicCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED:  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED: // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED:  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED:  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED: // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED: // Утвердить из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidation()
        break
    case FormDataEvent.IMPORT:
        noImport(logger)
        break
}

@Field
def allColumns = ['rowNum', 'bankName', 'sum45', 'norm45', 'reserve45', 'sum90', 'norm90', 'reserve90',
                  'totalReserve', 'sumIncome', 'normIncome', 'valueReserve', 'reservePrev', 'reserveCurrent',
                  'addChargeReserve', 'restoreReserve', 'usingReserve']

@Field
def nonEmptyColumns = allColumns - ['rowNum', 'bankName', 'sumIncome', 'normIncome']

@Field
def clearColumns = allColumns - ['rowNum', 'bankName']

@Field
def sumBankColumns = ['sum45', 'reserve45', 'sum90', 'reserve90', 'totalReserve', 'valueReserve', 'addChargeReserve', 'restoreReserve']

@Field
def sumCAColumns = ['sum45', 'reserve45', 'sum90', 'reserve90', 'totalReserve', 'valueReserve', 'reservePrev',
                    'reserveCurrent', 'addChargeReserve', 'restoreReserve', 'usingReserve']

@Field
def Map<String, Integer> departmentRowMap = [2 : 103, 3 : 98, 4 : 5, 5: 17, 6 : 60, 7 : 89, 8 : 9, 9 : 83, 10 : 33,
                                              11 : 110, 12 : 45, 13 : 65, 14 : 73, 15 : 28, 16 : 21, 17 : 53, 20 : 176,
                                                21 : 131, 22 : 115, 23 : 117]

@Field
def startDate = null

@Field
def endDate = null

@Field
def editableStyle = 'Редактируемая'

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def rowBank = dataRows[0]
    def rowCA = dataRows[18]
    def rowCAWriteoff = dataRows[32]
    def tbRows = dataRows[2..18]
    def reserveRows = dataRows[2..17] + rowCAWriteoff
    def subCARows = dataRows[20..23] // заполняются из источников
    def writeOffRows = dataRows[24..31] // заполняются вручную
    def bankRows = dataRows[2..17] + subCARows // заполняются из источников
    def useReserveRows = tbRows + writeOffRows

    // строки 3-18, 21-24
    bankRows.each { row ->
        // графы 9, 12, 14
        row.totalReserve = (row.reserve45 ?: 0) + (row.reserve90 ?: 0)
        row.reserveCurrent = (row.reservePrev ?: 0) + (row.addChargeReserve ?: 0) + (row.restoreReserve ?: 0) + (row.usingReserve ?: 0)
        row.valueReserve = row.reserveCurrent
    }
    // ЦА с списанием 33 строка
    sumBankColumns.each { column -> rowCAWriteoff[column] = subCARows.sum { row -> row[column] ?: 0 } }
    rowCAWriteoff.reservePrev = subCARows.sum { it.reservePrev ?: 0 } - writeOffRows.sum { it.reservePrev ?: 0 }
    rowCAWriteoff.reserveCurrent = subCARows.sum { it.reserveCurrent ?: 0 } - writeOffRows.sum { it.reserveCurrent ?: 0 }
    rowCAWriteoff.usingReserve = (subCARows + writeOffRows).sum { it.usingReserve ?: 0 }
    // ЦА 19 строка
    sumCAColumns.each { column -> rowCA[column] = subCARows.sum { row -> row[column] ?: 0 } }
    // строка "Сбербанк России" 1 строка
    sumBankColumns.each { column -> rowBank[column] = tbRows.sum { row -> row[column] ?: 0 } }
    rowBank.norm45 = rowCA.norm45 = 50
    rowBank.norm90 = rowCA.norm90 = 100
    if (rowBank.sumIncome != null) {
        rowBank.normIncome = rowBank.sumIncome * 0.1
    }
    ['reservePrev', 'reserveCurrent'].each { column ->
        rowBank[column] = reserveRows.sum { row -> row[column] ?: 0 }
    }
    rowBank.usingReserve = useReserveRows.sum { it.usingReserve ?: 0 }

    dataRowHelper.update(dataRows)

}

void preCalcLogicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def sourceFormTypeId = 329 // РНУ-30
    def sourceFormType = formTypeService.get(sourceFormTypeId)

    def departmentFormTypes = departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate())
    departmentRowMap.each { index, departmentId ->
        def row = dataRows[index.toInteger()]
        def departmentFormType = departmentFormTypes.find {
            // совпадает подразделение и тип формы
            it.departmentId == departmentId && it.formTypeId == sourceFormTypeId
        }
        if (departmentFormType == null) {
            logger.error ("Строка ${row.getIndex()}: " + "Не назначена источником налоговая форма «${sourceFormType.name}» для «${row.bankName}»!")
        } else {
            def child = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (departmentFormType.formTypeId == sourceFormTypeId && (child == null || child.state != WorkflowState.ACCEPTED)) {
                def cause = child == null ? "не создана" : "не находится в статусе «Принята»"
                def childDepartment = departmentService.get(departmentFormType.departmentId)
                logger.error("Строка ${row.getIndex()}: " + "${departmentFormType.kind.name} налоговая форма «${sourceFormType.name}» в подразделении «${childDepartment.name}» $cause!")
            }
        }
    }
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def rowBank = dataRows[0]
    checkNonEmptyColumns(rowBank, rowBank.getIndex(), nonEmptyColumns + ['sumIncome', 'normIncome'], logger, true)
    for (int i in ((2..18) + (20..23))) {
        def row = dataRows[i]
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)
    }
    for (int i in ((24..31))) {
        def row = dataRows[i]
        checkNonEmptyColumns(row, row.getIndex(), ['reservePrev', 'reserveCurrent', 'usingReserve'], logger, true)
    }
    def rowCAWriteoff = dataRows[32]
    checkNonEmptyColumns(rowCAWriteoff, rowCAWriteoff.getIndex(), nonEmptyColumns - ['norm45', 'norm90'], logger, true)
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // очищаем форму
    dataRows.each { row ->
        clearColumns.each {
            if (row.getCell(it)?.style?.alias != editableStyle) {
                row[it] = null
            }
        }
    }

    def sourceFormTypeId = 329 // РНУ-30

    // получить данные из источников
    for (def departmentFormType in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(),
            getReportPeriodStartDate(), getReportPeriodEndDate())) {
        def child = formDataService.getLast(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId, formData.periodOrder)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == sourceFormTypeId) {
            def childData = formDataService.getDataRowHelper(child)
            def childRows = childData.all
            def rowTotal = getDataRow(childRows, 'total')
            def childDepartmentId = child.departmentId
            def entry = departmentRowMap.find {index, departmentId ->
                departmentId == childDepartmentId
            }
            if (entry?.key != null) {
                fillRow(dataRows[entry.key.toInteger()], rowTotal)
            }
        }
    }
    dataRowHelper.update(dataRows)
}

void fillRow(def dataRow, def row) {
    dataRow.sum45 = row.debt45_90DaysSum
    dataRow.norm45 = 50
    dataRow.reserve45 = row.debt45_90DaysReserve
    dataRow.sum90 = row.debtOver90DaysSum
    dataRow.norm90 = 100
    dataRow.reserve90 = row.debtOver90DaysReserve
    dataRow.reservePrev = row.reservePrev
    dataRow.addChargeReserve = row.calcReserve
    dataRow.restoreReserve = row.reserveRecovery
    dataRow.usingReserve = row.useReserve
}