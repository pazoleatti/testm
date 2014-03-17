package form_template.income.income_simple

import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.TaxType
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
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
// графа 2  - incomeGroup               - Группа дохода
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
    case FormDataEvent.IMPORT:
        importData()
        break
}

//// Кэши и константы
@Field
def refBookCache = [:]

//Все аттрибуты
@Field
def allColumns = ['incomeTypeId', 'incomeGroup', 'incomeTypeByOperation', 'accountNo',
        'rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted',
        'logicalCheck', 'accountingRecords', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']

//Аттрибуты, очищаемые перед импортом формы
@Field
def resetColumns = ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted',
        'logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference']

@Field
def rowsNotCalc = ['R1', 'R53', 'R54', 'R156']

@Field
def totalColumns = ['rnu6Field10Sum', 'rnu6Field12Accepted', 'rnu6Field12PrevTaxPeriod', 'rnu4Field5Accepted']

@Field
def rows567 = ([2, 3] + (5..11) + (17..20) + [22, 24] + (28..30) + [48, 49, 51, 52] + (65..70) + [139] + (142..151) + (153..155))

@Field
def rows8 = ((2..52) + (55..155))

@Field
def chRows = ['R118', 'R119', 'R141', 'R142']

@Field
def formatY = new SimpleDateFormat('yyyy')
@Field
def format = new SimpleDateFormat('dd.MM.yyyy')

// Дата окончания отчетного периода
@Field
def endDate = null

@Field
def rbIncome101 = null

