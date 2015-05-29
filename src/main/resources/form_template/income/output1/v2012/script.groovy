package form_template.income.output1.v2012

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
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
        if (!logger.containsLevel(LogLevel.ERROR)) {
            calc()
            logicCheck()
        }
        break
    case FormDataEvent.SORT_ROWS:
        sortFormDataRows()
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
            def int index = row.getIndex()
            def BigDecimal value4 = calc4(row)
            def BigDecimal value7 = calc7(row)
            def BigDecimal value20 = calc20(row)
            def BigDecimal value21 = calc21(row)
            def BigDecimal value22 = calc22(row)
            def BigDecimal value23 = calc23(row)
            def BigDecimal value24 = calc24(row)

            // Проверки, выполняемые до расчёта
            checkOverflow(value4, row, 'dividendSumRaspredPeriod', index, 15, 'ОКРУГЛ («графа 5» + «графа 6» + «графа 12»; 0)')
            checkOverflow(value7, row, 'dividendTotalRaspredPeriod', index, 15, 'ОКРУГЛ («графа 5» + «графа 6»; 0)')
            checkOverflow(value20, row, 'dividendSumForTaxAll', index, 15, 'ОКРУГЛ («графа 12» – «графа 19» ; 0)')
            checkOverflow(value21, row, 'dividendSumForTaxStavka9', index, 15, 'ОКРУГЛ («графа 14» / «графа 13» * «графа 20» ; 0)')
            checkOverflow(value22, row, 'dividendSumForTaxStavka0', index, 15, 'ОКРУГЛ («графа 15» / «графа 13» * «графа 20» ; 0)')
            checkOverflow(value23, row, 'taxSum', index, 15, 'ОКРУГЛ («графа 21» / 100 * 9; 0)')
            checkOverflow(value24, row, 'taxSumFromPeriod', index, 15, '«графа 24» предыдущего отчётного периода + «графа 25» предыдущего отчётного периода.' +
                    '\n Значения граф текущей формы и формы предыдущего отчётного периода берутся для строк с одинаковым годом ,' +
                    ' т. е. «графа 3» в текущем отчётном периоде = «графе 3» в предыдущем отчётном периоде.\n' +
                    'Если отчёт по году («графа 3») впервые, то «графа 24» принимает значение «0»')

            // графа 1
            row.dividendType = '2'
            // графа 2
            row.taxPeriod = '34'
            // графа 4
            row.dividendSumRaspredPeriod = calc4(row)
            // графа 7
            row.dividendTotalRaspredPeriod = calc7(row)
            // графа 13
            row.dividendRussianMembersTotal = roundValue(row.dividendRussianMembersAll, 0)
            // графа 20
            row.dividendSumForTaxAll = calc20(row)
            // графа 21
            row.dividendSumForTaxStavka9 = calc21(row)
            // графа 22
            row.dividendSumForTaxStavka0 = calc22(row)
            // графа 23
            row.taxSum = calc23(row)
            // графа 24
            row.taxSumFromPeriod = calc24(row)
        }
        dataRowHelper.update(dataRows);
    }

    sortFormDataRows()
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

void sortFormDataRows() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached
    sortRows(refBookService, logger, dataRows, null, null, null)
    dataRowHelper.saveSort()
}

void importData() {
    int COLUMN_COUNT = 25
    int HEADER_ROW_COUNT = 3
    String TABLE_START_VALUE = 'Вид дивидендов'
    String TABLE_END_VALUE = null

    def allValues = []      // значения формы
    def headerValues = []   // значения шапки
    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)

    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)

    // проверка шапки
    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)
    // освобождение ресурсов для экономии памяти
    headerValues.clear()
    headerValues = null

    def fileRowIndex = paramsMap.rowOffset
    def colOffset = paramsMap.colOffset
    paramsMap.clear()
    paramsMap = null

    def rowIndex = 0
    def rows = []
    def allValuesCount = allValues.size()

    // формирвание строк нф
    for (def i = 0; i < allValuesCount; i++) {
        rowValues = allValues[0]
        fileRowIndex++
        // все строки пустые - выход
        if (!rowValues) {
            allValues.remove(rowValues)
            rowValues.clear()
            break
        }
        // простая строка
        rowIndex++
        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)
        rows.add(newRow)
        // освободить ненужные данные - иначе не хватит памяти
        allValues.remove(rowValues)
        rowValues.clear()
    }

    showMessages(rows, logger)
    if (!logger.containsLevel(LogLevel.ERROR)) {
        formDataService.getDataRowHelper(formData).save(rows)
    }
}

/**
 * Проверить шапку таблицы
 *
 * @param headerRows строки шапки
 * @param colCount количество колонок в таблице
 * @param rowCount количество строк в таблице
 */
