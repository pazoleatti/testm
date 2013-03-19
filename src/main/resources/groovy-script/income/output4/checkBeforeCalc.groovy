/**
 * Поля которые обязательно необходимо заполнить пользователю для расчётов
 * @author ekuvshinov
 */

//com.aplana.sbrf.taxaccounting.log.Logger logger
//com.aplana.sbrf.taxaccounting.model.FormData formData
//com.aplana.sbrf.taxaccounting.service.script.dictionary.DictionaryRegionService dictionaryRegionService
//com.aplana.sbrf.taxaccounting.service.script.DepartmentService departmentService

void setError(String cellName) {

    if (!cellName.empty) {
        logger.error('Поле ' + cellName.replace('%', '') + ' не заполнено')
    }
}

for (row in formData.dataRows) {

    //noinspection GroovyVariableNotAssigned
    if (row.getAlias() != 'total') {

        for (alias in ['divisionName',
                'stringCode', 'labalAboutPaymentTax',
                'propertyPrice', 'workersCount',

        ]) {
            /* [
                    'number', 'bankName', 'bankCode', 'divisionCode', 'divisionName', 'kpp', 'subjectCode', 'subjectName',
                    'stringCode', 'propertyPrice', 'workersCount', 'propertyWeight', 'countWeight', 'baseTaxOf', 'baseTaxOfRub',
                    'subjectTaxStavka', 'subjectTaxSum', 'subjectTaxCredit', 'taxSumToPay', 'taxSumToReduction',
                    'everyMontherPaymentAfterPeriod', 'everyMonthForKvartalNextPeriod', 'avansPayments1', 'avansPayments2',
                    'avansPayments3', 'labalAboutPaymentTax', 'changeShareBaseTax', 'taxSumOutside', 'thisFond', 'thisQuantity',
                    'lastFond', 'lastQuantity', 'delta21', 'delta28'
            ]
            */
            if (row.getCell(alias).value == null) {
                setError(row.getCell(alias).column.name)
            }
        }
    }
}