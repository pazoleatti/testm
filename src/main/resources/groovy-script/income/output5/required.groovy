/**
 * Проверка полей перед расчётом
 * @author ekuvshinov
 */
//com.aplana.sbrf.taxaccounting.model.FormData formData
//com.aplana.sbrf.taxaccounting.log.Logger logger

void setError(String cellName) {

    if (!cellName.empty) {
        logger.error('Поле ' + cellName.replace('%', '') + ' не заполнено')
    }
}

for (row in formData.dataRows) {
    temp = 'incomeDeductible'
    if (row.getCell(temp).value == null) {
        setError(row.getCell(temp).column.name)
    }
    if (row.getAlias() == 'type4') {
        for (alias in ['base', 'taxPayment']) {
            if (row.getCell(alias).value == null) {
                setError(row.getCell(alias).column.name)
            }
        }
    }
}