void checkHeaderXls(def headerRows, def colCount, rowCount) {
    if (headerRows.isEmpty() || headerRows.size() < rowCount) {
        throw new ServiceException(WRONG_HEADER_ROW_SIZE)
    }
    checkHeaderSize(headerRows[rowCount - 1].size(), headerRows.size(), colCount, rowCount)
    def headerMapping = [
            (headerRows[0][0]) : 'Вид дивидендов',
            (headerRows[0][1]) : 'Налоговый (отчетный) период',
            (headerRows[0][2]) : 'Отчетный год',
            (headerRows[0][3]) : 'Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде',
            (headerRows[0][17]): 'Дивиденды, полученные самим налоговым агентом в предыдущем и в текущем налоговом периоде до распределения дивидендов между акционерами (участниками)',
            (headerRows[0][19]): 'Сумма дивидендов, используемых для исчисления налога',
            (headerRows[0][22]): 'Исчисленная сумма налога, подлежащая уплате в бюджет',
            (headerRows[0][23]): 'Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды',
            (headerRows[0][24]): 'Сумма налога, начисленная с дивидендов, выплаченных в последнем квартале (месяце) отчетного (налогового) периода - всего',
            (headerRows[1][3]) : 'Всего',
            (headerRows[1][4]) : 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России. Дивиденды, начисленные иностранным организациям, всего',
            (headerRows[1][5]) : 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России. Дивиденды, начисленные физическим лицам, не являющимся резидентами России, всего',
            (headerRows[1][6]) : 'Итого',
            (headerRows[1][7]) : 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке, 0%',
            (headerRows[1][8]) : 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке, до 5% включительно',
            (headerRows[1][9]) : 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке, свыше 5% и до 10% включительно',
            (headerRows[1][10]): 'Дивиденды, начисленные иностранным организациям и физическим лицам, не являющимся резидентами России, дивиденды, налоги с которых исчислены по ставке, свыше 10%',
            (headerRows[1][11]): 'Дивиденды, подлежащие распределению российским акционерам (участникам), всего',
            (headerRows[1][12]): 'Сумма дивидендов, подлежащих распределению между акционерами (участниками) в текущем налоговом периоде. Дивиденды, подлежащие распределению российским акционерам (участникам). Итого',
            (headerRows[1][13]): 'Дивиденды, подлежащие распределению российским акционерам (участникам), в том числе в пользу, российских организаций (налоговая ставка), 9%',
            (headerRows[1][14]): 'Дивиденды, подлежащие распределению российским акционерам (участникам), в том числе в пользу, российских организаций (налоговая ставка), 0%',
            (headerRows[1][15]): 'Дивиденды, подлежащие распределению российским акционерам (участникам), в том числе в пользу, физических лиц - резидентов России',
            (headerRows[1][16]): 'Дивиденды, подлежащие распределению российским акционерам (участникам), в том числе в пользу акционеров (участников), не являющихся налогоплательщиками',
            (headerRows[1][17]): 'Всего',
            (headerRows[1][18]): 'В т. ч. без учета полученных дивидендов, налог с которых исчислен по ставке 0%',
            (headerRows[1][19]): 'Всего',
            (headerRows[1][20]): 'В том числе по российским организациям (по налоговой ставке), 9%',
            (headerRows[1][21]): 'В том числе по российским организациям (по налоговой ставке), 0%',
            (headerRows[1][22]): 'Исчисленная сумма налога, подлежащая уплате в бюджет',
            (headerRows[1][23]): 'Сумма налога, начисленная с дивидендов, выплаченных в предыдущие отчетные (налоговые) периоды',
            (headerRows[1][24]): 'Сумма налога, начисленная с дивидендов, выплаченных в последнем квартале (месяце) отчетного (налогового) периода - всего'
    ]
    (0..24).each { index ->
        headerMapping.put((headerRows[2][index]), (index + 1).toString())
    }
    checkHeaderEquals(headerMapping)
}

/**
 * Получить новую строку нф по значениям из экселя.
 *
 * @param values список строк со значениями
 * @param colOffset отступ в колонках
 * @param fileRowIndex номер строки в тф
 * @param rowIndex строка в нф
 */
def getNewRowFromXls(def values, def colOffset, def fileRowIndex, def rowIndex) {
    def newRow = formData.createStoreMessagingDataRow()
    newRow.setIndex(rowIndex)
    newRow.setImportIndex(fileRowIndex)
    editableColumns.each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    // графа 1
    def colIndex = 0
    newRow.dividendType = values[colIndex]

    // графа 2
    colIndex++
    newRow.taxPeriod = values[colIndex]

    // графа 3
    colIndex++
    newRow.financialYear = parseDate(values[colIndex], "yyyy", fileRowIndex, colIndex + colOffset, logger, true)

    // графа 4..25
    ['dividendSumRaspredPeriod', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll',
            'dividendTotalRaspredPeriod', 'dividendStavka0', 'dividendStavkaLess5',
            'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendRussianMembersAll',
            'dividendRussianMembersTotal', 'dividendRussianOrgStavka9', 'dividendRussianOrgStavka0',
            'dividendPersonRussia', 'dividendMembersNotRussianTax', 'dividendAgentAll',
            'dividendAgentWithStavka0', 'dividendSumForTaxAll', 'dividendSumForTaxStavka9',
            'dividendSumForTaxStavka0', 'taxSum', 'taxSumFromPeriod', 'taxSumFromPeriodAll'].each { alias ->
        colIndex++
        newRow[alias] = parseNumber(values[colIndex], fileRowIndex, colIndex + colOffset, logger, true)
    }

    return newRow
}