/**
 * У созданной формы проставляет редактируемые поля
 * @author ekuvshinov
 */
//com.aplana.sbrf.taxaccounting.model.FormData formData

for (row in formData.dataRows) {
    row.getCell('incomeDeductible').editable = true
    if (row.getAlias() == 'type4') {
        row.getCell('base').editable = true
        row.getCell('taxPayment').editable = true
    }
}