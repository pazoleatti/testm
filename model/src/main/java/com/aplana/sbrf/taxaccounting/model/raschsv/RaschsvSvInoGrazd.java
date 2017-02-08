package com.aplana.sbrf.taxaccounting.model.raschsv;

/**
 * Сведения об иностранных гражданах, лицах без гражданства (СвИноГражд)
 */
public class RaschsvSvInoGrazd {

    private Long raschsvSvPrimTarif2425Id;
    private String innfl;
    private String snils;
    private String grazd;
    private String familia;
    private String imya;
    private String otchestvo;
    private Long raschsvSvSum1TipId;

    // СвСум1Тип
    private RaschsvSvSum1Tip raschsvSvSum1Tip;

    public static final String TABLE_NAME = "raschsv_sv_ino_grazd";
    public static final String COL_RASCHSV_SV_PRIM_TARIF2_425_ID = "raschsv_sv_prim_tarif2_425_id";
    public static final String COL_RASCHSV_SV_SUM1_TIP_ID = "raschsv_sv_sum1_tip_id";
    public static final String COL_INNFL = "innfl";
    public static final String COL_SNILS = "snils";
    public static final String COL_GRAZD = "grazd";
    public static final String COL_FAMILIA = "familia";
    public static final String COL_IMYA = "imya";
    public static final String COL_OTCHESTVO = "otchestvo";

    public static final String[] COLUMNS = {COL_RASCHSV_SV_PRIM_TARIF2_425_ID, COL_RASCHSV_SV_SUM1_TIP_ID, COL_INNFL,
            COL_SNILS, COL_GRAZD, COL_FAMILIA, COL_IMYA, COL_OTCHESTVO};

    public Long getRaschsvSvPrimTarif2425Id() {
        return raschsvSvPrimTarif2425Id;
    }
    public void setRaschsvSvPrimTarif2425Id(Long raschsvSvPrimTarif2425Id) {
        this.raschsvSvPrimTarif2425Id = raschsvSvPrimTarif2425Id;
    }

    public String getInnfl() {
        return innfl;
    }
    public void setInnfl(String innfl) {
        this.innfl = innfl;
    }

    public String getSnils() {
        return snils;
    }
    public void setSnils(String snils) {
        this.snils = snils;
    }

    public String getGrazd() {
        return grazd;
    }
    public void setGrazd(String grazd) {
        this.grazd = grazd;
    }

    public String getFamilia() {
        return familia;
    }
    public void setFamilia(String familia) {
        this.familia = familia;
    }

    public String getImya() {
        return imya;
    }
    public void setImya(String imya) {
        this.imya = imya;
    }

    public String getOtchestvo() {
        return otchestvo;
    }
    public void setOtchestvo(String otchestvo) {
        this.otchestvo = otchestvo;
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
