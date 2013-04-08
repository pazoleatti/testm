/*
* Условие
 */
// проверка на банк
boolean isBank = true
departmentFormTypeService.getDestinations(formData.departmentId, formData.formTemplateId, FormDataKind.SUMMARY).each {
    if (it.departmentId != formData.departmentId) {
        isBank = false
    }
}
return (formDataEvent == FormDataEvent.COMPOSE && isBank) || formDataEvent != FormDataEvent.COMPOSE


/**
 * Расчет (контрольные графы) (calculationControlGraphs.groovy).
 * Форма "Сводная форма начисленных расходов (расходы сложные)".
 *
 * @author auldanov
 * @author rtimerbaev
 * @since 21.03.2013 14:00
 */

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
double summ(FormData formData, Range summRange, filter) {
    Rect summRect = summRange.getRangeRect(formData)
    Rect condRange = summRange.getRangeRect(formData)
    //noinspection GroovyAssignabilityCheck
    if (!summRect.isSameSize(condRange))
        throw new IllegalArgumentException(NOT_SAME_RANGES)

    double sum = 0
    List<DataRow> summRows = formData.getDataRows()
    List<Column> summCols = formData.getFormColumns()
    List<DataRow> condRows = formData.getDataRows()
    List<Column> condCols = formData.getFormColumns()
    for (int i = 0; i < condRange.getHeight(); i++) {
        for (int j = 0; j < condRange.getWidth(); j++) {
            Object condValue = condRows.get(condRange.y1 + i).get(condCols.get(condRange.x1 + j).getAlias())
            if (condValue != null && condValue != 'Требуется объяснение' && condValue != '' && filter(condRows.get(condRange.y1 + i))) {
                BigDecimal summValue = (BigDecimal) summRows.get(summRect.y1 + i).get(summCols.get(summRect.x1 + j).getAlias())
                if (summValue != null) {
                    sum += summValue.doubleValue()
                }
            }
        }
    }
    return sum;
}

/**
 * Обертка предназначенная для прямых вызовов функции без formData.
 */
DataRow getDataRow(String rowAlias) {
    return formData.getDataRow(rowAlias)
}
/**
 * Прямое получения значения ячейки по столбцу и колонке, значение Null воспринимается как 0.
 */
BigDecimal getCellValue(String row, String column) {
    if (getDataRow(row).getCell(column) == null) {
        throw new Exception('Не найдена ячейка')
    }
    return getDataRow(row).getCell(column).getValue() ?: 0;
}

/**
 * Прямое получения ячейки по столбцу и колонке.
 */
Cell getCell(String row, String column) {
    return getDataRow(row).getCell(column)
}

double summ(String columnName, String fromRowA, String toRowA) {
    def result = summ(formData, new ColumnRange(columnName,
            formData.getDataRowIndex(fromRowA), formData.getDataRowIndex(toRowA)))
    return result != null ? result : 0;
}

def row
def a, b, c

// 95 строка
row = formData.getDataRow('R95')
// графа 11
a = summ('consumptionBuhSumAccepted', 'R3', 'R92')
b = getCellValue('R94', 'consumptionBuhSumAccepted')
row.logicalCheck = ((BigDecimal) (a + b)).setScale(2, BigDecimal.ROUND_HALF_UP).toString()
// графа 12
a = summ('consumptionTaxSumS', 'R3', 'R92')
b = getCellValue('R94', 'opuSumByEnclosure3')
c = getCellValue('R93', 'opuSumByEnclosure3')
row.opuSumByEnclosure3 = a + b + c
// графа 13 = графа 11 - графа 6
if (row.logicalCheck != null && row.consumptionBuhSumAccepted != null) {
    row.opuSumByTableP = toBigDecimal(row.logicalCheck) - row.consumptionBuhSumAccepted
} else {
    row.opuSumByTableP = null
}
// графа 16 = графа 12 - графа 9
if (row.opuSumByEnclosure3 != null && row.consumptionTaxSumS != null) {
    row.difference = row.opuSumByEnclosure3 - row.consumptionTaxSumS
} else {
    row.difference = null
}


