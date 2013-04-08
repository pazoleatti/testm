/**
 * Создаёт строку
 * @author ekuvshinov
 */
//com.aplana.sbrf.taxaccounting.model.FormData formData

// Если есть строка итого, удалим её
if (formData.dataRows.size() != 0
        && formData.dataRows.get(formData.dataRows.size() - 1).getAlias() == 'total') {
    formData.dataRows.remove(formData.dataRows.size() - 1)
}
row = formData.appendDataRow()

// Сделаем некоторые колонки изменяемыми
for (alias in [
        'divisionName', 'stringCode', 'propertyPrice', 'workersCount', 'labalAboutPaymentTax', 'delta21', 'delta28'
]) {
    row.getCell(alias).editable = true
    row.getCell(alias).setStyleAlias('Редактируемая')
}
row.number = formData.dataRows.size()
row.delta21 = 0
row.delta28 = 0