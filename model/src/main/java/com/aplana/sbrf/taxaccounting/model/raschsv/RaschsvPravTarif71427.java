package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 7 пункта 1 статьи 427 (ПравТариф7.1.427)
 */
public class RaschsvPravTarif71427 extends IdentityObject<Long> {

    private Long raschsvObyazPlatSvId;
    private Long dohVsPred;
    private Long dohVsPer;
    private Long dohCelPostPred;
    private Long dohCelPostPer;
    private Long dohGrantPred;
    private Long dohGrantPer;
    private Long dohEkDeyatPred;
    private Long dohEkDeyatPer;
    private Double dolDohPred;
    private Double dolDohPer;

    public static final String SEQ = "seq_raschsv_prav_tarif7_1_427";
    public static final String TABLE_NAME = "raschsv_prav_tarif7_1_427";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_OBYAZ_PLAT_SV_ID = "raschsv_obyaz_plat_sv_id";
    public static final String COL_DOH_VS_PRED = "doh_vs_pred";
    public static final String COL_DOH_VS_PER = "doh_vs_per";
    public static final String COL_DOH_CEL_POST_PRED = "doh_cel_post_pred";
    public static final String COL_DOH_CEL_POST_PER = "doh_cel_post_per";
    public static final String COL_DOH_GRANT_PRED = "doh_grant_pred";
    public static final String COL_DOH_GRANT_PER = "doh_grant_per";
    public static final String COL_DOH_EK_DEYAT_PRED = "doh_ek_deyat_pred";
    public static final String COL_DOH_EK_DEYAT_PER = "doh_ek_deyat_per";
    public static final String COL_DOL_DOH_PRED = "dol_doh_pred";
    public static final String COL_DOL_DOH_PER = "dol_doh_per";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_OBYAZ_PLAT_SV_ID, COL_DOH_VS_PRED, COL_DOH_VS_PER,
            COL_DOH_CEL_POST_PRED, COL_DOH_CEL_POST_PER, COL_DOH_GRANT_PRED, COL_DOH_GRANT_PER, COL_DOH_EK_DEYAT_PRED,
            COL_DOH_EK_DEYAT_PER, COL_DOL_DOH_PRED, COL_DOL_DOH_PER};

    public Long getRaschsvObyazPlatSvId() { return raschsvObyazPlatSvId; }
    public void setRaschsvObyazPlatSvId(Long raschsvObyazPlatSvId) { this.raschsvObyazPlatSvId = raschsvObyazPlatSvId; }

    public Long getDohVsPred() {
        return dohVsPred;
    }
    public void setDohVsPred(Long dohVsPred) {
        this.dohVsPred = dohVsPred;
    }

    public Long getDohVsPer() {
        return dohVsPer;
    }
    public void setDohVsPer(Long dohVsPer) {
        this.dohVsPer = dohVsPer;
    }

    public Long getDohCelPostPred() {
        return dohCelPostPred;
    }
    public void setDohCelPostPred(Long dohCelPostPred) {
        this.dohCelPostPred = dohCelPostPred;
    }

    public Long getDohCelPostPer() {
        return dohCelPostPer;
    }
    public void setDohCelPostPer(Long dohCelPostPer) {
        this.dohCelPostPer = dohCelPostPer;
    }

    public Long getDohGrantPred() {
        return dohGrantPred;
    }
    public void setDohGrantPred(Long dohGrantPred) {
        this.dohGrantPred = dohGrantPred;
    }

    public Long getDohGrantPer() {
        return dohGrantPer;
    }
    public void setDohGrantPer(Long dohGrantPer) {
        this.dohGrantPer = dohGrantPer;
    }

    public Long getDohEkDeyatPred() {
        return dohEkDeyatPred;
    }
    public void setDohEkDeyatPred(Long dohEkDeyatPred) {
        this.dohEkDeyatPred = dohEkDeyatPred;
    }

    public Long getDohEkDeyatPer() {
        return dohEkDeyatPer;
    }
    public void setDohEkDeyatPer(Long dohEkDeyatPer) {
        this.dohEkDeyatPer = dohEkDeyatPer;
    }

    public Double getDolDohPred() {
        return dolDohPred;
    }
    public void setDolDohPred(Double dolDohPred) {
        this.dolDohPred = dolDohPred;
    }

    public Double getDolDohPer() {
        return dolDohPer;
    }
    public void setDolDohPer(Double dolDohPer) {
        this.dolDohPer = dolDohPer;
    }
}
