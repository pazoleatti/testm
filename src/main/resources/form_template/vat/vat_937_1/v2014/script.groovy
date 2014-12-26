package form_template.vat.vat_937_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.ReportPeriod
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import groovy.transform.Field

/**
 * (937.1) Итоговые данные книги покупок
 * formTemplate = 606
 *
 * 1    period      Налоговый период
 * 2    bill        Всего счетов-фактур (шт.)
 * 3    dealNds     Всего покупок, включая НДС (руб.)
 * 4    deal_20     В том числе (руб.). Покупки, облагаемые налогом по ставке. 20%. Стоимость без НДС
 * 5    deal_20_Nds В том числе (руб.). Покупки, облагаемые налогом по ставке. 20%. Сумма НДС
 * 6    deal_18     В том числе (руб.). Покупки, облагаемые налогом по ставке. 18%. Стоимость без НДС
 * 7    deal_18_Nds В том числе (руб.). Покупки, облагаемые налогом по ставке. 18%. Сумма НДС
 * 8    deal_10     В том числе (руб.). Покупки, облагаемые налогом по ставке. 10%. Стоимость без НДС
 * 9    deal_10_Nds В том числе (руб.). Покупки, облагаемые налогом по ставке. 10%. Сумма НДС
 * 10   deal_0      В том числе (руб.). Покупки, облагаемые налогом по ставке. 0%
 * 11   deal        В том числе (руб.). Покупки, освобождаемые от налога
 * 12   nds         Сумма НДС, подлежащая вычету (руб.)
 * 13   diff        Расхождение (руб.)
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
def allColumns = ['period', 'bill', 'dealNds', 'deal_20', 'deal_20_Nds', 'deal_18', 'deal_18_Nds',
                  'deal_10', 'deal_10_Nds', 'deal_0', 'deal', 'nds', 'diff']
@Field
def calcColumns = ['bill', 'dealNds', 'deal_20', 'deal_20_Nds', 'deal_18', 'deal_18_Nds',
                   'deal_10', 'deal_10_Nds', 'deal_0', 'deal']
@Field
def totalANonEmptyColumns = ['bill', 'dealNds', 'deal_20', 'deal_20_Nds', 'deal_18', 'deal_18_Nds', 'deal_10', 'deal_10_Nds', 'nds', 'diff']

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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalPeriod = getDataRow(dataRows, 'totalPeriod') // 4-я строка
    def totalAnnul = getDataRow(dataRows, 'totalAnnul') // 5-строка
    def totalB = getDataRow(dataRows, 'totalB') // 6-я строка

    def ReportPeriod reportPeriod = reportPeriodService.get(formData.reportPeriodId)
    // строка 4 графа 1
    def String code = "2${reportPeriod.order}-${reportPeriod.taxPeriod.year}"
    totalPeriod.period = code
    totalAnnul.period = "Аннулирование " + code
    totalB.period = "Всего " + code
    dataRowHelper.update(dataRows)
}

void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalA = getDataRow(dataRows, 'totalA') // 2-я строка
    def totalPeriod = getDataRow(dataRows, 'totalPeriod') // 4-я строка
    def totalAnnul = getDataRow(dataRows, 'totalAnnul') // 5-строка
    def totalB = getDataRow(dataRows, 'totalB') // 6-я строка

    // строка 2 «Графа 13» = По строке 2 («Графа 12» - «Графа 5» - «Графа 7» - «Графа 9»)
    def diff = (totalA.nds ?: 0) - (totalA.deal_20_Nds ?: 0) - (totalA.deal_18_Nds ?: 0) - (totalA.deal_10_Nds ?: 0)
    checkOverflow(diff, totalA, 'diff', totalA.getIndex(), sizeDiff, '«Графа 12» - «Графа 5» - «Графа 7» - «Графа 9»')
    totalA.diff = diff

    // строка 6 графы с 2 по 11
    calcColumns.each {
        totalB[it] = (totalPeriod[it] ?: 0) - (totalAnnul[it] ?: 0)
    }
    dataRowHelper.update(dataRows)
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalA = getDataRow(dataRows, 'totalA') // 2-я строка
    def totalPeriod = getDataRow(dataRows, 'totalPeriod') // 4-я строка
    def totalAnnul = getDataRow(dataRows, 'totalAnnul') // 5-строка
    def totalB = getDataRow(dataRows, 'totalB') // 6-я строка

    // 1. Обязательность заполнения:
    //	Графы 2-9, 12-14 строки 2;
    //	Графы 2-11 строк 4, 5
    checkNonEmptyColumns(totalA, totalA.getIndex(), totalANonEmptyColumns, logger, true)
    checkNonEmptyColumns(totalPeriod, totalPeriod.getIndex(), calcColumns, logger, true)
    checkNonEmptyColumns(totalAnnul, totalAnnul.getIndex(), calcColumns, logger, true)
    // 2-4. По строкам 2, 4, 5:
    // «Графа 5» = «Графа 4» * 20 / 100
    // «Графа 7» = «Графа 6» * 18 / 100
    // «Графа 9» = «Графа 8» * 10 / 100
    for (def row in [totalA, totalPeriod, totalAnnul]) {
        def errorMsg = "Строка ${row.getIndex()}: "
        if (row.deal_20 != null && row.deal_20_Nds != row.deal_20 * 0.2) {
            rowWarning(logger, row, errorMsg + "Сумма НДС, облагаемая по ставке 20%, неверная!")
        }
        if (row.deal_18 != null && row.deal_18_Nds != row.deal_18 * 0.18) {
            rowWarning(logger, row, errorMsg + "Сумма НДС, облагаемая по ставке 18%, неверная!")
        }
        if (row.deal_10 != null && row.deal_10_Nds != row.deal_10 * 0.1) {
            rowWarning(logger, row, errorMsg + "Сумма НДС, облагаемая по ставке 10%, неверная!")
        }
    }
    // 5. По строке 2:
    // «Графа 3» = «Графа 4» + «Графа 5» + «Графа 6» + «Графа 7» + «Графа 8» + «Графа 9»
    if (totalA.deal_20 != null && totalA.deal_20_Nds != null && totalA.deal_18 != null && totalA.deal_18_Nds != null && totalA.deal_10 != null && totalA.deal_10_Nds != null &&
            totalA.dealNds != totalA.deal_20 + totalA.deal_20_Nds + totalA.deal_18 + totalA.deal_18_Nds + totalA.deal_10 + totalA.deal_10_Nds) {
        logger.warn("Строка ${totalA.getIndex()}: " + "Сумма покупок по разделу «А» неверная!")
    }
    // 6. По строке 6:
    // «Графа 3» = «Графа 4» + «Графа 5» + «Графа 6» + «Графа 7» + «Графа 8» + «Графа 9» + «Графа 10» + «Графа 11»
    if (totalB.deal_20 != null && totalB.deal_20_Nds != null && totalB.deal_18 != null && totalB.deal_18_Nds != null && totalB.deal_10 != null && totalB.deal_10_Nds != null && totalB.deal_0 != null && totalB.deal != null &&
            totalB.dealNds != totalB.deal_20 + totalB.deal_20_Nds + totalB.deal_18 + totalB.deal_18_Nds + totalB.deal_10 + totalB.deal_10_Nds + totalB.deal_0 + totalB.deal) {
        logger.warn("Строка ${totalB.getIndex()}: " + "Сумма покупок по разделу «Б» неверная!")
    }
    // 7. По строке 2:
    // «Графа 14» = «Графа 12» + «Графа 13» - «Графа 5» - «Графа 7» - «Графа 9»
    if (totalA.nds != null && totalA.deal_20_Nds != null && totalA.deal_18_Nds != null && totalA.deal_10_Nds != null &&
            totalA.diff != totalA.nds - totalA.deal_20_Nds - totalA.deal_18_Nds - totalA.deal_10_Nds) {
        logger.error("Строка ${totalA.getIndex()}: " + "Неверно рассчитана графа «Расхождение (руб.)»!")
    }
    // 8. Если существует экземпляр налоговой формы 937.1.13, чье подразделение и  налоговый период,
    // соответствуют подразделению и налоговому периоду формы 937.1, то:
    //      a.	Выполняется проверка: «Графа 13» строки 2 формы 937.1 = «Графа 3» итоговой строки – «Графа 3» строки 2 (форма 937.1.13).
    //      b.	Если результат данной проверки неуспешный, то выдается сообщение об ошибке №2
    // Иначе если «Графа 13» (форма 937.1) <> 0 и экземпляр налоговой формы 937.1.13,
    // чье подразделение и  налоговый период, соответствуют подразделению и налоговому периоду формы 937.1,
    // не существует, то выдается сообщение об ошибке №1
    def appFormData = formDataService.getLast(607, formData.kind, formData.departmentId, formData.reportPeriodId, formData.periodOrder)
    if (appFormData) {
        def appDataRows = formDataService.getDataRowHelper(appFormData)?.allCached
        if (appDataRows) {
            def appOtherRow = getDataRow(appDataRows, 'R2{wan}')
            def appTotalRow = getDataRow(appDataRows, 'total')
            if (appTotalRow.sum == null || appOtherRow?.sum == null || totalA.diff != (appTotalRow.sum - appOtherRow.sum)) {
                logger.warn("Сумма расхождения не соответствует расшифровке! ")
            }
        }
    } else if (totalA.diff != 0) {
        logger.warn("Экземпляр налоговой формы 937.1.13 «Расшифровка графы 13» за период %s - %s не существует (отсутствуют первичные данные для проверки)!",
                getReportPeriodStartDate().format(dateFormat), getReportPeriodEndDate().format(dateFormat))
    }
    // 9. «Графа N» строки 6 = Графа N строки 4 - Графа N строки 5, где N = 2, 3, 4, 5, 6, 7, 8, 9, 10 или 11
    calcColumns.each {
        if (totalPeriod[it] != null &&  totalAnnul[it] != null &&
                totalB[it] != totalPeriod[it] - totalAnnul[it]) {
            logger.error("Строка ${totalB.getIndex()}: " + "Итоговые значения рассчитаны неверно в графе «${getColumnName(totalB, it)}»!")
        }
    }
}

void consolidation() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def totalA = getDataRow(dataRows, 'totalA') // 2-я строка
    def totalPeriod = getDataRow(dataRows, 'totalPeriod') // 4-я строка
    def totalAnnul = getDataRow(dataRows, 'totalAnnul') // 5-строка
    def totalB = getDataRow(dataRows, 'totalB') // 6-я строка

    //очистить форму
    totalANonEmptyColumns.each {
        totalA[it] = 0
    }
    calcColumns.each {
        totalPeriod[it] = 0
        totalAnnul[it] = 0
        totalB[it] = 0
    }

    for (formDataSource in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind(),
            getCalendarStartDate(), getReportPeriodEndDate())) {
        if (formDataSource.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.getLast(formDataSource.formTypeId, formDataSource.kind, formDataSource.departmentId, formData.reportPeriodId, formData.periodOrder)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceForm = formDataService.getDataRowHelper(source)
                addRowsToRows(dataRows, sourceForm.allCached)
            }
        }
    }
    dataRowHelper.save(dataRows)
}

void addRowsToRows(def dataRows, def addRows) {
    def totalA = getDataRow(dataRows, 'totalA') // 2-я строка
    def totalPeriod = getDataRow(dataRows, 'totalPeriod') // 4-я строка
    def totalAnnul = getDataRow(dataRows, 'totalAnnul') // 5-строка
    def totalB = getDataRow(dataRows, 'totalB') // 6-я строка

    def addA = getDataRow(addRows, 'totalA') // 2-я строка
    def addPeriod = getDataRow(addRows, 'totalPeriod') // 4-я строка
    def addAnnul = getDataRow(addRows, 'totalAnnul') // 5-строка
    def addB = getDataRow(addRows, 'totalB') // 6-я строка

    totalANonEmptyColumns.each {
        totalA[it] += addA[it]
    }
    calcColumns.each {
        totalPeriod[it] += addPeriod[it]
        totalAnnul[it] += addAnnul[it]
        totalB[it] += addB[it]
    }
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

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'Налоговый период', null, 13, 4)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 13, 4)

    def headerMapping = [
            (xml.row[0].cell[0]) : 'Налоговый период',
            (xml.row[0].cell[1]) : 'Всего счетов-фактур (шт.)',
            (xml.row[0].cell[2]) : 'Всего покупок, включая НДС (руб.)',
            (xml.row[0].cell[3]) : 'В том числе (руб.)',
            (xml.row[0].cell[11]) : 'Сумма НДС, подлежащая вычету (руб.)',
            (xml.row[0].cell[12]) : 'Расхождение (руб.)',
            (xml.row[1].cell[3]) : 'покупки, облагаемые налогом по ставке',
            (xml.row[1].cell[10]) : 'покупки, освобождаемые от налога',
            (xml.row[2].cell[3]) : '20%',
            (xml.row[2].cell[5]) : '18%',
            (xml.row[2].cell[7]) : '10%',
            (xml.row[2].cell[9]) : '0%',
            (xml.row[3].cell[3]) : 'стоимость без НДС',
            (xml.row[3].cell[4]) : 'сумма НДС',
            (xml.row[3].cell[5]) : 'стоимость без НДС',
            (xml.row[3].cell[6]) : 'сумма НДС',
            (xml.row[3].cell[7]) : 'стоимость без НДС',
            (xml.row[3].cell[8]) : 'сумма НДС'
    ]
    (0..12).each { index ->
        headerMapping.put((xml.row[4].cell[index]), (index+1).toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 4)
}

void addData(def xml, int headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    for (int i in [2, 4, 5, 6]) {
        def row = xml.row[headRowCount + i]
        def int xlsIndexRow = rowOffset + headRowCount + i

        dataRows[i - 1].setImportIndex(xlsIndexRow)

        // графа 1
        def xmlIndexCol = 0
        if (i != 2) { // пропускаем вторую строку
            dataRows[i - 1].period = row.cell[xmlIndexCol].text()
        }

        // графа 2
        xmlIndexCol = 1
        dataRows[i - 1].bill = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 3
        xmlIndexCol = 2
        dataRows[i - 1].dealNds = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 4
        xmlIndexCol = 3
        dataRows[i - 1].deal_20 = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 5
        xmlIndexCol = 4
        dataRows[i - 1].deal_20_Nds = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 6
        xmlIndexCol = 5
        dataRows[i - 1].deal_18 = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 7
        xmlIndexCol = 6
        dataRows[i - 1].deal_18_Nds = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 8
        xmlIndexCol = 7
        dataRows[i - 1].deal_10 = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 9
        xmlIndexCol = 8
        dataRows[i - 1].deal_10_Nds = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 10
        xmlIndexCol = 9
        dataRows[i - 1].deal_0 = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 11
        xmlIndexCol = 10
        dataRows[i - 1].deal = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 12
        xmlIndexCol = 11
        dataRows[i - 1].nds = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)

        // графа 13
        xmlIndexCol = 12
        dataRows[i - 1].diff = getNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset)
    }
}

void importTransportData() {
    def xml = getTransportXML(ImportInputStream, importService, UploadFileName, 13, 0)
    addTransportData(xml)
}

void addTransportData(def xml) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    def int rnuIndexRow = 2
    def int colOffset = 1

    for (int i in [2, 4, 5, 6]) {
        rnuIndexRow++
        def row = xml.row[i - 1]

        // графа 1
        def xmlIndexCol = 1
        if (i != 2) { // пропускаем вторую строку
            dataRows[i - 1].period = row.cell[xmlIndexCol].text()
        }

        // графа 2
        xmlIndexCol = 2
        dataRows[i - 1].bill = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 3
        xmlIndexCol = 3
        dataRows[i - 1].dealNds = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 4
        xmlIndexCol = 4
        dataRows[i - 1].deal_20 = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 5
        xmlIndexCol = 5
        dataRows[i - 1].deal_20_Nds = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 6
        xmlIndexCol = 6
        dataRows[i - 1].deal_18 = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 7
        xmlIndexCol = 7
        dataRows[i - 1].deal_18_Nds = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 8
        xmlIndexCol = 8
        dataRows[i - 1].deal_10 = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 9
        xmlIndexCol = 9
        dataRows[i - 1].deal_10_Nds = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 10
        xmlIndexCol = 10
        dataRows[i - 1].deal_0 = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 11
        xmlIndexCol = 11
        dataRows[i - 1].deal = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 12
        xmlIndexCol = 12
        dataRows[i - 1].nds = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)

        // графа 13
        xmlIndexCol = 13
        dataRows[i - 1].diff = getNumber(row.cell[xmlIndexCol].text(), rnuIndexRow, xmlIndexCol + colOffset)
    }
    dataRowHelper.save(dataRows)
}

