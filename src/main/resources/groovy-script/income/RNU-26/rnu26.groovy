/**
 * Скрипт для РНУ-26 (rnu26.groovy).
 * Форма "(РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения".
 *
 * TODO:
 *      - нет условии в проверках соответствия НСИ (потому что нету справочников)
 *		- логические проверки не доделаны, потому что возможно они поменяются
 * 		- про нумерацию пока не уточнили, пропустить
 *
 * @author rtimerbaev
 */

switch (formDataEvent) {
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
}

// графа 1  - rowNumber
// графа 2  - issuer
// графа 3  - shareType
// графа 4  - tradeNumber
// графа 5  - currency
// графа 6  - lotSizePrev
// графа 7  - lotSizeCurrent
// графа 8  - reserveCalcValuePrev
// графа 9  - cost
// графа 10 - signSecurity
// графа 11 - marketQuotation
// графа 12 - rubCourse
// графа 13 - marketQuotationInRub
// графа 14 - costOnMarketQuotation
// графа 15 - reserveCalcValue
// графа 16 - reserveCreation
// графа 17 - reserveRecovery

/**
 * Добавить новую строку.
 */
def addNewRow() {
    def newRow = formData.appendDataRow(currentDataRow, null)

    // графа 2..17
    ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev',
            'lotSizeCurrent', 'reserveCalcValuePrev', 'cost', 'signSecurity',
            'marketQuotation', 'rubCourse', 'marketQuotationInRub', 'costOnMarketQuotation',
            'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each {
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
            // Список проверяемых столбцов
            def columns = ['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev',
                    'lotSizeCurrent', 'reserveCalcValuePrev', 'cost', 'signSecurity',
                    'marketQuotationInRub', 'costOnMarketQuotation']

            // если не заполнены графа 11 и графа 12, то графа 13 должна быть заполнена вручную
            if (row.marketQuotation == null && row.rubCourse == null) {
                columns -= 'marketQuotationInRub'
            }

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
                    index = formData.dataRows.indexOf(row) + 1
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

    // удалить строку "итого" и "итого по Эмитенту: ..."
    def delRow = []
    formData.dataRows.each { row ->
        if (isTotal(row)) {
            delRow += row
        }
    }
    delRow.each { row ->
        formData.dataRows.remove(formData.dataRows.indexOf(row))
    }
    if (formData.dataRows.isEmpty()) {
        return
    }

    // отсортировать/группировать
    formData.dataRows.sort { it.issuer }

    formData.dataRows.eachWithIndex { row, index ->
        // графа 1
        row.rowNumber = index + 1 // TODO (Ramil Timerbaev) с нумерацией пока не уточнили, пропустить

        // графа 13
        if (row.marketQuotation != null && row.rubCourse != null) {
            row.marketQuotationInRub = row.marketQuotation * row.rubCourse
        }
    }

    // графы для которых надо вычислять итого и итого по эмитенту (графа 6..9, 14..17)
    def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev',
            'cost', 'costOnMarketQuotation', 'reserveCalcValue',
            'reserveCreation', 'reserveRecovery']
    // добавить строку "итого"
    def totalRow = formData.appendDataRow()
    totalRow.setAlias('total')
    totalRow.issuer = 'Общий итог'
    setTotalStyle(totalRow)
    totalColumns.each { alias ->
        totalRow.getCell(alias).setValue(getSum(alias))
    }

    // посчитать "итого по Эмитенту:..."
    def totalRows = [:]
    def sums = [:]
    def tmp = null
    totalColumns.each {
        sums[it] = 0
    }
    formData.dataRows.eachWithIndex { row, i ->
        if (!isTotal(row)) {
            if (tmp == null) {
                tmp = row.issuer
            }
            // если код расходы поменялся то создать новую строку "итого по Эмитента:..."
            if (tmp != row.issuer) {
                totalRows.put(i, getNewRow(tmp, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            // если строка последняя то сделать для ее кода расхода новую строку "итого по Эмитента:..."
            if (i == formData.dataRows.size() - 2) {
                totalColumns.each {
                    sums[it] += row.getCell(it).getValue()
                }
                totalRows.put(i + 1, getNewRow(row.issuer, totalColumns, sums))
                totalColumns.each {
                    sums[it] = 0
                }
            }
            totalColumns.each {
                sums[it] += row.getCell(it).getValue()
            }
            tmp = row.issuer
        }
    }
    // добавить "итого по Эмитенту:..." в таблицу
    def i = 0
    totalRows.each { index, row ->
        formData.dataRows.add(index + i, row)
        i = i + 1
    }
}

/**
 * Логические проверки.
 *
 * @param checkRequiredColumns проверять ли обязательные графы
 */
void logicalCheck(def checkRequiredColumns) {
    // данные предыдущего отчетного периода
    def formDataOld = getFormDataOld()

    if (formDataOld != null &&
            !formDataOld.dataRows.isEmpty() && !formData.dataRows.isEmpty()) {
        // 1. Проверка на полноту отражения данных предыдущих отчетных периодов (графа 15) в текущем отчетном периоде (выполняется один раз для всего экземпляра)
        def count
        def missContract = []
        def severalContract = []
        formDataOld.dataRows.each { prevRow ->
            if (prevRow.reserveCalcValue > 0) {
                count = 0
                formDataOld.dataRows.each { row ->
                    if (row.tradeNumber == prevRow.tradeNumber) {
                        count += 1
                    }
                }
                if (count == 0) {
                    missContract.add(prevRow.tradeNumber)
                } else if (count > 1) {
                    severalContract.add(prevRow.tradeNumber)
                }
            }
        }
        if (!missContract.isEmpty()) {
            def message = missContract.join(', ')
            logger.warn("Отсутствуют строки с номерами сделок: $message!")
        }
        if (!severalContract.isEmpty()) {
            def message = severalContract.join(', ')
            logger.warn("Существует несколько строк с номерами сделок: $message!")
        }
    }

    if (!formData.dataRows.isEmpty()) {
        def i = 1
        // суммы строки общих итогов
        def totalSums = [:]
        // графы для которых надо вычислять итого и итого по эмитенту (графа 6..9, 14..17)
        def totalColumns = ['lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev',
                'cost', 'costOnMarketQuotation', 'reserveCalcValue',
                'reserveCreation', 'reserveRecovery']
        // признак наличия итоговых строк
        def hasTotal = false
        // список групп кодов классификации для которых надо будет посчитать суммы
        def totalGroupsName = []

        for (def row : formData.dataRows) {
            if (isTotal(row)) {
                hasTotal = true
                continue
            }

            // 15. Обязательность заполнения поля графы 1..3, 5..10, 13, 14
            def colNames = []
            // Список проверяемых столбцов
            ['issuer', 'shareType', 'currency', 'lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev',
                    'cost', 'signSecurity', 'marketQuotationInRub', 'costOnMarketQuotation'].each {
                if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                    colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
                }
            }
            if (!colNames.isEmpty()) {
                if (!checkRequiredColumns) {
                    return
                }
                def index = row.rowNumber
                def errorMsg = colNames.join(', ')
                if (index != null) {
                    logger.error("В строке \"№ пп\" равной $index не заполнены колонки : $errorMsg.")
                } else {
                    index = formData.dataRows.indexOf(row) + 1
                    logger.error("В строке $index не заполнены колонки : $errorMsg.")
                }
                return
            }

            // 2. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 8, 17)
            if (row.lotSizeCurrent == 0 && row.reserveCalcValuePrev != row.reserveRecovery) {
                logger.warn('Графы 8 и 17 неравны!')
            }

            // 3. Проверка при нулевом значении размера лота на текущую отчётную дату (графа 7, 9, 14, 15)
            if (row.lotSizeCurrent == 0 && (row.cost != 0 || row.costOnMarketQuotation != 0 || row.reserveCalcValue != 0)) {
                logger.warn('Графы 9, 14 и 15 ненулевые!')
            }

            // 4. Проверка при нулевом значении размера лота на предыдущую отчётную дату (графа 6, 8, 17)
            if (row.lotSizePrev == 0 && (row.reserveCalcValuePrev != 0 || row.reserveRecovery != 0)) {
                logger.error('Графы 8 и 17 ненулевые!')
                return
            }

            // 5. Проверка необращающихся акций (графа 10, 15, 16)
            if (row.signSecurity == 'x' && (row.reserveCalcValue != 0 || row.reserveCreation != 0)) {
                logger.warn('Акции необращающиеся, графы 15 и 16 ненулевые!')
            }

            // 6. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
            if (row.signSecurity == '+' && row.reserveCalcValue - row.reserveCalcValuePrev > 0 && row.reserveRecovery != 0) {
                logger.error('Акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
                return
            }

            // 7. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 16)
            if (row.signSecurity == '+' && row.reserveCalcValue - row.reserveCalcValuePrev < 0 && row.reserveCreation != 0) {
                logger.error('Акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
                return
            }

            // 8. Проверка создания (восстановления) резерва по обращающимся акциям (графа 8, 10, 15, 17)
            if (row.signSecurity == '+' && row.reserveCalcValue - row.reserveCalcValuePrev == 0 &&
                    (row.reserveCreation != 0 || row.reserveRecovery != 0)) {
                logger.error('Акции обращающиеся – резерв сформирован (восстановлен) некорректно!')
                return
            }

            // 9. Проверка корректности формирования резерва (графа 8, 15, 16, 17)
            if (row.reserveCalcValuePrev + row.reserveCreation != row.reserveCalcValue + row.reserveRecovery) {
                logger.error('Резерв сформирован неверно!')
                return
            }

            // 10. Проверка на положительные значения при наличии созданного резерва
            if (row.reserveCreation > 0 && row.lotSizeCurrent < 0 && row.cost < 0 &&
                    row.costOnMarketQuotation < 0 && row.reserveCalcValue < 0) {
                logger.warn('Резерв сформирован. Графы 7, 9, 14 и 15 неположительные!')
            }

            // 11. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 6, 7 (за предыдущий период) )
            if (checkOld(row, 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent', formDataOld)) {
                def curCol = 4
                def curCol2 = 6
                def prevCol = 4
                def prevCol2 = 7
                logger.warn("РНУ сформирован некорректно! Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-26 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-26 за предыдущий отчётный период.")
            }

            // 12. Проверка корректности заполнения РНУ (графа 4, 4 (за предыдущий период), 8, 15 (за предыдущий период) )
            if (checkOld(row, 'tradeNumber', 'reserveCalcValuePrev', 'reserveCalcValue', formDataOld)) {
                def curCol = 4
                def curCol2 = 4
                def prevCol = 8
                def prevCol2 = 15
                logger.error("РНУ сформирован некорректно! Не выполняется условие: Если «графа $curCol» = «графа $prevCol» формы РНУ-26 за предыдущий отчётный период, то «графа $curCol2»  = «графа $prevCol2» формы РНУ-26 за предыдущий отчётный период.")
                return
            }

            // 16. Проверка на уникальность поля «№ пп» (графа 1)
            if (i != row.rowNumber) {
                logger.error('Нарушена уникальность номера по порядку!')
                return
            }
            i += 1

            // 17. Арифметическая проверка графы 13
            if (row.marketQuotation != null && row.rubCourse != null &&
                    row.marketQuotationInRub != row.marketQuotation * row.rubCourse) {
                logger.error('Неверно рассчитана графа «Рыночная котировка одной ценной бумаги в рублях»!')
                return
            }

            // 18. Проверка итоговых значений по эмитентам
            if (!totalGroupsName.contains(row.issuer)) {
                totalGroupsName.add(row.issuer)
            }

            // 19. Проверка итогового значений по всей форме - подсчет сумм для общих итогов
            totalColumns.each { alias ->
                if (totalSums[alias] == null) {
                    totalSums[alias] = 0
                }
                totalSums[alias] += (row.getCell(alias).getValue() ?: 0)
            }
        }

        if (formDataOld != null && hasTotal) {
            totalRow = formData.getDataRow('total')
            totalRowOld = formDataOld.getDataRow('total')

            // 13. Проверка корректности заполнения РНУ (графа 6, 7 (за предыдущий период))
            if (totalRow.lotSizePrev != totalRowOld.lotSizeCurrent) {
                def curCol = 6
                def prevCol = 7
                logger.error("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе $curCol = «Итого» по графе $prevCol формы РНУ-26 за предыдущий отчётный период.")
                return
            }

            // 14. Проверка корректности заполнения РНУ (графа 8, 15 (за предыдущий период))
            if (totalRow.cost != totalRowOld.reserveCalcValue) {
                def curCol = 8
                def prevCol = 15
                logger.error("РНУ сформирован некорректно! Не выполняется условие: «Итого» по графе $curCol = «Итого» по графе $prevCol формы РНУ-26 за предыдущий отчётный период.")
                return
            }
        }

        if (hasTotal) {
            def totalRow = formData.getDataRow('total')

            // 18. Проверка итоговых значений по эмитенту
            for (def codeName : totalGroupsName) {
                def row = formData.getDataRow('total' + codeName)
                for (def alias : totalColumns) {
                    if (calcSumByCode(codeName, alias) != row.getCell(alias).getValue()) {
                        logger.error("Итоговые значения по эмитенту $codeName рассчитаны неверно!")
                        return
                    }
                }
            }

            // 19. Проверка итогового значений по всей форме
            for (def alias : totalColumns) {
                if (totalSums[alias] != totalRow.getCell(alias).getValue()) {
                    hindError = true
                    logger.error('Итоговые значения рассчитаны неверно!')
                    return
                }
            }
        }
    }
}

/**
 * Проверки соответствия НСИ.
 */
void checkNSI() {
    // TODO (Ramil Timerbaev)
    // 1. Проверка курса валюты со справочным - Проверка актуальности значения» графы 6» на дату по «графе 5»
    if (false) {
        logger.warn('Неверный курс валюты!')
    }

    // 1. Проверка актуальности поля «Валюта выпуска ценной бумаги»	Какой алгоритм?
    if (false) {
        logger.warn('Валюта выпуска ценной бумаги указана неверно!')
    }

    // 2. Проверка актуальности поля «Признак ценной бумаги на текущую отчётную дату»
    if (false) {
        logger.warn('')
    }

    // 3. Проверка актуальности поля «Курс рубля к валюте рыночной котировки»
    if (false) {
        logger.warn('')
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
    def from = 0
    def to = formData.dataRows.size() - 2
    if (from > to) {
        return 0
    }
    return summ(formData, new ColumnRange(columnAlias, from, to))
}

/**
 * Получить новую строку.
 */
def getNewRow(def alias, def totalColumns, def sums) {
    def newRow = new DataRow('total' + alias, formData.getFormColumns(), formData.getFormStyles())
    newRow.issuer = alias + ' итог'
    setTotalStyle(newRow)
    totalColumns.each {
        newRow.getCell(it).setValue(sums[it])
    }
    return newRow
}

/**
 * Сверить данные с предыдущим периодом.
 *
 * @param row строка нф текущего периода
 * @param likeColumnName псевдоним графы по которому ищутся соответствующиеся строки
 * @param curColumnName псевдоним графы текущей нф для второго условия
 * @param prevColumnName псевдоним графы предыдущей нф для второго условия
 * @param prevForm данные нф предыдущего периода
 */
def checkOld(def row, def likeColumnName, def curColumnName, def prevColumnName, def prevForm) {
    if (prevForm == null) {
        return false
    }
    if (row.getCell(likeColumnName).getValue() == null) {
        return false
    }
    for (def prevRow : prevForm.dataRows) {
        if (row.getCell(likeColumnName).getValue() == prevRow.getCell(likeColumnName).getValue() &&
                row.getCell(curColumnName).getValue() != prevRow.getCell(prevColumnName).getValue()) {
            return true
        }
    }
}

/**
 * Получить данные за предыдущий отчетный период
 */
def getFormDataOld() {
    // предыдущий отчётный период
    def reportPeriodOld = reportPeriodService.getPrevReportPeriod(formData.reportPeriodId)

    // РНУ-26 за предыдущий отчетный период
    def formDataOld = null
    if (reportPeriodOld != null) {
        formDataOld = FormDataService.find(formData.formType.id, FormDataKind.PRIMARY, formDataDepartment.id, reportPeriodOld.id)
    }

    return formDataOld
}

/**
 * * Посчитать сумму указанного графа для строк с общим значением
 *
 * @param value значение общее для всех строк суммирования
 * @param alias название графа
 */
def calcSumByCode(def value, def alias) {
    def sum = 0
    formData.dataRows.each { row ->
        if (!isTotal(row) && row.issuer == value) {
            sum += (row.getCell(alias).getValue() ?: 0)
        }
    }
    return sum
}

/**
 * Устаносить стиль для итоговых строк.
 */
void setTotalStyle(def row) {
    ['rowNumber', 'issuer', 'shareType', 'tradeNumber', 'currency',
            'lotSizePrev', 'lotSizeCurrent', 'reserveCalcValuePrev', 'cost', 'signSecurity',
            'marketQuotation', 'rubCourse', 'marketQuotationInRub', 'costOnMarketQuotation',
            'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each {
        row.getCell(it).setStyleAlias('Контрольные суммы')
    }
}