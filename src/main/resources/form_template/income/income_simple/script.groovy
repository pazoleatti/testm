package form_template.income.income_simple

import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

import java.text.SimpleDateFormat

/**
 * Сводная форма "Доходы, учитываемые в простых РНУ (доходы простые)"
 * formTemplateId=301
 *
 * TODO:
 *      - не сделан подсчет графы 13 (контрольные графы) потому что справочники "Отчет о прибылях и убытках" и "Оборотная ведомость" еще не реализованы
 *      - указанные справочники реализованы, но не заполнены данными
 *
 * @since 6.06.2013
 * @author auldanov
 */

// графа 1  - incomeTypeId              - КНУ
// графа 2  - incomeGroup               - Группа доходов
// графа 3  - incomeTypeByOperation     - Вид дохода по операции
// графа 4  - accountNo                 - Балансовый счёт по учёту дохода
// графа 5  - rnu6Field10Sum            - РНУ-6 (графа 10) cумма
// графа 6  - rnu6Field12Accepted       - сумма
// графа 7  - rnu6Field12PrevTaxPeriod  - в т.ч. учтено в предыдущих налоговых периодах по графе 10
// графа 8  - rnu4Field5Accepted        - РНУ-4 (графа 5) сумма
// графа 9  - logicalCheck              - Логическая проверка
// графа 10 - accountingRecords         - Счёт бухгалтерского учёта
// графа 11 - opuSumByEnclosure2        - в Приложении №5
// графа 12 - opuSumByTableD            - в Таблице "Д"
// графа 13 - opuSumTotal               - в бухгалтерской отчётности
// графа 14 - difference                - Расхождение

switch (formDataEvent) {
    case FormDataEvent.CREATE:
        checkCreation()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.CALCULATE:
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
        calc()
        logicCheck()
        break
}

//// Кэши и константы
@Field
def refBookCache = [:]

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']

@Field
def rowsNotCalc = ['R1', 'R53', 'R54', 'R156']

@Field
def rows567 = ([2, 3] + (5..11) + (17..20) + [22, 24] + (28..30) + [48, 49, 51, 52] + (65..70) + [139] + (142..151) + (153..155))

@Field
def rows8 = ((2..52) + (55..155))

