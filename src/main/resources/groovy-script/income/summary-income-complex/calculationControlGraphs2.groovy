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
 * TODO
 *		1. графа 15 не реализована, так как в ЧТЗ пока не ясность с этим
 *		2. добавить расчет по строкам а) 54-59 б) 3-53, 60-131 (добавились в обновленном ЧТЗ)
 *
 * @author auldanov
 * @since 19.03.2013 12:00
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
    return summ(formData, cr, cr, {return true;})
}

/**
 * Прямое получения значения ячейки по столбцу и колонке, значение Null воспринимается как 0.
 */
BigDecimal getCellValue(String row, String column) {
    if (getDataRow(row).getCell(column)==null) {
        throw new Exception('Не найдена ячейка')
    }
    return getDataRow(row).getCell(column).getValue() ?:0;
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
                BigDecimal summValue = (BigDecimal) summRows.get(summRect.y1 + i).get(summCols.get(summRect.x1 + j).getAlias())
                if (summValue != null) {
                    sum += summValue.doubleValue()
                }
            }
        }
    }
    return sum
}







// ----Раздел А1
// графа 11
getCell('R22', 'logicalCheck').setValue(
        ((BigDecimal) summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R3'), getDataRowIndex('R19'))) + getCellValue('R21', 'logicalCheck')).setScale(2, BigDecimal.ROUND_HALF_UP)
)
// графа 12
getCell('R22', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R3'), getDataRowIndex('R19'))) + getCellValue('R20', 'opuSumByEnclosure2')
                + getCellValue('R21', 'opuSumByEnclosure2')
)
// графа 13
getCell('R22', 'opuSumByTableD').setValue(
        substract(getCell('R22', 'logicalCheck'), getCell('R22', 'incomeBuhSumAccepted'))
)
// графа 16
getCell('R22', 'difference').setValue(
        substract(getCell('R22', 'opuSumByEnclosure2'), getCell('R22', 'incomeTaxSumS'))
)



// Раздел Б1
// графа 11
getCell('R40', 'logicalCheck').setValue(((BigDecimal) getCell('R39', 'logicalCheck').getValue()).setScale(2, BigDecimal.ROUND_HALF_UP))
// графа 12
getCell('R40', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R24'), getDataRowIndex('R37'))) + getCellValue('R38', 'opuSumByEnclosure2')
                + getCellValue('R39', 'opuSumByEnclosure2')
)
// графа 13
getCell('R40', 'opuSumByTableD').setValue(
        substract(getCell('R40', 'logicalCheck'), getCell('R40', 'incomeBuhSumAccepted'))
)
// графа 16
getCell('R40', 'difference').setValue(
        substract(getCell('R40', 'opuSumByEnclosure2'), getCell('R40', 'incomeTaxSumS'))
)


// --Раздел А2
// графа 11
getCell('R80', 'logicalCheck').setValue(
        ((BigDecimal) summ(new ColumnRange('logicalCheck', getDataRowIndex('R43'), getDataRowIndex('R77'))) + getCellValue('R79', 'logicalCheck')).setScale(2, BigDecimal.ROUND_HALF_UP)
)
// графа 12
getCell('R80', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R43'), getDataRowIndex('R77'))) + getCellValue('R78', 'opuSumByEnclosure2')
                + getCellValue('R79', 'opuSumByEnclosure2')
)
// графа 13
getCell('R80', 'opuSumByTableD').setValue(
        substract(getCell('R80', 'logicalCheck'), getCell('R80', 'incomeBuhSumAccepted'))
)
// графа 16
getCell('R80', 'difference').setValue(
        substract(getCell('R80', 'opuSumByEnclosure2'), getCell('R80', 'incomeTaxSumS'))
)



// --- Раздел Б2
// графа 12
getCell('R96', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R82'), getDataRowIndex('R94'))) + getCellValue('R95', 'opuSumByEnclosure2')
)
// графа 16
getCell('R96', 'difference').setValue(
        substract(getCell('R96', 'opuSumByEnclosure2'), getCell('R96', 'incomeTaxSumS'))
)



// Раздел В
// графа 11
getCell('R104', 'logicalCheck').setValue(
        summ(new ColumnRange('logicalCheck', getDataRowIndex('R98'), getDataRowIndex('R103')))
)
// графа 12
getCell('R104', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R98'), getDataRowIndex('R103')))
)
// графа 13
getCell('R104', 'opuSumByTableD').setValue(
        substract(getCell('R104', 'logicalCheck'), getCell('R104', 'incomeBuhSumAccepted'))
)
// графа 16
getCell('R104', 'difference').setValue(
        substract(getCell('R104', 'opuSumByEnclosure2'), getCell('R104', 'incomeTaxSumS'))
)



// --- Раздел Г
// графа 11
getCell('R110', 'logicalCheck').setValue(
        summ(new ColumnRange('logicalCheck', getDataRowIndex('R106'), getDataRowIndex('R109')))
)
// графа 12
getCell('R110', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R106'), getDataRowIndex('R109')))
)
// графа 13
getCell('R110', 'opuSumByTableD').setValue(
        substract(getCell('R110', 'logicalCheck'), getCell('R110', 'incomeBuhSumAccepted'))
)
// графа 16
getCell('R110', 'difference').setValue(
        substract(getCell('R110', 'opuSumByEnclosure2'), getCell('R110', 'incomeTaxSumS'))
)



// --Раздел Д
// графа 12
getCell('R117', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R112'), getDataRowIndex('R115'))) + getCellValue('R116', 'opuSumByEnclosure2')
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
getCell('R128', 'logicalCheck').setValue(
        summ(new ColumnRange('logicalCheck', getDataRowIndex('R122'), getDataRowIndex('R127')))
)
// графа 12
getCell('R128', 'opuSumByEnclosure2').setValue(
        summ(new ColumnRange('incomeBuhSumAccepted', getDataRowIndex('R122'), getDataRowIndex('R127')))
)
// графа 13
getCell('R128', 'opuSumByTableD').setValue(
        substract(getCell('R128', 'logicalCheck'), getCell('R128', 'incomeBuhSumAccepted'))
)
// графа 16
getCell('R128', 'difference').setValue(
        substract(getCell('R128', 'opuSumByEnclosure2'), getCell('R128', 'incomeTaxSumS'))
)



// раздел где много строк для графы 11
['R4', 'R5', 'R7', 'R19', 'R43', 'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R64', 'R70', 'R72', 'R73', 'R74', 'R75', 'R76', 'R77', 'R98', 'R106', 'R122'].each{

    ColumnRange columnRange6 = new ColumnRange('incomeBuhSumAccepted', 0, formData.getDataRows().size() - 1)
    Double sum6column = summ(formData, columnRange6, columnRange6, { condRange ->
        return getCell(it, 'incomeTypeId').getValue() == condRange.getCell('incomeTypeId').getValue() && condRange.getCell('incomeBuhSumAccountNumber').getValue() != null
    })

    ColumnRange columnRange7 = new ColumnRange('incomeBuhSumPrevTaxPeriod', 0, formData.getDataRows().size() -1)
    Double sum7column = summ(formData, columnRange7, columnRange7, { condRange ->
        return getCell(it, 'incomeTypeId').getValue() == condRange.getCell('incomeTypeId').getValue() && condRange.getCell('incomeBuhSumAccountNumber').getValue() != null
    })

    def val = getCellValue(it, 'incomeTaxSumS') - (sum6column - sum7column)

    // другой вариант округления
    val = ((BigDecimal) val).setScale(2, BigDecimal.ROUND_HALF_UP)
    //val = round(val, 2)

    getCell(it, 'logicalCheck').setValue(
            val >= 0 ? val: specialNotation
    )
}


// получение данных из расходов сложных
def formDataComplexConsumption = FormDataService.find(303, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)
if (formDataComplexConsumption != null) {
    // строка 9 графа 11
    getCell('R9', 'logicalCheck').setValue(
            (BigDecimal)(getCellValue('R9', 'incomeTaxSumS')
                    - (getCellValue('R10', 'incomeTaxSumS') - (formDataComplexConsumption.getDataRow('R92').getCell('consumptionTaxSumS').getValue() ?:0) + (formDataComplexConsumption.getDataRow('R90').getCell('consumptionTaxSumS').getValue() ?:0))
            ).setScale(2, BigDecimal.ROUND_HALF_UP)
    )


    // строка 10 графа 11
    getCell('R10', 'logicalCheck').setValue(
            (BigDecimal)(getCellValue('R10', 'incomeTaxSumS')
                    - (getCellValue('R9', 'incomeTaxSumS') - (formDataComplexConsumption.getDataRow('R92').getCell('consumptionTaxSumS').getValue()?:0) + (formDataComplexConsumption.getDataRow('R90').getCell('consumptionTaxSumS').getValue()?:0))
            ).setScale(2, BigDecimal.ROUND_HALF_UP)
    )
} else {
    getCell('R9', 'logicalCheck').setValue(0)
    getCell('R10', 'logicalCheck').setValue(0)
}


/**
 * Все оставшиеся строки, не описанные выше в этой таблице.
 */
// получение данных из доходов простых
def formDataSimpleIncome = FormDataService.find(301, FormDataKind.SUMMARY, formData.departmentId, formData.reportPeriodId)

List<DataRow> dataRows = formData.getDataRows()
for (DataRow row: dataRows) {
    // проверка что строка не описана выше
    if (['R20', 'R21', 'R38', 'R39', 'R78', 'R79', 'R95', 'R116', 'R119', 'R22',
            'R40', 'R80', 'R96', 'R104', 'R110', 'R117', 'R120', 'R128', 'R4', 'R5', 'R7', 'R19', 'R43',
            'R45', 'R46', 'R47', 'R48', 'R49', 'R50', 'R64', 'R70', 'R72', 'R73', 'R74', 'R75', 'R76', 'R77',
            'R98', 'R106', 'R112', 'R9', 'R10'].contains(row.getAlias())) {

        // графа 12
        ColumnRange columnRange6 = new ColumnRange('incomeBuhSumAccepted', 0, formData.getFormColumns().size() - 1)
        row.getCell('opuSumByEnclosure2').setValue(
                summ(formData, columnRange6, columnRange6, { condRange ->
                    return row.getCell('incomeTypeId').getValue() == condRange.getCell('incomeTypeId').getValue()
                })
        )
        // графа 15
        row.getCell('opuSumByOpu').setValue(income102Dao.getIncome102(
                formData.reportPeriodId, row.incomeBuhSumAccountNumber.toString().substring(8), formData.departmentId
        ).getTotalSum())
        // графа 16
        row.getCell('difference').setValue(getCellValue(row.getAlias(), 'opuSumTotal') - getCellValue(row.getAlias(), 'opuSumByOpu'))
    }
}

// строки 54-59, графы 13,14
['R54', 'R55', 'R56'. 'R57', 'R58', 'R59'].each {
    value = getCell(it, 'incomeBuhSumAccountNumber').toString()
    account = value.substring(0, 3) + value.substring(4)
    getCell(it, 'opuSumByTableD').setValue(income101Dao.getIncome101(formData.reportPeriodId, account, formData.departmentId).incomeDebetRemains)
    getCell(it, 'opuSumTotal').setValue(income101Dao.getIncome101(formData.reportPeriodId, account, formData.departmentId).outcomeDebetRemains)
}

// строки 3-53, 60-131
((3..53) + (60..131)).each {
    def thisRow = formData.getDataRow("R"+it)

    if (formDataSimpleIncome != null) {
        // графа 13
        columnRange9 = new ColumnRange('rnu4Field5Accepted', 0, formDataSimpleIncome.getDataRows().size() - 1)
        thisRow.opuSumByTableD = summ(formDataSimpleIncome, columnRange9, columnRange9, { condRange ->
            return  thisRow.incomeBuhSumAccountNumber == condRange.getCell('accountNo').getValue()
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

