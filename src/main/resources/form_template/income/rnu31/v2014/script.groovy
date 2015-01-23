package form_template.income.rnu31.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * Форма "(РНУ-31) Регистр налогового учёта процентного дохода по купонным облигациям".
 * formTemplateId=1328
 *
 * @author rtimerbaev
 */

// графа 1  - number
// графа 2  - securitiesType
// графа 3  - ofz
// графа 4  - municipalBonds
// графа 5  - governmentBonds
// графа 6  - mortgageBonds
// графа 7  - municipalBondsBefore
// графа 8  - rtgageBondsBefore
// графа 9  - ovgvz
// графа 10 - eurobondsRF
// графа 11 - itherEurobonds
// графа 12 - corporateBonds

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        prevPeriodCheck()
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        prevPeriodCheck()
        calc()
        logicCheck()
        break
    case FormDataEvent.ADD_ROW :
        // Всего форма должна содержать одну строку
        break
    case FormDataEvent.DELETE_ROW :
        // Всего форма должна содержать одну строку
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        prevPeriodCheck()
        logicCheck()
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT :
        if (UploadFileName.endsWith(".rnu")) {
            importTransportData()
        } else {
            importData()
            calc()
            logicCheck()
        }
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
}

// Редактируемые атрибуты (графа 3..12)
@Field
def editableColumns = ['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds', 'municipalBondsBefore',
        'rtgageBondsBefore', 'ovgvz', 'eurobondsRF', 'itherEurobonds', 'corporateBonds']

// Проверяемые на пустые значения атрибуты (графа 3..12)
@Field
def nonEmptyColumns = editableColumns

@Field
def totalSumColumns = editableColumns

@Field
def startDate = null

@Field
def endDate = null

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getMonthStartDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return startDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getMonthEndDate(formData.reportPeriodId, formData.periodOrder).time
    }
    return endDate
}

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper?.allCached
    def row = getDataRow(dataRows, 'total')
    row.number = formData.periodOrder
    dataRowHelper.update(dataRows)
}

void logicCheck() {
    if (formData.periodOrder == null) {
        throw new ServiceException("Месячная форма создана как квартальная!")
    }
    def dataRows = formDataService.getDataRowHelper(formData)?.allCached
    // строка из текущего отчета
    def row = getDataRow(dataRows, 'total')

    // 1. Обязательность заполнения полей графы 3..12
    checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

    // 2. Проверка на уникальность поля «№ п.п»
    if (formData.periodOrder != row.number) {
        rowError(logger, row, "Нарушена уникальность номера по порядку!")
    }

    // 3-12. Проверка процентного (купонного) дохода по виду ценной бумаги
    if (formData.periodOrder != 1 && formData.kind == FormDataKind.PRIMARY) {
        // строка из предыдущего отчета
        def rowOld = getPrevMonthTotalRow()
        if (rowOld != null) {
            for (def column : editableColumns) {
                if (row.getCell(column).value != null
                        && rowOld.getCell(column).value != null
                        && row.getCell(column).value < rowOld.getCell(column).value) {
                    def msg = "Процентный (купонный) доход по «${getColumnName(row, column)}» уменьшился!"
                    // нефатальная для графы 5, 9, 10, 11
                    if (column in ['governmentBonds', 'ovgvz', 'eurobondsRF', 'itherEurobonds']) {
                        rowWarning(logger, row, msg)
                    } else {
                        rowError(logger, row, msg)
                    }
                }
            }
        }
    }

    // 14-22. Проверка на неотрицательные значения
    for (def column : ['ofz', 'municipalBonds', 'mortgageBonds', 'municipalBondsBefore', 'rtgageBondsBefore', 'corporateBonds']) {
        def value = row.getCell(column).value
        if (value != null && value < 0) {
            def columnName = getColumnName(row, column)
            rowError(logger, row, "Значение графы «$columnName» по строке 1 отрицательное!")
        }
    }
    for (def column : ['governmentBonds','ovgvz','eurobondsRF','itherEurobonds']) {
        def value = row.getCell(column).value
        if (value != null && value < 0) {
            def columnName = getColumnName(row, column)
            rowWarning(logger, row, "Значение графы «$columnName» по строке 1 отрицательное!")
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    // занулить данные и просуммировать из источников
    def row = getDataRow(dataRows, 'total')
    editableColumns.each { alias ->
        row.getCell(alias).setValue(0, row.getIndex())
    }

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.formType.id, formData.kind,
            getReportPeriodStartDate(), getReportPeriodEndDate()).each {
        if (it.formTypeId == formData.formType.id) {
            def sourceFormData = formDataService.getLast(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (sourceFormData != null && sourceFormData.state == WorkflowState.ACCEPTED) {
                def sourceDataRows = formDataService.getDataRowHelper(sourceFormData)?.allCached
                def sourceRow = getDataRow(sourceDataRows, 'total')
                editableColumns.each { alias ->
                    row.getCell(alias).setValue(sourceRow.getCell(alias).value + row.getCell(alias).getValue(), row.getIndex())
                }
            }
        }
    }
    dataRowHelper.save(dataRows)
}

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 5, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[1]): 'Ставка налога на прибыль',
            (xml.row[0].cell[2]): '15%',
            (xml.row[0].cell[6]): '9%',
            (xml.row[0].cell[8]): '0%',
            (xml.row[0].cell[9]): '20%',
            (xml.row[1].cell[1]): 'Вид ценных бумаг',
            (xml.row[1].cell[2]): 'ОФЗ',
            (xml.row[1].cell[3]): 'Субфедеральные и муниципальные облигации, за исключением муниципальных облигаций, выпущенных до 1 января 2007 года на срок не менее 3 лет',
            (xml.row[1].cell[4]): 'Государственные облигации Республики Беларусь',
            (xml.row[1].cell[5]): 'Ипотечные облигации, выпущенные после 1 января 2007 года',
            (xml.row[1].cell[6]): 'Муниципальные облигации, выпущенные до 1 января 2007 года на срок не менее 3 лет',
            (xml.row[1].cell[7]): 'Ипотечные облигации, выпущенные до 1 января 2007 года',
            (xml.row[1].cell[8]): 'ОВГВЗ',
            (xml.row[1].cell[9]): 'Еврооблигации РФ',
            (xml.row[1].cell[10]): 'Прочие еврооблигации',
            (xml.row[1].cell[11]): 'Корпоративные облигации',
            (xml.row[2].cell[0]): '1',
            (xml.row[2].cell[1]): '2',
            (xml.row[2].cell[2]): '3',
            (xml.row[2].cell[3]): '4',
            (xml.row[2].cell[4]): '5',
            (xml.row[2].cell[5]): '6',
            (xml.row[2].cell[6]): '7',
            (xml.row[2].cell[7]): '8',
            (xml.row[2].cell[8]): '9',
            (xml.row[2].cell[9]): '10',
            (xml.row[2].cell[10]): '11',
            (xml.row[2].cell[11]): '12'
    ]

    checkHeaderEquals(headerMapping)
    addData(xml, 3)
}

