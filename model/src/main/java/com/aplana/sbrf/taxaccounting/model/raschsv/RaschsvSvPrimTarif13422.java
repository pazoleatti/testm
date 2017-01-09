package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Сведения об обучающихся, необходимые для применения положений подпункта 1 пункта 3 статьи 422
 */
public class RaschsvSvPrimTarif13422 extends IdentityObject<Long> {

    private Long raschsvObyazPlatSvId;

    // Итого выплат
    private RaschsvVyplatIt422 raschsvVyplatIt422;

    // Сведения об обучающихся
    private List<RaschsvSvedObuch> raschsvSvedObuchList;

    public RaschsvSvPrimTarif13422() {
        super();
        raschsvSvedObuchList = new ArrayList<RaschsvSvedObuch>();
    }

    public static final String SEQ = "seq_raschsv_sv_prim_tarif1_422";
    public static final String TABLE_NAME = "raschsv_sv_prim_tarif1_3_422";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_OBYAZ_PLAT_SV_ID = "raschsv_obyaz_plat_sv_id";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_OBYAZ_PLAT_SV_ID};

    public Long getRaschsvObyazPlatSvId() {
        return raschsvObyazPlatSvId;
    }
    public void setRaschsvObyazPlatSvId(Long raschsvObyazPlatSvId) {
        this.raschsvObyazPlatSvId = raschsvObyazPlatSvId;
    }

    public RaschsvVyplatIt422 getRaschsvVyplatIt422() {
        return raschsvVyplatIt422;
    }
    public void setRaschsvVyplatIt422(RaschsvVyplatIt422 raschsvVyplatIt422) {
        this.raschsvVyplatIt422 = raschsvVyplatIt422;
    }

    public List<RaschsvSvedObuch> getRaschsvSvedObuchList() {
        return raschsvSvedObuchList;
    }
    public void setRaschsvSvedObuchList(List<RaschsvSvedObuch> raschsvSvedObuchList) {
        this.raschsvSvedObuchList = raschsvSvedObuchList;
    }
}
