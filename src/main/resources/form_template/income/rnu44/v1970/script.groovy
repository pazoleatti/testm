package form_template.income.rnu44.v1970

import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.FormDataKind
import groovy.transform.Field

/**
 * (РНУ-44) Регистр налогового учёта доходов, в виде восстановленной амортизационной премии при реализации ранее,
 * чем по истечении 5 лет с даты ввода в эксплуатацию Взаимозависимым лицам и резидентам оффшорных зон основных средств
 * введённых в эксплуатацию после 01.01.2013
 * formTemplateId=340
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
        checkRNU()
        logicCheck()
        break
    case FormDataEvent.CALCULATE :
        checkRNU()
        calc()
        logicCheck()
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
        checkRNU()
        logicCheck()
        break
    case FormDataEvent.COMPOSE:
        formDataService.consolidationTotal(formData, formDataDepartment.id, logger, ['total'])
        calc()
        logicCheck()
        break
    case FormDataEvent.IMPORT:
        importData()
        calc()
        logicCheck()
        break
}

//// Кэши и константы

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
def arithmeticCheckAlias = ['summ']

// алиасы графов для не арифметической проверки
@Field
def otherCheckAlias = ['operationDate', 'baseNumber', 'baseDate']

// Дата окончания отчетного периода
@Field
def reportPeriodEndDate = null

//границы раздела "А" в РНУ-49
@Field
String rnu49AIndex = 'A'

//границы раздела "А" в РНУ-49
@Field
String rnu49TotalAIndex = 'totalA'

//// Обертки методов

// заполняем ячейки, вычисляемые автоматически
def calc() {
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def rnu49FormData = getRnu49FormData()
    def rnu49Rows = rnu49FormData?.getAllCached()
    if (rnu49Rows == null || rnu49Rows.isEmpty()) return

    // получить строку "итого"
    def totalRow = getDataRow(dataRows, 'total')
    // очистить итоги
    totalSumColumns.each {
        totalRow[it] = null
    }

    // удалить строку "итого"
    deleteAllAliased(dataRows)

    def index = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    for (def dataRow : dataRows) {
        def rnu49Row = getRnu49Row(rnu49Rows, dataRow)

        dataRow.number = ++index

        if (rnu49Row) {
            def values = getValues(rnu49Row)
            values.keySet().each { colName ->
                dataRow[colName] = values[colName]
            }
        }
    }
    // добавить строку "итого"
    calcTotalSum(dataRows, totalRow, totalSumColumns)
    dataRows.add(totalRow)
    dataRowHelper.save(dataRows)
}

/**
 * Получить данные за формы "(РНУ-49) Регистр налогового учёта «ведомость определения результатов от реализации (выбытия) имущества»"
 */
