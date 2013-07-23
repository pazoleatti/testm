/**
 *
 * Расчёт распределения авансовых платежей и налога на прибыль по обособленным подразделениям организации
 *
 * @author akadyrgulov
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
        // 6.2.5.7. Пересчитать итоги
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
// после принятия из подготовлена
    case FormDataEvent.AFTER_MOVE_PREPARED_TO_ACCEPTED :
        logicalCheck(true)
        checkNSI()
        // на стороне сервера будет выполнен compose
        break
// обобщить
    case FormDataEvent.COMPOSE :
        consolidation()
        calc()
        logicalCheck(false)
        checkNSI()
        break
}

// графа 1  - number
// графа 2  - regionBank
// графа 3  - regionBankDivision
// графа 4  - kpp
// графа 5  - propertyPrice
// графа 6  - workersCount
// графа 7  - subjectTaxCredit
// графа 8  - calcFlag
// графа 9  - obligationPayTax
// графа 10 - baseTaxOf
// графа 11 - baseTaxOfRub
// графа 12 - subjectTaxStavka
// графа 13 - taxSum
// графа 14 - taxSumOutside
// графа 15 - taxSumToPay
// графа 16 - taxSumToReduction
// графа 17 - everyMontherPaymentAfterPeriod
// графа 18 - everyMonthForKvartalNextPeriod
// графа 19 - everyMonthForSecondKvartalNextPeriod
// графа 20 - everyMonthForThirdKvartalNextPeriod
// графа 21 - everyMonthForFourthKvartalNextPeriod

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def dataRowsHelper = formDataService.getDataRowHelper(formData)
    DataRow<Cell> dataRow = formData.createDataRow()

    logger.info('currentDataRow != null ' + currentDataRow)
    logger.info('dataRowsHelper.getAllCached().size() ' + dataRowsHelper.getAllCached().size())
    int index = currentDataRow != null ? currentDataRow.getIndex() : (dataRowsHelper.getAllCached().size() == 0 ? 1 : dataRowsHelper.getAllCached().size())
    logger.info('getIndex(currentDataRow) : ' + currentDataRow.getIndex())
    logger.info('index : ' + index)

    dataRowsHelper.insert(dataRow, index);
    dataRows = dataRowsHelper.getAllCached()
    dataRows.add(dataRow)
    // графа 3, 5..7 редактируемые
    ['regionBankDivision', 'propertyPrice', 'workersCount', 'subjectTaxCredit'].each {
        dataRow.getCell(it).editable = true
        dataRow.getCell(it).setStyleAlias('Редактируемая')
    }
    dataRowsHelper.save(dataRows)
}

/**
 * Удалить строку.
 */
