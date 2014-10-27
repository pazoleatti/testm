package form_template.income.output1_1.v2014

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * Сведения для расчёта налога с доходов в виде дивидендов (03/А)
 * formTemplateId=1411
 *
 * http://conf.aplana.com/pages/viewpage.action?pageId=8784122
 *
 * @author Stanislav Yasinskiy
 *
 1. 	financialYear                   Отчетный год
 2.     taxPeriod                       Налоговый (отчетный) период
 3.     emitent                         Эмитент
 4.     decreeNumber                    Номер решения о распределении доходов от долевого участия
 5.     dividendType                    Вид дивидендов
 6. 	dividendSumRaspredPeriod        Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Всего
 7.     dividendSumNalogAgent           Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. По которым выступает в качестве налогового агента
 8. 	dividendForgeinOrgAll           Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России. Организациям
 9. 	dividendForgeinPersonalAll      Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России. Физическим лицам
 10. 	dividendStavka0                 Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке. 0%
 11. 	dividendStavkaLess5             Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке. До 5% включительно
 12. 	dividendStavkaMore5             Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке. Свыше 5% и до 10% включительно
 13. 	dividendStavkaMore10            Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке. Свыше 10%
 14. 	dividendRussianMembersAll       Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Всего
 15. 	dividendRussianOrgStavka9       Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Организациям (налоговая ставка - 9%)
 16. 	dividendRussianOrgStavka0       Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Организациям (налоговая ставка - 0%)
 17. 	dividendPersonRussia            Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Физическим лицам
 18. 	dividendMembersNotRussianTax    Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Не являющихся налогоплательщиками
 19. 	dividendAgentAll                Дивиденды, полученные. Всего
 20. 	dividendAgentWithStavka0        Дивиденды, полученные. В т. ч. без учета полученных дивидендов, налог с которых исчислен по ставке 0%
 21. 	dividendSumForTaxAll            Сумма дивидендов, используемых для исчисления налога по российским организациям. Всего
 22. 	dividendSumForTaxStavka9        Сумма дивидендов, используемых для исчисления налога по российским организациям. Налоговая ставка 9%
 23. 	dividendSumForTaxStavka0        Сумма дивидендов, используемых для исчисления налога по российским организациям. Налоговая ставка 0%
 24. 	taxSum                          Исчисленная сумма налога, подлежащая уплате в бюджет
 25. 	taxSumFromPeriod                Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды
 26. 	taxSumFromPeriodAll             Сумма налога, начисленная с дивидендов, выплаченных в отчетном квартале
 *
 */
switch (formDataEvent) {
    case FormDataEvent.CREATE:
        if (formData.kind != FormDataKind.ADDITIONAL) {
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
    case FormDataEvent.ADD_ROW:
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
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
}

//// Кэши и константы
@Field
def providerCache = [:]
@Field
def recordCache = [:]
@Field
def refBookCache = [:]

@Field
def editableColumns = ['financialYear', 'taxPeriod', 'emitent', 'decreeNumber', 'dividendType', 'dividendSumRaspredPeriod',
                       'dividendSumNalogAgent', 'dividendForgeinOrgAll',
                       'dividendForgeinPersonalAll', 'dividendStavka0', 'dividendStavkaLess5', 'dividendStavkaMore5',
                       'dividendStavkaMore10', 'dividendRussianMembersAll', 'dividendRussianOrgStavka9',
                       'dividendRussianOrgStavka0', 'dividendPersonRussia', 'dividendMembersNotRussianTax',
                       'dividendAgentAll', 'dividendAgentWithStavka0', 'dividendSumForTaxAll','dividendSumForTaxStavka9',
                       'dividendSumForTaxStavka0','taxSum', 'taxSumFromPeriod', 'taxSumFromPeriodAll']

// Автозаполняемые атрибуты
@Field
def autoFillColumns = ['dividendSumForTaxAll', 'dividendSumForTaxStavka9', 'dividendSumForTaxStavka0', 'taxSum']

@Field
def nonEmptyColumns = ['financialYear', 'taxPeriod', 'dividendType', 'dividendSumRaspredPeriod', 'dividendSumNalogAgent',
                       'dividendForgeinOrgAll', 'dividendForgeinPersonalAll', 'dividendStavka0', 'dividendStavkaLess5',
                       'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendRussianOrgStavka9',
                       'dividendRussianOrgStavka0', 'dividendPersonRussia', 'dividendMembersNotRussianTax',
                       'dividendAgentAll', 'dividendAgentWithStavka0', 'taxSumFromPeriod', 'taxSumFromPeriodAll']

// Текущая дата
@Field
def currentDate = new Date()

//// Обертки методов

// Поиск записи в справочнике по значению (для расчетов)
def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,
                boolean required = true) {
    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,
            currentDate, rowIndex, cellName, logger, required)
}

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    // расчетов нет
}

