package form_template.income.output1_2.v2014

import au.com.bytecode.opencsv.CSVReader
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.util.StringUtils
import groovy.transform.Field

/**
 * Сведения для расчёта налога с доходов в виде дивидендов (03/А)
 * formTemplateId=1414
 * действует с 4 квартала 2014 года
 *
 * http://conf.aplana.com/pages/viewpage.action?pageId=8784122
 *
 * @author Bulat Kinzyabulatov
 *
 1		taCategory		            Категория налогового агента
 2		financialYear		        Отчетный год
 3		taxPeriod		            Налоговый (отчетный) период (код)
 4		emitent		                Эмитент
 5		inn		                    ИНН организации – эмитента ценных бумаг
 6		decreeNumber		        Номер решения о распределении доходов от долевого участия
 7		dividendType		        Вид дивидендов
 8		totalDividend		        Общая сумма дивидендов, подлежащая распределению российской организацией в пользу своих получателей (Д1)
 9		dividendSumRaspredPeriod	Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Всего
 10		dividendRussianTotal		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – российским организациям. Всего
 11		dividendRussianStavka0		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – российским организациям. Налоговая ставка 0%
 12		dividendRussianStavka6		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – российским организациям. Налоговая ставка 9%
 13		dividendRussianStavka9		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – российским организациям. По иной ставке
 14		dividendRussianTaxFree		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – российским организациям. Распределяемые в пользу акционеров (участников), не являющихся налогоплательщиками
 15		dividendRussianPersonal		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода - физическим лицам, являющимся налоговыми резидентами России
 16		dividendForgeinOrgAll		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России. Организациям
 17		dividendForgeinPersonalAll	Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России. Физическим лицам
 18		dividendStavka0		        Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России. 0%
 19		dividendStavkaLess5		    Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России. До 5% включительно
 20		dividendStavkaMore5		    Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России. Свыше 5% до 10 % включительно
 21		dividendStavkaMore10		Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России. Свыше 10%
 22		dividendTaxUnknown		    Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде. Дивиденды, начисленные получателям дохода – организациям и физическим лицам, налоговый статус которых не установлен
 23		dividendNonIncome		    Дивиденды, перечисленные лицам, не являющимся получателями дохода
 24		dividendAgentAll		    Дивиденды, полученные. Всего
 25		dividendAgentWithStavka0	Дивиденды, полученные. В т. ч. без учета полученных дивидендов, налог с которых исчислен по ставке 0%
 26		dividendD1D2		        Сумма дивидендов, распределяемых в пользу всех получателей, уменьшенная на показатель строки 081 (Д1-Д2)
 27		dividendSumForTaxStavka9	Сумма дивидендов, используемых для исчисления налога по российским организациям. Налоговая ставка 9%
 28		dividendSumForTaxStavka0	Сумма дивидендов, используемых для исчисления налога по российским организациям. Налоговая ставка 0%
 29		taxSum		                Исчисленная сумма налога, подлежащая уплате в бюджет
 30		taxSumFromPeriod		    Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды
 31		taxSumLast		            Сумма налога, начисленная с дивидендов, выплаченных в последнем квартале (месяце) отчетного (налогового) периода - всего
 *
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
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT_TRANSPORT_FILE:
        importTransportData()
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
        break
}

@Field
def allColumns = ['taCategory', 'financialYear', 'taxPeriod', 'emitent', 'inn', 'decreeNumber', 'dividendType',
                  'totalDividend', 'dividendSumRaspredPeriod', 'dividendRussianTotal', 'dividendRussianStavka0',
                  'dividendRussianStavka6', 'dividendRussianStavka9', 'dividendRussianTaxFree',
                  'dividendRussianPersonal', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll', 'dividendStavka0',
                  'dividendStavkaLess5', 'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendTaxUnknown',
                  'dividendNonIncome', 'dividendAgentAll', 'dividendAgentWithStavka0', 'dividendD1D2',
                  'dividendSumForTaxStavka9', 'dividendSumForTaxStavka0', 'taxSum', 'taxSumFromPeriod', 'taxSumLast']

// обязательные поля (графа 1..4, 6..31)
@Field
def nonEmptyColumns = allColumns - 'inn'

// редактируемые поля (графа 1..31)
@Field
def editableColumns = allColumns

void calc() {
    // расчетов нет, все поля редактируемые
    sortFormDataRows()
}

void logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    for (def row in dataRows) {
        def rowNum = row.getIndex()
        def errorMsg = "Строка $rowNum: "

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)

        // 2. Проверка на заполнение «Графы 5»
        if ((row.taCategory == 2) != (row.inn != null && !row.inn.isEmpty())) {
            rowError(logger, row, errorMsg + "Графа «${getColumnName(row, 'inn')}» должна быть заполнена в случае если графа «${getColumnName(row, 'taCategory')}» равна «2»!")
        }

        // 3. Проверка допустимых значений «Графы 1»
        if (row.taCategory != 1 && row.taCategory != 2) {
            errorMessage(row, 'taCategory', errorMsg)
        }

        // 4. Проверка допустимых значений «Графы 3»
        if (!['13', '21', '31', '33', '34', '35', '36', '37', '38', '39', '40', '41', '42', '43',
                                 '44', '45', '46', '50'].contains(row.taxPeriod)) {
            errorMessage(row, 'taxPeriod', errorMsg)
        }

        // 5. Проверка допустимых значений «Графы 7»
        if (!['1', '2'].contains(row.dividendType)) {
            errorMessage(row, 'dividendType', errorMsg)
        }

        // 6. Проверка значения «Графы 1». Если «Графа 1» = «2», то «Графа 24» и «Графа 25» равны значению «0»
        if (row.taCategory == 2) {
            ['dividendAgentAll', 'dividendAgentWithStavka0'].each {
                if (row[it] != 0) {
                    errorMessage(row, it, errorMsg)
                }
            }
        }
        // 7. Проверка значения «Графы 26»
        if (row.dividendD1D2 < 0) {
            // графа 27..31
            ['dividendSumForTaxStavka9', 'dividendSumForTaxStavka0', 'taxSum', 'taxSumFromPeriod', 'taxSumLast'].each {
                if (row[it] != 0) {
                    errorMessage(row, it, errorMsg)
                }
            }
        }

        // 8. Проверка правильности расчета «Графы 9»
        if (row.dividendSumRaspredPeriod != calc9(row)) {
            warnMessage9or10(row, 'dividendSumRaspredPeriod', '«Графа 9» = «Графа 10» + «Графа 15» + «Графа 16» + «Графа 17» + «Графа 22»')
        }

        // 9. Проверка правильности расчета «Графы 10»
        if (row.dividendRussianTotal != calc10(row)) {
            warnMessage9or10(row, 'dividendRussianTotal', '«Графа 10» = «Графа 11» + «Графа 12» + «Графа 13» + «Графа 14»')
        }
    }
}

// «Графа 9» = «Графа 10» + «Графа 15» + «Графа16» + «Графа 17» + «Графа 22»
def calc9( def row) {
    def tmp = ['dividendRussianTotal', 'dividendRussianPersonal', 'dividendForgeinOrgAll',
            'dividendForgeinPersonalAll', 'dividendTaxUnknown'].sum { alias ->
        return (row[alias] ?: 0)
    }
    return roundValue(tmp)
}

// «Графа 10» = «Графа 11» + «Графа 12» + «Графа13» + «Графа 14»
def calc10( def row) {
    def tmp = ['dividendRussianStavka0', 'dividendRussianStavka6', 'dividendRussianStavka9',
            'dividendRussianTaxFree'].sum { alias ->
        return (row[alias] ?: 0)
    }
    return roundValue(tmp)
}

void errorMessage(def row, def alias, def errorMsg) {
    rowError(logger, row, errorMsg + "Графа «${getColumnName(row, alias)}» заполнена неверно!")
}

void warnMessage9or10(def row, def alias, def condition) {
    def index = row.getIndex()
    def name = getColumnName(row, alias)
    logger.warn("Строка $index: Графа «$name» заполнена неверно! Не выполняется условие: $condition")
}

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'Категория налогового агента', null, 31, 5)
    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 31, 5)
    def headerMapping = [
            (xml.row[0].cell[0]): 'Категория налогового агента',
            (xml.row[0].cell[1]): 'Отчетный год',
            (xml.row[0].cell[2]): 'Налоговый (отчетный) период (код)',
            (xml.row[0].cell[3]): 'Эмитент',
            (xml.row[0].cell[4]): 'ИНН организации – эмитента ценных бумаг',
            (xml.row[0].cell[5]): 'Номер решения о распределении доходов от долевого участия',
            (xml.row[0].cell[6]): 'Вид дивидендов',
            (xml.row[0].cell[7]): 'Общая сумма дивидендов, подлежащая распределению российской организацией в пользу своих получателей (Д1)',
            (xml.row[0].cell[8]): 'Сумма дивидендов, подлежащих выплате акционерам (участникам) в текущем налоговом периоде',
            (xml.row[0].cell[22]): 'Дивиденды, перечисленные лицам, не являющимся получателями дохода',
            (xml.row[0].cell[23]): 'Дивиденды, полученные',
            (xml.row[0].cell[25]): 'Сумма дивидендов, распределяемых в пользу всех получателей, уменьшенная на показатель строки 081 (Д1-Д2)',
            (xml.row[0].cell[26]): 'Сумма дивидендов, используемая для исчисления налога, по российским организациям:',
            (xml.row[0].cell[28]): 'Исчисленная сумма налога, подлежащая уплате в бюджет',
            (xml.row[0].cell[29]): 'Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды',
            (xml.row[0].cell[30]): 'Сумма налога, начисленная с дивидендов, выплаченных в последнем квартале (месяце) отчетного (налогового) периода - всего',

            (xml.row[1].cell[8]): 'всего',
            (xml.row[1].cell[9]): 'Дивиденды, начисленные получателям дохода – российским организациям',
            (xml.row[1].cell[14]): 'Дивиденды, начисленные получателям дохода - физическим лицам, являющимся налоговыми резидентами России',
            (xml.row[1].cell[15]): 'Дивиденды, начисленные получателям дохода – иностранным организациям и физическим лицам, не являющимся резидентами России',
            (xml.row[1].cell[21]): 'Дивиденды, начисленные получателям дохода – организациям и физическим лицам, налоговый статус которых не установлен',
            (xml.row[1].cell[23]): 'всего',
            (xml.row[1].cell[24]): 'в т.ч. без учета полученных дивидендов, налог с которых исчислен по ставке 0%',
            (xml.row[1].cell[26]): 'налоговая ставка 9%',
            (xml.row[1].cell[27]): 'налоговая ставка 0%',

            (xml.row[2].cell[9]): 'всего',
            (xml.row[2].cell[10]): 'налоговая ставка 0%',
            (xml.row[2].cell[11]): 'налоговая ставка 9%',
            (xml.row[2].cell[12]): 'по иной ставке',
            (xml.row[2].cell[13]): 'распределяемые в пользу акционеров (участников), не являющихся налогоплательщиками',
            (xml.row[2].cell[15]): 'организациям',
            (xml.row[2].cell[16]): 'физическим лицам',
            (xml.row[2].cell[17]): 'Из них налоги, с которых исчислены по ставке:',

            (xml.row[3].cell[17]): '0%',
            (xml.row[3].cell[18]): 'до 5% включительно',
            (xml.row[3].cell[19]): 'свыше 5% до 10 % включительно',
            (xml.row[3].cell[20]): 'свыше 10%'
    ]
    (1..31).each { index ->
        headerMapping.put((xml.row[4].cell[index - 1]), index.toString())
    }

    checkHeaderEquals(headerMapping)

    addData(xml, 4)
}

void addData(def xml, def headRowCount) {
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        /* Пропуск строк шапок */
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        newRow.setImportIndex(xlsIndexRow)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def int xmlIndexCol = 0

        // графа 1
        newRow.taCategory = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графа 2
        newRow.financialYear = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++

        // графs 3-7
        for (alias in ['taxPeriod', 'emitent', 'inn', 'decreeNumber', 'dividendType']) {
            newRow[alias] = row.cell[xmlIndexCol].text()
            xmlIndexCol++
        }

        // графы 8-31
        for (alias in ['totalDividend', 'dividendSumRaspredPeriod', 'dividendRussianTotal', 'dividendRussianStavka0',
                       'dividendRussianStavka6', 'dividendRussianStavka9', 'dividendRussianTaxFree',
                       'dividendRussianPersonal', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll', 'dividendStavka0',
                       'dividendStavkaLess5', 'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendTaxUnknown',
                       'dividendNonIncome', 'dividendAgentAll', 'dividendAgentWithStavka0', 'dividendD1D2',
                       'dividendSumForTaxStavka9', 'dividendSumForTaxStavka0', 'taxSum', 'taxSumFromPeriod', 'taxSumLast']) {
            newRow[alias] = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol++
        }
        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}

