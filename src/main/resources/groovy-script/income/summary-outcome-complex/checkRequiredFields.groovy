/* Условие. */

/* Конец условия. */

/**
 * Проверка обязательных полей (checkRequiredFields.groovy).
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 *
 * TODO:
 *		1. возможно надо будет переделать вывод сообщений об ошибках (аналогично простым доходам)
 *
 * @author rtimerbaev
 * @since 19.02.2013 12:40
 */

// 6, 7, 9 графы
[
        'consumptionBuhSumAccepted' : ((3..25) + (34..59) + (65..70) + (89..84) + (86..88) + (114..117) + [122, 123] + (131..139)),
        'consumptionBuhSumPrevTaxPeriod' : ((3..25) + (34..52) + (55..59) + (65..70) + (89..84) + (86..88) + [122, 123] + (131..139)),
        'consumptionTaxSumS' : ([3, 5] + (9..13) + [16] + (19..24) + (26..38) + [40, 41, 42, 44] + (46..56) + [58] + (60..68) + (70..81) + [83, 85, 86, 87] + (89..92) + (97..109) + [114, 116] + (118..134) + [139] + (144..147))
].each() { colAlias, items ->
    def errorMsg = '';
    def colName = null;
    items.each() { item->
        def row = formData.getDataRow('R' + item)
        if (row.getCell(colAlias) != null && (row.getCell(colAlias).getValue() == null || ''.equals(row.getCell(colAlias).getValue()))) {
            errorMsg += (!''.equals(errorMsg) ? ', ' : '') + formData.getDataRowIndex('R' + item)
            if (colName == null) {
                colName = row.getCell(colAlias).getColumn().getName()
            }
        }
    }
    if (!''.equals(errorMsg)) {
        logger.error("Не заполнены поля колонки \"$colName\" в строках: $errorMsg.")
    }
}