// Заполнить форму данными
def addData(def xml, int headRowCount) {
    if (xml.row.size() > 0) {
        def xmlRow = xml.row[headRowCount]
        def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
        def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()
        def xmlIndexCol = 2
        def int xlsIndexRow = 1 + rowOffset
        def dataRowHelper = formDataService.getDataRowHelper(formData)
        def dataRows = dataRowHelper.allCached
        def row = getDataRow(dataRows, 'total')
        row.setImportIndex(xlsIndexRow)

        // графа 3
        row.ofz = getNumber(xmlRow.cell[xmlIndexCol++].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // графа 4
        row.municipalBonds = getNumber(xmlRow.cell[xmlIndexCol++].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // графа 5
        row.governmentBonds = getNumber(xmlRow.cell[xmlIndexCol++].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // графа 6
        row.mortgageBonds = getNumber(xmlRow.cell[xmlIndexCol++].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // графа 7
        row.municipalBondsBefore = getNumber(xmlRow.cell[xmlIndexCol++].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // графа 8
        row.rtgageBondsBefore = getNumber(xmlRow.cell[xmlIndexCol++].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // графа 9
        row.ovgvz = getNumber(xmlRow.cell[xmlIndexCol++].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // графа 10
        row.eurobondsRF = getNumber(xmlRow.cell[xmlIndexCol++].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // графа 11
        row.itherEurobonds = getNumber(xmlRow.cell[xmlIndexCol++].text(), xlsIndexRow, xmlIndexCol + colOffset)
        // графа 12
        row.corporateBonds = getNumber(xmlRow.cell[xmlIndexCol++].text(), xlsIndexRow, xmlIndexCol + colOffset)

        dataRowHelper.save(dataRows)
    }
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 12, 0)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def int rnuIndexRow = 3
    def int colOffset = 1
    def row = getDataRow(dataRows, 'total')

    def xmlRow = xml.row[0]

    // графа 3
    row.ofz = getNumber(xmlRow.cell[3].text(), rnuIndexRow, 3 + colOffset)
    // графа 4
    row.municipalBonds = getNumber(xmlRow.cell[4].text(), rnuIndexRow, 4 + colOffset)
    // графа 5
    row.governmentBonds = getNumber(xmlRow.cell[5].text(), rnuIndexRow, 5 + colOffset)
    // графа 6
    row.mortgageBonds = getNumber(xmlRow.cell[6].text(), rnuIndexRow, 6 + colOffset)
    // графа 7
    row.municipalBondsBefore = getNumber(xmlRow.cell[7].text(), rnuIndexRow, 7 + colOffset)
    // графа 8
    row.rtgageBondsBefore = getNumber(xmlRow.cell[8].text(), rnuIndexRow, 8 + colOffset)
    // графа 9
    row.ovgvz = getNumber(xmlRow.cell[9].text(), rnuIndexRow, 9 + colOffset)
    // графа 10
    row.eurobondsRF = getNumber(xmlRow.cell[10].text(), rnuIndexRow, 10 + colOffset)
    // графа 11
    row.itherEurobonds = getNumber(xmlRow.cell[11].text(), rnuIndexRow, 11 + colOffset)
    // графа 12
    row.corporateBonds = getNumber(xmlRow.cell[12].text(), rnuIndexRow, 12 + colOffset)

    dataRowHelper.save(dataRows)

}

// Получить строку за прошлый месяц
def getPrevMonthTotalRow() {
    // проверка на январь и если не задан месяц формы
    if (formData.periodOrder == null || formData.periodOrder == 1) {
        return null
    }
    def prevFormData = formDataService.getFormDataPrev(formData)
    if (prevFormData != null) {
        def prevDataRows = formDataService.getDataRowHelper(prevFormData)?.allCached
        return getDataRow(prevDataRows, 'total')
    }
    return null
}

void prevPeriodCheck() {
    // Проверка наличия формы за предыдущий период начиная с отчета за 2-й отчетный период,
    // т.е. проверка отчёта за январь не осуществляется
    if (formData.periodOrder != 1 && formData.kind == FormDataKind.PRIMARY) {
        formDataService.checkMonthlyFormExistAndAccepted(formData.formType.id, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, formData.periodOrder, true, logger, true)
    }
}