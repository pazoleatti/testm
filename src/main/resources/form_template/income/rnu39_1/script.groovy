package form_template.income.rnu39_1

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

/**
 * Скрипт для РНУ-39.1
 * Форма "(РНУ-39.1) Регистр налогового учёта процентного дохода по коротким позициям. Отчёт 1(месячный)
 *
 *  @author bkinzyabulatov
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        allCheck()
        break
    case FormDataEvent.CALCULATE :
        calc()
        allCheck()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED : // после принятия из подготовлена
        allCheck()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        allCheck()
        break
}

/**
 * Графа 1  currencyCode        Код валюты номинала
 * Графа 2  issuer              Эмитент
 * Графа 3  regNumber           Номер государственной регистрации
 * Графа 4  amount              Количество облигаций (шт.)
 * Графа 5  cost                Номинальная стоимость лота (руб.коп.)
 * Графа 6  shortPositionOpen   Дата открытия короткой позиции
 * Графа 7  shortPositionClose  Дата закрытия короткой позиции
 * Графа 8  pkdSumOpen          Сумма ПКД полученного при открытии короткой позиции
 * Графа 9  pkdSumClose         Сумма ПКД уплаченного при закрытии короткой позиции
 * Графа 10 maturityDatePrev    Дата погашения предыдущего купона
 * Графа 11 maturityDateCurrent Дата погашения текущего купона
 * Графа 12 currentCouponRate   Ставка текущего купона (% годовых)
 * Графа 13 incomeCurrentCoupon Объявленный доход по текущему купону (руб.коп.)
 * Графа 14 couponIncome        Выплачиваемый купонный доход (руб.коп.)
 * Графа 15 totalPercIncome     Всего процентный доход (руб.коп.)
 */