def BigDecimal calc21(def row) {
    if (row.dividendSumRaspredPeriod == null || row.dividendAgentWithStavka0 == null) {
        return null
    }
    return roundValue(row.dividendSumRaspredPeriod - row.dividendAgentWithStavka0, 0)
}

def BigDecimal calc22(def row) {
    if (row.dividendRussianOrgStavka9 == null || !row.dividendSumRaspredPeriod || row.dividendSumForTaxAll == null) {
        return null
    }
    return roundValue(row.dividendRussianOrgStavka9 / row.dividendSumRaspredPeriod * row.dividendSumForTaxAll, 0)
}

def BigDecimal calc23(def row) {
    if (row.dividendRussianOrgStavka0 == null || !row.dividendSumRaspredPeriod || row.dividendSumForTaxAll == null) {
        return null
    }
    return roundValue(row.dividendRussianOrgStavka0 / row.dividendSumRaspredPeriod * row.dividendSumForTaxAll, 0)
}

def BigDecimal calc24(def row) {
    if (row.dividendSumForTaxStavka9 == null) {
        return null
    }
    return roundValue(row.dividendSumForTaxStavka9 * 0.09, 0)
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // Алиасы граф для арифметической проверки
    def arithmeticCheckAlias = ['dividendSumForTaxAll', 'dividendSumForTaxStavka9', 'dividendSumForTaxStavka0', 'taxSum']
    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    for (def row in dataRows) {
        def rowNum = row.getIndex()

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)
        checkNonEmptyColumns(row, rowNum, ['emitent', 'decreeNumber'], logger, false)

        // Арифметические проверки расчета граф 21-24, 26
        needValue['dividendSumForTaxAll'] = calc21(row)
        needValue['dividendSumForTaxStavka9'] = calc22(row)
        needValue['dividendSumForTaxStavka0'] = calc23(row)
        needValue['taxSum'] = calc24(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // Проверка наличия значения графы 2 в справочнике «Коды, определяющие налоговый (отчётный) период»
        def cell = row.getCell('taxPeriod')
        getRecordId(8, 'CODE', cell.value, rowNum, cell.column.name, true)
    }

    // 2. Проверка наличия формы за предыдущий отчётный период
    if (formDataService.getFormDataPrev(formData, formData.departmentId) == null) {
        logger.warn('Форма за предыдущий отчётный период не создавалась!')
    }
}

def roundValue(BigDecimal value, def int precision) {
    value?.setScale(precision, BigDecimal.ROUND_HALF_UP)
}

