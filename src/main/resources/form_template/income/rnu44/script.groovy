package form_template.income.rnu44

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

/**
 * Скрипт для РНУ-44 (rnu44.groovy).
 * (РНУ-44) Регистр налогового учёта доходов, в виде восстановленной амортизационной премии при реализации ранее,
 * чем по истечении 5 лет с даты ввода в эксплуатацию Взаимозависимым лицам и резидентам оффшорных зон основных средств
 * введённых в эксплуатацию после 01.01.2013
 *
 * Версия ЧТЗ: 64
 *
 *
 * @author vsergeev
 *
 * Графы:
 *
 *                  *****   РНУ 44  *****
 * 1    number          № пп
 * 2    operationDate   Дата операции
 * 3    name            Основное средство
 * 4    inventoryNumber Инвентарный номер
 * 5    baseNumber      Номер
 * 6    baseDate        Дата
 * 7    summ            Сумма восстановленной амортизационной премии
 *
 *                  *****   РНУ 46  *****
 * 1    rowNumber               № пп
 * 2    invNumber               Инв. номер
 * 3    name                    Наименование объекта
 * 4    cost                    Первоначальная стоимость
 * 5    amortGroup              Амортизационная группа
 * 6    usefulLife              Срок полезного использования, (мес.)
 * 7    monthsUsed              Количество месяцев эксплуатации предыдущими собственниками (арендодателями,
 *                              ссудодателями)
 * 8    usefulLifeWithUsed      Срок полезного использования с учётом срока эксплуатации предыдущими собственниками
 *                              (арендодателями, ссудодателями) либо установленный самостоятельно, (мес.)
 * 9    specCoef                Специальный коэффициент
 * 10   cost10perMonth          За месяц
 * 11   cost10perTaxPeriod      с начала налогового периода
 * 12   cost10perExploitation   с даты ввода в эксплуатацию
 * 13   amortNorm               Норма амортизации (% в мес.)
 * 14   amortMonth              за месяц
 * 15   amortTaxPeriod          с начала налогового периода
 * 16   amortExploitation       с даты ввода в эксплуатацию
 * 17   exploitationStart       Дата ввода в эксплуатацию
 * 18   usefullLifeEnd          Дата истечения срока полезного использования
 * 19   rentEnd                 Дата истечения срока договора аренды / договора безвозмездного пользования
 *
 *                  *****   РНУ 49  *****
 * 1    rowNumber   № пп
 * 2    firstRecordNumber       Номер первой записи
 * 3    operationDate           Дата операции
 * 4    reasonNumber            номер
 * 5    reasonDate              дата
 * 6    invNumber               Инвентарный номер
 * 7    name                    Наименование
 * 8    price                   Цена приобретения
 * 9    amort                   Фактически начислено амортизации (отнесено на расходы)
 * 10   expensesOnSale          Расходы при реализации
 * 11   sum                     Сумма начисленной выручки от реализации
 * 12   sumInFact               Сумма фактически поступивших денежных средств
 * 13   costProperty            Стоимость материалов и имущества, полученных при ликвидации основных средств
 * 14   marketPrice             Рыночная цена
 * 15   sumIncProfit            Сумма к увеличению прибыли (уменьшению убытка)
 * 16   profit                  Прибыль от реализации
 * 17   loss                    Убыток от реализации
 * 18   usefullLifeEnd          Дата истечения срока полезного использования
 * 19   monthsLoss              Количество месяцев отнесения убытков на расходы
 * 20   expensesSum             Сумма расходов, приходящаяся на каждый месяц
 * 21   saledPropertyCode       Шифр вида реализованного (выбывшего) имущества
 * 22   saleCode                Шифр вида реализации (выбытия)
 * 23   propertyType            Тип имущества
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
        recalculateNumbers()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        recalculateNumbers()
        break
}

/**
 * заполняем ячейки, вычисляемые автоматически
 */

