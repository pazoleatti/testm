/**
 * Скрипт для РНУ-7 (rnu7.groovy).
 * Форма "(РНУ-7) Справка бухгалтера для отражения расходов, учитываемых в РНУ-5, учёт которых требует применения метода начисления".
 *
 * @version 59
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CREATE :
        checkCreation()
        break
    case FormDataEvent.CHECK :
        logicalCheck(true)
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck(false)
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
// проверка при "подготовить"
    case FormDataEvent.MOVE_CREATED_TO_PREPARED :
        checkOnPrepareOrAcceptance('Подготовка')
        break
// проверка при "принять"
    case FormDataEvent.MOVE_PREPARED_TO_ACCEPTED :
        checkOnPrepareOrAcceptance('Принятие')
        break
// проверка при "вернуть из принята в подготовлена"
    case FormDataEvent.MOVE_ACCEPTED_TO_PREPARED :
        checkOnCancelAcceptance()
        break
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        acceptance()
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicalCheck(false)
        checkNSI()
        break
}

// графа 1  - rowNumber
// графа 2  - code
// графа 3  - date
// графа 4  - balance
// графа 5  - docNumber
// графа 6  - docDate
// графа 7  - currencyCode
// графа 8  - rateOfTheBankOfRussia
// графа 9  - taxAccountingCurrency
// графа 10 - taxAccountingRuble
// графа 11 - accountingCurrency
// графа 12 - ruble

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def data = getData(formData)
    def newRow = formData.createDataRow()

    // Графы 2-5 Заполняется вручную
    ['balance', 'date', 'code', 'docNumber', 'docDate', 'currencyCode',
            'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'accountingCurrency'].each{ column ->
        newRow.getCell(column).setEditable(true)
    }
    insert(data, newRow)

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
void calc() {
    /*
     * Проверка объязательных полей.
     */

    def data = getData(formData)

    for (def row : getRows(data)) {
        if (!isTotal(row)) {
            // список проверяемых столбцов (графа ..)
            def requiredColumns = ['balance', 'date', 'code', 'docNumber', 'docDate', 'currencyCode',
                    'rateOfTheBankOfRussia', 'taxAccountingCurrency', 'accountingCurrency']
            if (!checkRequiredColumns(row, requiredColumns, true)) {
                return
            }
        }
    }

    /*
     * Расчеты.
     */

    // удалить строки "итого" и "итого по коду"
    def delRow = []
    getRows(data).each {
        if (isTotal(it)) {
            delRow += it
        }
    }
    delRow.each {
        getRows(data).remove(getIndex(it))
    }
    if (getRows(data).isEmpty()) {
        return
    }

    // отсортировать/группировать
    getRows(data).sort { it.code }

    // графа 8 (для строк НЕитого)
    getRows(data).each { row ->
        if (row.currencyCode != null && row.currencyCode == 'RUR') {
            row.rateOfTheBankOfRussia = 1
        }
    }

    getRows(data).eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        // графа 10 = графа 9 * графа 8
        row.taxAccountingRuble = round(row.taxAccountingCurrency * row.rateOfTheBankOfRussia, 2)

        // графа 12 = графа 11 * графа 8
        row.ruble = round(row.accountingCurrency * row.rateOfTheBankOfRussia, 2)
    }

    // графа 10, 12 для последней строки "итого"
    def total10 = 0
    def total12 = 0
    getRows(data).each { row ->
        total10 += row.taxAccountingRuble
        total12 += row.ruble
    }

    /** Столбцы для которых надо вычислять итого и итого по эмитенту. Графа 10, 12. */
    def totalColumns = ['taxAccountingRuble', 'ruble']

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
        getRows(data).add(index + i, row)
        i = i + 1
    }

    // добавить строки "итого"
    def totalRow = formData.createDataRow()
    getRows(data).add(totalRow)
    totalRow.setAlias('total')
    totalRow.code = 'Итого'
    setTotalStyle(totalRow)
    totalRow.taxAccountingRuble = total10
    totalRow.ruble = total12
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    def data = getData(formData)
    def tmp

    /** Дата начала отчетного периода. */
    tmp = reportPeriodService.getStartDate(formData.reportPeriodId)
    def a = (tmp ? tmp.getTime() : null)

    /** Дата окончания отчетного периода. */
    tmp = reportPeriodService.getEndDate(formData.reportPeriodId)
    def b = (tmp ? tmp.getTime() : null)

    if (!getRows(data).isEmpty()) {
        def i = 1
        // суммы строки общих итогов
        def totalSums = [:]
        // столбцы для которых надо вычислять итого и итого по коду классификации дохода. Графа 10, 12
        def totalColumns = ['taxAccountingRuble', 'ruble']
        // признак наличия итоговых строк
        def hasTotal = false
        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        for (def row : getRows(data)) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 4. Обязательность заполнения полей (графа 1..12)
            // список проверяемых столбцов (графа 1..12)
            def requiredColumns = ['rowNumber', 'balance', 'date', 'code', 'docNumber',
                    'docDate', 'currencyCode', 'rateOfTheBankOfRussia', 'taxAccountingCurrency',
                    'taxAccountingRuble', 'accountingCurrency', 'ruble']
            if (!checkRequiredColumns(row, requiredColumns, useLog)) {
                return false
            }

            // 1. Проверка даты совершения операции и границ отчётного периода (графа 3)
            if (row.date < a || b < row.date) {
                logger.error('Дата совершения операции вне границ отчётного периода!')
                return false
            }

            // 2. Проверка на нулевые значения (графа 9, 10, 11, 12)
            if (row.taxAccountingCurrency == 0 && row.taxAccountingRuble == 0 &&
                    row.accountingCurrency == 0 && row.ruble == 0) {
                logger.error('Все суммы по операции нулевые!')
                return false
            }

            // 5. Проверка на уникальность поля «№ пп» (графа 1)
            if (i != row.rowNumber) {
                logger.error('Нарушена уникальность номера по порядку!')
                return false
            }
            i += 1

            // 6. Арифметическая проверка графы 10
            tmp = round(row.rateOfTheBankOfRussia * row.taxAccountingCurrency , 2)
            if (row.taxAccountingRuble != tmp) {
                logger.warn('Неверное значение в поле «Сумма дохода, начисленная в налоговом учёте. Рубли»!')
            }

            // 7. Арифметическая проверка графы 12
            tmp = round(row.accountingCurrency * row.taxAccountingCurrency , 2)
            if (row.ruble != tmp) {
                logger.warn('Неверное значение поле «Сумма дохода, отражённая в бухгалтерском учёте. Рубли»!')
            }

            // 8. Проверка итоговых значений по кодам классификации дохода - нахождение кодов классификации
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
            def totalRow = formData.getDataRow('total')

            // 3. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода (графа 10, 12)
            if (totalRow.ruble > 0 && totalRow.taxAccountingRuble >= totalRow.ruble) {
                logger.warn('Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!')
            }

            // 8. Проверка итоговых значений по кодам классификации дохода
            for (def codeName : totalGroupsName) {
                def row = formData.getDataRow('total' + codeName)
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
    // 1. Проверка балансового счёта для кода классификации дохода - Проверка актуальности «графы 2»
    if (false) {
        logger.warn('Балансовый счёт в справочнике отсутствует!')
    }

    // 2. Проверка балансового счёта для кода классификации дохода - Проверка актуальности «графы 4»
    if (false) {
        logger.warn('Балансовый счёт в справочнике отсутствует!')
    }

    // 3. Проверка кода классификации дохода для данного РНУ - Проверка актуальности «графы 4» на дату по «графе 3»
    if (false) {
        logger.error('Операция в РНУ не учитывается!')
        return false

    }
    // 4. Проверка кода валюты - Проверка актуальности «графы 7»	1
    if (false) {
        logger.error('Код валюты в справочнике отсутствует!')
        return false
    }

    // 5. Проверка курса валюты со справочным - Проверка актуальности «графы 8» на дату по «графе 3»
    if (false) {
        logger.warn('Неверный курс валюты!')
    }
    return true
}

/**
 * Проверка наличия и статуса консолидированной формы при осуществлении перевода формы в статус "Подготовлена"/"Принята".
 */
void checkOnPrepareOrAcceptance(def value) {
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() { department ->
        if (department.formTypeId == formData.getFormType().getId()) {
            def form = formDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)
            // если форма существует и статус "принята"
            if (form != null && form.getState() == WorkflowState.ACCEPTED) {
                logger.error("$value первичной налоговой формы невозможно, т.к. уже подготовлена консолидированная налоговая форма.")
            }
        }
    }
}

