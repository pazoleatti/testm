package form_template.income.rnu44

import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange

import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.log.LogLevel
import groovy.transform.Field

/**
 * Скрипт для РНУ-44 (rnu44.groovy).
 * (РНУ-44) Регистр налогового учёта доходов, в виде восстановленной амортизационной премии при реализации ранее,
 * чем по истечении 5 лет с даты ввода в эксплуатацию Взаимозависимым лицам и резидентам оффшорных зон основных средств
 * введённых в эксплуатацию после 01.01.2013
 *
 * Версия ЧТЗ: 64
 *
 * @author lhaziev
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
    case FormDataEvent.CREATE:
        formDataService.checkUnique(formData, logger)
        break
    case FormDataEvent.CHECK :
        def rnu49FormData = getRnu49FormData()
        if (rnu49FormData==null) {
            logger.error("Отсутствуют данные РНУ-49!")
            return
        }
        logicalCheckWithTotalDataRowCheck()
        break
    case FormDataEvent.CALCULATE :
        def rnu49FormData = getRnu49FormData()
        if (rnu49FormData==null) {
            logger.error("Отсутствуют данные РНУ-49!")
            return
        }
        calc()
        !hasError() && logicalCheckWithTotalDataRowCheck()
        break
    case FormDataEvent.ADD_ROW :
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW :
        formDataService.getDataRowHelper(formData).delete(currentDataRow)
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        logicalCheckWithTotalDataRowCheck()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED:
// обобщить
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicalCheckWithTotalDataRowCheck()
        break
}

//// Кэши и константы
@Field def providerCache = [:]

// Редактируемые атрибуты
@Field def editableColumns = ['name', 'inventoryNumber']

// Автозаполняемые атрибуты
@Field def autoFillColumns = ['operationDate', 'baseNumber', 'baseDate', 'summ']

// Проверяемые на пустые значения атрибуты
@Field def nonEmptyColumns = ['operationDate', 'name', 'inventoryNumber', 'baseNumber', 'baseDate', 'summ']

// Дата окончания отчетного периода
@Field def reportPeriodEndDate = null

// Текущая дата
@Field def currentDate = new Date()

//// Обертки методов

// Проверка НСИ
boolean checkNSI(def refBookId, def row, def alias) {
    return formDataService.checkNSI(refBookId, refBookCache, row, alias, logger, false)
}

/**
 * заполняем ячейки, вычисляемые автоматически
 */

def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = getRows(dataRowHelper)

    def rnu49FormData = getRnu49FormData()

    def index = 1

    if (rnu49FormData!=null) {
        for (def dataRow : dataRows) {
            if ( ! isFixedRow(dataRow)) {      //строку итогов не заполняем
                def rnu49Row = getRnu49Row(rnu49FormData, dataRow)

                def values = getValues(rnu49Row)

                dataRow.number = index++
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
    return indexRow?rnu49Rows[indexRow]:[:]
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

/**
 * проверяем все данные формы на обязательное и корректное заполнение
 */
boolean logicalCheckWithTotalDataRowCheck() {
    if (checkColsFilledByAliases(false) ) {
        return (checkInventoryNumber() && checkCalculatedCells() && checkTotalResults())
    }
    return false
}

/**
 * проверяем заполнения столбцов по алиасам этих столбцов
 */
boolean checkColsFilledByAliases(boolean flag) {
    boolean isValid = true
    def dataRows = getRows(formDataService.getDataRowHelper(formData))
    int rowNum = 0
    dataRows.each { row ->
        if (row.getAlias() == null) checkNonEmptyColumns(row, ++rowNum, nonEmptyColumns, logger, flag)
    }

    return isValid
}

/**
 * проверяем актуальность Инвентарного номера
 */
boolean checkInventoryNumber() {
    def dataRows = getRows(formDataService.getDataRowHelper(formData))
    for(def row: dataRows) {
        def find = dataRows.find{it->
            (row!=it && row.inventoryNumber==it.inventoryNumber)
        }
        if (find!=null) {
            logger.error("Инвентарный номер не уникальный!")
            return false
        }
    }
    return true
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
 * Проверка является ли строка итоговой.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
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
                row[colAlias]?:0
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

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}
