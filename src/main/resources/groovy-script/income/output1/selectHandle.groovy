/**
 * Сделаем колонки заполняемые вручную
 * @autor EKuvshinov
 */
//com.aplana.sbrf.taxaccounting.model.FormData formData
for (row in formData.dataRows) {
    for (alias in ['financialYear', 'dividendSumRaspredPeriod', 'dividendForgeinOrgAll', 'dividendForgeinPersonalAll',
            'dividendStavka0', 'dividendStavkaLess5', 'dividendStavkaMore5', 'dividendStavkaMore10', 'dividendRussianOrgStavka9',
            'dividendRussianOrgStavka0', 'dividendPersonRussia', 'dividendMembersNotRussianTax', 'dividendAgentAll',
            'dividendAgentWithStavka0', 'taxSum', 'taxSumFromPeriodAll']) {
        row.getCell(alias).editable = true
    }
}
