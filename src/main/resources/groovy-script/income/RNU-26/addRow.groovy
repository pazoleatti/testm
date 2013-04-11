/**
 * Скрипт для добавления новой строки (addRow.groovy).
 * Форма "(РНУ-26) Регистр налогового учёта расчёта резерва под возможное обесценение акций, РДР, ADR, GDR и опционов эмитента в целях налогообложения".
 *
 * @author rtimerbaev
 */

def row = formData.appendDataRow()

// графа 2..17
['issuer', 'shareType', 'tradeNumber', 'currency', 'lotSizePrev',
        'lotSizeCurrent', 'reserveCalcValuePrev', 'cost', 'signSecurity',
        'marketQuotation', 'rubCourse', 'marketQuotationInRub', 'costOnMarketQuotation',
        'reserveCalcValue', 'reserveCreation', 'reserveRecovery'].each {
    row.getCell(it).editable = true
}

// графа 1  - rowNumber
// графа 2  - issuer
// графа 3  - shareType
// графа 4  - tradeNumber
// графа 5  - currency
// графа 6  - lotSizePrev
// графа 7  - lotSizeCurrent
// графа 8  - reserveCalcValuePrev
// графа 9  - cost
// графа 10 - signSecurity
// графа 11 - marketQuotation
// графа 12 - rubCourse
// графа 13 - marketQuotationInRub
// графа 14 - costOnMarketQuotation
// графа 15 - reserveCalcValue
// графа 16 - reserveCreation
// графа 17 - reserveRecovery