@Field
def rbIncome102 = null

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
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
    if (!fileName.endsWith('.xls')) {
        throw new ServiceException('Выбранный файл не соответствует формату xls!')
    }
    def xmlString = importService.getData(is, fileName, 'windows-1251', startStr, endStr)
    if (xmlString == null) {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
    }
    def xml = new XmlSlurper().parseText(xmlString)
    if (xml == null) {
        throw new ServiceException('Отсутствие значении после обработки потока данных')
    }
    return xml
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def row40001 = getDataRow(dataRows, 'R53')
    def row40002 = getDataRow(dataRows, 'R156')
    totalColumns.each{ alias ->
        row40001[alias] = getSum(dataRows, alias, 'R2', 'R52')
        row40002[alias] = getSum(dataRows, alias, 'R55', 'R155')
    }

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
        if (!(row.getAlias() in rowsNotCalc)) {
            def sum8Column = 0
            dataRows.each {
                if (it.accountNo == row.accountNo) {
                    sum8Column += it.rnu4Field5Accepted ?: 0
                }
            }
            row.opuSumByTableD = sum8Column
        }

        // Графа 13
        if (!(row.getAlias() in (rowsNotCalc + chRows))) {
            row.opuSumTotal = 0
            def income102Records = getIncome102Data(row)
            for (income102 in income102Records) {
                row.opuSumTotal += income102.TOTAL_SUM.numberValue
            }
        }

        if (row.getAlias() in chRows) {
            row.opuSumTotal = 0
            def income101Records = getIncome101Data(row)
            for (income101 in income101Records) {
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

// Возвращает данные из Оборотной Ведомости за период, для которого сформирована текущая форма
def getIncome101Data(def row) {
    // Справочник 50 - "Оборотная ведомость (Форма 0409101-СБ)"
    if (rbIncome101 == null) {
        rbIncome101 = refBookFactory.getDataProvider(50L)
    }
    return rbIncome101?.getRecords(getReportPeriodEndDate(), null, "ACCOUNT = '${row.accountingRecords}' AND DEPARTMENT_ID = ${formData.departmentId}", null)
}

// Возвращает данные из Отчета о прибылях и убытках за период, для которого сформирована текущая форма
def getIncome102Data(def row) {
    // справочник "Отчет о прибылях и убытках (Форма 0409102-СБ)"
    if (rbIncome102 == null) {
        rbIncome102 = refBookFactory.getDataProvider(52L)
    }
    return rbIncome102?.getRecords(getReportPeriodEndDate(), null, "OPU_CODE = '${row.accountingRecords}' AND DEPARTMENT_ID = ${formData.departmentId}", null)
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
    dataRows.each {row ->
        if (row.getAlias() in chRows) {
            def income101Records = getIncome101Data(row)
            if (!income101Records || income101Records.isEmpty()) {
                logger.error("Cтрока ${row.getIndex()}: Отсутствуют данные бухгалтерской отчетности в форме \"Оборотная ведомость\"")
            }
        }
        if (!(row.getAlias() in (rowsNotCalc + chRows))) {
            def income102Records = getIncome102Data(row)
            if (!income102Records || income102Records.isEmpty()) {
                logger.error("Cтрока ${row.getIndex()}: Отсутствуют данные бухгалтерской отчетности в форме \"Отчет о прибылях и убытках\"")
            }
        }
    }

    def row40001 = getDataRow(dataRows, 'R53')
    def row40002 = getDataRow(dataRows, 'R156')
    def need40001 = [:]
    def need40002 = [:]
    totalColumns.each{ alias ->
        need40001[alias] = getSum(dataRows, alias, 'R2', 'R52')
        need40002[alias] = getSum(dataRows, alias, 'R55', 'R155')
    }
    checkTotalSum(row40001, need40001)
    checkTotalSum(row40002, need40002)
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
                row.getCell(alias).setValue(0, row.getIndex())
            }
        }
        ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference'].each { alias ->
            row.getCell(alias).setValue(null, row.getIndex())
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
                        rowResult.getCell(alias).setValue(summ(rowResult.getCell(alias), row.getCell(alias)), null)
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
            row.getCell(alias).setValue(null, row.getIndex())
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
                            def knu = getKNUValue(rowRNU6.code)
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
                                    def reportPeriodList = reportPeriodService.getReportPeriodsByDate(TaxType.INCOME, dateFrom, rowRNU6.date)
                                    reportPeriodList.each { reportPeriod ->
                                        def primaryRNU6 = formDataService.find(source.formType.id, FormDataKind.PRIMARY, source.departmentId, reportPeriod.getId()) // TODO не реализовано получение по всем подразделениям.
                                        if (primaryRNU6 != null) {
                                            def dataPrimary = formDataService.getDataRowHelper(primaryRNU6)
                                            dataPrimary.getAll().each { rowPrimary ->
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
                            def knu = getKNUValue(rowRNU4.balance)
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

// Получение импортируемых данных
void importData() {
    def xml = getXML('КНУ', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 8, 3)

    def headerMapping = [
            (xml.row[0].cell[0]): 'КНУ',
            (xml.row[0].cell[1]): 'Группа дохода',
            (xml.row[0].cell[2]): 'Вид дохода по операциям',
            (xml.row[0].cell[3]): 'Балансовый счёт по учёту дохода',
            (xml.row[0].cell[4]): 'РНУ-6 (графа 10) сумма',
            (xml.row[0].cell[5]): 'РНУ-6 (графа 12)',
            (xml.row[0].cell[7]): 'РНУ-4 (графа 5) сумма',
            (xml.row[1].cell[5]): 'сумма',
            (xml.row[1].cell[6]): 'в т.ч. учтено в предыдущих налоговых периодах по графе 10',
            (xml.row[2].cell[0]): '1',
            (xml.row[2].cell[1]): '2',
            (xml.row[2].cell[2]): '3',
            (xml.row[2].cell[3]): '4',
            (xml.row[2].cell[4]): '5',
            (xml.row[2].cell[5]): '6',
            (xml.row[2].cell[6]): '7',
            (xml.row[2].cell[7]): '8',
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
    def int rowOffset = 3
    def int colOffset = 0
    def int maxRow = 156

    def rows = dataRowHelper.allCached
    def int rowIndex = 1
    def knu
    def type
    def num
    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // пропустить шапку таблицы
        if (xmlIndexRow <= headRowCount) {
            continue
        }
        // прервать по загрузке нужных строк
        if (rowIndex > maxRow) {
            break
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def curRow = getDataRow(rows, "R" + rowIndex)

        //очищаем столбцы
        resetColumns.each {
            curRow[it] = null
        }

        knu = normalize(curRow.incomeTypeId)
        //def group = normalize(curRow.incomeGroup)
        type = normalize(curRow.incomeTypeByOperation)
        num = normalize(curRow.accountNo)

        def xmlIndexCol = 0

        def knuImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        //def groupImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        def typeImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        def numImport = normalize(row.cell[xmlIndexCol].text()).replace(",", ".")

        //если совпадают или хотя бы один из атрибутов не пустой и значения строк в файлах входят в значения строк в шаблоне,
        //то продолжаем обработку строки иначе пропускаем строку
        if (!((knu == knuImport && type == typeImport && num == numImport) ||
                ((!knuImport.isEmpty() || !typeImport.isEmpty() || !numImport.isEmpty()) &&
                        knu.contains(knuImport) && type.contains(typeImport) && num.contains(numImport)))) {
            continue
        }
        rowIndex++

        xmlIndexCol = 4

        // графа 5
        if (row.cell[xmlIndexCol].text().trim().isBigDecimal()){
            curRow.rnu6Field10Sum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }
        xmlIndexCol++

        // графа 6
        if (row.cell[xmlIndexCol].text().trim().isBigDecimal()){
            curRow.rnu6Field12Accepted = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }
        xmlIndexCol++

        // графа 7
        if (row.cell[xmlIndexCol].text().trim().isBigDecimal()){
            curRow.rnu6Field12PrevTaxPeriod = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }
        xmlIndexCol++

        // графа 8
        curRow.rnu4Field5Accepted = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
    }
    if (rowIndex < maxRow) {
        logger.error("Структура файла не соответствует макету налоговой формы в строке с КНУ = $knu. ")
    }
    dataRowHelper.update(rows)
}

/** Получить сумму диапазона строк определенного столбца. */
def getSum(def dataRows, String columnAlias, String rowFromAlias, String rowToAlias) {
    def from = getDataRow(dataRows, rowFromAlias).getIndex() - 1
    def to = getDataRow(dataRows, rowToAlias).getIndex() - 1
    if (from > to) {
        return 0
    }
    return ((BigDecimal)summ(formData, dataRows, new ColumnRange(columnAlias, from, to))).setScale(2, BigDecimal.ROUND_HALF_UP)
}

void checkTotalSum(totalRow, needRow){
    def errorColumns = []
    totalColumns.each { totalColumn ->
        if (totalRow[totalColumn] != needRow[totalColumn]) {
            errorColumns += "\"" + getColumnName(totalRow, totalColumn) + "\""
        }
    }
    if (!errorColumns.isEmpty()){
        logger.error("Итоговое значение в строке ${totalRow.getIndex()} рассчитано неверно в графах ${errorColumns.join(", ")}!")
    }
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