def checkOverpower(def value, def row, def alias) {
    if (value?.abs() >= 1e15) {
        def checksMap = [
                'dividendSumForTaxAll'    : "«графа 6» - «графа 20»",
                'dividendSumForTaxStavka9': "ОКРУГЛ («графа 15» / «графа 6» * «графа 21» ; 0) ",
                'dividendSumForTaxStavka0': "ОКРУГЛ («графа 16» / «графа 6» * «графа 21» ; 0) ",
                'taxSum'                  : "ОКРУГЛ («графа 22» * 9%; 0)"
        ]
        def aliasMap = [
                'dividendSumForTaxAll'    : '21',
                'dividendSumForTaxStavka9': '22',
                'dividendSumForTaxStavka0': '23',
                'taxSum'                  : '24'
        ]
        throw new ServiceException("Строка ${row.getIndex()}: значение «Графы ${aliasMap[alias]}» превышает допустимую " +
                "разрядность (15 знаков). «Графа ${aliasMap[alias]}» рассчитывается как «${checksMap[alias]}»!")
    }
    return value
}

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'Отчетный год', null, 26, 5)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 26, 5)

    def headerMapping = [
            (xml.row[0].cell[0]) : 'Отчетный год',
            (xml.row[0].cell[1]) : 'Налоговый (отчетный) период',
            (xml.row[0].cell[2]) : 'Эмитент',
            (xml.row[0].cell[3]) : 'Номер решения о распределении доходов от долевого участия',
            (xml.row[0].cell[4]) : 'Вид дивидендов',
            (xml.row[0].cell[5]) : 'Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде',
            (xml.row[0].cell[18]): 'Дивиденды, полученные',
            (xml.row[0].cell[20]): 'Сумма дивидендов, используемых для исчисления налога по российским организациям:',
            (xml.row[0].cell[23]): 'Исчисленная сумма налога, подлежащая уплате в бюджет',
            (xml.row[0].cell[24]): 'Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды',
            (xml.row[0].cell[25]): 'Сумма налога, начисленная с дивидендов, выплаченных в отчетном квартале',
            (xml.row[1].cell[5]) : 'Всего',
            (xml.row[1].cell[6]) : 'по которым выступает в качестве налогового агента',
            (xml.row[1].cell[7]) : 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России:',
            (xml.row[1].cell[13]): 'Дивиденды, подлежащие распределению российским акционерам (участникам):',
            (xml.row[1].cell[18]): 'Всего',
            (xml.row[1].cell[19]): 'В т. ч. без учета полученных дивидендов, налог с которых исчислен по ставке 0%',
            (xml.row[1].cell[20]): 'Всего',
            (xml.row[1].cell[21]): 'налоговая ставка 9%',
            (xml.row[1].cell[22]): 'налоговая ставка 0%',
            (xml.row[2].cell[7]) : 'организациям',
            (xml.row[2].cell[8]) : 'физическим лицам',
            (xml.row[2].cell[9]) : 'из них налоги с которых исчислены по ставке:',
            (xml.row[2].cell[13]): 'Всего',
            (xml.row[2].cell[14]): 'организациям',
            (xml.row[2].cell[16]): 'физическим лицам',
            (xml.row[2].cell[17]): 'не являющихся налогоплательщиками',
            (xml.row[3].cell[9]) : '0%',
            (xml.row[3].cell[10]): 'до 5% включительно',
            (xml.row[3].cell[11]): 'свыше 5% и до 10% включительно',
            (xml.row[3].cell[12]): 'свыше 10%',
            (xml.row[3].cell[14]): 'налоговая ставка - 9%',
            (xml.row[3].cell[15]): 'налоговая ставка - 0%'
    ]
    (0..25).each { index ->
        headerMapping.put((xml.row[4].cell[index]), (index + 1).toString())
    }

    checkHeaderEquals(headerMapping)

    // добавить данные в форму
    addData(xml, 5)
}

void addData(def xml, headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    // количество графов в таблице
    def columnCount = 26
    def rows = []
    def int rowIndex = 1

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount - 1) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        if (row.cell.size() >= columnCount) {
            def newRow = formData.createDataRow()
            newRow.setIndex(rowIndex++)
            editableColumns.each {
                newRow.getCell(it).editable = true
                newRow.getCell(it).setStyleAlias('Редактируемая')
            }
            autoFillColumns.each {
                newRow.getCell(it).setStyleAlias('Автозаполняемая')
            }

            def xmlIndexCol = 0
            newRow.financialYear = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 1
            newRow.taxPeriod = row.cell[xmlIndexCol].text()
            xmlIndexCol = 2
            newRow.emitent = row.cell[xmlIndexCol].text()
            xmlIndexCol = 3
            newRow.decreeNumber = row.cell[xmlIndexCol].text()
            xmlIndexCol = 4
            newRow.dividendType = row.cell[xmlIndexCol].text()
            xmlIndexCol = 5
            newRow.dividendSumRaspredPeriod = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 6
            newRow.dividendSumNalogAgent = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 7
            newRow.dividendForgeinOrgAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 8
            newRow.dividendForgeinPersonalAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 9
            newRow.dividendStavka0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 10
            newRow.dividendStavkaLess5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 11
            newRow.dividendStavkaMore5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 12
            newRow.dividendStavkaMore10 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 13
            newRow.dividendRussianMembersAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 14
            newRow.dividendRussianOrgStavka9 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 15
            newRow.dividendRussianOrgStavka0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 16
            newRow.dividendPersonRussia = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 17
            newRow.dividendMembersNotRussianTax = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 18
            newRow.dividendAgentAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 19
            newRow.dividendAgentWithStavka0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 20
            newRow.dividendSumForTaxAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 21
            newRow.dividendSumForTaxStavka9 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 22
            newRow.dividendSumForTaxStavka0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 23
            newRow.taxSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 24
            newRow.taxSumFromPeriod = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
            xmlIndexCol = 25
            newRow.taxSumFromPeriodAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

            rows.add(newRow)
        }
    }
    dataRowHelper.save(rows)
}