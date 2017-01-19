package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.ArrayList;
import java.util.List;

/**
 * Сведения, необходимые для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425 (абзацем вторым подпункта 2 статьи 426) Налогового кодекса Российской Федерации (СвПримТариф2.2.425)
 */
public class RaschsvSvPrimTarif22425 extends IdentityObject<Long> {

    private Long raschsvObyazPlatSvId;

    // ВыплатИт
    private RaschsvVyplatIt425 raschsvVyplatIt425;

    // СвИноГражд
    private List<RaschsvSvInoGrazd> raschsvSvInoGrazdList;

    public RaschsvSvPrimTarif22425() {
        raschsvSvInoGrazdList = new ArrayList<RaschsvSvInoGrazd>();
    }

    public static final String SEQ = "seq_raschsv_sv_prim_tarif2_425";
    public static final String TABLE_NAME = "raschsv_sv_prim_tarif2_2_425";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_OBYAZ_PLAT_SV_ID = "raschsv_obyaz_plat_sv_id";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_OBYAZ_PLAT_SV_ID};

    public Long getRaschsvObyazPlatSvId() {
        return raschsvObyazPlatSvId;
    }
    public void setRaschsvObyazPlatSvId(Long raschsvObyazPlatSvId) {
        this.raschsvObyazPlatSvId = raschsvObyazPlatSvId;
    }

    public RaschsvVyplatIt425 getRaschsvVyplatIt425() {
        return raschsvVyplatIt425;
    }
    public void setRaschsvVyplatIt425(RaschsvVyplatIt425 raschsvVyplatIt425) {
        this.raschsvVyplatIt425 = raschsvVyplatIt425;
    }

    public List<RaschsvSvInoGrazd> getRaschsvSvInoGrazdList() {
        return raschsvSvInoGrazdList;
    }
    public void setRaschsvSvInoGrazdList(List<RaschsvSvInoGrazd> raschsvSvInoGrazdList) {
        this.raschsvSvInoGrazdList = raschsvSvInoGrazdList;
    }
    public void addRaschsvSvInoGrazd(RaschsvSvInoGrazd raschsvSvInoGrazd) {raschsvSvInoGrazdList.add(raschsvSvInoGrazd);}
}
