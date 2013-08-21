package com.aplana.sbrf.taxaccounting.model.migration.row;

import java.math.BigDecimal;

public class Rnu31Row extends AbstractRnuRow {

    private Long codeTypePaper;
    private String typePaper;
    private BigDecimal percCashOfz;
    private BigDecimal percCashEuro;
    private BigDecimal percCashFed;
    private BigDecimal percCashOgvz;
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

    public BigDecimal getPercCashEuro() {
        return percCashEuro;
    }

    public void setPercCashEuro(BigDecimal percCashEuro) {
        this.percCashEuro = percCashEuro;
    }

    public BigDecimal getPercCashFed() {
        return percCashFed;
    }

    public void setPercCashFed(BigDecimal percCashFed) {
        this.percCashFed = percCashFed;
    }

    public BigDecimal getPercCashOgvz() {
        return percCashOgvz;
    }

    public void setPercCashOgvz(BigDecimal percCashOgvz) {
        this.percCashOgvz = percCashOgvz;
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
    public String toRow() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getString(getNum())).append(SEP);
        sb.append(getString(codeTypePaper)).append(SEP);
        sb.append(getString(typePaper)).append(SEP);
        sb.append(getString(percCashOfz)).append(SEP);
        sb.append(getString(percCashEuro)).append(SEP);
        sb.append(getString(percCashFed)).append(SEP);
        sb.append(getString(percCashOgvz)).append(SEP);
        sb.append(getString(percCashOther)).append(SEP);
        sb.append(getString(percCashCorp)).append(SEP);
        return sb.toString();
    }
}
