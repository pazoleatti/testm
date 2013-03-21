/**
 * Проверка обязательных полей (checkRequiredFields.groovy).
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 *
 * @author rtimerbaev
 * @since 20.03.2013 18:40
 */

// 6, 7, 9 графы
[
        'consumptionBuhSumAccepted' : ((3..25) + (34..59) + (65..70) + (80..84) + (86..88) + (114..117) + [122, 123] + (131..139)),
        'consumptionBuhSumPrevTaxPeriod' : ((3..25) + (34..52) + (55..59) + (65..70) + (80..84) + (86..88) + [122, 123] + (131..139)),
        'consumptionTaxSumS' : ([3, 5] + (9..13) + [16] + (19..24) + (26..38) + [40, 41, 42, 44] + (46..56) + [58] + (60..68) + (70..81) + [83, 85, 86, 87] + (89..92) + (97..109) + [114, 116] + (118..134) + [139] + (144..147))
].each() { colAlias, items ->
    def errorMsg = ''
    def colName = formData.getDataRow('R1').getCell(colAlias).getColumn().getName()
    // разделы
    def sectionA1 = '', sectionB1 = '', sectionA2 = '', sectionD = ''
    items.each { item->
        def row = formData.getDataRow('R' + item)
        if (row.getCell(colAlias) != null && (row.getCell(colAlias).getValue() == null || ''.equals(row.getCell(colAlias).getValue()))) {
            switch (item) {
                case (3..94) :
                    sectionA1 += getEmptyCellIncomeType(row)
                    break
                case (97..110) :
                    sectionB1 += getEmptyCellIncomeType(row)
                    break
                case (114..141) :
                    sectionA2 += getEmptyCellIncomeType(row)
                    break
                case (144..147) :
                    sectionD += getEmptyCellIncomeType(row)
                    break
            }
        }
    }

    errorMsg += addSector(errorMsg, sectionA1, '"А1"')
    errorMsg += addSector(errorMsg, sectionB1, '"Б1"')
    errorMsg += addSector(errorMsg, sectionA2, '"А2"')
    errorMsg += addSector(errorMsg, sectionD, '"Д"')

    if (!''.equals(errorMsg)) {
        logger.error("Не заполнены ячейки колонки \"$colName\" в разделе: $errorMsg.")
    }
}

/**
 * Получить разделить между названиями разделов.
 */
def getSectionSeparator(def value1, def value2) {
    return ((!''.equals(value1)) && !''.equals(value2) ? ', ' : '')
}

/**
 * Получить код строки в которой есть незаполненная ячейка.
 */
def getEmptyCellIncomeType(def row) {
    return (row.consumptionTypeId != null ? row.consumptionTypeId : 'пусто') + ', '
}

/**
 * Удалить последнюю запятую.
 */
def deleteLastSeparator(String values) {
    return values.substring(0, values.length() - 2)
}

/**
 * Добавить в сообщение коды незаполненных ячеек.
 *
 * @param errorMsg сообщение
 * @param values список незаполненных полей в виде строки (перечислены через запятую)
 * @param sectorName название раздела
 */
def addSector(def errorMsg, def values, def sectorName) {
    if (values != null && !''.equals(values)) {
        return getSectionSeparator(errorMsg, values) + sectorName + ' (' + deleteLastSeparator(values) + ')'
    } else {
        return ''
    }
}