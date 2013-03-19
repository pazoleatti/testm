/* Условие. */

/* Конец условия. */

/**
 * Проверка обязательных полей (checkRequiredFields.groovy).
 * Форма "Расшифровка видов расходов, учитываемых в простых РНУ (расходы простые)".
 *
 * @author rtimerbaev
 * @since 22.02.2013 11:00
 */

/**
 * Получить разделить между названиями разделов.
 */
def getSectionSeparator(def value1, def value2) {
    return ((!''.equals(value1)) && !''.equals(value2) ? ', ' : '')
}

// 5-9 графы
[
        'rnu7Field10Sum' : ([17, 18, 22, 28, 29, 34] + (48..51) + [53, 54] + (57..62) + [74, 75, 78, 79, 80, 184, 185, 186]),
        'rnu7Field12Accepted' : ([17, 18, 22, 28, 29, 34] + (48..51) + [53, 54] + (57..62) + [74, 75, 78, 79, 80, 184, 185, 186, 193]),
        'rnu7Field12PrevTaxPeriod' : ([17, 18, 22, 28, 29, 34] + (48..51) + [53, 54] + (57..62) + [74, 75, 78, 79, 80, 184, 185, 186]),
        'rnu5Field5Accepted' : ((3..84) + [88] + (92..191)),
        'rnu5Field5PrevTaxPeriod' : ([10, 13, 14, 15, 23, 24, 25] + (30..33) + (35..39) + [52, 55, 56, 63] + (70..73) + [76, 77, 81, 82, 89, 155] + (166..176) + [178] + (180..183))
].each() { colAlias, items ->
    def errorMsg = ''
    def colName = formData.getDataRow('R1').getCell(colAlias).getColumn().getName()
    // разделы
    def sectionA1 = '', sectionB1 = '', sectionA2 = ''
    items.each { item->
        def row = formData.getDataRow('R' + item)
        if (row.getCell(colAlias) != null && (row.getCell(colAlias).getValue() == null || ''.equals(row.getCell(colAlias).getValue()))) {
            switch (item) {
                case (3..84) :
                    sectionA1 = '"А1"'
                    break
                case 88 :
                    sectionB1 = '"Б1"'
                    break
                case (92..193) :
                    sectionA2 = '"А2"'
                    break
            }
        }
    }
    errorMsg = sectionA1
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionB1) + sectionB1
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionA2) + sectionA2
    if (!''.equals(errorMsg)) {
        logger.error("Не заполнены ячейки колонки \"$colName\" в разделе: $errorMsg.")
    }
}