def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = getRows(dataRowHelper)

    def rnu49FormData = getRnu49FormData()

    if (rnu49FormData!=null) {
    for (def dataRow : dataRows) {
        if ( ! isFixedRow(dataRow)) {      //строку итогов не заполняем
            def rnu49Row = getRnu49Row(rnu49FormData, dataRow)

            def values = getValues(rnu49Row)

            values.keySet().each { colName ->
                dataRow[colName] = values[colName]
            }
        }
    }
    def totalResults = getTotalResults()
    dataRows.each { dataRow ->
        if (isFixedRow(dataRow)) {
            getTotalColsAliases().each { colName ->
                dataRow[colName] = totalResults[colName]
            }
        }
    }
    }
    save(dataRowHelper)
}

boolean checkCalculatedCells() {
    def isValid = true
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = getRows(dataRowHelper)

    def rnu49FormData = getRnu49FormData()

    if (rnu49FormData!=null) {
    for (def dataRow : dataRows) {
        if ( !isFixedRow(dataRow)) {      //строку итогов не проверяем
            def rnu49Row = getRnu49Row(rnu49FormData, dataRow)

            def values = getValues(rnu49Row)

            for (def colName : values.keySet()) {
                if (dataRow[colName] != values[colName]) {
                    isValid = false
                    def fieldNumber = dataRow.getIndex()
                    String msg = getColumnName(dataRow, colName)
                    logger.error("Строка $fieldNumber: Неверно рассчитана графа «$msg»!")
                }
            }
        }
    }
    }
    return isValid
}

/**
 * Получить данные за формы "(РНУ-49) Регистр налогового учёта «ведомость определения результатов от реализации (выбытия) имущества»"
 */
def getRnu49FormData() {
    def formData49 = formDataService.find(312, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (formData49!=null) {
        return getData(formData49)
    }
    return null
}

/**
 * В разделе А РНУ-49 находим строку, для которой «графа 6» = «графа 4» текущей строки формы РНУ-44.
 */
def getRnu49Row(def rnu49FormData, def dataRow) {
    def rnu49Rows = getRows(rnu49FormData)
    //находим границы раздела "А" в РНУ-49

    def startToSearchIndex = rnu49Rows.indexOf(rnu49FormData.getDataRow(rnu49Rows, getRnu49AIndex()))
    def endToSearchIndex = rnu49Rows.indexOf(rnu49FormData.getDataRow(rnu49Rows, getRnu49TotalAIndex()))

    def indexRow = (startToSearchIndex..endToSearchIndex).find { index ->
        rnu49Rows[index].invNumber == dataRow.inventoryNumber
    }
    return rnu49Rows[indexRow]
}

def getRnu49AIndex(){
    return 'A'
}

def getRnu49TotalAIndex(){
    return 'totalA'
}


/**
 * получаем мапу со значениями, расчитанными для каждой конкретной строки
 */
def getValues(def rnu49Row) {
    def values = [:]

    values.with {
        /*2*/   operationDate = getOperationDate(rnu49Row)
        /*5*/   baseNumber = getBaseNumber(rnu49Row)
        /*6*/   baseDate = getBaseDate(rnu49Row)
        /*7*/   summ = getSumm(rnu49Row)
    }

    return values
}

def getOperationDate(def rnu49Row) {
    return rnu49Row.operationDate
}

def getBaseNumber(rnu49Row){
    return rnu49Row.reasonNumber
}

def getBaseDate(rnu49Row){
    return rnu49Row.reasonDate
}

def getSumm(rnu49Row) {
    return rnu49Row.sum
}

/***********   ФУНКЦИИ ДЛЯ ПРОВЕРКИ ОБЯЗАТЕЛЬНЫХ ДЛЯ ЗАПОЛНЕНИЯ ДАННЫХ   ***********/

/**
 * перед расчетами проверяем заполнение только ячеек, доступных для ввода. т.к.
 * они нам нужны для расчетов, а рассчитываемые - не нужны
 */
boolean logicalCheckWithoutTotalDataRowCheck() {
    return checkColsFilledByAliases(getEditableColsAliases())
}

/**
 * проверяем все данные формы на обязательное и корректное заполнение
 */
boolean logicalCheckWithTotalDataRowCheck() {
    if (checkColsFilledByAliases(getAllRequiredColsAliases())) {
        return (checkCalculatedCells() && checkTotalResults())
    }

    return false
}

/**
 * возвращает список алиасов всех обязательных для заполнения столбцов
 */
def getAllRequiredColsAliases() {
    return ['operationDate', 'name', 'inventoryNumber', 'baseNumber', 'baseDate', 'summ']
}

/**
 * проверяем актуальность итоговых значения
 */
boolean checkTotalResults() {
    def isValid = true
    def controlTotalResults = getTotalResults()

    def dataRows = getRows(formDataService.getDataRowHelper(formData))
    dataRows.each { dataRow ->
        if (isFixedRow(dataRow)) {
            for (def colName : controlTotalResults.keySet()) {
                if (dataRow[colName] != controlTotalResults[colName]) {
                    isValid = false
                    def fieldNumber = dataRow.getIndex()
                    String msg = getColumnName(dataRow, colName)
                    logger.error("Строка $fieldNumber: Неверно рассчитана графа «$msg»!")
                }
            }
        }
    }

    return isValid
}

/**
 * проверяем заполнения столбцов по алиасам этих столбцов
 */
boolean checkColsFilledByAliases(List colsAliases) {
    boolean isValid = true
    def dataRows = getRows(formDataService.getDataRowHelper(formData))
    dataRows.each { dataRow ->
        if (! isFixedRow(dataRow)) {       //итоговые строки не проверяем
            isValid &= checkRequiredColumns(dataRow, colsAliases)
        }
    }

    return isValid
}

/***********   ДОБАВЛЕНИЕ СТРОКИ В ТАБЛИЦУ С ФИКСИРОВАННЫМИ СТРОКАМИ ИТОГОВ   ***********/

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)

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
            if(!isFixedRow(row)){
                index = getRows(data).indexOf(row)+1
                break
            }
        }
    }
    data.insert(newRow,index+1)
}


