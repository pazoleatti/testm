/**
 * Скрипт для РНУ-45
 * Форма "(РНУ-45) Регистр налогового учёта «ведомость начисленной амортизации по нематериальным активам»"
 *
 * @author akadyrgulov
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck()
        break
    case FormDataEvent.CALCULATE :
        calc() && logicalCheck()
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
        logicalCheck()
        break
    // после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        break
    // обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc() && logicalCheck()
        calc() && logicalCheck()
        break
}

// графа 1	- rowNumber
// графа 2	- inventoryNumber
// графа 3	- name
// графа 4	- buyDate
// графа 5	- usefulLife
// графа 6	- expirationDate
// графа 7	- startCost
// графа 8	- depreciationRate
// графа 9	- amortizationMonth
// графа 10	- amortizationSinceYear
// графа 11	- amortizationSinceUsed

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)
    def newRow = formData.createDataRow()

    // графа 2..7
    ['inventoryNumber', 'name', 'buyDate', 'usefulLife',
            'expirationDate', 'startCost'].each {
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

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
boolean calc() {
    def data = getData(formData)

    /*
	 * Проверка обязательных полей.
	 */
    // список проверяемых столбцов (графа 2..7)
    def requiredColumns = ['inventoryNumber', 'name', 'buyDate',
            'usefulLife', 'expirationDate', 'startCost']
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

    // отсортировать
    data.save(getRows(data).sort { (it.inventoryNumber) })

    def formDataOld = getFormDataOld()
    def dataOld = getData(formDataOld)
    def index = 1
    /*
     * Расчеты.
     */

    for (def row : getRows(data)) {
        // 5 графу необходимо проверить на 0, т.к. при расчете графы 8 она является знаменателем
        if (row.usefulLife == 0) {
            logger.error("В строке $row.rowNumber введен некорректный срок полезного использования!")
            return false
        }

        def oldRow10 = getValueForColumn10(dataOld, row)
        def oldRow11 = getValueForColumn11(dataOld, row)

        if (oldRow10 == 0 || oldRow11 == 0) {
            logger.error("Не найдена форма или запись за предшествующий период!")
            return false
        }

        // графа 1
        row.rowNumber = index
        index ++
        // «Графа 8» = ОКРУГЛ(1 / «Графа 5»  100%;4)
        row.depreciationRate = roundTo(((1 / row.usefulLife) * 100), 4)

        // «Графа 9» = ОКРУГЛ(«Графа 7»* «Графа 8»;2)
        row.amortizationMonth = roundTo(row.startCost * row.depreciationRate, 2)

        // графа 10, 11
        //Если «Графа 4» =Январь, то «Графа 10»=«Графа 9».
        //Иначе, «Графа 10» = «Графа 10» за предыдущий месяц + «Графа 9».

        // Если  «Графа 4» в отчетном месяце, то
        // «Графа 11» = «Графа 9»
        // Иначе, «Графа 11» = «Графа 11» за предыдущий месяц + «Графа 9»

        Calendar buyDate = Calendar.getInstance()
        buyDate.setTime(row.buyDate)
        // TODO (Aydar Kadyrgulov) проверку доделать
        if (buyDate.get(Calendar.MONTH) == Calendar.JANUARY) {
            row.amortizationSinceYear = row.amortizationMonth;
            row.amortizationSinceUsed = row.amortizationMonth
        } else {
            row.amortizationSinceYear = oldRow10 + row.amortizationMonth;
            row.amortizationSinceUsed = oldRow11 + row.amortizationMonth;
        }

    }

    data.save(getRows(data).sort {(it.inventoryNumber) })

    // графа 7, 9, 10, 11 для последней строки "итого"
    def total7 = 0
    def total9 = 0
    def total10 = 0
    def total11 = 0
    getRows(data).each { row ->
        total7 += row.startCost
        total9 += row.amortizationMonth
        total10 += row.amortizationSinceYear
        total11 += row.amortizationSinceUsed
    }

    // добавить строки "итого"
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    totalRow.startCost = total7
    totalRow.amortizationMonth = total9
    totalRow.amortizationSinceYear = total10
    totalRow.amortizationSinceUsed = total11
    setTotalStyle(totalRow)
    insert(data, totalRow)
    return true

// TODO (Aydar Kadyrgulov) убрать
// графа 1	- rowNumber
// графа 2	- inventoryNumber
// графа 3	- name
// графа 4	- buyDate
// графа 5	- usefulLife
// графа 6	- expirationDate
// графа 7	- startCost
// графа 8	- depreciationRate
// графа 9	- amortizationMonth
// графа 10	- amortizationSinceYear
// графа 11	- amortizationSinceUsed
    return true
}

