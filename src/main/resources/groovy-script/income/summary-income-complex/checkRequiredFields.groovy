/* Условие. */

/* Конец условия. */

/**
 * Проверка обязательных полей (checkRequiredFields.groovy).
 * Форма "Сводная форма начисленных доходов (доходы сложные)".
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

// 6, 7, 9 графы
[
        'incomeBuhSumAccepted' : ((4..8) + [19] + (43..51) + [64] + (70..77) + [98, 99, 100, 106, 107, 122, 123, 124]),
        'incomeBuhSumPrevTaxPeriod' : ((4..8) + [19] + (43..51) + [64] + (70..77) + [122, 123, 124]),
        'incomeTaxSumS' : ([3, 4, 5, 7] + (9..19) + (24..37) + [43] + (45..50) + (52..70) + (72..77) + (82..94) + [98, 101, 102, 103, 106, 108, 109] + (112..115) + [122, 125, 126, 127, 130, 131])
].each() { colAlias, items ->
    def errorMsg = ''
    def colName = formData.getDataRow('R1').getCell(colAlias).getColumn().getName()
    // разделы
    def sectionA1 = '', sectionB1 = '',
        sectionA2 = '', sectionB2 = '',
        sectionV = '', sectionG = '',
        sectionD = '', sectionE = '',
        sectionJ = '', sectionS = ''
    items.each { item->
        def row = formData.getDataRow('R' + item)
        if (row.getCell(colAlias) != null && (row.getCell(colAlias).getValue() == null || ''.equals(row.getCell(colAlias).getValue()))) {
            switch (item) {
                case (3..21) :
                    sectionA1 = '"А1"'
                    break
                case (24..39) :
                    sectionB1 = '"Б1"'
                    break
                case (43..89) :
                    sectionA2 = '"А2"'
                    break
                case (82..95) :
                    sectionB2 = '"Б2"'
                    break
                case (98..103) :
                    sectionV = '"В"'
                case (106..19) :
                    sectionG = '"Г"'
                case (112..116) :
                    sectionD = '"Д"'
                    break
                case 119 :
                    sectionE = '"Е"'
                    break
                case (122..127) :
                    sectionJ = '"Ж"'
                    break
                case (130..131) :
                    sectionS = '"специфичная информация"'
                    break
            }
        }
    }
    errorMsg = sectionA1
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionB1) + sectionB1
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionA2) + sectionA2
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionB2) + sectionB2
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionV) + sectionV
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionG) + sectionG
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionD) + sectionD
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionE) + sectionE
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionJ) + sectionJ
    errorMsg = errorMsg + getSectionSeparator(errorMsg, sectionS) + sectionS
    if (!''.equals(errorMsg)) {
        logger.error("Не заполнены ячейки колонки \"$colName\" в разделе: $errorMsg.")
    }
}