/**
 * Получить новую стролу с заданными стилями.
 */
def getNewRow() {
    def newRow = formData.createDataRow()

    getEditableColsAliases().each {
        newRow.getCell(it).editable = true
//        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    return newRow
}

/**
 * возвращает список алиасов столбцов, доступных для редактирования пользователем
 */
def getEditableColsAliases() {
    return ['name', 'inventoryNumber']
}

/**
 * Проверка является ли строка итоговой.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * возвращает список алиасов для итоговых строк
 */
def getTotalRowsAliases() {
    return ['total']
}

/***********   УДАЛЕНИЕ СТРОКИ ИЗ ТАБЛИЦЫ С ФИКСИРОВАННЫМИ СТРОКАМИ ИТОГОВ   ***********/

/**
 * Удалить строку.
 */
def deleteRow() {
    if (!isFixedRow(currentDataRow)) {
        getData(formData).delete(currentDataRow)
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
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()
    def totalRow = [:]
    for (def colAlias : getTotalColsAliases()) {
        totalRow.put(colAlias, dataRows.sum {row ->
            if (! isFixedRow(row)) {    //строка не входит в итоговые
                row[colAlias]
            } else {
                0
            }
        })
    }
    return totalRow
}

/**
 * возвращает список алиасов для стобцов, по которым подводятся итоги
 */
def getTotalColsAliases() {
    return ['summ']
}

/**
 * Получить данные формы.
 *
 * @param formData форма
 */
def getData(def formData) {
    if (formData != null && formData.id != null) {
        return formDataService.getDataRowHelper(formData)
    }
    return null
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 */
def getRows(def data) {
    return data.getAllCached();
}

/**
 * Сохранить измененные значения нф.
 *
 * @param data данные нф (helper)
 */
void save(def data) {
    data.save(getRows(data))
}

def recalculateNumbers(){
    def index = 1
    def data = getData(formData)
    getRows(data).each{row->
        if (!isFixedRow(row)) {
            row.number = index++
        }
    }
    data.save(getRows(data))
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.number
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            def data = getData(formData)
            index = getRows(data).indexOf(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
}

/**
 * Получить название графы по псевдониму.
 *
 * @param row строка
 * @param alias псевдоним графы
 */
def getColumnName(def row, def alias) {
    if (row != null && alias != null) {
        return row.getCell(alias).getColumn().getName().replace('%', '%%')
    }
    return ''
}