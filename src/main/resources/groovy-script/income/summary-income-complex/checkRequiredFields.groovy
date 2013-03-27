//com.aplana.sbrf.taxaccounting.model.FormData formData
//com.aplana.sbrf.taxaccounting.dao.script.Income102Dao income102Dao
//com.aplana.sbrf.taxaccounting.dao.script.Income101Dao income101Dao

/**
 * Проверка обязательных полей (checkRequiredFields.groovy).
 * Форма "Сводная форма начисленных доходов (доходы сложные)".
 *
 * @author rtimerbaev
 * @since 20.03.2013 18:30
 */

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
                    sectionA1 += getEmptyCellIncomeType(row)
                    break
                case (24..39) :
                    sectionB1 += getEmptyCellIncomeType(row)
                    break
                case (43..89) :
                    sectionA2 += getEmptyCellIncomeType(row)
                    break
                case (82..95) :
                    sectionB2 += getEmptyCellIncomeType(row)
                    break
                case (98..103) :
                    sectionV += getEmptyCellIncomeType(row)
                case (106..19) :
                    sectionG += getEmptyCellIncomeType(row)
                case (112..116) :
                    sectionD += getEmptyCellIncomeType(row)
                    break
                case 119 :
                    sectionE += getEmptyCellIncomeType(row)
                    break
                case (122..127) :
                    sectionJ += getEmptyCellIncomeType(row)
                    break
                case (130..131) :
                    sectionS += getEmptyCellIncomeType(row)
                    break
            }
        }
    }

    errorMsg += addSector(errorMsg, sectionA1, '"А1"')
    errorMsg += addSector(errorMsg, sectionB1, '"Б1"')
    errorMsg += addSector(errorMsg, sectionA2, '"А2"')
    errorMsg += addSector(errorMsg, sectionB2, '"Б2"')
    errorMsg += addSector(errorMsg, sectionV, '"В"')
    errorMsg += addSector(errorMsg, sectionG, '"Г"')
    errorMsg += addSector(errorMsg, sectionD, '"Д"')
    errorMsg += addSector(errorMsg, sectionE, '"Е"')
    errorMsg += addSector(errorMsg, sectionJ, '"Ж"')
    errorMsg += addSector(errorMsg, sectionS, '"специфичная информация"')

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