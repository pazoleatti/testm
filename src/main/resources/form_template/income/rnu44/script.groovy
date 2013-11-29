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
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        def rnu49FormData = getRnu49FormData()
        if (rnu49FormData==null) {
            logger.error("Отсутствуют данные РНУ-49!")
            return
        }
        calc()
        logicalCheck()
        break
    case FormDataEvent.ADD_ROW :
        formDataService.addRow(formData, currentDataRow, editableColumns, autoFillColumns)
        break
    case FormDataEvent.DELETE_ROW :
        if (currentDataRow != null && currentDataRow.getAlias() == null) {
            formDataService.getDataRowHelper(formData).delete(currentDataRow)
        }
        break
    case FormDataEvent.MOVE_CREATED_TO_APPROVED :  // Утвердить из "Создана"
    case FormDataEvent.MOVE_APPROVED_TO_ACCEPTED : // Принять из "Утверждена"
    case FormDataEvent.MOVE_CREATED_TO_ACCEPTED :  // Принять из "Создана"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :  // Подготовить из "Создана"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED : // Принять из "Подготовлена"
    case FormDataEvent.MOVE_PREPARED_TO_APPROVED : // Утвердить из "Подготовлена"
        def rnu49FormData = getRnu49FormData()
        if (rnu49FormData==null) {
            logger.error("Отсутствуют данные РНУ-49!")
            return
        }
        logicalCheck()
        break
// обобщить
    case FormDataEvent.COMPOSE:
        formDataService.consolidationSimple(formData, formDataDepartment.id, logger)
        calc()
        logicalCheck()
        break
}

//// Кэши и константы
@Field def providerCache = [:]

// все атрибуты
@Field
def allColumns = ['number', 'operationDate', 'name', 'inventoryNumber', 'baseNumber', 'baseDate', 'summ']

// Редактируемые атрибуты
@Field def editableColumns = ['name', 'inventoryNumber']

// Автозаполняемые атрибуты
@Field def autoFillColumns = allColumns - editableColumns//['operationDate', 'baseNumber', 'baseDate', 'summ']

// Проверяемые на пустые значения атрибуты
@Field def nonEmptyColumns = ['number', 'operationDate', 'name', 'inventoryNumber', 'baseNumber', 'baseDate', 'summ']

// Атрибуты итоговых строк для которых вычисляются суммы
@Field
def totalSumColumns = ['summ']

// алиасы графов для арифметической проверки
@Field
def arithmeticCheckAlias = ['summ']//['operationDate', 'baseNumber', 'baseDate', 'summ']

// алиасы графов для арифметической проверки
@Field
def otherCheckAlias = ['operationDate', 'baseNumber', 'baseDate']

// Дата окончания отчетного периода
@Field def reportPeriodEndDate = null

// Текущая дата
@Field def currentDate = new Date()

//границы раздела "А" в РНУ-49
@Field String rnu49AIndex = 'A'

//границы раздела "А" в РНУ-49
@Field String rnu49TotalAIndex = 'totalA'

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
    def dataRows = dataRowHelper.getAllCached()

    def rnu49FormData = getRnu49FormData()
    if (rnu49FormData==null) return
    def rnu49Rows = rnu49FormData.getAllCached()

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    if (rnu49FormData!=null) {
        for (def dataRow : dataRows) {
            def rnu49Row = getRnu49Row(rnu49Rows, dataRow)

            dataRow.number = ++index

            def values = getValues(rnu49Row)
            values.keySet().each { colName ->
                dataRow[colName] = values[colName]
            }
        }

        // добавить строку "итого"
        def totalRow = getTotalRow(dataRows)
        dataRows.add(totalRow)
    }
    dataRowHelper.save(dataRows)
}

/**
 * Получить данные за формы "(РНУ-49) Регистр налогового учёта «ведомость определения результатов от реализации (выбытия) имущества»"
 */
def getRnu49FormData() {
    def formData49 = formDataService.find(312, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (formData49!=null && formData49.id != null) {
        return formDataService.getDataRowHelper(formData49)
    }
    return null
}

/**
 * В разделе А РНУ-49 находим строку, для которой «графа 6» = «графа 4» текущей строки формы РНУ-44.
 */
def getRnu49Row(def rnu49Rows, def dataRow) {
    //находим границы раздела "А" в РНУ-49
    def startToSearchIndex = rnu49Rows.indexOf(getDataRow(rnu49Rows, rnu49AIndex))
    def endToSearchIndex = rnu49Rows.indexOf(getDataRow(rnu49Rows, rnu49TotalAIndex))

    def indexRow = (startToSearchIndex..endToSearchIndex).find { index ->
        rnu49Rows[index].invNumber == dataRow.inventoryNumber
    }
    return indexRow?rnu49Rows[indexRow]:[:]
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
boolean logicalCheck(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.allCached

    def rnu49FormData = getRnu49FormData()
    if (rnu49FormData==null) return
    def rnu49Rows = rnu49FormData.getAllCached()

    def calcValues = [:]
    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'rowNumber')
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        rowNumber++
        errorMsg = "Строка $rowNumber: "

        // 0.
        checkNonEmptyColumns(row, rowNumber, nonEmptyColumns, logger, true)

        // 1. Проверка уникальности значий в графе «Номер ссудного счета»
        def find = dataRows.find{it->
            (row!=it && row.inventoryNumber==it.inventoryNumber)
        }
        if (find!=null) {
            logger.error(errorMsg + "Инвентарный номер не уникальный!")
        }

        // 2. Проверка на уникальность поля «№ пп»
        if (rowNumber != row.number) {
            logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
        }

        // 3. Арифметические проверки расчета неитоговых граф
        def rnu49Row = getRnu49Row(rnu49Rows, row)
        if (rnu49Row==[:]) {
            logger.error(errorMsg + "Отсутствуют данные в РНУ-49!")
        }
        calcValues = getValues(rnu49Row)
        checkCalc(row, arithmeticCheckAlias, calcValues, logger, true)
        for (def colName : otherCheckAlias) {
            if (row[colName] != calcValues[colName]) {
                String msg = getColumnName(row, colName)
                logger.error(errorMsg+"Неверно рассчитана графа «$msg»!")
            }
        }
    }
    // 4. Арифметические проверки расчета итоговой графы
    checkTotalSum(dataRows, totalSumColumns, logger, true)
}

/** Сформировать итоговую строку с суммами. */
def getTotalRow(def dataRows) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total')
    newRow.fix = 'Итого'
    newRow.getCell('fix').colSpan = 1
    allColumns.each {
        newRow.getCell(it).setStyleAlias('Контрольные суммы')
    }
    calcTotalSum(dataRows, newRow, totalSumColumns)
    return newRow
}