void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(formData.reportPeriodId, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    if (findForm != null) {
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

def allCheck() {
    return !hasError() && logicalCheck() && checkNSI()
}

def boolean logicalCheck(){
    def reportDate = reportDate
    def rows = getRows(data)
    for (def row : rows){
        def errStart = getRowIndexString(row)
        if (isFixed(row)){
            continue
        }
        if (!checkRequiredColumns(row, getRequiredCols())){
            return false
        }
        if (row.shortPositionOpen > reportDate){
            logger.error("$errStart неверно указана дата первой части сделки!")
            return false
        }
        if ((isInASector(row) && row.shortPositionClose==null) || (!isInASector(row) && row.shortPositionClose > reportDate)){
            logger.error("$errStart неверно указана дата второй части сделки!")
            return false
        }
        def values = getValues(row, null)
        for (def colName : values.keySet()) {
            if (row[colName] != values[colName]) {
                def columnName = row.getCell(colName).getColumn().getName().replace('%', '%%')
                def rowStart = getRowIndexString(row)
                logger.error("${rowStart}поле $columnName заполнено неверно!")
                return false
            }
        }
    }
    // Проверка итоговых значений формы
    for (def section : ['A1', 'A2', 'A3', 'A4', 'B1', 'B2', 'B3', 'B4']) {
        firstRow = data.getDataRow(rows,section)
        lastRow = data.getDataRow(rows,'total' + section)
        for (def column : totalCols) {
            if (lastRow.getCell(column).getValue().equals(getSum(rows, column, firstRow, lastRow))) {
                def columnName = lastRow.getCell(column).getColumn().getName().replace('%', '%%')
                logger.error("Неверно рассчитано итоговое значение графы $columnName!")
                return false
            }
        }
    }

    return true
}

def boolean checkNSI(){
    def rows = getRows(data)
    for (def row : rows){
        if (isFixed(row)){
            continue
        }
        def errStart = getRowIndexString(row)
        if (row.currencyCode!=null && getCurrency(row.currencyCode)==null) {
            logger.warn("${errStart}неверный код валюты!")
        }
    }
    return true
}

void calc(){
    def data = data

    // проверить обязательные редактируемые поля
    for (def DataRow row : getRows(data)){
        if (!isFixed(row)){
            if (!checkRequiredColumns(row, requiredCols.intersect(editableCols))){
                return
            }
        }
    }
    sort()

    def rows = getRows(data)
    // расчет ячеек
    rows.each{row->
        if (!isFixed(row)) {
            getValues(row, row)
        }
    }
    // расчет итогов
    ['A1', 'A2', 'A3', 'A4', 'B1', 'B2', 'B3', 'B4'].each { section ->
        firstRow = data.getDataRow(rows,section)
        lastRow = data.getDataRow(rows,'total' + section)
        getTotalCols().each {
            lastRow[it] = getSum(rows, it, firstRow, lastRow)
        }
    }
    data.update(rows)
}

void addNewRow(){
    def data = data
    def rows = getRows(data)
    DataRow<Cell> newRow = getNewRow()
    if (currentDataRow == null || currentDataRow.getIndex() == -1) {
        def row = data.getDataRow(rows,'totalA1')
        data.insert(newRow,rows.indexOf(row)+1)
    } else if (currentDataRow.getAlias() == null) {
        data.insert(newRow, currentDataRow.getIndex()+1)
    } else {
        def alias = currentDataRow.getAlias()
        def totalAlias = alias.contains('total') ? alias : 'total' + ((alias in ['A', 'B'])? (alias + '1'):alias)
        def row = data.getDataRow(rows, totalAlias)
        data.insert(newRow, rows.indexOf(row)+1)
    }
}

void deleteRow(){
    if (!isFixed(currentDataRow)) {
        data.delete(currentDataRow)
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = data
    // удалить нефиксированные строки
    def deleteRows = []
    getRows(data).each { row ->
        if (!isFixed(row)) {
            deleteRows += row
        }
    }
    data.delete(deleteRows)

    // собрать из источников строки и разместить соответствующим разделам
    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def FormData source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                // подразделы
                ['A1', 'A2', 'A3', 'A4', 'B1', 'B2', 'B3', 'B4'].each { section ->
                    copyRows(source, formData, section, 'total' + section)
                }
            }
        }
    }
    data.save(getRows(data))
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Копировать заданный диапозон строк из источника в приемник.
 *
 * @param sourceForm форма источник
 * @param destinationForm форма приемник
 * @param fromAlias псевдоним строки с которой копировать строки (НЕ включительно),
 *      если = null, то копировать с 0 строки
 * @param toAlias псевдоним строки до которой копировать строки (НЕ включительно),
 *      в приемник строки вставляются перед строкой с этим псевдонимом
 */
void copyRows(def FormData sourceForm, def FormData destinationForm, def fromAlias, def toAlias) {
    def sourceData = getData(sourceForm)
    def destinationData = getData(destinationForm)
    def from = getIndexByAlias(sourceData, fromAlias) + 1
    def to = getIndexByAlias(sourceData, toAlias)
    if (from > to) {
        return
    }

    def copyRows = getRows(sourceData).subList(from, to)
    getRows(destinationData).addAll(getIndexByAlias(destinationData, toAlias), copyRows)
    // поправить индексы, потому что они после вставки не пересчитываются
    getRows(destinationData).eachWithIndex { row, i ->
        row.setIndex(i + 1)
    }
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Получить helper формы.
 */
def DataRowHelper getData() {
    return getData(formData)
}

def DataRowHelper getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

/**
 * Получить строки формы.
 */
def List<DataRow<Cell>> getRows(def DataRowHelper data) {
    return data.getAllCached();
}

/**
 * Проверка является ли строка фиксированной.
 */
def isFixed(def row) {
    return row != null && row.getAlias() != null
}

/**
 * Получить новую строку с заданными стилями.
 */
def DataRow getNewRow() {
    def row = formData.createDataRow()
    getEditableCols().each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
    return row
}

def List<String> getAllCols(){
    return ["currencyCode", "issuer", "regNumber", "amount", "cost",
            "shortPositionOpen", "shortPositionClose", "pkdSumOpen",
            "pkdSumClose", "maturityDatePrev", "maturityDateCurrent",
            "currentCouponRate", "incomeCurrentCoupon", "couponIncome",
            "totalPercIncome"]
}

def List<String> getTotalCols(){
    return ["amount", "cost", "pkdSumOpen", "pkdSumClose", "couponIncome", "totalPercIncome"]
}

def List<String> getEditableCols(){
    return ["currencyCode", "issuer", "regNumber", "amount", "cost",
            "shortPositionOpen", "shortPositionClose", "pkdSumOpen",
            "pkdSumClose", "maturityDatePrev", "maturityDateCurrent",
            "currentCouponRate", "incomeCurrentCoupon"]
}

//TODO не все так просто (пока все поля)
def List<String> getRequiredCols(){
    return ["currencyCode", "issuer", "regNumber", "amount", "cost",
            "shortPositionOpen", "shortPositionClose", "pkdSumOpen",
            "pkdSumClose", "maturityDatePrev", "maturityDateCurrent",
            "currentCouponRate", "incomeCurrentCoupon", "couponIncome",
            "totalPercIncome"]
}

def List<String> getSortColumns(){
    return ["currencyCode", "issuer"]
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def DataRow row, def ArrayList<String> columns) {
    def colNames = []
    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def errorBegin = getRowIndexString(row)
        def errorMsg = colNames.join(', ')
        logger.error(errorBegin+ "не заполнены колонки : $errorMsg.")
        return false
    }
    return true
}

/**
 * Начало предупреждений/ошибок
 * @param row
 * @return
 */
def String getRowIndexString(def DataRow row){
    def index = getIndex(row)+1
    return "В строке $index "
}

/**
 * Отсортировать / группировать строки
 */
void sort() {
    def data = data
    def rows = getRows(data)
    def sortRows = []
    def from
    def to

    ['A1', 'A2', 'A3', 'A4', 'B1', 'B2', 'B3', 'B4'].each { section ->
        from = getIndexByAlias(data, section) + 1
        to = getIndexByAlias(data, 'total'+section) - 1
        if (from<=to) {
            sortRows.add(rows[from..to])
        }

    }
    sortRows.each {
        it.sort {  DataRow a, DataRow b ->
            if (isFixed(a) || isFixed(b)){
                return 0
            }
            def aD = getCurrency(a.currencyCode)
            def bD = getCurrency(b.currencyCode)
            if (aD != bD) {
                return aD <=> bD
            } else {
                aD = a.issuer
                bD = b.issuer
                return aD <=> bD
            }
        }
    }
    data.save(rows)
}

/**
 * Получить номер строки в таблице по псевдонимиу (0..n).
 */
def getIndexByAlias(def DataRowHelper data, String rowAlias) {
    def row = getRowByAlias(data,rowAlias)
    return (row != null ? getIndex(row) : -1)
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф
 * @param alias алиас
 */
def getRowByAlias(def DataRowHelper data, def alias) {
    if (alias == null || alias == '' || data == null) {
        return null
    }
    return data.getDataRow(getRows(data),alias)
}

/**
 * получаем мапу со значениями, расчитанными для каждой конкретной строки
 */
def getValues(def row, def result) {
    if(result == null){
        result = [:]
    }

    result.with {
        couponIncome = getGraph14(row)
        totalPercIncome = getGraph15(row)
    }

    return result
}

def getGraph14(def row){
    def boolean cond = false
    if (isInASector(row)){
        def dateEnd = reportPeriodService.getEndDate(formData.reportPeriodId).getTime()
        def dateStart = reportPeriodService.getStartDate(formData.reportPeriodId).getTime()
        if(row.maturityDateCurrent in (dateStart..dateEnd)){
            cond = true
        }
    } else {
        if(row.maturityDateCurrent in ((row.shortPositionOpen)..(row.shortPositionClose))){
            cond = true
        }
    }
    if (cond){
        if(isRubleCurrency(row.currencyCode)){
            return row.amount * row.incomeCurrentCoupon
        } else {
            return roundTo(row.currentCouponRate * row.cost * (row.maturityDateCurrent - row.maturityDatePrev) / 360 *
                    getCourse(row.currencyCode, row.maturityDateCurrent), 2)
        }
    } else {
        return 0
    }
}

def getGraph15(def row){
    if (isInASector(row)){
        return row.couponIncome
    }else{
        return row.pkdSumClose + row.couponIncome - row.pkdSumOpen
    }
}

def boolean isInASector(def row){
    def rowB = getRowByAlias(data, 'B')
    return getRows(data).indexOf(row)<getRows(data).indexOf(rowB)
}

/**
 * Получить цифровой код валюты
 */
def getCurrency(def currencyCode) {
    return  refBookService.getStringValue(15,currencyCode,'CODE')
}

/**
 * Проверка валюты на рубли
 */
def isRubleCurrency(def currencyCode) {
    return  refBookService.getStringValue(15,currencyCode,'CODE')=='810'
}

/**
 * Получить курс валюты
 */
def getCourse(def currency, def date) {
    if (currency!=null) {
        def refCourseDataProvider = refBookFactory.getDataProvider(22)
        def res = refCourseDataProvider.getRecords(date, null, 'CODE_NUMBER='+currency, null);
        return res.getRecords().get(0).RATE.getNumberValue()
    } else {
        return null
    }
}

/**
 * Хелпер для округления чисел
 * @param value
 * @param newScale
 * @return
 */
BigDecimal roundTo(BigDecimal value, int newScale) {
    if (value != null) {
        return value.setScale(newScale, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

/**
 * Получить сумму столбца.
 */
def getSum(def rows, def columnAlias, def rowStart, def rowEnd) {
    def from = getIndex(rowStart) + 1
    def to = getIndex(rowEnd) - 1
    if (from > to) {
        return 0
    }
    return summ(formData, rows, new ColumnRange(columnAlias, from, to))
}

def getReportDate(){
    def tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    return (tmp ? tmp.getTime() + 1 : null)
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    return row.getIndex() - 1
}