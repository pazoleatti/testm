package com.aplana.sbrf.taxaccounting.model.ndfl;

import java.util.ArrayList;
import java.util.List;

/**
 * Обобщенные показатели для формирования 6 НДФЛ
 */
public class NdflPersonIncomeCommonValue {

    // Колличество ФЛ
    private Integer countPerson;

    // Сумма налога удержанная
    private Long withholdingTax;

    // Сумма налога, не удержанная налоговым агентом
    private Long notHoldingTax;

    // Сумма возвращенного налога
    private Long refoundTax;

    // Суммы по ставкам для формирования 6 НДФЛ
    private List<NdflPersonIncomeByRate> ndflPersonIncomeByRateList;

    public NdflPersonIncomeCommonValue() {
        ndflPersonIncomeByRateList = new ArrayList<NdflPersonIncomeByRate>();
    }

    public Integer getCountPerson() {
        return countPerson;
    }
    public void setCountPerson(Integer countPerson) {
        this.countPerson = countPerson;
    }
    public void addCountPerson(Integer countPerson) {
        this.countPerson = this.countPerson == null ? countPerson : this.countPerson + countPerson;
    }

    public Long getWithholdingTax() {
        return withholdingTax;
    }
    public void setWithholdingTax(Long withholdingTax) {
        this.withholdingTax = withholdingTax;
    }
    public void addWithholdingTax(Long withholdingTax) {
        this.withholdingTax = this.withholdingTax == null ? withholdingTax : this.withholdingTax + withholdingTax;
    }

    public Long getNotHoldingTax() {
        return notHoldingTax;
    }
    public void setNotHoldingTax(Long notHoldingTax) {
        this.notHoldingTax = notHoldingTax;
    }
    public void addNotHoldingTax(Long notHoldingTax) {
        this.notHoldingTax = this.notHoldingTax == null ? notHoldingTax : this.notHoldingTax + notHoldingTax;
    }

    public Long getRefoundTax() {
        return refoundTax;
    }
    public void setRefoundTax(Long refoundTax) {
        this.refoundTax = refoundTax;
    }
    public void addRefoundTax(Long refoundTax) {
        this.refoundTax = this.refoundTax == null ? refoundTax : this.refoundTax + refoundTax;
    }

    public List<NdflPersonIncomeByRate> getNdflPersonIncomeByRateList() {
        return ndflPersonIncomeByRateList;
    }
    public void setNdflPersonIncomeByRateList(List<NdflPersonIncomeByRate> ndflPersonIncomeByRateList) {
        this.ndflPersonIncomeByRateList = ndflPersonIncomeByRateList;
    }
}
