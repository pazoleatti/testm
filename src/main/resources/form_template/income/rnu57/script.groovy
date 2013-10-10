package form_template.income.rnu57

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.WorkflowState
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper

/**
 * Скрипт для РНУ-57 (rnu57.groovy).
 * (РНУ-57) Регистр налогового учёта финансового результата от реализации (погашения) векселей сторонних эмитентов
 *
 * Версия ЧТЗ: 64
 *
 * @author vsergeev
 *
 * Графы:
 *                  *****   РНУ 57  *****
 * 1    number                  № пп
 * 2    bill                    Вексель
 * 3    purchaseDate            Дата приобретения
 * 4    purchasePrice           Цена приобретения, руб.
 * 5    purchaseOutcome         Расходы, связанные с приобретением,  руб.
 * 6    implementationDate      Дата реализации (погашения)
 * 7    implementationPrice     Цена реализации (погашения), руб.
 * 8    implementationOutcome   Расходы, связанные с реализацией,  руб.
 * 9    price                   Расчётная цена, руб.
 * 10   percent                 Процентный доход, учтённый в целях налогообложения  (для дисконтных векселей), руб.
 * 11   implementationpPriceTax Цена реализации (погашения) для целей налогообложения
 *                              (для дисконтных векселей без процентного дохода),  руб.
 * 12   allIncome               Всего расходы по реализации (погашению), руб.
 * 13   implementationPriceUp   Превышение цены реализации для целей налогообложения над ценой реализации, руб.
 * 14   income                  Прибыль (убыток) от реализации (погашения) руб.
 *
 *                  *****   РНУ 56  *****
 * 1    number              № пп
 * 2    bill                Вексель
 * 3    buyDate             Дата приобретения
 * 4    currency            Код валюты
 * 5    nominal             Номинал, ед. валюты
 * 6    price               Цена приобретения, ед. валюты
 * 7    maturity            Срок платежа
 * 8    termDealBill        Возможный срок обращения векселя, дней
 * 9    percIncome          Заявленный процентный доход (дисконт), ед. валюты
 * 10   implementationDate  Дата реализации (погашения)
 * 11   sum                 Сумма, фактически поступившая в оплату, ед. валюты
 * 12   discountInCurrency  в валюте
 * 13   discountInRub       в рублях по курсу Банка России
 * 14   sumIncomeinCurrency в валюте
 * 15   sumIncomeinRuble    в рублях по курсу Банка России
 *
 *                  *****   РНУ 55  *****
 * 1    number              № пп
 * 2    bill                Вексель
 * 3    buyDate             Дата приобретения
 * 4    currency            Код валюты
 * 5    nominal             Номинал, ед. валюты
 * 6    percent             Процентная ставка
 * 7    implementationDate  Дата реализации (погашения)
 * 8    percentInCurrency   в валюте
 * 9    percentInRuble      в рублях по курсу Банка России
 * 10   sumIncomeinCurrency в валюте
 * 11   sumIncomeinRuble    в рублях по курсу Банка России
 *
 */
switch (formDataEvent) {
    case FormDataEvent.CHECK :
        checkAll()
        break
    case FormDataEvent.CALCULATE :
        if (logicalCheckPreCalc()) {
            calc()
        }
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteCurrentRow()
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED : // после принятия из подготовлена
        checkAll()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        checkAll()
        break
}

boolean checkCalculatedCells() {
    def data = data
    def rows = getRows(data)
    def isValid = true

    def rnu55FormData = getRnu55FormData()
    def rnu56FormData = getRnu56FormData()

    for (def dataRow : rows) {
        if ( ! isInTotalRowsAliases(dataRow.getAlias())) {      //строку итогов не проверяем
            def rnu55Row = getRnu55Row(rnu55FormData, dataRow)
            def rnu56Row = getRnu56Row(rnu56FormData, dataRow)

            def values = getValues(dataRow, rnu55Row, rnu56Row, rnu56FormData)

            for (def colName : values.keySet()) {
                if (dataRow[colName] != values[colName]) {
                    isValid = false
                    def columnName = dataRow.getCell(colName).getColumn().getName().replace('%', '%%')
                    def rowStart = getRowIndexString(dataRow)
                    logger.error("${rowStart}поле $columnName заполнено неверно!")
                    break
                }
            }
        }
    }
    return isValid
}