@Field
def formatY = new SimpleDateFormat('yyyy')
@Field
def format = new SimpleDateFormat('dd.MM.yyyy')

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // КНУ 40001
    def row40001 = getDataRow(dataRows, 'R53')
    row40001.rnu6Field10Sum = 0
    row40001.rnu6Field12Accepted = 0
    row40001.rnu6Field12PrevTaxPeriod = 0
    row40001.rnu4Field5Accepted = 0
    (2..52).each {
        def row = getDataRow(dataRows, "R$it")

        // «графа 5» =сумма значений  «графы 5» для строк с 2 по 52 (раздел «Доходы от реализации»)
        row40001.rnu6Field10Sum = (row40001.rnu6Field10Sum ?: 0) + (row.rnu6Field10Sum ?: 0)

        // «графа 6» =сумма значений  «графы 6» для строк с 2 по 52 (раздел «Доходы от реализации»)
        row40001.rnu6Field12Accepted = (row40001.rnu6Field12Accepted ?: 0) + (row.rnu6Field12Accepted ?: 0)

        // «графа 7» =сумма значений  «графы 7» для строк с 2 по 52 (раздел «Доходы от реализации»)
        row40001.rnu6Field12PrevTaxPeriod = (row40001.rnu6Field12PrevTaxPeriod ?: 0) + (row.rnu6Field12PrevTaxPeriod ?: 0)

        // «графа 8» =сумма значений  «графы 8» для строк с 2 по 52 (раздел «Доходы от реализации»)
        row40001.rnu4Field5Accepted = (row40001.rnu4Field5Accepted ?: 0) + (row.rnu4Field5Accepted ?: 0)
    }

    // КНУ 40002
    def row40002 = getDataRow(dataRows, 'R156')
    row40002.rnu6Field10Sum = 0
    row40002.rnu6Field12Accepted = 0
    row40002.rnu6Field12PrevTaxPeriod = 0
    row40002.rnu4Field5Accepted = 0
    (55..155).each {
        def row = getDataRow(dataRows, "R$it")

        // «графа 5» =сумма значений  «графы 5» для строк с 55 по 155 (раздел «Внереализационные доходы»)
        row40002.rnu6Field10Sum = (row40002.rnu6Field10Sum ?: 0) + (row.rnu6Field10Sum ?: 0)

        // «графа 6» =сумма значений  «графы 6» для строк с 55 по 155 (раздел «Внереализационные доходы»)
        row40002.rnu6Field12Accepted = (row40002.rnu6Field12Accepted ?: 0) + (row.rnu6Field12Accepted ?: 0)

        // «графа 7» =сумма значений  «графы 7» для строк с 55 по 155 (раздел «Внереализационные доходы»)
        row40002.rnu6Field12PrevTaxPeriod = (row40002.rnu6Field12PrevTaxPeriod ?: 0) + (row.rnu6Field12PrevTaxPeriod ?: 0)

        // «графа 8» =сумма значений  «графы 8» для строк с 55 по 155 (раздел «Внереализационные доходы»)
        row40002.rnu4Field5Accepted = (row40002.rnu4Field5Accepted ?: 0) + (row.rnu4Field5Accepted ?: 0)
    }

    def refBookIncome102 = refBookFactory.getDataProvider(52L)
    def refBookIncome101 = refBookFactory.getDataProvider(50L)
    def dateEnd = reportPeriodService.getEndDate(formData.reportPeriodId).time

    // Лог. проверка
    dataRows.each { row ->
        if (['R2', 'R3', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10', 'R11', 'R17', 'R18', 'R19', 'R20', 'R22', 'R24', 'R28',
                'R29', 'R30', 'R48', 'R49', 'R51', 'R52', 'R65', 'R66', 'R67', 'R68', 'R69', 'R70', 'R139', 'R142',
                'R143', 'R144', 'R145', 'R146', 'R147', 'R148', 'R149', 'R150', 'R151', 'R153', 'R154', 'R155'
        ].contains(row.getAlias())) {
            def BigDecimal summ = ((BigDecimal) ((row.rnu6Field10Sum ?: 0) - (row.rnu6Field12Accepted ?: 0)
                    + (row.rnu6Field12PrevTaxPeriod ?: 0))).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = summ < 0 ? "Требуется объяснение" : summ.toString()
        }

        // Графа 11
        if (!rowsNotCalc.contains(row.getAlias())) {
            // получим форму «Сводная форма начисленных доходов уровня обособленного подразделения»(см. раздел 6.1.1)
            def sum6ColumnOfForm302 = 0
            def formData302 = formDataService.find(302, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
            if (formData302 != null) {
                data302 = formDataService.getDataRowHelper(formData302)
                for (def rowOfForm302 in data302.allCached) {
                    if (rowOfForm302.incomeBuhSumAccountNumber == row.accountNo) {
                        sum6ColumnOfForm302 += rowOfForm302.incomeBuhSumAccepted ?: 0
                    }
                }
            }
            row.opuSumByEnclosure2 = sum6ColumnOfForm302
        }

        // Графа 12
        if (!rowsNotCalc.contains(row.getAlias())) {
            def sum8Column = 0
            dataRows.each {
                if (it.accountNo == row.accountNo) {
                    sum8Column += it.rnu4Field5Accepted ?: 0
                }
            }
            row.opuSumByTableD = sum8Column
        }

        def chRows = ['R118', 'R119', 'R141', 'R142']

        // Графа 13
        if (!rowsNotCalc.contains(row.getAlias()) && !(row.getAlias() in chRows)) {
            row.opuSumTotal = 0
            def income102Records = refBookIncome102.getRecords(dateEnd, null, "OPU_CODE = '${row.accountingRecords}'", null)
            for (income102 in income102Records) {
                row.opuSumTotal += income102.TOTAL_SUM.numberValue
            }
        }

        if (row.getAlias() in chRows) {
            row.opuSumTotal = 0
            for (income101 in refBookIncome101.getRecords(dateEnd, null, "ACCOUNT = '${row.accountingRecords}'", null)) {
                row.opuSumTotal += income101.DEBET_RATE.numberValue
            }
        }

        // Графа 14
        if (!rowsNotCalc.contains(row.getAlias()) && !(row.getAlias() in chRows)) {
            row.difference = (row.opuSumByEnclosure2 ?: 0) + (row.opuSumByTableD ?: 0) - (row.opuSumTotal ?: 0)
        }

        if (row.getAlias() in ['R118', 'R119']) {
            row.difference = (row.opuSumTotal ?: 0) - (row.rnu4Field5Accepted ?: 0)
        }

        if (row.getAlias() in ['R141', 'R142']) {
            row.difference = (row.opuSumTotal ?: 0) -
                    ((getDataRow(dataRows, 'R141').rnu4Field5Accepted ?: 0) +
                            (getDataRow(dataRows, 'R142').rnu4Field5Accepted ?: 0))
        }
    }

    dataRowHelper.update(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    dataRows.each {row ->
        if (!rowsNotCalc.contains(row.getAlias())) {
            // Проверка обязательных полей
            checkRequiredColumns(row, nonEmptyColumns)
        }
    }
}

// Консолидация формы
def consolidation() {
    isBank() ? consolidationBank() : consolidationSummary()
}

def consolidationBank() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // очистить форму
    dataRows.each { row ->
        ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted'].each { alias ->
            if (row.getCell(alias).isEditable() || row.getAlias() in ['R53', 'R156']) {
                row.getCell(alias).setValue(0)
            }
        }
        ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference'].each { alias ->
            row.getCell(alias).setValue(null)
        }
    }
    // получить данные из источников
    for (departmentFormType in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind())) {
        def child = formDataService.find(departmentFormType.formTypeId, departmentFormType.kind, departmentFormType.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == departmentFormType.formTypeId) {
            def childData = formDataService.getDataRowHelper(child)

            for (def row : childData.getAll()) {
                if (row.getAlias() == null || row.getAlias().contains('total')) {
                    continue
                }
                def rowResult = getDataRow(dataRows, row.getAlias())
                for (alias in ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']) {
                    if (row.getCell(alias).getValue() != null) {
                        rowResult.getCell(alias).setValue(summ(rowResult.getCell(alias), row.getCell(alias)))
                    }
                }
            }
        }
    }
    dataRowHelper.update(dataRows)
}

