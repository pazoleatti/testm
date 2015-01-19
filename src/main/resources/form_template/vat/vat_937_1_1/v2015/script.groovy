package form_template.vat.vat_937_1_1.v2015

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * (937.1.1 v2015) Сведения из дополнительных листов книги покупок
 * formTemplate = 616
 *
 * 1  rowNum                      № п/п
 * 2  typeCode 		              Код вида операции
 * 3  invoice                     Номер и дата счета-фактуры продавца
 * 4  invoiceCorrecting           Номер и дата исправления счета-фактуры продавца
 * 5  invoiceCorrection           Номер и дата корректировочного счета-фактуры продавца
 * 6  invoiceCorrectingCorrection Номер и дата исправления корректировочного счета-фактуры продавца
 * 7  documentPay                 Номер и дата документа, подтверждающего уплату налога
 * 8  dateRegistration            Дата принятия на учет товаров (работ, услуг), имущественных прав
 * 9  salesman                    Наименование продавца
 * 10 salesmanInnKpp              ИНН/КПП продавца
 * 11 agentName                   Сведения о посреднике (комиссионере, агенте). Наименование посредника
 * 12 agentInnKpp                 Сведения о посреднике (комиссионере, агенте). ИНН/КПП посредника
 * 13 declarationNum              Номер таможенной декларации
 * 14 currency                    Наименование и код валюты
 * 15 cost                        Стоимость покупок по счету-фактуре, разница стоимости по корректировочному счету-фактуре (включая НДС) в валюте счета-фактуры
 * 16 nds                         Сумма НДС по счету-фактуре, разница суммы НДС по корректировочному счету-фактуре, принимаемая к вычету, в рублях и копейках
 */


switch (formDataEvent) {
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.AFTER_CREATE:
        calcAfterCreate()
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
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
}

@Field
def allColumns = ['rowNum', 'typeCode', 'invoice', 'invoiceCorrecting', 'invoiceCorrection', 'invoiceCorrectingCorrection', 'documentPay', 'dateRegistration',
                  'salesman', 'salesmanInnKpp', 'agentName', 'agentInnKpp', 'declarationNum', 'currency', 'cost', 'nds']

// TODO: Заполнить после того, как будут известны поля
@Field
def calcColumns = []
@Field
def totalANonEmptyColumns = ['rowNum', 'typeCode', 'invoice', 'invoiceCorrection', 'cost', 'nds']

//TODO: Уточнить данное значение
@Field
def sizeDiff = 15

// Дата начала отчетного периода
@Field
def startDate = null

@Field
def calendarStartDate = null

// Дата окончания отчетного периода
@Field
def endDate = null

@Field
def dateFormat = 'dd.MM.yyyy'

// Получение числа из строки при импорте
def getNumber(def value, def indexRow, def indexCol) {
    return parseNumber(value, indexRow, indexCol, logger, true)
}

void calcAfterCreate() {
    // TODO: Реализовать метод после того, как будет известна логика
}

void calc() {
    // TODO: Реализовать метод после того, как будет известна логика
}

void logicCheck() {
    // TODO: Реализовать метод после того, как будет известна логика
}

void consolidation() {
    // TODO: Реализовать метод после того, как будет известна логика
}

void addRowsToRows(def dataRows, def addRows) {
    // TODO: Реализовать метод при необходимости после того, как будет известна логика
}

def getReportPeriodStartDate() {
    if (startDate == null) {
        startDate = reportPeriodService.getStartDate(formData.reportPeriodId).time
    }
    return startDate
}

def getCalendarStartDate() {
    if (!calendarStartDate) {
        calendarStartDate = reportPeriodService.getCalendarStartDate(formData.reportPeriodId).time
    }
    return calendarStartDate
}

def getReportPeriodEndDate() {
    if (endDate == null) {
        endDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    }
    return endDate
}

