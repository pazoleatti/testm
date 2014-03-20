package form_template.income.income_complex.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

import java.math.RoundingMode

/**
 * Форма "6.1.1	Сводная форма начисленных доходов (доходы сложные)".
 * formTemplateId=302
 *
 * TODO:
 *      - непонятно что делать при отсутствии простых доходов в getIncomeSimpleDataRows().
 *              В расчетах контрольных графов пока будут нули при отсутствии данных в доходах простых.
 *      - неясно что с проверками статуса декларации банка при принятии и отмене принятия формы
 *
 * @author vsergeev
 *
 * Графы:
 *
 * ********** 6.1.2 Сводная форма "Доходы, учитываемые в простых РНУ" уровня обособленного подразделения **********
 *
 * 1    incomeTypeId                КНУ
 * 2    incomeGroup                 Группа дохода
 * 3    incomeTypeByOperation       Вид дохода по операции
 * 4    accountNo                   Балансовый счёт по учёту дохода
 * 5    rnu6Field10Sum              РНУ-6 (графа 10) cумма
 * 6    rnu6Field12Accepted         сумма
 * 7    rnu6Field12PrevTaxPeriod    в т.ч. учтено в предыдущих налоговых периодах по графе 10
 * 8    rnu4Field5Accepted          РНУ-4 (графа 5) сумма
 * 9    logicalCheck                Логическая проверка
 * 10   accountingRecords           Счёт бухгалтерского учёта
 * 11   opuSumByEnclosure2          в Приложении №5
 * 12   opuSumByTableD              в Таблице "Д"
 * 13   opuSumTotal                 в бухгалтерской отчётности
 * 14   difference                  Расхождение
 *
 * ********** 6.1.1	Сводная форма начисленных доходов уровня обособленного подразделения **********
 *
 * 1   incomeTypeId                 КНУ
 * 2   incomeGroup                  Группа дохода
 * 3   incomeTypeByOperation        Вид дохода по операциям
 * 4   incomeBuhSumAccountNumber    балансовый счёт по учёту дохода
 * 5   incomeBuhSumRnuSource        источник информации в РНУ
 * 6   incomeBuhSumAccepted         сумма
 * 7   incomeBuhSumPrevTaxPeriod    в т.ч. учтено в предыдущих налоговых периодах
 * 8   incomeTaxSumRnuSource        источник информации в РНУ
 * 9   incomeTaxSumS                сумма
 * 10  rnuNo                        форма РНУ
 * 11  logicalCheck                 Логическая проверка
 * 12  accountingRecords            Счёт бухгалтерского учёта
 * 13  opuSumByEnclosure2           в Приложении №5
 * 14  opuSumByTableD               в Таблице "Д"
 * 15  opuSumTotal                  в бухгалтерской отчётности
 * 16  difference                   Расхождение
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK :
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        consolidationBank()
        if (isTerBank()) {
            calc()
        }
        break
    // подготовить/утвердить
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicCheck()
        break
    // принятия
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // принять из утверждена
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED : // принять из создана
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        break
}

// Редактируемые атрибуты (графа 6, 7, 9)
@Field
def editableColumns = ['incomeBuhSumAccepted', 'incomeBuhSumPrevTaxPeriod', 'incomeTaxSumS']

//Аттрибуты, очищаемые перед импортом формы
@Field
def resetColumns = ['incomeBuhSumAccepted', 'incomeBuhSumPrevTaxPeriod', 'incomeTaxSumS', 'logicalCheck',
        'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference']

// Атрибут итоговой строки для которой вычисляются суммы (графа 9)
@Field
def totalColumn = 'incomeTaxSumS'

// Алиас для первой строки итогов
@Field
def firstTotalRowAlias = 'R30'

// Алиас для второй строки итогов
@Field
def secondTotalRowAlias = 'R85'

// Алиасы строк 4-5 для расчета контрольных граф Сводной формы начисленных доходов
@Field
def rowsAliasesFor4to5 = ['R4', 'R5']

// Алиасы строк 35-30 для расчета контрольных граф Сводной формы начисленных доходов
@Field
def rowsAliasesFor35to40 = ['R35', 'R36', 'R37', 'R38', 'R39', 'R40']

// Алиасы строк, по которым надо подвести итоги для первой итоговой строки
@Field
def rowsAliasesForFirstControlSum = ['R2', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10', 'R11', 'R12', 'R13', 'R14',
        'R15', 'R16', 'R17', 'R18', 'R19', 'R20', 'R21', 'R22', 'R23', 'R24', 'R25', 'R26', 'R27', 'R28', 'R29']

// Алиасы строк, по которым надо подвести итоги для второй итоговой строки
@Field
def rowsAliasesForSecondControlSum = ['R32', 'R33', 'R34', 'R35', 'R36', 'R37', 'R38', 'R39', 'R40', 'R41', 'R42',
        'R43', 'R44', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R51', 'R52', 'R53', 'R54', 'R55', 'R56', 'R57',
        'R58', 'R59', 'R60', 'R61', 'R62', 'R63', 'R64', 'R65', 'R66', 'R67', 'R68', 'R69', 'R70', 'R71', 'R72',
        'R73', 'R74', 'R75', 'R76', 'R77', 'R78', 'R79', 'R80', 'R81', 'R82', 'R83', 'R84']

// Дата окончания отчетного периода
@Field
def endDate = null

@Field
def rbIncome101 = null

@Field
def rbIncome102 = null

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
    def dataRowsHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowsHelper.allCached

    // итоговые строки
    getDataRow(dataRows, firstTotalRowAlias)[totalColumn] = getSum(dataRows, totalColumn, rowsAliasesForFirstControlSum)
    getDataRow(dataRows, secondTotalRowAlias)[totalColumn] = getSum(dataRows, totalColumn, rowsAliasesForSecondControlSum)

    dataRowsHelper.save(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    for (def row : dataRows) {
        // проверка заполнения обязательных полей
        checkRequiredColumns(row, editableColumns)
    }

    rowsAliasesFor35to40.each { rowAlias ->
        def row = getDataRow(dataRows, rowAlias)
        final income101Data = getIncome101Data(row)
        if (!income101Data || income101Data.isEmpty()) { // Нет данных об оборотной ведомости
            logger.warn("Cтрока ${row.getIndex()}: Отсутствуют данные бухгалтерской отчетности в форме \"Оборотная ведомость\"")
        }
    }
    rowsAliasesFor4to5.each { rowAlias ->
        def row = getDataRow(dataRows, rowAlias)
        final income102Data = getIncome102Data(row)
        if (!income102Data || income102Data.isEmpty()) { // Нет данных об оборотной ведомости
            logger.warn("Cтрока ${row.getIndex()}: Отсутствуют данные бухгалтерской отчетности в форме \"Отчет о прибылях и убытках\"")
        }
    }

    checkTotalSum(getDataRow(dataRows, firstTotalRowAlias), getSum(dataRows, totalColumn, rowsAliasesForFirstControlSum))
    checkTotalSum(getDataRow(dataRows, secondTotalRowAlias), getSum(dataRows, totalColumn, rowsAliasesForSecondControlSum))

    if (!logger.containsLevel(LogLevel.ERROR)) {
        // контрольные графы
        calc4to5(dataRows)      // рассчет строк 4 и 5
        calc35to40(dataRows)    // рассчет строк 35-40
    }
    dataRowHelper.save(dataRows)
}

void consolidationBank() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached
    if (dataRows == null || dataRows.isEmpty()) {
        return
    }
    // очистить форму
    dataRows.each { row ->
        editableColumns.each { alias ->
            if (row.getCell(alias).isEditable()) {
                row.getCell(alias).setValue(0, row.getIndex())
            }
        }
        // графа 11, 13..16
        ['logicalCheck', 'opuSumByEnclosure2', 'opuSumByTableD', 'opuSumTotal', 'difference'].each { alias ->
            row.getCell(alias).setValue(null, row.getIndex())
        }
        if (row.getAlias() in [firstTotalRowAlias, secondTotalRowAlias]) {
            row.incomeTaxSumS = 0
        }
    }

    // получить консолидированные формы в дочерних подразделениях в текущем налоговом периоде
    departmentFormTypeService.getFormSources(formData.departmentId, formData.formType.id, formData.kind).each {
        def child = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == formData.formType.id) {
            for (def row : formDataService.getDataRowHelper(child).allCached) {
                if (row.getAlias() == null) {
                    continue
                }
                def rowResult = getDataRow(dataRows, row.getAlias())
                editableColumns.each {
                    if (row.getCell(it).value != null) {
                        rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)), rowResult.getIndex())
                    }
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
    logger.info("Формирование формы прошло успешно: «${formData.formType.name}».")
}

void checkCreation() {
    formDataService.checkUnique(formData, logger)
    if (formData.kind != FormDataKind.SUMMARY) {
        logger.error("Нельзя создавать форму с типом ${formData.kind.name}")
    }
}

// Проверить заполненость обязательных полей
// Нередактируемые не проверяются
void checkRequiredColumns(def row, def columns) {
    def colNames = []
    columns.each { column ->
        def cell = row.getCell(column)
        if (cell.isEditable() && (cell.getValue() == null || cell.getValue() == '')) {
            def name = getColumnName(row, column)
            colNames.add('«' + name + '»')
        }
    }
    if (!colNames.isEmpty()) {
        def errorMsg = colNames.join(', ')
        logger.error("Строка ${row.getIndex()}: не заполнены графы : $errorMsg.")
    }
}

// Расчет контрольных граф Сводной формы начисленных доходов (№ строки 35-40)
void calc35to40(def dataRows) {
    rowsAliasesFor35to40.each { rowAlias ->
        def row = getDataRow(dataRows, rowAlias)
        final income101Data = getIncome101Data(row)
        if (!income101Data) { // Нет данных об оборотной ведомости
            return
        }
        // графа 14
        row.opuSumByTableD = getOpuSumByTableDFor35to40(row, income101Data)
        // графа 15
        row.opuSumTotal = getOpuSumTotalFor35to40(row, income101Data)
        // графа 16
        row.difference = getDifferenceFor35to40(row)
    }
}

// Графа 16. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 35-40)
def getDifferenceFor35to40(def row) {
    // «графа 16» = «графа 9» - ( «графа 15» – «графа 14»)
    return row.incomeTaxSumS - (row.opuSumTotal - row.opuSumByTableD)
}

// Графа 15. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 35-40)
def getOpuSumTotalFor35to40(def row, def income101Data) {
    if (income101Data) {
        return income101Data.sum { income101Row ->
            if (income101Row.ACCOUNT.stringValue == row.accountingRecords) {
                return (income101Row.OUTCOME_DEBET_REMAINS.numberValue?:0)
            } else {
                return 0
            }
        }
    }
    return 0
}

// Графа 14. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 35-40)
def getOpuSumByTableDFor35to40(def row, def income101Data){
    if (income101Data) {
        return income101Data.sum { income101Row ->
            if (income101Row.ACCOUNT.stringValue == row.accountingRecords) {
                return (income101Row.INCOME_DEBET_REMAINS.numberValue ?: 0)
            } else {
                return 0
            }
        }
    }
    return 0
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

// Расчет контрольных граф Сводной формы начисленных доходов (№ строки 4-5)
void calc4to5(def dataRows) {
    final incomeSimpleDataRows = getIncomeSimpleDataRows()

    rowsAliasesFor4to5.each { rowAlias ->
        def row = getDataRow(dataRows, rowAlias)

        // графа 11
        row.logicalCheck = getLogicalCheckFor4to5(row)
        // графа 13
        row.opuSumByEnclosure2 = getOpuSumByEnclosure2For4to5(row)
        // графа 14
        row.opuSumByTableD = getOpuSumByTableDFor4to5(row, incomeSimpleDataRows)
        // графа 15
        row.opuSumTotal = getOpuSumTotalFor4to5(row)
        // графа 16
        row.difference = getDifferenceFor4to5(row)
    }
}

// Графа 16. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 4-5)
def getDifferenceFor4to5(def row) {
    // «графа 16» = («графа 13» + «графа 14») – «графа 15»
    return (row.opuSumByEnclosure2 + row.opuSumByTableD) - row.opuSumTotal
}

// Графа 15. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 4-5)
def getOpuSumTotalFor4to5(def row) {
    // получить отчет о прибылях и убытках
    def income102Records = getIncome102Data(row)
    def tmp = 0
    for (income102 in income102Records) {
        tmp += income102.TOTAL_SUM.numberValue
    }
    return tmp
}

// Графа 14. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 4-5)
def getOpuSumByTableDFor4to5(def row, def incomeSimpleDataRows) {
    if (incomeSimpleDataRows) {
        def sum = 0
        for (def simpleRow : incomeSimpleDataRows) {
            if (simpleRow.accountNo == row.incomeBuhSumAccountNumber && simpleRow.rnu4Field5Accepted != null) {
                sum += simpleRow.rnu4Field5Accepted
            }
        }
        return sum
    }
    return 0
}

// Получить строки формы «Сводная форма "Доходы, учитываемые в простых РНУ" уровня обособленного подразделения»
def getIncomeSimpleDataRows() {
    def formId = 301
    def formDataKind = FormDataKind.SUMMARY
    def departmentId = formData.departmentId
    def reportPeriodId = formData.reportPeriodId
    // TODO (Aydar Kadyrgulov) Проверить, существует ли за этот же период простая форма. если нет, то вывести сообщение.
    // TODO (Ramil Timerbaev) Аналитик сказала, что ничего нового от банка пока нет, оставь так как есть
    def incomeSimpleFormData = formDataService.find(formId, formDataKind, departmentId, reportPeriodId)
    if (incomeSimpleFormData != null && incomeSimpleFormData.id != null)
        return formDataService.getDataRowHelper(incomeSimpleFormData)?.allCached
    return null
}

// Графа 13. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 4-5)
def getOpuSumByEnclosure2For4to5(def row) {
    return row.incomeBuhSumAccepted
}

// Графа 11. Расчет контрольных граф Сводной формы начисленных доходов (№ строки 4-5)
def getLogicalCheckFor4to5(def row) {
    def result = row.incomeTaxSumS - (row.incomeBuhSumAccepted - row.incomeBuhSumPrevTaxPeriod)
    return  (result < 0 ? 'Требуется уточнение' : round(result).toString())
}

// Округление
def BigDecimal round(BigDecimal value, def int precision = 2) {
    return value?.setScale(precision, RoundingMode.HALF_UP)
}

// Подсчет сумм для столбца colName в строках rowsAliases
def getSum(def dataRows, def colName, def rowsAliases) {
    return rowsAliases.sum { rowAlias ->
        def tmp = getDataRow(dataRows, rowAlias)[colName]
        return (tmp ?: 0)
    }
}

// Проверка на террбанк
def isTerBank() {
    boolean isTerBank = false
    def departmentFormTypes = departmentFormTypeService.getFormDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY)
    for (def form : departmentFormTypes) {
        if (form.departmentId != formData.departmentId) {
            isTerBank = true
            break
        }
    }
    return isTerBank
}

// Получение импортируемых данных
void importData() {
    def xml = getXML('КНУ', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 10, 3)

    def headerMapping = [
            (xml.row[0].cell[0]): 'КНУ',
            (xml.row[0].cell[1]): 'Группа дохода',
            (xml.row[0].cell[2]): 'Вид дохода по операциям',
            (xml.row[0].cell[3]): 'Доход по данным бухгалтерского учёта',
            (xml.row[0].cell[7]): 'Доход по данным налогового учёта',
            (xml.row[1].cell[3]): 'балансовый счёт по учёту дохода',
            (xml.row[1].cell[4]): 'источник информации в РНУ',
            (xml.row[1].cell[5]): 'сумма',
            (xml.row[1].cell[6]): 'в т.ч. учтено в предыдущих налоговых периодах',
            (xml.row[1].cell[7]): 'источник информации в РНУ',
            (xml.row[1].cell[8]): 'сумма',
            (xml.row[1].cell[9]): 'форма РНУ',
            (xml.row[2].cell[0]): '1',
            (xml.row[2].cell[1]): '2',
            (xml.row[2].cell[2]): '3',
            (xml.row[2].cell[3]): '4',
            (xml.row[2].cell[4]): '5',
            (xml.row[2].cell[5]): '6',
            (xml.row[2].cell[6]): '7',
            (xml.row[2].cell[7]): '8',
            (xml.row[2].cell[8]): '9',
            (xml.row[2].cell[9]): '10'
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
    def int maxRow = 88

    def rows = dataRowHelper.allCached
    def int rowIndex = 1
    def knu
    def group
    def type
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
        group = normalize(curRow.incomeGroup)
        type = normalize(curRow.incomeTypeByOperation)

        def xmlIndexCol = 0

        def knuImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        def groupImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        def typeImport = normalize(row.cell[xmlIndexCol].text())

        //если совпадают или хотя бы один из атрибутов не пустой и значения строк в файлах входят в значения строк в шаблоне,
        //то продолжаем обработку строки иначе пропускаем строку
        if (!((knu == knuImport && group == groupImport && type == typeImport) ||
                ((!knuImport.isEmpty() || !groupImport.isEmpty() || !typeImport.isEmpty()) &&
                        knu.contains(knuImport) && group.contains(groupImport) && type.contains(typeImport)))) {
            continue
        }
        rowIndex++

        xmlIndexCol = 5

        // графа 6
        if (row.cell[xmlIndexCol].text().trim().isBigDecimal()){
            curRow.incomeBuhSumAccepted = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }
        xmlIndexCol++

        // графа 7
        if (row.cell[xmlIndexCol].text().trim().isBigDecimal()){
            curRow.incomeBuhSumPrevTaxPeriod = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }
        xmlIndexCol++

        // графа 8
        xmlIndexCol++

        // графа 9
        if (row.cell[xmlIndexCol].text().trim().isBigDecimal()){
            curRow.incomeTaxSumS = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }

    }
    if (rowIndex < maxRow) {
        logger.error("Структура файла не соответствует макету налоговой формы в строке с КНУ = $knu. ")
    }
    dataRowHelper.update(rows)
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

void checkTotalSum(totalRow, sum){
    if (totalRow[totalColumn] != sum) {
        logger.error("Итоговое значение в строке ${totalRow.getIndex()} рассчитано неверно в графе \"" + getColumnName(totalRow, totalColumn) + "\"")
    }
}