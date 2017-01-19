package com.aplana.sbrf.taxaccounting.model.raschsv;

/**
 * Итого выплат (ВыплатИт)
 */
public class RaschsvVyplatIt422 {

    private Long raschsvSvPrimTarif1422Id;
    private Long raschsvSvSum1TipId;

    // СвСум1Тип
    private RaschsvSvSum1Tip raschsvSvSum1Tip;

    public static final String TABLE_NAME = "raschsv_vyplat_it_422";
    public static final String COL_RASCHSV_SV_PRIM_TARIF1_422_ID = "raschsv_sv_prim_tarif1_422_id";
    public static final String COL_RASCHSV_SV_SUM1_TIP_ID = "raschsv_sv_sum1_tip_id";

    public static final String[] COLUMNS = {COL_RASCHSV_SV_PRIM_TARIF1_422_ID, COL_RASCHSV_SV_SUM1_TIP_ID};

    public RaschsvSvSum1Tip getRaschsvSvSum1Tip() {
        return raschsvSvSum1Tip;
    }
    public void setRaschsvSvSum1Tip(RaschsvSvSum1Tip raschsvSvSum1Tip) {
        this.raschsvSvSum1Tip = raschsvSvSum1Tip;
    }

    public Long getRaschsvSvPrimTarif1422Id() {
        return raschsvSvPrimTarif1422Id;
    }
    public void setRaschsvSvPrimTarif1422Id(Long raschsvSvPrimTarif1422Id) {
        this.raschsvSvPrimTarif1422Id = raschsvSvPrimTarif1422Id;
    }

    public Long getRaschsvSvSum1TipId() {
        return raschsvSvSum1TipId;
    }
    public void setRaschsvSvSum1TipId(Long raschsvSvSum1TipId) {
        this.raschsvSvSum1TipId = raschsvSvSum1TipId;
    }
}
