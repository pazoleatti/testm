/**
 * Скрипт для подсчета строки ИТОГО.
 * формы "Расчет суммы налога по каждому транспортному средству".
 *
 * @author mfayzullin
 */

// подготовка названии колонок по которым будут производиться подсчеты
def columns = ['calculatedTaxSum', 'benefitSum', 'taxSumToPay']
def sums = []

// подсчет сумм
def rowCount = formData.dataRows.size();
if (rowCount > 0) {
    columns.collect(sums) {
        [it, summ(formData, new ColumnRange(it, 0, rowCount - 1))]
    }
}

// добавление строки ИТОГО
def totalRow = formData.appendDataRow('total');
//totalRow.setManagedByScripts(true)
totalRow.tsType = 'ИТОГО:'

// вставка подсчитанных сумм в строку ИТОГО
sums.each {
    totalRow[it[0]] = it[1]
}