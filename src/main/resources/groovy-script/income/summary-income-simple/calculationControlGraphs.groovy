/**
 * Расчет контрольных граф НФ (calculationControlGraphs.groovy).
 * Форма "Расшифровка видов доходов, учитываемых в простых РНУ (доходы простые)".
 *
 *
 * @author auldanov
 * @author rtimerbaev
 * @since 19.03.2013 12:00
 * @version 14 (05.03.2013)
 */

/**
 * Выполнена в виде хелпера, учитывая принцип DRY.
 *
 * v0.1
 */

// обертка предназначенная для прямых вызовов функции без formData
DataRow getDataRow(String rowAlias) {
    return formData.getDataRow(rowAlias)
}

// обертка предназначенная для прямых вызовов функции без formData
int getDataRowIndex(String rowAlias) {
    return formData.getDataRowIndex(rowAlias)
}

// прямое получения ячейки по столбцу и колонке
Cell getCell(String row, String column) {
    return getDataRow(row).getCell(column)
}

// обертка предназначенная для прямых вызовов функции без formData
BigDecimal summ(ColumnRange cr) {
    return summ(formData, cr, cr, {return true;})
}

// прямое получения значения ячейки по столбцу и колонке, значение Null воспринимается как 0
BigDecimal getCellValue(String row, String column) {
    if (getDataRow(row).getCell(column) == null) {
        throw new Exception('Не найдена ячейка')
    }
    return getDataRow(row).getCell(column).getValue() ?: 0;
}

/**
 * Суммирует ячейки второго диапазона только для тех строк, для которых выполняется условие фильтрации. В данном
 * случае под условием фильтрации подразумевается равенство значений строк первого диапазона заранее заданному
 * значению. Является аналогом Excel функции 'СУММЕСЛИ' в нотации 'СУММЕСЛИ(диапазон, критерий, диапазон_суммирования)'
 * @see <a href='http://office.microsoft.com/ru-ru/excel-help/HP010342932.aspx?CTT=1'>СУММЕСЛИ(диапазон, критерий, [диапазон_суммирования])</a>
 *
 * @param formData таблица данных
 * @param conditionRange диапазон по которому осуществляется отбор строк (фильтрация)
 * @param filterValue значение фильтра
 * @param summRange диапазон суммирования
 * @return сумма ячеек
 */
double summ(FormData formData, Range conditionRange, Range summRange, filter) {

    Rect summRect = summRange.getRangeRect(formData)
    Rect condRange = conditionRange.getRangeRect(formData)
    if (!summRect.isSameSize(condRange))
        throw new IllegalArgumentException(NOT_SAME_RANGES)

    double sum = 0;
    List<DataRow> summRows = formData.getDataRows()
    List<Column> summCols = formData.getFormColumns()
    List<DataRow> condRows = formData.getDataRows()
    List<Column> condCols = formData.getFormColumns()
    for (int i = 0; i < condRange.getHeight(); i++) {
        for (int j = 0; j < condRange.getWidth(); j++) {
            Object condValue = condRows.get(condRange.y1 + i).get(condCols.get(condRange.x1 + j).getAlias());
            if (condValue != null && condValue != 'Требуется объяснение' && condValue != '' && filter(condRows.get(condRange.y1 + i))) {
                BigDecimal summValue = (BigDecimal) summRows.get(summRect.y1 + i).get(summCols.get(summRect.x1 + j).getAlias());
                if (summValue != null) {
                    sum += summValue.doubleValue()
                }
            }
        }
    }
    return sum;
}

def specialNotation = 'Требуется объяснение'

// графа 11 [1]
([3, 4, 7, 8, 9, 10, 12] + (13..30) + (33..36) + [38, 40, 44, 47, 62, 63, 85, 92, 93, 104, 114, 115, 197, 200, 201, 202, 204, 205]).each {
    summ = ((BigDecimal)(getCellValue('R' + it, 'rnu6Field10Sum') - (getCellValue('R'+it, 'rnu6Field12Accepted') - getCellValue('R' + it, 'rnu6Field12PrevTaxPeriod')))).setScale(2, BigDecimal.ROUND_HALF_UP)
    getCell('R' + it, 'logicalCheck').setValue(summ > 0 ? summ : specialNotation)
}

// графа 11 [2]
([31,32, 37, 56, 61] + (64..70) + (78..84) + [90, 91] + (98..103) + (105..109) + [111, 112, 113] + (116..164) + (170..199) + [203, 213, 214, 217]).each {
    summ = ((BigDecimal)(getCellValue('R' + it, 'rnu4Field5PrevTaxPeriod'))).setScale(2, BigDecimal.ROUND_HALF_UP)
    getCell('R' + it, 'logicalCheck').setValue(summ > 0 ? summ : specialNotation)
}

// графа 11 [3]
([5,6, 11, 39, 41, 42, 43, 45, 46] + (48..55) + (57..77) + [86, 110, 206, 207, 208]).each {
    summ = ((BigDecimal)(getCellValue('R' + it, 'rnu6Field10Sum') - getCellValue('R' + it, 'rnu4Field5PrevTaxPeriod'))).setScale(2, BigDecimal.ROUND_HALF_UP)
    getCell('R' + it, 'logicalCheck').setValue(summ > 0 ? summ : specialNotation)
}

