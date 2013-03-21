/* Условие. */
row.getAlias() != 'total'
/* Конец условия. */

/**
 * Установка номера строки (setRowIndex.groovy).
 * Форма "Расчет суммы налога по каждому транспортному средству".
 *
 * @author rtimerbaev
 * @since 20.02.2013 13:00
 */

row.rowNumber = rowIndex + 1