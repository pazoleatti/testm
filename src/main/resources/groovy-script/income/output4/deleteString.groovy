/**
 * Удаляет строку итого
 * @author ekuvshinov
 */
//com.aplana.sbrf.taxaccounting.model.FormData formData

r = null
try {
    r = formData.getDataRow('total')
} catch (IllegalArgumentException e){
}
if (additionalParameter.getAlias() != 'total' && formData.dataRows.size() != 0 && r != null) {
    formData.deleteDataRow(r);
}
formData.deleteDataRow(additionalParameter);

// Обновим индексы http://jira.aplana.com/browse/SBRFACCTAX-1759
i = 0;
for (row in formData.dataRows) {
    i++
    row.number = i
}