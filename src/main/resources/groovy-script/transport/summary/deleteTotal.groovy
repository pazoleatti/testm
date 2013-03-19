/**
 * Скрипт для удаления строки ИТОГО.
 * формы "Расчет суммы налога по каждому транспортному средству".
 *
 * @author rtimerbaev
 */

def row = formData.getDataRow('total')
if (row != null) {
    formData.getDataRows().remove(row)
}