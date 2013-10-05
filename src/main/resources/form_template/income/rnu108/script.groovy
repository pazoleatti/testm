package form_template.income.rnu108
/**
* Скрипт для РНУ-108
* Форма "(РНУ-108) Регистр налогового учёта расходов, связанных с приобретением услуг у Взаимозависимых лиц и резидентов оффшорных зон и подлежащих корректировке в связи с применением цен, не соответствующих рыночному уровню"
*
* @author akadyrgulov 
*/

// графа 1 - rowNumber
// графа 2 - personName
// графа 3 - inn
// графа 4 - date
// графа 5 - code
// графа 6 - docNumber
// графа 7 - docDate
// графа 8 - contractNumber
// графа 9 - contractDate
// графа 10 - priceService
// графа 11 - priceMarket
// графа 12 - factSum
// графа 13 - correctKoef
// графа 14 - marketSum
// графа 15 - deviatSum
// графа 16 - code2

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck()
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc() && logicalCheck() && checkNSI()
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
        logicalCheck() && checkNSI()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc() && logicalCheck() && checkNSI()
        break
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
boolean calc() {

    def data = getData(formData)
    /*
	 * Проверка обязательных полей.
	 */
    // список проверяемых столбцов (графа 2..10, 12)
    def requiredColumns = ['personName', 'inn', 'date', 'code', 'docNumber',
            'docDate', 'contractNumber', 'contractDate', 'priceService',
            'priceMarket', 'correctKoef']
    for (def row : getRows(data)) {
        if (!isTotal(row) && !checkRequiredColumns(row, requiredColumns)) {
            return false
        }
    }

    /**
     * Удалим все строки c итого
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
    data.save(getRows(data).sort { (it.personName) })

    /*
     * Расчеты.
     */

    getRows(data).eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        // графа 12
        row.factSum = row.priceService

        // графа 14
        row.marketSum = row.priceMarket

        // графа 15
        row.deviatSum = Math.abs(row.marketSum - row.factSum)

    }

    data.save(getRows(data).sort { (it.personName) })

    // графа 15 для последней строки "итого"
    def total15 = 0
    getRows(data).each { row ->
        total15 += row.deviatSum
    }

    /** Столбцы для которых надо вычислять итого и итого по Взаимозависимому лицу (резиденту оффшорной зоны). Графа 15. */
    def totalColumns = ['deviatSum']

    // посчитать "итого по Взаимозависимому лицу (резиденту оффшорной зоны)"
    def totalRows = [:]
    def tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }

    getRows(data).eachWithIndex { row, i ->
        if (tmp == null) {
            tmp = row.personName
        }
        // если код расходы поменялся то создать новую строку "итого по Взаимозависимому лицу (резиденту оффшорной зоны)"
        if (tmp != row.personName) {
            totalRows.put(i, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по Взаимозависимому лицу (резиденту оффшорной зоны)"
        if (i == getRows(data).size() - 1) {
            totalColumns.each {
                sums[it] += row.getCell(it).getValue()
            }
            totalRows.put(i + 1, getNewRow(row.personName, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        totalColumns.each {
            sums[it] += row.getCell(it).getValue()
        }
        tmp = row.personName
    }

    // добавить "итого по Взаимозависимому лицу (резиденту оффшорной зоны)" в таблицу
    def i = 0
    totalRows.each { index, row ->
        data.insert(row, index + i + 1)
        i = i + 1
    }

    // добавить строки "итого"
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 4
    totalRow.deviatSum = total15
    setTotalStyle(totalRow)
    insert(data, totalRow)
    return true
}

/**
 * Логические проверки.
 *
 */
def logicalCheck() {
    def tmp
    def data = getData(formData)
    /** Дата начала отчетного периода. */
    tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def a = (tmp ? tmp.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def b = (tmp ? tmp.getTime() : null)

    if (!getRows(data).isEmpty()) {
        def i = 1

        // список обязательных столбцов (все, кроме графы 13)
        def requiredColumns = [ 'rowNumber', 'personName', 'inn', 'date', 'code',
                'docNumber', 'docDate', 'contractNumber', 'contractDate', 'priceService',
                'priceMarket', 'factSum', 'marketSum', 'deviatSum', 'code2']

        // суммы строки общих итогов
        def totalSums = [:]

        // столбцы для которых надо вычислять итого и итого по коду классификации дохода. Графа 15
        def totalColumns = ['deviatSum']

        // признак наличия итоговых строк
        def hasTotal = false

        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        for (def row : getRows(data)) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 1. Заполненность обязательных графов
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }

            // 2. Проверка на уникальность поля «№ пп»
            for (def rowB : getRows(data)) {
                if(!row.equals(rowB) && row.rowNumber ==rowB.rowNumber){
                    logger.error("В строке $row.rowNumber нарушена уникальность номера по порядку!")
                    return false
                }
            }

            // 3. Проверка даты совершения операции и границ отчётного периода
            if (row.date < a || b < row.date) {
                logger.error("В строке $row.rowNumber дата совершения операции вне границ отчётного периода!")
                return false
            }

            // 4. Арифметические проверки графы 12, 14, 15
            // графа 12
            if (row.factSum != row.priceService) {
                logger.error("В строке $row.rowNumber неверно рассчитана графа \"Сумма фактически начисленного расхода\"!")
                return false
            }
            // графа 14
            if (row.marketSum != row.priceMarket){
                logger.error("В строке $row.rowNumber неверно рассчитана графа \"Сумма расхода соответствующая рыночному уровню\"!")
                return false
            }
            // графа 15
            if (row.deviatSum != Math.abs(row.marketSum - row.factSum)) {
                logger.error("В строке $row.rowNumber неверно рассчитана графа \"Сумма отклонения (превышения) фактического расхода от рыночного уровня\"!")
                return false
            }

            // 5. Проверка итоговых значений по кодам классификации дохода - нахождение кодов классификации расхода
            if (!totalGroupsName.contains(row.personName)) {
                totalGroupsName.add(row.personName)
            }

            // 6. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += row.getCell(alias).getValue()
            }
        }

        if (hasTotal) {
            def totalRow = getRowByAlias(data, 'total')


            // 5. Проверка итоговых значений по кодам классификации расхода
            for (def personName : totalGroupsName) {
                def row = getRowByAlias(data, 'total' + personName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(personName, alias) != row.getCell(alias).getValue()) {
                        logger.error("Итоговые значения по наименованию взаимозависимого лица (резидента оффшорной зоны) $personName рассчитаны неверно!")
                        return false
                    }
                }
            }

            // 6. Проверка итогового значений по всей форме
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
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    return true
}
/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)
    def newRow = formData.createDataRow()

    // графы 2..11, 13
    [ 'personName', 'inn', 'date',
            'code', 'docNumber',
            'docDate', 'contractNumber',
            'contractDate', 'priceService',
            'priceMarket', 'correctKoef'].each {
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

    if (findForm != null) {
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
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    newRow.fix = 'Итого по Взаимозависимому лицу (резиденту оффшорной зоны)'
    newRow.getCell('fix').colSpan = 4
    setTotalStyle(newRow)
    return newRow
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    [ 'rowNumber', 'fix', 'personName', 'inn', 'date',
            'docDate', 'contractNumber', 'contractDate',
            'priceMarket', 'factSum', 'correctKoef', 'priceService',
            'marketSum', 'deviatSum', 'code2', 'code', 'docNumber',
    ].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}

/**
 * Посчитать сумму указанного графа для строк с общим наименованием взаимозависимого лица (резидента оффшорной зоны)
 *
 * @param personName Наименование взаимозависимого лица (резидента оффшорной зоны)
 * @param alias название графа
 */
def calcSumByCode(def personName, def alias) {
    def data = getData(formData)
    def sum = 0
    getRows(data).each { row ->
        if (!isTotal(row) && row.personName == personName) {
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
