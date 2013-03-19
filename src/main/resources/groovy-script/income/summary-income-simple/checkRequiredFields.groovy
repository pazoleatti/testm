/**
 * Проверка обязательных полей (checkRequiredFields.groovy).
 * Форма "Расшифровка видов доходов, учитываемых в простых РНУ (доходы простые)".
 *
 * @author rtimerbaev
 * @since 21.02.2013 16:10
 */

/**
 * Получить разделить между названиями разделов.
 */
def getSectionSeparator(def value1, def value2) {
    return ((!''.equals(value1)) && !''.equals(value2) ? ', ' : '')
}

// 5-10 графы
[
        'rnu6Field10Sum' : ((3..30) + (33..36) + (38..55) + (57..60) + [61, 62] + (71..77) + [85, 86, 92, 93, 104, 110, 114, 115, 197] + (200..202) + (204..208)),
        'rnu6Field10Field2' : ((3..16) + [19] + (22..30) + (33..36) + (38..55) + (57..60) + [62, 63] + (71..77) + [85, 86, 110, 197] + (200..202) + (206..208)),
        'rnu6Field12Accepted' : ([3, 4] + (7..10) + (12..30) + (33..36) + [38, 40, 44, 47, 62, 63, 87, 92, 93, 104, 114, 115, 197] + (200..202) + [204, 205, 209]),
        'rnu6Field12PrevTaxPeriod' : ([3, 4] + (7..10) + (12..30) + (33..36) + [38, 40, 44, 47, 62, 63, 87, 92, 93, 104, 114, 115, 197] + (200..202) + [204, 205]),
        'rnu4Field5Accepted' : ((3..88) + (90..93) + (98..208) + [213, 214, 217, 220, 221]),
        'rnu4Field5PrevTaxPeriod' : ([5, 6, 11, 31, 32, 37, 39, 41, 42, 43, 45, 46] + (48..61) + (64..84) + [86, 90, 91] + (98..103) + (105..113) + (116..164) + (170..196) + [198, 199, 203, 206, 207, 208, 213, 214, 217])
].each { colAlias, items ->
    def errorMsg = ''
    def colName = formData.getDataRow('R1').getCell(colAlias).getColumn().getName()
    // разделы
    def sectionA1 = '', sectionB1 = '', sectionA2 = '', sectionB2 = '', sectionD = '', sectionE = ''
    items.each { item->
        def row = formData.getDataRow('R' + item)
        if (row.getCell(colAlias) != null && (row.getCell(colAlias).getValue() == null || ''.equals(row.getCell(colAlias).getValue()))) {
            switch (item) {
                case (3..86) :
                    sectionA1 = '"А1"'
                    break
                case (90..93) :
                    sectionB1 = '"Б1"'
                    break
                case (98..209) :
                    sectionA2 = '"А2"'
                    break
                case (213..214) :
                    sectionB2 = '"Б2"'
                    break
                case 217 :
                    sectionD = '"Д"'
                    break
                case (220..221) :
                    sectionE = '"Е"'
                    break
            }
        }
    }
    errorMsg = sectionA1
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionB1) + sectionB1
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionA2) + sectionA2
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionB2) + sectionB2
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionD) + sectionD
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionE) + sectionE
    if (!''.equals(errorMsg)) {
        logger.error("Не заполнены ячейки колонки \"$colName\" в разделе: $errorMsg.")
    }
}