package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Основание выплат, произведенных за счет средств, финансируемых из федерального бюджета
 */
public class RaschsvVyplPrichina extends IdentityObject<Long> {

    private Long raschsvVyplFinFbId;
    private String nodeName;
    private Double svVnfUhodInv;

    // Выплаты, произведенные за счет средств, финансируемых из Федерального бюджета
    private List<RaschsvRashVypl> raschsvRashVyplList;

    public RaschsvVyplPrichina() {
        super();
        raschsvRashVyplList = new ArrayList<RaschsvRashVypl>();
    }

    public static final String SEQ = "seq_raschsv_vypl_prichina";
    public static final String TABLE_NAME = "raschsv_vypl_prichina";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_VYPL_FIN_FB_ID = "raschsv_vypl_fin_fb_id";
    public static final String COL_NODE_NAME = "node_name";
    public static final String COL_SV_VNF_UHOD_INV = "sv_vnf_uhod_inv";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_VYPL_FIN_FB_ID, COL_NODE_NAME, COL_SV_VNF_UHOD_INV};

    public Long getRaschsvVyplFinFbId() {
        return raschsvVyplFinFbId;
    }
    public void setRaschsvVyplFinFbId(Long raschsvVyplFinFbId) {
        this.raschsvVyplFinFbId = raschsvVyplFinFbId;
    }

    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Double getSvVnfUhodInv() {
        return svVnfUhodInv;
    }
    public void setSvVnfUhodInv(Double svVnfUhodInv) {
        this.svVnfUhodInv = svVnfUhodInv;
    }

    public List<RaschsvRashVypl> getRaschsvRashVyplList() {
        return raschsvRashVyplList;
    }
    public void setRaschsvRashVyplList(List<RaschsvRashVypl> raschsvRashVyplList) {
        this.raschsvRashVyplList = raschsvRashVyplList;
    }
}