def getRnu49FormData() {
    def formData49 = formDataService.find(312, formData.kind, formDataDepartment.id, formData.reportPeriodId)
    if (formData49 != null && formData49.id != null) {
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
boolean logicCheck(){
    def dataRowHelper = formDataService.getDataRowHelper(formData)
    def dataRows = dataRowHelper.getAllCached()

    def rnu49Rows = getRnu49FormData()?.getAllCached()
    if (rnu49Rows == null) return

    def rowNumber = formDataService.getPrevRowNumber(formData, formDataDepartment.id, 'number')
    for (def row : dataRows) {
        if (row.getAlias() != null) {
            continue
        }
        rowNumber++
        def index = row.getIndex()
        def errorMsg = "Строка ${index}: "

        // 0.
        checkNonEmptyColumns(row, index, nonEmptyColumns, logger, true)

        // 1. Проверка уникальности значий в графе «Номер ссудного счета»
        def find = dataRows.find{it->
            (row != it && row.inventoryNumber == it.inventoryNumber)
        }
        if (find != null) {
            logger.error(errorMsg + "Инвентарный номер не уникальный!")
        }

        // 2. Проверка на уникальность поля «№ пп»
        if (rowNumber != row.number) {
            logger.error(errorMsg + "Нарушена уникальность номера по порядку!")
        }

        // 3. Арифметические проверки расчета неитоговых граф
        def rnu49Row = getRnu49Row(rnu49Rows, row)
        if (rnu49Row == [:]) {
            logger.error(errorMsg + "Отсутствуют данные в РНУ-49!")
        }
        def calcValues = getValues(rnu49Row)
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

// Получение импортируемых данных
void importData() {
    def xml = getXML(ImportInputStream, importService, UploadFileName, '№ пп', null)

    checkHeaderSize(xml.row[0].cell.size(), xml.row.size(), 6, 2)

    def headerMapping = [
            (xml.row[0].cell[0]): '№ пп',
            (xml.row[0].cell[2]): 'Дата операции',
            (xml.row[0].cell[3]): 'Основное средство',
            (xml.row[0].cell[5]): 'Основание для восстановления амортизационной премии',
            (xml.row[0].cell[7]): 'Сумма восстановленной амортизационной премии',
            (xml.row[1].cell[3]): 'Наименование',
            (xml.row[1].cell[4]): 'Инвентарный номер',
            (xml.row[1].cell[5]): 'Номер',
            (xml.row[1].cell[6]): 'Дата',
            (xml.row[2].cell[0]): '1',
            (xml.row[2].cell[2]): '2',
            (xml.row[2].cell[3]): '3',
            (xml.row[2].cell[4]): '4',
            (xml.row[2].cell[5]): '5',
            (xml.row[2].cell[6]): '6',
            (xml.row[2].cell[7]): '7'
    ]

    checkHeaderEquals(headerMapping)

    addData(xml, 2)
}

// Заполнить форму данными
void addData(def xml, int headRowCount) {
    reportPeriodEndDate = reportPeriodService.getEndDate(formData.reportPeriodId).time
    def dataRowHelper = formDataService.getDataRowHelper(formData)

    def xmlIndexRow = -1 // Строки xml, от 0
    def rowOffset = xml.infoXLS.rowOffset[0].cell[0].text().toInteger()
    def colOffset = xml.infoXLS.colOffset[0].cell[0].text().toInteger()

    def rows = []
    def int rowIndex = 1  // Строки НФ, от 1

    def dataRows = dataRowHelper.allCached

    // Итоговая строка
    def totalRow = getDataRow(dataRows, 'total')
    // Очистка итогов
    totalSumColumns.each { alias ->
        totalRow[alias] = null
    }

    for (def row : xml.row) {
        xmlIndexRow++
        def int xlsIndexRow = xmlIndexRow + rowOffset

        // Пропуск строк шапки
        if (xmlIndexRow <= headRowCount) {
            continue
        }

        if ((row.cell.find { it.text() != "" }.toString()) == "") {
            break
        }

        // Пропуск итоговых строк
        if (row.cell[1].text() != null && row.cell[1].text() != '') {
            continue
        }

        def xmlIndexCol = 0

        def newRow = formData.createDataRow()
        newRow.setIndex(rowIndex++)
        editableColumns.each {
            newRow.getCell(it).editable = true
            newRow.getCell(it).setStyleAlias('Редактируемая')
        }
        autoFillColumns.each {
            newRow.getCell(it).setStyleAlias('Автозаполняемая')
        }

        // графа 1
        newRow.number = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // fix
        xmlIndexCol++
        // графа 2
        newRow.operationDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 3
        newRow.name = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 4
        newRow.inventoryNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 5
        newRow.baseNumber = row.cell[xmlIndexCol].text()
        xmlIndexCol++
        // графа 6
        newRow.baseDate = parseDate(row.cell[xmlIndexCol].text(), "dd.MM.yyyy", xlsIndexRow, xmlIndexCol + colOffset, logger, true)
        xmlIndexCol++
        // графа 7
        newRow.summ = parseNumber(row.cell[xmlIndexCol].text(), xlsIndexRow, xmlIndexCol + colOffset, logger, true)

        rows.add(newRow)
    }
    rows.add(totalRow)
    dataRowHelper.save(rows)
}

// проверить наличие рну 49
void checkRNU() {
    if (formData.kind == FormDataKind.PRIMARY) {
        formDataService.checkFormExistAndAccepted(312, FormDataKind.PRIMARY, formData.departmentId, formData.reportPeriodId, false, logger, true)
    }
}