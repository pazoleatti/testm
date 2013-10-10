package form_template.income.rnu62

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

/**
 * Скрипт для РНУ-62
 * Форма "(РНУ-62) Регистр налогового учёта расходов по дисконтным векселям ОАО «Сбербанк России»"
 *
 * TODO походу расчеты еще изменят
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
 * Графа 1  rowNumber           № пп
 * Графа 2  billNumber          Номер векселя
 * Графа 3  creationDate        Дата составления
 * Графа 4  nominal             Номинал
 * Графа 5  sellingPrice        Цена реализации
 * Графа 6  currencyCode        Код валюты - Справочник 15 - 64 атрибут CODE
 * Графа 7  rateBRBillDate      Курс Банка России на дату составления векселя
 * Графа 8  rateBROperationDate Курс Банка России на дату совершения операции
 * Графа 9  paymentTermStart    Дата наступления срока платежа
 * Графа 10 paymentTermEnd      Дата окончания срока платежа
 * Графа 11 interestRate        Процентная ставка
 * Графа 12 operationDate       Дата совершения операции
 * Графа 13 rateWithDiscCoef    Ставка с учётом дисконтирующего коэффициента
 * Графа 14 sumStartInCurrency  Сумма дисконта начисленного на начало отчётного периода в валюте
 * Графа 15 sumStartInRub       Сумма дисконта начисленного на начало отчётного периода в рублях
 * Графа 16 sumEndInCurrency    Сумма дисконта начисленного на конец отчётного периода в валюте
 * Графа 17 sumEndInRub         Сумма дисконта начисленного на конец отчётного периода в рублях
 * Графа 18 sum                 Сумма дисконта начисленного за отчётный период (руб.)
 *
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
    def numbers = []
    def totalRow = null
    for (def DataRow row : getRows(data)){
        if(!isTotal(row) && !checkRequiredColumns(row,requiredCols)){
            return false
        } else if (isTotal(row)) {
            totalRow = row
            continue
        }
        def rowStart = getRowIndexString(row)
        def dateEnd = reportPeriodService.getEndDate(formData.reportPeriodId).getTime()
        def dateStart = reportPeriodService.getStartDate(formData.reportPeriodId).getTime()
        if (row.operationDate.compareTo(dateStart)<0 || row.operationDate.compareTo(dateEnd)>0){
            logger.error("${rowStart}дата совершения операции вне границ отчетного периода!")
            return false
        }
        if (row.rowNumber in numbers){
            logger.error("${rowStart}нарушена уникальность номера по порядку ${row.number}!")
            return false
        }else {
            numbers += row.rowNumber
        }
        if (isZeroEmpty(row.sumStartInCurrency) &&
                isZeroEmpty(row.sumStartInRub) &&
                isZeroEmpty(row.sumEndInCurrency) &&
                isZeroEmpty(row.sumEndInRub) &&
                isZeroEmpty(row.sum)){
            logger.error("${rowStart}все суммы по операции нулевые!")
            return false
        }
        //TODO проверить, в аналитике неадекватно описано
        //FIXME раскомментить
//        if(getRowPrev(row)==null){
//            logger.error("${rowStart}отсутствуют данные в РНУ-62 за предыдущий отчетный период!")
//            return false
//        }
        def values = getValues(row, null)
        for (def colName : calcColsWithoutNSI) {
            if (row[colName] != values[colName]) {
                isValid = false
                def columnName = row.getCell(colName).getColumn().getName().replace('%', '%%')
                logger.error("${rowStart}неверно рассчитана графа \"$columnName\"!")
                return false
            }
        }
    }
    if (totalRow == null || (totalRow.sum != getTotalRow(data, true).sum)){
        logger.error("Итоговые значения рассчитаны неверно!")
        return false
    }
    return true
}

def boolean checkNSI(){
    for (def row : getRows(data)) {
        def rowStart = getRowIndexString(row)
        if (row.currencyCode!=null && getCurrency(row.currencyCode)==null) {
            logger.warn("$rowStart неверный код валюты!")
        }
        if (row.rateBRBillDate != getGraph7(row)){
            logger.warn("$rowStart неверный курс валюты на дату составления векселя!")
        }
        if (row.rateBROperationDate != getGraph8(row)){
            logger.warn("$rowStart неверный курс валюты на дату совершения операции!")
        }
    }
    return true
}

void calc(){
    def data = data

    // проверить обязательные редактируемые поля
    for (def DataRow row : getRows(data)){
        if (!isTotal(row) && !checkRequiredColumns(row, requiredCols.intersect(editableCols))){
            return
        }
    }

    // удалить строку "итого"
    def delRow = []
    getRows(data).each {
        if (isTotal(it)) {
            delRow.add(it)
        }
    }
    delRow.each {
        data.delete(it)
    }

    def rows = getRows(data)
    if (rows.isEmpty()) {
        return
    }

    // сортировка
    rows.sort({ DataRow a, DataRow b ->
        sortRow(sortColumns, a, b)
    })
    data.save(rows)

    // расчет ячеек
    rows.each{row->
        getValues(row, row)
    }
    data.update(rows)

    //добавление итогов
    totalRow = getTotalRow(data, false)
    data.insert(totalRow, getRows(data).size()+1)
}

void addNewRow(){
    def data = data
    DataRow<Cell> newRow = getNewRow()
    def index = 0
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while(isTotal(row) && index>0){
            row = getRows(data).get(--index)
        }
        if(index!=currentDataRow.getIndex() && !isTotal(getRows(data).get(index))){
            index++
        }
    }else if (getRows(data).size()>0) {
        for(int i = getRows(data).size()-1;i>=0;i--){
            def row = getRows(data).get(i)
            if(!isTotal(row)){
                index = getIndex(row)+1
                break
            }
        }
    }
    data.insert(newRow,index+1)
}

void deleteRow(){
    if (!isTotal(currentDataRow)) {
        data.delete(currentDataRow)
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = data
    // удалить все строки и собрать из источников их строки
    data.clear()

    for (formDataSource in departmentFormTypeService.getFormSources(formData.departmentId, formData.getFormType().getId(), formData.getKind())) {
        if (formDataSource.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(formDataSource.formTypeId, formDataSource.kind, formDataSource.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                sourceForm = getData(source)
                for (row in getRows(sourceForm)) {
                    if (row.getAlias() == null) {
                        data.insert(row, getRows(data).size() + 1)
                    }
                }
            }
        }
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
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Получить новую стролу с заданными стилями.
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
    return ["rowNumber","billNumber", "creationDate", "nominal", "sellingPrice",
            "currencyCode", "rateBRBillDate", "rateBROperationDate",
            "paymentTermStart", "paymentTermEnd", "interestRate",
            "operationDate", "rateWithDiscCoef", "sumStartInCurrency",
            "sumStartInRub", "sumEndInCurrency", "sumEndInRub", "sum"]
}

def List<String> getEditableCols(){
    return ["billNumber", "creationDate", "nominal",
            "sellingPrice", "currencyCode", "rateBRBillDate",
            "rateBROperationDate", "paymentTermStart", "paymentTermEnd",
            "interestRate", "operationDate", "rateWithDiscCoef"]
}

//TODO уточнить
def List<String> getRequiredCols(){
    return ["rowNumber", "billNumber", "creationDate", "nominal",
            "sellingPrice", "currencyCode", "rateBRBillDate",
            "rateBROperationDate", "paymentTermStart", "paymentTermEnd",
            "interestRate", "operationDate", "sumEndInCurrency", "sumEndInRub", "sum"]
}

def List<String> getCalcColsWithoutNSI(){
    return ["sumStartInCurrency", "sumStartInRub", "sumEndInCurrency", "sumEndInRub", "sum"]
}

def List<String> getSortColumns(){
    return ["billNumber", "operationDate"]
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
    def index = row.rowNumber
    if (index != null) {
        return "В строке \"№ пп\" равной $index "
    } else {
        index = getIndex(row) + 1
        return "В строке $index "
    }
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    getRows(data).indexOf(row)
}

int sortRow(List<String> params, DataRow a, DataRow b) {
    for (String param : params) {
        def aD = a.getCell(param).value
        def bD = b.getCell(param).value

        if (aD != bD) {
            return aD <=> bD
        }
    }
    return 0
}

/**
 * получаем мапу со значениями, расчитанными для каждой конкретной строки
 */
