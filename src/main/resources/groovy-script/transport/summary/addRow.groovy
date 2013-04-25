/**
 * Скрипт для добавления новой строки (addRow.groovy).
 * Форма "Расчет суммы налога по каждому транспортному средству".
 */

def row = formData.appendDataRow();
['okato', 'tsTypeCode', 'vi', 'model', 'regNumber', 'taxBase', 'taxBaseOkeiUnit', 'ecoClass', 'years', 'ownMonths', 'taxBenefitCode', 'benefitStartDate', 'benefitEndDate'].each{ alias ->
    row.getCell(alias).editable = true
    row.getCell(alias).setStyleAlias("Редактируемое поле")
}