def calc() {
    def data = data
    calcValues(data)
    calcTotal(data)
    data.save(getRows(data))
}

/**
 * заполняем ячейки, вычисляемые автоматически
 */
def calcValues(def data) {
    def rnu55FormData = getRnu55FormData()
    def rnu56FormData = getRnu56FormData()

    for (def dataRow : getRows(data)) {
        if ( ! isInTotalRowsAliases(dataRow.getAlias())) {      //строку итогов не заполняем
            def rnu55Row = getRnu55Row(rnu55FormData, dataRow)
            def rnu56Row = getRnu56Row(rnu56FormData, dataRow)

            def values = getValues(dataRow, rnu55Row, rnu56Row, rnu56FormData)

            //TODO проверить корректность расчетов
            values.keySet().each { colName ->
                dataRow[colName] = values[colName]
            }
        }
    }
    getRows(data).sort{ it.bill }
}

/**
 * получаем мапу со значениями, расчитанными для каждой конкретной строки
 */
def getValues(def dataRow, def rnu55Row, def rnu56Row, def rnu56FormData) {
    def values = [:]

    values.with {
        purchasePrice = getPurchasePrice(dataRow, rnu55Row, rnu56Row)
        purchaseOutcome = getPurchaseOutcome(dataRow, rnu55Row, rnu56Row)
        percentInRuble = getPercentInRuble(dataRow, rnu55Row, rnu56Row)
        percent = getPercent(dataRow, rnu55Row, rnu56Row, rnu56FormData)
        implementationpPriceTax = getImplementationPriceTax(dataRow, rnu55Row, rnu56Row)
        allIncome = getAllIncome(dataRow)
        implementationPriceUp = getImplementationPriceUp(dataRow)
        income = getIncome(dataRow)
    }

    return values
}

