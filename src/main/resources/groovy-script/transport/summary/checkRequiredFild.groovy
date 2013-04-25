/*
 * Условие
 */

row.getAlias() != 'total'


 /**
 * Проверка обязательных полей.
 * Форма "Расчет суммы налога по каждому транспортному средству".
 *
 * @author rtimerbaev
 * @since 19.02.2013 13:30
 */

def errorMsg = '';

// 2, 3, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 20 , 21
['okato', 'tsTypeCode', 'vi', 'model', 'regNumber', 'taxBase', 'taxBaseOkeiUnit', 'ownMonths','coef362', 'calculatedTaxSum', 'taxSumToPay'].each {
// Тут у меня непонятки при мерже произошли. Старая строка закомментирована на всякий
// ['okato', 'tsTypeCode', 'vi', 'model', 'regNumber', 'taxBase', 'taxBaseOkeiUnit', 'ownMonths','coef362', 'taxRate', 'calculatedTaxSum', 'benefitSum', 'taxSumToPay'].each {
    if (row.getCell(it) != null && (row.getCell(it).getValue() == null || ''.equals(row.getCell(it).getValue()))) {
        errorMsg += (!''.equals(errorMsg) ? ', ' : '') + '"' + row.getCell(it).getColumn().getName() + '"'
    }
}
if (!''.equals(errorMsg)) {
    logger.error("Не заполнены поля в колонках : $errorMsg.")
}