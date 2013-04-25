/**
 * Скрипт для РНУ-49 (rnu49.groovy).
 * Форма "(РНУ-49) Регистр налогового учёта «ведомость определения результатов от реализации (выбытия) имущества»".
 *
 * TODO:
 *      - нет уловии в проверках соответствия НСИ (потому что нету справочников)
 *      - уникальность инвентарного номера
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK :
        logicalCheck()
        checkNSI()
        break
    case FormDataEvent.CALCULATE :
        calc()
        logicalCheck()
        checkNSI()
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

// графа 1  - rowNumber
// графа 2  - firstRecordNumber
// графа 3  - operationDate
// графа 4  - reasonNumber
// графа 5  - reasonDate
// графа 6  - invNumber
// графа 7  - name
// графа 8  - price
// графа 9  - amort
// графа 10 - expensesOnSale
// графа 11 - sum
// графа 12 - sumInFact
// графа 13 - costProperty
// графа 14 - marketPrice
// графа 15 - sumIncProfit
// графа 16 - profit
// графа 17 - loss
// графа 18 - usefullLifeEnd
// графа 19 - monthsLoss
// графа 20 - expensesSum
// графа 21 - saledPropertyCode
// графа 22 - saleCode
// графа 23 - propertyType

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())

    // графа 2..14, 18, 19, 21..23
    ['firstRecordNumber', 'operationDate', 'reasonNumber', 'reasonDate',
            'invNumber', 'name', 'price', 'amort', 'expensesOnSale',
            'sum', 'sumInFact', 'costProperty', 'marketPrice', 'usefullLifeEnd',
            'monthsLoss', 'saledPropertyCode', 'saleCode', 'propertyType'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).styleAlias = 'Редактируемая'
    }

    def index = formData.dataRows.indexOf(currentDataRow)
    if (index == -1) {
        index = 0
    }
    if (index + 1 == formData.dataRows.size()) {
        formData.dataRows.add(index, newRow)
    } else {
        formData.dataRows.add(index + 1, newRow)
    }

    setOrder()
}

/**
 * Удалить строку.
 */
def deleteRow() {
    if (isFixedRow(currentDataRow)) {
        return
    }

    formData.dataRows.remove(currentDataRow)
    setOrder()
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
        if (!isFixedRow(row)) {
            def colNames = []
            // Список проверяемых столбцов (графа 2..14, 18, 19, 21..23)
            def columns = ['firstRecordNumber', 'operationDate', 'reasonNumber', 'reasonDate',
                    'invNumber', 'name', 'price', 'amort', 'expensesOnSale',
                    'sum', 'sumInFact', 'costProperty', 'marketPrice', 'usefullLifeEnd',
                    'monthsLoss', 'saledPropertyCode', 'saleCode', 'propertyType']

            columns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                hasError = true
                def index = row.rowNumber
                def errorMsg = colNames.join(', ')
                if (index != null) {
                    logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = row.getOrder()
                    logger.error("В $index строке не заполнены колонки : $errorMsg.")
                }
            }
        }
    }
    if (hasError) {
        return
    }

    /*
    * Расчеты.
    */

    // графа 1, 15..17, 20
    formData.dataRows.eachWithIndex { row, i ->
        if (!isFixedRow(row)) {
            // графа 1
            row.rowNumber = (String) i + 1

            // графа 15
            if (row.sum - row.marketPrice * 0.8 > 0) {
                row.sumIncProfit = 0
            } else {
                row.sumIncProfit = row.marketPrice * 0.8 - row.sum
            }

            // графа 16
            row.profit = row.sum - (row.price - row.amort) - row.expensesOnSale + row.sumIncProfit

            // графа 17
            row.loss = row.profit

            // графа 20
            if (row.monthsLoss != 0) {
                row.expensesSum = round(row.loss / row.monthsLoss, 2)
            } else {
                row.expensesSum = 0
                def column = row.getCell('monthsLoss').getColumn().getName()
                logger.error("Деление на ноль. Возможно неправильное значение в графе \"$column\".")
            }
        }
    }

    // подразделы
    ['A', 'B', 'V', 'G', 'D', 'E'].each { section ->
        firstRow = formData.getDataRow(section)
        lastRow = formData.getDataRow('total' + section)
        // графы для которых считать итого (графа 9..13, 15..20)
        ['amort', 'expensesOnSale', 'sum', 'sumInFact', 'costProperty', 'sumIncProfit',
                'profit', 'loss', /*'usefullLifeEnd',*/ 'monthsLoss', 'expensesSum'].each {
            lastRow.getCell(it).setValue(getSum(it, firstRow, lastRow))
        }
    }
}

