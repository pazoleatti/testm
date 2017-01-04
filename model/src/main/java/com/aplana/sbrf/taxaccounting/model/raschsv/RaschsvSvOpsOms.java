package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование
 */
public class RaschsvSvOpsOms extends IdentityObject<Long> {

    private Long raschsvObyazPlatSvId;
    private String tarifPlat;

    // Вид расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование
    private List<RaschsvSvOpsOmsRasch> raschsvSvOpsOmsRaschList;

    public RaschsvSvOpsOms() {
        super();
        raschsvSvOpsOmsRaschList = new ArrayList<RaschsvSvOpsOmsRasch>();
    }

    public static final String SEQ = "seq_raschsv_sv_ops_oms";
    public static final String TABLE_NAME = "raschsv_sv_ops_oms";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_OBYAZ_PLAT_SV_ID = "raschsv_obyaz_plat_sv_id";
    public static final String COL_TARIF_PLAT = "tarif_plat";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_OBYAZ_PLAT_SV_ID, COL_TARIF_PLAT};

    public Long getRaschsvObyazPlatSvId() { return raschsvObyazPlatSvId; }
    public void setRaschsvObyazPlatSvId(Long raschsvObyazPlatSvId) { this.raschsvObyazPlatSvId = raschsvObyazPlatSvId; }

    public String getTarifPlat() { return tarifPlat; }
    public void setTarifPlat(String tarifPlat) { this.tarifPlat = tarifPlat; }

    public List<RaschsvSvOpsOmsRasch> getRaschsvSvOpsOmsRaschList() { return raschsvSvOpsOmsRaschList; }
    public void setRaschsvSvOpsOmsRaschList(List<RaschsvSvOpsOmsRasch> raschsvSvOpsOmsRaschList) {
        this.raschsvSvOpsOmsRaschList = raschsvSvOpsOmsRaschList;
    }
}
