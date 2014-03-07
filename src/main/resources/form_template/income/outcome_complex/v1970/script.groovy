package form_template.income.outcome_complex.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import groovy.transform.Field

/**
 * Форма "Сводная форма начисленных расходов (расходы сложные)"
 * formTemplateId=303
 *
 * http://conf.aplana.com/pages/viewpage.action?pageId=8784122
 *
 * @author Stanislav Yasinskiy
 *
 * графа  1 - consumptionTypeId
 * графа  2 - consumptionGroup
 * графа  3 - consumptionTypeByOperation
 * графа  4 - consumptionBuhSumAccountNumber
 * графа  5 - consumptionBuhSumRnuSource
 * графа  6 - consumptionBuhSumAccepted
 * графа  7 - consumptionBuhSumPrevTaxPeriod
 * графа  8 - consumptionTaxSumRnuSource
 * графа  9 - consumptionTaxSumS
 * графа 10 - rnuNo
 * графа 11 - logicalCheck
 * графа 12 - accountingRecords
 * графа 13 - opuSumByEnclosure3
 * графа 14 - opuSumByTableP
 * графа 15 - opuSumTotal
 * графа 16 - difference
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        if (formData.kind != FormDataKind.SUMMARY) {
            logger.error("Нельзя создавать форму с типом ${formData.kind?.name}")
        }
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
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
        consolidation()
        if (!isBank()) {
            calcTotal()
        }
        break
    case FormDataEvent.IMPORT:
        importData()
        break
}

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS']

//Аттрибуты, очищаемые перед импортом формы
@Field
def resetColumns = ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS', 'logicalCheck',
        'opuSumByEnclosure3', 'opuSumByTableP', 'opuSumTotal', 'difference']

@Field
def rowsCalc = ['R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R10', 'R11', 'R12', 'R13', 'R14', 'R15', 'R16', 'R17', 'R1',
        'R26', 'R27', 'R28', 'R29', 'R30', 'R31', 'R32', 'R70', 'R71']

@Field
def notImportSum = ['R1', 'R3', 'R4', 'R5', 'R6', 'R7', 'R8', 'R9', 'R12', 'R13', 'R15', 'R16', 'R17', 'R27', 'R29',
        'R67', 'R68', 'R71']

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

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def message = 'ТРЕБУЕТСЯ ОБЪЯСНЕНИЕ'
    def tmp
    def value
    def formDataSimple = getFormDataSimple()
    def income102NotFound = []
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    for (def row : dataRowHelper.getAllCached()) {
        // исключить итоговые строки
        if (row.getAlias() in ['R67', 'R93']) {
            continue
        }
        if (!isEmpty(row.consumptionTaxSumS) && !isEmpty(row.consumptionBuhSumAccepted) &&
                !isEmpty(row.consumptionBuhSumPrevTaxPeriod)) {
            // ОКРУГЛ( «графа9»-(Сумма 6-Сумма 7);2),
            sum6 = 0
            sum7 = 0
            for (rowSum in dataRowHelper.getAllCached()) {
                String knySum
                String kny
                if (rowSum.getCell('consumptionTypeId').hasValueOwner()) {
                    knySum = rowSum.getCell('consumptionTypeId').valueOwner.value
                } else {
                    knySum = rowSum.getCell('consumptionTypeId').value
                }
                if (row.getCell('consumptionTypeId').hasValueOwner()) {
                    kny = row.getCell('consumptionTypeId').valueOwner.value
                } else {
                    kny = row.getCell('consumptionTypeId').value
                }
                if (kny == knySum) {
                    sum6 += (rowSum.consumptionBuhSumAccepted ?: 0)
                    sum7 += (rowSum.consumptionBuhSumPrevTaxPeriod ?: 0)
                }
            }
            tmp = round(row.consumptionTaxSumS - (sum6 - sum7), 2)
            value = ((BigDecimal) tmp).setScale(2, BigDecimal.ROUND_HALF_UP)
            row.logicalCheck = ((tmp < 0) ? message : value.toString())
        }

        if (!isEmpty(row.consumptionBuhSumAccepted) && !isEmpty(row.consumptionBuhSumPrevTaxPeriod)) {
            // графа 13
            if (row.getAlias() in ['R3', 'R11']) {
                tmp = calcColumn6(['R3', 'R11'])
            } else {
                tmp = row.consumptionBuhSumAccepted
            }
            row.opuSumByEnclosure3 = tmp

            // графа 14
            row.opuSumByTableP = getSumFromSimple(formDataSimple, 'consumptionAccountNumber',
                    'rnu5Field5Accepted', row.consumptionBuhSumAccountNumber)

            // графа 15
            def income102 = income102Dao.getIncome102(formData.reportPeriodId, row.accountingRecords)
            if (income102 == null || income102.isEmpty()) {
                income102NotFound += getIndex(row)
                tmp = 0
            } else {
                tmp = ((income102[0] != null) ? income102[0].getTotalSum() : 0)
            }
            row.opuSumTotal = tmp

            // графа 16
            row.difference = (getValue(row.opuSumByEnclosure3) + getValue(row.opuSumByTableP)) - getValue(row.opuSumTotal)
        }
    }

    if (!income102NotFound.isEmpty()) {
        def rows = income102NotFound.join(', ')
        logger.warn("Не найдены соответствующие данные в отчете о прибылях и убытках для строк: $rows")
    }


    calcTotal()
    dataRowHelper.save(dataRowHelper.getAllCached())
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    for (def row in dataRows) {
        if (rowsCalc.contains(row.getAlias())) {
            // Проверка обязательных полей
            checkRequiredColumns(row, nonEmptyColumns)
        }
    }
}

/**
 * Расчет итоговых строк.
 */
