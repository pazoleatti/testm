/**
 * Проверка полей перед расчётом  (проставим 0 там где это нужно)
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
    if (row.incomeDeductible == null) {
        row.incomeDeductible = new BigDecimal(0)
    }
    if (row.getAlias() == 'type4') {
        if (row.base == null) {
            row.base = new BigDecimal(0)
        }
        if (row.taxPayment == null) {
            row.taxPayment = new BigDecimal(0)
        }
    }
}