def getRnu55FormData() {
    return formDataService.find(348, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

def getRnu56FormData() {
    return formDataService.find(349, formData.kind, formDataDepartment.id, formData.reportPeriodId)
}

def getRnu55Row(def rnu55formData, def dataRow) {
    def data55 = getData(rnu55formData)
    for(def row55 : getRows(data55)){
        if(dataRow.bill == row55.bill){
            return row55
        }
    }
}

def getRnu56Row(def rnu56formData, def dataRow) {
    def data56 = getData(rnu56formData)
    for(def row56 : getRows(data56)){
        if(dataRow.bill == row56.bill){
            return row56
        }
    }
}

def getPurchasePrice(def dataRow, def rnu55DataRow, def rnu56DataRow) {
    if (isHasTheSameBills(dataRow, rnu55DataRow)) {
        return calcPurchasePrice(rnu55DataRow)
    }
    if (isHasTheSameBills(dataRow, rnu56DataRow)) {
        return calcPurchasePrice(rnu56DataRow)
    }
}

def calcPurchasePrice(def dataRow) {
    dataRow.nominal * getCourseForCurrencyByDate(dataRow.currency, dataRow.buyDate)
}

def getPurchaseOutcome(def dataRow, def rnu55DataRow, def rnu56DataRow) {
    if (isHasTheSameBills(dataRow, rnu55DataRow)) {
        return rnu55DataRow.sumIncomeinRuble
    }
    if (isHasTheSameBills(dataRow, rnu56DataRow)) {
        return rnu56DataRow.sumIncomeinRuble
    }
}

def getPercentInRuble(def dataRow, def rnu55DataRow, def rnu56DataRow) {
    if (isHasTheSameBills(dataRow, rnu56DataRow)) {
        if (rnu56DataRow.maturity == dataRow.implementationDate) {
            return 0
        }

        if (rnu56DataRow.maturity > dataRow.implementationDate) {
            def N = rnu56DataRow.nominal
            def K = rnu56DataRow.price
            def T = rnu56DataRow.maturity - rnu56DataRow.buyDate
            def D = dataRow.implementationDate - rnu56DataRow.buyDate

            //в ЧТЗ, похоже, два условия: для рублей и для нерублей. условия отличаются только домножением на курс валют,
            // но для рубля курс всегда берется равным единице, так что условия можно объединить
            return ((N - K) / T * D + K) * getCourseForCurrencyByDate(rnu56DataRow.currency, dataRow.implementationDate)

        }
    }
    if (isHasTheSameBills(dataRow, rnu55DataRow)) {
        return rnu55DataRow.nominal * getCourseForCurrencyByDate(rnu55DataRow.currency, dataRow.implementationDate)
    }
}

def getPercent(def dataRow, def rnu55DataRow, def rnu56DataRow, def rnu56FormData) {
    def data56 = getData(rnu56FormData)
    if (isHasTheSameBills(dataRow, rnu56DataRow)) {
        return rnu56DataRow.discountInRub
    }
    if (rnu56DataRow.bill == null) {
        def rnu56TotalDataRow = data56.getDataRow(getRows(data56),getTotalDataRowAlias())
        return rnu56TotalDataRow.sumIncomeinRuble
    }
    if (isHasTheSameBills(dataRow, rnu55DataRow)) {
        return 0
    }
}

def getImplementationPriceTax(Object dataRow, Object rnu55DataRow, Object rnu56DataRow) {
    final def tmpValue = 0.8 * dataRow.price
    if (isHasTheSameBills(dataRow, rnu55DataRow)) {
        if (dataRow.implementationPrice >= tmpValue) {
            return dataRow.implementationPrice
        } else {
            return tmpValue
        }
    }
    if (isHasTheSameBills(dataRow, rnu56DataRow)) {
        if (dataRow.implementationPrice >= tmpValue) {
            return dataRow.implementationPrice - dataRow.percent
        } else {
            return tmpValue - dataRow.percent
        }
    }
}

def getAllIncome(def dataRow) {
    return dataRow.purchasePrice + dataRow.purchaseOutcome + dataRow.implementationOutcome
}

def getImplementationPriceUp(def dataRow) {
    def tmpOne = dataRow.implementationpPriceTax - dataRow.implementationPrice
    def tmpTwo = dataRow.implementationpPriceTax + dataRow.percent - dataRow.implementationPrice
    if (dataRow.percent == 0){
        return tmpOne
    }
    if (dataRow.percent > 0){
        if(tmpOne<0 || tmpTwo<0){
            return 0
        } else {
            return tmpTwo
        }
    }
}

def getIncome(def dataRow) {
    return dataRow.implementationpPriceTax - dataRow.allIncome
}

def getCourseForCurrencyByDate(def currency, def date) {
    if (currency!=null && !isRubleCurrency(currency)) {
        def refCourseDataProvider = refBookFactory.getDataProvider(22)
        def res = refCourseDataProvider.getRecords(date, null, 'CODE_NUMBER='+currency, null);
        return res.getRecords().get(0).RATE.getNumberValue()
    } else if (isRubleCurrency(currency)){
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

/**
 * сравниваем графы 2
 */
def isHasTheSameBills(def dataRow1, dataRow2) {
    if (dataRow1.bill == null || dataRow2.bill == null) {
        return false
    }
    return (dataRow1.bill == dataRow2.bill)
}

/********************************   ОБЩИЕ ФУНКЦИИ   ********************************/

/**
 * false, если в строке нет символов или строка null
 * true, если в строке есть символы
 */
boolean isBlankOrNull(value) {
    return (value == null || value.equals(''))
}

/***********   ФУНКЦИИ ДЛЯ ПРОВЕРКИ ОБЯЗАТЕЛЬНЫХ ДЛЯ ЗАПОЛНЕНИЯ ДАННЫХ   ***********/

boolean logicalCheckPreCalc(){
    return checkRequiredColumns() && logicalChecks(false)
}

/**
 * перед расчетами проверяем заполнение только ячеек, доступных для ввода. т.к.
 * они нам нужны для расчетов, а рассчитываемые - не нужны
 */
boolean checkRequiredColumns() {
    return checkColsFilledByAliases(getEditableColsAliases())
}

/**
 * проверяем все данные формы на обязательное и корректное заполнение
 */
boolean checkAll() {
    if (checkColsFilledByAliases(getAllRequiredColsAliases())) {
        return (logicalChecks(true) && checkCalculatedCells() && checkTotalResults())
    }

    return false
}

boolean logicalChecks(boolean checkNumbers){
    def numbers = []
    for (def row : getRows(data)){
        def rowStart = getRowIndexString(row)
        def dateEnd = reportPeriodService.getEndDate(formData.reportPeriodId).getTime()
        def dateStart = reportPeriodService.getStartDate(formData.reportPeriodId).getTime()
        if (row.purchaseDate.compareTo(dateEnd)>0){
            logger.error("${rowStart}дата приобретения вне границ отчетного периода!")
            return false
        }
        if (row.purchaseDate.compareTo(dateStart)<0 || row.purchaseDate.compareTo(dateEnd)>0){
            logger.error("${rowStart}дата реализации (погашения) вне границ отчетного периода!")
            return false
        }
        if (checkNumbers) {
            if (row.number in numbers){
                logger.error("Нарушена уникальность номера по порядку ${row.number}!")
                return false
            }else {
                numbers += row.number
            }
        }
        if (rnu55FormData == null){
            logger.error("Отсутствуют данные в РНУ-55!")
            return false
        }
        if (rnu56FormData == null){
            logger.error("Отсутствуют данные в РНУ-56!")
            return false
        }
    }
    return true
}

/**
 * возвращает список алиасов всех обязательных для заполнения столбцов
 */
def getAllRequiredColsAliases() {
    return ['number', 'bill', 'purchaseDate', 'purchasePrice', 'purchaseOutcome', 'implementationDate', 'implementationPrice',
            'implementationOutcome', 'price', 'percent', 'implementationpPriceTax', 'allIncome',
            'implementationPriceUp', 'income']
}

/**
 * проверяем актуальность итоговых значения
 */
boolean checkTotalResults() {
    def data = data
    def totalDataRow = data.getDataRow(getRows(data),getTotalDataRowAlias())
    def controlTotalResults = getTotalResults()

    for (def colName : controlTotalResults.keySet()) {
        if (totalDataRow[colName] != controlTotalResults[colName]) {
            logger.error('Итоговые значения рассчитаны неверно!')
            return false
        }
    }

    return true
}

/**
 * проверяем заполнения столбцов по алиасам этих столбцов
 */
boolean checkColsFilledByAliases(List colsAliases) {
    def data = data
    def rows = getRows(data)
    for (def dataRow in rows){
        if (!isInTotalRowsAliases(dataRow.getAlias()) && !checkRequiredColumns(dataRow,colsAliases)) {
            return false
        }
    }
    return true
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(DataRow row, Object columns) {
    def colNames = []

    columns.each { String col ->
        if (row.getCell(col).getValue() == null || ''.equals(row.getCell(col).getValue())) {
            def name = row.getCell(col).getColumn().getName().replace('%', '%%')
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

/***********   ДОБАВЛЕНИЕ СТРОКИ В ТАБЛИЦУ С ФИКСИРОВАННЫМИ СТРОКАМИ ИТОГОВ   ***********/

/**
 * добавляет строку в таблицу с фиксированными строками итогов. строка добавляется перед выделенной
 * строкой (если такая есть). если выделенной строки нет, то строка добавляется в конец таблицы перед
 * последней итоговой строкой
 */
def addNewRow() {
    def data = data
    def DataRow newRow = formData.createDataRow()

    makeCellsEditable(newRow)

    def index = 0
    if (currentDataRow!=null){
        index = currentDataRow.getIndex()
        def row = currentDataRow
        while(row.getAlias()!=null && index>0){
            row = getRows(data).get(--index)
        }
        if(index!=currentDataRow.getIndex() && getRows(data).get(index).getAlias()==null){
            index++
        }
    }else if (getRows(data).size()>0) {
        for(int i = getRows(data).size()-1;i>=0;i--){
            def row = getRows(data).get(i)
            if(row.getAlias()==null){
                index = getRows(data).indexOf(row)+1
                break
            }
        }
    }
    data.insert(newRow,index+1)
}

/**
 * делает ячейки, алиасы которых есть в списке редактируемых, редактируемыми
 */
def makeCellsEditable(def row) {
    getEditableColsAliases().each {
        row.getCell(it).editable = true
        row.getCell(it).setStyleAlias('Редактируемая')
    }
}

/**
 * возвращает список алиасов столбцов, доступных для редактирования пользователем
 */
def getEditableColsAliases() {
    return ['bill', 'purchaseDate', 'implementationDate', 'implementationPrice', 'implementationOutcome']
}

/***********   УДАЛЕНИЕ СТРОКИ ИЗ ТАБЛИЦЫ С ФИКСИРОВАННЫМИ СТРОКАМИ ИТОГОВ   ***********/

/**
 * удаляет выделенную строку, если она не является итоговой
 */
def deleteCurrentRow() {
    if (currentDataRow != null && currentDataRow.getAlias() == null) {
        data.delete(currentDataRow)
    }
}

/***********   ОБЩИЕ ФУНКЦИИ ДЛЯ ИТОГОВЫХ СТРОК   ***********/

/**
 * заполняем строку с итоговыми значениям
 */
def calcTotal(def data) {
    def totalResults = getTotalResults()
    def totalRow = data.getDataRow(getRows(data),getTotalDataRowAlias())
    getTotalColsAliases().each { colName ->
        totalRow[colName] = totalResults[colName]
    }
}

/**
 * false, если алиас строки не входит в список алиасов итоговых строк
 * true, если алиас строки входит в алиас итоговых строк
 */
boolean isInTotalRowsAliases(def alias){
    return (totalRowsAliases.find {totalAlias -> alias == totalAlias} != null)
}

/**
 * возвращает список алиасов для итоговых строк
 */
def getTotalRowsAliases() {
    return ['total']
}

def getTotalDataRowAlias() {
    return 'total'
}

/***********   ОБЩИЕ ФУНКЦИИ ДЛЯ СТОЛБЦОВ, ПО КОТОРЫМ ПОДВОДЯТСЯ ИТОГИ   ***********/

/**
 * находим для всех строк, кроме итоговых, суммы по столбцам, по которым подводят итоги
 * возвращаем мапу вида алиас_столбца -> итоговое_значение
 */
def getTotalResults() {
    def data = data
    def result = [:]
    for (def colAlias : getTotalColsAliases()) {
        result.put(colAlias, getRows(data).sum {row ->
            if (! isInTotalRowsAliases(row.getAlias())) {    //строка не входит в итоговые
                row[colAlias]
            } else {
                0
            }
        })
    }
    return result
}

/**
 * возвращает список алиасов для стобцов, по которым подводятся итоги
 */
def getTotalColsAliases() {
    return ['purchaseOutcome',  'implementationOutcome', 'percent',
            'implementationpPriceTax', 'allIncome', 'implementationPriceUp', 'income']
}

/**
 * Консолидация.
 */
void consolidation() {
    def data = data
    // удалить все строки и собрать из источников их строки
    data.clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                getRows(getData(source)).each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        data.insert(row, getRows(data).size()+1)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Получить данные формы.
 *
 * @param formData форма
 */
def DataRowHelper getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

def DataRowHelper getData(){
    return getData(formData)
}

/**
 * Получить строки формы.
 *
 * @param formData форма
 */
def List<DataRow<Cell>> getRows(def DataRowHelper data) {
    return data.getAllCached()
}

/**
 * Начало предупреждений/ошибок
 * @param row
 * @return
 */
def String getRowIndexString(def DataRow row){
    def index = row.number
    if (index != null) {
        return "В строке \"№ пп\" равной $index "
    } else {
        index = getRows(data).indexOf(row) + 1
        return "В строке $index "
    }

}