def getValues(def row, def result) {
    if(result == null){
        result = [:]
    }

    def rowPrev = getRowPrev(row)
    result.with {
        rowNumber=getGraph1(row)
        rateBRBillDate=getGraph7(row)
        rateBROperationDate=getGraph8(row)
        sumStartInCurrency=getGraph14(row, rowPrev)
        sumStartInRub=getGraph15(rowPrev)
        sumEndInCurrency=getGraph16(row)
        sumEndInRub=getGraph17(row)
        sum=getGraph18(row)
    }

    return result
}

def getGraph1(def row) {
    return getIndex(row)+1
}

def getGraph7(def row) {
    return getCourse(row.currencyCode, row.creationDate)
}

def getGraph8(def row) {
    return getCourse(row.currencyCode, row.operationDate)
}

def getGraph14(def row, def rowPrev) {
    if (row.rateWithDiscCoef!=null){
        return null
    } else {
        if (rowPrev !=null){
            return rowPrev.sumEndInCurrency
        }
    }
}

def getGraph15(def rowPrev) {
    if (rowPrev !=null){
        return rowPrev.sumEndInRub
    }
}

def getGraph16(def row) {
    def tmp
    if (row.operationDate < row.paymentTermStart){
        tmp = (row.nominal - row.sellingPrice)*(row.operationDate - row.creationDate) / (row.paymentTermStart - row.creationDate)
    }
    if (row.operationDate > row.paymentTermStart){
        tmp = row.nominal - row.sellingPrice
    }
    if (row.rateWithDiscCoef != null){
        countDaysInYear = getCountDaysInYear(reportPeriodService.getStartDate(formData.reportPeriodId))
        if (row.interestRate < row.rateWithDiscCoef){
            tmp = row.sellingPrice * (row.operationDate - row.creationDate) / countDaysInYear * row.interestRate / 100
        } else {
            tmp = row.sellingPrice * (row.operationDate - row.creationDate) / countDaysInYear * row.rateWithDiscCoef / 100
        }
    }

    if (getCountDaysInYear(row.creationDate) - getCountDaysInYear(row.paymentTermStart) != 0 ){
        //TODO заполняется вручную, но возможна формула
        tmp = null
    }

    if (getDiffBetweenYears(row.paymentTermEnd, row.operationDate)>=3){
        tmp = 0
    }
    if(!isRubleCurrency(row.currencyCode)){
        tmp = null
    }
    return tmp
}

