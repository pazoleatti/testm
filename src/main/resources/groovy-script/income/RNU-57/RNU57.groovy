/**
 * Скрипт для РНУ-57 (rnu57.groovy).
 * (РНУ-57) Регистр налогового учёта финансового результата от реализации (погашения) векселей сторонних эмитентов
 *
 * Версия ЧТЗ: 64
 *
 * Вопросы аналитикам: http://jira.aplana.com/browse/SBRFACCTAX-2662
 *
 * TODO:
 *      дописать получение данных из РНУ-55 и РНУ-56
 *      добавить справочники кодов валют
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
        logicalCheckWithTotalDataRowCheck()
        break
    case FormDataEvent.CALCULATE :
        if (logicalCheckWithoutTotalDataRowCheck()) {
            calc()
        }
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteCurrentRow()
        break
}

boolean logicalCheckWithoutTotalDataRowCheck() {
    if (isCheckColsFilledByAliases(getEditableColsAliases())) { //перед расчетами проверяем заполнение только ячеек, доступных для ввода. т.к. они нам нужны для расчетов, а рассчитываемые - не нужны
        return checkCalculatedCells()
    }
}

boolean logicalCheckWithTotalDataRowCheck() {
    if (isCheckColsFilledByAliases(getAllRequiredColsAliases())) {
        return (checkCalculatedCells() && checkTotalResults())
    }

    return false
}

boolean isCheckColsFilledByAliases(List colsAliases) {
    boolean isValid = true
    formData.dataRows.each { dataRow ->
        if (! isInTotalRowsAliases(dataRow.getAlias())) {       //итоговые строки не проверяем
            for (def colAlias : colsAliases) {
                if (isBlankOrNull(dataRow[colAlias])) {
                    def columnIndex = formData.dataRows.indexOf(dataRow) + 1
                    logger.error("Поле $columnIndex не заполнено!")
                    isValid = false
                    break
                }
            }
        }
    }

    return isValid
}

boolean checkCalculatedCells() {
    def isValid = true

    def rnu55FormData = getRnu55FormData()
    def rnu56FormData = getRnu56FormData()

    for (def dataRow : formData.getDataRows()) {
        if ( ! isInTotalRowsAliases(dataRow.getAlias())) {      //строку итогов не проверяем
            def rnu55Row = getRnu55Row(rnu55FormData, dataRow)
            def rnu56Row = getRnu56Row(rnu56FormData, dataRow)

            def values = getValues(dataRow, rnu55Row, rnu56Row, rnu56FormData)

            for (def colName : values.keySet()) {
                if (dataRow[colName] != values[colName]) {
                    isValid = false
                    def fieldNumber = formData.dataRows.indexOf(dataRow) + 1
                    logger.error("Строка $fieldNumber заполнена неверно!")
                    break
                }
            }
        }
    }

    return isValid
}

boolean checkTotalResults() {
    def totalDataRow = formData.getDataRow(getTotalDataRowAlias())
    def controlTotalResults = getTotalResults()

    for (def colName : controlTotalResults.keySet()) {
        if (totalDataRow[colName] != controlTotalResults[colName]) {
            logger.error('Итоговые значения рассчитаны неверно!')
            return false
        }
    }

    return true
}

def getEditableColsAliases() {
    return ['bill', 'purchaseDate', 'implementationDate', 'implementationPrice', 'implementationOutcome']
}

def getAllRequiredColsAliases() {
    return ['number', 'bill', 'purchaseDate', 'purchasePrice', 'purchaseOutcome', 'implementationDate', 'implementationPrice',
            'implementationOutcome', 'price', 'percent', 'implementationpPriceTax', 'allIncome',
            'implementationPriceUp', 'income']
}

def getTotalDataRowAlias() {
    return 'total'
}

def calc() {
    calcValues()
    calcTotal()
}

/**
 * заполняем строку с итоговыми значениям
 */
def calcTotal() {
    def totalResults = getTotalResults()
    def totalRow = formData.getDataRow(getTotalDataRowAlias())
    getTotalColsAliases().each { colName ->
        totalRow[colName] = totalResults[colName]
    }
}

/**
 * заполняем ячейки, вычисляемые автоматически
 */
