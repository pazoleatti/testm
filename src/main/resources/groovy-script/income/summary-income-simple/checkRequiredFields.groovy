/**
 * Проверка обязательных полей (checkRequiredFields.groovy).
 * Форма "Расшифровка видов доходов, учитываемых в простых РНУ (доходы простые)".
 *
 * @author rtimerbaev
 * @since 20.03.2013 18:10
 */

// 5-10 графы
[
        'rnu6Field10Sum' : ((3..30) + (33..36) + (38..55) + (57..60) + [62, 63] + (71..77) + [85, 86, 92, 93, 104, 110, 114, 115, 197] + (200..202) + (204..208)),
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
                    sectionA1 += getEmptyCellType(row)
                    break
                case (90..93) :
                    sectionB1 += getEmptyCellType(row)
                    break
                case (98..209) :
                    sectionA2 += getEmptyCellType(row)
                    break
                case (213..214) :
                    sectionB2 += getEmptyCellType(row)
                    break
                case 217 :
                    sectionD += getEmptyCellType(row)
                    break
                case (220..221) :
                    sectionE += getEmptyCellType(row)
                    break
            }
        }
    }

    errorMsg += addSector(errorMsg, sectionA1, '"А1"')
    errorMsg += addSector(errorMsg, sectionB1, '"Б1"')
    errorMsg += addSector(errorMsg, sectionA2, '"А2"')
    errorMsg += addSector(errorMsg, sectionB2, '"Б2"')
    errorMsg += addSector(errorMsg, sectionD, '"Д"')
    errorMsg += addSector(errorMsg, sectionE, '"Е"')

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
def getEmptyCellType(def row) {
    return (row.incomeTypeId != null ? row.incomeTypeId : 'пусто') + ', '
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