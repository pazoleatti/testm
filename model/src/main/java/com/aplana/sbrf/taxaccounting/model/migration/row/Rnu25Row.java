package com.aplana.sbrf.taxaccounting.model.migration.row;

import java.math.BigDecimal;

public class Rnu25Row extends AbstractRnuRow {

    private String numState;
    private String numDeal;
    private BigDecimal numPaperPrev;
    private BigDecimal numPaper;
    private BigDecimal reservePrev;
    private BigDecimal getPrice;
    private String typePaper;
    private BigDecimal marketPriceOne;
    private BigDecimal marketPrice;
    private BigDecimal reserve;
    private BigDecimal reserveCreate;
    private BigDecimal reserveRest;

    public Rnu25Row() {
    }

    public String getNumState() {
        return numState;
    }

    public void setNumState(String numState) {
        this.numState = numState;
    }

    public String getNumDeal() {
        return numDeal;
    }

    public void setNumDeal(String numDeal) {
        this.numDeal = numDeal;
    }

    public BigDecimal getNumPaperPrev() {
        return numPaperPrev;
    }

    public void setNumPaperPrev(BigDecimal numPaperPrev) {
        this.numPaperPrev = numPaperPrev;
    }

    public BigDecimal getNumPaper() {
        return numPaper;
    }

    public void setNumPaper(BigDecimal numPaper) {
        this.numPaper = numPaper;
    }

    public BigDecimal getReservePrev() {
        return reservePrev;
    }

    public void setReservePrev(BigDecimal reservePrev) {
        this.reservePrev = reservePrev;
    }

    public BigDecimal getGetPrice() {
        return getPrice;
    }

    public void setGetPrice(BigDecimal getPrice) {
        this.getPrice = getPrice;
    }

    public String getTypePaper() {
        return typePaper;
    }

    public void setTypePaper(String typePaper) {
        this.typePaper = typePaper;
    }

    public BigDecimal getMarketPriceOne() {
        return marketPriceOne;
    }

    public void setMarketPriceOne(BigDecimal marketPriceOne) {
        this.marketPriceOne = marketPriceOne;
    }

    public BigDecimal getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(BigDecimal marketPrice) {
        this.marketPrice = marketPrice;
    }

    public BigDecimal getReserve() {
        return reserve;
    }

    public void setReserve(BigDecimal reserve) {
        this.reserve = reserve;
    }

    public BigDecimal getReserveCreate() {
        return reserveCreate;
    }

    public void setReserveCreate(BigDecimal reserveCreate) {
        this.reserveCreate = reserveCreate;
    }

    public BigDecimal getReserveRest() {
        return reserveRest;
    }

    public void setReserveRest(BigDecimal reserveRest) {
        this.reserveRest = reserveRest;
    }

    @Override
    public String toRow(Integer i) {
        final StringBuilder sb = new StringBuilder();

        addTechNumeration(sb, i);

        sb.append(getString(getNum())).append(SEP);
        sb.append(getString(numState)).append(SEP);
        sb.append(getString(numDeal)).append(SEP);
        sb.append(getString(numPaperPrev)).append(SEP);
        sb.append(getString(numPaper)).append(SEP);
        sb.append(getString(reservePrev)).append(SEP);
        sb.append(getString(getPrice)).append(SEP);
        sb.append(getString(typePaper)).append(SEP);
        sb.append(getString(marketPriceOne)).append(SEP);
        sb.append(getString(marketPrice)).append(SEP);
        sb.append(getString(reserve)).append(SEP);
        sb.append(getString(reserveCreate)).append(SEP);
        sb.append(getString(reserveRest)).append(SEP);
        return sb.toString();
    }
}