void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    dataRowHelper.saveSort()
}

void importTransportData() {
    int COLUMN_COUNT = 31
    int TOTAL_ROW_COUNT = 0
    int ROW_MAX = 1000
    def DEFAULT_CHARSET = "cp866"
    char SEPARATOR = '|'
    char QUOTE = '\''

    def dataRowHelper = formDataService.getDataRowHelper(formData)
    dataRowHelper.clear()

    checkBeforeGetXml(ImportInputStream, UploadFileName)

    if (!UploadFileName.endsWith(".rnu")) {
        logger.error(WRONG_RNU_FORMAT)
    }

    if (ImportInputStream == null) {
        logger.error("Поток данных не должен быть пустым")
    }
    if (UploadFileName == null || "".equals(UploadFileName.trim())) {
        logger.error("Имя файла не может быть пустым")
    }

    InputStreamReader isr = new InputStreamReader(ImportInputStream, DEFAULT_CHARSET)
    CSVReader reader = new CSVReader(isr, SEPARATOR, QUOTE)

    def dataRows = []
    String[] rowCells
    // количество пустых строк
    int countEmptyRow = 0
    int fileRowIndex = 0 // номер строки в файле
    int rowIndex = 0// номер строки в НФ
    int totalRowCount = 0// счетчик кол-ва итогов
    while ((rowCells = reader.readNext()) != null) {
        fileRowIndex++
        // если еще не было пустых строк, то это первая строка - заголовок
        if (rowCells.length == 1 && rowCells[0].length() < 1) { // если встретилась вторая пустая строка, то дальше только строки итогов и ЦП
            if (countEmptyRow > 0) {
                totalRowCount++
                // итоговая строка
                addRow(dataRows, reader.readNext(), COLUMN_COUNT, fileRowIndex, ++rowIndex, true)
                break
            }
            countEmptyRow++
            continue
        }
        // обычная строка
        if (countEmptyRow != 0 && !addRow(dataRows, rowCells, COLUMN_COUNT, fileRowIndex, ++rowIndex, false)){
            break
        }
        rowCells = null // очищаем кучу
        // периодически сбрасываем строки
        if (dataRows.size() > ROW_MAX) {
            dataRowHelper.insert(dataRows, dataRowHelper.allCached.size() + 1)
            dataRows.clear()
        }
    }
    if (TOTAL_ROW_COUNT != 0 && totalRowCount != TOTAL_ROW_COUNT) {
        logger.error(ROW_FILE_WRONG, fileRowIndex)
    }
    reader.close()
    if (dataRows.size() != 0) {
        dataRowHelper.insert(dataRows, dataRowHelper.allCached.size() + 1)
        dataRows.clear()
    }
}