def calcValues() {
    def rnu55FormData = getRnu55FormData()
    def rnu56FormData = getRnu56FormData()

    for (def dataRow : formData.getDataRows()) {
        if ( ! isInTotalRowsAliases(dataRow.getAlias())) {      //строку итогов не заполняем
            def rnu55Row = getRnu55Row(rnu55FormData, dataRow)
            def rnu56Row = getRnu56Row(rnu56FormData, dataRow)

            def values = getValues(dataRow, rnu55Row, rnu56Row, rnu56FormData)

            values.keySet().each { colName ->
                dataRow[colName] = values[colName]
            }
        }
    }
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
        implementationpPriceTax = getImplementationpPriceTax(dataRow, rnu55Row, rnu56Row)
        allIncome = getAllIncome(dataRow, rnu55Row, rnu56Row)
        implementationPriceUp = getImplementationPriceUp(dataRow, rnu55Row, rnu56Row)
        income = getIncome(dataRow, rnu55Row, rnu56Row)
    }

    return values
}

def getRnu55FormData() {
    //todo тут будем получать formData из РНУ 55. какую форму получать - в вопросах аналитикам
}

def getRnu56FormData() {
    //todo тут будем получать formData из РНУ 56. какую форму получать - в вопросах аналитикам
}

def getRnu55Row(def rnu55formData, def dataRow) {
    //todo тут будем получать строку из РНУ-55, с которой будем сравнивать значения текущей строки
}

def getRnu56Row(def rnu56formData, def dataRow) {
    //todo тут будем получать строку из РНУ-56, с которой будем сравнивать значения текущей строки
}