// 111 строка
row = formData.getDataRow('R111')
// графа 12
a = summ('consumptionTaxSumS', 'R97', 'R109')
b = getCellValue('R110', 'opuSumByEnclosure3')
row.opuSumByEnclosure3 = ((BigDecimal) (a + b)).setScale(2, BigDecimal.ROUND_HALF_UP)
// графа 13
row.opuSumByTableP = getCellValue('R110', 'opuSumByTableP')
// графа 14 = графа 13 - графа 7
if (row.opuSumByTableP != null && row.consumptionBuhSumPrevTaxPeriod != null) {
    row.opuSumTotal = row.opuSumByTableP - row.consumptionBuhSumPrevTaxPeriod
} else {
    row.opuSumTotal = null
}
// графа 16 = графа 12 - графа 9
if (row.opuSumByEnclosure3 != null && row.consumptionTaxSumS != null) {
    row.difference = row.opuSumByEnclosure3 - row.consumptionTaxSumS
} else {
    row.difference = null
}


// 142 строка
row = formData.getDataRow('R142')
// графа 11
a = summ('consumptionBuhSumAccepted', 'R114', 'R139')
b = toBigDecimal(getCell('R141', 'logicalCheck').getValue())
row.logicalCheck = ((BigDecimal) (a + b)).setScale(2, BigDecimal.ROUND_HALF_UP).toString()
// графа 12
a = summ('consumptionTaxSumS', 'R114', 'R139')
b = getCellValue('R141', 'opuSumByEnclosure3')
c = getCellValue('R140', 'opuSumByEnclosure3')
row.opuSumByEnclosure3 = a + b + c
// графа 13 = графа 11 - графа 6
if (row.logicalCheck != null && row.consumptionBuhSumAccepted != null) {
    row.opuSumByTableP = toBigDecimal(row.logicalCheck) - row.consumptionBuhSumAccepted
} else {
    row.opuSumByTableP = null
}
// графа 16 = графа 12 - графа 9
if (row.opuSumByEnclosure3 != null && row.consumptionTaxSumS != null) {
    row.difference = row.opuSumByEnclosure3 - row.consumptionTaxSumS
} else {
    row.difference = null
}

//Для всех строк, для которых графа 11 является редактируемой, за исключением строк: 75 (21514), 76 (21515), 77(21518), 90(21657), 92(21659), 94, 141, 142
([3, 5] + (8..13) + [16] + [19,20,21] + (36..38) + (40..42) + [44] + (46..56) + [58] + (65..68) + [70, 80, 81, 83, 86, 87, 114, 116, 122, 123] + (131..134) + [139]).each {

    column6Range =  new ColumnRange('consumptionBuhSumAccepted', 0, formData.getDataRows().size() - 1)
    summ6Column = summ(formData, column6Range, {condRange ->
        return getCell('R' + it, 'consumptionTypeId').getValue() == condRange.getCell('consumptionTypeId').getValue() && condRange.getCell('consumptionBuhSumAccountNumber').getValue() != null
    })

    column7Range =  new ColumnRange('consumptionBuhSumPrevTaxPeriod', 0, formData.getDataRows().size() - 1)
    summ7Column = summ(formData, column7Range, {condRange ->
        return getCell('R' + it, 'consumptionTypeId').getValue() == condRange.getCell('consumptionTypeId').getValue()
    })

    summResult = ((BigDecimal)(getCellValue('R'+it, 'consumptionTaxSumS') - (summ6Column - summ7Column))).setScale(2, BigDecimal.ROUND_HALF_UP)
    getCell('R'+it, 'logicalCheck').setValue(
            summResult >= 0 ? summResult.toString() : 'Требуется объяснение'
    )
}

/**
 * Изменения в ЧТЗ 28.03.13
 * Строки выделились из вышестоящих
 */
