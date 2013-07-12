/**
 * Скрипт для РНУ-44 (rnu44.groovy).
 * (РНУ-44) Регистр налогового учёта доходов, в виде восстановленной амортизационной премии при реализации ранее,
 * чем по истечении 5 лет с даты ввода в эксплуатацию Взаимозависимым лицам и резидентам оффшорных зон основных средств
 * введённых в эксплуатацию после 01.01.2013
 *
 * Версия ЧТЗ: 64
 *
 * TODO:
 *         -    не понятно, как получать данные из РНУ-46 и РНУ-49 с.м. http://jira.aplana.com/browse/SBRFACCTAX-2731
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
        break
    case FormDataEvent.DELETE_ROW :
        deleteCurrentRow()
        break
}

def calc() {
    calcValues()
    calcTotal()
}

boolean checkCalculatedCells() {
    def isValid = true

    def rnu46FormData = getRnu46FormData()
    def rnu49FormData = getRnu49FormData()

    for (def dataRow : formData.getDataRows()) {
        if ( ! isInTotalRowsAliases(dataRow.getAlias())) {      //строку итогов не проверяем
            def rnu46Row = getRnu46Row(rnu46FormData, dataRow)
            def rnu49Row = getRnu49Row(rnu49FormData, dataRow)

            def values = getValues(rnu46Row, rnu49Row)

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

/**
 * заполняем ячейки, вычисляемые автоматически
 */
def calcValues() {
    def rnu46FormData = getRnu46FormData()
    def rnu49FormData = getRnu49FormData()

    for (def dataRow : formData.getDataRows()) {
        if ( ! isInTotalRowsAliases(dataRow.getAlias())) {      //строку итогов не заполняем
            def rnu46Row = getRnu46Row(rnu46FormData, dataRow)
            def rnu49Row = getRnu49Row(rnu49FormData, dataRow)

            def values = getValues(rnu46Row, rnu49Row)

            values.keySet().each { colName ->
                dataRow[colName] = values[colName]
            }
        }
    }
}

def getRnu46FormData() {
    //todo за какой период брать форму?  с.м. http://jira.aplana.com/browse/SBRFACCTAX-2731
}

def getRnu49FormData() {
    //todo за какой период брать форму?  с.м. http://jira.aplana.com/browse/SBRFACCTAX-2731
}

/**
 * получаем мапу со значениями, расчитанными для каждой конкретной строки
 */
def getValues(def rnu46Row, def rnu49Row) {
    def values = [:]

    values.with {
        /*2*/   operationDate = getOperationDate(rnu49Row)
        /*5*/   baseNumber = getBaseNumber(rnu49Row)
        /*6*/   baseDate = getBaseDate(rnu49Row)
        /*7*/   summ = getSumm(rnu46Row)
    }

    return values
}

/**
 * В РНУ-46 находим строку, для которой «графа 2» = «графа 4» текущей строки формы РНУ-44.
 */
def getRnu46Row(rnu46FormData, dataRow){
    return rnu46FormData.dataRows.find { rnu46DataRow ->
        rnu46DataRow.invNumber == dataRow.inventoryNumber
    }
}

/**
 * В разделе А РНУ-49 находим строку, для которой «графа 6» = «графа 4» текущей строки формы РНУ-44.
 */
def getRnu49Row(rnu49FormData, dataRow) {

    //находим границы раздела "А" в РНУ-49
    def startToSearchIndex = rnu49FormData.dataRows.indexOf(rnu49FormData.getDataRow(getRnu49AIndex()))
    def endToSearchIndex = rnu49FormData.dataRows.indexOf(rnu49FormData.getDataRow(getRnu49TotalAIndex()))

    return startToSearchIndex..endToSearchIndex.find { index ->
        rnu49FormData.dataRows[index].invNumber = dataRow.inventoryNumber
    }
}

def getRnu49AIndex(){
    return 'A'
}

