/**
 * Скрипт для РНУ-61
 * Форма "(РНУ-61) Регистр налогового учёта расходов по процентным векселям ОАО «Сбербанк России», учёт которых требует применения метода начисления"
 *
 * @author akadyrgulov
 */

// графа 1  - rowNumber
// графа 2  - billNumber
// графа 3  - creationDate
// графа 4  - nominal
// графа 5  - currencyCode
// графа 6  - rateBRBill
// графа 7  - rateBROperation
// графа 8  - paymentStart
// графа 9  - paymentEnd
// графа 10 - interestRate
// графа 11 - operationDate
// графа 12 - sum70606
// графа 13 - sumLimit
// графа 14 - percAdjustment

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
        allCheck()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        if (allCheck()) {
            // для сохранения изменений приемников
            data.commit()
        }
        break
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
def calc() {
    def data = getData(formData)

    Calendar periodStartDate = reportPeriodService.getStartDate(formData.reportPeriodId)
    def daysOfYear = (new GregorianCalendar()).isLeapYear(periodStartDate.get(Calendar.YEAR)) ? 365 : 366

    /*
	 * Проверка обязательных полей.
	 */

    // список проверяемых столбцов (графа 2..5, 18..14)
    def requiredColumns = ['billNumber', 'creationDate', 'nominal',
            'currencyCode', 'paymentStart', 'paymentEnd', 'interestRate',
            'operationDate', 'sum70606']
    for (def row : getRows(data)) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    /**
     * Удалим все строки и итого
     * для этого соберем алиасы, затем удалим все
     */
    def totalAlases = []
    getRows(data).each{row->
        if (row.getAlias() != null && isTotalRow(row)) {
            totalAlases += row.getAlias()
        }
    }

    totalAlases.each{ alias ->
        data.delete(getRowByAlias(data, alias))
    }

    // отсортировать/группировать
    // отсортировать/группировать
    data.save(getRows(data).sort { it.billNumber })

    /*
     * Расчеты.
     */

    getRows(data).eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        row.rateBRBill = getRate(row, row.creationDate)
        if (row.rateBRBill == null) {
            return false
        }

        row.rateBROperation = getRate(row, row.operationDate)
        if (row.rateBROperation == null) {
            return false
        }

        /*Если «Графа 11» < «Графа 9» и «Графа 12» не заполнена (т.е. для векселей, погашенных до окончания срока платежа), то
        «Графа 13» = («Графа 4» ´ «Графа 10» / 100 ´ («Графа 11» - «Графа 3») / 365 (366)), с округлением до двух знаков после запятой по правилам округления ´ «Графа 7»

        Если «Графа 11» > «графы 9» и «Графа 12» не заполнена (т.е. для векселей, погашенных после окончания срока платежа), то
        «Графа 13» = («Графа 4» ´ «Графа 10» / 100 ´ («Графа 9» - «Графа 3») / 365 (366)), с округлением до двух знаков после запятой по правилам округления ´ «Графа 7»

        Если «Графа 3» и «Графа 9» принадлежат разным годам и продолжительность каждого года разная (в одном 365 дней, в другом 366), то
        Заполняется вручную

        Если «Графа 5» ≠ 810 (вексель в валюте) и проводится пересчет, то
        «Графа 13» не заполняется.
*/
        if ((row.sum70606 != null && row.sum70606 != ''))
            {
                if (row.operationDate < row.paymentEnd) {
                    logger.info('графа 11 - графа 3 = ' + (row.operationDate - row.creationDate))
                    row.sumLimit = roundTo((row.nominal * row.interestRate / 100 * (row.operationDate - row.creationDate) / daysOfYear), 2) * row.rateBROperation
                }

                if (row.operationDate > row.paymentEnd) {
                    logger.info('графа 11 - графа 3 = ' + (row.operationDate - row.creationDate))
                    row.sumLimit = roundTo((row.nominal * row.interestRate / 100 * (row.paymentEnd - row.creationDate) / daysOfYear), 2) * row.rateBROperation
                }
            }


        if (row.sum70606 != null && row.sum70606 != '') {
            if (row.sum70606 > row.sumLimit) {
                row.percAdjustment = row.sum70606 - row.sumLimit
            }
        } else {
            row.percAdjustment = row.nominal * (row.rateBRBill - row.rateBROperation)

            //if (row.operationDate)
        }

        // TODO (Aydar Kadyrgulov) http://jira.aplana.com/browse/SBRFACCTAX-4594
// графа 1  - rowNumber
// графа 2  - billNumber
// графа 3  - creationDate
// графа 4  - nominal
// графа 5  - currencyCode
// графа 6  - rateBRBill
// графа 7  - rateBROperation
// графа 8  - paymentStart
// графа 9  - paymentEnd
// графа 10 - interestRate
// графа 11 - operationDate
// графа 12 - sum70606
// графа 13 - sumLimit
// графа 14 - percAdjustment
    }

    // сохраняем и подсчитываем итого
    data.save(getRows(data).sort { it.billNumber })

    return true
}