((22..24)+[34,35]).each{
    column6Range =  new ColumnRange('consumptionBuhSumAccepted', 0, formData.getDataRows().size() - 1)
    summ6Column = summ(formData, column6Range, {condRange ->
        return getCell('R' + it, 'consumptionTypeId').getValue() == condRange.getCell('consumptionTypeId').getValue() && condRange.getCell('consumptionBuhSumAccountNumber').getValue() != null
    })

    column7Range =  new ColumnRange('consumptionBuhSumPrevTaxPeriod', 0, formData.getDataRows().size() - 1)
    summ7Column = summ(formData, column7Range, {condRange ->
        return getCell('R' + it, 'consumptionTypeId').getValue() == condRange.getCell('consumptionTypeId').getValue()
    })

    summResult = ((BigDecimal)(getCellValue('R'+it, 'consumptionTaxSumS') - (summ6Column - summ7Column))).setScale(2, BigDecimal.ROUND_HALF_UP)
    getCell('R'+it, 'logicalCheck').setValue(
            summResult <= 0 ? summResult.toString() : 'Требуется объяснение'
    )
}

// Сводная форма начисленных доходов уровня обособленного подразделения (доходы сложные)
def formDataComplexIncome = FormDataService.find(302, formData.kind, formData.departmentId, formData.reportPeriodId)
if (formDataComplexIncome != null) {

    // Строка 75, графа 11
    sum75 = (
    getCellValue('R75', 'consumptionTaxSumS')- (
    formDataComplexIncome.getDataRow('R13').getCell('incomeTaxSumS').getValue() ?: 0 -
            getCellValue('R72', 'consumptionTaxSumS') -
            getCellValue('R76', 'consumptionTaxSumS')
    )
    ).setScale(2, BigDecimal.ROUND_HALF_UP)

    getCell('R75', 'logicalCheck').setValue(sum75 == 0 ? '0' : 'Требуется объяснение')

    // Строка 76, графа 11
    sum76 = (
    getCellValue('R76', 'consumptionTaxSumS')- (
    formDataComplexIncome.getDataRow('R13').getCell('incomeTaxSumS').getValue() ?: 0 -
            getCellValue('R72', 'consumptionTaxSumS') -
            getCellValue('R76', 'consumptionTaxSumS')
    )
    ).setScale(2, BigDecimal.ROUND_HALF_UP)

    getCell('R76', 'logicalCheck').setValue(sum76 == 0 ? '0' : 'Требуется объяснение')

    // Строка 77, графа 11
    sum77 = (
    getCellValue('R77', 'consumptionTaxSumS')- (
    formDataComplexIncome.getDataRow('R14').getCell('incomeTaxSumS').getValue() ?: 0 +
            getCellValue('R73', 'consumptionTaxSumS')
    )
    ).setScale(2, BigDecimal.ROUND_HALF_UP)

    getCell('R77', 'logicalCheck').setValue(sum77 == 0 ? '0' : 'Требуется объяснение')

    // Строка 90, графа 11
    sum90 = (
    getCellValue('R90', 'consumptionTaxSumS')- (
    (formDataComplexIncome.getDataRow('R9').getCell('incomeTaxSumS').getValue() ?: 0) -
            (formDataComplexIncome.getDataRow('R10').getCell('incomeTaxSumS').getValue() ?: 0) +
            getCellValue('R92', 'consumptionTaxSumS')
    )
    ).setScale(2, BigDecimal.ROUND_HALF_UP)

    getCell('R90', 'logicalCheck').setValue(sum90 == 0 ? '0' : 'Требуется объяснение')

    // Строка 92, графа 11
    sum92 = (
    getCellValue('R92', 'consumptionTaxSumS')- (
    (formDataComplexIncome.getDataRow('R10').getCell('incomeTaxSumS').getValue() ?: 0) -
            (formDataComplexIncome.getDataRow('R9').getCell('incomeTaxSumS').getValue() ?: 0) +
            getCellValue('R90', 'consumptionTaxSumS')
    )
    ).setScale(2, BigDecimal.ROUND_HALF_UP)

    getCell('R92', 'logicalCheck').setValue(sum92 == 0 ? '0' : 'Требуется объяснение')
} else {
    // Строка 75, графа 11
    getCell('R75', 'logicalCheck').setValue('0')

    // Строка 76, графа 11
    getCell('R76', 'logicalCheck').setValue('0')

    // Строка 77, графа 11
    getCell('R77', 'logicalCheck').setValue('0')

    // Строка 90, графа 11
    getCell('R90', 'logicalCheck').setValue('0')

    // Строка 92, графа 11
    getCell('R92', 'logicalCheck').setValue('0')
}

