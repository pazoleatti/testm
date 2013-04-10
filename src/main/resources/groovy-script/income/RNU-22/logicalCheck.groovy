/**
 * Логические проверки. Проверки соответствия НСИ (logicalCheck.groovy).
 * Форма "(РНУ-22) Регистр налогового учёта периодически взимаемых комиссий по операциям кредитования".
 *
 * TODO:
 *		- разобраться с датами начала и окончания отчетного периода
 *
 * @author rtimerbaev
 */

/** Отчётный период. */
def reportPeriod = reportPeriodService.get(formData.reportPeriodId)

/** Налоговый период. */
def taxPeriod = (reportPeriod != null ? taxPeriodService.get(reportPeriod.taxPeriodId) : null)

/** Дата начала отчетного периода. */
def a = (taxPeriod != null ? taxPeriod.getStartDate() : null)

/** Дата окончания отчетного периода. */
def b = (taxPeriod != null ? taxPeriod.getEndDate() : null)

if (!formData.dataRows.isEmpty()) {
    def index = 1
    for (def row : formData.dataRows) {
        if (isTotal(row)) {
            continue
        }

        // 1. Проверка даты совершения операции и границ отчётного периода (графа 5, 10, 12)
        if (a > row.transactionDate ||
                row.calcPeriodAccountingEndDate > b ||
                row.calcPeriodEndDate > b) {
            logger.error('Дата совершения операции вне границ отчётного периода!')
            break
        }

        // 2. Проверка на нулевые значения (графа 13..20)
        def hasNull = true
        ['accruedCommisCurrency', 'accruedCommisRub', 'commisInAccountingCurrency', 'commisInAccountingRub',
                'accrualPrevCurrency', 'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub'].each { alias ->
            def tmp = row.getCell(alias).getValue()
            if (tmp != null && tmp != 0) {
                hasNull = false
            }
        }
        if (hasNull) {
            logger.error('Все суммы по операции нулевые!')
            break
        }

        // 3. Проверка на сумму платы (графа 4)
        if (row.base != null && row.base == 0) {
            logger.warn('Суммы платы равны 0!')
            break
        }

        // 4. Проверка задания расчётного периода
        if (row.calcPeriodAccountingBeginDate > row.calcPeriodAccountingEndDate ||
                row.calcPeriodBeginDate > row.calcPeriodEndDate) {
            logger.warn('Неправильно задан расчётный период!')
            break
        }

        // 5. Проверка на корректность даты договора
        if (row.contraclData > b) {
            logger.error('Дата договора неверная!')
            break
        }


        // 7. Проверка на заполнение поля «<Наименование поля>». Графа 1..8, 13..20
        def colNames = []
        // Список проверяемых столбцов
        ['contractNumber', 'contraclData', 'base', 'transactionDate', 'course',
                'interestRate', 'basisForCalc', 'accruedCommisCurrency', 'accruedCommisRub',
                'commisInAccountingCurrency', 'commisInAccountingRub', 'accrualPrevCurrency',
                'accrualPrevRub', 'reportPeriodCurrency', 'reportPeriodRub'].each {
            if (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue())) {
                colNames.add('"' + row.getCell(it).getColumn().getName() + '"')
            }
        }
        if (!colNames.isEmpty()) {
            def errorMsg = colNames.join(', ')
            logger.error('Поля $colName не заполнены!')
            break
        }

        // 8. Проверка на заполнение поля «<Наименование поля>»
        // При заполнении граф 9 и 10, графы 11 и 12 должны быть пустыми.
        def checkColumn9and10 = row.calcPeriodAccountingBeginDate != null && row.calcPeriodAccountingEndDate != null &&
                (row.calcPeriodBeginDate != null || row.calcPeriodEndDate != null)
        // При заполнении граф 11 и 12, графы 9 и 10 должны быть пустыми.
        def checkColumn11and12 = row.calcPeriodAccountingBeginDate != null && row.calcPeriodAccountingEndDate != null &&
                (row.calcPeriodBeginDate != null || row.calcPeriodEndDate != null)
        if (checkColumn9and10 || checkColumn11and12) {
            logger.error('Поля в графе 9, 10, 11, 12 заполены неверно!')
            break
        }

        // 9. Проверка на уникальность поля «№ пп» (графа 1)
        if (index != row.rowNumber) {
            logger.error('Нарушена уникальность номера по порядку!')
            break
        }
        index += 1
    }

    // 6. Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода
    def totalRow = formData.getDataRow('total')
    if (totalRow.commisInAccountingRub > 0 && totalRow.accruedCommisRub >= totalRow.commisInAccountingRub) {
        logger.warn('Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!')
    }
}

/*
 * Вспомогалельные методы.
 */

/**
 * Проверка является ли строка итоговой.
 */
def isTotal(def row) {
    return row != null && row.getAlias() != null && row.getAlias().contains('total')
}