/**
 * Консолидация.
 */
void consolidation() {
    // удалить все строки и собрать из источников их строки
    def data = getData(formData)
    getRows(data).clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                source.getDataRows().each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        insert(data, row)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
}

/**
 * Проверки при переходе "Отменить принятие".
 */
void checkOnCancelAcceptance() {
    List<DepartmentFormType> departments = departmentFormTypeService.getFormDestinations(formData.getDepartmentId(),
            formData.getFormType().getId(), formData.getKind());
    DepartmentFormType department = departments.getAt(0);
    if (department != null) {
        FormData form = formDataService.find(department.formTypeId, department.kind, department.departmentId, formData.reportPeriodId)

        if (form != null && (form.getState() == WorkflowState.PREPARED || form.getState() == WorkflowState.ACCEPTED)) {
            logger.error("Нельзя отменить принятие налоговой формы, так как уже принята вышестоящая налоговая форма")
        }
    }
}

/**
 * Принять.
 */
void acceptance() {
    if (!logicalCheck(true) || !checkNSI()) {
        return
    }
    departmentFormTypeService.getFormDestinations(formDataDepartment.id,
            formData.getFormType().getId(), formData.getKind()).each() {
        formDataCompositionService.compose(formData, it.departmentId, it.formTypeId, it.kind, logger)
    }
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
    // отчётный период
    def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

    //проверка периода ввода остатков
    if (reportPeriod != null && reportPeriod.isBalancePeriod()) {
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
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = formData.createDataRow()
    newRow.setAlias('total' + alias)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    newRow.code = 'Итого по коду'
    setTotalStyle(newRow)
    return newRow
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'balance', 'date', 'code', 'docNumber', 'docDate',
            'currencyCode', 'rateOfTheBankOfRussia', 'taxAccountingCurrency',
            'taxAccountingRuble', 'accountingCurrency', 'ruble'].each {
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
    def sum = 0
    def data = getData(formData)
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
def checkRequiredColumns(def row, def columns, def useLog) {
    def colNames = []

    // если "графа 7" == 'RUR', то "графа 8" = 1 и ее проверять не надо
    def needColumn8 = (row.currencyCode != null && row.currencyCode == 'RUR')
    if (!needColumn8) {
        columns -= 'rateOfTheBankOfRussia'
    }

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = row.getCell(it).getColumn().getName().replace('%', '%%')
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!useLog) {
            return false
        }
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = getIndex(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return false
    }
    return true
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
 * Получить строку по алиасу.
 *
 * @param data данные нф (helper)
 * @param alias алиас
 */
def getRowByAlias(def data, def alias) {
    return data.getDataRow(getRows(data), alias)
}

/**
 * Удалить строку из нф
 *
 * @param data данные нф (helper)
 * @param row строка для удаления
 */
void deleteRow(def data, def row) {
    data.delete(row)
}