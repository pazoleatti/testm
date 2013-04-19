/**
 * Скрипт для РНУ-25 (rnu25.groovy).
 * Форма "(РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения".
 *
 * TODO:
 *      - нет уловии в проверках соответствия НСИ (потому что нету справочников)
 * 		- про нумерацию пока не уточнили, пропустить
 *		- проверки не доделаны, потому что возможно они поменяются
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
    case FormDataEvent.CHECK :
    case FormDataEvent.CALCULATE :
        calc()
        // logicalCheck()
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
// графа 2  - regNumber
// графа 3  - tradeNumber
// графа 4  - lotSizePrev
// графа 5  - lotSizeCurrent
// графа 6  - reserve
// графа 7  - cost
// графа 8  - signSecurity
// графа 9  - marketQuotation
// графа 10 - costOnMarketQuotation
// графа 11 - reserveCalcValue
// графа 12 - reserveCreation
// графа 13 - reserveRecovery

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())

    // графа 2..13
    ['regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent',
            'reserve', 'cost', 'signSecurity', 'marketQuotation',
            'costOnMarketQuotation', 'reserveCalcValue',
            'reserveCreation', 'reserveRecovery'].each {
        newRow.getCell(it).editable = true
        newRow.getCell(it).setStyleAlias('Редактируемая')
    }

    def pos = (currentDataRow != null && !formData.dataRows.isEmpty() ? currentDataRow.getOrder() : formData.dataRows.size)
    formData.dataRows.add(pos, newRow)

    setOrder()
}

/**
 * Удалить строку.
 */