def getRnu49TotalAIndex(){
    return 'totalA'
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

def getSumm(rnu46Row) {
    return rnu46Row.cost10perTaxPeriod
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
    return ['number', 'bill', 'purchaseDate', 'purchasePrice', 'purchaseOutcome', 'implementationDate', 'implementationPrice',
            'implementationOutcome', 'price', 'percent', 'implementationpPriceTax', 'allIncome',
            'implementationPriceUp', 'income']
}

/**
 * проверяем актуальность итоговых значения
 */
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

/**
 * проверяем заполнения столбцов по алиасам этих столбцов
 */
boolean checkColsFilledByAliases(List colsAliases) {
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

/***********   ДОБАВЛЕНИЕ СТРОКИ В ТАБЛИЦУ С ФИКСИРОВАННЫМИ СТРОКАМИ ИТОГОВ   ***********/

/**
 * добавляет строку в таблицу с фиксированными строками итогов. строка добавляется перед выделенной
 * строкой (если такая есть). если выделенной строки нет, то строка добавляется в конец таблицы перед
 * последней итоговой строкой
 */
def addNewRow() {
    def newRow = formData.createDataRow()

    makeCellsEditable(newRow)

    int index = getNewRowIndex()

    formData.dataRows.add(index, newRow)

    return newRow
}

/**
 * делает ячейки, алиасы которых есть в списке редактируемых, редактируемыми
 */
def makeCellsEditable(def row) {
    getEditableColsAliases().each {
        row.getCell(it).editable = true
    }
}

/**
 * возвращает список алиасов столбцов, доступных для редактирования пользователем
 */
def getEditableColsAliases() {
    return ['name', 'inventoryNumber']
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

/***********   ОБЩИЕ ФУНКЦИИ ДЛЯ ИТОГОВЫХ СТРОК   ***********/

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

def getTotalRowAlias() {
    return 'total'
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
    return ['summ']
}/**
 * ������ ��� ���-44 (rnu44.groovy).
 * (���-44) ������� ���������� ����� �������, � ���� ��������������� ��������������� ������ ��� ���������� �����,
 * ��� �� ��������� 5 ��� � ���� ����� � ������������ ��������������� ����� � ���������� ��������� ��� �������� �������
 * �������� � ������������ ����� 01.01.2013
 *
 * ������ ���: 64
 *
 * TODO:
 *
 * @author vsergeev
 *
 * �����:
 *
 *                  *****   ��� 44  *****
 * 1    number          � ��
 * 2    operationDate   ���� ��������
 * 3    name            �������� ��������
 * 4    inventoryNumber ����������� �����
 * 5    baseNumber      �����
 * 6    baseDate        ����
 * 7    summ            ����� ��������������� ��������������� ������
 *
 *                  *****   ��� 46  *****
 * 1    rowNumber               � ��
 * 2    invNumber               ���. �����
 * 3    name                    ������������ �������
 * 4    cost                    �������������� ���������
 * 5    amortGroup              ��������������� ������
 * 6    usefulLife              ���� ��������� �������������, (���.)
 * 7    monthsUsed              ���������� ������� ������������ ����������� �������������� (��������������,
 *                              �������������)
 * 8    usefulLifeWithUsed      ���� ��������� ������������� � ������ ����� ������������ ����������� ��������������
 *                              (��������������, �������������) ���� ������������� ��������������, (���.)
 * 9    specCoef                ����������� �����������
 * 10   cost10perMonth          �� �����
 * 11   cost10perTaxPeriod      � ������ ���������� �������
 * 12   cost10perExploitation   � ���� ����� � ������������
 * 13   amortNorm               ����� ����������� (% � ���.)
 * 14   amortMonth              �� �����
 * 15   amortTaxPeriod          � ������ ���������� �������
 * 16   amortExploitation       � ���� ����� � ������������
 * 17   exploitationStart       ���� ����� � ������������
 * 18   usefullLifeEnd          ���� ��������� ����� ��������� �������������
 * 19   rentEnd                 ���� ��������� ����� �������� ������ / �������� �������������� �����������
 *
 *                  *****   ��� 49  *****
 * 1    rowNumber   � ��
 * 2    firstRecordNumber       ����� ������ ������
 * 3    operationDate           ���� ��������
 * 4    reasonNumber            �����
 * 5    reasonDate              ����
 * 6    invNumber               ����������� �����
 * 7     name                   ������������
 * 8    price                   ���� ������������
 * 9    amort                   ���������� ��������� ����������� (�������� �� �������)
 * 10   expensesOnSale          ������� ��� ����������
 * 11   sum                     ����� ����������� ������� �� ����������
 * 12   sumInFact               ����� ���������� ����������� �������� �������
 * 13   costProperty            ��������� ���������� � ���������, ���������� ��� ���������� �������� �������
 * 14   marketPrice             �������� ����
 * 15   sumIncProfit            ����� � ���������� ������� (���������� ������)
 * 16   profit                  ������� �� ����������
 * 17   loss                    ������ �� ����������
 * 18   usefullLifeEnd          ���� ��������� ����� ��������� �������������
 * 19   monthsLoss              ���������� ������� ��������� ������� �� �������
 * 20   expensesSum             ����� ��������, ������������ �� ������ �����
 * 21   saledPropertyCode       ���� ���� �������������� (���������) ���������
 * 22   saleCode                ���� ���� ���������� (�������)
 * 23   propertyType            ��� ���������
 */
switch (formDataEvent) {
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        if (logicalCheck(false)) {
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

def getTotalRowIndex() {
    return formData.getDataRowIndex(getTotalRowAlias())
}

/********************************   ����� �������   ********************************/

/**
 * false, ���� � ������ ��� �������� ��� ������ null
 * true, ���� � ������ ���� �������
 */
boolean isBlankOrNull(value) {
    return (value == null || value.equals(''))
}

/**
 * ���������� true, ���� � ������� ������� �����-������ �������
 * ����� ���������� false
 */
boolean isCurrentDataRowSelected() {
    return (currentDataRow != null && formData.dataRows.indexOf(currentDataRow) >= 0)
}

/***********   ���������� ������ � ������� � �������������� �������� ������   ***********/

/**
 * ��������� ������ � ������� � �������������� �������� ������. ������ ����������� ����� ����������
 * ������� (���� ����� ����). ���� ���������� ������ ���, �� ������ ����������� � ����� ������� �����
 * ��������� �������� �������
 */
def addNewRow() {
    def newRow = formData.createDataRow()

    makeCellsEditable(newRow)

    int index = getNewRowIndex()

    formData.dataRows.add(index, newRow)

    return newRow
}

/**
 * ������ ������, ������ ������� ���� � ������ �������������, ��������������
 */
def makeCellsEditable(def row) {
    getEditableColsAliases().each {
        row.getCell(it).editable = true
    }
}

/**
 * ���������� ������ ������� ��������, ��������� ��� �������������� �������������
 */
def getEditableColsAliases() {
    return ['name', 'inventoryNumber']
}

/**
 * ���������� ������ ��� ������������ �������
 * (������� ������, ��������������� �������� addNewRow(). �� �� ����� ����� ����������� ������� ����� ������)
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
 * ���� ����� �� �������, ������� �� ������ � �������� startIndex (������������). ������� ������ ����������
 * ������ (����� ������� �� ������� ��� �������� � getTotalRowsAliases()).
 *
 * ���������� ������ ���� ������.
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

/***********   ����� ������� ��� �������� �����   ***********/

/**
 * false, ���� ����� ������ �� ������ � ������ ������� �������� �����
 * true, ���� ����� ������ ������ � ����� �������� �����
 */
boolean isInTotalRowsAliases(def alias){
    return (totalRowsAliases.find {totalAlias -> alias == totalAlias} != null)
}

/**
 * ���������� ������ ������� ��� �������� �����
 */
def getTotalRowsAliases() {
    return ['total']
}

def getTotalRowAlias() {
    return 'total'
}

/***********   �������� ������ �� ������� � �������������� �������� ������   ***********/

/**
 * ������� ���������� ������, ���� ��� �� �������� ��������
 * ���� ���������� ������ �������� ��������, �� ��� �� ��������� � ��������� ��������� � ����������� ������
 */
def deleteCurrentRow() {
    if (isCurrentDataRowSelected() &&
            totalRowsAliases.find { totalRowAlias ->
                totalRowAlias == currentDataRow.getAlias()
            } == null) {
        formData.dataRows.remove(currentDataRow)
    } else {
        logger.error ('���������� ������� ������������� ������!')
    }
}

/***********   ����� ������� ��� ��������, �� ������� ���������� �����   ***********/

/**
 * ������� ��� ���� �����, ����� ��������, ����� �� ��������, �� ������� �������� �����
 * ���������� ���� ���� �����_������� -> ��������_��������
 */
def getTotalResults() {
    def result = [:]
    for (def colAlias : getTotalColsAliases()) {
        result.put(colAlias, formData.dataRows.sum {row ->
            if (! isInTotalRowsAliases(row.getAlias())) {    //������ �� ������ � ��������
                row[colAlias]
            } else {
                0
            }
        })
    }
    return result
}

/**
 * ���������� ������ ������� ��� �������, �� ������� ���������� �����
 */
def getTotalColsAliases() {
    return ['summ']
}