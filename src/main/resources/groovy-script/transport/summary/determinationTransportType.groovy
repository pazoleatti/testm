/*
* Определение наименования типа транспортного средства
* Скрипт для получения названия вида транспортного средства по коду ТС.
* Форма "Расчет суммы налога по каждому транспортному средству".
*/

if (row.tsTypeCode != null){
    row.tsType = transportTaxDao.getTsTypeName(row.tsTypeCode)
}
