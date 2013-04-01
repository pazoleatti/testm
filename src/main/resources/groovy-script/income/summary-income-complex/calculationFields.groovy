/**
 * (calculationFields.groovy)
 * Алгоритмы заполнения полей формы при расчете данных формы
 * Табл. 6 Алгоритмы заполнения вычисляемых полей фиксированных строк  Сводной формы начисленных доходов уровня обособленного подразделения
 * Расчет (основные графы).
 * Форма "Сводная форма начисленных доходов (доходы сложные)".
 *
 * @author auldanov
 */

// v0.1

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
    return formData.getDataRowIndex(rowAlias);
}

/**
 * Прямое получения ячейки по столбцу и колонке.
 */
Cell getCell(String row, String column) {
    return getDataRow(row).getCell(column);
}

/**
 * Обертка предназначенная для прямых вызовов функции без formData.
 */
BigDecimal summ(ColumnRange cr) {
    return summ(formData, cr, cr, {return true;})
}

/**
 * Прямое получения значения ячейки по столбцу и колонке,
 * значение Null воспринимается как 0.
 */
BigDecimal getCellValue(String row, String column) {
    if (getDataRow(row).getCell(column)==null){
        throw new Exception('Не найдена ячейка');
    }

    return getDataRow(row).getCell(column).getValue() ?:0;
}

def specialNotation = 'Требуется объяснение';


/**
 * Суммирует ячейки второго диапазона только для тех строк, для которых выполняется условие фильтрации. В данном
 * случае под условием фильтрации подразумевается равенство значений строк первого диапазона заранее заданному
 * значению. Является аналогом Excel функции "СУММЕСЛИ" в нотации "СУММЕСЛИ(диапазон, критерий, диапазон_суммирования)"
 * @see <a href="http://office.microsoft.com/ru-ru/excel-help/HP010342932.aspx?CTT=1">СУММЕСЛИ(диапазон, критерий, [диапазон_суммирования])</a>
 *
 * @param formData таблица данных
 * @param conditionRange диапазон по которому осуществляется отбор строк (фильтрация)
 * @param filterValue значение фильтра
 * @param summRange диапазон суммирования
 * @return сумма ячеек
 */
double summ(FormData formData, Range conditionRange, Range summRange, filter) {
    Rect summRect = summRange.getRangeRect(formData);
    Rect condRange = conditionRange.getRangeRect(formData);
    if (!summRect.isSameSize(condRange))
        throw new IllegalArgumentException(NOT_SAME_RANGES);

    double sum = 0;
    List<DataRow> summRows = formData.getDataRows();
    List<Column> summCols = formData.getFormColumns();
    List<DataRow> condRows = formData.getDataRows();
    List<Column> condCols = formData.getFormColumns();
    for (int i = 0; i < condRange.getHeight(); i++) {
        for (int j = 0; j < condRange.getWidth(); j++) {
            Object condValue = condRows.get(condRange.y1 + i).get(condCols.get(condRange.x1 + j).getAlias());
            if (condValue != null && condValue != 'Требуется объяснение' && condValue != "" && filter(condRows.get(condRange.y1 + i))) {
                BigDecimal summValue = (BigDecimal) summRows.get(summRect.y1 + i).get(summCols.get(summRect.x1 + j).getAlias());
                if (summValue != null) {
                    sum += summValue.doubleValue();
                }
            }
        }
    }
    return sum;
}





// ----Раздел А1
// графа 6, 9
['incomeBuhSumAccepted', 'incomeTaxSumS'].each {
    getCell('R22', it).setValue(
            summ(new ColumnRange(it, getDataRowIndex('R2') + 1, getDataRowIndex('R22') - 1))
    );
}

// Раздел Б1
// графа 6, 9
['incomeBuhSumAccepted', 'incomeTaxSumS'].each {
    getCell('R40', it).setValue(
            summ(new ColumnRange(it, getDataRowIndex('R23') + 1, getDataRowIndex('R40') - 1))
    );
}

// --Раздел А2
// графа 6, 9
['incomeBuhSumAccepted', 'incomeTaxSumS'].each {
    getCell('R80', it).setValue(
            summ(new ColumnRange(it, getDataRowIndex('R42') + 1, getDataRowIndex('R80') - 1))
    );
}

// --- Раздел Б2
// графа 9
getCell('R96', 'incomeTaxSumS').setValue(
        summ(new ColumnRange("incomeTaxSumS", getDataRowIndex('R81') + 1, getDataRowIndex('R94') + 1))
);

// Раздел В
// графа 6, 9
["incomeBuhSumAccepted", "incomeTaxSumS"].each {
    getCell('R104', it).setValue(
            summ(new ColumnRange(it, getDataRowIndex('R97') + 1, getDataRowIndex('R104') - 1))
    );
}

// --- Раздел Г
// графа 6, 9
['incomeBuhSumAccepted', 'incomeTaxSumS'].each {
    getCell('R110', it).setValue(
            summ(new ColumnRange(it, getDataRowIndex('R105') + 1, getDataRowIndex('R110') - 1))
    );
}

// --Раздел Д
// графа 9
getCell('R117', 'incomeTaxSumS').setValue(
        summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R111') + 1, getDataRowIndex('R117') - 1))
);

// --Раздел Е
// графа 9
getCell('R120', 'incomeTaxSumS').setValue(
        summ(new ColumnRange('incomeTaxSumS', getDataRowIndex('R118') + 1, getDataRowIndex('R120') - 1))
);

// Раздел Ж
// графа 6, 9
['incomeBuhSumAccepted', 'incomeTaxSumS'].each {
    getCell('R128', it).setValue(
            summ(new ColumnRange(it, getDataRowIndex('R121') + 1, getDataRowIndex('R128') - 1))
    );
}
