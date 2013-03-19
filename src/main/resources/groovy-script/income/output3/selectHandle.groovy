/**
 * Сделаем колонки заполняемые вручную
 * @autor EKuvshinov
 */
//com.aplana.sbrf.taxaccounting.model.DataRow row
for (alias in ['paymentType', 'okatoCode', 'budgetClassificationCode', 'dateOfPayment', 'sumTax']) {
    row.getCell(alias).editable = true
}