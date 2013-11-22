package com.aplana.sbrf.taxaccounting.model.migration.row;

import java.math.BigDecimal;

public class Rnu31Row extends AbstractRnuRow {

    private Long codeTypePaper;
    private String typePaper;
    private BigDecimal percCashOfz;
    private BigDecimal percCashFed;
    private BigDecimal percCashBel;
    private BigDecimal percCashIpotAfter;
    private BigDecimal percCashMun;
    private BigDecimal percCashIpotBefore;
    private BigDecimal percCashOgvz;
    private BigDecimal percCashEuroNew;
    private BigDecimal percCashOther;
    private BigDecimal percCashCorp;


    public Rnu31Row() {
    }

    public Long getCodeTypePaper() {
        return codeTypePaper;
    }

    public void setCodeTypePaper(Long codeTypePaper) {
        this.codeTypePaper = codeTypePaper;
    }

    public String getTypePaper() {
        return typePaper;
    }

    public void setTypePaper(String typePaper) {
        this.typePaper = typePaper;
    }

    public BigDecimal getPercCashOfz() {
        return percCashOfz;
    }

    public void setPercCashOfz(BigDecimal percCashOfz) {
        this.percCashOfz = percCashOfz;
    }

    public BigDecimal getPercCashFed() {
        return percCashFed;
    }

    public void setPercCashFed(BigDecimal percCashFed) {
        this.percCashFed = percCashFed;
    }

    public BigDecimal getPercCashBel() {
        return percCashBel;
    }

    public void setPercCashBel(BigDecimal percCashBel) {
        this.percCashBel = percCashBel;
    }

    public BigDecimal getPercCashIpotAfter() {
        return percCashIpotAfter;
    }

    public void setPercCashIpotAfter(BigDecimal percCashIpotAfter) {
        this.percCashIpotAfter = percCashIpotAfter;
    }

    public BigDecimal getPercCashMun() {
        return percCashMun;
    }

    public void setPercCashMun(BigDecimal percCashMun) {
        this.percCashMun = percCashMun;
    }

    public BigDecimal getPercCashIpotBefore() {
        return percCashIpotBefore;
    }

    public void setPercCashIpotBefore(BigDecimal percCashIpotBefore) {
        this.percCashIpotBefore = percCashIpotBefore;
    }

    public BigDecimal getPercCashOgvz() {
        return percCashOgvz;
    }

    public void setPercCashOgvz(BigDecimal percCashOgvz) {
        this.percCashOgvz = percCashOgvz;
    }

    public BigDecimal getPercCashEuroNew() {
        return percCashEuroNew;
    }

    public void setPercCashEuroNew(BigDecimal percCashEuroNew) {
        this.percCashEuroNew = percCashEuroNew;
    }

    public BigDecimal getPercCashOther() {
        return percCashOther;
    }

    public void setPercCashOther(BigDecimal percCashOther) {
        this.percCashOther = percCashOther;
    }

    public BigDecimal getPercCashCorp() {
        return percCashCorp;
    }

    public void setPercCashCorp(BigDecimal percCashCorp) {
        this.percCashCorp = percCashCorp;
    }

    @Override
    public String toRow(Integer i) {
        final StringBuilder sb = new StringBuilder();

        addTechNumeration(sb, i);

        sb.append(getString(getNum())).append(SEP);
        //sb.append(getString(codeTypePaper)).append(SEP);  // в архиве с примерами 13 полей
        sb.append(getString(typePaper)).append(SEP);
        sb.append(getString(percCashOfz)).append(SEP);
        sb.append(getString(percCashFed)).append(SEP);
        sb.append(getString(percCashBel)).append(SEP);
        sb.append(getString(percCashIpotAfter)).append(SEP);
        sb.append(getString(percCashMun)).append(SEP);
        sb.append(getString(percCashIpotBefore)).append(SEP);
        sb.append(getString(percCashOgvz)).append(SEP);
        sb.append(getString(percCashEuroNew)).append(SEP);
        sb.append(getString(percCashOther)).append(SEP);
        sb.append(getString(percCashCorp)).append(SEP);
        return sb.toString();
    }
}
