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