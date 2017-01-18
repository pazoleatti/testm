package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Расходы РасхОССЗак
 */
public class RaschsvRashOssZakRash extends IdentityObject<Long> {

    private Long raschsvRashOssZakId;
    private String nodeName;
    private Integer chislSluch;
    private Integer kolVypl;
    private Double pashVsego;
    private Double rashFinFb;

    public static final String SEQ = "seq_raschsv_rash_oss_zak_rash";
    public static final String TABLE_NAME = "raschsv_rash_oss_zak_rash";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_RASH_OSS_ZAK_ID = "raschsv_rash_oss_zak_id";
    public static final String COL_NODE_NAME = "node_name";
    public static final String COL_CHISL_SLUCH = "chisl_sluch";
    public static final String COL_KOL_VYPL = "kol_vypl";
    public static final String COL_PASH_VSEGO = "pash_vsego";
    public static final String COL_RASH_FIN_FB = "rash_fin_fb";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_RASH_OSS_ZAK_ID, COL_NODE_NAME, COL_CHISL_SLUCH,
            COL_KOL_VYPL, COL_PASH_VSEGO, COL_RASH_FIN_FB};

    public Long getRaschsvRashOssZakId() {
        return raschsvRashOssZakId;
    }
    public void setRaschsvRashOssZakId(Long raschsvRashOssZakId) {
        this.raschsvRashOssZakId = raschsvRashOssZakId;
    }

    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getChislSluch() {
        return chislSluch;
    }
    public void setChislSluch(Integer chislSluch) {
        this.chislSluch = chislSluch;
    }

    public Integer getKolVypl() {
        return kolVypl;
    }
    public void setKolVypl(Integer kolVypl) {
        this.kolVypl = kolVypl;
    }

    public Double getPashVsego() {
        return pashVsego;
    }
    public void setPashVsego(Double pashVsego) {
        this.pashVsego = pashVsego;
    }

    public Double getRashFinFb() {
        return rashFinFb;
    }
    public void setRashFinFb(Double rashFinFb) {
        this.rashFinFb = rashFinFb;
    }
}
