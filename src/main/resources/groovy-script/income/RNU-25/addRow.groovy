/**
 * Скрипт для добавления новой строки (addRow.groovy).
 * Форма "(РНУ-25) Регистр налогового учёта расчёта резерва под возможное обесценение ГКО, ОФЗ и ОБР в целях налогообложения".
 *
 * @author rtimerbaev
 */

def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())

// графа 2..13
['regNumber', 'tradeNumber', 'lotSizePrev', 'lotSizeCurrent',
        'reserve', 'cost', 'signSecurity', 'marketQuotation',
        'costOnMarketQuotation', 'reserveCalcValue',
        'reserveCreation', 'reserveRecovery'].each {
    newRow.getCell(it).editable = true
}

def pos = (currentDataRow != null && !formData.dataRows.isEmpty() ? currentDataRow.getOrder() : formData.dataRows.size)
formData.dataRows.add(pos, newRow)

// поправить значения order
formData.dataRows.eachWithIndex { row, index ->
    row.setOrder(index + 1)
}

// графа 1  - rowNumber
// графа 2  - regNumber
// графа 3  - tradeNumber
// графа 4  - lotSizePrev
// графа 5  - lotSizeCurrent
// графа 6  - reserve
// графа 7  - cost
// графа 8  - signSecurity
// графа 9  - marketQuotation
// графа 10 - costOnMarketQuotation
// графа 11 - reserveCalcValue
// графа 12 - reserveCreation
// графа 13 - reserveRecovery