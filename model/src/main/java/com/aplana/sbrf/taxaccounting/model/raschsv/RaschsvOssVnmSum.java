package com.aplana.sbrf.taxaccounting.model.raschsv;

/**
 * Связь РасчСВ_ОСС.ВНМ и СвСум1Тип
 */
public class RaschsvOssVnmSum {

    private Long raschsvOssVnmId;
    private Long raschsvSvSum1TipId;
    private String nodeName;

    // СвСум1Тип
    private RaschsvSvSum1Tip raschsvSvSum1Tip;

    public static final String TABLE_NAME = "raschsv_oss_vnm_sum";
    public static final String COL_RASCHSV_OSS_VNM_ID = "raschsv_oss_vnm_id";
    public static final String COL_NODE_NAME = "node_name";
    public static final String COL_RASCHSV_SV_SUM1_TIP_ID = "raschsv_sv_sum1_tip_id";

    public static final String[] COLUMNS = {COL_RASCHSV_OSS_VNM_ID, COL_RASCHSV_SV_SUM1_TIP_ID, COL_NODE_NAME};

    public Long getRaschsvOssVnmId() {
        return raschsvOssVnmId;
    }
    public void setRaschsvOssVnmId(Long raschsvOssVnmId) {
        this.raschsvOssVnmId = raschsvOssVnmId;
    }

    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Long getRaschsvSvSum1TipId() {
        return raschsvSvSum1TipId;
    }
    public void setRaschsvSvSum1TipId(Long raschsvSvSum1TipId) {
        this.raschsvSvSum1TipId = raschsvSvSum1TipId;
    }

    public RaschsvSvSum1Tip getRaschsvSvSum1Tip() {
        return raschsvSvSum1Tip;
    }
    public void setRaschsvSvSum1Tip(RaschsvSvSum1Tip raschsvSvSum1Tip) {
        this.raschsvSvSum1Tip = raschsvSvSum1Tip;
    }
}
