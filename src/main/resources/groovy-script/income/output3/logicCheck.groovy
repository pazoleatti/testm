/**
 * ЧТЗ выходные налоговые формы Ф2 Э1-2 П6.3.3.9.2.1
 * @author ekuvshinov
 * @since 08.02.2013
 */

//com.aplana.sbrf.taxaccounting.log.Logger logger

void setError(String cellName) {

    if (!cellName.empty) {
        logger.error('Поле ' + cellName.replace('%', '') + ' не заполнено')
    }
}

for (alias in ['paymentType', 'okatoCode', 'budgetClassificationCode', 'dateOfPayment', 'sumTax']) {
//noinspection GroovyVariableNotAssigned
    if (row.getCell(alias).value == null) {
        setError(row.getCell(alias).column.name)
    }
}