/**
 * Логические проверки.
 */
void logicalCheck() {
    if (!formData.dataRows.isEmpty()) {
        for (def row : formData.dataRows) {
            if (isFixedRow(row)) {
                continue
            }

            // 1. Обязательность заполнения поля графы (графа 1..22)
            def colNames = []
            // Список проверяемых столбцов (графа 1..22)
            def columns = ['rowNumber', 'firstRecordNumber', 'operationDate', 'reasonNumber',
                    'reasonDate', 'invNumber', 'name', 'price', 'amort', 'expensesOnSale',
                    'sum', 'sumInFact', 'costProperty', 'marketPrice', 'sumIncProfit',
                    'profit', 'loss', 'usefullLifeEnd', 'monthsLoss', 'expensesSum',
                    'saledPropertyCode', 'saleCode']

            columns.each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                hasError = true
                def index = row.rowNumber
                def errorMsg = colNames.join(', ')
                if (index != null) {
                    logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = row.getOrder()
                    logger.error("В $index строке не заполнены колонки : $errorMsg.")
                }
                break
            }

            // 2. Проверка на уникальность поля «инвентарный номер» (графа 6)
            // TODO (Ramil Timerbaev) Как должна производиться эта проверка?
            if (false) {
                logger.warn('Инвентарный номер не уникальный!')
            }

            // 3. Проверка на нулевые значения (графа 8, 13, 15, 17, 20)
            if (row.price == 0 &&
                    row.costProperty == 0 &&
                    row.sumIncProfit == 0 &&
                    row.loss == 0 &&
                    row.expensesSum == 0) {
                logger.error('Все суммы по операции нулевые!')
                break
            }
            // 4. Проверка формата номера первой записи	Формат графы 2: ГГ-НННН
            if (!row.firstRecordNumber.matches('\\w{2}-\\w{6}')) {
                logger.error('Неправильно указан номер предыдущей записи!')
                break
            }

            // 5. Арифметическая проверка графы 15
            def hasError = false
            if (row.sum - row.marketPrice * 0.8 > 0) {
                hasError = (row.sumIncProfit != 0)
            } else {
                hasError = (row.sumIncProfit != (row.marketPrice * 0.8 - row.sum))
            }
            if (hasError) {
                logger.error('Неверное значение графы «Сумма к увеличению прибыли (уменьшению убытка)»!')
                break
            }

            // 6. Арифметическая проверка графы 16
            if (row.profit != (row.sum - (row.price - row.amort) - row.expensesOnSale + row.sumIncProfit)) {
                logger.error('Неверное значение графы «Прибыль от реализации»!')
                break
            }

            // 7. Арифметическая проверка графы 17
            if (row.loss != row.profit) {
                logger.error('Неверное значение графы «Убыток от реализации»!')
                break
            }

            // 8. Арифметическая проверка графы 20
            if (row.monthsLoss != 0 && row.expensesSum != round(row.loss / row.monthsLoss, 2)) {
                logger.error('Неверное значение графы «Сумма расходов, приходящаяся на каждый месяц»!')
                break
            }

            // 9. Проверка итоговых значений формы
            hasError = false
            // подразделы
            ['A', 'B', 'V', 'G', 'D', 'E'].each { section ->
                if (!hasError) {
                    firstRow = formData.getDataRow(section)
                    lastRow = formData.getDataRow('total' + section)
                    // графы для которых считать итого (графа 9..13, 15..20)
                    ['amort', 'expensesOnSale', 'sum', 'sumInFact', 'costProperty', 'sumIncProfit',
                            'profit', 'loss', /*'usefullLifeEnd',*/ 'monthsLoss', 'expensesSum'].each {
                        if (!hasError &&
                                lastRow.getCell(it).getValue().equals(getSum(it, firstRow, lastRow))) {
                            hasError = true
                        }
                    }
                }
            }
            if (hasError) {
                logger.error('Итоговые значения рассчитаны неверно!')
                break
            }
        }
    }
}

