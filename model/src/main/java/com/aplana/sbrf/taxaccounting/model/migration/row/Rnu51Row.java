package com.aplana.sbrf.taxaccounting.model.migration.row;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Модель для строк РНУ 51
 * @author Alexander Ivanov
 */
public class Rnu51Row extends AbstractRnuRow {

    private Long codedeal;
    private String typepaper;
    private String defpaper;
    private Date dget;
    private Date dimpl;
    private Long numpaper;
    private BigDecimal salepriceperc;
    private BigDecimal rsaleprice;
    private BigDecimal getsalepricetax;
    private BigDecimal getmpriceperc;
    private BigDecimal getmprice;
    private BigDecimal rmarketprice;
    private BigDecimal marketpriceperc;
    private BigDecimal rcost;
    private BigDecimal rtotalcost;
    private BigDecimal rprofitcost;
    private BigDecimal rgetprice;
    private BigDecimal rgetcost;
    private BigDecimal rsumext;
    private BigDecimal rsalepricetax;
    private BigDecimal rovwrprice;

    public Rnu51Row() {
    }

    public Long getCodedeal() {
        return codedeal;
    }

    public void setCodedeal(Long codedeal) {
        this.codedeal = codedeal;
    }

    public String getTypepaper() {
        return typepaper;
    }

    public void setTypepaper(String typepaper) {
        this.typepaper = typepaper;
    }

    public String getDefpaper() {
        return defpaper;
    }

    public void setDefpaper(String defpaper) {
        this.defpaper = defpaper;
    }

    public Date getDget() {
        return dget;
    }

    public void setDget(Date dget) {
        this.dget = dget;
    }

    public Date getDimpl() {
        return dimpl;
    }

    public void setDimpl(Date dimpl) {
        this.dimpl = dimpl;
    }

    public Long getNumpaper() {
        return numpaper;
    }

    public void setNumpaper(Long numpaper) {
        this.numpaper = numpaper;
    }

    public BigDecimal getSalepriceperc() {
        return salepriceperc;
    }

    public void setSalepriceperc(BigDecimal salepriceperc) {
        this.salepriceperc = salepriceperc;
    }

    public BigDecimal getRsaleprice() {
        return rsaleprice;
    }

    public void setRsaleprice(BigDecimal rsaleprice) {
        this.rsaleprice = rsaleprice;
    }

    public BigDecimal getGetsalepricetax() {
        return getsalepricetax;
    }

    public void setGetsalepricetax(BigDecimal getsalepricetax) {
        this.getsalepricetax = getsalepricetax;
    }

    public BigDecimal getGetmpriceperc() {
        return getmpriceperc;
    }

    public void setGetmpriceperc(BigDecimal getmpriceperc) {
        this.getmpriceperc = getmpriceperc;
    }

    public BigDecimal getGetmprice() {
        return getmprice;
    }

    public void setGetmprice(BigDecimal getmprice) {
        this.getmprice = getmprice;
    }

    public BigDecimal getRmarketprice() {
        return rmarketprice;
    }

    public void setRmarketprice(BigDecimal rmarketprice) {
        this.rmarketprice = rmarketprice;
    }

    public BigDecimal getMarketpriceperc() {
        return marketpriceperc;
    }

    public void setMarketpriceperc(BigDecimal marketpriceperc) {
        this.marketpriceperc = marketpriceperc;
    }

    public BigDecimal getRcost() {
        return rcost;
    }

    public void setRcost(BigDecimal rcost) {
        this.rcost = rcost;
    }

    public BigDecimal getRtotalcost() {
        return rtotalcost;
    }

    public void setRtotalcost(BigDecimal rtotalcost) {
        this.rtotalcost = rtotalcost;
    }

    public BigDecimal getRprofitcost() {
        return rprofitcost;
    }

    public void setRprofitcost(BigDecimal rprofitcost) {
        this.rprofitcost = rprofitcost;
    }

    public BigDecimal getRgetprice() {
        return rgetprice;
    }

    public void setRgetprice(BigDecimal rgetprice) {
        this.rgetprice = rgetprice;
    }

    public BigDecimal getRgetcost() {
        return rgetcost;
    }

    public void setRgetcost(BigDecimal rgetcost) {
        this.rgetcost = rgetcost;
    }

    public BigDecimal getRsumext() {
        return rsumext;
    }

    public void setRsumext(BigDecimal rsumext) {
        this.rsumext = rsumext;
    }

    public BigDecimal getRsalepricetax() {
        return rsalepricetax;
    }

    public void setRsalepricetax(BigDecimal rsalepricetax) {
        this.rsalepricetax = rsalepricetax;
    }

    public BigDecimal getRovwrprice() {
        return rovwrprice;
    }

    public void setRovwrprice(BigDecimal rovwrprice) {
        this.rovwrprice = rovwrprice;
    }
}
