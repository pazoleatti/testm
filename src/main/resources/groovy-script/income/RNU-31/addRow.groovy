/**
 * Скрипт для добавления новой строки (addRow.groovy).
 * Форма "(РНУ-31) Регистр налогового учёта процентного дохода по купонным облигациям".
 *
 * @author rtimerbaev
 */

if (!formData.dataRows.isEmpty()) {
    return
}

def newRow = formData.appendDataRow()
newRow.number = 1
newRow.securitiesType = 'Процентный (купонный) доход по облигациям'

// графа 3..12
['ofz', 'municipalBonds', 'governmentBonds', 'mortgageBonds', 'municipalBondsBefore', 'rtgageBondsBefore', 'ovgvz', 'eurobondsRF', 'itherEurobonds', 'corporateBonds'].each {
    newRow.getCell(it).editable = true
}

// графа 1  - number
// графа 2  - securitiesType
// графа 3  - ofz
// графа 4  - municipalBonds
// графа 5  - governmentBonds
// графа 6  - mortgageBonds
// графа 7  - municipalBondsBefore
// графа 8  - rtgageBondsBefore
// графа 9  - ovgvz
// графа 10 - eurobondsRF
// графа 11 - itherEurobonds
// графа 12 - corporateBonds