def logicalCheck(){
    return true
}

def checkNSI(){
    return true
}

def allCheck() {
    return !hasError() && logicalCheck() && checkNSI()
}

/**
 * Имеются ли фатальные ошибки.
 */
def hasError() {
    return logger.containsLevel(LogLevel.ERROR)
}

/**
 * Проверка при создании формы.
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

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)
    def newRow = formData.createDataRow()

    ['billNumber', 'creationDate', 'nominal',
            'currencyCode', 'paymentStart', 'paymentEnd', 'interestRate',
            'operationDate', 'sum70606'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }


    def i = getRows(data).size()
    while(i>0 && isTotalRow(getRows(data).get(i-1))){i--}
    data.insert(newRow, i + 1)

    // проставление номеров строк
    i = 1;
    getRows(data).each{ row->
        if (!isTotal(row)) {
            row.rowNumber = i++
        }
    }
    save(data)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    def data = getData(formData)
    data.delete(currentDataRow)
    // проставление номеров строк
    def i = 1;
    getRows(data).each{ row->
        if (!isTotal(row)) {
            row.rowNumber = i++
        }
    }
    save(data)
}


/*
 * Вспомогательные методы.
 */

def getRate(def row, def date) {
    def currencyRefBookId = 15L
    def RUB = '810'
    // справочник 22 "Курсы Валют"
    def refDataProvider = refBookFactory.getDataProvider(22)
    def currCode = refBookService.getRecordData(currencyRefBookId, row.currencyCode)

    if (currCode == null) {
        logger.error("В строке $row.rowNumber код валюты в справочнике отсутствует!")
        return null
    } else {
        if (currCode.get('CODE').getStringValue() == RUB) {
            return 1.0000
        }
        else {
            def records = refDataProvider.getRecords(date, null, "CODE_NUMBER = " + row.currencyCode, null)
            if (records != null && records.getRecords() != null && records.getRecords().size() > 0) {
                def record = records.getRecords().getAt(0)
                def rate = record.get('RATE') // атрибут "Курс валюты"
                return roundTo(rate.getNumberValue(), 4)
            } else {
                logger.warn("В строке $row.rowNumber неверный курс валюты!")
                return null
            }
        }
    }
    return null
}

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def data = getData(formData)
    def from = 0
    def to = getRows(data).size() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    newRow.fix = 'Итого по КНУ ' + (getCodeAttribute(alias))
    newRow.getCell('fix').colSpan = 2
    setTotalStyle(newRow)
    return newRow
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'fix', 'code', 'numberFirstRecord', 'numberFirstRecord', 'opy', 'operationDate',
            'name', 'documentNumber', 'date', 'periodCounts',
            'advancePayment', 'outcomeInNalog', 'outcomeInBuh'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Посчитать сумму указанного графа для строк с общим кодом классификации
 *
 * @param code код классификации дохода
 * @param alias название графа
 */
def calcSumByCode(def code, def alias) {
    def data = getData(formData)
    def sum = 0
    getRows(data).each { row ->
        if (!isTotal(row) && row.code == code) {
            sum += row.getCell(alias).getValue()
        }
    }
    return sum
}

/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    def data = getData(formData)
    getRows(data).indexOf(row)
}

/**
 * Проверить заполненость обязательных полей.
 *
 * @param row строка
 * @param columns список обязательных графов
 * @param useLog нужно ли записывать сообщения в лог
 * @return true - все хорошо, false - есть незаполненные поля
 */
def checkRequiredColumns(def row, def columns) {
    def colNames = []

    def cell
    columns.each {
        cell = row.getCell(it)
        if (cell.isEditable() && (cell.getValue() == null || row.getCell(it).getValue() == '')) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        def index = row.rowNumber
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getRows(getData(formData)).indexOf(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
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
 * Получить атрибут 130 - "Код налогового учёта" справочник 27 - "Классификатор расходов Сбербанка России для целей налогового учёта".
 *
 * @param id идентификатор записи справочника
 */
def getCodeAttribute(def id) {
    return refBookService.getStringValue(27, id, 'CODE')
}

/**
 * Проверка является ли строка итоговой (любой итоговой, т.е. по коду, либо основной)
 */
def isTotalRow(row){
    row.getAlias()==~/total\d*/
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
 * Вставить новыую строку в конец нф.
 *
 * @param data данные нф
 * @param row строка
 */
void insert(def data, def row) {
    data.insert(row, getRows(data).size() + 1)
}

/**
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    return data.getDataRow(getRows(data), alias)
}

/**
 * Хелпер для округления чисел
 * @param value
 * @param newScale
 * @return
 */
BigDecimal roundTo(BigDecimal value, int round) {
    if (value != null) {
        return value.setScale(round, BigDecimal.ROUND_HALF_UP)
    } else {
        return value
    }
}

