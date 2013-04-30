/**
 * Скрипт для РНУ-31 (rnu31.groovy).
 * Форма "(РНУ-31) Регистр налогового учёта процентного дохода по купонным облигациям".
 *
 * TODO:
 *      - нет уcловии в проверках соответствия НСИ (потому что нету справочников)
 *		- для проверки 1 нет условия (не ясно как получать предыдущий отчет)
 *		- как определить первый ли это отчет?
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK :
        logicalCheck(true)
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck(false)
        break
    case FormDataEvent.ADD_ROW :
        // addNewRow()
        break
}

// графа 1  - number
// графа 2  - securitiesType
// графа 3  - ofz
// графа 4  - municipalBonds
// графа 5  - governmentBonds
// графа 6  - mortgageBonds
// графа 7  - municipalBondsBefore
// графа 8  - rtgageBondsBefore
// графа 9  - ovgvz
// графа 10 - eurobondsRF
// графа 11 - itherEurobonds
// графа 12 - corporateBonds

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.appendDataRow(currentDataRow, null)

    // графа 3..12
    ['ofz', 'municipalBonds', 'governmentBonds	', 'mortgageBonds',
            'municipalBondsBefore', 'rtgageBondsBefore', 'ovgvz',
            'eurobondsRF', 'itherEurobonds', 'corporateBonds'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
}

/**
 * Расчеты. Алгоритмы заполнения полей формы.
 */
void calc() {
    /*
     * Проверка объязательных полей.
     */
    def hasError = false
    formData.dataRows.each { row ->
        if (!isTotal(row)) {
            def colNames = []
            // Список проверяемых столбцов (графа 3..12)
            def requiredColumns = ['ofz', 'municipalBonds', 'governmentBonds',
                    'mortgageBonds', 'municipalBondsBefore', 'rtgageBondsBefore',
                    'ovgvz', 'eurobondsRF', 'itherEurobonds', 'corporateBonds']

            requiredColumns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    // TODO (Ramil Timerbaev) из за % в названии заголовка может выдавать ошибки
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                hasError = true
                def index = row.rowNumber
                def errorMsg = colNames.join(', ')
                if (!isEmpty(index)) {
                    logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = formData.dataRows.indexOf(row) + 1
                    logger.error("В строке $index не заполнены колонки : $errorMsg.")
                }
            }
        }
    }
    if (hasError) {
        return
    }
}

/**
 * Логические проверки.
 *
 * @param checkRequiredColumns проверять ли обязательные графы
 */
void logicalCheck(def checkRequiredColumns) {
    // данные предыдущего отчета
    def formDataOld = getFormDataOld() // TODO (Ramil Timerbaev) как получить?

    /** Строка из предыдущего отчета. */
    def rowOld = (formDataOld != null && !formDataOld.dataRows.isEmpty() ? formDataOld.getDataRow('total') : null)

    /** Строка из текущего отчета. */
    def row = (formData != null && !formData.dataRows.isEmpty() ? formData.getDataRow('total') : null)
    if (row == null) {
        return
    }

    // 22. Обязательность заполнения полей графы 1..12
    def colNames = []
    def requiredColumns = ['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds',
            'municipalBondsBefore', 'rtgageBondsBefore', 'ovgvz', 'eurobondsRF',
            'itherEurobonds', 'corporateBonds']

    requiredColumns.each {
        if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
            colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
        }
    }
    if (!colNames.isEmpty()) {
        if (!checkRequiredColumns) {
            return
        }
        def index = row.number
        def errorMsg = colNames.join(', ')
        if (index != null) {
            logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
        } else {
            index = formData.dataRows.indexOf(row) + 1
            logger.error("В строке $index не заполнены колонки : $errorMsg.")
        }
        return
    }

    // графы для которых тип ошибки нефатальный (графа 5, 9, 10, 11)
    def warnColumns = ['governmentBonds', 'ovgvz', 'eurobondsRF', 'itherEurobonds']

    // TODO (Ramil Timerbaev) добавить проверку "начиная с отчета за февраль"
    if (!isFirstMonth()) {
        // 1. Проверка наличия предыдущего экземпляра отчета
        if (rowOld == null) {
            logger.error('Отсутствует предыдущий экземпляр отчета')
            return
        }

        // 2..11 Проверка процентного (купонного) дохода по видам валютных ценных бумаг (графы 3..12)
        for (def column : requiredColumns) {
            if (row.getCell(column).getValue() < rowOld.getCell(column).getValue()) {
                def securitiesType = row.securitiesType
                def message = "Процентный (купонный) доход по $securitiesType уменьшился!"
                if (column in warnColumns) {
                    logger.warn(message)
                } else {
                    logger.error(message)
                }
                return
            }
        }
    }

    // 12..21. Проверка на неотрицательные значения (графы 3..12)
    for (def column : requiredColumns) {
        if (row.getCell(column).getValue() < 0) {
            def columnName = row.getCell(column).getColumn().getName()
            def message = "Значения графы \"$columnName\" по строке 1 отрицательное!"
            if (column in warnColumns) {
                logger.warn(message)
            } else {
                logger.error(message)
            }
            return
        }
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Проверка пустое ли значение.
 */
def isEmpty(def value) {
    return value == null || value == ''
}

/**
 * Получить данные за предыдущий отчетный период
 */
def getFormDataOld() {
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-25 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = FormDataService.find(formData.formType.id, FormDataKind.PRIMARY, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

/**
 * Первый ли это месяц (январь)
 */
def isFirstMonth() {
    return true
}