/**
 * Логические проверки. Проверки соответствия НСИ (logicalCheck.groovy).
 * Форма "(РНУ-6) Справка бухгалтера для отражения доходов, учитываемых в РНУ-4, учёт которых требует применения метода начисления".
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
def a = (taxPeriod != null ? taxPeriod.getStartDate() : null )

/** Дата окончания отчетного периода. */
def b = (taxPeriod != null ? taxPeriod.getEndDate() : null)

if (!formData.dataRows.isEmpty()) {
    for (def row : formData.dataRows) {
        if (isTotal(row)) {
            continue
        }

        // графа 3 - Проверка даты совершения операции и границ отчётного периода
        if (row.date < a || b < row.date) {
            logger.error('Дата совершения операции вне границ отчётного периода!')
            break
        }

        // графа 9, 10, 11, 12 - Проверка на нулевые значения
        if (!isTotal(row) &&
                row.taxAccountingCurrency == 0 && row.taxAccountingRuble == 0 &&
                row.accountingCurrency == 0 && row.ruble == 0) {
            logger.error('Все суммы по операции нулевые!')
            break
        }
    }
}

// графа 10, 12 - Проверка на превышение суммы дохода по данным бухгалтерского учёта над суммой начисленного дохода
def totalRow = formData.getDataRow('total')
if (totalRow.ruble > 0 && totalRow.taxAccountingRuble >= totalRow.ruble) {
    logger.warn('Сумма данных бухгалтерского учёта превышает сумму начисленных платежей!')
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