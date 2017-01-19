package com.aplana.sbrf.taxaccounting.model.raschsv;

/**
 * Итого выплат (ВыплатИт)
 */
public class RaschsvVyplatIt425 {

    private Long raschsvSvPrimTarif22425Id;
    private Long raschsvSvSum1TipId;

    // СвСум1Тип
    private RaschsvSvSum1Tip raschsvSvSum1Tip;

    public static final String TABLE_NAME = "raschsv_vyplat_it_425";
    public static final String COL_RASCHSV_SV_PRIM_TARIF2_425_ID = "raschsv_sv_prim_tarif2_425_id";
    public static final String COL_RASCHSV_SV_SUM1_TIP_ID = "raschsv_sv_sum1_tip_id";

    public static final String[] COLUMNS = {COL_RASCHSV_SV_PRIM_TARIF2_425_ID, COL_RASCHSV_SV_SUM1_TIP_ID};

    public Long getRaschsvSvPrimTarif22425Id() {
        return raschsvSvPrimTarif22425Id;
    }
    public void setRaschsvSvPrimTarif22425Id(Long raschsvSvPrimTarif22425Id) {
        this.raschsvSvPrimTarif22425Id = raschsvSvPrimTarif22425Id;
    }

    public RaschsvSvSum1Tip getRaschsvSvSum1Tip() {
        return raschsvSvSum1Tip;
    }
    public void setRaschsvSvSum1Tip(RaschsvSvSum1Tip raschsvSvSum1Tip) {
        this.raschsvSvSum1Tip = raschsvSvSum1Tip;
    }

    public Long getRaschsvSvSum1TipId() {
        return raschsvSvSum1TipId;
    }
    public void setRaschsvSvSum1TipId(Long raschsvSvSum1TipId) {
        this.raschsvSvSum1TipId = raschsvSvSum1TipId;
    }
}
