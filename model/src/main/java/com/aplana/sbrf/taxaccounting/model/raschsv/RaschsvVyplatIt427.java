package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Итого выплат
 */
public class RaschsvVyplatIt427 {

    private Long raschsvSvPrimTarif91427Id;

    // Сведения по суммам (тип 1)
    private RaschsvSvSum1Tip raschsvSvSum1Tip;

    public static final String TABLE_NAME = "raschsv_vyplat_it_427";
    public static final String COL_RASCHSV_SV_PRIM_TARIF9_427_ID = "raschsv_sv_prim_tarif9_427_id";
    public static final String COL_RASCHSV_SV_SUM1_TIP_ID = "raschsv_sv_sum1_tip_id";

    public static final String[] COLUMNS = {COL_RASCHSV_SV_PRIM_TARIF9_427_ID, COL_RASCHSV_SV_SUM1_TIP_ID};

    public Long getRaschsvSvPrimTarif91427Id() {
        return raschsvSvPrimTarif91427Id;
    }
    public void setRaschsvSvPrimTarif91427Id(Long raschsvSvPrimTarif91427Id) {
        this.raschsvSvPrimTarif91427Id = raschsvSvPrimTarif91427Id;
    }

    public RaschsvSvSum1Tip getRaschsvSvSum1Tip() {
        return raschsvSvSum1Tip;
    }
    public void setRaschsvSvSum1Tip(RaschsvSvSum1Tip raschsvSvSum1Tip) {
        this.raschsvSvSum1Tip = raschsvSvSum1Tip;
    }
}
