package form_template.vat.vat_973_1_14.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import groovy.transform.Field

/**
 *  (937.1.14) Расшифровка графы 14 «Расхождение» формы 937.1
 *
 *  formTemplateId=607
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CALCULATE:
        calc()
        logicCheck()
        break
    case FormDataEvent.CHECK:
        logicCheck()
        break
    case FormDataEvent.ADD_ROW:
        addRow()
        break
    case FormDataEvent.DELETE_ROW:
        if (currentDataRow.getAlias() == null) formDataService.getDataRowHelper(formData).delete(currentDataRow)
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
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
}

// Редактируемые атрибуты
@Field
def editableColumns = ['differences', 'sum']

// Проверяемые на пустые значения атрибуты
@Field
def nonEmptyColumns = ['rowNum', 'differences', 'sum']

// Дата начала отчетного периода
@Field
def startDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

@Field
def dateFormat = 'dd.MM.yyyy'

// Добавление строки
void addRow() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    // По-умолчанию перед «Итого»
    def index = getDataRow(dataRows, 'total').getIndex()
    // Если выделена нефиксированная строка
    if (currentDataRow != null && currentDataRow.getAlias() == null) {
        index = currentDataRow.getIndex() + 1
    }
    def newRow = formData.createDataRow()
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    newRow.index = index
    dataRowHelper.insert(newRow, index)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    for (def row in dataRows) {
        def index = row.getIndex()
        if (row.getAlias() != 'total') {
            // Проверка заполнения граф
            checkNonEmptyColumns(row, index ?: 0, nonEmptyColumns, logger, true)
        }
    }
    // Проверка суммы в строке 13
    def other = getDataRow(dataRows, 'R13')
    if (other.sum != calcOther(dataRows)) {
        logger.error("Сумма в строке 13 «Прочие (расшифровать):» не совпадает с расшифровкой!")
    }
    // Проверка итоговых значений
    def itog = getDataRow(dataRows, 'total')
    if (itog.sum != calcItog(dataRows)) {
        logger.error(WRONG_TOTAL, getColumnName(itog, 'sum'))
    }
    // Проверка наличия экземпляра налоговой формы 937.1 по соответствующему подразделению за соответствующий налоговый период; проверка итоговой суммы
    def formData937_1 = formDataService.find(606, formData.kind, formData.departmentId, formData.reportPeriodId)
    if (formData937_1 == null) {
        logger.warn("Экземпляр налоговой формы 937.1 «Итоговые данные книги покупок» за период %s — %s не существует (отсутствуют первичные данные для проверки)!",
                getReportPeriodStartDate().format(dateFormat), getReportPeriodEndDate().format(dateFormat))
    } else {
        def dataRows937_1 = formDataService.getDataRowHelper(formData937_1).allCached
        def totalARow = null
        if (dataRows937_1 != null) {
            totalARow = getDataRow(dataRows937_1, 'totalA')
        }
        if (calcItog(dataRows) - calcOther(dataRows) != totalARow?.diff) {
            logger.warn('Сумма расхождения не соответствует расшифровке!')
        }
    }
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def rowNum = 0
    for (def row in dataRows) {
        rowNum++
        if (row.getAlias() == null) {
            row.rowNum = rowNum
        }
    }
    def itog = getDataRow(dataRows, 'total')
    itog?.sum = calcItog(dataRows)
    def other = getDataRow(dataRows, 'R13')
    other?.sum = calcOther(dataRows)
    dataRowHelper.update(dataRows)
}

// Расчет итога
def calcItog(def dataRows) {
    def sum = 0 as BigDecimal
    for (def row in dataRows) {
        if (row.getAlias() != 'total' && row.getAlias() != 'R13') {
            sum += row.sum == null ? 0 : row.sum
        }
    }
    return sum
}

// Расчет прочих
def calcOther(def dataRows) {
    def sum = 0 as BigDecimal
    for (def row in dataRows) {
        if (row.getAlias() == null) {
            sum += row.sum == null ? 0 : row.sum
        }
    }
    return sum
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def tmp = []
    // Суммы по фиксированным строкам
    def staticSum = [:]
    // Строка «Итого»
    def totalRow = null
    // Инициализация сумм и разбор строк
    for (def row in dataRows) {
        if (row.getAlias() != null && row.getAlias() != 'total') {
            tmp.add(row)
            staticSum.put(row.getAlias(), 0 as BigDecimal)
        } else if (row.getAlias() == 'total') {
            totalRow = row
        }
    }
    dataRows = tmp
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
        if (source != null && source.state == WorkflowState.ACCEPTED && source.getFormType().getTaxType() == TaxType.VAT) {
            formDataService.getDataRowHelper(source).getAllCached().each { srcRow ->
                def srcAlias = srcRow.getAlias()
                if (srcAlias == null) {
                    dataRows.add(srcRow)
                } else if (srcAlias != 'total') {
                    staticSum.put(srcAlias, staticSum.get(srcAlias) + srcRow.sum)
                }
            }
        }
    }
    // Установка сумм для фиксированных строк
    for (def row in dataRows) {
        if (row.getAlias() != null) {
            row.sum = staticSum.get(row.getAlias())
        }
    }
    // Добавление строки «Итого»
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
    logger.info("Формирование консолидированной формы прошло успешно.")
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

void importData() {
    def tmpRow = formData.createDataRow()
    def xml = getXML(ImportInputStream, importService, UploadFileName, getColumnName(tmpRow, 'rowNum'), null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 3, 1)

    // TODO отделить шапку от фиксированных строк, но пока метода нет и сообщение не утвердили
    def headerMapping = [
            (xml.row[0].cell[0]) : getColumnName(tmpRow, 'rowNum'),
            (xml.row[0].cell[2]) : getColumnName(tmpRow, 'differences'),
            (xml.row[0].cell[3]) : getColumnName(tmpRow, 'sum'),
            (xml.row[1].cell[0]) : '1',
            (xml.row[1].cell[2]) : '2',
            (xml.row[1].cell[3]) : '3',
            (xml.row[2].cell[0]) : '1',
            (xml.row[2].cell[2]) : 'Суммы НДС, начисленные при безвозмездной передаче имущества (услуг), реализации по цене ниже рыночной',
            (xml.row[3].cell[0]) : '2',
            (xml.row[3].cell[2]) : 'Счета-фактуры, не зарегистрированные в книге покупок в момент реализации монет и слитков из драгоценных металлов, т.к. регистрация была произведена до 01.10.2003 г.',
            (xml.row[4].cell[0]) : '3',
            (xml.row[4].cell[2]) : 'Счета-фактуры, не зарегистрированы в книге покупок при передаче в эксплуатацию основных средств, НМА, инвентаря и принадлежностей, т.к. регистрация была произведена до 01.10.2003 г.',
            (xml.row[5].cell[0]) : '4',
            (xml.row[5].cell[2]) : 'В книге покупок отсутствуют счета-фактуры по монетам и слиткам из драгоценных металлов, т.к. реквизиты счетов-фактур не поступили из ЦА',
            (xml.row[6].cell[0]) : '5',
            (xml.row[6].cell[2]) : 'В книге покупок отсутствуют счета-фактуры по централизованным закупкам',
            (xml.row[7].cell[0]) : '6',
            (xml.row[7].cell[2]) : 'Счета-фактуры, не зарегистрированные в книге покупок при передаче в эксплуатацию основных средств, НМА, инвентаря и принадлежностей, т.к. своевременно не получены',
            (xml.row[8].cell[0]) : '7',
            (xml.row[8].cell[2]) : 'Счета-фактуры, не зарегистрированные в книге покупок при отнесении на расходы НДС по услугам, т.к. своевременно не получены',
            (xml.row[9].cell[0]) : '8',
            (xml.row[9].cell[2]) : 'Зарегистрированы в книге покупок счета-фактуры, полученные за предыдущие отчетные периоды',
            (xml.row[10].cell[0]) : '9',
            (xml.row[10].cell[2]) : 'Зарегистрированы в книге покупок счета-фактуры в случае изменения, либо расторжения договора и возврата сумм оплаты, частичной оплаты, полученной в счет предстоящего оказания услуг (поставки товаров, выполнения работ), передачи имущественных прав (ранее зарегистрированные в книге покупок)',
            (xml.row[11].cell[0]) : '10',
            (xml.row[11].cell[2]) : 'Суммы НДС, отраженные по дополнительным листам книги покупок',
            (xml.row[12].cell[0]) : '11',
            (xml.row[12].cell[2]) : 'Округления',
            (xml.row[13].cell[0]) : '12',
            (xml.row[13].cell[2]) : 'Исправительные обороты',
            (xml.row[14].cell[0]) : '13',
            (xml.row[14].cell[2]) : 'Прочие (расшифровать):'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def totalRow = getDataRow(dataRows, 'total')
    totalRow.sum = 0
    dataRows.remove(totalRow)
    dataRows.removeAll{ it.getAlias() == null }

    for (def row : xml.row) {
        xmlIndexRow++

        // Пропуск строк шапки
        if (xmlIndexRow < headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != '') {
            continue
        }

        def int xlsIndexRow = xmlIndexRow + rowOffset

        def newRow = null

        def rowIndex = xmlIndexRow - headRowCount + 1
        def isFixed = rowIndex <= 13
        if (isFixed) {
            newRow = getDataRow(dataRows, "R$rowIndex")
        } else {
            newRow = formData.createDataRow()
            editableColumns.each {
                newRow.getCell(it).editable = true
                newRow.getCell(it).setStyleAlias('Редактируемая')
            }
            newRow.setIndex(rowIndex)
        }

        if (!isFixed) {
            newRow.rowNum = parseNumber(row.cell[0].text(), xlsIndexRow, 0 + colOffset, logger, true)
            newRow.differences = row.cell[2].text()
        }
        newRow.sum = parseNumber(row.cell[3].text(), xlsIndexRow, 3 + colOffset, logger, true)

        if (!isFixed) {
            dataRows.add(newRow)
        }
    }
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}