void calcTotal() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def totalRow1 = dataRowHelper.getDataRow(dataRowHelper.getAllCached(), 'R67')
    def totalRow2 = dataRowHelper.getDataRow(dataRowHelper.getAllCached(), 'R93')

    // суммы для графы 9
    ['consumptionTaxSumS'].each { alias ->
        totalRow1.getCell(alias).setValue(getSum(alias, 'R2', 'R66'), totalRow1.getIndex())
        totalRow2.getCell(alias).setValue(getSum(alias, 'R69', 'R92'), totalRow2.getIndex())
    }
}

/**
 * Скрипт для консолидации.
 *
 * @author rtimerbaev
 * @since 22.02.2013 15:30
 */
void consolidation() {
    if (!isBank()) {
        return
    }
    // очистить форму
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.getAllCached().each { row ->
        ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each { alias ->
            if (row.getCell(alias).isEditable()) {
                row.getCell(alias).setValue(0,  row.getIndex())
            }
        }
        // графа 11, 13..16
        ['logicalCheck', 'opuSumByEnclosure3', 'opuSumByTableP', 'opuSumTotal', 'difference'].each { alias ->
            row.getCell(alias).setValue(null, row.getIndex())
        }
        if (row.getAlias() in ['R67', 'R93']) {
            row.consumptionTaxSumS = 0
        }
    }

    // получить консолидированные формы из источников     .
    departmentFormTypeService.getSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def child = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (child != null && child.state == WorkflowState.ACCEPTED && child.formType.id == formData.formType.id) {
            for (def row : formDataService.getDataRowHelper(child).getAllCached()) {
                if (row.getAlias() == null) {
                    continue
                }
                def rowResult = dataRowHelper.getDataRow(dataRowHelper.getAllCached(), row.getAlias())
                ['consumptionBuhSumAccepted', 'consumptionBuhSumPrevTaxPeriod', 'consumptionTaxSumS'].each {
                    if (row.getCell(it).getValue() != null && !row.getCell(it).hasValueOwner()) {
                        rowResult.getCell(it).setValue(summ(rowResult.getCell(it), row.getCell(it)), rowResult.getIndex())
                    }
                }
            }
        }
    }
    logger.info('Формирование сводной формы уровня Банка прошло успешно.')
    dataRowHelper.save(dataRowHelper.allCached)
    dataRowHelper.commit()
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка на банк.
 */
def isBank() {
    boolean isBank = true
    departmentFormTypeService.getDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
        if (it.departmentId != formData.departmentId) {
            isBank = false
        }
    }
    return isBank
}

double summ(String columnName, String fromRowA, String toRowA) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def from = dataRowHelper.getDataRowIndex(dataRowHelper.getAllCached(), fromRowA)
    def to = dataRowHelper.getDataRowIndex(dataRowHelper.getAllCached(), toRowA)
    if (from > to) {
        return 0
    }
    def result = summ(formData, dataRowHelper.getAllCached(), new ColumnRange(columnName, from, to))
    return result ?: 0;
}

/**
 * Получить сумму диапазона строк определенного столбца.
 */
def getSum(String columnAlias, String rowFromAlias, String rowToAlias) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def from = dataRowHelper.getDataRowIndex(dataRowHelper.getAllCached(), rowFromAlias)
    def to = dataRowHelper.getDataRowIndex(dataRowHelper.getAllCached(), rowToAlias)
    if (from > to) {
        return 0
    }
    return summ(formData, dataRowHelper.getAllCached(), new ColumnRange(columnAlias, from, to))
}