// получение нф «Сводная форма "Расшифровка видов расходов, учитываемых в простых РНУ" уровня обособленного подразделения» (расходы простые)
def formDataSimpleConsumption = FormDataService.find(304, formData.kind, formData.departmentId, formData.reportPeriodId)

/**
 * Для всех строк, для всех ячеек, обозначенных как вычисляемые.
 */
((3..25) + (34..59) + (65..70) + (80..84) + (86..88) + (114..117) + [122, 123] + (131..139)).each {
    // «графа 12» = сумма значений «графы 6» для тех строк, для которых значение «графы 4» равно значению «графы 4» текущей строки
    getCell('R' + it, 'opuSumByEnclosure3').setValue(
            summ(formData, new ColumnRange('consumptionBuhSumAccepted', 0, formData.getDataRows().size() - 1), {condRange ->
                return getCell('R' + it, 'consumptionBuhSumAccountNumber').getValue() == condRange.getCell('consumptionBuhSumAccountNumber').getValue()
            })
    )

    /*
     * «графа 13» = сумма значений «графы 8» формы «Сводная форма 'Расшифровка видов расходов, учитываемых в простых РНУ' уровня обособленного подразделения»
     * (см. раздел 6.1.4) для тех строк, для которых значение «графы 4»  равно значению «графы 4» текущей строки текущей формы.
     */
    if (formDataSimpleConsumption != null) {
        getCell('R' + it, 'opuSumByTableP').setValue(
                summ(formDataSimpleConsumption, new ColumnRange('rnu5Field5Accepted', 0, formDataSimpleConsumption.getDataRows().size() - 1), {condRange ->
                    return getCell('R' + it, 'consumptionBuhSumAccountNumber').getValue() == condRange.getCell('consumptionAccountNumber').getValue()
                })
        )
    } else {
        getCell('R' + it, 'opuSumByTableP').setValue(0)
    }

    //«графа 14» = «графа12» + «графа13»
    getCell('R' + it, 'opuSumTotal').setValue(getCellValue('R' + it, 'opuSumByEnclosure3') + getCellValue('R' + it, 'opuSumByTableP'))

    // «графа15»
    def tmpValue15 = income102Dao.getIncome102(formData.reportPeriodId, getCell('R' + it, 'consumptionBuhSumAccountNumber').toString().substring(8), formData.departmentId)
    if (tmpValue15 == null || tmpValue15.totalSum == null) {
        logger.info("Нет информации в отчётах о прибылях и убытках")
        getCell('R' + it, 'opuSumByOpu').setValue(0)
    } else {
        getCell('R' + it, 'opuSumByOpu').setValue(tmpValue15.totalSum)
    }

    // «графа 16» = «графа14» - «графа15»
    getCell('R' + it, 'difference').setValue(
            getCellValue('R' + it, 'opuSumTotal') + getCellValue('R' + it, 'opuSumByOpu')
    )
}

(75..77).each {
    // «графа15»
    def tmpValue15 = income102Dao.getIncome102(formData.reportPeriodId, getCell('R' + it, 'consumptionBuhSumAccountNumber').toString().substring(8), formData.departmentId)
    if (tmpValue15 == null || tmpValue15.totalSum == null) {
        logger.info("Нет информации в отчётах о прибылях и убытках")
        getCell('R' + it, 'opuSumByOpu').setValue(0)
    } else {
        getCell('R' + it, 'opuSumByOpu').setValue(tmpValue15.totalSum)
    }
}

/**
 * Получить число из строки.
 */
def toBigDecimal(String value) {
    if (value == null) {
        return new BigDecimal(0)
    }
    def result
    try {
        result = new BigDecimal(Double.parseDouble(value))
    } catch (NumberFormatException e){
        result = new BigDecimal(0)
    }
    return result
}