package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Сумма страховых взносов на пенсионное, медицинское, социальное страхование
 */
public class RaschsvUplPer extends IdentityObject<Long> {

    private Long raschsvObyazPlatSvId;
    private String nodeName;
    private String kbk;
    private Double sumSbUplPer;
    private Double sumSbUpl1m;
    private Double sumSbUpl2m;
    private Double sumSbUpl3m;

    public static final String SEQ = "seq_raschsv_upl_per";
    public static final String TABLE_NAME = "raschsv_upl_per";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_OBYAZ_PLAT_SV_ID = "raschsv_obyaz_plat_sv_id";
    public static final String COL_NODE_NAME = "node_name";
    public static final String COL_KBK = "kbk";
    public static final String COL_SUM_SB_UPL_PER = "sum_sb_upl_per";
    public static final String COL_SUM_SB_UPL_1M = "sum_sb_upl_1m";
    public static final String COL_SUM_SB_UPL_2M = "sum_sb_upl_2m";
    public static final String COL_SUM_SB_UPL_3M = "sum_sb_upl_3m";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_OBYAZ_PLAT_SV_ID, COL_NODE_NAME, COL_KBK,
            COL_SUM_SB_UPL_PER, COL_SUM_SB_UPL_1M, COL_SUM_SB_UPL_2M, COL_SUM_SB_UPL_3M};

    public Long getRaschsvObyazPlatSvId() { return raschsvObyazPlatSvId; }
    public void setRaschsvObyazPlatSvId(Long raschsvObyazPlatSvId) { this.raschsvObyazPlatSvId = raschsvObyazPlatSvId; }

    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }

    public String getKbk() { return kbk; }
    public void setKbk(String kbk) { this.kbk = kbk; }

    public Double getSumSbUplPer() { return sumSbUplPer; }
    public void setSumSbUplPer(Double sumSbUplPer) { this.sumSbUplPer = sumSbUplPer; }

    public Double getSumSbUpl1m() { return sumSbUpl1m; }
    public void setSumSbUpl1m(Double sumSbUpl1m) { this.sumSbUpl1m = sumSbUpl1m; }

    public Double getSumSbUpl2m() { return sumSbUpl2m; }
    public void setSumSbUpl2m(Double sumSbUpl2m) { this.sumSbUpl2m = sumSbUpl2m; }

    public Double getSumSbUpl3m() { return sumSbUpl3m; }
    public void setSumSbUpl3m(Double sumSbUpl3m) { this.sumSbUpl3m = sumSbUpl3m; }
}