def consolidationSummary() {
    def dataRowHelper =  formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    // Очистить форму
    dataRows.each { row ->
        ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted'].each { alias ->
            row.getCell(alias).setValue(null)
        }
    }

    def prevDataRows = null
    def prevFormData = formDataService.getFormDataPrev(formData, formData.departmentId)
    if (prevFormData != null) {
        prevDataRows = formDataService.getDataRowHelper(prevFormData)?.getAll()
    }

    rows567.each { rowNum ->
        def row = getDataRow(dataRows, "R$rowNum")
        row.rnu6Field10Sum = 0
        row.rnu6Field12Accepted = 0
        row.rnu6Field12PrevTaxPeriod = 0
    }
    rows8.each { rowNum ->
        def row = getDataRow(dataRows, "R$rowNum")
        row.rnu4Field5Accepted = 0
    }

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)

        if (source != null && source.state == WorkflowState.ACCEPTED) {
            rows567.each { rowNum ->
                def row = getDataRow(dataRows, "R$rowNum")
                def graph5 = 0
                def graph6 = 0
                def graph7 = 0
                if (source.getFormType().getId() == 318) {
                    def dataRNU6 = formDataService.getDataRowHelper(source)
                    dataRNU6.getAll().each {rowRNU6 ->
                        if (rowRNU6.getAlias() == null) {
                            def knu = getKNUValue(rowRNU6.kny)
                            // если «графа 2» (столбец «Код налогового учета») формы источника = «графе 1» (столбец «КНУ») текущей строки и
                            //«графа 4» (столбец «Балансовый счёт (номер)») формы источника = «графе 4» (столбец «Балансовый счёт по учёту дохода»)
                            if (row.incomeTypeId != null && row.accountNo != null && row.incomeTypeId == knu && isEqualNum(row.accountNo, rowRNU6.code)) {
                                //«графа 5» =  сумма значений по «графе 10» (столбец «Сумма дохода в налоговом учёте. Рубли») всех форм источников вида «(РНУ-6)
                                graph5 += rowRNU6.taxAccountingRuble ?: 0
                                //«графа 6» =  сумма значений по «графе 12» (столбец «Сумма дохода в бухгалтерском учёте. Рубли») всех форм источников вида «(РНУ-6)
                                graph6 += rowRNU6.ruble ?: 0
                                //графа 7
                                if (rowRNU6.ruble != null && rowRNU6.ruble != 0) {
                                    def dateFrom = format.parse('01.01.' + (Integer.valueOf(formatY.format(rowRNU6.date)) - 3))
                                    def taxPeriodList = taxPeriodService.listByTaxTypeAndDate(TaxType.INCOME, dateFrom, rowRNU6.date)
                                    taxPeriodList.each {taxPeriod ->
                                        def reportPeriodList = reportPeriodService.listByTaxPeriod(taxPeriod.getId())
                                        reportPeriodList.each {reportPeriod ->
                                            def primaryRNU6 = formDataService.find(source.formType.id, FormDataKind.PRIMARY, source.departmentId, reportPeriod.getId()) // TODO не реализовано получение по всем подразделениям.
                                            if (primaryRNU6 != null) {
                                                def dataPrimary = formDataService.getDataRowHelper(primaryRNU6)
                                                dataPrimary.getAll().each {rowPrimary ->
                                                    if (rowPrimary.code != null && rowPrimary.code == rowRNU6.code &&
                                                            rowPrimary.docNumber != null && rowPrimary.docNumber == rowRNU6.docNumber &&
                                                            rowPrimary.docDate != null && rowPrimary.docDate == rowRNU6.docDate) {
                                                        graph7 += rowPrimary.taxAccountingRuble
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                row.rnu6Field10Sum += graph5
                row.rnu6Field12Accepted += graph6
                row.rnu6Field12PrevTaxPeriod += graph7
            }
            rows8.each { rowNum ->
                def row = getDataRow(dataRows, "R$rowNum")
                def graph8 = 0
                if (source.formType.id == 316) {
                    def dataRNU4 = formDataService.getDataRowHelper(source)
                    dataRNU4.getAll().each { rowRNU4 ->
                        if (rowRNU4.getAlias() == null) {
                            def knu = getKNUValue(rowRNU4.code)
                            if (row.incomeTypeId != null && row.accountNo != null && row.incomeTypeId == knu && isEqualNum(row.accountNo, rowRNU4.balance)) {
                                //«графа 8» =  сумма значений по «графе 5» (столбец «Сумма дохода за отчётный квартал») всех форм источников вида «(РНУ-4)
                                graph8 += rowRNU4.sum
                            }
                        }
                    }
                }
                row.rnu4Field5Accepted += graph8
            }
        }
    }

    if (prevFormData != null && reportPeriodService.get(formData.reportPeriodId).order != 1) {
        rows567.each { rowNum ->
            def row = getDataRow(dataRows, "R$rowNum")
            //«графа 5» +=«графа 5» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            def rowPrev = getDataRow(prevDataRows, "R$rowNum")
            row.rnu6Field10Sum += rowPrev.rnu6Field10Sum
            //«графа 6» +=«графа 6» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            row.rnu6Field12Accepted += rowPrev.rnu6Field12Accepted
            //«графа 7» +=«графа 7» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            row.rnu6Field12PrevTaxPeriod += rowPrev.rnu6Field12PrevTaxPeriod
        }
        rows8.each { rowNum ->
            def row = getDataRow(dataRows, "R$rowNum")
            //«графа 8» +=«графа 8» формы предыдущего отчётного периода (не учитывается при расчете в первом отчётном периоде)
            def rowPrev = getDataRow(prevDataRows, "R$rowNum")
            row.rnu4Field5Accepted += rowPrev.rnu4Field5Accepted
        }
    }
    dataRowHelper.update(dataRows)
}

void checkCreation() {
    if (formData.kind != FormDataKind.SUMMARY) {
        logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
    }
    formDataService.checkUnique(formData, logger)
}

// Проверка на банк
def isBank() {
    boolean isBank = true
    departmentFormTypeService.getFormDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
        if (it.departmentId != formData.departmentId) {
            isBank = false
        }
    }
    return isBank
}

def getKNUValue(def value) {
    getRefBookValue(28, value)?.CODE?.stringValue
}

def getBalanceValue(def value) {
    getRefBookValue(28, value)?.NUMBER?.stringValue
}

boolean isEqualNum(String accNum, def balance) {
    return accNum.replace('.', '') == getBalanceValue(balance).replace('.', '')
}

// Проверить заполненость обязательных полей
// Нередактируемые не проверяются
def checkRequiredColumns(def row, def columns) {
    def colNames = []
    columns.each {
        def cell = row.getCell(it)
        if (cell.isEditable() && (cell.getValue() == null || row.getCell(it).getValue() == '')) {
            def name = getColumnName(row, it)
            colNames.add('«' + name + '»')
        }
    }
    if (!colNames.isEmpty()) {
        def errorMsg = colNames.join(', ')
        logger.error("Строка ${row.getIndex()}: не заполнены графы : $errorMsg.")
    }
}