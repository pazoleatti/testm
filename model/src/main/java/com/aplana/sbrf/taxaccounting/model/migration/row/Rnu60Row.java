package com.aplana.sbrf.taxaccounting.model.migration.row;

import java.math.BigDecimal;
import java.util.Date;

public class Rnu60Row extends AbstractRnuRow {

    private String numDeal;
    private String defPaper;
    private String codecurrency;
    private BigDecimal nompaper;
    private Date drepo1;
    private Date drepo2;
    private BigDecimal getpricenkd;
    private BigDecimal salepricenkd;
    private BigDecimal costrepo;
    private BigDecimal implrepo;
    private BigDecimal bankrate;
    private BigDecimal costrepo269;
    private BigDecimal costrepotax;

    public Rnu60Row() {
    }

    public String getNumDeal() {
        return numDeal;
    }

    public void setNumDeal(String numDeal) {
        this.numDeal = numDeal;
    }

    public String getDefPaper() {
        return defPaper;
    }

    public void setDefPaper(String defPaper) {
        this.defPaper = defPaper;
    }

    public String getCodecurrency() {
        return codecurrency;
    }

    public void setCodecurrency(String codecurrency) {
        this.codecurrency = codecurrency;
    }

    public BigDecimal getNompaper() {
        return nompaper;
    }

    public void setNompaper(BigDecimal nompaper) {
        this.nompaper = nompaper;
    }

    public Date getDrepo1() {
        return drepo1;
    }

    public void setDrepo1(Date drepo1) {
        this.drepo1 = drepo1;
    }

    public Date getDrepo2() {
        return drepo2;
    }

    public void setDrepo2(Date drepo2) {
        this.drepo2 = drepo2;
    }

    public BigDecimal getGetpricenkd() {
        return getpricenkd;
    }

    public void setGetpricenkd(BigDecimal getpricenkd) {
        this.getpricenkd = getpricenkd;
    }

    public BigDecimal getSalepricenkd() {
        return salepricenkd;
    }

    public void setSalepricenkd(BigDecimal salepricenkd) {
        this.salepricenkd = salepricenkd;
    }

    public BigDecimal getCostrepo() {
        return costrepo;
    }

    public void setCostrepo(BigDecimal costrepo) {
        this.costrepo = costrepo;
    }

    public BigDecimal getImplrepo() {
        return implrepo;
    }

    public void setImplrepo(BigDecimal implrepo) {
        this.implrepo = implrepo;
    }

    public BigDecimal getBankrate() {
        return bankrate;
    }

    public void setBankrate(BigDecimal bankrate) {
        this.bankrate = bankrate;
    }

    public BigDecimal getCostrepo269() {
        return costrepo269;
    }

    public void setCostrepo269(BigDecimal costrepo269) {
        this.costrepo269 = costrepo269;
    }

    public BigDecimal getCostrepotax() {
        return costrepotax;
    }

    public void setCostrepotax(BigDecimal costrepotax) {
        this.costrepotax = costrepotax;
    }
}