/**
 * Получить значение или ноль.
 *
 * @param value значение которое надо проверить
 */
def getValue(def value) {
    return value ?: 0
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    formDataService.getDataRowHelper(formData).getAllCached().indexOf(row)
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == ''
}

/**
 * Получить значение для графы 13. Сумма значении графы 6 указанных строк
 *
 * @param aliasRows список алиасов значения которых надо просуммировать
 */
def calcColumn6(def aliasRows) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def sum = 0
    aliasRows.each { alias ->
        sum += dataRowHelper.getDataRow(dataRowHelper.getAllCached(), alias).consumptionBuhSumAccepted
    }
    return sum
}

/**
 * Получить данные формы "расходы простые" (id = 304)
 */
def getFormDataSimple() {
    return formDataService.find(304, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

/**
 * Получить сумму значений из расходов простых.
 *
 * @param data данные формы
 * @param columnAliasCheck алиас графы, по которой отбираются строки для суммирования
 * @param columnAliasSum алиас графы, значения которой суммируются
 * @param value значение, по которому отбираются строки для суммирования
 */
def getSumFromSimple(data, columnAliasCheck, columnAliasSum, value) {
    def sum = 0
    if (data != null && (columnAliasCheck != null || columnAliasCheck != '') && value != null) {
        for (def row : formDataService.getDataRowHelper(data).getAllCached()) {
            if (row.getCell(columnAliasCheck).getValue() == value) {
                sum += (row.getCell(columnAliasSum).getValue() ?: 0)
            }
        }
    }
    return sum
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

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 10, 3)

    def headerMapping = [
            (xml.row[0].cell[0]): 'КНУ',
            (xml.row[0].cell[1]): 'Группа расхода',
            (xml.row[0].cell[2]): 'Вид расхода по операции',
            (xml.row[0].cell[3]): 'Расход по данным бухгалтерского учёта',
            (xml.row[0].cell[7]): 'Расход по данным налогового учёта',
            (xml.row[1].cell[3]): 'номер счёта учёта',
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
    def int maxRow = 93

    def rows = dataRowHelper.allCached
    def int rowIndex = 1
    def knu
    def group
    //def type
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

        def alias = "R" + rowIndex
        def curRow = getDataRow(rows, alias)

        //очищаем столбцы
        resetColumns.each {
            curRow[it] = null
        }

        knu = normalize(curRow.consumptionTypeId)
        group = normalize(curRow.consumptionGroup)
        //type = normalize(curRow.consumptionTypeByOperation)
        num = normalize(curRow.consumptionBuhSumAccountNumber)

        def xmlIndexCol = 0

        def knuImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        def groupImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        //def typeImport = normalize(row.cell[xmlIndexCol].text())
        xmlIndexCol++

        def numImport = normalize(row.cell[xmlIndexCol].text())

        //если совпадают или хотя бы один из атрибутов не пустой и значения строк в файлах входят в значения строк в шаблоне,
        //то продолжаем обработку строки иначе пропускаем строку
        if (!((knu == knuImport && group == groupImport && num == numImport) ||
                ((!knuImport.isEmpty() || !groupImport.isEmpty() || !numImport.isEmpty()) &&
                        knu.contains(knuImport) && group.contains(groupImport) && num.contains(numImport)))) {
            continue
        }
        rowIndex++

        xmlIndexCol = 5

        // графа 6
        val = row.cell[xmlIndexCol].text().trim()
        if (val.isBigDecimal()) {
            curRow.consumptionBuhSumAccepted = parseNumber(val, xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }
        xmlIndexCol++

        // графа 7
        val = row.cell[xmlIndexCol].text().trim()
        if (val.isBigDecimal()) {
            curRow.consumptionBuhSumPrevTaxPeriod = parseNumber(val, xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }
        xmlIndexCol++

        // графа 8
        xmlIndexCol++

        // графа 9
        val = row.cell[xmlIndexCol].text().trim()
        if (!notImportSum.contains(alias) && val.isBigDecimal()) {
            curRow.consumptionTaxSumS = parseNumber(val, xlsIndexRow, xmlIndexCol + colOffset, logger, false)
        }

    }
    if (rowIndex < maxRow) {
        logger.error("Структура файла не соответствует макету налоговой формы в строке с КНУ = $knu. ")
    }
    dataRowHelper.update(rows)
}