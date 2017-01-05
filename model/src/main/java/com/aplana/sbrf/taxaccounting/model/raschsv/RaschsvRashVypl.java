package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

/**
 * Информация о выплате, произведенной за счет средств, финансируемых из Федерального бюджета
 */
public class RaschsvRashVypl extends IdentityObject<Long> {

    private Long raschsvVyplPrichinaId;
    private String nodeName;
    private Integer chislPoluch;
    private Integer kolVypl;
    private Double rashod;

    public static final String SEQ = "seq_raschsv_rash_vypl";
    public static final String TABLE_NAME = "raschsv_rash_vypl";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_VYPL_PRICHINA_ID = "raschsv_vypl_prichina_id";
    public static final String COL_NODE_NAME = "node_name";
    public static final String COL_CHISL_POLUCH = "chisl_poluch";
    public static final String COL_KOL_VYPL = "kol_vypl";
    public static final String COL_RASHOD = "rashod";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_VYPL_PRICHINA_ID, COL_NODE_NAME, COL_CHISL_POLUCH,
            COL_KOL_VYPL, COL_RASHOD};

    public Long getRaschsvVyplPrichinaId() {
        return raschsvVyplPrichinaId;
    }
    public void setRaschsvVyplPrichinaId(Long raschsvVyplPrichinaId) {
        this.raschsvVyplPrichinaId = raschsvVyplPrichinaId;
    }

    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getChislPoluch() {
        return chislPoluch;
    }
    public void setChislPoluch(Integer chislPoluch) {
        this.chislPoluch = chislPoluch;
    }

    public Integer getKolVypl() {
        return kolVypl;
    }
    public void setKolVypl(Integer kolVypl) {
        this.kolVypl = kolVypl;
    }

    public Double getRashod() {
        return rashod;
    }
    public void setRashod(Double rashod) {
        this.rashod = rashod;
    }
}
