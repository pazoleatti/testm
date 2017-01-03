package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Сведения по количеству физических лиц
 */
public class RaschsvKolLicTip extends IdentityObject<Long> {

    private Integer kolVsegoPer;
    private Integer kolVsegoPosl3m;
    private Integer kol1mPosl3m;
    private Integer kol2mPosl3m;
    private Integer kol3mPosl3m;

    public static final String SEQ = "seq_raschsv_kol_lic_tip";
    public static final String TABLE_NAME = "raschsv_kol_lic_tip";
    public static final String COL_ID = "id";
    public static final String COL_KOL_VSEGO_PER = "kol_vsego_per";
    public static final String COL_KOL_VSEGO_POSL_3M = "kol_vsego_posl_3m";
    public static final String COL_KOL_1M_POSL_3M = "kol_1m_posl_3m";
    public static final String COL_KOL_2M_POSL_3M = "kol_2m_posl_3m";
    public static final String COL_KOL_3M_POSL_3M = "kol_3m_posl_3m";

    public static final String[] COLUMNS = {COL_ID, COL_KOL_VSEGO_PER, COL_KOL_VSEGO_POSL_3M, COL_KOL_1M_POSL_3M,
            COL_KOL_2M_POSL_3M, COL_KOL_3M_POSL_3M};

    public Integer getKolVsegoPer() { return kolVsegoPer; }
    public void setKolVsegoPer(Integer kolVsegoPer) { this.kolVsegoPer = kolVsegoPer; }

    public Integer getKolVsegoPosl3m() { return kolVsegoPosl3m; }
    public void setKolVsegoPosl3m(Integer kolVsegoPosl3m) { this.kolVsegoPosl3m = kolVsegoPosl3m; }

    public Integer getKol1mPosl3m() { return kol1mPosl3m; }
    public void setKol1mPosl3m(Integer kol1mPosl3m) { this.kol1mPosl3m = kol1mPosl3m; }

    public Integer getKol2mPosl3m() { return kol2mPosl3m; }
    public void setKol2mPosl3m(Integer kol2mPosl3m) { this.kol2mPosl3m = kol2mPosl3m; }

    public Integer getKol3mPosl3m() { return kol3mPosl3m; }
    public void setKol3mPosl3m(Integer kol3mPosl3m) { this.kol3mPosl3m = kol3mPosl3m; }
}
