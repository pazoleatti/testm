package form_template.income.output1.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import groovy.transform.Field

/**
 * Сведения для расчёта налога с доходов в виде дивидендов (доходов от долевого участия в других организациях,
 * созданных на территории Российской Федерации)
 * formTemplateId=306
 *
 * http://conf.aplana.com/pages/viewpage.action?pageId=8784122
 *
 * @author Stanislav Yasinskiy
 1.     dividendType                    Вид дивидендов
 2.     taxPeriod                       Налоговый (отчетный) период
 3. 	financialYear                   Отчетный год
 4. 	dividendSumRaspredPeriod        Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Всего
 5. 	dividendForgeinOrgAll           Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России. Дивиденды, начисленные иностранным организациям, всего
 6. 	dividendForgeinPersonalAll      Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России. Дивиденды, начисленные физическим лицам, не являющимся резидентами России, всего
 7. 	dividendTotalRaspredPeriod      Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Итого
 8. 	dividendStavka0                 Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке, 0%
 9. 	dividendStavkaLess5             Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке, до 5% включительно
 10. 	dividendStavkaMore5             Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке, свыше 5% и до 10% включительно
 11. 	dividendStavkaMore10            Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке, свыше 10%
 12. 	dividendRussianMembersAll       Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам), всего
 13. 	dividendRussianMembersTotal     Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Итого
 14. 	dividendRussianOrgStavka9       Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам), в том числе в пользу, российских организаций (налоговая ставка), 9%
 15. 	dividendRussianOrgStavka0       Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам), в том числе в пользу, российских организаций (налоговая ставка), 0%
 16. 	dividendPersonRussia            Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам), в том числе в пользу, физических лиц - резидентов России
 17. 	dividendMembersNotRussianTax    Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам), в том числе в пользу акционеров (участников), не являющихся налогоплательщиками
 18. 	dividendAgentAll                Дивиденды, полученные самим налоговым агентом в предыдущем и в текущем налоговом периоде до распределения дивидендов между акционерами (участниками). Всего
 19. 	dividendAgentWithStavka0        Дивиденды, полученные самим налоговым агентом в предыдущем и в текущем налоговом периоде до распределения дивидендов между акционерами (участниками). В т. ч. без учета полученных дивидендов, налог с которых исчислен по ставке 0%
 20. 	dividendSumForTaxAll            Сумма дивидендов, используемых для исчисления налога. Всего
 21. 	dividendSumForTaxStavka9        Сумма дивидендов, используемых для исчисления налога. В том числе по российским организациям (по налоговой ставке), 9%
 22. 	dividendSumForTaxStavka0        Сумма дивидендов, используемых для исчисления налога. В том числе по российским организациям (по налоговой ставке), 0%
 23. 	taxSum                          Исчисленная сумма налога, подлежащая уплате в бюджет
 24. 	taxSumFromPeriod                Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды
 25. 	taxSumFromPeriodAll             Сумма налога, начисленная с дивидендов, выплаченных в последнем квартале (месяце) отчетного (налогового) периода - всего
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
}

//// Кэши и константы
@Field
def refBookCache = [:]

// 3, 5, 6, 8-12, 14-19, 25
@Field
def editableColumns = ['financialYear', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll',
                       'dividendStavka0', 'dividendStavkaLess5', 'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendRussianMembersAll',
                       'dividendRussianOrgStavka9', 'dividendRussianOrgStavka0', 'dividendPersonRussia', 'dividendMembersNotRussianTax',
                       'dividendAgentAll', 'dividendAgentWithStavka0', 'taxSumFromPeriodAll']

// Проверяемые на пустые значения атрибуты 1 – 11, 14 – 19, 24, 25
@Field
def nonEmptyColumns = ['dividendType', 'taxPeriod', 'financialYear', 'dividendSumRaspredPeriod', 'dividendForgeinOrgAll',
                       'dividendForgeinPersonalAll', 'dividendTotalRaspredPeriod', 'dividendStavka0', 'dividendStavkaLess5',
                       'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendRussianOrgStavka9', 'dividendRussianOrgStavka0',
                       'dividendPersonRussia', 'dividendMembersNotRussianTax', 'dividendAgentAll', 'dividendAgentWithStavka0',
                       'taxSumFromPeriod', 'taxSumFromPeriodAll']

//// Обертки методов

// Разыменование записи справочника
def getRefBookValue(def long refBookId, def Long recordId) {
    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)
}

//// Кастомные методы

// Алгоритмы заполнения полей формы
void calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    if (!dataRows.isEmpty()) {

        for (def row in dataRows) {
            // графа 1
            row.dividendType = '2'
            // графа 2
            row.taxPeriod = '34'
            // графа 4
            row.dividendSumRaspredPeriod = checkOverpower(calc4(row), row, "dividendSumRaspredPeriod")
            // графа 7
            row.dividendTotalRaspredPeriod = checkOverpower(calc7(row), row, "dividendTotalRaspredPeriod")
            // графа 13
            row.dividendRussianMembersTotal = roundValue(row.dividendRussianMembersAll, 0)
            // графа 20
            row.dividendSumForTaxAll = checkOverpower(calc20(row), row, "dividendSumForTaxAll")
            // графа 21
            row.dividendSumForTaxStavka9 = checkOverpower(calc21(row), row, "dividendSumForTaxStavka9")
            // графа 22
            row.dividendSumForTaxStavka0 = checkOverpower(calc22(row), row, "dividendSumForTaxStavka0")
            // графа 23
            row.taxSum = checkOverpower(calc23(row), row, "taxSum")
            // графа 24
            row.taxSumFromPeriod = checkOverpower(calc24(row), row, "taxSumFromPeriod")
        }
        dataRowHelper.update(dataRows);
    }
}

def BigDecimal calc4(def row) {
    if (row.dividendRussianMembersAll == null || row.dividendForgeinOrgAll == null || row.dividendForgeinPersonalAll == null) {
        return null
    }
    return roundValue(row.dividendForgeinOrgAll + row.dividendForgeinPersonalAll + row.dividendRussianMembersAll, 0)
}

def BigDecimal calc7(def row) {
    if (row.dividendForgeinOrgAll == null || row.dividendForgeinPersonalAll == null) {
        return null
    }
    return roundValue(row.dividendForgeinOrgAll + row.dividendForgeinPersonalAll, 0)
}

def BigDecimal calc20(def row) {
    if (row.dividendRussianMembersAll == null || row.dividendAgentWithStavka0 == null) {
        return null
    }
    return roundValue((row.dividendRussianMembersAll ?: 0) - (row.dividendAgentWithStavka0 ?: 0), 0)
}

def BigDecimal calc21(def row) {
    if (row.dividendRussianOrgStavka9 == null || !row.dividendRussianMembersAll || row.dividendSumForTaxAll == null) {
        return null
    }
    return roundValue(row.dividendRussianOrgStavka9 / row.dividendRussianMembersAll * row.dividendSumForTaxAll, 0)
}

def BigDecimal calc22(def row) {
    if (row.dividendRussianOrgStavka0 == null || !row.dividendRussianMembersAll || row.dividendSumForTaxAll == null) {
        return null
    }
    return roundValue(row.dividendRussianOrgStavka0 / row.dividendRussianMembersAll * row.dividendSumForTaxAll, 0)
}

def BigDecimal calc23(def row) {
    if (row.dividendSumForTaxStavka9 == null) {
        return null
    }
    return roundValue(row.dividendSumForTaxStavka9 * 0.09, 0)
}

def BigDecimal calc24(def row) {
    // TODO сумма или одна строка? если одна строка - то закоментаренный вариант
    // [15:10:41] Sariya Mustafina: в прошлом периоде должна быть одна строка
    // [15:10:55] Sariya Mustafina: но это нигде не проверяется
    // [15:10:58] Sariya Mustafina: спрошу
    def result = 0
    // --
    formPrev = formDataService.getFormDataPrev(formData)
    if (formPrev != null) {
        for (rowPrev in formDataService.getDataRowHelper(formPrev).getAll()) {
            if (rowPrev.financialYear.format('yyyy') == row.financialYear.format('yyyy')) {
                result += rowPrev.taxSumFromPeriod ?: 0 + rowPrev.taxSumFromPeriodAll ?: 0
                // return rowPrev.taxSumFromPeriod ?: 0 + rowPrev.taxSumFromPeriodAll ?: 0
            }
        }
    }
    return result
    // return 0
}

def logicCheck() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    // Алиасы граф для арифметической проверки
    def arithmeticCheckAlias = ['dividendSumRaspredPeriod', 'dividendSumForTaxAll', 'dividendSumForTaxStavka9',
                                'dividendSumForTaxStavka0', 'taxSum', 'taxSumFromPeriod']
    // Для хранения правильных значении и сравнения с имеющимися при арифметических проверках
    def needValue = [:]

    for (def row in dataRows) {

        // 1. Проверка на заполнение поля
        checkNonEmptyColumns(row, row.getIndex(), nonEmptyColumns, logger, true)

        // Арифметические проверки расчета граф 4, 18, 19, 20, 22
        needValue['dividendSumRaspredPeriod'] = calc4(row)
        needValue['dividendSumForTaxAll'] = calc20(row)
        needValue['dividendSumForTaxStavka9'] = calc21(row)
        needValue['dividendSumForTaxStavka0'] = calc22(row)
        needValue['taxSum'] = calc23(row)
        needValue['taxSumFromPeriod'] = calc24(row)
        checkCalc(row, arithmeticCheckAlias, needValue, logger, true)

        // 2. Проверка наличия формы за предыдущий отчётный период
        if (formDataService.getFormDataPrev(formData) == null) {
            logger.warn('Форма за предыдущий отчётный период не создавалась!')
        }
    }
}

def roundValue(BigDecimal value, def precision) {
    value?.setScale(precision, BigDecimal.ROUND_HALF_UP)
}

def checkOverpower(def value, def row, def alias) {
    if (value?.abs() >= 1e15) {
        def checksMap = [
                'dividendSumRaspredPeriod'      : "ОКРУГЛ («графа 5» + «графа 6» + «графа 12»; 0)",
                'dividendTotalRaspredPeriod'    : "ОКРУГЛ («графа 5» + «графа 6»; 0)",
                'dividendSumForTaxAll'          : "ОКРУГЛ («графа 12» – «графа 19» ; 0)",
                'dividendSumForTaxStavka9'      : "ОКРУГЛ («графа 14» / «графа 13» * «графа 20» ; 0) ",
                'dividendSumForTaxStavka0'      : "ОКРУГЛ («графа 15» / «графа 13» * «графа 20» ; 0) ",
                'taxSum'                        : "ОКРУГЛ («графа 21» / 100 * 9; 0)",
                'taxSumFromPeriod'              : "«графа 24» предыдущего отчётного периода + «графа 25» предыдущего отчётного периода." +
                        "\n Значения граф текущей формы и формы предудущего отчётного периода берутся для строк с одинаковым годом ," +
                        " т. е. «графа 3» в текущем отчётном периоде = «графе 3» в предыдущем отчётном периоде.\n" +
                        "Если отчёт по году («графа 3») впервые, то «графа 24» принимает значение «0»"
        ]
        def aliasMap = [
                'dividendSumRaspredPeriod'      : '4',
                'dividendTotalRaspredPeriod'    : '7',
                'dividendSumForTaxAll'          : '20',
                'dividendSumForTaxStavka9'      : '21',
                'dividendSumForTaxStavka0'      : '22',
                'taxSum'                        : '23',
                'taxSumFromPeriod'              : '24'
        ]
        throw new ServiceException("Строка ${row.getIndex()}: значение «Графы ${aliasMap[alias]}» превышает допустимую " +
                "разрядность (15 знаков). «Графа ${aliasMap[alias]}» рассчитывается как «${checksMap[alias]}»!")
    }
    return value
}

void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, 'Вид дивидендов', null, 25, 3)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 25, 3)

    def headerMapping = [
            (xml.row[0].cell[0]) : 'Вид дивидендов',
            (xml.row[0].cell[1]) : 'Налоговый (отчетный) период',
            (xml.row[0].cell[2]) : 'Отчетный год',
            (xml.row[0].cell[3]) : 'Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде',
            (xml.row[0].cell[17]) : 'Дивиденды, полученные самим налоговым агентом в предыдущем и в текущем налоговом периоде до распределения дивидендов между акционерами (участниками)',
            (xml.row[0].cell[19]) : 'Сумма дивидендов, используемых для исчисления налога',
            (xml.row[0].cell[22]): 'Исчисленная сумма налога, подлежащая уплате в бюджет',
            (xml.row[0].cell[23]): 'Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды',
            (xml.row[0].cell[24]): 'Сумма налога, начисленная с дивидендов, выплаченных в последнем квартале (месяце) отчетного (налогового) периода - всего',
            (xml.row[1].cell[3]) : 'Всего',
            (xml.row[1].cell[4]) : 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России. Дивиденды, начисленные иностранным организациям, всего',
            (xml.row[1].cell[5]) : 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России. Дивиденды, начисленные физическим лицам, не являющимся резидентами России, всего',
            (xml.row[1].cell[6]): 'Итого',
            (xml.row[1].cell[7]): 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке, 0%',
            (xml.row[1].cell[8]): 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке, до 5% включительно',
            (xml.row[1].cell[9]): 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке, свыше 5% и до 10% включительно',
            (xml.row[1].cell[10]): 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке, свыше 10%',
            (xml.row[1].cell[11]): 'Дивиденды, подлежащие распределению российским акционерам (участникам), всего',
            (xml.row[1].cell[12]) : 'Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Итого',
            (xml.row[1].cell[13]) : 'Дивиденды, подлежащие распределению российским акционерам (участникам), в том числе в пользу, российских организаций (налоговая ставка), 9%',
            (xml.row[1].cell[14]) : 'Дивиденды, подлежащие распределению российским акционерам (участникам), в том числе в пользу, российских организаций (налоговая ставка), 0%',
            (xml.row[1].cell[15]): 'Дивиденды, подлежащие распределению российским акционерам (участникам), в том числе в пользу, физических лиц - резидентов России',
            (xml.row[1].cell[16]): 'Дивиденды, подлежащие распределению российским акционерам (участникам), в том числе в пользу акционеров (участников), не являющихся налогоплательщиками',
            (xml.row[1].cell[17]): 'Всего',
            (xml.row[1].cell[18]): 'В т. ч. без учета полученных дивидендов, налог с которых исчислен по ставке 0%',
            (xml.row[1].cell[19]) : 'Всего',
            (xml.row[1].cell[20]): 'В том числе по российским организациям (по налоговой ставке), 9%',
            (xml.row[1].cell[21]): 'В том числе по российским организациям (по налоговой ставке), 0%',
            (xml.row[1].cell[22]): 'Исчисленная сумма налога, подлежащая уплате в бюджет',
            (xml.row[1].cell[23]): 'Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды',
            (xml.row[1].cell[24]): 'Сумма налога, начисленная с дивидендов, выплаченных в последнем квартале (месяце) отчетного (налогового) периода - всего'
    ]
    (0..24).each { index ->
        headerMapping.put((xml.row[2].cell[index]), (index + 1).toString())
    }

    checkHeaderEquals(headerMapping)

    // добавить данные в форму
    addData(xml, 3)
}

void addData(def xml, headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def int rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def int colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    // количество графов в таблице
    def columnCount = 25
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

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }

        def xmlIndexCol = 0
        newRow.dividendType = row.cell[xmlIndexCol].text()
        xmlIndexCol = 1
        newRow.taxPeriod = row.cell[xmlIndexCol].text()
        xmlIndexCol = 2
        newRow.financialYear = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 3
        newRow.dividendSumRaspredPeriod = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 4
        newRow.dividendForgeinOrgAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 5
        newRow.dividendForgeinPersonalAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 6
        newRow.dividendTotalRaspredPeriod = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 7
        newRow.dividendStavka0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 8
        newRow.dividendStavkaLess5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 9
        newRow.dividendStavkaMore5 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 10
        newRow.dividendStavkaMore10 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 11
        newRow.dividendRussianMembersAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 12
        newRow.dividendRussianMembersTotal = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 13
        newRow.dividendRussianOrgStavka9 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 14
        newRow.dividendRussianOrgStavka0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 15
        newRow.dividendPersonRussia = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 16
        newRow.dividendMembersNotRussianTax = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 17
        newRow.dividendAgentAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 18
        newRow.dividendAgentWithStavka0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 19
        newRow.dividendSumForTaxAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 20
        newRow.dividendSumForTaxStavka9 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 21
        newRow.dividendSumForTaxStavka0 = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 22
        newRow.taxSum = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 23
        newRow.taxSumFromPeriod = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol = 24
        newRow.taxSumFromPeriodAll = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    dataRowHelper.save(rows)
}