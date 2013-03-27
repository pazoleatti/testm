/*
* Скрипт для удаления строки (deleteRow.groovy)
* Форма - Расчёт суммы налога по каждому транспортному средству
*/

def row = (DataRow)additionalParameter
if (row.getAlias() != "total"){
    // удаление строки
    formData.deleteDataRow(additionalParameter)
    // пересчет номеров строк
    def n = 1
    formData.getDataRows().each{ r ->
        r.rowNumber = n++
    }
}