/**
 * Проверки соответствия НСИ.
 */
void checkNSI() {
    if (!formData.dataRows.isEmpty()) {
        for (def row : formData.dataRows) {
            if (isFixedRow(row)) {
                continue
            }

            // 1. Проверка шифра при реализации амортизируемого имущества
            // Графа 21 (группа «А») = 1 или 2, и графа 22 = 1
            if (isSection('A', row) &&
                    ((row.saledPropertyCode != 1 && row.saledPropertyCode != 2) || row.saleCode != 1)) {
                logger.error('Для реализованного амортизируемого имущества (группа «А») указан неверный шифр!')
            }

            // 2. Проверка шифра при реализации прочего имущества
            // Графа 21 (группа «Б») = 3 или 4, и графа 22 = 1
            if (isSection('B', row) &&
                    ((row.saledPropertyCode != 3 && row.saledPropertyCode != 4) || row.saleCode != 1)) {
                logger.error('Для реализованного прочего имущества (группа «Б») указан неверный шифр!')
            }

            // 3. Проверка шифра при списании (ликвидации) амортизируемого имущества
            // Графа 21 (группа «В») = 1 или 2, и графа 22 = 2
            if (isSection('V', row) &&
                    ((row.saledPropertyCode != 1 && row.saledPropertyCode != 2) || row.saleCode != 2)) {
                logger.error('Для списанного (ликвидированного) амортизируемого имущества (группа «В») указан неверный шифр!')
            }

            // 4. Проверка шифра при реализации имущественных прав (кроме прав требования, долей паёв)
            // Графа 21 (группа «Г») = 5, и графа 22 = 1
            if (isSection('G', row) &&
                    (row.saledPropertyCode != 5 || row.saleCode != 1)) {
                logger.error('Для реализованных имущественных прав (кроме прав требования, долей паёв) (группа «Г») указан неверный шифр!')
            }

            // 5. Проверка шифра при реализации прав на земельные участки
            // Графа 21 (группа «Д») = 6, и графа 22 = 1
            if (isSection('D', row) &&
                    (row.saledPropertyCode != 6 || row.saleCode != 1)) {
                logger.error('Для реализованных прав на земельные участки (группа «Д») указан неверный шифр!')
            }

            // 6. Проверка шифра при реализации долей, паёв
            // TODO (Ramil Timerbaev) Откуда взялось значение – 7. Графа 21 может принимать значения 1, 2, 3, 4, 5, 6
            // Графа 21 (группа «Е») = 7 , и графа 22 = 1
            if (isSection('E', row) &&
                    (row.saledPropertyCode != 7 || row.saleCode != 1)) {
                logger.error('Для реализованных имущественных прав (кроме прав требования, долей паёв) (группа «Е») указан неверный шифр!')
            }

            // 7. Проверка актуальности поля «Шифр вида реализованного (выбывшего) имущества»
            // Проверка соответствия «графы 21» справочным данным справочника «Шифр вида реализованного (выбывшего) имущества»
            if (false) {
                logger.warn('Шифр вида реализованного (выбывшего) имущества в справочнике отсутствует!')
            }

            // 8. Проверка актуальности поля «Шифр вида реализации (выбытия)»	Проверка соответствия «графы 22» справочным данным справочника «Шифр вида реализации (выбытия)»
            if (false) {
                logger.warn('Шифр вида реализации (выбытия) в справочнике отсутствует!')
            }

            // 9. Проверка актуальности поля «Тип имущества»
            // Проверка соответствия «графы 23» справочным данным справочника «Тип имущества»
            if (false) {
                logger.warn('Тип имущества в справочнике не найден!')
            }
        }
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
 * Поправить значания order.
 */
void setOrder() {
    formData.dataRows.eachWithIndex { row, index ->
        row.setOrder(index + 1)
    }
}

/**
 * Получить сумму столбца.
 */
def getSum(def columnAlias, def rowStart, def rowEnd) {
    def from = rowStart.getOrder()
    def to = rowEnd.getOrder() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
}

def isSection(def section, def row) {
    def sectionRow = formData.getDataRow(section)
    def totalRow = formData.getDataRow('total' + section)
    return row.getOrder() > sectionRow.getOrder() && row.getOrder() < totalRow.getOrder()
}