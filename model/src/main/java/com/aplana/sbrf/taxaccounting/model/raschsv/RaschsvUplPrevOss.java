package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством (УплПревОСС)
 */
public class RaschsvUplPrevOss extends IdentityObject<Long> {

    private Long raschsvObyazPlatSvId;
    private String kbk;
    private Double sumSbUplPer;
    private Double sumSbUpl1m;
    private Double sumSbUpl2m;
    private Double sumSbUpl3m;
    private Double prevRashSvPer;
    private Double prevRashSv1m;
    private Double prevRashSv2m;
    private Double prevRashSv3m;

    public static final String SEQ = "seq_raschsv_upl_prev_oss";
    public static final String TABLE_NAME = "raschsv_upl_prev_oss";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_OBYAZ_PLAT_SV_ID = "raschsv_obyaz_plat_sv_id";
    public static final String COL_KBK = "kbk";
    public static final String COL_SUM_SB_UPL_PER = "sum_sb_upl_per";
    public static final String COL_SUM_SB_UPL_1M = "sum_sb_upl_1m";
    public static final String COL_SUM_SB_UPL_2M = "sum_sb_upl_2m";
    public static final String COL_SUM_SB_UPL_3M = "sum_sb_upl_3m";
    public static final String COL_PREV_RASH_SV_PER = "prev_rash_sv_per";
    public static final String COL_PREV_RASH_SV_1M = "prev_rash_sv_1m";
    public static final String COL_PREV_RASH_SV_2M = "prev_rash_sv_2m";
    public static final String COL_PREV_RASH_SV_3M = "prev_rash_sv_3m";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_OBYAZ_PLAT_SV_ID, COL_KBK,
            COL_SUM_SB_UPL_PER, COL_SUM_SB_UPL_1M, COL_SUM_SB_UPL_2M, COL_SUM_SB_UPL_3M, COL_PREV_RASH_SV_PER,
            COL_PREV_RASH_SV_1M, COL_PREV_RASH_SV_2M, COL_PREV_RASH_SV_3M};

    public Long getRaschsvObyazPlatSvId() { return raschsvObyazPlatSvId; }
    public void setRaschsvObyazPlatSvId(Long raschsvObyazPlatSvId) { this.raschsvObyazPlatSvId = raschsvObyazPlatSvId; }

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

    public Double getPrevRashSvPer() { return prevRashSvPer; }
    public void setPrevRashSvPer(Double prevRashSvPer) { this.prevRashSvPer = prevRashSvPer; }

    public Double getPrevRashSv1m() { return prevRashSv1m; }
    public void setPrevRashSv1m(Double prevRashSv1m) { this.prevRashSv1m = prevRashSv1m; }

    public Double getPrevRashSv2m() { return prevRashSv2m; }
    public void setPrevRashSv2m(Double prevRashSv2m) { this.prevRashSv2m = prevRashSv2m; }

    public Double getPrevRashSv3m() { return prevRashSv3m; }
    public void setPrevRashSv3m(Double prevRashSv3m) { this.prevRashSv3m = prevRashSv3m; }
}
