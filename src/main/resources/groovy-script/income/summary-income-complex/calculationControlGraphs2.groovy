import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.Column
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.FormData
import com.aplana.sbrf.taxaccounting.model.script.range.ColumnRange
import com.aplana.sbrf.taxaccounting.model.script.range.Range
import com.aplana.sbrf.taxaccounting.model.script.range.Rect

//com.aplana.sbrf.taxaccounting.model.FormData formData
//com.aplana.sbrf.taxaccounting.dao.script.Income102Dao income102Dao
//com.aplana.sbrf.taxaccounting.dao.script.Income101Dao income101Dao

/**
 * Расчет (контрольные графы) (calculationControlGraphs2.groovy).
 * Реализована как 2 часть Табл. 8 Расчет контрольных граф Сводной формы начисленных доходов
 * (строки начинаются с Контрольная сумма и до конца таблицы).
 * Форма "Сводная форма начисленных доходов (доходы сложные)".
 *
 * @author auldanov
 * @since 22.03.2013 15:30
 */

/**
 * Обертка предназначенная для прямых вызовов функции без formData.
 */
DataRow getDataRow(String rowAlias) {
    return formData.getDataRow(rowAlias)
}

/**
 * Обертка предназначенная для прямых вызовов функции без formData.
 */
int getDataRowIndex(String rowAlias) {
    return formData.getDataRowIndex(rowAlias)
}

/**
 * Прямое получения ячейки по столбцу и колонке.
 */
Cell getCell(String row, String column) {
    return getDataRow(row).getCell(column)
}

/**
 * Обертка предназначенная для прямых вызовов функции без formData.
 */
BigDecimal summ(ColumnRange cr) {
    return summ(formData, cr, cr, { return true; })
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

def specialNotation = 'Требуется объяснение'

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

    double sum = 0
    List<DataRow> summRows = formData.getDataRows()
    List<Column> summCols = formData.getFormColumns()
    List<DataRow> condRows = formData.getDataRows()
    List<Column> condCols = formData.getFormColumns()
    for (int i = 0; i < condRange.getHeight(); i++) {
        for (int j = 0; j < condRange.getWidth(); j++) {
            Object condValue = condRows.get(condRange.y1 + i).get(condCols.get(condRange.x1 + j).getAlias())
            if (condValue != null && condValue != 'Требуется объяснение' && condValue != '' && filter(condRows.get(condRange.y1 + i))) {
                def summValue = summRows.get(summRect.y1 + i).get(summCols.get(summRect.x1 + j).getAlias())
                if (summValue != null) {
                    BigDecimal temp = new BigDecimal(0);
                    if (summValue instanceof String) {
                        temp = new BigDecimal(summValue.replace(',', '.'))
                    } else {
                        temp = summValue
                    }
                    sum += temp.doubleValue()
                }
            }
        }
    }
    return sum
}

/**
 * Получить число из строки.
 */
def toBigDecimal(String value) {
    def result
    try {
        result = new BigDecimal(Double.parseDouble(value))
    } catch (NumberFormatException e) {
        result = new BigDecimal(0)
    }
    return result
}

