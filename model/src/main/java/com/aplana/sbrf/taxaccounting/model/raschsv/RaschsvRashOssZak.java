package com.aplana.sbrf.taxaccounting.model.raschsv;

import com.aplana.sbrf.taxaccounting.model.IdentityObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации (РасхОССЗак)
 */
public class RaschsvRashOssZak extends IdentityObject<Long> {

    private Long raschsvObyazPlatSvId;

    // Расходы РасхОССЗак
    private List<RaschsvRashOssZakRash> raschsvRashOssZakRashList;

    public RaschsvRashOssZak() {
        super();
        raschsvRashOssZakRashList = new ArrayList<RaschsvRashOssZakRash>();
    }

    public static final String SEQ = "seq_raschsv_rash_oss_zak";
    public static final String TABLE_NAME = "raschsv_rash_oss_zak";
    public static final String COL_ID = "id";
    public static final String COL_RASCHSV_OBYAZ_PLAT_SV_ID = "raschsv_obyaz_plat_sv_id";

    public static final String[] COLUMNS = {COL_ID, COL_RASCHSV_OBYAZ_PLAT_SV_ID};

    public Long getRaschsvObyazPlatSvId() { return raschsvObyazPlatSvId; }
    public void setRaschsvObyazPlatSvId(Long raschsvObyazPlatSvId) { this.raschsvObyazPlatSvId = raschsvObyazPlatSvId; }

    public List<RaschsvRashOssZakRash> getRaschsvRashOssZakRashList() {
        return raschsvRashOssZakRashList;
    }
    public void setRaschsvRashOssZakRashList(List<RaschsvRashOssZakRash> raschsvRashOssZakRashList) {
        this.raschsvRashOssZakRashList = raschsvRashOssZakRashList;
    }
}