// TODO: При необходимости исправить данный метод после получения постановки
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'Налоговый период', null, 13, 4)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 13, 4)

    def headerMapping = [
            (xml.row[0].cell[0]) : '№ п/п',
            (xml.row[0].cell[1]) : 'Код вида операции',
            (xml.row[0].cell[2]) : 'Номер и дата счета-фактуры продавца',
            (xml.row[0].cell[3]) : 'Номер и дата исправления счета-фактуры продавца',
            (xml.row[0].cell[4]): 'Номер и дата корректировочного счета-фактуры продавца',
            (xml.row[0].cell[5]): 'Номер и дата исправления корректировочного счета-фактуры продавца',
            (xml.row[0].cell[6]) : 'Номер и дата документа, подтверждающего уплату налога',
            (xml.row[0].cell[7]): 'Дата принятия на учет товаров (работ, услуг), имущественных прав',
            (xml.row[0].cell[8]) : 'Наименование продавца',
            (xml.row[0].cell[9]) : 'ИНН/КПП продавца',
            (xml.row[0].cell[10]) : 'Сведения о посреднике (комиссионере, агенте)',
            (xml.row[0].cell[12]) : 'Номер таможенной декларации',
            (xml.row[0].cell[13]) : 'Наименование и код валюты',
            (xml.row[0].cell[14]) : 'Стоимость покупок по счету-фактуре, разница стоимости по корректировочному счету-фактуре (включая НДС) в валюте счета-фактуры',
            (xml.row[0].cell[15]) : 'Сумма НДС по счету-фактуре, разница суммы НДС по корректировочному счету-фактуре, принимаемая к вычету, в рублях и копейках',
            (xml.row[1].cell[10]) : 'Наименование посредника',
            (xml.row[1].cell[11]) : 'ИНН/КПП посредника',
    ]
    (0..15).each { index ->
        headerMapping.put((xml.row[4].cell[index]), (index + 1).toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 4)
}

void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    // TODO: Исправить данный цикл после получения постановки
    for (int i in [2, 4, 5, 6]) {
        def row = xml.row[headRowCount + i]
        def int xlsIndexRow = rowOffset + headRowCount + i

        dataRows[i - 1].setImportIndex(xlsIndexRow)

        // графа 1
        def xmlIndexCol = 0
        if (i != 2) { // пропускаем вторую строку
            dataRows[i - 1].rowNum = row.cell[xmlIndexCol].text()
        }

        // графа 2
        xmlIndexCol = 1
        dataRows[i - 1].typeCode = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 3
        xmlIndexCol = 2
        dataRows[i - 1].invoice = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 4
        xmlIndexCol = 3
        dataRows[i - 1].invoiceCorrecting = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 5
        xmlIndexCol = 4
        dataRows[i - 1].invoiceCorrection = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 6
        xmlIndexCol = 5
        dataRows[i - 1].invoiceCorrectingCorrection = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 7
        xmlIndexCol = 6
        dataRows[i - 1].documentPay = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 8
        xmlIndexCol = 7
        dataRows[i - 1].dateRegistration = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 9
        xmlIndexCol = 8
        dataRows[i - 1].salesman = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 10
        xmlIndexCol = 9
        dataRows[i - 1].salesmanInnKpp = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 11
        xmlIndexCol = 10
        dataRows[i - 1].agentName = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 12
        xmlIndexCol = 11
        dataRows[i - 1].agentInnKpp = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 13
        xmlIndexCol = 12
        dataRows[i - 1].declarationNum = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 14
        xmlIndexCol = 13
        dataRows[i - 1].currency = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 15
        xmlIndexCol = 14
        dataRows[i - 1].cost = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 16
        xmlIndexCol = 15
        dataRows[i - 1].nds = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
    }
}

// TODO: После получения постановки при необходимости удалить данный метод
void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 16, 0)
    addTransportData(xml)
}

// TODO: После получения постановки при необходимости исправить или удалить данный метод
void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def int rnuIndexRow = 2
    def int colOffset = 1

    for (int i in [2, 4, 5, 6]) {
        rnuIndexRow++
        def row = xml.row[i - 1]

        // графа 1
        def xmlIndexCol = 0
        if (i != 2) { // пропускаем вторую строку
            dataRows[i - 1].rowNum = row.cell[xmlIndexCol].text()
        }

        // графа 2
        xmlIndexCol = 1
        dataRows[i - 1].typeCode = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 3
        xmlIndexCol = 2
        dataRows[i - 1].invoice = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 4
        xmlIndexCol = 3
        dataRows[i - 1].invoiceCorrecting = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 5
        xmlIndexCol = 4
        dataRows[i - 1].invoiceCorrection = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 6
        xmlIndexCol = 5
        dataRows[i - 1].invoiceCorrectingCorrection = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 7
        xmlIndexCol = 6
        dataRows[i - 1].documentPay = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 8
        xmlIndexCol = 7
        dataRows[i - 1].dateRegistration = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 9
        xmlIndexCol = 8
        dataRows[i - 1].salesman = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 10
        xmlIndexCol = 9
        dataRows[i - 1].salesmanInnKpp = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 11
        xmlIndexCol = 10
        dataRows[i - 1].agentName = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 12
        xmlIndexCol = 11
        dataRows[i - 1].agentInnKpp = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 13
        xmlIndexCol = 12
        dataRows[i - 1].declarationNum = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 14
        xmlIndexCol = 13
        dataRows[i - 1].currency = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 15
        xmlIndexCol = 14
        dataRows[i - 1].cost = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 16
        xmlIndexCol = 15
        dataRows[i - 1].nds = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
    }
    dataRowHelper.save(dataRows)
}