// Добавляет строку в текущий буфер строк
boolean addRow(def dataRowsCut, String[] rowCells, def columnCount, def fileRowIndex, def rowIndex, boolean isTotal) {
    if (rowCells == null || isTotal) {
        return true
    }

    def DataRow newRow = formData.createDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)

    if (rowCells.length != columnCount + 2) {
        rowError(logger, newRow, String.format(ROW_FILE_WRONG, fileRowIndex))
        return false
    }

    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    def int colOffset = 1
    def int colIndex = 1

    // графа 1
    newRow.taCategory = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графа 2
    newRow.financialYear = parseDate(pure(rowCells[colIndex]), "yyyy", fileRowIndex, colIndex + colOffset, logger, true)
    colIndex++

    // графs 3-7
    for (alias in ['taxPeriod', 'emitent', 'inn', 'decreeNumber', 'dividendType']) {
        newRow[alias] = pure(rowCells[colIndex])
        colIndex++
    }

    // графы 8-31
    for (alias in ['totalDividend', 'dividendSumRaspredPeriod', 'dividendRussianTotal', 'dividendRussianStavka0',
                   'dividendRussianStavka6', 'dividendRussianStavka9', 'dividendRussianTaxFree',
                   'dividendRussianPersonal', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll', 'dividendStavka0',
                   'dividendStavkaLess5', 'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendTaxUnknown',
                   'dividendNonIncome', 'dividendAgentAll', 'dividendAgentWithStavka0', 'dividendD1D2',
                   'dividendSumForTaxStavka9', 'dividendSumForTaxStavka0', 'taxSum', 'taxSumFromPeriod', 'taxSumLast']) {
        newRow[alias] = parseNumber(pure(rowCells[colIndex]), fileRowIndex, colIndex + colOffset, logger, true)
        colIndex++
    }
    dataRowsCut.add(newRow)
    return true
}

static String pure(String cell) {
    return StringUtils.cleanString(cell).intern()
}

def roundValue(def value, int precision = 0) {
    if (value != null) {
        return ((BigDecimal) value).setScale(precision, BigDecimal.ROUND_HALF_UP)
    } else {
        return null
    }
}