// ----Раздел А1
// графа 11
def tmpLogicalCheck = toBigDecimal(getCell('R21', 'logicalCheck').getValue())
def tmpValue = ((BigDecimal) summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R3'), getDataRowIndex('R19'))) + tmpLogicalCheck).setScale(2, BigDecimal.ROUND_HALF_UP)
getCell('R22', 'logicalCheck').setValue(tmpValue.toString())

// графа 12
tmp = summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R3'), getDataRowIndex('R19')))
getCell('R22', 'opuSumByEnclosure2').setValue(
        tmp + getCellValue('R20', 'opuSumByEnclosure2')
                + getCellValue('R21', 'opuSumByEnclosure2')
)
//logger.info ('tmp = ' + tmp.toString() + ' 20 = ' + getCellValue('R20', 'opuSumByEnclosure2').toString() + ' 21 = ' + getCellValue('R21', 'opuSumByEnclosure2').toString())
// графа 13
getCell('R22', 'opuSumByTableD').setValue(tmpValue - (getCell('R22', 'incomeBuhSumAccepted').getValue() ?: 0))

// графа 16
getCell('R22', 'difference').setValue(
        substract(getCell('R22', 'opuSumByEnclosure2'), getCell('R22', 'incomeTaxSumS'))
)

// Раздел Б1
// графа 11
tmpValue = toBigDecimal(getCell('R39', 'logicalCheck').getValue()).setScale(2, BigDecimal.ROUND_HALF_UP)
getCell('R40', 'logicalCheck').setValue(tmpValue.toString())
// графа 12
getCell('R40', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R24'), getDataRowIndex('R37'))) + getCellValue('R38', 'opuSumByEnclosure2')
                + getCellValue('R39', 'opuSumByEnclosure2')
)
// графа 13
getCell('R40', 'opuSumByTableD').setValue(tmpValue - (getCell('R40', 'incomeBuhSumAccepted').getValue() ?: 0))

// графа 16
getCell('R40', 'difference').setValue(
        substract(getCell('R40', 'opuSumByEnclosure2'), getCell('R40', 'incomeTaxSumS'))
)

// --Раздел А2
// графа 11
tmpLogicalCheck = toBigDecimal(getCell('R79', 'logicalCheck').getValue())
tmpValue = ((BigDecimal) summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R43'), getDataRowIndex('R77'))) + tmpLogicalCheck).setScale(2, BigDecimal.ROUND_HALF_UP)
getCell('R80', 'logicalCheck').setValue(tmpValue.toString())
// графа 12
getCell('R80', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R43'), getDataRowIndex('R77'))) + getCellValue('R78', 'opuSumByEnclosure2')
                + getCellValue('R79', 'opuSumByEnclosure2')
)
// графа 13
getCell('R80', 'opuSumByTableD').setValue(tmpValue - (getCell('R80', 'incomeBuhSumAccepted').getValue() ?: 0))

// графа 16
getCell('R80', 'difference').setValue(
        substract(getCell('R80', 'opuSumByEnclosure2'), getCell('R80', 'incomeTaxSumS'))
)

// --- Раздел Б2
// графа 12
getCell('R96', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R82'), getDataRowIndex('R94'))) + getCellValue('R95', 'opuSumByEnclosure2')
)
// графа 16
getCell('R96', 'difference').setValue(
        substract(getCell('R96', 'opuSumByEnclosure2'), getCell('R96', 'incomeTaxSumS'))
)

// Раздел В
// графа 11
tmpValue = summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R98'), getDataRowIndex('R103')))
getCell('R104', 'logicalCheck').setValue(tmpValue.toString())

// графа 12
getCell('R104', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R98'), getDataRowIndex('R103')))
)
// графа 13
getCell('R104', 'opuSumByTableD').setValue(tmpValue - (getCell('R104', 'incomeBuhSumAccepted').getValue() ?: 0))

// графа 16
getCell('R104', 'difference').setValue(
        substract(getCell('R104', 'opuSumByEnclosure2'), getCell('R104', 'incomeTaxSumS'))
)

// --- Раздел Г
// графа 11
tmpValue = summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R106'), getDataRowIndex('R109')))
getCell('R110', 'logicalCheck').setValue(tmpValue.toString())

// графа 12
getCell('R110', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R106'), getDataRowIndex('R109')))
)
// графа 13
getCell('R110', 'opuSumByTableD').setValue(tmpValue - (getCell('R110', 'incomeBuhSumAccepted').getValue() ?: 0))

// графа 16
getCell('R110', 'difference').setValue(
        substract(getCell('R110', 'opuSumByEnclosure2'), getCell('R110', 'incomeTaxSumS'))
)

// --Раздел Д
// графа 12
getCell('R117', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R112'), getDataRowIndex('R115'))) + getCellValue('R116', 'opuSumByEnclosure2')
)
// графа 16
getCell('R117', 'difference').setValue(
        substract(getCell('R117', 'opuSumByEnclosure2'), getCell('R117', 'incomeTaxSumS'))
)

// --Раздел Е
// графа 12
getCell('R120', 'opuSumByEnclosure2').setValue(
        getCellValue('R119', 'opuSumByEnclosure2')
)
// графа 16
getCell('R120', 'difference').setValue(
        substract(getCell('R120', 'opuSumByEnclosure2'), getCell('R120', 'incomeTaxSumS'))
)

// Раздел Ж
// графа 11
tmpValue = summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R122'), getDataRowIndex('R127')))
getCell('R128', 'logicalCheck').setValue(tmpValue.toString())

// графа 12
getCell('R128', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R122'), getDataRowIndex('R127')))
)
// графа 13
getCell('R128', 'opuSumByTableD').setValue(tmpValue - (getCell('R128', 'incomeBuhSumAccepted').getValue() ?: 0))

// графа 16
getCell('R128', 'difference').setValue(
        substract(getCell('R128', 'opuSumByEnclosure2'), getCell('R128', 'incomeTaxSumS'))
)

// раздел где много строк для графы 11
for (row in ['R4', 'R5', 'R7', 'R19', 'R43', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R64', 'R70', 'R72', 'R73', 'R74', 'R75', 'R76', 'R77', 'R98', 'R106', 'R122']) {

    // Очень ритуальный подсчёт суммы из за возможности объединения ячеек и проблемы сущетсвующий что у колонки с которой объединяем значение null
    sum6column = 0
    prev = 'not use null please :)'
    for (rowForSumm in formData.dataRows) {
        if (rowForSumm.incomeBuhSumAccountNumber != null
                && rowForSumm.incomeBuhSumAccepted != null
                && ((rowForSumm.incomeTypeId != null && rowForSumm.incomeTypeId == getCell(row, 'incomeTypeId').getValue())
                || (rowForSumm.incomeTypeId == null && prev == getCell(row, 'incomeTypeId').getValue()))) {
            prev = getCell(row, 'incomeTypeId').getValue()
            sum6column += rowForSumm.incomeBuhSumAccepted
        } else {
            prev = 'not use null please :)'
        }
    }

    // тот же магический подсчёт
    sum7column = 0
    prev = 'not use null please :)'
    for (rowForSumm in formData.dataRows) {
        if (rowForSumm.incomeBuhSumAccountNumber != null
                && rowForSumm.incomeBuhSumPrevTaxPeriod != null
                && ((rowForSumm.incomeTypeId != null && rowForSumm.incomeTypeId == getCell(row, 'incomeTypeId').getValue())
                || (rowForSumm.incomeTypeId == null && prev == getCell(row, 'incomeTypeId').getValue()))) {
            prev = getCell(row, 'incomeTypeId').getValue()
            sum7column += rowForSumm.incomeBuhSumPrevTaxPeriod
        } else {
            prev = 'not use null please :)'
        }
    }

    /*ColumnRange columnRange6 = new ColumnRange('incomeBuhSumAccepted', 0, formData.getDataRows().size() - 1)
    Double sum6column = summ(formData, columnRange6, columnRange6, { condRange ->
        return getCell(it, 'incomeTypeId').getValue() == condRange.getCell('incomeTypeId').getValue() && condRange.getCell('incomeBuhSumAccountNumber').getValue() != null
    })*/

    /*ColumnRange columnRange7 = new ColumnRange('incomeBuhSumPrevTaxPeriod', 0, formData.getDataRows().size() - 1)
    Double sum7column = summ(formData, columnRange7, columnRange7, { condRange ->
        return getCell(row, 'incomeTypeId').getValue() == condRange.getCell('incomeTypeId').getValue() && condRange.getCell('incomeBuhSumAccountNumber').getValue() != null
    })  */

    //logger.info(String.valueOf(row + " sum6 = " + sum6column))
    //logger.info(row + " sum6 = " + sum7column)
    def val = getCellValue(row, 'incomeTaxSumS') - (sum6column - sum7column)

    // другой вариант округления
    val = ((BigDecimal) val).setScale(2, BigDecimal.ROUND_HALF_UP)
    //val = round(val, 2)

    getCell(row, 'logicalCheck').setValue(
            val >= 0 ? val.toString() : specialNotation
    )
}

// получение данных из расходов сложных
def formDataComplexConsumption = FormDataService.find(303, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
temp2 = 0
temp3 = 0
if (formDataComplexConsumption != null) {
    temp2 = formDataComplexConsumption.getDataRow('R90').getCell('consumptionTaxSumS').getValue()
    temp3 = formDataComplexConsumption.getDataRow('R92').getCell('consumptionTaxSumS').getValue()
}
// строка 9 графа 11
temp = ((BigDecimal) (getCellValue('R9', 'incomeTaxSumS')
        - (getCellValue('R10', 'incomeTaxSumS') - temp3 + temp2)
).setScale(2, BigDecimal.ROUND_HALF_UP))
getCell('R9', 'logicalCheck').setValue(temp == 0 ? '0' : 'Требуется объяснение')

// строка 10 графа 11
temp = ((BigDecimal) (getCellValue('R10', 'incomeTaxSumS')
        - (getCellValue('R9', 'incomeTaxSumS') - temp2 + temp3)
).setScale(2, BigDecimal.ROUND_HALF_UP))
getCell('R10', 'logicalCheck').setValue(temp == 0 ? '0' : 'Требуется объяснение')

/**
 * Все оставшиеся строки, не описанные выше в этой таблице.
 */
// получение данных из доходов простых
def formDataSimpleIncome = FormDataService.find(301, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)

List<DataRow> dataRows = formData.getDataRows()
for (DataRow row : dataRows) {
    // проверка что строка не описана выше
    if (['R4', 'R5', 'R6', 'R7', 'R8', 'R19', 'R43',
            'R44', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R51',
            'R70', 'R71', 'R72', 'R73', 'R74', 'R75', 'R76', 'R77', 'R64',
            'R98', 'R99', 'R100', 'R106', 'R107',
            'R122', 'R123', 'R124'
    ].contains(row.getAlias())) {

        // графа 12
        summ = 0
        for(rowData in formData.dataRows) {
            if (rowData.incomeBuhSumAccepted != null && row.incomeBuhSumAccountNumber == rowData.incomeBuhSumAccountNumber) {
                summ += rowData.incomeBuhSumAccepted
            }
        }

        row.getCell('opuSumByEnclosure2').setValue(summ);

        /*ColumnRange columnRange6 = new ColumnRange('incomeBuhSumAccepted', 0, formData.getFormColumns().size() - 1)
        row.getCell('opuSumByEnclosure2').setValue(
                summ(formData, columnRange6, columnRange6, { condRange ->
                    return row.getCell('incomeTypeId').getValue() == condRange.getCell('incomeTypeId').getValue()
                })
        )*/
    }
    if (['R4', 'R5', 'R6', 'R7', 'R8', 'R19', 'R43', 'R64',
            'R44', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R51', 'R54', 'R55', 'R56', 'R57', 'R58', 'R59',
            'R70', 'R71', 'R72', 'R73', 'R74', 'R75', 'R76', 'R77',
            'R98', 'R99', 'R100', 'R106', 'R107', 'R122', 'R123', 'R124'
    ].contains(row.getAlias())) {
        // графа 15
        temp = income102Dao.getIncome102(formData.reportPeriodId, row.incomeBuhSumAccountNumber.toString().substring(8), formData.departmentId)
        if (temp == null) {
            logger.info("Нет информации о в отчётах о прибылях и убытках")
            row.getCell('opuSumByOpu').setValue(0)
        } else {
            row.getCell('opuSumByOpu').setValue(temp.totalSum)
        }
    }
    if (['R4', 'R5', 'R6', 'R7', 'R8', 'R19', 'R43', 'R64',
            'R44', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R51', 'R54', 'R55', 'R56', 'R57', 'R58', 'R59',
            'R70', 'R71', 'R72', 'R73', 'R74', 'R75', 'R76', 'R77', 'R128',
            'R98', 'R99', 'R100', 'R106', 'R107', 'R122', 'R123', 'R124'
    ].contains(row.getAlias())) {
        // графа 16
        row.getCell('difference').setValue(getCellValue(row.getAlias(), 'opuSumTotal') - getCellValue(row.getAlias(), 'opuSumByOpu'))
    }
}

// строки 54-59, графы 13,14, 15, 16
['R54', 'R55', 'R56', 'R57', 'R58', 'R59'].each {
    value = getCell(it, 'incomeBuhSumAccountNumber').getValue().toString()
    account = value.substring(0, 3) + value.substring(4)
    temp = income101Dao.getIncome101(formData.reportPeriodId, account, formData.departmentId)
    if (temp == null) {
        logger.info("Нет данных о оборотной ведомости")
        getCell(it, 'opuSumByTableD').setValue(0)
        getCell(it, 'opuSumTotal').setValue(0)
    } else {
        getCell(it, 'opuSumByTableD').setValue(temp.incomeDebetRemains)
        getCell(it, 'opuSumTotal').setValue(temp.outcomeDebetRemains)
    }
    getCell(it, 'opuSumByOpu').setValue((BigDecimal) getCell(it, 'opuSumByTableD').getValue() - (BigDecimal) getCell(it, 'opuSumTotal').getValue())
    getCell(it, 'difference').setValue((getCell(it, 'opuSumByOpu').getValue() ?: 0) - (getCell(it, 'incomeTaxSumS').getValue() ?: 0))

}

// строки 3-53, 60-131
((4..8) + (19) + (43..51) + (64) + (70..77) + (98..100) + (106..107) + (122..124)).each {
    def thisRow = formData.getDataRow("R" + it)

    if (formDataSimpleIncome != null) {
        // графа 13
        columnRange9 = new ColumnRange('rnu4Field5Accepted', 0, formDataSimpleIncome.getDataRows().size() - 1)
        thisRow.opuSumByTableD = summ(formDataSimpleIncome, columnRange9, columnRange9, { condRange ->
            return thisRow.incomeBuhSumAccountNumber == condRange.getCell('accountNo').getValue()
        })
    } else {
        thisRow.opuSumByTableD = 0
    }

    // графа 14
    if (thisRow.opuSumByEnclosure2 != null && thisRow.opuSumByTableD != null) {
        thisRow.opuSumTotal = thisRow.opuSumByEnclosure2 + thisRow.opuSumByTableD
    } else {
        thisRow.opuSumTotal = null
    }
}