def getGraph17(def row) {
    def tmp
    if(row.rateWithDiscCoef != null &&
        row.sumStartInCurrency != null &&
        row.sumEndInCurrency != null){
        if(row.operationDate >= row.paymentTermStart){
            tmp = (row.nominal * row.rateBROperationDate)-(row.sellingPrice * row.rateBRBillDate)
        } else {
            //TODO "второй строкой"?
            tmp = roundTo(row.sellingPrice * (row.rateBROperationDate - row.rateBRBillDate),2) + row.sumStartInRub
        }
    } else {
        tmp = (row.sumEndInCurrency?:0) * row.rateBROperationDate
    }
    return tmp
}

def getGraph18(def row) {
    return row.sumEndInRub - row.sellingPrice
}

/**
 * Получить курс валюты
 */
def getCourse(def currency, def date) {
    if (currency!=null && !isRubleCurrency(currency)) {
        def refCourseDataProvider = refBookFactory.getDataProvider(22)
        def res = refCourseDataProvider.getRecords(date, null, 'CODE_NUMBER='+currency, null);
        return res.getRecords().get(0).RATE.getNumberValue()
    } else if ( isRubleCurrency(currency)){
        return 1;
    } else {
        return null
    }
}

/**
 * Проверка валюты на рубли
 */
def isRubleCurrency(def currencyCode) {
    return  refBookService.getStringValue(15,currencyCode,'CODE')=='810'
}

def FormData getFormDataPrev() {
    def reportPeriodPrev = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)
    return reportPeriodPrev? formDataService.find(formData.formType.id, formData.kind, formData.departmentId, reportPeriodPrev.id):null
}

def DataRow getRowPrev(def DataRow row) {
    if (formDataPrev != null) {
        def dataPrev = getData(formDataPrev)
        for(def rowPrev : getRows(dataPrev)){
            if (!isTotal(rowPrev) && row.billNumber!=null && row.billNumber == rowPrev.billNumber){
                return row
            }
        }
    }
    return null
}

/**
 * Количество дней в году за который делаем
 * @return
 */
int getCountDaysInYear(def Date date) {
    def calendar = Calendar.getInstance()
    calendar.setTime(date)
    return countDaysOfYear = (new GregorianCalendar()).isLeapYear(calendar.get(Calendar.YEAR)) ? 366 : 365
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

def getTotalRow(DataRowHelper data, def totalExist) {
    def newRow = formData.createDataRow()
    newRow.billNumber = "Итого"
    newRow.setAlias('total')
    def from = 0
    def to = getRows(data).size() - (totalExist?2:1)
    if (from > to) {
        return null
    }
    newRow.sum = summ(formData, getRows(data), new ColumnRange('sum', from, to))
    setTotalStyle(newRow)
    return newRow
}

def boolean isZeroEmpty(def value){
    return value==null || value == 0
}

/**
 * Получить цифровой код валюты
 */
def getCurrency(def currencyCode) {
    return  refBookService.getStringValue(15,currencyCode,'CODE')
}

def int getDiffBetweenYears(def Date dateA, def Date dateB){
    def calendarA = Calendar.getInstance()
    calendarA.setTime(dateA)
    def calendarB = Calendar.getInstance()
    calendarB.setTime(dateB)
    return calendarA.get(Calendar.YEAR) - calendarB.get(Calendar.YEAR)
}

/**
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    allCols.each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}