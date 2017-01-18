package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Выплаты, произведенные за счет средств, финансируемых из федерального бюджета (ВыплФинФБ)
 */
public class RaschsvVyplFinFb extends IdentityObject<Long> {

    private Long raschsvObyazPlatSvId;

    // Причина ВыплФинФБ
    private List<RaschsvVyplPrichina> raschsvVyplPrichinaList;

    public RaschsvVyplFinFb() {
        super();
        raschsvVyplPrichinaList = new ArrayList<RaschsvVyplPrichina>();
    }

    public static final String SEQ = "seq_raschsv_vypl_fin_fb";
    public static final String TABLE_NAME = "raschsv_vypl_fin_fb";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_OBYAZ_PLAT_SV_ID = "raschsv_obyaz_plat_sv_id";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_OBYAZ_PLAT_SV_ID};

    public Long getRaschsvObyazPlatSvId() { return raschsvObyazPlatSvId; }
    public void setRaschsvObyazPlatSvId(Long raschsvObyazPlatSvId) { this.raschsvObyazPlatSvId = raschsvObyazPlatSvId; }

    public List<RaschsvVyplPrichina> getRaschsvVyplPrichinaList() {
        return raschsvVyplPrichinaList;
    }
    public void setRaschsvVyplPrichinaList(List<RaschsvVyplPrichina> raschsvVyplPrichinaList) {
        this.raschsvVyplPrichinaList = raschsvVyplPrichinaList;
    }
}
