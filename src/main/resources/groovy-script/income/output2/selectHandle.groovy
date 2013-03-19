/**
 * Сделаем колонки заполняемые вручную
 * @autor EKuvshinov
 */
//com.aplana.sbrf.taxaccounting.model.DataRow row
for (alias in ['title', 'zipCode', 'subdivisionRF', 'area', 'city', 'region', 'street', 'homeNumber', 'corpNumber', 'apartment',
        'surname', 'name', 'patronymic', 'phone', 'dividendDate', 'sumDividend', 'sumTax']) {
    row.getCell(alias).editable = true
}