def logicalCheck() {
// TODO (Aydar Kadyrgulov)
    def data = getData(formData)

    // 1. Проверка на заполнение полей 1..11
    def requiredColumns = ['rowNumber', 'inventoryNumber',
            'name', 'buyDate', 'usefulLife',
            'expirationDate', 'startCost',
            'depreciationRate', 'amortizationMonth',
            'amortizationSinceYear', 'amortizationSinceUsed']

    for (def row : getRows(data)) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    if (!getRows(data).isEmpty()) {


        // суммы строки общих итогов
        def totalSums = [:]

        // столбцы для которых надо вычислять итого. Графа 7, 9, 10, 11
        def totalColumns = ['startCost', 'amortizationMonth', 'amortizationSinceYear', 'amortizationSinceUsed']

        // признак наличия итоговых строк
        def hasTotal = false
        for (def row : getRows(data)) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }
            // На всякий случай проверить графу 5 на ноль, т.к. используется для расчета графы 8
            if (row.usefulLife == 0) {
                logger.error("В строке $row.rowNumber введен некорректный срок полезного использования!")
                return false
            }

            // 2. Проверка на уникальность поля «инвентарный номер»
            for (def rowB : getRows(data)) {
                if(!row.equals(rowB) && row.inventoryNumber ==rowB.inventoryNumber){
                    logger.error("В строке $row.rowNumber нарушена уникальность инвентарного номера!")
                    return false
                }
            }

            // 3. Проверка на нулевые значения
            if (row.startCost == 0 && row.amortizationMonth == 0 && row.amortizationSinceYear == 0 && row.amortizationSinceUsed == 0) {
               logger.error("В строке $row.rowNumber все суммы по операции нулевые!")
               return false
            }

            // 4. Арифметические проверки расчета неитоговых граф
            // графа 8
            if (row.depreciationRate != roundTo(((1 / row.usefulLife) * 100), 4)) {
                logger.error("В строке $row.rowNumber ")
                return false
            }
            // графа 9
            if (row.amortizationMonth != roundTo((row.startCost * row.depreciationRate), 2)) {
                logger.error("В строке $row.rowNumber ")
                return false
            }
            // графа 10
            if (row.amortizationSinceYear == 1) {
                logger.error("В строке $row.rowNumber ")

                return false
            }
            // графа 11
            if (row.amortizationSinceUsed == 1) {
                logger.error("В строке $row.rowNumber ")
                return false
            }

            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += row.getCell(alias).getValue()
            }
        }

        if (hasTotal) {
            def totalRow = getRowByAlias(data, 'total')

            // 9. Проверка итогового значений по всей форме
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    logger.error('Итоговые значения рассчитаны неверно!')
                    return false
                }
            }
        }

        }

    return true
}
/**
 * Проверка при создании формы.
 */
void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriodService.isBalancePeriod(reportPeriod.id, formData.departmentId)) {
        logger.error('Налоговая форма не может создаваться в периоде ввода остатков.')
        return
    }

    def findForm = formDataService.find(formData.formType.id,
            formData.kind, formData.departmentId, formData.reportPeriodId)

    // TODO (Aydar Kadyrgulov) пока не реализован выбор месяца.
    // но в будущем у новой формы будет заполнено поле periodOrder (месяц)

    if (findForm != null && findForm.periodOrder == formData.periodOrder) {
        logger.info('periodOrder = ' + findForm.periodOrder)
        logger.error('Налоговая форма с заданными параметрами уже существует.')
    }
}

/*
 * Вспомогательные методы.
 */

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
 * Установить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    [ 'rowNumber', 'fix', 'inventoryNumber', 'name',
            'buyDate', 'usefulLife', 'expirationDate',
            'startCost', 'depreciationRate', 'amortizationMonth',
            'amortizationSinceYear', 'amortizationSinceUsed'
    ].each {
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

/**
 * Получить данные за предыдущий месяц
 */
def getFormDataOld() {
    // необходима форма за предыдущий месяц (formData.periodOrder - 1)
    def prevMonth = formData.periodOrder - 1

    // определяем к какому отчетному периоду принадлежит предыдущий месяц
    def currentReportPeriod = reportPeriodService.get(formData.reportPeriodId)
    int currentMonth = currentReportPeriod.getMonths()
    logger.info("previous month $prevMonth month $currentMonth")
    if (prevMonth < currentMonth && prevMonth > (currentMonth - 3)) {
        logger.info('Предыдущий месяц находится в текущем отчетном периоде')
    } else {
        logger.info('Предыдущий месяц находится в предыдущем отчетном периоде')
    }
    // TODO (Aydar Kadyrgulov) нужен метод, чтобы получить форму за предыдущий месяц.

    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-45 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = formDataService.find(formData.formType.id, formData.kind, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

/**
 * Получить значение за предыдущий отчетный период для графы 10.
 *
 * @param dataOld данные за предыдущий период
 * @param row строка текущего периода
 * @return возвращает найденое значение, иначе возвратит 0
 */
def getValueForColumn10(def dataOld, def row) {
    def value = null
    def count = 0
    if (dataOld != null && !getRows(dataOld).isEmpty()) {
        for (def rowOld : getRows(dataOld)) {
            if (rowOld.inventoryNumber == row.inventoryNumber) {
                value = rowOld.amortizationSinceYear
                if (value!=null) {
                    count += 1
                }
            }
        }
    }
    // если count не равно 1, то или нет формы за предыдущий период,
    // или нет соответствующей записи в предыдущем периода или записей несколько
    return (count == 1 ? value : 0)
}

/**
 * Получить значение за предыдущий отчетный период для графы 10.
 *
 * @param dataOld данные за предыдущий период
 * @param row строка текущего периода
 * @return возвращает найденое значение, иначе возвратит 0
 */
def getValueForColumn11(def dataOld, def row) {
    def value = null
    def count = 0
    if (dataOld != null && !getRows(dataOld).isEmpty()) {
        for (def rowOld : getRows(dataOld)) {
            if (rowOld.inventoryNumber == row.inventoryNumber) {
                value = rowOld.amortizationSinceUsed
                if (value!=null) {
                    count += 1
                }
            }
        }
    }
    // если count не равно 1, то или нет формы за предыдущий период,
    // или нет соответствующей записи в предыдущем периода или записей несколько
    return (count == 1 ? value : 0)
}