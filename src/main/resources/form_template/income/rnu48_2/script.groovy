/**
 * Скрипт для РНУ-48-2 (rnu48_2.groovy).
 * "(РНУ-48.2) Регистр налогового учёта «Сводная ведомость ввода в эксплуатацию инвентаря
 * и принадлежностей до 40 000 руб.»
 *
 * Версия ЧТЗ: 64
 *
 * Вопросы аналитикам: http://jira.aplana.com/browse/SBRFACCTAX-2469
 *
 * TODO:
 *          -   добавить справочник в графу 2
 *
 * @author vsergeev
 *
 * Графы:
 * number   -   № пп
 * kind     -   Вид расходов
 * summ     -   Сумма, включаемая в состав материальных расходов , (руб.)
 */
switch (formDataEvent) {
    case FormDataEvent.CHECK :
        logicalCheckWithTotalDataRowCheck()
        break
    case FormDataEvent.CALCULATE :
        if (logicalCheckWithoutTotalDataRowCheck()) {
            calc()
        }
        break
    case FormDataEvent.ADD_ROW :
        addNewRow()
        break
    case FormDataEvent.DELETE_ROW :
        deleteRow()
        break
}

void addNewRow() {
    //do nothing
}

void deleteRow() {
    //do nothing
}

void calc() {
    def totalRow = getTotalDataRow()
    totalRow.summ = getTotal()
}

boolean logicalCheckWithTotalDataRowCheck() {
    return (requiredColsFilled()) ?  totalRowCheck() : false
}

boolean logicalCheckWithoutTotalDataRowCheck() {
    return (requiredColsFilled())
}

/**
 * 2.   Проверка итоговых значений по всей форме
 */
boolean totalRowCheck() {
    boolean isValid = true
    def totalRow = getTotalDataRow()
    if (isBlankOrNull(totalRow.summ) || ! totalRow.summ.equals(getTotal())) {
        isValid = false
        logger.error('Итоговые значения рассчитаны неверно!')
    }

    return isValid
}

/**
 * 1.    Проверка на заполнение поля «<Наименование поля>»
 */
boolean requiredColsFilled() {
    boolean isValid = true
    def requiredRows = getRowsWithDataAliases()
    requiredRows.each {
        def dataRow = formData.getDataRow(it)
        def fieldNumber = dataRow.number
        if (isBlankOrNull(dataRow.summ)) {
            isValid = false
            logger.error("Поле $fieldNumber не заполнено!")
        }
    }

    return isValid
}

BigDecimal getTotal() {
    def rowsForSumm = getRowsWithDataAliases()
    return formData.dataRows.sum() {dataRow ->
        logger.warn(dataRow.getClass().getName())
        if (rowsForSumm.find{alias -> alias == dataRow.getAlias()} != null) {
            return dataRow.summ
        } else {
            return 0
        }
    }
}

def getTotalDataRow() {
    return formData.getDataRow('R4')
}

List<String> getRowsWithDataAliases() {
    return ['R0', 'R1', 'R2', 'R3']
}

boolean isBlankOrNull(value) {
    value == null || value.equals('')
}
