package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 13.08.15 20:29
 */

public class TAAbstractScriptingServiceImplTest extends TAAbstractScriptingServiceImpl {

	private static final String SCRIPT1 = "// графа 71 - col_052_3_2\n" +
			" /*   case FormDataEvent.CHECK:\n" +
			"       /* formDataService.checkUnique(formData, logger)*/\n" +
			"        break" + "switch (formDataEvent) {\n" +
			" /*   case FormDataEvent.CHECK:\n" +
			"        formDataService.checkUnique(formData, logger)*/\n" +
			"        break" + "switch (formDataEvent) {\n" +
			"//    case FormDataEvent.CALCULATE:\n" +
			"    case FormDataEvent.CREATE:\n" +
			"        formDataService.checkUnique(formData, logger)\n" +
			" /*   case FormDataEvent.CHECK:\n" +
			"        formDataService.checkUnique(formData, logger)*/\n" +
			"        break";
	private static final String SCRIPT2 = "/**\n"+
			" * Сведения для расчёта налога с доходов в виде дивидендов (03/А)\n"+
			" * formTemplateId=1411\n"+
			" *\n"+
			" * http://conf.aplana.com/pages/viewpage.action?pageId=8784122\n"+
			" *\n"+
			" *\n"+
			" *\n"+
			" */\n"+
			"switch (formDataEvent) {\n"+
			"    case FormDataEvent.CREATE:\n"+
			"        formDataService.checkUnique(formData, logger)\n"+
			"        break\n"+
			"\n"+
			"// 7, 8 графа источника\n"+
			"@Field\n"+
			"def keyColumns = ['decisionNumber', 'decisionDate']\n"+
			"\n"+
			"def getLastReportPeriod() {\n"+
			"    ReportPeriod period = reportPeriodService.get(formData.reportPeriodId)\n"+
			"    List<ReportPeriod> periodList = reportPeriodService.listByTaxPeriod(period.taxPeriod.id)\n"+
			"    return periodList.max{ ReportPeriod rp -> rp.order }\n"+
			"}\n"+
			"\n"+
			"//// Обертки методов\n"+
			"\n"+
			"// Поиск записи в справочнике по значению (для расчетов)\n"+
			"def getRecordId(def Long refBookId, def String alias, def String value, def int rowIndex, def String cellName,\n"+
			"                boolean required = true) {\n"+
			"    return formDataService.getRefBookRecordId(refBookId, recordCache, providerCache, alias, value,\n"+
			"            getReportPeriodEndDate(), rowIndex, cellName, logger, required)\n"+
			"}\n"+
			"\n"+
			"// Разыменование записи справочника\n"+
			"def getRefBookValue(def long refBookId, def Long recordId) {\n"+
			"    return formDataService.getRefBookValue(refBookId, recordId, refBookCache)\n"+
			"}\n"+
			"\n"+
			"//// Кастомные методы\n"+
			"\n"+
			"// Алгоритмы заполнения полей формы\n"+
			"void calc() {\n"+
			"    // расчетов нет\n"+
			"}\n"+
			"\n"+
			"def logicCheck() {\n"+
			"    def dataRows = formDataService.getDataRowHelper(formData).allCached\n"+
			"\n"+
			"    for (def row in dataRows) {\n"+
			"        def rowNum = row.getIndex()\n"+
			"\n"+
			"        // 1. Проверка на заполнение поля\n"+
			"        checkNonEmptyColumns(row, rowNum, nonEmptyColumns, logger, true)\n"+
			"        checkNonEmptyColumns(row, rowNum, ['emitent', 'decreeNumber'], logger, false)\n"+
			"\n"+
			"        // Проверка наличия значения графы 2 в справочнике «Коды, определяющие налоговый (отчётный) период»\n"+
			"        def cell = row.getCell('taxPeriod')\n"+
			"        getRecordId(8, 'CODE', cell.value, rowNum, cell.column.name, true)\n"+
			"    }\n"+
			"\n"+
			"    // 2. Проверка наличия формы за предыдущий отчётный период\n"+
			"    if (formDataService.getFormDataPrev(formData) == null) {\n"+
			"        logger.warn('Форма за предыдущий отчётный период не создавалась!')\n"+
			"    }\n"+
			"}\n"+
			"\n"+
			"def roundValue(BigDecimal value, def int precision) {\n"+
			"    value?.setScale(precision, BigDecimal.ROUND_HALF_UP)\n"+
			"}\n"+
			"\n"+
			"def formNewRow(def rowList, def prevPeriodStartDate, def prevPeriodEndDate, def lastPeriodStartDate, def lastPeriodEndDate) {\n"+
			"    def newRow = formData.createDataRow()\n"+
			"    editableColumns.each {\n"+
			"        newRow.getCell(it).editable = true\n"+
			"        newRow.getCell(it).setStyleAlias('Редактируемая')\n"+
			"    }\n"+
			"    // беру первую строку\n"+
			"    def row = rowList[0]\n"+
			"\n"+
			"    // «Графа 1» = Значение «Графы 9» первичной формы\n"+
			"    newRow.financialYear = row.year\n"+
			"    // Графа 2\n"+
			"    newRow.taxPeriod = calcPeriod(row.firstMonth, row.lastMonth)\n"+
			"    // «Графа 3» = «Графа 2» первичной формы\n"+
			"    newRow.emitent = row.emitentName\n"+
			"    // «Графа 4» = «Графа 7» первичной формы\n"+
			"    newRow.decreeNumber = row.decisionNumber\n"+
			"    // Если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «12», то «Графа 5» = «2», иначе «Графа 5» = «1»\n"+
			"    newRow.dividendType = (row.firstMonth == 1 && row.lastMonth == 12) ? '2' : '1'\n"+
			"    // «Графа 6» = «Графа 12» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы\n"+
			"    newRow.dividendSumRaspredPeriod = row.allSum\n"+
			"    // «Графа 7» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы\n"+
			"    newRow.dividendSumNalogAgent = rowList.sum{ it.dividends ?: 0 }\n"+
			"    // «Графа 8» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 16» первичной формы = «1» и «Графа 17» первичной формы не равна «RUS»\n"+
			"    newRow.dividendForgeinOrgAll = rowList.sum{ (it.type == 1 && it.status != 1 && it.dividends != null) ? it.dividends : 0 }\n"+
			"    // «Графа 9» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 16» первичной формы = «2» и «Графа 17» первичной формы не равна «RUS»\n"+
			"    newRow.dividendForgeinPersonalAll = rowList.sum{ (it.type == 2 && it.status != 1 && it.dividends != null) ? it.dividends : 0 }\n"+
			"    // «Графа 10» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы = «0» и «Графа 17» первичной формы не равна «RUS»\n"+
			"    newRow.dividendStavka0 = rowList.sum{ (it.rate == 0 && it.status != 1 && it.dividends != null) ? it.dividends : 0 }\n"+
			"    // «Графа 11» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы <= «5» и «Графа 17» первичной формы не равна «RUS»\n"+
			"    newRow.dividendStavkaLess5 = rowList.sum{ (!(it.rate > 5) && it.status != 1 && it.dividends != null) ? it.dividends : 0 }\n"+
			"    // «Графа 12» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы > «5» и <= «10» и «Графа 17» первичной формы не равна «RUS»\n"+
			"    newRow.dividendStavkaMore5 = rowList.sum{ ((it.rate > 5 && !(it.rate > 10)) && it.status != 1 && it.dividends != null) ? it.dividends : 0 }\n"+
			"    // «Графа 13» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 22» первичной формы > «10» и «Графа 17» первичной формы не равна «RUS»\n"+
			"    newRow.dividendStavkaMore10 = rowList.sum{ ((it.rate > 10) && it.status != 1 && it.dividends != null) ? it.dividends : 0 }\n"+
			"    // «Графа 14» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS»\n"+
			"    newRow.dividendRussianMembersAll = rowList.sum{ (it.status == 1 && it.dividends != null) ? it.dividends : 0 }\n"+
			"    // «Графа 15» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «9»\n"+
			"    newRow.dividendRussianOrgStavka9 = rowList.sum{ (it.status == 1 && it.type == 1 && it.rate == 9 && it.dividends != null) ? it.dividends : 0 }\n"+
			"    // «Графа 16» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «0»\n"+
			"    newRow.dividendRussianOrgStavka0 = rowList.sum{ (it.status == 1 && it.type == 1 && it.rate == 0 && it.dividends != null) ? it.dividends : 0 }\n"+
			"    // «Графа 17» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «2»\n"+
			"    newRow.dividendPersonRussia = rowList.sum{ (it.status == 1 && it.type == 2 && it.dividends != null) ? it.dividends : 0 }\n"+
			"    // «Графа 18» = Сумма по «Графа 23» для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, если «Графа 17» первичной формы = 4\n"+
			"    newRow.dividendMembersNotRussianTax = rowList.sum{ (it.status == '4' && it.dividends != null) ? it.dividends : 0 }\n"+
			"    // Если «Графа 2» первичной формы = «ОАО Сбербанк России» и «Графа 3» первичной формы = «7707083893», то «Графа 19» = «Графа 4» первичной формы для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, иначе не заполняется\n"+
			"    newRow.dividendAgentAll = ((row.emitentName == sbString || row.emitentName == sbString2) && row.emitentInn == graph3String && row.all != null) ? row.all : null\n"+
			"    // Если «Графа 2» первичной формы = «ОАО Сбербанк России» и «Графа 3» первичной формы = «7707083893», то «Графа 20» = («Графа 4» первичной формы – «Графа 5» первичной формы) для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, иначе «Графа 20» = «Графа 6» первичной формы для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы.\n"+
			"    newRow.dividendAgentWithStavka0 = ((row.emitentName == sbString || row.emitentName == sbString2) && row.emitentInn == graph3String) ? ((row.all ?: 0) - (row.rateZero ?: 0)) : (row.distributionSum ?: 0)\n"+
			"    // Если «Графа 2» первичной формы = «ОАО Сбербанк России» и «Графа 3» первичной формы = «7707083893», то «Графа 21» = («Графа 12» первичной формы – («Графа 4» первичной формы – «Графа 5» первичной формы)) для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы, иначе «Графа 21» = («Графа 12» первичной формы - «Графа 6» первичной формы) для каждого уникального сочетания «Графа 7» первичной формы и «Графа 8» первичной формы.\n"+
			"    newRow.dividendSumForTaxAll = ((row.emitentName == sbString || row.emitentName == sbString2) && row.emitentInn == graph3String) ? ((row.allSum ?: 0) - ((row.all ?: 0) - (row.rateZero ?: 0))) : ((row.allSum ?: 0) - (row.distributionSum ?: 0))\n"+
			"    // Если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «9», то «Графа 22» = («Графа 23» первичной формы / «Графа 12» первичной формы * «Графа 6»)\n"+
			"    newRow.dividendSumForTaxStavka9 = rowList.sum{ (it.status == 1 && it.type == 1 && it.rate == 9 && it.dividends && it.allSum && it.distributionSum) ? (it.dividends / it.allSum * it.distributionSum) : 0 }\n"+
			"    // Если «Графа 17» первичной формы = «RUS» и «Графа 16» первичной формы = «1» и «Графа 22» первичной формы = «0», то «Графа 23» = («Графа 23» первичной формы / «Графа 12» первичной формы * «Графа 6»)\n"+
			"    newRow.dividendSumForTaxStavka0 = rowList.sum{ (it.status == 1 && it.type == 1 && it.rate == 0 && it.dividends && it.allSum && it.distributionSum) ? (it.dividends / it.allSum * it.distributionSum) : 0 }\n"+
			"    // Графа 24: Принимает значение: Если графа 17 = RUS, графа 16 = 1 (ЮЛ) ∑ Граф 27 для одного Решения (графа 7-8)\n"+
			"    newRow.taxSum = rowList.sum{ (it.status == 1 && it.type == 1 && it.withheldSum != null) ? it.withheldSum : 0 }\n"+
			"    // Графа 25: Принимает значение: Если графа 17 = RUS, графа 16 = 1 (ЮЛ) ∑ Граф 27 для одного Решения (графа 7-8) если дата по графе 28 принадлежит ПРЕДЫДУЩЕМУ отчетному периоду\n"+
			"    newRow.taxSumFromPeriod = rowList.sum{ (it.status == 1 && it.type == 1 && it.withheldDate != null && it.withheldDate.before(prevPeriodEndDate) && it.withheldDate.after(prevPeriodStartDate) && it.withheldSum != null) ? it.withheldSum : 0 }\n"+
			"    // Графа 26: Принимает значение: Если графа 17 = RUS, графа 16 = 1 (ЮЛ) ∑ Граф 27 для одного Решения (графа 7-8) если дата по графе 28 принадлежит последнему кварталу отчетного периода\n"+
			"    newRow.taxSumFromPeriodAll = rowList.sum{ (it.status == 1 && it.type == 1 && it.withheldDate != null && it.withheldDate.before(lastPeriodEndDate) && it.withheldDate.after(lastPeriodStartDate) && it.withheldSum != null) ? it.withheldSum : 0 }\n"+
			"\n"+
			"    return newRow\n"+
			"}\n"+
			"\n"+
			"def calcPeriod(def firstMonth, def lastMonth) {\n"+
			"    // «Графа 2» = «21», если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «3».\n"+
			"    if (firstMonth==1 && lastMonth==3) {\n"+
			"        return '21'\n"+
			"    }\n"+
			"    // «Графа 2» = «31», если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «6».\n"+
			"    if (firstMonth==1 && lastMonth==6) {\n"+
			"        return '31'\n"+
			"    }\n"+
			"    // «Графа 2» = «33», если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «9».\n"+
			"    if (firstMonth==1 && lastMonth==9) {\n"+
			"        return '33'\n"+
			"    }\n"+
			"    // «Графа 2» = «34», если «Графа 10» первичной формы = «1» и «Графа 11» первичной формы = «12».\n"+
			"    if (firstMonth==1 && lastMonth==12) {\n"+
			"        return '34'\n"+
			"    }\n"+
			"    // «Графа 2» = «35», если «Графа 11» первичной формы - «Графа 10» первичной формы = «0».\n"+
			"    if ((lastMonth - firstMonth) == 0) {\n"+
			"        return '35'\n"+
			"    }\n"+
			"    // «Графа 2» = «36», если «Графа 11» первичной формы - «Графа 10» первичной формы = «1».\n"+
			"    if ((lastMonth - firstMonth) == 1) {\n"+
			"        return '36'\n"+
			"    }\n"+
			"    // «Графа 2» = «37», если «Графа 11» первичной формы - «Графа 10» первичной формы = «2».\n"+
			"    if ((lastMonth - firstMonth) == 2) {\n"+
			"        return '37'\n"+
			"    }\n"+
			"    // «Графа 2» = «38», если «Графа 11» первичной формы - «Графа 10» первичной формы = «3».\n"+
			"    if ((lastMonth - firstMonth) == 3) {\n"+
			"        return '38'\n"+
			"    }\n"+
			"    // «Графа 2» = «39», если «Графа 11» первичной формы - «Графа 10» первичной формы = «4».\n"+
			"    if ((lastMonth - firstMonth) == 4) {\n"+
			"        return '39'\n"+
			"    }\n"+
			"    // «Графа 2» = «40», если «Графа 11» первичной формы - «Графа 10» первичной формы = «5».\n"+
			"    if ((lastMonth - firstMonth) == 5) {\n"+
			"        return '40'\n"+
			"    }\n"+
			"    // «Графа 2» = «41», если «Графа 11» первичной формы - «Графа 10» первичной формы = «6».\n"+
			"    if ((lastMonth - firstMonth) == 6) {\n"+
			"        return '41'\n"+
			"    }\n"+
			"    // «Графа 2» = «42», если «Графа 11» первичной формы - «Графа 10» первичной формы = «7».\n"+
			"    if ((lastMonth - firstMonth) == 7) {\n"+
			"        return '42'\n"+
			"    }\n"+
			"    // «Графа 2» = «43», если «Графа 11» первичной формы - «Графа 10» первичной формы = «8».\n"+
			"    if ((lastMonth - firstMonth) == 8) {\n"+
			"        return '43'\n"+
			"    }\n"+
			"    // «Графа 2» = «44», если «Графа 11» первичной формы - «Графа 10» первичной формы = «9».\n"+
			"    if ((lastMonth - firstMonth) == 9) {\n"+
			"        return '44'\n"+
			"    }\n"+
			"    // «Графа 2» = «45», если «Графа 11» первичной формы - «Графа 10» первичной формы = «10».\n"+
			"    if ((lastMonth - firstMonth) == 10) {\n"+
			"        return '45'\n"+
			"    }\n"+
			"    // «Графа 2» = «46», если «Графа 11» первичной формы - «Графа 10» первичной формы = «11»\n"+
			"    if ((lastMonth - firstMonth) == 11) {\n"+
			"        return '46'\n"+
			"    }\n"+
			"}\n"+
			"\n"+
			"void importData() {\n"+
			"    int COLUMN_COUNT = 26\n"+
			"    int HEADER_ROW_COUNT = 5\n"+
			"    String TABLE_START_VALUE = 'Отчетный год'\n"+
			"    String TABLE_END_VALUE = null\n"+
			"\n"+
			"    def allValues = []      // значения формы\n"+
			"    def headerValues = []   // значения шапки\n"+
			"    def paramsMap = ['rowOffset' : 0, 'colOffset' : 0]  // мапа с параметрами (отступы сверху и слева)\n"+
			"\n"+
			"    checkAndReadFile(ImportInputStream, UploadFileName, allValues, headerValues, TABLE_START_VALUE, TABLE_END_VALUE, HEADER_ROW_COUNT, paramsMap)\n"+
			"\n"+
			"    // проверка шапки\n"+
			"    checkHeaderXls(headerValues, COLUMN_COUNT, HEADER_ROW_COUNT)\n"+
			"    if (logger.containsLevel(LogLevel.ERROR)) {\n"+
			"        return\n"+
			"    }\n"+
			"    // освобождение ресурсов для экономии памяти\n"+
			"    headerValues.clear()\n"+
			"    headerValues = null\n"+
			"\n"+
			"    def fileRowIndex = paramsMap.rowOffset\n"+
			"    def colOffset = paramsMap.colOffset\n"+
			"    paramsMap.clear()\n"+
			"    paramsMap = null\n"+
			"\n"+
			"    def rowIndex = 0\n"+
			"    def rows = []\n"+
			"    def allValuesCount = allValues.size()\n"+
			"    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time\n"+
			"\n"+
			"    // формирвание строк нф\n"+
			"    for (def i = 0; i < allValuesCount; i++) {\n"+
			"        rowValues = allValues[0]\n"+
			"        fileRowIndex++\n"+
			"        // все строки пустые - выход\n"+
			"        if (!rowValues) {\n"+
			"            allValues.remove(rowValues)\n"+
			"            rowValues.clear()\n"+
			"            break\n"+
			"        }\n"+
			"        // простая строка\n"+
			"        rowIndex++\n"+
			"        def newRow = getNewRowFromXls(rowValues, colOffset, fileRowIndex, rowIndex)\n"+
			"        rows.add(newRow)\n"+
			"        // освободить ненужные данные - иначе не хватит памяти\n"+
			"        allValues.remove(rowValues)\n"+
			"        rowValues.clear()\n"+
			"    }\n"+
			"\n"+
			"    showMessages(rows, logger)\n"+
			"    if (!logger.containsLevel(LogLevel.ERROR)) {\n"+
			"        updateIndexes(rows)\n"+
			"        formDataService.getDataRowHelper(formData).allCached = rows\n"+
			"    }\n"+
			"}\n"+
			"\n"+
			"/**\n"+
			" * Проверить шапку таблицы\n"+
			" *\n"+
			" * @param headerRows строки шапки\n"+
			" * @param colCount количество колонок в таблице\n"+
			" * @param rowCount количество строк в таблице\n"+
			" */\n"+
			"void checkHeaderXls(def headerRows, def colCount, rowCount) {\n"+
			"    if (headerRows.isEmpty() || headerRows.size() < rowCount) {\n"+
			"        throw new ServiceException(WRONG_HEADER_ROW_SIZE)\n"+
			"    }\n"+
			"    checkHeaderSize(headerRows[rowCount - 1].size(), headerRows.size(), colCount, rowCount)\n"+
			"    (0..25).each { index ->\n"+
			"        headerMapping.put((headerRows[4][index]), (index + 1).toString())\n"+
			"    }\n"+
			"    checkHeaderEquals(headerMapping, logger)\n"+
			"}\n"+
			"\n"+
			"/**\n"+
			" * Получить новую строку нф по значениям из экселя.\n"+
			" *\n"+
			" * @param values список строк со значениями\n"+
			" * @param colOffset отступ в колонках\n"+
			" * @param fileRowIndex номер строки в тф\n"+
			" * @param rowIndex строка в нф\n"+
			" */\n"+
			"    // графа 2\n"+
			"    def colIndex = 0\n"+
			"    def yearStr = values[colIndex]\n"+
			"    if (yearStr != null) {\n"+
			"        if (yearStr.contains(\".\")) {\n"+
			"            newRow.financialYear = parseDate(yearStr, \"dd.MM.yyyy\", fileRowIndex, colIndex + colOffset, logger, true)\n"+
			"        } else {\n"+
			"            def yearNum = parseNumber(yearStr, fileRowIndex, colIndex + colOffset, logger, true)\n"+
			"            if (yearNum != null && yearNum != 0) {\n"+
			"                newRow.financialYear = new GregorianCalendar(yearNum as Integer, Calendar.JANUARY, 1).getTime()\n"+
			"            }\n"+
			"        }\n"+
			"    }\n"+
			"\n"+
			"    // графа 2..4\n"+
			"    ['taxPeriod', 'emitent', 'decreeNumber', 'dividendType'].each { alias ->\n"+
			"        colIndex++\n"+
			"        newRow[alias] = values[colIndex]\n"+
			"    }\n"+
			"\n"+
			"    // графа 5..26\n"+
			"    }\n"+
			"\n"+
			"    return newRow\n"+
			"}";

	@Test
	public void canExecuteScriptTest() {
		assertTrue(canExecuteScript(SCRIPT1, FormDataEvent.CREATE));
		assertFalse(canExecuteScript(SCRIPT1, FormDataEvent.IMPORT));
		assertFalse(canExecuteScript(SCRIPT1, FormDataEvent.CALCULATE));
		assertFalse(canExecuteScript(SCRIPT1, FormDataEvent.CHECK));
		assertTrue(canExecuteScript(SCRIPT2, FormDataEvent.CREATE));
		assertFalse(canExecuteScript("  ", FormDataEvent.IMPORT));
		assertFalse(canExecuteScript(null, FormDataEvent.IMPORT));
		assertFalse(canExecuteScript(null, null));
		assertTrue(canExecuteScript("test", null));
	}
}
