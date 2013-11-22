package com.aplana.sbrf.taxaccounting.model.migration.row;

import java.math.BigDecimal;
import java.util.Date;

public class Rnu64Row extends AbstractRnuRow {

    private Date dealDate;
    private String partDeal;
    private String numDeal;
    private String defPaper;
    private BigDecimal cost;


    public Rnu64Row() {
    }

    public Date getDealDate() {
        return dealDate;
    }

    public void setDealDate(Date dealDate) {
        this.dealDate = dealDate;
    }

    public String getPartDeal() {
        return partDeal;
    }

    public void setPartDeal(String partDeal) {
        this.partDeal = partDeal;
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

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
}
