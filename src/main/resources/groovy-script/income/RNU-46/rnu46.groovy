/**
 * Скрипт для РНУ-46 (rnu46.groovy).
 * Форма "(РНУ-46) Регистр налогового учёта «карточка по учёту основных средств и капитальных вложений в неотделимые улучшения арендованного и полученного по договору безвозмездного пользования имущества»".
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- получение значений за предыдущий месяц, за предыдущие месяцы
 *		- определение номера месяца
 *		- проверка уникальности инвентарного номера
 *
 * @author rtimerbaev
 */

import java.text.SimpleDateFormat

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
}

// графа 1  - rowNumber
// графа 2  - invNumber
// графа 3  - name
// графа 4  - cost
// графа 5  - amortGroup
// графа 6  - usefulLife
// графа 7  - monthsUsed
// графа 8  - usefulLifeWithUsed
// графа 9  - specCoef
// графа 10 - cost10perMonth
// графа 11 - cost10perTaxPeriod
// графа 12 - cost10perExploitation
// графа 13 - amortNorm
// графа 14 - amortMonth
// графа 15 - amortTaxPeriod
// графа 16 - amortExploitation
// графа 17 - exploitationStart
// графа 18 - usefullLifeEnd
// графа 19 - rentEnd

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())

    // графа 2..7, 9, 17..19
    ['invNumber', 'name', 'cost', 'amortGroup', 'usefulLife', 'monthsUsed',
            'specCoef', 'exploitationStart', 'usefullLifeEnd', 'rentEnd'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }
    def pos = (currentDataRow != null && !formData.dataRows.isEmpty() ? currentDataRow.getOrder() : formData.dataRows.size)
    formData.dataRows.add(pos, newRow)
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
        def colNames = []
        // Список проверяемых столбцов (графа 2..7, 9, 17..19)
        def columns = ['invNumber', 'name', 'cost', 'amortGroup', 'usefulLife', 'monthsUsed',
                'specCoef', 'exploitationStart', 'usefullLifeEnd', 'rentEnd']

        columns.each {
            if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                // TODO (Ramil Timerbaev) из за % в названии заголовка может выдавать ошибки
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
    if (hasError) {
        return
    }

    /*
      * Расчеты.
      */

    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def lastDay2001 = format.parse('31.12.2001')

    def tmp = null
    formData.dataRows.eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1

        // графа 8
        // TODO (Ramil Timerbaev) спросить у аналитика
        if (row.specCoef > 0) {
            row.usefulLifeWithUsed = (row.usefulLife - row.monthsUsed) / row.specCoef
        } else {
            row.usefulLifeWithUsed = 0 // TODO (Ramil Timerbaev) не описано в чтз
        }

        // графа 10
        tmp = 0
        if (row.amortGroup in ['1', '2', '8', '9', '10']) {
            tmp = row.cost * 0.1
        } else if (row.amortGroup in ('3'..'7')) {
            tmp = row.cost * 0.3
        }
        row.cost10perMonth = tmp

        // графа 12
        // TODO (Ramil Timerbaev) getFromOld() = 12 графа предыдущего месяца
        row.cost10perExploitation = getFromOld() + row.cost10perMonth

        // графа 13
        if (row.usefulLifeWithUsed != 0) {
            row.amortNorm = (1 / row.usefulLifeWithUsed) * 100
        } else {
            row.amortNorm = 0
        }

        // графа 14
        // TODO (Ramil Timerbaev) требуется пояснение относительно этой формулы
        if (row.usefullLifeEnd > lastDay2001) {
            row.amortMonth = 0
            // row.amortMonth = (row.cost (на начало месяца) - row.cost10perExploitation - row.amortExploitation (на начало месяца)) / (row.usefullLifeEnd - последнее число предыдущего месяца)
        } else {
            row.amortMonth = row.cost / 84
        }

        // графа 11, 15, 16
        if (isFirstMonth()) {
            row.cost10perTaxPeriod = row.cost10perMonth
            row.amortTaxPeriod = row.amortMonth
            row.amortExploitation = row.amortMonth
        } else {
            // TODO (Ramil Timerbaev) getFromOld() = 11 графа предыдущего месяца
            row.cost10perTaxPeriod = getFromOld() + row.cost10perMonth

            // TODO (Ramil Timerbaev) getFromOld() = 15 графа предыдущего месяца
            row.amortTaxPeriod = getFromOld() + row.amortMonth

            // TODO (Ramil Timerbaev) getFromOld() = 16 графа предыдущего месяца
            row.amortExploitation = getFromOld() + row.amortMonth
        }
    }
}