def getPurchasePrice(def dataRow, def rnu55DataRow, def rnu56DataRow) {
    //todo тут спорный вопрос о порядке следования условий, см. в вопросах аналитикам, уточнить!
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

def getPurchaseOutcome(def dataRow, def rnu55dataRow, def rnu56DataRow) {
    if (isHasTheSameBills(dataRow, rnu55dataRow)) {
        return null     //todo косяк в аналитике, см. http://jira.aplana.com/browse/SBRFACCTAX-2698
    }
    if (isHasTheSameBills(dataRow, rnu56DataRow)) {
        return null     //todo косяк в аналитике, см. http://jira.aplana.com/browse/SBRFACCTAX-2698
    }
}

def getPercentInRuble(def dataRow, def rnu55DataRow, def rnu56DataRow) {
    if (isHasTheSameBills(dataRow, rnu56DataRow)) {
        if (rnu56DataRow.maturity == dataRow.implementationDate) {
            return 0
        }

        if (rnu56DataRow.maturity > dataRow.implementationDate) {   //todo разобраться с условиями. см. http://jira.aplana.com/browse/SBRFACCTAX-2704
            def N = rnu56DataRow.nominal
            def K = rnu56DataRow.price
            def T = getT(dataRow, rnu56DataRow)
            def D = TimeCategory.minus(dataRow.implementationDate, rnu56DataRow.buyDate)

            //в ЧТЗ, похоже, два условия: для рублей и для нерублей. условия отличаются только домножением на курс валют,
            // но для рубля курс всегда берется равным единице, так что условия можно объединить
            return (N - K) / T * D + K * getCourseForCurrencyByDate(rnu56DataRow.currency, dataRow.implementationDate)

        }
    }
    if (isHasTheSameBills(dataRow, rnu55DataRow)) {
        return rnu55DataRow.nominal * getCourseForCurrencyByDate(rnu55DataRow.currency, dataRow.implementationDate)
    }
}

def getT(def dataRow, def rnu56DataRow) {
    //todo не до конца понятно, как расчитывать. см. http://jira.aplana.com/browse/SBRFACCTAX-2704
}

def getPercent(def dataRow, def rnu55DataRow, def rnu56DataRow, def rnu56FormData) {
    if (isHasTheSameBills(dataRow, rnu56DataRow)) {
        return rnu56DataRow.discountInRub
    }
    if (rnu56DataRow.bill == null) {
        def rnu56TotalDataRow = rnu56FormData.getDataRow(getTotalDataRowAlias())     //todo после реализации РНУ-56 убедиться, что у строики итогов именно такой алиас
        return rnu56TotalDataRow.sumIncomeinRuble
    }
    if (isHasTheSameBills(dataRow, rnu55DataRow)) {
        return 0
    }
}

def getImplementationpPriceTax(def dataRow, def rnu55DataRow, def rnu56DataRow) {
    //todo тут спорный вопрос о порядке следования условий, см. в вопросах аналитикам, уточнить!
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

def getAllIncome(def dataRow, def rnu55dataRow, def rnu56DataRow) {
    return dataRow.purchasePrice + dataRow.purchaseOutcome + dataRow.implementationOutcome
}

def getImplementationPriceUp(def dataRow, def rnu55DataRow, def rnu56DataRow) {
    def tmpValue = dataRow.implementationpPriceTax + dataRow.percent - dataRow.implementationPrice
    if (tmpValue < 0) {
        return 0
    }
    if (isHasTheSameBills(dataRow, rnu55DataRow)) {
        return dataRow.implementationpPriceTax - dataRow.implementationPrice
    }
    if (isHasTheSameBills(dataRow, rnu56DataRow)) {
        return dataRow.implementationpPriceTax - dataRow.implementationPrice + dataRow.percent
    }
}

def getIncome(def dataRow, def rnu55DataRow, def rnu56DataRow) {
    return dataRow.implementationpPriceTax - dataRow.allIncome
}

def getCourseForCurrencyByDate(def currency, def date) {
    //todo тут будем получать курс валюты по дате, когда сделают соответствующий справочник
    //если валюта = 810 (рубли), то возвращать курс 1.0. почему-то в ЧТЗ это отмечено отдельно. наверное, рублей в справочнике не будет
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

/**
 * возвращает true, если в таблице выделен какой-нибудь столбце
 * иначе возвращает false
 */
boolean isCurrentDataRowSelected() {
    return (currentDataRow != null && formData.dataRows.indexOf(currentDataRow) >= 0)
}

/***********   ОБЩИЕ ФУНКЦИИ ДЛЯ ИТОГОВЫХ СТРОК   ***********/

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

/***********   ДОБАВЛЕНИЕ СТРОКИ В ТАБЛИЦУ С ФИКСИРОВАННЫМИ СТРОКАМИ ИТОГОВ   ***********/

/**
 * добавляет строку в таблицу с фиксированными строками итогов. строка добавляется перед выделенной
 * строкой (если такая есть). если выделенной строки нет, то строка добавляется в конец таблицы перед
 * последней итоговой строкой
 */
def addNewRow() {
    def newRow = formData.createDataRow()

    getEditableColsAliases().each{ value ->
        newRow.getCell(value).editable = true
    }

    int index = getNewRowIndex()

    formData.dataRows.add(index, newRow)

    return newRow
}

/**
 * возвращает индекс для добавляемого столбца
 * (находит строку, удовлетворяющую условиям addNewRow(). на ее место будет произведена вставка новой строки)
 */
int getNewRowIndex() {
    def index

    def isTotalRow = false
    if (isCurrentDataRowSelected()) {
        index = formData.dataRows.indexOf(currentDataRow)
        if ( ! isBlankOrNull(currentDataRow.getAlias())) {
            isTotalRow = true
        }
    } else {
        index = formData.dataRows.size() - 1
    }

    index = goToTopAndGetMaxIndexOfRowWithoutAlias(index)

    if (isTotalRow && index != null) {
        index += 1
    } else if (index == null) {
        index = 0
    }

    return index
}

/**
 * идем вверх по таблице, начиная со строки с индексом startIndex (включительно). находим первую неитоговую
 * строку (алиас которой не помечен как итоговый в getTotalRowsAliases()).
 *
 * возвращает индекс этой строки.
 */
def goToTopAndGetMaxIndexOfRowWithoutAlias(def startIndex) {
    for (int i = startIndex; i >= 0; i--) {
        if (getTotalRowsAliases().find{ totalRowAlias ->
            totalRowAlias == formData.dataRows[i].getAlias()
        } == null) {
            return i
        }
    }

    return null
}

/***********   УДАЛЕНИЕ СТРОКИ ИЗ ТАБЛИЦЫ С ФИКСИРОВАННЫМИ СТРОКАМИ ИТОГОВ   ***********/

/**
 * удаляет выделенную строку, если она не является итоговой
 * если выделенная строки является итоговой, то она не удаляется и выводится сообщение о критичесокй ошибке
 */
def deleteCurrentRow() {
    if (isCurrentDataRowSelected() &&
            totalRowsAliases.find { totalRowAlias ->
                totalRowAlias == currentDataRow.getAlias()
            } == null) {
        formData.dataRows.remove(currentDataRow)
    } else {
        logger.error ('Невозможно удалить фиксированную строку!')
    }
}

/***********   ОБЩИЕ ФУНКЦИИ ДЛЯ СТОЛБЦОВ, ПО КОТОРЫМ ПОДВОДЯТСЯ ИТОГИ   ***********/

/**
 * находим для всех строк, кроме итоговых, суммы по столбцам, по которым подводят итоги
 * возвращаем мапу вида алиас_столбца -> итоговое_значение
 */
def getTotalResults() {
    def result = [:]
    for (def colAlias : getTotalColsAliases()) {
        result.put(colAlias, formData.dataRows.sum {row ->
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
    return ['purchasePrice', 'purchaseOutcome', 'implementationPrice', 'implementationOutcome', 'price', 'percent',
            'implementationpPriceTax', 'allIncome', 'implementationPriceUp', 'income']
}