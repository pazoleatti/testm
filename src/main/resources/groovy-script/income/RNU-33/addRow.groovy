/**
 * Скрипт для добавления новой строки (addRow.groovy).
 * Форма "(РНУ-33) Регистр налогового учёта процентного дохода и финансового результата от реализации (выбытия) ГКО".
 *
 * @author rtimerbaev
 */

def newRow = new DataRow(formData.getFormColumns(), formData.getFormStyles())

// графа 2..14, 16, 18..20
['code', 'valuablePaper', 'issue', 'purchaseDate', 'implementationDate',
        'bondsCount','purchaseCost', 'costs', 'redemptionVal', 'exercisePrice',
        'exerciseRuble', 'marketPricePercent', 'marketPriceRuble',
        'costsRetirement', 'parPaper', 'averageWeightedPricePaper', 'issueDays'].each {
    newRow.getCell(it).editable = true
}
def pos = (currentDataRow != null ? currentDataRow.getOrder() : formData.dataRows.size)
formData.dataRows.add(pos, newRow)

// поправить значения order
formData.dataRows.eachWithIndex { row, index ->
    row.setOrder(index + 1)
}

// графа 1  - rowNumber
// графа 2  - code
// графа 3  - valuablePaper
// графа 4  - issue
// графа 5  - purchaseDate
// графа 6  - implementationDate
// графа 7  - bondsCount
// графа 8  - purchaseCost
// графа 9  - costs
// графа 10 - redemptionVal
// графа 11 - exercisePrice
// графа 12 - exerciseRuble
// графа 13 - marketPricePercent
// графа 14 - marketPriceRuble
// графа 15 - exercisePriceRetirement
// графа 16 - costsRetirement
// графа 17 - allCost
// графа 18 - parPaper
// графа 19 - averageWeightedPricePaper
// графа 20 - issueDays
// графа 21 - tenureSkvitovannymiBonds
// графа 22 - interestEarned
// графа 23 - profitLoss
// графа 24 - excessOfTheSellingPrice