/**
 * Логические проверки.
 */
void logicalCheck() {
    SimpleDateFormat format = new SimpleDateFormat('dd.MM.yyyy')
    def lastDay2001 = format.parse('31.12.2001')

    def hasError = false
    for (def row : formData.dataRows) {
        // 1. Обязательность заполнения поля (графа 1..18)
        def colNames = []
        // Список проверяемых столбцов (графа 1..18)
        def columns = ['invNumber', 'name', 'cost', 'amortGroup', 'usefulLife', 'monthsUsed',
                'specCoef', 'exploitationStart', 'usefullLifeEnd', 'rentEnd']

        columns.each {
            if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                // TODO (Ramil Timerbaev) из за % в названии заголовка может выдавать ошибки
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

        // 2. Проверка на уникальность поля «инвентарный номер» (графа 2)
        // TODO (Ramil Timerbaev) Как должна производиться эта проверка?
        if (false) {
            logger.warn('Инвентарный номер не уникальный!')
            break
        }

        // 3. Проверка на нулевые значения (графа 9, 10, 11, 13, 14, 15)
        if (row.specCoef == 0 &&
                row.cost10perMonth == 0 &&
                row.cost10perTaxPeriod == 0 &&
                row.amortNorm &&
                row.amortMonth == 0 &&
                row.amortTaxPeriod) {
            logger.error('Все суммы по операции нулевые!')
            break
        }

        // 4. Проверка суммы расходов в виде капитальных вложений с начала года (графа 10, 9, 10 (за прошлый месяц), 9 (за предыдущие месяцы текущего года))
        if (row.cost10perMonth >= row.specCoef &&
                // TODO (Ramil Timerbaev) getFromOld() = 10 графа предыдущего месяца
                row.cost10perMonth == row.specCoef + getFromOld() &&
                // TODO (Ramil Timerbaev) getFromOld() = сумма графы 9 всех предыдущих месяцев
                row.cost10perMonth == getFromOld()) {
            logger.error('Неверная сумма расходов в виде капитальных вложений с начала года!')
            break
        }

        // 5. Проверка суммы начисленной амортизации с начала года (графа 14, 13, 14 (за прошлый месяц), 13 (за предыдущие месяцы текущего года))
        if (row.amortMonth < row.amortNorm ||
                // TODO (Ramil Timerbaev) getFromOld() = 14 графа предыдущего месяца
                row.amortMonth != row.amortNorm + getFromOld() ||
                // TODO (Ramil Timerbaev) getFromOld() = сумма графы 13 всех предыдущих месяцев
                row.amortMonth != getFromOld()) {
            logger.error('Неверная сумма начисленной амортизации с начала года!')
            break
        }

        // 6. Арифметическая проверка графы 8
        if (row.specCoef < 0 || row.usefulLifeWithUsed != (row.usefulLife - row.monthsUsed) / row.specCoef) {
            hasError = true
        } else if (row.specCoef == 0) {
            hasError = true
        } else {
            // row.usefulLifeWithUsed = 0 // TODO (Ramil Timerbaev) не описано в чтз
        }
        if (hasError) {
            logger.error('Неверное значение графы «Срок полезного использования с учётом срока эксплуатации предыдущими собственниками (арендодателями, ссудодателями) либо установленный самостоятельно, (мес.)»!')
            break
        }

        // 7. Арифметическая проверка графы 10
        hasError = false
        if (row.amortGroup in ['1', '2', '8', '9', '10'] && row.cost10perMonth != row.cost * 0.1) {
            hasError = true
        } else if (row.amortGroup in ('3'..'7') && row.cost10perMonth != row.cost * 0.3) {
            hasError = true
        }
        if (hasError) {
            logger.error('Неверное значение графы «10% (30%) от первоначальной стоимости, включаемые в расходы.За месяц»!')
            break
        }

        // 8. Арифметическая проверка графы 11
        hasError = false
        if (isFirstMonth() && row.cost10perTaxPeriod != row.cost10perMonth) {
            hasError = true

            // TODO (Ramil Timerbaev) getFromOld() = 11 графа предыдущего месяца
        } else if (!isFirstMonth() && row.cost10perTaxPeriod != getFromOld() + row.cost10perMonth) {
            hasError = true
        }
        if (hasError) {
            logger.error('Неверное значение графы «10% (30%) от первоначальной стоимости, включаемые в расходы.с начала налогового периода»!')
            break
        }

        // 9. Арифметическая проверка графы 12
        // TODO (Ramil Timerbaev) getFromOld() = 12 графа предыдущего месяца
        if (row.cost10perExploitation != getFromOld() + row.cost10perMonth) {
            logger.error('Неверное значение графы «10% (30%) от первоначальной стоимости, включаемые в расходы.с даты ввода в эксплуатацию»!')
            break
        }

        // 10. Арифметическая проверка графы 13
        hasError = false
        if (row.usefulLifeWithUsed != 0 && row.amortNorm != (1 / row.usefulLifeWithUsed) * 100) {
            hasError = true
        } else if (row.usefulLifeWithUsed == 0) {
            // hasError = true // TODO (Ramil Timerbaev) уточнить
        }
        if (hasError) {
            logger.error('Неверное значение графы «Норма амортизации (процентов в мес.)»!')
            break
        }

        // 11. Арифметическая проверка графы 14
        hasError = false
        // TODO (Ramil Timerbaev) требуется пояснение относительно этой формулы
        if (row.usefullLifeEnd > lastDay2001) {
            // row.amortMonth = (row.cost (на начало месяца) - row.cost10perExploitation - row.amortExploitation (на начало месяца)) / (row.usefullLifeEnd - последнее число предыдущего месяца)
            hasError = true
        } else if (row.usefullLifeEnd <= lastDay2001 && row.amortMonth != row.cost / 84) {
            hasError = true
        }
        if (hasError) {
            logger.error('Неверно рассчитана графа «Сумма начисленной амортизации.за месяц»!')
            break
        }

        // 12. Арифметическая проверка графы 15
        if (isFirstMonth() && row.amortTaxPeriod != row.amortMonth) {
            hasError = true

            // TODO (Ramil Timerbaev) getFromOld() = 15 графа предыдущего месяца
        } else if (!isFirstMonth() && row.amortTaxPeriod != getFromOld() + row.amortMonth) {
            hasError = true
        }
        if (hasError) {
            logger.error('Неверное значение графы «Сумма начисленной амортизации.с начала налогового периода»!')
            break
        }

        // 13. Арифметическая проверка графы 16
        if (isFirstMonth() && row.amortExploitation != row.amortMonth) {
            hasError = true

            // TODO (Ramil Timerbaev) getFromOld() = 16 графа предыдущего месяца
        } else if (!isFirstMonth() && row.amortExploitation != getFromOld() + row.amortMonth) {
            hasError = true
        }
        if (hasError) {
            logger.error('Неверное значение графы «Сумма начисленной амортизации.с даты ввода в эксплуатацию»!')
            break
        }
    }
}

/**
 * Проверки соответствия НСИ.
 */
void checkNSI() {
    // 1. Проверка амортизационной группы (графа 5)
    if (false) {
        logger.error('Амортизационная группа не существует!')
    }

    // 2. Проверка срока полезного использования (графа 6)
    if (false) {
        logger.error('Срок полезного использования указан неверно!')
    }
}

/*
 * Вспомогательные методы.
 */

/**
 * Поправить значания order.
 */
void setOrder() {
    formData.dataRows.eachWithIndex { row, index ->
        row.setOrder(index + 1)
    }
}

/**
 * Получить значение из предыдущего месяца.
 */
def getFromOld() {
    // TODO (Ramil Timerbaev)
    return 0
}

/**
 * Первый ли это месяц (январь)
 */
def isFirstMonth() {
    return false
}