// получение данных из доходов сложных (302) для вычисления 12 и 13 графы
def formData302 = FormDataService.find(302, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
if (formData302 != null) {
    ((3..86) + (90..93) + (98..164) + (170..208) + [213, 214, 217]).each {
        // графа 12
        ColumnRange columnRange6 = new ColumnRange('incomeBuhSumAccepted', 0, formData302.getDataRows().size() - 1)
        Double sum6column = summ(formData302, columnRange6, columnRange6, { condRange ->
            return getCell('R' + it, 'accountNo').getValue() == condRange.getCell('incomeBuhSumAccountNumber').getValue()
        })
        getCell('R' + it, 'opuSumByEnclosure2').setValue(sum6column)

        // графа 13
        columnRange9 = new ColumnRange('incomeTaxSumS', 0, formData302.getDataRows().size() - 1)
        Double sum9column = summ(formData302, columnRange6, columnRange6, { condRange ->
            return getCell('R' + it, 'accountNo').getValue() == condRange.getCell('incomeBuhSumAccountNumber').getValue()
        })
        getCell('R' + it, 'opuSumByTableD').setValue(sum9column)
    }
} else {
    // если источников нет то зануляем поля в которые должны были перетянуться данные
    ((3..86) + (90..93) + (98..164) + (170..208) + [213, 214, 217]).each {
        // графа 12
        getCell('R' + it, 'opuSumByEnclosure2').setValue(0)

        // графа 13
        getCell('R' + it, 'opuSumByTableD').setValue(0)
    }
}

// графы 12, 13, 14, 15, 16
((3..86) + (90..93) + (98..164) + (170..208) + [213, 214, 217]).each {
    // графа 14
    getCell('R' + it, 'opuSumTotal').setValue(
            getCellValue('R' + it, 'opuSumByEnclosure2') + getCellValue('R' + it, 'opuSumByTableD')
    )

    // графа 15
    def bVal = new StringBuffer(getCell('R'+it, 'accountNo').value)
    bVal.delete(1,8)
    def data = income102Dao.getIncome102(formData.reportPeriodId, bVal.toString(), formData.departmentId)
    if (data == null)
        logger.warn("Не найдены соответствующие данные в отчете о прибылях и убытках")
    getCell('R'+it, 'opuSumByOpu').setValue(data ? data.creditRate: 0)


    // графа 16
    getCell('R' + it, 'difference').setValue(
            getCellValue('R' + it, 'opuSumByOpu') - getCellValue('R' + it, 'rnu4Field5Accepted')
    )
}

// Графа 15 и 16
(165..169).each {
    // графа 15
    def bVal = new StringBuffer(getCell('R'+it, 'accountNo').value)
    bVal.delete(4,5)
    def data = income101Dao.getIncome101(formData.reportPeriodId, bVal.toString(), formData.departmentId)
    if (data == null)
        logger.warn("Не найдены соответствующие данные в оборотной ведомости")
    getCell('R'+it, 'opuSumByOpu').setValue(data ? data.incomeDebetRemains : 0)
    // графа 16
    getCell('R' + it, 'difference').setValue(
            getCellValue('R' + it, 'opuSumByOpu') - getCellValue('R' + it, 'rnu4Field5Accepted')
    )
}


// Графа 15 Строка 209
bVal = new StringBuffer(getCell('R209', 'accountNo').value)
bVal.delete(4,5)
def data209x15 = income101Dao.getIncome101(formData.reportPeriodId, bVal.toString(), formData.departmentId)
if (data209x15 == null)
    logger.warn("Не найдены соответствующие данные в оборотной ведомости")
getCell('R209', 'opuSumByOpu').setValue(data209x15 ? data209x15.debetRate : 0)
// Графа 16  Строка 209 («графа 16» = «графа 15» - «графа 7»)
getCell('R209', 'difference').setValue(
        getCellValue('R209', 'opuSumByOpu') - getCellValue('R209', 'rnu6Field12Accepted')
)


// Графа 15 (Строка 220)
bVal = new StringBuffer(getCell('R220', 'accountNo').value)
bVal.delete(4,5)
def data220x15 = income101Dao.getIncome101(formData.reportPeriodId, bVal.toString(), formData.departmentId)
if (data220x15 == null)
    logger.warn("Не найдены соответствующие данные в оборотной ведомости")
getCell('R220', 'opuSumByOpu').setValue(data220x15 ? data220x15.debetRate : 0)
/*
 * Графа 16 строка 220
 * «графа 16» = «графа 15» - ( А+ Б)
 * А – значение «графы 9» для строки 220
 * Б – значение «графы 9» для строки 221
*/
getCell('R220', 'difference').setValue(
        getCellValue('R220', 'rnu4Field5Accepted') - getCellValue('R221', 'rnu4Field5Accepted')
)

