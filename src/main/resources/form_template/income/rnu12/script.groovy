/**
 * Скрипт для РНУ-12 (rnu12.groovy).
 * Форма "(РНУ-12) Регистр налогового учёта расходов по хозяйственным операциям и оказанным Банку услугам".
 *
 * @version 59
 *
 * @author rtimerbaev
 */

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

// графа 1  - rowNumber
// графа 2  - code
// графа 3  - numberFirstRecord
// графа 4  - opy
// графа 5  - operationDate
// графа 6  - name
// графа 7  - documentNumber
// графа 8  - date
// графа 9  - periodCounts
// графа 10 - advancePayment
// графа 11 - outcomeInNalog
// графа 12 - outcomeInBuh

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)
    def newRow = formData.createDataRow()

    // графа 2..10, 12
    ['code', 'numberFirstRecord', 'opy', 'operationDate',
            'name', 'documentNumber', 'date', 'periodCounts',
            'advancePayment', 'outcomeInBuh'].each {
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
    // список проверяемых столбцов (графа 2..10, 12)
    def requiredColumns = ['code', 'numberFirstRecord', 'opy',
            'operationDate', 'name', 'documentNumber', 'date',
            'periodCounts', 'advancePayment', 'outcomeInBuh']
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
    data.save(getRows(data).sort { getCodeAttribute(it.code) })

    /*
     * Расчеты.
     */

    getRows(data).eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        // графа 11
        if (row.advancePayment > 0 && row.periodCounts != null) {
            row.outcomeInNalog = roundTo((row.advancePayment / row.periodCounts), 2)
        }
    }

    data.save(getRows(data).sort { getCodeAttribute(it.code) })

    // графа 10, 12 для последней строки "итого"
    def total10 = 0
    def total11 = 0
    def total12 = 0
    getRows(data).each { row ->
        total10 += row.advancePayment
        total11 += row.outcomeInNalog
        total12 += row.outcomeInBuh
    }

    /** Столбцы для которых надо вычислять итого и итого по коду. Графа 10, 11, 12. */
    def totalColumns = ['advancePayment', 'outcomeInNalog', 'outcomeInBuh']

    // посчитать "итого по коду"
    def totalRows = [:]
    def tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }

    getRows(data).eachWithIndex { row, i ->
        if (tmp == null) {
            tmp = row.code
        }
        // если код расходы поменялся то создать новую строку "итого по коду"
        if (tmp != row.code) {
            totalRows.put(i, getNewRow(tmp, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        // если строка последняя то сделать для ее кода расхода новую строку "итого по коду"
        if (i == getRows(data).size() - 1) {
            totalColumns.each {
                sums[it] += row.getCell(it).getValue()
            }
            totalRows.put(i + 1, getNewRow(row.code, totalColumns, sums))
            totalColumns.each {
                sums[it] = 0
            }
        }
        totalColumns.each {
            sums[it] += row.getCell(it).getValue()
        }
        tmp = row.code
    }

    // добавить "итого по коду" в таблицу
    def i = 0
    totalRows.each { index, row ->
        data.insert(row, index + i + 1)
        i = i + 1
    }

    // добавить строки "итого"
    def totalRow = formData.createDataRow()
    totalRow.setAlias('total')
    totalRow.fix = 'Итого'
    totalRow.getCell('fix').colSpan = 2
    totalRow.advancePayment = total10
    totalRow.outcomeInNalog = total11
    totalRow.outcomeInBuh   = total12
    setTotalStyle(totalRow)
    insert(data, totalRow)
    return true
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
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

        // список обязательных столбцов (графа 1..12)
        def requiredColumns = ['rowNumber', 'code', 'numberFirstRecord', 'opy',
                'operationDate', 'name', 'documentNumber', 'date', 'periodCounts',
                'advancePayment', 'outcomeInNalog', 'outcomeInBuh']

        // суммы строки общих итогов
        def totalSums = [:]

        // столбцы для которых надо вычислять итого и итого по коду классификации дохода. Графа 10, 11, 12
        def totalColumns = ['advancePayment', 'outcomeInNalog', 'outcomeInBuh']

        // признак наличия итоговых строк
        def hasTotal = false

        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        for (def row : getRows(data)) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 6. Обязательность заполнения поля графы 1-12
            if (!checkRequiredColumns(row, requiredColumns)) {
                return false
            }

            // 1. Проверка даты совершения операции и границ отчетного периода (графа 5)
            if (row.operationDate < a || b < row.operationDate) {
                logger.error("В строке $row.rowNumber дата совершения операции вне границ отчётного периода!")
                return false
            }

            // 2. Проверка количества отчетных периодов при авансовых платежах (графа 9)
            if (row.periodCounts < 1 || 999 < row.periodCounts) {
                logger.error("В строке $row.rowNumber неверное количество отчетных периодов при авансовых платежах!")
                return false
            }

            // 3. Проверка на нулевые значения (графа 11, 12)
            if (row.outcomeInNalog == 0 && row.outcomeInBuh == 0) {
                logger.error("В строке $row.rowNumber все суммы по операции нулевые!")
                return false
            }

            // 4. Проверка формата номера первой записи
            if (!row.numberFirstRecord.matches('\\d{2}-\\w{6}')) {
                logger.error("В строке $row.rowNumber неправильно указан номер первой записи (формат: ГГ-НННННН, см. №852-р в актуальной редакции)!")
                return false
            }

            // 7. Проверка на уникальность поля «№ пп» (графа 1)
            for (def rowB : getRows(data)) {
                if(!row.equals(rowB) && row.rowNumber ==rowB.rowNumber){
                    logger.error("В строке $row.rowNumber нарушена уникальность номера по порядку!")
                    return false
                }
            }

            // 8. Проверка итоговых значений по кодам классификации дохода - нахождение кодов классификации расхода
            if (!totalGroupsName.contains(row.code)) {
                totalGroupsName.add(row.code)
            }

            // 9. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += row.getCell(alias).getValue()
            }
        }

        if (hasTotal) {
            def totalRow = getRowByAlias(data, 'total')

            // 5. Проверка на превышение суммы расхода по данным бухгалтерского учёта над суммой начисленного расхода (графа 11, 12)
            if (totalRow.outcomeInNalog <= totalRow.outcomeInBuh) {
                logger.warn("Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!")
            }

            // 8. Проверка итоговых значений по кодам классификации расхода
            for (def codeName : totalGroupsName) {
                def row = getRowByAlias(data, 'total' + codeName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                        logger.error("Итоговые значения по коду $codeName рассчитаны неверно!")
                        return false
                    }
                }
            }

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
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def data = getData(formData)
    if (!getRows(data).isEmpty()) {
        // справочник 27 - «Классификатор расходов Сбербанка России для целей налогового учёта»
        def expensesClassifierRefBookId = 27


        for (def row : getRows(data)) {
            if (isTotal(row)) {
                continue
            }

            def refDataProvider = refBookFactory.getDataProvider(expensesClassifierRefBookId)

            // 1. Проверка кода классификации расхода для данного РНУ (графа 2)
            def knuRecords = refDataProvider.getRecords(row.operationDate, null, "CODE = " + getCodeAttribute(row.code), null)
            if (knuRecords == null || knuRecords.getRecords() == null || knuRecords.getRecords().size() == 0) {
                logger.error("Операция в строке $row.rowNumber в РНУ не учитывается!")
                return false
            }

            // 2. Проверка символа ОПУ для кода классификации расхода (графа 4)
            if (refBookService.getRecordData(expensesClassifierRefBookId, row.opy) == null) {
                logger.error("Символ ОПУ в строке $row.rowNumber в справочнике отсутствует!")
                return false
            }

            if (row.code != row.opy) {
                logger.error("В строке $row.rowNumber \"Код налогового учета\" не совпадает с \"Символом ОПУ\"!")
                return false
            }
        }
    }
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    // TODO (Aydar Kadyrgulov)
    // удалить все строки и собрать из источников их строки
    getRows(data).clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                source.getDataRows().each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        getRows(data).add(row)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
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
