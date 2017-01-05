package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Сведения, необходимые для применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 9 пункта 1 статьи 427
 */
public class RaschsvSvPrimTarif91427 extends IdentityObject<Long> {

    private Long raschsvObyazPlatSvId;

    // Итого выплат
    private RaschsvVyplatIt427 raschsvVyplatIt427;

    // Сведения о патенте
    private List<RaschsvSvedPatent> raschsvSvedPatentList;

    public RaschsvSvPrimTarif91427() {
        raschsvSvedPatentList = new ArrayList<RaschsvSvedPatent>();
    }

    public static final String SEQ = "seq_raschsv_sv_prim_tarif9_427";
    public static final String TABLE_NAME = "raschsv_sv_prim_tarif9_1_427";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_OBYAZ_PLAT_SV_ID = "raschsv_obyaz_plat_sv_id";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_OBYAZ_PLAT_SV_ID};

    public Long getRaschsvObyazPlatSvId() {
        return raschsvObyazPlatSvId;
    }
    public void setRaschsvObyazPlatSvId(Long raschsvObyazPlatSvId) {
        this.raschsvObyazPlatSvId = raschsvObyazPlatSvId;
    }

    public RaschsvVyplatIt427 getRaschsvVyplatIt427() {
        return raschsvVyplatIt427;
    }
    public void setRaschsvVyplatIt427(RaschsvVyplatIt427 raschsvVyplatIt427) {
        this.raschsvVyplatIt427 = raschsvVyplatIt427;
    }

    public List<RaschsvSvedPatent> getRaschsvSvedPatentList() {
        return raschsvSvedPatentList;
    }
    public void setRaschsvSvedPatentList(List<RaschsvSvedPatent> raschsvSvedPatentList) {
        this.raschsvSvedPatentList = raschsvSvedPatentList;
    }
}
