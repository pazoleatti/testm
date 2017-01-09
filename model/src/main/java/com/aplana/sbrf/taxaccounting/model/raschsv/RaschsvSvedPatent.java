package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.Date;

/**
 * Сведения о патенте
 */
public class RaschsvSvedPatent {

    private Long raschsvSvPrimTarif91427Id;
    private String nomPatent;
    private String vydDeyatPatent;
    private Date dataNachDeyst;
    private Date dataKonDeyst;

    // Сведения по суммам (тип 1)
    private RaschsvSvSum1Tip raschsvSvSum1Tip;

    public static final String TABLE_NAME = "raschsv_sved_patent";
    public static final String COL_RASCHSV_SV_PRIM_TARIF9_427_ID = "raschsv_sv_prim_tarif9_427_id";
    public static final String COL_RASCHSV_SV_SUM1_TIP_ID = "raschsv_sv_sum1_tip_id";
    public static final String COL_NOM_PATENT = "nom_patent";
    public static final String COL_VYD_DEYAT_PATENT = "vyd_deyat_patent";
    public static final String COL_DATA_NACH_DEYST = "data_nach_deyst";
    public static final String COL_DATA_KON_DEYST = "data_kon_deyst";

    public static final String[] COLUMNS = {COL_RASCHSV_SV_PRIM_TARIF9_427_ID, COL_RASCHSV_SV_SUM1_TIP_ID, COL_NOM_PATENT,
            COL_VYD_DEYAT_PATENT, COL_DATA_NACH_DEYST, COL_DATA_KON_DEYST};

    public Long getRaschsvSvPrimTarif91427Id() {
        return raschsvSvPrimTarif91427Id;
    }
    public void setRaschsvSvPrimTarif91427Id(Long raschsvSvPrimTarif91427Id) {
        this.raschsvSvPrimTarif91427Id = raschsvSvPrimTarif91427Id;
    }

    public String getNomPatent() {
        return nomPatent;
    }
    public void setNomPatent(String nomPatent) {
        this.nomPatent = nomPatent;
    }

    public String getVydDeyatPatent() {
        return vydDeyatPatent;
    }
    public void setVydDeyatPatent(String vydDeyatPatent) {
        this.vydDeyatPatent = vydDeyatPatent;
    }

    public Date getDataNachDeyst() {
        return dataNachDeyst;
    }
    public void setDataNachDeyst(Date dataNachDeyst) {
        this.dataNachDeyst = dataNachDeyst;
    }

    public Date getDataKonDeyst() {
        return dataKonDeyst;
    }
    public void setDataKonDeyst(Date dataKonDeyst) {
        this.dataKonDeyst = dataKonDeyst;
    }

    public RaschsvSvSum1Tip getRaschsvSvSum1Tip() {
        return raschsvSvSum1Tip;
    }
    public void setRaschsvSvSum1Tip(RaschsvSvSum1Tip raschsvSvSum1Tip) {
        this.raschsvSvSum1Tip = raschsvSvSum1Tip;
    }
}
