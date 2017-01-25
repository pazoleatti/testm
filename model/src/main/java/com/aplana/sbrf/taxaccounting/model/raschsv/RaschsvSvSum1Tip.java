package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.math.BigDecimal;

/**
 * Сведения по суммам (тип 1) (СвСум1Тип)
 */
public class RaschsvSvSum1Tip extends IdentityObject<Long> {

    private BigDecimal sumVsegoPer;
    private BigDecimal sumVsegoPosl3m;
    private BigDecimal sum1mPosl3m;
    private BigDecimal sum2mPosl3m;
    private BigDecimal sum3mPosl3m;

    public static final String SEQ = "seq_raschsv_sv_sum_1tip";
    public static final String TABLE_NAME = "raschsv_sv_sum_1tip";
    public static final String COL_ID = "id";
    public static final String COL_SUM_VSEGO_PER = "sum_vsego_per";
    public static final String COL_SUM_VSEGO_POSL_3M = "sum_vsego_posl_3m";
    public static final String COL_SUM_1M_POSL_3M = "sum_1m_posl_3m";
    public static final String COL_SUM_2M_POSL_3M = "sum_2m_posl_3m";
    public static final String COL_SUM_3M_POSL_3M = "sum_3m_posl_3m";

    public static final String[] COLUMNS = {COL_ID, COL_SUM_VSEGO_PER, COL_SUM_VSEGO_POSL_3M, COL_SUM_1M_POSL_3M,
            COL_SUM_2M_POSL_3M, COL_SUM_3M_POSL_3M};

    public BigDecimal getSumVsegoPer() { return sumVsegoPer; }
    public void setSumVsegoPer(BigDecimal sumVsegoPer) { this.sumVsegoPer = sumVsegoPer; }

    public BigDecimal getSumVsegoPosl3m() { return sumVsegoPosl3m; }
    public void setSumVsegoPosl3m(BigDecimal sumVsegoPosl3m) { this.sumVsegoPosl3m = sumVsegoPosl3m; }

    public BigDecimal getSum1mPosl3m() { return sum1mPosl3m; }
    public void setSum1mPosl3m(BigDecimal sum1mPosl3m) { this.sum1mPosl3m = sum1mPosl3m; }

    public BigDecimal getSum2mPosl3m() { return sum2mPosl3m; }
    public void setSum2mPosl3m(BigDecimal sum2mPosl3m) { this.sum2mPosl3m = sum2mPosl3m; }

    public BigDecimal getSum3mPosl3m() { return sum3mPosl3m; }
    public void setSum3mPosl3m(BigDecimal sum3mPosl3m) { this.sum3mPosl3m = sum3mPosl3m; }
}
