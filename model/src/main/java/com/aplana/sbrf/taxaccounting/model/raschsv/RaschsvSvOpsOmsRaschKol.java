package com.aplana.sbrf.taxaccounting.model.raschsv;

/**
 * Связь РасчСВ_ОПС, РасчСВ_ОМС, РасчСВ_428.1-2, РасчСВ_428.3, РасчСВ_ДСО и КолЛицТип
 */
public class RaschsvSvOpsOmsRaschKol {

    private Long raschsvOpsOmsRaschKolId;
    private Long raschsvKolLicTipId;
    private String nodeName;

    // КолЛицТип
    private RaschsvKolLicTip raschsvKolLicTip;

    public static final String TABLE_NAME = "raschsv_ops_oms_rasch_kol";
    public static final String COL_RASCHSV_OPS_OMS_RASCH_KOL_ID = "raschsv_ops_oms_rasch_kol_id";
    public static final String COL_NODE_NAME = "node_name";
    public static final String COL_RASCHSV_KOL_LIC_TIP_ID = "raschsv_kol_lic_tip_id";

    public static final String[] COLUMNS = {COL_RASCHSV_OPS_OMS_RASCH_KOL_ID, COL_RASCHSV_KOL_LIC_TIP_ID, COL_NODE_NAME};

    public Long getRaschsvOpsOmsRaschKolId() {
        return raschsvOpsOmsRaschKolId;
    }
    public void setRaschsvOpsOmsRaschKolId(Long raschsvOpsOmsRaschKolId) {
        this.raschsvOpsOmsRaschKolId = raschsvOpsOmsRaschKolId;
    }

    public Long getRaschsvKolLicTipId() {
        return raschsvKolLicTipId;
    }
    public void setRaschsvKolLicTipId(Long raschsvKolLicTipId) {
        this.raschsvKolLicTipId = raschsvKolLicTipId;
    }

    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public RaschsvKolLicTip getRaschsvKolLicTip() {
        return raschsvKolLicTip;
    }
    public void setRaschsvKolLicTip(RaschsvKolLicTip raschsvKolLicTip) {
        this.raschsvKolLicTip = raschsvKolLicTip;
    }
}