def deleteRow() {
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
        if (!isTotal(row)) {
            def colNames = []
            // Список проверяемых столбцов (графа 2..13)
            def columns = ['regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', 'reserve',
                    'cost', 'signSecurity', 'marketQuotation', 'costOnMarketQuotation',
                    'reserveCalcValue', 'reserveCreation', 'reserveRecovery']

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
                    logger.error("В строке $index не заполнены колонки : $errorMsg.")
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

    // удалить строку "итого" и "итого по ГРН: ..."
    def delRow = []
    formData.dataRows.each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        formData.dataRows.remove(formData.dataRows.indexOf(row))
    }

    // отсортировать/группировать
    formData.dataRows.sort { it.regNumber }

    /** Столбцы для которых надо вычислять итого и итого по ГРН. Графа 4..7, 10..13. */
    def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserve', 'cost', 'costOnMarketQuotation',
            'reserveCalcValue', 'reserveCreation', 'reserveRecovery']
    // добавить строку "итого"
    def totalRow = formData.appendDataRow()
    totalRow.setAlias('total')
    totalRow.regNumber = 'Общий итого'
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }

    // посчитать "итого по ГРН:..."
    def totalRows = [:]
    def tmp = null
    def sums = [:]
    totalColumns.each {
        sums[it] = 0
    }
    formData.dataRows.eachWithIndex { row, i ->
        if (!isTotal(row)) {
            if (tmp == null) {
                tmp = row.regNumber
            }
            // если код расходы поменялся то создать новую строку "итого по ГРН:..."
            if (tmp != row.regNumber) {
                totalRows.put(i, getNewRow(tmp, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по ГРН:..."
            if (i == formData.dataRows.size() - 2) {
                totalColumns.each {
                    sums[it] += row.getCell(it).getValue()
                }
                totalRows.put(i + 1, getNewRow(tmp, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                sums[it] += row.getCell(it).getValue()
            }
            tmp = row.regNumber
        }
    }
    // добавить "итого по ГРН:..." в таблицу
    def i = 0
    totalRows.each { index, row ->
        formData.dataRows.add(index + i, row)
        i = i + 1
    }

    // графа 1, + поправить значения order
    formData.dataRows.eachWithIndex { row, index ->
        if (row.getAlias() != 'total') {
            row.rowNumber = index + 1 // TODO (Ramil Timerbaev) с нумерацией пока не уточнили, пропустить
            row.setOrder(index + 1)
        }
    }
}

/**
 * Логические проверки.
 */
void logicalCheck() {
    /** Предыдущий отчётный период. */
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    /** Данные нф за предыдущий отчетный период. */
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = FormDataService.find(325, FormDataKind.PRIMARY, formData.departmentId, reportPeriodOld.id)
    }

    if (!formData.dataRows.isEmpty()) {
        def index = 1
        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                continue
            }

            // 2. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 6, 13)
            if (row.lotSizeCurrent == 0 && row.reserve != row.reserveRecovery) {
                logger.warn('Графы 6 и 13 неравны!')
                break
            }

            // 3. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 5, 7, 10, 11)
            if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
                logger.warn('Графы 7, 10 и 11 ненулевые!')
                break
            }

            // 4. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 4, 6, 13)
            if (row.lotSizePrev == 0 && (row.reserve != 0 || row.reserveRecovery != 0)) {
                logger.error('Графы 6 и 13 ненулевые!')
                break
            }

            // 5. Проверка необращающихся акций (графа 8, 11, 12)
            if (row.signSecurity == 'x' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
                logger.warn('Облигации необращающиеся, графы 11 и 12 ненулевые!')
                break
            }

            // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
            if (row.signSecurity == '+' && row.reserveCalcValue - row.reserve > 0 && row.reserveRecovery != 0) {
                logger.error('Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
                break
            }

            // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 12)
            if (row.signSecurity == '+' && row.reserveCalcValue - row.reserve < 0 && row.reserveCreation != 0) {
                logger.error('Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
                break
            }

            // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 6, 11, 13)
            if (row.signSecurity == '+' && row.reserveCalcValue - row.reserve == 0 &&
                    (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
                logger.error('Облигации обращающиеся – резерв сформирован (восстановлен) некорректно!')
                break
            }

            // 9. Проверка на положительные значения при наличии созданного резерва
            if (row.reserveCreation > 0 && row.lotSizeCurrent < 0 && row.cost < 0 &&
                    row.costOnMarketQuotation < 0 && row.reserveCalcValue < 0) {
                logger.warn('Резерв сформирован. Графы 5, 7, 10 и 11 неположительные!')
                break
            }

            // 10. Проверка корректности формирования резерва (графа 6, 11, 12, 13)
            if (row.reserve + row.reserveCreation == row.reserveCalcValue + row.reserveRecovery) {
                logger.error('Резерв сформирован неверно!')
                break
            }

            if (formDataOld != null) {
                totalRow = formData.getDataRow('total')
                totalRowOld = formDataOld.getDataRow('total')

                // 1. Проверка на полноту отражения данных предыдущих отчетных периодов (графа 11) в текущем отчетном периоде (выполняется один раз для всего экземпляра)
                /*
                        Если «графа 11» > 0 формы «Регистр налогового учёта доходов по выданным гарантиям» за предыдущий отчётный период, то
                        проверяется, есть ли запись с таким же номером сделки (графа 3) в текущем периоде  («графа 3» = «графа 3» для предыдущего отчётного периода)
                        0
                        •	Если записи нет:
                        Отсутствуют строки с номерами сделок : <список номеров сделок>!
                        •	Если записей несколько:
                        Существует несколько строк с номерами сделок: <список номеров сделок>!
                        (сообщения появляются в протоколе ошибок после всех строк)
                    */
                if (totalRowOld.reserveCalcValue > 0 && totalRow.tradeNumber != totalRowOld.tradeNumber) {
                    logger.warn('') // TODO (Ramil Timerbaev)
                    break
                }

                // 11. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 4, 5 (за предыдущий период) )
                // TODO (Ramil Timerbaev) про эту проверку Карина уточнит
                if (totalRow.tradeNumber == totalRowOld.tradeNumber && totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
                    logger.error('РНУ сформирован некорректно!.')
                    break
                }

                // 12. Проверка корректности заполнения РНУ (графа 3, 3 (за предыдущий период), 6, 11 (за предыдущий период) )
                // TODO (Ramil Timerbaev) про эту проверку Карина уточнит
                if (totalRow.tradeNumber == totalRowOld.tradeNumber && totalRow.reserve != totalRowOld.reserveCalcValue) {
                    logger.error('РНУ сформирован некорректно!')
                    break
                }

                // 13. Проверка корректности заполнения РНУ (графа 4, 5 (за предыдущий период))
                if (totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
                    logger.error('РНУ сформирован некорректно!')
                    break
                }

                // 14. Проверка корректности заполнения РНУ (графа 6, 11 (за предыдущий период))
                if (totalRow.reserve != totalRowOld.reserveCalcValue) {
                    logger.error('РНУ сформирован некорректно!')
                    break
                }
            }

            // 15. Проверка на заполнение поля «<Наименование поля>». Графа 1..3, 5..13
            def colNames = []
            // Список проверяемых столбцов
            ['regNumber', 'tradeNumber', 'lotSizeCurrent', 'reserve', 'cost',
                    'signSecurity', 'marketQuotation', 'costOnMarketQuotation',
                    'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                def errorMsg = colNames.join(', ')
                logger.error("Поля $errorMsg не заполнены!")
                break
            }

            // 16. Проверка на уникальность поля «№ пп» (графа 1)
            if (index != row.rowNumber) {
                logger.error('Нарушена уникальность номера по порядку!')
                break
            }
            index += 1
        }
    }
}

/**
 * Проверки соответствия НСИ.
 */
void checkNSI() {
    // 1. Проверка актуальности поля «Признак ценной бумаги на текущую отчётную дату»
    if (false) {
        logger.warn('Признак ценной бумаги на текущую отчётную дату указан неверно!')
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
def getSum(def columnAlias) {
    return summ(formData, new ColumnRange(columnAlias, 0, formData.dataRows.size() - 2))
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = new DataRow('total' + alias, formData.getFormColumns(), formData.getFormStyles())
    newRow.regNumber = alias + ' итог'
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}