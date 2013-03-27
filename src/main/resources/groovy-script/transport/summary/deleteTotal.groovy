/**
 * Скрипт для удаления строки ИТОГО (deleteTotal.groovy).
 * формы "Расчет суммы налога по каждому транспортному средству".
 *
 * @author rtimerbaev
 */

def row = (formData.dataRows.size() > 0 ? formData.getDataRow('total') : null)
if (row != null) {
    formData.getDataRows().remove(row)
}