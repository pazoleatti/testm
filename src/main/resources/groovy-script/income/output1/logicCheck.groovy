/**
 * Проверка полей которые обязательно надо заполнить пользователю
 * @author ekuvshinov
 * @since 11.02.2013
 */
//com.aplana.sbrf.taxaccounting.log.Logger logger
//com.aplana.sbrf.taxaccounting.model.DataRow row

void setError(String cellName) {

    if (!cellName.empty) {
        logger.error('Поле ' + cellName.replace('%', '') + ' не заполнено')
    }
}

for (alias in ['financialYear', 'dividendSumRaspredPeriod', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll',
        'dividendStavka0', 'dividendStavkaLess5', 'dividendStavkaMore5', 'dividendStavkaMore10',
        'dividendRussianOrgStavka9', 'dividendRussianOrgStavka0', 'dividendPersonRussia',
        'dividendMembersNotRussianTax', 'dividendAgentAll', 'dividendAgentWithStavka0', 'taxSum', 'taxSumFromPeriodAll'
]) {
//noinspection GroovyVariableNotAssigned
    if (row.getCell(alias).value == null) {
        setError(row.getCell(alias).column.name)
    }
}