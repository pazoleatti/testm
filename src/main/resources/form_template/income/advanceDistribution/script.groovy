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
    // TODO (Aydar Kadyrgulov)
    def dataRowsHelper = formDataService.getDataRowHelper(formData)
    DataRow<Cell> dataRow = formData.createDataRow()
    if (currentDataRow == null) currentDataRow = 0

    dataRows = dataRowsHelper.getAllCached()
    dataRows.add(currentDataRow, dataRow)
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
            return a.kpp <=> b.kpp
        }
        if (a.regionBank == b.regionBank) {
            return a.regionBankDivision <=> b.regionBankDivision
        }
        return a.regionBank <=> b.regionBank
    }

    // TODO
    // расчет графы 1..4, 8..21
    dataRowsHelper.getAllCached().eachWithIndex { row, i ->
        // графа 1
        row.number = i + 1

        // графа 2
        row.regionBank =0

        // графа 4
        row.kpp =0

        // графа 8
        row.calcFlag =0

        // графа 9
        row.obligationPayTax =0

        // графа 10
        row.baseTaxOf =0

        // графа 11
        row.baseTaxOfRub =0

        // графа 12
        row.subjectTaxStavka =0

        // графа 13
        row.taxSum =0

        // графа 14
        row.taxSumOutside =0

        // графа 15
        row.taxSumToPay =0

        // графа 16
        row.taxSumToReduction =0

        // графа 17
        row.everyMontherPaymentAfterPeriod =0

        // графа 18
        row.everyMonthForKvartalNextPeriod =0

        // графа 19
        row.everyMonthForSecondKvartalNextPeriod =0

        // графа 20
        row.everyMonthForThirdKvartalNextPeriod =0

        // графа 21
        row.everyMonthForFourthKvartalNextPeriod =0
    }

    // добавить строку ЦА (скорректрированный) (графа 1..21)
    def caRow = formData.createDataRow()
    dataRowsHelper.getAllCached().add(caRow)
    caRow.setAlias('ca')
    caRow.regionBank = 'Итого'  // TODO (Aydar Kadyrgulov)
    setTotalStyle(caRow)
    // TODO доделать
    // расчет графы 1..21
    dataRowsHelper.getAllCached().eachWithIndex { row, i ->
        // графа 1
        row.number = i + 1

        // графа 2
        row.regionBank =0

        // графа 3
        row.regionBankDivision =0

        // графа 4
        row.kpp =0

        // графа 5
        row.propertyPrice =0

        // графа 6
        row.workersCount =0

        // графа 7
        row.subjectTaxCredit =0

        // графа 8
        row.calcFlag =0

        // графа 9
        row.obligationPayTax =0

        // графа 10
        row.baseTaxOf =0

        // графа 11
        row.baseTaxOfRub =0

        // графа 12
        row.subjectTaxStavka = 0

        // графа 13
        row.taxSum =0

        // графа 14
        row.taxSumOutside =0

        // графа 15
        row.taxSumToPay =0

        // графа 16
        row.taxSumToReduction =0

        // графа 17
        row.everyMontherPaymentAfterPeriod =0

        // графа 18
        row.everyMonthForKvartalNextPeriod =0

        // графа 19
        row.everyMonthForSecondKvartalNextPeriod =0

        // графа 20
        row.everyMonthForThirdKvartalNextPeriod =0

        // графа 21
        row.everyMonthForFourthKvartalNextPeriod = 0
    }


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
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}



/**
 * Получить номер строки в таблице.
 */
def getIndex(def row) {
    def dataRowsHelper = formDataService.getDataRowHelper(formData)
    dataRowsHelper.getAllCached().indexOf(row)
    dataRowsHelper.save(dataRowsHelper.getAllCached());
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
    dataRowsHelper.save(dataRowsHelper.getAllCached());
    return summ(formData, new ColumnRange(columnAlias, from, to))
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