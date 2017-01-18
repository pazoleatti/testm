package com.aplana.sbrf.taxaccounting.model.raschsv;

/**
 * Связь РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и СвСум1Тип
 */
public class RaschsvSvOpsOmsRaschSum {

    private Long raschsvOpsOmsRaschSumId;
    private Long raschsvSvSum1TipId;
    private String nodeName;

    // СвСум1Тип
    private RaschsvSvSum1Tip raschsvSvSum1Tip;

    public static final String TABLE_NAME = "raschsv_ops_oms_rasch_sum";
    public static final String COL_RASCHSV_OPS_OMS_RASCH_SUM_ID = "raschsv_ops_oms_rasch_sum_id";
    public static final String COL_RASCHSV_SV_SUM1_TIP_ID = "raschsv_sv_sum1_tip_id";
    public static final String COL_NODE_NAME = "node_name";

    public static final String[] COLUMNS = {COL_RASCHSV_OPS_OMS_RASCH_SUM_ID, COL_RASCHSV_SV_SUM1_TIP_ID, COL_NODE_NAME};

    public Long getRaschsvOpsOmsRaschSumId() {
        return raschsvOpsOmsRaschSumId;
    }
    public void setRaschsvOpsOmsRaschSumId(Long raschsvOpsOmsRaschSumId) {
        this.raschsvOpsOmsRaschSumId = raschsvOpsOmsRaschSumId;
    }

    public Long getRaschsvSvSum1TipId() {
        return raschsvSvSum1TipId;
    }
    public void setRaschsvSvSum1TipId(Long raschsvSvSum1TipId) {
        this.raschsvSvSum1TipId = raschsvSvSum1TipId;
    }

    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public RaschsvSvSum1Tip getRaschsvSvSum1Tip() {
        return raschsvSvSum1Tip;
    }
    public void setRaschsvSvSum1Tip(RaschsvSvSum1Tip raschsvSvSum1Tip) {
        this.raschsvSvSum1Tip = raschsvSvSum1Tip;
    }
}