def deleteRow() {
    def dataRowsHelper = formDataService.getDataRowHelper(formData)
    dataRowsHelper.delete(currentDataRow)
    dataRows = dataRowsHelper.getAllCached()
    dataRowsHelper.save(dataRows)
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    /*
     * Проверка объязательных полей.
     */

    // список проверяемых столбцов (графа 3, 5..7)

    def requiredColumns = ['regionBankDivision', 'propertyPrice', 'workersCount', 'subjectTaxCredit']

    def dataRowsHelper = formDataService.getDataRowHelper(formData)

    for (def row : dataRowsHelper.getAllCached()) {
        if (!isFixedRow(row) && !checkRequiredColumns(row, requiredColumns, true)) {
            return
        }
    }

    /*
     * Расчеты
     */

    // удалить фиксированные строки
    def delRow = []
    dataRowsHelper.getAllCached().each { row ->
        if (isFixedRow(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        dataRowsHelper.getAllCached().remove(getIndex(row))
    }
    if (dataRowsHelper.getAllCached().isEmpty()) {
        return
    }

    // отсортировать/группировать
    dataRowsHelper.getAllCached().sort { a, b ->
        if (a.regionBank == b.regionBank && a.regionBankDivision == b.regionBankDivision) {
            return b.kpp <=> a.kpp
        }
        if (a.regionBank == b.regionBank) {
            return b.regionBankDivision <=> a.regionBankDivision
        }
        return b.regionBank <=> a.regionBank
    }

    // TODO
    // расчет графы 1..4, 8..21
    def propertyPriceSumm = getSumAll("propertyPrice")
    def workersCountSumm = getSumAll("workersCount")

    logger.info('propertyPriceSumm = ' + propertyPriceSumm);
    logger.info('workersCountSumm = ' + workersCountSumm);
    dataRowsHelper.getAllCached().eachWithIndex { row, i ->

        // TODO (Aydar Kadyrgulov) после привязки к справочнику брать ID выбранной записи
        def departmentParam = departmentService.getDepartmentParam(Integer.valueOf(row.regionBankDivision))

        // графа 1
        row.number = i + 1

        /*
        Заполняется автоматически на основании значения «Графы 3». regionBankDivision
        «Графа 2» = значение атрибута «Наименование подразделения» справочника «Подразделения»,
        где «Индекс территориального банка» текущего подразделения («Графа 3»)
        равен «Индексу территориального банка» среди записей справочника «Подразделения» с типом,
        соответствующему территориальному банку.
        */
        // графа 2
        row.regionBank = departmentParam.name

        /*
        Заполняется автоматически на основании значения «Графы 3». regionBankDivision
        «Графа 4» = значение атрибута «КПП» формы настроек подразделений.
        */
        // графа 4
        row.kpp = departmentParam.kpp

        /*
        Заполняется автоматически на основании значения «Графы 3». regionBankDivision
        «Графа 8» = значение атрибута «Признак расчёта» формы настроек подразделений.
        */
        // графа 8 TODO (Aydar Kadyrgulov) Признак расчёта
        row.calcFlag = 0

        /*
        Заполняется автоматически на основании значения «Графы 3».
        «Графа 9» = значение атрибута «Обязанность по уплате налога» формы настроек подразделений.
        */
        // графа 9 TODO (Aydar Kadyrgulov) Обязанность по уплате налога
        row.obligationPayTax =0

        /*
         «Графа 10» =ОКРУГЛ ((((«графа 5»  / «итого по графе 5») * 100) + ((«графа 6» / «итого по графе 6») * 100)) / 2; 8)
        */
        // графа 10
        def tmp = (((( row.propertyPrice  / propertyPriceSumm) * 100) + ((row.workersCount / workersCountSumm) * 100)) / 2)
        row.baseTaxOf = round( tmp, 8)

        /*
         «Графа 11» = ОКРУГЛ («распределяемая налоговая база за отчётный период» * «графа 10» / 100; 0)
         */
        // графа 11
        row.baseTaxOfRub =0

        /*
        Заполняется автоматически на основании значения «Графы 3».
        «Графа 12» = значение атрибута «Ставка налога (региональная часть)» формы настроек подразделений.
         */
        // графа 12
        row.subjectTaxStavka =0

        /*
        ЕСЛИ «графа 11» > 0
        ТОГДА «графа 13» = ОКРУГЛ («графа 11» * «графа 12» / 100;0)
        ИНАЧЕ «графа 13» = 0
         */
        // графа 13
        row.taxSum =0

        /*
        «Графа 14» = ОКРУГЛ («Сумма налога на прибыль, выплаченная за пределами Российской Федерации в отчётном периоде» * «графа 10» / 100;0)
         */
        // графа 14
        row.taxSumOutside =0

        /*
        ЕСЛИ «графа 13» > «графа 7» + «графа 14»
        ТОГДА «графа 15» = «графа 13» - («графа 7» + «графа 14»)
        ИНАЧЕ «графа 15» = 0
         */
        // графа 15
        row.taxSumToPay =0

        /*
        ЕСЛИ «графа 13» < «графа 7» + «графа 14»
        ТОГДА «графа 16» = («графа 7» + «графа 14») - «графа 13»
        ИНАЧЕ «графа 16» = 0
         */
        // графа 16
        row.taxSumToReduction =0


        /*
        Для первого отчётного периода
        «графа 17» = «графа 19»
        Для второго отчётного периода
        «графа 17» = «графа 20»
        Для третьего отчётного периода
        «графа 17» = «графа 21»
        Для налогового периода
        «графа 17» = 0
         */
        // графа 17
        row.everyMontherPaymentAfterPeriod =0

        /*
        Для первого отчётного периода
        «графа 18» = 0
        Для второго отчётного периода
        «графа 18» = 0
        Для третьего отчётного периода
        «графа 18» = «графа 17»
        Для налогового периода
        «графа 18» = 0
         */
        // графа 18
        row.everyMonthForKvartalNextPeriod =0

        /*
        Для первого отчётного периода
        «графа 19» = «графа 13»
        Для второго отчётного периода
        «графа 19» = 0
        Для третьего отчётного периода
        «графа 19» = 0
        Для налогового периода
        «графа 19» = 0
         */
        // графа 19
        row.everyMonthForSecondKvartalNextPeriod =0

        /*
        Для первого отчётного периода
        «графа 20» = 0
        Для второго отчётного периода
        «графа 20» = «графа 13» - «графа 19»
        Для третьего отчётного периода
        «графа 20» = 0
        Для налогового периода
        «графа 20» = 0
         */
        // графа 20
        row.everyMonthForThirdKvartalNextPeriod =0

        /*
        Для первого отчётного периода
        «графа 21» = 0
        Для второго отчётного периода
        «графа 21» = 0
        Для третьего отчётного периода
        «графа 21» = «графа 13» - «графа 20»
        Для налогового периода
        «графа 21» = 0
         */
        // графа 21
        row.everyMonthForFourthKvartalNextPeriod =0
    }

    // добавить строку ЦА (скорректрированный) (графа 1..21)
    def caRow = formData.createDataRow()
    dataRowsHelper.getAllCached().add(caRow)
    caRow.setAlias('ca')
    caRow.regionBank = 'Центральный аппарат (скорректированный)'  // TODO (Aydar Kadyrgulov)
    setTotalStyle(caRow)
    dataRowsHelper.save(dataRowsHelper.getAllCached());
    // TODO доделать
    // расчет графы 1..21
    /*dataRowsHelper.getAllCached().eachWithIndex { row, i ->
        def departmentParam = departmentService.getDepartmentParam(Integer.valueOf(row.regionBankDivision))

        // графа 1
        row.number = i + 1

        // графа 2
        row.regionBank = departmentParam.name

        // графа 3
        row.regionBankDivision =null

        // графа 4
        row.kpp = departmentParam.kpp

        // графа 5
        row.propertyPrice =null

        // графа 6
        row.workersCount =null

        // графа 7
        row.subjectTaxCredit =null

        // графа 8
        row.calcFlag =null

        // графа 9
        row.obligationPayTax =null

        // графа 10
        row.baseTaxOf =null

        // графа 11
        row.baseTaxOfRub =null

        // графа 12
        row.subjectTaxStavka = null

        // графа 13
        row.taxSum =null

        // графа 14
        row.taxSumOutside =null

        // графа 15
        row.taxSumToPay =null

        // графа 16
        row.taxSumToReduction =null

        // графа 17
        row.everyMontherPaymentAfterPeriod =null

        // графа 18
        row.everyMonthForKvartalNextPeriod =null

        // графа 19
        row.everyMonthForSecondKvartalNextPeriod =null

        // графа 20
        row.everyMonthForThirdKvartalNextPeriod =null

        // графа 21
        row.everyMonthForFourthKvartalNextPeriod = null
    }*/


    // добавить итого (графа 5..7, 10, 11, 13..21)
    def totalRow = formData.createDataRow()
    dataRowsHelper.getAllCached().add(totalRow)
    totalRow.setAlias('total')
    totalRow.regionBank = 'Итого' // TODO (Aydar Kadyrgulov)
    setTotalStyle(totalRow)
    ['propertyPrice', 'workersCount', 'subjectTaxCredit', 'baseTaxOf',
            'baseTaxOfRub', 'taxSum', 'taxSumOutside', 'taxSumToPay',
            'taxSumToReduction', 'everyMontherPaymentAfterPeriod',
            'everyMonthForKvartalNextPeriod', 'everyMonthForSecondKvartalNextPeriod',
            'everyMonthForThirdKvartalNextPeriod',
            'everyMonthForFourthKvartalNextPeriod'].each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }
    dataRowsHelper.save(dataRowsHelper.getAllCached());
}

/**
 * Логические проверки.
 *
 * @param useLog нужно ли записывать в лог сообщения о незаполненности обязательных полей
 */
def logicalCheck(def useLog) {
    def dataRowsHelper = formDataService.getDataRowHelper(formData)
    if (!dataRowsHelper.getAllCached().isEmpty()) {
        def i = 1

        // список проверяемых столбцов (графа 1..21)
        def requiredColumns = ['number', 'regionBank', 'regionBankDivision', 'kpp',
                'propertyPrice', 'workersCount', 'subjectTaxCredit', 'calcFlag',
                'obligationPayTax', 'baseTaxOf', 'baseTaxOfRub', 'subjectTaxStavka',
                'taxSum', 'taxSumOutside', 'taxSumToPay', 'taxSumToReduction',
                'everyMontherPaymentAfterPeriod', 'everyMonthForKvartalNextPeriod',
                'everyMonthForSecondKvartalNextPeriod', 'everyMonthForThirdKvartalNextPeriod',
                'everyMonthForFourthKvartalNextPeriod']

        for (def row : dataRowsHelper.getAllCached()) {
            if (isTotal(row)) {
                continue
            }

            // 1. Обязательность заполнения поля графы 1..21
            if (!checkRequiredColumns(row, requiredColumns, useLog)) {
                return
            }
        }
    }
    dataRowsHelper.save(dataRowsHelper.getAllCached());
    return true
}

/**
 * Проверки соответствия НСИ.
 */
def checkNSI() {
    def dataRowsHelper = formDataService.getDataRowHelper(formData)
    if (!dataRowsHelper.getAllCached().isEmpty()) {
        for (def row : dataRowsHelper.getAllCached()) {
            if (isFixedRow(row)) {
                continue
            }

            // 1. Проверка совпадения наименования подразделения со справочным
            if (false) {
                logger.error('Неверное наименование подразделения!')
                return false
            }
        }
    }
    dataRowsHelper.save(dataRowsHelper.getAllCached());
    return true
}

/**
 * Консолидация.
 */
void consolidation() {
    // TODO

    def dataRowsHelper = formDataService.getDataRowHelper(formData)
    // удалить все строки и собрать из источников их строки
    dataRowsHelper.getAllCached().clear()

    departmentFormTypeService.getFormSources(formDataDepartment.id, formData.getFormType().getId(), formData.getKind()).each {
        if (it.formTypeId == formData.getFormType().getId()) {
            def source = formDataService.find(it.formTypeId, it.kind, it.departmentId, formData.reportPeriodId)
            if (source != null && source.state == WorkflowState.ACCEPTED) {
                source.getDataRows().each { row->
                    if (row.getAlias() == null || row.getAlias() == '') {
                        dataRowsHelper.getAllCached().add(row)
                    }
                }
            }
        }
    }
    logger.info('Формирование консолидированной формы прошло успешно.')
    dataRowsHelper.save(dataRowsHelper.getAllCached());
}

/**
 * Проверка при создании формы.
 */
void checkCreation() {
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
 * Проверка является ли строка фиксированной.
 */
def isFixedRow(def row) {
    return row != null && row.getAlias() != null
}

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == '' || value == 0
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['number', 'regionBank', 'regionBankDivision', 'kpp', 'propertyPrice',
            'workersCount', 'subjectTaxCredit', 'calcFlag', 'obligationPayTax',
            'baseTaxOf', 'baseTaxOfRub', 'subjectTaxStavka', 'taxSum',
            'taxSumOutside', 'taxSumToPay', 'taxSumToReduction',
            'everyMontherPaymentAfterPeriod', 'everyMonthForKvartalNextPeriod',
            'everyMonthForSecondKvartalNextPeriod', 'everyMonthForThirdKvartalNextPeriod',
            'everyMonthForFourthKvartalNextPeriod'].each {
        row.getCell(it).setStyleAlias('Итоговая')
    }
}



/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    def dataRowsHelper = formDataService.getDataRowHelper(formData)
    dataRowsHelper.getAllCached().indexOf(row)
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias) {
    def dataRowsHelper = formDataService.getDataRowHelper(formData)
    def from = 0
    def to = dataRowsHelper.getAllCached().size() - 2 - 1 // добавлен -1 что б исключить скорректированную строку
    if (from > to) {
        return 0
    }
    return summ(formData, dataRowsHelper.getAllCached(), new ColumnRange(columnAlias, from, to))
}

/**
 * Получить сумму столбца.
 */
def getSumAll(def columnAlias) {
    def dataRowsHelper = formDataService.getDataRowHelper(formData)
    def from = 0
    def to = dataRowsHelper.getAllCached().size() - 1
    if (from > to) {
        return 0
    }
    return summ(formData, dataRowsHelper.getAllCached(), new ColumnRange(columnAlias, from, to))
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

    columns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            def name = getColumnName(row, it)
            colNames.add('"' + name + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!useLog) {
            return false
        }
        def index = getIndex(row) + 1
        def errorMsg = colNames.join(', ')
        if (!isEmpty(index)) {
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